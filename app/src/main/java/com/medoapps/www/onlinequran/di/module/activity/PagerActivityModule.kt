package com.medoapps.www.onlinequran.di.module.activity

import android.content.Context
import androidx.core.content.ContextCompat
import com.quran.data.core.QuranInfo
import com.quran.data.core.QuranPageInfo
import com.medoapps.www.onlinequran.R
import com.quran.data.di.ActivityScope
import com.medoapps.www.onlinequran.data.QuranDisplayData
import com.medoapps.www.onlinequran.ui.PagerActivity
import com.medoapps.www.onlinequran.ui.helpers.AyahSelectedListener
import com.medoapps.www.onlinequran.util.QuranPageInfoImpl
import com.medoapps.www.onlinequran.util.QuranScreenInfo
import com.medoapps.www.onlinequran.util.QuranUtils
import com.medoapps.www.onlinequran.util.TranslationUtil
import com.quran.mobile.di.AyahActionFragmentProvider
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet

@Module
class PagerActivityModule(private val pagerActivity: PagerActivity) {

  @Provides
  fun provideAyahSelectedListener(): AyahSelectedListener {
    return pagerActivity
  }

  @Provides
  fun provideQuranPageInfo(
    quranInfo: QuranInfo,
    quranDisplayData: QuranDisplayData
  ): QuranPageInfo {
    return QuranPageInfoImpl(pagerActivity, quranInfo, quranDisplayData)
  }

  @Provides
  @ActivityScope
  fun provideImageWidth(screenInfo: QuranScreenInfo): String {
    return if (QuranUtils.isDualPages(pagerActivity, screenInfo)) {
      screenInfo.tabletWidthParam
    } else {
      screenInfo.widthParam
    }
  }

  @Provides
  @ActivityScope
  fun provideTranslationUtil(context: Context, quranInfo: QuranInfo): TranslationUtil {
    return TranslationUtil(
      ContextCompat.getColor(context, R.color.translation_translator_color),
      quranInfo
    )
  }

  @Provides
  @ElementsIntoSet
  fun provideAdditionalAyahPanels(): Set<AyahActionFragmentProvider> {
    return emptySet()
  }
}
