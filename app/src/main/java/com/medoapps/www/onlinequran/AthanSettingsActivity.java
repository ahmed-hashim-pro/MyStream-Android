package com.medoapps.www.onlinequran;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.medoapps.www.onlinequran.athan.AthanScheduler;
import com.medoapps.www.onlinequran.athan.AthanSound;
import com.medoapps.www.onlinequran.athan.PrayerSettings;
import com.medoapps.www.onlinequran.athan.PrayerTimeEngine;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Settings screen for the athan feature: calculation method, manual minute
 * corrections, athan sound and reminders, location, and hijri calendar offset.
 * Every change immediately reschedules all athan alarms.
 */
public class AthanSettingsActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 7002;
    private static final int[] PRE_REMINDER_VALUES = {0, 5, 10, 15, 20, 30};
    private static final int[] IQAMA_VALUES = {0, 10, 15, 20, 25, 30};

    private TextView tvCurrentCity, btnUseCurrentLocation;
    private TextView tvSoundAthan, tvSoundFajr, tvSoundIqama;
    private EditText editCity;
    private LinearLayout manualLocationContainer, dependentContainer;
    private SwitchCompat switchAthanFeature;
    private FusedLocationProviderClient fusedLocationClient;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /** Spinner selection callback used by {@link #listen}. */
    private interface OnPick {
        void pick(int position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_athan_settings);

        Toolbar toolbar = findViewById(R.id.athan_settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.athan_settings_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_main));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupMasterSwitch();
        setupCalculationSection();
        buildCorrectionRows();
        setupNotificationsSection();
        setupLocationSection();
        setupCalendarSection();

        applyFeatureEnabled(PrayerSettings.isAthanFeatureEnabled(this));
    }

    // ----------------------------------------------------- master switch

    private void setupMasterSwitch() {
        dependentContainer = findViewById(R.id.athan_dependent_container);
        switchAthanFeature = findViewById(R.id.switch_athan_feature);
        switchAthanFeature.setChecked(PrayerSettings.isAthanFeatureEnabled(this));
        switchAthanFeature.setOnCheckedChangeListener((button, checked) -> {
            PrayerSettings.setAthanFeatureEnabled(this, checked);
            AthanScheduler.rescheduleAll(this);
            applyFeatureEnabled(checked);
        });
    }

    /** Greys out and disables every control below the master switch when off. */
    private void applyFeatureEnabled(boolean enabled) {
        dependentContainer.setAlpha(enabled ? 1f : 0.4f);
        setEnabledRecursive(dependentContainer, enabled);
    }

    private void setEnabledRecursive(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                setEnabledRecursive(group.getChildAt(i), enabled);
            }
        }
    }

    // ------------------------------------------------------- A. calculation

    private void setupCalculationSection() {
        bindEnumSpinner(findViewById(R.id.spinner_method),
                R.array.athan_method_names, R.array.athan_method_values, true);
        bindEnumSpinner(findViewById(R.id.spinner_high_latitude),
                R.array.athan_high_latitude_names, R.array.athan_high_latitude_values, false);

        RadioGroup madhabGroup = findViewById(R.id.radio_madhab);
        madhabGroup.check("HANAFI".equals(PrayerSettings.getMadhab(this))
                ? R.id.radio_madhab_hanafi : R.id.radio_madhab_shafi);
        madhabGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String value = checkedId == R.id.radio_madhab_hanafi ? "HANAFI" : "SHAFI";
            if (!value.equals(PrayerSettings.getMadhab(this))) {
                PrayerSettings.setMadhab(this, value);
                AthanScheduler.rescheduleAll(this);
            }
        });
    }

    /** Spinner bound to the calculation-method or high-latitude enum setting. */
    private void bindEnumSpinner(Spinner spinner, int namesRes, int valuesRes, final boolean method) {
        final String[] values = getResources().getStringArray(valuesRes);
        spinner.setAdapter(makeAdapter(getResources().getStringArray(namesRes)));
        spinner.setSelection(Math.max(0, indexOf(values, method
                ? PrayerSettings.getCalculationMethod(this) : PrayerSettings.getHighLatitudeRule(this))));
        listen(spinner, position -> {
            String saved = method ? PrayerSettings.getCalculationMethod(this)
                    : PrayerSettings.getHighLatitudeRule(this);
            if (values[position].equals(saved)) return;
            if (method) PrayerSettings.setCalculationMethod(this, values[position]);
            else PrayerSettings.setHighLatitudeRule(this, values[position]);
            AthanScheduler.rescheduleAll(this);
        });
    }

    // ------------------------------------------------------- B. corrections

    private void buildCorrectionRows() {
        LinearLayout container = findViewById(R.id.corrections_container);
        for (int i = 0; i < PrayerSettings.PRAYER_COUNT; i++) {
            final int prayerIndex = i;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setMinimumHeight(dp(48));
            row.setPaddingRelative(dp(16), 0, dp(8), 0);

            TextView name = new TextView(this);
            name.setText(PrayerTimeEngine.PRAYER_NAME_RES[i]);
            name.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            name.setTextSize(15);
            row.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView minus = makeStepButton("−");
            final TextView value = new TextView(this);
            value.setTextColor(ContextCompat.getColor(this, R.color.gold_accent));
            value.setTextSize(14);
            value.setTypeface(value.getTypeface(), Typeface.BOLD);
            value.setGravity(Gravity.CENTER);
            value.setMinWidth(dp(48));
            value.setText(formatSigned(PrayerSettings.getCorrection(this, i)));
            TextView plus = makeStepButton("+");

            minus.setOnClickListener(v -> adjustCorrection(prayerIndex, -1, value));
            plus.setOnClickListener(v -> adjustCorrection(prayerIndex, 1, value));

            row.addView(minus);
            row.addView(value);
            row.addView(plus);
            container.addView(row, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void adjustCorrection(int prayerIndex, int delta, TextView label) {
        int value = Math.max(-59, Math.min(59, PrayerSettings.getCorrection(this, prayerIndex) + delta));
        if (value == PrayerSettings.getCorrection(this, prayerIndex)) return;
        PrayerSettings.setCorrection(this, prayerIndex, value);
        label.setText(formatSigned(value));
        AthanScheduler.rescheduleAll(this);
    }

    private static String formatSigned(int value) {
        return value > 0 ? "+" + value : String.valueOf(value);
    }

    private TextView makeStepButton(String glyph) {
        TextView button = new TextView(this);
        button.setText(glyph);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(20);
        button.setTypeface(button.getTypeface(), Typeface.BOLD);
        button.setTextColor(ContextCompat.getColor(this, R.color.gold_accent));
        TypedValue out = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, out, true);
        button.setBackgroundResource(out.resourceId);
        button.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(44)));
        return button;
    }

    // ----------------------------------------------- C. athan & notifications

    private void setupNotificationsSection() {
        tvSoundAthan = findViewById(R.id.tv_sound_athan);
        tvSoundFajr = findViewById(R.id.tv_sound_fajr);
        tvSoundIqama = findViewById(R.id.tv_sound_iqama);
        findViewById(R.id.row_sound_athan).setOnClickListener(v -> openSoundPicker(AthanSound.SLOT_ATHAN));
        findViewById(R.id.row_sound_fajr).setOnClickListener(v -> openSoundPicker(AthanSound.SLOT_FAJR));
        findViewById(R.id.row_sound_iqama).setOnClickListener(v -> openSoundPicker(AthanSound.SLOT_IQAMA));
        findViewById(R.id.btn_test_athan).setOnClickListener(v -> testAthan());
        refreshSoundLabels();
        bindSwitch(R.id.switch_vibrate, true);
        bindSwitch(R.id.switch_dua, false);
        bindMinutesSpinner(findViewById(R.id.spinner_pre_reminder), PRE_REMINDER_VALUES, true);
        bindMinutesSpinner(findViewById(R.id.spinner_iqama), IQAMA_VALUES, false);
    }

    /**
     * Fires the athan immediately (no waiting for a prayer time): starts the
     * playback service with the current athan sound and opens the full-screen
     * athan screen, so the whole flow can be tested on demand.
     */
    private void testAthan() {
        int index = PrayerTimeEngine.getNextPrayerIndex(this);
        long now = System.currentTimeMillis();
        // The service creates its notification channels itself on start.
        Intent svc = new Intent(this, com.medoapps.www.onlinequran.athan.AthanPlaybackService.class)
                .putExtra(com.medoapps.www.onlinequran.athan.AthanScheduler.EXTRA_PRAYER_INDEX, index)
                .putExtra(com.medoapps.www.onlinequran.athan.AthanScheduler.EXTRA_PRAYER_TIME, now)
                .putExtra(com.medoapps.www.onlinequran.athan.AthanPlaybackService.EXTRA_KIND,
                        com.medoapps.www.onlinequran.athan.AthanPlaybackService.KIND_ATHAN);
        ContextCompat.startForegroundService(this, svc);
        startActivity(new Intent(this, AthanAlarmActivity.class)
                .putExtra(AthanAlarmActivity.EXTRA_PRAYER_INDEX, index)
                .putExtra(AthanAlarmActivity.EXTRA_KIND,
                        com.medoapps.www.onlinequran.athan.AthanPlaybackService.KIND_ATHAN)
                .putExtra(AthanAlarmActivity.EXTRA_PRAYER_TIME, now));
    }

    /** Updates each sound row's subtitle to the currently selected sound name. */
    private void refreshSoundLabels() {
        tvSoundAthan.setText(soundNameForSlot(AthanSound.SLOT_ATHAN));
        tvSoundFajr.setText(soundNameForSlot(AthanSound.SLOT_FAJR));
        tvSoundIqama.setText(soundNameForSlot(AthanSound.SLOT_IQAMA));
    }

    private String soundNameForSlot(String slot) {
        AthanSound sound = AthanSound.byId(
                AthanSound.catalogForSlot(this, slot), PrayerSettings.getSoundId(this, slot));
        return sound == null ? "" : sound.displayName;
    }

    /** Opens the in-app sound picker for the given slot; selection persists there. */
    private void openSoundPicker(String slot) {
        startActivity(new Intent(this, AthanSoundPickerActivity.class).putExtra("slot", slot));
    }

    private void bindSwitch(int viewId, final boolean vibrate) {
        SwitchCompat toggle = findViewById(viewId);
        toggle.setChecked(vibrate ? PrayerSettings.isVibrateEnabled(this)
                : PrayerSettings.isDuaAfterAthanEnabled(this));
        toggle.setOnCheckedChangeListener((button, checked) -> {
            if (vibrate) PrayerSettings.setVibrateEnabled(this, checked);
            else PrayerSettings.setDuaAfterAthanEnabled(this, checked);
            AthanScheduler.rescheduleAll(this);
        });
    }

    /** Off/N-minute spinner bound to the pre-reminder or iqama setting. */
    private void bindMinutesSpinner(Spinner spinner, final int[] values, final boolean preReminder) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i] == 0 ? getString(R.string.athan_off_option)
                    : getString(R.string.athan_minutes_format, values[i]);
        }
        spinner.setAdapter(makeAdapter(labels));
        spinner.setSelection(Math.max(0, indexOf(values, preReminder
                ? PrayerSettings.getPreReminderMinutes(this) : PrayerSettings.getIqamaReminderMinutes(this))));
        listen(spinner, position -> {
            int saved = preReminder ? PrayerSettings.getPreReminderMinutes(this)
                    : PrayerSettings.getIqamaReminderMinutes(this);
            if (values[position] == saved) return;
            if (preReminder) PrayerSettings.setPreReminderMinutes(this, values[position]);
            else PrayerSettings.setIqamaReminderMinutes(this, values[position]);
            AthanScheduler.rescheduleAll(this);
        });
    }

    // ---------------------------------------------------------- D. location

    private void setupLocationSection() {
        tvCurrentCity = findViewById(R.id.tv_current_city);
        editCity = findViewById(R.id.edit_city);
        manualLocationContainer = findViewById(R.id.manual_location_container);
        btnUseCurrentLocation = findViewById(R.id.btn_use_current_location);

        RadioGroup group = findViewById(R.id.radio_location_mode);
        group.check(PrayerSettings.getLocationMode(this) == PrayerSettings.LOCATION_MANUAL
                ? R.id.radio_location_manual : R.id.radio_location_auto);
        updateLocationModeViews();
        group.setOnCheckedChangeListener((g, checkedId) -> {
            int mode = checkedId == R.id.radio_location_manual
                    ? PrayerSettings.LOCATION_MANUAL : PrayerSettings.LOCATION_AUTO;
            if (mode != PrayerSettings.getLocationMode(this)) {
                PrayerSettings.setLocationMode(this, mode);
                AthanScheduler.rescheduleAll(this);
            }
            updateLocationModeViews();
        });

        btnUseCurrentLocation.setOnClickListener(v -> requestCurrentLocation());
        findViewById(R.id.btn_search_city).setOnClickListener(v -> searchCity());
        editCity.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return false;
            searchCity();
            return true;
        });
        updateCityLabel();
    }

    private void updateLocationModeViews() {
        boolean manual = PrayerSettings.getLocationMode(this) == PrayerSettings.LOCATION_MANUAL;
        manualLocationContainer.setVisibility(manual ? View.VISIBLE : View.GONE);
        btnUseCurrentLocation.setVisibility(manual ? View.GONE : View.VISIBLE);
    }

    private void updateCityLabel() {
        String city = PrayerSettings.getCityName(this);
        if (city.isEmpty()) {
            city = String.format(Locale.US, "%.3f, %.3f",
                    PrayerSettings.getLatitude(this), PrayerSettings.getLongitude(this));
        }
        tvCurrentCity.setText(city);
    }

    private void applyLocation(double lat, double lng, String city) {
        PrayerSettings.setLocation(this, lat, lng, city);
        AthanScheduler.rescheduleAll(this);
        updateCityLabel();
        Toast.makeText(this, R.string.athan_location_updated, Toast.LENGTH_SHORT).show();
    }

    /** A single geocoding candidate the user can pick from. */
    private static final class CityResult {
        final double lat, lng;
        final String shortName;  // stored as the city label
        final String fullLabel;  // shown in the disambiguation list

        CityResult(double lat, double lng, String shortName, String fullLabel) {
            this.lat = lat;
            this.lng = lng;
            this.shortName = shortName;
            this.fullLabel = fullLabel;
        }
    }

    private void searchCity() {
        final String query = editCity.getText().toString().trim();
        if (query.isEmpty()) return;
        Toast.makeText(this, R.string.athan_searching, Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            // Prefer the device geocoder, but it is missing or empty on many
            // devices/emulators; fall back to a network geocoder so a valid
            // city is still found. Both return several candidates so the user
            // can disambiguate same-named places.
            List<CityResult> candidates = geocodeWithDevice(query);
            if (candidates.isEmpty()) {
                candidates = geocodeWithNominatim(query);
            }
            final List<CityResult> found = candidates;
            runOnUiThread(() -> {
                if (isFinishing()) return;
                if (found.isEmpty()) {
                    Toast.makeText(this, R.string.athan_city_not_found, Toast.LENGTH_LONG).show();
                } else {
                    // Always let the user confirm the match, even a single one.
                    showCityChooser(found);
                }
            });
        });
    }

    /** Device geocoder (Google/OEM backend). Returns up to 5 candidates. */
    private List<CityResult> geocodeWithDevice(String query) {
        List<CityResult> out = new ArrayList<>();
        if (!Geocoder.isPresent()) return out;
        try {
            List<Address> results =
                    new Geocoder(this, Locale.getDefault()).getFromLocationName(query, 5);
            if (results != null) {
                for (Address a : results) {
                    out.add(new CityResult(a.getLatitude(), a.getLongitude(),
                            shortNameOf(a, query), fullLabelOf(a)));
                }
            }
        } catch (IOException ignored) {
            // Backend unreachable — caller falls back to the network geocoder.
        }
        return out;
    }

    private static String shortNameOf(Address a, String fallback) {
        String name = a.getLocality();
        if (name == null || name.isEmpty()) name = a.getSubAdminArea();
        if (name == null || name.isEmpty()) name = a.getAdminArea();
        if (name == null || name.isEmpty()) name = a.getFeatureName();
        return (name == null || name.isEmpty()) ? fallback : name;
    }

    private static String fullLabelOf(Address a) {
        StringBuilder sb = new StringBuilder();
        if (a.getMaxAddressLineIndex() >= 0) {
            sb.append(a.getAddressLine(0));
        } else {
            appendPart(sb, a.getLocality());
            appendPart(sb, a.getAdminArea());
            appendPart(sb, a.getCountryName());
        }
        return sb.length() == 0 ? a.getFeatureName() : sb.toString();
    }

    private static void appendPart(StringBuilder sb, String part) {
        if (part == null || part.isEmpty()) return;
        if (sb.length() > 0) sb.append(", ");
        sb.append(part);
    }

    /**
     * OpenStreetMap Nominatim geocoder — a network fallback that works even
     * when the device has no geocoder backend. Returns up to 5 candidates.
     */
    private List<CityResult> geocodeWithNominatim(String query) {
        List<CityResult> out = new ArrayList<>();
        HttpURLConnection conn = null;
        try {
            String url = "https://nominatim.openstreetmap.org/search?format=jsonv2"
                    + "&accept-language=" + Locale.getDefault().getLanguage()
                    + "&limit=5&q=" + URLEncoder.encode(query, "UTF-8");
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            // Nominatim's usage policy requires an identifying User-Agent.
            conn.setRequestProperty("User-Agent", "MyStream-Athan/1.0 (com.medoapps.www.onlinequran)");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            if (conn.getResponseCode() != 200) return out;

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
            }

            JSONArray arr = new JSONArray(body.toString());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                double lat = Double.parseDouble(o.getString("lat"));
                double lng = Double.parseDouble(o.getString("lon"));
                String display = o.optString("display_name", "");
                String shortName = o.optString("name", "");
                if (shortName.isEmpty()) {
                    shortName = display.contains(",") ? display.substring(0, display.indexOf(',')) : display;
                }
                if (shortName.isEmpty()) shortName = query;
                out.add(new CityResult(lat, lng, shortName,
                        display.isEmpty() ? shortName : display));
            }
        } catch (Exception ignored) {
            // Network/parse failure — caller shows "city not found".
        } finally {
            if (conn != null) conn.disconnect();
        }
        return out;
    }

    private void showCityChooser(List<CityResult> candidates) {
        View content = getLayoutInflater().inflate(R.layout.dialog_city_chooser, null);
        LinearLayout list = content.findViewById(R.id.dialog_city_list);

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setView(content)
                        .create();
        if (dialog.getWindow() != null) {
            // Let the layout's rounded background be the visible surface.
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        for (int i = 0; i < candidates.size(); i++) {
            CityResult c = candidates.get(i);
            list.addView(buildCityRow(c, dialog));
            if (i < candidates.size() - 1) list.addView(buildRowDivider());
        }

        content.findViewById(R.id.dialog_city_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /** A styled candidate row: pin badge + city name + region/country. */
    private View buildCityRow(CityResult c, androidx.appcompat.app.AlertDialog dialog) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setClickable(true);
        row.setFocusable(true);
        row.setPadding(dp(24), dp(12), dp(24), dp(12));
        TypedValue ripple = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, ripple, true);
        row.setBackgroundResource(ripple.resourceId);
        row.setOnClickListener(v -> {
            applyLocation(c.lat, c.lng, c.shortName);
            dialog.dismiss();
        });

        ImageView pin = new ImageView(this);
        int badge = dp(40);
        LinearLayout.LayoutParams pinLp = new LinearLayout.LayoutParams(badge, badge);
        pinLp.setMarginEnd(dp(16));
        pin.setLayoutParams(pinLp);
        pin.setBackgroundResource(R.drawable.bg_city_icon_circle);
        pin.setImageResource(R.drawable.ic_pref_marker);
        pin.setColorFilter(ContextCompat.getColor(this, R.color.gold_accent));
        int pad = dp(8);
        pin.setPadding(pad, pad, pad, pad);
        row.addView(pin);

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = new TextView(this);
        name.setText(c.shortName);
        name.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        name.setTypeface(name.getTypeface(), Typeface.BOLD);
        text.addView(name);

        TextView sub = new TextView(this);
        sub.setText(c.fullLabel);
        sub.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        sub.setMaxLines(2);
        sub.setEllipsize(android.text.TextUtils.TruncateAt.END);
        text.addView(sub);

        row.addView(text);
        return row;
    }

    private View buildRowDivider() {
        View divider = new View(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        lp.setMarginStart(dp(80));
        lp.setMarginEnd(dp(24));
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.gold_accent_faint));
        return divider;
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCurrentLocation() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE);
            return;
        }
        fetchCurrentLocation();
    }

    /** Same current-fix request pattern as PrayerTimesActivity.fetchLocation(). */
    private void fetchCurrentLocation() {
        if (!hasLocationPermission()) return;
        int priority = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ? Priority.PRIORITY_HIGH_ACCURACY
                : Priority.PRIORITY_BALANCED_POWER_ACCURACY;
        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(priority)
                .setMaxUpdateAgeMillis(5 * 60 * 1000)
                .setDurationMillis(10 * 1000)
                .build();
        fusedLocationClient.getCurrentLocation(request, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) reverseGeocodeAndSave(location.getLatitude(), location.getLongitude());
                    else Toast.makeText(this, R.string.athan_city_not_found, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, R.string.athan_city_not_found, Toast.LENGTH_SHORT).show());
    }

    /** Best-effort locality lookup; falls back to an empty city name. */
    private void reverseGeocodeAndSave(final double lat, final double lng) {
        executor.execute(() -> {
            String city = "";
            try {
                List<Address> results = new Geocoder(this, Locale.getDefault()).getFromLocation(lat, lng, 1);
                if (results != null && !results.isEmpty() && results.get(0).getLocality() != null) {
                    city = results.get(0).getLocality();
                }
            } catch (Exception ignored) {
            }
            final String cityName = city;
            runOnUiThread(() -> {
                if (!isFinishing()) applyLocation(lat, lng, cityName);
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE
                && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        }
    }

    // ---------------------------------------------------------- E. calendar

    private void setupCalendarSection() {
        final TextView tvOffset = findViewById(R.id.tv_hijri_offset);
        TextView minus = findViewById(R.id.btn_hijri_minus);
        TextView plus = findViewById(R.id.btn_hijri_plus);
        minus.setText("−");
        plus.setText("+");
        tvOffset.setText(getString(R.string.athan_days_format, PrayerSettings.getHijriOffset(this)));
        minus.setOnClickListener(v -> adjustHijriOffset(-1, tvOffset));
        plus.setOnClickListener(v -> adjustHijriOffset(1, tvOffset));
    }

    private void adjustHijriOffset(int delta, TextView label) {
        int value = Math.max(-2, Math.min(2, PrayerSettings.getHijriOffset(this) + delta));
        if (value == PrayerSettings.getHijriOffset(this)) return;
        PrayerSettings.setHijriOffset(this, value);
        label.setText(getString(R.string.athan_days_format, value));
        AthanScheduler.rescheduleAll(this);
    }

    // ------------------------------------------------------------- plumbing

    private void listen(Spinner spinner, final OnPick callback) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                callback.pick(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private ArrayAdapter<String> makeAdapter(String[] names) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private static int indexOf(String[] values, String value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) return i;
        }
        return -1;
    }

    private static int indexOf(int[] values, int value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == value) return i;
        }
        return -1;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
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
    protected void onResume() {
        super.onResume();
        // Sound selection happens in the picker; refresh the row subtitles.
        refreshSoundLabels();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancellationTokenSource.cancel();
        executor.shutdown();
    }
}
