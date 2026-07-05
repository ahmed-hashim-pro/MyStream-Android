package com.quran.data.page.provider.shemerly

import com.quran.data.pageinfo.common.ShemerlyDataSource
import com.quran.data.pageinfo.common.size.FixedPageSizeCalculator
import com.quran.data.source.DisplaySize
import com.quran.data.source.PageProvider
import com.quran.data.source.PageSizeCalculator
import com.medoapps.www.onlinequran.pages.madani.R

class ShemerlyPageProvider : PageProvider {
  companion object {
    private val baseUrl = "https://android.quran.com/data/shemerly"
    // 521 page Shemerly layout, Hafs riwaya
    private val dataSource by lazy { ShemerlyDataSource() }
  }

  override fun getDataSource() = dataSource

  // shemerly pages are only published at width 1200
  override fun getPageSizeCalculator(displaySize: DisplaySize): PageSizeCalculator =
      FixedPageSizeCalculator("1200")

  // the shemerly zips carry no .vN marker files; version 1 always passes
  // the isVersion check, so no patch prompt is ever raised
  override fun getImageVersion() = 1

  override fun getImagesBaseUrl() = "$baseUrl/"

  override fun getImagesZipBaseUrl() = "$baseUrl/zips/"

  override fun getPatchBaseUrl() = "$baseUrl/patches/v"

  override fun getAyahInfoBaseUrl() = "$baseUrl/databases/ayahinfo/"

  override fun getAudioDirectoryName() = "audio"

  override fun getDatabaseDirectoryName() = "databases"

  // the images zip ships databases/ayahinfo_1200.db, which lands under the
  // shemerly images directory - use it instead of re-downloading
  override fun getAyahInfoDirectoryName() = "shemerly/databases"

  // translation and audio databases are shared with the madani page set
  override fun getDatabasesBaseUrl() = "https://android.quran.com/data/databases/"

  override fun getAudioDatabasesBaseUrl() = getDatabasesBaseUrl() + "audio/"

  override fun getImagesDirectoryName() = "shemerly"

  override fun getFallbackPageType() = "madani"

  override fun getPreviewTitle() = R.string.shemerly_title

  override fun getPreviewDescription() = R.string.shemerly_description
}
