package com.medoapps.www.onlinequran

import timber.log.Timber
import timber.log.Timber.DebugTree

class DebugApplication : QuranApplication() {
  override fun setupTimber() {
    Timber.plant(DebugTree())
  }
}
