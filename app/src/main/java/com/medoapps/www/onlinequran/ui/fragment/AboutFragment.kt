package com.medoapps.www.onlinequran.ui.fragment

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.medoapps.www.onlinequran.BuildConfig
import com.medoapps.www.onlinequran.R

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
  }

  companion object {
    private val imagePrefKeys = arrayOf("madaniImages", "naskhImages", "qaloonImages")
  }
}
