package com.medoapps.www.onlinequran.ui.preference

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.medoapps.www.onlinequran.R

class QuranHeaderPreference @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0,
) : Preference(context, attrs, defStyleAttr, defStyleRes) {

  init {
    layoutResource = R.layout.about_header
    isSelectable = false
  }

  override fun onBindViewHolder(holder: PreferenceViewHolder) {
    super.onBindViewHolder(holder)
    if (isEnabled) {
      // Day/night-aware ink: the header sits on the light preference list, so a
      // hardcoded white made the app name invisible in light mode.
      val tv = holder.findViewById(R.id.title) as? TextView
      tv?.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
    }
  }
}
