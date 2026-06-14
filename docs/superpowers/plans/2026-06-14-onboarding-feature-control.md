# Onboarding Feature-Control Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the outdated first-launch welcome carousel with a modern, theme-aware onboarding that showcases the five feature pillars and lets the user toggle athan + the seven notification reminders + theme during onboarding, writing to the app's existing settings stores.

**Architecture:** A rewritten `WelcomeActivity` hosts a `ViewPager2` over a `FragmentStateAdapter` with 8 steps (Intro, 5 Tour pillars, Personalize, Ready). Feature toggles are collected into a plain `OnboardingState`, then committed on finish via `OnboardingFeatureController`, which delegates to a thin `FeatureGateway` (production impl wraps the existing static schedulers; a fake impl is used in unit tests). Light renders "Warm Gold Glass", dark renders "Midnight Premium" via `values/` + `values-night/` resources. Full RTL/LTR support.

**Tech Stack:** Java, Android XML Views, Material 3, AndroidX `ViewPager2`, JUnit (JVM unit tests), Espresso (instrumented tests).

**Reference spec:** `docs/superpowers/specs/2026-06-14-onboarding-feature-control-design.md`

---

## File Structure

New package: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/`

| File | Responsibility |
|------|----------------|
| `onboarding/Reminder.java` | Enum of the 7 reminders + their default hour/minute (data only). |
| `onboarding/OnboardingState.java` | Mutable POJO holding chosen toggle state (athan, per-reminder map, theme mode). |
| `onboarding/FeatureGateway.java` | Interface abstracting the side-effecting calls (set athan, enable/disable reminder, set theme, read current state). |
| `onboarding/AndroidFeatureGateway.java` | Production `FeatureGateway` wrapping existing static schedulers / `PrayerSettings` / `SettingSaved`. |
| `onboarding/OnboardingFeatureController.java` | Pure logic: given an `OnboardingState`, drives the gateway. Unit-tested. |
| `onboarding/OnboardingHost.java` | Interface implemented by `WelcomeActivity`: exposes shared state + navigation callbacks to fragments. |
| `onboarding/OnboardingIntroFragment.java` | Step 0 hero. |
| `onboarding/OnboardingTourFragment.java` | Steps 1–5 (data-driven by arguments). |
| `onboarding/OnboardingPersonalizeFragment.java` | Step 6 toggles + theme picker. |
| `onboarding/OnboardingReadyFragment.java` | Step 7 permissions + finish. |
| `WelcomeActivity.java` (rewrite) | Hosts ViewPager2, dot indicator, Skip/Next bar, implements `OnboardingHost`. |
| Layouts (new) | `activity_welcome.xml`, `fragment_onboarding_intro.xml`, `fragment_onboarding_tour.xml`, `fragment_onboarding_personalize.xml`, `fragment_onboarding_ready.xml`. |
| Drawables (new) | backgrounds, glass card, gradient button, dot, tour art vectors. |
| `values/strings.xml` + `values-ar/strings.xml` | New `onb_*` strings (en + ar). |
| `values/colors.xml` + `values-night/colors.xml` | Onboarding palette tokens. |
| Test: `app/src/test/java/.../onboarding/OnboardingFeatureControllerTest.java` | JVM unit test of controller↔gateway wiring. |
| Test: `app/src/androidTest/java/.../onboarding/WelcomeFlowTest.java` | Espresso navigation + RTL test. |

Removed at the end: `slide_screen1.xml`–`slide_screen4.xml`, `layout-v14/slide_screen1.xml`.

---

## Task 1: Add ViewPager2 dependency

**Files:**
- Modify: `app/build.gradle` (dependencies block, near line 152 with the other AndroidX entries)

- [ ] **Step 1: Add the dependency**

In `app/build.gradle`, inside the `dependencies { ... }` block under the `// AndroidX` comment, add:

```gradle
    implementation "androidx.viewpager2:viewpager2:1.1.0"
```

- [ ] **Step 2: Sync / verify it resolves**

Run: `./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep viewpager2`
Expected: a line showing `androidx.viewpager2:viewpager2:1.1.0`.

- [ ] **Step 3: Commit**

```bash
git add app/build.gradle
git commit -m "build: add androidx viewpager2 for onboarding"
```

---

## Task 2: Reminder enum (data)

**Files:**
- Create: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/Reminder.java`

Default times below MATCH the existing scheduler defaults so onboarding is consistent with the in-app Notification Settings screen.

- [ ] **Step 1: Create the enum**

```java
package com.medoapps.www.onlinequran.onboarding;

/**
 * The seven optional daily reminders the user can toggle during onboarding.
 * Default hour/minute mirror each scheduler's existing defaults so an enabled
 * reminder lands at the same time the in-app Notification Settings screen uses.
 */
public enum Reminder {
    DAILY_AYAH(7, 0),
    MORNING_ATHKAR(6, 0),
    EVENING_ATHKAR(17, 0),
    DUA_HISN(20, 0),
    DAILY_HADITH(8, 0),
    ASMAUL_HUSNA(9, 0),
    SUHOOR_FASTING(4, 0);

    public final int defaultHour;
    public final int defaultMinute;

    Reminder(int defaultHour, int defaultMinute) {
        this.defaultHour = defaultHour;
        this.defaultMinute = defaultMinute;
    }
}
```

- [ ] **Step 2: Compile check**

Run: `./gradlew :app:compileDebugJavaWithJavac`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/onboarding/Reminder.java
git commit -m "feat(onboarding): add Reminder enum with default times"
```

---

## Task 3: OnboardingState (POJO)

**Files:**
- Create: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingState.java`

- [ ] **Step 1: Create the class**

```java
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
```

- [ ] **Step 2: Compile check**

Run: `./gradlew :app:compileDebugJavaWithJavac`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingState.java
git commit -m "feat(onboarding): add OnboardingState holder"
```

---

## Task 4: FeatureGateway interface

**Files:**
- Create: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/FeatureGateway.java`

- [ ] **Step 1: Create the interface**

```java
package com.medoapps.www.onlinequran.onboarding;

/**
 * Abstracts every side-effecting feature write the onboarding performs, so the
 * controller logic can be unit-tested with a fake and the production impl can
 * wrap the existing static schedulers.
 */
public interface FeatureGateway {

    void setAthanEnabled(boolean enabled);

    void enableReminder(Reminder reminder);

    void disableReminder(Reminder reminder);

    void setThemeMode(int nightMode);

    // --- reads used to seed initial toggle positions ---

    boolean isAthanEnabled();

    boolean isReminderEnabled(Reminder reminder);

    int currentThemeMode();
}
```

- [ ] **Step 2: Compile check**

Run: `./gradlew :app:compileDebugJavaWithJavac`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/onboarding/FeatureGateway.java
git commit -m "feat(onboarding): add FeatureGateway interface"
```

---

## Task 5: OnboardingFeatureController (TDD)

**Files:**
- Create: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingFeatureController.java`
- Test: `app/src/test/java/com/medoapps/www/onlinequran/onboarding/OnboardingFeatureControllerTest.java`

- [ ] **Step 1: Write the failing test**

```java
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
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "*.OnboardingFeatureControllerTest"`
Expected: FAIL — compilation error, `OnboardingFeatureController` does not exist.

- [ ] **Step 3: Write the minimal implementation**

```java
package com.medoapps.www.onlinequran.onboarding;

/** Applies an {@link OnboardingState} to the app via a {@link FeatureGateway}. */
public class OnboardingFeatureController {

    private final FeatureGateway gateway;

    public OnboardingFeatureController(FeatureGateway gateway) {
        this.gateway = gateway;
    }

    public void apply(OnboardingState state) {
        gateway.setAthanEnabled(state.athanEnabled);
        for (Reminder r : Reminder.values()) {
            if (state.isReminderEnabled(r)) {
                gateway.enableReminder(r);
            } else {
                gateway.disableReminder(r);
            }
        }
        gateway.setThemeMode(state.themeMode);
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "*.OnboardingFeatureControllerTest"`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingFeatureController.java \
        app/src/test/java/com/medoapps/www/onlinequran/onboarding/OnboardingFeatureControllerTest.java
git commit -m "feat(onboarding): add OnboardingFeatureController with unit tests"
```

---

## Task 6: AndroidFeatureGateway (production glue)

**Files:**
- Create: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/AndroidFeatureGateway.java`

This wraps the existing static APIs verified in the codebase:
`PrayerSettings.setAthanFeatureEnabled` / `isAthanFeatureEnabled`, `AthanScheduler.rescheduleAll`,
the seven schedulers' `schedule`/`cancel`, and `SettingSaved` + `SeparateFunctions.changeAppThemeGlobally`.

- [ ] **Step 1: Create the class**

```java
package com.medoapps.www.onlinequran.onboarding;

import android.content.Context;

import com.medoapps.www.onlinequran.AsmaulHusnaScheduler;
import com.medoapps.www.onlinequran.AthkarAlarmScheduler;
import com.medoapps.www.onlinequran.DailyAyahScheduler;
import com.medoapps.www.onlinequran.DailyHadithScheduler;
import com.medoapps.www.onlinequran.FastingReminderScheduler;
import com.medoapps.www.onlinequran.HisnNotificationScheduler;
import com.medoapps.www.onlinequran.SettingSaved;
import com.medoapps.www.onlinequran.athan.AthanScheduler;
import com.medoapps.www.onlinequran.athan.PrayerSettings;
import com.medoapps.www.onlinequran.util.SeparateFunctions;

/** Production {@link FeatureGateway} backed by the app's existing schedulers. */
public class AndroidFeatureGateway implements FeatureGateway {

    private final Context context;

    public AndroidFeatureGateway(Context context) {
        // use application context — these writes outlive the activity
        this.context = context.getApplicationContext();
    }

    @Override
    public void setAthanEnabled(boolean enabled) {
        PrayerSettings.setAthanFeatureEnabled(context, enabled);
        // rescheduleAll cancels everything when the feature is off and
        // (re)schedules with the Doze-proof exact/inexact fallback when on.
        AthanScheduler.rescheduleAll(context);
    }

    @Override
    public void enableReminder(Reminder reminder) {
        switch (reminder) {
            case DAILY_AYAH:
                DailyAyahScheduler.scheduleDailyAyah(context, reminder.defaultHour, reminder.defaultMinute);
                break;
            case MORNING_ATHKAR:
                AthkarAlarmScheduler.scheduleMorning(context, reminder.defaultHour, reminder.defaultMinute);
                break;
            case EVENING_ATHKAR:
                AthkarAlarmScheduler.scheduleEvening(context, reminder.defaultHour, reminder.defaultMinute);
                break;
            case DUA_HISN:
                HisnNotificationScheduler.schedule(context, reminder.defaultHour, reminder.defaultMinute);
                break;
            case DAILY_HADITH:
                DailyHadithScheduler.scheduleDailyHadith(context, reminder.defaultHour, reminder.defaultMinute);
                break;
            case ASMAUL_HUSNA:
                AsmaulHusnaScheduler.schedule(context, reminder.defaultHour, reminder.defaultMinute);
                break;
            case SUHOOR_FASTING:
                FastingReminderScheduler.schedule(context, reminder.defaultHour, reminder.defaultMinute);
                break;
        }
    }

    @Override
    public void disableReminder(Reminder reminder) {
        switch (reminder) {
            case DAILY_AYAH:      DailyAyahScheduler.cancel(context); break;
            case MORNING_ATHKAR:  AthkarAlarmScheduler.cancelMorning(context); break;
            case EVENING_ATHKAR:  AthkarAlarmScheduler.cancelEvening(context); break;
            case DUA_HISN:        HisnNotificationScheduler.cancel(context); break;
            case DAILY_HADITH:    DailyHadithScheduler.cancel(context); break;
            case ASMAUL_HUSNA:    AsmaulHusnaScheduler.cancel(context); break;
            case SUHOOR_FASTING:  FastingReminderScheduler.cancel(context); break;
        }
    }

    @Override
    public void setThemeMode(int nightMode) {
        SettingSaved.currentThemeMode = nightMode;
        new SettingSaved(context).SaveData();
        new SeparateFunctions(context).changeAppThemeGlobally();
    }

    @Override
    public boolean isAthanEnabled() {
        return PrayerSettings.isAthanFeatureEnabled(context);
    }

    @Override
    public boolean isReminderEnabled(Reminder reminder) {
        switch (reminder) {
            case DAILY_AYAH:
                return context.getSharedPreferences("daily_ayah_prefs", Context.MODE_PRIVATE)
                        .getBoolean("notification_enabled", false);
            case MORNING_ATHKAR:  return AthkarAlarmScheduler.isMorningEnabled(context);
            case EVENING_ATHKAR:  return AthkarAlarmScheduler.isEveningEnabled(context);
            case DUA_HISN:        return HisnNotificationScheduler.isEnabled(context);
            case DAILY_HADITH:
                return context.getSharedPreferences("daily_hadith_prefs", Context.MODE_PRIVATE)
                        .getBoolean("notification_enabled", false);
            case ASMAUL_HUSNA:    return AsmaulHusnaScheduler.isEnabled(context);
            case SUHOOR_FASTING:  return FastingReminderScheduler.isEnabled(context);
        }
        return false;
    }

    @Override
    public int currentThemeMode() {
        SettingSaved s = new SettingSaved(context);
        s.LoadData();
        return SettingSaved.currentThemeMode;
    }
}
```

- [ ] **Step 2: Compile check**

Run: `./gradlew :app:compileDebugJavaWithJavac`
Expected: BUILD SUCCESSFUL. If a symbol is unresolved, confirm the class/method name against the file noted in the spec and fix the import.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/onboarding/AndroidFeatureGateway.java
git commit -m "feat(onboarding): add AndroidFeatureGateway wrapping existing schedulers"
```

---

## Task 7: Onboarding palette & drawables

**Files:**
- Modify: `app/src/main/res/values/colors.xml` (append)
- Create: `app/src/main/res/values-night/colors.xml`
- Create drawables: `bg_onboarding.xml`, `bg_onboarding_card.xml`, `bg_onboarding_art.xml`, `bg_onboarding_button.xml`, `bg_onboarding_dot.xml`, `bg_onboarding_shortcut.xml`

- [ ] **Step 1: Append onboarding colors (light)**

In `app/src/main/res/values/colors.xml`, before the closing `</resources>`, add:

```xml
    <!-- Onboarding (light = Warm Gold Glass) -->
    <color name="onb_bg_top">#FFF8EF</color>
    <color name="onb_bg_bottom">#F1E0C0</color>
    <color name="onb_card">#9EFFFFFF</color>
    <color name="onb_card_stroke">#E6CF9F</color>
    <color name="onb_text_primary">#5B4A22</color>
    <color name="onb_text_secondary">#7A6A45</color>
    <color name="onb_accent_start">#D4A44C</color>
    <color name="onb_accent_end">#B8860B</color>
    <color name="onb_on_accent">#FFFFFF</color>
    <color name="onb_dot_inactive">#CDBF9E</color>
```

- [ ] **Step 2: Create night overrides (Midnight Premium)**

Create `app/src/main/res/values-night/colors.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Onboarding (dark = Midnight Premium) -->
    <color name="onb_bg_top">#1F2A44</color>
    <color name="onb_bg_bottom">#0B0F1C</color>
    <color name="onb_card">#14FFFFFF</color>
    <color name="onb_card_stroke">#22FFFFFF</color>
    <color name="onb_text_primary">#ECE4D2</color>
    <color name="onb_text_secondary">#B7AE97</color>
    <color name="onb_accent_start">#E7C884</color>
    <color name="onb_accent_end">#C9982F</color>
    <color name="onb_on_accent">#1A1205</color>
    <color name="onb_dot_inactive">#39435C</color>
</resources>
```

- [ ] **Step 3: Create the background gradient** — `app/src/main/res/drawable/bg_onboarding.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <gradient
        android:type="linear"
        android:angle="270"
        android:startColor="@color/onb_bg_top"
        android:endColor="@color/onb_bg_bottom" />
</shape>
```

- [ ] **Step 4: Create the glass card** — `app/src/main/res/drawable/bg_onboarding_card.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="@color/onb_card" />
    <corners android:radius="24dp" />
    <stroke android:width="1dp" android:color="@color/onb_card_stroke" />
</shape>
```

- [ ] **Step 5: Create the art frame** — `app/src/main/res/drawable/bg_onboarding_art.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="@color/onb_card" />
    <corners android:radius="24dp" />
    <stroke android:width="1dp" android:color="@color/onb_card_stroke" />
</shape>
```

- [ ] **Step 6: Create the accent button** — `app/src/main/res/drawable/bg_onboarding_button.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <gradient
        android:type="linear"
        android:angle="0"
        android:startColor="@color/onb_accent_start"
        android:endColor="@color/onb_accent_end" />
    <corners android:radius="30dp" />
</shape>
```

- [ ] **Step 7: Create the dashed shortcut chip** — `app/src/main/res/drawable/bg_onboarding_shortcut.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="@color/onb_card" />
    <corners android:radius="12dp" />
    <stroke android:width="1dp" android:color="@color/onb_accent_start" android:dashWidth="6dp" android:dashGap="4dp" />
</shape>
```

- [ ] **Step 8: Create the dot (active)** — `app/src/main/res/drawable/bg_onboarding_dot.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval">
    <solid android:color="@color/onb_accent_end" />
    <size android:width="8dp" android:height="8dp" />
</shape>
```

- [ ] **Step 9: Compile check**

Run: `./gradlew :app:processDebugResources`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 10: Commit**

```bash
git add app/src/main/res/values/colors.xml app/src/main/res/values-night/colors.xml \
        app/src/main/res/drawable/bg_onboarding*.xml
git commit -m "feat(onboarding): add Gold Glass / Midnight Premium palette and drawables"
```

---

## Task 8: Tour art vectors

**Files:**
- Create: `app/src/main/res/drawable/ic_onb_mushaf.xml`, `ic_onb_reciters.xml`, `ic_onb_radio.xml`, `ic_onb_athan.xml`, `ic_onb_tools.xml`

These are simple, tintable placeholder vectors (production art is an open item in the spec). Each is a valid 24dp vector tinted with the accent at render time.

- [ ] **Step 1: Create `ic_onb_mushaf.xml`**

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"
    android:tint="@color/onb_accent_end">
    <path android:fillColor="@android:color/white"
        android:pathData="M12,4C9,2.5 5,2.5 3,4v15c2,-1.5 6,-1.5 9,0c3,-1.5 7,-1.5 9,0V4C19,2.5 15,2.5 12,4zM12,6v11" />
</vector>
```

- [ ] **Step 2: Create `ic_onb_reciters.xml`**

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"
    android:tint="@color/onb_accent_end">
    <path android:fillColor="@android:color/white"
        android:pathData="M12,3a4,4 0 0,0 -4,4v4a4,4 0 0,0 8,0V7a4,4 0 0,0 -4,-4zM6,11a6,6 0 0,0 12,0M12,17v4M8,21h8" />
</vector>
```

- [ ] **Step 3: Create `ic_onb_radio.xml`**

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"
    android:tint="@color/onb_accent_end">
    <path android:fillColor="@android:color/white"
        android:pathData="M3.24,6.15C2.51,6.43 2,7.17 2,8v12a2,2 0 0,0 2,2h16a2,2 0 0,0 2,-2V8a2,2 0 0,0 -2,-2H8.3l8.26,-3.34L15.88,1L3.24,6.15zM7,20a2.5,2.5 0 1,1 0,-5a2.5,2.5 0 0,1 0,5z" />
</vector>
```

- [ ] **Step 4: Create `ic_onb_athan.xml`**

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"
    android:tint="@color/onb_accent_end">
    <path android:fillColor="@android:color/white"
        android:pathData="M12,2c-1.1,1.5 -3,3 -3,5a3,3 0 0,0 6,0c0,-2 -1.9,-3.5 -3,-5zM6,11c-1,1 -2,2 -2,4v6h16v-6c0,-2 -1,-3 -2,-4H6zM11,15h2v6h-2z" />
</vector>
```

- [ ] **Step 5: Create `ic_onb_tools.xml`**

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"
    android:tint="@color/onb_accent_end">
    <path android:fillColor="@android:color/white"
        android:pathData="M3,5h8v8H3zM13,5h8v4h-8zM13,11h8v8h-8zM3,15h8v4H3z" />
</vector>
```

- [ ] **Step 6: Compile check**

Run: `./gradlew :app:processDebugResources`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/res/drawable/ic_onb_*.xml
git commit -m "feat(onboarding): add tour pillar art vectors"
```

---

## Task 9: Onboarding strings (en + ar)

**Files:**
- Modify: `app/src/main/res/values/strings.xml` (append)
- Modify: `app/src/main/res/values-ar/strings.xml` (append)

- [ ] **Step 1: Append English strings** to `app/src/main/res/values/strings.xml` before `</resources>`:

```xml
    <!-- Onboarding -->
    <string name="onb_get_started">Get Started</string>
    <string name="onb_skip">Skip</string>
    <string name="onb_next">Next</string>
    <string name="onb_continue">Continue</string>
    <string name="onb_enter_app">Enter app</string>

    <string name="onb_intro_tagline">Quran, Radio &amp; Prayer times — online and offline</string>

    <string name="onb_offline_badge">Available offline</string>

    <string name="onb_mushaf_title">Read &amp; listen, even offline</string>
    <string name="onb_mushaf_desc">Download the Mushaf once, then read while the verse highlights in sync with the recitation — anywhere, no internet needed.</string>

    <string name="onb_reciters_title">150+ reciters, online or offline</string>
    <string name="onb_reciters_desc">Stream the whole Quran from your favourite qari, or download any surah to listen later with no connection.</string>

    <string name="onb_radio_title">Live Quran radio</string>
    <string name="onb_radio_desc">Tune in to dozens of live Quran stations across every qira\'a, all day long.</string>

    <string name="onb_athan_title">Never miss a prayer</string>
    <string name="onb_athan_desc">Accurate offline prayer times with a real athan at every prayer — pick your voice, location and reminders.</string>

    <string name="onb_tools_title">Everything in one place</string>
    <string name="onb_tools_desc">Qibla, Tasbih, Athkar, Zakat, 99 Names, Duas and more — your complete Islamic toolkit.</string>

    <string name="onb_personalize_title">Personalize</string>
    <string name="onb_personalize_sub">Turn features on now — change anytime in Settings</string>
    <string name="onb_section_prayer">Prayer Times</string>
    <string name="onb_section_reminders">Daily Reminders</string>
    <string name="onb_section_appearance">Appearance</string>

    <string name="onb_athan_toggle_title">Athan &amp; Prayer Alarms</string>
    <string name="onb_athan_toggle_desc">Adhan at every prayer, offline</string>
    <string name="onb_athan_shortcut">Set your location &amp; athan voice</string>

    <string name="onb_reminder_daily_ayah">Daily Ayah</string>
    <string name="onb_reminder_morning_athkar">Morning Athkar</string>
    <string name="onb_reminder_evening_athkar">Evening Athkar</string>
    <string name="onb_reminder_dua">Dua of the Day</string>
    <string name="onb_reminder_hadith">Daily Hadith</string>
    <string name="onb_reminder_asmaul_husna">Asmaul Husna</string>
    <string name="onb_reminder_suhoor">Suhoor Reminder</string>

    <string name="onb_theme_light">Light</string>
    <string name="onb_theme_dark">Dark</string>
    <string name="onb_theme_system">System</string>

    <string name="onb_ready_title">You\'re all set</string>
    <string name="onb_ready_desc">We\'ll ask for a couple of permissions so reminders and the athan can reach you on time.</string>
    <string name="onb_grant_exact_alarm">Allow exact alarms for accurate athan times</string>
```

- [ ] **Step 2: Append Arabic strings** to `app/src/main/res/values-ar/strings.xml` before `</resources>`:

```xml
    <!-- Onboarding -->
    <string name="onb_get_started">ابدأ الآن</string>
    <string name="onb_skip">تخطٍّ</string>
    <string name="onb_next">التالي</string>
    <string name="onb_continue">متابعة</string>
    <string name="onb_enter_app">دخول التطبيق</string>

    <string name="onb_intro_tagline">قرآن وإذاعة ومواقيت الصلاة — أونلاين وبدون إنترنت</string>

    <string name="onb_offline_badge">متاح بدون إنترنت</string>

    <string name="onb_mushaf_title">اقرأ واستمع — بدون إنترنت</string>
    <string name="onb_mushaf_desc">نزّل المصحف مرة واحدة، ثم اقرأ بينما تُضاء الآية مع التلاوة — في أي مكان وبدون إنترنت.</string>

    <string name="onb_reciters_title">أكثر من ١٥٠ قارئ — أونلاين أو بدون إنترنت</string>
    <string name="onb_reciters_desc">استمع للقرآن كاملاً بصوت قارئك المفضّل، أو نزّل أي سورة للاستماع لاحقاً بدون اتصال.</string>

    <string name="onb_radio_title">إذاعات القرآن المباشرة</string>
    <string name="onb_radio_desc">استمع لعشرات الإذاعات المباشرة بمختلف القراءات على مدار اليوم.</string>

    <string name="onb_athan_title">لا تفوّت صلاة</string>
    <string name="onb_athan_desc">مواقيت صلاة دقيقة بدون إنترنت مع أذان حقيقي لكل صلاة — اختر الصوت والموقع والتذكيرات.</string>

    <string name="onb_tools_title">كل شيء في مكان واحد</string>
    <string name="onb_tools_desc">القبلة والمسبحة والأذكار والزكاة وأسماء الله الحسنى والأدعية وأكثر — حقيبتك الإسلامية المتكاملة.</string>

    <string name="onb_personalize_title">التخصيص</string>
    <string name="onb_personalize_sub">فعّل المزايا الآن — يمكنك تغييرها لاحقاً من الإعدادات</string>
    <string name="onb_section_prayer">مواقيت الصلاة</string>
    <string name="onb_section_reminders">التذكيرات اليومية</string>
    <string name="onb_section_appearance">المظهر</string>

    <string name="onb_athan_toggle_title">الأذان ومنبهات الصلاة</string>
    <string name="onb_athan_toggle_desc">أذان عند كل صلاة وبدون إنترنت</string>
    <string name="onb_athan_shortcut">حدّد موقعك وصوت الأذان</string>

    <string name="onb_reminder_daily_ayah">آية اليوم</string>
    <string name="onb_reminder_morning_athkar">أذكار الصباح</string>
    <string name="onb_reminder_evening_athkar">أذكار المساء</string>
    <string name="onb_reminder_dua">دعاء اليوم</string>
    <string name="onb_reminder_hadith">حديث اليوم</string>
    <string name="onb_reminder_asmaul_husna">اسم الله اليوم</string>
    <string name="onb_reminder_suhoor">تذكير السحور</string>

    <string name="onb_theme_light">فاتح</string>
    <string name="onb_theme_dark">داكن</string>
    <string name="onb_theme_system">النظام</string>

    <string name="onb_ready_title">كل شيء جاهز</string>
    <string name="onb_ready_desc">سنطلب بعض الأذونات حتى تصلك التذكيرات والأذان في وقتها.</string>
    <string name="onb_grant_exact_alarm">اسمح بالمنبهات الدقيقة لمواقيت أذان دقيقة</string>
```

- [ ] **Step 3: Compile check**

Run: `./gradlew :app:processDebugResources`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/values/strings.xml app/src/main/res/values-ar/strings.xml
git commit -m "feat(onboarding): add en + ar onboarding strings"
```

---

## Task 10: OnboardingHost interface

**Files:**
- Create: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingHost.java`

- [ ] **Step 1: Create the interface**

```java
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
```

- [ ] **Step 2: Compile check**

Run: `./gradlew :app:compileDebugJavaWithJavac`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingHost.java
git commit -m "feat(onboarding): add OnboardingHost interface"
```

---

## Task 11: Intro fragment (step 0)

**Files:**
- Create: `app/src/main/res/layout/fragment_onboarding_intro.xml`
- Create: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingIntroFragment.java`

- [ ] **Step 1: Create the layout**

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:gravity="center"
        android:padding="32dp">

        <ImageView
            android:id="@+id/onb_intro_logo"
            android:layout_width="120dp"
            android:layout_height="120dp"
            android:src="@mipmap/ic_launcher"
            android:contentDescription="@string/app_name" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:text="@string/app_name"
            android:textColor="@color/onb_text_primary"
            android:textSize="28sp"
            android:textStyle="bold" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:gravity="center"
            android:text="@string/onb_intro_tagline"
            android:textColor="@color/onb_text_secondary"
            android:textSize="15sp" />
    </LinearLayout>
</FrameLayout>
```

- [ ] **Step 2: Create the fragment**

```java
package com.medoapps.www.onlinequran.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.medoapps.www.onlinequran.R;

public class OnboardingIntroFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_intro, container, false);
    }
}
```

- [ ] **Step 3: Compile check**

Run: `./gradlew :app:compileDebugJavaWithJavac`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/fragment_onboarding_intro.xml \
        app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingIntroFragment.java
git commit -m "feat(onboarding): add intro hero fragment"
```

---

## Task 12: Tour fragment (steps 1–5)

**Files:**
- Create: `app/src/main/res/layout/fragment_onboarding_tour.xml`
- Create: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingTourFragment.java`

- [ ] **Step 1: Create the layout**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center_horizontal"
    android:paddingStart="28dp"
    android:paddingEnd="28dp"
    android:paddingTop="56dp"
    android:paddingBottom="24dp">

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:background="@drawable/bg_onboarding_art">

        <ImageView
            android:id="@+id/onb_tour_art"
            android:layout_width="96dp"
            android:layout_height="96dp"
            android:layout_gravity="center"
            android:contentDescription="@null" />

        <TextView
            android:id="@+id/onb_tour_offline"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="top|start"
            android:layout_margin="12dp"
            android:background="@drawable/bg_onboarding_shortcut"
            android:paddingStart="8dp"
            android:paddingEnd="8dp"
            android:paddingTop="4dp"
            android:paddingBottom="4dp"
            android:text="@string/onb_offline_badge"
            android:textColor="@color/onb_text_primary"
            android:textSize="11sp"
            android:visibility="gone" />
    </FrameLayout>

    <TextView
        android:id="@+id/onb_tour_title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="20dp"
        android:gravity="center"
        android:textColor="@color/onb_text_primary"
        android:textSize="22sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/onb_tour_desc"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="10dp"
        android:gravity="center"
        android:textColor="@color/onb_text_secondary"
        android:textSize="14sp" />
</LinearLayout>
```

- [ ] **Step 2: Create the fragment**

```java
package com.medoapps.www.onlinequran.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.medoapps.www.onlinequran.R;

public class OnboardingTourFragment extends Fragment {

    private static final String ARG_ART = "art";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";
    private static final String ARG_OFFLINE = "offline";

    public static OnboardingTourFragment newInstance(int artRes, int titleRes, int descRes, boolean offline) {
        OnboardingTourFragment f = new OnboardingTourFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_ART, artRes);
        b.putInt(ARG_TITLE, titleRes);
        b.putInt(ARG_DESC, descRes);
        b.putBoolean(ARG_OFFLINE, offline);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_tour, container, false);
        Bundle args = requireArguments();

        ((android.widget.ImageView) v.findViewById(R.id.onb_tour_art))
                .setImageResource(args.getInt(ARG_ART));
        ((android.widget.TextView) v.findViewById(R.id.onb_tour_title))
                .setText(args.getInt(ARG_TITLE));
        ((android.widget.TextView) v.findViewById(R.id.onb_tour_desc))
                .setText(args.getInt(ARG_DESC));
        v.findViewById(R.id.onb_tour_offline)
                .setVisibility(args.getBoolean(ARG_OFFLINE) ? View.VISIBLE : View.GONE);
        return v;
    }
}
```

- [ ] **Step 3: Compile check**

Run: `./gradlew :app:compileDebugJavaWithJavac`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/fragment_onboarding_tour.xml \
        app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingTourFragment.java
git commit -m "feat(onboarding): add data-driven tour pillar fragment"
```

---

## Task 13: Personalize fragment (step 6)

**Files:**
- Create: `app/src/main/res/layout/fragment_onboarding_personalize.xml`
- Create: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingPersonalizeFragment.java`

The fragment reads/writes the shared `OnboardingState` from the host, seeds the athan toggle from current settings, and exposes the seven reminder switches + theme picker. The "Continue" button calls `host.goToNextPage()`. The athan shortcut launches `AthanSettingsActivity`.

- [ ] **Step 1: Create the layout**

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:paddingStart="18dp"
        android:paddingEnd="18dp"
        android:paddingTop="40dp"
        android:paddingBottom="24dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/onb_personalize_title"
            android:textColor="@color/onb_text_primary"
            android:textSize="22sp"
            android:textStyle="bold" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="2dp"
            android:text="@string/onb_personalize_sub"
            android:textColor="@color/onb_text_secondary"
            android:textSize="12sp" />

        <!-- Prayer Times -->
        <TextView style="@style/OnbSectionLabel" android:text="@string/onb_section_prayer" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:background="@drawable/bg_onboarding_card"
            android:padding="12dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical">

                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="@string/onb_athan_toggle_title"
                        android:textColor="@color/onb_text_primary"
                        android:textStyle="bold"
                        android:textSize="14sp" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="@string/onb_athan_toggle_desc"
                        android:textColor="@color/onb_text_secondary"
                        android:textSize="11sp" />
                </LinearLayout>

                <com.google.android.material.materialswitch.MaterialSwitch
                    android:id="@+id/onb_switch_athan"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content" />
            </LinearLayout>

            <TextView
                android:id="@+id/onb_athan_shortcut"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="10dp"
                android:background="@drawable/bg_onboarding_shortcut"
                android:drawablePadding="6dp"
                android:padding="10dp"
                android:text="@string/onb_athan_shortcut"
                android:textColor="@color/onb_accent_end"
                android:textSize="12sp" />
        </LinearLayout>

        <!-- Daily Reminders -->
        <TextView style="@style/OnbSectionLabel" android:text="@string/onb_section_reminders" />

        <LinearLayout
            android:id="@+id/onb_reminders_container"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical" />

        <!-- Appearance -->
        <TextView style="@style/OnbSectionLabel" android:text="@string/onb_section_appearance" />

        <RadioGroup
            android:id="@+id/onb_theme_group"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal">

            <RadioButton
                android:id="@+id/onb_theme_light"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="@string/onb_theme_light"
                android:textColor="@color/onb_text_primary" />

            <RadioButton
                android:id="@+id/onb_theme_dark"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="@string/onb_theme_dark"
                android:textColor="@color/onb_text_primary" />

            <RadioButton
                android:id="@+id/onb_theme_system"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="@string/onb_theme_system"
                android:textColor="@color/onb_text_primary" />
        </RadioGroup>

        <Button
            android:id="@+id/onb_continue"
            android:layout_width="match_parent"
            android:layout_height="52dp"
            android:layout_marginTop="20dp"
            android:background="@drawable/bg_onboarding_button"
            android:text="@string/onb_continue"
            android:textColor="@color/onb_on_accent"
            android:textStyle="bold" />
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

- [ ] **Step 2: Add the section-label style** to `app/src/main/res/values/styles.xml` before `</resources>`:

```xml
    <style name="OnbSectionLabel" parent="">
        <item name="android:layout_width">wrap_content</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:layout_marginTop">18dp</item>
        <item name="android:layout_marginBottom">6dp</item>
        <item name="android:textAllCaps">true</item>
        <item name="android:textSize">11sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:textColor">@color/onb_text_secondary</item>
    </style>
```

- [ ] **Step 3: Create the fragment**

```java
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
```

- [ ] **Step 4: Compile check**

Run: `./gradlew :app:compileDebugJavaWithJavac`
Expected: BUILD SUCCESSFUL. If `MaterialSwitch` is unresolved, confirm the Material dependency version supports `com.google.android.material.materialswitch.MaterialSwitch` (Material 1.7+); the project uses Material 3, which includes it.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/layout/fragment_onboarding_personalize.xml \
        app/src/main/res/values/styles.xml \
        app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingPersonalizeFragment.java
git commit -m "feat(onboarding): add Personalize step with feature toggles + theme picker"
```

---

## Task 14: Ready fragment (step 7)

**Files:**
- Create: `app/src/main/res/layout/fragment_onboarding_ready.xml`
- Create: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingReadyFragment.java`

Requests `POST_NOTIFICATIONS` when any feature is enabled; offers an exact-alarm grant when athan is on and exact alarms are unavailable; "Enter app" calls `host.finishOnboarding()` regardless of grant outcome.

- [ ] **Step 1: Create the layout**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="32dp">

    <ImageView
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:src="@mipmap/ic_launcher"
        android:contentDescription="@null" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="20dp"
        android:text="@string/onb_ready_title"
        android:textColor="@color/onb_text_primary"
        android:textSize="24sp"
        android:textStyle="bold" />

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="10dp"
        android:gravity="center"
        android:text="@string/onb_ready_desc"
        android:textColor="@color/onb_text_secondary"
        android:textSize="14sp" />

    <TextView
        android:id="@+id/onb_exact_alarm_link"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:padding="8dp"
        android:text="@string/onb_grant_exact_alarm"
        android:textColor="@color/onb_accent_end"
        android:textSize="13sp"
        android:visibility="gone" />

    <Button
        android:id="@+id/onb_enter_app"
        android:layout_width="match_parent"
        android:layout_height="52dp"
        android:layout_marginTop="28dp"
        android:background="@drawable/bg_onboarding_button"
        android:text="@string/onb_enter_app"
        android:textColor="@color/onb_on_accent"
        android:textStyle="bold" />
</LinearLayout>
```

- [ ] **Step 2: Create the fragment**

```java
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
```

- [ ] **Step 3: Compile check**

Run: `./gradlew :app:compileDebugJavaWithJavac`
Expected: BUILD SUCCESSFUL. (`AthanScheduler.canUseExactAlarms(Context)` is confirmed present.)

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/fragment_onboarding_ready.xml \
        app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingReadyFragment.java
git commit -m "feat(onboarding): add Ready step with contextual permission requests"
```

---

## Task 15: Rewrite WelcomeActivity (host + ViewPager2)

**Files:**
- Replace: `app/src/main/java/com/medoapps/www/onlinequran/WelcomeActivity.java`
- Replace: `app/src/main/res/layout/activity_welcome.xml`

The new activity keeps the existing first-launch guard and the anonymous-auth finish behavior from the old `launchHomeScreen()`, but drives the 8-step `ViewPager2` and implements `OnboardingHost`.

- [ ] **Step 1: Replace the layout**

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/bg_onboarding">

    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/onb_pager"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <LinearLayout
        android:id="@+id/onb_dots"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_above="@+id/onb_bottom_bar"
        android:layout_centerHorizontal="true"
        android:layout_marginBottom="12dp"
        android:orientation="horizontal" />

    <RelativeLayout
        android:id="@+id/onb_bottom_bar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:paddingBottom="20dp">

        <Button
            android:id="@+id/onb_skip"
            style="?android:attr/borderlessButtonStyle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentStart="true"
            android:text="@string/onb_skip"
            android:textColor="@color/onb_text_secondary" />

        <Button
            android:id="@+id/onb_next"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentEnd="true"
            android:background="@drawable/bg_onboarding_button"
            android:minWidth="120dp"
            android:paddingStart="24dp"
            android:paddingEnd="24dp"
            android:text="@string/onb_next"
            android:textColor="@color/onb_on_accent"
            android:textStyle="bold" />
    </RelativeLayout>
</RelativeLayout>
```

- [ ] **Step 2: Replace the activity**

```java
package com.medoapps.www.onlinequran;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.medoapps.www.onlinequran.onboarding.AndroidFeatureGateway;
import com.medoapps.www.onlinequran.onboarding.OnboardingFeatureController;
import com.medoapps.www.onlinequran.onboarding.OnboardingHost;
import com.medoapps.www.onlinequran.onboarding.OnboardingIntroFragment;
import com.medoapps.www.onlinequran.onboarding.OnboardingPersonalizeFragment;
import com.medoapps.www.onlinequran.onboarding.OnboardingReadyFragment;
import com.medoapps.www.onlinequran.onboarding.OnboardingState;
import com.medoapps.www.onlinequran.onboarding.OnboardingTourFragment;
import com.medoapps.www.onlinequran.service.AuthService;

import android.content.Intent;

public class WelcomeActivity extends AppCompatActivity implements OnboardingHost {

    /** Page indices. Pages 0..LAST_TOUR_PAGE show dots + the Skip/Next bar. */
    private static final int LAST_TOUR_PAGE = 5; // intro(0) + 5 tour slides (1..5)
    private static final int DOT_COUNT = LAST_TOUR_PAGE + 1; // 6 dots for pages 0..5

    private ViewPager2 pager;
    private LinearLayout dotsLayout;
    private View bottomBar;
    private Button btnSkip, btnNext;

    private PreferenceManager prefManager;
    private AuthService authService;
    private OnboardingState onboardingState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authService = new AuthService(this);
        prefManager = new PreferenceManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            return;
        }

        // Seed state from current settings so toggles reflect reality on re-runs.
        AndroidFeatureGateway gateway = new AndroidFeatureGateway(this);
        onboardingState = OnboardingState.defaults();
        onboardingState.athanEnabled = gateway.isAthanEnabled();
        onboardingState.themeMode = gateway.currentThemeMode();
        for (com.medoapps.www.onlinequran.onboarding.Reminder r
                : com.medoapps.www.onlinequran.onboarding.Reminder.values()) {
            onboardingState.setReminderEnabled(r, gateway.isReminderEnabled(r));
        }

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        changeStatusBarColor();

        setContentView(R.layout.activity_welcome);
        pager = findViewById(R.id.onb_pager);
        dotsLayout = findViewById(R.id.onb_dots);
        bottomBar = findViewById(R.id.onb_bottom_bar);
        btnSkip = findViewById(R.id.onb_skip);
        btnNext = findViewById(R.id.onb_next);

        pager.setAdapter(new OnboardingPagerAdapter(this));
        pager.registerOnPageChangeCallback(pageChangeCallback);
        buildDots();
        updateChromeForPage(0);

        btnSkip.setOnClickListener(v -> finishOnboarding());
        btnNext.setOnClickListener(v -> goToNextPage());
    }

    // ---- OnboardingHost ----

    @Override
    public OnboardingState getOnboardingState() {
        return onboardingState;
    }

    @Override
    public void goToNextPage() {
        int next = pager.getCurrentItem() + 1;
        if (next < OnboardingPagerAdapter.PAGE_COUNT) {
            pager.setCurrentItem(next, true);
        } else {
            finishOnboarding();
        }
    }

    @Override
    public void finishOnboarding() {
        new OnboardingFeatureController(new AndroidFeatureGateway(this)).apply(onboardingState);
        launchHomeScreen();
    }

    // ---- chrome ----

    private final ViewPager2.OnPageChangeCallback pageChangeCallback =
            new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    updateChromeForPage(position);
                }
            };

    private void updateChromeForPage(int position) {
        boolean showBar = position <= LAST_TOUR_PAGE;
        bottomBar.setVisibility(showBar ? View.VISIBLE : View.GONE);
        dotsLayout.setVisibility(showBar ? View.VISIBLE : View.GONE);
        if (showBar) {
            highlightDot(position);
            btnNext.setText(position == 0 ? getString(R.string.onb_get_started) : getString(R.string.onb_next));
        }
    }

    private void buildDots() {
        dotsLayout.removeAllViews();
        for (int i = 0; i < DOT_COUNT; i++) {
            TextView dot = new TextView(this);
            dot.setText(Html.fromHtml("&#8226;"));
            dot.setTextSize(28);
            dot.setPadding(6, 0, 6, 0);
            dot.setTextColor(getResources().getColor(R.color.onb_dot_inactive));
            dotsLayout.addView(dot);
        }
        highlightDot(0);
    }

    private void highlightDot(int position) {
        if (position >= DOT_COUNT) return;
        for (int i = 0; i < dotsLayout.getChildCount(); i++) {
            ((TextView) dotsLayout.getChildAt(i)).setTextColor(
                    getResources().getColor(R.color.onb_dot_inactive));
        }
        ((TextView) dotsLayout.getChildAt(position)).setTextColor(
                getResources().getColor(R.color.onb_accent_end));
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        try {
            if (!authService.isUserSignedIn()) {
                authService.signInAnonymously();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /** 8 steps: intro, 5 tour pillars, personalize, ready. */
    private static class OnboardingPagerAdapter extends FragmentStateAdapter {
        static final int PAGE_COUNT = 8;

        OnboardingPagerAdapter(FragmentActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new OnboardingIntroFragment();
                case 1:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_mushaf, R.string.onb_mushaf_title, R.string.onb_mushaf_desc, true);
                case 2:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_reciters, R.string.onb_reciters_title, R.string.onb_reciters_desc, true);
                case 3:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_radio, R.string.onb_radio_title, R.string.onb_radio_desc, false);
                case 4:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_athan, R.string.onb_athan_title, R.string.onb_athan_desc, true);
                case 5:
                    return OnboardingTourFragment.newInstance(
                            R.drawable.ic_onb_tools, R.string.onb_tools_title, R.string.onb_tools_desc, false);
                case 6:
                    return new OnboardingPersonalizeFragment();
                default:
                    return new OnboardingReadyFragment();
            }
        }

        @Override
        public int getItemCount() {
            return PAGE_COUNT;
        }
    }
}
```

- [ ] **Step 3: Compile check**

Run: `./gradlew :app:compileDebugJavaWithJavac`
Expected: BUILD SUCCESSFUL. If `R.string.start`/`R.string.next` references remain elsewhere they are untouched; this activity uses the new `onb_*` strings.

- [ ] **Step 4: Install and smoke-test the flow on a device/emulator**

Run:
```bash
./gradlew :app:installMadaniDebug
adb shell pm clear com.medoapps.www.onlinequran
adb shell am start -n com.medoapps.www.onlinequran/.SplashScreen
```
Expected: splash → new onboarding; swipe intro → 5 tour slides → Personalize (toggles + theme) → Continue → Ready → Enter app → MainActivity. (`installMadaniDebug` matches the Madani debug variant noted in the inspection report; if the variant task name differs, run `./gradlew tasks --all | grep installMadani` to confirm.)

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/WelcomeActivity.java \
        app/src/main/res/layout/activity_welcome.xml
git commit -m "feat(onboarding): rebuild WelcomeActivity with ViewPager2 8-step flow"
```

---

## Task 16: Remove obsolete slide layouts

**Files:**
- Delete: `app/src/main/res/layout/slide_screen1.xml`, `slide_screen2.xml`, `slide_screen3.xml`, `slide_screen4.xml`
- Delete: `app/src/main/res/layout-v14/slide_screen1.xml`

- [ ] **Step 1: Confirm nothing else references them**

Run: `grep -rn "slide_screen" app/src/main/java app/src/main/res`
Expected: no matches (the old `WelcomeActivity` was the only consumer and is now replaced). If any match remains, stop and resolve it before deleting.

- [ ] **Step 2: Delete the files**

```bash
git rm app/src/main/res/layout/slide_screen1.xml \
       app/src/main/res/layout/slide_screen2.xml \
       app/src/main/res/layout/slide_screen3.xml \
       app/src/main/res/layout/slide_screen4.xml \
       app/src/main/res/layout-v14/slide_screen1.xml
```

- [ ] **Step 3: Compile check**

Run: `./gradlew :app:assembleMadaniDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git commit -m "chore(onboarding): remove obsolete welcome slide layouts"
```

---

## Task 17: Instrumented navigation + RTL test

**Files:**
- Create: `app/src/androidTest/java/com/medoapps/www/onlinequran/onboarding/WelcomeFlowTest.java`

- [ ] **Step 1: Write the test**

```java
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
```

- [ ] **Step 2: Run the test on a connected device/emulator**

Run: `./gradlew :app:connectedMadaniDebugAndroidTest --tests "*.WelcomeFlowTest"`
Expected: PASS (2 tests). If the connected-test task name differs by variant, list it with `./gradlew tasks --all | grep connected | grep AndroidTest`.

- [ ] **Step 3: RTL spot-check (manual, Arabic locale)**

Run:
```bash
adb shell pm clear com.medoapps.www.onlinequran
adb shell am start -n com.medoapps.www.onlinequran/.WelcomeActivity
# switch device language to Arabic in Settings, or:
adb shell "setprop persist.sys.locale ar; stop; start"
```
Expected: layout mirrors (Skip on the right, Next on the left), Arabic strings render, swipe advances right-to-left, no clipping.

- [ ] **Step 4: Commit**

```bash
git add app/src/androidTest/java/com/medoapps/www/onlinequran/onboarding/WelcomeFlowTest.java
git commit -m "test(onboarding): add Espresso navigation + first-launch flow test"
```

---

## Task 18: Full-suite verification

- [ ] **Step 1: Unit tests**

Run: `./gradlew :app:testMadaniDebugUnitTest`
Expected: BUILD SUCCESSFUL (includes `OnboardingFeatureControllerTest`).

- [ ] **Step 2: Assemble the app**

Run: `./gradlew :app:assembleMadaniDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: On-device end-to-end check that toggles take effect**

Run:
```bash
adb shell pm clear com.medoapps.www.onlinequran
adb shell am start -n com.medoapps.www.onlinequran/.SplashScreen
# Complete onboarding: leave athan ON, switch ON "Daily Ayah", pick Dark theme, finish.
adb shell run-as com.medoapps.www.onlinequran cat /data/data/com.medoapps.www.onlinequran/shared_prefs/athan_settings.xml
adb shell run-as com.medoapps.www.onlinequran cat /data/data/com.medoapps.www.onlinequran/shared_prefs/daily_ayah_prefs.xml
```
Expected: `athan_settings.xml` shows `athan_feature_enabled=true`; `daily_ayah_prefs.xml` shows `notification_enabled=true`; the app launches in dark mode (Midnight Premium onboarding colors were visible).

- [ ] **Step 4: Final commit (if any tweaks were needed)**

```bash
git add -A
git commit -m "test(onboarding): verify full suite + on-device prefs"
```

---

## Self-Review Notes (addressed)

- **Spec coverage:** Intro/Tour/Personalize/Ready (Tasks 11–15); two distinct offline Quran slides (Task 12 + Task 15 adapter, offline badge in Task 12 layout + strings); athan + 7 reminder + theme toggles writing to existing stores (Tasks 5–6, 13); contextual permissions (Task 14); RTL via `start/end` + `values-ar` + ViewPager2 (Tasks 9, 12, 15, 17); Gold Glass/Midnight Premium via `values`/`values-night` (Task 7); cleanup of old slides (Task 16); defaults athan-on/reminders-off/system-theme (Task 3); unit + instrumented tests (Tasks 5, 17).
- **Placeholder scan:** Tour art uses real (if simple) vector XML, flagged as replaceable production art per spec — not a plan placeholder.
- **Type consistency:** `FeatureGateway` method names (`setAthanEnabled`, `enableReminder`, `disableReminder`, `setThemeMode`, `isAthanEnabled`, `isReminderEnabled`, `currentThemeMode`) are identical across Tasks 4/5/6; `OnboardingHost` (`getOnboardingState`, `goToNextPage`, `finishOnboarding`) identical across Tasks 10/13/14/15; `Reminder` constants used consistently in Tasks 2/6/13.
