package com.medoapps.www.onlinequran.ui.preference

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.ListPreference
import androidx.preference.PreferenceViewHolder
import com.medoapps.www.onlinequran.R

/**
 * ListPreference that shows its currently selected entry as the trailing gold
 * value label (mockup .prow .val) via @layout/preference_widget_value.
 */
class GoldValueListPreference @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : ListPreference(context, attrs) {

  init {
    widgetLayoutResource = R.layout.preference_widget_value
  }

  override fun onBindViewHolder(holder: PreferenceViewHolder) {
    super.onBindViewHolder(holder)
    (holder.findViewById(R.id.pref_current_value) as? TextView)?.text = entry
  }

  override fun setValue(value: String?) {
    super.setValue(value)
    notifyChanged()
  }
}
