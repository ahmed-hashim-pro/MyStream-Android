# Onboarding Auto-Location + Auto Calculation Method — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** On the welcome screen, acquire the device location the moment the user grants permission, apply it to the athan prayer times, and derive the calculation method and madhab from where the user actually is.

**Architecture:** A pure, Android-free `PrayerLocaleDefaults` holds the country→method/madhab knowledge and is unit-tested directly. A thin `LocationApplier` is the single funnel every location fix in the app passes through (onboarding, Prayer Times, Athan settings), so the automatic pick behaves identically everywhere — including on travel. A new default-on `auto_method` pref gates whether the method follows the location; it is deliberately separate from the existing `LOCATION_AUTO` mode, which only controls whether location is tracked at all.

**Tech Stack:** Java 8, Android (minSdk per app), `com.batoulapps.adhan:adhan:1.2.1`, Google Play Services `FusedLocationProviderClient`, JUnit 4, Material `MaterialSwitch`.

## Global Constraints

- **Spec:** `docs/superpowers/specs/2026-07-25-onboarding-auto-location-method-design.md`
- **Language:** Java (this package is Java, not Kotlin). Match surrounding comment density and style.
- **All new user-facing strings MUST be added to BOTH `app/src/main/res/values/strings.xml` (EN) and `app/src/main/res/values-ar/strings.xml` (AR).** Project standing rule.
- **All new UI MUST be verified in light AND dark themes.** Project standing rule. `@color/white` is black at night — use the onboarding `@color/onb_*` tokens already used in the fragment.
- **Method/madhab values are `com.batoulapps.adhan` enum constant NAMES stored as strings** — `PrayerSettings` stores strings, `PrayerTimeEngine.getMethod()` does `CalculationMethod.valueOf(...)` inside a try/catch. Never store a name that is not a real enum constant.
- **Valid `CalculationMethod` constants (adhan 1.2.1), exhaustive:** `MUSLIM_WORLD_LEAGUE`, `EGYPTIAN`, `KARACHI`, `UMM_AL_QURA`, `DUBAI`, `MOON_SIGHTING_COMMITTEE`, `NORTH_AMERICA`, `KUWAIT`, `QATAR`, `SINGAPORE`, `OTHER`.
- **Valid `Madhab` constants:** `SHAFI`, `HANAFI`.
- **Build:** `./gradlew :app:compileMadaniDebugJavaWithJavac`
- **Unit tests:** `./gradlew :app:testMadaniDebugUnitTest`
- **No Robolectric in this project.** Anything touching `Context`/`SharedPreferences` cannot be unit-tested — keep decision logic in the pure class and verify glue by compile + on-device.
- Commit after every task. Conventional-commit style, e.g. `feat(athan): ...`.

---

### Task 1: `PrayerLocaleDefaults` — the pure country→settings core

**Files:**
- Create: `app/src/main/java/com/medoapps/www/onlinequran/athan/PrayerLocaleDefaults.java`
- Test: `app/src/test/java/com/medoapps/www/onlinequran/athan/PrayerLocaleDefaultsTest.java`

**Interfaces:**
- Consumes: nothing (this is the base of the feature).
- Produces, relied on by Tasks 2 and 5:
  - `PrayerLocaleDefaults.Defaults` with `public final String method` and `public final String madhab`
  - `static Defaults forCountry(String iso2)`
  - `static Defaults forCoordinates(double lat, double lng)`
  - `static boolean shouldReapply(String lastApplied, String resolved)`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/medoapps/www/onlinequran/athan/PrayerLocaleDefaultsTest.java`:

```java
package com.medoapps.www.onlinequran.athan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PrayerLocaleDefaultsTest {

    @Test
    public void forCountry_mapsTheGulf() {
        assertEquals("UMM_AL_QURA", PrayerLocaleDefaults.forCountry("SA").method);
        assertEquals("DUBAI", PrayerLocaleDefaults.forCountry("AE").method);
        assertEquals("KUWAIT", PrayerLocaleDefaults.forCountry("KW").method);
        assertEquals("QATAR", PrayerLocaleDefaults.forCountry("QA").method);
    }

    @Test
    public void forCountry_mapsTheMajorRegions() {
        assertEquals("EGYPTIAN", PrayerLocaleDefaults.forCountry("EG").method);
        assertEquals("KARACHI", PrayerLocaleDefaults.forCountry("PK").method);
        assertEquals("SINGAPORE", PrayerLocaleDefaults.forCountry("ID").method);
        assertEquals("NORTH_AMERICA", PrayerLocaleDefaults.forCountry("US").method);
    }

    @Test
    public void forCountry_isCaseInsensitiveAndTrims() {
        assertEquals("KARACHI", PrayerLocaleDefaults.forCountry("pk").method);
        assertEquals("KARACHI", PrayerLocaleDefaults.forCountry(" PK ").method);
    }

    @Test
    public void forCountry_unknownOrBlankFallsBackToMwlShafi() {
        assertEquals("MUSLIM_WORLD_LEAGUE", PrayerLocaleDefaults.forCountry("ZZ").method);
        assertEquals("SHAFI", PrayerLocaleDefaults.forCountry("ZZ").madhab);
        assertEquals("MUSLIM_WORLD_LEAGUE", PrayerLocaleDefaults.forCountry(null).method);
        assertEquals("MUSLIM_WORLD_LEAGUE", PrayerLocaleDefaults.forCountry("").method);
        assertEquals("MUSLIM_WORLD_LEAGUE", PrayerLocaleDefaults.forCountry("   ").method);
    }

    @Test
    public void forCountry_picksHanafiForSouthAndCentralAsia() {
        assertEquals("HANAFI", PrayerLocaleDefaults.forCountry("PK").madhab);
        assertEquals("HANAFI", PrayerLocaleDefaults.forCountry("IN").madhab);
        assertEquals("HANAFI", PrayerLocaleDefaults.forCountry("TR").madhab);
        assertEquals("SHAFI", PrayerLocaleDefaults.forCountry("EG").madhab);
        assertEquals("SHAFI", PrayerLocaleDefaults.forCountry("SA").madhab);
    }

    /** Turkey has no dedicated method but must still get the Hanafi madhab. */
    @Test
    public void forCountry_turkeyIsMwlButHanafi() {
        assertEquals("MUSLIM_WORLD_LEAGUE", PrayerLocaleDefaults.forCountry("TR").method);
        assertEquals("HANAFI", PrayerLocaleDefaults.forCountry("TR").madhab);
    }

    /** Box order is load-bearing: the narrow Gulf box must beat the wide Saudi one. */
    @Test
    public void forCoordinates_resolvesRealCities() {
        assertEquals("UMM_AL_QURA", PrayerLocaleDefaults.forCoordinates(21.4225, 39.8262).method); // Makkah
        assertEquals("DUBAI", PrayerLocaleDefaults.forCoordinates(25.20, 55.27).method);           // Dubai
        assertEquals("EGYPTIAN", PrayerLocaleDefaults.forCoordinates(30.04, 31.24).method);        // Cairo
        assertEquals("KARACHI", PrayerLocaleDefaults.forCoordinates(24.86, 67.01).method);         // Karachi
        assertEquals("SINGAPORE", PrayerLocaleDefaults.forCoordinates(-6.21, 106.85).method);      // Jakarta
        assertEquals("NORTH_AMERICA", PrayerLocaleDefaults.forCoordinates(43.65, -79.38).method);  // Toronto
    }

    @Test
    public void forCoordinates_unmappedFallsBackToMwl() {
        assertEquals("MUSLIM_WORLD_LEAGUE", PrayerLocaleDefaults.forCoordinates(51.5, -0.12).method); // London
    }

    @Test
    public void forCoordinates_southAsiaIsHanafi() {
        assertEquals("HANAFI", PrayerLocaleDefaults.forCoordinates(24.86, 67.01).madhab);
    }

    @Test
    public void shouldReapply_onlyWhenTheKeyActuallyChanges() {
        assertTrue(PrayerLocaleDefaults.shouldReapply("", "EG"));
        assertTrue(PrayerLocaleDefaults.shouldReapply(null, "EG"));
        assertTrue(PrayerLocaleDefaults.shouldReapply("SA", "EG"));
        assertFalse(PrayerLocaleDefaults.shouldReapply("EG", "EG"));
        assertFalse(PrayerLocaleDefaults.shouldReapply("EG", "eg"));
    }

    /** A blank resolution must never wipe a good saved pick. */
    @Test
    public void shouldReapply_isFalseWhenResolutionIsUnusable() {
        assertFalse(PrayerLocaleDefaults.shouldReapply("EG", null));
        assertFalse(PrayerLocaleDefaults.shouldReapply("EG", ""));
        assertFalse(PrayerLocaleDefaults.shouldReapply("EG", "  "));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testMadaniDebugUnitTest --tests '*PrayerLocaleDefaultsTest*'`
Expected: FAIL — compilation error, `cannot find symbol: class PrayerLocaleDefaults`.

- [ ] **Step 3: Write the implementation**

Create `app/src/main/java/com/medoapps/www/onlinequran/athan/PrayerLocaleDefaults.java`:

```java
package com.medoapps.www.onlinequran.athan;

import java.util.Locale;

/**
 * Maps a user's location to sensible prayer-calculation defaults.
 *
 * Pure and Android-free so it can be unit-tested directly. The geocoded ISO-3166
 * alpha-2 country code is the primary signal; {@link #forCoordinates} is a coarse
 * offline fallback for when reverse geocoding is unavailable (it needs network).
 *
 * Values are {@code com.batoulapps.adhan} enum constant NAMES — see
 * {@link PrayerSettings#setCalculationMethod} / {@link PrayerSettings#setMadhab}.
 */
public final class PrayerLocaleDefaults {

    /** A calculation method + madhab pair for a location. */
    public static final class Defaults {
        public final String method;
        public final String madhab;

        public Defaults(String method, String madhab) {
            this.method = method;
            this.madhab = madhab;
        }
    }

    private static final String MWL = "MUSLIM_WORLD_LEAGUE";
    private static final String SHAFI = "SHAFI";
    private static final String HANAFI = "HANAFI";

    private PrayerLocaleDefaults() {
    }

    /**
     * Defaults for an ISO-3166 alpha-2 country code (case-insensitive, trimmed).
     * Unknown or blank codes fall back to Muslim World League + Shafi.
     */
    public static Defaults forCountry(String iso2) {
        if (iso2 == null || iso2.trim().isEmpty()) {
            return new Defaults(MWL, SHAFI);
        }
        String c = iso2.trim().toUpperCase(Locale.US);
        return new Defaults(methodFor(c), madhabFor(c));
    }

    private static String methodFor(String c) {
        switch (c) {
            case "SA":
                return "UMM_AL_QURA";
            case "AE": case "OM": case "BH":
                return "DUBAI";
            case "KW":
                return "KUWAIT";
            case "QA":
                return "QATAR";
            case "EG": case "LY": case "DZ": case "TN": case "MA":
            case "SD": case "SY": case "IQ": case "JO": case "LB": case "YE":
                return "EGYPTIAN";
            case "PK": case "IN": case "BD": case "AF": case "LK":
                return "KARACHI";
            case "SG": case "MY": case "ID": case "BN":
                return "SINGAPORE";
            case "US": case "CA": case "MX":
                return "NORTH_AMERICA";
            default:
                // Turkey and Europe land here: no dedicated method, MWL is the norm.
                return MWL;
        }
    }

    private static String madhabFor(String c) {
        switch (c) {
            case "PK": case "IN": case "BD": case "AF":
            case "TR": case "UZ": case "TJ": case "TM": case "KZ": case "KG":
                return HANAFI;
            default:
                return SHAFI;
        }
    }

    /**
     * Coarse offline fallback, used only when reverse geocoding gives no country.
     *
     * Boxes are tested in this exact order and the first match wins. The order is
     * load-bearing: the boxes overlap, and the narrow Gulf box must be tested before
     * the wide Saudi one or Dubai (lng 55.3) would be swallowed by it.
     *
     * Deliberately coarse — this only needs to beat a blind UMM_AL_QURA when there is
     * no network. Known limits: Turkey resolves MWL+Shafi here (the country path gets
     * Hanafi right), and Dammam falls in the Gulf box rather than Saudi.
     */
    public static Defaults forCoordinates(double lat, double lng) {
        if (in(lat, 22, 27) && in(lng, 50, 60)) return new Defaults("DUBAI", SHAFI);
        if (in(lat, 16, 32) && in(lng, 34, 56)) return new Defaults("UMM_AL_QURA", SHAFI);
        if (in(lat, 5, 38) && in(lng, 60, 92)) return new Defaults("KARACHI", HANAFI);
        if (in(lat, 20, 38) && in(lng, -18, 36)) return new Defaults("EGYPTIAN", SHAFI);
        if (in(lat, -11, 8) && in(lng, 94, 142)) return new Defaults("SINGAPORE", SHAFI);
        if (in(lat, 15, 72) && in(lng, -170, -50)) return new Defaults("NORTH_AMERICA", SHAFI);
        return new Defaults(MWL, SHAFI);
    }

    private static boolean in(double v, double min, double max) {
        return v >= min && v <= max;
    }

    /**
     * Whether an automatic pick should be (re)applied: true only when the newly
     * resolved key is usable AND differs from the one last applied. This is what
     * stops routine fixes in the user's home country rewriting their settings, and
     * stops a failed resolution wiping a good saved pick.
     */
    public static boolean shouldReapply(String lastApplied, String resolved) {
        if (resolved == null || resolved.trim().isEmpty()) {
            return false;
        }
        String previous = lastApplied == null ? "" : lastApplied.trim();
        return !resolved.trim().equalsIgnoreCase(previous);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:testMadaniDebugUnitTest --tests '*PrayerLocaleDefaultsTest*'`
Expected: PASS — 11 tests, 0 failures.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/athan/PrayerLocaleDefaults.java \
        app/src/test/java/com/medoapps/www/onlinequran/athan/PrayerLocaleDefaultsTest.java
git commit -m "feat(athan): derive calculation method and madhab from location

Pure country->method/madhab table with a coarse lat/lng fallback for when
reverse geocoding has no network. No behaviour change yet - nothing calls it."
```

---

### Task 2: `PrayerSettings` prefs + `LocationApplier` funnel

**Files:**
- Modify: `app/src/main/java/com/medoapps/www/onlinequran/athan/PrayerSettings.java` (append a new section before the `// ----- hijri` section)
- Create: `app/src/main/java/com/medoapps/www/onlinequran/athan/LocationApplier.java`

**Interfaces:**
- Consumes from Task 1: `PrayerLocaleDefaults.forCountry`, `.forCoordinates`, `.shouldReapply`, `Defaults.method`, `Defaults.madhab`.
- Produces, relied on by Tasks 3, 4 and 5:
  - `PrayerSettings.isAutoMethodEnabled(Context)` / `setAutoMethodEnabled(Context, boolean)`
  - `PrayerSettings.getAutoMethodCountry(Context)` / `setAutoMethodCountry(Context, String)`
  - `LocationApplier.apply(Context context, double lat, double lng, String city, String countryCode)`

- [ ] **Step 1: Add the two pref pairs to `PrayerSettings`**

Insert immediately after the existing `setLocation(...)` method and before the `// ----------------------------------------------------------------- hijri` comment:

```java
    // ------------------------------------------------- automatic method pick

    /**
     * Whether the calculation method and madhab should follow the detected location.
     *
     * Deliberately distinct from {@link #getLocationMode}: that decides whether the
     * location is tracked from the device at all, this decides whether the method
     * follows it. A user can track their location automatically while pinning a
     * calculation method they trust.
     */
    public static boolean isAutoMethodEnabled(Context c) {
        return prefs(c).getBoolean("auto_method", true);
    }

    public static void setAutoMethodEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean("auto_method", enabled).apply();
    }

    /**
     * The country (or, offline, the resolved method name) the automatic pick was last
     * applied for. Used to rewrite the method only when the user actually moves.
     */
    public static String getAutoMethodCountry(Context c) {
        return prefs(c).getString("auto_method_country", "");
    }

    public static void setAutoMethodCountry(Context c, String country) {
        prefs(c).edit().putString("auto_method_country", country == null ? "" : country).apply();
    }
```

- [ ] **Step 2: Create `LocationApplier`**

Create `app/src/main/java/com/medoapps/www/onlinequran/athan/LocationApplier.java`:

```java
package com.medoapps.www.onlinequran.athan;

import android.content.Context;

import java.util.Locale;

/**
 * The single place a location fix is turned into athan settings.
 *
 * Every screen that obtains a fix — onboarding, {@code PrayerTimesActivity} and
 * {@code AthanSettingsActivity} — funnels through {@link #apply}, so the automatic
 * calculation-method pick behaves identically everywhere, including when the user
 * travels. Adding a fourth fix site means calling this, not re-implementing it.
 */
public final class LocationApplier {

    private LocationApplier() {
    }

    /**
     * Persist a fix and, when auto-update is on and the region actually changed,
     * re-derive the calculation method and madhab from it. Always reschedules.
     *
     * @param city        best-effort locality name; empty string is fine
     * @param countryCode ISO-3166 alpha-2 from reverse geocoding. null/empty is fine —
     *                    a coarse coordinate fallback is used instead, so this still
     *                    works with no network.
     */
    public static void apply(Context context, double lat, double lng,
                             String city, String countryCode) {
        // these writes outlive whatever screen requested the fix
        Context c = context.getApplicationContext();
        PrayerSettings.setLocation(c, lat, lng, city);

        if (PrayerSettings.isAutoMethodEnabled(c)) {
            boolean geocoded = countryCode != null && !countryCode.trim().isEmpty();
            PrayerLocaleDefaults.Defaults defaults = geocoded
                    ? PrayerLocaleDefaults.forCountry(countryCode)
                    : PrayerLocaleDefaults.forCoordinates(lat, lng);
            // Key the did-it-change check on the country when we have one, else on the
            // resolved method — so an offline fix also only writes once per region
            // instead of on every single fix.
            String key = geocoded
                    ? countryCode.trim().toUpperCase(Locale.US)
                    : defaults.method;
            if (PrayerLocaleDefaults.shouldReapply(PrayerSettings.getAutoMethodCountry(c), key)) {
                PrayerSettings.setCalculationMethod(c, defaults.method);
                PrayerSettings.setMadhab(c, defaults.madhab);
                PrayerSettings.setAutoMethodCountry(c, key);
            }
        }

        AthanScheduler.rescheduleAll(c);
    }
}
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:compileMadaniDebugJavaWithJavac`
Expected: `BUILD SUCCESSFUL`. (Warnings about obsolete Java 8 source/target are pre-existing and expected.)

- [ ] **Step 4: Verify Task 1's tests still pass**

Run: `./gradlew :app:testMadaniDebugUnitTest`
Expected: PASS, no regressions.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/athan/PrayerSettings.java \
        app/src/main/java/com/medoapps/www/onlinequran/athan/LocationApplier.java
git commit -m "feat(athan): single funnel for applying a location fix

LocationApplier persists the fix, re-derives method+madhab when the region
changed and auto-update is on, then reschedules. Adds the auto_method pref
(default on), kept separate from LOCATION_AUTO which only governs tracking."
```

---

### Task 3: Onboarding state + gateway seam

**Files:**
- Modify: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingState.java`
- Modify: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/FeatureGateway.java`
- Modify: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/AndroidFeatureGateway.java`
- Modify: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingFeatureController.java`
- Test: `app/src/test/java/com/medoapps/www/onlinequran/onboarding/OnboardingFeatureControllerTest.java`

**Interfaces:**
- Consumes from Task 2: `PrayerSettings.isAutoMethodEnabled` / `setAutoMethodEnabled`.
- Produces, relied on by Task 4: `OnboardingState.autoMethodEnabled` (public boolean field, default `true`), `FeatureGateway.setAutoMethodEnabled(boolean)`, `FeatureGateway.isAutoMethodEnabled()`.

- [ ] **Step 1: Write the failing test**

In `OnboardingFeatureControllerTest.java`, add this field to `FakeGateway` (beside the existing `String pageTypeSet = null;`):

```java
        Boolean autoMethodSet = null;
```

Add these two overrides to `FakeGateway` (beside the existing `setPageType` / `currentPageType` overrides):

```java
        @Override public void setAutoMethodEnabled(boolean enabled) { autoMethodSet = enabled; }
        @Override public boolean isAutoMethodEnabled() { return false; }
```

Add this test method to the class:

```java
    @Test
    public void apply_setsAutoMethodFromState() {
        FakeGateway gw = new FakeGateway();
        OnboardingState s = OnboardingState.defaults(); // auto-method on by default
        new OnboardingFeatureController(gw).apply(s);
        assertTrue(gw.autoMethodSet);

        FakeGateway gw2 = new FakeGateway();
        OnboardingState s2 = OnboardingState.defaults();
        s2.autoMethodEnabled = false;
        new OnboardingFeatureController(gw2).apply(s2);
        assertFalse(gw2.autoMethodSet);
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testMadaniDebugUnitTest --tests '*OnboardingFeatureControllerTest*'`
Expected: FAIL — compilation error, `cannot find symbol: variable autoMethodEnabled` and `method does not override a method from its superclass`.

- [ ] **Step 3: Add the field to `OnboardingState`**

Add beside the other public fields:

```java
    public boolean autoMethodEnabled;
```

And in `defaults()`, add after the `pageType` assignment:

```java
        // Auto-update on: the point of the welcome-screen location fetch is that a new
        // user gets the right times and method without hunting through settings.
        s.autoMethodEnabled = true;
```

Also update the `defaults()` javadoc line to mention it:

```java
    /** App defaults: athan on, bubble on, all reminders off, theme follows system, auto-method on. */
```

- [ ] **Step 4: Add the pair to `FeatureGateway`**

Add after `setPageType`:

```java
    /**
     * Whether the calculation method and madhab should follow the detected location.
     * See {@code PrayerSettings#isAutoMethodEnabled}.
     */
    void setAutoMethodEnabled(boolean enabled);
```

And add to the reads block at the bottom, after `currentPageType()`:

```java
    boolean isAutoMethodEnabled();
```

- [ ] **Step 5: Implement in `AndroidFeatureGateway`**

Add after the `currentPageType()` override:

```java
    @Override
    public void setAutoMethodEnabled(boolean enabled) {
        PrayerSettings.setAutoMethodEnabled(context, enabled);
    }

    @Override
    public boolean isAutoMethodEnabled() {
        return PrayerSettings.isAutoMethodEnabled(context);
    }
```

(`PrayerSettings` is already imported in this file.)

- [ ] **Step 6: Wire it in `OnboardingFeatureController.apply`**

Add after the `gateway.setPageType(state.pageType);` line:

```java
        gateway.setAutoMethodEnabled(state.autoMethodEnabled);
```

- [ ] **Step 7: Run tests to verify they pass**

Run: `./gradlew :app:testMadaniDebugUnitTest`
Expected: PASS — the new `apply_setsAutoMethodFromState` plus all pre-existing tests.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/onboarding/ \
        app/src/test/java/com/medoapps/www/onlinequran/onboarding/OnboardingFeatureControllerTest.java
git commit -m "feat(onboarding): carry the auto-method choice through the gateway seam

OnboardingState gains autoMethodEnabled (default on) and the controller
propagates it, matching how every other onboarding side-effect is applied."
```

---

### Task 4: Welcome screen — fetch on grant, show the city, offer the toggle

**Files:**
- Modify: `app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingPermissionsFragment.java`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-ar/strings.xml`

**Interfaces:**
- Consumes from Task 2: `LocationApplier.apply(Context, double, double, String, String)`.
- Consumes from Task 3: `OnboardingState.autoMethodEnabled`.
- Produces: no new API — this is the user-facing wiring.

- [ ] **Step 1: Add the strings (EN)**

In `app/src/main/res/values/strings.xml`, beside the existing `onb_perm_loc_*` strings:

```xml
    <string name="onb_perm_loc_auto_update">Auto-update from my location</string>
    <string name="onb_perm_loc_auto_update_why">Keep prayer times and the calculation method correct when you travel</string>
    <string name="onb_perm_loc_detecting">Detecting your location…</string>
```

- [ ] **Step 2: Add the strings (AR)**

In `app/src/main/res/values-ar/strings.xml`, beside the existing `onb_perm_loc_*` strings:

```xml
    <string name="onb_perm_loc_auto_update">التحديث التلقائي حسب موقعي</string>
    <string name="onb_perm_loc_auto_update_why">تحديث مواقيت الصلاة وطريقة الحساب تلقائيًا عند السفر</string>
    <string name="onb_perm_loc_detecting">جارٍ تحديد موقعك…</string>
```

- [ ] **Step 3: Add the imports and fields to `OnboardingPermissionsFragment`**

Add these imports:

```java
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.widget.CompoundButton;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.medoapps.www.onlinequran.athan.LocationApplier;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
```

Add these fields beside the existing ones:

```java
    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource cancellationTokenSource;
    private ExecutorService executor;
    /** One fetch per granted state — refresh() runs on every resume and toggle. */
    private boolean locationFetchStarted;
    private TextView locationDetail; // the LOC row's subtitle, retitled to the city
```

- [ ] **Step 4: Give `PermRow` a handle on its subtitle**

The LOC row's subtitle must be updatable to show the detected city. Replace the `PermRow` class with:

```java
    private static final class PermRow {
        final String key;
        final TextView granted;
        final TextView grant;
        final TextView why;
        PermRow(String key, TextView granted, TextView grant, TextView why) {
            this.key = key;
            this.granted = granted;
            this.grant = grant;
            this.why = why;
        }
    }
```

Then in `addRow(...)`, leave the existing `parent.addView(row);` line exactly as it is and replace **only** the single line that follows it. Change:

```java
        rows.add(new PermRow(key, granted, grantBtn));
```

to:

```java
        rows.add(new PermRow(key, granted, grantBtn, why));
        // keep a handle on the location row's subtitle so the detected city can replace it
        if (LOC.equals(key)) {
            locationDetail = why;
        }
```

- [ ] **Step 5: Add the auto-update toggle row directly beneath the location row**

Add this method to the fragment:

```java
    /**
     * The auto-update switch, built programmatically so it can sit immediately under
     * the LOC row (the rows themselves are added in code, so XML can't position it).
     */
    private void addAutoUpdateRow(LinearLayout parent) {
        Context c = requireContext();
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(4), 0, dp(4));

        LinearLayout textCol = new LinearLayout(c);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = new TextView(c);
        name.setText(R.string.onb_perm_loc_auto_update);
        name.setTextColor(getResources().getColor(R.color.onb_text_primary));
        name.setTextSize(14);
        textCol.addView(name);

        TextView why = new TextView(c);
        why.setText(R.string.onb_perm_loc_auto_update_why);
        why.setTextColor(getResources().getColor(R.color.onb_text_secondary));
        why.setTextSize(11);
        textCol.addView(why);
        row.addView(textCol);

        MaterialSwitch toggle = new MaterialSwitch(c);
        toggle.setChecked(host.getOnboardingState().autoMethodEnabled);
        toggle.setOnCheckedChangeListener((CompoundButton b, boolean checked) ->
                host.getOnboardingState().autoMethodEnabled = checked);
        row.addView(toggle);

        parent.addView(row);
    }
```

Then in `onCreateView`, immediately after the existing LOC `addRow(...)` call, insert:

```java
        addAutoUpdateRow((LinearLayout) athanRows);
```

so the block reads:

```java
        addRow((LinearLayout) athanRows, NOTIF, R.string.onb_perm_notif_name, R.string.onb_perm_notif_why);
        addRow((LinearLayout) athanRows, LOC, R.string.onb_perm_loc_name, R.string.onb_perm_loc_why);
        addAutoUpdateRow((LinearLayout) athanRows);
        addRow((LinearLayout) athanRows, EXACT, R.string.onb_perm_exact_name, R.string.onb_perm_exact_why);
        addRow((LinearLayout) athanRows, BATT, R.string.onb_perm_batt_name, R.string.onb_perm_batt_why);
```

- [ ] **Step 6: Initialise the location client and trigger the fetch on grant**

In `onCreate(Bundle)`, after the `runtimeLauncher` assignment, add:

```java
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        cancellationTokenSource = new CancellationTokenSource();
        executor = Executors.newSingleThreadExecutor();
```

At the end of `refresh()`, add:

```java
        // Permission granted is not the same as location known: fetch the fix here so a
        // user who never opens Prayer Times still gets their own times, not Makkah's.
        if (isGranted(LOC) && !locationFetchStarted) {
            locationFetchStarted = true;
            if (locationDetail != null) {
                locationDetail.setText(R.string.onb_perm_loc_detecting);
            }
            fetchLocation();
        }
```

- [ ] **Step 7: Implement the fetch**

Add these methods to the fragment. This mirrors the proven `PrayerTimesActivity.fetchLocation()` pattern:

```java
    /**
     * Prefer a real current fix over the often-stale cached one; accept a recent cached
     * fix (≤5 min) immediately, otherwise wait up to 10 s, then fall back to the last
     * known location — a stale fix still beats the engine's Makkah fallback.
     */
    private void fetchLocation() {
        if (!isGranted(LOC)) return;
        int priority = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                ? Priority.PRIORITY_HIGH_ACCURACY
                : Priority.PRIORITY_BALANCED_POWER_ACCURACY;

        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(priority)
                .setMaxUpdateAgeMillis(5 * 60 * 1000)
                .setDurationMillis(10 * 1000)
                .build();

        fusedLocationClient.getCurrentLocation(request, cancellationTokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        onLocationFix(location);
                    } else {
                        fallbackToLastLocation();
                    }
                })
                .addOnFailureListener(e -> fallbackToLastLocation());
    }

    private void fallbackToLastLocation() {
        if (!isGranted(LOC)) return;
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                onLocationFix(location);
            } else if (isAdded() && locationDetail != null) {
                // nothing to show: put the original rationale back
                locationDetail.setText(R.string.onb_perm_loc_why);
            }
        });
    }

    /**
     * Reverse-geocode off the main thread, then hand the fix to {@link LocationApplier}.
     * The write uses the application context so it completes even if the user has already
     * swiped past this page.
     */
    private void onLocationFix(Location location) {
        final double lat = location.getLatitude();
        final double lng = location.getLongitude();
        final Context appContext = requireContext().getApplicationContext();
        executor.execute(() -> {
            String city = "";
            String country = "";
            try {
                Geocoder geocoder = new Geocoder(appContext, Locale.getDefault());
                List<Address> result = geocoder.getFromLocation(lat, lng, 1);
                if (result != null && !result.isEmpty()) {
                    Address address = result.get(0);
                    if (address.getCountryCode() != null) {
                        country = address.getCountryCode();
                    }
                    if (address.getLocality() != null) {
                        city = address.getLocality();
                    } else if (address.getSubAdminArea() != null) {
                        city = address.getSubAdminArea();
                    } else if (address.getAdminArea() != null) {
                        city = address.getAdminArea();
                    }
                }
            } catch (Exception ignored) {
                // Reverse geocoding needs network and is best-effort only; LocationApplier
                // falls back to coarse coordinates when the country comes back empty.
            }
            LocationApplier.apply(appContext, lat, lng, city, country);

            final String label = city.isEmpty()
                    ? String.format(Locale.US, "%.3f, %.3f", lat, lng)
                    : city;
            final View view = getView();
            if (view != null) {
                view.post(() -> {
                    if (isAdded() && locationDetail != null) {
                        locationDetail.setText(label);
                    }
                });
            }
        });
    }
```

- [ ] **Step 8: Clean up on destroy**

Add to the fragment:

```java
    @Override
    public void onDestroyView() {
        // the fetch may outlive the page; stop it rather than leak a callback into a dead view
        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (executor != null) {
            executor.shutdownNow();
        }
        super.onDestroy();
    }
```

- [ ] **Step 9: Verify it compiles**

Run: `./gradlew :app:compileMadaniDebugJavaWithJavac`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 10: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/onboarding/OnboardingPermissionsFragment.java \
        app/src/main/res/values/strings.xml app/src/main/res/values-ar/strings.xml
git commit -m "feat(onboarding): fetch the location on grant and show the detected city

Granting the permission no longer just unlocks the Enter-app gate - the page
now fetches a fix, funnels it through LocationApplier so the calculation
method follows it, and retitles the row to the detected city. Adds the
auto-update switch directly beneath that row."
```

---

### Task 5: Route the existing screens through the funnel + settings toggle

**Files:**
- Modify: `app/src/main/java/com/medoapps/www/onlinequran/PrayerTimesActivity.java:330-357` (`onLocationFix`)
- Modify: `app/src/main/java/com/medoapps/www/onlinequran/AthanSettingsActivity.java` (`applyLocation`, `reverseGeocodeAndSave`, `setupLocationSection`)
- Modify: `app/src/main/res/layout/activity_athan_settings.xml`

**Interfaces:**
- Consumes from Task 2: `LocationApplier.apply(Context, double, double, String, String)`, `PrayerSettings.isAutoMethodEnabled` / `setAutoMethodEnabled`.
- Consumes from Task 4: `R.string.onb_perm_loc_auto_update`, `R.string.onb_perm_loc_auto_update_why` (reused here so the wording and its Arabic stay in one place).
- Produces: nothing new.

- [ ] **Step 1: Route `PrayerTimesActivity.onLocationFix` through the funnel**

Add the import:

```java
import com.medoapps.www.onlinequran.athan.LocationApplier;
```

In `onLocationFix`, capture the country code and replace the two-line persist. The body of the `try` block gains a country capture, and the `setLocation` + `rescheduleAll` pair becomes one `LocationApplier.apply` call:

```java
    private void onLocationFix(Location location) {
        final double lat = location.getLatitude();
        final double lng = location.getLongitude();
        executor.execute(() -> {
            String city = "";
            String country = "";
            try {
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> result = geocoder.getFromLocation(lat, lng, 1);
                if (result != null && !result.isEmpty()) {
                    Address address = result.get(0);
                    if (address.getCountryCode() != null) {
                        country = address.getCountryCode();
                    }
                    if (address.getLocality() != null) {
                        city = address.getLocality();
                    } else if (address.getSubAdminArea() != null) {
                        city = address.getSubAdminArea();
                    } else if (address.getAdminArea() != null) {
                        city = address.getAdminArea();
                    }
                }
            } catch (Exception ignored) {
                // Reverse geocoding is best-effort only.
            }
            // one funnel for every fix: persists, re-derives the method on a real move,
            // and reschedules
            LocationApplier.apply(getApplicationContext(), lat, lng, city, country);
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) renderAll();
            });
        });
    }
```

- [ ] **Step 2: Route `AthanSettingsActivity` through the funnel**

Add the import:

```java
import com.medoapps.www.onlinequran.athan.LocationApplier;
```

Replace `applyLocation` with a country-aware version:

```java
    private void applyLocation(double lat, double lng, String city, String countryCode) {
        // one funnel for every fix; a null/empty country falls back to coarse coordinates
        LocationApplier.apply(this, lat, lng, city, countryCode);
        updateCityLabel();
        Toast.makeText(this, R.string.athan_location_updated, Toast.LENGTH_SHORT).show();
    }
```

Update `reverseGeocodeAndSave` to capture and pass the country:

```java
    /** Best-effort locality lookup; falls back to an empty city name. */
    private void reverseGeocodeAndSave(final double lat, final double lng) {
        executor.execute(() -> {
            String city = "";
            String country = "";
            try {
                List<Address> results = new Geocoder(this, Locale.getDefault()).getFromLocation(lat, lng, 1);
                if (results != null && !results.isEmpty()) {
                    Address address = results.get(0);
                    if (address.getLocality() != null) city = address.getLocality();
                    if (address.getCountryCode() != null) country = address.getCountryCode();
                }
            } catch (Exception ignored) {
            }
            final String cityName = city;
            final String countryCode = country;
            runOnUiThread(() -> {
                if (!isFinishing()) applyLocation(lat, lng, cityName, countryCode);
            });
        });
    }
```

Find every other call to `applyLocation(` in this file (the manual city-search path in `searchCity()`) and pass `null` as the fourth argument — a manually picked city still gets a sensible method from the coordinate fallback:

```java
        applyLocation(result.lat, result.lng, result.shortName, null);
```

- [ ] **Step 3: Add the toggle to the settings layout**

In `app/src/main/res/layout/activity_athan_settings.xml`, inside the location section, immediately after the `@id/radio_location_mode` `RadioGroup`, add:

```xml
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:paddingTop="8dp"
        android:paddingBottom="8dp">

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="@string/onb_perm_loc_auto_update"
                android:textColor="@color/text_primary"
                android:textSize="14sp" />

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="@string/onb_perm_loc_auto_update_why"
                android:textColor="@color/text_secondary"
                android:textSize="11sp" />
        </LinearLayout>

        <com.google.android.material.materialswitch.MaterialSwitch
            android:id="@+id/switch_auto_method"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />
    </LinearLayout>
```

`@color/text_primary` / `@color/text_secondary` are the tokens the rest of this layout uses (19 and 6 usages respectively) and are already theme-aware — do not substitute `@color/white`, which is black at night.

- [ ] **Step 4: Bind the toggle**

At the end of `setupLocationSection()`, add:

```java
        MaterialSwitch autoMethod = findViewById(R.id.switch_auto_method);
        autoMethod.setChecked(PrayerSettings.isAutoMethodEnabled(this));
        autoMethod.setOnCheckedChangeListener((b, checked) ->
                PrayerSettings.setAutoMethodEnabled(this, checked));
```

Add the import if it is not already present:

```java
import com.google.android.material.materialswitch.MaterialSwitch;
```

- [ ] **Step 5: Verify it compiles**

Run: `./gradlew :app:compileMadaniDebugJavaWithJavac`
Expected: `BUILD SUCCESSFUL`. If it fails with `applyLocation` arity errors, a call site was missed in Step 2 — grep with `grep -n "applyLocation(" app/src/main/java/com/medoapps/www/onlinequran/AthanSettingsActivity.java` and fix each.

- [ ] **Step 6: Run the full unit suite**

Run: `./gradlew :app:testMadaniDebugUnitTest`
Expected: PASS, no regressions.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/PrayerTimesActivity.java \
        app/src/main/java/com/medoapps/www/onlinequran/AthanSettingsActivity.java \
        app/src/main/res/layout/activity_athan_settings.xml
git commit -m "feat(athan): follow the calculation method when the user moves

Prayer Times and Athan settings now route their fixes through LocationApplier
too, so auto-update works wherever the location changes - not just onboarding.
Adds the matching toggle to the settings location section."
```

---

### Task 6: End-to-end device verification

**Files:** none modified — this task is verification only.

**Interfaces:**
- Consumes: the whole feature.

- [ ] **Step 1: Install a clean build**

```bash
./gradlew :app:installMadaniDebug
adb shell pm clear com.medoapps.www.onlinequran
adb shell am start -n com.medoapps.www.onlinequran/.SplashScreen
```

(`MainActivity` is not exported — launch via `.SplashScreen`.)

- [ ] **Step 2: Seed a Cairo location and walk onboarding**

```bash
adb emu geo fix 31.24 30.04
```

Advance to the permissions page, grant location. Expected: the location row shows "Detecting your location…" then flips to the detected city, and the auto-update switch beneath it reads ON.

- [ ] **Step 3: Confirm the Cairo settings landed**

```bash
adb shell run-as com.medoapps.www.onlinequran cat shared_prefs/athan_settings.xml
```

Expected: `calculation_method` = `EGYPTIAN`, `madhab` = `SHAFI`, `auto_method` = `true`, `auto_method_country` = `EG`, and `lat`/`lng` present.

- [ ] **Step 4: Confirm the method follows a move**

```bash
adb emu geo fix 67.01 24.86
```

Open Prayer Times, then re-dump prefs.
Expected: `calculation_method` = `KARACHI`, `madhab` = `HANAFI`, `auto_method_country` = `PK`.

- [ ] **Step 5: Confirm the toggle actually pins the method**

In Athan settings turn the auto-update switch OFF, then:

```bash
adb emu geo fix 31.24 30.04
```

Open Prayer Times and re-dump prefs.
Expected: `lat`/`lng` update to Cairo but `calculation_method` STAYS `KARACHI` and `madhab` stays `HANAFI` — the pin held.

- [ ] **Step 6: Verify both themes and both locales**

Check the new onboarding row and the new settings row in light AND dark, and in EN AND AR (project standing rule). In AR confirm the switch mirrors to the correct side and neither label is clipped.

```bash
adb shell "cmd uimode night yes"   # dark
adb shell "cmd uimode night no"    # light
```

- [ ] **Step 7: Commit any fixes**

If steps 2–6 surfaced layout or contrast problems, fix them and commit:

```bash
git commit -am "fix(onboarding): <what the device pass turned up>"
```

If nothing needed fixing, there is nothing to commit — say so rather than inventing a commit.

---

## Notes for the implementer

- **Do not** reuse `PrayerSettings.LOCATION_AUTO` for the new toggle. They are intentionally different concepts: `LOCATION_AUTO` = "track my location"; `auto_method` = "let the method follow it". A user may want the first without the second.
- **Do not** call `PrayerSettings.setLocation(...)` directly in new code. Everything goes through `LocationApplier.apply(...)`; that is the whole point of Task 2.
- The `auto_method_country` check is what keeps this from fighting the user. Without it, every routine fix would rewrite the method and a manual choice would never survive.
- Reverse geocoding requires network. The coordinate fallback exists precisely so a first run on a plane still beats a blind `UMM_AL_QURA`; do not "simplify" it away.
