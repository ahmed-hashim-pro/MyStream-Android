package com.medoapps.www.onlinequran.ui.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import com.quran.data.source.PageProvider
import com.medoapps.www.onlinequran.QuranAdvancedPreferenceActivity
import com.medoapps.www.onlinequran.QuranApplication
import com.medoapps.www.onlinequran.QuranPreferenceActivity
import com.medoapps.www.onlinequran.R
import com.medoapps.www.onlinequran.data.Constants
import com.medoapps.www.onlinequran.pageselect.PageSelectActivity
import com.medoapps.www.onlinequran.ui.AudioManagerActivity
import com.medoapps.www.onlinequran.ui.TranslationManagerActivity
import com.quran.mobile.di.ExtraPreferencesProvider
import javax.inject.Inject

class QuranSettingsFragment : PreferenceFragmentCompat(),
  SharedPreferences.OnSharedPreferenceChangeListener {

  @Inject
  lateinit var pageTypes: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards PageProvider>

  @Inject
  lateinit var extraPreferences: Set<@JvmSuppressWildcards ExtraPreferencesProvider>

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    addPreferencesFromResource(R.xml.quran_preferences)

    val appContext = requireContext().applicationContext

    // field injection
    (appContext as QuranApplication).applicationComponent.inject(this)

    // handle translation manager click
    val translationPref: Preference? = findPreference(Constants.PREF_TRANSLATION_MANAGER)
    translationPref?.setOnPreferenceClickListener {
      startActivity(Intent(activity, TranslationManagerActivity::class.java))
      true
    }

    // handle audio manager click
    val audioManagerPref: Preference? = findPreference(Constants.PREF_AUDIO_MANAGER)
    audioManagerPref?.setOnPreferenceClickListener {
      startActivity(Intent(activity, AudioManagerActivity::class.java))
      true
    }

    val pageChangePref: Preference? = findPreference(Constants.PREF_PAGE_TYPE)
    if (pageTypes.size < 2 && pageChangePref != null) {
      val readingPrefs: Preference? = findPreference(Constants.PREF_READING_CATEGORY)
      (readingPrefs as PreferenceGroup).removePreference(pageChangePref)
    }

    // add additional injected preferences (if any)
    extraPreferences
      .sortedBy { it.order }
      .forEach { it.addPreferences(preferenceScreen) }

    // tint all preference icons with gold accent
    tintPreferenceIcons(preferenceScreen)
  }

  private fun tintPreferenceIcons(preferenceGroup: PreferenceGroup) {
    val tintColor = ContextCompat.getColor(requireContext(), R.color.gold_accent)
    val tintList = ColorStateList.valueOf(tintColor)
    for (i in 0 until preferenceGroup.preferenceCount) {
      val preference = preferenceGroup.getPreference(i)
      preference.icon?.let { icon ->
        icon.setTintList(tintList)
        preference.icon = icon
      }
      if (preference is PreferenceGroup) {
        tintPreferenceIcons(preference)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
  }

  override fun onPause() {
    preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    super.onPause()
  }

  override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
    if (key == Constants.PREF_USE_ARABIC_NAMES) {
      val context = activity
      if (context is QuranPreferenceActivity) {
        context.restartActivity()
      }
    }
  }

  override fun onPreferenceTreeClick(preference: Preference): Boolean {
    val key = preference.key
    if ("key_prefs_advanced" == key) {
      val intent = Intent(activity, QuranAdvancedPreferenceActivity::class.java)
      startActivity(intent)
      return true
    } else if (Constants.PREF_PAGE_TYPE == key) {
      val intent = Intent(activity, PageSelectActivity::class.java)
      startActivity(intent)
      return true
    }

    for (extraPref in extraPreferences) {
      if (extraPref.onPreferenceClick(preference)) {
        return true
      }
    }

    return super.onPreferenceTreeClick(preference)
  }
}
