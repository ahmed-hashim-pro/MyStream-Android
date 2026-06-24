package com.medoapps.www.onlinequran;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class HisnNotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "hisn_muslim_channel";
    private static final int NOTIFICATION_ID = 1005;

    @Override
    public void onReceive(Context context, Intent intent) {
        String[] dua = HisnAlMuslimActivity.getRandomDua();
        // dua[0] = category title, dua[1] = arabic text, dua[2] = source

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, context.getString(R.string.notif_hisn_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.notif_hisn_channel_desc));
            nm.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, HisnAlMuslimActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_hisn)
                .setContentTitle(context.getString(R.string.notif_hisn_content_title, dua[0]))
                .setContentText(dua[1])
                .setSubText(dua[2])
                .setStyle(new NotificationCompat.BigTextStyle().bigText(dua[1]))
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setContentIntent(pi);

        nm.notify(NOTIFICATION_ID, builder.build());
    }
}
