package com.medoapps.www.onlinequran;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

public class LiveStreamPlayer extends AppCompatActivity {

    private String Title = "";
    private String LiveUrl = "";
    StyledPlayerView playerView;
    private ExoPlayer player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_stream);

        Bundle b=getIntent().getExtras();
        Title=b.getString("Title");
        LiveUrl=b.getString("LiveUrl");

        // 1. Create a default TrackSelector
        ExoTrackSelection.Factory videoTrackSelectionFactory = new
                AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector = new
                DefaultTrackSelector(this, videoTrackSelectionFactory);

        playerView = findViewById(R.id.player_view);
        player = new ExoPlayer.Builder(this).setTrackSelector(trackSelector).build();
        player.setPlayWhenReady(true);
        playerView.setPlayer(player);

// DASH
//    DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(
//        Util.getUserAgent(<context>, "ExoPlayer"));
//    DefaultDashChunkSource.Factory chunkSourceFactory = new
//        DefaultDashChunkSource.Factory(dataSourceFactory);
//
//    MediaSource mediaSource = new DashMediaSource(Uri.parse(<dash url>),
//        dataSourceFactory, chunkSourceFactory, null, null);


// HLS
    DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this,
        new DefaultHttpDataSource.Factory().setUserAgent("ExoPlayer"));

    MediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
        .createMediaSource(MediaItem.fromUri(Uri.parse(LiveUrl)));

// MP4
// Produces DataSource instances through which media data is loaded.
//        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
//        Util.getUserAgent(this, "ExoPlayer"));

// Produces Extractor instances for parsing the media data.
//        final ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

// This is the MediaSource representing the media to be played.
//        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(
//                "http://m.live.net.sa:1935/live/quran/chunklist_w1026992551.m3u8"),
//                dataSourceFactory, extractorsFactory, null, null);

        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);

        /*playerView = findViewById(R.id.player_view);

        // Instantiate the player.
//        ExoPlayer player = new ExoPlayer.Builder(this).build();
        ExoPlayer player =
                new ExoPlayer.Builder(this)

                        .build();
// Attach player to the view.
        playerView.setPlayer(player);
        MediaItem mediaItem =
                new MediaItem.Builder()
                        .setUri(LiveUrl)

                        .build();
// Set the media item to be played.
        player.setMediaItem(mediaItem);
// Prepare the player.
        player.prepare();*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        playerView.getPlayer().release();
    }
}