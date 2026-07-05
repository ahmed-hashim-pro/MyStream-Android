package com.quran.data.pageinfo.common.size

import com.quran.data.source.PageSizeCalculator

/** For page sets published at a single width only. */
class FixedPageSizeCalculator(private val width: String) : PageSizeCalculator {
  override fun getWidthParameter() = width
  override fun getTabletWidthParameter() = width
  override fun setOverrideParameter(parameter: String) {
    // overrides only apply to legacy madani width upgrades
  }
}
