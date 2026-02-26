/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.medoapps.www.onlinequran;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.medoapps.www.onlinequran.classes.YouTubeConfig;
import com.medoapps.www.onlinequran.hashimyoutubeplayer.YouTubeFailureRecoveryActivity;

/**
 * A simple YouTube Android API demo application which shows how to create a simple application that
 * shows a YouTube Video in a {@link YouTubePlayerFragment}.
 * <p>
 * Note, this sample app extends from {@link YouTubeFailureRecoveryActivity} to handle errors, which
 * itself extends {@link YouTubeBaseActivity}. However, you are not required to extend
 * {@link YouTubeBaseActivity} if using {@link YouTubePlayerFragment}s.
 */
public class FragmentDemoActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {
  YouTubePlayerView youTubeView;
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.fragments_demo);

    youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
    youTubeView.initialize(YouTubeConfig.getApiKey(), this);
  }


  @Override
  public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
    youTubePlayer.cueVideo("nCgQDjiotG0");
  }

  @Override
  public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
    Log.d("sdfsdfsdfswewer", String.valueOf(youTubeInitializationResult));
  }
}
