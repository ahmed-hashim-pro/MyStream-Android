/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.medoapps.www.onlinequran;

import java.lang.ref.WeakReference;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.transition.Slide;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.medoapps.www.onlinequran.admin.AdminDashboard;
import com.medoapps.www.onlinequran.data.StaticConfig;
import com.medoapps.www.onlinequran.hashimyoutubeplayer.YouTubePosts;
import com.medoapps.www.onlinequran.hashimyoutubeplayer.YoutubePlayerViewActivity;
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.service.AuthService;
import com.medoapps.www.onlinequran.service.BillingService;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public  class  MainActivity extends BaseActivity {


    BottomNavigationView bottomNavigationView;

    private NavController navController;

    private static final String TAG = "MainActivity";
//    private AdView mAdView;

    public ImageView userImage;
    public ImageView crown_photo;
    public ImageView adminPostPhoto;
    public TextView AdminPostText;
    public TextView userName;
    private DatabaseReference mUserReference;
    //private InterstitialAd mInterstitialAd;
    private int LimitPageNumber = 3;
    private User myAccount;
    private Context context;
    private FloatingActionButton fab_add, addBostBTN ,fab_new_youtube_post ;
    private ExtendedFloatingActionButton admin_fab;

    boolean isAllFabsVisible = false;
    static WeakReference<MainActivity> instance8Ref;
    //private RewardedVideoAd mRewardedVideoAd;
    private static final String AD_UNIT_ID = BuildConfig.ADMOB_AD_UNIT_ID;
    private static final String APP_ID = BuildConfig.ADMOB_APP_ID;


    private RelativeLayout AdminLayout ;
    FirebaseRemoteConfig mFirebaseRemoteConfig;


    private static final String admin_post_photo_url = "admin_post_photo_url";
    private static final String admin_post_text = "admin_post_text";
    private static final String admin_post_targrt_url = "admin_post_targrt_url";
    private static final String admin_post_show = "admin_post_show";
    Resources resources;

    AdView adView ;
    private AdView mAdView;

    private RewardedAd rewardedAd;
    boolean isLoading;

    private ImageView prize;

    private DatabaseReference mDatabase;

    User user;
    CardView completeProfileCard;
    CardView subscribeCard;
    AppBarLayout appbar;
    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_USER_KEY = "user_key";
    private boolean isSubscribedPremium ;
    AuthService authService;

    @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setupWindowAnimations();
        super.onCreate(savedInstanceState);
        authService = new AuthService(MainActivity.this);

        /*Transition fadeTransition =
                TransitionInflater.from(this).
                        inflateTransition(R.transition.activity_fade);*/
        /*LnaguageClass lc = new LnaguageClass(this);
        Locale loc = new Locale(SettingSaved.LanguageSelect==1?"ar_001":"en_US");

        lc.setLocale(loc);*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isNightMode = (getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            if (!isNightMode) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        setContentView(R.layout.activity_main);
        // Drive layout direction from the effective locale, not Locale.getDefault() — the latter is
        // stale after a runtime switch to System, which left the home LTR under an Arabic system.
        getWindow().getDecorView().setLayoutDirection(
                AppLanguage.isRtl(this)
                        ? android.view.View.LAYOUT_DIRECTION_RTL
                        : android.view.View.LAYOUT_DIRECTION_LTR);
        instance8Ref = new WeakReference<>(this);

        // Keep athan alarms in place on every app open; must never break launch.
        try {
            com.medoapps.www.onlinequran.athan.AthanScheduler.rescheduleAll(this);
        } catch (Exception ignored) {
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        try {
            adView = new AdView(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        prize = findViewById(R.id.prize);
        completeProfileCard = findViewById(R.id.completeProfileCard);
        subscribeCard = findViewById(R.id.subscribeCard);
        appbar = findViewById(R.id.appbar);
        mAdView.setVisibility(View.GONE);
        completeProfileCard.setVisibility(View.GONE);
        subscribeCard.setVisibility(View.GONE);
//        loadBannerAd();

        //Initializing the bottomNavigationView
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        applyDockEmojiIcons();

        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            if (appbar == null) return;
            int id = destination.getId();
            boolean mainTab = id == R.id.nav_home || id == R.id.nav_quran
                    || id == R.id.nav_radio || id == R.id.nav_more;
            appbar.setVisibility(mainTab ? View.GONE : View.VISIBLE);
        });

        admin_fab = (ExtendedFloatingActionButton) findViewById(R.id.admin_fab);
        fab_add = (FloatingActionButton) findViewById(R.id.fab_add);
        addBostBTN = (FloatingActionButton) findViewById(R.id.fab_new_post);
        fab_new_youtube_post = (FloatingActionButton) findViewById(R.id.fab_new_youtube_post);

        // [END set_default_values]

        admin_fab.setVisibility(View.GONE);
        fab_add.setVisibility(View.GONE);
        addBostBTN.setVisibility(View.GONE);
        fab_new_youtube_post.setVisibility(View.GONE);
        addBostBTN.hide();
        fab_new_youtube_post.hide();
        isAllFabsVisible = false;
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAllFabsVisible) {
                    addBostBTN.show();
                    fab_new_youtube_post.show();
                    isAllFabsVisible = true;
                } else {
                    addBostBTN.hide();
                    fab_new_youtube_post.hide();
                    isAllFabsVisible = false;
                }
            }
        });
        admin_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AdminDashboard.class));

            }
        });
        // Button launches NewPostActivity
        addBostBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load full screan ad
                /*if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }*/
                startActivity(new Intent(MainActivity.this, NewPostActivity.class));
            }
        });

        // Button launches NewYoutubePostActivity
        fab_new_youtube_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load full screan ad
                /*if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }*/
                startActivity(new Intent(MainActivity.this, NewYouTubePostActivity.class));
            }
        });
        completeProfileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUserInformation();
            }
        });
        subscribeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillingService billingService = new BillingService(MainActivity.this,MainActivity.this);

                billingService.startConnection();
            }
        });


        loadRewardedAd();

        loadad();//to load ads full screen


        playStartupSound();
        TimerInteriterialAd();
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(getUid());

        // Create the adapter that will return a fragment for each section
        /*mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
                    new RecitesName(),
                    new RadioFragment(),
                    new OtherCategoryFragment(),
                    new RecentPosts(),
                    new TopPosts()


            };
            private final String[] mFragmentNames = new String[] {
                    getString(R.string.HolyQuran),
                    getString(R.string.Radio),
                    getString(R.string.OtherCategories),
                    getString(R.string.heading_recent),
                    getString(R.string.heading_my_top_posts),


            };
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }
            @Override
            public int getCount() {
                return mFragments.length;
            }
            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };*/
        // Set up the ViewPager with the sections adapter.
//        mViewPager = findViewById(R.id.container);
        userImage = findViewById(R.id.user_photo);
        crown_photo = findViewById(R.id.crown_photo);
        userName = findViewById(R.id.User_name);
        // Show Hijri date in toolbar
        TextView tvHijriMain = findViewById(R.id.tv_hijri_date_main);
        if (tvHijriMain != null) {
            tvHijriMain.setText(IslamicEventsActivity.getTodayHijriString(this));
        }
        adminPostPhoto = findViewById(R.id.admin_post);
        AdminPostText = findViewById(R.id.post_author);
        AdminLayout = findViewById(R.id.admin_card);

        crown_photo.setVisibility(View.GONE);


        //start user photo animation
        //RotateAnimation anim = new RotateAnimation(0f, 350f, 15f, 15f);
        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(3);
        anim.setDuration(700);

        userImage.startAnimation(anim);

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent down = new Intent(MainActivity.this, UserPage.class);
                startActivity(down);*/
            }
        });


        prize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadRewardedAd();
                showRewardedVideo();

            }
        });

        checkIsAnonymousSignIn();

        Intent downloaderIntent = new Intent(MainActivity.this, MyFirebaseMessagingService.class);
        startService(downloaderIntent);


        subscribeToCloudMessagingTopic();
        checkPushNotificationData();
        saveFCMTokenToDatabase();

        recieveDynamicLinks();



    }
    public class OnSwipeTouchListener implements OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener (Context ctx){
            gestureDetector = new GestureDetector(ctx, new GestureListener());
        }
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    }
                    else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {

                        Log.w("SWIPE_THRESHOLDdsdsa", String.valueOf(diffY));
                        Log.w("SWIPE_THRESHOLDdsdsav", String.valueOf(velocityY));

                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }

    }
    private void doBounceAnimation(View targetView) {
        Interpolator interpolator = new Interpolator() {
            @Override
            public float getInterpolation(float v) {
                return getPowOut(v,7);//Add getPowOut(v,3); for more up animation
            }
        };
        ObjectAnimator animator = ObjectAnimator.ofFloat(targetView, "translationY", 0, -100, 0);
        animator.setInterpolator(interpolator);
        animator.setStartDelay(2000);
        animator.setDuration(2000);
        animator.setRepeatCount(2);
        animator.start();
    }

    private float getPowOut(float elapsedTimeRate, double pow) {
        return (float) ((float) 1 - Math.pow(1 - elapsedTimeRate, pow));
    }
    private float getPowIn(float elapsedTimeRate, double pow) {
        return (float) Math.pow(elapsedTimeRate, pow);
    }
    private void checkIsAnonymousSignIn(){
        if(authService.isAnonymousSignIn()){
            //userImage.setVisibility(View.GONE);
            userName.setText("My Stream");

        }
    }
    private void loadOrdinaryUser(){
        crown_photo.setVisibility(View.GONE);
        // Bottom banner ad disabled for now — re-enable this call (and the AdView's
        // visibility in the layouts) to bring the ad above the tabs back.
        // loadBannerAd();
    }
    private void loadPremiumUser(){
        crown_photo.setVisibility(View.VISIBLE);

    }
    private void loadBannerAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }

        });
    }

    private void saveFCMTokenToDatabase(){
        new SeparateFunctions(getApplicationContext()).getCurrentFCMToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                mUserReference.child("FCMToken").setValue(task.getResult());
//                Log.d(TAG, "onCreate: " +task.getResult());
            }
        });
    }

    private void checkPushNotificationData(){
        StorageUtil storage = new StorageUtil(MainActivity.this);

        if (storage.loadPushNotificationVideo() != null ){
            ArrayList<Post> PushNotificationVideoArrayList = new ArrayList<>();;

            PushNotificationVideoArrayList = storage.loadPushNotificationVideo();
            storage.clearCachePushNotificationVideo();
            Post post = PushNotificationVideoArrayList.get(0);
            Log.d(TAG, "checkPushNotificationData: " +post.title);

            Intent intent = new Intent(MainActivity.this, YoutubePlayerViewActivity.class);
            intent.putExtra(EXTRA_POST_KEY, post.id);
            intent.putExtra(EXTRA_USER_KEY, post.uid);
            intent.putExtra("videoId", post.YouTubeVideoId);
            intent.putExtra("videoTitle", post.title);
            intent.putExtra("videoDescription", post.body);
            startActivity(intent);
        }






    }

    private void subscribeToCloudMessagingTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic("YoutubeVideos")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
//                        Toast.makeText(MainActivity.this, "subscribed", Toast.LENGTH_SHORT).show();
                        /*String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();*/
                    }
                });
    }
    private void setupWindowAnimations() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            /*Slide slide = new Slide();
            slide.setDuration(1000);
            getWindow().setExitTransition(slide);*/

            Fade fade = new Fade();
            fade.setDuration(1000);
            getWindow().setEnterTransition(fade);
        }
    }
    private void startUserInformation(){
        startActivity(new Intent(MainActivity.this, UserInformation.class));
        finish();
    }

    public void recieveDynamicLinks(){
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            Log.d(TAG, "createDynamicLink recieveDynamicLinks: " +deepLink);
                            String myDeepLink = deepLink.toString();
                            Log.d(TAG, "onSuccessrwerew: " + myDeepLink);
                            if (myDeepLink.contains("reciter")){
                                String serverName = myDeepLink.split("reciter/")[1];
                                String Rewayat = "";
                                String RealRecitesName = "";
//                                Log.d(TAG, "createDynamicLink recieveDynamicLinks: " +serverName);

                                ArrayList<AuthorClass> listrecites = new ArrayList<AuthorClass>();

                                LnaguageClass lc = new LnaguageClass();
                                listrecites = lc.AutherList();
                                for (AuthorClass listrecitesitem : listrecites) {
                                    try {
                                        if (listrecitesitem.ServerName.equalsIgnoreCase(serverName)) {
                                            Rewayat = listrecitesitem.Rewayat;
                                            RealRecitesName = listrecitesitem.RealName;

                                            Intent intent = new Intent(MainActivity.this, AyaList.class);
                                            intent.putExtra("RecitesName",serverName);
                                            intent.putExtra("Rewayat",Rewayat);
                                            intent.putExtra("RealRecitesName",RealRecitesName);

                                            if (SettingSaved.titlesTextAnimate){

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                                                }else {
                                                    startActivity(intent);
                                                }
                                            }else{
                                                startActivity(intent);

                                            }
//                                            overridePendingTransition(R.transition.activity_fade, R.transition.activity_slide);

                                            break;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }


                            }else if (myDeepLink.contains("surah")){
                                String all = myDeepLink.split("surah/")[1];
                                String reciterServerName = all.split("/")[0];
                                String surahServerName = all.split("/")[1];
                                String Rewayat = "";
                                String RealRecitesName = "";

                                ArrayList<AuthorClass> listrecites = new ArrayList<AuthorClass>();

                                LnaguageClass lc = new LnaguageClass();
                                listrecites = lc.AutherList();
                                for (AuthorClass listrecitesitem : listrecites) {
                                    try {
                                        if (listrecitesitem.ServerName.equalsIgnoreCase(reciterServerName)) {
                                            Rewayat = listrecitesitem.Rewayat;
                                            RealRecitesName = listrecitesitem.RealName;
                                            Intent intent= new Intent( MainActivity.this,NewQuranPlayer.class);
                                            intent.putExtra("RecitesName",reciterServerName);
                                            intent.putExtra("Rewayat",Rewayat);
                                            intent.putExtra("RealRecitesName",RealRecitesName);
                                            intent.putExtra("RecitesAYA",String.valueOf(Integer.parseInt(surahServerName)-1));
                                            intent.putExtra("IsRadio",false);

                                            startActivity(intent);
                                            break;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }



                            }else if (myDeepLink.contains("radio")){
                                String serverName = myDeepLink.split("radio/")[1];

                                String Rewayat = "";
                                String RealRecitesName = "";
                                int radioIndex = 0;

                                ArrayList<AuthorClass> listrecites = new ArrayList<AuthorClass>();

                                RadioLanguageClass lc = new RadioLanguageClass();
                                listrecites = lc.AutherList();

                                for (int i=0;i< listrecites.size();i++) {
                                    try {
                                        if (listrecites.get(i).ServerName.equalsIgnoreCase(serverName)) {
                                            Rewayat = listrecites.get(i).Rewayat;
                                            RealRecitesName = listrecites.get(i).RealName;
//                                            radioIndex = i;
                                            Intent intent= new Intent( MainActivity.this,NewQuranPlayer.class);
                                            intent.putExtra("RecitesName",serverName);
                                            intent.putExtra("Rewayat",Rewayat);
                                            intent.putExtra("RealRecitesName",Rewayat);
                                            intent.putExtra("RecitesAYA",String.valueOf(i));
                                            intent.putExtra("IsRadio",true);
                                            startActivity(intent);
                                            break;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
//                                Log.d(TAG, "createDynamicLink recieveDynamicLinks: " +radioIndex);


                            }else if (myDeepLink.contains("watch")){
                                String videoId = myDeepLink.split("watch/")[1];

                                Intent intent = new Intent(MainActivity.this, YoutubePlayerViewActivity.class);
//                                intent.putExtra(EXTRA_POST_KEY, postKey);
//                                intent.putExtra(EXTRA_USER_KEY, userKEY);
                                intent.putExtra("videoId", videoId);
//                                intent.putExtra("videoTitle", viewHolder.title);
//                                intent.putExtra("videoDescription", viewHolder.descriptiontxt);
                                startActivity(intent);
                            }

                        }

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.w(TAG, "createDynamicLink recieveDynamicLinks:onFailure", e);
                    }
                });
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Move the task containing the MainActivity to the back of the activity stack, instead of
        // destroying it. Therefore, MainActivity will be shown when the user switches back to the app.
        if (navController != null
                && navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == R.id.nav_home) {
            moveTaskToBack(true);
        } else if (navController == null || !navController.navigateUp()) {
            moveTaskToBack(true);
        }





    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        Log.d(TAG, "onActivityResult: " + requestCode + "--" +resultCode + "---"  + data);
        super.onActivityResult(requestCode, resultCode , data);

    }

    private void openCompleteProfileCard() {
        if (authService.isAnonymousSignIn())
            return;

        Slide transition = new Slide(Gravity.BOTTOM);

        transition.setDuration(5000);
        transition.addTarget(R.id.email_Login_Container);
        TransitionManager.beginDelayedTransition(appbar);
        completeProfileCard.setVisibility( View.VISIBLE );

    }
    private void openSubscriptionCard() {
        Slide transition = new Slide(Gravity.BOTTOM);

        transition.setDuration(5000);
        transition.addTarget(R.id.email_Login_Container);
        TransitionManager.beginDelayedTransition(appbar);
        subscribeCard.setVisibility( View.VISIBLE );

    }


    @Override
    protected void onStart() {
        super.onStart();

        ValueEventListener userListener = new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                try {
                    user = dataSnapshot.getValue(User.class);


                    if (user.CanPost != null && user.CanPost == true){
                        fab_add.setVisibility(View.VISIBLE);
                        admin_fab.setVisibility(View.VISIBLE);
                    }else {
                        fab_add.setVisibility(View.GONE);
                        admin_fab.setVisibility(View.GONE);

                    }
                    if (user.ProfileCompleted != null && user.ProfileCompleted == false){
                        //show hint for user to complete profile
                        openCompleteProfileCard();


                    }else if(user.ProfileCompleted == null){
                        //show hint for user to complete profile
                        openCompleteProfileCard();


                    }else{
                        completeProfileCard.setVisibility( View.GONE );

                    }

                    SettingSaved settingSaved = new SettingSaved(MainActivity.this);
                    settingSaved.LoadData();
                    if (user.isSubscribedPremium != null && user.isSubscribedPremium == false){

                        isSubscribedPremium = false;
                        SettingSaved.isSubscribedPremium = false;
                        loadOrdinaryUser();
//                        openSubscriptionCard();


                    }else if(user.isSubscribedPremium == null){

                        isSubscribedPremium = false;
                        SettingSaved.isSubscribedPremium = false;
                        loadOrdinaryUser();
//                        openSubscriptionCard();


                    }else{
                        SettingSaved.isSubscribedPremium = true;
                        isSubscribedPremium = true;
                        subscribeCard.setVisibility( View.GONE );
                        loadPremiumUser();

                    }


                    settingSaved.SaveData();

                    String ProfileUrl = user.photourl;


                    try {
                        if (ProfileUrl != null){

                            Glide.with(getApplicationContext()).load(ProfileUrl).into(userImage);
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "onDataChange: " + e);

                        e.printStackTrace();
                    }
                    if (user.firstname != null){

                        userName.setText(user.firstname+" "+user.lastname);
                    }

                    if (user.avata==null){

                        DatabaseReference globalPostRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
                        onStarClicked(globalPostRef);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                //  Toast.makeText(MainActivity.this, "Failed to load post.",Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mUserReference.addValueEventListener(userListener);


    }

    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    protected void onResume() {
        super.onResume();
        BillingService billingService = new BillingService(MainActivity.this,MainActivity.this);

        billingService.FetchingPurchases();
    }

    private void playStartupSound(){
        if (SettingSaved.StartupSound == 1){
            if(SettingSaved.playsound==0){
                //load sound
                final AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                final int originalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                android.media.MediaPlayer mediaPlayer = android.media.MediaPlayer.create(MainActivity.this, R.raw.sound);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                    }
                });
                SettingSaved.playsound=1;
            }
        }

    }
    private void loadRewardedAd() {
        if (rewardedAd == null) {
            isLoading = true;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(
                    this,
                    getString(R.string.REWARD_VIDEO_ADMOB_AD_UNIT_ID),
                    adRequest,
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(TAG, loadAdError.getMessage());
                            rewardedAd = null;
                            MainActivity.this.isLoading = false;
//                            Toast.makeText(MainActivity.this, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            MainActivity.this.rewardedAd = rewardedAd;
                            Log.d(TAG, "onAdLoaded");
                            MainActivity.this.isLoading = false;
                            try {
                                Glide.with(getApplicationContext()).load(R.drawable.newgift).into(prize);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            ViewGroup.LayoutParams params = prize.getLayoutParams();
                            params.height = -1;
                            params.width = 150;
                            prize.setLayoutParams(params);
//                            Toast.makeText(MainActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    private void showRewardedVideo() {

        if (rewardedAd == null) {
            Log.d("TAG", "The rewarded ad wasn't ready yet.");
            return;
        }
//        showVideoButton.setVisibility(View.INVISIBLE);

        rewardedAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d(TAG, "onAdShowedFullScreenContent");
//                        Toast.makeText(MainActivity.this, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.d(TAG, "onAdFailedToShowFullScreenContent");
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;
//                        Toast.makeText(MainActivity.this, "onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;
                        Log.d(TAG, "onAdDismissedFullScreenContent");
//                        Toast.makeText(MainActivity.this, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT) .show();
                        // Preload the next rewarded ad.
                        MainActivity.this.loadRewardedAd();
                    }
                });
        Activity activityContext = MainActivity.this;
        rewardedAd.show(
                activityContext,
                new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d("TAG", "The user earned the reward.");
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();
                    }
                });
    }

    // [START post_stars_transaction]
    private void onStarClicked(DatabaseReference userRef) {
        userRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                User p = mutableData.getValue(User.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }else {
                    User newUser = new User();
                    newUser.avata = StaticConfig.STR_DEFAULT_BASE64;
                    p.avata = StaticConfig.STR_DEFAULT_BASE64;
                    //myAccount.avata = p.avata;
                }



                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed


                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }
    public void option(View view) {

        Intent newpage = new Intent(MainActivity.this, Settings.class);
        if (SettingSaved.titlesTextAnimate){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startActivity(newpage, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
            }else {

                startActivity(newpage);
            }
        }else{
            startActivity(newpage);

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
    public void TimerInteriterialAd(){

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            public void run() {
                Log.i("hello", "world");
                runOnUiThread(new Runnable() {
                    public void run() {

                        /*mInterstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdLoaded() {
                                if (SettingSaved.isfullscreenadshow==false){
                                    //Toast.makeText(MainActivity.this, "ad load", Toast.LENGTH_SHORT).show();
                                    mInterstitialAd.show();
                                    SettingSaved.isfullscreenadshow= true;
                                    SettingSaved settingSaved=new SettingSaved(getApplicationContext());
                                    settingSaved.SaveData();
                                    settingSaved.LoadData();
                                }

                                //onBackPressed();
                                // Code to be executed when an ad finishes loading.
                            }

                            @Override
                            public void onAdFailedToLoad(int errorCode) {
                                loadad();
                                // Code to be executed when an ad request fails.
                            }

                            @Override
                            public void onAdOpened() {
                               // Toast.makeText(MainActivity.this, "opened", Toast.LENGTH_SHORT).show();

                                // Code to be executed when the ad is displayed.
                            }

                            @Override
                            public void onAdLeftApplication() {
                                // Code to be executed when the user has left the app.
                            }

                            @Override
                            public void onAdClosed() {
                                //Toast.makeText(MainActivity.this, "ad closed", Toast.LENGTH_SHORT).show();

                                SettingSaved.isfullscreenadshow=false;
                                SettingSaved settingSaved=new SettingSaved(getApplicationContext());
                                settingSaved.SaveData();
                                settingSaved.LoadData();
                                loadad();
                                // Code to be executed when when the interstitial ad is closed.
                            }




                        });*/


                           // loadad();
                        }


                });

            }
        }, 20, 20, TimeUnit.MINUTES);
    }
    private void fetchWelcome() {
        //mWelcomeTextView.setText(mFirebaseRemoteConfig.getString(LOADING_PHRASE_CONFIG_KEY));

        long cacheExpiration = 3600; // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        /*if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }*/

        // [START fetch_config_with_callback]
        // cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
        // will use fetch data from the Remote Config service, rather than cached parameter values,
        // if cached parameter values are more than cacheExpiration seconds old.
        // See Best Practices in the README for more information.
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(RecitesName.this, "Fetch Succeeded", Toast.LENGTH_SHORT).show();

                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
//                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            //Toast.makeText(RecitesName.this, "Fetch Failed", Toast.LENGTH_SHORT).show();
                        }
                        displayWelcomeMessage();
                    }
                });
        // [END fetch_config_with_callback]
    }
    // [START display_welcome_message]
    private void displayWelcomeMessage() {

        final String photoUrl = mFirebaseRemoteConfig.getString(admin_post_photo_url);
        final String PostText = mFirebaseRemoteConfig.getString(admin_post_text);
        final String TargetUrl = mFirebaseRemoteConfig.getString(admin_post_targrt_url);
        Boolean IsPostShow = mFirebaseRemoteConfig.getBoolean(admin_post_show);

        if (IsPostShow==true && AdminLayout != null) {


            AdminLayout.setVisibility(View.VISIBLE);
            try {
                if (adminPostPhoto != null) {
                    Glide.with(getApplicationContext()).load(photoUrl).into(adminPostPhoto);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (AdminPostText != null) {
                AdminPostText.setText(PostText);
            }
            AdminLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TargetUrl));
                    startActivity(browserIntent);
                }
            });





            //mWelcomeTextView.setAllCaps(true);
        } else {

            if (AdminLayout != null) AdminLayout.setVisibility(View.GONE);
            //mWelcomeTextView.setAllCaps(false);
        }


    }

    /** Trigger the rewarded ad from a tab header. */
    public void showRewardedAd() {
        loadRewardedAd();
        showRewardedVideo();
    }

    /**
     * Switch to a bottom-nav destination the same way tapping the tab does, so the
     * bottom-nav selection and the NavigationUI back stack stay in sync. Used by the
     * Home hub's shortcuts (e.g. the radio card / quick actions) — navigating the
     * NavController directly would desync the tab selection and break later tab taps.
     */
    public void selectBottomTab(int itemId) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(itemId);
        }
    }

    /**
     * Renders Design-A's literal emoji as the dock icons (🏠 📖 📻 🕋 ⋯). Icon
     * tint is disabled so the full-colour emoji show as in A; the active state is
     * carried by the cream pill + gold label. Color-emoji ignore the paint colour;
     * the monochrome ⋯ glyph uses it.
     */
    private void applyDockEmojiIcons() {
        if (bottomNavigationView == null) return;
        bottomNavigationView.setItemIconTintList(null);
        android.view.Menu menu = bottomNavigationView.getMenu();
        setDockIcon(menu, R.id.nav_home, "🏠");
        setDockIcon(menu, R.id.nav_quran, "🎧");   // القرآن — recitations (listening)
        setDockIcon(menu, R.id.nav_radio, "📻");
        setDockIcon(menu, R.id.nav_mushaf, "📖");  // المصحف — mushaf (reading)
        setDockIcon(menu, R.id.nav_more, "⋯");
    }

    private void setDockIcon(android.view.Menu menu, int itemId, String glyph) {
        android.view.MenuItem item = menu.findItem(itemId);
        if (item != null) item.setIcon(emojiIcon(glyph));
    }

    /** Draws a glyph (emoji or text symbol) into a square BitmapDrawable for the dock. */
    private android.graphics.drawable.Drawable emojiIcon(String glyph) {
        float density = getResources().getDisplayMetrics().density;
        int size = (int) (40 * density);
        android.graphics.Bitmap bmp =
                android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bmp);
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(android.graphics.Paint.Align.CENTER);
        paint.setColor(0xFF8A8A8A); // used by the ⋯ text glyph; colour-emoji ignore it
        paint.setTextSize(size * 0.78f);
        android.graphics.Paint.FontMetrics fm = paint.getFontMetrics();
        float y = size / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(glyph, size / 2f, y, paint);
        return new android.graphics.drawable.BitmapDrawable(getResources(), bmp);
    }

    public void message(View view) {

        Intent intent = new Intent(this, Messenger.class);
        //intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
        //intent.putExtra(PostDetailActivity.EXTRA_USER_KEY, userKEY);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance8Ref = null;
    }
}
