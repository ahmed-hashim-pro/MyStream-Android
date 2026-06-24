// app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleBootReceiver.java
package com.medoapps.www.onlinequran.bubble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Restores the bubble after reboot and re-applies it at window boundaries. */
public class BubbleBootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        // Both BOOT_COMPLETED and our boundary alarm land here.
        BubbleScheduler.reschedule(context);
    }
}
