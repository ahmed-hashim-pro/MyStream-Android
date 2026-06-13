package com.medoapps.www.onlinequran.athan;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.medoapps.www.onlinequran.PrayerTimesActivity;
import com.medoapps.www.onlinequran.R;

import java.util.Date;

/**
 * Receives every alarm fired by {@link AthanScheduler} and turns it into the
 * right notification: the athan itself (optionally with full audio playback
 * through {@link AthanPlaybackService}), the pre-prayer reminder, the iqama
 * reminder, and the nightly maintenance reschedule.
 */
public class AthanAlarmReceiver extends BroadcastReceiver {

    static final String CHANNEL_ATHAN = "athan_full_v1";
    static final String CHANNEL_BEEP = "athan_beep_v1";
    static final String CHANNEL_SILENT = "athan_silent_v1";
    /** Carries the alarm sound itself, for when the playback service can't start. */
    static final String CHANNEL_FALLBACK = "athan_fallback_v1";

    static final int NOTIF_ATHAN_BASE = 5000;
    private static final int NOTIF_PRE_BASE = 5100;
    private static final int NOTIF_IQAMA_BASE = 5200;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        String action = intent.getAction();

        if (AthanScheduler.ACTION_MAINTENANCE.equals(action)) {
            AthanScheduler.rescheduleAll(context);
            return;
        }

        int index = intent.getIntExtra(AthanScheduler.EXTRA_PRAYER_INDEX, -1);
        long prayerTimeMillis = intent.getLongExtra(AthanScheduler.EXTRA_PRAYER_TIME, 0L);
        if (index < 0 || index >= PrayerSettings.PRAYER_COUNT) return;

        int mode = PrayerSettings.getNotificationMode(context, index);
        if (mode == PrayerSettings.MODE_OFF) return;

        // Sunrise is not a prayer: never play the full athan for it.
        if (index == PrayerSettings.PRAYER_SUNRISE && mode == PrayerSettings.MODE_ATHAN) {
            mode = PrayerSettings.MODE_BEEP;
        }

        createChannels(context);
        String prayerName = context.getString(PrayerTimeEngine.PRAYER_NAME_RES[index]);
        String timeAt = context.getString(R.string.athan_notif_time_at,
                PrayerTimeEngine.formatTime(context, new Date(prayerTimeMillis)));

        if (AthanScheduler.ACTION_ATHAN.equals(action)) {
            handleAthan(context, index, mode, prayerName, timeAt, prayerTimeMillis);
        } else if (AthanScheduler.ACTION_PRE_REMINDER.equals(action)) {
            String title = context.getString(R.string.athan_notif_pre_title,
                    prayerName, PrayerSettings.getPreReminderMinutes(context));
            notify(context, NOTIF_PRE_BASE + index,
                    baseBuilder(context, CHANNEL_BEEP, title, timeAt).build());
        } else if (AthanScheduler.ACTION_IQAMA.equals(action)) {
            handleIqama(context, index, prayerName, timeAt, prayerTimeMillis);
        }
    }

    private void handleIqama(Context context, int index,
                             String prayerName, String timeAt, long prayerTimeMillis) {
        AthanSound iq = AthanSound.byId(
                AthanSound.catalogForSlot(context, AthanSound.SLOT_IQAMA),
                PrayerSettings.getSoundId(context, AthanSound.SLOT_IQAMA));

        String title = context.getString(R.string.athan_notif_iqama_title, prayerName);
        notify(context, NOTIF_IQAMA_BASE + index,
                baseBuilder(context, CHANNEL_BEEP, title, timeAt).build());

        // Silent selection: notification only, no playback.
        if (iq == null || iq.type == AthanSound.Type.SILENT) return;

        Intent serviceIntent = new Intent(context, AthanPlaybackService.class)
                .putExtra(AthanScheduler.EXTRA_PRAYER_INDEX, index)
                .putExtra(AthanScheduler.EXTRA_PRAYER_TIME, prayerTimeMillis)
                .putExtra(AthanPlaybackService.EXTRA_KIND, AthanPlaybackService.KIND_IQAMA);
        try {
            ContextCompat.startForegroundService(context, serviceIntent);
        } catch (Exception e) {
            // Background FGS starts can be rejected on API 31+; re-post the
            // iqama notification on the fallback channel, which carries the
            // alarm sound itself.
            NotificationCompat.Builder fallback =
                    baseBuilder(context, CHANNEL_FALLBACK, title, timeAt);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                fallback.setSound(defaultAlarmUri(), android.media.AudioManager.STREAM_ALARM);
            }
            notify(context, NOTIF_IQAMA_BASE + index, fallback.build());
        }
    }

    private void handleAthan(Context context, int index, int mode,
                             String prayerName, String timeAt, long prayerTimeMillis) {
        String title = context.getString(R.string.athan_notif_title, prayerName);

        String bigText = timeAt;
        if (PrayerSettings.isDuaAfterAthanEnabled(context)) {
            bigText = timeAt + "\n\n" + context.getString(R.string.athan_dua_text);
        }

        if (mode == PrayerSettings.MODE_ATHAN) {
            // The playback service owns the rich notification (full-screen
            // intent + Stop + audio); don't post a duplicate here.
            Intent serviceIntent = new Intent(context, AthanPlaybackService.class)
                    .putExtra(AthanScheduler.EXTRA_PRAYER_INDEX, index)
                    .putExtra(AthanScheduler.EXTRA_PRAYER_TIME, prayerTimeMillis)
                    .putExtra(AthanPlaybackService.EXTRA_KIND, AthanPlaybackService.KIND_ATHAN);
            try {
                ContextCompat.startForegroundService(context, serviceIntent);
            } catch (Exception e) {
                // Background FGS starts can be rejected on API 31+ (notably when
                // the alarm degraded to inexact delivery). Post a sound-carrying,
                // full-screen fallback so the athan still alerts over apps.
                NotificationCompat.Builder fallback =
                        baseBuilder(context, CHANNEL_FALLBACK, title, timeAt);
                fallback.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));
                fallback.setCategory(NotificationCompat.CATEGORY_ALARM);
                fallback.setPriority(NotificationCompat.PRIORITY_HIGH);
                PendingIntent fsPi = fullScreenPi(context, index,
                        AthanPlaybackService.KIND_ATHAN, prayerTimeMillis);
                fallback.setFullScreenIntent(fsPi, true);
                fallback.setContentIntent(fsPi);
                fallback.addAction(0, context.getString(R.string.athan_stop),
                        stopServicePi(context, index));
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    fallback.setSound(defaultAlarmUri(), android.media.AudioManager.STREAM_ALARM);
                }
                notify(context, NOTIF_ATHAN_BASE + index, fallback.build());
            }
            return;
        }

        // MODE_BEEP / MODE_SILENT: notification only, no over-apps takeover.
        String channel = (mode == PrayerSettings.MODE_BEEP) ? CHANNEL_BEEP : CHANNEL_SILENT;
        NotificationCompat.Builder builder = baseBuilder(context, channel, title, timeAt);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));
        notify(context, NOTIF_ATHAN_BASE + index, builder.build());
    }

    /** Full-screen intent to the over-apps athan screen. */
    static PendingIntent fullScreenPi(Context context, int index, String kind, long prayerTimeMillis) {
        Intent fs = new Intent(context, com.medoapps.www.onlinequran.AthanAlarmActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra(com.medoapps.www.onlinequran.AthanAlarmActivity.EXTRA_PRAYER_INDEX, index)
                .putExtra(com.medoapps.www.onlinequran.AthanAlarmActivity.EXTRA_KIND, kind)
                .putExtra(com.medoapps.www.onlinequran.AthanAlarmActivity.EXTRA_PRAYER_TIME, prayerTimeMillis);
        return PendingIntent.getActivity(context, 5300 + index, fs,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static PendingIntent stopServicePi(Context context, int index) {
        Intent stop = new Intent(context, AthanPlaybackService.class).setAction(AthanPlaybackService.ACTION_STOP);
        return PendingIntent.getService(context, NOTIF_ATHAN_BASE + index, stop,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static android.net.Uri defaultAlarmUri() {
        android.net.Uri uri = android.media.RingtoneManager
                .getDefaultUri(android.media.RingtoneManager.TYPE_ALARM);
        return uri != null ? uri : android.media.RingtoneManager
                .getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION);
    }

    private NotificationCompat.Builder baseBuilder(Context context, String channelId,
                                                   String title, String text) {
        Intent contentIntent = new Intent(context, PrayerTimesActivity.class);
        PendingIntent contentPi = PendingIntent.getActivity(context, 0, contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_prayer_times)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentPi)
                .setAutoCancel(true);
    }

    private void notify(Context context, int id, android.app.Notification notification) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        try {
            nm.notify(id, notification);
        } catch (SecurityException ignored) {
            // Notification permission revoked on API 33+; nothing to do.
        }
    }

    /** Creates the three athan channels; safe to call repeatedly. */
    static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        // Full athan: sound comes from AthanPlaybackService, not the channel.
        NotificationChannel athan = new NotificationChannel(CHANNEL_ATHAN,
                context.getString(R.string.athan_channel_athan),
                NotificationManager.IMPORTANCE_HIGH);
        athan.setSound(null, null);
        athan.enableVibration(true);
        nm.createNotificationChannel(athan);

        NotificationChannel beep = new NotificationChannel(CHANNEL_BEEP,
                context.getString(R.string.athan_channel_reminders),
                NotificationManager.IMPORTANCE_HIGH);
        nm.createNotificationChannel(beep);

        NotificationChannel silent = new NotificationChannel(CHANNEL_SILENT,
                context.getString(R.string.athan_channel_silent),
                NotificationManager.IMPORTANCE_LOW);
        silent.setSound(null, null);
        nm.createNotificationChannel(silent);

        NotificationChannel fallback = new NotificationChannel(CHANNEL_FALLBACK,
                context.getString(R.string.athan_channel_athan),
                NotificationManager.IMPORTANCE_HIGH);
        fallback.setSound(defaultAlarmUri(), new android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
        fallback.enableVibration(true);
        nm.createNotificationChannel(fallback);
    }
}
