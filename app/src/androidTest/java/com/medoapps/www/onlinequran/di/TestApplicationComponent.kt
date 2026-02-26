package com.medoapps.www.onlinequran.di

import com.quran.analytics.provider.AnalyticsModule
import com.quran.common.networking.NetworkModule
import com.quran.data.di.AppScope
import com.quran.data.page.provider.QuranDataModule
import com.medoapps.www.onlinequran.core.worker.di.WorkerModule
import com.medoapps.www.onlinequran.di.component.application.ApplicationComponent
import com.medoapps.www.onlinequran.di.module.application.ApplicationModule
import com.medoapps.www.onlinequran.di.module.application.DatabaseModule
import com.medoapps.www.onlinequran.di.module.application.PageAggregationModule
import com.medoapps.www.onlinequran.di.module.widgets.BookmarksWidgetUpdaterModule
import com.medoapps.www.onlinequran.di.quran.TestQuranActivityComponent
import com.squareup.anvil.annotations.MergeComponent
import javax.inject.Singleton

@Singleton
@MergeComponent(
  AppScope::class,
  modules = [
    AnalyticsModule::class,
    ApplicationModule::class,
    DatabaseModule::class,
    NetworkModule::class,
    PageAggregationModule::class,
    QuranDataModule::class,
    WorkerModule::class,
    BookmarksWidgetUpdaterModule::class
  ]
)
interface TestApplicationComponent : ApplicationComponent {
  override fun quranActivityComponentBuilder(): TestQuranActivityComponent.Builder
}
