package com.medoapps.www.onlinequran.di.module.fragment

import dagger.Module
import dagger.Provides

@Module
class QuranPageModule(private vararg val pages: Int) {

  @Provides
  fun providePages(): IntArray {
    return pages
  }
}
