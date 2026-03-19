package com.medoapps.www.onlinequran;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class AsmaulHusnaScheduler {

    private static final String PREFS = "asmaul_husna_notification_prefs";
    private static final int REQUEST_CODE = 3001;

    public static void schedule(Context context, int hour, int minute) {
        getPrefs(context).edit()
                .putInt("hour", hour)
                .putInt("minute", minute)
                .putBoolean("enabled", true)
                .apply();
        setAlarm(context, hour, minute);
    }

    public static void cancel(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(buildPI(context));
        getPrefs(context).edit().putBoolean("enabled", false).apply();
    }

    public static void restoreAlarm(Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs.getBoolean("enabled", false)) {
            setAlarm(context, prefs.getInt("hour", 9), prefs.getInt("minute", 0));
        }
    }

    public static int getHour(Context context) { return getPrefs(context).getInt("hour", 9); }
    public static int getMinute(Context context) { return getPrefs(context).getInt("minute", 0); }
    public static boolean isEnabled(Context context) { return getPrefs(context).getBoolean("enabled", false); }

    private static void setAlarm(Context context, int hour, int minute) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, buildPI(context));
    }

    private static PendingIntent buildPI(Context context) {
        Intent intent = new Intent(context, AsmaulHusnaNotificationReceiver.class);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
