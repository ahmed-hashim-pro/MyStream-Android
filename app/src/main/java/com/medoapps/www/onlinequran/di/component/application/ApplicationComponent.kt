package com.medoapps.www.onlinequran.di.component.application

import com.quran.analytics.provider.AnalyticsModule
import com.quran.common.networking.NetworkModule
import com.quran.data.di.AppScope
import com.quran.data.page.provider.QuranDataModule
import com.medoapps.www.onlinequran.QuranApplication
import com.medoapps.www.onlinequran.QuranDataActivity
import com.medoapps.www.onlinequran.QuranForwarderActivity
import com.medoapps.www.onlinequran.QuranImportActivity
import com.medoapps.www.onlinequran.SearchActivity
import com.medoapps.www.onlinequran.core.worker.di.WorkerModule
import com.medoapps.www.onlinequran.data.QuranDataProvider
import com.medoapps.www.onlinequran.di.component.activity.PagerActivityComponent
import com.medoapps.www.onlinequran.di.component.activity.QuranActivityComponent
import com.medoapps.www.onlinequran.di.module.application.ApplicationModule
import com.medoapps.www.onlinequran.di.module.application.DatabaseModule
import com.medoapps.www.onlinequran.di.module.application.PageAggregationModule
import com.medoapps.www.onlinequran.di.module.widgets.BookmarksWidgetUpdaterModule
import com.medoapps.www.onlinequran.pageselect.PageSelectActivity
import com.medoapps.www.onlinequran.service.AudioService
import com.medoapps.www.onlinequran.service.QuranDownloadService
import com.medoapps.www.onlinequran.ui.AudioManagerActivity
import com.medoapps.www.onlinequran.ui.SheikhAudioManagerActivity
import com.medoapps.www.onlinequran.ui.TranslationManagerActivity
import com.medoapps.www.onlinequran.ui.fragment.AddTagDialog
import com.medoapps.www.onlinequran.ui.fragment.BookmarksFragment
import com.medoapps.www.onlinequran.ui.fragment.JumpFragment
import com.medoapps.www.onlinequran.ui.fragment.JuzListFragment
import com.medoapps.www.onlinequran.ui.fragment.QuranAdvancedSettingsFragment
import com.medoapps.www.onlinequran.ui.fragment.QuranSettingsFragment
import com.medoapps.www.onlinequran.ui.fragment.SuraListFragment
import com.medoapps.www.onlinequran.ui.fragment.TagBookmarkDialog
import com.medoapps.www.onlinequran.widget.BookmarksWidget
import com.medoapps.www.onlinequran.widget.BookmarksWidgetListProvider
import com.medoapps.www.onlinequran.widget.ShowJumpFragmentActivity
import com.quran.mobile.di.QuranApplicationComponent
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
interface ApplicationComponent: QuranApplicationComponent {
  // subcomponents
  fun pagerActivityComponentBuilder(): PagerActivityComponent.Builder
  fun quranActivityComponentBuilder(): QuranActivityComponent.Builder

  // application
  fun inject(quranApplication: QuranApplication)

  // content provider
  fun inject(quranDataProvider: QuranDataProvider)

  // services
  fun inject(audioService: AudioService)
  fun inject(quranDownloadService: QuranDownloadService)

  // activities
  fun inject(quranDataActivity: QuranDataActivity)
  fun inject(quranImportActivity: QuranImportActivity)
  fun inject(audioManagerActivity: AudioManagerActivity)
  fun inject(sheikhAudioManagerActivity: SheikhAudioManagerActivity)
  fun inject(quranForwarderActivity: QuranForwarderActivity)
  fun inject(searchActivity: SearchActivity)
  fun inject(pageSelectActivity: PageSelectActivity)
  fun inject(showJumpFragmentActivity: ShowJumpFragmentActivity)

  // fragments
  fun inject(bookmarksFragment: BookmarksFragment)
  fun inject(fragment: QuranSettingsFragment)
  fun inject(translationManagerActivity: TranslationManagerActivity)
  fun inject(quranAdvancedSettingsFragment: QuranAdvancedSettingsFragment)
  fun inject(suraListFragment: SuraListFragment)
  fun inject(juzListFragment: JuzListFragment)
  fun inject(jumpFragment: JumpFragment)

  // dialogs
  fun inject(tagBookmarkDialog: TagBookmarkDialog)
  fun inject(addTagDialog: AddTagDialog)

  // widgets
  fun inject(bookmarksWidgetListProvider: BookmarksWidgetListProvider)
  fun inject(bookmarksWidget: BookmarksWidget)
}
