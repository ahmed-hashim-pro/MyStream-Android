package com.medoapps.www.onlinequran;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by MEDO on 11/01/2018.
 */

public class NotificationReturnSlot extends Activity {
public managerdb manager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        String action = (String) getIntent().getExtras().get("DO");
        if (action.equals("volume")) {
            Log.i("NotificationReturnSlot", "volume");
            //Your code
            /*
            Intent i = new Intent();
            i.setClassName(" com.medoapps.www.onlinequran", " com.medoapps.www.onlinequran");
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            */


            Intent i = getPackageManager().getLaunchIntentForPackage("com.medoapps.www.onlinequran");
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.setPackage(null);
            startActivity(i);


        } else if (action.equals("stop")) {
            //Your code


            Log.i("NotificationReturnSlot", "stopNotification");

        }
        else if (action.equals("prev")) {
            //Your code

            Log.i("NotificationReturnSlot", "prev");

        }
        finish();
    }
}