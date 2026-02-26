package com.medoapps.www.onlinequran;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.medoapps.www.onlinequran.models.Post;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Ahmed Hashim on 16-07-30.
 */
public class StorageUtil {

    private final String STORAGE = " com.medoapps.www.onlinequran.STORAGE";
    private final String APPSTORAGE = " com.medoapps.www.onlinequran.APPSTORAGE";
    private final String APPYOUTUBEVIDEOSTORAGE = " com.medoapps.www.onlinequran.APPYOUTUBEVIDEOSTORAGE";
    private final String APPPUSHNOTIFICATIONSSTORAGE = " com.medoapps.www.onlinequran.APPPUSHNOTIFICATIONSSTORAGE";
    private final String DOWNLOADSTORAGE = " com.medoapps.www.onlinequran.STORAGE.DOWNLOAD";
    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storePushNotificationVideo(ArrayList<Post> arrayList) {
        preferences = context.getSharedPreferences(APPPUSHNOTIFICATIONSSTORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("pushNotificationVideoArrayList", json);
        editor.apply();
    }
    public ArrayList<Post> loadPushNotificationVideo() {
        preferences = context.getSharedPreferences(APPPUSHNOTIFICATIONSSTORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("pushNotificationVideoArrayList", null);
        Type type = new TypeToken<ArrayList<Post>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeYoutubeVideos(ArrayList<Post> arrayList) {
        preferences = context.getSharedPreferences(APPYOUTUBEVIDEOSTORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("youtubeVideosArrayList", json);
        editor.apply();
    }

    public ArrayList<Post> loadYoutubeVideos() {
        preferences = context.getSharedPreferences(APPYOUTUBEVIDEOSTORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("youtubeVideosArrayList", null);
        Type type = new TypeToken<ArrayList<Post>>() {
        }.getType();
        return gson.fromJson(json, type);
    }



    public void storeAudio(ArrayList<Audio> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioArrayList", json);
        editor.apply();
    }

    public ArrayList<Audio> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audioArrayList", null);
        Type type = new TypeToken<ArrayList<Audio>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeDownloadlist(ArrayList<AuthorClass> arrayList) {
        preferences = context.getSharedPreferences(DOWNLOADSTORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("DownloadsArrayList", json);
        editor.apply();
    }
    public ArrayList<AuthorClass> loadDownloadlist() {
        preferences = context.getSharedPreferences(DOWNLOADSTORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("DownloadsArrayList", null);
        Type type = new TypeToken<ArrayList<AuthorClass>>() {
        }.getType();
        return gson.fromJson(json, type);
    }


    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    public void storeIsMediaStoppedFromUser(Boolean isMediaStoppedFromUser) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isMediaStoppedFromUser", isMediaStoppedFromUser);
        editor.apply();
    }



    public void storeDownloadIndex(int index) {
        preferences = context.getSharedPreferences(DOWNLOADSTORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("downloadIndex", index);
        editor.apply();
    }
    public void storeDownloadRecitesName(String RecitesName) {
        preferences = context.getSharedPreferences(DOWNLOADSTORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("downloadRecitesName", RecitesName);
        editor.apply();
    }
    public void storeDownloadRealRecitesName(String RecitesName) {
        preferences = context.getSharedPreferences(DOWNLOADSTORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("downloadRealRecitesName", RecitesName);
        editor.apply();
    }





    public void storeServiceBound(Boolean ServiceBound) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("serviceBound", ServiceBound);
        editor.apply();
    }
    public void storeIsPlayerRepeat(Boolean isRepeat) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isRepeat", isRepeat);
        editor.apply();
    }

    public void storeIsPlayerShuffle(Boolean isShuffle) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isShuffle", isShuffle);
        editor.apply();
    }






    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("audioIndex", -1);//return -1 if no data found
    }

    public Boolean loadIsMediaStoppedFromUser() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getBoolean("isMediaStoppedFromUser", false);//return -1 if no data found
    }


    public int loadDownloadIndex() {
        preferences = context.getSharedPreferences(DOWNLOADSTORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("downloadIndex", -1);//return -1 if no data found
    }
    public String loadDownloadRecitesName() {
        preferences = context.getSharedPreferences(DOWNLOADSTORAGE, Context.MODE_PRIVATE);
        return preferences.getString("downloadRecitesName", "");//return -1 if no data found
    }
    public String loadDownloadRealRecitesName() {
        preferences = context.getSharedPreferences(DOWNLOADSTORAGE, Context.MODE_PRIVATE);
        return preferences.getString("downloadRealRecitesName", "");//return -1 if no data found
    }






    public boolean loadServiceBound() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getBoolean("serviceBound", false);//return -1 if no data found
    }

    public boolean loadIsPlayerRepeat() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getBoolean("isRepeat", false);//return -1 if no data found
    }
    public boolean loadIsPlayerShuffle() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getBoolean("isShuffle", false);//return -1 if no data found
    }


    public void storeProfileCompleted(Boolean ProfileCompleted) {
        preferences = context.getSharedPreferences(APPSTORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("ProfileCompleted", ProfileCompleted);
        editor.apply();
    }
    public boolean loadProfileCompleted() {
        preferences = context.getSharedPreferences(APPSTORAGE, Context.MODE_PRIVATE);
        return preferences.getBoolean("ProfileCompleted", false);//return -1 if no data found
    }



    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
    public void clearCacheDownloadslist() {
        preferences = context.getSharedPreferences(DOWNLOADSTORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
    public void clearCacheYoutubeVideoslist() {
        preferences = context.getSharedPreferences(APPYOUTUBEVIDEOSTORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
    public void clearCachePushNotificationVideo() {
        preferences = context.getSharedPreferences(APPPUSHNOTIFICATIONSSTORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }





}
