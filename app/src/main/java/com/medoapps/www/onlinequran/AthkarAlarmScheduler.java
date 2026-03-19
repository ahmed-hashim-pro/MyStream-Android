package com.medoapps.www.onlinequran;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class AthkarAlarmScheduler {

    private static final String PREFS = "athkar_notification_prefs";
    private static final int MORNING_REQUEST_CODE = 2001;
    private static final int EVENING_REQUEST_CODE = 2002;

    public static void scheduleMorning(Context context, int hour, int minute) {
        save(context, "morning_hour", hour, "morning_minute", minute, "morning_enabled", true);
        schedule(context, hour, minute, AthkarNotificationReceiver.TYPE_MORNING, MORNING_REQUEST_CODE);
    }

    public static void scheduleEvening(Context context, int hour, int minute) {
        save(context, "evening_hour", hour, "evening_minute", minute, "evening_enabled", true);
        schedule(context, hour, minute, AthkarNotificationReceiver.TYPE_EVENING, EVENING_REQUEST_CODE);
    }

    public static void cancelMorning(Context context) {
        cancel(context, AthkarNotificationReceiver.TYPE_MORNING, MORNING_REQUEST_CODE);
        getPrefs(context).edit().putBoolean("morning_enabled", false).apply();
    }

    public static void cancelEvening(Context context) {
        cancel(context, AthkarNotificationReceiver.TYPE_EVENING, EVENING_REQUEST_CODE);
        getPrefs(context).edit().putBoolean("evening_enabled", false).apply();
    }

    public static void restoreAlarms(Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs.getBoolean("morning_enabled", false)) {
            int h = prefs.getInt("morning_hour", 6);
            int m = prefs.getInt("morning_minute", 0);
            schedule(context, h, m, AthkarNotificationReceiver.TYPE_MORNING, MORNING_REQUEST_CODE);
        }
        if (prefs.getBoolean("evening_enabled", false)) {
            int h = prefs.getInt("evening_hour", 17);
            int m = prefs.getInt("evening_minute", 0);
            schedule(context, h, m, AthkarNotificationReceiver.TYPE_EVENING, EVENING_REQUEST_CODE);
        }
    }

    public static int getMorningHour(Context context) { return getPrefs(context).getInt("morning_hour", 6); }
    public static int getMorningMinute(Context context) { return getPrefs(context).getInt("morning_minute", 0); }
    public static boolean isMorningEnabled(Context context) { return getPrefs(context).getBoolean("morning_enabled", false); }

    public static int getEveningHour(Context context) { return getPrefs(context).getInt("evening_hour", 17); }
    public static int getEveningMinute(Context context) { return getPrefs(context).getInt("evening_minute", 0); }
    public static boolean isEveningEnabled(Context context) { return getPrefs(context).getBoolean("evening_enabled", false); }

    private static void schedule(Context context, int hour, int minute, String type, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        PendingIntent pi = buildPendingIntent(context, type, requestCode);
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pi);
    }

    private static void cancel(Context context, String type, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        alarmManager.cancel(buildPendingIntent(context, type, requestCode));
    }

    private static PendingIntent buildPendingIntent(Context context, String type, int requestCode) {
        Intent intent = new Intent(context, AthkarNotificationReceiver.class);
        intent.putExtra(AthkarNotificationReceiver.EXTRA_TYPE, type);
        return PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void save(Context context, String hourKey, int hour, String minKey, int minute, String enabledKey, boolean enabled) {
        getPrefs(context).edit()
                .putInt(hourKey, hour)
                .putInt(minKey, minute)
                .putBoolean(enabledKey, enabled)
                .apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
