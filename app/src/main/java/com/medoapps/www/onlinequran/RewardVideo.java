package com.medoapps.www.onlinequran;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

//import com.google.android.gms.ads.InterstitialAd;
//import com.google.android.gms.ads.reward.RewardItem;
//import com.google.android.gms.ads.reward.RewardedVideoAd;
//import com.google.android.gms.ads.reward.RewardedVideoAdListener;

/**
 * Main Activity. Inflates main activity xml and .
 */
public class RewardVideo extends Activity  {
    private static final String AD_UNIT_ID = "ca-app-pub-9350633918697995/7533398267";
    private static final String APP_ID = " ca-app-pub-9350633918697995~2524775865";
    private static final double COUNTER_TIME = 0.1;
    private static final int GAME_OVER_REWARD = 1;
    private static final String TAG = "MainActivity";

    private AdView mAdView;
    private int mCoinCount;
    private TextView mCoinCountText;
    private TextView presstext;
    private CountDownTimer mCountDownTimer;
    private boolean mGameOver;
    private boolean mGamePaused;
    //private RewardedVideoAd mRewardedVideoAd;
    private Button mRetryButton;
    private Button mShowVideoButton;
    private long mTimeRemaining;
    //private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_video);

        //load banner Ad
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        loadad();//to load ads full screen


        presstext=(TextView)findViewById(R.id.press);

        loadRewardedVideoAd();

        // Create the "retry" button, which tries to show an interstitial between game plays.
        mRetryButton = ((Button) findViewById(R.id.retry_button));
        mRetryButton.setVisibility(View.INVISIBLE);
        presstext.setVisibility(View.INVISIBLE);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });

        // Create the "show" button, which shows a rewarded video if one is loaded.
        mShowVideoButton = ((Button) findViewById(R.id.watch_video));
        mShowVideoButton.setVisibility(View.INVISIBLE);
        mShowVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRewardedVideo();

            }
        });

        // Display current coin count to user.
        mCoinCountText = ((TextView) findViewById(R.id.coin_count_text));
        mCoinCount = 0;
        mCoinCountText.setText("Coins: " + mCoinCount);

        startGame();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseGame();
//        mRewardedVideoAd.pause(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mGameOver && mGamePaused) {
            resumeGame();
        }
//        mRewardedVideoAd.resume(this);
    }

    private void pauseGame() {
        mCountDownTimer.cancel();
        mGamePaused = true;
    }

    private void resumeGame() {
        createTimer(mTimeRemaining);
        mGamePaused = false;
    }

    private void loadRewardedVideoAd() {
        /*if (!mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.loadAd(getString(R.string.Video_ad_unit_id), new AdRequest.Builder().build());
        }*/
    }

    private void addCoins(int coins) {
        mCoinCount = mCoinCount + coins;
        mCoinCountText.setText("Coins: " + mCoinCount);
    }

    private void startGame() {
        // Hide the retry button, load the ad, and start the timer.
        mRetryButton.setVisibility(View.INVISIBLE);
        presstext.setVisibility(View.INVISIBLE);
        mShowVideoButton.setVisibility(View.INVISIBLE);
        loadRewardedVideoAd();
        createTimer(COUNTER_TIME);
        mGamePaused = false;
        mGameOver = false;
    }

    // Create the game timer, which counts down to the end of the level
    // and shows the "retry" button.
    private void createTimer(double time) {
        final TextView textView = ((TextView) findViewById(R.id.timer));
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mCountDownTimer = new CountDownTimer((long) (time * 1000), 50) {
            @Override
            public void onTick(long millisUnitFinished) {
                mTimeRemaining = ((millisUnitFinished / 1000) + 1);
                textView.setText("seconds remaining: " + mTimeRemaining);
            }

            @Override
            public void onFinish() {
                textView.setText("الدال علي الخير كفاعله");
                addCoins(GAME_OVER_REWARD);
                mRetryButton.setVisibility(View.VISIBLE);
                mGameOver = true;
                presstext.setVisibility(View.VISIBLE);
            }
        };
        mCountDownTimer.start();
    }

    private void showRewardedVideo() {
        mShowVideoButton.setVisibility(View.INVISIBLE);
        /*if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }*/
    }






    public void loadad(){

        /*mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.Pop_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {wad
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });
*/

    }
}
