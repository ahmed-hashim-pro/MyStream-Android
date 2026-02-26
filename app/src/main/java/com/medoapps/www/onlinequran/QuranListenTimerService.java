package com.medoapps.www.onlinequran;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.medoapps.www.onlinequran.models.Post;

import java.util.ArrayList;
import java.util.Calendar;

public class QuranListenTimerService extends Service {

    public static AlarmManager am;
    public static PendingIntent pendingIntent;
    public static AlarmManager amForVideosNotification;
    public static PendingIntent pendingIntentForVideosNotification;


    ArrayList<Post> youtubeVideosArrayList;
    final int min = 0;
    int max = 0;
    private Post post;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;


    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        // do your jobs here
        reminder();
        reminderListenVideos();

        //Toast.makeText(this, "My Stream Is Running", Toast.LENGTH_SHORT).show();

        /*if (SettingSaved.SounlLoad==0) {

            SettingSaved.SounlLoad=1;//sound load
            //load sound
            final AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            final int originalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.sound);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer mp)
                {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                }
            });



        }else {

        }
        if (SettingSaved.IsOpen==0) {
            SettingSaved.IsOpen =  1;//App Is Opened

            Intent dialogIntent = new Intent(this, SplashScreen.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);




        }else {

        }
*/

        Thread myThread = new Thread(){
            @Override
            public void run() {
                try {

                    sleep(1000*60*1440);

                    onTaskRemoved(intent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        myThread.start();

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        timer ();
        Intent restartServiceIntent = new Intent (getApplicationContext(),this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
        timer ();

    }

    public void timer (){

        Thread myThread = new Thread(){
            @Override
            public void run() {
                try {

                    sleep(20000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        myThread.start();
    }

    void reminder(){

        Log.d("TAG", "reminder: ");

        // load setting informatin if we have
        SettingSaved settingSaved = new SettingSaved(this);
        settingSaved.LoadData();
        //start notification every day
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, SettingSaved.selectedHour);
        calendar.set(Calendar.MINUTE, SettingSaved.selectedMinute);
        calendar.set(Calendar.SECOND, 0);
        Intent intent1 = new Intent(QuranListenTimerService.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(QuranListenTimerService.this, 0,intent1, PendingIntent.FLAG_MUTABLE);
        am = (AlarmManager) QuranListenTimerService.this.getSystemService(QuranListenTimerService.this.ALARM_SERVICE);
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

        Intent intent1 = new Intent(QuranListenTimerService.this, AutoVideosNotificationReceiver.class);

        long interval = 60 * 1000; // 1 minute
        pendingIntentForVideosNotification = PendingIntent.getBroadcast(QuranListenTimerService.this, 0,intent1, PendingIntent.FLAG_MUTABLE);
        amForVideosNotification = (AlarmManager) QuranListenTimerService.this.getSystemService(QuranListenTimerService.this.ALARM_SERVICE);
        amForVideosNotification.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, pendingIntentForVideosNotification);


    }


    static void cancelReminder(){

        try {
            if (am!= null){
                am.cancel(pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
