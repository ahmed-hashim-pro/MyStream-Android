package com.medoapps.www.onlinequran.ui.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.recyclerview.widget.RecyclerView
import com.quran.data.source.PageProvider
import com.medoapps.www.onlinequran.QuranAdvancedPreferenceActivity
import com.medoapps.www.onlinequran.QuranApplication
import com.medoapps.www.onlinequran.QuranPreferenceActivity
import com.medoapps.www.onlinequran.R
import com.medoapps.www.onlinequran.data.Constants
import com.medoapps.www.onlinequran.pageselect.PageSelectActivity
import com.medoapps.www.onlinequran.ui.AudioManagerActivity
import com.medoapps.www.onlinequran.ui.TranslationManagerActivity
import com.medoapps.www.onlinequran.ui.helpers.PreferenceScreenChrome
import com.medoapps.www.onlinequran.ui.preference.GoldValuePreference
import com.medoapps.www.onlinequran.util.QuranSettings
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

    // gold chip-framed icons on every row (mockup .prow .ic)
    PreferenceScreenChrome.chipIcons(requireContext(), preferenceScreen)

    // always-visible gold "Requires Dual page mode" caption (mockup 13a)
    findPreference<Preference>(getString(R.string.prefs_split_page_and_translation))?.let { pref ->
      val gold = ContextCompat.getColor(requireContext(), R.color.gold_accent)
      val builder = SpannableStringBuilder(pref.summary ?: "")
      if (builder.isNotEmpty()) builder.append("\n")
      val start = builder.length
      builder.append(getString(R.string.prefs_requires_dual_page))
      builder.setSpan(ForegroundColorSpan(gold), start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      builder.setSpan(StyleSpan(Typeface.BOLD), start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      builder.setSpan(RelativeSizeSpan(0.85f), start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      pref.summary = builder
    }
  }

  override fun onCreateRecyclerView(
    inflater: LayoutInflater,
    parent: ViewGroup,
    savedInstanceState: Bundle?
  ): RecyclerView {
    // section cards + side padding (mockup 12/13 .card grouping)
    return super.onCreateRecyclerView(inflater, parent, savedInstanceState)
      .also { PreferenceScreenChrome.style(it) }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // the cards draw their own hairlines; drop the default list dividers
    setDivider(null)
  }

  /** Trailing gold value on the Page Type row (mockup .prow .val). */
  private fun updatePageTypeValue() {
    val pref = findPreference<Preference>(Constants.PREF_PAGE_TYPE) as? GoldValuePreference ?: return
    val provider = pageTypes[QuranSettings.getInstance(requireContext()).pageType] ?: return
    pref.currentValue = getString(provider.getPreviewTitle())
  }

  override fun onResume() {
    super.onResume()
    preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    // refresh after returning from PageSelectActivity
    updatePageTypeValue()
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
