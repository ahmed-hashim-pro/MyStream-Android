package com.medoapps.www.onlinequran.fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.app.NotificationCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.medoapps.www.onlinequran.EditPost;
import com.medoapps.www.onlinequran.MainActivity;
import com.medoapps.www.onlinequran.PostDetailActivity;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.viewholder.PostViewHolder;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;

public abstract class PostListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener ,Toolbar.OnMenuItemClickListener {

    private static final String TAG = "PostListFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    public static  FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private FirebaseRecyclerAdapter<User, PostViewHolder> uAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private static final String CHANNEL_ID_DEFAULT = "default";
    static final int FINISHED_NOTIFICATION_ID = 1;

    private static final String PARAMS_TYPE = "params_type";
    public static final String TYPE_LINEAR = "type_linear";
    public static final String TYPE_GRID = "type_grid";
    private String mType;
    SwipeRefreshLayout mSwipeRefreshLayout;



    public PostListFragment() {}


    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);
        setHasOptionsMenu(true);
        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]
        //playerView = rootView.findViewById(R.id.video_view);
        mRecycler = rootView.findViewById(R.id.messages_list);



        // SwipeRefreshLayout
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);


        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
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
        loadData();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        loadData();
    }






    @Override
    public void onResume() {
        super.onResume();
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

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }

    }


    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);


    public void loadData(){

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getContext());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);
        mRecycler.setHasFixedSize(true);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);


        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(postsQuery, Post.class )
                .build();


        mAdapter = new FirebaseRecyclerAdapter<Post ,PostViewHolder>(options) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();

                mAdapter.notifyDataSetChanged();
                String caption =getString(R.string.newPost);
                // Make Intent to MainActivity
                Intent intent = new Intent(getContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                showFinishedNotification(caption, intent, true);
            }


            @Override
            public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());


                return new PostViewHolder(inflater.inflate(R.layout.item_post, viewGroup, false));

            }

            @Override
            protected void onBindViewHolder(PostViewHolder viewHolder, int position, final Post model) {
                final DatabaseReference postRef = getRef(position);

                final String userKEY = String.valueOf(model.uid);
                //attach_url = model.attachment;
                //initializePlayer();
                // Set click listener for the whole post view

                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Count View
                        DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                        DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());

                        // Run two transactions
                        onViewClicked(globalPostRef);
                        onViewClicked(userPostRef);
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                        intent.putExtra(PostDetailActivity.EXTRA_USER_KEY, userKEY);
                        startActivity(intent);
                    }
                });

                // Determine if the current user has liked this post and set UI accordingly
                if (model.stars.containsKey(getUid())) {
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
                } else {
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                }
                // Determine if the current user has ability to delete and edit the post
                if (userKEY.equals(getUid())) {
                    viewHolder.optionView.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.optionView.setVisibility(View.GONE);
                }
                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        // Need to write to both places the post is stored
                        DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                        DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());

                        // Run two transactions
                        onStarClicked(globalPostRef);
                        onStarClicked(userPostRef);
                    }


                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View optionView) {
                        // Need to write to both places the post is stored
                        DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                        DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());

                        PopupMenu popup = new PopupMenu(getContext(), optionView);
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.post_option, popup.getMenu());
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.post_edit:

                                        // Launch PostDetailActivity
                                        Intent intent = new Intent(getActivity(), EditPost.class);
                                        intent.putExtra(EditPost.EXTRA_POST_KEY, postKey);
                                        intent.putExtra(EditPost.EXTRA_USER_KEY, userKEY);
                                        startActivity(intent);

                                        break;

                                    case R.id.post_delete:

                                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("posts").child(postRef.getKey());
                                        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                dataSnapshot.getRef().removeValue();

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                        DatabaseReference rootRef2 = FirebaseDatabase.getInstance().getReference().child("user-posts").child(model.uid).child(postRef.getKey());
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
                                            childUpdates.remove("/posts/" +postKey );
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



                mSwipeRefreshLayout.setRefreshing(false);
            }
        };

        mRecycler.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        



    }

    public  void showFinishedNotification(String caption, Intent intent, boolean success) {
        // Make PendingIntent for notification
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0 /* requestCode */, intent,
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
