package com.medoapps.www.onlinequran.ui.helpers

import android.content.Context
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import com.medoapps.www.onlinequran.ui.util.TypefaceManager

class UthmaniSpan(context: Context) : MetricAffectingSpan() {

  private val typeFace = TypefaceManager.getUthmaniTypeface(context)

  override fun updateDrawState(ds: TextPaint) {
    ds.typeface = typeFace
  }

  override fun updateMeasureState(paint: TextPaint) {
    paint.typeface = typeFace
  }
}
