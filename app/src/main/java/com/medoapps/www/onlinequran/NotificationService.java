package com.medoapps.www.onlinequran;

import static com.medoapps.www.onlinequran.NotificationPanel.*;
//import static com.medoapps.www.onlinequran.NotificationPanel.nManager;
//import static com.medoapps.www.onlinequran.NotificationPanel.ubdateNotification;
import static com.medoapps.www.onlinequran.NewQuranPlayer.btnPlay;
import static com.medoapps.www.onlinequran.NewQuranPlayer.songsList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationService extends BroadcastReceiver {
    public NewQuranPlayer manager;
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
                if(NewQuranPlayer.NewQuranPlayerInstance.mp.isPlaying()){

                    if(NewQuranPlayer.NewQuranPlayerInstance.mp!=null){
                        NewQuranPlayer.NewQuranPlayerInstance.mp.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource( R.drawable.btn_play);

                        ubdateNotification();


                    }
                }else{

                    // Resume song
                    if(NewQuranPlayer.NewQuranPlayerInstance.mp!=null){
                        NewQuranPlayer.NewQuranPlayerInstance.mp.start();
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
                if(NewQuranPlayer.NewQuranPlayerInstance.currentSongIndex < (songsList.size() - 1)){
                    NewQuranPlayer.NewQuranPlayerInstance.playSong(NewQuranPlayer.NewQuranPlayerInstance.currentSongIndex + 1);
                    NewQuranPlayer.NewQuranPlayerInstance.currentSongIndex = NewQuranPlayer.NewQuranPlayerInstance.currentSongIndex + 1;
                }else{
                    // play first song
                    NewQuranPlayer.NewQuranPlayerInstance.playSong(0);
                    NewQuranPlayer.NewQuranPlayerInstance.currentSongIndex = 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (ACTION3.equals(action)){
            try {
                // preveous action
                if(NewQuranPlayer.NewQuranPlayerInstance.currentSongIndex > 0){
                    NewQuranPlayer.NewQuranPlayerInstance.playSong(NewQuranPlayer.NewQuranPlayerInstance.currentSongIndex - 1);
                    NewQuranPlayer.NewQuranPlayerInstance.currentSongIndex = NewQuranPlayer.NewQuranPlayerInstance.currentSongIndex - 1;
                }else{
                    // play last song
                    NewQuranPlayer.NewQuranPlayerInstance.playSong(songsList.size() - 1);
                    NewQuranPlayer.NewQuranPlayerInstance.currentSongIndex = songsList.size() - 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }else if (ACTION4.equals(action)) {

            try {
                //forawrd action
                // get current song position
                int currentPosition = NewQuranPlayer.NewQuranPlayerInstance.mp.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if(currentPosition + NewQuranPlayer.NewQuranPlayerInstance.seekForwardTime <= NewQuranPlayer.NewQuranPlayerInstance.mp.getDuration()){
                    // forward song
                    NewQuranPlayer.NewQuranPlayerInstance.mp.seekTo(currentPosition + NewQuranPlayer.NewQuranPlayerInstance.seekForwardTime);
                    Toast.makeText(context, "+5", Toast.LENGTH_SHORT).show();
                }else{
                    // forward to end position
                    NewQuranPlayer.NewQuranPlayerInstance.mp.seekTo(NewQuranPlayer.NewQuranPlayerInstance.mp.getDuration());
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }


        }else if(ACTION5.equals(action)){
            try {
                //backward action
                // get current song position
                int currentPosition = NewQuranPlayer.NewQuranPlayerInstance.mp.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if(currentPosition - NewQuranPlayer.NewQuranPlayerInstance.seekBackwardTime >= 0){
                    // forward song
                    NewQuranPlayer.NewQuranPlayerInstance.mp.seekTo(currentPosition - NewQuranPlayer.NewQuranPlayerInstance.seekBackwardTime);
                    Toast.makeText(context, "-5", Toast.LENGTH_SHORT).show();
                }else{
                    // backward to starting position
                    NewQuranPlayer.NewQuranPlayerInstance.mp.seekTo(0);
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

                Intent intentone = new Intent(context.getApplicationContext(), NewQuranPlayer.class);
                intentone.putExtra("RecitesName", SettingSaved.FinalRecite);
                intentone.putExtra("RecitesAYA", SettingSaved.FinalAya);
                intentone.putExtra("Rewayat", SettingSaved.FinalRewayat);
                intentone.putExtra("RealRecitesName", SettingSaved.FinalRealRecitesName);
                intentone.putExtra("IsRadio", false);
                context.startActivity(intentone);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }else if(ACTION7.equals(action)){
            try {
                //close notification


                if(NewQuranPlayer.NewQuranPlayerInstance.mp != null) {
                    nManager.cancel(500);
                    if (NewQuranPlayer.NewQuranPlayerInstance.mp.isPlaying()) {
                        btnPlay.setImageResource(R.drawable.btn_play);
                        NewQuranPlayer.NewQuranPlayerInstance.mp.pause();
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
