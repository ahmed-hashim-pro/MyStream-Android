package com.quran.data.page.provider.warsh

import com.quran.data.pageinfo.common.WarshDataSource
import com.quran.data.pageinfo.common.size.NoOverridePageSizeCalculator
import com.quran.data.source.DisplaySize
import com.quran.data.source.PageProvider
import com.quran.data.source.PageSizeCalculator
import com.medoapps.www.onlinequran.pages.madani.R

class WarshPageProvider : PageProvider {
  companion object {
    private val baseUrl = "https://android.quran.com/data/warsh"
    // 604 page Warsh-riwaya print; its ayahinfo database is normalized
    // to Hafs ayah numbering, so the shared Hafs machinery applies
    private val dataSource by lazy { WarshDataSource() }
  }

  override fun getDataSource() = dataSource

  // warsh publishes the same width buckets as madani (320..1260), but the
  // legacy madani 1920 width-upgrade override must not leak into this set
  override fun getPageSizeCalculator(displaySize: DisplaySize): PageSizeCalculator =
      NoOverridePageSizeCalculator(displaySize)

  // the warsh zips carry no .vN marker files; version 1 always passes
  // the isVersion check, so no patch prompt is ever raised
  override fun getImageVersion() = 1

  override fun getImagesBaseUrl() = "$baseUrl/"

  override fun getImagesZipBaseUrl() = "$baseUrl/zips/"

  override fun getPatchBaseUrl() = "$baseUrl/patches/v"

  override fun getAyahInfoBaseUrl() = "$baseUrl/databases/ayahinfo/"

  override fun getAudioDirectoryName() = "audio"

  override fun getDatabaseDirectoryName() = "databases"

  // the images zip ships databases/ayahinfo_<width>.db, which lands under
  // the warsh images directory - use it instead of re-downloading
  override fun getAyahInfoDirectoryName() = "warsh/databases"

  // translation and audio databases are shared with the madani page set
  override fun getDatabasesBaseUrl() = "https://android.quran.com/data/databases/"

  override fun getAudioDatabasesBaseUrl() = getDatabasesBaseUrl() + "audio/"

  override fun getImagesDirectoryName() = "warsh"

  // the Maghribi print marks vowels and hamzat al-wasl in red
  override fun imagesColored() = true

  override fun getFallbackPageType() = "madani"

  override fun getPreviewTitle() = R.string.warsh_title

  override fun getPreviewDescription() = R.string.warsh_description
}
