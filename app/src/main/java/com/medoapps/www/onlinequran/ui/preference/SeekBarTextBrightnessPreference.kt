package com.medoapps.www.onlinequran.ui.preference

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.SeekBar
import com.medoapps.www.onlinequran.R

class SeekBarTextBrightnessPreference(
  context: Context, attrs: AttributeSet
) : SeekBarPreference(context, attrs) {

  override fun getPreviewVisibility(): Int = View.VISIBLE

  override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    super.onProgressChanged(seekBar, progress, fromUser)
    // white-alpha preview on a dark night-paper chip so it reads on the light
    // settings card too (the value simulates night-mode text brightness)
    previewText.setBackgroundResource(R.drawable.bg_preview_night_chip)
    previewText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
    previewText.setTextColor(Color.argb(progress, 255, 255, 255))
  }
}
