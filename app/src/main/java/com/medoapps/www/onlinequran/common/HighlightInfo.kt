package com.medoapps.www.onlinequran.common

import com.medoapps.www.onlinequran.ui.helpers.HighlightType

class HighlightInfo(
  val sura: Int,
  val ayah: Int,
  val word: Int,
  val highlightType: HighlightType,
  val scrollToAyah: Boolean
)
