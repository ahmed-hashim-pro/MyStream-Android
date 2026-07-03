package com.medoapps.www.onlinequran.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.quran.data.core.QuranInfo
import com.quran.data.source.PageProvider
import com.medoapps.www.onlinequran.core.worker.WorkerTaskFactory
import com.medoapps.www.onlinequran.util.QuranFileUtils
import com.medoapps.www.onlinequran.util.QuranScreenInfo
import com.medoapps.www.onlinequran.util.QuranSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class MissingPageDownloadWorker(private val context: Context,
                                params: WorkerParameters,
                                private val okHttpClient: OkHttpClient,
                                private val quranInfo: QuranInfo,
                                private val quranScreenInfo: QuranScreenInfo,
                                private val quranFileUtils: QuranFileUtils,
                                private val quranSettings: QuranSettings,
                                private val pageProvider: PageProvider
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result = coroutineScope {
    Timber.d("MissingPageDownloadWorker")
    val requestedPageType = inputData.getString(WorkerConstants.PAGE_TYPE)
    if (requestedPageType != quranSettings.pageType) {
      Timber.d("MissingPageDownloadWorker skipping stale request for $requestedPageType")
      return@coroutineScope Result.success()
    }
    if (!pageProvider.supportsPerPageDownloads()) {
      // this page set only publishes zips, so individual pages can't be
      // fetched - recovery happens via the full zip download instead
      Timber.d("MissingPageDownloadWorker skipping $requestedPageType - no per page downloads")
      return@coroutineScope Result.success()
    }
    val pagesToDownload = findMissingPagesToDownload()
    Timber.d("MissingPageDownloadWorker found $pagesToDownload missing pages")
    if (pagesToDownload.size < MISSING_PAGE_LIMIT) {
      // attempt to download missing pages
      val results = pagesToDownload.asFlow()
          .map { downloadPage(it) }
          .flowOn(Dispatchers.IO)
          .toList()

      val failures = results.count { !it }
      if (failures > 0) {
        Timber.d("MissingPageWorker failed with $failures from ${pagesToDownload.size}")
      } else {
        Timber.d("MissingPageWorker success with ${pagesToDownload.size}")
      }
    }
    Result.success()
  }

  private fun findMissingPagesToDownload(): List<PageToDownload> {
    val width = quranScreenInfo.widthParam
    val result = findMissingPagesForWidth(width)

    val tabletWidth = quranScreenInfo.tabletWidthParam
    return if (width == tabletWidth) {
      result
    } else {
      result + findMissingPagesForWidth(tabletWidth)
    }
  }

  private fun findMissingPagesForWidth(width: String): List<PageToDownload> {
    val result = mutableListOf<PageToDownload>()
    val pagesDirectory = File(quranFileUtils.getQuranImagesDirectory(context, width))
    for (page in 1..quranInfo.numberOfPages) {
      val pageFile = QuranFileUtils.getPageFileName(page)
      if (!File(pagesDirectory, pageFile).exists()) {
        result.add(PageToDownload(width, page))
      }
    }
    return result
  }

  data class PageToDownload(val width: String, val page: Int)

  private fun downloadPage(pageToDownload: PageToDownload): Boolean {
    Timber.d("downloading ${pageToDownload.page} for ${pageToDownload.width} - thread: %s",
        Thread.currentThread().name)
    val pageName = QuranFileUtils.getPageFileName(pageToDownload.page)

    return try {
      quranFileUtils.getImageFromWeb(okHttpClient, context, pageToDownload.width, pageName)
          .isSuccessful
    } catch (throwable: Throwable) {
      false
    }
  }

  class Factory @Inject constructor(
    private val quranInfo: QuranInfo,
    private val quranFileUtils: QuranFileUtils,
    private val quranScreenInfo: QuranScreenInfo,
    private val okHttpClient: OkHttpClient,
    private val quranSettings: QuranSettings,
    private val pageProvider: PageProvider
  ) : WorkerTaskFactory {
    override fun makeWorker(
      appContext: Context,
      workerParameters: WorkerParameters
    ): ListenableWorker {
      return MissingPageDownloadWorker(
          appContext, workerParameters, okHttpClient, quranInfo, quranScreenInfo, quranFileUtils,
          quranSettings, pageProvider
      )
    }
  }

  companion object {
    private const val MISSING_PAGE_LIMIT = 50
  }
}
