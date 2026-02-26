package com.medoapps.www.onlinequran.dao.translation

interface TranslationRowData {
  fun isSeparator(): Boolean
  fun name(): String
  fun needsUpgrade(): Boolean
}
