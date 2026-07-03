package com.quran.data.source

import androidx.annotation.StringRes

interface PageProvider {
  fun getDataSource(): QuranDataSource
  fun getPageSizeCalculator(displaySize: DisplaySize): PageSizeCalculator

  fun getImageVersion(): Int

  fun getImagesBaseUrl(): String
  fun getImagesZipBaseUrl(): String
  fun getPatchBaseUrl(): String
  fun getAyahInfoBaseUrl(): String
  fun getDatabasesBaseUrl(): String
  fun getAudioDatabasesBaseUrl(): String

  fun getAudioDirectoryName(): String
  fun getDatabaseDirectoryName(): String
  fun getAyahInfoDirectoryName(): String
  fun getImagesDirectoryName(): String

  fun ayahInfoDbHasGlyphData(): Boolean = false

  // whether the page images carry meaningful colors (ex tajweed rule colors)
  // that night mode filters must preserve instead of channel-inverting
  fun imagesColored(): Boolean = false

  // whether individual page images can be fetched from getImagesBaseUrl() —
  // some page sets are only published as zips
  fun supportsPerPageDownloads(): Boolean = true

  @StringRes fun getPreviewTitle(): Int
  @StringRes fun getPreviewDescription(): Int

  fun getPageContentType(): PageContentType = PageContentType.IMAGE
  fun getFallbackPageType(): String? = null
}
