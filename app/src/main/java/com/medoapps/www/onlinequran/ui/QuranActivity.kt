package com.medoapps.www.onlinequran.ui

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.medoapps.www.onlinequran.R
import com.medoapps.www.onlinequran.AboutUsActivity
import com.medoapps.www.onlinequran.HelpActivity
import com.medoapps.www.onlinequran.QuranApplication
import com.medoapps.www.onlinequran.QuranPreferenceActivity
import com.medoapps.www.onlinequran.SearchActivity
import com.medoapps.www.onlinequran.ShortcutsActivity
import com.medoapps.www.onlinequran.data.Constants
import com.medoapps.www.onlinequran.model.bookmark.RecentPageModel
import com.medoapps.www.onlinequran.presenter.data.QuranIndexEventLogger
import com.medoapps.www.onlinequran.presenter.translation.TranslationManagerPresenter
import com.medoapps.www.onlinequran.service.AudioService
import com.medoapps.www.onlinequran.ui.fragment.AddTagDialog
import com.medoapps.www.onlinequran.ui.fragment.AddTagDialog.Companion.newInstance
import com.medoapps.www.onlinequran.ui.fragment.BookmarksFragment
import com.medoapps.www.onlinequran.ui.fragment.JumpFragment
import com.medoapps.www.onlinequran.ui.fragment.JuzListFragment
import com.medoapps.www.onlinequran.ui.fragment.SuraListFragment
import com.medoapps.www.onlinequran.ui.fragment.TagBookmarkDialog
import com.medoapps.www.onlinequran.ui.fragment.TagBookmarkDialog.OnBookmarkTagsUpdateListener
import com.medoapps.www.onlinequran.ui.helpers.JumpDestination
import com.medoapps.www.onlinequran.util.AppBottomSheet
import com.medoapps.www.onlinequran.util.AudioUtils
import com.medoapps.www.onlinequran.util.QuranSettings
import com.medoapps.www.onlinequran.util.QuranUtils
import com.medoapps.www.onlinequran.view.SlidingTabLayout
import com.quran.mobile.di.ExtraScreenProvider
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject
import kotlin.math.abs

/**
 * The home screen activity for the app. Displays a toolbar and 3 fragments:
 *
 *  * [SuraListFragment]
 *  * [JuzListFragment]
 *  * [BookmarksFragment]
 *
 * When this activity is created, it may run a background check to see if updated translations
 * are available, and if so, show a dialog asking the user if they want to download them.
 *
 * This activity is called from several places:
 *  * [com.medoapps.www.onlinequran.QuranDataActivity]
 *  * [ShortcutsActivity]
 */
class QuranActivity : AppCompatActivity(),
    OnBookmarkTagsUpdateListener,
    JumpDestination {
  private var upgradeDialog: Dialog? = null
  private var showedTranslationUpgradeDialog = false
  private var isRtl = false
  private var isPaused = false
  private var searchItem: MenuItem? = null
  private var supportActionMode: ActionMode? = null
  private val compositeDisposable = CompositeDisposable()
  lateinit var latestPageObservable: Observable<Int>

  @Inject
  lateinit var settings: QuranSettings
  @Inject
  lateinit var audioUtils: AudioUtils
  @Inject
  lateinit var recentPageModel: RecentPageModel
  @Inject
  lateinit var translationManagerPresenter: TranslationManagerPresenter
  @Inject
  lateinit var quranIndexEventLogger: QuranIndexEventLogger
  @Inject
  lateinit var extraScreens: Set<@JvmSuppressWildcards ExtraScreenProvider>

  public override fun onCreate(savedInstanceState: Bundle?) {
    val quranApp = application as QuranApplication
    quranApp.refreshLocale(this, false)

    super.onCreate(savedInstanceState)
    quranApp.applicationComponent
        .quranActivityComponentBuilder()
        .build()
        .inject(this)

    setContentView(R.layout.quran_index)

    // Match status bar and nav bar to the toolbar background
    val headerColor = ContextCompat.getColor(this, R.color.mushaf_header_background)
    window.statusBarColor = headerColor
    window.navigationBarColor = headerColor
    val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = !isNightMode

    isRtl = isRtl()

    val tb = findViewById<Toolbar>(R.id.toolbar)
    setSupportActionBar(tb)
    val ab = supportActionBar
    ab?.setTitle(R.string.app_name)

    val pager = findViewById<ViewPager>(R.id.index_pager)
    pager.offscreenPageLimit = 3
    val pagerAdapter = PagerAdapter(supportFragmentManager)
    pager.adapter = pagerAdapter
    val indicator = findViewById<SlidingTabLayout>(R.id.indicator)
    indicator.setViewPager(pager)
    if (isRtl) {
      pager.currentItem = TITLES.size - 1
    }

    if (savedInstanceState != null) {
      showedTranslationUpgradeDialog = savedInstanceState.getBoolean(
          SI_SHOWED_UPGRADE_DIALOG, false
      )
    }

    latestPageObservable = recentPageModel.getLatestPageObservable()
    val intent = intent
    if (intent != null) {
      val extras = intent.extras
      if (extras != null) {
        if (extras.getBoolean(EXTRA_SHOW_TRANSLATION_UPGRADE, false)) {
          if (!showedTranslationUpgradeDialog) {
            showTranslationsUpgradeDialog()
          }
        }
      }
      if (ShortcutsActivity.ACTION_JUMP_TO_LATEST == intent.action) {
        jumpToLastPage()
      }
    }
    updateTranslationsListAsNeeded()
    quranIndexEventLogger.logAnalytics()
  }

  public override fun onResume() {
    compositeDisposable.add(latestPageObservable.subscribe())
    super.onResume()
    val isRtl = isRtl()
    if (isRtl != this.isRtl) {
      val i = intent
      finish()
      startActivity(i)
    } else {
      compositeDisposable.add(
          Completable.timer(500, MILLISECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe {
                startService(
                    audioUtils.getAudioIntent(this@QuranActivity, AudioService.ACTION_STOP)
                )
              }
      )
    }
    isPaused = false
  }

  override fun onPause() {
    compositeDisposable.clear()
    isPaused = true
    super.onPause()
  }

  private fun isRtl(): Boolean {
    return settings.isArabicNames || QuranUtils.isRtl()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    super.onCreateOptionsMenu(menu)
    val inflater = menuInflater
    inflater.inflate(R.menu.home_menu, menu)
    searchItem = menu.findItem(R.id.search)
    val searchView = searchItem?.actionView as SearchView
    val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
    searchView.queryHint = getString(R.string.search_hint)
    searchView.setSearchableInfo(
        searchManager.getSearchableInfo(
            ComponentName(this, SearchActivity::class.java)
        )
    )

    // Add additional injected screens (if any)
    extraScreens
      .sortedBy { it.order }
      .forEach { menu.add(Menu.NONE, it.id, Menu.NONE, it.titleResId) }

    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (val itemId = item.itemId) {
      R.id.settings -> {
        startActivity(Intent(this, QuranPreferenceActivity::class.java))
      }
      R.id.last_page -> {
        jumpToLastPage()
      }
      R.id.help -> {
        startActivity(Intent(this, HelpActivity::class.java))
      }
      R.id.about -> {
        startActivity(Intent(this, AboutUsActivity::class.java))
      }
      R.id.jump -> {
        gotoPageDialog()
      }
      /*R.id.other_apps -> {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://search?q=pub:quran.com")
        if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
          intent.data = Uri.parse("https://play.google.com/store/search?q=pub:quran.com")
        }
        startActivity(intent)
      }*/
      else -> {
        val handled = extraScreens.firstOrNull { it.id == itemId }?.onClick(this) ?: false
        return handled || super.onOptionsItemSelected(item)
      }
    }
    return true
  }

  override fun onSupportActionModeFinished(mode: ActionMode) {
    supportActionMode = null
    super.onSupportActionModeFinished(mode)
  }

  override fun onSupportActionModeStarted(mode: ActionMode) {
    supportActionMode = mode
    super.onSupportActionModeStarted(mode)
  }

  override fun onBackPressed() {
    val searchItem = searchItem
    val supportActionMode = supportActionMode

    if (supportActionMode != null) {
      supportActionMode.finish()
    } else if (searchItem != null && searchItem.isActionViewExpanded) {
      searchItem.collapseActionView()
    } else {
      super.onBackPressed()
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(
        SI_SHOWED_UPGRADE_DIALOG,
        showedTranslationUpgradeDialog
    )
    super.onSaveInstanceState(outState)
  }

  private fun jumpToLastPage() {
    compositeDisposable.add(
        latestPageObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { recentPage: Int ->
              jumpTo(
                  if (recentPage == Constants.NO_PAGE) 1 else recentPage
              )
            }
    )
  }

  private fun updateTranslationsListAsNeeded() {
    if (!updatedTranslations) {
      val time = settings.lastUpdatedTranslationDate
      Timber.d("checking whether we should update translations..")
      if (System.currentTimeMillis() - time > Constants.TRANSLATION_REFRESH_TIME) {
        Timber.d("updating translations list...")
        updatedTranslations = true
        translationManagerPresenter.checkForUpdates()
      }
    }
  }

  private fun showTranslationsUpgradeDialog() {
    showedTranslationUpgradeDialog = true

    upgradeDialog = AppBottomSheet.showConfirmation(
        this,
        "",
        getString(R.string.translation_updates_available),
        getString(R.string.translation_dialog_yes),
        getString(R.string.translation_dialog_later),
        {
          upgradeDialog = null
          launchTranslationActivity()
        },
        {
          upgradeDialog = null
          settings.setHaveUpdatedTranslations(false)
        }
    )
  }

  private fun launchTranslationActivity() {
    val i = Intent(this, TranslationManagerActivity::class.java)
    startActivity(i)
  }

  override fun jumpTo(page: Int) {
    val i = Intent(this, PagerActivity::class.java)
    i.putExtra("page", page)
    i.putExtra(PagerActivity.EXTRA_JUMP_TO_TRANSLATION, settings.wasShowingTranslation)
    // Forward the Home "read + listen" auto-play flag ONCE: consume it from this
    // long-lived index activity so a later sura/juz tap doesn't re-trigger playback.
    if (intent.getBooleanExtra(PagerActivity.EXTRA_AUTO_PLAY, false)) {
      i.putExtra(PagerActivity.EXTRA_AUTO_PLAY, true)
      intent.removeExtra(PagerActivity.EXTRA_AUTO_PLAY)
    }
    startActivity(i)
  }

  override fun jumpToAndHighlight(page: Int, sura: Int, ayah: Int) {
    val i = Intent(this, PagerActivity::class.java)
    i.putExtra("page", page)
    i.putExtra(PagerActivity.EXTRA_HIGHLIGHT_SURA, sura)
    i.putExtra(PagerActivity.EXTRA_HIGHLIGHT_AYAH, ayah)
    startActivity(i)
  }

  private fun gotoPageDialog() {
    if (!isPaused) {
      val fm = supportFragmentManager
      val jumpDialog = JumpFragment()
      jumpDialog.show(fm, JumpFragment.TAG)
    }
  }

  fun addTag() {
    if (!isPaused) {
      val fm = supportFragmentManager
      val addTagDialog = AddTagDialog()
      addTagDialog.show(fm, AddTagDialog.TAG)
    }
  }

  fun editTag(id: Long, name: String?) {
    if (!isPaused) {
      val fm = supportFragmentManager
      val addTagDialog = newInstance(id, name!!)
      addTagDialog.show(fm, AddTagDialog.TAG)
    }
  }

  fun tagBookmarks(ids: LongArray?) {
    if (ids != null && ids.size == 1) {
      tagBookmark(ids[0])
      return
    }

    if (!isPaused) {
      val fm = supportFragmentManager
      val tagBookmarkDialog = TagBookmarkDialog.newInstance(ids)
      tagBookmarkDialog.show(fm, TagBookmarkDialog.TAG)
    }
  }

  private fun tagBookmark(id: Long) {
    if (!isPaused) {
      val fm = supportFragmentManager
      val tagBookmarkDialog = TagBookmarkDialog.newInstance(id)
      tagBookmarkDialog.show(fm, TagBookmarkDialog.TAG)
    }
  }

  override fun onAddTagSelected() {
    val fm = supportFragmentManager
    val dialog = AddTagDialog()
    dialog.show(fm, AddTagDialog.TAG)
  }

  private inner class PagerAdapter(fm: FragmentManager) :
      FragmentPagerAdapter(fm) {

    override fun getCount() = 3

    override fun getItem(position: Int): Fragment {
      var pos = position
      if (isRtl) {
        pos = abs(position - 2)
      }
      return when (pos) {
        SURA_LIST -> SuraListFragment.newInstance()
        JUZ2_LIST -> JuzListFragment.newInstance()
        BOOKMARKS_LIST -> BookmarksFragment.newInstance()
        else -> BookmarksFragment.newInstance()
      }
    }

    override fun getItemId(position: Int): Long {
      val pos = if (isRtl) abs(position - 2) else position
      return when (pos) {
        SURA_LIST -> SURA_LIST.toLong()
        JUZ2_LIST -> JUZ2_LIST.toLong()
        BOOKMARKS_LIST -> BOOKMARKS_LIST.toLong()
        else -> BOOKMARKS_LIST.toLong()
      }
    }

    override fun getPageTitle(position: Int): CharSequence {
      val resId = if (isRtl) ARABIC_TITLES[position] else TITLES[position]
      return getString(resId)
    }
  }

  companion object {
    private val TITLES = intArrayOf(
        R.string.quran_sura,
        R.string.quran_juz2,
        R.string.menu_bookmarks
    )
    private val ARABIC_TITLES = intArrayOf(
        R.string.menu_bookmarks,
        R.string.quran_juz2,
        R.string.quran_sura
    )
    const val EXTRA_SHOW_TRANSLATION_UPGRADE = "transUp"
    private const val SI_SHOWED_UPGRADE_DIALOG = "si_showed_dialog"
    private const val SURA_LIST = 0
    private const val JUZ2_LIST = 1
    private const val BOOKMARKS_LIST = 2
    private var updatedTranslations = false
  }
}
