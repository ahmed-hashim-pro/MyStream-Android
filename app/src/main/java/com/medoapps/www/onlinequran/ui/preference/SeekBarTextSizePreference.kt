package com.medoapps.www.onlinequran.ui.preference

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.medoapps.www.onlinequran.R

class SeekBarTextSizePreference(
  context: Context, attrs: AttributeSet
) : SeekBarPreference(context, attrs) {

  override fun getPreviewVisibility(): Int = View.VISIBLE

  override fun getSizeLabelsVisibility(): Int = View.VISIBLE

  override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    super.onProgressChanged(seekBar, progress, fromUser)
    previewText.textSize = progress.toFloat()
    // reset state a recycled text-brightness row may have left behind
    previewText.background = null
    previewText.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
  }
}
