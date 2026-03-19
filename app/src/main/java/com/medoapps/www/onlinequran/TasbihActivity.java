package com.medoapps.www.onlinequran;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

public class TasbihActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "tasbih_prefs";
    private static final String KEY_TOTAL = "tasbih_total";

    private int count = 0;
    private int target = 33;
    private long totalCount = 0;

    private TextView tvCounter;
    private TextView tvTargetDisplay;
    private TextView tvSelectedDhikr;
    private TextView tvTotalCount;
    private FrameLayout flCounterCircle;

    private Vibrator vibrator;
    private SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Dhikr chip views
    private MaterialCardView chipSubhanallah;
    private MaterialCardView chipAlhamdulillah;
    private MaterialCardView chipAllahuakbar;
    private MaterialCardView chipLailaha;
    private MaterialCardView chipAstaghfirullah;
    private MaterialCardView chipLahawla;
    private MaterialCardView selectedChip;

    // Target button views
    private MaterialCardView btnTarget33;
    private MaterialCardView btnTarget100;
    private MaterialCardView btnTarget1000;
    private MaterialCardView btnTargetUnlimited;
    private MaterialCardView selectedTargetBtn;

    private final String[] dhikrTexts = {
            "\u0633\u0628\u062d\u0627\u0646 \u0627\u0644\u0644\u0647",        // سبحان الله
            "\u0627\u0644\u062d\u0645\u062f \u0644\u0644\u0647",              // الحمد لله
            "\u0627\u0644\u0644\u0647 \u0623\u0643\u0628\u0631",              // الله أكبر
            "\u0644\u0627 \u0625\u0644\u0647 \u0625\u0644\u0627 \u0627\u0644\u0644\u0647", // لا إله إلا الله
            "\u0623\u0633\u062a\u063a\u0641\u0631 \u0627\u0644\u0644\u0647",  // أستغفر الله
            "\u0644\u0627 \u062d\u0648\u0644 \u0648\u0644\u0627 \u0642\u0648\u0629 \u0625\u0644\u0627 \u0628\u0627\u0644\u0644\u0647" // لا حول ولا قوة إلا بالله
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasbih);

        // ActionBar setup
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.tasbih_counter);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Status bar and nav bar colors
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_main));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        // Init vibrator and prefs
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        totalCount = prefs.getLong(KEY_TOTAL, 0);

        // Bind views
        tvCounter = findViewById(R.id.tv_counter);
        tvTargetDisplay = findViewById(R.id.tv_target_display);
        tvSelectedDhikr = findViewById(R.id.tv_selected_dhikr);
        tvTotalCount = findViewById(R.id.tv_total_count);
        flCounterCircle = findViewById(R.id.fl_counter_circle);

        chipSubhanallah = findViewById(R.id.chip_subhanallah);
        chipAlhamdulillah = findViewById(R.id.chip_alhamdulillah);
        chipAllahuakbar = findViewById(R.id.chip_allahuakbar);
        chipLailaha = findViewById(R.id.chip_lailaha);
        chipAstaghfirullah = findViewById(R.id.chip_astaghfirullah);
        chipLahawla = findViewById(R.id.chip_lahawla);

        btnTarget33 = findViewById(R.id.btn_target_33);
        btnTarget100 = findViewById(R.id.btn_target_100);
        btnTarget1000 = findViewById(R.id.btn_target_1000);
        btnTargetUnlimited = findViewById(R.id.btn_target_unlimited);

        MaterialCardView btnReset = findViewById(R.id.btn_reset);

        // Initial state
        selectedChip = chipSubhanallah;
        selectedTargetBtn = btnTarget33;
        updateTargetDisplay();
        updateTotalDisplay();

        // Counter circle tap
        flCounterCircle.setOnClickListener(v -> incrementCounter());

        // Dhikr chip click listeners
        chipSubhanallah.setOnClickListener(v -> selectDhikr(chipSubhanallah, 0));
        chipAlhamdulillah.setOnClickListener(v -> selectDhikr(chipAlhamdulillah, 1));
        chipAllahuakbar.setOnClickListener(v -> selectDhikr(chipAllahuakbar, 2));
        chipLailaha.setOnClickListener(v -> selectDhikr(chipLailaha, 3));
        chipAstaghfirullah.setOnClickListener(v -> selectDhikr(chipAstaghfirullah, 4));
        chipLahawla.setOnClickListener(v -> selectDhikr(chipLahawla, 5));

        // Target button click listeners
        btnTarget33.setOnClickListener(v -> selectTarget(btnTarget33, 33));
        btnTarget100.setOnClickListener(v -> selectTarget(btnTarget100, 100));
        btnTarget1000.setOnClickListener(v -> selectTarget(btnTarget1000, 1000));
        btnTargetUnlimited.setOnClickListener(v -> selectTarget(btnTargetUnlimited, 0));

        // Reset button
        btnReset.setOnClickListener(v -> resetCounter());
    }

    private void incrementCounter() {
        count++;
        totalCount++;
        tvCounter.setText(String.valueOf(count));
        updateTargetDisplay();
        updateTotalDisplay();
        saveTotalCount();
        vibrateShort();

        // Check if target reached (target > 0 means a finite target)
        if (target > 0 && count == target) {
            showTargetReachedFeedback();
        }
    }

    private void vibrateShort() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(20);
        }
    }

    private void showTargetReachedFeedback() {
        // Temporarily change circle stroke color to a highlight
        if (flCounterCircle.getBackground() instanceof GradientDrawable) {
            GradientDrawable bg = (GradientDrawable) flCounterCircle.getBackground();
            int highlightColor = ContextCompat.getColor(this, R.color.gold_accent_semi);
            int goldColor = ContextCompat.getColor(this, R.color.gold_accent);

            bg.setStroke(6, highlightColor);
            bg.setColor(ContextCompat.getColor(this, R.color.gold_accent_faint));

            handler.postDelayed(() -> {
                bg.setStroke(3, goldColor);
                bg.setColor(ContextCompat.getColor(this, R.color.background_card));
            }, 500);
        }
    }

    private void selectDhikr(MaterialCardView chip, int index) {
        // Deselect previous
        if (selectedChip != null) {
            selectedChip.setStrokeColor(ContextCompat.getColorStateList(this, R.color.gold_accent_faint));
        }
        // Select new
        selectedChip = chip;
        selectedChip.setStrokeColor(ContextCompat.getColorStateList(this, R.color.gold_accent));
        tvSelectedDhikr.setText(dhikrTexts[index]);
    }

    private void selectTarget(MaterialCardView btn, int newTarget) {
        // Deselect previous
        if (selectedTargetBtn != null) {
            selectedTargetBtn.setStrokeColor(ContextCompat.getColorStateList(this, R.color.gold_accent_faint));
        }
        // Select new
        selectedTargetBtn = btn;
        selectedTargetBtn.setStrokeColor(ContextCompat.getColorStateList(this, R.color.gold_accent));
        target = newTarget;
        updateTargetDisplay();
    }

    private void updateTargetDisplay() {
        if (target == 0) {
            tvTargetDisplay.setText(String.valueOf(count));
        } else {
            tvTargetDisplay.setText(count + " / " + target);
        }
    }

    private void updateTotalDisplay() {
        tvTotalCount.setText("\u0627\u0644\u0625\u062c\u0645\u0627\u0644\u064a: " + totalCount); // الإجمالي:
    }

    private void resetCounter() {
        count = 0;
        tvCounter.setText("0");
        updateTargetDisplay();
    }

    private void saveTotalCount() {
        prefs.edit().putLong(KEY_TOTAL, totalCount).apply();
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
