package com.medoapps.www.onlinequran.viewholder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.models.User;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserViewHolder extends RecyclerView.ViewHolder {

    public TextView authorView;
    public TextView Views;
    public ImageView starView;
    public ImageView optionView;
    public CircleImageView pPictureView;
    public ImageView ThumbImage;

    //private PlayerView playerView;
    //private SimpleExoPlayer player;
    private String attach_url=null;

    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = false;

    public User user4;


    public UserViewHolder(View itemView) {
        super(itemView);

        authorView = itemView.findViewById(R.id.user_name);
        pPictureView = itemView.findViewById(R.id.user_profile);
    }

    public void bindToPost(User user, View.OnClickListener starClickListener  ) {

        //utils = new Utilities();


        String u = user.photourl;
        authorView.setText(user.firstname+""+user.lastname);

        /*player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(itemView.getContext()),
                new DefaultTrackSelector(), new DefaultLoadControl());
        long totalDuration = player.getDuration();*/
        //Views.setText(""+milliSecondsToTimer(totalDuration));
        try {
            Glide.with(itemView.getContext()).load(u).into(pPictureView);
        } catch (Exception e) {
            e.printStackTrace();
        }


        pPictureView.setOnClickListener(starClickListener);

    }

    public void bindToUser(User user, View.OnClickListener starClickListener) {


        User user2 = new User();
        String u = user.photourl;
        String u2 = "https://firebasestorage.googleapis.com/v0/b/online-quran-3b07c.appspot.com/o/photos%2Fimage%3A10205?alt=media&token=52f537ce-fb27-4aaa-9ba5-412b9e71d7f5";


        try {
            Glide.with(itemView.getContext())
                    .load(u).into(pPictureView);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
   /* private void initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(itemView.getContext()),
                new DefaultTrackSelector(), new DefaultLoadControl());

        playerView.setPlayer(player);

        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        String gg = "https://firebasestorage.googleapis.com/v0/b/storage-e8e09.appspot.com/o/video%2Fvideo%3A10347?alt=media&token=6d336d12-d0b0-442e-808c-230ade06257a";
        String hh = "https://firebasestorage.googleapis.com/v0/b/storage-e8e09.appspot.com/o/video%2F114.mp3?alt=media&token=789e2793-b905-423c-888c-127defb97032";
        Uri uri = Uri.parse(attach_url);
        MediaSource mediaSource = buildMediaSource(uri);
        //player.prepare(mediaSource, true, false);
    }
    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab")).
                createMediaSource(uri);
    }

    public String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }
*/
}
