package com.medoapps.www.onlinequran.di.component.activity

import com.quran.data.di.QuranReadingScope
import com.quran.data.di.ActivityScope
import com.medoapps.www.onlinequran.di.component.fragment.QuranPageComponent
import com.medoapps.www.onlinequran.di.module.activity.PagerActivityModule
import com.medoapps.www.onlinequran.ui.PagerActivity
import com.medoapps.www.onlinequran.ui.fragment.AyahPlaybackFragment
import com.medoapps.www.onlinequran.ui.fragment.AyahTranslationFragment
import com.medoapps.www.onlinequran.ui.fragment.TagBookmarkFragment
import com.quran.page.common.toolbar.AyahToolBar
import com.quran.mobile.di.QuranReadingActivityComponent
import com.squareup.anvil.annotations.MergeSubcomponent
import dagger.Subcomponent

@ActivityScope
@MergeSubcomponent(QuranReadingScope::class, modules = [PagerActivityModule::class])
interface PagerActivityComponent : QuranReadingActivityComponent {
  // subcomponents
  fun quranPageComponentBuilder(): QuranPageComponent.Builder

  fun inject(pagerActivity: PagerActivity)
  fun inject(ayahToolBar: AyahToolBar)

  fun inject(tagBookmarkFragment: TagBookmarkFragment)
  fun inject(ayahPlaybackFragment: AyahPlaybackFragment)
  fun inject(ayahTranslationFragment: AyahTranslationFragment)

  @Subcomponent.Builder
  interface Builder {
    fun withPagerActivityModule(pagerModule: PagerActivityModule): Builder
    fun build(): PagerActivityComponent
  }
}
