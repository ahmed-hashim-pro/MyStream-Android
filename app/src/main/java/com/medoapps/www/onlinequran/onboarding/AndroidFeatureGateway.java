package com.medoapps.www.onlinequran.onboarding;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import com.medoapps.www.onlinequran.AsmaulHusnaScheduler;
import com.medoapps.www.onlinequran.AthkarAlarmScheduler;
import com.medoapps.www.onlinequran.DailyAyahScheduler;
import com.medoapps.www.onlinequran.DailyHadithScheduler;
import com.medoapps.www.onlinequran.FastingReminderScheduler;
import com.medoapps.www.onlinequran.HisnNotificationScheduler;
import com.medoapps.www.onlinequran.SettingSaved;
import com.medoapps.www.onlinequran.athan.AthanScheduler;
import com.medoapps.www.onlinequran.athan.PrayerSettings;
import com.medoapps.www.onlinequran.bubble.AthkarBubbleService;
import com.medoapps.www.onlinequran.bubble.BubblePrefs;
import com.medoapps.www.onlinequran.bubble.BubbleScheduler;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

/** Production {@link FeatureGateway} backed by the app's existing schedulers. */
public class AndroidFeatureGateway implements FeatureGateway {

    private final Context context;

    public AndroidFeatureGateway(Context context) {
        // use application context — these writes outlive the activity
        this.context = context.getApplicationContext();
    }

    @Override
    public void setAthanEnabled(boolean enabled) {
        PrayerSettings.setAthanFeatureEnabled(context, enabled);
        // rescheduleAll cancels everything when the feature is off and
        // (re)schedules with the Doze-proof exact/inexact fallback when on.
        AthanScheduler.rescheduleAll(context);
    }

    @Override
    public void setBubbleEnabled(boolean enabled) {
        BubblePrefs bp = new BubblePrefs(context);
        if (enabled) {
            // Only start the bubble if it can actually draw over apps; the Ready step surfaces the
            // overlay grant, but the user may have skipped it. Leaving it off avoids a "running"
            // bubble that can never appear.
            boolean canOverlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    || Settings.canDrawOverlays(context);
            if (canOverlay) {
                bp.setEnabled(true);
                bp.setDismissUntil(0L); // explicit enable overrides any prior dismissal
                ContextCompat.startForegroundService(context,
                        new Intent(context, AthkarBubbleService.class));
                BubbleScheduler.reschedule(context);
            } else {
                bp.setEnabled(false);
            }
        } else {
            boolean wasEnabled = bp.isEnabled();
            bp.setEnabled(false);
            // Only message the service if it could be running; avoids starting it just to stop it.
            if (wasEnabled) {
                context.startService(new Intent(context, AthkarBubbleService.class)
                        .setAction(AthkarBubbleService.ACTION_STOP));
            }
        }
    }

    @Override
    public void enableReminder(Reminder reminder) {
        switch (reminder) {
            case DAILY_AYAH:
                DailyAyahScheduler.scheduleDailyAyah(context, reminder.defaultHour, reminder.defaultMinute);
                context.getSharedPreferences("daily_ayah_prefs", Context.MODE_PRIVATE)
                        .edit().putBoolean("notification_enabled", true).apply();
                break;
            case MORNING_ATHKAR:
                AthkarAlarmScheduler.scheduleMorning(context, reminder.defaultHour, reminder.defaultMinute);
                break;
            case EVENING_ATHKAR:
                AthkarAlarmScheduler.scheduleEvening(context, reminder.defaultHour, reminder.defaultMinute);
                break;
            case DUA_HISN:
                HisnNotificationScheduler.schedule(context, reminder.defaultHour, reminder.defaultMinute);
                break;
            case DAILY_HADITH:
                DailyHadithScheduler.scheduleDailyHadith(context, reminder.defaultHour, reminder.defaultMinute);
                context.getSharedPreferences("daily_hadith_prefs", Context.MODE_PRIVATE)
                        .edit().putBoolean("notification_enabled", true).apply();
                break;
            case ASMAUL_HUSNA:
                AsmaulHusnaScheduler.schedule(context, reminder.defaultHour, reminder.defaultMinute);
                break;
            case SUHOOR_FASTING:
                FastingReminderScheduler.schedule(context, reminder.defaultHour, reminder.defaultMinute);
                break;
        }
    }

    @Override
    public void disableReminder(Reminder reminder) {
        switch (reminder) {
            case DAILY_AYAH:
                DailyAyahScheduler.cancel(context);
                context.getSharedPreferences("daily_ayah_prefs", Context.MODE_PRIVATE)
                        .edit().putBoolean("notification_enabled", false).apply();
                break;
            case MORNING_ATHKAR:  AthkarAlarmScheduler.cancelMorning(context); break;
            case EVENING_ATHKAR:  AthkarAlarmScheduler.cancelEvening(context); break;
            case DUA_HISN:        HisnNotificationScheduler.cancel(context); break;
            case DAILY_HADITH:
                DailyHadithScheduler.cancel(context);
                context.getSharedPreferences("daily_hadith_prefs", Context.MODE_PRIVATE)
                        .edit().putBoolean("notification_enabled", false).apply();
                break;
            case ASMAUL_HUSNA:    AsmaulHusnaScheduler.cancel(context); break;
            case SUHOOR_FASTING:  FastingReminderScheduler.cancel(context); break;
        }
    }

    @Override
    public void setPageType(String pageType) {
        com.medoapps.www.onlinequran.util.QuranSettings settings =
                com.medoapps.www.onlinequran.util.QuranSettings.getInstance(context);
        // getPageType() is null on a fresh install (the pref is only backfilled
        // by the lazy DI provider, which onboarding never touches) - compare
        // null-safely and never write a null type
        if (pageType != null && !pageType.equals(settings.getPageType())) {
            // Mirrors the page-select switch, minus bookmark migration: onboarding
            // runs before any bookmarks (or pages) exist. Clearing the downloaded-pages
            // flag makes the data activity check/download the newly chosen set.
            settings.removeDidDownloadPages();
            settings.setPageType(pageType);
        }
    }

    @Override
    public String currentPageType() {
        return com.medoapps.www.onlinequran.util.QuranSettings.getInstance(context).getPageType();
    }

    @Override
    public void setThemeMode(int nightMode) {
        SettingSaved.currentThemeMode = nightMode;
        new SettingSaved(context).SaveData();
        new SeparateFunctions(context).changeAppThemeGlobally();
    }

    @Override
    public boolean isAthanEnabled() {
        return PrayerSettings.isAthanFeatureEnabled(context);
    }

    @Override
    public boolean isBubbleEnabled() {
        return new BubblePrefs(context).isEnabled();
    }

    @Override
    public boolean isReminderEnabled(Reminder reminder) {
        switch (reminder) {
            case DAILY_AYAH:
                return context.getSharedPreferences("daily_ayah_prefs", Context.MODE_PRIVATE)
                        .getBoolean("notification_enabled", false);
            case MORNING_ATHKAR:  return AthkarAlarmScheduler.isMorningEnabled(context);
            case EVENING_ATHKAR:  return AthkarAlarmScheduler.isEveningEnabled(context);
            case DUA_HISN:        return HisnNotificationScheduler.isEnabled(context);
            case DAILY_HADITH:
                return context.getSharedPreferences("daily_hadith_prefs", Context.MODE_PRIVATE)
                        .getBoolean("notification_enabled", false);
            case ASMAUL_HUSNA:    return AsmaulHusnaScheduler.isEnabled(context);
            case SUHOOR_FASTING:  return FastingReminderScheduler.isEnabled(context);
        }
        return false;
    }

    @Override
    public int currentThemeMode() {
        SettingSaved s = new SettingSaved(context);
        s.LoadData();
        return SettingSaved.currentThemeMode;
    }
}
