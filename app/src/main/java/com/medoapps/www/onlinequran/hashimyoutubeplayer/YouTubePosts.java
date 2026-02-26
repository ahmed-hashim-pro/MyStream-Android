package com.medoapps.www.onlinequran.hashimyoutubeplayer;

import static com.facebook.FacebookSdk.getApplicationContext;
import static java.text.DateFormat.getDateTimeInstance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.StorageUtil;
import com.medoapps.www.onlinequran.classes.YouTubeConfig;
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.models.PostType;
import com.medoapps.www.onlinequran.models.Report;
import com.medoapps.www.onlinequran.models.ReportType;
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.models.UserTypes;
import com.medoapps.www.onlinequran.service.ReportService;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YouTubePosts extends Fragment {
    private static final String TAG = "RecentPosts";
    private EditText editText, etd;
    private Button button;
    public RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter adapter;
    private DatabaseReference mDatabase;
    private DatabaseReference mUserReference;
    private User CurrentUser;

    private ProgressBar Progressloader;
    private TextView NoPoststextView ,NetworkErrortextView;
    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_USER_KEY = "user_key";

    private int mPageSize;
    private int mCurrentSize;
    private boolean mSyncing;
    private boolean mOrderASC;
    private boolean orderASC = true;

    MyListAdapter myListAdapter;

    private List<Object> recyclerViewItems = new ArrayList<>();
    ArrayList<Post> youtubeVideosArrayList = new ArrayList<>();;
    List<Object> tempItems = new ArrayList<>();
    private List<String> mPostIds = new ArrayList<>();

    private int mFirstPostsNumber = 5;
    private int mAddPostsNumber = 5;
    Long postsCount = Long.valueOf(0);

    private String lastItemIdInList ;
    private Long mLastKey;
    private boolean mGettingMoreposts;
    public static final int ITEMS_PER_AD =7;
    RecyclerView.SmoothScroller smoothScroller;
    private int loadMoreNumber = 1;
    int loadedItems = 0;
    private boolean isSubscribedPremium;

    public static YouTubePosts newInstance() {
        YouTubePosts fragment = new YouTubePosts();
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.activity_youtube_posts, container, false);
        Progressloader = view.findViewById(R.id.Progressloader);
        NoPoststextView = view.findViewById(R.id.NoPoststextView);
        NetworkErrortextView = view.findViewById(R.id.NetworkErrortextView);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(getUid());

        Progressloader.setVisibility(View.VISIBLE);
        NoPoststextView.setVisibility(View.GONE);
        NetworkErrortextView.setVisibility(View.GONE);
        SeparateFunctions separateFunctions = new SeparateFunctions(getContext());

        if (separateFunctions.isNetworkAvailable()){

            getPostsCount();


        }else{
            NetworkErrortextView.setVisibility(View.VISIBLE);
            Progressloader.setVisibility(View.GONE);


        }


        myListAdapter = new MyListAdapter(getContext());


         smoothScroller = new LinearSmoothScroller(getContext()) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
        recyclerView = view.findViewById(R.id.messages_list);

        linearLayoutManager = new LinearLayoutManager(getContext());
//        linearLayoutManager.setReverseLayout(true);
//        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        mPageSize = mCurrentSize = Math.abs(2);
        mOrderASC = orderASC;
        scrollToTop();

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
//        Progressloader.setVisibility(View.VISIBLE);
//        NoPoststextView.setVisibility(View.GONE);
//        getCurrentUser();
    }

    @Override
    public void onStop() {
        super.onStop();
//        Toast.makeText(getContext(), "stop", Toast.LENGTH_SHORT).show();

        try {
//            adapter.stopListening();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Toast.makeText(getContext(), "destroy", Toast.LENGTH_SHORT).show();
        try {
            adapter.stopListening();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  void scrollToTop(){
        if (recyclerView!= null){

            smoothScroller.setTargetPosition(0);

            linearLayoutManager.startSmoothScroll(smoothScroller);
        }




        Log.d(TAG, "scrollToTop: ");
    }


    private void getPostsCount(){
        FirebaseDatabase.getInstance().getReference()
                .child("GlobalVariable").child("YoutubePostsCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: " + snapshot.getChildrenCount());
                postsCount = snapshot.getValue(Long.class);
                getCurrentUser();
//                usersNumber.setText(usersCount.toString());
//                FirebaseDatabase.getInstance().getReference().child("GlobalVariable").child("YoutubePostsCount").setValue(postssCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    public void getCurrentUser(){
        ValueEventListener userListener = new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                try {
                    User user = dataSnapshot.getValue(User.class);
                    CurrentUser = user;
                    if (user.isSubscribedPremium != null && user.isSubscribedPremium == false){

                        isSubscribedPremium = false;

                    }else if(user.isSubscribedPremium == null){

                        isSubscribedPremium = false;

                    }else{
                        isSubscribedPremium = true;

                    }
                    addNews(mFirstPostsNumber);
                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            int id = linearLayoutManager.findLastCompletelyVisibleItemPosition();
//                            Log.d(TAG, "onScrolledsdfdsfdsf: " + id );
//                            Log.d(TAG, "onScrolledsdfdsfdsf: loadMoreNumber" + loadMoreNumber );

                            if(id>= (mFirstPostsNumber-1)*loadMoreNumber ){
//                                Log.d(TAG, "onScrolledsdfdsfdsf: " + postsCount );
                                if (postsCount- myListAdapter.getItemCount() < mAddPostsNumber){
                                    if (postsCount- myListAdapter.getItemCount()>=1){
                                        loadMoreNumber = loadMoreNumber+1;
                                        addNewPost(Integer.valueOf(String.valueOf(postsCount- myListAdapter.getItemCount())));

                                    }


                                }else{
                                    loadMoreNumber = loadMoreNumber+1;

                                    addNewPost(mAddPostsNumber);
                                }

                            }else if (postsCount - myListAdapter.getItemCount() < mAddPostsNumber){
//                                Log.d(TAG, "onScrolledsdfdsfdsf: else if" + postsCount );
//                                Log.d(TAG, "onScrolledsdfdsfdsf: else if getItemCount()" + myListAdapter.getItemCount() );

                                if (postsCount- myListAdapter.getItemCount()>=1){
                                    loadMoreNumber = loadMoreNumber+1;
//                                    Log.d(TAG, "onScrolledsdfdsfdsf: else if" + (postsCount- myListAdapter.getItemCount()) );

                                    addNewPost(Integer.valueOf(String.valueOf(postsCount- myListAdapter.getItemCount())));

                                }



                            }

                            /*if(id>= (mFirstPostsNumber -1)*loadMoreNumber){

                                Log.d(TAG, "onScrolledsdfdsfdsf: " + mLastKey );
                                addNewPost(mLastKey);

                            }*/
                            Log.d(TAG, "onScrolledsdfdsfdsf: else if getItemCount() final" + myListAdapter.getItemCount() );

                        }
                    });

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
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    private void fetch() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("youtube-posts").orderByChild("postType").equalTo(String.valueOf(PostType.YouTube));


        FirebaseRecyclerOptions<Object> options =
                new FirebaseRecyclerOptions.Builder<Object>()
                        .setQuery(query, Object.class )
                        .build();





        adapter = new FirebaseRecyclerAdapter<Object, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_youtube_post, parent, false);

                return new ViewHolder(view);
            }



            @Override
            public void onChildChanged(@NonNull ChangeEventType type, @NonNull DataSnapshot snapshot, int newIndex, int oldIndex) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex);
                Log.d("optionsTAG", String.valueOf(adapter.getItemCount()));
                if (adapter.getItemCount() > 0){
                    Progressloader.setVisibility(View.GONE);
                    NoPoststextView.setVisibility(View.GONE);
                }else{
                    Progressloader.setVisibility(View.GONE);
                    NoPoststextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
//                Log.d("optionsTAG", String.valueOf(adapter.getItemCount()));
//                Progressloader.setVisibility(View.GONE);

            }

            @Override
            public int getItemCount() {

                return super.getItemCount();
            }

            @Override
            protected void onBindViewHolder(ViewHolder viewHolder, final int position, final Object objectModel) {
                final DatabaseReference postRef = getRef(position);


                Map<String, Object> map = (Map<String, Object>) objectModel;

                Post model = new Post();
//                model =  ((Post) objectModel);
//                model = ((Post) objectModel);
                model = model.fromMap(map);
//                model.title = (String) map.get("title");
                Log.d(TAG, "onBindViewHolder: "+model);


                    final String userKEY = String.valueOf(model.uid);
                    //attach_url = model.attachment;
                    //initializePlayer();
                    // Set click listener for the whole post view

                    final String postKey = postRef.getKey();
                    Post finalModel = model;
                    viewHolder.EntireLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Count View
                            DatabaseReference globalPostRef = mDatabase.child("youtube-posts").child(postRef.getKey());
                            DatabaseReference userPostRef = mDatabase.child("user-posts").child(finalModel.uid).child(postRef.getKey());

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
                            intent.putExtra("videoTitle", viewHolder.title);
                            intent.putExtra("videoDescription", viewHolder.descriptiontxt);
                            startActivity(intent);
                        }
                    });

                    // Determine if the current user has liked this post and set UI accordingly
                    if (model.stars != null && model.stars.containsKey(getUid())) {
                        viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
                    } else {
                        viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                    }
                    // Determine if the current user has ability to delete and edit the post
                    if (userKEY.equals(getUid()) || CurrentUser.UserType != null && CurrentUser.UserType == UserTypes.Admin) {
                        viewHolder.optionView.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.optionView.setVisibility(View.GONE);
                    }
                    // Bind Post to ViewHolder, setting OnClickListener for the star button
                    viewHolder.bindToPost(model, new View.OnClickListener() {
                        @Override
                        public void onClick(View starView) {
                            // Need to write to both places the post is stored
                            DatabaseReference globalPostRef = mDatabase.child("youtube-posts").child(postRef.getKey());
                            DatabaseReference userPostRef = mDatabase.child("user-posts").child(finalModel.uid).child(postRef.getKey());

                            // Run two transactions
                            onStarClicked(globalPostRef);
                            onStarClicked(userPostRef);
                        }


                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View optionView) {
                            // Need to write to both places the post is stored
                            DatabaseReference globalPostRef = mDatabase.child("youtube-posts").child(postRef.getKey());
                            DatabaseReference userPostRef = mDatabase.child("user-posts").child(finalModel.uid).child(postRef.getKey());

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

                                            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("youtube-posts").child(postRef.getKey());
                                            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    dataSnapshot.getRef().removeValue();

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            DatabaseReference rootRef2 = FirebaseDatabase.getInstance().getReference().child("user-posts").child(finalModel.uid).child(postRef.getKey());
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
                        viewHolder.title = model.title;
                        viewHolder.descriptiontxt = model.body;
                        viewHolder.textView.setText(model.title);
                        viewHolder.description.setText(model.body);

                        try {
                            Glide.with(getContext())
                                    .load(model.Thumb_Url)
                                    .into(viewHolder.imageView);
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
                                            viewHolder.title = snippet.getString("title");
                                            viewHolder.descriptiontxt = snippet.getString("description");
                                            viewHolder.textView.setText(snippet.getString("title"));
                                            viewHolder.description.setText(snippet.getString("description"));

                                            try {
                                                Glide.with(getContext())
                                                        .load(thumbnailsHigh)
                                                        .into(viewHolder.imageView);
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

        };

        /*adapter.getSnapshots().add(adapter.getSnapshots().get(1));
        adapter.getSnapshots().add(adapter.getSnapshots().get(1));
        adapter.getSnapshots().add(adapter.getSnapshots().get(1));*/
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    return;
                }
                if (linearLayoutManager.findLastVisibleItemPosition() < adapter.getItemCount() - 20) {
                    return;
                }
                /*adapter.more();
                if(mPageSize > 0 && !isSyncing()) {
                    mCurrentSize += mPageSize;
                    setup();
                }*/
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }
        });

    }

    public static String getTimeDate(long timestamp){
        try{
            DateFormat dateFormat = getDateTimeInstance();
            Date netDate = (new Date(timestamp));
            return dateFormat.format(netDate);
        } catch(Exception e) {
            return "date";
        }
    }
    void addNewPost(int AddPostsNumber){



        if (!mGettingMoreposts){
            mGettingMoreposts = true;
            Query ref = FirebaseDatabase.getInstance().getReference()
                    .child("youtube-posts").orderByChild("createdAt")
                    .endBefore(mLastKey)
                    .limitToLast(AddPostsNumber)

                    ;
            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.exists()) {

                        loadedItems ++;

                        Post post = dataSnapshot.getValue(Post.class);
                    /*Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    Post post = new Post().fromMap(map);*/

                        /*if(!recyclerViewItems.contains(post)) {
//                            recyclerViewItems.add(post);
                            tempItems.add(post);

                        }*/
                        if (!mPostIds.contains(dataSnapshot.getKey())){
//                            Log.d(TAG, "onChildAdded: add new");
                            tempItems.add(post);
                            mPostIds.add(dataSnapshot.getKey());
//                            Log.d(TAG, "onChildAdded: add new" + tempItems.size());


                        }
//                Log.d(TAG, "onChildAdded: " +tempItems.size());
//                Log.d(TAG, "onChildAdded: " +recyclerViewItems.size());
                        Log.d(TAG, "onChildAdded: dsad " + loadedItems);
                        if ((loadedItems == AddPostsNumber || loadedItems == AddPostsNumber -1)&& loadedItems != 0 && tempItems.size()>0  ) {
                            Log.d(TAG, "onChildAdded: "+tempItems.size());

                            Post tempPost = (Post) tempItems.get(0);
                            mLastKey = tempPost.createdAt;
                            mGettingMoreposts = false;

//                        Collections.reverse(recyclerViewItems);

                            addNativeAds(tempItems);
                            loadNativeAds(tempItems);
                            recyclerViewItems.addAll(tempItems);

                            myListAdapter.setData(recyclerViewItems);
                            tempItems.clear();

//                        linearLayoutManager.setReverseLayout(false);
//                        linearLayoutManager.setStackFromEnd(false);
                            myListAdapter.notifyDataSetChanged();

                            loadedItems =0;

                        }
//                    Post tempPost = (Post) tempItems.get(0);

                        if (mLastKey.equals(post.createdAt)){
//                            Toast.makeText(getContext(), "no more", Toast.LENGTH_SHORT).show();
                            mGettingMoreposts = false;
                            loadedItems =0;
                        }


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

    }

    void addNews(int mPosts){
        Query ref = FirebaseDatabase.getInstance().getReference()
                .child("youtube-posts").orderByKey()
                .limitToLast(mPosts);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Log.d(TAG, "onDataChange: "+snapshot );
                if (snapshot.getValue() == null){
                    Progressloader.setVisibility(View.GONE);
                    NoPoststextView.setVisibility(View.VISIBLE);
                }else {
                    Progressloader.setVisibility(View.GONE);
                    NoPoststextView.setVisibility(View.GONE);
                }

//                Toast.makeText(getContext(), "finish", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                Progressloader.setVisibility(View.GONE);
                NoPoststextView.setVisibility(View.GONE);

                Post post = dataSnapshot.getValue(Post.class);

                /*final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                final MyPojo pojo = mapper.convertValue(map, MyPojo.class);*/
//                Log.d(TAG, "onChildAdded: "+post);

                recyclerViewItems.add(post);
                youtubeVideosArrayList.add(post);

                tempItems.add(post);
                mPostIds.add(dataSnapshot.getKey());
//                Log.d(TAG, "onChildAdded: " +tempItems.size());
//                Log.d(TAG, "onChildAdded: " +recyclerViewItems.size());
                if (tempItems.size() == mPosts) {
                    Post tempPost = (Post) tempItems.get(0);
                    mLastKey = tempPost.createdAt;


                    tempItems.clear();

                    Collections.reverse(recyclerViewItems);

                    addNativeAds(recyclerViewItems);
                    loadNativeAds(recyclerViewItems);
                    myListAdapter.setData(recyclerViewItems);

                    recyclerView.setAdapter(myListAdapter);
                    StorageUtil storage = new StorageUtil(getApplicationContext());
                    storage.storeYoutubeVideos(youtubeVideosArrayList);
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

    private void addBannerAds(List<Object> recyclerViewItems) {


        try {
            for (int i = 1; i <= recyclerViewItems.size(); i += ITEMS_PER_AD) {
                final AdView adView = new AdView(requireContext());
                adView.setAdSize(AdSize.BANNER);
                adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
                recyclerViewItems.add(i, adView);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadBannerAds(List<Object> recyclerViewItems) {
        // Load the first banner ad in the items list (subsequent ads will be loaded automatically
        // in sequence).
        loadBannerAd(1,recyclerViewItems);
    }

    private void loadBannerAd(final int index, List<Object> recyclerViewItems) {

        if (index >= recyclerViewItems.size()) {
            return;
        }

        Object item = recyclerViewItems.get(index);
        if (!(item instanceof AdView)) {
            return;
           /* throw new ClassCastException("Expected item at index " + index + " to be a banner ad"
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


    private void addNativeAds(List<Object> recyclerViewItems) {

        if (isSubscribedPremium)
            return;

        try {
            for (int i = 1; i <= recyclerViewItems.size(); i += ITEMS_PER_AD) {

                NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.ad_unified_youtube_posts, null);
//                NativeAdView adView = null;
//                AdLoader.Builder builder = new AdLoader.Builder(getContext(), getString(R.string.NATIVE_ADMOB_AD_UNIT_ID));
                recyclerViewItems.add(i, adView);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadNativeAds(List<Object> recyclerViewItems) {
        if (isSubscribedPremium)
            return;
        // Load the first banner ad in the items list (subsequent ads will be loaded automatically
        // in sequence).
        loadNativeAd(1,recyclerViewItems);
    }

    private void loadNativeAd(final int index, List<Object> recyclerViewItems) {

        if (index >= recyclerViewItems.size()) {
            return;
        }


        Object item = recyclerViewItems.get(index);
        if (!(item instanceof NativeAdView)) {
            return;
           /* throw new ClassCastException("Expected item at index " + index + " to be a banner ad"
                    + " ad.");*/
        }

//        final AdLoader.Builder builder = (AdLoader.Builder) item;
        AdLoader.Builder builder = new AdLoader.Builder(getContext(), getString(R.string.NATIVE_ADMOB_AD_UNIT_ID));


        builder.forNativeAd(
                new NativeAd.OnNativeAdLoadedListener() {
                    // OnLoadedListener implementation.
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        // If this callback occurs after the activity is destroyed, you must call
                        // destroy and return or you may get a memory leak.
                        boolean isDestroyed = false;
//                        viewHolder.refresh.setEnabled(true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            try {
                                isDestroyed = getActivity().isDestroyed();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (isDestroyed || getActivity().isFinishing() || getActivity().isChangingConfigurations()) {
                            nativeAd.destroy();
                            return;
                        }
                        // You must call destroy on old ads when you are done with them,
                        // otherwise you will have a memory leak.
                        /*if (viewHolder.nativeAd != null) {
                            viewHolder.nativeAd.destroy();
                        }
                        viewHolder.nativeAd = nativeAd;*/

//                        FrameLayout frameLayout = getView().findViewById(R.id.fl_adplaceholder);
//                        NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.ad_unified_youtube_posts, null);
                        NativeAdView adView = (NativeAdView) item;
                        populateNativeAdView(nativeAd, adView);
                        myListAdapter.notifyDataSetChanged();
                        loadNativeAd(index + ITEMS_PER_AD, recyclerViewItems);
//                        myListAdapter.notifyItemChanged(index);
//                        frameLayout.removeAllViews();
//                        frameLayout.addView(adView);

                    }
                });

        VideoOptions videoOptions =
                new VideoOptions.Builder().setStartMuted(true).build();

        com.google.android.gms.ads.nativead.NativeAdOptions adOptions =
                new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader =
                builder
                        .withAdListener(
                                new AdListener() {
                                    @Override
                                    public void onAdFailedToLoad(LoadAdError loadAdError) {
//                                        viewHolder.refresh.setEnabled(true);
                                        String error =
                                                String.format(
                                                        "domain: %s, code: %d, message: %s",
                                                        loadAdError.getDomain(),
                                                        loadAdError.getCode(),
                                                        loadAdError.getMessage());
                                            /*Toast.makeText(
                                                    getContext(),
                                                    "Failed to load native ad with error " + error,
                                                    Toast.LENGTH_SHORT)
                                                    .show();*/


//                                        recyclerViewItems.add(index,adView);

                                        Log.d(TAG, "onAdFailedToLoadindex: " + index + "---" + recyclerViewItems.size()  );
                                        loadNativeAd(index + ITEMS_PER_AD, recyclerViewItems);
//                                        recyclerViewItems.remove(index);
//                                        myListAdapter.notifyDataSetChanged();
                                        try {

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
//                                        recyclerViewItems.remove(index);


                                        Log.d(TAG, "onAdFailedToLoad native: " + error);
                                    }
                                })
                        .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }




    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view.
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every NativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        VideoController vc = nativeAd.getMediaContent().getVideoController();

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {


            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
//                    viewHolder.refresh.setEnabled(true);
//                    viewHolder.videoStatus.setText("Video status: Video playback has ended.");
                    super.onVideoEnd();
                }
            });
        } else {
//            viewHolder.videoStatus.setText("Video status: Ad does not contain a video asset.");
//            viewHolder.refresh.setEnabled(true);
        }
    }

    public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{
        private List<Object> postObject;
        private String lastSendedId  ;
        private Long lastSendedTimeStamp  ;

//        public int[] adsPositions ;
        private  List<Integer> adsPositions;


        YouTubeThumbnailView youTubeThumbnailView ;
        // RecyclerView recyclerView;


        public MyListAdapter(List<Object> recyclerViewItems) {
            this.postObject = recyclerViewItems;
        }

        public MyListAdapter(Context context) {

        }

        public void setData(List<Object> recyclerViewItems){
            this.postObject = recyclerViewItems;
        }

        public String getLastItemId() {

            String returned ;
//            Post post = (Post) postObject.get(postObject.size()-1);
            Post post = (Post) postObject.get(mFirstPostsNumber -1);
            if (lastSendedId != post.id){
                lastSendedId = post.id;
                returned =  post.id;
            }else {
                returned =  null ;
            }
            Log.d(TAG, "getLastItemId: " +returned);

            return returned;

        }
        public Long getLastItemTimeStamp(){
            Long returned ;
            Post post = (Post) postObject.get(postObject.size()-1);
//            Post post = (Post) postObject.get(0);
            if (!lastSendedTimeStamp.equals(post.createdAt)){
                lastSendedTimeStamp = post.createdAt;
                returned =  post.createdAt;
            }else {
                returned =  null ;
            }
            Log.d(TAG, "getLastItemId: " +returned);

            return returned;
        }
        private void onShareBy( String videoTitle,String videoDescriptiontxt,String YouTubeVideoId,String Thumb_Url, String postId, String postTitle) {


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
                        createReport(shortLink.toString(),postId,postTitle);


                    } else {
                        Log.d(TAG, "createDynamicLink 2: " +task);
                        // Error
                        // ...
                    }
                }

            });


        }

        private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView,ViewHolder viewHolder) {
            // Set the media view.
            adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));

            // Set other ad assets.
            adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
            adView.setBodyView(adView.findViewById(R.id.ad_body));
            adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
            adView.setIconView(adView.findViewById(R.id.ad_app_icon));
            adView.setPriceView(adView.findViewById(R.id.ad_price));
            adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
            adView.setStoreView(adView.findViewById(R.id.ad_store));
            adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

            // The headline and mediaContent are guaranteed to be in every NativeAd.
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
            adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

            // These assets aren't guaranteed to be in every NativeAd, so it's important to
            // check before trying to display them.
            if (nativeAd.getBody() == null) {
                adView.getBodyView().setVisibility(View.INVISIBLE);
            } else {
                adView.getBodyView().setVisibility(View.VISIBLE);
                ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
            }

            if (nativeAd.getCallToAction() == null) {
                adView.getCallToActionView().setVisibility(View.INVISIBLE);
            } else {
                adView.getCallToActionView().setVisibility(View.VISIBLE);
                ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
            }

            if (nativeAd.getIcon() == null) {
                adView.getIconView().setVisibility(View.GONE);
            } else {
                ((ImageView) adView.getIconView()).setImageDrawable(
                        nativeAd.getIcon().getDrawable());
                adView.getIconView().setVisibility(View.VISIBLE);
            }

            if (nativeAd.getPrice() == null) {
                adView.getPriceView().setVisibility(View.INVISIBLE);
            } else {
                adView.getPriceView().setVisibility(View.VISIBLE);
                ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
            }

            if (nativeAd.getStore() == null) {
                adView.getStoreView().setVisibility(View.INVISIBLE);
            } else {
                adView.getStoreView().setVisibility(View.VISIBLE);
                ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
            }

            if (nativeAd.getStarRating() == null) {
                adView.getStarRatingView().setVisibility(View.INVISIBLE);
            } else {
                ((RatingBar) adView.getStarRatingView())
                        .setRating(nativeAd.getStarRating().floatValue());
                adView.getStarRatingView().setVisibility(View.VISIBLE);
            }

            if (nativeAd.getAdvertiser() == null) {
                adView.getAdvertiserView().setVisibility(View.INVISIBLE);
            } else {
                ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
                adView.getAdvertiserView().setVisibility(View.VISIBLE);
            }

            // This method tells the Google Mobile Ads SDK that you have finished populating your
            // native ad view with this native ad.
            adView.setNativeAd(nativeAd);

            // Get the video controller for the ad. One will always be provided, even if the ad doesn't
            // have a video asset.
            VideoController vc = nativeAd.getMediaContent().getVideoController();

            // Updates the UI to say whether or not this ad has a video asset.
            if (vc.hasVideoContent()) {


                // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
                // VideoController will call methods on this object when events occur in the video
                // lifecycle.
                vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                    @Override
                    public void onVideoEnd() {
                        // Publishers should allow native ads to complete video playback before
                        // refreshing or replacing them with another ad in the same UI location.
                        viewHolder.refresh.setEnabled(true);
                        viewHolder.videoStatus.setText("Video status: Video playback has ended.");
                        super.onVideoEnd();
                    }
                });
            } else {
                viewHolder.videoStatus.setText("Video status: Ad does not contain a video asset.");
                viewHolder.refresh.setEnabled(true);
            }
        }


        private void refreshAd(ViewHolder viewHolder) {
            viewHolder.refresh.setEnabled(false);

            AdLoader.Builder builder = new AdLoader.Builder(getContext(), getString(R.string.NATIVE_ADMOB_AD_UNIT_ID));

            builder.forNativeAd(
                    new NativeAd.OnNativeAdLoadedListener() {
                        // OnLoadedListener implementation.
                        @Override
                        public void onNativeAdLoaded(NativeAd nativeAd) {
                            // If this callback occurs after the activity is destroyed, you must call
                            // destroy and return or you may get a memory leak.
                            boolean isDestroyed = false;
                            viewHolder.refresh.setEnabled(true);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                try {
                                    isDestroyed = getActivity().isDestroyed();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (isDestroyed || getActivity().isFinishing() || getActivity().isChangingConfigurations()) {
                                nativeAd.destroy();
                                return;
                            }
                            // You must call destroy on old ads when you are done with them,
                            // otherwise you will have a memory leak.
                            if (viewHolder.nativeAd != null) {
                                viewHolder.nativeAd.destroy();
                            }
                            viewHolder.nativeAd = nativeAd;
                            FrameLayout frameLayout = getView().findViewById(R.id.fl_adplaceholder);
                            NativeAdView adView =
                                    (NativeAdView) getLayoutInflater().inflate(R.layout.ad_unified_youtube_posts, null);
                            populateNativeAdView(nativeAd, adView,viewHolder);
                            frameLayout.removeAllViews();
                            frameLayout.addView(adView);
                        }
                    });

            VideoOptions videoOptions =
                    new VideoOptions.Builder().setStartMuted(viewHolder.startVideoAdsMuted.isChecked()).build();

            com.google.android.gms.ads.nativead.NativeAdOptions adOptions =
                    new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();

            builder.withNativeAdOptions(adOptions);

            AdLoader adLoader =
                    builder
                            .withAdListener(
                                    new AdListener() {
                                        @Override
                                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                                            viewHolder.refresh.setEnabled(true);
                                            String error =
                                                    String.format(
                                                            "domain: %s, code: %d, message: %s",
                                                            loadAdError.getDomain(),
                                                            loadAdError.getCode(),
                                                            loadAdError.getMessage());
                                            /*Toast.makeText(
                                                    getContext(),
                                                    "Failed to load native ad with error " + error,
                                                    Toast.LENGTH_SHORT)
                                                    .show();*/
                                        }
                                    })
                            .build();

            adLoader.loadAd(new AdRequest.Builder().build());

            viewHolder.videoStatus.setText("");
        }
        @Override
        public MyListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.item_youtube_post, parent, false);
            MyListAdapter.ViewHolder viewHolder = new MyListAdapter.ViewHolder(listItem);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyListAdapter.ViewHolder viewHolder, int  position) {


            /*if (postObject.get(position) instanceof AdView){
                AdView adView = (AdView) postObject.get(position);

                if (adsPositions== null)
                    adsPositions = new ArrayList<Integer>();

                if (!adsPositions.contains(position)) {

                    refreshAd(viewHolder);
                    adsPositions.add(position);
                    if(adView.getParent() != null) {
                        ((ViewGroup)adView.getParent()).removeView(adView); // <- fix
                    }
//                viewHolder.cardview.addView(adView); //  <==========  ERROR IN THIS LINE DURING 2ND RUN
                    viewHolder.cardview.setVisibility(View.VISIBLE);
                    viewHolder.cardContent.setVisibility(View.GONE);

                    viewHolder.EntireLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                }
                if(adView.getParent() != null) {
                    ((ViewGroup)adView.getParent()).removeView(adView); // <- fix
                }
//                viewHolder.cardview.addView(adView); //  <==========  ERROR IN THIS LINE DURING 2ND RUN
                viewHolder.cardview.setVisibility(View.VISIBLE);
                viewHolder.cardContent.setVisibility(View.GONE);

                viewHolder.EntireLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });



            }else */if (postObject.get(position) instanceof NativeAdView){
                NativeAdView adView = (NativeAdView) postObject.get(position);

                if(adView.getParent() != null) {
                    ((ViewGroup)adView.getParent()).removeView(adView); // <- fix
                }
                viewHolder.frameLayout.removeAllViews();

                if ((adView.getHeadlineView()) == null){
                    viewHolder.cardview.setVisibility(View.GONE);
                    viewHolder.cardContent.setVisibility(View.GONE);
                    viewHolder.frameLayout.removeAllViews();
                }else {
                    viewHolder.frameLayout.addView(adView);
//                viewHolder.cardview.addView(adView); //  <==========  ERROR IN THIS LINE DURING 2ND RUN
                    viewHolder.cardview.setVisibility(View.VISIBLE);
                    viewHolder.cardContent.setVisibility(View.GONE);
                }

                Log.d(TAG, "onBindViewHolderyoutube: " + (adView.getHeadlineView()));



                viewHolder.EntireLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

            }else
            {
                viewHolder.cardview.setVisibility(View.GONE);
                viewHolder.cardContent.setVisibility(View.VISIBLE);

//                        final DatabaseReference postRef = getRef(position);


//                Map<String, Object> map = (Map<String, Object>) postObject.get(position);

                Post model = new Post();
                model = (Post) postObject.get(position);


                    if (model.createdAt != null){
                        Log.d(TAG, "onBindViewHolder: " + getTimeDate(model.createdAt));

                    }
                    final String userKEY = String.valueOf(model.uid);
                    //attach_url = model.attachment;
                    //initializePlayer();
                    // Set click listener for the whole post view

                    final String postKey = model.id;
                    Post finalModel = model;
                    viewHolder.EntireLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Count View
                            DatabaseReference globalPostRef = mDatabase.child("youtube-posts").child(finalModel.id);
                            DatabaseReference userPostRef = mDatabase.child("user-posts").child(finalModel.uid).child(finalModel.id);
                            int currentCount = Integer.parseInt(viewHolder.Views.getText().toString());
                            viewHolder.Views.setText( String.valueOf(currentCount + 1));
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
                            intent.putExtra("videoTitle", viewHolder.title);
                            intent.putExtra("videoDescription", viewHolder.descriptiontxt);
                            startActivity(intent);
                            createReport(null,postKey,viewHolder.title);

                        }
                    });

                    // Determine if the current user has liked this post and set UI accordingly
                    if (model.stars != null && model.stars.containsKey(getUid())) {
                        viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
                    } else {
                        viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                    }
                    // Determine if the current user has ability to delete and edit the post
                    if (userKEY.equals(getUid()) || CurrentUser.UserType != null && CurrentUser.UserType == UserTypes.Admin) {
                        viewHolder.optionView.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.optionView.setVisibility(View.GONE);
                    }
                    // Bind Post to ViewHolder, setting OnClickListener for the star button
                    viewHolder.bindToPost(model, new View.OnClickListener() {
                        @Override
                        public void onClick(View starView) {
                            // Need to write to both places the post is stored
                            DatabaseReference globalPostRef = mDatabase.child("youtube-posts").child(finalModel.id);
                            DatabaseReference userPostRef = mDatabase.child("user-posts").child(finalModel.uid).child(finalModel.id);



                            // Run two transactions
                            onStarClickedWithViewHolder(globalPostRef,viewHolder,postKey,viewHolder.title,false);
                            onStarClickedWithViewHolder(userPostRef, viewHolder,postKey,viewHolder.title,true);
                        }


                    },new View.OnClickListener() {
                        @Override
                        public void onClick(View shareView) {
                            // Need to write to both places the post is stored

                            onShareBy(viewHolder.title,finalModel.body,finalModel.YouTubeVideoId,finalModel.Thumb_Url,postKey,viewHolder.title);
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
                        viewHolder.title = model.title;
                        viewHolder.descriptiontxt = model.body;
                        viewHolder.textView.setText(model.title);
                        viewHolder.description.setText(model.body);

                        try {
                            Glide.with(getContext())
                                    .load(model.Thumb_Url)
                                    .into(viewHolder.imageView);
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
                                            viewHolder.title = snippet.getString("title");
                                            viewHolder.descriptiontxt = snippet.getString("description");
                                            viewHolder.textView.setText(snippet.getString("title"));
                                            viewHolder.description.setText(snippet.getString("description"));

                                            try {
                                                Glide.with(getContext())
                                                        .load(thumbnailsHigh)
                                                        .into(viewHolder.imageView);
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

        }
        public  class ViewHolder extends RecyclerView.ViewHolder {



            CardView cardview;
            CardView cardContent;


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

                frameLayout = itemView.findViewById(R.id.fl_adplaceholder);
                cardview = (CardView) itemView.findViewById(R.id.cardviewad);
                cardContent = (CardView) itemView.findViewById(R.id.cardContent);


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
            return postObject.size();
        }

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

    /*public void more() {
        if(mPageSize > 0 && !isSyncing()) {
            mCurrentSize += mPageSize;
            setup();
        }
    }*/
    // [START post_stars_transaction]
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

                        createReportStarUnStar(postId,postTitle,false);
                    }

                } else {
                    // Star the post and add self to stars
                    p.starCount = p.starCount + 1;
                    p.stars.put(getUid(), true);
//                    int currentCount = Integer.parseInt(viewHolder.numStarsView.getText().toString());
                    viewHolder.numStarsView.setText(String.valueOf(p.starCount));
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);

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
// [END post_stars_transaction]
public class ViewHolder extends RecyclerView.ViewHolder {
    public TextView titleView;
    public TextView authorView;
    public TextView Views;
    public ImageView starView;
    public RelativeLayout EntireLayout;
    public RelativeLayout star_layout;
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

    public User user4;


    public ViewHolder(View itemView) {
        super(itemView);

        titleView = itemView.findViewById(R.id.post_title);
        authorView = itemView.findViewById(R.id.post_author);
        starView = itemView.findViewById(R.id.star);
        EntireLayout = itemView.findViewById(R.id.EntireLayout);
        star_layout = itemView.findViewById(R.id.star_layout);
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



    }

    public void bindToPost(Object model, View.OnClickListener starClickListener ,View.OnClickListener optionClickListener ) {
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
        optionView.setOnClickListener(optionClickListener);
    }


}
}
