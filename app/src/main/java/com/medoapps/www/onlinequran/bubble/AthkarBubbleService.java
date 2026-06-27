// app/src/main/java/com/medoapps/www/onlinequran/bubble/AthkarBubbleService.java
package com.medoapps.www.onlinequran.bubble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.medoapps.www.onlinequran.R;

/** Foreground service that hosts the floating athkar overlay. Type: specialUse. */
public class AthkarBubbleService extends Service {
    public static final String ACTION_STOP = "com.medoapps.athkar.bubble.STOP";
    /** Rebuild the overlay in place (e.g. after a style change) without a stop/start race. */
    public static final String ACTION_REFRESH = "com.medoapps.athkar.bubble.REFRESH";
    static final int NOTIF_ID = 4000;
    static final String CHANNEL = "bubble_channel";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_STOP.equals(action)) {
            detachOverlay();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        createChannel(this);
        startForeground(NOTIF_ID, buildNotification());
        boolean refresh = ACTION_REFRESH.equals(action);
        if (refresh) {
            detachOverlay(); // tear down the old overlay so the new style/session is rebuilt below
        }
        boolean newlyShown = attachOverlay();
        // Chime only when the bubble genuinely appears (athkar time / enable), not on an in-place refresh.
        if (newlyShown && !refresh) maybePlaySound();
        return START_STICKY;
    }

    private Notification buildNotification() {
        Intent stop = new Intent(this, AthkarBubbleService.class).setAction(ACTION_STOP);
        android.app.PendingIntent stopPi = android.app.PendingIntent.getService(
                this, NOTIF_ID, stop,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_nav_more) // existing small icon; swap if a dedicated one is added
                .setContentText(getString(R.string.bubble_notif_text))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(0, getString(android.R.string.cancel), stopPi)
                .build();
    }

    static void createChannel(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        NotificationChannel ch = new NotificationChannel(
                CHANNEL, ctx.getString(R.string.bubble_channel_name), NotificationManager.IMPORTANCE_LOW);
        ch.setShowBadge(false);
        nm.createNotificationChannel(ch);
    }

    private BubbleOverlayController overlay;

    // --- overlay hooks ---
    /** @return true iff the bubble was newly added (i.e. it wasn't already showing). */
    private boolean attachOverlay() {
        if (overlay == null) overlay = new BubbleOverlayController(this);
        return overlay.show();
    }

    /** Plays the short appear-sound, honoring the user's on/off pref. Uses the notification
     *  usage so it respects notification volume / Do Not Disturb. */
    private void maybePlaySound() {
        if (!new BubblePrefs(this).isSoundOn()) return;
        try {
            MediaPlayer mp;
            android.media.AudioManager am = (android.media.AudioManager) getSystemService(AUDIO_SERVICE);
            if (am != null) {
                android.media.AudioAttributes attrs = new android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                mp = MediaPlayer.create(this, R.raw.sound, attrs, am.generateAudioSessionId());
            } else {
                mp = MediaPlayer.create(this, R.raw.sound);
            }
            if (mp != null) {
                mp.setOnCompletionListener(MediaPlayer::release);
                mp.start();
            }
        } catch (Exception ignored) {}
    }
    private void detachOverlay() {
        if (overlay != null) overlay.hide();
    }

    @Override public void onDestroy() { detachOverlay(); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
