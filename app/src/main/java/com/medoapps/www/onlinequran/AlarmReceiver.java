package com.medoapps.www.onlinequran;

import static android.os.Build.VERSION.SDK_INT;
import static com.facebook.FacebookSdk.getApplicationContext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    int MID=200;
    String CHANNEL_ID = "HASHIM_CHANNEL-ID_01";

    @Override
    public void onReceive(Context context, Intent intent) {
        StorageUtil storage = new StorageUtil(getApplicationContext());
        if (storage.loadYoutubeVideos() == null ){
            return;
        }
        createNotificationChannel(context);
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, RecitesName.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_MUTABLE);


        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(
                context,CHANNEL_ID).setSmallIcon(getNotificationIcon())
                .setContentTitle("My Stream")
                .setContentText("استمع للورد اليومي").setSound(alarmSound)
                .setAutoCancel(true).setWhen(when)
                .setContentIntent(createContentIntent(context))
                ;

        notificationManager.notify(MID, mNotifyBuilder.build());
        MID++;


    }
    private PendingIntent createContentIntent(Context context) {
        Intent openUI = new Intent(context, NewQuranPlayer.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        openUI.putExtra(MusicPlayerActivity.EXTRA_START_FULLSCREEN, true);
        openUI.putExtra("RecitesName","mohammed_siddiq_alminshawi_mojawwad");
        openUI.putExtra("Rewayat","المصحف المجود");
        openUI.putExtra("RealRecitesName","المصحف المجود");
        openUI.putExtra("RecitesAYA",String.valueOf(0));
        openUI.putExtra("IsRadio",true);
        openUI.putExtra("isStartFromNotification",false);

        return PendingIntent.getActivity(context, 026, openUI,
                PendingIntent.FLAG_IMMUTABLE);
    }
    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.mystreamwhite : R.drawable.mystream;
    }

}
