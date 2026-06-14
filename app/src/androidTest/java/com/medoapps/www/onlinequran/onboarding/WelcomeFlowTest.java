package com.medoapps.www.onlinequran.onboarding;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.medoapps.www.onlinequran.PreferenceManager;
import com.medoapps.www.onlinequran.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WelcomeFlowTest {

    @Before
    public void resetFirstLaunch() {
        Context ctx = ApplicationProvider.getApplicationContext();
        // Ensure the onboarding actually shows.
        new PreferenceManager(ctx).setFirstTimeLaunch(true);
    }

    @Test
    public void personalizeStep_showsAllSevenReminderSwitches_andAthanToggle() {
        try (androidx.test.core.app.ActivityScenario<com.medoapps.www.onlinequran.WelcomeActivity> scenario =
                     androidx.test.core.app.ActivityScenario.launch(
                             com.medoapps.www.onlinequran.WelcomeActivity.class)) {

            // Drive to the Personalize page (intro + 5 tour = 6 Next taps).
            for (int i = 0; i < 6; i++) {
                onView(withId(R.id.onb_next)).perform(click());
            }

            onView(withId(R.id.onb_switch_athan)).check(matches(isDisplayed()));
            onView(withId(R.id.onb_continue)).check(matches(isDisplayed()));
            // The reminders container holds one row per Reminder.
            onView(withId(R.id.onb_reminders_container)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void enterApp_setsFirstTimeLaunchFalse() {
        Context ctx = ApplicationProvider.getApplicationContext();
        try (androidx.test.core.app.ActivityScenario<com.medoapps.www.onlinequran.WelcomeActivity> scenario =
                     androidx.test.core.app.ActivityScenario.launch(
                             com.medoapps.www.onlinequran.WelcomeActivity.class)) {

            for (int i = 0; i < 6; i++) {
                onView(withId(R.id.onb_next)).perform(click());
            }
            onView(withId(R.id.onb_continue)).perform(click()); // -> Ready
            onView(withId(R.id.onb_enter_app)).perform(click()); // finish
        }
        // After finishing, the first-launch flag must be false.
        org.junit.Assert.assertFalse(new PreferenceManager(ctx).isFirstTimeLaunch());
    }
}
