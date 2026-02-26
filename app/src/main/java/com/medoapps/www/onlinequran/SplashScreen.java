package com.medoapps.www.onlinequran;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.medoapps.www.onlinequran.QuranApplication.OnShowAdCompleteListener;
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import java.util.ArrayList;

public class SplashScreen extends AppCompatActivity {
    private static final String TAG = "SpalshScreen";
    private AdView mAdView;
    public ImageView splash;
    private static final long COUNTER_TIME = (long) 1;
    private PreferenceManager prefManager;

    String YouTubeVideoId ;
    String title ;
    String body ;
    String id ;
    String uid ;
    String Thumb_Url ;
    ArrayList<Post> PushNotificationVideoArrayList = new ArrayList<>();;

    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_USER_KEY = "user_key";
    CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        /*LnaguageClass lc = new LnaguageClass(this);
        SettingSaved ss = new SettingSaved(this);
        ss.LoadData();
        lc.setAppLocale(SettingSaved.LanguageSelect==1?"ar":"en-US");*/
        prefManager = new PreferenceManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            SeparateFunctions separateFunctions = new SeparateFunctions(SplashScreen.this);
            separateFunctions.changeAppThemeGlobally();
        }



        splash=(ImageView) findViewById(R.id.imageView3);
        //MobileAds.initialize(this, getString(R.string.ad_APP_ID));
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        //load interstial ad by atimer
        AdmobInterstitial.loadInterstitial(this);

        createTimer(COUNTER_TIME);




    }

    private void checkPushNotificationData(){
        if (getIntent() != null) {
            Bundle b=getIntent().getExtras();


            if (b != null)
            {
                PushNotificationVideoArrayList.clear();
                if (b.getString("videoId") != null){
                    title = b.getString("title");
                    id =  b.getString("id");
                    uid =  b.getString("uid");
                    YouTubeVideoId =  b.getString("videoId");
                    body =  b.getString("body");
                    Thumb_Url =  b.getString("Thumb_Url");

                    Post post = new Post();
                    post.id = id;
                    post.title = title;
                    post.uid = uid;
                    post.YouTubeVideoId = YouTubeVideoId;
                    post.body = body;
                    post.Thumb_Url = Thumb_Url;

                    PushNotificationVideoArrayList.add(post);

                    StorageUtil storage = new StorageUtil(getApplicationContext());
                    storage.storePushNotificationVideo(PushNotificationVideoArrayList);
                }

            }



        }
    }
    private void createTimer(long seconds) {
        final TextView counterTextView = findViewById(R.id.timer);

        countDownTimer =
                new CountDownTimer(seconds * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
//                        secondsRemaining = ((millisUntilFinished / 1000) + 1);
//                        counterTextView.setText("App is done loading in: " + secondsRemaining);
                    }

                    @Override
                    public void onFinish() {
                        Log.d(TAG, "asdsdsadsadsa: " );
//                        secondsRemaining = 0;
//                        counterTextView.setText("Done.");
                        checkPushNotificationData();
                        checkOpenAd();

//                        startWelcomeActivity();


                    }
                };
        countDownTimer.start();

    }
    public  void checkOpenAd(){
        Application application = getApplication();

        // If the application is not an instance of QuranApplication, log an error message and
        // start the MainActivity without showing the app open ad.
        if (!(application instanceof QuranApplication)) {
            Log.e(TAG, "Failed to cast application to QuranApplication.");
            startWelcomeActivity();
            return;
        }

        // Show the app open ad.
        ((QuranApplication) application)
                .showAdIfAvailable(
                        SplashScreen.this,
                        new OnShowAdCompleteListener() {
                            @Override
                            public void onShowAdComplete() {
                                startWelcomeActivity();
                            }
                        });
    }

    public void startWelcomeActivity(){
        Intent intent = new Intent(this, WelcomeActivity.class);
        this.startActivity(intent);
        if (countDownTimer!=null){
            countDownTimer.cancel();
        }
        finish();
    }

    private void oldTimerThread(){
        Thread myThread = new Thread(){
            @Override
            public void run() {
                try {

                    sleep(3000);


                    SettingSaved.IsOpen =  1;//App Is Opened
                    SettingSaved.SounlLoad=1;//sound load

                    checkOpenAd();
//                    startWelcomeActivity();


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        myThread.start();
    }

}
