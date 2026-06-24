package com.medoapps.www.onlinequran;

import static android.os.Build.VERSION.SDK_INT;
import static com.medoapps.www.onlinequran.util.Permissions.REQUEST_CODE_ASK_STORAGE_PERMISSIONS;

import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.NotificationChannel;

import com.medoapps.www.onlinequran.util.AppBottomSheet;
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
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import io.supercharge.shimmerlayout.ShimmerLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat.Builder;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;

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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


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
    private DownloadService downloadService;
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

    private boolean selectionMode = false;
    private Set<Integer> selectedPositions = new HashSet<>();
    private TextView downloadAllText;
    private TextView selectAllBTN;
    private View downloadAllArrow;

    // Navy-hero identity views (hidden during selection mode to free the row for the pill).
    private View heroAvatar;
    private View heroIdentity;
    private TextView heroSubtitle;
    private TextView collapsedReciterName;   // reciter name shown in the toolbar when collapsed
    private boolean searchExpanded = false;

    private ImageButton toggleViewBTN;
    private static final int VIEW_MODE_LIST = 0;
    private static final int VIEW_MODE_GRID = 1;
    private static final int VIEW_MODE_COMPACT = 2;
    private int viewMode = VIEW_MODE_LIST;
    private static final String PREF_VIEW_MODE = "ayalist_view_mode";

    // Canonical ayah counts (Hafs), indexed by surah number 1..114; index 0 unused.
    // Used by the design-C ayah-count chip. The aya list is always the 114 surahs in order.
    private static final int[] AYAH_COUNTS = {
        0,
        7, 286, 200, 176, 120, 165, 206, 75, 129, 109,
        123, 111, 43, 52, 99, 128, 111, 110, 98, 135,
        112, 78, 118, 64, 77, 227, 93, 88, 69, 60,
        34, 30, 73, 54, 45, 83, 182, 88, 75, 85,
        54, 53, 89, 59, 37, 35, 38, 29, 18, 45,
        60, 49, 62, 55, 78, 96, 29, 22, 24, 13,
        14, 11, 11, 18, 12, 12, 30, 52, 52, 44,
        28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
        29, 19, 36, 25, 22, 17, 19, 26, 30, 20,
        15, 21, 11, 8, 8, 19, 5, 8, 8, 11,
        11, 8, 3, 9, 5, 4, 7, 3, 6, 3,
        5, 4, 5, 6
    };

    private ShimmerLayout loader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setAnimation();
        setContentView(R.layout.activity_aya_list);

        // Navy TabHeader -> match the status bar (fixed navy) with light icons in both themes.
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.navy_700));
        androidx.core.view.WindowInsetsControllerCompat __wic =
                androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (__wic != null) __wic.setAppearanceLightStatusBars(false);


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
        loader = (ShimmerLayout) findViewById(R.id.skeletonLoader);
        loader.startShimmerAnimation();
        LnaguageClass lc = new LnaguageClass(AyaList.this,AyaList.this);

        ActivityReciter = lc.SetTextFont(ActivityReciter,"");
        ActivityReciter.setText(RealRecitesName);
        ActivityReciter.setSelected(true);
        setupCollapsingHero();

        backBTN = (ImageButton) findViewById(R.id.backBTN);
        downloadAllBTN = (CardView) findViewById(R.id.downloadAllBTN);
        downloadAllText = (TextView) findViewById(R.id.downloadAllText);
        selectAllBTN = (TextView) findViewById(R.id.selectAllBTN);
        downloadAllArrow = findViewById(R.id.downloadAllArrow);
        heroAvatar = findViewById(R.id.heroAvatar);
        heroIdentity = findViewById(R.id.heroIdentity);
        heroSubtitle = (TextView) findViewById(R.id.heroSubtitle);
        selectAllBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSelectAll();
            }
        });
        downloadAllBTN.setEnabled(false);
        downloadAllBTN.setAlpha(0.5f);
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rateApp();
//                finish();
            }
        });

        viewMode = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(PREF_VIEW_MODE, VIEW_MODE_LIST);
        toggleViewBTN = (ImageButton) findViewById(R.id.toggleViewBTN);
        updateToggleIcon();
        toggleViewBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showViewModeMenu(view);
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
        applyLayoutMode();
        LayoutLoading=(LinearLayout)findViewById(R.id.LayoutLoading);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        LayoutLoading.setVisibility(View.GONE);

        findViewById(R.id.cancelDownloadBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDownload();
            }
        });
//        getAppSpecificAlbumStorageDir(this,"My Stream2");

        downloadAllBTN.setEnabled(true);
        downloadAllBTN.setAlpha(1.0f);
        downloadAllBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectionMode) {
                    startDownloadSelected();
                } else {
                    enterSelectionMode(-1);
                }
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


            applyLayoutMode();
            loader.stopShimmerAnimation();
            loader.setVisibility(View.GONE);

            // Navy-hero subtitle: surah count (locale-formatted digits).
            if (heroSubtitle != null) {
                int cnt = (listrecitesAya != null) ? listrecitesAya.size() : 0;
                heroSubtitle.setText(getString(R.string.surah_count, cnt));
            }
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
                    }else if (tempImgUrlForPermissionWait != null){
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
                            downloadAllBTN.setAlpha(1.0f);


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
        AppBottomSheet.showConfirmation(this,
            getString(R.string.DownloadAllTitle),
            getString(R.string.DownloadAllMessage),
            getString(android.R.string.yes),
            getString(android.R.string.no),
            () -> showRewardedVideo(), null);
    }
    private  void startDownloadAll(){
        downloadAllRequest = true;

        // On API < 30, storage permission is needed for direct file access.
        // On API 30+, DownloadService uses MediaStore (scoped storage) so permission is not required.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Permissions permissions = new Permissions(AyaList.this, AyaList.this);
            if (!permissions.checkStoragePermission()) {
                return;
            }
        }

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 200);
            }
        }

        try {
            listDownloadAya.clear();
            listrecitesAya.clear();
            LnaguageClass lc = new LnaguageClass(AyaList.this, AyaList.this);
            listrecitesAya = lc.GuranAya(RecitesName, Rewayat);

            for (int i = 0; i < listrecitesAya.size(); ++i) {
                AuthorClass temp = listrecitesAya.get(i);
                if (temp.ImgUrl != null && temp.ImgUrl.contains("http")) {
                    listDownloadAya.add(temp);
                }
            }

            if (listDownloadAya.isEmpty()) {
                Toast.makeText(this, getString(R.string.no_items_to_download), Toast.LENGTH_SHORT).show();
                return;
            }

            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.clearCacheDownloadslist();
            storage.storeDownloadlist(listDownloadAya);
            storage.storeDownloadIndex(0);
            storage.storeDownloadRecitesName(RecitesName);
            storage.storeDownloadRealRecitesName(RealRecitesName);

            LayoutLoading.setVisibility(View.VISIBLE);
            downloadAllBTN.setEnabled(false);
            downloadAllBTN.setAlpha(0.5f);

            Intent downloaderIntent = new Intent(AyaList.this, DownloadService.class);
            startService(downloaderIntent);
            bindService(downloaderIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            register_loadAyaReceiver();

            Toast.makeText(this, getString(R.string.download_started), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "startDownloadAll failed", e);
            Toast.makeText(this, getString(R.string.download_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private void enterSelectionMode(int initialPosition) {
        selectionMode = true;
        selectedPositions.clear();
        if (initialPosition >= 0) {
            selectedPositions.add(initialPosition);
        }
        updateDownloadCard();
        if (listAya.getAdapter() != null) {
            listAya.getAdapter().notifyDataSetChanged();
        }
    }

    private void exitSelectionMode() {
        selectionMode = false;
        selectedPositions.clear();
        updateDownloadCard();
        if (listAya.getAdapter() != null) {
            listAya.getAdapter().notifyDataSetChanged();
        }
    }

    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        updateDownloadCard();
        if (listAya.getAdapter() != null) {
            listAya.getAdapter().notifyItemChanged(position);
        }
        if (selectedPositions.isEmpty()) {
            exitSelectionMode();
        }
    }

    private void updateDownloadCard() {
        // In selection mode, hide the hero identity so the download pill + select-all get the row.
        int identityVis = selectionMode ? View.GONE : View.VISIBLE;
        if (heroAvatar != null) heroAvatar.setVisibility(identityVis);
        if (heroIdentity != null) heroIdentity.setVisibility(identityVis);

        if (selectionMode && !selectedPositions.isEmpty()) {
            downloadAllText.setText(String.format(getString(R.string.download_selected), selectedPositions.size()));
        } else if (selectionMode) {
            downloadAllText.setText(getString(R.string.select_surahs));
        } else {
            downloadAllText.setText(getString(R.string.download_all_list));
        }

        if (selectionMode) {
            selectAllBTN.setVisibility(View.VISIBLE);
            downloadAllArrow.setVisibility(View.GONE);
            // Toggle label between select all / deselect all
            int nonDownloadedCount = getNonDownloadedCount();
            if (selectedPositions.size() >= nonDownloadedCount && nonDownloadedCount > 0) {
                selectAllBTN.setText(getString(R.string.deselect_all));
            } else {
                selectAllBTN.setText(getString(R.string.select_all));
            }
        } else {
            selectAllBTN.setVisibility(View.GONE);
            downloadAllArrow.setVisibility(View.VISIBLE);
        }
    }

    private int getNonDownloadedCount() {
        int count = 0;
        for (int j = 0; j < recyclerViewItems.size(); j++) {
            if (recyclerViewItems.get(j) instanceof AuthorClass) {
                AuthorClass item = (AuthorClass) recyclerViewItems.get(j);
                if (!item.StateName.equals(LnaguageClass.avalible())) {
                    count++;
                }
            }
        }
        return count;
    }

    private void toggleSelectAll() {
        int nonDownloadedCount = getNonDownloadedCount();
        if (selectedPositions.size() >= nonDownloadedCount && nonDownloadedCount > 0) {
            // Deselect all
            selectedPositions.clear();
        } else {
            // Select all non-downloaded
            selectedPositions.clear();
            for (int j = 0; j < recyclerViewItems.size(); j++) {
                if (recyclerViewItems.get(j) instanceof AuthorClass) {
                    AuthorClass item = (AuthorClass) recyclerViewItems.get(j);
                    if (!item.StateName.equals(LnaguageClass.avalible())) {
                        selectedPositions.add(j);
                    }
                }
            }
        }
        updateDownloadCard();
        if (listAya.getAdapter() != null) {
            listAya.getAdapter().notifyDataSetChanged();
        }
    }

    private void startDownloadSelected() {
        downloadAllRequest = false;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Permissions permissions = new Permissions(AyaList.this, AyaList.this);
            if (!permissions.checkStoragePermission()) {
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 200);
            }
        }

        try {
            listDownloadAya.clear();
            for (int pos : selectedPositions) {
                if (pos < recyclerViewItems.size()
                        && recyclerViewItems.get(pos) instanceof AuthorClass) {
                    AuthorClass item = (AuthorClass) recyclerViewItems.get(pos);
                    if (item.ImgUrl != null && item.ImgUrl.contains("http")) {
                        listDownloadAya.add(item);
                    }
                }
            }

            if (listDownloadAya.isEmpty()) {
                Toast.makeText(this, getString(R.string.no_items_to_download), Toast.LENGTH_SHORT).show();
                exitSelectionMode();
                return;
            }

            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.clearCacheDownloadslist();
            storage.storeDownloadlist(listDownloadAya);
            storage.storeDownloadIndex(0);
            storage.storeDownloadRecitesName(RecitesName);
            storage.storeDownloadRealRecitesName(RealRecitesName);

            LayoutLoading.setVisibility(View.VISIBLE);
            downloadAllBTN.setEnabled(false);
            downloadAllBTN.setAlpha(0.5f);

            Intent downloaderIntent = new Intent(AyaList.this, DownloadService.class);
            startService(downloaderIntent);
            bindService(downloaderIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            register_loadAyaReceiver();

            Toast.makeText(this, getString(R.string.download_started), Toast.LENGTH_SHORT).show();
            exitSelectionMode();
        } catch (Exception e) {
            Log.e(TAG, "startDownloadSelected failed", e);
            Toast.makeText(this, getString(R.string.download_failed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (selectionMode) {
            exitSelectionMode();
        } else {
            super.onBackPressed();
        }
    }

    private void cancelDownload() {
        try {
            if (serviceBound && downloadService != null) {
                downloadService.canceldownload();
            }
            if (serviceBound) {
                unbindService(serviceConnection);
                serviceBound = false;
                downloadService = null;
            }
            stopService(new Intent(AyaList.this, DownloadService.class));
        } catch (Exception e) {
            Log.e(TAG, "cancelDownload failed", e);
        }
        LayoutLoading.setVisibility(View.GONE);
        downloadAllBTN.setEnabled(true);
        downloadAllBTN.setAlpha(1.0f);
        Toast.makeText(this, getString(R.string.download_canceled), Toast.LENGTH_SHORT).show();
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
            DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
            downloadService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            downloadService = null;
            serviceBound = false;
        }
    };

    private BroadcastReceiver updateProgressBarReceiver  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LayoutLoading.setVisibility(View.GONE);
            downloadAllBTN.setEnabled(true);
            downloadAllBTN.setAlpha(1.0f);
            LnaguageClass.clearAyaAvailabilityCache();   // a download finished -> availability changed
            LoadAya();
        }
    };

    private BroadcastReceiver downloadProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra(DownloadService.EXTRA_PROGRESS, 0);
            String surahName = intent.getStringExtra(DownloadService.EXTRA_SURAH_NAME);
            progressBar.setProgress(progress);
            if (surahName != null && !surahName.isEmpty()) {
                ((TextView) findViewById(R.id.textView3)).setText(surahName + " - " + progress + "%");
            }
        }
    };

    private void register_loadAyaReceiver() {
        try {
            unregisterReceiver(updateProgressBarReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            unregisterReceiver(downloadProgressReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        IntentFilter filter = new IntentFilter(Broadcast_LoadAya);
        try {
            ContextCompat.registerReceiver(this, updateProgressBarReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        } catch (Exception e) {
            e.printStackTrace();
        }

        IntentFilter progressFilter = new IntentFilter(DownloadService.BROADCAST_DOWNLOAD_PROGRESS);
        try {
            ContextCompat.registerReceiver(this, downloadProgressReceiver, progressFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(updateProgressBarReceiver); } catch (Exception ignored) {}
        try { unregisterReceiver(downloadProgressReceiver); } catch (Exception ignored) {}
        if (serviceBound) {
            try { unbindService(serviceConnection); } catch (Exception ignored) {}
            serviceBound = false;
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
    /**
     * Wires the collapsing navy hero: as the AppBarLayout collapses on scroll, the expanded
     * identity row fades out and the reciter name fades into the pinned toolbar.
     */
    private void setupCollapsingHero() {
        final View expanded = findViewById(R.id.heroExpanded);
        collapsedReciterName = (TextView) findViewById(R.id.collapsedReciterName);
        com.google.android.material.appbar.AppBarLayout appBar = findViewById(R.id.appbar);
        if (collapsedReciterName != null) collapsedReciterName.setText(RealRecitesName);
        if (appBar == null) return;
        appBar.addOnOffsetChangedListener(
                new com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(com.google.android.material.appbar.AppBarLayout bar, int verticalOffset) {
                int range = bar.getTotalScrollRange();
                float fraction = (range > 0) ? Math.min(1f, Math.abs(verticalOffset) / (float) range) : 0f;
                if (expanded != null) expanded.setAlpha(1f - fraction);
                // Don't reveal the collapsed title while the search field is open over the toolbar.
                if (collapsedReciterName != null && !searchExpanded) {
                    collapsedReciterName.setAlpha(fraction);
                }
            }
        });
    }

    /**
     * Colors the framework SearchView's internal views so they stay legible on the fixed-navy
     * TabHeader in both light and dark mode (mirrors RecitesName). text_on_navy / hint_on_navy
     * are fixed; gold_accent adapts but stays gold on navy in both modes.
     */
    private void styleSearchViewForNavy() {
        if (searchView == null) return;
        int textOnNavy = ContextCompat.getColor(this, R.color.text_on_navy);
        int hintOnNavy = ContextCompat.getColor(this, R.color.hint_on_navy);
        int gold = ContextCompat.getColor(this, R.color.gold_accent);

        EditText queryText = searchView.findViewById(
                getResources().getIdentifier("android:id/search_src_text", null, null));
        if (queryText != null) {
            queryText.setTextColor(textOnNavy);
            queryText.setHintTextColor(hintOnNavy);
        }
        for (String idName : new String[]{
                "android:id/search_button", "android:id/search_mag_icon", "android:id/search_close_btn"}) {
            ImageView icon = searchView.findViewById(getResources().getIdentifier(idName, null, null));
            if (icon != null) icon.setColorFilter(gold);
        }
    }

    private void searchManager(){
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView = (SearchView) findViewById(R.id.search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        // The header is now the fixed-navy TabHeader; color the SearchView internals to stay legible.
        styleSearchViewForNavy();
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
//                ActivityTitle.setVisibility(View.VISIBLE);
                ActivityReciter.setVisibility(View.VISIBLE);
                searchExpanded = false;
                if (collapsedReciterName != null) collapsedReciterName.setVisibility(View.VISIBLE);
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ActivityTitle.setVisibility(View.GONE);
                ActivityReciter.setVisibility(View.GONE);
                // Hide the collapsed-title overlay so the search field has the toolbar to itself.
                searchExpanded = true;
                if (collapsedReciterName != null) collapsedReciterName.setVisibility(View.GONE);
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



        new Thread(() -> createReport(null)).start();
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
    private void updateToggleIcon() {
        // Fixed tune/sliders icon — mode is chosen from the popup menu
    }

    private void showViewModeMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_view_mode, popup.getMenu());

        // Mark the current mode
        int activeId;
        switch (viewMode) {
            case VIEW_MODE_GRID: activeId = R.id.view_mode_grid; break;
            case VIEW_MODE_COMPACT: activeId = R.id.view_mode_compact; break;
            default: activeId = R.id.view_mode_list; break;
        }
        popup.getMenu().findItem(activeId).setChecked(true);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.view_mode_list) {
                    viewMode = VIEW_MODE_LIST;
                } else if (id == R.id.view_mode_grid) {
                    viewMode = VIEW_MODE_GRID;
                } else if (id == R.id.view_mode_compact) {
                    viewMode = VIEW_MODE_COMPACT;
                } else {
                    return false;
                }
                PreferenceManager.getDefaultSharedPreferences(AyaList.this)
                        .edit().putInt(PREF_VIEW_MODE, viewMode).apply();
                updateToggleIcon();
                applyLayoutMode(true);
                return true;
            }
        });
        popup.show();
    }

    private void applyLayoutMode() {
        applyLayoutMode(false);
    }

    private void applyLayoutMode(boolean animate) {
        if (animate) {
            listAya.animate().alpha(0f).setDuration(150).withEndAction(new Runnable() {
                @Override
                public void run() {
                    swapLayoutManager();
                    listAya.animate().alpha(1f).setDuration(150).start();
                }
            }).start();
        } else {
            swapLayoutManager();
        }
    }

    private void swapLayoutManager() {
        if (viewMode == VIEW_MODE_GRID) {
            GridLayoutManager glm = new GridLayoutManager(this, 2);
            glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (position < recyclerViewItems.size()
                            && recyclerViewItems.get(position) instanceof AdView)
                        return 2;
                    return 1;
                }
            });
            listAya.setLayoutManager(glm);
        } else {
            listAya.setLayoutManager(new LinearLayoutManager(AyaList.this));
        }
        listAya.setAdapter(new VivzAdapter(recyclerViewItems, viewMode));
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


        applyLayoutMode();


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

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 200);
            }
        }

        listDownloadAya.clear();
        //get list of recites
        listrecitesAya.clear();
        LnaguageClass lc = new LnaguageClass(AyaList.this,AyaList.this);
        listrecitesAya = lc.GuranAya(RecitesName,Rewayat);
        for (int i = 0; i < listrecitesAya.size(); ++i) {

            AuthorClass temp = listrecitesAya.get(i);

            if (temp.ImgUrl.equalsIgnoreCase(ImgUrl)){

                listDownloadAya.add(temp);

            }
        }
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.clearCacheDownloadslist();

        storage.storeDownloadlist(listDownloadAya);
        storage.storeDownloadIndex(0);
        storage.storeDownloadRecitesName(RecitesName);
        storage.storeDownloadRealRecitesName(RealRecitesName);

        LayoutLoading.setVisibility(View.VISIBLE);

        Intent downloaderIntent = new Intent(AyaList.this, DownloadService.class);
        startService(downloaderIntent);
        bindService(downloaderIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        register_loadAyaReceiver();
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
                        metaDataEditor.changeMetaData(SDPath + "/AhmedHashim_" + RecitesName + RecitesAYA + ".mp3");


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

                    output = new FileOutputStream(SDPath + "/AhmedHashim_" + RecitesName + RecitesAYA + ".mp3");


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
                    metaDataEditor.changeMetaData(SDPath + "/AhmedHashim_" + RecitesName + RecitesAYA + ".mp3");

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
    private void deleteLocalSurah(AuthorClass item, VivzAdapter.ViewHolder holder) {
        try {
            String filePath = item.ImgUrl;
            boolean deleted = false;

            if (filePath.startsWith("content://")) {
                // File was found via MediaStore content URI (e.g. after reinstall)
                try {
                    ContentResolver resolver = getContentResolver();
                    deleted = resolver.delete(Uri.parse(filePath), null, null) > 0;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // File path - try direct file delete
                File file = new File(filePath);
                if (file.exists()) {
                    deleted = file.delete();
                }

                // Also remove from MediaStore on API 29+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        ContentResolver resolver = getContentResolver();
                        String selection = MediaStore.Audio.Media.DATA + "=?";
                        String[] selectionArgs = new String[]{filePath};
                        resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (deleted) {
                LnaguageClass.clearAyaAvailabilityCache();   // a file was removed -> availability changed
                // Rebuild the streaming URL
                LnaguageClass lc = new LnaguageClass(AyaList.this, AyaList.this);
                String streamUrl;
                if (Rewayat != null && !Rewayat.isEmpty()) {
                    streamUrl = "https://server" + lc.serverNumber(RecitesName) + ".mp3quran.net/" + RecitesName + "/" + Rewayat + "/" + item.ServerName + ".mp3";
                } else {
                    streamUrl = "https://server" + lc.serverNumber(RecitesName) + ".mp3quran.net/" + RecitesName + "/" + item.ServerName + ".mp3";
                }

                // Update the item in place
                item.StateName = LnaguageClass.disavalible();
                item.ImgUrl = streamUrl;

                // Update UI immediately
                holder.budownload.setVisibility(View.VISIBLE);
                holder.budelete.setVisibility(View.GONE);
                holder.cost.setText(item.StateName);
                holder.statusIcon.setImageResource(R.drawable.ic_streaming);
                holder.statusIcon.setVisibility(View.VISIBLE);

                Toast.makeText(AyaList.this, getString(R.string.surah_deleted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AyaList.this, getString(R.string.surah_delete_failed), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(AyaList.this, getString(R.string.surah_delete_failed), Toast.LENGTH_SHORT).show();
        }
    }

    //=====================================
    class VivzAdapter extends RecyclerView.Adapter<VivzAdapter.ViewHolder>
    {

        private static final int VIEW_TYPE_AD = 0;
        private static final int VIEW_TYPE_SURAH_LIST = 1;
        private static final int VIEW_TYPE_SURAH_GRID = 2;
        private static final int VIEW_TYPE_SURAH_COMPACT = 3;

//        ArrayList<AuthorClass> listrecitesLocal;
        private List<Object> listrecitesLocalobject;
        private int viewMode;


        VivzAdapter(ArrayList<AuthorClass> listrecites) {
            this(new ArrayList<Object>(listrecites), AyaList.this.viewMode);
        }

        public VivzAdapter(List<Object> recyclerViewItems) {
            this(recyclerViewItems, AyaList.this.viewMode);
        }

        public VivzAdapter(List<Object> recyclerViewItems, int viewMode) {
            listrecitesLocalobject = recyclerViewItems;
            this.viewMode = viewMode;
        }

        @Override
        public int getItemViewType(int position) {
            if (listrecitesLocalobject.get(position) instanceof AdView) {
                return VIEW_TYPE_AD;
            }
            switch (viewMode) {
                case VIEW_MODE_GRID:
                    return VIEW_TYPE_SURAH_GRID;
                case VIEW_MODE_COMPACT:
                    return VIEW_TYPE_SURAH_COMPACT;
                default:
                    return VIEW_TYPE_SURAH_LIST;
            }
        }

        @NonNull
        @Override
        public VivzAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            int layoutRes;
            switch (viewType) {
                case VIEW_TYPE_SURAH_GRID:
                    layoutRes = R.layout.grid_item_ayalist;
                    break;
                case VIEW_TYPE_SURAH_COMPACT:
                    layoutRes = R.layout.compact_item_ayalist;
                    break;
                default:
                    layoutRes = R.layout.single_rowayalist;
                    break;
            }
            View listItem = layoutInflater.inflate(layoutRes, parent, false);
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

                boolean isDownloaded = temp.StateName.equals(LnaguageClass.avalible());

                // --- Selection mode handling ---
                if (selectionMode) {
                    if (holder.selectCheckBox != null) {
                        holder.selectCheckBox.setVisibility(View.VISIBLE);
                        holder.image.setVisibility(View.GONE);
                        if (isDownloaded) {
                            holder.selectCheckBox.setChecked(true);
                            holder.selectCheckBox.setEnabled(false);
                        } else {
                            holder.selectCheckBox.setChecked(selectedPositions.contains(postion));
                            holder.selectCheckBox.setEnabled(true);
                        }
                    }
                    // Hide action buttons in selection mode
                    holder.ShareButton.setVisibility(View.GONE);
                    holder.budownload.setVisibility(View.GONE);
                    holder.budelete.setVisibility(View.GONE);

                    holder.image.setClickable(false);
                    holder.title.setClickable(false);
                    holder.cardContent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isDownloaded) {
                                toggleSelection(postion);
                            }
                        }
                    });
                    holder.cardContent.setOnLongClickListener(null);
                } else {
                    // --- Normal mode ---
                    if (holder.selectCheckBox != null) {
                        holder.selectCheckBox.setVisibility(View.GONE);
                    }
                    holder.image.setVisibility(View.VISIBLE);
                    holder.ShareButton.setVisibility(View.VISIBLE);

                    //share
                    holder.ShareButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onShareBy(temp.RealName,RealRecitesName,temp.ServerName);
                        }
                    });
                    // if already downloaded: hide download, show delete
                    if(isDownloaded) {
                        holder.budownload.setVisibility(View.GONE);
                        holder.budelete.setVisibility(View.VISIBLE);
                    } else {
                        holder.budownload.setVisibility(View.VISIBLE);
                        holder.budelete.setVisibility(View.GONE);
                    }
                    // delete local file
                    holder.budelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AppBottomSheet.showConfirmation(AyaList.this,
                                getString(R.string.audio_manager_surah_delete),
                                getString(R.string.audio_manager_remove_audio_msg, temp.RealName),
                                getString(android.R.string.yes),
                                getString(android.R.string.no),
                                () -> deleteLocalSurah(temp, holder), null);
                        }
                    });
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
                    holder.image.setClickable(false);
                    holder.title.setClickable(false);
                    holder.cardContent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if( ISDonwloading!=true)
                                for (int i=0;i< listrecitesAya.size();i++) {
                                    if(listrecitesAya.get(i).RealName.equals(temp.RealName)){
                                        RecitesAYA=String.valueOf(i);
                                        surahName = listrecitesAya.get(i).RealName;
                                        AyaNameView = holder.title;
                                        AyaImage = holder.image;
                                        DisplayAya();
                                        break;
                                    }
                                }
                        }
                    });

                    // Quick long-press (200ms) to enter selection mode (only for non-downloaded surahs)
                    if (!isDownloaded) {
                        holder.cardContent.setOnTouchListener(new View.OnTouchListener() {
                            private Handler handler = new Handler();
                            private boolean longPressTriggered = false;
                            private Runnable longPressRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    longPressTriggered = true;
                                    if (!selectionMode) {
                                        enterSelectionMode(postion);
                                    }
                                }
                            };

                            @Override
                            public boolean onTouch(View v, android.view.MotionEvent event) {
                                switch (event.getAction()) {
                                    case android.view.MotionEvent.ACTION_DOWN:
                                        longPressTriggered = false;
                                        v.setPressed(true);
                                        handler.postDelayed(longPressRunnable, 200);
                                        break;
                                    case android.view.MotionEvent.ACTION_UP:
                                        handler.removeCallbacks(longPressRunnable);
                                        if (!longPressTriggered) {
                                            v.performClick();
                                        }
                                        v.setPressed(false);
                                        break;
                                    case android.view.MotionEvent.ACTION_CANCEL:
                                        handler.removeCallbacks(longPressRunnable);
                                        v.setPressed(false);
                                        break;
                                    case android.view.MotionEvent.ACTION_MOVE:
                                        break;
                                }
                                return true;
                            }
                        });
                        holder.cardContent.setOnLongClickListener(null);
                    } else {
                        holder.cardContent.setOnTouchListener(null);
                        holder.cardContent.setOnLongClickListener(null);
                    }
                }

//            budownload.setText(getResources().getString(R.string.downlaod));
                holder.title.setText(temp.RealName);
                holder.cost.setText(temp.StateName);// it updated

                // Design-C medallion number + ayah-count chip (List mode only; null in grid/compact).
                if (holder.surahNumber != null || holder.chipAyah != null) {
                    int surahNum = -1;
                    try { surahNum = Integer.parseInt(temp.ServerName.trim()); }
                    catch (Exception ignored) {}
                    if (holder.surahNumber != null) {
                        if (surahNum > 0) {
                            // Localize digits to match the (locale-formatted) ayah chip — Arabic-Indic in AR.
                            Locale numLocale = (SettingSaved.LanguageSelect == 2) ? Locale.ENGLISH : new Locale("ar");
                            holder.surahNumber.setText(String.format(numLocale, "%d", surahNum));
                        } else {
                            holder.surahNumber.setText("");
                        }
                    }
                    if (holder.chipAyah != null) {
                        if (surahNum >= 1 && surahNum <= 114) {
                            holder.chipAyah.setVisibility(View.VISIBLE);
                            holder.chipAyah.setText(getString(R.string.ayah_count_chip, AYAH_COUNTS[surahNum]));
                        } else {
                            holder.chipAyah.setVisibility(View.GONE);
                        }
                    }
                }

                if (isDownloaded) {
                    holder.statusIcon.setImageResource(R.drawable.ic_offline);
                    holder.statusIcon.setVisibility(View.VISIBLE);
                } else {
                    holder.statusIcon.setImageResource(R.drawable.ic_streaming);
                    holder.statusIcon.setVisibility(View.VISIBLE);
                }
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
            TextView surahNumber ;   // design-C medallion (List mode only)
            TextView chipAyah ;      // design-C ayah-count chip (List mode only)
            ImageView image ;
            ImageButton budownload ;
            ImageButton ShareButton ;
            ImageButton budelete ;
            ImageView statusIcon ;
            CardView cardview;
            CardView cardContent;
            CheckBox selectCheckBox;

            public ViewHolder(View itemView) {
                super(itemView);


                 title=(TextView) itemView.findViewById( R.id.textView1);
                 cost=(TextView) itemView.findViewById( R.id.textView2);
                 // Present only in the List layout (single_rowayalist); null in grid/compact.
                 surahNumber=(TextView) itemView.findViewById( R.id.surahNumber);
                 chipAyah=(TextView) itemView.findViewById( R.id.chipAyah);
                 image =(ImageView) itemView.findViewById( R.id.imageView);
                 budownload =(ImageButton) itemView.findViewById( R.id.button);
                 ShareButton =(ImageButton) itemView.findViewById( R.id.buttonShare);
                 budelete =(ImageButton) itemView.findViewById( R.id.buttonDelete);
                 statusIcon =(ImageView) itemView.findViewById( R.id.statusIcon);
                 cardview =(CardView) itemView.findViewById( R.id.cardviewad);
                 cardContent =(CardView) itemView.findViewById( R.id.cardContent);
                 selectCheckBox =(CheckBox) itemView.findViewById( R.id.selectCheckBox);

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
