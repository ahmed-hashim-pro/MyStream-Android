package com.medoapps.www.onlinequran.ui.preference

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar

class SeekBarBackgroundBrightnessPreference(
  context: Context, attrs: AttributeSet
) : SeekBarPreference(context, attrs) {

  override fun getPreviewVisibility(): Int = View.GONE

  override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    super.onProgressChanged(seekBar, progress, fromUser)
    previewBox.visibility = View.VISIBLE

    // mutate the rounded swatch's fill (setBackgroundColor would wipe the
    // rounded shape + hairline border from bg_brightness_swatch)
    val boxColor = Color.argb(255, progress, progress, progress)
    (previewBox.background?.mutate() as? GradientDrawable)?.setColor(boxColor)
  }
}
