package com.medoapps.www.onlinequran;

import static com.medoapps.www.onlinequran.R.id.adView;
import static com.medoapps.www.onlinequran.SettingSaved.AppVersion;
import static com.medoapps.www.onlinequran.SettingSaved.userubdated;
import static com.medoapps.www.onlinequran.SettingSaved.userubdated2;
import static com.medoapps.www.onlinequran.SettingSaved.userubdated3;
import static java.text.DateFormat.getDateTimeInstance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.medoapps.www.onlinequran.classes.YouTubeConfig;
import com.medoapps.www.onlinequran.hashimyoutubeplayer.YoutubePlayerViewActivity;
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.models.Report;
import com.medoapps.www.onlinequran.models.ReportType;
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.models.UserTypes;
import com.medoapps.www.onlinequran.service.ReportService;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import io.supercharge.shimmerlayout.ShimmerLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//import com.google.android.gms.ads.InterstitialAd;
//import com.google.android.gms.ads.reward.RewardItem;
//import com.google.android.gms.ads.reward.RewardedVideoAd;
//import com.google.android.gms.ads.reward.RewardedVideoAdListener;


public class RecitesName extends Fragment  {
    //banner add
    private static final String TAG = "RecitesName";
    private AdView mAdView;
    //private InterstitialAd mInterstitialAd;
    //private RewardedVideoAd mAd;
    static RecitesName instance4;
    private PreferenceManager prefManager;
    public static final String PAGE_TITLE = "Tab2";

    TextView recitelisttxt;
    Boolean new_ubdate =false;
    FirebaseRemoteConfig mFirebaseRemoteConfig;



    public ArrayList<AuthorClass> listrecites = new ArrayList<AuthorClass>();
    private List<Object> recyclerViewItems = new ArrayList<>();

    RecyclerView lVRecites;
    ShimmerLayout reciterLoader;

    private ImageButton toggleViewBTN;
    private static final int VIEW_MODE_LIST = 0;
    private static final int VIEW_MODE_GRID = 1;
    private static final int VIEW_MODE_COMPACT = 2;
    private int viewMode = VIEW_MODE_LIST;
    private static final String PREF_VIEW_MODE = "reciter_view_mode";

    String RecitesName="";
    String Rewayat="";
    String RealRecitesName="";
    SearchView searchView;


    View SharedView;
    // Remote Config keys
    private static final String LOADING_PHRASE_CONFIG_KEY = "loading_phrase";
    private static final String urlUpdate = "url";
    private static final String chicUpdate = "new_ubdate";
    private static final String new_ubdate2 = "new_ubdate2";
    private static final String url2 = "url2";
    private static final String url3 = "url3";
    private static final String new_ubdate3 = "new_ubdate3";
    private static final String ubdated_version = "ubdated_version";

    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_USER_KEY = "user_key";

    List<Object> tempItems = new ArrayList<>();
    private Long mLastKey;

    public static final int ITEMS_PER_AD = 20;

    private User CurrentUser;
    private DatabaseReference mDatabase;
    private DatabaseReference mUserReference;
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/4177191030";
    MainActivity MainActivityInstance;
    private boolean isSubscribedPremium ;

    public static RecitesName newInstance() {
        RecitesName fragment = new RecitesName();
        return fragment;
    }

    public RecitesName() {

    }

    public RecitesName(MainActivity MainActivityInstance) {
        this.MainActivityInstance = MainActivityInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_recites_name, container, false);
    }

    public void searchManager(){
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) getView().findViewById(R.id.search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                recitelisttxt.setVisibility(View.VISIBLE);
                toggleViewBTN.setVisibility(View.VISIBLE);
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recitelisttxt.setVisibility(View.GONE);
                toggleViewBTN.setVisibility(View.GONE);
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
//                ArrayList<AuthorClass> listrecitestemp = new ArrayList<AuthorClass>();
                List<Object> recyclerViewItemstemp = new ArrayList<>();
                for (Object listrecitesitem : recyclerViewItems) {
                    try {
                        AuthorClass kk = (AuthorClass) listrecitesitem;

                        if (kk.RealName.contains(newText)) {
                            recyclerViewItemstemp.add(listrecitesitem);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                int tempItemsIndex = 0;
                for (int i = 2; i <= recyclerViewItemstemp.size(); i += 15) {

                    if (tempItemsIndex < tempItems.size())
                        recyclerViewItemstemp.add(i, tempItems.get(tempItemsIndex));
                    tempItemsIndex = tempItemsIndex+1;

                }
                addBannerAds(recyclerViewItemstemp);
                loadBannerAds(recyclerViewItemstemp);
                lVRecites.setAdapter(new MyListAdapter(recyclerViewItemstemp));
                return false;
            }
        });
    }
    public void getCurrentUser(){
        /*DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("users/" + getUid())) {
//                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());



                    return;
                } else {


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        ValueEventListener userListener = new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                try {
                    User user = dataSnapshot.getValue(User.class);
                    CurrentUser = user;
                    // disable youtube posts
                    //loadSomeYoutubePosts(9);
                    loadAllBannerAds();
//                    isSubscribedPremium = user.isSubscribedPremium;
                    if (user.isSubscribedPremium != null && user.isSubscribedPremium == false){

                        isSubscribedPremium = false;

                    }else if(user.isSubscribedPremium == null){

                        isSubscribedPremium = false;

                    }else{
                        isSubscribedPremium = true;

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SettingSaved settingSaved=new SettingSaved(getContext());
        settingSaved.LoadData();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(getUid());
        SeparateFunctions separateFunctions = new SeparateFunctions(getContext());
        if (separateFunctions.isNetworkAvailable()){
            getCurrentUser();
        }



/*
        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }*/
        instance4 =this;
        reciterLoader = (ShimmerLayout) getView().findViewById(R.id.reciterSkeletonLoader);
        reciterLoader.startShimmerAnimation();
        recitelisttxt = (TextView) getView().findViewById(R.id.recitelisttxt);

        viewMode = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt(PREF_VIEW_MODE, VIEW_MODE_LIST);
        toggleViewBTN = (ImageButton) getView().findViewById(R.id.toggleViewBTN);
        toggleViewBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showViewModeMenu(v);
            }
        });

        searchManager();


        chicLastRecite();


        //load interstial ad by atimer
        //prepareAd();

       /* ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            public void run() {
                Log.i("hello", "world");
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Log.d("TAG"," Interstitial not loaded");
                        }

                        prepareAd();


                    }
                });

            }
        }, 1, 1, TimeUnit.HOURS);
*/
        //start app in background
        //startService(new Intent(this, RunBackground.class));

        loadad();//to load ads full screen

        SettingSaved.IsOpen =  1;//App Is Opened
        SettingSaved.SounlLoad=1;//sound load
        //load banner ad
        mAdView = (AdView) getView().findViewById(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }



        });

        lVRecites = (RecyclerView) getView().findViewById(R.id.recyclerView);
        LnaguageClass lc = new LnaguageClass();
        listrecites = lc.AutherList();

        for (int i = 0; i < listrecites.size(); ++i) {

            AuthorClass temp = listrecites.get(i);

            recyclerViewItems.add(temp);
        }

//        addBannerAds(recyclerViewItems);
//        loadBannerAds(recyclerViewItems);

        lVRecites.setHasFixedSize(false);
        applyLayoutMode();

        reciterLoader.stopShimmerAnimation();
        reciterLoader.setVisibility(View.GONE);

        if(SettingSaved.OnTimeAds==false) {
            if(SettingSaved.IsRated==1){
                SettingSaved.OnTimeAds=true;
            }
        }
//        loadSomeYoutubePosts(7);
    }

    private void showViewModeMenu(View anchor) {
        PopupMenu popup = new PopupMenu(getContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_view_mode, popup.getMenu());

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
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit().putInt(PREF_VIEW_MODE, viewMode).apply();
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
            lVRecites.animate().alpha(0f).setDuration(150).withEndAction(new Runnable() {
                @Override
                public void run() {
                    swapLayoutManager();
                    lVRecites.animate().alpha(1f).setDuration(150).start();
                }
            }).start();
        } else {
            swapLayoutManager();
        }
    }

    private void swapLayoutManager() {
        if (viewMode == VIEW_MODE_GRID) {
            GridLayoutManager glm = new GridLayoutManager(getContext(), 2);
            glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (position < recyclerViewItems.size()) {
                        Object item = recyclerViewItems.get(position);
                        if (item instanceof AdView || item instanceof Post)
                            return 2;
                    }
                    return 1;
                }
            });
            lVRecites.setLayoutManager(glm);
        } else {
            lVRecites.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        lVRecites.setAdapter(new MyListAdapter(recyclerViewItems, viewMode));
    }

    void loadAllBannerAds(){
        addBannerAds(recyclerViewItems);
        loadBannerAds(recyclerViewItems);
        swapLayoutManager();
    }
    void loadSomeYoutubePosts(int mPosts){
        Query ref = FirebaseDatabase.getInstance().getReference()
                .child("youtube-posts").orderByKey()
                .limitToLast(mPosts);


        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {



                Post post = dataSnapshot.getValue(Post.class);


                tempItems.add(post);
                if (tempItems.size() == mPosts) {
                    Post tempPost = (Post) tempItems.get(0);
                    int tempItemsIndex = 0;
                    mLastKey = tempPost.createdAt;

                    for (int i = 2; i <= recyclerViewItems.size(); i += 15) {

                        if (tempItemsIndex < mPosts) {
                            recyclerViewItems.add(i, tempItems.get(tempItemsIndex));
                            tempItemsIndex = tempItemsIndex + 1;
                        }

                    }


                    /*addBannerAds(recyclerViewItems);
                    loadBannerAds(recyclerViewItems);
                    lVRecites.setAdapter(new MyListAdapter(recyclerViewItems));*/
                    loadAllBannerAds();

                }

            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{
        private List<Object> listrecitesLocalobject;
        private int viewMode;

        private static final int VIEW_TYPE_AD = 0;
        private static final int VIEW_TYPE_YOUTUBE = 1;
        private static final int VIEW_TYPE_RECITER_LIST = 2;
        private static final int VIEW_TYPE_RECITER_GRID = 3;
        private static final int VIEW_TYPE_RECITER_COMPACT = 4;


        YouTubeThumbnailView youTubeThumbnailView ;
        // RecyclerView recyclerView;


        public MyListAdapter(List<Object> recyclerViewItems) {
            this(recyclerViewItems, VIEW_MODE_LIST);
        }

        public MyListAdapter(List<Object> recyclerViewItems, int viewMode) {
            this.listrecitesLocalobject = recyclerViewItems;
            this.viewMode = viewMode;
        }

        private void onShareYoutubeBy( String videoTitle,String videoDescriptiontxt,String YouTubeVideoId,String Thumb_Url, String postId, String postTitle) {


            Uri imageUri = Uri.parse(Thumb_Url);
            String title = videoTitle;
            String description = videoDescriptiontxt;
            if (description.length() > 40)
                description = description.substring(0, 39) + "...";


            Log.d(TAG, "createDynamicLink 2: " +title);
            Log.d(TAG, "createDynamicLink 2: " +description);
            Log.d(TAG, "createDynamicLink 2: " +"watch/"+YouTubeVideoId);

            SeparateFunctions separateFunctions = new SeparateFunctions(getActivity());
            separateFunctions.createDynamicLink(getActivity(),"watch/"+YouTubeVideoId,title,description,imageUri).addOnCompleteListener(getActivity(), new OnCompleteListener<ShortDynamicLink>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<ShortDynamicLink> task) {
                    if (task.isSuccessful()) {
                        // Short link created
                        Uri shortLink = task.getResult().getShortLink();
                        Uri flowchartLink = task.getResult().getPreviewLink();
                        Log.d(TAG, "createDynamicLink 2: " +shortLink);

                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");

                        String shareBody = "";
                        shareBody = shortLink.toString();
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Stream");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));

                        createYouTubePostReport(shortLink.toString(),postId,postTitle);


                    } else {
                        Log.d(TAG, "createDynamicLink 2: " +task);
                        // Error
                        // ...
                    }
                }

            });


        }

        private void onShareBy( String soura,String reciter,String ServerName) {

            Uri imageUri = Uri.parse(getString(R.string.dynamicLinkShareImage));
            String title = "";
            String description = "";
            if (SettingSaved.LanguageSelect == 2){
                String englishTitle = new SeparateFunctions(getContext()).getLocaleStringResource(new Locale("en"), R.string.sharePart3, getContext());
                String englishDescription = new SeparateFunctions(getContext()).getLocaleStringResource(new Locale("en"), R.string.dynamicLinkShareTitle, getContext());

                title = englishTitle + soura ;
                description = englishDescription;
            }else {
                String arabicTitle = new SeparateFunctions(getContext()).getLocaleStringResource(new Locale("ar"), R.string.sharePart3, getContext());
                String arabicDescription = new SeparateFunctions(getContext()).getLocaleStringResource(new Locale("ar"), R.string.dynamicLinkShareTitle, getContext());

                title = arabicTitle +" "+ soura  ;
                description = arabicDescription;
            }
            SeparateFunctions separateFunctions = new SeparateFunctions(getActivity());
            separateFunctions.createDynamicLink(getActivity(),"reciter/"+ServerName,title,description,imageUri).addOnCompleteListener(getActivity(), new OnCompleteListener<ShortDynamicLink>() {
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
                            shareBody = getResources().getString(R.string.sharePart1) +" " +soura+ " "+ "."+ " "+ getResources().getString(R.string.sharePart4) + " " + shortLink.toString();
//                            shareBody = getResources().getString(R.string.sharePart1) +" " +soura+ " "+ "."+ " "+ getResources().getString(R.string.sharePart4) + " " + getResources().getString(R.string.shareURL);

                        }else{
                            shareBody = getResources().getString(R.string.sharePart1) +" " +soura+ " " +"."+ " "+ getResources().getString(R.string.sharePart4) + " " + shortLink.toString();

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
        @Override
        public int getItemViewType(int position) {
            Object item = listrecitesLocalobject.get(position);
            if (item instanceof AdView) {
                return VIEW_TYPE_AD;
            } else if (item instanceof Post) {
                return VIEW_TYPE_YOUTUBE;
            }
            switch (viewMode) {
                case VIEW_MODE_GRID: return VIEW_TYPE_RECITER_GRID;
                case VIEW_MODE_COMPACT: return VIEW_TYPE_RECITER_COMPACT;
                default: return VIEW_TYPE_RECITER_LIST;
            }
        }

        @Override
        public MyListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            int layoutRes;
            switch (viewType) {
                case VIEW_TYPE_RECITER_GRID:
                    layoutRes = R.layout.recites_ticket_grid;
                    break;
                case VIEW_TYPE_RECITER_COMPACT:
                    layoutRes = R.layout.recites_ticket_compact;
                    break;
                default:
                    layoutRes = R.layout.recites_ticket;
                    break;
            }
            View listItem = layoutInflater.inflate(layoutRes, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);

            return viewHolder;
        }

        public  String getTimeDate(long timestamp){
            try{
                DateFormat dateFormat = getDateTimeInstance();
                Date netDate = (new Date(timestamp));
                return dateFormat.format(netDate);
            } catch(Exception e) {
                return "date";
            }
        }
        @Override
        public void onBindViewHolder(MyListAdapter.ViewHolder holder, int  position) {
            // Reset itemView to full size in case it was previously collapsed (e.g. unloaded ad)
            holder.itemView.setVisibility(View.VISIBLE);
            RecyclerView.LayoutParams resetLp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            resetLp.height = RecyclerView.LayoutParams.WRAP_CONTENT;
            resetLp.width = RecyclerView.LayoutParams.MATCH_PARENT;
            holder.itemView.setLayoutParams(resetLp);

            try {
                if (listrecitesLocalobject.get(position) instanceof AdView){
                    AdView adView = (AdView) listrecitesLocalobject.get(position);

                    if(adView.getParent() != null) {
                        ((ViewGroup)adView.getParent()).removeView(adView);
                    }
                    holder.cardview.addView(adView);
                    holder.cardContent.setVisibility(View.GONE);
                    holder.youtubeCardContent.setVisibility(View.GONE);

                    if (Boolean.TRUE.equals(adView.getTag())) {
                        holder.cardview.setVisibility(View.VISIBLE);
                        holder.itemView.setVisibility(View.VISIBLE);
                        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                        lp.height = RecyclerView.LayoutParams.WRAP_CONTENT;
                        lp.width = RecyclerView.LayoutParams.MATCH_PARENT;
                        holder.itemView.setLayoutParams(lp);
                    } else {
                        holder.cardview.setVisibility(View.GONE);
                        holder.itemView.setVisibility(View.GONE);
                        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                        lp.height = 0;
                        lp.width = 0;
                        lp.setMargins(0, 0, 0, 0);
                        holder.itemView.setLayoutParams(lp);
                    }
                }else if (listrecitesLocalobject.get(position) instanceof Post)
                {
                    holder.cardview.setVisibility(View.GONE);
                    holder.cardContent.setVisibility(View.GONE);
                    holder.youtubeCardContent.setVisibility(View.VISIBLE);



                    Post model = new Post();
                    model = (Post) listrecitesLocalobject.get(position);



                        if (model.createdAt != null){
                            Log.d(TAG, "onBindViewHolder: " + getTimeDate(model.createdAt));

                        }
                        final String userKEY = String.valueOf(model.uid);
                        //attach_url = model.attachment;
                        //initializePlayer();
                        // Set click listener for the whole post view

                        final String postKey = model.id;
                        Post finalModel = model;
                        holder.EntireLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                // Count View
                                DatabaseReference globalPostRef = mDatabase.child("youtube-posts").child(finalModel.id);
                                DatabaseReference userPostRef = mDatabase.child("user-posts").child(finalModel.uid).child(finalModel.id);
                                int currentCount = Integer.parseInt(holder.Views.getText().toString());
                                holder.Views.setText( String.valueOf(currentCount + 1));
                                // Run two transactions
                                onViewClicked(globalPostRef);
                                onViewClicked(userPostRef);


                                // Launch PostDetailActivity
                        /*Intent intent = new Intent(getContext(), PostDetailActivity.class);
                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                        intent.putExtra(PostDetailActivity.EXTRA_USER_KEY, userKEY);
                        startActivity(intent);*/


                                Intent intent = new Intent(getContext(), YoutubePlayerViewActivity.class);
                                intent.putExtra(EXTRA_POST_KEY, postKey);
                                intent.putExtra(EXTRA_USER_KEY, userKEY);
                                intent.putExtra("videoId", finalModel.YouTubeVideoId);
                                intent.putExtra("videoTitle", holder.title);
                                intent.putExtra("videoDescription", holder.descriptiontxt);
                                startActivity(intent);
                                if (MainActivityInstance!=null){
                                    MainActivityInstance.switchToYoutubeFragment();

                                }
                                createYouTubePostReport(null,postKey,holder.title);

                            }
                        });

                        // Determine if the current user has liked this post and set UI accordingly
                        if (model.stars != null && model.stars.containsKey(getUid())) {
                            holder.starView.setImageResource(R.drawable.ic_toggle_star_24);
                        } else {
                            holder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                        }
                        // Determine if the current user has ability to delete and edit the post
                        if (userKEY.equals(getUid()) || CurrentUser.UserType != null && CurrentUser.UserType == UserTypes.Admin) {
                            holder.optionView.setVisibility(View.VISIBLE);
                        } else {
                            holder.optionView.setVisibility(View.GONE);
                        }
                        // Bind Post to ViewHolder, setting OnClickListener for the star button
                        holder.bindToPost(model, new View.OnClickListener() {
                            @Override
                            public void onClick(View starView) {
                                // Need to write to both places the post is stored
                                DatabaseReference globalPostRef = mDatabase.child("youtube-posts").child(finalModel.id);
                                DatabaseReference userPostRef = mDatabase.child("user-posts").child(finalModel.uid).child(finalModel.id);



                                // Run two transactions
                                onStarClickedWithViewHolder(globalPostRef,holder,postKey,holder.title,false);
                                onStarClickedWithViewHolder(userPostRef, holder,postKey,holder.title,true);
                            }


                        },new View.OnClickListener() {
                            @Override
                            public void onClick(View shareView) {
                                // Need to write to both places the post is stored

                                onShareYoutubeBy(holder.title,finalModel.body,finalModel.YouTubeVideoId,finalModel.Thumb_Url,postKey,holder.title);
                            }


                        }, new View.OnClickListener() {
                            @Override
                            public void onClick(View optionView) {
                                // Need to write to both places the post is stored
                                DatabaseReference globalPostRef = mDatabase.child("youtube-posts").child(finalModel.id);
                                DatabaseReference userPostRef = mDatabase.child("user-posts").child(finalModel.uid).child(finalModel.id);

                                PopupMenu popup = new PopupMenu(getContext(), optionView);
                                MenuInflater inflater = popup.getMenuInflater();
                                inflater.inflate(R.menu.post_option, popup.getMenu());
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.post_edit:

                                                // Launch PostDetailActivity

                                        /*Intent intent = new Intent(getContext(), EditPost.class);
                                        intent.putExtra(EditPost.EXTRA_POST_KEY, postKey);
                                        intent.putExtra(EditPost.EXTRA_USER_KEY, userKEY);
                                        startActivity(intent);*/

                                                break;

                                            case R.id.post_delete:

                                                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("youtube-posts").child(finalModel.id);
                                                rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        dataSnapshot.getRef().removeValue();

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                                DatabaseReference rootRef2 = FirebaseDatabase.getInstance().getReference().child("user-posts").child(finalModel.uid).child(finalModel.id);
                                                rootRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        dataSnapshot.getRef().removeValue();

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                                try {
                                                    Map<String, Object> childUpdates = new HashMap<>();
                                                    childUpdates.remove("/youtube-posts/" +postKey );
                                                    childUpdates.remove("/user-posts/" + userKEY + "/" + postKey);
                                                    mDatabase.updateChildren(childUpdates);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                break;

                                            default:
                                                break;

                                        }
                                        return true;
                                    }
                                });

                                popup.show();
                            }
                        });

                        if (model.title != null){
                            holder.title = model.title;
                            holder.descriptiontxt = model.body;
                            holder.textView.setText(model.title);
                            holder.description.setText(model.body);

                            try {
                                Glide.with(getContext())
                                        .load(model.Thumb_Url)
                                        .into(holder.imageView);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }else
                        {
                            //set title and description of youtube video
                            RequestQueue queue = Volley.newRequestQueue(getContext());
                            String url = "https://www.googleapis.com/youtube/v3/videos?part=id%2C+snippet&id="+model.YouTubeVideoId+"&key=" + YouTubeConfig.getInfoAPI_KEY();
                            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                    new com.android.volley.Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            JSONObject snippet= null;
                                            JSONObject thumbnails= null;
                                            try {
                                                snippet = new JSONObject(response.toString())
                                                        .getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
                                                thumbnails = new JSONObject(response.toString())
                                                        .getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high");

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            try {
                                                String thumbnailsHigh=thumbnails.getString("url");
                                                holder.title = snippet.getString("title");
                                                holder.descriptiontxt = snippet.getString("description");
                                                holder.textView.setText(snippet.getString("title"));
                                                holder.description.setText(snippet.getString("description"));

                                                try {
                                                    Glide.with(getContext())
                                                            .load(thumbnailsHigh)
                                                            .into(holder.imageView);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            // Display the first 500 characters of the response string.
//                        textView.setText("Response is: " + response.substring(0,500));
                                        }
                                    }, new com.android.volley.Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                }
                            });
                            // Add the request to the RequestQueue.
                            queue.add(stringRequest);
                        }




                }
                else{
                    AuthorClass temp = (AuthorClass) listrecitesLocalobject.get(position);


                    holder.cardview.setVisibility(View.GONE);
                    holder.cardContent.setVisibility(View.VISIBLE);
                    holder.youtubeCardContent.setVisibility(View.GONE);

                    LnaguageClass lc = new LnaguageClass(getContext());
                    holder.txtRecitesName = lc.SetTextFont(holder.txtRecitesName,"");
                    holder.txtRecitesName.setText(temp.RealName);

                    holder.txtRecitesName.setSelected(true);
                    holder.entireCard.setLayoutDirection(SettingSaved.LanguageSelect==1?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
                    holder.cardContent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            RecitesName = temp.ServerName;
                            Rewayat = temp.Rewayat;
                            RealRecitesName = temp.RealName;
                            // String welcomes = listrecitesitem.ServerName;
                            SharedView = holder.txtRecitesName;
                            DisplayAya();
                        }
                    });

                    holder.buttonShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onShareBy(temp.RealName,RealRecitesName,temp.ServerName);

                        }
                    });
                }

            } catch (Exception e) {

                e.printStackTrace();
            }

        }
        public  class ViewHolder extends RecyclerView.ViewHolder {



            TextView txtRecitesName ;
            ImageButton buttonShare ;
            ImageButton buttonSharevideo ;
            LinearLayout entireCard ;
            CardView cardview;
            CardView cardContent;
            CardView youtubeCardContent;



            //for youtubePost

            public TextView titleView;
            public TextView authorView;
            public TextView Views;
            public ImageView starView;
            public RelativeLayout EntireLayout;
            public RelativeLayout star_layout;
            public RelativeLayout share_layout;
            public ImageView optionView;
            public TextView numStarsView;
            public TextView bodyView;
            public ImageView pPictureView;
            public ImageView ThumbImage;

            public ImageView imageView;
            public TextView textView;
            public TextView description;
            String title = "";
            String descriptiontxt = "";
            //private PlayerView playerView;
            //private SimpleExoPlayer player;
            private String attach_url=null;

            private long playbackPosition;
            private int currentWindow;
            private boolean playWhenReady = false;

            private Button refresh;
            private Button btn_SHOWAD;
            private CheckBox startVideoAdsMuted;
            private TextView videoStatus;
            private ImageButton closeAd;
            private LinearLayout AdContainer;
            private NativeAd nativeAd;;

            FrameLayout frameLayout ;

            public ViewHolder(View itemView) {
                super(itemView);



                 txtRecitesName = (TextView) itemView.findViewById(R.id.txtRecitesName);
                 buttonShare = (ImageButton) itemView.findViewById(R.id.buttonShare);
                 entireCard = (LinearLayout) itemView.findViewById(R.id.entireCard);
                cardview = (CardView) itemView.findViewById(R.id.cardviewad);
                cardContent = (CardView) itemView.findViewById(R.id.cardContent);
                youtubeCardContent = (CardView) itemView.findViewById(R.id.youtubeCardContent);


                frameLayout = itemView.findViewById(R.id.fl_adplaceholder);


                titleView = itemView.findViewById(R.id.post_title);
                authorView = itemView.findViewById(R.id.post_author);
                starView = itemView.findViewById(R.id.star);
                EntireLayout = itemView.findViewById(R.id.EntireLayout);
                star_layout = itemView.findViewById(R.id.star_layout);
                share_layout = itemView.findViewById(R.id.share_layout);
                optionView = itemView.findViewById(R.id.option);
                numStarsView = itemView.findViewById(R.id.post_num_stars);
                bodyView = itemView.findViewById(R.id.post_body);
                pPictureView = itemView.findViewById(R.id.post_author_photo);
                //playerView = itemView.findViewById(R.id.video_view);

                ThumbImage = itemView.findViewById(R.id.Thumb_Image);
                Views = itemView.findViewById(R.id.views);

                imageView = (ImageView) itemView.findViewById(R.id.imageView5);
                textView = (TextView) itemView.findViewById(R.id.textView13);
                description = (TextView) itemView.findViewById(R.id.description);
                refresh = itemView.findViewById(R.id.btn_refresh);
                btn_SHOWAD = itemView.findViewById(R.id.btn_SHOWAD);
                closeAd = itemView.findViewById(R.id.closeAd);
                AdContainer = itemView.findViewById(R.id.AdContainer);
                startVideoAdsMuted = itemView.findViewById(R.id.cb_start_muted);
                videoStatus = itemView.findViewById(R.id.tv_video_status);

            }
            public void bindToPost(Object model, View.OnClickListener starClickListener, View.OnClickListener shareClickListener ,View.OnClickListener optionClickListener ) {
                User user = new User();
                //utils = new Utilities();


                Post post = (Post) model;
                String u = post.profilePhoto;
                String u2 = "https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/photos%2Fimage%3A10205?alt=media&token=52f537ce-fb27-4aaa-9ba5-412b9e71d7f5";
                titleView.setText(post.title);
                authorView.setText(post.author);
                numStarsView.setText(String.valueOf(post.starCount));
                Views.setText(String.valueOf(post.viewCount));
                bodyView.setText(post.body);


                try {
                    Glide.with(itemView.getContext()).load(u).into(pPictureView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                attach_url = post.attachment;
                String h = post.Thumb_Url;
                try {
                    Glide.with(itemView.getContext()).load(h).into(ThumbImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                star_layout.setOnClickListener(starClickListener);
                share_layout.setOnClickListener(shareClickListener);

                optionView.setOnClickListener(optionClickListener);
            }

        }

        @Override
        public int getItemCount() {
            return listrecitesLocalobject.size();
        }

    }
    private void onStarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.stars.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    p.starCount = p.starCount - 1;
                    p.stars.remove(getUid());
                } else {
                    // Star the post and add self to stars
                    p.starCount = p.starCount + 1;
                    p.stars.put(getUid(), true);
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
    private void onStarClickedWithViewHolder(DatabaseReference postRef, MyListAdapter.ViewHolder viewHolder,String postId, String postTitle , Boolean set) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.stars.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    p.starCount = p.starCount - 1;
                    p.stars.remove(getUid());

                    viewHolder.numStarsView.setText(String.valueOf(p.starCount));
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                    if (set == true){
                        createYouTubePostReportStarUnStar(postId,postTitle,false);

                    }

                } else {
                    // Star the post and add self to stars
                    p.starCount = p.starCount + 1;
                    p.stars.put(getUid(), true);
//                    int currentCount = Integer.parseInt(viewHolder.numStarsView.getText().toString());
                    viewHolder.numStarsView.setText(String.valueOf(p.starCount));
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
                    if (set == true){
                        createYouTubePostReportStarUnStar(postId,postTitle,true);

                    }
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



    // [END post_stars_transaction]
    // [START post_stars_transaction]
    private void onViewClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                p.viewCount = p.viewCount + 1;

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

    public String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser()!= null){

            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }else {
            return "guest_mode";
        }
    }
    /**
     * Adds banner ads to the items list.
     * @param recyclerViewItems
     */
    private void addBannerAds(List<Object> recyclerViewItems) {
        if (isSubscribedPremium)
            return;

        // In grid mode (2 columns), ads must land after an even number of
        // single-span items so every row is complete before the full-width ad.
        int startPos = 5;
        int adInterval = ITEMS_PER_AD;
        if (viewMode == VIEW_MODE_GRID) {
            if (startPos % 2 != 0) startPos++;       // 5 → 6
            if ((adInterval - 1) % 2 != 0) adInterval++; // 20 → 21 (20 reciters between ads)
        }

        // Loop through the items array and place a new banner ad in every ith position in
        // the items List.
        for (int i = startPos; i <= recyclerViewItems.size(); i += adInterval) {
            try {
                final AdView adView = new AdView(requireContext());
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
        if (isSubscribedPremium)
            return;
        // Load the first banner ad in the items list (subsequent ads will be loaded automatically
        // in sequence).
        loadBannerAd(5,recyclerViewItems);
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
            return;
            /*throw new ClassCastException("Expected item at index " + index + " to be a banner ad"
                    + " ad.");*/
        }

        final AdView adView = (AdView) item;

        // Set an AdListener on the AdView to wait for the previous banner ad
        // to finish loading before loading the next ad in the items list.
        adView.setAdListener(
                new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        adView.setTag(true);
                        try {
                            if (lVRecites != null && lVRecites.getAdapter() != null) {
                                int pos = recyclerViewItems.indexOf(adView);
                                if (pos >= 0) {
                                    lVRecites.getAdapter().notifyItemChanged(pos);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
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

    /**
     * Display a welcome message in all caps if welcome_message_caps is set to true. Otherwise,
     * display a welcome message as fetched from welcome_message.
     */
    // [START display_welcome_message]
    private void displayWelcomeMessage() {
        // [START get_config_values]
        final String welcomeMessage = mFirebaseRemoteConfig.getString(urlUpdate);
        // [END get_config_values]
        Boolean up = mFirebaseRemoteConfig.getBoolean(chicUpdate);
        final String Url2 = mFirebaseRemoteConfig.getString(url2);
        // [END get_config_values]
        Boolean chickUpdate2 = mFirebaseRemoteConfig.getBoolean(new_ubdate2);

        final String Url3 = mFirebaseRemoteConfig.getString(url3);
        // [END get_config_values]
        Boolean chickUpdate3 = mFirebaseRemoteConfig.getBoolean(new_ubdate3);
        final String ubdatedVersion = mFirebaseRemoteConfig.getString(ubdated_version);
        if (up==true) {
            if(userubdated==false){

                //chec for ubdate
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

                // Setting Dialog Title
                alertDialog.setTitle(R.string.new_ubdate);

                // Setting Dialog Message
                alertDialog.setMessage(R.string.newVersion);

                // Setting Icon to Dialog
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);

                // Setting Positive "Yes" Button
                alertDialog.setPositiveButton(R.string.ubdate, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {

                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(welcomeMessage));
                        startActivity(browserIntent);

                        SettingSaved.userubdated=true;
                        SettingSaved settingSaved=new SettingSaved(getActivity());
                        settingSaved.SaveData();
                        //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                    }
                });

                // Setting Negative "NO" Button
                alertDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event

                        dialog.cancel();
                    }
                });

                // Showing Alert Message
                alertDialog.show();


            }



            //mWelcomeTextView.setAllCaps(true);
        } else {
            //mWelcomeTextView.setAllCaps(false);
        }

        if (chickUpdate2==true) {
            if(userubdated2==false){

                //chec for ubdate
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

                // Setting Dialog Title
                alertDialog.setTitle(R.string.new_ubdate);

                // Setting Dialog Message
                alertDialog.setMessage(R.string.newVersion);

                // Setting Icon to Dialog
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);

                // Setting Positive "Yes" Button
                alertDialog.setPositiveButton(R.string.ubdate, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {

                        // Write your code here to invoke YES event
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Url2));
                        startActivity(browserIntent);

                        SettingSaved.userubdated2=true;
                        SettingSaved settingSaved=new SettingSaved(getActivity());
                        settingSaved.SaveData();
                        //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                    }
                });

                // Setting Negative "NO" Button
                alertDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event

                        dialog.cancel();
                    }
                });

                // Showing Alert Message
                alertDialog.show();


            }



            //mWelcomeTextView.setAllCaps(true);
        } else {
            //mWelcomeTextView.setAllCaps(false);
        }
        if (chickUpdate3==true) {
            if(userubdated3==false){

                //chec for ubdate
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

                // Setting Dialog Title
                alertDialog.setTitle(R.string.new_ubdate);

                // Setting Dialog Message
                alertDialog.setMessage(R.string.newVersion);

                // Setting Icon to Dialog
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);

                // Setting Positive "Yes" Button
                alertDialog.setPositiveButton(R.string.ubdate, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {

                        // Write your code here to invoke YES event
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Url3));
                        startActivity(browserIntent);

                        SettingSaved.userubdated3=true;
                        SettingSaved settingSaved=new SettingSaved(getActivity());
                        settingSaved.SaveData();
                        //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                    }
                });

                // Setting Negative "NO" Button
                alertDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event

                        dialog.cancel();
                    }
                });

                // Showing Alert Message
                alertDialog.show();


            }



            //mWelcomeTextView.setAllCaps(true);
        } else {
            //mWelcomeTextView.setAllCaps(false);
        }
        if (ubdatedVersion.equals(AppVersion)){

        }else{
//chec for ubdate
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            // Setting Dialog Title
            alertDialog.setTitle(R.string.new_ubdate);

            // Setting Dialog Message
            alertDialog.setMessage(R.string.newVersion);

            // Setting Icon to Dialog
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);

            // Setting Positive "Yes" Button
            alertDialog.setPositiveButton(R.string.ubdate, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {

                    // Write your code here to invoke YES event
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Url3));
                    startActivity(browserIntent);


                    //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                }
            });

            // Setting Negative "NO" Button
            alertDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Write your code here to invoke NO event

                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        }
    }


    private void loadRewardedVideoAd() {
//        //mAd.loadAd(getString(R.string.Video_ad_unit_id), new AdRequest.Builder().build());

    }
    /*
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            // onQuitPressed();
        }

        return super.onKeyDown(keyCode, event);
    }*/


    private void DisplayAya() {


        ListAya();

//        checkPermission();

    }
    private void checkPermission(){
        if ( Build.VERSION.SDK_INT >= 23)
        {
            if (
                    (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                            (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                            (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) ||
                            (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) ||
                            (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED)
            )
            {

                requestPermissions(new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.MODIFY_AUDIO_SETTINGS},
                        REQUEST_CODE_ASK_PERMISSIONS);


                return;
            }else {
                ListAya();
            }

        }else {
            ListAya();
        }
    }
    void ListAya(){
        try{
            if(    RecitesName.length()>1){
                Intent intent = new Intent(getActivity(), AyaList.class);
                intent.putExtra("RecitesName",RecitesName);
                intent.putExtra("Rewayat",Rewayat);
                intent.putExtra("RealRecitesName",RealRecitesName);

                View sharedView = SharedView;
                String transitionName = "shared_reciter";


                if (SettingSaved.titlesTextAnimate){

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), sharedView, transitionName);
                        startActivity(intent, transitionActivityOptions.toBundle());
//                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                    }else{

                        startActivity(intent);
                    }
                }else{
                    startActivity(intent);
                }




                createReport(null);

            }
        }catch (Exception ex)
        {}
    }

    private void createReport(String shareUrl){
        ReportType reportType;
        if (shareUrl!= null){
            reportType = ReportType.ShareReciter;
        }else{
            reportType = ReportType.OpenReciter;

        }
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                User user = dataSnapshot.getValue(User.class);

                Long Date = new SeparateFunctions(getContext()).getTimeStamp();
                Report report = new Report("",user.id,user.username,user.photourl, reportType,RecitesName,RealRecitesName,shareUrl,"",Date);
                ReportService reportService = new ReportService(getActivity(),getContext());
                reportService.createReciteReport(report);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void createYouTubePostReport(String shareUrl , String postId, String postTitle ){
        ReportType reportType ;
        if (shareUrl!= null){
            reportType = ReportType.ShareYouTubePost;
        }else{
            reportType = ReportType.OpenYouTubePost;
        }


        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                User user = dataSnapshot.getValue(User.class);

                Long Date = new SeparateFunctions(getContext()).getTimeStamp();
                Report report = new Report("",user.id,user.username,user.photourl, reportType,postId,postTitle,shareUrl,"",Date,true);
                ReportService reportService = new ReportService(getActivity(),getContext());
                reportService.createSurahReport(report);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void createYouTubePostReportStarUnStar( String postId, String postTitle,Boolean isStar ){
        ReportType reportType ;
        if (isStar==true){
            reportType = ReportType.StarYouTubePost;
        }else{
            reportType = ReportType.UnStarYouTubePost;
        }

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                User user = dataSnapshot.getValue(User.class);

                Long Date = new SeparateFunctions(getContext()).getTimeStamp();
                Report report ;
                if (isStar==true){
                    report = new Report("",user.id,user.username,user.photourl, reportType,postId,postTitle,"",Date);
                }else{
                    report = new Report("",user.id,user.username,user.photourl, reportType,postId,postTitle,"",Date,new String[]{});

                }
                ReportService reportService = new ReportService(getActivity(),getContext());
                reportService.createSurahReport(report);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }








    class VivzAdapter extends BaseAdapter {

        ArrayList<AuthorClass> listrecitesLocal;

        VivzAdapter(ArrayList<AuthorClass> listrecites) {

            listrecitesLocal = new ArrayList<AuthorClass>();
            listrecitesLocal = listrecites;

        }


        @Override
        public int getCount() {
            return listrecitesLocal.size();
        }

        @Override
        public String getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private void onShareBy( String soura,String reciter) {

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
//            String shareBody = getResources().getString(R.string.sharemessage) + "  https://rebrand.ly/notfof70d";
            String shareBody = "";
            if (getResources().getString(R.string.sharePart3) == "reciter"){
                shareBody = getResources().getString(R.string.sharePart1) +" " +soura+ " "+ "."+ " "+ getResources().getString(R.string.sharePart4) + " " + getResources().getString(R.string.shareURL);

            }else{
                shareBody = getResources().getString(R.string.sharePart1) +" " +soura+ " " +"."+ " "+ getResources().getString(R.string.sharePart4) + " " + getResources().getString(R.string.shareURL);

            }
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Stream");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));

        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater mInflater = getActivity().getLayoutInflater();
            View myView = mInflater.inflate(R.layout.recites_ticket, null);
            TextView txtRecitesName = (TextView) myView.findViewById(R.id.txtRecitesName);
            ImageButton buttonShare = (ImageButton) myView.findViewById(R.id.buttonShare);
            LinearLayout entireCard = (LinearLayout) myView.findViewById(R.id.entireCard);

            LnaguageClass lc = new LnaguageClass(getContext());
            txtRecitesName = lc.SetTextFont(txtRecitesName,"");

            final AuthorClass temp = listrecitesLocal.get(position);
            txtRecitesName.setText(temp.RealName);
            CardView cardContent = (CardView) myView.findViewById(R.id.cardContent);
            cardContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RecitesName = temp.ServerName;
                    Rewayat = temp.Rewayat;
                    RealRecitesName = temp.RealName;
                    // String welcomes = listrecitesitem.ServerName;
                    DisplayAya();
                }
            });
            buttonShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onShareBy(temp.RealName,RealRecitesName);

                }
            });
            return myView;


        }


    }

    //get access to mailbox
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                    ListAya();
                } else {
                    // Permission Denied
                    ListAya();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Required to reward the user.



    public void loadad(){

        /*/*mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId(getString(R.string.Pop_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });*/

    }

    public void  prepareAd(){

        /*mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId(getString(R.string.Pop_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());*/
    }
    @Override
    public void onDestroy() {
        SettingSaved.IsOpen =  0;//App Is Opened
        SettingSaved.SounlLoad= 0;//Sound not load
        super.onDestroy();
    }

    private void DisplayDownload() {
        if ((int) Build.VERSION.SDK_INT >= 23)
        {
            if ((ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||
                    (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||
                    ( ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)||
                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED)
            {

                requestPermissions(new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.MODIFY_AUDIO_SETTINGS},
                        REQUEST_CODE_ASK_PERMISSIONS);


                return;
            }

        }
        //permisons

        Intent down = new Intent(getActivity(), Downloads.class);
        startActivity(down);
    }

    void reminder(){


// load setting informatin if we have
        SettingSaved settingSaved = new SettingSaved(getActivity());
        settingSaved.LoadData();
        //start notification every day
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, SettingSaved.selectedHour);
        calendar.set(Calendar.MINUTE, SettingSaved.selectedMinute);
        calendar.set(Calendar.SECOND, 0);
        Intent intent1 = new Intent(getActivity(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0,intent1, PendingIntent.FLAG_MUTABLE);
        AlarmManager am = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        Toast.makeText(getActivity(), R.string.reminder_started, Toast.LENGTH_SHORT).show();
    }

    public void chicLastRecite(){
        SettingSaved settingSaved = new SettingSaved(getActivity());
                settingSaved.LoadData();

        if (SettingSaved.FinalRecite!=""){
            //chec for ubdate
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            // Setting Dialog Title
            alertDialog.setTitle(R.string.lastrecite);

            // Setting Dialog Message
            alertDialog.setMessage(R.string.history);

            // Setting Icon to Dialog
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);

            // Setting Positive "Yes" Button
            alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {

                    // Write your code here to invoke YES event
                    Intent intent= new Intent( getActivity(),managerdb.class);
                    intent.putExtra("RecitesName", SettingSaved.FinalRecite);
                    intent.putExtra("RecitesAYA", SettingSaved.FinalAya);
                    intent.putExtra("Rewayat", SettingSaved.FinalRewayat);
                    intent.putExtra("RealRecitesName", SettingSaved.FinalRealRecitesName);
                    startActivity(intent);
                    //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                }
            });

            // Setting Negative "NO" Button
            alertDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Write your code here to invoke NO event

                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();


        }
    }

}
