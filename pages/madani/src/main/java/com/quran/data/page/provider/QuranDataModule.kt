package com.quran.data.page.provider

import com.quran.common.upgrade.LocalDataUpgrade
import com.quran.common.upgrade.PreferencesUpgrade
import com.quran.data.page.provider.madani.MadaniPageProvider
import com.quran.data.page.provider.madinacolored.MadinaColoredPageProvider
import com.quran.data.page.provider.naskh.NaskhPageProvider
import com.quran.data.page.provider.newmadani.NewMadaniPageProvider
import com.quran.data.page.provider.qaloon.QaloonPageProvider
import com.quran.data.page.provider.shemerly.ShemerlyPageProvider
import com.quran.data.page.provider.tajweed.TajweedPageProvider
import com.quran.data.page.provider.warsh.WarshPageProvider
import com.quran.data.pageinfo.mapper.AyahMapper
import com.quran.data.pageinfo.mapper.IdentityAyahMapper
import com.quran.data.source.PageProvider
import com.quran.page.common.draw.ImageDrawHelper
import com.quran.page.common.factory.PageViewFactoryProvider
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
object QuranDataModule {

  @Provides
  fun providePageViewFactoryProvider(): PageViewFactoryProvider {
    return PageViewFactoryProvider { null }
  }

  @JvmStatic
  @Provides
  @IntoMap
  @StringKey("madani")
  fun provideMadaniPageSet(): PageProvider {
    return MadaniPageProvider()
  }

  @JvmStatic
  @Provides
  @IntoMap
  @StringKey("tajweed")
  fun provideTajweedPageSet(): PageProvider {
    return TajweedPageProvider()
  }

  @JvmStatic
  @Provides
  @IntoMap
  @StringKey("naskh")
  fun provideNaskhPageSet(): PageProvider {
    return NaskhPageProvider()
  }

  @JvmStatic
  @Provides
  @IntoMap
  @StringKey("shemerly")
  fun provideShemerlyPageSet(): PageProvider {
    return ShemerlyPageProvider()
  }

  @JvmStatic
  @Provides
  @IntoMap
  @StringKey("new_madani")
  fun provideNewMadaniPageSet(): PageProvider {
    return NewMadaniPageProvider()
  }

  @JvmStatic
  @Provides
  @IntoMap
  @StringKey("qaloon")
  fun provideQaloonPageSet(): PageProvider {
    return QaloonPageProvider()
  }

  @JvmStatic
  @Provides
  @IntoMap
  @StringKey("warsh")
  fun provideWarshPageSet(): PageProvider {
    return WarshPageProvider()
  }

  @JvmStatic
  @Provides
  @IntoMap
  @StringKey("madina_colored")
  fun provideMadinaColoredPageSet(): PageProvider {
    return MadinaColoredPageProvider()
  }

  @JvmStatic
  @Provides
  @ElementsIntoSet
  fun provideImageDrawHelpers(): Set<ImageDrawHelper> {
    return emptySet()
  }

  @JvmStatic
  @Provides
  fun provideLocalDataUpgrade(): LocalDataUpgrade = object : LocalDataUpgrade {  }

  @JvmStatic
  @Provides
  fun providePreferencesUpgrade(): PreferencesUpgrade = PreferencesUpgrade { _, _, _ -> true }

  @JvmStatic
  @Reusable
  @Provides
  fun provideAyahMapper(): AyahMapper = IdentityAyahMapper()
}
