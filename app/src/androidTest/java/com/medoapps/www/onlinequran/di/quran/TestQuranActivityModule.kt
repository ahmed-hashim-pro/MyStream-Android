package com.medoapps.www.onlinequran.di.quran

import com.medoapps.www.onlinequran.presenter.data.QuranIndexEventLogger
import dagger.Module
import dagger.Provides

@Module
class TestQuranActivityModule {

  @Provides
  fun bindQuranIndexEventLogger(): QuranIndexEventLogger {
    return QuranIndexEventLogger { }
  }
}
