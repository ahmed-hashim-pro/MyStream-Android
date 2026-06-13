package com.medoapps.www.onlinequran.athan;

import android.content.Context;

import com.medoapps.www.onlinequran.R;

import java.util.Calendar;

/**
 * Tabular (Kuwaiti-algorithm) hijri conversion with the user's manual day
 * offset applied. Self-contained so the athan package does not depend on UI
 * classes elsewhere in the app.
 */
public final class HijriDate {

    private HijriDate() {
    }

    /** Returns {hijriYear, hijriMonth 1-12, hijriDay} for a gregorian calendar day. */
    public static int[] fromGregorian(Calendar gregorian) {
        int y = gregorian.get(Calendar.YEAR);
        int m = gregorian.get(Calendar.MONTH) + 1;
        int d = gregorian.get(Calendar.DAY_OF_MONTH);

        long jd = (1461L * (y + 4800 + (m - 14) / 12)) / 4
                + (367L * (m - 2 - 12 * ((m - 14) / 12))) / 12
                - (3L * ((y + 4900 + (m - 14) / 12) / 100)) / 4
                + d - 32075;

        long l = jd - 1948440 + 10632;
        long n = (l - 1) / 10631;
        l = l - 10631 * n + 354;
        long j = ((10985 - l) / 5316) * ((50 * l) / 17719)
                + (l / 5670) * ((43 * l) / 15238);
        l = l - ((30 - j) / 15) * ((17719 * j) / 50)
                - (j / 16) * ((15238 * j) / 43) + 29;
        int hm = (int) ((24 * l) / 709);
        int hd = (int) (l - (709L * hm) / 24);
        int hy = (int) (30 * n + j - 30);

        return new int[]{hy, hm, hd};
    }

    /** Today's hijri date with the user's offset, e.g. "27 ذو الحجة 1447 هـ". */
    public static String todayString(Context context) {
        Calendar day = Calendar.getInstance();
        day.add(Calendar.DAY_OF_YEAR, PrayerSettings.getHijriOffset(context));
        int[] h = fromGregorian(day);
        String[] months = context.getResources().getStringArray(R.array.athan_hijri_months);
        String month = (h[1] >= 1 && h[1] <= 12) ? months[h[1] - 1] : String.valueOf(h[1]);
        return context.getString(R.string.athan_hijri_date_format, h[2], month, h[0]);
    }
}
