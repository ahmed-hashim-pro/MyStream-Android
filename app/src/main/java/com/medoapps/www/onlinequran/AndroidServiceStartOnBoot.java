package com.medoapps.www.onlinequran;

import static com.medoapps.www.onlinequran.QuranListenTimerService.am;
import static com.medoapps.www.onlinequran.QuranListenTimerService.amForVideosNotification;
import static com.medoapps.www.onlinequran.QuranListenTimerService.pendingIntent;
import static com.medoapps.www.onlinequran.QuranListenTimerService.pendingIntentForVideosNotification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.Calendar;


/**
 * Created by MEDO on 12/09/2017.
 */

public class AndroidServiceStartOnBoot extends Service {
    private static final String EXTRA_SIM_STATE = "ss";
    private Intent intent;
    private Context context;

    @Override
    public void onCreate() {

        reminder();
        reminderListenVideos();
       /* Intent dialogIntent = new Intent(this, SplashScreen.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);*/

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        reminder();
        reminderListenVideos();
        /*Intent dialogIntent = new Intent(this, SplashScreen.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);*/



//        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
//        Toast.makeText(this, "Service Destroy", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    void reminder(){


        // load setting informatin if we have
        SettingSaved settingSaved = new SettingSaved(this);
        settingSaved.LoadData();
        //start notification every day
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, SettingSaved.selectedHour);
        calendar.set(Calendar.MINUTE, SettingSaved.selectedMinute);
        calendar.set(Calendar.SECOND, 0);
        Intent intent1 = new Intent(AndroidServiceStartOnBoot.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(AndroidServiceStartOnBoot.this, 0,intent1, PendingIntent.FLAG_MUTABLE);
        am = (AlarmManager) AndroidServiceStartOnBoot.this.getSystemService(AndroidServiceStartOnBoot.this.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);



    }
    void reminderListenVideos(){


        // load setting informatin if we have
        SettingSaved settingSaved = new SettingSaved(this);
        settingSaved.LoadData();
        //start notification every day
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, SettingSaved.selectedHour);
        calendar.set(Calendar.MINUTE, SettingSaved.selectedMinute);
        calendar.set(Calendar.SECOND, 0);

        Intent intent1 = new Intent(AndroidServiceStartOnBoot.this, AutoVideosNotificationReceiver.class);

        long interval = 60 * 1000; // 1 minute
        pendingIntentForVideosNotification = PendingIntent.getBroadcast(AndroidServiceStartOnBoot.this, 0,intent1, PendingIntent.FLAG_MUTABLE);
        amForVideosNotification = (AlarmManager) AndroidServiceStartOnBoot.this.getSystemService(AndroidServiceStartOnBoot.this.ALARM_SERVICE);
        amForVideosNotification.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, pendingIntentForVideosNotification);


    }
}
