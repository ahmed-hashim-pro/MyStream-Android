package com.medoapps.www.onlinequran.ui.preference

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.medoapps.www.onlinequran.R

/**
 * Plain preference with a trailing gold current-value label (mockup .prow .val),
 * for rows whose value is picked on a separate screen (e.g. Page Type).
 */
class GoldValuePreference @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : Preference(context, attrs) {

  var currentValue: CharSequence? = null
    set(value) {
      field = value
      notifyChanged()
    }

  init {
    widgetLayoutResource = R.layout.preference_widget_value
  }

  override fun onBindViewHolder(holder: PreferenceViewHolder) {
    super.onBindViewHolder(holder)
    (holder.findViewById(R.id.pref_current_value) as? TextView)?.text = currentValue
  }
}
