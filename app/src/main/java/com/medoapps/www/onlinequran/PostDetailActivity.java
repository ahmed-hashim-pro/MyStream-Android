package com.medoapps.www.onlinequran;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
import static com.medoapps.www.onlinequran.R.id.adView;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medoapps.www.onlinequran.models.Block;
import com.medoapps.www.onlinequran.models.Comment;
import com.medoapps.www.onlinequran.models.Inappropriate;
import com.medoapps.www.onlinequran.models.InappropriateType;
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.models.Reporting;
import com.medoapps.www.onlinequran.models.ReportingType;
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import java.util.ArrayList;
import java.util.List;

//import com.google.android.gms.ads.InterstitialAd;
//import com.google.android.gms.ads.reward.RewardItem;
//import com.google.android.gms.ads.reward.RewardedVideoAd;
//import com.google.android.gms.ads.reward.RewardedVideoAdListener;

public class PostDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PostDetailActivity";
    private AdView mAdView;

    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_USER_KEY = "user_key";
    private static final String REQUIRED = "Required";


    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private DatabaseReference mUserReference;
    private DatabaseReference mUserReference2;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private String mUserKey;
    private CommentAdapter mAdapter;
    private String postUID;
    private String av = null;

    private TextView mAuthorView;
    private TextView mTitleView;
    private TextView mBodyView;
    private TextView mViews;
    private ImageView mProfileView;
    private ImageView fullThumb;
    private EditText mCommentField;
    private Button mCommentButton;
    private Button show;
    private RecyclerView mCommentsRecycler;
    private ExoPlayer player;
    private ExoPlayer playerfull;
   // private PlayerView playerView;
    private DefaultExtractorsFactory extractorsFactory ;
    private DataSource.Factory mediaDataSourceFactory;
    private BandwidthMeter bandwidthMeter;
    private DataSource.Factory manifestDataSourceFactory;



    //private ComponentListener componentListener;
    private String attach_url="https://www.empty.mp4";
    private LinearLayout playerHeight ;


    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;


    private static final String AD_UNIT_ID = BuildConfig.ADMOB_AD_UNIT_ID;
    private static final String APP_ID = BuildConfig.ADMOB_APP_ID;
    private static final long COUNTER_TIME = 1;
    private CountDownTimer mCountDownTimer;
    //private InterstitialAd mInterstitialAd;
    //private RewardedVideoAd mRewardedVideoAd;
    private long mTimeRemaining;
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter.Builder(null).build();



    private final String STATE_RESUME_WINDOW = "resumeWindow";
    private final String STATE_RESUME_POSITION = "resumePosition";
    private final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";

    private StyledPlayerView mExoPlayerView;
    private MediaSource mVideoSource;
    private boolean mExoPlayerFullscreen = false;
    private FrameLayout mFullScreenButton;
    private ImageView mFullScreenIcon;
    private Dialog mFullScreenDialog;

    private int mResumeWindow;
    private long mResumePosition;

    private DatabaseReference mCommentReportsReference;
    private DatabaseReference mVideoReportsReference;
    private DatabaseReference mInappropriateReference;
    private DatabaseReference mBlockUserReference;
    Post post;
    ImageView videoOption;

    public PostDetailActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        // hide auto keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().setSoftInputMode(SOFT_INPUT_ADJUST_PAN);
        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (savedInstanceState != null) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
        }
        SettingSaved settingSaved=new SettingSaved(PostDetailActivity.this);
        settingSaved.LoadData();


        loadad();//to load ads full screen

        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        mUserKey = getIntent().getStringExtra(EXTRA_USER_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        // Initialize Database
        mPostReference = FirebaseDatabase.getInstance().getReference()
                .child("posts").child(mPostKey);
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserKey);
        mCommentsReference = FirebaseDatabase.getInstance().getReference()
                .child("post-comments").child(mPostKey);

        mCommentReportsReference = FirebaseDatabase.getInstance().getReference().child("reporting").child("Comments");
        mVideoReportsReference = FirebaseDatabase.getInstance().getReference().child("reporting").child("LocalPost");
        mInappropriateReference = FirebaseDatabase.getInstance().getReference().child("User-settings").child("Inappropriate");
        mBlockUserReference = FirebaseDatabase.getInstance().getReference().child("User-settings").child("Block");

        // Initialize Views
        mAuthorView = findViewById(R.id.post_author);
        mTitleView = findViewById(R.id.post_title);
        mBodyView = findViewById(R.id.post_body);
        mProfileView = findViewById(R.id.post_author_photo);
        mCommentField = findViewById(R.id.field_comment_text);
        mCommentButton = findViewById(R.id.button_post_comment);
        mCommentsRecycler = findViewById(R.id.recycler_comments);
        //componentListener = new ComponentListener();
        //playerView = findViewById(R.id.video_view);
        playerHeight = findViewById(R.id.playerHeight);
        fullThumb = findViewById(R.id.FullThumb);
        show = findViewById(R.id.showVideo);
        mViews = findViewById(R.id.ViewsNum);

        videoOption = findViewById(R.id.videoOption);

        mCommentButton.setOnClickListener(this);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        videoOption.setOnClickListener(this);

//load banner ad
        loadBannerAd();



    }

    private void loadBannerAd() {
        if (SettingSaved.isSubscribedPremium)
            return;
        mAdView = (AdView) findViewById(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                //mAdView.setVisibility(View.VISIBLE);
            }


        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen);

        super.onSaveInstanceState(outState);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        System.out.println("IN onConfigurationChanged()");
    }
    @Override
    public void onStart() {
        super.onStart();
        loadRewardedVideoAd();

        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                post = dataSnapshot.getValue(Post.class);
                post.id = dataSnapshot.getKey();
                postUID = post.uid;
                //Toast.makeText(PostDetailActivity.this, mUserKey, Toast.LENGTH_SHORT).show();
                // [START_EXCLUDE]
                mAuthorView.setText(post.author);
                mAuthorView.setTextColor(getResources().getColor(R.color.toolbarTextColor));
                mTitleView.setText(post.title);
                mTitleView.setTextColor(getResources().getColor(R.color.toolbarTextColor));
                mBodyView.setText(post.body);
                mBodyView.setTextColor(getResources().getColor(R.color.toolbarTextColor));
                mViews.setText(String.valueOf(post.viewCount));
                attach_url = post.attachment;
                av = post.Upload_Type;
                if (av.contains("video")){
                    // Gets the layout params that will allow you to resize the layout
                    ViewGroup.LayoutParams params = playerHeight.getLayoutParams();
                    // Changes the height and width to the specified *pixels*

                    //params.height = 800;

                    //playerHeight.setLayoutParams(params);

                }else{

                }
                String thumb_url = post.Thumb_Url;
                try {
                    Glide.with(getApplicationContext())
                            .load(thumb_url)
                            .into(fullThumb);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (player == null) {
                    //initializePlayer();

                    //initExoPlayer();

                }


                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]

                // [END_EXCLUDE]
            }
        };


        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI

                User url = dataSnapshot.getValue(User.class);

                String id = url.email;

                String ProfileUrl = url.photourl;
                //Toast.makeText(PostDetailActivity.this, id, Toast.LENGTH_SHORT).show();
                String u = "https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/photos%2Fimage%3A10330?alt=media&token=7c747aba-746d-47b9-a218-d5b12cd63022";

                try {
                    Glide.with(getApplicationContext())
                            .load(ProfileUrl)
                            .into(mProfileView);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]

                // [END_EXCLUDE]
            }
        };
        mPostReference.addValueEventListener(postListener);
        mUserReference.addValueEventListener(userListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;

        // Listen for comments
        mAdapter = new CommentAdapter(this, mCommentsReference);
        mCommentsRecycler.setAdapter(mAdapter);

       /*if (attach_url!=null) {
           initializePlayer();
       }*/
       //hideSystemUi();
    }
    @Override
    protected void onPause() {
        super.onPause();

        //Toast.makeText(this, "Pause", Toast.LENGTH_SHORT).show();
       // playbackPosition = player.getCurrentPosition();
        //currentWindow = player.getCurrentWindowIndex();

        //to prevent show full screen ad while playing video
        SettingSaved.isfullscreenadshow= false;
        SettingSaved settingSaved=new SettingSaved(getApplicationContext());
        settingSaved.SaveData();
        settingSaved.LoadData();
        //Toast.makeText(this, String.valueOf(playbackPosition), Toast.LENGTH_SHORT).show();
        if (mExoPlayerView != null && mExoPlayerView.getPlayer() != null) {
            mResumeWindow = mExoPlayerView.getPlayer().getCurrentWindowIndex();
            mResumePosition = Math.max(0, mExoPlayerView.getPlayer().getContentPosition());

            mExoPlayerView.getPlayer().release();
        }

        if (mFullScreenDialog != null)
            mFullScreenDialog.dismiss();

    }

    @Override
    public void onStop() {
        super.onStop();
        //releasePlayer();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }
        //to prevent show full screen ad while playing video
        SettingSaved.isfullscreenadshow= false;
        SettingSaved settingSaved=new SettingSaved(getApplicationContext());
        settingSaved.SaveData();
        settingSaved.LoadData();

        // Clean up comments listener
        mAdapter.cleanupListener();
    }

    @Override
    protected void onDestroy() {
        SettingSaved.isfullscreenadshow= false;
        SettingSaved settingSaved=new SettingSaved(getApplicationContext());
        settingSaved.SaveData();
        settingSaved.LoadData();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(this, "Resume", Toast.LENGTH_SHORT).show();

        //to prevent show full screen ad while playing video
        SettingSaved.isfullscreenadshow= true;
        SettingSaved settingSaved=new SettingSaved(getApplicationContext());
        settingSaved.SaveData();
        settingSaved.LoadData();



        if(attach_url=="https://www.empty.mp4") {

            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    Post post = dataSnapshot.getValue(Post.class);
                    postUID = post.uid;
                    //Toast.makeText(PostDetailActivity.this, mUserKey, Toast.LENGTH_SHORT).show();
                    // [START_EXCLUDE]

                    attach_url = post.attachment;



                    if (mExoPlayerView == null) {
                        mExoPlayerView =  findViewById(R.id.exoplayer);
                        initFullscreenDialog();
                        initFullscreenButton();

                        String streamUrl = "https://content.jwplatform.com/videos/87kzYcSU-hP63b8R3.mp4";
                        String userAgent = getApplicationContext().getApplicationInfo().packageName;
                        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory().setUserAgent(userAgent).setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS).setReadTimeoutMs(DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS).setAllowCrossProtocolRedirects(true);
                        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(PostDetailActivity.this, httpDataSourceFactory);
                        //Uri daUri = Uri.parse(attach_url);

                        //mVideoSource = new HlsMediaSource(daUri, dataSourceFactory, 1, null, null);
                        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter.Builder(getApplicationContext()).build();
                        DataSource.Factory dataSourceFactory2 = new DefaultDataSource.Factory(getApplicationContext(), new DefaultHttpDataSource.Factory().setUserAgent("Application Name").setTransferListener((TransferListener) defaultBandwidthMeter));
                        Uri uri = Uri.parse(attach_url);
                        mVideoSource = new ProgressiveMediaSource.Factory(dataSourceFactory2).createMediaSource(MediaItem.fromUri(uri));
                    }

                    initExoPlayer();
                    if (mExoPlayerFullscreen) {
                        ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
                        mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(PostDetailActivity.this, R.drawable.ic_fullscreen_skrink));
                        mFullScreenDialog.show();
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    // [START_EXCLUDE]

                    // [END_EXCLUDE]
                }
            };


            mPostReference.addValueEventListener(postListener);
            mPostListener = postListener;

        }else {


            if (mExoPlayerView == null) {
                mExoPlayerView =  findViewById(R.id.exoplayer);
                initFullscreenDialog();
                initFullscreenButton();

                String streamUrl = "https://content.jwplatform.com/videos/87kzYcSU-hP63b8R3.mp4";
                String userAgent = getApplicationContext().getApplicationInfo().packageName;
                DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory().setUserAgent(userAgent).setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS).setReadTimeoutMs(DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS).setAllowCrossProtocolRedirects(true);
                DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(PostDetailActivity.this, httpDataSourceFactory);
                //Uri daUri = Uri.parse(attach_url);

                //mVideoSource = new HlsMediaSource(daUri, dataSourceFactory, 1, null, null);
                DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter.Builder(getApplicationContext()).build();
                DataSource.Factory dataSourceFactory2 = new DefaultDataSource.Factory(getApplicationContext(), new DefaultHttpDataSource.Factory().setUserAgent("Application Name").setTransferListener((TransferListener) defaultBandwidthMeter));
                Uri uri = Uri.parse(attach_url);
                mVideoSource = new ProgressiveMediaSource.Factory(dataSourceFactory2).createMediaSource(MediaItem.fromUri(uri));
            }

            initExoPlayer();
            if (mExoPlayerFullscreen) {
                ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
                mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(PostDetailActivity.this, R.drawable.ic_fullscreen_skrink));
                mFullScreenDialog.show();
            }
        }




    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_post_comment) {
            postComment();
        }
        if (i == R.id.videoOption){
            PopupMenu popup = new PopupMenu(PostDetailActivity.this, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.youtube_post_inside_activity_option, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {

                        case R.id.post_report:

                            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(PostDetailActivity.this);
                            bottomSheetDialog.setContentView(R.layout.modal_bottom_report_sheet);

                            TextInputLayout reportTextField = bottomSheetDialog.findViewById(R.id.reportTextField);
                            bottomSheetDialog.findViewById(R.id.completeReport).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    String reportReasonText = reportTextField.getEditText().getText().toString();
                                    if (TextUtils.isEmpty(reportReasonText)) {
                                        reportTextField.setError(REQUIRED);
                                        return;
                                    }
                                    addVideoReport(post,reportReasonText);
                                    bottomSheetDialog.dismiss();
                                    Toast.makeText(PostDetailActivity.this,getString(R.string.ReportAdded) , Toast.LENGTH_SHORT).show();
                                }
                            });



                            bottomSheetDialog.show();


                            int reportsNum = post.reportsNumber+1;
                            mPostReference.child("isReported").setValue(true);
                            mPostReference.child("reportsNumber").setValue(reportsNum);
//                                            globalPostRef.updateChildren(comment);



                            break;

                        case R.id.post_flag:

                            addInappropriate();

                            break;

                        case R.id.user_block:
                            blockUser();
                            break;
                        default:
                            break;

                    }
                    return true;
                }
            });

            popup.show();
        }
    }

    private void blockUser() {
        final String uid = getUid();
        String key =  mBlockUserReference.push().getKey();

        Block block = new Block(key,uid,post.uid,null,post.id,false,false );
        // Push the comment, it will appear in the list
        mBlockUserReference.child(key).setValue(block);
        Toast.makeText(getApplicationContext(), getString(R.string.Blocked), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void addVideoReport(Post post ,String text) {



        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.firstname+" "+user.lastname;
                        String autherPhoto = user.photourl;

                        String key =  mVideoReportsReference.push().getKey();
                        Long Date = new SeparateFunctions(getApplicationContext()).getTimeStamp();
                        Reporting reporting = new Reporting(key,uid, ReportingType.LocalPost,null,post.id,authorName,autherPhoto,text,Date);

                        // Push the comment, it will appear in the list
                        mVideoReportsReference.child(key).setValue(reporting);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void addInappropriate() {

        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.firstname+" "+user.lastname;
                        String autherPhoto = user.photourl;

                        String key =  mInappropriateReference.push().getKey();

                        Inappropriate inappropriate = new Inappropriate(key,uid, InappropriateType.LocalPost,null,post.id,authorName,autherPhoto);
                        // Push the comment, it will appear in the list
                        mInappropriateReference.child(key).setValue(inappropriate);
                        Toast.makeText(getApplicationContext(), getString(R.string.Flagged), Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
    private void initFullscreenDialog() {

        mFullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog();
                super.onBackPressed();
            }
        };
    }


    private void openFullscreenDialog() {

        mResumePosition=mExoPlayerView.getPlayer().getContentPosition();
        ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
        mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(PostDetailActivity.this, R.drawable.ic_fullscreen_skrink));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
    }


    private void closeFullscreenDialog() {
        mResumePosition=mExoPlayerView.getPlayer().getContentPosition();
        ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
        ((FrameLayout) findViewById(R.id.main_media_frame)).addView(mExoPlayerView);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(PostDetailActivity.this, R.drawable.ic_fullscreen_expand));
    }

    private void initFullscreenButton() {

        StyledPlayerControlView controlView = mExoPlayerView.findViewById(R.id.exo_controller);
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mExoPlayerFullscreen)
                    openFullscreenDialog();
                else
                    closeFullscreenDialog();
            }
        });
    }
    private void initExoPlayer() {
        //mVideoSource = new HlsMediaSource(daUri, dataSourceFactory, 1, null, null);




        ExoTrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this, videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
         playerfull = new ExoPlayer.Builder(this).setRenderersFactory(new DefaultRenderersFactory(this)).setTrackSelector(trackSelector).setLoadControl(loadControl).build();
        mExoPlayerView.setPlayer(playerfull);

        boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;


        if (haveResumePosition) {
            mExoPlayerView.getPlayer().seekTo(mResumeWindow, mResumePosition);
        }
        playerfull.setMediaSource(mVideoSource, !haveResumePosition);
        playerfull.prepare();
        mExoPlayerView.getPlayer().setPlayWhenReady(true);

    }
   /* private void initializePlayer() {
        //load full screan ad
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }

        //to prevent show full screen ad while playing video
        SettingSaved.isfullscreenadshow= true;
        SettingSaved settingSaved=new SettingSaved(getApplicationContext());
        settingSaved.SaveData();
        settingSaved.LoadData();

        loadRewardedVideoAd();
        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);

        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(), new DefaultLoadControl());

        playerView.setPlayer(player);


        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        String gg = "https://firebasestorage.googleapis.com/v0/b/storage-e8e09.appspot.com/o/video%2Fvideo%3A10347?alt=media&token=6d336d12-d0b0-442e-808c-230ade06257a";
        String hh = "https://firebasestorage.googleapis.com/v0/b/storage-e8e09.appspot.com/o/video%2F114.mp3?alt=media&token=789e2793-b905-423c-888c-127defb97032";
        Uri uri = Uri.parse(attach_url);
        //extractorsFactory = new DefaultExtractorsFactory();
        MediaSource mediaSource = buildMediaSource(uri);
       *//* MediaSource mediaSource = new ExtractorMediaSource(uri,
                mediaDataSourceFactory, extractorsFactory, null, null);*//*
        player.prepare(mediaSource, true, false);
        player.seekTo(playbackPosition);
    }*/
    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    /*private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab")).
                createMediaSource(uri);
    }*/

   /* @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }*/
    private void postComment() {


        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.firstname+" "+user.lastname;
                        String autherPhoto = user.photourl;

                        // Create new comment object
                        String commentText = mCommentField.getText().toString();


                        if (TextUtils.isEmpty(commentText)) {
                            mCommentField.setError(REQUIRED);
                            return;
                        }
                        String key =  mCommentsReference.push().getKey();

                        Comment comment = new Comment(key,uid,post.id,post.postType, authorName, commentText,autherPhoto);

                        // Push the comment, it will appear in the list
                        mCommentsReference.child(key).setValue(comment);

                        // Clear the field
                        mCommentField.setText(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }





    public void ShowVideo(View view) {

        showRewardedVideo();
        //show.setVisibility(View.GONE);

    }

    private void addReport(Comment comment ,String text) {



        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.firstname+" "+user.lastname;
                        String autherPhoto = user.photourl;

                        String key =  mCommentReportsReference.push().getKey();
                        Long Date = new SeparateFunctions(getApplicationContext()).getTimeStamp();

                        Reporting reporting = new Reporting(key,uid, ReportingType.Comment,comment.id,post.id,authorName,autherPhoto,text,Date);

                        // Push the comment, it will appear in the list
                        mCommentReportsReference.child(key).setValue(reporting);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    private static class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView authorView;
        public TextView bodyView;
        public ImageView photoView;
        public ImageView option;
        public LinearLayout comment_edit_form,textShowForm;
        public Button button_update_comment,button_comment_cancel;
        public EditText field_comment_update_text;


        public CommentViewHolder(View itemView) {
            super(itemView);

            authorView = itemView.findViewById(R.id.comment_author);
            bodyView = itemView.findViewById(R.id.comment_body);
            photoView = itemView.findViewById(R.id.comment_photo);
            option = itemView.findViewById(R.id.option);
            comment_edit_form = itemView.findViewById(R.id.comment_edit_form);
            textShowForm = itemView.findViewById(R.id.textShowForm);
            button_update_comment = itemView.findViewById(R.id.button_update_comment);
            button_comment_cancel = itemView.findViewById(R.id.button_comment_cancel);
            field_comment_update_text = itemView.findViewById(R.id.field_comment_update_text);

        }
    }



    private class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;


        private List<String> mCommentIds = new ArrayList<>();
        private List<Comment> mComments = new ArrayList<>();

        public CommentAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new comment has been added, add it to the displayed list
                    Comment comment = dataSnapshot.getValue(Comment.class);

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    if (comment.isDeleted != null && comment.isDeleted == true){

                    }else{
                        mCommentIds.add(dataSnapshot.getKey());
                        mComments.add(comment);
                        notifyItemInserted(mComments.size() - 1);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    Comment newComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Replace with the new data
                        mComments.set(commentIndex, newComment);

                        // Update the RecyclerView
                        notifyItemChanged(commentIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                    }

                    if(newComment.isDeleted != null && newComment.isDeleted){

                        Log.d(TAG, "onChildChangeddfsdf: ");

                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex);

                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Remove data from the list
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    Comment movedComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final CommentViewHolder holder, int position) {
            Comment comment = mComments.get(position);
            holder.authorView.setText(comment.author);


            String uUID = comment.uid;

            holder.bodyView.setText(comment.text);

            mUserReference2 = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(uUID);

            ValueEventListener userListener2 = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI

                    User url = dataSnapshot.getValue(User.class);

                    String id = url.email;

                    String ProfileUrl = url.photourl;
                    //Toast.makeText(PostDetailActivity.this, id, Toast.LENGTH_SHORT).show();
                    //String u = "https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/photos%2Fimage%3A10330?alt=media&token=7c747aba-746d-47b9-a218-d5b12cd63022";


                    try {
                        Glide.with(getApplicationContext())
                                .load(ProfileUrl)
                                .into(holder.photoView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    holder.button_update_comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String commentText = holder.field_comment_update_text.getText().toString();


                            if (TextUtils.isEmpty(commentText)) {
                                holder.field_comment_update_text.setError(REQUIRED);
                                return;
                            }

                            DatabaseReference globalPostRef = mCommentsReference.child(comment.id);

                            globalPostRef.child("text").setValue(commentText);
                            holder.comment_edit_form.setVisibility(View.GONE);
                            holder.textShowForm.setVisibility(View.VISIBLE);


                        }
                    });

                    holder.button_comment_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            holder.comment_edit_form.setVisibility(View.GONE);
                            holder.textShowForm.setVisibility(View.VISIBLE);
                        }
                    });
                    holder.option.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DatabaseReference globalPostRef = mCommentsReference.child(comment.id);

                            PopupMenu popup = new PopupMenu(PostDetailActivity.this, view);
                            MenuInflater inflater = popup.getMenuInflater();
                            inflater.inflate(R.menu.comment_option, popup.getMenu());
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.comment_edit:

                                            holder.comment_edit_form.setVisibility(View.VISIBLE);
                                            holder.textShowForm.setVisibility(View.GONE);
                                            holder.field_comment_update_text.setText(comment.text);


                                            break;

                                        case R.id.comment_delete:

                                            globalPostRef.child("isDeleted").setValue(true);

                                            break;

                                        case R.id.comment_report:

                                            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(PostDetailActivity.this);
                                            bottomSheetDialog.setContentView(R.layout.modal_bottom_report_sheet);




                                            TextInputLayout reportTextField = bottomSheetDialog.findViewById(R.id.reportTextField);
                                            bottomSheetDialog.findViewById(R.id.completeReport).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                    String reportReasonText = reportTextField.getEditText().getText().toString();
                                                    if (TextUtils.isEmpty(reportReasonText)) {
                                                        reportTextField.setError(REQUIRED);
                                                        return;
                                                    }
                                                    addReport(comment,reportReasonText);
                                                    bottomSheetDialog.dismiss();
                                                    Toast.makeText(PostDetailActivity.this,getString(R.string.ReportAdded) , Toast.LENGTH_SHORT).show();
                                                }
                                            });



                                            bottomSheetDialog.show();

                                            int reportsNum = comment.reportsNumber+1;

                                            globalPostRef.child("isReported").setValue(true);
                                            globalPostRef.child("reportsNumber").setValue(reportsNum);

//                                            globalPostRef.updateChildren(comment);



                                            break;

                                        default:
                                            break;

                                    }
                                    return true;
                                }
                            });

                            String userId = getUid();

                            if (comment.uid.equals(userId)){
                                popup.getMenu().findItem(R.id.comment_edit).setVisible(true);
                                popup.getMenu().findItem(R.id.comment_delete).setVisible(true);
                                popup.getMenu().findItem(R.id.comment_report).setVisible(false);
                            }else {
                                popup.getMenu().findItem(R.id.comment_edit).setVisible(false);
                                popup.getMenu().findItem(R.id.comment_delete).setVisible(false);
                                popup.getMenu().findItem(R.id.comment_report).setVisible(true);
                            }

                            popup.show();

                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    // [START_EXCLUDE]

                    // [END_EXCLUDE]
                }
            };

            mUserReference2.addValueEventListener(userListener2);

        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

    }
    /*private class ComponentListener extends Player.DefaultEventListener implements
            VideoRendererEventListener, AudioRendererEventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;
            switch (playbackState) {
                case Player.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case Player.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case Player.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    break;
                case Player.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.d(TAG, "changed state to " + stateString + " playWhenReady: " + playWhenReady);
        }

        // Implementing VideoRendererEventListener.

        @Override
        public void onVideoEnabled(DecoderCounters counters) {
            // Do nothing.
        }

        @Override
        public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
            // Do nothing.
        }

        @Override
        public void onVideoInputFormatChanged(Format format) {
            // Do nothing.
        }

        @Override
        public void onDroppedFrames(int count, long elapsedMs) {
            // Do nothing.
        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            // Do nothing.
        }

        @Override
        public void onRenderedFirstFrame(Surface surface) {
            // Do nothing.
        }

        @Override
        public void onVideoDisabled(DecoderCounters counters) {
            // Do nothing.
        }

        // Implementing AudioRendererEventListener.

        @Override
        public void onAudioEnabled(DecoderCounters counters) {
            // Do nothing.
        }

        @Override
        public void onAudioSessionId(int audioSessionId) {
            // Do nothing.
        }

        @Override
        public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
            // Do nothing.
        }

        @Override
        public void onAudioInputFormatChanged(Format format) {
            // Do nothing.
        }

        @Override
        public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
            // Do nothing.
        }

        @Override
        public void onAudioDisabled(DecoderCounters counters) {
            // Do nothing.
        }

    }*/

    private void loadRewardedVideoAd() {

    }

    // Create the game timer, which counts down to the end of the level
    // and shows the "retry" button.
    private void createTimer(long time) {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mCountDownTimer = new CountDownTimer(time * 1000, 50) {
            @Override
            public void onTick(long millisUnitFinished) {
                mTimeRemaining = ((millisUnitFinished / 1000) + 1);

                //Toast.makeText(PostDetailActivity.this, String.valueOf(mTimeRemaining), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {


            }
        };
        mCountDownTimer.start();
    }

    private void showRewardedVideo() {

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
}
