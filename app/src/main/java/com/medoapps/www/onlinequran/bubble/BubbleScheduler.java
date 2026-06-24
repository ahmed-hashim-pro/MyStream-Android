// app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleScheduler.java
package com.medoapps.www.onlinequran.bubble;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

import com.medoapps.www.onlinequran.athan.PrayerSettings;
import com.medoapps.www.onlinequran.athan.PrayerTimeEngine;

import java.util.Date;

/** Arms alarms at the day/night window boundaries (Fajr, Asr) and starts/stops the
 *  bubble service for the current moment. Request codes 3100 (show) / 3101 (hide). */
public final class BubbleScheduler {
    private BubbleScheduler() {}
    private static final int RC_BOUNDARY = 3100;
    public static final String ACTION_APPLY = "com.medoapps.athkar.bubble.APPLY";

    public static void reschedule(Context ctx) {
        applyNow(ctx);
        BubblePrefs bp = new BubblePrefs(ctx);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = boundaryPi(ctx);
        am.cancel(pi);
        if (!bp.isEnabled() || bp.isAlwaysOn()) return; // no boundary alarms needed
        try {
            Date[] t = PrayerTimeEngine.getTodayTimes(ctx);
            long now = System.currentTimeMillis();
            long fajr = t[PrayerSettings.PRAYER_FAJR].getTime();
            long asr = t[PrayerSettings.PRAYER_ASR].getTime();
            long next = now < fajr ? fajr : (now < asr ? asr : fajr + 24L * 3600_000L);
            am.set(AlarmManager.RTC, next, pi);
        } catch (Exception ignored) {}
    }

    public static void applyNow(Context ctx) {
        BubblePrefs bp = new BubblePrefs(ctx);
        Intent svc = new Intent(ctx, AthkarBubbleService.class);
        boolean shouldShow = bp.isEnabled() && (bp.isAlwaysOn() || inWindow(ctx));
        if (shouldShow) ContextCompat.startForegroundService(ctx, svc);
        else ctx.startService(svc.setAction(AthkarBubbleService.ACTION_STOP));
    }

    private static boolean inWindow(Context ctx) {
        // Day & Night windows == always at least one session is active, so the bubble is
        // shown whenever enabled. A future tightening (e.g. only N minutes around Fajr/Asr)
        // would change this predicate; today both windows together cover the full day.
        return true;
    }

    private static PendingIntent boundaryPi(Context ctx) {
        Intent i = new Intent(ctx, BubbleBootReceiver.class).setAction(ACTION_APPLY);
        return PendingIntent.getBroadcast(ctx, RC_BOUNDARY, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
