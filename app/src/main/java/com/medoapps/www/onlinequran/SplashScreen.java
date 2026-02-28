package com.medoapps.www.onlinequran;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.medoapps.www.onlinequran.QuranApplication.OnShowAdCompleteListener;
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import java.util.ArrayList;

public class SplashScreen extends AppCompatActivity {
    private static final String TAG = "SplashScreen";
    public ImageView splash;
    private static final long COUNTER_TIME = 2;
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
        androidx.core.splashscreen.SplashScreen splashScreen =
                androidx.core.splashscreen.SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Smooth fade-out for the native splash screen
        splashScreen.setOnExitAnimationListener(splashScreenView -> {
            splashScreenView.getView().animate()
                    .alpha(0f)
                    .setDuration(400)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(splashScreenView::remove)
                    .start();
        });

        prefManager = new PreferenceManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            SeparateFunctions separateFunctions = new SeparateFunctions(SplashScreen.this);
            separateFunctions.changeAppThemeGlobally();
        }

        ImageView goldRing = findViewById(R.id.goldRing);
        splash = (ImageView) findViewById(R.id.imageView3);
        View divider = findViewById(R.id.divider);
        TextView appNameText = findViewById(R.id.appNameText);
        View progressBar = findViewById(R.id.progressBar1);
        TextView bottomAttribution = findViewById(R.id.bottomAttribution);

        // Stage 1: Gold ring scale 0.6→1.0 + fade to 0.15
        goldRing.animate()
                .alpha(0.15f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(700)
                .setStartDelay(200)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();

        // Stage 2: Logo scale 0.9→1.0 + fade in
        splash.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setStartDelay(350)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();

        // Stage 3: Divider scaleX 0→1.0 + fade in (extends from center)
        divider.setPivotX(divider.getWidth() / 2f);
        divider.post(() -> divider.setPivotX(divider.getWidth() / 2f));
        divider.animate()
                .alpha(1f)
                .scaleX(1f)
                .setDuration(400)
                .setStartDelay(600)
                .setInterpolator(new DecelerateInterpolator(2f))
                .start();

        // Stage 4: App name fade in
        appNameText.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(750)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();

        // Stage 5: Progress spinner fade in
        progressBar.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(850)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();

        // Stage 6: Bottom attribution fade in
        bottomAttribution.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(950)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();

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
        countDownTimer =
                new CountDownTimer(seconds * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        Log.d(TAG, "asdsdsadsadsa: " );
                        checkPushNotificationData();
                        checkOpenAd();
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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        myThread.start();
    }

}
