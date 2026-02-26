package com.medoapps.www.onlinequran;

/**
 * Created by Ahmed Hashim on 12/26/15.
 */

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

/**
 * Created by ASUS S550C on 18/01/2015.
 */
public class OtherCategoryListLanguageClass {
    private Context context;

    public OtherCategoryListLanguageClass(Context context) {
        this.context = context;
    }
    public OtherCategoryListLanguageClass() {

    }

    public  ArrayList<String> ServerFolderName = new ArrayList<String>();
    public   ArrayList<OtherCategory> OtherCategoryListInfo = new ArrayList<OtherCategory>();
    public   ArrayList<OtherCategory> LiveListInfo = new ArrayList<OtherCategory>();

    @SuppressWarnings("deprecation")
    public void setLocale(Locale locale){
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            configuration.setLocale(locale);
        } else{
            configuration.locale=locale;
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
            context.createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration,displayMetrics);
        }*/

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
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN_MR1){
            config.setLocale(new Locale(localeCode.toLowerCase()));
        } else {
            config.locale = new Locale(localeCode.toLowerCase());
        }
        resources.updateConfiguration(config, dm);
    }
     public TextView SetTextFont(TextView tv,String type)
    {
        TextView newtv = tv;
        Typeface tf = null;
        switch(SettingSaved.LanguageSelect)
        {
            case 1:
//                tf = Typeface.createFromAsset(context.getAssets(),R.font.rocketfuel);
                tf = ResourcesCompat.getFont(context, R.font.droidkufi_kegular);
                newtv.setTextSize(15);

                newtv.setTypeface(tf);

                break;
            case 2:
                tf = ResourcesCompat.getFont(context, R.font.ajarsans_regular);
                newtv.setTextSize(20);
                newtv.setTypeface(tf);

                break;
            //   up so on

        }
        return newtv;
    }
    public static String avalible()
    {
        if (SettingSaved.LanguageSelect == 1)

            return ("من الهاتف");

        else
            return ("From phone");


    }
    public static String disavalible()
    {
        if (SettingSaved.LanguageSelect == 1)

            return ("بث مباشر");

        else
            return ("online");

    }



    public    ArrayList<OtherCategory> CategoryList()
    {
        //120 read of quran
        OtherCategoryListInfo.clear();
        if (SettingSaved.LanguageSelect == 1)
        {
            OtherCategoryListInfo.add(new OtherCategory(context.getString(R.string.livestrem) ,R.drawable.ic_mekka,new LiveList(),null));//**
            OtherCategoryListInfo.add(new OtherCategory(context.getString(R.string.zekr) ,R.drawable.zekr,null,RewardVideo.class));//**
//            OtherCategoryListInfo.add(new OtherCategory( context.getString(R.string.youtube) ,R.drawable.outline_file_download_24,new YouTubeList(),null));//**
//            OtherCategoryListInfo.add(new OtherCategory( context.getString(R.string.youtube) ,R.drawable.outline_file_download_24,new youtube_list(),null));//**
//            OtherCategoryListInfo.add(new OtherCategory( context.getString(R.string.youtube) ,R.drawable.ic_tab_group,new YouTubePosts(),null));//**
//            OtherCategoryListInfo.add(new OtherCategory( context.getString(R.string.youtube) ,R.drawable.outline_file_download_24,null, VideoWallDemoActivity.class));//**
//            OtherCategoryListInfo.add(new OtherCategory( context.getString(R.string.youtube) ,R.drawable.outline_file_download_24,null, PlayerControlsDemoActivity.class));//**

        }
        else
        {
            OtherCategoryListInfo.add(new OtherCategory(context.getString(R.string.livestrem) ,R.drawable.ic_mekka,new LiveList(),null));//**
            OtherCategoryListInfo.add(new OtherCategory(context.getString(R.string.zekr) ,R.drawable.zekr,null,RewardVideo.class));//**
        }

        return (OtherCategoryListInfo);

    }
    public    ArrayList<OtherCategory> LiveStreamList()
    {
        LiveListInfo.clear();
        if (SettingSaved.LanguageSelect == 1)
        {
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,R.drawable.ic_mekka,"http://m.live.net.sa:1935/live/quran/chunklist_w1026992551.m3u8"));//**
            LiveListInfo.add(new OtherCategory(context.getString(R.string.alsunnaalnbweyachannel) ,R.drawable.ic_madina,"http://m.live.net.sa:1935/live/sunnah/playlist.m3u8"));//**

        }
        else
        {
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,R.drawable.ic_mekka,"http://m.live.net.sa:1935/live/quran/chunklist_w1026992551.m3u8"));//**
            LiveListInfo.add(new OtherCategory(context.getString(R.string.alsunnaalnbweyachannel) ,R.drawable.ic_madina,"http://m.live.net.sa:1935/live/sunnah/playlist.m3u8"));//**

        }

        return (LiveListInfo);

    }
    public    ArrayList<OtherCategory> YouTubeList()
    {
        LiveListInfo.clear();
        if (SettingSaved.LanguageSelect == 1)
        {
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,R.drawable.ic_mekka,"http://m.live.net.sa:1935/live/quran/chunklist_w1026992551.m3u8"));//**
            LiveListInfo.add(new OtherCategory(context.getString(R.string.alsunnaalnbweyachannel) ,R.drawable.ic_madina,"http://m.live.net.sa:1935/live/sunnah/playlist.m3u8"));//**

        }
        else
        {
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,R.drawable.ic_mekka,"http://m.live.net.sa:1935/live/quran/chunklist_w1026992551.m3u8"));//**
            LiveListInfo.add(new OtherCategory(context.getString(R.string.alsunnaalnbweyachannel) ,R.drawable.ic_madina,"http://m.live.net.sa:1935/live/sunnah/playlist.m3u8"));//**

        }

        return (LiveListInfo);

    }

    public    ArrayList<OtherCategory> YouTubeVideoList()
    {
        LiveListInfo.clear();
        if (SettingSaved.LanguageSelect == 1)
        {
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,"iOxzG3jjFkY"));//**
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,"HjfvDIGZMPc"));//**
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,"TOi1Ipwv5Qc"));//**
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,"VHoT4N43jK8"));//**
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,"r3RXHOTMmLw"));//**
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,"w8mBplMtwJ8"));//**
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,"y2tEPmwWEiI"));//**

        }
        else
        {
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,"Ao3XJ-UDdzI"));//**
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,"eReZPXVzqg8"));//**
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,"eReZPXVzqg8"));//**
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,"eReZPXVzqg8"));//**
            LiveListInfo.add(new OtherCategory(context.getString(R.string.qurankaremchannel) ,"eReZPXVzqg8"));//**
        }

        return (LiveListInfo);

    }




    public String serverNumber (String  EnglishName ){
        String ArabicName ="11";

        if (EnglishName.endsWith("islam"))
            ArabicName ="14";

        return ArabicName;


    }
    
}

