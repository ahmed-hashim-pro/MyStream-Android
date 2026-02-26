package com.medoapps.www.onlinequran.ui.helpers

import com.quran.data.model.selection.SelectionIndicator
import com.medoapps.www.onlinequran.common.LocalTranslation
import com.medoapps.www.onlinequran.common.QuranAyahInfo

interface AyahTracker {
  fun getToolBarPosition(sura: Int, ayah: Int): SelectionIndicator
  fun getQuranAyahInfo(sura: Int, ayah: Int): QuranAyahInfo?
  fun getLocalTranslations(): Array<LocalTranslation>?
}
