package com.quran.data.page.provider.allahred

import com.quran.data.pageinfo.common.MadaniDataSource
import com.quran.data.pageinfo.common.size.FixedPageSizeCalculator
import com.quran.data.source.DisplaySize
import com.quran.data.source.PageProvider
import com.quran.data.source.PageSizeCalculator
import com.medoapps.www.onlinequran.pages.madani.R

/**
 * "Name of Allah in red" mushaf (لفظ الجلالة بالأحمر).
 *
 * The published red-لفظ-الجلالة edition (اسم الله باللون الأحمر): the standard King
 * Fahd Madinah 604-page plate with الله/ربّ/هو printed red inside an ornamental
 * border. Each page is warped onto the King Fahd (madani) grid, so a single uniform
 * madani ayahinfo aligns every page — it reuses [MadaniDataSource] and tap-to-ayah,
 * the recitation highlight and audio follow-along all work unchanged.
 *
 * The scanned white paper is removed (color-to-alpha) so the pages are transparent
 * glyphs on alpha, like the madani set: the reader's own day/sepia/night paper shows
 * through, and [imagesColored] routes night mode through the hue-preserving invert so
 * the red لفظ الجلالة, gold ayah markers and the colored frame keep their hue. The
 * ornamental frame is dropped on inner pages but kept on the illuminated openings.
 *
 * Images + the ayahinfo ship as one zip on our own bucket (see [baseUrl]).
 */
class AllahRedPageProvider : PageProvider {
  companion object {
    // Served from our own S3 bucket (eu-central-1). Layout under this base:
    //   <baseUrl>/zips/images_1000.zip   (width_1000/page001..604.png + databases/ayahinfo_1000.db)
    //   <baseUrl>/width_1000/pageNNN.png  (per-page fallback, optional)
    private const val baseUrl = "https://geohashim-quran.s3.eu-central-1.amazonaws.com/allah_red"

    // same 604-page layout as madani (this is the King Fahd plate underneath)
    private val dataSource by lazy { MadaniDataSource() }
  }

  override fun getDataSource() = dataSource

  // published at a single width; the warped page is 1000px wide
  override fun getPageSizeCalculator(displaySize: DisplaySize): PageSizeCalculator =
      FixedPageSizeCalculator("1000")

  // no .vN marker files ship with this set; version 1 always passes isVersion()
  override fun getImageVersion() = 1

  // colored pages (red لفظ الجلالة + colored border): use the hue-preserving
  // night path instead of the channel invert, exactly like tajweed/qaloon/warsh
  override fun imagesColored() = true

  // the bucket only publishes the full zip, no per-page files — so declining the
  // download falls back to madani (getFallbackPageType) and missing-page recovery
  // re-fetches the zip instead of 404-ing per-page URLs (matches tajweed)
  override fun supportsPerPageDownloads() = false

  override fun getImagesBaseUrl() = "$baseUrl/"

  override fun getImagesZipBaseUrl() = "$baseUrl/zips/"

  override fun getPatchBaseUrl() = "$baseUrl/patches/v"

  override fun getAyahInfoBaseUrl() = "$baseUrl/databases/ayahinfo/"

  override fun getAudioDirectoryName() = "audio"

  override fun getDatabaseDirectoryName() = "databases"

  // the images zip ships databases/ayahinfo_1000.db, which lands under this print's
  // images directory - use it instead of re-downloading
  override fun getAyahInfoDirectoryName() = "allah_red/databases"

  // translation and audio databases are shared with the madani page set
  override fun getDatabasesBaseUrl() = "https://android.quran.com/data/databases/"

  override fun getAudioDatabasesBaseUrl() = getDatabasesBaseUrl() + "audio/"

  // distinct directory so these pages never collide with madani's ""
  override fun getImagesDirectoryName() = "allah_red"

  override fun getFallbackPageType() = "madani"

  override fun getPreviewTitle() = R.string.allah_red_title

  override fun getPreviewDescription() = R.string.allah_red_description
}
