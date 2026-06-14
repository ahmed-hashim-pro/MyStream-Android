package com.medoapps.www.onlinequran.onboarding;

/** Bridge between the onboarding fragments and {@link com.medoapps.www.onlinequran.WelcomeActivity}. */
public interface OnboardingHost {

    /** Shared, mutable state the Personalize step edits and the Ready step applies. */
    OnboardingState getOnboardingState();

    /** Advance the pager by one page. */
    void goToNextPage();

    /** Commit the chosen state and launch the home screen. */
    void finishOnboarding();
}
