package com.medoapps.www.onlinequran.onboarding;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.athan.AthanScheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * Final onboarding page: a per-feature permission gate. Each feature the user kept on lists its
 * required permissions with a Grant button; the user either grants every permission of an enabled
 * feature OR turns that feature off. "Enter app" stays disabled until every ON feature is fully
 * granted — so a refused permission never traps the user (they can always disable the feature).
 */
public class OnboardingPermissionsFragment extends Fragment {

    // permission keys
    private static final String NOTIF = "notif";
    private static final String LOC = "loc";
    private static final String EXACT = "exact";
    private static final String BATT = "batt";
    private static final String OVERLAY = "overlay";

    private OnboardingHost host;
    private ActivityResultLauncher<String[]> runtimeLauncher;
    private boolean askedNotif, askedLoc; // to detect a permanent denial -> route to Settings

    private MaterialSwitch athanToggle, bubbleToggle;
    private View athanRows, bubbleRows;
    private Button enterApp;
    private final List<PermRow> rows = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        host = (OnboardingHost) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runtimeLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> refresh());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_permissions, container, false);
        OnboardingState state = host.getOnboardingState();

        athanRows = v.findViewById(R.id.onb_perm_athan_rows);
        bubbleRows = v.findViewById(R.id.onb_perm_bubble_rows);
        enterApp = v.findViewById(R.id.onb_perm_enter);

        addRow((LinearLayout) athanRows, NOTIF, R.string.onb_perm_notif_name, R.string.onb_perm_notif_why);
        addRow((LinearLayout) athanRows, LOC, R.string.onb_perm_loc_name, R.string.onb_perm_loc_why);
        addRow((LinearLayout) athanRows, EXACT, R.string.onb_perm_exact_name, R.string.onb_perm_exact_why);
        addRow((LinearLayout) athanRows, BATT, R.string.onb_perm_batt_name, R.string.onb_perm_batt_why);
        addRow((LinearLayout) bubbleRows, OVERLAY, R.string.onb_perm_overlay_name, R.string.onb_perm_overlay_why);

        athanToggle = v.findViewById(R.id.onb_perm_athan_toggle);
        athanToggle.setChecked(state.athanEnabled);
        athanToggle.setOnCheckedChangeListener((b, checked) -> { state.athanEnabled = checked; refresh(); });

        bubbleToggle = v.findViewById(R.id.onb_perm_bubble_toggle);
        bubbleToggle.setChecked(state.bubbleEnabled);
        bubbleToggle.setOnCheckedChangeListener((b, checked) -> { state.bubbleEnabled = checked; refresh(); });

        enterApp.setOnClickListener(view -> host.finishOnboarding());
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    /** Re-evaluate every row's grant status, the cards' visibility, and the Enter-app gate. */
    private void refresh() {
        OnboardingState state = host.getOnboardingState();
        athanRows.setVisibility(state.athanEnabled ? View.VISIBLE : View.GONE);
        bubbleRows.setVisibility(state.bubbleEnabled ? View.VISIBLE : View.GONE);

        for (PermRow row : rows) {
            boolean granted = isGranted(row.key);
            row.granted.setVisibility(granted ? View.VISIBLE : View.GONE);
            row.grant.setVisibility(granted ? View.GONE : View.VISIBLE);
        }

        boolean athanOk = !state.athanEnabled
                || (isGranted(NOTIF) && isGranted(LOC) && isGranted(EXACT) && isGranted(BATT));
        boolean bubbleOk = !state.bubbleEnabled || isGranted(OVERLAY);
        boolean ready = athanOk && bubbleOk;
        enterApp.setEnabled(ready);
        enterApp.setAlpha(ready ? 1f : 0.5f);
    }

    private boolean isGranted(String key) {
        Context c = requireContext();
        switch (key) {
            case NOTIF:
                return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                        || hasPermission(Manifest.permission.POST_NOTIFICATIONS);
            case LOC:
                return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        || hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            case EXACT:
                return AthanScheduler.canUseExactAlarms(c);
            case BATT:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
                PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
                return pm == null || pm.isIgnoringBatteryOptimizations(c.getPackageName());
            case OVERLAY:
                return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(c);
        }
        return true;
    }

    private void grant(String key) {
        switch (key) {
            case NOTIF:
                requestRuntime(new String[]{Manifest.permission.POST_NOTIFICATIONS}, true);
                break;
            case LOC:
                requestRuntime(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, false);
                break;
            case EXACT:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    openSettings(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri()));
                }
                break;
            case BATT:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    openSettings(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri()));
                }
                break;
            case OVERLAY:
                openSettings(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, packageUri()));
                break;
        }
    }

    /**
     * Launch the runtime request, or — if it was already asked and the system won't show the
     * dialog again (permanently denied) — send the user to app Settings so they're never stuck.
     */
    private void requestRuntime(String[] perms, boolean isNotif) {
        boolean asked = isNotif ? askedNotif : askedLoc;
        boolean canPrompt = !asked || anyRationale(perms);
        if (canPrompt) {
            if (isNotif) askedNotif = true; else askedLoc = true;
            runtimeLauncher.launch(perms);
        } else {
            openSettings(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri()));
        }
    }

    private boolean anyRationale(String[] perms) {
        for (String p : perms) {
            if (shouldShowRequestPermissionRationale(p)) return true;
        }
        return false;
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(requireContext(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private Uri packageUri() {
        return Uri.parse("package:" + requireContext().getPackageName());
    }

    private void openSettings(Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Builds one permission row (name + why + Grant/✓Granted) and registers it for refresh. */
    private void addRow(LinearLayout parent, String key, int nameRes, int whyRes) {
        Context c = requireContext();
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int padV = dp(8);
        row.setPadding(0, padV, 0, padV);

        LinearLayout textCol = new LinearLayout(c);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = new TextView(c);
        name.setText(nameRes);
        name.setTextColor(getResources().getColor(R.color.onb_text_primary));
        name.setTextSize(14);
        textCol.addView(name);

        TextView why = new TextView(c);
        why.setText(whyRes);
        why.setTextColor(getResources().getColor(R.color.onb_text_secondary));
        why.setTextSize(11);
        textCol.addView(why);
        row.addView(textCol);

        TextView granted = new TextView(c);
        granted.setText(R.string.onb_perm_granted);
        granted.setTextColor(getResources().getColor(R.color.onb_text_secondary));
        granted.setTextSize(13);
        granted.setPadding(dp(8), 0, 0, 0);
        granted.setVisibility(View.GONE);
        row.addView(granted);

        TextView grantBtn = new TextView(c);
        grantBtn.setText(R.string.onb_perm_grant);
        grantBtn.setTextColor(getResources().getColor(R.color.onb_accent_end));
        grantBtn.setTypeface(grantBtn.getTypeface(), android.graphics.Typeface.BOLD);
        grantBtn.setTextSize(13);
        grantBtn.setBackgroundResource(R.drawable.bg_onboarding_shortcut);
        grantBtn.setPadding(dp(16), dp(8), dp(16), dp(8));
        grantBtn.setOnClickListener(v -> grant(key));
        row.addView(grantBtn);

        parent.addView(row);
        rows.add(new PermRow(key, granted, grantBtn));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class PermRow {
        final String key;
        final TextView granted;
        final TextView grant;
        PermRow(String key, TextView granted, TextView grant) {
            this.key = key;
            this.granted = granted;
            this.grant = grant;
        }
    }
}
