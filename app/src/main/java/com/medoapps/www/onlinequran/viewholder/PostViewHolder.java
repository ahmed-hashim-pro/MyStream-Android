package com.medoapps.www.onlinequran.viewholder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.models.Post;
import com.medoapps.www.onlinequran.models.User;

public class PostViewHolder extends RecyclerView.ViewHolder {

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


    public PostViewHolder(View itemView) {
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
