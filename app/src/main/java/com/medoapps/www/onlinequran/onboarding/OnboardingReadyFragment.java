package com.medoapps.www.onlinequran.onboarding;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.athan.AthanScheduler;

public class OnboardingReadyFragment extends Fragment {

    private OnboardingHost host;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        host = (OnboardingHost) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> { /* either way, onboarding may proceed */ });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_ready, container, false);

        View exactAlarmLink = v.findViewById(R.id.onb_exact_alarm_link);
        exactAlarmLink.setOnClickListener(view -> openExactAlarmSettings());

        v.findViewById(R.id.onb_enter_app).setOnClickListener(view -> host.finishOnboarding());
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        OnboardingState state = host.getOnboardingState();

        // Request notifications if anything that notifies is on.
        boolean anyNotifying = state.athanEnabled || anyReminderOn(state);
        if (anyNotifying
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Surface the exact-alarm grant only when athan is on and exact alarms are unavailable.
        boolean showExactAlarm = state.athanEnabled
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && !AthanScheduler.canUseExactAlarms(requireContext());
        requireView().findViewById(R.id.onb_exact_alarm_link)
                .setVisibility(showExactAlarm ? View.VISIBLE : View.GONE);
    }

    private boolean anyReminderOn(OnboardingState state) {
        for (Reminder r : Reminder.values()) {
            if (state.isReminderEnabled(r)) return true;
        }
        return false;
    }

    private void openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .setData(Uri.parse("package:" + requireContext().getPackageName()));
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
