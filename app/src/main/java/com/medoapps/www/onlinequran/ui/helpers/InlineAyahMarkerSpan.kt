package com.medoapps.www.onlinequran.ui.helpers

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.text.style.ReplacementSpan

/**
 * Inline ayah-end marker: a compact rounded gold-faint pill carrying the gold ayah
 * number, drawn at the end of the Arabic line (mockup `.mk`). Replaces the old
 * separate full-width [com.medoapps.www.onlinequran.view.AyahNumberView] row so the
 * number reads inline like the printed mushaf. Sizes scale off the Arabic paint so it
 * tracks the live A+/A- font-size strip.
 */
class InlineAyahMarkerSpan(
  private val number: String,
  private val boxColor: Int,
  private val numberColor: Int
) : ReplacementSpan() {

  private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = boxColor }
  private val numberPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    color = numberColor
    isFakeBoldText = true
    textAlign = Paint.Align.CENTER
  }

  private fun boxSize(paint: Paint): Float = paint.textSize * 0.86f
  private fun sideMargin(paint: Paint): Float = paint.textSize * 0.16f

  override fun getSize(
    paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?
  ): Int = (boxSize(paint) + sideMargin(paint) * 2f).toInt()

  override fun draw(
    canvas: Canvas, text: CharSequence?, start: Int, end: Int,
    x: Float, top: Int, y: Int, bottom: Int, paint: Paint
  ) {
    val box = boxSize(paint)
    val margin = sideMargin(paint)
    // center the pill on the visual middle of the Arabic glyphs (baseline + font mid)
    val cy = y + (paint.ascent() + paint.descent()) / 2f
    val left = x + margin
    val rect = RectF(left, cy - box / 2f, left + box, cy + box / 2f)
    val radius = box * 0.32f
    canvas.drawRoundRect(rect, radius, radius, boxPaint)

    numberPaint.textSize = box * 0.52f
    val fm = numberPaint.fontMetrics
    val textY = rect.centerY() - (fm.ascent + fm.descent) / 2f
    canvas.drawText(number, rect.centerX(), textY, numberPaint)
  }
}
