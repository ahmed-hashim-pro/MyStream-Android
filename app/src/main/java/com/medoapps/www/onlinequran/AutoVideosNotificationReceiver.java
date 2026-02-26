package com.medoapps.www.onlinequran;

import static android.os.Build.VERSION.SDK_INT;
import static com.facebook.FacebookSdk.getApplicationContext;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.medoapps.www.onlinequran.hashimyoutubeplayer.YoutubePlayerViewActivity;
import com.medoapps.www.onlinequran.models.Post;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class AutoVideosNotificationReceiver extends BroadcastReceiver {

    int MID=301;
    String CHANNEL_ID = "HASHIM_CHANNEL-ID_04";

    private Post post;
    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_USER_KEY = "user_key";

    ArrayList<Post> youtubeVideosArrayList;
    final int min = 0;
    int max = 0;

    int random ;

    @Override
    public void onReceive(Context context, Intent intent) {

        post = null;
        random = new Random().nextInt((900 - 100) + 1) + 100;
        youtubeVideosArrayList = new ArrayList<>();;
        StorageUtil storage = new StorageUtil(getApplicationContext());
        youtubeVideosArrayList = storage.loadYoutubeVideos();
        if (youtubeVideosArrayList != null && youtubeVideosArrayList.size() !=0){
            max = youtubeVideosArrayList.size()-1;
            final int random = new Random().nextInt((max - min) + 1) + min;

//            Log.d("TAG", "onReceivedfdsf: " + random);
            post = youtubeVideosArrayList.get(random);

        }else {
            return;
        }
        createNotificationChannel(context);

        new generatePictureStyleNotification(context,"Title", "Message",
                post.Thumb_Url).execute();
    }
    private PendingIntent createContentIntent(Context context) {

        Intent openUI = new Intent(context, YoutubePlayerViewActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        openUI.putExtra(EXTRA_POST_KEY, post.id);
        openUI.putExtra(EXTRA_USER_KEY, post.uid);
        openUI.putExtra("videoId", post.YouTubeVideoId);
        openUI.putExtra("videoTitle", post.title);
        openUI.putExtra("videoDescription", post.body);
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
                    .setContentText(post.title).setSound(alarmSound)
                    .setLargeIcon(result)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(result)
                            .bigLargeIcon(null))
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