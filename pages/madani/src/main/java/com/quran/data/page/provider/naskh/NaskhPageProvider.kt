package com.quran.data.page.provider.naskh

import com.quran.data.pageinfo.common.NaskhDataSource
import com.quran.data.pageinfo.common.size.FixedPageSizeCalculator
import com.quran.data.source.DisplaySize
import com.quran.data.source.PageProvider
import com.quran.data.source.PageSizeCalculator
import com.medoapps.www.onlinequran.pages.madani.R

class NaskhPageProvider : PageProvider {
  companion object {
    private val baseUrl = "https://android.quran.com/data/naskh"
    // 612 page SHL naskh layout, Hafs riwaya
    private val dataSource by lazy { NaskhDataSource() }
  }

  override fun getDataSource() = dataSource

  // naskh pages are only published at width 1280
  override fun getPageSizeCalculator(displaySize: DisplaySize): PageSizeCalculator =
      FixedPageSizeCalculator("1280")

  override fun getImageVersion() = 3

  override fun getImagesBaseUrl() = "$baseUrl/"

  override fun getImagesZipBaseUrl() = "$baseUrl/zips/"

  override fun getPatchBaseUrl() = "$baseUrl/patches/v"

  override fun getAyahInfoBaseUrl() = "$baseUrl/databases/ayahinfo/"

  override fun getAudioDirectoryName() = "audio"

  override fun getDatabaseDirectoryName() = "databases"

  // the images zip ships databases/ayahinfo_1280.db, which lands under the
  // naskh images directory - use it instead of re-downloading
  override fun getAyahInfoDirectoryName() = "naskh/databases"

  // translation and audio databases are shared with the madani page set
  override fun getDatabasesBaseUrl() = "https://android.quran.com/data/databases/"

  override fun getAudioDatabasesBaseUrl() = getDatabasesBaseUrl() + "audio/"

  override fun getImagesDirectoryName() = "naskh"

  override fun getFallbackPageType() = "madani"

  override fun getPreviewTitle() = R.string.naskh_title

  override fun getPreviewDescription() = R.string.naskh_description
}
