package com.medoapps.www.onlinequran.hashimyoutubeplayer;

import static com.medoapps.www.onlinequran.R.id.adView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
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
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.SettingSaved;
import com.medoapps.www.onlinequran.classes.MySpannable;
import com.medoapps.www.onlinequran.classes.YouTubeConfig;
import com.medoapps.www.onlinequran.models.Block;
import com.medoapps.www.onlinequran.models.Comment;
import com.medoapps.www.onlinequran.models.Inappropriate;
import com.medoapps.www.onlinequran.models.InappropriateType;
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.models.Report;
import com.medoapps.www.onlinequran.models.ReportType;
import com.medoapps.www.onlinequran.models.Reporting;
import com.medoapps.www.onlinequran.models.ReportingType;
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.service.ReportService;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import java.util.ArrayList;
import java.util.List;

//import com.google.android.gms.ads.InterstitialAd;
//import com.google.android.gms.ads.reward.RewardedVideoAd;

public class YoutubePlayerViewActivity extends YouTubeFailureRecoveryActivity implements View.OnClickListener {

    private static final String TAG = "YoutubePlayerView";

    private String videoId;
    private String videoTitle;
    private String videoDescription;
    public TextView titleTXT;
    public TextView descriptionTXT;
    public LinearLayout EntireLayout;
    private static final int CONTENT_VIEW_ID = 10101010;


    private AdView mAdView;

    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_USER_KEY = "user_key";
    private static final String REQUIRED = "Required";


    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private DatabaseReference mCommentReportsReference;
    private DatabaseReference mVideoReportsReference;
    private DatabaseReference mInappropriateReference;
    private DatabaseReference mBlockUserReference;

    private DatabaseReference mUserReference;
    private DatabaseReference mUserReference2;
    private DatabaseReference mDatabase;

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
    public ImageView starView;
    public TextView numStarsView;
    public RelativeLayout star_layout;
    public RelativeLayout share_layout;

    private ImageView mProfileView;
    private ImageView fullThumb;
    private EditText mCommentField;
    private Button mCommentButton;
    private ImageButton seemore;
    private CardView titleCard;
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


    private static final String AD_UNIT_ID = "ca-app-pub-9350633918697995/7533398267";
    private static final String APP_ID = " ca-app-pub-9350633918697995~2524775865";
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

    private RelativeLayout descriptionContainer;
    Post post;
    ImageView videoOption;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player_view);

        titleTXT = (TextView) findViewById(R.id.textView13);
        descriptionTXT = (TextView) findViewById(R.id.description);

        Bundle b=getIntent().getExtras();
        videoId=b.getString("videoId");
        videoTitle=b.getString("videoTitle");
        videoDescription=b.getString("videoDescription");

        titleTXT.setText(videoTitle);
        descriptionTXT.setText(videoDescription);
        SettingSaved settingSaved=new SettingSaved(YoutubePlayerViewActivity.this);
        settingSaved.LoadData();
//        makeTextViewResizable(descriptionTXT, 3, "See More", true);


        YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubeView.initialize(YouTubeConfig.getApiKey(), this);



        // Initialize the Mobile Ads SDK.
//        //MobileAds.initialize(this, getString(R.string.ad_APP_ID));

        // Get post key from intent



        // Initialize Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mCommentReportsReference = FirebaseDatabase.getInstance().getReference().child("reporting").child("Comments");
        mVideoReportsReference = FirebaseDatabase.getInstance().getReference().child("reporting").child("YouTubePost");
        mInappropriateReference = FirebaseDatabase.getInstance().getReference().child("User-settings").child("Inappropriate");
        mBlockUserReference = FirebaseDatabase.getInstance().getReference().child("User-settings").child("Block");





        // Initialize Views
        starView = findViewById(R.id.star);
        numStarsView = findViewById(R.id.post_num_stars);
        star_layout = findViewById(R.id.star_layout);
        share_layout = findViewById(R.id.share_layout);
        mAuthorView = findViewById(R.id.post_author);
        mTitleView = findViewById(R.id.post_title);
        mBodyView = findViewById(R.id.post_body);
        mProfileView = findViewById(R.id.post_author_photo);
        mCommentField = findViewById(R.id.field_comment_text);
        mCommentButton = findViewById(R.id.button_post_comment);
        seemore = findViewById(R.id.seemore);
        videoOption = findViewById(R.id.videoOption);
        titleCard = findViewById(R.id.titleCard);
        mCommentsRecycler = findViewById(R.id.recycler_comments);
        //componentListener = new ComponentListener();
        //playerView = findViewById(R.id.video_view);
        playerHeight = findViewById(R.id.playerHeight);
        fullThumb = findViewById(R.id.FullThumb);
        show = findViewById(R.id.showVideo);
        mViews = findViewById(R.id.ViewsNum);
        descriptionContainer = findViewById(R.id.descriptionContainer);

        mCommentButton.setOnClickListener(this);
        star_layout.setOnClickListener(this);
        share_layout.setOnClickListener(this);
        seemore.setOnClickListener(this);
        titleCard.setOnClickListener(this);
        videoOption.setOnClickListener(this);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));

//load banner ad
        loadBannerAd();

        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        mUserKey = getIntent().getStringExtra(EXTRA_USER_KEY);
        if (mPostKey == null) {
            Log.d(TAG, "onCreate: dsadasdsa"+videoId);
            getPostFromVideoId();
//            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }else if (videoId == null){
            finish();
        }
        else{
            startCommentAdapter();

        }

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

    private void onShareBy( String videoTitle,String videoDescriptiontxt,String YouTubeVideoId,String Thumb_Url, String postId, String postTitle) {


        Uri imageUri = Uri.parse(Thumb_Url);
        String title = videoTitle;
        String description = videoDescriptiontxt;
        if (description.length() > 40)
            description = description.substring(0, 39) + "...";


//        Log.d(TAG, "createDynamicLink 2: " +title);
//        Log.d(TAG, "createDynamicLink 2: " +description);
//        Log.d(TAG, "createDynamicLink 2: " +"watch/"+YouTubeVideoId);

        SeparateFunctions separateFunctions = new SeparateFunctions(YoutubePlayerViewActivity.this);
        separateFunctions.createDynamicLink(YoutubePlayerViewActivity.this,"watch/"+YouTubeVideoId,title,description,imageUri).addOnCompleteListener(YoutubePlayerViewActivity.this, new OnCompleteListener<ShortDynamicLink>() {
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

                    createReport(shortLink.toString(),postId,postTitle);


                } else {
                    Log.d(TAG, "createDynamicLink 2: " +task);
                    // Error
                    // ...
                }
            }

        });


    }

    public void startCommentAdapter(){
        mCommentsReference = FirebaseDatabase.getInstance().getReference()
                .child("youtube-post-comments").child(mPostKey);
        mPostReference = FirebaseDatabase.getInstance().getReference()
                .child("youtube-posts").child(mPostKey);
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserKey);

        mAdapter = new CommentAdapter(this, mCommentsReference);
        mCommentsRecycler.setAdapter(mAdapter);
        getPostFromPostId();

    }

    public void startCommentAdapterFromDynamicLink(){
        mCommentsReference = FirebaseDatabase.getInstance().getReference()
                .child("youtube-post-comments").child(mPostKey);

        mPostReference = FirebaseDatabase.getInstance().getReference()
                .child("youtube-posts").child(mPostKey);
        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(mUserKey);
        mAdapter = new CommentAdapter(this, mCommentsReference);
        mCommentsRecycler.setAdapter(mAdapter);
        getPostFromPostId();

    }


    public void getPostFromVideoId(){
        Query rootRef = FirebaseDatabase.getInstance().getReference().child("youtube-posts").orderByChild("YouTubeVideoId").equalTo(videoId);
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot :  dataSnapshot.getChildren()) {
                    post = snapshot.getValue(Post.class);

                    post.id = snapshot.getKey();

                    videoTitle=post.title;
                    videoDescription=post.body;
                    mPostKey = post.id;
                    mUserKey = post.uid;
                    titleTXT.setText(videoTitle);
                    descriptionTXT.setText(videoDescription);
                    Log.d(TAG, "onDataChangfdfdsfe: " + mPostKey);
                    startCommentAdapterFromDynamicLink();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getPostFromPostId(){
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                post = dataSnapshot.getValue(Post.class);
                post.id = dataSnapshot.getKey();

                videoTitle=post.title;
                videoDescription=post.body;
                postUID = post.uid;
                titleTXT.setText(videoTitle);
                descriptionTXT.setText(videoDescription);
                //Toast.makeText(this, mUserKey, Toast.LENGTH_SHORT).show();
                // [START_EXCLUDE]
                mAuthorView.setText(post.author);
                mAuthorView.setTextColor(getResources().getColor(R.color.black));
                mTitleView.setText(post.title);
                mTitleView.setTextColor(getResources().getColor(R.color.white));
                mBodyView.setText(post.body);
                mBodyView.setTextColor(getResources().getColor(R.color.white));
                mViews.setText(String.valueOf(post.viewCount));
                attach_url = post.attachment;

                // Determine if the current user has liked this post and set UI accordingly
                if (post.stars.containsKey(getUid())) {
                    starView.setImageResource(R.drawable.ic_toggle_star_24);
                } else {
                    starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                }
                numStarsView.setText(String.valueOf(post.starCount));

                String thumb_url = post.Thumb_Url;
                try {
                    Glide.with(getApplicationContext())
                            .load(thumb_url)
                            .into(fullThumb);
                } catch (Exception e) {
                    e.printStackTrace();
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


        /*ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI

                User url = dataSnapshot.getValue(User.class);

                String id = url.email;

                String ProfileUrl = url.photourl;
                //Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
                String u = "https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/photos%2Fimage%3A10330?alt=media&token=7c747aba-746d-47b9-a218-d5b12cd63022";

                Glide.with(YoutubePlayerViewActivity.this)
                        .load(ProfileUrl)
                        .into(mProfileView);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(YoutubePlayerViewActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };*/
        mPostReference.addValueEventListener(postListener);
//        mUserReference.addValueEventListener(userListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Add value event listener to the post
        // [START post_value_event_listener]
        /*ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Post post = dataSnapshot.getValue(Post.class);
                postUID = post.uid;
                //Toast.makeText(this, mUserKey, Toast.LENGTH_SHORT).show();
                // [START_EXCLUDE]
                mAuthorView.setText(post.author);
                mAuthorView.setTextColor(getResources().getColor(R.color.black));
                mTitleView.setText(post.title);
                mTitleView.setTextColor(getResources().getColor(R.color.white));
                mBodyView.setText(post.body);
                mBodyView.setTextColor(getResources().getColor(R.color.white));
                mViews.setText(String.valueOf(post.viewCount));
                attach_url = post.attachment;

                // Determine if the current user has liked this post and set UI accordingly
                if (post.stars.containsKey(getUid())) {
                    starView.setImageResource(R.drawable.ic_toggle_star_24);
                } else {
                    starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                }
                numStarsView.setText(String.valueOf(post.starCount));

                String thumb_url = post.Thumb_Url;
                Glide.with(getApplicationContext())
                        .load(thumb_url)
                        .into(fullThumb);
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
                Toast.makeText(YoutubePlayerViewActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
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
                //Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
                String u = "https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/photos%2Fimage%3A10330?alt=media&token=7c747aba-746d-47b9-a218-d5b12cd63022";

                Glide.with(YoutubePlayerViewActivity.this)
                        .load(ProfileUrl)
                        .into(mProfileView);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(YoutubePlayerViewActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mPostReference.addValueEventListener(postListener);
        mUserReference.addValueEventListener(userListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;*/

        // Listen for comments
        /*mAdapter = new CommentAdapter(this, mCommentsReference);
        mCommentsRecycler.setAdapter(mAdapter);*/
    }

    @Override
    public void onStop() {
        super.onStop();
        //releasePlayer();


        try {
            if (mAdapter!= null)
                mAdapter.cleanupListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Clean up comments listener
    }
    
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_post_comment) {
            postComment();
        }
        if (i == R.id.star_layout) {
            // Need to write to both places the post is stored
            DatabaseReference globalPostRef = mDatabase.child("youtube-posts").child(mPostKey);
            DatabaseReference userPostRef = mDatabase.child("user-posts").child(mUserKey).child(mPostKey);

            // Run two transactions
            try {
                onStarClicked(globalPostRef,post.id,post.title,false);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                onStarClicked(userPostRef,post.id,post.title,true);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (i == R.id.share_layout) {
            // Need to write to both places the post is stored
            onShareBy(videoTitle,videoDescription,videoId,post.Thumb_Url,post.id,videoTitle);

        }

        if (i == R.id.seemore) {
            ViewGroup.LayoutParams params = descriptionContainer.getLayoutParams();
            if (params.height ==-1){
                params.height = 0;
                params.width = -1;
                seemore.setImageResource(R.drawable.outline_expand_more_24);
            }else{
                seemore.setImageResource(R.drawable.outline_expand_less_24);
                params.height = -1;
                params.width = -1;
            }
            descriptionContainer.setLayoutParams(params);
        }
        if (i == R.id.titleCard) {
            ViewGroup.LayoutParams params = descriptionContainer.getLayoutParams();
            if (params.height ==-1){
                params.height = 0;
                params.width = -1;
                seemore.setImageResource(R.drawable.outline_expand_more_24);
            }else{
                seemore.setImageResource(R.drawable.outline_expand_less_24);
                params.height = -1;
                params.width = -1;
            }
            descriptionContainer.setLayoutParams(params);
        }
        if (i == R.id.videoOption){
            PopupMenu popup = new PopupMenu(YoutubePlayerViewActivity.this, v);
                            MenuInflater inflater = popup.getMenuInflater();
                            inflater.inflate(R.menu.youtube_post_inside_activity_option, popup.getMenu());
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {

                                        case R.id.post_report:

                                            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(YoutubePlayerViewActivity.this);
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
                                                    Toast.makeText(YoutubePlayerViewActivity.this,getString(R.string.ReportAdded) , Toast.LENGTH_SHORT).show();
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
    private void addVideoReport(Post post ,String text) {


        try {
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

                            Reporting reporting = new Reporting(key,uid, ReportingType.YouTubePost,null,post.id,authorName,autherPhoto,text,Date);

                            // Push the comment, it will appear in the list
                            mVideoReportsReference.child(key).setValue(reporting);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
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


                        Inappropriate inappropriate = new Inappropriate(key,uid, InappropriateType.YouTubePost,null,post.id,authorName,autherPhoto);
                        // Push the comment, it will appear in the list
                        mInappropriateReference.child(key).setValue(inappropriate);
                        Toast.makeText(getApplicationContext(), getString(R.string.Flagged), Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    // [START post_stars_transaction]
    private void onStarClicked(DatabaseReference postRef,String postId, String postTitle , Boolean set) {
        try {
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
                        if (set == true){

                            createReportStarUnStar(postId,postTitle,false);
                        }
                    } else {
                        // Star the post and add self to stars
                        p.starCount = p.starCount + 1;
                        p.stars.put(getUid(), true);
                        if (set == true){

                            createReportStarUnStar(postId,postTitle,true);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
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

                        createReportComment(key,post.id);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void createReport(String shareUrl , String postId, String postTitle ){
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

                Long Date = new SeparateFunctions(YoutubePlayerViewActivity.this).getTimeStamp();
                Report report = new Report("",user.id,user.username,user.photourl, reportType,postId,postTitle,shareUrl,"",Date,true);
                ReportService reportService = new ReportService(YoutubePlayerViewActivity.this,YoutubePlayerViewActivity.this);
                reportService.createSurahReport(report);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void createReportStarUnStar( String postId, String postTitle,Boolean isStar ){
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

                Long Date = new SeparateFunctions(YoutubePlayerViewActivity.this).getTimeStamp();
                Report report ;
                if (isStar==true){
                    report = new Report("",user.id,user.username,user.photourl, reportType,postId,postTitle,"",Date);
                }else{
                    report = new Report("",user.id,user.username,user.photourl, reportType,postId,postTitle,"",Date,new String[]{});

                }
                ReportService reportService = new ReportService(YoutubePlayerViewActivity.this,YoutubePlayerViewActivity.this);
                reportService.createSurahReport(report);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void createReportComment( String commentId, String postId ){
        ReportType reportType = ReportType.CommentYouTubePost;


        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                User user = dataSnapshot.getValue(User.class);

                Long Date = new SeparateFunctions(YoutubePlayerViewActivity.this).getTimeStamp();
                Report report = new Report("",user.id,user.username,user.photourl, reportType,commentId,postId,"",Date,new Long[]{});  ;

                ReportService reportService = new ReportService(YoutubePlayerViewActivity.this,YoutubePlayerViewActivity.this);
                reportService.createSurahReport(report);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                    //Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
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

                            PopupMenu popup = new PopupMenu(YoutubePlayerViewActivity.this, view);
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

                                            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(YoutubePlayerViewActivity.this);
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
                                                    Toast.makeText(YoutubePlayerViewActivity.this,getString(R.string.ReportAdded) , Toast.LENGTH_SHORT).show();
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
    
    
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        if (!wasRestored) {
            player.loadVideo(videoId);
        }
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerView) findViewById(R.id.youtube_view);
    }

    public static void makeTextViewResizable(final TextView tv, final int maxLine, final String expandText, final boolean viewMore) {

        if (tv.getTag() == null) {
            tv.setTag(tv.getText());
        }
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {

                ViewTreeObserver obs = tv.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
                if (maxLine == 0) {
                    Toast.makeText(tv.getContext(), "1", Toast.LENGTH_SHORT).show();
                    int lineEndIndex = tv.getLayout().getLineEnd(0);
                    String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                } else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
                    Toast.makeText(tv.getContext(), "2", Toast.LENGTH_SHORT).show();

                    int lineEndIndex = tv.getLayout().getLineEnd(maxLine - 1);
                    String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
                                    viewMore), TextView.BufferType.NORMAL);
                } else {
                    Toast.makeText(tv.getContext(), "3", Toast.LENGTH_SHORT).show();

                    int lineEndIndex = tv.getLayout().getLineEnd(tv.getLayout().getLineCount() - 1);
                    String text = tv.getText().subSequence(0, lineEndIndex) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, lineEndIndex, expandText,
                                    viewMore), TextView.BufferType.NORMAL);
                }
            }
        });

    }

    private static SpannableStringBuilder addClickablePartTextViewResizable(final Spanned strSpanned, final TextView tv,
                                                                            final int maxLine, final String spanableText, final boolean viewMore) {
        String str = strSpanned.toString();
        SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);

        if (str.contains(spanableText)) {


            ssb.setSpan(new MySpannable(false){
                @Override
                public void onClick(View widget) {
                    if (viewMore) {
                        tv.setLayoutParams(tv.getLayoutParams());
                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                        tv.invalidate();
                        makeTextViewResizable(tv, -1, "See Less", false);
                    } else {
                        tv.setLayoutParams(tv.getLayoutParams());
                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                        tv.invalidate();
                        makeTextViewResizable(tv, 3, ".. See More", true);
                    }
                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length(), 0);

        }
        return ssb;

    }

}