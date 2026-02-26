package com.medoapps.www.onlinequran.admin.fragment.report;

import static com.facebook.FacebookSdk.getApplicationContext;
import static java.text.DateFormat.getDateTimeInstance;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.models.Report;
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class AdminDashReportsForUser extends Fragment {


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
    private final boolean orderASC = true;

    MyListAdapter myListAdapter;

    private final List<Object> recyclerViewItems = new ArrayList<>();
    List<Object> tempItems = new ArrayList<>();

    private final int mFirstGetUsersNumber = 10;
    private final int mAddPostsNumber = 10;
    private int loadMoreNumber = 1;
    private String lastItemIdInList ;
    private Long mGetMoreUsersNumber;
    private boolean mGettingMoreposts;
    public static final int ITEMS_PER_AD = 9;
    RecyclerView.SmoothScroller smoothScroller;

    String mLastKey ;
    private final List<String> mPostIds = new ArrayList<>();
    Long usersCount = Long.valueOf(0);
    TextView usersNumber;
    TextView usersAddedNumber;
    int lastAdapterItemId;

    ExtendedFloatingActionButton loadMore_fab;

//    String userId ;

    TextView userName;
    ImageView author_photo;

    private static final String ARG_PARAM1 = "param1";
    private String userId;

   /* public AdminDashReportsForUser(String userId) {
        this.userId = userId;
    }
    public AdminDashReportsForUser() {

    }*/



    public static AdminDashReportsForUser newInstance(String userId) {
        AdminDashReportsForUser fragment = new AdminDashReportsForUser();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_PARAM1);

            Log.d(TAG, "onCreatefff: " + userId);
        }


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_PARAM1);

            Log.d(TAG, "onCreatefff: " + userId);
        }
        View view =  inflater.inflate(R.layout.fragment_admin_dash_reports_for_user, container, false);
        Progressloader = view.findViewById(R.id.Progressloader);
        NoPoststextView = view.findViewById(R.id.NoPoststextView);
        NetworkErrortextView = view.findViewById(R.id.NetworkErrortextView);
        usersNumber = view.findViewById(R.id.usersNumber);
        usersAddedNumber = view.findViewById(R.id.usersAddedNumber);
        loadMore_fab = view.findViewById(R.id.loadMore_fab);
        userName = view.findViewById(R.id.userName);
        author_photo = view.findViewById(R.id.author_photo);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(getUid());

        Progressloader.setVisibility(View.VISIBLE);
        NoPoststextView.setVisibility(View.GONE);
        NetworkErrortextView.setVisibility(View.GONE);
        loadMore_fab.hide();
        recyclerView = view.findViewById(R.id.messages_list);

        linearLayoutManager = new LinearLayoutManager(getContext());
//        linearLayoutManager.setReverseLayout(true);
//        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        SeparateFunctions separateFunctions = new SeparateFunctions(getContext());

        if (separateFunctions.isNetworkAvailable()){

            if (userId != null){

                getCurrentUser();
            }


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

        mPageSize = mCurrentSize = Math.abs(2);
        mOrderASC = orderASC;
        scrollToTop();
        loadMore_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lastAdapterItemId>= (mFirstGetUsersNumber-1)*loadMoreNumber ){
                    Log.d(TAG, "onScrolled: " + lastAdapterItemId);

                    if (usersCount- myListAdapter.getItemCount() < mAddPostsNumber){
                        if (usersCount- myListAdapter.getItemCount()>=1){
                            loadMoreNumber = loadMoreNumber+1;
                            loadMoreUsers(Integer.valueOf(String.valueOf(usersCount- myListAdapter.getItemCount())));

                        }


                    }else{
                        loadMoreNumber = loadMoreNumber+1;

                        loadMoreUsers(mAddPostsNumber);
                    }

                }else if (usersCount- myListAdapter.getItemCount() < mAddPostsNumber){
                    if (usersCount- myListAdapter.getItemCount()>=1){
                        loadMoreNumber = loadMoreNumber+1;
                        loadMoreUsers(Integer.valueOf(String.valueOf(usersCount- myListAdapter.getItemCount())));

                    }


                }
                loadMore_fab.hide();
            }
        });
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

    }

    public  void scrollToTop(){
        if (recyclerView!= null){

            smoothScroller.setTargetPosition(0);

            linearLayoutManager.startSmoothScroll(smoothScroller);
        }




        Log.d(TAG, "scrollToTop: ");
    }


    public void getCurrentUser(){
        if (userId == null)
            return;
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    User url = dataSnapshot.getValue(User.class);
                    userName.setText(url.username);
                    try {
                        Glide.with(getApplicationContext()).load(url.photourl).into(author_photo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    CurrentUser = url;
                    getUsersCount();

                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            int id = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                            lastAdapterItemId = id;
                            if(id>= (mFirstGetUsersNumber-1)*loadMoreNumber ){
                                loadMore_fab.show();

                            }else if (usersCount- myListAdapter.getItemCount() < mAddPostsNumber){

                                if (usersCount- myListAdapter.getItemCount()>=1){
                                    loadMore_fab.show();

                                }else{
                                    loadMore_fab.hide();
                                }

                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
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
    private void getUsersCount(){
        FirebaseDatabase.getInstance().getReference()
                .child("reports").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: " + snapshot.getChildrenCount());
                usersCount = snapshot.getChildrenCount();
                usersNumber.setText(usersCount.toString());

                if (usersCount>=mFirstGetUsersNumber ){

                    getUsers(mFirstGetUsersNumber);
                }else{
                    if (usersCount>=1){

                        getUsers(Integer.valueOf(String.valueOf(usersCount)));
                    }else{
                        Progressloader.setVisibility(View.GONE);
                        NoPoststextView.setVisibility(View.VISIBLE);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void loadMoreUsers(int AddPostsNumber){


        Log.d(TAG, "loadMoreUsersfsdf: " + AddPostsNumber);
        if (!mGettingMoreposts){
            Progressloader.setVisibility(View.VISIBLE);

            mGettingMoreposts = true;
            Query ref = FirebaseDatabase.getInstance().getReference()
                    .child("reports").child(userId).orderByKey()
                    .startAfter(mLastKey)
                    .limitToFirst(AddPostsNumber)
//                    .endBefore(mLastKey)
//                    .limitToFirst(AddPostsNumber)
                    ;
            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.exists()) {


                        Report report = dataSnapshot.getValue(Report.class);

//                        report.id = dataSnapshot.getKey();
                        if (!mPostIds.contains(report.id)){
//                            Log.d(TAG, "onChildAdded: add new");
                            tempItems.add(report);
                            mPostIds.add(dataSnapshot.getKey());
//                            Log.d(TAG, "onChildAdded: add new" + tempItems.size());


                        }
                        /*if(!recyclerViewItems.contains(user)) {

                        }*/
                        if (tempItems.size() >= AddPostsNumber-1) {
                            Log.d(TAG, "onChildAdded: add new tempItems");

                            Report tempPost = (Report) tempItems.get(tempItems.size()-1);
//                            mGetMoreUsersNumber = tempPost.createdAt;
                            mLastKey = tempPost.id;
                            Log.d(TAG, "onChildAdded new: "+mLastKey);


                            mGettingMoreposts = false;

                            recyclerViewItems.addAll(tempItems);

                            myListAdapter.setData(recyclerViewItems);
                            tempItems.clear();

                            myListAdapter.notifyDataSetChanged();
                            Progressloader.setVisibility(View.GONE);


                        }

                        /*if (mLastKey.equals(tempItems.get(0))){
                            Toast.makeText(getContext(), "no more", Toast.LENGTH_SHORT).show();
                            mGettingMoreposts = false;

                        }*/

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

    void getUsers(int mPosts){
        Log.d(TAG, "getUsers: gdfgfd" +mPosts);

        Query ref = FirebaseDatabase.getInstance().getReference()
                .child("reports").child(userId).orderByKey()
                .limitToFirst(mPosts);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null){
                    Progressloader.setVisibility(View.GONE);
                    NoPoststextView.setVisibility(View.VISIBLE);
                }else {
                    Progressloader.setVisibility(View.GONE);
                    NoPoststextView.setVisibility(View.GONE);
                }

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

                Report report = new Report();

                report = dataSnapshot.getValue(Report.class);
//                report.id = dataSnapshot.getKey();
//                Log.d(TAG, "onChildAdded: "+user.id);
                recyclerViewItems.add(report);
                tempItems.add(report);
                mPostIds.add(dataSnapshot.getKey());
                if (tempItems.size() == mPosts) {
                    Report tempPost = (Report) tempItems.get(tempItems.size()-1);
//                    mGetMoreUsersNumber = tempPost.createdAt;
                    mLastKey = tempPost.id;
                    Log.d(TAG, "onChildAdded: "+mLastKey);




                    tempItems.clear();

                    Collections.reverse(recyclerViewItems);

                    myListAdapter.setData(recyclerViewItems);

                    recyclerView.setAdapter(myListAdapter);

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

    private void openBottomSheetDialogModal(Report report){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.modal_bottom_user_report);

        LinearLayout ReciterLayout = bottomSheetDialog.findViewById(R.id.ReciterLayout);
        LinearLayout surahLayout = bottomSheetDialog.findViewById(R.id.surahLayout);
        LinearLayout RadioLayout = bottomSheetDialog.findViewById(R.id.RadioLayout);
        LinearLayout YouTubePostLayout = bottomSheetDialog.findViewById(R.id.YouTubePostLayout);
        LinearLayout LocalPostLayout = bottomSheetDialog.findViewById(R.id.LocalPostLayout);

        ImageView userPhoto = bottomSheetDialog.findViewById(R.id.userPhoto);
        TextView userName = bottomSheetDialog.findViewById(R.id.userName);
        TextView reportType = bottomSheetDialog.findViewById(R.id.reportType);
        TextView createdAt = bottomSheetDialog.findViewById(R.id.createdAt);
        TextView text = bottomSheetDialog.findViewById(R.id.text);
        TextView openReciterName = bottomSheetDialog.findViewById(R.id.openReciterName);
        TextView shareReciterUrl = bottomSheetDialog.findViewById(R.id.shareReciterUrl);
        TextView openSurahName = bottomSheetDialog.findViewById(R.id.openSurahName);
        TextView shareSurahUrl = bottomSheetDialog.findViewById(R.id.shareSurahUrl);
        TextView openRadioName = bottomSheetDialog.findViewById(R.id.openRadioName);
        TextView shareRadioUrl = bottomSheetDialog.findViewById(R.id.shareRadioUrl);

        TextView openYouTubePostTitle = bottomSheetDialog.findViewById(R.id.openYouTubePostTitle);
        TextView shareYouTubePostUrl = bottomSheetDialog.findViewById(R.id.shareYouTubePostUrl);
        TextView starYouTubePostTitle = bottomSheetDialog.findViewById(R.id.starYouTubePostTitle);
        TextView unStarYouTubePostTitle = bottomSheetDialog.findViewById(R.id.unStarYouTubePostTitle);

        TextView openLocalPostTitle = bottomSheetDialog.findViewById(R.id.openLocalPostTitle);
        TextView shareLocalPostUrl = bottomSheetDialog.findViewById(R.id.shareLocalPostUrl);
        TextView starLocalPostTitle = bottomSheetDialog.findViewById(R.id.starLocalPostTitle);
        TextView unStarLocalPostTitle = bottomSheetDialog.findViewById(R.id.unStarLocalPostTitle);


        ReciterLayout.setVisibility(View.GONE);
        surahLayout.setVisibility(View.GONE);
        RadioLayout.setVisibility(View.GONE);
        YouTubePostLayout.setVisibility(View.GONE);
        LocalPostLayout.setVisibility(View.GONE);

        switch (report.reportType){
            case OpenReciter :
            case ShareReciter:
                ReciterLayout.setVisibility(View.VISIBLE);
                break;

            case OpenSurah:
            case ShareSurah:
                surahLayout.setVisibility(View.VISIBLE);
                ReciterLayout.setVisibility(View.VISIBLE);
                break;

            case OpenRadio:
            case ShareRadio:
                RadioLayout.setVisibility(View.VISIBLE);
                break;

            case OpenYouTubePost:
            case ShareYouTubePost:
            case StarYouTubePost:
            case UnStarYouTubePost:
                YouTubePostLayout.setVisibility(View.VISIBLE);
                break;

            case OpenLocalPost:
            case ShareLocalPost:
            case StarLocalPost:
            case UnStarLocalPost:
                LocalPostLayout.setVisibility(View.VISIBLE);
                break;



            default:
//                throw new IllegalStateException("Unexpected value: " + report.reportType);
        }

        try {
            Glide.with(getApplicationContext()).load(report.userPhoto).into(userPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(userName).setText(report.userName);
        Objects.requireNonNull(text).setText(report.text);
        Objects.requireNonNull(reportType).setText(report.reportType.toString());
        Objects.requireNonNull(openReciterName).setText(report.openReciterName);
        Objects.requireNonNull(shareReciterUrl).setText(report.shareReciterUrl);
        Objects.requireNonNull(openSurahName).setText(report.openSurahName);
        Objects.requireNonNull(shareSurahUrl).setText(report.shareSurahUrl);
        Objects.requireNonNull(openRadioName).setText(report.openRadioName);
        Objects.requireNonNull(shareRadioUrl).setText(report.shareRadioUrl);
        Objects.requireNonNull(openYouTubePostTitle).setText(report.openYouTubePostTitle);
        Objects.requireNonNull(shareYouTubePostUrl).setText(report.shareYouTubePostUrl);
        Objects.requireNonNull(starYouTubePostTitle).setText(report.starYouTubePostTitle);
        Objects.requireNonNull(unStarYouTubePostTitle).setText(report.unStarYouTubePostTitle);
        Objects.requireNonNull(openLocalPostTitle).setText(report.openLocalPostTitle);
        Objects.requireNonNull(shareLocalPostUrl).setText(report.shareLocalPostUrl);
        Objects.requireNonNull(starLocalPostTitle).setText(report.starLocalPostTitle);
        Objects.requireNonNull(unStarLocalPostTitle).setText(report.unStarLocalPostTitle);



        Objects.requireNonNull(createdAt).setText(new SeparateFunctions(getContext()).getTimeDateFromTimeStamp(report.createdAt));


        /*TextInputLayout reportTextField = bottomSheetDialog.findViewById(R.id.reportTextField);
        bottomSheetDialog.findViewById(R.id.completeReport).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });*/



        bottomSheetDialog.show();
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
            usersAddedNumber.setText(String.valueOf(getItemCount()));
        }


        private void onShareBy( String videoTitle,String videoDescriptiontxt,String YouTubeVideoId,String Thumb_Url) {


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


                    } else {
                        Log.d(TAG, "createDynamicLink 2: " +task);
                        // Error
                        // ...
                    }
                }

            });


        }



        @Override
        public MyListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.item_admin_user_card, parent, false);
            MyListAdapter.ViewHolder viewHolder = new MyListAdapter.ViewHolder(listItem);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyListAdapter.ViewHolder viewHolder, int  position) {

            if (postObject.get(position) instanceof NativeAdView){

            }else
            {
                viewHolder.cardContent.setVisibility(View.VISIBLE);

                Report report = new Report();
                report = (Report) postObject.get(position);


                try {
                    Glide.with(getApplicationContext()).load(report.userPhoto).into(viewHolder.post_author_photo);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                viewHolder.user_email.setText(report.userName);
                viewHolder.user_name.setText(report.reportType.toString());

                Report finalReport = report;
                viewHolder.allLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openBottomSheetDialogModal(finalReport);
                    }
                });




            }

        }
        public  class ViewHolder extends RecyclerView.ViewHolder {



            CardView cardContent;
            TextView user_name;
            TextView user_email;
            ImageView post_author_photo;
            ImageView option;
            RelativeLayout allLayout;




            public ViewHolder(View itemView) {
                super(itemView);

                cardContent = itemView.findViewById(R.id.cardContent);
                user_name = itemView.findViewById(R.id.user_name);
                user_email = itemView.findViewById(R.id.user_email);
                post_author_photo = itemView.findViewById(R.id.post_author_photo);
                option = itemView.findViewById(R.id.option);
                allLayout = itemView.findViewById(R.id.allLayout);





            }
        }

        @Override
        public int getItemCount() {
            return postObject.size();
        }

    }


}