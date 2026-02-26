package com.medoapps.www.onlinequran;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class Downloads extends AppCompatActivity{


    //private InterstitialAd mInterstitialAd;
    private static final String TAG = "Downloads";
    private AdView mAdView;
    SeekBar seekBar1;
    ImageButton btPLAY,btNEXT,btPREV,btFF,btFB;
    MyCustomAdapter Adapater;
    MediaPlayer mp;
    int SeekValue;
    ListView ls;
    String[] items;
    public TextView songCurrentDurationLabel;
    public  TextView songTotalDurationLabel;
    public  Utilities utils;
    public static Handler mHandler = new Handler();
    final int[] save = {-1};
    public LinearLayout playerLayout;

    int currentSongIndexNext =0;
    int currentsongIndexPrev =0;
    int mSelectedItem;
    public View row;


    TextView textView;
    TextView textView1;
    TextView Duration;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);

        loadad();//to load ads full screen


        btPLAY=(ImageButton)findViewById(R.id.btPlayP);
        btNEXT=(ImageButton)findViewById(R.id.btNextP);
        btPREV=(ImageButton)findViewById(R.id.btPvP);
        btFF=(ImageButton)findViewById(R.id.btFfP);
        btFB=(ImageButton)findViewById(R.id.btFbP);
        seekBar1 = (SeekBar) findViewById(R.id.seekBar);
        songCurrentDurationLabel = (TextView) findViewById( R.id.songCurrentDurationP);
        songTotalDurationLabel = (TextView) findViewById( R.id.songTotalDurationP);
        playerLayout =(LinearLayout)findViewById(R.id.playerlayout);


        mp=new MediaPlayer();
        utils = new Utilities();
        //load banner ad
        mAdView = (AdView) findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }



        });

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SeekValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // remove message Handler from updating progress bar
                mHandler.removeCallbacks(mUpdateTimeTask);

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);
                int totalDuration = mp.getDuration();
                int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
                mp.seekTo(SeekValue);
                // update timer progress again
                updateProgressBar();
            }
        });
        ls = (ListView) findViewById(R.id.listView);
        CheckUserPermsions();

        ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( final AdapterView<?> parent, View view, int position, long id) {



               /* parent.getChildAt(position).setBackgroundColor(
                        Color.parseColor("#A9BCF5"));
                if (save[0] != -1 && save[0] != position) {
                    parent.getChildAt(save[0]).setBackgroundColor(
                            Color.parseColor("#d6e6ff"));
                }

                save[0] = position;*/

                view.setBackgroundResource(R.color.orange);

                if (row != null) {
                    row.setBackgroundResource(R.color.softBlue);
                }
                row = view;

                if(mp!=null){

                    mp.stop();
                    mp.release();
                }
                SongInfo songInfo=SongsList.get(position);
                currentSongIndexNext =position+1;
                currentsongIndexPrev =position-1;
                mp=new MediaPlayer();
                playerLayout.setVisibility(View.VISIBLE);
                try {
                    mp.setDataSource(songInfo.Path);
                    mp.prepare();
                    mp.start();
                    btPLAY.setImageResource( R.drawable.btn_pause);
                    seekBar1.setMax(mp.getDuration());
                    // update timer progress again
                    updateProgressBar();
                    /*parent.getChildAt(position).setBackgroundColor(
                            Color.parseColor("#A9BCF5"));*/
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        mythread my = new mythread();
        my.start();

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {

               // Toast.makeText(Downloads.this, "finished", Toast.LENGTH_LONG).show();

                btPLAY.setImageResource( R.drawable.btn_play);

            }
        });



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_managerdb, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.gbackmenu) { // stoped
            // Intent intent=new Intent(this,MainActivity.class);
            //startActivity(intent);
            //load full screan ad
            /*/*if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                Log.d("TAG", "The interstitial wasn't loaded yet.");
            }*/
            if(mp.isPlaying())
                if(mp!=null)
                    mp.pause();
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK )
        {


            if(mp.isPlaying())
                if(mp!=null)
                    mp.pause();

            this.finish();


        }

        return super.onKeyDown(keyCode, event);
    }


    ArrayList<SongInfo> SongsList = new ArrayList<SongInfo>();

    /* online media
public ArrayList<SongInfo> getAllSongs() {
    SongsList.clear();
    SongsList.add(new SongInfo("http://server6.mp3quran.net/thubti/001.mp3","Fataha","bakar","quran"));
    SongsList.add(new SongInfo("http://server6.mp3quran.net/thubti/002.mp3","Bakara","bakar","quran"));
    SongsList.add(new SongInfo("http://server6.mp3quran.net/thubti/003.mp3","Al-Imran","bakar","quran"));
    SongsList.add(new SongInfo("http://server6.mp3quran.net/thubti/004.mp3","An-Nisa'","bakar","quran"));
    SongsList.add(new SongInfo("http://server6.mp3quran.net/thubti/005.mp3","Al-Ma'idah","bakar","quran"));
    SongsList.add(new SongInfo("http://server6.mp3quran.net/thubti/006.mp3","Al-An'am","bakar","quran"));
    SongsList.add(new SongInfo("http://server6.mp3quran.net/thubti/007.mp3","Al-A'raf","bakar","quran"));
    return SongsList;
}*/
    //local

    public ArrayList<SongInfo> getAllSongs() {
        Uri allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection1 = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.TITLE, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.ArtistColumns.ARTIST,};

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        File directory = Environment.getExternalStorageDirectory();

          String DOWNLOAD_FILE_DIR = Environment.getExternalStorageDirectory().getPath() + "/My Stream";

        String selection2 = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                MediaStore.Audio.Media.DATA + " LIKE '"+DOWNLOAD_FILE_DIR+"/%'" ;

        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor1 = contentResolver.query(uri, null, selection2, null, sortOrder);

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,MediaStore.Audio.Media.DATA + " like ? ",
                new String[] {"%My Stream%"},  sortOrder);


        Toast.makeText(this, String.valueOf(cursor.getCount()), Toast.LENGTH_SHORT).show();
        if (cursor != null ) {
            if (cursor.moveToFirst()) {

                    String song_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String songs = null;
                    /*if (song_name.endsWith("mp3") && song_name.startsWith("Medo")) {

                        songs = song_name;
                    }*/
                    String fullpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String album_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String artist_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String title_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                    SongsList.add(new SongInfo(fullpath, song_name, album_name, artist_name,title_name,duration));

            }


        }
        cursor.close();
        return SongsList;
    }




    class mythread extends Thread {
        public void run() {


            while (true) {
                try {
                    Thread.sleep(1000);

                } catch (Exception e) {
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //seek bar   seekBar1.setProgress(mp .getCurrentPosition());
                        if (mp != null)
                            seekBar1.setProgress(mp.getCurrentPosition());

                    }
                });


            }
        }
    }

    // adapter
    private class MyCustomAdapter extends BaseAdapter {
        ArrayList<SongInfo> fullsongpath;

        public MyCustomAdapter(ArrayList<SongInfo> fullsongpath) {
            this.fullsongpath = fullsongpath;
        }


        @Override
        public int getCount() {



            return fullsongpath.size();
        }

        @Override
        public String getItem(int position) {

           /* items=new String[fullsongpath.size()];
            for(int i = 0 ;i<fullsongpath.size();i++){
                //toast(mysong.get(i).getName().toString());
                items[i]= String.valueOf(fullsongpath.get(position).song_name.startsWith("Medo"));
            }*/

            String items= String.valueOf(fullsongpath.get(position).song_name.startsWith("Medo"));


            return items;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater mInflater = getLayoutInflater();
            View myView = mInflater.inflate(R.layout.item, null);
            SongInfo s = fullsongpath.get(position);
             textView = (TextView) myView.findViewById(R.id.textView);
            textView.setText(s.title_name);
             textView1 = (TextView) myView.findViewById(R.id.textView2);
            textView1.setText(s.artist_name);
            Duration = (TextView) myView.findViewById(R.id.tvduration);


            try {
                long du= Long.parseLong(s.duration);
                Duration.setText(""+utils.milliSecondsToTimer(du));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            return myView;
        }

    }


    public void buPlay(View view) {
        if(mp.isPlaying()){
            //load full screan ad
                    /*if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Log.d("TAG", "The interstitial wasn't loaded yet.");
                    }*/
            if(mp!=null){
                mp.pause();
                // Changing button image to play button
                btPLAY.setImageResource( R.drawable.btn_play);



            }
        }else{
            //load full screan ad
                    /*if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Log.d("TAG", "The interstitial wasn't loaded yet.");
                    }*/
            // Resume song
            if(mp!=null){
                mp.start();
                // Changing button image to pause button
                btPLAY.setImageResource( R.drawable.btn_pause);


            }
        }
    }

    public void buNext(View view) {

        if(mp!=null){

            mp.stop();
            mp.release();
        }





        if(currentSongIndexNext < (SongsList.size() - 1)) {
            SongInfo songInfo = SongsList.get(currentSongIndexNext);
            currentSongIndexNext = currentSongIndexNext + 1;
            save[0] =currentSongIndexNext + 1;


            try {

                mp.setDataSource(songInfo.Path);
                mp.prepare();
                mp.start();
                btPLAY.setImageResource(R.drawable.btn_pause);
                seekBar1.setMax(mp.getDuration());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            currentSongIndexNext =0;
            SongInfo songInfo = SongsList.get(currentSongIndexNext);
            currentSongIndexNext = currentSongIndexNext + 1;

            try {
                mp.setDataSource(songInfo.Path);
                mp.prepare();
                mp.start();
                btPLAY.setImageResource(R.drawable.btn_pause);
                seekBar1.setMax(mp.getDuration());
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    public void buPrev(View view) {
        if(mp!=null){

            mp.stop();
            mp.release();
        }
        if(currentSongIndexNext-1 > 0){
            SongInfo songInfo = SongsList.get(currentSongIndexNext-1);
            currentSongIndexNext = currentSongIndexNext - 1;
            mp = new MediaPlayer();
            try {
                mp.setDataSource(songInfo.Path);
                mp.prepare();
                mp.start();
                btPLAY.setImageResource(R.drawable.btn_pause);
                seekBar1.setMax(mp.getDuration());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            SongInfo songInfo = SongsList.get(SongsList.size() );
            currentSongIndexNext = SongsList.size() ;
            mp = new MediaPlayer();
            try {
                mp.setDataSource(songInfo.Path);
                mp.prepare();
                mp.start();
                btPLAY.setImageResource(R.drawable.btn_pause);
                seekBar1.setMax(mp.getDuration());
            } catch (IOException e) {
                e.printStackTrace();
            }

            //position = mySongs.size() - 1;
        }
    }
    public void buFf(View view) {
        //load full screan ad
                /*if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }*/
        // get current song position
        int currentPosition = mp.getCurrentPosition();
        // check if seekForward time is lesser than song duration
        if(currentPosition + 5000 <= mp.getDuration()){
            // forward song
            mp.seekTo(currentPosition + 5000);
            Toast.makeText(Downloads.this, "+5", Toast.LENGTH_SHORT).show();
        }else{
            // forward to end position
            mp.seekTo(mp.getDuration());
        }
    }
    public void bufb(View view) {
        //load full screan ad
               /* if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }*/
        // get current song position
        int currentPosition = mp.getCurrentPosition();
        // check if seekBackward time is greater than 0 sec
        if(currentPosition - 5000 >= 0){
            // forward song
            mp.seekTo(currentPosition - 5000);
            Toast.makeText(Downloads.this, "-5", Toast.LENGTH_SHORT).show();
        }else{
            // backward to starting position
            mp.seekTo(0);
        }
    }


    void CheckUserPermsions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }

        LoadSng();

    }

    //get acces to location permsion
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LoadSng();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "denail", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    void LoadSng() {
        Adapater = new MyCustomAdapter(getAllSongs());
        ls.setAdapter(Adapater);
    }

    /**
     * Update timer on seekbar
     * */
    public  void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     * */
    public  Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            try{
                long totalDuration = mp.getDuration();
                long currentDuration = mp.getCurrentPosition();

                // Displaying Total Views time
                songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
                // Displaying time completed playing
                songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

                /*// Updating progress bar
                int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
                //Log.d("Progress", ""+progress);
                sb.setProgress(progress);*/

                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100);
                if(currentDuration>=(totalDuration/8)){

                }
            }
            catch (Exception ex){}
        }
    };

    public void loadad(){

        /*/*mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.Pop_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });
*/

    }


}