package com.medoapps.www.onlinequran;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.Calendar;

/**
 * Home-screen widget that displays a daily Quran ayah.
 * The ayah changes once per day based on the day-of-year index.
 */
public class DailyAyahWidget extends AppWidgetProvider {

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
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Pick ayah based on day of year (consistent throughout the day, changes daily)
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        int index = dayOfYear % AYAH_TEXTS.length;

        String ayahText = AYAH_TEXTS[index];
        String ayahReference = AYAH_REFERENCES[index];

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_daily_ayah);
        views.setTextViewText(R.id.tv_widget_ayah, ayahText);
        views.setTextViewText(R.id.tv_widget_reference, ayahReference);

        // Tapping the widget opens the main activity
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
