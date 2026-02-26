package com.medoapps.www.onlinequran.fragment;

/*

public abstract class UserListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener ,Toolbar.OnMenuItemClickListener {

    private static final String TAG = "PostListFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    private DatabaseReference uDatabase;
    // [END define_database_reference]

    public static  FirebaseRecyclerAdapter<User, UserViewHolder> mAdapter;
    private FirebaseRecyclerAdapter<User, UserViewHolder> uAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private static final String CHANNEL_ID_DEFAULT = "default";
    static final int FINISHED_NOTIFICATION_ID = 1;


    SwipeRefreshLayout mSwipeRefreshLayout;

    public UserListFragment() {}


    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_users, container, false);
        setHasOptionsMenu(true);
        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        uDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        // [END create_database_reference]
        //playerView = rootView.findViewById(R.id.video_view);
        mRecycler = rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);


        // SwipeRefreshLayout
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);


        */
/**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         *//*

        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);
                loadData();
                // Fetching data from server
                //loadRecyclerViewData();
            }

        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    */
/**
     * This method is called when swipe refresh is pulled down
     *//*

    @Override
    public void onRefresh() {
        loadData();
    }







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
    private void onDeleteClicked(DatabaseReference postRef) {
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


    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
        if (uAdapter != null) {
            uAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
        if (uAdapter != null) {
            uAdapter.stopListening();
        }
    }


    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);


    public void loadData(){

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query usersQuery = getQuery(mDatabase);


        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(usersQuery, User.class )
                .build();



        mAdapter = new FirebaseRecyclerAdapter<User ,UserViewHolder>(options) {

            @Override
            public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new UserViewHolder(inflater.inflate(R.layout.item_user, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(UserViewHolder viewHolder, int position, final User model) {
                final DatabaseReference userRef = getRef(position);

                //final String userKEY = String.valueOf(model.email);
                //attach_url = model.attachment;
                //initializePlayer();
                // Set click listener for the whole post view
                String caption =getString(R.string.newPost);
                // Make Intent to MainActivity
                Intent intent = new Intent(getContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //showFinishedNotification(caption, intent, true);
                final String postKey = userRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Count View
                        DatabaseReference globalPostRef = mDatabase.child("posts").child(userRef.getKey());
                        //DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(userRef.getKey());


                        Intent intent = new Intent(getActivity(), ChatPage.class);
                        //intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                        //intent.putExtra(PostDetailActivity.EXTRA_USER_KEY, userKEY);
                        startActivity(intent);
                    }
                });

                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        Intent intent = new Intent(getActivity(), ChatPage.class);
                        //intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                        //intent.putExtra(PostDetailActivity.EXTRA_USER_KEY, userKEY);
                        startActivity(intent);
                    }


                });




                mSwipeRefreshLayout.setRefreshing(false);
            }
        };
        mRecycler.setAdapter(mAdapter);



    }

    public  void showFinishedNotification(String caption, Intent intent, boolean success) {
        // Make PendingIntent for notification
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0 */
/* requestCode *//*
, intent,
                PendingIntent.FLAG_MUTABLE);

        int icon = success ? R.drawable.ic_check_white_24 : R.drawable.ic_error_white_24dp;

        createDefaultChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID_DEFAULT)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(caption)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);

        manager.notify(FINISHED_NOTIFICATION_ID, builder.build());
    }

    private void createDefaultChannel() {
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_DEFAULT,
                    "Default",
                    NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(channel);
        }
    }
    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.mystream : R.drawable.mystreamwhite;
    }
    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) this);
        popup.inflate(R.menu.post_option);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.post_edit:

                Toast.makeText(getActivity().getApplicationContext(), "edit", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.post_delete:
                Toast.makeText(getActivity().getApplicationContext(), "delete", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }
}
*/
