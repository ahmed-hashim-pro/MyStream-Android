package com.medoapps.www.onlinequran.onboarding;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.EnumMap;
import java.util.Map;

/** Mutable holder for the choices the user makes on the Personalize step. */
public class OnboardingState {

    public boolean athanEnabled;
    public boolean bubbleEnabled;
    public int themeMode; // an AppCompatDelegate.MODE_NIGHT_* value
    public String pageType; // mushaf print key, e.g. "madani"
    public boolean autoMethodEnabled; // let the calculation method follow the location
    private final Map<Reminder, Boolean> reminders = new EnumMap<>(Reminder.class);

    private OnboardingState() {
        for (Reminder r : Reminder.values()) {
            reminders.put(r, Boolean.FALSE);
        }
    }

    /** App defaults: athan on, bubble on, all reminders off, theme follows system, auto-method on. */
    public static OnboardingState defaults() {
        OnboardingState s = new OnboardingState();
        s.athanEnabled = true;
        // Feature-forward: bubble defaults on. It only actually starts if the user grants the
        // overlay permission on the Ready step, so this is safe — it just surfaces that grant.
        s.bubbleEnabled = true;
        s.themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        // Default print for new installs: the "Madinah Colored" set (red لفظ الجلالة).
        // The onboarding picker centers/pre-selects this; users can still switch.
        s.pageType = "madina_colored";
        // Auto-update on: the point of the welcome-screen location fetch is that a new
        // user gets the right times and method without hunting through settings.
        s.autoMethodEnabled = true;
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
