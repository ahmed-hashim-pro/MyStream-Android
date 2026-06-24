package com.medoapps.www.onlinequran;

import android.content.Context;
import android.content.SharedPreferences;

/** Per-day athkar completion, shared by the floating bubble and the Stories rings.
 *  Stateless w.r.t. "today": callers pass the day-of-year so the logic stays testable. */
public class AthkarProgressStore {
    public static final String PREFS = "athkar_daily_prefs";
    private final SharedPreferences prefs;

    public AthkarProgressStore(Context context) {
        this.prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String key(int dayOfYear, String session, int index) {
        return "done_" + dayOfYear + "_" + session + "_" + index;
    }

    public void markDone(int dayOfYear, String session, int index) {
        prefs.edit().putBoolean(key(dayOfYear, session, index), true).apply();
    }

    public boolean isDone(int dayOfYear, String session, int index) {
        return prefs.getBoolean(key(dayOfYear, session, index), false);
    }

    public int doneCount(int dayOfYear, String session, int total) {
        int n = 0;
        for (int i = 0; i < total; i++) if (isDone(dayOfYear, session, i)) n++;
        return n;
    }
}
