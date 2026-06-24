package com.medoapps.www.onlinequran;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.util.DisplayMetrics;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Locale;

public class OtherCategoryListLanguageClass {
    private Context context;

    public OtherCategoryListLanguageClass(Context context) {
        this.context = context;
    }
    public OtherCategoryListLanguageClass() {
    }

    public ArrayList<String> ServerFolderName = new ArrayList<String>();
    public ArrayList<OtherCategory> OtherCategoryListInfo = new ArrayList<OtherCategory>();
    public ArrayList<OtherCategory> LiveListInfo = new ArrayList<OtherCategory>();

    @SuppressWarnings("deprecation")
    public void setLocale(Locale locale){
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        Locale.setDefault(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context.createConfigurationContext(configuration);
        } else {
            context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
        }
    }

    public void setAppLocale(String localeCode){
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            config.setLocale(new Locale(localeCode.toLowerCase()));
        } else {
            config.locale = new Locale(localeCode.toLowerCase());
        }
        resources.updateConfiguration(config, dm);
    }

    public TextView SetTextFont(TextView tv, String type) {
        TextView newtv = tv;
        Typeface tf = null;
        switch(SettingSaved.LanguageSelect) {
            case 1:
                tf = ResourcesCompat.getFont(context, R.font.droidkufi_kegular);
                newtv.setTextSize(15);
                newtv.setTypeface(tf);
                break;
            case 2:
                tf = ResourcesCompat.getFont(context, R.font.ajarsans_regular);
                newtv.setTextSize(20);
                newtv.setTypeface(tf);
                break;
        }
        return newtv;
    }

    public static String avalible() {
        if (SettingSaved.LanguageSelect == 1)
            return ("من الهاتف");
        else
            return ("From phone");
    }

    public static String disavalible() {
        if (SettingSaved.LanguageSelect == 1)
            return ("بث مباشر");
        else
            return ("online");
    }

    public ArrayList<OtherCategory> CategoryList() {
        OtherCategoryListInfo.clear();

        // Live Streaming
        OtherCategoryListInfo.add(new OtherCategory(
                context.getString(R.string.livestrem),
                R.drawable.ic_live_tv,
                new LiveList(), null));

        // Prayer Times
        OtherCategoryListInfo.add(new OtherCategory(
                context.getString(R.string.prayer_times),
                R.drawable.ic_prayer_times,
                null, PrayerTimesActivity.class));

        // Qibla Direction
        OtherCategoryListInfo.add(new OtherCategory(
                context.getString(R.string.qibla_finder),
                R.drawable.ic_qibla,
                null, QiblaActivity.class));

        // Morning & Evening Athkar
        OtherCategoryListInfo.add(new OtherCategory(
                context.getString(R.string.morning_athkar),
                R.drawable.ic_athkar,
                null, AthkarActivity.class));

        // Tasbih Counter
        OtherCategoryListInfo.add(new OtherCategory(
                context.getString(R.string.tasbih_counter),
                R.drawable.ic_tasbih,
                null, TasbihActivity.class));

        // Reading Progress
        OtherCategoryListInfo.add(new OtherCategory(
                context.getString(R.string.reading_progress),
                R.drawable.ic_reading_progress,
                null, ReadingProgressActivity.class));

        // Dua Collection
        OtherCategoryListInfo.add(new OtherCategory(
                context.getString(R.string.dua_collection),
                R.drawable.ic_dua,
                null, DuaActivity.class));

        // 99 Names of Allah
        OtherCategoryListInfo.add(new OtherCategory(
                context.getString(R.string.asmaul_husna),
                R.drawable.ic_asmaul_husna,
                null, AsmaulHusnaActivity.class));

        // Zakat Calculator
        OtherCategoryListInfo.add(new OtherCategory(
                context.getString(R.string.zakat_calculator),
                R.drawable.ic_zakat,
                null, ZakatCalculatorActivity.class));

        // Fasting Tracker
        OtherCategoryListInfo.add(new OtherCategory(
                context.getString(R.string.fasting_tracker),
                R.drawable.ic_fasting,
                null, FastingTrackerActivity.class));

        // Daily Hadith
        OtherCategoryListInfo.add(new OtherCategory(
                context.getString(R.string.daily_hadith),
                R.drawable.ic_hadith,
                null, DailyHadithActivity.class));

        // Islamic Events
        OtherCategoryListInfo.add(new OtherCategory(
                context.getString(R.string.islamic_events),
                R.drawable.ic_islamic_events,
                null, IslamicEventsActivity.class));

        // Hisn Al Muslim
        OtherCategoryListInfo.add(new OtherCategory(
                context.getString(R.string.hisn_al_muslim),
                R.drawable.ic_hisn,
                null, HisnAlMuslimActivity.class));

        // Settings
        OtherCategoryListInfo.add(new OtherCategory(
                context.getString(R.string.menu_settings),
                R.drawable.ic_settings_gear,
                null, Settings.class));

        return OtherCategoryListInfo;
    }

    public ArrayList<OtherCategory> LiveStreamList() {
        LiveListInfo.clear();

        // Quran Kareem Channel
        LiveListInfo.add(new OtherCategory(
                context.getString(R.string.qurankaremchannel),
                R.drawable.ic_mekka,
                "http://m.live.net.sa:1935/live/quran/chunklist_w1026992551.m3u8"));

        // Sunnah Channel
        LiveListInfo.add(new OtherCategory(
                context.getString(R.string.alsunnaalnbweyachannel),
                R.drawable.ic_madina,
                "http://m.live.net.sa:1935/live/sunnah/playlist.m3u8"));

        // Quran Radio Live
        LiveListInfo.add(new OtherCategory(
                context.getString(R.string.quran_radio_live),
                R.drawable.ic_quran_radio,
                "http://stream.radiojar.com/0tpy1h0kxtzuv"));

        return LiveListInfo;
    }

    public ArrayList<OtherCategory> YouTubeList() {
        LiveListInfo.clear();
        LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel), R.drawable.ic_mekka, "http://m.live.net.sa:1935/live/quran/chunklist_w1026992551.m3u8"));
        LiveListInfo.add(new OtherCategory(context.getString(R.string.alsunnaalnbweyachannel), R.drawable.ic_madina, "http://m.live.net.sa:1935/live/sunnah/playlist.m3u8"));
        return LiveListInfo;
    }

    public ArrayList<OtherCategory> YouTubeVideoList() {
        LiveListInfo.clear();
        if (SettingSaved.LanguageSelect == 1) {
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel), "iOxzG3jjFkY"));
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel), "HjfvDIGZMPc"));
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel), "TOi1Ipwv5Qc"));
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel), "VHoT4N43jK8"));
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel), "r3RXHOTMmLw"));
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel), "w8mBplMtwJ8"));
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel), "y2tEPmwWEiI"));
        } else {
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel), "Ao3XJ-UDdzI"));
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel), "eReZPXVzqg8"));
        }
        return LiveListInfo;
    }

    public String serverNumber(String EnglishName) {
        String ArabicName = "11";
        if (EnglishName.endsWith("islam"))
            ArabicName = "14";
        return ArabicName;
    }
}
