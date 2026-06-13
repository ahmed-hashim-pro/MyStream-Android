package com.medoapps.www.onlinequran.athan;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import com.medoapps.www.onlinequran.R;

/**
 * Foreground service that plays the full athan audio when a prayer alarm
 * fires. Started by {@link AthanAlarmReceiver}; stopped by its own
 * notification action, audio-focus loss, playback completion, or error.
 */
public class AthanPlaybackService extends Service {

    public static final String ACTION_STOP = "com.medoapps.athan.STOP";

    private static final int NOTIF_ID = 5300;
    private static final long[] VIBRATE_PATTERN = {0, 500, 400, 500, 400, 500};

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest; // API 26+
    private AudioManager.OnAudioFocusChangeListener focusListener;
    private int currentIndex = -1;
    private long prayerTimeMillis;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopPlayback();
            stopSelf();
            return START_NOT_STICKY;
        }

        int index = (intent == null) ? PrayerSettings.PRAYER_FAJR
                : intent.getIntExtra(AthanScheduler.EXTRA_PRAYER_INDEX, PrayerSettings.PRAYER_FAJR);
        if (index < 0 || index >= PrayerSettings.PRAYER_COUNT) index = PrayerSettings.PRAYER_FAJR;
        currentIndex = index;
        prayerTimeMillis = (intent == null) ? System.currentTimeMillis()
                : intent.getLongExtra(AthanScheduler.EXTRA_PRAYER_TIME, System.currentTimeMillis());

        AthanAlarmReceiver.createChannels(this);
        startForeground(NOTIF_ID, buildNotification(index));

        stopPlayback();
        startPlayback(index);
        if (PrayerSettings.isVibrateEnabled(this)) {
            startVibration();
        }
        return START_NOT_STICKY;
    }

    private android.app.Notification buildNotification(int index) {
        String prayerName = getString(PrayerTimeEngine.PRAYER_NAME_RES[index]);
        Intent stopIntent = new Intent(this, AthanPlaybackService.class).setAction(ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getService(this, NOTIF_ID, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, AthanAlarmReceiver.CHANNEL_ATHAN)
                .setSmallIcon(R.drawable.ic_prayer_times)
                .setContentTitle(getString(R.string.athan_notif_title, prayerName))
                .setOngoing(true)
                .addAction(0, getString(R.string.athan_stop), stopPi)
                .build();
    }

    // ------------------------------------------------------------- playback

    private void startPlayback(int index) {
        Uri soundUri = resolveSoundUri(index);
        if (soundUri == null) {
            stopSelf();
            return;
        }

        if (!requestAudioFocus()) {
            stopSelf();
            return;
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioAttributes(alarmAttributes());
            mediaPlayer.setDataSource(this, soundUri);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopSelf();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    stopSelf();
                    return true;
                }
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            stopSelf();
        }
    }

    private Uri resolveSoundUri(int index) {
        String saved = PrayerSettings.getAthanSoundUri(this, index);
        if (!TextUtils.isEmpty(saved)) {
            return Uri.parse(saved);
        }
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (uri == null) {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        return uri;
    }

    private static AudioAttributes alarmAttributes() {
        return new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
    }

    // ---------------------------------------------------------- audio focus

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) return false;

        focusListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS
                        || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                        || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    stopPlayback();
                    stopSelf();
                }
            }
        };

        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(alarmAttributes())
                    .setOnAudioFocusChangeListener(focusListener)
                    .build();
            result = audioManager.requestAudioFocus(focusRequest);
        } else {
            result = audioManager.requestAudioFocus(focusListener,
                    AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonAudioFocus() {
        if (audioManager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (focusRequest != null) {
                audioManager.abandonAudioFocusRequest(focusRequest);
                focusRequest = null;
            }
        } else if (focusListener != null) {
            audioManager.abandonAudioFocus(focusListener);
        }
        focusListener = null;
    }

    // -------------------------------------------------------------- vibrate

    private void startVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = (vm == null) ? null : vm.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (vibrator == null || !vibrator.hasVibrator()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(VIBRATE_PATTERN, -1));
        } else {
            vibrator.vibrate(VIBRATE_PATTERN, -1);
        }
    }

    // ------------------------------------------------------------- teardown

    private void stopPlayback() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            } catch (IllegalStateException ignored) {
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        abandonAudioFocus();
    }

    @Override
    public void onDestroy() {
        stopPlayback();
        stopForeground(true);
        replaceAthanNotification();
        super.onDestroy();
    }

    /**
     * The receiver's athan notification carries a "Stop athan" action that is
     * dead once this service is gone. Re-post it without the action so the
     * prayer info (and dua) stays readable with no zombie button.
     */
    private void replaceAthanNotification() {
        if (currentIndex < 0) return;
        String prayerName = getString(PrayerTimeEngine.PRAYER_NAME_RES[currentIndex]);
        String timeAt = getString(R.string.athan_notif_time_at,
                PrayerTimeEngine.formatTime(this, new java.util.Date(prayerTimeMillis)));
        String bigText = timeAt;
        if (PrayerSettings.isDuaAfterAthanEnabled(this)) {
            bigText = timeAt + "\n\n" + getString(R.string.athan_dua_text);
        }
        Intent contentIntent = new Intent(this, com.medoapps.www.onlinequran.PrayerTimesActivity.class);
        PendingIntent contentPi = PendingIntent.getActivity(this, 0, contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        android.app.Notification finalNotif =
                new NotificationCompat.Builder(this, AthanAlarmReceiver.CHANNEL_ATHAN)
                        .setSmallIcon(R.drawable.ic_prayer_times)
                        .setContentTitle(getString(R.string.athan_notif_title, prayerName))
                        .setContentText(timeAt)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                        .setContentIntent(contentPi)
                        .setAutoCancel(true)
                        .build();
        android.app.NotificationManager nm =
                (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        try {
            nm.notify(AthanAlarmReceiver.NOTIF_ATHAN_BASE + currentIndex, finalNotif);
        } catch (SecurityException ignored) {
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
