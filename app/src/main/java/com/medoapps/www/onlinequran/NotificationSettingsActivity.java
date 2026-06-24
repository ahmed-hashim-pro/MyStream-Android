package com.medoapps.www.onlinequran;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NotificationSettingsActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 3001;
    private Runnable pendingNotificationAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        HeroController.attach(this)
                .back()
                .centered()
                .title(R.string.settings_section_notifications)
                .apply();

        setupClickListeners();
        refreshTimeLabels();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTimeLabels();
    }

    private void setupClickListeners() {
        findViewById(R.id.item_daily_ayah).setOnClickListener(v ->
                withNotificationPermission(this::showDailyAyahTimePicker));

        findViewById(R.id.item_morning_athkar).setOnClickListener(v ->
                withNotificationPermission(this::showMorningAthkarTimePicker));

        findViewById(R.id.item_evening_athkar).setOnClickListener(v ->
                withNotificationPermission(this::showEveningAthkarTimePicker));

        findViewById(R.id.item_hisn).setOnClickListener(v ->
                withNotificationPermission(this::showHisnTimePicker));

        findViewById(R.id.item_daily_hadith).setOnClickListener(v ->
                withNotificationPermission(this::showDailyHadithTimePicker));

        findViewById(R.id.item_asmaul_husna).setOnClickListener(v ->
                withNotificationPermission(this::showAsmaulHusnaTimePicker));

        findViewById(R.id.item_fasting).setOnClickListener(v ->
                withNotificationPermission(this::showFastingReminderTimePicker));
    }

    private void refreshTimeLabels() {
        // Daily Ayah
        SharedPreferences ayahPrefs = getSharedPreferences("daily_ayah_prefs", Context.MODE_PRIVATE);
        setTimeLabel(R.id.daily_ayah_time, R.id.daily_ayah_dot,
                ayahPrefs.getBoolean("notification_enabled", false),
                ayahPrefs.getInt("notification_hour", 7),
                ayahPrefs.getInt("notification_minute", 0));

        // Morning Athkar
        setTimeLabel(R.id.morning_athkar_time, R.id.morning_athkar_dot,
                AthkarAlarmScheduler.isMorningEnabled(this),
                AthkarAlarmScheduler.getMorningHour(this),
                AthkarAlarmScheduler.getMorningMinute(this));

        // Evening Athkar
        setTimeLabel(R.id.evening_athkar_time, R.id.evening_athkar_dot,
                AthkarAlarmScheduler.isEveningEnabled(this),
                AthkarAlarmScheduler.getEveningHour(this),
                AthkarAlarmScheduler.getEveningMinute(this));

        // Hisn
        setTimeLabel(R.id.hisn_time, R.id.hisn_dot,
                HisnNotificationScheduler.isEnabled(this),
                HisnNotificationScheduler.getHour(this),
                HisnNotificationScheduler.getMinute(this));

        // Daily Hadith
        SharedPreferences hadithPrefs = getSharedPreferences("daily_hadith_prefs", Context.MODE_PRIVATE);
        setTimeLabel(R.id.daily_hadith_time, R.id.daily_hadith_dot,
                hadithPrefs.getBoolean("notification_enabled", false),
                hadithPrefs.getInt("notification_hour", 8),
                hadithPrefs.getInt("notification_minute", 0));

        // Asmaul Husna
        setTimeLabel(R.id.asmaul_husna_time, R.id.asmaul_husna_dot,
                AsmaulHusnaScheduler.isEnabled(this),
                AsmaulHusnaScheduler.getHour(this),
                AsmaulHusnaScheduler.getMinute(this));

        // Fasting
        setTimeLabel(R.id.fasting_time, R.id.fasting_dot,
                FastingReminderScheduler.isEnabled(this),
                FastingReminderScheduler.getHour(this),
                FastingReminderScheduler.getMinute(this));
    }

    private void setTimeLabel(int textViewId, int dotViewId, boolean enabled, int hour, int minute) {
        TextView tv = findViewById(textViewId);
        View dot = findViewById(dotViewId);
        if (enabled) {
            tv.setText(String.format(getString(R.string.notif_time_format), hour, minute));
            tv.setTextColor(ContextCompat.getColor(this, R.color.gold_accent));
            dot.setBackgroundResource(R.drawable.status_dot_active);
        } else {
            tv.setText(getString(R.string.notif_not_set));
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            dot.setBackgroundResource(R.drawable.status_dot_inactive);
        }
        tv.setVisibility(View.VISIBLE);
    }

    // Time picker methods

    private void showDailyAyahTimePicker() {
        SharedPreferences prefs = getSharedPreferences("daily_ayah_prefs", Context.MODE_PRIVATE);
        int hour = prefs.getInt("notification_hour", 7);
        int minute = prefs.getInt("notification_minute", 0);
        new TimePickerDialog(this, (view, h, m) -> {
            DailyAyahScheduler.scheduleDailyAyah(this, h, m);
            prefs.edit()
                    .putInt("notification_hour", h)
                    .putInt("notification_minute", m)
                    .putBoolean("notification_enabled", true)
                    .apply();
            Toast.makeText(this,
                    String.format(getString(R.string.daily_ayah_time_set), h, m),
                    Toast.LENGTH_SHORT).show();
            refreshTimeLabels();
        }, hour, minute, true).show();
    }

    private void showMorningAthkarTimePicker() {
        int hour = AthkarAlarmScheduler.getMorningHour(this);
        int minute = AthkarAlarmScheduler.getMorningMinute(this);
        new TimePickerDialog(this, (view, h, m) -> {
            AthkarAlarmScheduler.scheduleMorning(this, h, m);
            Toast.makeText(this,
                    String.format(getString(R.string.athkar_notification_set), h, m),
                    Toast.LENGTH_SHORT).show();
            refreshTimeLabels();
        }, hour, minute, true).show();
    }

    private void showEveningAthkarTimePicker() {
        int hour = AthkarAlarmScheduler.getEveningHour(this);
        int minute = AthkarAlarmScheduler.getEveningMinute(this);
        new TimePickerDialog(this, (view, h, m) -> {
            AthkarAlarmScheduler.scheduleEvening(this, h, m);
            Toast.makeText(this,
                    String.format(getString(R.string.athkar_notification_set), h, m),
                    Toast.LENGTH_SHORT).show();
            refreshTimeLabels();
        }, hour, minute, true).show();
    }

    private void showHisnTimePicker() {
        int hour = HisnNotificationScheduler.getHour(this);
        int minute = HisnNotificationScheduler.getMinute(this);
        new TimePickerDialog(this, (view, h, m) -> {
            HisnNotificationScheduler.schedule(this, h, m);
            Toast.makeText(this,
                    String.format(getString(R.string.hisn_notification_set), h, m),
                    Toast.LENGTH_SHORT).show();
            refreshTimeLabels();
        }, hour, minute, true).show();
    }

    private void showDailyHadithTimePicker() {
        SharedPreferences prefs = getSharedPreferences("daily_hadith_prefs", Context.MODE_PRIVATE);
        int hour = prefs.getInt("notification_hour", 8);
        int minute = prefs.getInt("notification_minute", 0);
        new TimePickerDialog(this, (view, h, m) -> {
            DailyHadithScheduler.scheduleDailyHadith(this, h, m);
            prefs.edit()
                    .putInt("notification_hour", h)
                    .putInt("notification_minute", m)
                    .putBoolean("notification_enabled", true)
                    .apply();
            Toast.makeText(this,
                    String.format(getString(R.string.daily_hadith_time_set), h, m),
                    Toast.LENGTH_SHORT).show();
            refreshTimeLabels();
        }, hour, minute, true).show();
    }

    private void showAsmaulHusnaTimePicker() {
        int hour = AsmaulHusnaScheduler.getHour(this);
        int minute = AsmaulHusnaScheduler.getMinute(this);
        new TimePickerDialog(this, (view, h, m) -> {
            AsmaulHusnaScheduler.schedule(this, h, m);
            Toast.makeText(this,
                    String.format(getString(R.string.asmaul_husna_notification_set), h, m),
                    Toast.LENGTH_SHORT).show();
            refreshTimeLabels();
        }, hour, minute, true).show();
    }

    private void showFastingReminderTimePicker() {
        int hour = FastingReminderScheduler.getHour(this);
        int minute = FastingReminderScheduler.getMinute(this);
        new TimePickerDialog(this, (view, h, m) -> {
            FastingReminderScheduler.schedule(this, h, m);
            Toast.makeText(this,
                    String.format(getString(R.string.fasting_reminder_set), h, m),
                    Toast.LENGTH_SHORT).show();
            refreshTimeLabels();
        }, hour, minute, true).show();
    }

    // Permission handling

    private void withNotificationPermission(Runnable action) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            pendingNotificationAction = action;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION);
        } else {
            action.run();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingNotificationAction != null) {
                    pendingNotificationAction.run();
                    pendingNotificationAction = null;
                }
            } else {
                Toast.makeText(this, R.string.notification_permission_denied, Toast.LENGTH_SHORT).show();
                pendingNotificationAction = null;
            }
        }
    }
}
