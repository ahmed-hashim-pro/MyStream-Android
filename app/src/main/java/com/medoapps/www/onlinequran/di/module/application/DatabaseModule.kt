package com.medoapps.www.onlinequran.di.module.application

import com.quran.data.dao.BookmarksDao
import com.quran.data.dao.TranslationsDao
import com.medoapps.www.onlinequran.database.BookmarksDaoImpl
import com.medoapps.www.onlinequran.database.TranslationsDaoImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DatabaseModule {

  @Provides
  @Singleton
  fun provideBookamrksDao(daoImpl: BookmarksDaoImpl): BookmarksDao {
    return daoImpl
  }

  @Provides
  @Singleton
  fun provideTranslationsDao(daoImpl: TranslationsDaoImpl): TranslationsDao {
    return daoImpl
  }
}
