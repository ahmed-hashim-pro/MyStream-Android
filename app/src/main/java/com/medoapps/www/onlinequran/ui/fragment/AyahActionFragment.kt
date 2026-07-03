package com.medoapps.www.onlinequran.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.quran.data.model.SuraAyah
import com.quran.data.model.selection.AyahSelection
import com.quran.data.model.selection.endSuraAyah
import com.quran.data.model.selection.startSuraAyah
import com.quran.reading.common.AudioEventPresenter
import com.quran.reading.common.ReadingEventPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

abstract class AyahActionFragment : Fragment() {
  private var scope: CoroutineScope = MainScope()

  @Inject
  lateinit var readingEventPresenter: ReadingEventPresenter

  @Inject
  lateinit var audioEventPresenter: AudioEventPresenter

  protected var start: SuraAyah? = null
  protected var end: SuraAyah? = null
  // true when start/end came from a deliberate ayah selection; false when they
  // merely track the currently-playing ayah. Subclasses use this to tell a
  // fresh user pick apart from passive playback-following.
  protected var startedFromManualSelection: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    scope = MainScope()
    readingEventPresenter.ayahSelectionFlow
      .combine(audioEventPresenter.audioPlaybackAyahFlow) { selectedAyah, playbackAyah ->
        if (selectedAyah !is AyahSelection.None) {
          start = selectedAyah.startSuraAyah()
          end = selectedAyah.endSuraAyah()
          startedFromManualSelection = true
        } else if (playbackAyah != null) {
          start = playbackAyah
          end = playbackAyah
          startedFromManualSelection = false
        }
        refreshView()
      }
      .launchIn(scope)

    readingEventPresenter.detailsPanelFlow
      .map {
        onToggleDetailsPanel(it)
      }
      .launchIn(scope)
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }

  open fun onToggleDetailsPanel(isVisible: Boolean) { }

  protected abstract fun refreshView()
}
