package com.medoapps.www.onlinequran;

import static com.medoapps.www.onlinequran.NotificationPanel.*;
//import static com.medoapps.www.onlinequran.NotificationPanel.nManager;
//import static com.medoapps.www.onlinequran.NotificationPanel.ubdateNotification;
import static com.medoapps.www.onlinequran.managerdb.btnPlay;
import static com.medoapps.www.onlinequran.managerdb.songsList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationService extends BroadcastReceiver {
    public managerdb manager;
    public static final String ACTION1 = "ACTION1";
    public static final String ACTION2 = "ACTION2";
    public static final String ACTION3 = "ACTION3";
    public static final String ACTION4 = "ACTION4";
    public static final String ACTION5 = "ACTION5";
    public static final String ACTION6 = "ACTION6";
    public static final String ACTION7 = "ACTION7";
    public static final String ACTION8 = "ACTION8";
    public static final String ACTION9 = "ACTION9";





    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        if (ACTION1.equals(action)) {
            // do stuff...
            try {
                if(managerdb.instance.mp.isPlaying()){

                    if(managerdb.instance.mp!=null){
                        managerdb.instance.mp.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource( R.drawable.btn_play);

                        ubdateNotification();


                    }
                }else{

                    // Resume song
                    if(managerdb.instance.mp!=null){
                        managerdb.instance.mp.start();
                        // Changing button image to pause button
                        btnPlay.setImageResource( R.drawable.btn_pause);
                        ubdateNotification();

                    }
                }
                nManager.notify(500, nBuilder.build());
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

        } else if (ACTION2.equals(action)) {

            try {
                // next action
                // check if next song is there or not
                if(managerdb.instance.currentSongIndex < (songsList.size() - 1)){
                    managerdb.instance.playSong(managerdb.instance.currentSongIndex + 1);
                    managerdb.instance.currentSongIndex = managerdb.instance.currentSongIndex + 1;
                }else{
                    // play first song
                    managerdb.instance.playSong(0);
                    managerdb.instance.currentSongIndex = 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (ACTION3.equals(action)){
            try {
                // preveous action
                if(managerdb.instance.currentSongIndex > 0){
                    managerdb.instance.playSong(managerdb.instance.currentSongIndex - 1);
                    managerdb.instance.currentSongIndex = managerdb.instance.currentSongIndex - 1;
                }else{
                    // play last song
                    managerdb.instance.playSong(songsList.size() - 1);
                    managerdb.instance.currentSongIndex = songsList.size() - 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }else if (ACTION4.equals(action)) {

            try {
                //forawrd action
                // get current song position
                int currentPosition = managerdb.instance.mp.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if(currentPosition + managerdb.instance.seekForwardTime <= managerdb.instance.mp.getDuration()){
                    // forward song
                    managerdb.instance.mp.seekTo(currentPosition + managerdb.instance.seekForwardTime);
                    Toast.makeText(context, "+5", Toast.LENGTH_SHORT).show();
                }else{
                    // forward to end position
                    managerdb.instance.mp.seekTo(managerdb.instance.mp.getDuration());
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }


        }else if(ACTION5.equals(action)){
            try {
                //backward action
                // get current song position
                int currentPosition = managerdb.instance.mp.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if(currentPosition - managerdb.instance.seekBackwardTime >= 0){
                    // forward song
                    managerdb.instance.mp.seekTo(currentPosition - managerdb.instance.seekBackwardTime);
                    Toast.makeText(context, "-5", Toast.LENGTH_SHORT).show();
                }else{
                    // backward to starting position
                    managerdb.instance.mp.seekTo(0);
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }


        }else if(ACTION6.equals(action)){
            try {
                //open managerdp class
               /* Intent i = new Intent();
                i.setClassName(" com.medoapps.www.onlinequran", " com.medoapps.www.onlinequran.managerdb");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("RecitesName",SettingSaved.FinalRecite);
                i.putExtra("RecitesAYA",SettingSaved.FinalAya);
                context.startActivity(i);*/

                Intent intentone = new Intent(context.getApplicationContext(), managerdb.class);
                intentone.putExtra("RecitesName", SettingSaved.FinalRecite);
                intentone.putExtra("RecitesAYA", SettingSaved.FinalAya);
                context.startActivity(intentone);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }else if(ACTION7.equals(action)){
            try {
                //close notification


                if(managerdb.instance.mp != null) {
                    nManager.cancel(500);
                    if (managerdb.instance.mp.isPlaying()) {
                        btnPlay.setImageResource(R.drawable.btn_play);
                        managerdb.instance.mp.pause();
                        SettingSaved.isfullscreenadshow= false;
                        SettingSaved settingSaved=new SettingSaved(context);
                        settingSaved.SaveData();
                        settingSaved.LoadData();
                    }else {

                    }

                }else {

                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }else if (ACTION8.equals(action)){

            try {
                AyaList.instance3.canceldownload();

            } catch (Exception e) {
                e.printStackTrace();
            }


        }else if (ACTION9.equals(action)){

            try {
//                DownloadService.instance3.canceldownload();
//                Toast.makeText(context, "closekjkjl", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
            }


        }else{

            throw new IllegalArgumentException("Unsupported action: " + action);
        }
    }



}
