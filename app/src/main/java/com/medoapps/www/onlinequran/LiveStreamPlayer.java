package com.medoapps.www.onlinequran;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

public class LiveStreamPlayer extends AppCompatActivity {

    private String title = "";
    private String liveUrl = "";
    private StyledPlayerView playerView;
    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_stream);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            title = b.getString("Title", "");
            liveUrl = b.getString("LiveUrl", "");
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        playerView = findViewById(R.id.player_view);
    }

    private void initPlayer() {
        if (player != null) return;

        ExoTrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector =
                new DefaultTrackSelector(this, videoTrackSelectionFactory);

        player = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .build();
        player.setPlayWhenReady(true);
        playerView.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                // Try to reconnect on error
                if (player != null && liveUrl != null && !liveUrl.isEmpty()) {
                    player.setMediaSource(buildMediaSource());
                    player.prepare();
                }
            }
        });

        player.setMediaSource(buildMediaSource());
        player.prepare();
    }

    private MediaSource buildMediaSource() {
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this,
                new DefaultHttpDataSource.Factory().setUserAgent("MyStream"));

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(liveUrl));

        if (liveUrl.contains(".m3u8")) {
            return new HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem);
        } else {
            return new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player == null) {
            initPlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
        if (playerView != null) {
            playerView.setPlayer(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
