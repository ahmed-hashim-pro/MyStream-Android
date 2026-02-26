package com.medoapps.www.onlinequran.base

import com.medoapps.www.onlinequran.QuranApplication
import com.medoapps.www.onlinequran.di.DaggerTestApplicationComponent
import com.medoapps.www.onlinequran.di.component.application.ApplicationComponent
import com.medoapps.www.onlinequran.di.module.application.ApplicationModule

class TestApplication : QuranApplication() {

  override fun initializeInjector(): ApplicationComponent {
    return DaggerTestApplicationComponent.builder()
        .applicationModule(ApplicationModule(this))
        .build()
  }

  override fun setupTimber() {
  }
}
