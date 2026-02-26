package com.medoapps.www.onlinequran.di.component.fragment

import com.quran.data.di.QuranPageScope
import com.quran.data.di.QuranReadingPageScope
import com.quran.data.page.provider.di.QuranPageExtrasComponent
import com.medoapps.www.onlinequran.di.module.fragment.QuranPageModule
import com.medoapps.www.onlinequran.ui.fragment.QuranPageFragment
import com.medoapps.www.onlinequran.ui.fragment.TabletFragment
import com.medoapps.www.onlinequran.ui.fragment.TranslationFragment
import com.squareup.anvil.annotations.MergeSubcomponent
import dagger.Subcomponent

@QuranPageScope
@MergeSubcomponent(QuranReadingPageScope::class, modules = [QuranPageModule::class])
interface QuranPageComponent: QuranPageExtrasComponent {
  fun inject(quranPageFragment: QuranPageFragment)
  fun inject(tabletFragment: TabletFragment)
  fun inject(translationFragment: TranslationFragment)

  @Subcomponent.Builder
  interface Builder {
    fun withQuranPageModule(quranPageModule: QuranPageModule): Builder
    fun build(): QuranPageComponent
  }
}
