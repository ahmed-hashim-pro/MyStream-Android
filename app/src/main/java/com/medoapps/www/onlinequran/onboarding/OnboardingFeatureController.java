package com.medoapps.www.onlinequran.onboarding;

/** Applies an {@link OnboardingState} to the app via a {@link FeatureGateway}. */
public class OnboardingFeatureController {

    private final FeatureGateway gateway;

    public OnboardingFeatureController(FeatureGateway gateway) {
        this.gateway = gateway;
    }

    public void apply(OnboardingState state) {
        gateway.setAthanEnabled(state.athanEnabled);
        gateway.setBubbleEnabled(state.bubbleEnabled);
        for (Reminder r : Reminder.values()) {
            if (state.isReminderEnabled(r)) {
                gateway.enableReminder(r);
            } else {
                gateway.disableReminder(r);
            }
        }
        gateway.setThemeMode(state.themeMode);
        gateway.setPageType(state.pageType);
    }
}
