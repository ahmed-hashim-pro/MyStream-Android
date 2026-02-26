package com.medoapps.www.onlinequran;

import static android.app.PendingIntent.getActivity;
import static android.os.Build.VERSION.SDK_INT;
import static com.medoapps.www.onlinequran.managerdb.ReciteNameText;
import static com.medoapps.www.onlinequran.managerdb.instance;
import static com.medoapps.www.onlinequran.managerdb.notificationTitle;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.ImageButton;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

/**
 * Created by MEDO on 11/01/2018.
 */

public class NotificationPanel {

    private Context parent;
    public static   NotificationManager nManager;
    public static NotificationCompat.Builder nBuilder;
    public static RemoteViews remoteView;
    public static ImageButton playnotificationplay;
    public static String CHANNEL_ID = "HASHIM_CHANNEL-ID_02";

    private void createNotificationChannel(Context parent) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = parent.getString(R.string.channel_name_2);
            String description = parent.getString(R.string.channel_description_2);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = parent.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public NotificationPanel(Context parent) {
        createNotificationChannel(parent);
        // TODO Auto-generated constructor stub
        this.parent = parent;
        nBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(parent,CHANNEL_ID)
                .setContentTitle("القرءان الكريم")
                .setSmallIcon(getNotificationIcon())
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(null)
                .setOngoing(true);


        remoteView = new RemoteViews(parent.getPackageName(), R.layout.notificatonview);
        remoteView.setTextViewText(R.id.aya,notificationTitle);
        remoteView.setTextViewText(R.id.recite, ReciteNameText);



        //set the button listeners
        setListeners(remoteView);
        nBuilder.setContent(remoteView);

        nManager = (NotificationManager) parent.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(500, nBuilder.build());


    }

    public void setListeners(RemoteViews view){
        //listener 1 for play pause action
        Intent volume = new Intent(parent,NotificationService.class);
        volume.setAction(NotificationService.ACTION1);
        PendingIntent btn1 = PendingIntent.getBroadcast(parent, 0, volume, PendingIntent.FLAG_IMMUTABLE);
        view.setOnClickPendingIntent(R.id.btn1, btn1);

        //listener 2 for next action
        Intent stop = new Intent(parent, NotificationService.class);
        stop.setAction(NotificationService.ACTION2);
        PendingIntent btn2 = PendingIntent.getBroadcast(parent, 1, stop, PendingIntent.FLAG_IMMUTABLE);
        view.setOnClickPendingIntent(R.id.btn2, btn2);

        //listener 3 for preveous action
        Intent prev = new Intent(parent, NotificationService.class);
        prev.setAction(NotificationService.ACTION3);
        PendingIntent btn3 = PendingIntent.getBroadcast(parent, 2, prev, PendingIntent.FLAG_IMMUTABLE);
        view.setOnClickPendingIntent(R.id.btn3, btn3);

        //listener 4 for forward action
        Intent forward = new Intent(parent, NotificationService.class);
        forward.setAction(NotificationService.ACTION4);
        PendingIntent btn4 = PendingIntent.getBroadcast(parent, 3, forward, PendingIntent.FLAG_IMMUTABLE);
        view.setOnClickPendingIntent(R.id.btn4, btn4);

        //listener 5 for backward action
        Intent backward = new Intent(parent, NotificationService.class);
        backward.setAction(NotificationService.ACTION5);
        PendingIntent btn5 = PendingIntent.getBroadcast(parent, 4, backward, PendingIntent.FLAG_IMMUTABLE);
        view.setOnClickPendingIntent(R.id.btn5, btn5);
/*
        //listener 6 for open activity managerdp action
        Intent openactivity = new Intent(parent, NotificationService.class);
        openactivity.setAction(NotificationService.ACTION6);
        PendingIntent btn6 = PendingIntent.getBroadcast(parent, 5, openactivity, 0);
        view.setOnClickPendingIntent(R.id.btn6, btn6);
*/
        //listener 7 for close notification action
        Intent close = new Intent(parent, NotificationService.class);
        close.setAction(NotificationService.ACTION7);
        PendingIntent btn7 = PendingIntent.getBroadcast(parent, 6, close, PendingIntent.FLAG_IMMUTABLE);
        view.setOnClickPendingIntent(R.id.btn7, btn7);


        //listener 8
        Intent volume1 = new Intent(parent,NotificationReturnSlot.class);
        volume1.putExtra("DO", "volume");
        PendingIntent btn6 = getActivity(parent, 5, volume1, 0);
        view.setOnClickPendingIntent(R.id.btn6, btn6);


    }

    public  void notificationCancel() {
        nManager.cancel(500);
    }
    public static void ubdateNotification(){

        remoteView.setTextViewText(R.id.aya,notificationTitle);
        remoteView.setTextViewText(R.id.recite, ReciteNameText);
        if(instance.mp.isPlaying()){

            if(instance.mp!=null){

                instance.runAdAgain(true);
                remoteView.setImageViewResource(R.id.btn1, R.drawable.ic_pause_circle_filled_black_24dp);
                //remoteView.setViewVisibility(R.id.btn1,View.INVISIBLE);

            }
        }else{

            // Resume song
            if(instance.mp!=null){

                instance.runAdAgain(false);
                remoteView.setImageViewResource(R.id.btn1, R.drawable.ic_play_circle_filled_white_24dp);
                //remoteView.setViewVisibility(R.id.btn1,View.INVISIBLE);


            }
        }
        nManager.notify(500, nBuilder.build());

    }
    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.mystreamwhite : R.drawable.mystream;
    }
}