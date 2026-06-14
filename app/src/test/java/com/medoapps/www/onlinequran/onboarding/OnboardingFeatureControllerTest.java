package com.medoapps.www.onlinequran.onboarding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import androidx.appcompat.app.AppCompatDelegate;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class OnboardingFeatureControllerTest {

    /** Records what the controller asked the gateway to do. */
    private static class FakeGateway implements FeatureGateway {
        Boolean athanSet = null;
        final Set<Reminder> enabled = EnumSet.noneOf(Reminder.class);
        final Set<Reminder> disabled = EnumSet.noneOf(Reminder.class);
        final List<Integer> themeModes = new ArrayList<>();

        @Override public void setAthanEnabled(boolean enabled) { athanSet = enabled; }
        @Override public void enableReminder(Reminder r) { this.enabled.add(r); }
        @Override public void disableReminder(Reminder r) { this.disabled.add(r); }
        @Override public void setThemeMode(int nightMode) { themeModes.add(nightMode); }
        @Override public boolean isAthanEnabled() { return false; }
        @Override public boolean isReminderEnabled(Reminder r) { return false; }
        @Override public int currentThemeMode() { return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; }
    }

    @Test
    public void apply_setsAthanFromState() {
        FakeGateway gw = new FakeGateway();
        OnboardingState s = OnboardingState.defaults(); // athan on by default
        new OnboardingFeatureController(gw).apply(s);
        assertTrue(gw.athanSet);
    }

    @Test
    public void apply_enablesOnlyToggledReminders_disablesTheRest() {
        FakeGateway gw = new FakeGateway();
        OnboardingState s = OnboardingState.defaults();
        s.setReminderEnabled(Reminder.DAILY_AYAH, true);
        s.setReminderEnabled(Reminder.MORNING_ATHKAR, true);

        new OnboardingFeatureController(gw).apply(s);

        assertTrue(gw.enabled.contains(Reminder.DAILY_AYAH));
        assertTrue(gw.enabled.contains(Reminder.MORNING_ATHKAR));
        assertFalse(gw.enabled.contains(Reminder.EVENING_ATHKAR));
        assertTrue(gw.disabled.contains(Reminder.EVENING_ATHKAR));
        assertTrue(gw.disabled.contains(Reminder.SUHOOR_FASTING));
        // every reminder is acted on exactly once (enable XOR disable)
        assertEquals(Reminder.values().length, gw.enabled.size() + gw.disabled.size());
    }

    @Test
    public void apply_setsThemeMode() {
        FakeGateway gw = new FakeGateway();
        OnboardingState s = OnboardingState.defaults();
        s.themeMode = AppCompatDelegate.MODE_NIGHT_YES;
        new OnboardingFeatureController(gw).apply(s);
        assertEquals(1, gw.themeModes.size());
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, (int) gw.themeModes.get(0));
    }
}
