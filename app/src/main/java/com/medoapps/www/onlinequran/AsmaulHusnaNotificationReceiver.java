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

public class AsmaulHusnaNotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "asmaul_husna_channel";
    private static final int NOTIFICATION_ID = 1003;

    @Override
    public void onReceive(Context context, Intent intent) {
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        int index = dayOfYear % AsmaulHusnaActivity.getNameCount();
        String[] name = AsmaulHusnaActivity.getName(index);

        String title = context.getString(R.string.notif_asma_title, name[0]);
        String body = name[1];

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, context.getString(R.string.notif_asma_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.notif_asma_channel_desc));
            nm.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, AsmaulHusnaActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_asmaul_husna)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setContentIntent(pi);

        nm.notify(NOTIFICATION_ID, builder.build());
    }
}
