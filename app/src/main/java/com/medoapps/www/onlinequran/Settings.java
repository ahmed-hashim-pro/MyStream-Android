package com.medoapps.www.onlinequran;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.medoapps.www.onlinequran.service.AuthService;
import com.medoapps.www.onlinequran.util.AppBottomSheet;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

import org.json.JSONArray;
import org.json.JSONObject;

public class Settings extends AppCompatActivity {
    private AdView mAdView;
    private SeparateFunctions separateFunctions;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_new);
        authService = new AuthService(this);
        separateFunctions = new SeparateFunctions(this);

        loadBannerAd();

        // Navy hero header (static, centered)
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        HeroController.attach(this).back().centered().title(R.string.action_settings).apply();

        setupGeneralSection();
        setupNotificationsSection();
        setupDataSection();
        setupAboutSection();
        setupSignOut();
        setupVersion();
    }

    // ── General Section ──

    private void setupGeneralSection() {
        SettingSaved settings = new SettingSaved(getApplication());
        settings.LoadData();

        // App language picker (System / Arabic / English) — like the theme picker
        View langItem = findViewById(R.id.item_language);
        ((ImageView) langItem.findViewById(R.id.imgchannel)).setImageResource(R.drawable.outline_translate_24);
        ((TextView) langItem.findViewById(R.id.title)).setText(R.string.settings_language);
        setSubtitle(langItem, R.id.subtitle, AppLanguage.currentLabel(this));
        langItem.findViewById(R.id.switch1).setVisibility(View.GONE);
        View langArrow = langItem.findViewById(R.id.arrow);
        if (langArrow != null) langArrow.setVisibility(View.VISIBLE);
        langItem.setOnClickListener(v -> AppLanguage.showPicker(this, false));

        // Startup Sound switch
        View soundItem = findViewById(R.id.item_startup_sound);
        ((ImageView) soundItem.findViewById(R.id.imgchannel)).setImageResource(R.drawable.outline_music_note_24);
        ((TextView) soundItem.findViewById(R.id.title)).setText(R.string.StartupSound);
        SwitchCompat soundSwitch = soundItem.findViewById(R.id.switch1);
        soundSwitch.setChecked(settings.StartupSound == 1);
        soundSwitch.setOnCheckedChangeListener((btn, checked) -> {
            settings.StartupSound = checked ? 1 : 2;
            settings.SaveData();
        });

        // Animation switch
        View animItem = findViewById(R.id.item_animation);
        ((ImageView) animItem.findViewById(R.id.imgchannel)).setImageResource(R.drawable.animation_icon_48px);
        ((TextView) animItem.findViewById(R.id.title)).setText(R.string.TitlesAnimation);
        SwitchCompat animSwitch = animItem.findViewById(R.id.switch1);
        animSwitch.setChecked(settings.titlesTextAnimate);
        animSwitch.setOnCheckedChangeListener((btn, checked) -> {
            settings.titlesTextAnimate = checked;
            settings.SaveData();
        });

        // Dark Mode
        View darkItem = findViewById(R.id.item_dark_mode);
        ((ImageView) darkItem.findViewById(R.id.imgchannel)).setImageResource(R.drawable.outline_dark_mode_24);
        ((TextView) darkItem.findViewById(R.id.textView)).setText(R.string.DarkMode);
        setSubtitle(darkItem, R.id.subtitle, getString(R.string.settings_subtitle_dark_mode));
        darkItem.findViewById(R.id.arrow).setVisibility(View.VISIBLE);
        darkItem.setOnClickListener(v -> startActivity(new Intent(this, ThemesActivity.class)));
    }

    // ── Notifications Section ──

    private void setupNotificationsSection() {
        // Prayer Times Alarm → new Athan settings (replaces the legacy TimePicker reminder)
        View prayerItem = findViewById(R.id.item_prayer_alarm);
        ((ImageView) prayerItem.findViewById(R.id.imgchannel)).setImageResource(R.drawable.round_add_alert_24);
        ((TextView) prayerItem.findViewById(R.id.textView)).setText(R.string.athan_settings_title);
        setSubtitle(prayerItem, R.id.subtitle, getString(R.string.settings_subtitle_prayer_alarm));
        prayerItem.findViewById(R.id.arrow).setVisibility(View.VISIBLE);
        prayerItem.setOnClickListener(v -> startActivity(new Intent(this, AthanSettingsActivity.class)));

        // Notification Settings page
        View notifItem = findViewById(R.id.item_notification_settings);
        ((ImageView) notifItem.findViewById(R.id.imgchannel)).setImageResource(R.drawable.round_add_alert_20);
        ((TextView) notifItem.findViewById(R.id.textView)).setText(R.string.notification_settings);
        setSubtitle(notifItem, R.id.subtitle, getString(R.string.settings_subtitle_notifications));
        notifItem.findViewById(R.id.arrow).setVisibility(View.VISIBLE);
        notifItem.setOnClickListener(v -> startActivity(new Intent(this, NotificationSettingsActivity.class)));

        // Floating Athkar Bubble dedicated screen
        View bubbleItem = findViewById(R.id.item_floating_bubble);
        ((ImageView) bubbleItem.findViewById(R.id.imgchannel)).setImageResource(R.drawable.ic_athkar);
        ((TextView) bubbleItem.findViewById(R.id.textView)).setText(R.string.bubble_settings_row_title);
        setSubtitle(bubbleItem, R.id.subtitle, getString(R.string.bubble_settings_row_subtitle));
        bubbleItem.findViewById(R.id.arrow).setVisibility(View.VISIBLE);
        bubbleItem.setOnClickListener(v -> startActivity(new Intent(this, BubbleSettingsActivity.class)));
    }

    // ── Data Section ──

    private void setupDataSection() {
        // Downloads
        View dlItem = findViewById(R.id.item_downloads);
        ((ImageView) dlItem.findViewById(R.id.imgchannel)).setImageResource(R.drawable.outline_file_download_24);
        ((TextView) dlItem.findViewById(R.id.textView)).setText(R.string.downloads);
        setSubtitle(dlItem, R.id.subtitle, getString(R.string.settings_subtitle_downloads));
        dlItem.findViewById(R.id.arrow).setVisibility(View.VISIBLE);
        dlItem.setOnClickListener(v ->
                Toast.makeText(this, R.string.not_available, Toast.LENGTH_SHORT).show());

        // Bookmarks
        View bmItem = findViewById(R.id.item_bookmarks);
        ((ImageView) bmItem.findViewById(R.id.imgchannel)).setImageResource(R.drawable.ic_bookmark_border_black_pressed_24dp);
        ((TextView) bmItem.findViewById(R.id.textView)).setText(R.string.last_history);
        setSubtitle(bmItem, R.id.subtitle, getString(R.string.settings_subtitle_bookmarks));
        bmItem.findViewById(R.id.arrow).setVisibility(View.VISIBLE);
        bmItem.setOnClickListener(v -> showBookmarkListDialog());
    }

    // ── About Section ──

    private void setupAboutSection() {
        // Share
        View shareItem = findViewById(R.id.item_share);
        ((ImageView) shareItem.findViewById(R.id.imgchannel)).setImageResource(R.drawable.outline_share_24_settings);
        ((TextView) shareItem.findViewById(R.id.textView)).setText(R.string.separeteapp);
        shareItem.setOnClickListener(v ->
                new SeparateFunctions(getApplicationContext()).generateAppShareLink(this));

        // Rate
        View rateItem = findViewById(R.id.item_rate);
        ((ImageView) rateItem.findViewById(R.id.imgchannel)).setImageResource(R.drawable.outline_rate_review_24);
        ((TextView) rateItem.findViewById(R.id.textView)).setText(R.string.rateapp);
        rateItem.setOnClickListener(v ->
                new SeparateFunctions(this).openRationgIntent());

        // Facebook
        View fbItem = findViewById(R.id.item_facebook);
        ((ImageView) fbItem.findViewById(R.id.imgchannel)).setImageResource(R.drawable.facebook);
        ((TextView) fbItem.findViewById(R.id.textView)).setText(R.string.facebook);
        fbItem.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/mystream.info"))));

        // Website
        View webItem = findViewById(R.id.item_website);
        ((ImageView) webItem.findViewById(R.id.imgchannel)).setImageResource(R.drawable.round_public_24);
        ((TextView) webItem.findViewById(R.id.textView)).setText(R.string.website);
        webItem.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://geohashim.com"))));

        // About
        View aboutItem = findViewById(R.id.item_about);
        ((ImageView) aboutItem.findViewById(R.id.imgchannel)).setImageResource(R.drawable.round_info_24);
        ((TextView) aboutItem.findViewById(R.id.textView)).setText(R.string.about);
        aboutItem.findViewById(R.id.arrow).setVisibility(View.VISIBLE);
        aboutItem.setOnClickListener(v ->
                startActivity(new Intent(this, AboutApp.class)));
    }

    // ── Version ──

    private void setupVersion() {
        TextView versionText = findViewById(R.id.versionText);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionText.setText(getString(R.string.app_name) + " v" + pInfo.versionName);
        } catch (Exception e) {
            versionText.setVisibility(View.GONE);
        }
    }

    private void setSubtitle(View item, int subtitleId, String text) {
        TextView subtitle = item.findViewById(subtitleId);
        if (text != null && !text.isEmpty()) {
            subtitle.setText(text);
            subtitle.setVisibility(View.VISIBLE);
        }
    }

    // ── Sign Out ──

    private void setupSignOut() {
        if (!authService.isAnonymousSignIn()) {
            CardView card = findViewById(R.id.card_sign_out);
            card.setVisibility(View.VISIBLE);
            findViewById(R.id.item_sign_out).setOnClickListener(v ->
                    separateFunctions.showNewCustomDialog(
                            getString(R.string.notifyLohOut),
                            getString(R.string.abortLogOut),
                            getString(R.string.sign_out),
                            getString(android.R.string.no),
                            logOutRunnable,
                            android.R.drawable.ic_dialog_alert));
        }
    }

    // ── Ad ──

    private void loadBannerAd() {
        mAdView = findViewById(R.id.adView);
        mAdView.setVisibility(View.GONE);
        if (SettingSaved.isSubscribedPremium) return;

        mAdView.loadAd(new AdRequest.Builder().build());
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
    }

    // ── Bookmarks Dialog ──

    private void showBookmarkListDialog() {
        final JSONArray bookmarks = SettingSaved.getBookmarks();
        if (bookmarks.length() == 0) {
            Toast.makeText(this, R.string.nohistory, Toast.LENGTH_SHORT).show();
            return;
        }

        LnaguageClass lc = new LnaguageClass(this, this);
        String[] items = new String[bookmarks.length()];
        for (int i = 0; i < bookmarks.length(); i++) {
            try {
                JSONObject obj = bookmarks.getJSONObject(i);
                if (obj.optBoolean("isRadio", false)) {
                    String reciter = lc.getReciterDisplayName(obj.optString("recite", ""));
                    if (reciter.equals(obj.optString("recite", ""))) reciter = obj.optString("realName", "");
                    items[i] = reciter;
                } else {
                    int ayaIndex = Integer.parseInt(obj.optString("aya", "0"));
                    String surah = lc.getSurahNameByIndex(ayaIndex);
                    String reciter = lc.getReciterDisplayName(obj.optString("recite", ""));
                    if (surah.isEmpty()) surah = obj.optString("surahTitle", "");
                    if (reciter.equals(obj.optString("recite", ""))) reciter = obj.optString("realName", "");
                    items[i] = surah + " — " + reciter;
                }
            } catch (Exception e) {
                items[i] = "Bookmark " + (i + 1);
            }
        }

        AppBottomSheet.showList(this,
                getString(R.string.select_bookmark),
                items,
                (position) -> {
                    try {
                        JSONObject obj = bookmarks.getJSONObject(position);
                        LnaguageClass lc2 = new LnaguageClass(this, this);
                        String reciterName = lc2.getReciterDisplayName(obj.optString("recite", ""));
                        if (reciterName.equals(obj.optString("recite", "")))
                            reciterName = obj.optString("realName", "");
                        Intent intent = new Intent(this, NewQuranPlayer.class);
                        intent.putExtra("RecitesName", obj.optString("recite"));
                        intent.putExtra("RecitesAYA", obj.optString("aya"));
                        intent.putExtra("Rewayat", obj.optString("rewayat"));
                        intent.putExtra("RealRecitesName", reciterName);
                        intent.putExtra("IsRadio", obj.optBoolean("isRadio", false));
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                (position) -> {
                    AppBottomSheet.showConfirmation(this,
                            "",
                            getString(R.string.delete_bookmark_confirm),
                            getString(android.R.string.yes),
                            getString(android.R.string.no),
                            () -> {
                                try {
                                    JSONObject obj = bookmarks.getJSONObject(position);
                                    SettingSaved.removeBookmark(getApplicationContext(),
                                            obj.optString("recite"), obj.optString("aya"));
                                    Toast.makeText(this, R.string.bookmark_removed, Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }, null);
                    return true;
                });
    }

    // ── Log Out ──

    private final Runnable logOutRunnable = this::logOut;

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        if (MainActivity.instance8Ref != null && MainActivity.instance8Ref.get() != null) {
            MainActivity.instance8Ref.get().finish();
        }
        finish();
        StorageUtil storageUtil = new StorageUtil(this);
        storageUtil.storeProfileCompleted(false);
        closeMediaService();
        storageUtil.clearCacheYoutubeVideoslist();
        startActivity(new Intent(this, SignInActivity.class));
    }

    private void closeMediaService() {
        new StorageUtil(this).clearCachedAudioPlaylist();
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

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            binder.destroyFromOutside();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
}
