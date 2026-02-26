package com.medoapps.www.onlinequran;

import static com.medoapps.www.onlinequran.R.id.adView;
import static com.medoapps.www.onlinequran.R.id.seekBar;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.ArrayList;


public class DownloadsPlayer extends AppCompatActivity   {
//    //private RewardedVideoAd mAd;
    private static final String TAG = "DownloadsPlayer";
    public MediaSession m_objMediaSession;
    private AdView mAdView;
//    //private InterstitialAd mInterstitialAd;
    public static MediaPlayer mp;
    ArrayList<File> mySongs;
    int position=0;
    Uri u;

    public  SeekBar sb;
    ImageButton btPLAY,btNEXT,btPREV,btFF,btFB;
    public TextView songCurrentDurationLabel;
    public  TextView songTotalDurationLabel;
    public  Utilities utils;
    ScrollView scrollview;
    public  TextView songTitleLabel2;
    public static Handler mHandler = new Handler();

    Thread ubdateSeekBar;

    private static final float VISUALIZER_HEIGHT_DIP = 50f;
    private Visualizer mVisualizer;
    private Equalizer mEqualizer;
    private LinearLayout mLinearLayout;
    private VisualizerView mVisualizerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloadsplayer);
        //set the device's volume control to control the audio stream we'll be playing
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //load banner ad
        mAdView = (AdView) findViewById(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }



        });

       /* // load native ad
        NativeExpressAdView adView = (NativeExpressAdView)findViewById(R.id.adView4);

        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);*/
        loadad();//to load ads full screen

        //load full screan ad
        /*if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }*/



        btPLAY=(ImageButton)findViewById(R.id.btPlayP);
        btNEXT=(ImageButton)findViewById(R.id.btNextP);
        btPREV=(ImageButton)findViewById(R.id.btPvP);
        btFF=(ImageButton)findViewById(R.id.btFfP);
        btFB=(ImageButton)findViewById(R.id.btFbP);
        sb=(SeekBar)findViewById(seekBar);
        songCurrentDurationLabel = (TextView) findViewById( R.id.songCurrentDurationP);
        songTotalDurationLabel = (TextView) findViewById( R.id.songTotalDurationP);
        scrollview = ((ScrollView) findViewById(R.id.scrollvissP));
        songTitleLabel2 = (TextView) findViewById( R.id.songTitle2);


        utils = new Utilities();



        ubdateSeekBar=new Thread(){
            @Override
            public void run() {
                int totalDuration=mp.getDuration();
                int currentPosition = 0;

                while (currentPosition<totalDuration){
                    try {
                        sleep(500);
                        currentPosition=mp.getCurrentPosition();
                        sb.setProgress(currentPosition);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //super.run();
            }
        };
        Intent i = getIntent();
        Bundle b = i.getExtras();
        mySongs=(ArrayList)b.getParcelableArrayList("songlist");


        position = b.getInt("pos",0);

        playsong(position);

        btPLAY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        });
        btNEXT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp!=null){

                    mp.stop();
                    mp.release();
                }

                if(position < (mySongs.size() - 1)){
                    position=position+1;
                    u = Uri.parse(mySongs.get(position).toString());
                mp= MediaPlayer.create(getApplicationContext(),u);
                mp.start();
                sb.setMax(mp.getDuration());
                    //playsong(position+1);
                    //position=position+1;
                    //create the equalizer with default priority of 0 & attach to our media player

                    mEqualizer.setEnabled(true);
                    //set up visualizer and equalizer bars
                    setupVisualizerFxAndUI();
                    //setupEqualizerFxAndUI();


                    mVisualizer.setEnabled(true);
                    autoscroll();

                }else{
                    position=0;
                    u = Uri.parse(mySongs.get(position).toString());
                mp= MediaPlayer.create(getApplicationContext(),u);
                mp.start();
                sb.setMax(mp.getDuration());
                    //playsong(0);
                    // play first song
                    //create the equalizer with default priority of 0 & attach to our media player

                    mEqualizer.setEnabled(true);
//set up visualizer and equalizer bars
                    setupVisualizerFxAndUI();
                   // setupEqualizerFxAndUI();
                    // enable the visualizer

                    mVisualizer.setEnabled(true);
                    autoscroll();

                }

                /*u = Uri.parse(mySongs.get(position).toString());
                mp= MediaPlayer.create(getApplicationContext(),u);
                mp.start();
                sb.setMax(mp.getDuration());*/




            }
        });
        btPREV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp!=null){

                    mp.stop();
                    mp.release();
                }

                position=(position-1<0)?mySongs.size()-1:position-1;
                /*if(position > 0){
                    playsong(position - 1);
                    //position = position - 1;
                }else{
                    // play last song
                    playsong(mySongs.size() - 1);
                    //position = mySongs.size() - 1;
                }
*/

                u = Uri.parse(mySongs.get(position).toString());
                mp= MediaPlayer.create(getApplicationContext(),u);
                mp.start();
                sb.setMax(mp.getDuration());

                mEqualizer.setEnabled(true);
                setupVisualizerFxAndUI();
                // setupEqualizerFxAndUI();
                // enable the visualizer

                mVisualizer.setEnabled(true);
                autoscroll();


            }
        });
        btFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    Toast.makeText(DownloadsPlayer.this, "+5", Toast.LENGTH_SHORT).show();
                }else{
                    // forward to end position
                    mp.seekTo(mp.getDuration());
                }

            }
        });
        btFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    Toast.makeText(DownloadsPlayer.this, "-5", Toast.LENGTH_SHORT).show();
                }else{
                    // backward to starting position
                    mp.seekTo(0);
                }

            }
        });
        /*if(mp!=null){

            mp.stop();
            mp.release();
        }*/








        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

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
                mp.seekTo(currentPosition);

                // update timer progress again
                updateProgressBar();

            }
        });

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                // mVisualizer.setEnabled(false);
                //create the equalizer with default priority of 0 & attach to our media player

                mEqualizer.setEnabled(true);

                // enable the visualizer

                mVisualizer.setEnabled(true);
                if(mp!=null){

                    mp.stop();
                    mp.release();
                }

                if(position < (mySongs.size() - 1)){
                    position=position+1;
                    u = Uri.parse(mySongs.get(position).toString());
                    mp= MediaPlayer.create(getApplicationContext(),u);
                    mp.start();
                    sb.setMax(mp.getDuration());

                    mEqualizer.setEnabled(true);
                    setupVisualizerFxAndUI();
                    // setupEqualizerFxAndUI();
                    // enable the visualizer

                    mVisualizer.setEnabled(true);
                    autoscroll();
                    // playsong(position);
                    //position=position+1;

                }else{
                    position=0;
                    u = Uri.parse(mySongs.get(position).toString());
                    mp= MediaPlayer.create(getApplicationContext(),u);
                    mp.start();
                    sb.setMax(mp.getDuration());

                    mEqualizer.setEnabled(true);
                    setupVisualizerFxAndUI();
                    // setupEqualizerFxAndUI();
                    // enable the visualizer

                    mVisualizer.setEnabled(true);
                    autoscroll();
                    //playsong(0);
                    // play first song

                }

            }
        });


    }


    public void playsong(int position){
        if(mp!=null){

            mp.stop();
            mp.release();
        }


        u = Uri.parse(mySongs.get(position).toString());
        mp= MediaPlayer.create(getApplicationContext(),u);

        mp.start();

        String songTitle = mySongs.get(position).toString().replace(".mp3","").replace("/","");

        String[] separated = songTitle.split("Medo_");

        songTitleLabel2.setText(separated[1]);


        btPLAY.setImageResource( R.drawable.btn_pause);
        sb.setMax(mp.getDuration());
        ubdateSeekBar.start();
        // update timer progress again
        updateProgressBar();
        //create the equalizer with default priority of 0 & attach to our media player
        mEqualizer = new Equalizer(0, mp.getAudioSessionId());
        mEqualizer.setEnabled(true);
        //set up visualizer and equalizer bars
        setupVisualizerFxAndUI();
        setupEqualizerFxAndUI();
        // enable the visualizer

        mVisualizer.setEnabled(true);




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

        /*mInterstitialAd = new InterstitialAd(this);
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

    /* shows spinner with list of equalizer presets to choose from
    - updates the seekBar progress and gain levels according
    to those of the selected preset*/
    private void equalizeSound() {
//        set up the spinner
        ArrayList<String> equalizerPresetNames = new ArrayList<String>();
        ArrayAdapter<String> equalizerPresetSpinnerAdapter
                = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                equalizerPresetNames);
        equalizerPresetSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner equalizerPresetSpinner = (Spinner) findViewById(R.id.spinner);

//        get list of the device's equalizer presets
        for (short i = 0; i < mEqualizer.getNumberOfPresets(); i++) {
            equalizerPresetNames.add(mEqualizer.getPresetName(i));
        }

        equalizerPresetSpinner.setAdapter(equalizerPresetSpinnerAdapter);

//        handle the spinner item selections
        equalizerPresetSpinner.setOnItemSelectedListener(new AdapterView
                .OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                //first list item selected by default and sets the preset accordingly
                mEqualizer.usePreset((short) position);
//                get the number of frequency bands for this equalizer engine
                short numberFrequencyBands = mEqualizer.getNumberOfBands();
//                get the lower gain setting for this equalizer band
                final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];

//                set seekBar indicators according to selected preset
                for (short i = 0; i < numberFrequencyBands; i++) {
                    short equalizerBandIndex = i;
                    SeekBar seekBar = (SeekBar) findViewById(equalizerBandIndex);
//                    get current gain setting for this equalizer band
//                    set the progress indicator of this seekBar to indicate the current gain value
                    seekBar.setProgress(mEqualizer
                            .getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
//                not used
            }
        });
    }

    /* displays the SeekBar sliders for the supported equalizer frequency bands
     user can move sliders to change the frequency of the bands*/
    private void setupEqualizerFxAndUI() {

//        get reference to linear layout for the seekBars
        mLinearLayout = (LinearLayout) findViewById(R.id.linearLayoutEqual);

//        equalizer heading
        TextView equalizerHeading = new TextView(this);
        equalizerHeading.setText("Equalizer");
        equalizerHeading.setTextSize(20);
        equalizerHeading.setGravity(Gravity.CENTER_HORIZONTAL);
        mLinearLayout.addView(equalizerHeading);

//        get number frequency bands supported by the equalizer engine
        short numberFrequencyBands = mEqualizer.getNumberOfBands();

//        get the level ranges to be used in setting the band level
//        get lower limit of the range in milliBels
        final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];
//        get the upper limit of the range in millibels
        final short upperEqualizerBandLevel = mEqualizer.getBandLevelRange()[1];

//        loop through all the equalizer bands to display the band headings, lower
//        & upper levels and the seek bars
        for (short i = 0; i < numberFrequencyBands; i++) {
            final short equalizerBandIndex = i;

//            frequency header for each seekBar
            TextView frequencyHeaderTextview = new TextView(this);
            frequencyHeaderTextview.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            frequencyHeaderTextview.setGravity(Gravity.CENTER_HORIZONTAL);
            frequencyHeaderTextview
                    .setText((mEqualizer.getCenterFreq(equalizerBandIndex) / 1000) + " Hz");
            mLinearLayout.addView(frequencyHeaderTextview);

//            set up linear layout to contain each seekBar
            LinearLayout seekBarRowLayout = new LinearLayout(this);
            seekBarRowLayout.setOrientation(LinearLayout.HORIZONTAL);

//            set up lower level textview for this seekBar
            TextView lowerEqualizerBandLevelTextview = new TextView(this);
            lowerEqualizerBandLevelTextview.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            lowerEqualizerBandLevelTextview.setText((lowerEqualizerBandLevel / 100) + " dB");
//            set up upper level textview for this seekBar
            TextView upperEqualizerBandLevelTextview = new TextView(this);
            upperEqualizerBandLevelTextview.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            upperEqualizerBandLevelTextview.setText((upperEqualizerBandLevel / 100) + " dB");

            //            **********  the seekBar  **************
//            set the layout parameters for the seekbar
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;

//            create a new seekBar
            SeekBar seekBar = new SeekBar(this);
//            give the seekBar an ID
            seekBar.setId(i);

            seekBar.setLayoutParams(layoutParams);
            seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);
//            set the progress for this seekBar
            seekBar.setProgress(mEqualizer.getBandLevel(equalizerBandIndex));

//            change progress as its changed by moving the sliders
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    mEqualizer.setBandLevel(equalizerBandIndex,
                            (short) (progress + lowerEqualizerBandLevel));
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                    //not used
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    //not used
                }
            });

//            add the lower and upper band level textviews and the seekBar to the row layout
            seekBarRowLayout.addView(lowerEqualizerBandLevelTextview);
            seekBarRowLayout.addView(seekBar);
            seekBarRowLayout.addView(upperEqualizerBandLevelTextview);

            mLinearLayout.addView(seekBarRowLayout);

            //        show the spinner
            equalizeSound();
        }
    }

    /*displays the audio waveform*/
    private void setupVisualizerFxAndUI() {

        mLinearLayout = (LinearLayout) findViewById(R.id.linearLayoutVisual);
        // Create a VisualizerView to display the audio waveform for the current PrefManager
        mVisualizerView = new VisualizerView(this);
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)));
        mLinearLayout.addView(mVisualizerView);


        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(mp.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                              int samplingRate) {
                mVisualizerView.updateVisualizer(bytes);
            }

            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }
    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing() && mp != null) {
            mVisualizer.release();
            mEqualizer.release();

        }
    }

    public void autoscroll(){

        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}
