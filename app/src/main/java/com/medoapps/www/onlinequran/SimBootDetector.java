package com.medoapps.www.onlinequran;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

/**
 * Created by MEDO on 12/09/2017.
 */

public class SimBootDetector extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            if (Objects.requireNonNull(intent.getAction()).equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                Intent serviceIntent = new Intent(context, AndroidServiceStartOnBoot.class);
                context.startService(serviceIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}
