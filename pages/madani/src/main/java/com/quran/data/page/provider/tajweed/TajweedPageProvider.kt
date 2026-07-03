package com.quran.data.page.provider.tajweed

import com.quran.data.pageinfo.common.MadaniDataSource
import com.quran.data.source.DisplaySize
import com.quran.data.source.PageProvider
import com.quran.data.source.PageSizeCalculator
import com.medoapps.www.onlinequran.pages.madani.R

class TajweedPageProvider : PageProvider {
  companion object {
    private val baseUrl = "https://android.quran.com/data/tajweed"
    // same 604 page madani layout, only the glyph colors differ
    private val dataSource by lazy { MadaniDataSource() }
  }

  override fun getDataSource() = dataSource

  override fun getPageSizeCalculator(displaySize: DisplaySize): PageSizeCalculator =
      TajweedPageSizeCalculator()

  override fun getImageVersion() = 7

  override fun getImagesBaseUrl() = "$baseUrl/"

  override fun getImagesZipBaseUrl() = "$baseUrl/zips/"

  override fun getPatchBaseUrl() = "$baseUrl/patches/v"

  override fun getAyahInfoBaseUrl() = "$baseUrl/databases/ayahinfo/"

  override fun getAudioDirectoryName() = "audio"

  override fun getDatabaseDirectoryName() = "databases"

  // the images zip ships databases/ayahinfo_1280.db, which lands under the
  // tajweed images directory - use it instead of re-downloading
  override fun getAyahInfoDirectoryName() = "tajweed/databases"

  // translation and audio databases are shared with the madani page set
  override fun getDatabasesBaseUrl() = "https://android.quran.com/data/databases/"

  override fun getAudioDatabasesBaseUrl() = getDatabasesBaseUrl() + "audio/"

  override fun getImagesDirectoryName() = "tajweed"

  override fun imagesColored() = true

  // the CDN only publishes tajweed pages as a zip, no per-page files
  override fun supportsPerPageDownloads() = false

  override fun getFallbackPageType() = "madani"

  override fun getPreviewTitle() = R.string.tajweed_title

  override fun getPreviewDescription() = R.string.tajweed_description
}

private class TajweedPageSizeCalculator : PageSizeCalculator {
  // tajweed pages are only published at width 1280
  override fun getWidthParameter() = "1280"
  override fun getTabletWidthParameter() = "1280"
  override fun setOverrideParameter(parameter: String) {
    // overrides only apply to legacy madani width upgrades
  }
}
