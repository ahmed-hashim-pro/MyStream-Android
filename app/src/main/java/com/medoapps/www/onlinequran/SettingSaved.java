package com.medoapps.www.onlinequran;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by Ahmed Hashim on 12/17/15.
 */
public class SettingSaved extends AppCompatActivity {

    private static final String TAG = "SettingSaved" ;
    Context context;

    int MODE_PRIVATE = 0;
    public static final String MyPREFERENCES = "com.medoapps.www.onlinequran" ;
    public static int LanguageSelect=1;
    public static int StartupSound=2;
    public static String  APPURL="com.medoapps.www.onlinequran";
    public static boolean OnTimeAds=false;
    public static int IsOpen=0;//closed
    public static int SounlLoad=0;
    public static int IsRated=0;//app rate 0 not rate 1 is rate
    public static int playsound=0;
    public static int selectedHour=20;
    public static int selectedMinute=1;
    public static int firstopen=1;
    public static int currentThemeMode=-1;
    public static int numberOFBackClicksForIntent=0;
    public static Boolean userubdated=true;
    public static Boolean userubdated2=true;
    public static Boolean userubdated3=true;
    public static String AppVersion = "5.0.1961";
    public static int ReminderSelect=1;
    public static int ReminderStart=1;
    public static String FinalRecite="";
    public static String FinalAya="";
    public static String FinalRewayat="";
    public static String FinalRealRecitesName="";
    public static String specnumber="No_Num";
    public static String mDownloadUrl=null;
    public static String mDownloadUrlThumb=null;
    public static String title = "";
    public static String body = "";
    public static String Upload_Type = null;
    public static Boolean isfullscreenadshow = false;
    public static Boolean editMode = false ;
    public static String mPostKey = "";
    public static String thumb_url = "";
    public  static String file_uri = "";
    public static Boolean isUserCountAsReporter=false;
    public static int reportsNumberForShowSharingDialog =0;
    public static Boolean isSubscribedPremium=false;
    public static Boolean titlesTextAnimate=false;
    public static String BookmarksList = "[]";



    SharedPreferences sharedpreferences;
    public SettingSaved(Context context) {
        this.context=context;
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

    }
    public void SaveData()  {

        try

        {
            sharedpreferences=PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedpreferences.edit();

            editor.putInt("LanguageSelect", LanguageSelect);
            editor.putInt("StartupSound", StartupSound);
            editor.putInt("currentThemeMode", currentThemeMode);
            editor.putInt("numberOFBackClicksForIntent", numberOFBackClicksForIntent);
            editor.putInt("IsRated", IsRated);
            editor.putInt("SelectedHour",selectedHour);
            editor.putInt("SelectedMinute",selectedMinute);
            editor.putInt("firstopen",firstopen);
            editor.putBoolean("userubdated",userubdated);
            editor.putInt("ReminderSelect",ReminderSelect);
            editor.putInt("ReminderStart",ReminderStart);
            editor.putString("FinalRecite",FinalRecite);
            editor.putString("FinalAya",FinalAya);
            editor.putString("FinalRewayat",FinalRewayat);
            editor.putString("FinalRealRecitesName",FinalRealRecitesName);
            editor.putString("specnumber",specnumber);
            editor.putString("title",title);
            editor.putString("body",body);
            editor.putString("mDownloadUrl",mDownloadUrl);
            editor.putString("mDownloadUrlThumb",mDownloadUrlThumb);
            editor.putString("Upload_Type",Upload_Type);
            editor.putBoolean("userubdated2",userubdated2);
            editor.putBoolean("userubdated3",userubdated3);
            editor.putBoolean("isfullscreenadshow",isfullscreenadshow);
            editor.putString("AppVersion",AppVersion);
            editor.putBoolean("editMode",editMode);
            editor.putString("mPostKey",mPostKey);
            editor.putString("thumb_url",thumb_url);
            editor.putString("file_uri",file_uri);
            editor.putBoolean("isUserCountAsReporter",isUserCountAsReporter);
            editor.putInt("reportsNumberForShowSharingDialog",reportsNumberForShowSharingDialog);
            editor.putBoolean("isSubscribedPremium",isSubscribedPremium);
            editor.putBoolean("titlesTextAnimate",titlesTextAnimate);
            editor.putString("BookmarksList",BookmarksList);


            editor.commit();


            LoadData( );
        }

        catch( Exception e)

        {

            Toast.makeText(context, "Unable to write to the SettingFile file.", Toast.LENGTH_LONG).show();
        }
    }
    public   void LoadData( ) {

        sharedpreferences=PreferenceManager.getDefaultSharedPreferences(context);
        LanguageSelect=sharedpreferences.getInt("LanguageSelect", -1);
        if(LanguageSelect==-1) //first time
        {
            String lng= Locale.getDefault().getLanguage();
            if( lng.toLowerCase().equals("ar"))
                LanguageSelect=1;
            else
                LanguageSelect=2;
            LoadData( );
        }
        Log.d(TAG, "LoadData: " + LanguageSelect);

        StartupSound=sharedpreferences.getInt("StartupSound", 1);
        currentThemeMode=sharedpreferences.getInt("currentThemeMode", currentThemeMode);
        numberOFBackClicksForIntent=sharedpreferences.getInt("numberOFBackClicksForIntent", numberOFBackClicksForIntent);
        IsRated=sharedpreferences.getInt("IsRated", 0);
        selectedHour=sharedpreferences.getInt("SelectedHour",selectedHour);
        selectedMinute=sharedpreferences.getInt("SelectedMinute",selectedMinute);
        firstopen=sharedpreferences.getInt("firstopen",firstopen);
        userubdated=sharedpreferences.getBoolean("userubdated",userubdated);
        ReminderSelect=sharedpreferences.getInt("ReminderSelect",ReminderSelect);
        ReminderStart=sharedpreferences.getInt("ReminderStart",ReminderStart);
        FinalRecite=sharedpreferences.getString("FinalRecite","");
        FinalAya=sharedpreferences.getString("FinalAya","");
        FinalRewayat=sharedpreferences.getString("FinalRewayat","");
        FinalRealRecitesName=sharedpreferences.getString("FinalRealRecitesName","");
        title=sharedpreferences.getString("title",title);
        body=sharedpreferences.getString("body",body);
        mDownloadUrl=sharedpreferences.getString("mDownloadUrl",mDownloadUrl);
        mDownloadUrlThumb=sharedpreferences.getString("mDownloadUrlThumb",mDownloadUrlThumb);
        Upload_Type=sharedpreferences.getString("Upload_Type",Upload_Type);
        userubdated2=sharedpreferences.getBoolean("userubdated2",userubdated2);
        userubdated2=sharedpreferences.getBoolean("userubdated3",userubdated3);
        isfullscreenadshow=sharedpreferences.getBoolean("isfullscreenadshow",isfullscreenadshow);
        AppVersion = sharedpreferences.getString("AppVersion",AppVersion);
        editMode = sharedpreferences.getBoolean("editMode",editMode);
        mPostKey = sharedpreferences.getString("mPostKey",mPostKey);
        thumb_url = sharedpreferences.getString("thumb_url",thumb_url);
        file_uri = sharedpreferences.getString("file_uri",file_uri);
        isUserCountAsReporter = sharedpreferences.getBoolean("isUserCountAsReporter",isUserCountAsReporter);
        reportsNumberForShowSharingDialog = sharedpreferences.getInt("reportsNumberForShowSharingDialog",reportsNumberForShowSharingDialog);
        isSubscribedPremium = sharedpreferences.getBoolean("isSubscribedPremium",isSubscribedPremium);
        titlesTextAnimate = sharedpreferences.getBoolean("titlesTextAnimate",titlesTextAnimate);
        BookmarksList = sharedpreferences.getString("BookmarksList","[]");





    }

    public static JSONArray getBookmarks() {
        try {
            return new JSONArray(BookmarksList);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    public static boolean isBookmarked(String recite, String aya) {
        try {
            JSONArray arr = getBookmarks();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (obj.optString("recite").equals(recite) && obj.optString("aya").equals(aya)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void addBookmark(Context ctx, String recite, String aya, String rewayat, String realName, String surahTitle) {
        if (isBookmarked(recite, aya)) return;
        try {
            JSONArray arr = getBookmarks();
            JSONObject obj = new JSONObject();
            obj.put("recite", recite);
            obj.put("aya", aya);
            obj.put("rewayat", rewayat);
            obj.put("realName", realName);
            obj.put("surahTitle", surahTitle);
            arr.put(obj);
            BookmarksList = arr.toString();
            SettingSaved ss = new SettingSaved(ctx);
            ss.SaveData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeBookmark(Context ctx, String recite, String aya) {
        try {
            JSONArray arr = getBookmarks();
            JSONArray newArr = new JSONArray();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (!(obj.optString("recite").equals(recite) && obj.optString("aya").equals(aya))) {
                    newArr.put(obj);
                }
            }
            BookmarksList = newArr.toString();
            SettingSaved ss = new SettingSaved(ctx);
            ss.SaveData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
