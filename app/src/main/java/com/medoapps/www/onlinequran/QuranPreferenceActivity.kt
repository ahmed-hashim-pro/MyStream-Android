package com.medoapps.www.onlinequran

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.medoapps.www.onlinequran.ui.fragment.QuranSettingsFragment
import com.medoapps.www.onlinequran.util.AudioManagerUtils

class QuranPreferenceActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    (application as QuranApplication).refreshLocale(this, false)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.preferences)

    window.statusBarColor = ContextCompat.getColor(this, R.color.background_main)
    window.navigationBarColor = ContextCompat.getColor(this, R.color.background_main)

    val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
    toolbar.setTitle(R.string.menu_settings)
    setSupportActionBar(toolbar)
    val ab = supportActionBar
    ab?.setDisplayHomeAsUpEnabled(true)

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
