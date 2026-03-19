package com.medoapps.www.onlinequran;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrayerTimesActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 100;
    private TextView tvFajr, tvSunrise, tvDhuhr, tvAsr, tvMaghrib, tvIsha;
    private TextView tvDate, tvLocation, tvError;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedLocationClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_times);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.prayer_times);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_main));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        tvFajr = findViewById(R.id.tv_fajr_time);
        tvSunrise = findViewById(R.id.tv_sunrise_time);
        tvDhuhr = findViewById(R.id.tv_dhuhr_time);
        tvAsr = findViewById(R.id.tv_asr_time);
        tvMaghrib = findViewById(R.id.tv_maghrib_time);
        tvIsha = findViewById(R.id.tv_isha_time);
        tvDate = findViewById(R.id.tv_date);
        tvLocation = findViewById(R.id.tv_location);
        tvError = findViewById(R.id.tv_error);
        progressBar = findViewById(R.id.progress_bar);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        String today = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        tvDate.setText(today);

        requestLocationAndFetch();
    }

    private void requestLocationAndFetch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE);
            return;
        }
        fetchLocation();
    }

    private void fetchLocation() {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Fallback: use Makkah coordinates
            fetchPrayerTimes(21.4225, 39.8262, "مكة المكرمة");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                fetchPrayerTimes(location.getLatitude(), location.getLongitude(), "");
            } else {
                // Fallback to Makkah
                fetchPrayerTimes(21.4225, 39.8262, "مكة المكرمة");
            }
        }).addOnFailureListener(e -> {
            // Fallback to Makkah
            fetchPrayerTimes(21.4225, 39.8262, "مكة المكرمة");
        });
    }

    private void fetchPrayerTimes(double lat, double lng, String locationName) {
        if (!locationName.isEmpty()) {
            tvLocation.setText(locationName);
        }

        executor.execute(() -> {
            try {
                String dateStr = new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date());
                String apiUrl = "https://api.aladhan.com/v1/timings/" + dateStr
                        + "?latitude=" + lat
                        + "&longitude=" + lng
                        + "&method=4";  // Umm Al-Qura method

                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(sb.toString());
                    JSONObject data = json.getJSONObject("data");
                    JSONObject timings = data.getJSONObject("timings");

                    String fajr = timings.getString("Fajr");
                    String sunrise = timings.getString("Sunrise");
                    String dhuhr = timings.getString("Dhuhr");
                    String asr = timings.getString("Asr");
                    String maghrib = timings.getString("Maghrib");
                    String isha = timings.getString("Isha");

                    // Get readable date
                    JSONObject dateObj = data.getJSONObject("date");
                    JSONObject hijri = dateObj.getJSONObject("hijri");
                    String hijriDate = hijri.getString("day") + " "
                            + hijri.getJSONObject("month").getString("ar") + " "
                            + hijri.getString("year") + " هـ";

                    // Get location from meta
                    JSONObject meta = data.getJSONObject("meta");
                    String timezone = meta.getString("timezone");

                    runOnUiThread(() -> {
                        tvFajr.setText(fajr);
                        tvSunrise.setText(sunrise);
                        tvDhuhr.setText(dhuhr);
                        tvAsr.setText(asr);
                        tvMaghrib.setText(maghrib);
                        tvIsha.setText(isha);
                        tvDate.setText(hijriDate);
                        if (locationName.isEmpty()) {
                            tvLocation.setText(timezone);
                        }
                        progressBar.setVisibility(View.GONE);

                        // Schedule prayer time notifications
                        PrayerAlarmScheduler.schedulePrayerAlarms(
                                PrayerTimesActivity.this,
                                fajr, sunrise, dhuhr, asr, maghrib, isha);

                        // Cache times for widget
                        getSharedPreferences("prayer_times_cache", MODE_PRIVATE).edit()
                            .putString("fajr", fajr)
                            .putString("sunrise", sunrise)
                            .putString("dhuhr", dhuhr)
                            .putString("asr", asr)
                            .putString("maghrib", maghrib)
                            .putString("isha", isha)
                            .apply();

                        // Update widget
                        Intent widgetIntent = new Intent(PrayerTimesActivity.this, PrayerTimesWidget.class);
                        widgetIntent.setAction("com.medoapps.UPDATE_PRAYER_WIDGET");
                        sendBroadcast(widgetIntent);
                    });
                } else {
                    showError();
                }
                conn.disconnect();
            } catch (Exception e) {
                showError();
            }
        });
    }

    private void showError() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("تعذر تحميل مواقيت الصلاة");
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                // Use Makkah as fallback
                fetchPrayerTimes(21.4225, 39.8262, "مكة المكرمة");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
