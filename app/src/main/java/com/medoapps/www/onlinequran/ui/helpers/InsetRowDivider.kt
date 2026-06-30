package com.medoapps.www.onlinequran.ui.helpers

import android.graphics.Canvas
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView

/**
 * Draws a faint inset hairline at the bottom of each item of [rowViewType] when
 * the following item is also of [rowViewType] — i.e. only between consecutive
 * surah rows, never around the Juz' band headers (mockup .row+.row divider).
 */
class InsetRowDivider(
  density: Float,
  color: Int,
  private val rowViewType: Int,
  insetDp: Float = 16f,
) : RecyclerView.ItemDecoration() {

  private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.color = color }
  private val thickness = density.coerceAtLeast(1f)
  private val inset = insetDp * density

  override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
    val adapter = parent.adapter ?: return
    val left = inset
    val right = parent.width - inset
    for (i in 0 until parent.childCount) {
      val child = parent.getChildAt(i)
      val pos = parent.getChildAdapterPosition(child)
      if (pos == RecyclerView.NO_POSITION) continue
      if (adapter.getItemViewType(pos) != rowViewType) continue
      val next = pos + 1
      if (next >= adapter.itemCount) continue
      if (adapter.getItemViewType(next) != rowViewType) continue
      val y = child.bottom.toFloat()
      c.drawRect(left, y - thickness, right, y, paint)
    }
  }
}
