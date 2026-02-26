package com.medoapps.www.onlinequran.di.module.activity

import com.medoapps.www.onlinequran.presenter.data.QuranIndexEventLogger
import com.medoapps.www.onlinequran.presenter.data.QuranIndexEventLoggerImpl
import dagger.Binds
import dagger.Module

@Module
interface QuranActivityModule {
  @Binds
  fun bindQuranIndexEventLogger(impl: QuranIndexEventLoggerImpl): QuranIndexEventLogger
}
