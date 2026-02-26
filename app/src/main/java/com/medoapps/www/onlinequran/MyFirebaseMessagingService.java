package com.medoapps.www.onlinequran;

import static android.os.Build.VERSION.SDK_INT;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.medoapps.www.onlinequran.hashimyoutubeplayer.YoutubePlayerViewActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Random;

//import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_USER_KEY = "user_key";
    int random ;
    String CHANNEL_ID = "HASHIM_CHANNEL-ID_04";

    String YouTubeVideoId ;
    String title ;
    String body ;
    String id ;
    String uid ;
    String Thumb_Url ;
    private DatabaseReference mUserReference;

    private static final String TAG = "MessagingService";

    @Override
    public void onCreate() {
        super.onCreate();

    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        random = new Random().nextInt((900 - 100) + 1) + 100;

//        Log.d(TAG, "From: Message data payload" + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());


            Map<String, String> data = remoteMessage.getData();
            id = data.get("id");
            uid = data.get("uid");
            YouTubeVideoId = data.get("YouTubeVideoId");
            title = data.get("title");
            body = data.get("body");
            Thumb_Url = data.get("Thumb_Url");
            Log.d(TAG, "Message data payload: " + title);
            scheduleJob();

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

        }
    }

    private void scheduleJob() {
        createNotificationChannel(getApplicationContext());

        new generatePictureStyleNotification(getApplicationContext(),title, body,
                Thumb_Url).execute();
    }


    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    private void sendRegistrationToServer(String token) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            mUserReference = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(getUid());
            mUserReference.child("FCMToken").setValue(token);
        }


    }


    //This method is only generating push notification
    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, RecitesName.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }


    private PendingIntent createContentIntent(Context context) {

        Intent openUI = new Intent(context, YoutubePlayerViewActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        openUI.putExtra(EXTRA_POST_KEY, id);
        openUI.putExtra(EXTRA_USER_KEY, uid);
        openUI.putExtra("videoId", YouTubeVideoId);
        openUI.putExtra("videoTitle", title);
        openUI.putExtra("videoDescription", body);
        return PendingIntent.getActivity(context, random, openUI,
                PendingIntent.FLAG_IMMUTABLE);
    }
    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.mystreamwhite : R.drawable.mystream;
    }

    public class generatePictureStyleNotification extends AsyncTask<String, Void, Bitmap> {

        private Context mContext;
        private String title, message, imageUrl;

        public generatePictureStyleNotification(Context context, String title, String message, String imageUrl) {
            super();
            this.mContext = context;
            this.title = title;
            this.message = message;
            this.imageUrl = imageUrl;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            InputStream in;
            try {
                URL url = new URL(this.imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            long when = System.currentTimeMillis();
            NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            Intent notificationIntent = new Intent(getApplicationContext(), RecitesName.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                    notificationIntent, PendingIntent.FLAG_MUTABLE);


            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


            NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(
                    getApplicationContext(),CHANNEL_ID)
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle(getApplicationContext().getResources().getString(R.string.watch))
                    .setContentText(title).setSound(alarmSound)
                    .setLargeIcon(result)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(result)
                            .bigLargeIcon((android.graphics.Bitmap) null))
                    .setAutoCancel(true)
                    .setWhen(when)
                    .setContentIntent(createContentIntent(getApplicationContext()))
                    ;


            notificationManager.notify(random, mNotifyBuilder.build());
//            MID = MID+1;
//            Log.d("TAG", "onReceivedfdsf: " + random);
        }
    }
}