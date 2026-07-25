# Onboarding — auto-location + auto calculation method

**Date:** 2026-07-25
**Status:** Design approved-pending
**Scope:** `onboarding/` package, `athan/` package, `AthanSettingsActivity`, `PrayerTimesActivity`

## Problem

The welcome flow already *asks for* the location permission (`OnboardingPermissionsFragment`,
the `LOC` row added by the feature-gated permissions work), but it never *uses* it:

- Nothing fetches a fix during onboarding. `PrayerSettings.setLocation()` is only ever called
  from `PrayerTimesActivity.onLocationFix()` and `AthanSettingsActivity`. A new user therefore
  grants location, finishes onboarding, and still gets **Makkah** prayer times (the
  `PrayerSettings.getLatitude()` fallback, 21.4225/39.8262) until they happen to open the
  Prayer Times screen.
- The calculation method is a **fixed** `UMM_AL_QURA` default for everyone
  (`PrayerSettings.getCalculationMethod`). A user in Cairo, Karachi or Toronto gets Makkah's
  method, which shifts Fajr/Isha noticeably. Nothing anywhere derives it from location.
- Madhab is likewise a fixed `SHAFI` default. Asr differs by up to ~1 hour between madhabs,
  so this is very visible for Hanafi-majority regions.

Goal: on the welcome screen, acquire the device location, apply it to the athan times, and
select the calculation method (and madhab) automatically from where the user actually is.

## Decisions

| Question | Decision |
|---|---|
| When to fetch | **Immediately on grant**, in the permissions page — not deferred to "Enter app" |
| Override rule | Re-apply on location change, **gated by a new auto-update toggle** |
| Data model | **Separate new flag**, independent of the existing `LOCATION_AUTO` mode |
| Toggle default | **ON** |
| Madhab | **Also auto-selected** (Hanafi for South/Central Asia + Turkey) |
| Visibility | **Show the detected city** in the location row |
| Toggle placement | Permissions page next to the location row, **and** Athan settings |
| Offline | **Coarse lat/lng fallback** when `Geocoder` fails |
| Refactor reach | **All three screens** funnel through the new applier |

Note the toggle is deliberately *separate* from `PrayerSettings.LOCATION_AUTO`: that mode
controls whether the location is tracked from the device at all, whereas the new flag controls
whether **method/madhab follow the location**. Keeping them apart lets a user track their
location automatically while pinning a calculation method they trust.

## Architecture

### 1. `athan/PrayerLocaleDefaults.java` — new, pure

The core of the feature and the only part with real domain knowledge. No Android
dependencies, so it is directly unit-testable.

```java
public final class PrayerLocaleDefaults {
    public static final class Defaults {
        public final String method;   // CalculationMethod enum name
        public final String madhab;   // "SHAFI" | "HANAFI"
    }
    public static Defaults forCountry(String iso2);              // primary
    public static Defaults forCoordinates(double lat, double lng); // offline fallback
}
```

**Country → method** (ISO-3166 alpha-2, from `Address.getCountryCode()`):

| Countries | Method |
|---|---|
| `SA` | `UMM_AL_QURA` |
| `AE`, `OM`, `BH` | `DUBAI` |
| `KW` | `KUWAIT` |
| `QA` | `QATAR` |
| `EG`, `LY`, `DZ`, `TN`, `MA`, `SD`, `SY`, `IQ`, `JO`, `LB`, `YE` | `EGYPTIAN` |
| `PK`, `IN`, `BD`, `AF`, `LK` | `KARACHI` |
| `SG`, `MY`, `ID`, `BN` | `SINGAPORE` |
| `US`, `CA`, `MX` | `NORTH_AMERICA` |
| *(anything else, incl. TR + Europe)* | `MUSLIM_WORLD_LEAGUE` |

**Country → madhab:** `HANAFI` for `PK`, `IN`, `BD`, `AF`, `TR`, `UZ`, `TJ`, `TM`, `KZ`, `KG`;
`SHAFI` otherwise.

**Offline fallback** (`forCoordinates`) — coarse bounding boxes used only when reverse
geocoding yields no country code. **Evaluated top-to-bottom in exactly the order below; first
match wins.** Order is load-bearing: the boxes overlap, and the narrower Gulf box must be
tested before the wide Saudi one or Dubai (lng 55.3) would be swallowed by it.

| # | Box (lat / lng) | Result |
|---|---|---|
| 1 | 22..27 / 50..60 | `DUBAI` |
| 2 | 16..32 / 34..56 | `UMM_AL_QURA` |
| 3 | 5..38 / 60..92 | `KARACHI` + `HANAFI` |
| 4 | 20..38 / −18..36 | `EGYPTIAN` |
| 5 | −11..8 / 94..142 | `SINGAPORE` |
| 6 | 15..72 / −170..−50 | `NORTH_AMERICA` |
| — | *(no match)* | `MUSLIM_WORLD_LEAGUE` |

Worked check: Makkah (21.4/39.8)→2, Dubai (25.2/55.3)→1, Cairo (30.0/31.2)→4,
Karachi (24.9/67.0)→3, Jakarta (−6.2/106.8)→5, Toronto (43.7/−79.4)→6, London→default.

These are intentionally coarse — they exist so a first run with no network still beats a blind
`UMM_AL_QURA`, not to be authoritative. The geocoded country always wins when available. Known
limitation: Turkey resolves to `MWL`+`SHAFI` on this path (the country path correctly gives
`HANAFI`), and Dammam falls in the Gulf box rather than Saudi. Both are acceptable for a
network-less fallback.

### 2. `athan/PrayerSettings.java` — two new prefs

```java
boolean isAutoMethodEnabled(Context)          // key "auto_method", default TRUE
void    setAutoMethodEnabled(Context, boolean)
String  getAutoMethodCountry(Context)         // key "auto_method_country", default ""
void    setAutoMethodCountry(Context, String)
```

`auto_method_country` records the country the auto-pick last applied for. Method/madhab are
only rewritten when the resolved country **differs** from it — so routine fixes in the user's
home city never clobber anything, and the write happens exactly once per genuine move.

### 3. `athan/LocationApplier.java` — new, the single funnel

Every "a location fix landed" path in the app routes through this:

```java
public static void apply(Context c, double lat, double lng, String city, String countryCode)
```

1. `PrayerSettings.setLocation(c, lat, lng, city)` (unchanged existing contract)
2. If `isAutoMethodEnabled(c)`, ask `PrayerLocaleDefaults.resolve(storedKey, countryCode, lat, lng)`
   what to write; when it returns non-null, `setCalculationMethod` + `setMadhab` +
   `setAutoMethodCountry`
3. `AthanScheduler.rescheduleAll(c)`

The applier is deliberately nothing but read/write glue — the whole decision lives in the pure
class so it is unit-testable without Robolectric.

**The two key namespaces (learned the hard way).** `auto_method_country` holds either a geocoded
ISO country or a coordinate-derived key, and the two MUST stay distinguishable — hence the
`coord:` prefix on the latter. An un-namespaced coordinate key alternates with the country key
for the same place (Amman: `JO`→EGYPTIAN online vs the Saudi box→UMM_AL_QURA offline; Antalya:
`TR`→MWL+HANAFI vs the Egyptian box→EGYPTIAN+SHAFI), and every alternation rewrites the
settings — flipping prayer times, and in Turkey's case swinging Asr by roughly an hour, purely
with connectivity. `resolve()` therefore also refuses to let a network-less fix downgrade an
already-known country: the boxes serve a cold start only.

This is why the refactor reaches all three screens: auto-update-on-travel only works if the
existing screens' fixes go through the same funnel.

### 4. Onboarding wiring

- `OnboardingState`: new `autoMethodEnabled` field, default `true`.
- `FeatureGateway`: add `setAutoMethodEnabled(boolean)` + `isAutoMethodEnabled()`, matching the
  existing read/write seam. `AndroidFeatureGateway` delegates to `PrayerSettings`; the test
  fake in `OnboardingFeatureControllerTest` gains the same pair.
- `OnboardingFeatureController.apply()`: one added `gateway.setAutoMethodEnabled(...)` call.
- `OnboardingPermissionsFragment`:
  - Location fetch on grant. The existing `runtimeLauncher` callback already calls `refresh()`;
    hook the fetch there, guarded so it runs once per granted state.
  - Fetch mirrors the proven `PrayerTimesActivity` approach: `FusedLocationProviderClient` with
    a `CurrentLocationRequest` (`PRIORITY_HIGH_ACCURACY` when fine location is granted, else
    `PRIORITY_BALANCED_POWER_ACCURACY`; `maxUpdateAge` 5 min, `duration` 10 s), falling back to
    `getLastLocation()`, then `Geocoder` off the main thread for city + country code.
  - The `LOC` row's subtitle becomes the detected city once resolved.
  - A `MaterialSwitch` for auto-update sits directly beneath the location row, bound to
    `OnboardingState.autoMethodEnabled` **and written straight to the pref on toggle**. The
    state-only write is not enough: the fetch happens on this very page and `LocationApplier`
    reads the pref, so a user who switches auto-update off and then grants location would
    still get a method applied — and the seeded `auto_method_country` would make it permanent.
  - `WelcomeActivity` seeds `autoMethodEnabled` from the gateway alongside the other toggles,
    so a re-run reflects a user who previously turned it off.

### 5. `AthanSettingsActivity`

- Same auto-update `MaterialSwitch`, in the existing location section next to the
  auto/manual mode control.
- Its existing `PrayerSettings.setLocation(...)` call site routes through `LocationApplier`.
- After a fix, the method spinner and madhab radio are re-synced from the stored settings.
  Without that they keep showing the pre-fix pick while the prefs say otherwise, and the user
  cannot re-select the value on screen (the listeners no-op when it already matches). Both
  listeners short-circuit on an unchanged value, so the re-sync cannot loop or reschedule.
- `CityResult` carries the geocoded country so a manually searched city picks its real method
  instead of falling back to the coarse boxes (Nominatim supplies none, so that path stays `""`).

### 6. `PrayerTimesActivity`

`onLocationFix()` already geocodes; extend it to capture `Address.getCountryCode()` and call
`LocationApplier.apply(...)` in place of its current `setLocation` + `rescheduleAll` pair.

## Error handling

- **Permission denied** — unchanged current behaviour: no fetch, engine falls back to Makkah.
  The permissions page already lets the user disable Athan instead of granting, so nobody is
  trapped.
- **No fix within 10 s** — fall back to `getLastLocation()`; if that is also null, leave
  settings untouched and let the existing Prayer Times fetch retry later.
- **Geocoder throws / returns nothing** (offline, throttled) — city stays empty, country falls
  back to `forCoordinates(lat, lng)`. Reverse geocoding stays strictly best-effort, as today.
- **Fragment detached mid-fetch** — the callbacks must null-check the view and no-op; the fetch
  outlives the page if the user swipes on. The settings write is context-based
  (`requireContext().getApplicationContext()`) so it completes regardless.
- **Unknown/garbage country code** — `PrayerLocaleDefaults` returns the
  `MUSLIM_WORLD_LEAGUE`/`SHAFI` default rather than throwing.

## Files

**New**
- `app/src/main/java/com/medoapps/www/onlinequran/athan/PrayerLocaleDefaults.java`
- `app/src/main/java/com/medoapps/www/onlinequran/athan/LocationApplier.java`
- `app/src/test/java/com/medoapps/www/onlinequran/athan/PrayerLocaleDefaultsTest.java`

**Modified**
- `athan/PrayerSettings.java` — two new pref pairs
- `onboarding/OnboardingState.java`, `FeatureGateway.java`, `AndroidFeatureGateway.java`,
  `OnboardingFeatureController.java` — the `autoMethodEnabled` seam
- `onboarding/OnboardingPermissionsFragment.java` — fetch, city subtitle, toggle
- `AthanSettingsActivity.java` — route through `LocationApplier` **and** add the auto-update toggle
- `PrayerTimesActivity.java` — route through `LocationApplier` only (no toggle on this screen)
- `res/values/strings.xml` + `res/values-ar/strings.xml` — toggle label + description
- `app/src/test/java/com/medoapps/www/onlinequran/onboarding/OnboardingFeatureControllerTest.java`
  — fake gateway gains the new pair

## Verification

**Unit**
- `PrayerLocaleDefaultsTest` — table-drives the country map (SA→UMM_AL_QURA, EG→EGYPTIAN,
  PK→KARACHI+HANAFI, US→NORTH_AMERICA, unknown→MWL+SHAFI), plus the coordinate fallback and
  a null/empty country code.
- `OnboardingFeatureControllerTest` — asserts `apply()` propagates `autoMethodEnabled` both ways.
- `./gradlew :app:testMadaniDebugUnitTest`

**Build**
- `./gradlew :app:compileMadaniDebugJavaWithJavac`

**On device** (emulator, per the project's usual `.SplashScreen` launch)
1. Fresh install → onboarding → permissions page → grant location.
   Confirm the row shows the detected city and the toggle reads ON.
2. `adb emu geo fix <lng> <lat>` for a Cairo and a Karachi coordinate; confirm
   `adb shell run-as … cat shared_prefs/athan_settings.xml` shows `calculation_method`
   `EGYPTIAN` / `KARACHI` and madhab `SHAFI` / `HANAFI` respectively.
3. Turn the toggle OFF, move the location again, confirm method/madhab do **not** change.
4. Verify the new row + toggle in **light and dark** and in **EN and AR** (project standing
   requirement for all new UI).
