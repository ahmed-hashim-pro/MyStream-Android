package com.medoapps.www.onlinequran;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.os.Build.VERSION.SDK_INT;
import static com.medoapps.www.onlinequran.NewQuranPlayer.Broadcast_updateProgressBarReceiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.ServiceInfo;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaSessionManager;

import com.medoapps.www.onlinequran.util.Permissions;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Created by MEDO on 3/02/2022.
 */

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,

        AudioManager.OnAudioFocusChangeListener {


    public static final String ACTION_PLAY = "com.medoapps.www.onlinequran.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.medoapps.www.onlinequran.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.medoapps.www.onlinequran.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.medoapps.www.onlinequran.ACTION_NEXT";
    public static final String ACTION_STOP = "com.medoapps.www.onlinequran.ACTION_STOP";
    public static final String ACTION_CLOSE = "com.medoapps.www.onlinequran.ACTION_CLOSE";
    private static TelephonyCallback Tele;

    private MediaPlayer mediaPlayer;

    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;
    private static final int WAKE_LOCK = 333;

    //Used to pause/resume MediaPlayer
    private int resumePosition = 0;

    //AudioFocus
    private AudioManager audioManager;

    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();

    //List of available Audio files
    private ArrayList<Audio> audioList;
    private int audioIndex = -1;
    private Audio activeAudio; //an object on the currently playing audio


    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyCallback telephonyCallback;
    private TelephonyManager telephonyManager;
    private TelephonyCallback.CallStateListener newCallStateListener;
    public PlaybackStatus playbackStatusPublic;

    private   NotificationManager nManager;
    private String CHANNEL_ID = "HASHIM_CHANNEL-ID_02";
    private NotificationCompat.Builder nBuilder;
    private Notification notification ;
    private RemoteViews remoteView;
    private boolean canContiueAfterFucus =  false;
    WifiManager.WifiLock wifiLock;

    PowerManager pm;
    PowerManager.WakeLock wl;


    String TAG = "MediaPlayerService";

    MediaPlayerService instance;

    /**
     * Service lifecycle methods
     */
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startWakeLock(){
//        batteryOptimization();
         pm = getApplicationContext().getSystemService(PowerManager.class);
         wl = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                 String.valueOf(WAKE_LOCK));
        if(wl.isHeld() == false) {  // but we don't hold it
            wl.acquire();
        }
    }
    private void closeWakeLock(){
        if (wl != null)
            wl.release();
        wl = null;
        pm = null;


    }
    private void batteryOptimization(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.

        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
        Log.d(TAG, "onCreate: ");
    }

    //The system calls this method when an activity, requests the service be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {



            instance =this;
            Log.d(TAG, "onStartCommand: ");
            //Load data from SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
        }

        if (mediaSessionManager == null && mediaSession == null) {
            try {
                //Toast.makeText(getApplicationContext(), "onStartCommand", Toast.LENGTH_SHORT).show();

                if (SDK_INT >= Build.VERSION_CODES.M) {
                    startWakeLock();
                }
                Log.d(TAG, "onStartCommand:  test");
                initMediaSession();
                initMediaPlayer();
                updateMetaData();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
//        return START_STICKY;
//        return START_NOT_STICKY;

        return super.onStartCommand(intent, flags, startId);
    }

    /*@Override
    public boolean onUnbind(Intent intent) {
        mediaSession.release();
        //Toast.makeText(getApplicationContext(), "onUnbind", Toast.LENGTH_SHORT).show();

//        removeNotification();
        return super.onUnbind(intent);
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroysasd: " +"onDestroy" );

        destroyService();
        instance = null;
    }

    private void destroyService(){
        //Toast.makeText(getApplicationContext(), "destroyService", Toast.LENGTH_SHORT).show();
        StorageUtil storage = new StorageUtil(getApplicationContext());
//        storage.storeServiceBound(false);
        if (mediaPlayer != null) {
            pauseMedia();
            stopMedia();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        storage.storeIsMediaStoppedFromUser(false);

        removeNotification();

        //unregister BroadcastReceivers
//        unregisterReceiver(becomingNoisyReceiver);
//        unregisterReceiver(playNewAudio);
        closeWakeLock();
        stopSelf();


        //clear cached playlist
//        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
    }
    /**
     * Service Binder
     */
    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MediaPlayerService.this;
        }

        public MediaPlayer getMediaPlayer() {
            // Return this instance of LocalService so clients can call public methods
            return mediaPlayer;
        }
        public void destroyFromOutside(){
            destroyService();
        }


    }


    /**
     * MediaPlayer callback methods
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
        Log.d(TAG, "onBufferingUpdate: " + percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed.
//        skipToNext();
        Log.d(TAG, "onCompletion: ");
        StorageUtil storage = new StorageUtil(getApplicationContext());

        if (storage.loadIsPlayerRepeat()){
            repeatMedia();
        }else if(storage.loadIsPlayerShuffle()){
            shuffleMedia();
        }else{
            SkipToNextPublic();

        }
//        playbackAction(2);
        /*stopMedia();

        removeNotification();
        //stop the service
        stopSelf();*/
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //        //Invoked when there has been an error during an asynchronous operation

        Log.d(TAG, "onError what: " + what );
        Log.d(TAG, "onError extra: " +  extra);
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        if (what == -38){
            mp.reset();
        }
        Log.d("MediaPlayer what", String.valueOf(what));

        //Toast.makeText(getApplicationContext(), String.valueOf(what), Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        Log.d(TAG, "onPrepared: ");
        StorageUtil storage = new StorageUtil(getApplicationContext());
        Log.d(TAG, "onPrepared isMediaStoppedFromUser: " +storage.loadIsMediaStoppedFromUser() );

        if (!mp.isPlaying() && storage.loadIsMediaStoppedFromUser()==false){
            Intent broadcastIntent = new Intent(Broadcast_updateProgressBarReceiver);
            broadcastIntent.setPackage(getPackageName());
            sendBroadcast(broadcastIntent);

            playMedia();
            setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 347);
        }
        Permissions permissions = new Permissions(this, null);

        if (!activeAudio.getData().contains("http")) {
            if (!permissions.checkStoragePermissionForService()) {
                Log.d(TAG, "onPrepared: error load , no permission to open audio" );
                skipToNext();

            }
        }


    }

    public void mediaPlayeronPrepared(){

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    @Override
    public void onAudioFocusChange(int focusState) {

        Log.d(TAG, "onAudioFocusChange: " + focusState);
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (canContiueAfterFucus){
                    if (mediaPlayer == null) initMediaPlayer();
                    else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                    mediaPlayer.setVolume(1.0f, 1.0f);
                }

                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    PausePublic();
//                mediaPlayer.release();
//                mediaPlayer = null;
                }

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer != null && mediaPlayer.isPlaying()){
                    canContiueAfterFucus = true;
                    mediaPlayer.pause();
                }else{
                    canContiueAfterFucus = false;

                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }


    /**
     * AudioFocus
     */
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        try {
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                    audioManager.abandonAudioFocus(this);
        } catch (Exception e) {
            e.printStackTrace();
            return true;

        }
    }


    /**
     * MediaPlayer actions
     */
    private void initMediaPlayer() {



        startMediaSeparatly();

//        new Thread(runnable).start();
//        dfa.execute("");
//        performOnBackgroundThread(busyLoop);
//        busyLoop.run();
        /*try {
            mediaPlayer.prepareAsync();
            setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 463);

        } catch (IOException e) {
            e.printStackTrace();
        }*/

        /*try {
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
    public void startMediaSeparatly(){
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();//new MediaPlayer instance

//        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
         /*wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        wifiLock.acquire();*/



        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();




        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "audio/mp3"); // change content type if necessary
            headers.put("Accept-Ranges", "bytes");
            headers.put("Status", "206");
            headers.put("Cache-control", "no-cache");
            // Set the data source to the mediaFile location
            String dataSource = activeAudio.getData();
            if (dataSource.startsWith("http://") || dataSource.startsWith("https://")) {
                mediaPlayer.setDataSource(dataSource);
            } else {
                Uri uri = Uri.parse(dataSource);
                mediaPlayer.setDataSource(this, uri, headers);
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            //Set up MediaPlayer event listeners
            mediaPlayer.setOnCompletionListener(this );
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnInfoListener(this);
            SeparateFunctions test =new  SeparateFunctions(getApplicationContext());
            Log.d(TAG, "onCreate: dfsdfs"+ test.isNetworkAvailable());
            if (!test.isNetworkAvailable() && activeAudio.getData().contains("http")){
                Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
                destroyService();
            }

            if ( !activeAudio.getData().contains("http") || test.isNetworkAvailable()  ){
                Permissions permissions = new Permissions(this, null);


                if (activeAudio.getData().contains("http")){
                   /* AsyncPrepareMediaPlayer runner = new AsyncPrepareMediaPlayer();
                    runner.execute();*/
//                    runThread();
//
                    mediaPlayer.prepareAsync();

                }else{
                    if (permissions.checkStoragePermissionForService()){
                        mediaPlayer.prepare();
                        /*AsyncPrepareMediaPlayer runner = new AsyncPrepareMediaPlayer();
                        runner.execute();*/
                    }else{
                        mediaPlayer.prepare();
                        /*AsyncPrepareMediaPlayer runner = new AsyncPrepareMediaPlayer();
                        runner.execute();*/
                        skipToNext();

                    }
                }


//                new Player().execute(uri.toString());
            }



        } catch (IOException e) {
            Log.d(TAG, "initMediaPlayer: " + e);
            e.printStackTrace();
            stopSelf();
        }


    }
    private void runThread(){
        new Thread ( new Runnable() {
            @Override
            public void run() {
                mediaPlayer.prepareAsync();
                // This code will run in another thread. Usually as soon as start() gets called!
            }
        }).start();
    }
    private class AsyncPrepareMediaPlayer extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            try {
                Looper.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                //int time = Integer.parseInt(params[0])*1000;
                mediaPlayer.prepareAsync();
                Toast.makeText(getApplicationContext(), "prepare", Toast.LENGTH_SHORT).show();

            }  catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                initMediaPlayer();
            }
            return "";
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            //progressDialog.dismiss();
            // finalResult.setText(result);
        }


        @Override
        protected void onPreExecute() {
            /*progressDialog = ProgressDialog.show(context,
                    "ProgressDialog",
                    "Wait for  seconds");*/
        }


        @Override
        protected void onProgressUpdate(String... text) {
            // finalResult.setText(text[0]);

        }
    }


    private void playMedia() {

        Log.d(TAG, "playMedia: ");
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING,479);


        }
    }

    private void stopMedia() {

        /*StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.storeIsMediaStoppedFromUser(true);*/

        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            resumePosition = 0;
            setMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED , 491);

        }
    }

    private void pauseMedia() {
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.storeIsMediaStoppedFromUser(true);
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {

            resumePosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PAUSED , 500);
        }
    }

    private void resumeMedia() {
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.storeIsMediaStoppedFromUser(false);
        Log.d(TAG, "resumeMedia: ");
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {


            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
            setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 509);

        }
    }

    private void skipToNext() {
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.storeIsMediaStoppedFromUser(false);
        if (audioIndex == audioList.size() - 1) {
            //if last in playlist
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get next in playlist
            activeAudio = audioList.get(++audioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 532);

    }
    private void repeatMedia() {


        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 544);

    }
    private void shuffleMedia() {

        Random rand = new Random();
        int currentSongIndex = rand.nextInt((audioList.size() - 1) - 0 + 1) + 0;
        activeAudio = audioList.get(currentSongIndex);

        new StorageUtil(getApplicationContext()).storeAudioIndex(currentSongIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 561);

    }


    private void skipToPrevious() {
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.storeIsMediaStoppedFromUser(false);
        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get previous in playlist
            activeAudio = audioList.get(--audioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 585);

    }

    public void STATE_PLAYING_public(){
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 592);

    }

    public void SkipToNextPublic(){
        skipToNext();
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 600);

    }
    public void SkipToPreviousPublic(){
        skipToPrevious();
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 607);

    }
    public void ResumePublic(){
        resumeMedia();
        buildNotification(PlaybackStatus.PLAYING);
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 613);
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 614);


    }
    public void PausePublic(){
        pauseMedia();
        buildNotification(PlaybackStatus.PAUSED);
        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PAUSED , 621);

    }

     public MediaPlayer CurrentServiceMediaPlayer(){
         return mediaPlayer ;
    }




    /**
     * ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs
     */
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()){
                    canContiueAfterFucus = true;
                }else {
                    canContiueAfterFucus = false;
                }
            }
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        ContextCompat.registerReceiver(this, becomingNoisyReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    /**
     * Handle PhoneState changes
     */
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        if (SDK_INT >= Build.VERSION_CODES.S) {
            Log.d(TAG, "callStateListener: 31 sdk " );
            telephonyCallback = new TeleMan();

            if (
                    (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            ){
                /*if (SDK_INT >= Build.VERSION_CODES.M) {
                    NewQuranPlayer.NewQuranPlayerInstance.requestPermissions(new String[]{
                                    android.Manifest.permission.READ_PHONE_STATE,

                            },
                            123);
                }*/
            }else{
                telephonyManager.registerTelephonyCallback(getMainExecutor(), telephonyCallback);
            }

        }else{
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    Log.d(TAG, "onCallStateChangeddsasd: " + state);
                    switch (state) {
                        //if at least one call exists or the phone is ringing
                        //pause the MediaPlayer
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                        case TelephonyManager.CALL_STATE_RINGING:
                            if (mediaPlayer != null) {
                                if (mediaPlayer.isPlaying()){
                                    canContiueAfterFucus = true;
                                }else {
                                    canContiueAfterFucus = false;
                                }
                                PausePublic();
//                                pauseMedia();
                                ongoingCall = true;
                            }
                            break;
                        case TelephonyManager.CALL_STATE_IDLE:
                            // Phone idle. Start playing.
                            if (mediaPlayer != null) {
                                if (ongoingCall) {
                                    ongoingCall = false;
                                    if (canContiueAfterFucus){
                                        resumeMedia();

                                    }
                                }
                            }
                            break;
                    }
                }
            };

            telephonyManager.listen(phoneStateListener,
                    PhoneStateListener.LISTEN_CALL_STATE);
        }

        // Register the listener with the telephony manager
        // Listen for changes to the device call state.

    }

    /**
     * MediaSession and Notification actions
     */
    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists
        if (mediaSession != null) return; //mediaSessionManager exists

//        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);


        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();

                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 732);
            }

            @Override
            public void onPause() {
                super.onPause();

                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PAUSED , 741);

            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();

                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT , 749);
                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 753);

            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();

                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS , 761);
                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 765);

            }

            @Override
            public void onStop() {
                super.onStop();
                //Toast.makeText(getApplicationContext(), "onStop service", Toast.LENGTH_SHORT).show();

                removeNotification();
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED , 775);

                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
                mediaPlayer.seekTo((int) position);
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 785);

            }
        });
//        setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING,789);

    }

    private void setMediaSessionPlaybackState(int state , int lineNumber){

//        Toast.makeText(getApplicationContext(), String.valueOf(mediaPlayer.getCurrentPosition()), Toast.LENGTH_SHORT).show();
//        Log.d(TAG, "setMediaSessionPlaybackState: in line  " + lineNumber + " --- "+mediaPlayer.getCurrentPosition());
        if (mediaPlayer == null)
            return;
        mediaSession.setPlaybackState(
                new PlaybackStateCompat.Builder()
                        .setState(state, mediaPlayer.getCurrentPosition(), 1f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
        );


        /*mediaSession.setPlaybackState(
                new PlaybackStateCompat.Builder()
                        .setState(state, mediaPlayer != null ?mediaPlayer.getCurrentPosition():0, 1f)
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
        );*/


    }
    public String getTitle(){

        try {
            return  activeAudio.getTitle();
        } catch (Exception e) {
            e.printStackTrace();
            return " ";
        }

    }
    public String getArtist(){
        try {
            return  activeAudio.getArtist();
        } catch (Exception e) {
            e.printStackTrace();
            return " ";
        }

    }
    public String getData(){
        try {
            return  activeAudio.getData();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                R.drawable.mystream); //replace with medias albumArt
        // Update the current metadata

        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTitle())
                .build());

        if (mediaPlayer != null) {
            try {
                mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration())
                        .build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private void createNotificationChannel(Context parent) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = parent.getString(R.string.channel_name_2);
            String description = parent.getString(R.string.channel_description_2);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = parent.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void buildNotification(PlaybackStatus playbackStatus) {

        /**
         * Notification actions -> playbackAction()
         *  0 -> Play
         *  1 -> Pause
         *  2 -> Next track
         *  3 -> Previous track
         */

        Log.d("asdsadsa", "buildNotification: ");
        if (mediaPlayer == null)
            return;
        createNotificationChannel(this);
        playbackStatusPublic = playbackStatus;
        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.mystream); //replace with your own image

        MediaControllerCompat controller = mediaSession.getController();

        nBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this,CHANNEL_ID)
                .setShowWhen(false)
                .setColor(getResources().getColor(R.color.blue))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(getNotificationIcon())
//                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentText(activeAudio.getArtist())
                .setContentTitle(activeAudio.getTitle())
                .setContentInfo(activeAudio.getAlbum())
//                .setProgress(mediaPlayer.getDuration(),mediaPlayer.getCurrentPosition(),true)

                .setContentIntent(createContentIntent())
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "close", playbackAction(4))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
//                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2)
                        )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setOngoing(true);


        /*remoteView = new RemoteViews(this.getPackageName(), R.layout.notificatonview);
        remoteView.setTextViewText(R.id.aya,notificationTitle);
        remoteView.setTextViewText(R.id.recite, ReciteNameText);*/



        //set the button listeners
//        setListeners(remoteView);
//        nBuilder.setContent(remoteView);

//        nManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        nManager.notify(NOTIFICATION_ID, nBuilder.build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, nBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIFICATION_ID, nBuilder.build());
        }
//        activeAudio.getRecitesName();
        Log.d(TAG, "buildNotification: " + activeAudio.getRecitesName());


//        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }
    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(getApplicationContext(), NewQuranPlayer.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        openUI.putExtra(MusicPlayerActivity.EXTRA_START_FULLSCREEN, true);
        Log.d(TAG, "createContentIntent: " + activeAudio.getRecitesName());
        openUI.putExtra("RecitesName",activeAudio.getRecitesName());
        openUI.putExtra("Rewayat",activeAudio.getRewayat());
        openUI.putExtra("RealRecitesName",activeAudio.getRealRecitesName());
        if (audioIndex != -1 && audioIndex < audioList.size()){

            openUI.putExtra("RecitesAYA",String.valueOf(audioIndex));
        }else{
            openUI.putExtra("RecitesAYA","0");

        }
        openUI.putExtra("IsRadio",activeAudio.getIsRadio());
        openUI.putExtra("isStartFromNotification",true);
        openUI.putExtra("currentPlayerPosition",mediaPlayer.getCurrentPosition());
        /*if (description != null) {
            openUI.putExtra(MusicPlayerActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION, description);
        }*/
        return PendingIntent.getActivity(getApplicationContext(), 159, openUI,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 1003);

                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);

                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            case 4:
                // close notification and end the service
//                destroyService();
                playbackAction.setAction(ACTION_CLOSE);

//                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);

            default:
                break;
        }
        return null;
    }

    private void removeNotification() {
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //Toast.makeText(getApplicationContext(), "removeNotification", Toast.LENGTH_SHORT).show();

        if (nManager!= null)
            nManager.cancel(NOTIFICATION_ID);


        try {
            stopForeground(true);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        } else if (actionString.equalsIgnoreCase(ACTION_CLOSE)) {
            destroyService();
        }

    }


    /**
     * Play new Audio
     */
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences
            audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            //Toast.makeText(getApplicationContext(), "playNewAudio", Toast.LENGTH_SHORT).show();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            if (mediaPlayer != null){
                mediaPlayer.reset();

            }


            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
            setMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING , 1095);

        }
    };

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(com.medoapps.www.onlinequran.NewQuranPlayer.Broadcast_PLAY_NEW_AUDIO);
        ContextCompat.registerReceiver(this, playNewAudio, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

    }
    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.mystreamwhite : R.drawable.mystream;
    }

//    @RequiresApi(api = Build.VERSION_CODES.S)
    @RequiresApi(api = Build.VERSION_CODES.S)
    public class TeleMan extends TelephonyCallback implements TelephonyCallback.ServiceStateListener, TelephonyCallback.CallStateListener {
    public TeleMan() {
        super();
    }


    @Override
    public void onServiceStateChanged(@NonNull ServiceState serviceState) {
        Log.d(TAG, "onServiceStateChangedfdfds: "  + serviceState);


    }

    @Override
    public void onCallStateChanged(int state) {
        Log.d(TAG, "onCallStateChangedsfdsf: " + state);
        switch (state) {
            case TelephonyManager.CALL_STATE_OFFHOOK:
            case TelephonyManager.CALL_STATE_RINGING:
                if (mediaPlayer != null) {
                    canContiueAfterFucus = mediaPlayer.isPlaying();
                    PausePublic();
                    ongoingCall = true;
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if (mediaPlayer != null && ongoingCall) {
                    ongoingCall = false;
                    if (canContiueAfterFucus) {
                        resumeMedia();
                    }
                }
                break;
        }
    }
}

}
