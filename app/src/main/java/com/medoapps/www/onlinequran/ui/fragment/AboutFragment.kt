package com.medoapps.www.onlinequran.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.recyclerview.widget.RecyclerView
import com.medoapps.www.onlinequran.BuildConfig
import com.medoapps.www.onlinequran.R
import com.medoapps.www.onlinequran.ui.helpers.PreferenceScreenChrome

class AboutFragment : PreferenceFragmentCompat() {

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    addPreferencesFromResource(R.xml.about)

    val flavor = BuildConfig.FLAVOR + "Images"
    val parent = findPreference("aboutDataSources") as PreferenceCategory?
    imagePrefKeys.filter { it != flavor }.map {
      val pref: Preference? = findPreference(it)
      if (pref != null) {
        parent?.removePreference(pref)
      }
    }

    // URL summaries render LTR in both locales (mockup dir="ltr"), otherwise
    // the AR locale visually reorders scheme/punctuation.
    isolateUrlSummaries(preferenceScreen)
  }

  private fun isolateUrlSummaries(group: PreferenceGroup) {
    for (i in 0 until group.preferenceCount) {
      val preference = group.getPreference(i)
      if (preference is PreferenceGroup) {
        isolateUrlSummaries(preference)
        continue
      }
      val summary = preference.summary?.toString() ?: continue
      if (summary.startsWith("http")) {
        preference.summary = "⁦$summary⁩" // LTR isolate
      }
    }
  }

  override fun onCreateRecyclerView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    savedInstanceState: Bundle?
  ): RecyclerView {
    // section cards + side padding (mockup 14 .card grouping)
    return super.onCreateRecyclerView(inflater, parent, savedInstanceState)
      .also { PreferenceScreenChrome.style(it) }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setDivider(null)
  }

  companion object {
    private val imagePrefKeys = arrayOf("madaniImages", "naskhImages", "qaloonImages")
  }
}
