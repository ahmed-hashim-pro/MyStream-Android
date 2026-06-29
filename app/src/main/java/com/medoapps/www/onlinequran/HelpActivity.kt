package com.medoapps.www.onlinequran

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.medoapps.www.onlinequran.R

class HelpActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.help)
    findViewById<ImageButton>(R.id.help_back)?.setOnClickListener { finish() }

    val helpText = findViewById<TextView>(R.id.txtHelp)
    helpText.text = HtmlCompat.fromHtml(getString(R.string.help), HtmlCompat.FROM_HTML_MODE_COMPACT)
  }
}
