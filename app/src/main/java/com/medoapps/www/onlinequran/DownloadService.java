package com.medoapps.www.onlinequran;

import static android.os.Build.VERSION.SDK_INT;

import static com.medoapps.www.onlinequran.AyaList.Broadcast_LoadAya;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.pm.ServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.medoapps.www.onlinequran.util.MetaDataEditor;
import com.medoapps.www.onlinequran.util.MetaDataEditorHashimUpdate;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


/**
 * Created by MEDO on 3/02/2022.
 */

public class DownloadService extends Service  {


    public static final String ACTION_PLAY = "com.medoapps.www.onlinequran.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.medoapps.www.onlinequran.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.medoapps.www.onlinequran.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.medoapps.www.onlinequran.ACTION_NEXT";
    public static final String ACTION_STOP = "com.medoapps.www.onlinequran.ACTION_STOP";
    public static final String ACTION_CLOSE_DOWNLOAD = "com.medoapps.www.onlinequran.download.ACTION_CLOSE_DOWNLOAD";
    public static final String BROADCAST_DOWNLOAD_PROGRESS = "com.medoapps.www.onlinequran.DOWNLOAD_PROGRESS";
    public static final String EXTRA_PROGRESS = "progress";
    public static final String EXTRA_SURAH_NAME = "surah_name";

    //MediaSession

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 200;
    private static final int WAKE_LOCK = 587;



    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();

    //List of available Audio files
    private ArrayList<AuthorClass> downloadList;
    private int downloadIndex = -1;
    private String RecitesName;
    private String RealRecitesName;
    private AuthorClass activeDownload; //an object on the currently playing audio


    //Handle incoming phone calls
    private boolean ongoingCall = false;
    public PlaybackStatus playbackStatusPublic;

    private   NotificationManager nManager;
    private String CHANNEL_ID = "HASHIM_CHANNEL-ID_03";
    private boolean canContiueAfterFucus =  false;
    WifiManager.WifiLock wifiLock;

    PowerManager pm;
    PowerManager.WakeLock wl;

    DownloadFileAsync dfa;

    public NotificationManager mNotifyManager;
    public NotificationCompat.Builder mBuilder;

    String RecitesAYA="";
    String TAG = "DownloadService";

    String[] allUrl;
    private int currenDdownloadIndex = 0;

    int servicStartId ;
    boolean ISDonwloadingCanceled = false;
    Handler mHandler=new Handler();
    private String notificationContentText;

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
                PowerManager.FULL_WAKE_LOCK
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
    @Override
    public void onCreate() {
        super.onCreate();
        dfa = new DownloadFileAsync();
//        register_playNewAudio();
    }

    //The system calls this method when an activity, requests the service be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            //Load data from SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            downloadList = storage.loadDownloadlist();
            downloadIndex = storage.loadDownloadIndex();
            RecitesName = storage.loadDownloadRecitesName();
            RealRecitesName = storage.loadDownloadRealRecitesName();


            allUrl = new String[ downloadList.size() ];

            int index =0;

            for (AuthorClass temp: downloadList) {
                allUrl[index] = temp.ImgUrl;
                 index += 1;
            }

            if (downloadIndex != -1 && downloadIndex < downloadList.size()) {
                //index is in a valid range
                activeDownload = downloadList.get(downloadIndex);
                if (!ISDonwloading){
                    startDownload(activeDownload.ImgUrl,activeDownload.ServerName );
                }else {
                    Toast.makeText(getApplicationContext(), "Wait ,, download under process", Toast.LENGTH_SHORT).show();
                }

            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        handleIncomingActions(intent);
        return START_NOT_STICKY;
//        servicStartId = startId;
//        return super.onStartCommand(intent, flags, startId);
    }

    public void runa() throws Exception{
        mHandler.post(new Runnable(){
            public void run(){
//                Toast.makeText(MyService.this, "test", Toast.LENGTH_LONG).show()
            }
        }
    );}


    public void startDownload( String ImgUrl,String ServerName ) {
        notificationContentText = RealRecitesName + " - " + activeDownload.RealName;

        createNotificationChannel(getApplicationContext());
        String GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL";


        // The id of the group.
        String groupId = "my_group_01";
        CharSequence groupName = "hashim_notification";
        RecitesAYA=ServerName;
        String url = ImgUrl ;// "http://farm1.static.flickr.com/114/298125983_0e4bf66782_b.jpg";
        startNotification(0,"");


        try {
            if (allUrl.length == 0)
                return;
            ISDonwloadingCanceled = false;
            dfa.execute(allUrl[downloadIndex]);
        } catch (Exception e) {
            stopSelf();
            Toast.makeText(getApplicationContext(), "can not downlaod", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void startNotification( int progress,String ContentText){

        /*if (ContentText != null && !ContentText.equalsIgnoreCase("")   ){

            notificationContentText = ContentText;
        }
        if (ContentText == null){
            ContentText = notificationContentText;
        }*/
        Intent cancel = new Intent(getApplicationContext(), notificationButtonReceiver.class);
        cancel.setAction(notificationButtonReceiver.ACTION9);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, cancel, PendingIntent.FLAG_IMMUTABLE);
        createNotificationChannel(this);
        /*Intent buttonIntent = new Intent(getApplicationContext(), ButtonReceiver.class);
        buttonIntent.putExtra("notificationId",NOTIFICATION_ID);

//Create the PendingIntent
        PendingIntent btPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, buttonIntent,0);
        */
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.mystream);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String progressText = progress > 0
                ? getString(R.string.aya_download_progress) + " " + progress + "%"
                : getString(R.string.aya_download_progress);

        mBuilder = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID);
        mBuilder.setContentTitle(notificationContentText)
                .setContentText(progressText)
                .setSmallIcon(getNotificationIcon())
                .setLargeIcon(largeIcon)
                .setProgress(100, progress, false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(new NotificationCompat.Action(R.drawable.ic_cancel_white_18dp,getString(R.string.cancel),playbackAction(0)))
                .setOnlyAlertOnce(true)
                .setOngoing(true);
//                .addAction(new NotificationCompat.Action(R.drawable.ic_cancel_white_18dp,getString(R.string.cancel),pendingIntent));


        if (SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, mBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIFICATION_ID, mBuilder.build());
        }

        // Broadcast progress to the activity
        try {
            Intent progressIntent = new Intent(BROADCAST_DOWNLOAD_PROGRESS);
            progressIntent.setPackage(getPackageName());
            progressIntent.putExtra(EXTRA_PROGRESS, progress);
            if (activeDownload != null) {
                progressIntent.putExtra(EXTRA_SURAH_NAME, activeDownload.RealName);
            }
            sendBroadcast(progressIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class notificationButtonReceiver extends BroadcastReceiver {
        public static final String ACTION9 = "ACTION9";

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();


            if (ACTION9.equals(action)){

                try {
//                DownloadService.instance3.canceldownload();
//                    Toast.makeText(getApplicationContext(), "closekjkjl", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

        }
    }
    public  boolean ISDonwloading=false;
    InputStream  input;
    OutputStream output;
    FileOutputStream fos;
    BufferedInputStream fis;
    BufferedOutputStream out;
    int contentLength;
    int count;
    long lastProgress = 0;

    class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ISDonwloading=true;

            // Displays the progress bar for the first time for the notification.
            /*mBuilder.setProgress(100, 0, false);
//            mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
            startForeground(NOTIFICATION_ID, mBuilder.build());*/
        }

        @Override
        public String doInBackground(String... aurl) {

            notificationContentText = RealRecitesName + " - " + activeDownload.RealName;

            if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT){
                try {

                    URL url = new URL(aurl[0]);
                    URLConnection conexion = url.openConnection();
                    conexion.connect();
                    contentLength = conexion.getContentLength();

                    int lenghtOfFile = conexion.getContentLength();
                    Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

                    ContentResolver resolver = getApplicationContext()
                            .getContentResolver();

                    Uri audioCollection = new SeparateFunctions(getApplicationContext()).getAudioCollection();
                    ContentValues newSongDetails = new ContentValues();
                    newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME,
                            "AhmedHashim_"+RecitesName + activeDownload.ServerName + ".mp3");
                    newSongDetails.put(MediaStore.Audio.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_MUSIC+"/MyStream");
                    newSongDetails.put(MediaStore.Audio.Media.MIME_TYPE,
                            "audio/mpeg");

                    Uri myFavoriteSongUri = resolver.insert(audioCollection, newSongDetails);

                    Log.d(TAG, "doInBackgroundfdsf:  " + myFavoriteSongUri);
                    File SDPath =  new SeparateFunctions(getApplicationContext()).getAppSpecificDownloadStorageDir(getApplicationContext(),null);
                    if(!SDPath.exists()) {
                        SDPath.mkdirs();
                    }

                    byte data[] = new byte[1024];

                    long total = 0;

                    fis =  new BufferedInputStream(url.openStream());
                    output = resolver.openOutputStream(myFavoriteSongUri);

                    while ((count = fis.read(data)) != -1) {

                        if (ISDonwloadingCanceled) {
                            cancel(true);

                            break;
                        }
                        if (ISDonwloadingCanceled) {
                            cancel(true);

                            break;
                        } else {
                            total += count;
//                            lastProgress = count;
                            int mbNumber = 5*1024;
                            if (total>lastProgress+mbNumber){
                                lastProgress = total;

                                startNotification((int)((total*100)/lenghtOfFile),null);
//                                publishProgress(""+(int)((total*100)/lenghtOfFile));
                            }
                            output.write(data, 0, count);
                        }

                    }
                    MetaDataEditorHashimUpdate metaDataEditor = new MetaDataEditorHashimUpdate(getApplicationContext());
                    metaDataEditor.changeMetaData(SDPath + "/AhmedHashim_" + RecitesName + activeDownload.ServerName + ".mp3");

                    output.flush();
                    output.close();
                    fis.close();

                } catch (Exception e) {

                    Log.d(TAG, "doInBackgroundfdsf: Exception" + e);
                }
            }else{
                try {
                    if (ISDonwloadingCanceled)
                        return null;

                    String tempurl = aurl[0];
                    notificationContentText = RealRecitesName + " - " + activeDownload.RealName;
                    startNotification(0,RealRecitesName + " - " + activeDownload.RealName);

                    URL url = new URL(tempurl);
                    URLConnection conexion = url.openConnection();
                    conexion.connect();

                    int lenghtOfFile = conexion.getContentLength();


                    String folder_main = "My Stream";
                    input = new BufferedInputStream(url.openStream());

                    File SDPath =  new SeparateFunctions(getApplicationContext()).getAppSpecificDownloadStorageDir(getApplicationContext(),null);

                    if (!SDPath.exists()) {
                        SDPath.mkdirs();
                    }

                    output = new FileOutputStream(SDPath + "/AhmedHashim_" + RecitesName + activeDownload.ServerName + ".mp3");

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        if (ISDonwloadingCanceled) {
                            cancel(true);

                            break;
                        }
                        if (ISDonwloadingCanceled) {
                            cancel(true);

                            break;
                        } else {
                            total += count;
                            int mbNumber = 5*1024;
                            if (total>lastProgress+mbNumber){
                                lastProgress = total;

                                startNotification((int)((total*100)/lenghtOfFile),null);
//                                publishProgress(""+(int)((total*100)/lenghtOfFile));
                            }
                            output.write(data, 0, count);
                        }
                    }

                    MetaDataEditor metaDataEditor = new MetaDataEditor(getApplicationContext());
                    metaDataEditor.changeMetaData(SDPath + "/AhmedHashim_" + RecitesName + activeDownload.ServerName + ".mp3");
                    output.flush();
                    output.close();
                    input.close();
                } catch (Exception e) {}
            }

            /*int i;
            for (i = 0; i <= 100; i += 5) {
                // Sets the progress indicator completion percentage
                publishProgress(String.valueOf(Math.min(i, 100)));
                Log.d(TAG, "doInBackground: loop " + Math.min(i, 100));

                try {
                    // Sleep for 5 seconds
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    Log.d("TAG", "sleep failure");
                }
            }*/

//            ISDonwloadingCanceled = false;
            return null;

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            ISDonwloading = false;
            destroyService();
        }

        protected void onProgressUpdate(String... progress) {
            // Log.d("ANDRO_ASYNC",progress[0]);
            //mProgressDialog.setProgress(Integer.parseInt(progress[0]));
//            progressBar.setProgress(Integer.parseInt(progress[0]));

            // Update progress
            if (!ISDonwloadingCanceled){
                /*mBuilder.setProgress(100, Integer.parseInt(progress[0]), false);
                startForeground(NOTIFICATION_ID, mBuilder.build());*/
                startNotification(Integer.parseInt(progress[0]),null);

//                mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
            }else
            {
//                mBuilder.setContentText(getString(R.string.download_canceled));
//                mBuilder.setProgress(0, 0, false);
//                mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());


            }
        }

        @Override
        protected void onPostExecute(String unused) {

            ISDonwloading=false;
            /*mBuilder.setContentText(getString(R.string.download_done));
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);

            startForeground(NOTIFICATION_ID, mBuilder.build());*/



            startNotification(0,getString(R.string.download_done));
            Toast.makeText(getApplicationContext(), getString(R.string.download_done), Toast.LENGTH_LONG).show();
            if (ISDonwloadingCanceled){
                ISDonwloadingCanceled = false;
                destroyService();
            }else{
                if (downloadIndex == downloadList.size() - 1) {
                    destroyService();

                }else {
                    try {
                        downloadIndex =+1;
                        //get next in playlist
                        activeDownload = downloadList.get(downloadIndex);
                        dfa = null;
                        dfa = new DownloadFileAsync();
                        dfa.execute(allUrl[downloadIndex]);
                    } catch (Exception e) {
                        destroyService();

                        e.printStackTrace();
                    }
                }

            }
            try {
                Intent broadcastIntent = new Intent(Broadcast_LoadAya);
                broadcastIntent.setPackage(getPackageName());
                sendBroadcast(broadcastIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }

//            skipToNext();
        }




    }
    private void skipToNext() {

        if (downloadIndex == downloadList.size() - 1) {
            //if last in playlist
            downloadIndex = 0;
            activeDownload = downloadList.get(downloadIndex);
            new StorageUtil(getApplicationContext()).storeDownloadIndex(-1);

            destroyService();
        } else {
            //get next in playlist
            activeDownload = downloadList.get(++downloadIndex);
            //Update stored index
            new StorageUtil(getApplicationContext()).storeDownloadIndex(downloadIndex);

            startDownload(activeDownload.ImgUrl,activeDownload.ServerName );
        }

    }

    public void canceldownload(){
        dfa.cancel(true);
        ISDonwloading=false;
        ISDonwloadingCanceled=true;
        try {
            Intent broadcastIntent = new Intent(Broadcast_LoadAya);
            broadcastIntent.setPackage(getPackageName());
            sendBroadcast(broadcastIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Dismiss the download notification
        try {
            if (mNotifyManager != null) {
                mNotifyManager.cancel(NOTIFICATION_ID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        //Toast.makeText(getApplicationContext(), "onDestroy service", Toast.LENGTH_SHORT).show();

//        destroyService();

    }

    private void destroyService(){

        canceldownload();
        allUrl = null;
        downloadList = null;
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.clearCacheDownloadslist();
        removeNotification();

        try {
//            unregisterReceiver(playNewAudio);
        } catch (Exception e) {
            e.printStackTrace();
        }


//        storage.storeDownloadlist(null);
        closeWakeLock();
        stopSelf();

    }
    /**
     * Service Binder
     */
    public class LocalBinder extends Binder {
        public DownloadService getService() {
            // Return this instance of LocalService so clients can call public methods
            return DownloadService.this;
        }

    }







    private void createNotificationChannel(Context parent) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = parent.getString(R.string.channel_name_3);
            String description = parent.getString(R.string.channel_description_3);
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
    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(getApplicationContext(), NewQuranPlayer.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        openUI.putExtra(MusicPlayerActivity.EXTRA_START_FULLSCREEN, true);
//        openUI.putExtra("RecitesName", activeDownload.getRecitesName());
//        openUI.putExtra("Rewayat", activeDownload.getRewayat());
//        openUI.putExtra("RealRecitesName", activeDownload.getRealRecitesName());
//        openUI.putExtra("RecitesAYA",String.valueOf(audioIndex));
//        openUI.putExtra("IsRadio", activeDownload.getIsRadio());
//        openUI.putExtra("isStartFromNotification",true);
//        openUI.putExtra("currentPlayerPosition",mediaPlayer.getCurrentPosition());
        /*if (description != null) {
            openUI.putExtra(MusicPlayerActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION, description);
        }*/
        return PendingIntent.getActivity(getApplicationContext(), 025, openUI,
                PendingIntent.FLAG_IMMUTABLE);
    }


    private void removeNotification() {

        if (nManager!= null)
            nManager.cancel(NOTIFICATION_ID);

        if (mNotifyManager!= null)
            mNotifyManager.cancel(NOTIFICATION_ID);


        try {
            stopForeground(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences


        }
    };

    private void register_playNewAudio() {
        //Register playNewMedia receiver
//        IntentFilter filter = new IntentFilter(com.medoapps.www.onlinequran.NewQuranPlayer.Broadcast_PLAY_NEW_AUDIO);
//        registerReceiver(playNewAudio, filter);

    }
    private int getNotificationIcon() {
        boolean useWhiteIcon = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.mystreamwhite : R.drawable.mystream;
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, DownloadService.class);
//        playbackAction.setAction(ACTION_CLOSE_DOWNLOAD);

//        return PendingIntent.getService(this, actionNumber, playbackAction, 0);
        switch (actionNumber) {
            case 0:
                // close notification and end the service
//                destroyService();
                playbackAction.setAction(ACTION_CLOSE_DOWNLOAD);

                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);

            default:
                break;
        }
        return null;
    }
    private void handleIncomingActions(Intent playbackAction) {

        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_CLOSE_DOWNLOAD)) {
//            Toast.makeText(getApplicationContext(), "close download", Toast.LENGTH_SHORT).show();

            canceldownload();
//            destroyService();
        }

    }
}
