package com.medoapps.www.onlinequran;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FastingTrackerActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "fasting_tracker_prefs";
    private static final String KEY_FASTED_DATES = "fasted_dates";

    private SharedPreferences prefs;

    private TextView tvMonthCount;
    private TextView tvYearCount;
    private TextView tvStreak;
    private TextView tvTodayDate;
    private TextView tvTodayStatus;
    private TextView tvBtnMarkToday;
    private MaterialCardView btnMarkToday;
    private LinearLayout llUpcomingDays;
    private LinearLayout llHistory;

    // Chip views
    private MaterialCardView chipMonday;
    private MaterialCardView chipThursday;
    private MaterialCardView chipWhiteDays;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    // Day names — populated from the current locale in onCreate (values below are a fallback only).
    private String[] ARABIC_DAY_NAMES = {
            "\u0627\u0644\u0623\u062d\u062f",       // الأحد
            "\u0627\u0644\u0625\u062b\u0646\u064a\u0646",   // الإثنين
            "\u0627\u0644\u062b\u0644\u0627\u062b\u0627\u0621", // الثلاثاء
            "\u0627\u0644\u0623\u0631\u0628\u0639\u0627\u0621", // الأربعاء
            "\u0627\u0644\u062e\u0645\u064a\u0633",   // الخميس
            "\u0627\u0644\u062c\u0645\u0639\u0629",   // الجمعة
            "\u0627\u0644\u0633\u0628\u062a"         // السبت
    };

    // Month names — populated from the current locale in onCreate (values below are a fallback only).
    private String[] ARABIC_MONTH_NAMES = {
            "\u064a\u0646\u0627\u064a\u0631",   // يناير
            "\u0641\u0628\u0631\u0627\u064a\u0631",  // فبراير
            "\u0645\u0627\u0631\u0633",         // مارس
            "\u0623\u0628\u0631\u064a\u0644",    // أبريل
            "\u0645\u0627\u064a\u0648",         // مايو
            "\u064a\u0648\u0646\u064a\u0648",    // يونيو
            "\u064a\u0648\u0644\u064a\u0648",    // يوليو
            "\u0623\u063a\u0633\u0637\u0633",    // أغسطس
            "\u0633\u0628\u062a\u0645\u0628\u0631",  // سبتمبر
            "\u0623\u0643\u062a\u0648\u0628\u0631",  // أكتوبر
            "\u0646\u0648\u0641\u0645\u0628\u0631",  // نوفمبر
            "\u062f\u064a\u0633\u0645\u0628\u0631"   // ديسمبر
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fasting_tracker);

        // Localize day/month names to the current app language (Sun=1..Sat=7; months 0..11).
        java.util.Locale dateLocale = androidx.core.os.ConfigurationCompat
                .getLocales(getResources().getConfiguration()).get(0);
        java.text.DateFormatSymbols dfs = new java.text.DateFormatSymbols(dateLocale);
        String[] weekdays = dfs.getWeekdays();
        ARABIC_DAY_NAMES = new String[]{weekdays[1], weekdays[2], weekdays[3],
                weekdays[4], weekdays[5], weekdays[6], weekdays[7]};
        ARABIC_MONTH_NAMES = dfs.getMonths();

        // Navy hero header
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        HeroController.attach(this).back().centered().title(R.string.fasting_title).apply();

        // Nav bar color
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Bind views
        tvMonthCount = findViewById(R.id.tv_month_count);
        tvYearCount = findViewById(R.id.tv_year_count);
        tvStreak = findViewById(R.id.tv_streak);
        tvTodayDate = findViewById(R.id.tv_today_date);
        tvTodayStatus = findViewById(R.id.tv_today_status);
        tvBtnMarkToday = findViewById(R.id.tv_btn_mark_today);
        btnMarkToday = findViewById(R.id.btn_mark_today);
        llUpcomingDays = findViewById(R.id.ll_upcoming_days);
        llHistory = findViewById(R.id.ll_history);

        chipMonday = findViewById(R.id.chip_monday);
        chipThursday = findViewById(R.id.chip_thursday);
        chipWhiteDays = findViewById(R.id.chip_white_days);

        // Set today's date display
        Calendar today = Calendar.getInstance();
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK); // Sunday=1
        String dayName = ARABIC_DAY_NAMES[dayOfWeek - 1];
        int day = today.get(Calendar.DAY_OF_MONTH);
        String monthName = ARABIC_MONTH_NAMES[today.get(Calendar.MONTH)];
        tvTodayDate.setText(dayName + " " + day + " " + monthName);

        // Highlight today's Sunnah chip if applicable
        highlightSunnahChips(today);

        // Mark today button
        btnMarkToday.setOnClickListener(v -> toggleTodayFasting());

        // Initial UI update
        updateUI();
    }

    /**
     * Highlights chips if today is a Sunnah fasting day.
     */
    private void highlightSunnahChips(Calendar today) {
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek == Calendar.MONDAY) {
            chipMonday.setStrokeColor(ContextCompat.getColorStateList(this, R.color.gold_accent));
        }
        if (dayOfWeek == Calendar.THURSDAY) {
            chipThursday.setStrokeColor(ContextCompat.getColorStateList(this, R.color.gold_accent));
        }

        // White days: 13, 14, 15 of each Hijri month
        // Approximate check using day of Gregorian month as a rough indicator
        // (Note: accurate Hijri conversion requires a library; this is a simple heuristic)
        int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);
        // We show this chip as highlighted as a general reminder
        chipWhiteDays.setOnClickListener(v -> {
            // Info: White days are 13, 14, 15 of each Hijri month
        });
    }

    /**
     * Toggle today's fasting status.
     */
    private void toggleTodayFasting() {
        String todayStr = sdf.format(new Date());
        List<String> dates = loadFastedDates();

        if (dates.contains(todayStr)) {
            // Remove: un-mark today
            dates.remove(todayStr);
        } else {
            // Add: mark today
            dates.add(todayStr);
        }

        saveFastedDates(dates);
        updateUI();
    }

    /**
     * Refresh all UI elements based on stored data.
     */
    private void updateUI() {
        List<String> dates = loadFastedDates();
        String todayStr = sdf.format(new Date());
        Calendar now = Calendar.getInstance();

        // -- Today status --
        boolean fastedToday = dates.contains(todayStr);
        if (fastedToday) {
            tvTodayStatus.setText(getString(R.string.fasting_status_recorded)); // تم تسجيل الصيام
            tvTodayStatus.setTextColor(ContextCompat.getColor(this, R.color.gold_accent));
            tvBtnMarkToday.setText(getString(R.string.fasting_btn_unmark)); // إلغاء التسجيل
            btnMarkToday.setCardBackgroundColor(ContextCompat.getColor(this, R.color.background_input_button));
            tvBtnMarkToday.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        } else {
            tvTodayStatus.setText(getString(R.string.fasting_status_not_recorded)); // لم يتم التسجيل بعد
            tvTodayStatus.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tvBtnMarkToday.setText(getString(R.string.fasting_btn_mark_today)); // تسجيل صيام اليوم
            btnMarkToday.setCardBackgroundColor(ContextCompat.getColor(this, R.color.gold_accent));
            tvBtnMarkToday.setTextColor(ContextCompat.getColor(this, R.color.text_on_gold));
        }

        // -- Month count --
        int monthCount = 0;
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);
        for (String dateStr : dates) {
            try {
                Date d = sdf.parse(dateStr);
                if (d != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    if (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear) {
                        monthCount++;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        tvMonthCount.setText(String.valueOf(monthCount));

        // -- Year count --
        int yearCount = 0;
        for (String dateStr : dates) {
            try {
                Date d = sdf.parse(dateStr);
                if (d != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    if (cal.get(Calendar.YEAR) == currentYear) {
                        yearCount++;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        tvYearCount.setText(String.valueOf(yearCount));

        // -- Streak (consecutive Sunnah fasting days: Monday/Thursday) --
        int streak = calculateStreak(dates);
        tvStreak.setText(String.valueOf(streak));

        // -- Upcoming suggested fasting days --
        populateUpcomingDays();

        // -- Recent history --
        populateHistory(dates);
    }

    /**
     * Calculate streak of consecutive Sunnah fasting days (Monday and Thursday).
     * Counts backwards from the most recent fasted Sunnah day without a gap.
     */
    private int calculateStreak(List<String> dates) {
        if (dates.isEmpty()) return 0;

        // Sort dates descending
        List<String> sorted = new ArrayList<>(dates);
        Collections.sort(sorted, Collections.reverseOrder());

        // Get all Sunnah days (Mon/Thu) going backwards from today
        List<String> sunnahDays = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        // Look back up to 180 days
        for (int i = 0; i < 180; i++) {
            int dow = cal.get(Calendar.DAY_OF_WEEK);
            if (dow == Calendar.MONDAY || dow == Calendar.THURSDAY) {
                sunnahDays.add(sdf.format(cal.getTime()));
            }
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        int streak = 0;
        for (String sunnahDay : sunnahDays) {
            if (sorted.contains(sunnahDay)) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * Populate the upcoming suggested fasting days section.
     */
    private void populateUpcomingDays() {
        llUpcomingDays.removeAllViews();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1); // Start from tomorrow

        int count = 0;
        int maxDays = 6; // Show up to 6 upcoming Sunnah days

        for (int i = 0; i < 60 && count < maxDays; i++) {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.MONDAY || dayOfWeek == Calendar.THURSDAY) {
                addUpcomingDayRow(cal, dayOfWeek == Calendar.MONDAY
                        ? ARABIC_DAY_NAMES[1]   // Monday (locale-aware)
                        : ARABIC_DAY_NAMES[4]); // Thursday (locale-aware)
                count++;
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (count == 0) {
            addEmptyRow(llUpcomingDays, getString(R.string.fasting_no_upcoming_days)); // لا توجد أيام قادمة
        }
    }

    /**
     * Add a row for an upcoming day.
     */
    private void addUpcomingDayRow(Calendar cal, String dayName) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dpToPx(6), 0, dpToPx(6));

        // Gold dot
        View dot = new View(this);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
        dotParams.setMarginEnd(dpToPx(10));
        dot.setLayoutParams(dotParams);
        dot.setBackgroundColor(ContextCompat.getColor(this, R.color.gold_accent));

        // Day name
        TextView tvDay = new TextView(this);
        tvDay.setText(dayName);
        tvDay.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        tvDay.setTextSize(14);
        tvDay.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvDay.setLayoutParams(dayParams);

        // Date
        TextView tvDate = new TextView(this);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String monthName = ARABIC_MONTH_NAMES[cal.get(Calendar.MONTH)];
        tvDate.setText(day + " " + monthName);
        tvDate.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tvDate.setTextSize(13);

        row.addView(dot);
        row.addView(tvDay);
        row.addView(tvDate);

        llUpcomingDays.addView(row);

        // Divider (except if it's the container's last child, handled by max count)
        View divider = new View(this);
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
        divParams.topMargin = dpToPx(2);
        divider.setLayoutParams(divParams);
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.gold_accent_faint));
        llUpcomingDays.addView(divider);
    }

    /**
     * Populate the recent history section.
     */
    private void populateHistory(List<String> dates) {
        llHistory.removeAllViews();

        if (dates.isEmpty()) {
            addEmptyRow(llHistory, getString(R.string.fasting_no_history)); // لا يوجد سجل بعد
            return;
        }

        // Sort descending and show the last 15
        List<String> sorted = new ArrayList<>(dates);
        Collections.sort(sorted, Collections.reverseOrder());

        int limit = Math.min(sorted.size(), 15);
        for (int i = 0; i < limit; i++) {
            String dateStr = sorted.get(i);
            addHistoryRow(dateStr);
        }
    }

    /**
     * Add a row for a historical fasted date.
     */
    private void addHistoryRow(String dateStr) {
        try {
            Date d = sdf.parse(dateStr);
            if (d == null) return;

            Calendar cal = Calendar.getInstance();
            cal.setTime(d);

            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            String dayName = ARABIC_DAY_NAMES[dayOfWeek - 1];
            int day = cal.get(Calendar.DAY_OF_MONTH);
            String monthName = ARABIC_MONTH_NAMES[cal.get(Calendar.MONTH)];

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dpToPx(6), 0, dpToPx(6));

            // Checkmark-like gold indicator
            TextView checkView = new TextView(this);
            checkView.setText("\u2713"); // checkmark
            checkView.setTextColor(ContextCompat.getColor(this, R.color.gold_accent));
            checkView.setTextSize(16);
            LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            checkParams.setMarginEnd(dpToPx(10));
            checkView.setLayoutParams(checkParams);

            // Day name
            TextView tvDay = new TextView(this);
            tvDay.setText(dayName);
            tvDay.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tvDay.setTextSize(14);
            tvDay.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            tvDay.setLayoutParams(dayParams);

            // Date
            TextView tvDate = new TextView(this);
            tvDate.setText(day + " " + monthName);
            tvDate.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            tvDate.setTextSize(13);

            row.addView(checkView);
            row.addView(tvDay);
            row.addView(tvDate);

            llHistory.addView(row);

            // Divider
            View divider = new View(this);
            LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
            divParams.topMargin = dpToPx(2);
            divider.setLayoutParams(divParams);
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.gold_accent_faint));
            llHistory.addView(divider);
        } catch (Exception ignored) {
        }
    }

    /**
     * Add a placeholder row when there is no data.
     */
    private void addEmptyRow(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tv.setTextSize(14);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, dpToPx(16), 0, dpToPx(16));
        parent.addView(tv);
    }

    // ================================================================
    // Persistence (SharedPreferences + JSON array)
    // ================================================================

    private List<String> loadFastedDates() {
        String json = prefs.getString(KEY_FASTED_DATES, "[]");
        List<String> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getString(i));
            }
        } catch (JSONException ignored) {
        }
        return list;
    }

    private void saveFastedDates(List<String> dates) {
        JSONArray arr = new JSONArray();
        for (String d : dates) {
            arr.put(d);
        }
        prefs.edit().putString(KEY_FASTED_DATES, arr.toString()).apply();
    }

    // ================================================================
    // Utility
    // ================================================================

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
