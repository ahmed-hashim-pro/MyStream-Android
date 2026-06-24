package com.medoapps.www.onlinequran;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReadingProgressActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "reading_progress";
    private static final String KEY_TODAY_PAGES = "today_pages";
    private static final String KEY_TOTAL_PAGES = "total_pages";
    private static final String KEY_STREAK_DAYS = "streak_days";
    private static final String KEY_LAST_READ_DATE = "last_read_date";
    private static final String KEY_DAILY_GOAL = "daily_goal";

    private SharedPreferences prefs;
    private Vibrator vibrator;

    private int todayPages = 0;
    private int totalPages = 0;
    private int streakDays = 0;
    private int dailyGoal = 5;

    private TextView tvProgressCount;
    private TextView tvCongrats;
    private TextView tvStreak;
    private TextView tvTotal;

    // Goal buttons
    private MaterialCardView btnGoal1;
    private MaterialCardView btnGoal3;
    private MaterialCardView btnGoal5;
    private MaterialCardView btnGoal10;
    private MaterialCardView btnGoal20;
    private MaterialCardView selectedGoalBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_progress);

        // Navy hero header
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        HeroController.attach(this).back().centered().title(R.string.reading_progress).apply();

        // Nav bar color
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        // Init vibrator and prefs
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Bind views
        tvProgressCount = findViewById(R.id.tv_progress_count);
        tvCongrats = findViewById(R.id.tv_congrats);
        tvStreak = findViewById(R.id.tv_streak);
        tvTotal = findViewById(R.id.tv_total);

        MaterialCardView btnMinus = findViewById(R.id.btn_minus);
        MaterialCardView btnPlus = findViewById(R.id.btn_plus);

        btnGoal1 = findViewById(R.id.btn_goal_1);
        btnGoal3 = findViewById(R.id.btn_goal_3);
        btnGoal5 = findViewById(R.id.btn_goal_5);
        btnGoal10 = findViewById(R.id.btn_goal_10);
        btnGoal20 = findViewById(R.id.btn_goal_20);

        // Load saved data and handle date logic
        loadProgress();

        // Set initial selected goal button
        selectedGoalBtn = getGoalButton(dailyGoal);
        if (selectedGoalBtn != null) {
            selectedGoalBtn.setStrokeColor(ContextCompat.getColorStateList(this, R.color.gold_accent));
        }

        // Update all displays
        updateUI();

        // Button click listeners
        btnPlus.setOnClickListener(v -> addPage());
        btnMinus.setOnClickListener(v -> removePage());

        // Goal button listeners
        btnGoal1.setOnClickListener(v -> selectGoal(btnGoal1, 1));
        btnGoal3.setOnClickListener(v -> selectGoal(btnGoal3, 3));
        btnGoal5.setOnClickListener(v -> selectGoal(btnGoal5, 5));
        btnGoal10.setOnClickListener(v -> selectGoal(btnGoal10, 10));
        btnGoal20.setOnClickListener(v -> selectGoal(btnGoal20, 20));
    }

    private void loadProgress() {
        totalPages = prefs.getInt(KEY_TOTAL_PAGES, 0);
        streakDays = prefs.getInt(KEY_STREAK_DAYS, 0);
        dailyGoal = prefs.getInt(KEY_DAILY_GOAL, 5);
        String lastReadDate = prefs.getString(KEY_LAST_READ_DATE, "");

        String today = getTodayDate();
        String yesterday = getYesterdayDate();

        if (today.equals(lastReadDate)) {
            // Same day - load today's pages
            todayPages = prefs.getInt(KEY_TODAY_PAGES, 0);
        } else if (yesterday.equals(lastReadDate)) {
            // Yesterday - keep streak, reset today's pages
            todayPages = 0;
        } else {
            // Older or no date - reset streak and today's pages
            streakDays = 0;
            todayPages = 0;
        }
    }

    private void addPage() {
        todayPages++;
        totalPages++;
        saveProgress();
        updateUI();

        // Check if goal reached exactly
        if (todayPages == dailyGoal) {
            vibrateShort();
            tvCongrats.setVisibility(View.VISIBLE);
        }
    }

    private void removePage() {
        if (todayPages > 0) {
            todayPages--;
            if (totalPages > 0) {
                totalPages--;
            }
            saveProgress();
            updateUI();

            // Hide congrats if we drop below goal
            if (todayPages < dailyGoal) {
                tvCongrats.setVisibility(View.GONE);
            }
        }
    }

    private void selectGoal(MaterialCardView btn, int goal) {
        // Deselect previous
        if (selectedGoalBtn != null) {
            selectedGoalBtn.setStrokeColor(ContextCompat.getColorStateList(this, R.color.gold_accent_faint));
        }
        // Select new
        selectedGoalBtn = btn;
        selectedGoalBtn.setStrokeColor(ContextCompat.getColorStateList(this, R.color.gold_accent));
        dailyGoal = goal;

        // Save and update
        prefs.edit().putInt(KEY_DAILY_GOAL, dailyGoal).apply();
        updateUI();
    }

    private void saveProgress() {
        String today = getTodayDate();
        String lastReadDate = prefs.getString(KEY_LAST_READ_DATE, "");

        // If this is the first read of a new day, increment streak
        if (!today.equals(lastReadDate) && todayPages == 1) {
            streakDays++;
        }

        prefs.edit()
                .putInt(KEY_TODAY_PAGES, todayPages)
                .putInt(KEY_TOTAL_PAGES, totalPages)
                .putInt(KEY_STREAK_DAYS, streakDays)
                .putString(KEY_LAST_READ_DATE, today)
                .apply();
    }

    private void updateUI() {
        // Progress count
        tvProgressCount.setText(todayPages + " / " + dailyGoal);

        // Streak display
        tvStreak.setText(getString(R.string.reading_streak_format, streakDays));

        // Total pages
        tvTotal.setText(getString(R.string.reading_total_pages_label, totalPages));

        // Congrats visibility
        if (todayPages >= dailyGoal) {
            tvCongrats.setVisibility(View.VISIBLE);
        } else {
            tvCongrats.setVisibility(View.GONE);
        }
    }

    private void vibrateShort() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(50);
        }
    }

    private MaterialCardView getGoalButton(int goal) {
        switch (goal) {
            case 1: return btnGoal1;
            case 3: return btnGoal3;
            case 5: return btnGoal5;
            case 10: return btnGoal10;
            case 20: return btnGoal20;
            default: return btnGoal5;
        }
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(new Date());
    }

    private String getYesterdayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        return sdf.format(cal.getTime());
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
