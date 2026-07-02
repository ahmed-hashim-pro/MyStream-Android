package com.medoapps.www.onlinequran.ui.helpers

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceGroupAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Draws the mockup's rounded section cards (.card) behind each run of
 * preference rows between [PreferenceCategory] headers, with inset hairlines
 * between rows inside a card (mockup .prow+.prow). Attach to the settings
 * RecyclerView along with 12dp horizontal padding + clipToPadding=false.
 */
@SuppressLint("RestrictedApi")
class PreferenceCardDecoration(
  density: Float,
  fillColor: Int,
  strokeColor: Int,
  hairlineColor: Int
) : RecyclerView.ItemDecoration() {

  private val radius = 16 * density
  private val hairInset = 14 * density
  private val hairThickness = density.coerceAtLeast(1f)
  private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
    color = fillColor
  }
  private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeWidth = density.coerceAtLeast(1f)
    color = strokeColor
  }
  private val hairline = Paint().apply { color = hairlineColor }

  override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
    val adapter = parent.adapter as? PreferenceGroupAdapter ?: return
    fun isRow(pos: Int): Boolean =
      pos in 0 until adapter.itemCount && adapter.getItem(pos) !is PreferenceCategory

    val left = parent.paddingLeft.toFloat()
    val right = (parent.width - parent.paddingRight).toFloat()

    var i = 0
    while (i < parent.childCount) {
      val child = parent.getChildAt(i)
      val pos = parent.getChildAdapterPosition(child)
      if (pos == RecyclerView.NO_POSITION || !isRow(pos)) {
        i++
        continue
      }
      // extend this run over consecutive visible row children
      var j = i
      var lastPos = pos
      while (j + 1 < parent.childCount) {
        val next = parent.getChildAdapterPosition(parent.getChildAt(j + 1))
        if (next == lastPos + 1 && isRow(next)) {
          j++
          lastPos = next
        } else break
      }
      var top = child.top.toFloat() + child.translationY
      var bottom = parent.getChildAt(j).let { it.bottom.toFloat() + it.translationY }
      // if the card continues beyond the viewport, push its corners off-screen
      if (isRow(pos - 1)) top -= radius * 2
      if (isRow(lastPos + 1)) bottom += radius * 2

      val rect = RectF(left, top, right, bottom)
      c.drawRoundRect(rect, radius, radius, fill)
      c.drawRoundRect(rect, radius, radius, stroke)

      for (k in i until j) {
        val y = parent.getChildAt(k).bottom.toFloat()
        c.drawRect(left + hairInset, y - hairThickness, right - hairInset, y, hairline)
      }
      i = j + 1
    }
  }
}
