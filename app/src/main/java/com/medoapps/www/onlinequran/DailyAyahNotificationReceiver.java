package com.medoapps.www.onlinequran;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

/**
 * BroadcastReceiver triggered daily by AlarmManager to show a Quran ayah notification.
 * Also triggers DailyAyahWidget update so the widget stays in sync.
 */
public class DailyAyahNotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "daily_ayah_channel";
    private static final String CHANNEL_NAME = "آية اليوم";
    private static final int NOTIFICATION_ID = 1001;

    private static final String[] AYAH_TEXTS = {
            "وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ",
            "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            "وَاصْبِرْ فَإِنَّ اللَّهَ لَا يُضِيعُ أَجْرَ الْمُحْسِنِينَ",
            "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي",
            "وَقُل رَّبِّ زِدْنِي عِلْمًا",
            "فَاذْكُرُونِي أَذْكُرْكُمْ",
            "وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ",
            "إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
            "وَمَا تَوْفِيقِي إِلَّا بِاللَّهِ",
            "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            "وَإِلَىٰ رَبِّكَ فَارْغَب",
            "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً",
            "وَهُوَ مَعَكُمْ أَيْنَ مَا كُنتُمْ",
            "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا",
            "وَاللَّهُ خَيْرُ الرَّازِقِينَ",
            "إِنَّا فَتَحْنَا لَكَ فَتْحًا مُّبِينًا",
            "وَرَحْمَتِي وَسِعَتْ كُلَّ شَيْءٍ",
            "لَا تَحْزَنْ إِنَّ اللَّهَ مَعَنَا",
            "وَنَحْنُ أَقْرَبُ إِلَيْهِ مِنْ حَبْلِ الْوَرِيدِ",
            "وَاللَّهُ يُحِبُّ الْمُحْسِنِينَ",
            "إِنَّ اللَّهَ لَا يُضِيعُ أَجْرَ الْمُحْسِنِينَ",
            "وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ",
            "قُلْ هُوَ اللَّهُ أَحَدٌ",
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            "وَكَفَىٰ بِاللَّهِ وَكِيلًا",
            "وَاللَّهُ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            "ادْعُونِي أَسْتَجِبْ لَكُمْ",
            "وَاللَّهُ الْمُسْتَعَانُ",
            "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
            "رَبِّ لَا تَذَرْنِي فَرْدًا وَأَنتَ خَيْرُ الْوَارِثِينَ",
            "وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا"
    };

    private static final String[] AYAH_REFERENCES = {
            "الطلاق: 3",
            "الشرح: 6",
            "هود: 115",
            "طه: 25-26",
            "طه: 114",
            "البقرة: 152",
            "الضحى: 5",
            "البقرة: 153",
            "هود: 88",
            "الرعد: 28",
            "الشرح: 8",
            "البقرة: 201",
            "الحديد: 4",
            "الشرح: 5",
            "الجمعة: 11",
            "الفتح: 1",
            "الأعراف: 156",
            "التوبة: 40",
            "ق: 16",
            "آل عمران: 134",
            "التوبة: 120",
            "البقرة: 45",
            "الإخلاص: 1",
            "الفاتحة: 1",
            "النساء: 81",
            "البقرة: 284",
            "غافر: 60",
            "يوسف: 18",
            "آل عمران: 173",
            "الأنبياء: 89",
            "الطلاق: 2"
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        // Pick ayah based on day of year (same logic as DailyAyahWidget)
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        int index = dayOfYear % AYAH_TEXTS.length;

        String ayahText = AYAH_TEXTS[index];
        String ayahReference = AYAH_REFERENCES[index];

        showNotification(context, ayahText, ayahReference);
        updateWidget(context);
    }

    private void showNotification(Context context, String ayahText, String ayahReference) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("إشعار آية اليوم من القرآن الكريم");
            notificationManager.createNotificationChannel(channel);
        }

        // PendingIntent to open MainActivity on tap
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_athkar)
                .setContentTitle("آية اليوم")
                .setContentText(ayahText)
                .setSubText(ayahReference)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(ayahText))
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void updateWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, DailyAyahWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);
        for (int appWidgetId : appWidgetIds) {
            DailyAyahWidget.updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}
