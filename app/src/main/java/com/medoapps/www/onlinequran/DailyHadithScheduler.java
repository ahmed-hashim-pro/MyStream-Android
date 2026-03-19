package com.medoapps.www.onlinequran;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Utility class to schedule / cancel the daily hadith notification alarm.
 * Mirrors the pattern of DailyAyahScheduler with its own SharedPreferences and request code.
 */
public class DailyHadithScheduler {

    private static final int REQUEST_CODE = 1000;

    /**
     * Schedules a daily alarm using the previously saved time (default 8:00 AM).
     */
    public static void scheduleDailyHadith(Context context) {
        android.content.SharedPreferences prefs =
                context.getSharedPreferences("daily_hadith_prefs", Context.MODE_PRIVATE);
        int hour = prefs.getInt("notification_hour", 8);
        int minute = prefs.getInt("notification_minute", 0);
        scheduleDailyHadith(context, hour, minute);
    }

    /**
     * Schedules a daily alarm at the given hour and minute.
     * If the specified time has already passed today, the first trigger will be tomorrow.
     * Uses setInexactRepeating with INTERVAL_DAY for battery efficiency.
     */
    public static void scheduleDailyHadith(Context context, int hour, int minute) {
        // Save the chosen time
        context.getSharedPreferences("daily_hadith_prefs", Context.MODE_PRIVATE).edit()
                .putInt("notification_hour", hour)
                .putInt("notification_minute", minute)
                .apply();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = buildPendingIntent(context);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    /**
     * Cancels the daily hadith alarm.
     */
    public static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = buildPendingIntent(context);
        alarmManager.cancel(pendingIntent);
    }

    private static PendingIntent buildPendingIntent(Context context) {
        Intent intent = new Intent(context, DailyHadithNotificationReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
