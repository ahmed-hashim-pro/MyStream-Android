package com.medoapps.www.onlinequran.athan;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.HighLatitudeRule;
import com.batoulapps.adhan.Madhab;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;
import com.medoapps.www.onlinequran.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * On-device prayer-time calculation. No network involved: times are computed
 * with the adhan library from the saved coordinates and the user's calculation
 * settings, including per-prayer manual corrections.
 *
 * Prayer indices follow {@link PrayerSettings}: 0 Fajr, 1 Sunrise, 2 Dhuhr,
 * 3 Asr, 4 Maghrib, 5 Isha.
 */
public final class PrayerTimeEngine {

    /** String resources for prayer names, indexed by prayer index. */
    public static final int[] PRAYER_NAME_RES = {
            R.string.athan_prayer_fajr,
            R.string.athan_prayer_sunrise,
            R.string.athan_prayer_dhuhr,
            R.string.athan_prayer_asr,
            R.string.athan_prayer_maghrib,
            R.string.athan_prayer_isha
    };

    private PrayerTimeEngine() {
    }

    /** Computed times for one day, in prayer-index order. */
    public static Date[] getTimes(Context context, Calendar day) {
        Coordinates coordinates = new Coordinates(
                PrayerSettings.getLatitude(context), PrayerSettings.getLongitude(context));
        DateComponents date = new DateComponents(
                day.get(Calendar.YEAR), day.get(Calendar.MONTH) + 1, day.get(Calendar.DAY_OF_MONTH));

        CalculationParameters params = getMethod(context).getParameters();
        params.madhab = getMadhab(context);
        params.highLatitudeRule = getHighLatitudeRule(context);
        params.adjustments.fajr = PrayerSettings.getCorrection(context, PrayerSettings.PRAYER_FAJR);
        params.adjustments.sunrise = PrayerSettings.getCorrection(context, PrayerSettings.PRAYER_SUNRISE);
        params.adjustments.dhuhr = PrayerSettings.getCorrection(context, PrayerSettings.PRAYER_DHUHR);
        params.adjustments.asr = PrayerSettings.getCorrection(context, PrayerSettings.PRAYER_ASR);
        params.adjustments.maghrib = PrayerSettings.getCorrection(context, PrayerSettings.PRAYER_MAGHRIB);
        params.adjustments.isha = PrayerSettings.getCorrection(context, PrayerSettings.PRAYER_ISHA);

        PrayerTimes pt = new PrayerTimes(coordinates, date, params);
        return new Date[]{pt.fajr, pt.sunrise, pt.dhuhr, pt.asr, pt.maghrib, pt.isha};
    }

    public static Date[] getTodayTimes(Context context) {
        return getTimes(context, Calendar.getInstance());
    }

    /**
     * Index of the next upcoming prayer (never sunrise), or
     * {@link PrayerSettings#PRAYER_FAJR} for tomorrow when today is done.
     */
    public static int getNextPrayerIndex(Context context) {
        Date now = new Date();
        Date[] today = getTodayTimes(context);
        for (int i = 0; i < today.length; i++) {
            if (i == PrayerSettings.PRAYER_SUNRISE) continue;
            if (today[i].after(now)) return i;
        }
        return PrayerSettings.PRAYER_FAJR;
    }

    /** Time of the next upcoming prayer, rolling over to tomorrow's fajr. */
    public static Date getNextPrayerTime(Context context) {
        Date now = new Date();
        Date[] today = getTodayTimes(context);
        for (int i = 0; i < today.length; i++) {
            if (i == PrayerSettings.PRAYER_SUNRISE) continue;
            if (today[i].after(now)) return today[i];
        }
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        return getTimes(context, tomorrow)[PrayerSettings.PRAYER_FAJR];
    }

    /** Formats a time using the device's 12/24-hour preference. */
    public static String formatTime(Context context, Date time) {
        return DateFormat.getTimeFormat(context).format(time);
    }

    /**
     * Writes the HH:mm cache the home-screen widget reads and pokes it to
     * refresh, so the widget stays in sync with the engine.
     */
    public static void updateWidgetCache(Context context) {
        Date[] times = getTodayTimes(context);
        java.text.SimpleDateFormat hm = new java.text.SimpleDateFormat("HH:mm", Locale.US);
        context.getSharedPreferences("prayer_times_cache", Context.MODE_PRIVATE).edit()
                .putString("fajr", hm.format(times[0]))
                .putString("sunrise", hm.format(times[1]))
                .putString("dhuhr", hm.format(times[2]))
                .putString("asr", hm.format(times[3]))
                .putString("maghrib", hm.format(times[4]))
                .putString("isha", hm.format(times[5]))
                .apply();
        Intent widgetIntent = new Intent(context, com.medoapps.www.onlinequran.PrayerTimesWidget.class);
        widgetIntent.setAction("com.medoapps.UPDATE_PRAYER_WIDGET");
        context.sendBroadcast(widgetIntent);
    }

    // ------------------------------------------------------------- enums

    public static CalculationMethod getMethod(Context context) {
        try {
            return CalculationMethod.valueOf(PrayerSettings.getCalculationMethod(context));
        } catch (IllegalArgumentException e) {
            return CalculationMethod.UMM_AL_QURA;
        }
    }

    public static Madhab getMadhab(Context context) {
        try {
            return Madhab.valueOf(PrayerSettings.getMadhab(context));
        } catch (IllegalArgumentException e) {
            return Madhab.SHAFI;
        }
    }

    public static HighLatitudeRule getHighLatitudeRule(Context context) {
        try {
            return HighLatitudeRule.valueOf(PrayerSettings.getHighLatitudeRule(context));
        } catch (IllegalArgumentException e) {
            return HighLatitudeRule.MIDDLE_OF_THE_NIGHT;
        }
    }
}
