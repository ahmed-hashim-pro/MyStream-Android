package com.medoapps.www.onlinequran;


import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.medoapps.www.onlinequran.NotificationPanel.*;
import static com.medoapps.www.onlinequran.R.id.adView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.medoapps.www.onlinequran.util.Config;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
//import com.google.android.gms.ads.InterstitialAd;
//import com.google.android.gms.ads.reward.RewardedVideoAd;


public class NewQuranPlayer extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    //private RewardedVideoAd mAd;

    public MediaSession m_objMediaSession;
    public static ImageButton btnPlay;
    //banner add
    private static final String TAG = "managerdb";
    private AdView mAdView;

    public ImageButton btnForward;
    public ImageButton btnBackward;
    public ImageButton btnNext;
    public ImageButton btnPrevious;
    public ImageButton btnBookmark;
    //private InterstitialAd mInterstitialAd;
    public ImageButton btnRepeat;
    public ImageButton btnShuffle;
    public SeekBar songProgressBar;
    public TextView songTitleLabel;
    public  TextView songReciteName;
    public  TextView songCurrentDurationLabel;
    public  TextView songTotalDurationLabel;
    public LinearLayout layoutads;
    public static String notificationTitle;
    public static String ReciteNameText;
    // Media Player
    public static android.media.MediaPlayer mp ;
    WifiManager.WifiLock wifiLock;
    public static Boolean isPlaying = false;
    // Handler to update UI timer, progress bar etc,.
    public static Handler mHandler = new Handler();

    public  SongsManager songManager;
    public  Utilities utils;
    public  int seekForwardTime = 5000; // 5000 milliseconds
    public  int seekBackwardTime = 5000; // 5000 milliseconds
    public  int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    public  String RecitesName="";
    public  String Rewayat="";
    public  String RealRecitesName="";
    public  Boolean IsRadio = false;
    String RecitesAYA="";

    public  Boolean isStartFromNotification = false;
    public  int currentPlayerPosition;


    private static final float VISUALIZER_HEIGHT_DIP = 50f;
    private Visualizer mVisualizer;
    private Equalizer mEqualizer;
    private LinearLayout mLinearLayout;
    private VisualizerView mVisualizerView;
    ScrollView scrollview;
    private boolean equlizerstart = false;


    static NewQuranPlayer NewQuranPlayerInstance;


    public static ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();





    public static final String Broadcast_PLAY_NEW_AUDIO = "com.medoapps.www.onlinequran.PlayNewAudio";
    public static final String Broadcast_updateProgressBarReceiver = "com.medoapps.www.onlinequran.updateProgressBarReceiver";

    private MediaPlayerService player;
    private MediaPlayer serviceMediaPlayer;
    boolean serviceBound = false;
    ArrayList<Audio> audioList;

    ImageView collapsingImageView;

    int imageIndex = 0;


    private static final String ADMOB_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110";
    private Button refresh;
    private Button btn_SHOWAD;
    private CheckBox startVideoAdsMuted;
    private TextView videoStatus;
    private NativeAd nativeAd;
    private ImageButton closeAd;
    private LinearLayout AdContainer;

    private SeparateFunctions separateFunctions;

    private int numberOfTitleSet = 1;
    private ProgressBar loadingBar;
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("serviceStatus", serviceBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        serviceBound = savedInstanceState.getBoolean("serviceStatus");
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
//            serviceMediaPlayer = binder.getMediaPlayer();
            serviceMediaPlayer = player.CurrentServiceMediaPlayer();

            songProgressBar.setProgress(0);
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);

            if (player!= null){

                songTitleLabel.setText(player.getTitle());
                songReciteName.setText(player.getArtist());

            }



            serviceBound = true;
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeServiceBound(true);

//            Toast.makeText(getApplicationContext(), "service connected", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Toast.makeText(getApplicationContext(), "onServiceDisconnected", Toast.LENGTH_SHORT).show();
            serviceBound = false;
        }
    };


    private void playAudio(int audioIndex) {
        //Check is service is active

        if (isStartFromNotification){
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            updateProgressBar();
        }else{
            StorageUtil storageo = new StorageUtil(getApplicationContext());

            storageo.storeIsMediaStoppedFromUser(false);
            if (!serviceBound) {
                //Toast.makeText(this, "new play", Toast.LENGTH_SHORT).show();

                //Store Serializable audioList to SharedPreferences
                StorageUtil storage = new StorageUtil(getApplicationContext());
                storage.storeAudio(audioList);
                storage.storeAudioIndex(audioIndex);




                Intent playerIntent = new Intent(NewQuranPlayer.this, MediaPlayerService.class);
                Activity activity = NewQuranPlayer.this;
//                playerIntent.putExtra("activity", (Parcelable) activity);
                startService(playerIntent);

                bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);



                try {


                } catch (Exception e) {
                    e.printStackTrace();
                }

                boolean isServiceNotFirstRun;
                isServiceNotFirstRun = storage.loadServiceBound();
                storage.storeAudioIndex(audioIndex);

                Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
                sendBroadcast(broadcastIntent);
                /*if(isServiceNotFirstRun){
                    Toast.makeText(this, "play again over", Toast.LENGTH_SHORT).show();

                    //Store the new audioIndex to SharedPreferences
                    storage.storeAudioIndex(audioIndex);

                    //Service is active
                    //Send a broadcast to the service -> PLAY_NEW_AUDIO
                    Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
                    sendBroadcast(broadcastIntent);
                }else{

                }*/

            } else {
                //Store the new audioIndex to SharedPreferences
                StorageUtil storage = new StorageUtil(getApplicationContext());
                storage.storeAudioIndex(audioIndex);

                //Service is active
                //Send a broadcast to the service -> PLAY_NEW_AUDIO
                Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
                sendBroadcast(broadcastIntent);
            }
        }
        updateProgressBar();


    }
    private BroadcastReceiver updateProgressBarReceiver  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Updating progress bar
//            serviceMediaPlayer = player.CurrentServiceMediaPlayer();


            updateProgressBar();
            if (player != null){
                loadingBar.setVisibility(View.GONE);
                songTitleLabel.setText(player.getTitle());
                songReciteName.setText(player.getArtist());
                batteryOptimizationCheck();

            }


            /*serviceMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

                }
            });*/

        }
    };

    private void register_updateProgressBarReceiver() {
        //Register playNewMedia receiver


        try {
            unregisterReceiver(updateProgressBarReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        IntentFilter filter = new IntentFilter(com.medoapps.www.onlinequran.NewQuranPlayer.Broadcast_updateProgressBarReceiver);
        try {
            registerReceiver(updateProgressBarReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void loadAudio() {

//        audioList = null;
        audioList = new ArrayList<>();

        for(HashMap<String, String> map : songsList){

            String songPathTxt = map.get("songPath");
            String songTitleTxt = map.get("songTitle");
            if (IsRadio){
                audioList.add(new Audio(songPathTxt, songTitleTxt + "-" + RealRecitesName , "Ahmed HAshim", "Radio",RecitesName,Rewayat,RealRecitesName,RecitesAYA,IsRadio));

            }else {
                audioList.add(new Audio(songPathTxt, songTitleTxt, "Ahmed HAshim", RealRecitesName,RecitesName,Rewayat,RealRecitesName,RecitesAYA,IsRadio));

            }

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unbindService(serviceConnection);

//            player.STATE_PLAYING_public();
//            unregisterReceiver(updateProgressBarReceiver);
//            unbindService(serviceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (nativeAd != null) {
            nativeAd.destroy();
        }
//        unbindService(serviceConnection);
//        unregisterReceiver(updateProgressBarReceiver);
//        //Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
        /*if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }*/
    }


    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view.
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every NativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        VideoController vc = nativeAd.getMediaContent().getVideoController();

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {


            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
                    refresh.setEnabled(true);
                    videoStatus.setText("Video status: Video playback has ended.");
                    super.onVideoEnd();
                }
            });
        } else {
            videoStatus.setText("Video status: Ad does not contain a video asset.");
            refresh.setEnabled(true);
        }
    }



    private void refreshAd() {
        if (SettingSaved.isSubscribedPremium)
            return;

        refresh.setEnabled(false);

        AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.NATIVE_ADMOB_AD_UNIT_ID));

        builder.forNativeAd(
                new NativeAd.OnNativeAdLoadedListener() {
                    // OnLoadedListener implementation.
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        // If this callback occurs after the activity is destroyed, you must call
                        // destroy and return or you may get a memory leak.
                        boolean isDestroyed = false;
                        refresh.setEnabled(true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            isDestroyed = isDestroyed();
                        }
                        if (isDestroyed || isFinishing() || isChangingConfigurations()) {
                            nativeAd.destroy();
                            return;
                        }
                        // You must call destroy on old ads when you are done with them,
                        // otherwise you will have a memory leak.
                        if (NewQuranPlayer.this.nativeAd != null) {
                            NewQuranPlayer.this.nativeAd.destroy();
                        }
                        NewQuranPlayer.this.nativeAd = nativeAd;
                        FrameLayout frameLayout = findViewById(R.id.fl_adplaceholder);
                        NativeAdView adView =
                                (NativeAdView) getLayoutInflater().inflate(R.layout.ad_unified, null);
                        populateNativeAdView(nativeAd, adView);
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);
                        AdContainer.setVisibility(View.VISIBLE);
                    }
                });

        VideoOptions videoOptions =
                new VideoOptions.Builder().setStartMuted(startVideoAdsMuted.isChecked()).build();

        NativeAdOptions adOptions =
                new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();


        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader =
                builder
                        .withAdListener(
                                new AdListener() {
                                    @Override
                                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                                        refresh.setEnabled(true);
                                        String error =
                                                String.format(
                                                        "domain: %s, code: %d, message: %s",
                                                        loadAdError.getDomain(),
                                                        loadAdError.getCode(),
                                                        loadAdError.getMessage());
                                        /*Toast.makeText(
                                                NewQuranPlayer.this,
                                                "Failed to load native ad with error " + error,
                                                Toast.LENGTH_SHORT)
                                                .show();*/
                                    }
                                })
                        .build();

        adLoader.loadAd(new AdRequest.Builder().build());

        videoStatus.setText("");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }*/
        setContentView(R.layout.activity_new_quran_player);
        NewQuranPlayerInstance =this;

        SettingSaved settingSaved = new SettingSaved(NewQuranPlayer.this);
        settingSaved.LoadData();


        separateFunctions = new SeparateFunctions(this);

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        loadingBar = findViewById(R.id.loadingBar);
        refresh = findViewById(R.id.btn_refresh);
        btn_SHOWAD = findViewById(R.id.btn_SHOWAD);
        closeAd = findViewById(R.id.closeAd);
        AdContainer = findViewById(R.id.AdContainer);
        startVideoAdsMuted = findViewById(R.id.cb_start_muted);
        videoStatus = findViewById(R.id.tv_video_status);

        AdContainer.setVisibility(View.GONE);
        btn_SHOWAD.setVisibility(View.GONE);

        closeAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdContainer.setVisibility(View.GONE);
                btn_SHOWAD.setVisibility(View.VISIBLE);

            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View unusedView) {
                refreshAd();
            }
        });
        btn_SHOWAD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshAd();
                btn_SHOWAD.setVisibility(View.GONE);


            }
        });

        refreshAd();




        Bundle b=getIntent().getExtras();
        if (b!= null){
            RecitesName=b.getString("RecitesName");
            IsRadio=b.getBoolean("IsRadio");
            Rewayat=b.getString("Rewayat");
            RealRecitesName=b.getString("RealRecitesName");
            RecitesAYA=b.getString("RecitesAYA");
            isStartFromNotification=b.getBoolean("isStartFromNotification");
            currentPlayerPosition=b.getInt("currentPlayerPosition");
        }else{
            finish();
        }

        //set the device's volume control to control the audio stream we'll be playing
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // All player buttons
        btnPlay = (ImageButton) findViewById( R.id.btnPlay);
        btnForward = (ImageButton) findViewById( R.id.btnForward);
        btnBackward = (ImageButton) findViewById( R.id.btnBackward);
        btnNext = (ImageButton) findViewById( R.id.btnNext);
        btnPrevious = (ImageButton) findViewById( R.id.btnPrevious);
        layoutads=(LinearLayout)findViewById(R.id.layoutads);
        btnRepeat = (ImageButton) findViewById( R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById( R.id.btnShuffle);
        songProgressBar = (SeekBar) findViewById( R.id.songProgressBar);
        songTitleLabel = (TextView) findViewById(R.id. songTitle);
        songReciteName = (TextView)findViewById(R.id.songrecite);
        songCurrentDurationLabel = (TextView) findViewById( R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById( R.id.songTotalDurationLabel);
        scrollview = ((ScrollView) findViewById(R.id.scrollmanager));
        btnBookmark=(ImageButton) findViewById(R.id.bookmark);

        LnaguageClass lc = new LnaguageClass(NewQuranPlayer.this);

        songTitleLabel = lc.SetTextFont(songTitleLabel,"");
        songReciteName = lc.SetTextFont(songReciteName,"");
        songTitleLabel.setSelected(true);
        if (IsRadio){
            btnRepeat.setVisibility(View.GONE);
            btnShuffle.setVisibility(View.GONE);
        }
        StorageUtil storage = new StorageUtil(getApplicationContext());

//        storage.storeIsMediaStoppedFromUser(false);
        if (storage.loadIsPlayerShuffle()){
            btnShuffle.setImageResource( R.drawable.btn_shuffle_focused);

        }

        if (storage.loadIsPlayerRepeat()){
            btnRepeat.setImageResource( R.drawable.btn_repeat_focused);
        }

        //load banner ad
        loadBannerAd();


        //chic for btnBookmark
        if(SettingSaved.FinalRecite.equals(RecitesName)&&SettingSaved.FinalAya.equals(RecitesAYA)){
            btnBookmark.setImageResource(R.drawable.ic_bookmark_border_black_pressed_24dp);
        }else {
            ////Toast.makeText(this, "no bookmark", Toast.LENGTH_SHORT).show();
            // //Toast.makeText(this, SettingSaved.FinalRecite+ RecitesName, Toast.LENGTH_LONG).show();

        }


        songManager = new SongsManager(NewQuranPlayer.this,NewQuranPlayer.this);
        utils = new Utilities();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important

        // Getting all songs list
        songsList = songManager.getPlayList( RecitesName,Rewayat,IsRadio);

        // By default play first song
        currentSongIndex=Integer.parseInt(  RecitesAYA);//-1 ;

        loadAudio();
        playAudio(currentSongIndex);
        btnPlay.setImageResource( R.drawable.btn_pause);
        String songTitle = songsList.get(currentSongIndex).get("songTitle");
        if (player != null){
            songTitleLabel.setText(player.getTitle());
            songReciteName.setText(player.getArtist());
        }
        register_updateProgressBarReceiver();

        if (player != null){
            player.STATE_PLAYING_public();
        }

        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (player != null && player.CurrentServiceMediaPlayer() != null){
                    if (player.playbackStatusPublic == PlaybackStatus.PLAYING) {
                        player.PausePublic();
                        btnPlay.setImageResource( R.drawable.btn_play);


                    } else if (player.playbackStatusPublic == PlaybackStatus.PAUSED) {

                        player.ResumePublic();
                        btnPlay.setImageResource( R.drawable.btn_pause);

                    }
                }else {
                    stopMediaPlayerService();
                    loadAndPlayAudio();
                }


            }
        });

        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
        btnForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (player != null && player.CurrentServiceMediaPlayer() != null){
                    int currentPosition = serviceMediaPlayer.getCurrentPosition();
                    // check if seekForward time is lesser than song duration
                    if(currentPosition + seekForwardTime <= serviceMediaPlayer.getDuration()){
                        // forward song
                        serviceMediaPlayer.seekTo(currentPosition + seekForwardTime);
                        //Toast.makeText(NewQuranPlayer.this, "+5", Toast.LENGTH_SHORT).show();
                    }else{
                        // forward to end position
                        serviceMediaPlayer.seekTo(serviceMediaPlayer.getDuration());
                    }
                }else {
                    stopMediaPlayerService();
                    loadAndPlayAudio();
                }

            }
        });

        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
        btnBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (player != null && player.CurrentServiceMediaPlayer() != null){
                    int currentPosition = serviceMediaPlayer.getCurrentPosition();
                    // check if seekBackward time is greater than 0 sec
                    if(currentPosition - seekBackwardTime >= 0){
                        // forward song
                        serviceMediaPlayer.seekTo(currentPosition - seekBackwardTime);
                        //Toast.makeText(NewQuranPlayer.this, "-5", Toast.LENGTH_SHORT).show();
                    }else{
                        // backward to starting position
                        serviceMediaPlayer.seekTo(0);
                    }
                }else {
                    stopMediaPlayerService();
                    loadAndPlayAudio();
                }


            }
        });

        /**
         * Next button click event
         * Plays next song by taking currentSongIndexNext + 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (player != null && player.CurrentServiceMediaPlayer() != null){
                    loadingBar.setVisibility(View.VISIBLE);

                    player.SkipToNextPublic();
                    player.STATE_PLAYING_public();
                }else {
                    loadingBar.setVisibility(View.VISIBLE);

                    stopMediaPlayerService();
                    loadAndPlayAudio();
                }

            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndexNext - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (player != null && player.CurrentServiceMediaPlayer() != null){
                    loadingBar.setVisibility(View.VISIBLE);

                    player.SkipToPreviousPublic();
                }else {
                    loadingBar.setVisibility(View.VISIBLE);

                    stopMediaPlayerService();
                    loadAndPlayAudio();
                }

            }
        });

        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                StorageUtil storage = new StorageUtil(getApplicationContext());
                storage.storeServiceBound(true);
                if(storage.loadIsPlayerRepeat()){
                    storage.storeIsPlayerRepeat(false);

                    isRepeat = false;
                    //Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource( R.drawable.btn_repeat);
                }else{
                    // make repeat to true
                    storage.storeIsPlayerRepeat(true);
                    isRepeat = true;
                    //Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    storage.storeIsPlayerShuffle(false);
                    isShuffle = false;
                    btnRepeat.setImageResource( R.drawable.btn_repeat_focused);
                    btnShuffle.setImageResource( R.drawable.btn_shuffle);
                }
            }
        });

        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                StorageUtil storage = new StorageUtil(getApplicationContext());

                if(storage.loadIsPlayerShuffle()){
                    storage.storeIsPlayerShuffle(false);

                    isShuffle = false;
                    //Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource( R.drawable.btn_shuffle);
                }else{
                    // make repeat to true
                    storage.storeIsPlayerShuffle(true);

                    isShuffle= true;
                    //Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    storage.storeIsPlayerRepeat(false);

                    isRepeat = false;
                    btnShuffle.setImageResource( R.drawable.btn_shuffle_focused);
                    btnRepeat.setImageResource( R.drawable.btn_repeat);
                }
            }
        });

        /**
         * Button Click event for Play list click event
         * Launches list activity which displays list of songs
         * */

        btnBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingSaved.FinalRecite= ReciteNameText;
                SettingSaved.FinalAya=RecitesAYA;
                SettingSaved settingSaved=new SettingSaved(getApplicationContext());
                settingSaved.SaveData();
                settingSaved.LoadData();
                btnBookmark.setImageResource(R.drawable.ic_bookmark_border_black_pressed_24dp);

            }
        });

    }

    private void loadBannerAd() {
        if (SettingSaved.isSubscribedPremium)
            return;

        mAdView = (AdView) findViewById(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }
        });
    }


    public void stopMediaPlayerService(){
        serviceBound = false;

        try {
            unbindService(serviceConnection);
            player.stopSelf();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //service is active
    }
    public void loadAndPlayAudio(){
        loadAudio();
        playAudio(currentSongIndex);
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

        if (id == R.id.gbackmenu) {
//load full screan ad
            /*if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                Log.d("TAG", "The interstitial wasn't loaded yet.");
            }*/
            this.finish();


        }



        return super.onOptionsItemSelected(item);
    }


    // @Override


    /**
     * Receiving song index from playlist view
     * and play the song
     * */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
            currentSongIndex = data.getExtras().getInt("songIndex");
            // play selected song
//            playSong(currentSongIndex);
        }

    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK )
        {


/*
            if(mp.isPlaying())
                if(mp!=null)
                    mp.pause();

            this.finish();
            nManager.cancel(2);
*/

        }

        return super.onKeyDown(keyCode, event);
    }
    /**
     * Function to play a song
     * @param songIndex - index of song
     * */
    public  void  playSong(int songIndex){


        try {
            mp.reset();
            mp.setDataSource(songsList.get(songIndex).get("songPath"));
//            mp.setDataSource("https://qurango.net/radio/ahmed_altrabulsi");
            mp.prepare();
            mp.start();

            // prevent ad to show while listening
            SettingSaved.isfullscreenadshow= true;
            SettingSaved settingSaved=new SettingSaved(getApplicationContext());
            settingSaved.SaveData();
            settingSaved.LoadData();



            // Displaying Song title
            String songTitle = songsList.get(songIndex).get("songTitle");
            songTitleLabel.setText(songTitle);
            notificationTitle=songTitle;
            ReciteNameText =RealRecitesName;
            songReciteName.setText(ReciteNameText);
            ubdateNotification();


            if(equlizerstart==false){

                //hashim close the visulizer
               /*
                //create the equalizer with default priority of 0 & attach to our media player
                mEqualizer = new Equalizer(0, mp.getAudioSessionId());
                mEqualizer.setEnabled(true);
                //set up visualizer and equalizer bars
                setupVisualizerFxAndUI();
                setupEqualizerFxAndUI();
                // enable the visualizer

                mVisualizer.setEnabled(true);

                //equlizerstart=true;
*/
            }else{


            }


            autoscroll();




            // Changing Button Image to pause image
            btnPlay.setImageResource( R.drawable.btn_pause);

            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);

            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {

        checkRating();
        super.onBackPressed();
    }
    public void checkRating()
    {
        Log.d(TAG, "checkRating: ");
        SettingSaved settingSaved = new SettingSaved(NewQuranPlayer.this);
        settingSaved.LoadData();

        Log.d(TAG, "checkRating: " + settingSaved.numberOFBackClicksForIntent);
        if (settingSaved.numberOFBackClicksForIntent == Config.numberOFBackClicksForRating){
            separateFunctions.rateAppInAppReview(NewQuranPlayer.this);
            settingSaved.numberOFBackClicksForIntent =settingSaved.numberOFBackClicksForIntent+1 ;
            settingSaved.SaveData();
        }else {
            settingSaved.numberOFBackClicksForIntent =settingSaved.numberOFBackClicksForIntent+1 ;
            settingSaved.SaveData();
            finish();
        }
    }
    private void batteryOptimizationCheck(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                separateFunctions.showNewCustomDialog(getString(R.string.BatteryOptimizationTitle),getString(R.string.BatteryOptimizationDialog),getString(android.R.string.yes),getString(android.R.string.no),showAndroidSystematteryOptimizationRunnable,android.R.drawable.ic_dialog_info);
//                showUserPermitBatteryOptimizationDialog();

            }
        }
    }
    Runnable showAndroidSystematteryOptimizationRunnable = new Runnable() {
        @Override
        public void run() {
            showAndroidSystematteryOptimization();
        }
    };
    private void showAndroidSystematteryOptimization(){
        Intent intent = new Intent();
        String packageName = getPackageName();

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
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

                long totalDuration = serviceMediaPlayer.getDuration();
                long currentDuration = serviceMediaPlayer.getCurrentPosition();

                // Displaying Total Views time
                songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
                // Displaying time completed playing
                songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

                if (player != null){
                    if (numberOfTitleSet < 4){
                        numberOfTitleSet = numberOfTitleSet+1;
                        songTitleLabel.setText(player.getTitle());
                        songReciteName.setText(player.getArtist());
                    }

                }
                // Updating progress bar
                int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
                //Log.d("Progress", ""+progress);
                songProgressBar.setProgress(progress);

                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100);
                if(currentDuration>=(totalDuration/8)){
                    layoutads.setVisibility(View.VISIBLE);
                }


            }
            catch (Exception ex){}
        }
    };

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            mHandler.removeCallbacks(mUpdateTimeTask);
            int totalDuration = serviceMediaPlayer.getDuration();
            int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

            // forward or backward to certain seconds
            serviceMediaPlayer.seekTo(currentPosition);

            // update timer progress again
            updateProgressBar();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }






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

//        //Toast.makeText(this, "pause", Toast.LENGTH_SHORT).show();

    }

    private void releasemVisualizermEqualizer(){
        if(equlizerstart==true){
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

    public void BUClick(View view){
        checkRating();
//        this.finish();
    }
    public void runAdAgain(Boolean chick){

        SettingSaved.isfullscreenadshow= chick;
        SettingSaved settingSaved=new SettingSaved(getApplicationContext());
        settingSaved.SaveData();
        settingSaved.LoadData();
    }


    /*public class BackgroundAudioService extends Service {


         SimpleExoPlayer player;
        Context context;


        @Override
        public void onCreate() {
            super.onCreate();
            context = getApplicationContext();
            player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
            playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(context, "My_channel_id", R.string.app_name, R.string.app_name, 123, mediaDescriptionAdapter, new PlayerNotificationManager.NotificationListener() {
                @Override
                public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                    stopSelf();
                }

                @Override
                public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                    if(ongoing)
                        startForeground(notificationId,notification);
                }
            });
            player.addListener(new Player.EventListener() {

                @Override
                public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

                }

                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

                }

                @Override
                public void onLoadingChanged(boolean isLoading) {

                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                }

                @Override
                public void onRepeatModeChanged(int repeatMode) {

                }

                @Override
                public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {

                }

                @Override
                public void onPositionDiscontinuity(int reason) {

                }

                @Override
                public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

                }

                @Override
                public void onSeekProcessed() {

                }

                @Override
                public void onPlaybackStateChanged(int state) {
                    int pos = player.getCurrentWindowIndex();
                    PlayerSingleton.getInstance().audioFile = PlayerSingleton.getInstance().playingList.get(pos);
                *//*Bitmap b = coverpicture(PlayerSingleton.getInstance().audioFile.getPath());
                if(b==null){
                    playerView.setBackground();
                }else{
                    playerView.setBackground(null);
                    playerView.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
                }*//*
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    if(!isPlaying){
                        stopForeground(false);
                    }else{
                        playerNotificationManager.setPlayer(player);
                        player.play();
                    }
                }
            });
        }


        @Override
        public void onDestroy() {
            super.onDestroy();
            playerNotificationManager.setPlayer(null);
            player.release();
            player = null;
            PlayerSingleton.getInstance().audioFile = null;
            PlayerSingleton.getInstance().playingList = new ArrayList<>();
            new Thread(()->{
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
                if (appProcesses != null){
                    final String packageName = context.getPackageName();
                    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                        if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                            Intent i = new Intent(this, PlayerActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.putExtra("STOP", "STOP");
                            context.startActivity(i);
                        }
                    }
                }
            }).start();


        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        private PlayerNotificationManager.MediaDescriptionAdapter mediaDescriptionAdapter = new PlayerNotificationManager.MediaDescriptionAdapter() {
            @Override
            public String getCurrentSubText(Player player) {
                return " ";
            }

            @Override
            public String getCurrentContentTitle(Player player) {
                if(PlayerSingleton.getInstance().playingList.size()>player.getCurrentWindowIndex())
                    return PlayerSingleton.getInstance().playingList.get(player.getCurrentWindowIndex()).getTitle();
                else
                    return "";
            }

            @Override
            public PendingIntent createCurrentContentIntent(Player player) {
                Intent intentForeground = new Intent(context, PlayerActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                return PendingIntent.getActivity(getApplicationContext(), 0, intentForeground, 0);
            }

            @Override
            public String getCurrentContentText(Player player) {
                if(PlayerSingleton.getInstance().playingList.size()>player.getCurrentWindowIndex())
                    return PlayerSingleton.getInstance().playingList.get(player.getCurrentWindowIndex()).getAlbum();
                else
                    return "";
            }

            @Override
            public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                if(PlayerSingleton.getInstance().playingList.size()>player.getCurrentWindowIndex())
                    return coverpicture(PlayerSingleton.getInstance().playingList.get(player.getCurrentWindowIndex()).getPath());
                else
                    return null;
            }
        };


        PlayerNotificationManager playerNotificationManager;
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {


            String where = intent.getStringExtra("WHERE");
            String path = intent.getStringExtra("PATH");

            if(PlayerSingleton.getInstance().audioFile!=null) {
                if (!path.equals(PlayerSingleton.getInstance().audioFile.getPath())) {
                    player.pause();
                }
            }


            new StartOperation(where,path).execute();





            return START_STICKY;
        }

        static Bitmap coverpicture(String path) {
            MediaMetadataRetriever mr;
            byte[] byte1 = new byte[1];

            mr = new MediaMetadataRetriever();
            mr.setDataSource(path);
            try {
                byte1 = mr.getEmbeddedPicture();
                mr.release();
            }catch (Exception e){
                e.printStackTrace();
            }



            if(byte1 != null) {
                return BitmapFactory.decodeByteArray(byte1, 0, byte1.length);
            }
            else {
                return null;
            }

        }


        public class StartOperation extends AsyncTask{

            String where,path;
            int pos;
            ConcatenatingMediaSource concatenatingMediaSource;

            public StartOperation(String where,String path){
                this.where = where;
                this.path = path;
            }

            @Override
            protected Object doInBackground(Object... objects) {

                PlayerSingleton playerSingleton = PlayerSingleton.getInstance();

                playerSingleton.playingList = new ArrayList<>();
                playerSingleton.audioFile = null;

                if(where!=null&&path!=null){
                    switch (where) {
                        case "search":
                            playerSingleton.playingList = audioFileDatabase.audioFileDao().getAudioFilesStatic();
                            playerSingleton.audioFile = audioFileDatabase.audioFileDao().checkforExist(path).get(0);
                            pos = playerSingleton.playingList.indexOf(playerSingleton.audioFile);
                            break;
                        case "fav":
                            playerSingleton.playingList = audioFileDatabase.audioFileDao().getAllFavStatic();
                            playerSingleton.audioFile = audioFileDatabase.audioFileDao().checkforExist(path).get(0);
                            pos = playerSingleton.playingList.indexOf(playerSingleton.audioFile);
                            break;
                        case "folder":
                            playerSingleton.audioFile = audioFileDatabase.audioFileDao().checkforExist(path).get(0);
                            playerSingleton.playingList = audioFileDatabase.audioFileDao().selectByFolderStatic(playerSingleton.audioFile.getFolder());
                            pos = playerSingleton.playingList.indexOf(playerSingleton.audioFile);
                            break;
                        case "album":
                            playerSingleton.audioFile = audioFileDatabase.audioFileDao().checkforExist(path).get(0);
                            playerSingleton.playingList = audioFileDatabase.audioFileDao().selectByAlbumStatic(playerSingleton.audioFile.getAlbum());
                            pos = playerSingleton.playingList.indexOf(playerSingleton.audioFile);
                            break;
                        default:
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show());

                            stopSelf();
                    }
                }


                DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                        context, Util.getUserAgent(context, getString(R.string.app_name)));

                concatenatingMediaSource = new ConcatenatingMediaSource();
                for (int i = 0; i < playerSingleton.playingList.size(); i++) {
                    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(playerSingleton.playingList.get(i).getPath()));
                    MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
                    //previous  new ExtractorMediaSource.Factory
                    ///CHECK
                    concatenatingMediaSource.addMediaSource(mediaSource);
                }



                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                player.setMediaSource(concatenatingMediaSource);
                player.prepare();
                player.seekTo(pos,0);
                Log.e("TOT"+PlayerSingleton.getInstance().playingList.size(),"pos"+pos);
                playerNotificationManager.setPlayer(player);
                playerView.setPlayer(player);
                playerControlView.setPlayer(player);
                player.play();
            }
        }

    }*/









}