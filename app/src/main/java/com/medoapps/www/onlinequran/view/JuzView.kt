package com.medoapps.www.onlinequran.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import androidx.core.content.ContextCompat
import com.medoapps.www.onlinequran.R

class JuzView(
  context: Context,
  type: Int,
  private val overlayText: String?
) : Drawable() {

  private var radius = 0
  private var circleY = 0
  private val percentage: Int
  private var textOffset = 0f
  private var ringStroke = 0f

  private lateinit var circleRect: RectF
  private lateinit var ringRect: RectF
  private val circlePaint = Paint()
  private var overlayTextPaint: TextPaint? = null
  private val circleBackgroundPaint = Paint()

  init {
    val resources = context.resources
    val circleColor = ContextCompat.getColor(context, R.color.gold_accent)
    val circleBackground = ContextCompat.getColor(context, R.color.gold_accent_faint)

    // Ring/donut markers (matches the mockup .pie conic-gradient ring, not a filled disc).
    circlePaint.apply {
      style = Paint.Style.STROKE
      strokeCap = Paint.Cap.BUTT
      color = circleColor
      isAntiAlias = true
    }

    circleBackgroundPaint.apply {
      style = Paint.Style.STROKE
      strokeCap = Paint.Cap.BUTT
      color = circleBackground
      isAntiAlias = true
    }

    if (!overlayText.isNullOrEmpty()) {
      // Sits in the hollow ring centre now, so it must read on the row bg in both
      // themes (dark in light, white in dark) — not the old navy on the gold disc.
      val textPaintColor = ContextCompat.getColor(context, R.color.text_primary)
      val textPaintSize = resources.getDimensionPixelSize(R.dimen.juz_overlay_text_size)
      overlayTextPaint = TextPaint()
      overlayTextPaint?.apply {
        isAntiAlias = true
        color = textPaintColor
        textSize = textPaintSize.toFloat()
        textAlign = Paint.Align.CENTER
      }

      overlayTextPaint?.let { textPaint ->
        val textHeight = textPaint.descent() - textPaint.ascent()
        textOffset = textHeight / 2 - textPaint.descent()
      }
    }

    this.percentage = when (type) {
      TYPE_JUZ -> 100
      TYPE_THREE_QUARTERS -> 75
      TYPE_HALF -> 50
      TYPE_QUARTER -> 25
      else -> 0
    }
  }

  override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
    super.setBounds(left, top, right, bottom)
    radius = (right - left) / 2
    val yOffset = (bottom - top - 2 * radius) / 2
    circleY = radius + yOffset
    circleRect = RectF(
      left.toFloat(), (top + yOffset).toFloat(),
      right.toFloat(), (top + yOffset + 2 * radius).toFloat()
    )
    // ~0.1 * diameter band (CSS .pie inset:4px on a 40px circle); inset the ring rect
    // by half the stroke so the band's outer edge sits on the bounds.
    ringStroke = radius * 0.22f
    circlePaint.strokeWidth = ringStroke
    circleBackgroundPaint.strokeWidth = ringStroke
    val half = ringStroke / 2f
    ringRect = RectF(
      circleRect.left + half, circleRect.top + half,
      circleRect.right - half, circleRect.bottom - half
    )
  }

  override fun draw(canvas: Canvas) {
    // faint full track ring, then the gold quarter arc on top (arc, not a filled wedge)
    canvas.drawArc(ringRect, 0f, 360f, false, circleBackgroundPaint)
    canvas.drawArc(
      ringRect, -90f,
      (3.6 * percentage).toFloat(), false, circlePaint
    )
    overlayTextPaint?.let { textPaint ->
      if (overlayText != null) {
        canvas.drawText(
          overlayText, circleRect.centerX(),
          circleRect.centerY() + textOffset, textPaint
        )
      }
    }
  }

  override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

  override fun setAlpha(alpha: Int) {}

  override fun setColorFilter(cf: ColorFilter?) {}

  companion object {
    const val TYPE_JUZ = 1
    const val TYPE_QUARTER = 2
    const val TYPE_HALF = 3
    const val TYPE_THREE_QUARTERS = 4
  }
}
