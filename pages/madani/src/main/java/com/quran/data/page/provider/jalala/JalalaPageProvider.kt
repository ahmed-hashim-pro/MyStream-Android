package com.quran.data.page.provider.jalala

import com.quran.data.pageinfo.common.MadaniDataSource
import com.quran.data.pageinfo.common.size.FixedPageSizeCalculator
import com.quran.data.source.DisplaySize
import com.quran.data.source.PageProvider
import com.quran.data.source.PageSizeCalculator
import com.medoapps.www.onlinequran.pages.madani.R

/**
 * "Names of Allah in red" mushaf (مصحف تلوين لفظ الجلالة).
 *
 * This is the published red-لفظ-الجلالة edition, which is the standard King Fahd
 * Madinah 604-page plate with الله/ربّ/هو printed red and an ornamental frame added.
 * Because the calligraphy sits in the exact King Fahd positions, it reuses
 * [MadaniDataSource] (same 604-page layout) and the ayahinfo boxes are the King Fahd
 * boxes re-projected into the framed image — so tap-to-ayah, the recitation highlight
 * and audio follow-along all work unchanged.
 *
 * Images + the re-projected ayahinfo ship as one zip on our own bucket (see [baseUrl]).
 */
class JalalaPageProvider : PageProvider {
  companion object {
    // Served from our own S3 bucket (eu-central-1). Layout under this base:
    //   <baseUrl>/zips/images_1280.zip   (width_1280/page001..604.png + databases/ayahinfo_1280.db)
    //   <baseUrl>/width_1280/pageNNN.png  (per-page fallback, optional)
    private const val baseUrl = "https://geohashim-quran.s3.eu-central-1.amazonaws.com/jalala"

    // same 604-page layout as madani (this is the King Fahd plate underneath)
    private val dataSource by lazy { MadaniDataSource() }
  }

  override fun getDataSource() = dataSource

  // published at a single width; the framed page is 1280px wide
  override fun getPageSizeCalculator(displaySize: DisplaySize): PageSizeCalculator =
      FixedPageSizeCalculator("1280")

  // no .vN marker files ship with this set; version 1 always passes isVersion()
  override fun getImageVersion() = 1

  // colored pages (red لفظ الجلالة + colored border): use the hue-preserving
  // night path instead of the channel invert, exactly like tajweed/qaloon/warsh
  override fun imagesColored() = true

  override fun getImagesBaseUrl() = "$baseUrl/"

  override fun getImagesZipBaseUrl() = "$baseUrl/zips/"

  override fun getPatchBaseUrl() = "$baseUrl/patches/v"

  override fun getAyahInfoBaseUrl() = "$baseUrl/databases/ayahinfo/"

  override fun getAudioDirectoryName() = "audio"

  override fun getDatabaseDirectoryName() = "databases"

  // the images zip ships databases/ayahinfo_1600.db, which lands under the
  // jalala images directory - use it instead of re-downloading
  override fun getAyahInfoDirectoryName() = "jalala/databases"

  // translation and audio databases are shared with the madani page set
  override fun getDatabasesBaseUrl() = "https://android.quran.com/data/databases/"

  override fun getAudioDatabasesBaseUrl() = getDatabasesBaseUrl() + "audio/"

  // distinct directory so the framed pages never collide with madani's ""
  override fun getImagesDirectoryName() = "jalala"

  override fun getFallbackPageType() = "madani"

  override fun getPreviewTitle() = R.string.jalala_title

  override fun getPreviewDescription() = R.string.jalala_description
}
