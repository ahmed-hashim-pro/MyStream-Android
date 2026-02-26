package com.medoapps.www.onlinequran.data
import com.quran.data.page.provider.MadaniConstants

import com.medoapps.www.onlinequran.database.DatabaseHandler
import com.medoapps.www.onlinequran.ui.util.TypefaceManager

object QuranFileConstants {
  // server urls
  const val FONT_TYPE = TypefaceManager.TYPE_UTHMANI_HAFS

  // arabic database
  const val ARABIC_DATABASE = MadaniConstants.ARABIC_DATABASE
  const val ARABIC_SHARE_TABLE = DatabaseHandler.SHARE_TEXT_TABLE

  // SDK_INT is always >= 21
  const val ARABIC_SHARE_TEXT_HAS_BASMALLAH = true
  const val FETCH_QUARTER_NAMES_FROM_DATABASE = false

  const val FALLBACK_PAGE_TYPE = "madani"
  const val SEARCH_EXTRA_REPLACEMENTS = ""
}
