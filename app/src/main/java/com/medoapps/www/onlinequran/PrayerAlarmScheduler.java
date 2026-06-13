package com.medoapps.www.onlinequran;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class PrayerAlarmScheduler {

    private static final String[] PRAYER_NAMES = {
            "صلاة الفجر",
            "صلاة الظهر",
            "صلاة العصر",
            "صلاة المغرب",
            "صلاة العشاء"
    };

    /**
     * Schedule alarms for all five prayers (excluding sunrise).
     * Each time string should be in "HH:mm" format.
     * If a prayer time has already passed today, it is skipped.
     *
     * @param context  Application or activity context
     * @param fajr     Fajr time in HH:mm
     * @param sunrise  Sunrise time (not scheduled, included for API compatibility)
     * @param dhuhr    Dhuhr time in HH:mm
     * @param asr      Asr time in HH:mm
     * @param maghrib  Maghrib time in HH:mm
     * @param isha     Isha time in HH:mm
     */
    public static void schedulePrayerAlarms(Context context, String fajr, String sunrise,
                                            String dhuhr, String asr, String maghrib, String isha) {
        String[] times = {fajr, dhuhr, asr, maghrib, isha};

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Since Android 12 exact alarms need the SCHEDULE_EXACT_ALARM special access,
        // which Android 14+ denies by default; calling setExact* without it throws
        // SecurityException. Fall back to windowed alarms when not granted.
        boolean canUseExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                || alarmManager.canScheduleExactAlarms();

        Calendar now = Calendar.getInstance();

        for (int i = 0; i < times.length; i++) {
            String time = times[i];
            if (time == null || time.isEmpty()) continue;

            // Strip any timezone info like " (EET)" that the API might return
            String cleanTime = time.trim();
            int spaceIndex = cleanTime.indexOf(' ');
            if (spaceIndex > 0) {
                cleanTime = cleanTime.substring(0, spaceIndex);
            }

            String[] parts = cleanTime.split(":");
            if (parts.length < 2) continue;

            int hour, minute;
            try {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                continue;
            }

            Calendar prayerTime = Calendar.getInstance();
            prayerTime.set(Calendar.HOUR_OF_DAY, hour);
            prayerTime.set(Calendar.MINUTE, minute);
            prayerTime.set(Calendar.SECOND, 0);
            prayerTime.set(Calendar.MILLISECOND, 0);

            // Skip if this prayer time has already passed today
            if (prayerTime.before(now)) {
                continue;
            }

            Intent intent = new Intent(context, PrayerNotificationReceiver.class);
            intent.putExtra("prayer_name", PRAYER_NAMES[i]);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    i, // unique request code 0-4 for fajr through isha
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            try {
                if (!canUseExact) {
                    // Inexact, but still fires during Doze — a windowed alarm could be
                    // deferred until the next maintenance window. Only reachable on
                    // API 31+, so setAndAllowWhileIdle (API 23+) needs no guard.
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            prayerTime.getTimeInMillis(),
                            pendingIntent
                    );
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            prayerTime.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            prayerTime.getTimeInMillis(),
                            pendingIntent
                    );
                }
            } catch (SecurityException e) {
                // Exact-alarm access revoked between the check above and this call;
                // deliver inexactly rather than crash.
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        prayerTime.getTimeInMillis(),
                        pendingIntent
                );
            }
        }
    }

    /**
     * Cancel all five prayer alarms.
     *
     * @param context Application or activity context
     */
    public static void cancelAllAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        for (int i = 0; i < 5; i++) {
            Intent intent = new Intent(context, PrayerNotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    i,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(pendingIntent);
        }
    }
}
