package com.medoapps.www.onlinequran.onboarding;

/**
 * Abstracts every side-effecting feature write the onboarding performs, so the
 * controller logic can be unit-tested with a fake and the production impl can
 * wrap the existing static schedulers.
 */
public interface FeatureGateway {

    void setAthanEnabled(boolean enabled);

    /**
     * Enable/disable the Floating Athkar Bubble. Enabling only takes effect if the overlay
     * (draw-over-apps) permission is granted; otherwise the bubble is left off.
     */
    void setBubbleEnabled(boolean enabled);

    void enableReminder(Reminder reminder);

    void disableReminder(Reminder reminder);

    void setThemeMode(int nightMode);

    // --- reads used to seed initial toggle positions ---

    boolean isAthanEnabled();

    boolean isBubbleEnabled();

    boolean isReminderEnabled(Reminder reminder);

    int currentThemeMode();
}
