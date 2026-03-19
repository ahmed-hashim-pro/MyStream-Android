package com.medoapps.www.onlinequran;

/**
 * Created by Ahmed Hashim on 12/26/15.
 */

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

public class SongsManager {
    // SDCard Path
    final String MEDIA_PATH = new String("/sdcard/");
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    private Context context;
    private Activity activity;

    // Constructor
    public SongsManager(){

    }

    public SongsManager(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    /**
     * Function to read all mp3 files from sdcard
     * and store the details in ArrayList
     * */
    public ArrayList<HashMap<String, String>> getPlayList(String  RecitesName,String  Rewayat,Boolean isRadio){
        File home = new File(MEDIA_PATH);
        songsList.clear();

        if (isRadio){
            ArrayList<AuthorClass> list=new ArrayList<AuthorClass>();
            RadioLanguageClass lc=new RadioLanguageClass();
            list=lc.AutherList();
            for(AuthorClass temp:list){

/// / MainActivity.PathQuran="http://www.mp3quran.net/newMedia.php?id=" + String.valueOf(IDSelect) + "&file=http://server11.mp3quran.net/" + MajorDeprtment + "/" +  IDString + ".mp3";
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("songTitle", temp.RealName  );
                song.put("songPath",   temp.ImgUrl  );

                // Adding each song to SongList
                songsList.add(song);
            }
        }else {
            ArrayList<AuthorClass> list = new ArrayList<AuthorClass>();
            LnaguageClass lc=new LnaguageClass(context,activity);
            list=lc.GuranAya(RecitesName,Rewayat);
            for(AuthorClass temp:list){

/// / MainActivity.PathQuran="http://www.mp3quran.net/newMedia.php?id=" + String.valueOf(IDSelect) + "&file=http://server11.mp3quran.net/" + MajorDeprtment + "/" +  IDString + ".mp3";
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("songTitle", temp.RealName  );
                song.put("songPath",   temp.ImgUrl  );
//                Log.d("TAG", "getPlayList: " + temp.ImgUrl);

                // Adding each song to SongList
                songsList.add(song);
            }
        }





        // return songs list array

        return songsList;


    }

    /**
     * Class to filter files which are having .mp3 extension
     * */
    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3") || name.endsWith(".MP3"));
        }
    }
}
