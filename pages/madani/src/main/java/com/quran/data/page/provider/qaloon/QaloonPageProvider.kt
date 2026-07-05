package com.quran.data.page.provider.qaloon

import com.quran.data.pageinfo.common.QaloonDataSource
import com.quran.data.pageinfo.common.size.NoOverridePageSizeCalculator
import com.quran.data.source.DisplaySize
import com.quran.data.source.PageProvider
import com.quran.data.source.PageSizeCalculator
import com.medoapps.www.onlinequran.pages.madani.R

class QaloonPageProvider : PageProvider {
  companion object {
    private val baseUrl = "https://android.quran.com/data/qaloon"
    // 604 page Qaloon-riwaya print; its ayahinfo database is normalized
    // to Hafs ayah numbering, so the shared Hafs machinery applies
    private val dataSource by lazy { QaloonDataSource() }
  }

  override fun getDataSource() = dataSource

  // qaloon publishes the same width buckets as madani (320..1260), but the
  // legacy madani 1920 width-upgrade override must not leak into this set
  override fun getPageSizeCalculator(displaySize: DisplaySize): PageSizeCalculator =
      NoOverridePageSizeCalculator(displaySize)

  // the zip carries a .v2 marker
  override fun getImageVersion() = 2

  override fun getImagesBaseUrl() = "$baseUrl/"

  override fun getImagesZipBaseUrl() = "$baseUrl/zips/"

  override fun getPatchBaseUrl() = "$baseUrl/patches/v"

  override fun getAyahInfoBaseUrl() = "$baseUrl/databases/ayahinfo/"

  override fun getAudioDirectoryName() = "audio"

  override fun getDatabaseDirectoryName() = "databases"

  // the images zip ships databases/ayahinfo_<width>.db, which lands under
  // the qaloon images directory - use it instead of re-downloading
  override fun getAyahInfoDirectoryName() = "qaloon/databases"

  // translation and audio databases are shared with the madani page set
  override fun getDatabasesBaseUrl() = "https://android.quran.com/data/databases/"

  override fun getAudioDatabasesBaseUrl() = getDatabasesBaseUrl() + "audio/"

  override fun getImagesDirectoryName() = "qaloon"

  // the print marks vowels and hamzat al-wasl in red
  override fun imagesColored() = true

  override fun getFallbackPageType() = "madani"

  override fun getPreviewTitle() = R.string.qaloon_title

  override fun getPreviewDescription() = R.string.qaloon_description
}
