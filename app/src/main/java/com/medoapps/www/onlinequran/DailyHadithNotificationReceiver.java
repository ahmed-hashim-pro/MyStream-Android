package com.medoapps.www.onlinequran;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

/**
 * BroadcastReceiver triggered daily by AlarmManager to show a hadith notification.
 * Mirrors the pattern of DailyAyahNotificationReceiver.
 */
public class DailyHadithNotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "daily_hadith_channel";
    private static final int NOTIFICATION_ID = 1002;

    @Override
    public void onReceive(Context context, Intent intent) {
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        int index = dayOfYear % DailyHadithActivity.getHadithCount();
        String[] hadith = DailyHadithActivity.getHadith(index);

        String hadithText = hadith[0];
        String hadithSource = hadith[1];

        showNotification(context, hadithText, hadithSource);
    }

    private void showNotification(Context context, String hadithText, String hadithSource) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_hadith_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(context.getString(R.string.notif_hadith_channel_description));
            notificationManager.createNotificationChannel(channel);
        }

        // PendingIntent to open DailyHadithActivity on tap
        Intent openIntent = new Intent(context, DailyHadithActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_hadith)
                .setContentTitle(context.getString(R.string.notif_hadith_title))
                .setContentText(hadithText)
                .setSubText(hadithSource)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(hadithText))
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
