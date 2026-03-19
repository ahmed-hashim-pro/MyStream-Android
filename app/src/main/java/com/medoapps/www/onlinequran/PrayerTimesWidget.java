package com.medoapps.www.onlinequran;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PrayerTimesWidget extends AppWidgetProvider {

    private static final String ACTION_UPDATE = "com.medoapps.UPDATE_PRAYER_WIDGET";

    private static final String[] PRAYER_KEYS = {"fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha"};
    private static final String[] PRAYER_NAMES_AR = {"الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء"};

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName widget = new ComponentName(context, PrayerTimesWidget.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widget);
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("prayer_times_cache", Context.MODE_PRIVATE);

        String[] times = new String[6];
        for (int i = 0; i < PRAYER_KEYS.length; i++) {
            times[i] = prefs.getString(PRAYER_KEYS[i], "--:--");
        }

        // Determine the next prayer
        int nextPrayerIndex = findNextPrayer(times);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_prayer_times);

        // Static label
        views.setTextViewText(R.id.tv_next_prayer_label, "الصلاة القادمة");

        // Next prayer name and time
        views.setTextViewText(R.id.tv_next_prayer_name, PRAYER_NAMES_AR[nextPrayerIndex]);
        views.setTextViewText(R.id.tv_next_prayer_time, times[nextPrayerIndex]);

        // All times (exclude sunrise for compact display)
        // Indices: 0=fajr, 1=sunrise, 2=dhuhr, 3=asr, 4=maghrib, 5=isha
        String allTimes = PRAYER_NAMES_AR[0] + " " + times[0]
                + " | " + PRAYER_NAMES_AR[2] + " " + times[2]
                + " | " + PRAYER_NAMES_AR[3] + " " + times[3]
                + " | " + PRAYER_NAMES_AR[4] + " " + times[4]
                + " | " + PRAYER_NAMES_AR[5] + " " + times[5];
        views.setTextViewText(R.id.tv_all_times, allTimes);

        // PendingIntent to open PrayerTimesActivity
        Intent intent = new Intent(context, PrayerTimesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_prayer_root, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static int findNextPrayer(String[] times) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
        Calendar now = Calendar.getInstance();
        String currentTime = sdf.format(now.getTime());

        try {
            Date current = sdf.parse(currentTime);
            for (int i = 0; i < times.length; i++) {
                if ("--:--".equals(times[i])) continue;
                // Skip sunrise (index 1) for "next prayer" — it's not a prayer
                if (i == 1) continue;
                Date prayerTime = sdf.parse(times[i]);
                if (prayerTime != null && current != null && prayerTime.after(current)) {
                    return i;
                }
            }
        } catch (ParseException e) {
            // fallback
        }

        // All prayers have passed, show Fajr (tomorrow)
        return 0;
    }
}
