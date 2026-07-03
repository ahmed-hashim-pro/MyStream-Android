package com.medoapps.www.onlinequran

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.medoapps.www.onlinequran.ui.fragment.QuranSettingsFragment
import com.medoapps.www.onlinequran.util.AudioManagerUtils

class QuranPreferenceActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    (application as QuranApplication).refreshLocale(this, false)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.preferences)

    // Navy hero (sets the navy status bar itself); keep the cream nav bar below.
    window.navigationBarColor = ContextCompat.getColor(this, R.color.background_main)
    HeroController.attach(this)
      .back()
      .compact()
      .title(R.string.menu_settings)
      .subtitle(getString(R.string.prefs_settings_subtitle))
      .apply()

    AudioManagerUtils.clearCache()

    val fm = supportFragmentManager
    val fragment = fm.findFragmentById(R.id.content)
    if (fragment == null) {
      fm.beginTransaction()
        .replace(R.id.content, QuranSettingsFragment())
        .commit()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      finish()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  fun restartActivity() {
    (application as QuranApplication).refreshLocale(this, true)
    val intent = intent
    finish()
    startActivity(intent)
  }

}
