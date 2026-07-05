package com.quran.data.page.provider.newmadani

import com.quran.data.pageinfo.common.NewMadaniDataSource
import com.quran.data.pageinfo.common.size.FixedPageSizeCalculator
import com.quran.data.source.DisplaySize
import com.quran.data.source.PageProvider
import com.quran.data.source.PageSizeCalculator
import com.medoapps.www.onlinequran.pages.madani.R

class NewMadaniPageProvider : PageProvider {
  companion object {
    private val baseUrl = "https://android.quran.com/data/new_madani"
    // the newer Madinah print: 604 Hafs pages, but 25 of them break
    // lines differently from the classic plates
    private val dataSource by lazy { NewMadaniDataSource() }
  }

  override fun getDataSource() = dataSource

  // new madani pages are only published at width 1260
  override fun getPageSizeCalculator(displaySize: DisplaySize): PageSizeCalculator =
      FixedPageSizeCalculator("1260")

  // the zip carries a .v1 marker
  override fun getImageVersion() = 1

  override fun getImagesBaseUrl() = "$baseUrl/"

  override fun getImagesZipBaseUrl() = "$baseUrl/zips/"

  override fun getPatchBaseUrl() = "$baseUrl/patches/v"

  override fun getAyahInfoBaseUrl() = "$baseUrl/databases/ayahinfo/"

  override fun getAudioDirectoryName() = "audio"

  override fun getDatabaseDirectoryName() = "databases"

  // the images zip ships databases/ayahinfo_1260.db, which lands under the
  // new_madani images directory - use it instead of re-downloading
  override fun getAyahInfoDirectoryName() = "new_madani/databases"

  // translation and audio databases are shared with the madani page set
  override fun getDatabasesBaseUrl() = "https://android.quran.com/data/databases/"

  override fun getAudioDatabasesBaseUrl() = getDatabasesBaseUrl() + "audio/"

  override fun getImagesDirectoryName() = "new_madani"

  override fun getFallbackPageType() = "madani"

  override fun getPreviewTitle() = R.string.new_madani_title

  override fun getPreviewDescription() = R.string.new_madani_description
}
