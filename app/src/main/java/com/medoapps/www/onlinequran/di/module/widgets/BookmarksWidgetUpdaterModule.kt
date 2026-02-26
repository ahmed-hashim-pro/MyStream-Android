package com.medoapps.www.onlinequran.di.module.widgets

import com.medoapps.www.onlinequran.widget.BookmarksWidgetUpdater
import com.medoapps.www.onlinequran.widget.BookmarksWidgetUpdaterImpl
import dagger.Binds
import dagger.Module

@Module
interface BookmarksWidgetUpdaterModule {

  @Binds
  fun bindBookmarksWidgetUpdater(impl: BookmarksWidgetUpdaterImpl): BookmarksWidgetUpdater
}
