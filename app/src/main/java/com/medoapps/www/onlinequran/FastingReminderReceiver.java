package com.medoapps.www.onlinequran;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.medoapps.www.onlinequran.athan.HijriDate;
import com.medoapps.www.onlinequran.athan.PrayerSettings;

import java.util.Calendar;

public class FastingReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "fasting_reminder_channel";
    private static final int NOTIFICATION_ID = 1004;

    @Override
    public void onReceive(Context context, Intent intent) {
        // The Suhur reminder fires on a daily repeating alarm, so even when the
        // user has it enabled it must only notify during Ramadan (hijri month 9).
        // Apply the user's hijri day offset the same way HijriDate.todayString does.
        Calendar day = Calendar.getInstance();
        day.add(Calendar.DAY_OF_YEAR, PrayerSettings.getHijriOffset(context));
        if (!HijriDate.isRamadan(day)) {
            return;
        }

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "تذكير السحور", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("تنبيه لوقت السحور قبل الفجر");
            nm.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, FastingTrackerActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_fasting)
                .setContentTitle("تذكير السحور")
                .setContentText("حان وقت السحور، لا تنسَ نية الصيام")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("حان وقت السحور، لا تنسَ نية الصيام\nقال ﷺ: «تسحروا فإن في السحور بركة»"))
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);

        nm.notify(NOTIFICATION_ID, builder.build());
    }
}
