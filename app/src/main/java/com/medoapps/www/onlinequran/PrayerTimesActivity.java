package com.medoapps.www.onlinequran;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.medoapps.www.onlinequran.athan.AthanScheduler;
import com.medoapps.www.onlinequran.athan.HijriDate;
import com.medoapps.www.onlinequran.athan.LocationApplier;
import com.medoapps.www.onlinequran.athan.PrayerSettings;
import com.medoapps.www.onlinequran.athan.PrayerTimeEngine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Advanced athan home screen. All prayer times are computed on-device by
 * {@link PrayerTimeEngine}; no network calls are involved.
 */
public class PrayerTimesActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int MENU_MONTHLY = 1;
    private static final int MENU_SETTINGS = 2;

    private TextView tvHijriDate, tvGregorianDate, tvCity;
    private TextView tvNextPrayerName, tvNextPrayerTime, tvCountdown;
    private View cardExactAlarm;
    private View cardOverlay;
    private LinearLayout listPrayers;

    private final LinearLayout[] rows = new LinearLayout[PrayerSettings.PRAYER_COUNT];
    private final TextView[] rowTimes = new TextView[PrayerSettings.PRAYER_COUNT];
    private final ImageButton[] rowModeButtons = new ImageButton[PrayerSettings.PRAYER_COUNT];

    private FusedLocationProviderClient fusedLocationClient;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private CountDownTimer countdownTimer;
    private final Runnable rerenderRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isFinishing() && !isDestroyed()) renderAll();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_times);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.prayer_times);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_main));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        tvHijriDate = findViewById(R.id.tv_hijri_date);
        tvGregorianDate = findViewById(R.id.tv_gregorian_date);
        tvCity = findViewById(R.id.tv_city);
        tvNextPrayerName = findViewById(R.id.tv_next_prayer_name);
        tvNextPrayerTime = findViewById(R.id.tv_next_prayer_time);
        tvCountdown = findViewById(R.id.tv_countdown);
        cardExactAlarm = findViewById(R.id.card_exact_alarm);
        cardOverlay = findViewById(R.id.card_overlay);
        listPrayers = findViewById(R.id.list_prayers);

        buildPrayerRows();

        findViewById(R.id.row_city).setOnClickListener(v ->
                startActivity(new Intent(this, AthanSettingsActivity.class)));
        findViewById(R.id.btn_exact_alarm).setOnClickListener(v -> openExactAlarmSettings());
        findViewById(R.id.btn_overlay).setOnClickListener(v -> openOverlaySettings());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        requestStartupPermissions();
        if (PrayerSettings.getLocationMode(this) == PrayerSettings.LOCATION_AUTO
                && hasLocationPermission()) {
            fetchLocation();
        }
    }

    // ------------------------------------------------------------ rendering

    private void renderAll() {
        tvHijriDate.setText(HijriDate.todayString(this));
        tvGregorianDate.setText(
                new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(new Date()));
        renderCity();

        Date[] times = PrayerTimeEngine.getTodayTimes(this);
        int nextIndex = PrayerTimeEngine.getNextPrayerIndex(this);
        Date nextTime = PrayerTimeEngine.getNextPrayerTime(this);

        tvNextPrayerName.setText(PrayerTimeEngine.PRAYER_NAME_RES[nextIndex]);
        tvNextPrayerTime.setText(PrayerTimeEngine.formatTime(this, nextTime));

        int highlight = ContextCompat.getColor(this, R.color.gold_accent_faint);
        for (int i = 0; i < PrayerSettings.PRAYER_COUNT; i++) {
            rowTimes[i].setText(PrayerTimeEngine.formatTime(this, times[i]));
            applyModeIcon(i, PrayerSettings.getNotificationMode(this, i));
            rows[i].setBackgroundColor(i == nextIndex ? highlight : Color.TRANSPARENT);
        }

        startCountdown(nextTime);
    }

    private void renderCity() {
        if (PrayerSettings.hasLocation(this)) {
            String city = PrayerSettings.getCityName(this);
            if (city == null || city.isEmpty()) {
                city = String.format(Locale.getDefault(), "%.2f, %.2f",
                        PrayerSettings.getLatitude(this), PrayerSettings.getLongitude(this));
            }
            tvCity.setText(city);
        } else {
            tvCity.setText(R.string.athan_location_makkah_fallback);
        }
    }

    private void startCountdown(Date nextTime) {
        cancelCountdown();
        long remaining = nextTime.getTime() - System.currentTimeMillis();
        if (remaining <= 0) {
            tvCountdown.setText(R.string.athan_now);
            tvCountdown.postDelayed(rerenderRunnable, 1500);
            return;
        }
        countdownTimer = new CountDownTimer(remaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(getString(R.string.athan_time_remaining,
                        formatRemaining(millisUntilFinished)));
            }

            @Override
            public void onFinish() {
                renderAll();
            }
        };
        countdownTimer.start();
    }

    private void cancelCountdown() {
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }
        if (tvCountdown != null) {
            tvCountdown.removeCallbacks(rerenderRunnable);
        }
    }

    private String formatRemaining(long millis) {
        long totalSeconds = millis / 1000;
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s);
    }

    // ----------------------------------------------------------- prayer rows

    private void buildPrayerRows() {
        TypedValue rippleValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless,
                rippleValue, true);

        for (int i = 0; i < PrayerSettings.PRAYER_COUNT; i++) {
            final int index = i;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setMinimumHeight(dp(56));
            row.setPaddingRelative(dp(16), dp(8), dp(8), dp(8));

            TextView name = new TextView(this);
            name.setText(PrayerTimeEngine.PRAYER_NAME_RES[i]);
            name.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            name.setTypeface(name.getTypeface(), android.graphics.Typeface.BOLD);
            row.addView(name, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView time = new TextView(this);
            time.setText("--:--");
            time.setTextColor(ContextCompat.getColor(this, R.color.gold_accent));
            time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            time.setTypeface(time.getTypeface(), android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            timeParams.setMarginEnd(dp(12));
            row.addView(time, timeParams);

            ImageButton modeButton = new ImageButton(this);
            modeButton.setBackgroundResource(rippleValue.resourceId);
            modeButton.setScaleType(android.widget.ImageView.ScaleType.CENTER);
            modeButton.setContentDescription(getString(PrayerTimeEngine.PRAYER_NAME_RES[i]));
            modeButton.setOnClickListener(v -> cycleMode(index));
            row.addView(modeButton, new LinearLayout.LayoutParams(dp(44), dp(44)));

            listPrayers.addView(row, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            rows[i] = row;
            rowTimes[i] = time;
            rowModeButtons[i] = modeButton;
        }
    }

    private void cycleMode(int prayerIndex) {
        int mode = PrayerSettings.getNotificationMode(this, prayerIndex);
        int max = (prayerIndex == PrayerSettings.PRAYER_SUNRISE)
                ? PrayerSettings.MODE_BEEP : PrayerSettings.MODE_ATHAN;
        int next = (mode >= max) ? PrayerSettings.MODE_OFF : mode + 1;
        PrayerSettings.setNotificationMode(this, prayerIndex, next);
        applyModeIcon(prayerIndex, next);
        AthanScheduler.rescheduleAll(this);
        updateOverlayCard();
    }

    private void applyModeIcon(int prayerIndex, int mode) {
        int iconRes;
        switch (mode) {
            case PrayerSettings.MODE_SILENT:
                iconRes = R.drawable.ic_athan_mode_silent;
                break;
            case PrayerSettings.MODE_BEEP:
                iconRes = R.drawable.ic_athan_mode_beep;
                break;
            case PrayerSettings.MODE_ATHAN:
                iconRes = R.drawable.ic_athan_mode_athan;
                break;
            default:
                iconRes = R.drawable.ic_athan_mode_off;
                break;
        }
        ImageButton button = rowModeButtons[prayerIndex];
        button.setImageResource(iconRes);
        int tintRes = (mode == PrayerSettings.MODE_ATHAN)
                ? R.color.gold_accent : R.color.text_secondary;
        button.setColorFilter(ContextCompat.getColor(this, tintRes));
    }

    // ------------------------------------------------------------- location

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void fetchLocation() {
        if (!hasLocationPermission()) return;

        // Prefer an actual current fix over the often-stale cached one; accept
        // a recent cached fix (≤5 min) immediately, otherwise wait up to 10 s.
        int priority = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                ? Priority.PRIORITY_HIGH_ACCURACY
                : Priority.PRIORITY_BALANCED_POWER_ACCURACY;

        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(priority)
                .setMaxUpdateAgeMillis(5 * 60 * 1000)
                .setDurationMillis(10 * 1000)
                .build();

        fusedLocationClient.getCurrentLocation(request, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        onLocationFix(location);
                    } else {
                        fallbackToLastLocation();
                    }
                })
                .addOnFailureListener(e -> fallbackToLastLocation());
    }

    /** A stale fix is still better than the engine's Makkah fallback. */
    private void fallbackToLastLocation() {
        if (!hasLocationPermission()) return;
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                onLocationFix(location);
            }
        });
    }

    private void onLocationFix(Location location) {
        final double lat = location.getLatitude();
        final double lng = location.getLongitude();
        executor.execute(() -> {
            String city = "";
            String country = "";
            try {
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> result = geocoder.getFromLocation(lat, lng, 1);
                if (result != null && !result.isEmpty()) {
                    Address address = result.get(0);
                    if (address.getCountryCode() != null) {
                        country = address.getCountryCode();
                    }
                    if (address.getLocality() != null) {
                        city = address.getLocality();
                    } else if (address.getSubAdminArea() != null) {
                        city = address.getSubAdminArea();
                    } else if (address.getAdminArea() != null) {
                        city = address.getAdminArea();
                    }
                }
            } catch (Exception ignored) {
                // Reverse geocoding is best-effort only.
            }
            // one funnel for every fix: persists, re-derives the method on a real move,
            // and reschedules
            LocationApplier.apply(getApplicationContext(), lat, lng, city, country);
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) renderAll();
            });
        });
    }

    // ----------------------------------------------------------- permissions

    private void requestStartupPermissions() {
        ArrayList<String> needed = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 33
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (PrayerSettings.getLocationMode(this) == PrayerSettings.LOCATION_AUTO
                && !hasLocationPermission()) {
            needed.add(Manifest.permission.ACCESS_FINE_LOCATION);
            needed.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!needed.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    needed.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSIONS_REQUEST_CODE) return;

        boolean locationGranted = false;
        for (int i = 0; i < permissions.length; i++) {
            boolean granted = i < grantResults.length
                    && grantResults[i] == PackageManager.PERMISSION_GRANTED;
            if (Manifest.permission.POST_NOTIFICATIONS.equals(permissions[i]) && !granted) {
                Toast.makeText(this, R.string.athan_notification_permission_rationale,
                        Toast.LENGTH_LONG).show();
            }
            if ((Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[i])
                    || Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[i]))
                    && granted) {
                locationGranted = true;
            }
        }
        if (locationGranted
                && PrayerSettings.getLocationMode(this) == PrayerSettings.LOCATION_AUTO) {
            fetchLocation();
        }
        // Denied location: the engine silently falls back to Makkah coordinates.
    }

    // ----------------------------------------------------------- exact alarm

    private void updateExactAlarmCard() {
        cardExactAlarm.setVisibility(
                AthanScheduler.canUseExactAlarms(this) ? View.GONE : View.VISIBLE);
    }

    private void openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + getPackageName())));
            } catch (Exception ignored) {
                // Some OEM builds do not expose this settings screen.
            }
        }
    }

    // ------------------------------------------------------ display over apps

    /** Show the prompt only when a full-athan prayer is set but the overlay isn't allowed. */
    private void updateOverlayCard() {
        boolean granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || Settings.canDrawOverlays(this);
        boolean anyAthanMode = false;
        for (int i = 0; i < PrayerSettings.PRAYER_COUNT; i++) {
            if (PrayerSettings.getNotificationMode(this, i) == PrayerSettings.MODE_ATHAN) {
                anyAthanMode = true;
                break;
            }
        }
        cardOverlay.setVisibility(!granted && anyAthanMode ? View.VISIBLE : View.GONE);
    }

    private void openOverlaySettings() {
        try {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
            } catch (Exception ignored) {
            }
        }
    }

    // ------------------------------------------------------------- lifecycle

    @Override
    protected void onResume() {
        super.onResume();
        renderAll();
        AthanScheduler.rescheduleAll(this);
        updateExactAlarmCard();
        updateOverlayCard();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelCountdown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancellationTokenSource.cancel();
        executor.shutdown();
    }

    // ------------------------------------------------------------------ menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_MONTHLY, 0, R.string.athan_monthly_timetable)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, MENU_SETTINGS, 1, R.string.athan_settings_title)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == MENU_MONTHLY) {
            startActivity(new Intent(this, MonthlyPrayerTimesActivity.class));
            return true;
        }
        if (id == MENU_SETTINGS) {
            startActivity(new Intent(this, AthanSettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
