package com.medoapps.www.onlinequran.di.component.activity

import com.medoapps.www.onlinequran.di.module.activity.QuranActivityModule
import com.medoapps.www.onlinequran.ui.QuranActivity
import dagger.Subcomponent

@Subcomponent(modules = [QuranActivityModule::class])
interface QuranActivityComponent {
  fun inject(quranActivity: QuranActivity)

  @Subcomponent.Builder
  interface Builder {
    fun build(): QuranActivityComponent
  }
}
