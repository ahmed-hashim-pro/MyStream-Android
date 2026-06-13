package com.medoapps.www.onlinequran.athan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;
import java.util.Date;

/**
 * Schedules every athan-related alarm: the athan itself, the optional
 * pre-prayer reminder, the optional iqama reminder, and a nightly maintenance
 * alarm that rolls everything forward to the next day.
 *
 * Exact alarms are used when the SCHEDULE_EXACT_ALARM special access is
 * granted; otherwise alarms degrade to setAndAllowWhileIdle (inexact but
 * Doze-proof) instead of crashing — see PrayerAlarmScheduler history.
 */
public final class AthanScheduler {

    public static final String ACTION_ATHAN = "com.medoapps.athan.ATHAN";
    public static final String ACTION_PRE_REMINDER = "com.medoapps.athan.PRE_REMINDER";
    public static final String ACTION_IQAMA = "com.medoapps.athan.IQAMA";
    public static final String ACTION_MAINTENANCE = "com.medoapps.athan.MAINTENANCE";

    public static final String EXTRA_PRAYER_INDEX = "prayer_index";
    public static final String EXTRA_PRAYER_TIME = "prayer_time_millis";

    // Request-code bases; existing app alarms use 0-4, 999, 2001-2002, 3001-3003.
    private static final int RC_ATHAN_BASE = 5000;
    private static final int RC_PRE_BASE = 5100;
    private static final int RC_IQAMA_BASE = 5200;
    private static final int RC_MAINTENANCE = 5999;

    private AthanScheduler() {
    }

    /** Cancels and re-creates all alarms for the rest of today + maintenance. */
    public static void rescheduleAll(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        cancelAll(context, alarmManager);
        // Legacy alarms from the pre-athan PrayerAlarmScheduler (request codes
        // 0-4) survive app updates and would double-notify; clear them.
        com.medoapps.www.onlinequran.PrayerAlarmScheduler.cancelAllAlarms(context);

        boolean canUseExact = canUseExactAlarms(alarmManager);
        Date now = new Date();
        Date[] times = PrayerTimeEngine.getTodayTimes(context);
        int preMinutes = PrayerSettings.getPreReminderMinutes(context);
        int iqamaMinutes = PrayerSettings.getIqamaReminderMinutes(context);

        for (int i = 0; i < PrayerSettings.PRAYER_COUNT; i++) {
            int mode = PrayerSettings.getNotificationMode(context, i);
            if (mode == PrayerSettings.MODE_OFF) continue;

            Date prayerTime = times[i];
            if (prayerTime.after(now)) {
                schedule(context, alarmManager, canUseExact, RC_ATHAN_BASE + i,
                        ACTION_ATHAN, i, prayerTime.getTime());
            }

            if (preMinutes > 0) {
                long preAt = prayerTime.getTime() - preMinutes * 60_000L;
                if (preAt > now.getTime()) {
                    schedule(context, alarmManager, canUseExact, RC_PRE_BASE + i,
                            ACTION_PRE_REMINDER, i, prayerTime.getTime());
                }
            }

            // Iqama reminders only make sense for the five prayers
            if (iqamaMinutes > 0 && i != PrayerSettings.PRAYER_SUNRISE) {
                long iqamaAt = prayerTime.getTime() + iqamaMinutes * 60_000L;
                if (iqamaAt > now.getTime()) {
                    schedule(context, alarmManager, canUseExact, RC_IQAMA_BASE + i,
                            ACTION_IQAMA, i, prayerTime.getTime());
                }
            }
        }

        scheduleMaintenance(context, alarmManager);
        PrayerTimeEngine.updateWidgetCache(context);
    }

    /**
     * Fires a few minutes past midnight so the new day's alarms (starting
     * with fajr) are always in place even if the app is never opened.
     */
    private static void scheduleMaintenance(Context context, AlarmManager alarmManager) {
        Calendar next = Calendar.getInstance();
        next.add(Calendar.DAY_OF_YEAR, 1);
        next.set(Calendar.HOUR_OF_DAY, 0);
        next.set(Calendar.MINUTE, 5);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);

        Intent intent = new Intent(context, AthanAlarmReceiver.class).setAction(ACTION_MAINTENANCE);
        PendingIntent pi = PendingIntent.getBroadcast(context, RC_MAINTENANCE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
        } catch (SecurityException e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
        }
    }

    private static void schedule(Context context, AlarmManager alarmManager, boolean canUseExact,
                                 int requestCode, String action, int prayerIndex, long prayerTimeMillis) {
        long triggerAt;
        if (ACTION_PRE_REMINDER.equals(action)) {
            triggerAt = prayerTimeMillis - PrayerSettings.getPreReminderMinutes(context) * 60_000L;
        } else if (ACTION_IQAMA.equals(action)) {
            triggerAt = prayerTimeMillis + PrayerSettings.getIqamaReminderMinutes(context) * 60_000L;
        } else {
            triggerAt = prayerTimeMillis;
        }

        Intent intent = new Intent(context, AthanAlarmReceiver.class)
                .setAction(action)
                .putExtra(EXTRA_PRAYER_INDEX, prayerIndex)
                .putExtra(EXTRA_PRAYER_TIME, prayerTimeMillis);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        try {
            if (!canUseExact) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        } catch (SecurityException e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    private static void cancelAll(Context context, AlarmManager alarmManager) {
        for (int i = 0; i < PrayerSettings.PRAYER_COUNT; i++) {
            cancel(context, alarmManager, RC_ATHAN_BASE + i, ACTION_ATHAN);
            cancel(context, alarmManager, RC_PRE_BASE + i, ACTION_PRE_REMINDER);
            cancel(context, alarmManager, RC_IQAMA_BASE + i, ACTION_IQAMA);
        }
        cancel(context, alarmManager, RC_MAINTENANCE, ACTION_MAINTENANCE);
    }

    private static void cancel(Context context, AlarmManager alarmManager, int requestCode, String action) {
        Intent intent = new Intent(context, AthanAlarmReceiver.class).setAction(action);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pi);
    }

    /** Whether exact alarms are currently permitted. */
    public static boolean canUseExactAlarms(AlarmManager alarmManager) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                || alarmManager.canScheduleExactAlarms();
    }

    public static boolean canUseExactAlarms(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return am != null && canUseExactAlarms(am);
    }
}
