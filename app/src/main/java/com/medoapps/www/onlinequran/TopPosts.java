package com.medoapps.www.onlinequran;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.models.User;

import java.util.HashMap;
import java.util.Map;

public class TopPosts extends Fragment {
    private static final String TAG = "RecentPosts";
    private EditText editText, etd;
    private Button button;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter adapter;
    private DatabaseReference mDatabase;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_recent_posts, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        recyclerView = getView().findViewById(R.id.messages_list);

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        fetch();
    }


    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    private void fetch() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("posts").orderByChild("starCount");

        FirebaseRecyclerOptions<Post> options =
                new FirebaseRecyclerOptions.Builder<Post>()
                        .setQuery(query, Post.class )
                        .build();

        adapter = new FirebaseRecyclerAdapter<Post, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_post, parent, false);

                return new ViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(ViewHolder viewHolder, final int position, final Post model) {
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
                        Intent intent = new Intent(getContext(), PostDetailActivity.class);
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
                                        Intent intent = new Intent(getContext(), EditPost.class);
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


            }

        };
        recyclerView.setAdapter(adapter);
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
        public ImageView optionView;
        public TextView numStarsView;
        public TextView bodyView;
        public ImageView pPictureView;
        public ImageView ThumbImage;

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
            optionView = itemView.findViewById(R.id.option);
            numStarsView = itemView.findViewById(R.id.post_num_stars);
            bodyView = itemView.findViewById(R.id.post_body);
            pPictureView = itemView.findViewById(R.id.post_author_photo);
            //playerView = itemView.findViewById(R.id.video_view);

            ThumbImage = itemView.findViewById(R.id.Thumb_Image);
            Views = itemView.findViewById(R.id.views);
        }

        public void bindToPost(Post post, View.OnClickListener starClickListener ,View.OnClickListener optionClickListener ) {
            User user = new User();
            //utils = new Utilities();


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


            starView.setOnClickListener(starClickListener);
            optionView.setOnClickListener(optionClickListener);
        }


    }
}
