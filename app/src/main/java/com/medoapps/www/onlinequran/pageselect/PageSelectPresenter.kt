package com.medoapps.www.onlinequran.pageselect

import android.content.Context
import androidx.preference.PreferenceManager
import com.quran.data.core.QuranInfo
import com.quran.data.dao.BookmarksDao
import com.quran.data.source.PageProvider
import com.medoapps.www.onlinequran.model.bookmark.BookmarkModel
import com.medoapps.www.onlinequran.presenter.Presenter
import com.medoapps.www.onlinequran.ui.PagerActivity
import com.medoapps.www.onlinequran.util.ImageUtil
import com.medoapps.www.onlinequran.util.QuranFileUtils
import com.medoapps.www.onlinequran.util.UrlUtil
import dagger.Reusable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

@Reusable
class PageSelectPresenter @Inject
constructor(
  private val appContext: Context,
  private val imageUtil: ImageUtil,
  private val quranFileUtils: QuranFileUtils,
  private val mainThreadScheduler: Scheduler,
  private val urlUtil: UrlUtil,
  private val bookmarksDao: BookmarksDao,
  // unfortunately needed for now due to the old Rx code
  // not knowing about changes from BookmarksDao, etc.
  private val bookmarkModel: BookmarkModel,
  private val pageTypes:
  Map<@JvmSuppressWildcards String, @JvmSuppressWildcards PageProvider>
) :
  Presenter<PageSelectActivity> {
  private val baseUrl = "https://android.quran.com/data/pagetypes/snips"
  private val compositeDisposable = CompositeDisposable()
  private val downloadingSet = mutableSetOf<String>()
  private var currentView: PageSelectActivity? = null

  private fun generateData() {
    val base = quranFileUtils.quranBaseDirectory
    if (base != null) {
      val outputPath = File(File(base, "pagetypes"), "snips")
      if (!outputPath.exists()) {
        outputPath.mkdirs()
        File(outputPath, ".nomedia").createNewFile()
      }

      val data = pageTypes.map {
        val provider = it.value
        val previewImage = File(outputPath, "${it.key}.png")
        val downloadedImage = if (previewImage.exists()) {
          previewImage
        } else if (!downloadingSet.contains(it.key)) {
          downloadingSet.add(it.key)
          val url = "$baseUrl/${it.key}.png"
          compositeDisposable.add(
            // some page sets have no snip on the server - those ship in assets
            Maybe.fromCallable<File> {
              if (copySnipFromAssets(it.key, previewImage)) previewImage else null
            }
              .switchIfEmpty(
                imageUtil.downloadImage(url, previewImage)
                  .onErrorResumeWith(
                    imageUtil.downloadImage(urlUtil.fallbackUrl(url), previewImage)
                  )
              )
              .subscribeOn(Schedulers.io())
              .observeOn(mainThreadScheduler)
              .subscribe({ generateData() }, { e -> Timber.e(e) })
          )
          null
        } else {
          // already downloading
          null
        }
        PageTypeItem(
          it.key,
          downloadedImage,
          provider.getPreviewTitle(),
          provider.getPreviewDescription()
        )
      }
      currentView?.onUpdatedData(data)
    }
  }

  private fun copySnipFromAssets(key: String, destination: File): Boolean {
    // stage to a temp file so an interrupted copy can't leave a truncated
    // snip that previewImage.exists() would then treat as valid forever
    val stagingFile = File(destination.path + ".tmp")
    return try {
      appContext.assets.open("pagetypes/$key.png").use { input ->
        stagingFile.outputStream().use { output -> input.copyTo(output) }
      }
      stagingFile.renameTo(destination)
    } catch (ioException: IOException) {
      stagingFile.delete()
      false
    }
  }

  /**
   * Migrate bookmark and recent page data between two page types
   * Consider a page set like madani (604 pages) versus one like Shemerly (521 pages).
   * When switching between them, bookmarks need to be mapped so that the same bookmark
   * retains its meaning.
   *
   * Note that this does not support non-Hafs qira'at yet, where the ayah numbers may
   * have changed due to kufi versus madani counting.
   */
  suspend fun migrateBookmarksData(sourcePageType: String, destinationPageType: String) {
    val source = pageTypes[sourcePageType]?.getDataSource()
    val destination = pageTypes[destinationPageType]?.getDataSource()
    // compare layouts, not page counts: new madani also has 604 pages yet
    // breaks 25 of them differently from madani, and qaloon/warsh differ
    // from it on page 592 - only layout-identical sets (madani/tajweed)
    // can skip the migration
    if (source != null && destination != null &&
      (!source.suraForPageArray.contentEquals(destination.suraForPageArray) ||
        !source.ayahForPageArray.contentEquals(destination.ayahForPageArray))
    ) {
      val sourcePageSuraStart = source.suraForPageArray
      val sourcePageAyahStart = source.ayahForPageArray
      val destinationQuranInfo = QuranInfo(destination)

      val suraAyahFromPage = { page: Int ->
        sourcePageSuraStart[page - 1] to sourcePageAyahStart[page - 1]
      }

      // update the bookmarks
      val updatedBookmarks = bookmarksDao.bookmarks()
        .map {
          val page = it.page
          val (pageSura, pageAyah) = suraAyahFromPage(page)
          val sura = it.sura ?: pageSura
          val ayah = it.ayah ?: pageAyah

          val mappedPage = destinationQuranInfo.getPageFromSuraAyah(sura, ayah)

          // we only copy the page because sura and ayah are the same.
          it.copy(page = mappedPage)
        }

      if (updatedBookmarks.isNotEmpty()) {
        bookmarksDao.replaceBookmarks(updatedBookmarks)
        bookmarkModel.notifyBookmarksUpdated()
      }

      // and update the recents
      val updatedRecentPages = bookmarksDao.recentPages()
        .sortedByDescending { it.timestamp }
        .map {
          val page = it.page
          val (pageSura, pageAyah) = suraAyahFromPage(page)

          val mappedPage = destinationQuranInfo.getPageFromSuraAyah(pageSura, pageAyah)
          it.copy(page = mappedPage)
        }

      if (updatedRecentPages.isNotEmpty()) {
        bookmarksDao.removeRecentPages()
        bookmarksDao.replaceRecentPages(updatedRecentPages)
        bookmarkModel.notifyRecentPagesUpdated(updatedRecentPages.first().page)
      }

      // and the home continue-reading card, which reads its own prefs
      val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
      val homeLastRead = prefs.getInt(PagerActivity.HOME_LAST_READ_PAGE, 0)
      if (homeLastRead in 1..sourcePageSuraStart.size) {
        val (sura, ayah) = suraAyahFromPage(homeLastRead)
        prefs.edit()
          .putInt(
            PagerActivity.HOME_LAST_READ_PAGE,
            destinationQuranInfo.getPageFromSuraAyah(sura, ayah)
          )
          .putInt(PagerActivity.HOME_LAST_READ_TOTAL, destination.numberOfPages)
          .apply()
      }
    }
  }

  override fun bind(what: PageSelectActivity) {
    currentView = what
    generateData()
  }

  override fun unbind(what: PageSelectActivity) {
    if (currentView === what) {
      currentView = null
      compositeDisposable.clear()
      downloadingSet.clear()
    }
  }
}
