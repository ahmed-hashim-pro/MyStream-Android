package com.medoapps.www.onlinequran;

/**
 * Created by Ahmed Hashim on 12/26/15.
 */

import android.app.Activity;

import androidx.fragment.app.Fragment;

/**
 * Created by ASUS S550C on 18/01/2015.
 */
public class OtherCategory {
    public String title ;
    public int ImgDrawable ;
    public String liveUrl ;
    public String youtubeVideoId ;
    public Fragment fragment ;
    public Class<? extends Activity> activity ;

    public OtherCategory(){}
    public OtherCategory(String title ,int ImgDrawable)
    {
        this.title=title;
        this.ImgDrawable=ImgDrawable;
    }
    public OtherCategory(String title ,int ImgDrawable , String liveUrl)
    {
        this.title=title;
        this.ImgDrawable=ImgDrawable;
        this.liveUrl=liveUrl;
    }
    public OtherCategory(String title ,int ImgDrawable  ,Fragment fragment , Class<? extends Activity> activity)
    {
        this.title=title;
        this.ImgDrawable=ImgDrawable;
//        this.liveUrl=liveUrl;
        this.fragment=fragment;
        this.activity=activity;
    }


    public OtherCategory(String title  , String youtubeVideoId)
    {
        this.title=title;
        this.youtubeVideoId=youtubeVideoId;
    }

}
