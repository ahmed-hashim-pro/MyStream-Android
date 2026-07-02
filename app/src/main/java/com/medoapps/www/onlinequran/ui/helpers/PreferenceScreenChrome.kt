package com.medoapps.www.onlinequran.ui.helpers

import android.content.Context
import android.graphics.drawable.LayerDrawable
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceGroup
import androidx.recyclerview.widget.RecyclerView
import com.medoapps.www.onlinequran.R

/**
 * Shared navy+gold chrome for the settings screens (mockups 12/13), used by
 * both QuranSettingsFragment and QuranAdvancedSettingsFragment.
 */
object PreferenceScreenChrome {

  /** 12dp side padding + rounded section cards behind the preference rows. */
  @JvmStatic
  fun style(recyclerView: RecyclerView) {
    val context = recyclerView.context
    val density = context.resources.displayMetrics.density
    val pad = (12 * density).toInt()
    recyclerView.setPaddingRelative(pad, 0, pad, pad)
    recyclerView.clipToPadding = false
    recyclerView.addItemDecoration(
      PreferenceCardDecoration(
        density,
        ContextCompat.getColor(context, R.color.background_card),
        ContextCompat.getColor(context, R.color.row_divider_hairline),
        ContextCompat.getColor(context, R.color.row_divider_hairline)
      )
    )
  }

  /**
   * Wraps every row icon in the 38dp gold-faint chip (mockup .prow .ic), with a
   * gold tint that dims when the row is dependency-disabled.
   */
  @JvmStatic
  fun chipIcons(context: Context, group: PreferenceGroup) {
    val tint = ContextCompat.getColorStateList(context, R.color.pref_icon_tint)
    val inset = (8 * context.resources.displayMetrics.density).toInt()
    for (i in 0 until group.preferenceCount) {
      val preference = group.getPreference(i)
      if (preference is PreferenceGroup) {
        chipIcons(context, preference)
        continue
      }
      val icon = preference.icon ?: continue
      if (icon is LayerDrawable) continue // already chipped
      icon.mutate().setTintList(tint)
      val chip = ContextCompat.getDrawable(context, R.drawable.bg_pref_icon_chip) ?: continue
      val layer = LayerDrawable(arrayOf(chip, icon))
      layer.setLayerInset(1, inset, inset, inset, inset)
      preference.icon = layer
    }
  }
}
