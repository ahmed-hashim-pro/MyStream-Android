package com.medoapps.www.onlinequran;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.medoapps.www.onlinequran.service.AuthService;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import java.util.ArrayList;

//import com.google.android.gms.ads.InterstitialAd;

public class Settings extends AppCompatActivity {


    private static final int REQUEST_INVITE = 0;
    //private InterstitialAd mInterstitialAd;
    private static final String TAG = "Sellings";
    private AdView mAdView;
    private ImageButton backBTN;
    ArrayList<SettingItem> fullsongpath =new ArrayList<SettingItem>();
    SeparateFunctions separateFunctions ;
    AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        authService = new AuthService(Settings.this);

       /* // load native ad
        NativeExpressAdView adView = (NativeExpressAdView)findViewById(R.id.adView3);
        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);*/

        separateFunctions = new SeparateFunctions(this);
        //load full screan ad
        /*if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }*/

        //load banner Ad
        loadBannerAd();

        backBTN = (ImageButton) findViewById(R.id.backBTN);
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        fullsongpath.add(new SettingItem(getResources().getString(R.string.separeteapp), R.drawable.outline_share_24_settings));

        fullsongpath.add(new SettingItem(getResources().getString(R.string.rateapp), R.drawable.outline_rate_review_24));
//intiixae items
        fullsongpath.add(new SettingItem(getResources().getString(R.string.ListLanguages), R.drawable.outline_translate_24));
        fullsongpath.add(new SettingItem(getResources().getString(R.string.StartupSound), R.drawable.outline_music_note_24));
        fullsongpath.add(new SettingItem(getResources().getString(R.string.TitlesAnimation), R.drawable.animation_icon_48px));
        fullsongpath.add(new SettingItem(getResources().getString(R.string.DarkMode), R.drawable.outline_dark_mode_24));
        fullsongpath.add(new SettingItem(getResources().getString(R.string.facebook), R.drawable.facebook));
        fullsongpath.add(new SettingItem(getResources().getString(R.string.website), R.drawable.round_public_24));
        fullsongpath.add(new SettingItem(getResources().getString(R.string.downloads), R.drawable.outline_file_download_24));
        fullsongpath.add(new SettingItem(getResources().getString(R.string.set_time), R.drawable.round_add_alert_24));
        fullsongpath.add(new SettingItem(getResources().getString(R.string.last_history),R.drawable.ic_bookmark_border_black_pressed_24dp));



        fullsongpath.add(new SettingItem(getResources().getString(R.string.zekr),R.drawable.zekr));
        fullsongpath.add(new SettingItem(getResources().getString(R.string.about),R.drawable.round_info_24));
        if (!authService.isAnonymousSignIn()){

            fullsongpath.add(new SettingItem(getResources().getString(R.string.sign_out),R.drawable.round_logout_24));
        }



        ListView ls=( ListView) findViewById(R.id.listView);
        ls.setAdapter(new MyCustomAdapter());
        ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {


                    switch (position) {

                        case 0:
                        {

                            new SeparateFunctions(getApplicationContext()).generateAppShareLink(Settings.this);
//                            SeparateFunctions func = new SeparateFunctions(Settings.this);
//                            func.onShareBy();
                        } break;

                        case 1:
                        {

                            SeparateFunctions separateFunctions = new SeparateFunctions(Settings.this);
                            separateFunctions.openRationgIntent();

                        }
                        break;
                        case 5:
                        {

                            Intent down = new Intent(Settings.this, ThemesActivity.class);
                            startActivity(down);

                        }
                        break;
                        case 6:
                        {

                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/mystream.info"));
                            startActivity(browserIntent);

                        }
                        break;

                        case 7:
                        {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://geohashim.com"));
                            startActivity(browserIntent);

                        }
                        break;
                        case 8:
                        {
                            Toast.makeText(getApplication(), getString(R.string.not_available), Toast.LENGTH_SHORT).show();
                            /*Intent down = new Intent(Settings.this, Downloads.class);
                            startActivity(down);*/
                        }break;
                        case 9:
                        {
                            Intent down = new Intent(Settings.this, TimePicker.class);
                            startActivity(down);
                        }break;

                        case 10:
                        {
                            if (SettingSaved.FinalRecite!=""){
                                //chec for ubdate
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Settings.this);

                                // Setting Dialog Title
                                alertDialog.setTitle(R.string.lastrecite);

                                // Setting Dialog Message
                                alertDialog.setMessage(R.string.history);

                                // Setting Icon to Dialog
                                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);

                                // Setting Positive "Yes" Button
                                alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int which) {

                                        // Write your code here to invoke YES event
                                        Intent intent= new Intent( Settings.this,managerdb.class);
                                        intent.putExtra("RecitesName", SettingSaved.FinalRecite);
                                        intent.putExtra("RecitesAYA", SettingSaved.FinalAya);
                                        startActivity(intent);
                                        //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                // Setting Negative "NO" Button
                                alertDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Write your code here to invoke NO event

                                        dialog.cancel();
                                    }
                                });

                                // Showing Alert Message
                                alertDialog.show();


                            }else {
                                Toast.makeText(Settings.this, getString(R.string.nohistory), Toast.LENGTH_SHORT).show();
                            }
                        }break;
                        case 11: {
                            Intent newpage = new Intent(Settings.this, RewardVideo.class);
                            startActivity(newpage);


                        }break;
                        case 12: {
                            Intent newpage = new Intent(Settings.this, AboutApp.class);
                            startActivity(newpage);
                        }break;
                        case 13: {
                            separateFunctions.showNewCustomDialog(getString(R.string.notifyLohOut),getString(R.string.abortLogOut),getString(R.string.sign_out),getString(android.R.string.no),logOutRunnable,android.R.drawable.ic_dialog_alert);


                        }break;



                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadBannerAd() {
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setVisibility(View.GONE);
        if (SettingSaved.isSubscribedPremium)
            return;


        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void generateAppShareLink(){
        Uri imageUri = Uri.parse(getString(R.string.dynamicLinkShareImage));

        SeparateFunctions separateFunctions = new SeparateFunctions(Settings.this);
        separateFunctions.createDynamicLink(Settings.this,"welcome",getString(R.string.app_name),getString(R.string.sharemessage),imageUri).addOnCompleteListener(Settings.this, new OnCompleteListener<ShortDynamicLink>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<ShortDynamicLink> task) {
                if (task.isSuccessful()) {
                    // Short link created
                    Uri shortLink = task.getResult().getShortLink();


                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = getString(R.string.sharemessage) +"  "+ shortLink;
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My Stream");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));


                } else {
                    Log.d(TAG, "createDynamicLink 2: " +task);
                    // Error
                }
            }});
    }
    Runnable logOutRunnable = new Runnable() {
        @Override
        public void run() {
            logOut();
        }
    };
    private void logOut(){
        FirebaseAuth.getInstance().signOut();
        if (MainActivity.instance8Ref != null && MainActivity.instance8Ref.get() != null) {
            MainActivity.instance8Ref.get().finish();
        }
        finish();
        StorageUtil storageUtil = new StorageUtil(this);
        storageUtil.storeProfileCompleted(false);
        closeMediaService();
        storageUtil.clearCacheYoutubeVideoslist();
        Intent down = new Intent(Settings.this, SignInActivity.class);
        startActivity(down);
    }
    private void closeMediaService(){
        new StorageUtil(Settings.this).clearCachedAudioPlaylist();
        Intent playerIntent = new Intent(this, MediaPlayerService.class);
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unbindService(serviceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            binder.destroyFromOutside();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Toast.makeText(getApplicationContext(), "onServiceDisconnected", Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_managerdb, menu);
//        menu.getItem(0).

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.gbackmenu) { // stoped
            // Intent intent=new Intent(this,MainActivity.class);
            //startActivity(intent);
            //load full screan ad

            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    // adapter for song list
    private class MyCustomAdapter extends BaseAdapter {


        public MyCustomAdapter() {

        }


        @Override
        public int getCount() {
            return fullsongpath.size();
        }

        @Override
        public String getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater mInflater = getLayoutInflater();

            final SettingItem s = fullsongpath.get(position);

            if((position==2) ){

                SettingSaved settings = new SettingSaved(getApplication());
                settings.LoadData();
                View myView = mInflater.inflate(R.layout.setting_item_alert, null);
                SettingItem item = fullsongpath.get(position);

                final Switch swNotify=(Switch)myView.findViewById(R.id.switch1);
                final ImageView imgchannel=(ImageView)myView.findViewById(R.id.imgchannel);

                imgchannel.setImageDrawable(ContextCompat.getDrawable(Settings.this, item.ImageURL));

                swNotify.setChecked( settings.LanguageSelect==1?true:false);
                swNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        settings.LanguageSelect = isChecked == true ? 1 : 2;
                        settings.SaveData();

                        Toast.makeText(Settings.this, getString(R.string.language_take_effect), Toast.LENGTH_SHORT).show();
                        /*LnaguageClass lc = new LnaguageClass(Sellings.this);
                        lc.setAppLocale(isChecked == true?"ar":"en-US");*/


                    }
                });
                return myView;
            }else if((position==3) ){

                SettingItem item = fullsongpath.get(position);
                SettingSaved settings = new SettingSaved(getApplication());
                settings.LoadData();
                View myView = mInflater.inflate(R.layout.setting_item_alert, null);
                final Switch swNotify=(Switch)myView.findViewById(R.id.switch1);
                final TextView title=(TextView)myView.findViewById(R.id.title);
                final ImageView imgchannel=(ImageView)myView.findViewById(R.id.imgchannel);

                imgchannel.setImageDrawable(ContextCompat.getDrawable(Settings.this, R.drawable.outline_music_note_24));

                title.setText(item.Name);
                swNotify.setTextOn("");
                swNotify.setTextOff("");
                swNotify.setChecked( settings.StartupSound==1?true:false);
                swNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        settings.StartupSound = isChecked == true ? 1 : 2;
                        settings.SaveData();
//                        Toast.makeText(Settings.this, getString(R.string.language_take_effect), Toast.LENGTH_SHORT).show();
                    }
                });
                return myView;
            }else if((position==4) ){

                SettingItem item = fullsongpath.get(position);
                SettingSaved settings = new SettingSaved(getApplication());
                settings.LoadData();
                View myView = mInflater.inflate(R.layout.setting_item_alert, null);
                final Switch swNotify=(Switch)myView.findViewById(R.id.switch1);
                final TextView title=(TextView)myView.findViewById(R.id.title);
                final ImageView imgchannel=(ImageView)myView.findViewById(R.id.imgchannel);

                imgchannel.setImageDrawable(ContextCompat.getDrawable(Settings.this, R.drawable.animation_icon_48px));

                title.setText(item.Name);
                swNotify.setTextOn("");
                swNotify.setTextOff("");
                swNotify.setChecked( settings.titlesTextAnimate);
                swNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        settings.titlesTextAnimate = isChecked ;
                        settings.SaveData();
                    }
                });
                return myView;
            }

            else
            {
                View myView = mInflater.inflate(R.layout.settingitem, null);
                TextView textView = (TextView) myView.findViewById(R.id.textView);
                textView.setText(s.Name);
                ImageView img=(ImageView)myView.findViewById(R.id.imgchannel);
                img.setImageResource(s.ImageURL);
                return myView;}
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                /*String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }*/
            } else {
                Toast.makeText(this, "can not share app", Toast.LENGTH_SHORT).show();
            }
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
