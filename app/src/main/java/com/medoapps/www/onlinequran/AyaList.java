package com.medoapps.www.onlinequran;

import static android.os.Build.VERSION.SDK_INT;
import static com.medoapps.www.onlinequran.util.Permissions.REQUEST_CODE_ASK_STORAGE_PERMISSIONS;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import androidx.core.content.ContextCompat;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat.Builder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.medoapps.www.onlinequran.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.medoapps.www.onlinequran.models.Report;
import com.medoapps.www.onlinequran.models.ReportType;
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.service.ReportService;
import com.medoapps.www.onlinequran.util.MetaDataEditor;
import com.medoapps.www.onlinequran.util.MetaDataEditorHashimUpdate;
import com.medoapps.www.onlinequran.util.Permissions;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AyaList extends AppCompatActivity {

    private static final String TAG = "AyaList";
//    //private InterstitialAd mInterstitialAd;
    private AdView mAdView;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    RecyclerView listAya;
    public static ArrayList<AuthorClass> listrecitesAya = new ArrayList<AuthorClass>();
    public static ArrayList<AuthorClass> listDownloadAya = new ArrayList<AuthorClass>();
    private ProgressDialog mProgressDialog;
    LinearLayout LayoutLoading;
    ProgressBar progressBar;
    static  String RecitesName="";
    static  String Rewayat="";
    static  String RealRecitesName="";
    String RecitesAYA="";
//for notification
    public NotificationManager mNotifyManager;
    public Builder mBuilder;
    int id = 1;
    static AyaList instance2;
    static  DownloadFileAsync instance3 ;
    DownloadFileAsync dfa = new DownloadFileAsync();

    String CHANNEL_ID = "HASHIM_CHANNEL-ID_01";

    private ImageButton backBTN;
    private CardView downloadAllBTN;
    SearchView searchView;
    TextView ActivityTitle;
    TextView ActivityReciter;
    private List<Object> recyclerViewItems = new ArrayList<>();
    public static final int ITEMS_PER_AD = 20;

    private boolean serviceBound;
    private RewardedAd rewardedAd;
    boolean isLoading;

    private SeparateFunctions separateFunctions;

    String surahName ;
    View AyaNameView;
    View AyaImage;
    private boolean isDownloading;
    public static final String Broadcast_LoadAya = "com.medoapps.www.onlinequran.LoadAya";
    private String tempImgUrlForPermissionWait;
    private String ServerNameForPermissionWait;
    private boolean downloadAllRequest;

    private ProgressBar loader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setAnimation();
        setContentView(R.layout.activity_aya_list);


        /*getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.your_LAYOUT);*/
        instance2=this;
        SettingSaved settingSaved=new SettingSaved(AyaList.this);
        settingSaved.LoadData();
        new SeparateFunctions(AyaList.this).getAppSpecificDownloadStorageDir(AyaList.this,AyaList.this);
        separateFunctions = new SeparateFunctions(this);
        mAdView = findViewById(R.id.adView);
        mAdView.setVisibility(View.GONE);
        /*AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }

        });*/

        //get Recites
        Bundle b=getIntent().getExtras();
        RecitesName=b.getString("RecitesName");
        Rewayat=b.getString("Rewayat");
        RealRecitesName=b.getString("RealRecitesName");


//        ActivityTitle = (TextView) findViewById(R.id.ActivityTitle);
        ActivityReciter = (TextView) findViewById(R.id.ActivityReciter);
        loader = (ProgressBar) findViewById(R.id.loader);
        LnaguageClass lc = new LnaguageClass(AyaList.this,AyaList.this);

        ActivityReciter = lc.SetTextFont(ActivityReciter,"");
        ActivityReciter.setText(RealRecitesName);
        ActivityReciter.setSelected(true);

        backBTN = (ImageButton) findViewById(R.id.backBTN);
        downloadAllBTN = (CardView) findViewById(R.id.downloadAllBTN);
        downloadAllBTN.setEnabled(false);
        downloadAllBTN.setBackgroundColor(getResources().getColor(R.color.grey_300));
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rateApp();
//                finish();
            }
        });

        searchManager();
        loadad();//to load ads full screen

        //load interstial ad by atimer
        AdmobInterstitial.loadInterstitial(this);

        //load full screan ad
        /*/*if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }*/
        //load banner ad



        //save recite name to play it out
        /*SettingSaved.FinalRecite=RecitesName;
        SettingSaved settingSaved=new SettingSaved(getApplicationContext());
        settingSaved.SaveData();*/
        listAya =(RecyclerView) findViewById ( R.id.listView) ;

        /*//get list of recites
        listrecitesAya.clear();
        LnaguageClass hh = new LnaguageClass(AyaList.this,AyaList.this);
        listrecitesAya = hh.GuranAya(RecitesName,Rewayat);

        for (int i = 0; i < listrecitesAya.size(); ++i) {

            AuthorClass temp = listrecitesAya.get(i);

            recyclerViewItems.add(temp);
        }

        addBannerAds(recyclerViewItems);
        loadBannerAds(recyclerViewItems);*/

        //LoadAya();
        asyncLoadAya();
        listAya.setHasFixedSize(true);
        listAya.setLayoutManager(new LinearLayoutManager(AyaList.this));
        listAya.setAdapter(new VivzAdapter(recyclerViewItems));
        LayoutLoading=(LinearLayout)findViewById(R.id.LayoutLoading);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        LayoutLoading.setVisibility(View.GONE);
//        getAppSpecificAlbumStorageDir(this,"My Stream2");

        downloadAllBTN.setEnabled(true);
        downloadAllBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startDownloadAll();

//                separateFunctions.showNewCustomDialog(getString(R.string.DownloadAllTitle),getString(R.string.DownloadAllMessage),getString(android.R.string.yes),getString(android.R.string.no),showRewardedVideoRunnable,android.R.drawable.ic_dialog_info);

            }
        });
        loadRewardedAd();

    }
    private void asyncLoadAya(){
        AsyncLoadAyaRunner runner = new AsyncLoadAyaRunner();
        runner.execute();
    }
    private class AsyncLoadAyaRunner extends AsyncTask<String, String, String> {

        private String resp;
        ProgressDialog progressDialog;
        LnaguageClass lc = new LnaguageClass(AyaList.this,AyaList.this);
        boolean isHasOfflineSurah;
        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()

            RecyclerView list =(RecyclerView) findViewById ( R.id.listView) ;

            //get list of recites
            listrecitesAya = lc.GuranAya(RecitesName,Rewayat);

            recyclerViewItems.clear();
            for (int i = 0; i < listrecitesAya.size(); ++i) {

                AuthorClass temp = listrecitesAya.get(i);
                if (temp.StateName.equals(LnaguageClass.avalible()))
                    isHasOfflineSurah = true;

                recyclerViewItems.add(temp);
            }


            return "";
        }


        @Override
        protected void onPostExecute(String result) {

            addBannerAds(recyclerViewItems);
            loadBannerAds(recyclerViewItems);

            if (isHasOfflineSurah){
                Permissions permissions = new Permissions(AyaList.this,AyaList.this);
                if (!permissions.checkStoragePermissionWithoutAsk())
                    separateFunctions.showNewCustomDialog(getString(R.string.StoragePermissionTitle),getString(R.string.AskStaragePermissionMessage),getString(R.string.getPermission),getString(android.R.string.cancel),showStoragePermission,android.R.drawable.ic_dialog_info);

            }


            listAya.setAdapter(new VivzAdapter(recyclerViewItems));
            loader.setVisibility(View.GONE);
        }


        @Override
        protected void onPreExecute() {
            /*progressDialog = ProgressDialog.show(context,
                    "ProgressDialog",
                    "Wait for  seconds");*/
        }


        @Override
        protected void onProgressUpdate(String... text) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_ASK_STORAGE_PERMISSIONS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (downloadAllRequest){
                        startDownloadAll();
                    }else{

                        startDownload(tempImgUrlForPermissionWait,ServerNameForPermissionWait );
                    }

                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    // Explain to the user that the feature is unavailable

                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
//        Transition slide = TransitionInflater.from(this).inflateTransition(R.transition.activity_slide);
        /*Fade fade = (Fade) TransitionInflater.from(this).inflateTransition(R.transition.activity_fade);

        getWindow().setExitTransition(fade);*/

        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(fade);
    }
    public void setAnimation()
    {
        if(Build.VERSION.SDK_INT>20) {
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.LEFT);
            slide.setDuration(3000);
            slide.setInterpolator(new AccelerateDecelerateInterpolator());
            getWindow().setExitTransition(slide);
            getWindow().setEnterTransition(slide);
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
                            AyaList.this.isLoading = false;
//                            Toast.makeText(AyaList.this, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
//                            startDownloadAll();
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            AyaList.this.rewardedAd = rewardedAd;
                            Log.d(TAG, "onAdLoaded");
                            AyaList.this.isLoading = false;
                            downloadAllBTN.setEnabled(true);
                            downloadAllBTN.setBackgroundColor(getResources().getColor(R.color.white));


                            /*Glide.with(AyaList.this).load(R.drawable.newgift).into(prize);

                            ViewGroup.LayoutParams params = prize.getLayoutParams();
                            params.height = -1;
                            params.width = 150;
                            prize.setLayoutParams(params);*/
                        }
                    });
        }
    }
    Runnable showRewardedVideoRunnable = new Runnable() {
        @Override
        public void run() {
            showRewardedVideo();
        }
    };
    Runnable showStoragePermission  = new Runnable() {
        @Override
        public void run() {
            Permissions permissions = new Permissions(AyaList.this,AyaList.this);
            permissions.checkStoragePermission();
        }
    };

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
//                        Toast.makeText(AyaList.this, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.d(TAG, "onAdFailedToShowFullScreenContent");
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;
                        startDownloadAll();

//                        Toast.makeText(AyaList.this, "onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;
                        Log.d(TAG, "onAdDismissedFullScreenContent");
//                        Toast.makeText(AyaList.this, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT) .show();
                        // Preload the next rewarded ad.
                        AyaList.this.loadRewardedAd();
                    }
                });
        Activity activityContext = AyaList.this;
        rewardedAd.show(
                activityContext,
                new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d("TAG", "The user earned the reward.");
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();
                        startDownloadAll();
                    }
                });
    }
    private void showUserDownloadAllDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.DownloadAllTitle);
        alertDialog.setMessage(R.string.DownloadAllMessage);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                showRewardedVideo();


            }
        });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        alertDialog.show();
    }
    private  void startDownloadAll(){
        downloadAllRequest = true;
        Permissions permissions = new Permissions(AyaList.this,AyaList.this);

        if(!permissions.checkStoragePermission())
            return;

        listDownloadAya.clear();
        //get list of recites
        listrecitesAya.clear();
        LnaguageClass lc = new LnaguageClass(AyaList.this,AyaList.this);
        listrecitesAya = lc.GuranAya(RecitesName,Rewayat);
        for (int i = 0; i < listrecitesAya.size(); ++i) {

            AuthorClass temp = listrecitesAya.get(i);

            if (temp.ImgUrl.contains("http")){

                listDownloadAya.add(temp);
//                        Log.d("fdsfds", String.valueOf(listDownloadAya));

            }
//                    recyclerViewItems.add(temp);
        }
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.clearCacheDownloadslist();

        storage.storeDownloadlist(listDownloadAya);
        storage.storeDownloadIndex(0);
        storage.storeDownloadRecitesName(RecitesName);
        storage.storeDownloadRealRecitesName(RealRecitesName);

        Intent downloaderIntent = new Intent(AyaList.this, DownloadService.class);
        startService(downloaderIntent);
        bindService(downloaderIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        register_loadAyaReceiver();

//                bindService(downloaderIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    public Void openFullStoragePermissionIntent(){

        try {
            Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
            startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
        } catch (Exception ex) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
        }
        return null;
    }
    Runnable openFullStoragePermissionIntentRunnable = new Runnable() {
        public void run() {
            openFullStoragePermissionIntent();
        }
    };


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
//            player = binder.getService();

            serviceBound = true;


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Toast.makeText(getApplicationContext(), "onServiceDisconnected", Toast.LENGTH_SHORT).show();
            serviceBound = false;
        }
    };

    private BroadcastReceiver updateProgressBarReceiver  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            LoadAya();


        }
    };

    private void register_loadAyaReceiver() {
        //Register playNewMedia receiver


        try {
            unregisterReceiver(updateProgressBarReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        IntentFilter filter = new IntentFilter(com.medoapps.www.onlinequran.AyaList.Broadcast_LoadAya);
        try {
            ContextCompat.registerReceiver(this, updateProgressBarReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /**
     * Adds banner ads to the items list.
     * @param recyclerViewItems
     */
    private void addBannerAds(List<Object> recyclerViewItems) {
        if (SettingSaved.isSubscribedPremium)
            return;
        // Loop through the items array and place a new banner ad in every ith position in
        // the items List.
        for (int i = 0; i <= recyclerViewItems.size(); i += ITEMS_PER_AD) {
            try {
                final AdView adView = new AdView(this);
                adView.setAdSize(AdSize.BANNER);
                adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
                recyclerViewItems.add(i, adView);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Sets up and loads the banner ads.
     * @param recyclerViewItems
     */
    private void loadBannerAds(List<Object> recyclerViewItems) {
        if (SettingSaved.isSubscribedPremium)
            return;
        // Load the first banner ad in the items list (subsequent ads will be loaded automatically
        // in sequence).
        loadBannerAd(0,recyclerViewItems);
    }

    /**
     * Loads the banner ads in the items list.
     */
    private void loadBannerAd(final int index, List<Object> recyclerViewItems) {

        if (index >= recyclerViewItems.size()) {
            return;
        }

        Object item = recyclerViewItems.get(index);
        if (!(item instanceof AdView)) {
            throw new ClassCastException("Expected item at index " + index + " to be a banner ad"
                    + " ad.");
        }

        final AdView adView = (AdView) item;

        // Set an AdListener on the AdView to wait for the previous banner ad
        // to finish loading before loading the next ad in the items list.
        adView.setAdListener(
                new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        // The previous banner ad loaded successfully, call this method again to
                        // load the next ad in the items list.
                        loadBannerAd(index + ITEMS_PER_AD, recyclerViewItems);
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        // The previous banner ad failed to load. Call this method again to load
                        // the next ad in the items list.
                        String error =
                                String.format(
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                        Log.e(
                                "MainActivity",
                                "The previous banner ad failed to load with error: "
                                        + error
                                        + ". Attempting to"
                                        + " load the next banner ad in the items list.");
                        loadBannerAd(index + ITEMS_PER_AD, recyclerViewItems);
                    }
                });

        // Load the banner ad.
        adView.loadAd(new AdRequest.Builder().build());
    }
    private void searchManager(){
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView = (SearchView) findViewById(R.id.search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
//                ActivityTitle.setVisibility(View.VISIBLE);
                ActivityReciter.setVisibility(View.VISIBLE);

                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ActivityTitle.setVisibility(View.GONE);
                ActivityReciter.setVisibility(View.GONE);

            }
        });
        //final Context co=this;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                /*ArrayList<AuthorClass> listrecitestemp = new ArrayList<AuthorClass>();

                for (AuthorClass listrecitesitem : listrecitesAya) {
                    if (listrecitesitem.RealName.contains(newText)) {
                        listrecitestemp.add(listrecitesitem);

                    }
                }
                listAya .setAdapter(new VivzAdapter(listrecitestemp));*/

                List<Object> recyclerViewItemstemp = new ArrayList<>();
                for (Object listrecitesitem : recyclerViewItems) {
                    if (listrecitesitem instanceof AuthorClass){
                        AuthorClass kk = (AuthorClass) listrecitesitem;

                        if (kk.RealName.contains(newText)) {
                            recyclerViewItemstemp.add(listrecitesitem);
                        }
                    }
                }

                addBannerAds(recyclerViewItemstemp);
                loadBannerAds(recyclerViewItemstemp);
                listAya .setAdapter(new VivzAdapter(recyclerViewItemstemp));
                return false;
            }
        });
    }
    private void DisplayAya(){
        Intent intent= new Intent( this,NewQuranPlayer.class);
        intent.putExtra("RecitesName",RecitesName);
        intent.putExtra("Rewayat",Rewayat);
        intent.putExtra("RealRecitesName",RealRecitesName);
        intent.putExtra("RecitesAYA",RecitesAYA);
        intent.putExtra("IsRadio",false);


        View ayaNameView = AyaNameView;
        View reciteNameView = ActivityReciter;
        View ayaImage = AyaImage;
        String transitionNameReciter = "shared_reciter";
        String transitionNameSurah = "shared_surah";
        String transitionNameSurahImage = "shared_surah_image";


        if (SettingSaved.titlesTextAnimate){
            if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                if (AyaNameView != null && AyaImage != null){
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                            Pair.create(ayaNameView, transitionNameSurah),
                            Pair.create(ayaImage, transitionNameSurahImage)

                    );
//            ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(AyaList.this, ayaNameView, transitionNameSurah);
                    startActivity(intent, options.toBundle());
                }else{
                    startActivity(intent);
                }

            }else{
                startActivity(intent);
            }
        }else{
            startActivity(intent);

        }



        createReport(null);
    }

    private void createReport(String shareUrl){
        ReportType reportType;
        if (shareUrl!= null){
             reportType = ReportType.ShareSurah;
        }else{
             reportType = ReportType.OpenSurah;

        }
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                User user = dataSnapshot.getValue(User.class);

                Long Date = new SeparateFunctions(AyaList.this).getTimeStamp();
                Report report = new Report("",user.id,user.username,user.photourl, reportType,RecitesAYA,surahName,shareUrl,RecitesName,RealRecitesName,"",Date,"");
                ReportService reportService = new ReportService(AyaList.this,AyaList.this);
                reportService.createSurahReport(report);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser()!= null){

            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }else {
            return "guest_mode";
        }
    }
    public void LoadAya(){
        RecyclerView list =(RecyclerView) findViewById ( R.id.listView) ;

//get list of recites
        LnaguageClass lc = new LnaguageClass(AyaList.this,AyaList.this);
        listrecitesAya = lc.GuranAya(RecitesName,Rewayat);

        recyclerViewItems.clear();
        Boolean isHasOfflineSurah = false;
        for (int i = 0; i < listrecitesAya.size(); ++i) {

            AuthorClass temp = listrecitesAya.get(i);
            if (temp.StateName.equals(LnaguageClass.avalible()))
                isHasOfflineSurah = true;

            recyclerViewItems.add(temp);
        }

        addBannerAds(recyclerViewItems);
        loadBannerAds(recyclerViewItems);

        if (isHasOfflineSurah){
            Permissions permissions = new Permissions(AyaList.this,AyaList.this);
            if (!permissions.checkStoragePermissionWithoutAsk())
                separateFunctions.showNewCustomDialog(getString(R.string.StoragePermissionTitle),getString(R.string.AskStaragePermissionMessage),getString(R.string.getPermission),getString(android.R.string.cancel),showStoragePermission,android.R.drawable.ic_dialog_info);

        }


        listAya.setAdapter(new VivzAdapter(recyclerViewItems));


    }


//    SearchView searchView;
    Menu myMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_aya_list, menu);
        myMenu=menu;
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //final Context co=this;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<AuthorClass> listrecitestemp = new ArrayList<AuthorClass>();
                for (AuthorClass listrecitesitem : listrecitesAya) {
                    if (listrecitesitem.RealName.contains(newText)) {
                        listrecitestemp.add(listrecitesitem);

                    }
                }
                listAya .setAdapter(new VivzAdapter(listrecitestemp));
                return false;
            }
        });
        //   searchView.setOnCloseListener(this);
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
           /* /*if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                Log.d("TAG", "The interstitial wasn't loaded yet.");
            }*/
            rateApp();

        }

        return super.onOptionsItemSelected(item);
    }

    Runnable goToMarketRunnable = new Runnable() {
        @Override
        public void run() {
            goToMarket();
        }
    };
    Runnable finishActivityRunnable = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    private void goToMarket(){
        SeparateFunctions separateFunctions = new SeparateFunctions(AyaList.this);
        separateFunctions.openRationgIntent();
        finish();
    }
    private void rateApp(){
        // rate app
        if( ISDonwloading!=true) //it he isnot donlaidng know
            if(SettingSaved.IsRated==0) {
                separateFunctions.showNewCustomWithFinishDialog(getString(R.string.RateApp),getString(R.string.rateq),getString(R.string.rateBtn),getString(android.R.string.no),goToMarketRunnable,finishActivityRunnable,android.R.drawable.ic_dialog_info);
            }
            else{
                finish();
            }
    }

    final private int APP_STORAGE_ACCESS_REQUEST_CODE = 126;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == APP_STORAGE_ACCESS_REQUEST_CODE)
        {
            if (Environment.isExternalStorageManager())
            {
                // Permission granted. Now resume your workflow.
            }else{
                Toast.makeText(getApplicationContext(), "permission denied", Toast.LENGTH_LONG).show();

            }

        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    /// file downlaod
    public void startDownload( String ImgUrl,String ServerName ) {


        listDownloadAya.clear();
        //get list of recites
        listrecitesAya.clear();
        LnaguageClass lc = new LnaguageClass(AyaList.this,AyaList.this);
        listrecitesAya = lc.GuranAya(RecitesName,Rewayat);
        for (int i = 0; i < listrecitesAya.size(); ++i) {

            AuthorClass temp = listrecitesAya.get(i);

            if (temp.ImgUrl.equalsIgnoreCase(ImgUrl)){

                listDownloadAya.add(temp);
//                        Log.d("fdsfds", String.valueOf(listDownloadAya));

            }
//                    recyclerViewItems.add(temp);
        }
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.clearCacheDownloadslist();

        storage.storeDownloadlist(listDownloadAya);
        storage.storeDownloadIndex(0);
        storage.storeDownloadRecitesName(RecitesName);
        storage.storeDownloadRealRecitesName(RealRecitesName);


        Handler mHandler=new Handler();
        mHandler.post(new Runnable(){
                          public void run(){
                              Intent downloaderIntent = new Intent(AyaList.this, DownloadService.class);
                              startService(downloaderIntent);
                              bindService(downloaderIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                              register_loadAyaReceiver();
                          }

                      });
        /*Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                Intent downloaderIntent = new Intent(AyaList.this, DownloadService.class);
                startService(downloaderIntent);
            }
        });
        th.start();*/






        /*createNotificationChannel();
        String GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL";

        // The id of the group.
        String groupId = "my_group_01";
// The user-visible name of the group.
        CharSequence groupName = "hashim_notification";
        RecitesAYA=ServerName;
        String url = ImgUrl ;
        //here i can start notification
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        Intent cancel = new Intent(getApplicationContext(),NotificationService.class);
        cancel.setAction(NotificationService.ACTION8);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(AyaList.this, 0, cancel, PendingIntent.FLAG_IMMUTABLE);
        mBuilder = new Builder(AyaList.this,CHANNEL_ID);
        mBuilder.setContentTitle(getString(R.string.Download_aya))
                .setContentText(getString(R.string.download_progress))
                .setSmallIcon(getNotificationIcon())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(new NotificationCompat.Action(R.drawable.ic_cancel_white_18dp,getString(R.string.cancel),pendingIntent));

        mNotifyManager.notify(id, mBuilder.build());

        try {
            dfa.execute(url);
        } catch (Exception e) {
            if (isDownloading == false){

                dfa.canceldownload();
                dfa = null;
                dfa = new DownloadFileAsync();
                dfa.execute(url);

            }
            e.printStackTrace();
        }
*/


    }
/*
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading file.."+RecitesAYA);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"cancel",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        LoadAya();
                        dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
                        LayoutLoading.setVisibility(View.GONE);
                        ISDonwloading=false;
                    }
                });
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }
    */





    public  boolean ISDonwloading=false;
    InputStream  input;
    OutputStream output;
    FileOutputStream fos;
    BufferedInputStream fis;
    BufferedOutputStream out;
    int contentLength;

    int count;


    class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isDownloading = true;
            LayoutLoading.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            progressBar.setMax(100);
            //ISDonwloading=true;

            // Displays the progress bar for the first time for the notification.
            mBuilder.setProgress(100, 0, false);
            mNotifyManager.notify(id, mBuilder.build());

             showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        public String doInBackground(String... aurl) {

            instance3=this;

            if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT){
                try {

                    URL url = new URL(aurl[0]);
                    URLConnection conexion = url.openConnection();
                    conexion.connect();
                    contentLength = conexion.getContentLength();

                    int lenghtOfFile = conexion.getContentLength();

                    ContentResolver resolver = getApplicationContext()
                            .getContentResolver();

                    Uri audioCollection = new SeparateFunctions(AyaList.this).getAudioCollection();
                    ContentValues newSongDetails = new ContentValues();
                    newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME,
                            "AhmedHashim_"+RecitesName + RecitesAYA + ".mp3");
                    newSongDetails.put(MediaStore.Audio.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_MUSIC+"/MyStream");
//                    newSongDetails.put(MediaStore.Audio.Media.IS_PENDING, 1);
                    newSongDetails.put(MediaStore.Audio.Media._ID, RecitesName + RecitesAYA);
                    newSongDetails.put(MediaStore.Audio.Media.ALBUM_ID, "AhmedHashim");
                    newSongDetails.put(MediaStore.Audio.Media.ARTIST,"AhmedHashim");
                    newSongDetails.put(MediaStore.Audio.Media.GENRE,"AhmedHashim");
                    newSongDetails.put(MediaStore.Audio.Media.ALBUM,"AhmedHashim");
                    newSongDetails.put(MediaStore.Audio.Media.AUTHOR,"AhmedHashim");
                    newSongDetails.put(MediaStore.Audio.Media.TITLE,"AhmedHashim");

                    Uri myFavoriteSongUri = resolver.insert(audioCollection, newSongDetails);

                    File SDPath =  new SeparateFunctions(AyaList.this).getAppSpecificDownloadStorageDir(AyaList.this,AyaList.this);
                    if(!SDPath.exists()) {
                        SDPath.mkdirs();
                    }

                    byte data[] = new byte[1024];

                    long total = 0;


                    try (ParcelFileDescriptor pfd =
                                 resolver.openFileDescriptor(myFavoriteSongUri, "rw", null)) {
                        fis =  new BufferedInputStream(url.openStream());

//                        fos = new FileOutputStream(pfd.getFileDescriptor()); // for change media tag
//                        FileOutputStream out = (FileOutputStream) resolver.openOutputStream(myFavoriteSongUri);


                        /*FileOutputStream out = (FileOutputStream) resolver.openOutputStream(myFavoriteSongUri);
                        FileChannel outChannel = out.getChannel();
                        ReadableByteChannel inChannel = Channels.newChannel(fis);
                        outChannel.transferFrom(inChannel, 0, Long.MAX_VALUE);
                        out.close();*/

                        output = resolver.openOutputStream(myFavoriteSongUri);

                        while ((count = fis.read(data)) != -1) {

                            total += count;
                            try {
                                publishProgress(""+(int)((total*100)/lenghtOfFile));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            output.write(data, 0, count);

                        }
                        MetaDataEditorHashimUpdate metaDataEditor = new MetaDataEditorHashimUpdate(AyaList.this);
                        metaDataEditor.changeMetaData(SDPath + RecitesName + RecitesAYA + ".mp3");


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    output.flush();
                    output.close();
                    fis.close();

                } catch (Exception e) {

                    Log.d(TAG, "doInBackgroundfdsf: Exception" + e);
                }
            }else{

                try {
                    Log.d(TAG, "doInBackground: " + aurl[0]);
                    String tempurl = aurl[0];

                    Log.d(TAG, "doInBackground12345: ---" + tempurl);


                    URL url = new URL(tempurl);
                    URLConnection conexion = url.openConnection();
                    conexion.connect();

                    int lenghtOfFile = conexion.getContentLength();
                    Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

                    String folder_main = "My Stream";
                    input = new BufferedInputStream(url.openStream());

                    Log.d(TAG, "doInBackground12345: ---" + Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));




                    File SDPath =  new SeparateFunctions(AyaList.this).getAppSpecificDownloadStorageDir(AyaList.this,AyaList.this);
                    if (!SDPath.exists()) {
                        SDPath.mkdirs();
                    }

                    output = new FileOutputStream(SDPath + RecitesName + RecitesAYA + ".mp3");


               /* try (BufferedInputStream in = new BufferedInputStream(new URL(aurl[0]).openStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(SDPath + RecitesName + RecitesAYA + "88888888.mp3")) {
                    byte dataBuffer[] = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        Log.d(TAG, "doInBackground12345: ---" + dataBuffer);

                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    Log.d(TAG, "doInBackground12345: ---" + e);

                    // handle exception
                }*/


                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        Log.d(TAG, "doInBackground: " + (int) ((total * 100) / lenghtOfFile));
                        try {
                            publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        output.write(data, 0, count);


                    }

                    MetaDataEditor metaDataEditor = new MetaDataEditor(AyaList.this);
                    metaDataEditor.changeMetaData(SDPath + RecitesName + RecitesAYA + ".mp3");

                    output.flush();
                    output.close();
                    input.close();


                /*URL url2 = new URL(tempurl);
                ReadableByteChannel readableByteChannel = Channels.newChannel(url2.openStream());

                FileOutputStream fileOutputStream = new FileOutputStream(SDPath + RecitesName + RecitesAYA + ".mp3");
                FileChannel fileChannel = fileOutputStream.getChannel();
                fileChannel
                        .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);*/



                    Log.d(TAG, "doInBackground: finish");

                } catch (Exception e) {

                    Log.d(TAG, "doInBackground12345: Exception ---  " + e);
                }
            }


            /*int i;
            for (i = 0; i <= 100; i += 5) {
                // Sets the progress indicator completion percentage
                publishProgress(String.valueOf(Math.min(i, 100)));
                try {
                    // Sleep for 5 seconds
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    Log.d("TAG", "sleep failure");
                }
            }*/
            return null;

        }
        protected void onProgressUpdate(String... progress) {
            // Log.d("ANDRO_ASYNC",progress[0]);
            //mProgressDialog.setProgress(Integer.parseInt(progress[0]));
            try {
                progressBar.setProgress(Integer.parseInt(progress[0]));

                // Update progress
                mBuilder.setProgress(100, Integer.parseInt(progress[0]), false);
                mNotifyManager.notify(id, mBuilder.build());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPostExecute(String unused) {
            LoadAya();
            isDownloading = false;
            //dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
            LayoutLoading.setVisibility(View.GONE);
            ISDonwloading=false;
            mBuilder.setContentText(getString(R.string.download_done));
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(id, mBuilder.build());
            Toast.makeText(AyaList.this, getString(R.string.download_done), Toast.LENGTH_LONG).show();

        }



        public void canceldownload(){
            dfa.cancel(true);
            ISDonwloading=false;
            mBuilder.setContentText(getString(R.string.download_canceled));
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(id, mBuilder.build());
            mNotifyManager.cancel(id);
            LoadAya();
            Toast.makeText(AyaList.this, getString(R.string.download_canceled), Toast.LENGTH_LONG).show();

        }
    }
    //=====================================
    class VivzAdapter extends RecyclerView.Adapter<VivzAdapter.ViewHolder>
    {




//        ArrayList<AuthorClass> listrecitesLocal;
        private List<Object> listrecitesLocalobject;


        VivzAdapter(ArrayList<AuthorClass> listrecites) {

//            listrecitesLocal = new ArrayList<AuthorClass>();
//            listrecitesLocal = listrecites;

//            listrecitesLocalobject = new List<Object>;
//            listrecitesLocalobject = recyclerViewItems;


        }

        public VivzAdapter(List<Object> recyclerViewItems) {
            listrecitesLocalobject = recyclerViewItems;
        }


        @NonNull
        @Override
        public VivzAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.single_rowayalist, parent, false);
            VivzAdapter.ViewHolder viewHolder = new VivzAdapter.ViewHolder(listItem);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull VivzAdapter.ViewHolder holder, int i) {

            LayoutInflater mInflater = getLayoutInflater();
//            View row = mInflater.inflate(R.layout.single_rowayalist, null);


            //  budownload.setBackground(getResources().getDrawable(R.drawable.buttonred)) ; // "@drawable/buttonred");

            if (listrecitesLocalobject.get(i) instanceof AdView){

                AdView adView = (AdView) listrecitesLocalobject.get(i);

                if (listrecitesLocalobject.get(i) instanceof AdView){
                    Log.d("sfssdfsddsf", "The interstitial wasn't loaded yet.");

                }


                if(adView.getParent() != null) {
                    ((ViewGroup)adView.getParent()).removeView(adView); // <- fix
                }
                holder.cardview.addView(adView); //  <==========  ERROR IN THIS LINE DURING 2ND RUN
                holder.cardview.setVisibility(View.VISIBLE);
                holder.cardContent.setVisibility(View.GONE);

                holder.ShareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                holder.budownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                holder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                holder.title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

            }else {
                final AuthorClass temp= (AuthorClass) this.listrecitesLocalobject.get(i);
                final int postion =i;
                //final  String linkaya=temp.ImgUrl;
                final  String  ServerName =temp.ServerName;
                holder.cardview.setVisibility(View.GONE);
                holder.cardContent.setVisibility(View.VISIBLE);
                holder.cardContent.setLayoutDirection(SettingSaved.LanguageSelect==1?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);

            /*
            //check if SD availbel
            Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

            if(isSDPresent)
            {
                // yes SD-card is present
               // budownload.setEnabled(true);
                budownload.setVisibility(View.VISIBLE);
            }
            else
            {  // No SD-card is present;
                budownload.setVisibility(View.GONE);

            }*/

                //share
                holder.ShareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        onShareBy(temp.RealName,RealRecitesName,temp.ServerName);
                    }
                });
                // if already dowload
                if(temp.StateName.equals(LnaguageClass.avalible()))
                    holder.budownload.setVisibility(View.INVISIBLE);
                // downlaod file
                holder.budownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if( ISDonwloading!=true){
//                        checkPermission();
                            Permissions permissions = new Permissions(AyaList.this,AyaList.this);
                            if(permissions.checkStoragePermission()){
                                startDownload(temp.ImgUrl,ServerName );

                            }else {
                                downloadAllRequest = false;
                                tempImgUrlForPermissionWait = temp.ImgUrl;
                                ServerNameForPermissionWait = ServerName;
                            }
                        }
                        Toast.makeText(AyaList.this, RecitesAYA, Toast.LENGTH_SHORT).show();
                    }
                });
                //=====================================================
                holder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //get aya
                        //load full screan ad
                    /*if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Log.d("TAG", "The interstitial wasn't loaded yet.");
                    }*/
                        if( ISDonwloading!=true)
                            for (int i=0;i< listrecitesAya.size();i++) {
                                if(listrecitesAya.get(i).RealName.equals(temp.RealName)){
                                    RecitesAYA=String.valueOf(i);// ServerName;
                                    surahName = listrecitesAya.get(i).RealName;
                                    //save server name to play it out
                                /*SettingSaved.FinalAya=RecitesAYA;
                                SettingSaved settingSaved=new SettingSaved(getApplicationContext());
                                settingSaved.SaveData();*/
                                    AyaNameView = holder.title;

                                    AyaImage = holder.image;
                                    DisplayAya();
                                    break;
                                }

                            }

                    }
                });
                holder.title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //load full screan ad
                    /*if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Log.d("TAG", "The interstitial wasn't loaded yet.");
                    }*/
                        //get aya
                        if( ISDonwloading!=true)
                            for (int i=0;i< listrecitesAya.size();i++) {
                                if(listrecitesAya.get(i).RealName.equals(temp.RealName)){
                                    RecitesAYA=String.valueOf(i);// ServerName;
                                    surahName = listrecitesAya.get(i).RealName;


                                    //save server name to play it out
                               /* SettingSaved.FinalAya=RecitesAYA;
                                SettingSaved settingSaved=new SettingSaved(getApplicationContext());
                                settingSaved.SaveData();*/
                                    AyaNameView = holder.title;
                                    AyaImage = holder.image;

                                    DisplayAya();
                                    break;
                                }

                            }
                    }
                });


//            budownload.setText(getResources().getString(R.string.downlaod));
                holder.title.setText(temp.RealName);
                holder.cost.setText(temp.StateName);// it updated
                //image.setImageResource(temp.ImgUrl);
            }




        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return this.listrecitesLocalobject.size();

        }

        private void onShareBy( String soura,String reciter,String  ServerName) {


            Uri imageUri = Uri.parse(getString(R.string.dynamicLinkShareImage));
            String title = "";
            String description = "";
            if (SettingSaved.LanguageSelect == 2){
                String englishTitle = new SeparateFunctions(AyaList.this).getLocaleStringResource(new Locale("en"), R.string.surah, AyaList.this);

                String englishTitle2 = new SeparateFunctions(AyaList.this).getLocaleStringResource(new Locale("en"), R.string.forreciter, AyaList.this);
                String englishDescription = new SeparateFunctions(AyaList.this).getLocaleStringResource(new Locale("en"), R.string.dynamicLinkShareTitle, AyaList.this);

                title = englishTitle +""+ soura + " " +  englishTitle2 + " " + reciter ;
                description = englishDescription;
            }else {
                String arabicTitle = new SeparateFunctions(AyaList.this).getLocaleStringResource(new Locale("ar"), R.string.surah, AyaList.this);
                String arabicTitle2 = new SeparateFunctions(AyaList.this).getLocaleStringResource(new Locale("ar"), R.string.forreciter, AyaList.this);
                String arabicDescription = new SeparateFunctions(AyaList.this).getLocaleStringResource(new Locale("ar"), R.string.dynamicLinkShareTitle, AyaList.this);

                title = arabicTitle +""+ soura + " " +  arabicTitle2 + " " + reciter ;
                description = arabicDescription;
            }

            SeparateFunctions separateFunctions = new SeparateFunctions(AyaList.this);
            separateFunctions.createDynamicLink(AyaList.this,"surah/"+RecitesName+"/"+ServerName,title,description,imageUri).addOnCompleteListener(AyaList.this, new OnCompleteListener<ShortDynamicLink>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<ShortDynamicLink> task) {
                    if (task.isSuccessful()) {
                        // Short link created
                        Uri shortLink = task.getResult().getShortLink();
                        Uri flowchartLink = task.getResult().getPreviewLink();
                        Log.d(TAG, "createDynamicLink 2: " +shortLink);

                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
//            String shareBody = getResources().getString(R.string.sharemessage) + "  https://rebrand.ly/notfof70d";
                        String shareBody = "";
                        if (getResources().getString(R.string.sharePart3) == "reciter"){
                            shareBody = getResources().getString(R.string.sharePart1) +" " +soura+ " "+ getResources().getString(R.string.sharePart2) + " "+ reciter + " "+ getResources().getString(R.string.sharePart3) +"."+ " "+ getResources().getString(R.string.sharePart4) + " " + shortLink.toString();

                        }else{
                            shareBody = getResources().getString(R.string.sharePart1) +" " +soura+ " "+ getResources().getString(R.string.sharePart2) + " "+ getResources().getString(R.string.sharePart3) + " "+ reciter +"."+ " "+ getResources().getString(R.string.sharePart4) + " " + shortLink.toString();

                        }
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Stream");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));

                        createReport(shortLink.toString());


                    } else {
                        Log.d(TAG, "createDynamicLink 2: " +task);
                        // Error
                        // ...
                    }
                }

            });




        }



        public  class ViewHolder extends RecyclerView.ViewHolder {



            TextView title ;
            TextView cost ;
            ImageView image ;
            ImageButton budownload ;
            ImageButton ShareButton ;
            CardView cardview;
            CardView cardContent;

            public ViewHolder(View itemView) {
                super(itemView);


                 title=(TextView) itemView.findViewById( R.id.textView1);
                 cost=(TextView) itemView.findViewById( R.id.textView2);
                 image =(ImageView) itemView.findViewById( R.id.imageView);
                 budownload =(ImageButton) itemView.findViewById( R.id.button);
                 ShareButton =(ImageButton) itemView.findViewById( R.id.buttonShare);
                 cardview =(CardView) itemView.findViewById( R.id.cardviewad);
                 cardContent =(CardView) itemView.findViewById( R.id.cardContent);

            }

        }

    }
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
    private int getNotificationIcon() {
        boolean useWhiteIcon = (SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.mystreamwhite : R.drawable.mystream;
    }
}
