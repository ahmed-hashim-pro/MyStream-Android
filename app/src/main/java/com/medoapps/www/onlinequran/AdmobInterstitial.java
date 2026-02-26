package com.medoapps.www.onlinequran;

/**
 * Created by MEDO on 20/10/2017.
 */

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class AdmobInterstitial extends AppCompatActivity {
    public static final String TAG = AdmobInterstitial.class.getSimpleName();
    public static final String ID = "ca-app-pub-9350633918697995/9485553465";
    private static ScheduledFuture loaderHandler;

    public static void loadInterstitial(final Activity activity) {
        final Runnable loader = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Loading Admob interstitial...");
                Toast.makeText(activity, "ad will be run", Toast.LENGTH_SHORT).show();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //  InterstitialAd here
                        /*final InterstitialAd interstitial = new InterstitialAd(activity);
                        interstitial.setAdUnitId(activity.getApplicationContext().getString(R.string.Pop_ad_unit_id));
                        AdRequest adRequest = new AdRequest.Builder().build();
                        interstitial.loadAd(adRequest);
                        interstitial.setAdListener(new AdListener() {
                            public void onAdLoaded() {
                                displayInterstitial(interstitial);
                            }
                        });*/
                    }
                });
            }
        };


        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        loaderHandler = scheduler.scheduleWithFixedDelay(loader, 30, 30, TimeUnit.SECONDS);
    }

    /*private static void displayInterstitial(final InterstitialAd interstitialAd) {

        if (interstitialAd.isLoaded()) {
            interstitialAd.show();

        }
    }
*/


}