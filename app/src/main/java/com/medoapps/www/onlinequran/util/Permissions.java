package com.medoapps.www.onlinequran.util;

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import com.medoapps.www.onlinequran.BuildConfig;
import com.medoapps.www.onlinequran.R;

public class Permissions {


    Context context;
    Activity activity;
    private SeparateFunctions separateFunctions;

    final public static int REQUEST_CODE_ASK_STORAGE_PERMISSIONS = 122;
    final private int REQUEST_CODE_ASK_MICROPHONE_PERMISSIONS = 124;
    final private int APP_STORAGE_ACCESS_REQUEST_CODE = 126;


    public Permissions(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        separateFunctions = new SeparateFunctions(context);

    }


    public Boolean checkStoragePermission(){
        // On API 30+, MediaStore is used (scoped storage) — no legacy permission needed
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return true;
        }
        return askStoragePermission();
    }
    public Boolean checkStoragePermissionForService(){
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return true;
        }
        if ((int) SDK_INT >= 23)
        {
            if (
                    (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||
                            (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            )
            {

                return false;
            }else{
                return true;
            }

        }else{
            return true;
        }
    }
    public Boolean checkStoragePermissionWithoutAsk(){
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return true;
        }
        if ((int) SDK_INT >= 23)
        {
            if (
                    (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||
                            (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            )
            {

                return false;
            }else{
                return true;
            }

        }else{
            return true;
        }
    }

    public Boolean checkFullStoragePermission(){
        if (SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {

                separateFunctions.showNewCustomDialog(context.getString(R.string.needPermissionTitle),context.getString(R.string.needSstoragePermissionDescription),context.getString(android.R.string.yes),context.getString(android.R.string.no),openFullStoragePermissionIntentRunnable,android.R.drawable.ic_dialog_info);

                return false;
            }else {
                return true;
            }
        }else{

            return askStoragePermission();
        }
    }

    private Runnable openFullStoragePermissionIntentRunnable = new Runnable() {
        public void run() {
            openFullStoragePermissionIntent();
        }
    };
    private Void openFullStoragePermissionIntent(){

        try {
            Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
            activity.startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
        } catch (Exception ex) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            activity.startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
        }
        return null;
    }

    private boolean askStoragePermission(){
        if ((int) SDK_INT >= 23)
        {
            if (
                    (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||
                            (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            )
            {

                if (SDK_INT >= Build.VERSION_CODES.M) {
                    activity.requestPermissions(new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                            },
                            REQUEST_CODE_ASK_STORAGE_PERMISSIONS);
                }
                /*if (SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissions(new String[]{
                                    Manifest.permission.MANAGE_MEDIA,
                            },
                            REQUEST_CODE_ASK_STORAGE_PERMISSIONS);
                }*/



                return false;
            }else{
                return true;
            }

        }else{
            return true;
        }
    }

    private void askMicrophonePermission(){
        if ((int) SDK_INT >= 23)
        {
            if (( ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)||
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED)
            {

                if (SDK_INT >= Build.VERSION_CODES.M) {
                    activity.requestPermissions(new String[]{
                                    Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.MODIFY_AUDIO_SETTINGS
                            },
                            REQUEST_CODE_ASK_MICROPHONE_PERMISSIONS);
                }


                return;
            }

        }
    }

}
