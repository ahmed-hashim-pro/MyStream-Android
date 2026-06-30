package com.medoapps.www.onlinequran.view

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

/**
 * Applies a specific [Typeface] to a span of text (e.g. the Amiri surah name
 * inside an otherwise-Cairo line). Works on all API levels, unlike the
 * API-28 TypefaceSpan(Typeface) constructor.
 */
class CustomTypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
  override fun updateDrawState(paint: TextPaint) {
    paint.typeface = typeface
  }

  override fun updateMeasureState(paint: TextPaint) {
    paint.typeface = typeface
  }
}
