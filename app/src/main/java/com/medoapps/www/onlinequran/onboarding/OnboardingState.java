package com.medoapps.www.onlinequran.onboarding;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.EnumMap;
import java.util.Map;

/** Mutable holder for the choices the user makes on the Personalize step. */
public class OnboardingState {

    public boolean athanEnabled;
    public int themeMode; // an AppCompatDelegate.MODE_NIGHT_* value
    private final Map<Reminder, Boolean> reminders = new EnumMap<>(Reminder.class);

    private OnboardingState() {
        for (Reminder r : Reminder.values()) {
            reminders.put(r, Boolean.FALSE);
        }
    }

    /** App defaults: athan on, all reminders off, theme follows system. */
    public static OnboardingState defaults() {
        OnboardingState s = new OnboardingState();
        s.athanEnabled = true;
        s.themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        return s;
    }

    public boolean isReminderEnabled(Reminder r) {
        Boolean v = reminders.get(r);
        return v != null && v;
    }

    public void setReminderEnabled(Reminder r, boolean enabled) {
        reminders.put(r, enabled);
    }
}
