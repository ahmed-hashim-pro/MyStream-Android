package com.medoapps.www.onlinequran.data

import com.medoapps.www.onlinequran.ui.util.TypefaceManager
import com.medoapps.www.onlinequran.database.DatabaseHandler

object QuranFileConstants {
  // server urls
  const val FONT_TYPE = TypefaceManager.TYPE_UTHMANI_HAFS

  // arabic database
  const val ARABIC_DATABASE = "quran.ar.db"
  const val ARABIC_SHARE_TABLE = DatabaseHandler.ARABIC_TEXT_TABLE
  const val ARABIC_SHARE_TEXT_HAS_BASMALLAH = true
  const val FETCH_QUARTER_NAMES_FROM_DATABASE = false

  const val FALLBACK_PAGE_TYPE = "qaloon"
  const val SEARCH_EXTRA_REPLACEMENTS = ""
}
