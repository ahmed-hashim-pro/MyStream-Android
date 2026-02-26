package com.medoapps.www.onlinequran;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

//import com.google.android.gms.ads.InterstitialAd;

/**
 * Main Activity for sending App Invites and launchings the DeepLinkActivity when an
 * App Invite is received.
 */
public class invite extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    //private InterstitialAd mInterstitialAd;
    private AdView mAdView;
    private static final String TAG = invite.class.getSimpleName();
    private static final int REQUEST_INVITE = 0;

    private GoogleApiClient mGoogleApiClient;


    // [START on_create]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // [START_EXCLUDE]
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        // Invite button click listener
        findViewById(R.id.invite_button).setOnClickListener(this);
        findViewById(R.id.custom_invite_button).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);
        // [END_EXCLUDE]
        loadad();//to load ads full screen

        //load full screan ad
        /*if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }*/
        // Create an auto-managed GoogleApiClient with acccess to App Invites.
        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, this)
                .build();*/

        // Check for App Invite invitations and launch deep-link activity if possible.
        // Requires that an Activity is registered in AndroidManifest.xml to handle
        // deep-link URLs.
        boolean autoLaunchDeepLink = true;
        /*AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(AppInviteInvitationResult result) {
                                Log.d(TAG, "getInvitation:onResult:" + result.getStatus());
                                // Because autoLaunchDeepLink = true we don't have to do anything
                                // here, but we could set that to false and manually choose
                                // an Activity to launch to handle the deep link here.
                            }
                        });*/
    }
    // [END on_create]

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        showMessage(getString(R.string.google_play_services_error));
    }

    /**
     * User has clicked the 'Invite' button, launch the invitation UI with the proper
     * title, message, and deep link
     */
    // [START on_invite_clicked]
    private void onInviteClicked() {
        //load full screan ad
        /*if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }*/
        /*Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);*/
    }
    // [END on_invite_clicked]

    /**
     * User has clicked the 'Custom Invite' button, launch the invitation UI but pass in
     * a custom HTML body and subject for email invites.
     */
    // [START on_custom_invite_clicked]
    private void onShareBy() {
        //load full screan ad
        /*if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }*/

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = getResources().getString(R.string.sharemessage) + "  https://rebrand.ly/notfof70d";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Stream");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));

    }
    private void onCustomInviteClicked() {
        //load full screan ad
        /*if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }*/
        // When using the setEmailHtmlContent method, you must also set a subject using the
        // setEmailSubject message and you may not use either setCustomImage or setCallToActionText
        // in conjunction with the setEmailHtmlContent method.
        //
        // The "%%APPINVITE_LINK_PLACEHOLDER%%" token is replaced by the invitation server
        // with the custom invitation deep link based on the other parameters you provide.
        /*Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                .setEmailHtmlContent("<html><body>" +
                        "<h1>App Invites</h1>" +
                        "<a href=\"%%APPINVITE_LINK_PLACEHOLDER%%\">Install Now!</a>" +
                        "<body></html>")
                .setEmailSubject(getString(R.string.invitation_subject))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);*/
    }
    // [END on_custom_invite_clicked]

    // [START on_activity_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Check how many invitations were sent and log a message
                // The ids array contains the unique invitation ids for each invitation sent
                // (one for each contact select by the user). You can use these for analytics
                // as the ID will be consistent on the sending and receiving devices.
//                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
//                Log.d(TAG, getString(R.string.sent_invitations_fmt, ids.length));
            } else {
                // Sending failed or it was canceled, show failure message to the user
                showMessage(getString(R.string.send_failed));
            }
        }
    }
    // [END on_activity_result]

    private void showMessage(String msg) {
        ViewGroup container = (ViewGroup) findViewById(R.id.snackbar_layout);
        //Snackbar.make(container, msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.invite_button:
                onInviteClicked();
                break;
            case R.id.custom_invite_button:
                onCustomInviteClicked();
                break;
            case R.id.share:
                onShareBy();
                break;
        }
    }

    public void loadad(){

        /*mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.Pop_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });
*/

    }
}
