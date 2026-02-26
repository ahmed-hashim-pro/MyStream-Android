package com.medoapps.www.onlinequran.dao.translation

class TranslationItemDisplaySort : Comparator<TranslationItem> {
  override fun compare(first: TranslationItem, second: TranslationItem): Int {
    return first.displayOrder.compareTo(second.displayOrder)
  }
}
