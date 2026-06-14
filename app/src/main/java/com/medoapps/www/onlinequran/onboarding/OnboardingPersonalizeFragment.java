package com.medoapps.www.onlinequran.onboarding;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.medoapps.www.onlinequran.AthanSettingsActivity;
import com.medoapps.www.onlinequran.R;

public class OnboardingPersonalizeFragment extends Fragment {

    private OnboardingHost host;

    private static final int[] REMINDER_LABELS = {
            R.string.onb_reminder_daily_ayah,
            R.string.onb_reminder_morning_athkar,
            R.string.onb_reminder_evening_athkar,
            R.string.onb_reminder_dua,
            R.string.onb_reminder_hadith,
            R.string.onb_reminder_asmaul_husna,
            R.string.onb_reminder_suhoor,
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        host = (OnboardingHost) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_personalize, container, false);
        OnboardingState state = host.getOnboardingState();

        // Athan master toggle
        MaterialSwitch athanSwitch = v.findViewById(R.id.onb_switch_athan);
        View shortcut = v.findViewById(R.id.onb_athan_shortcut);
        athanSwitch.setChecked(state.athanEnabled);
        shortcut.setVisibility(state.athanEnabled ? View.VISIBLE : View.GONE);
        athanSwitch.setOnCheckedChangeListener((btn, checked) -> {
            state.athanEnabled = checked;
            shortcut.setVisibility(checked ? View.VISIBLE : View.GONE);
        });
        shortcut.setOnClickListener(view ->
                startActivity(new Intent(requireContext(), AthanSettingsActivity.class)));

        // Reminder switches — one row per Reminder, in enum order
        LinearLayout remindersContainer = v.findViewById(R.id.onb_reminders_container);
        Reminder[] reminders = Reminder.values();
        for (int i = 0; i < reminders.length; i++) {
            addReminderRow(inflater, remindersContainer, state, reminders[i], REMINDER_LABELS[i]);
        }

        // Theme picker
        RadioGroup themeGroup = v.findViewById(R.id.onb_theme_group);
        checkThemeRadio(v, state.themeMode);
        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.onb_theme_light) {
                state.themeMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.onb_theme_dark) {
                state.themeMode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                state.themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
        });

        v.findViewById(R.id.onb_continue).setOnClickListener(view -> host.goToNextPage());
        return v;
    }

    private void addReminderRow(LayoutInflater inflater, LinearLayout parent,
                                OnboardingState state, Reminder reminder, int labelRes) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_onboarding_card);
        int pad = dp(11);
        row.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(8);
        row.setLayoutParams(lp);

        TextView label = new TextView(requireContext());
        label.setText(labelRes);
        label.setTextColor(getResources().getColor(R.color.onb_text_primary));
        label.setTextSize(14);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        label.setLayoutParams(labelLp);
        row.addView(label);

        MaterialSwitch sw = new MaterialSwitch(requireContext());
        sw.setChecked(state.isReminderEnabled(reminder));
        sw.setOnCheckedChangeListener((btn, checked) -> state.setReminderEnabled(reminder, checked));
        row.addView(sw);

        parent.addView(row);
    }

    private void checkThemeRadio(View v, int mode) {
        if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
            ((RadioGroup) v.findViewById(R.id.onb_theme_group)).check(R.id.onb_theme_light);
        } else if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            ((RadioGroup) v.findViewById(R.id.onb_theme_group)).check(R.id.onb_theme_dark);
        } else {
            ((RadioGroup) v.findViewById(R.id.onb_theme_group)).check(R.id.onb_theme_system);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
