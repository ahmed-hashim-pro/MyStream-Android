package com.medoapps.www.onlinequran.athan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Re-creates every athan alarm after events that wipe or invalidate the
 * AlarmManager schedule: device boot, timezone change, manual clock change,
 * and app update.
 */
public class AthanBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            AthanScheduler.rescheduleAll(context);
        }
    }
}
