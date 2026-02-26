package com.medoapps.www.onlinequran.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.medoapps.www.onlinequran.QuranDataActivity
import com.medoapps.www.onlinequran.R
import com.medoapps.www.onlinequran.SearchActivity
import com.medoapps.www.onlinequran.ShortcutsActivity
import com.medoapps.www.onlinequran.ui.QuranActivity

/**
 * Widget that displays buttons for:
 * - searching the Quran
 * - jumping into the app
 * - jumping to the most recent page
 * - jumping to a particular Ayah
 */
class SearchWidget : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    // There may be multiple widgets active, so update all of them
    for (appWidgetId in appWidgetIds) {
      val widget = RemoteViews(context.packageName, R.layout.search_widget)

      var intent = Intent(context, QuranDataActivity::class.java)
      var pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
      widget.setOnClickPendingIntent(R.id.search_widget_icon_button, pendingIntent)

      intent = Intent(context, SearchActivity::class.java)
      pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
      widget.setOnClickPendingIntent(R.id.search_widget_btn_search, pendingIntent)

      intent = Intent(context, QuranActivity::class.java)
      intent.action = ShortcutsActivity.ACTION_JUMP_TO_LATEST
      pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
      widget.setOnClickPendingIntent(R.id.search_widget_btn_go_to_quran, pendingIntent)

      intent = Intent(context, ShowJumpFragmentActivity::class.java)
      pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
      widget.setOnClickPendingIntent(R.id.search_widget_btn_jump, pendingIntent)

      appWidgetManager.updateAppWidget(appWidgetId, widget)
    }
    super.onUpdate(context, appWidgetManager, appWidgetIds)
  }
}
