package com.medoapps.www.onlinequran;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class PrayerNotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "prayer_times_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String prayerName = intent.getStringExtra("prayer_name");
        if (prayerName == null) {
            prayerName = context.getString(R.string.notif_prayer_default_name);
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel on Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_prayer_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        // PendingIntent to open PrayerTimesActivity on tap
        Intent tapIntent = new Intent(context, PrayerTimesActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_prayer_times)
                .setContentTitle(context.getString(R.string.notif_prayer_alarm_title))
                .setContentText(prayerName)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent);

        int notificationId = prayerName.hashCode();
        notificationManager.notify(notificationId, builder.build());
    }
}
