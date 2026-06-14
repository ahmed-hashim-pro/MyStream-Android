# Modern Onboarding with Feature Control — Design

**Date:** 2026-06-14
**Branch:** `feature/advanced-athan`
**Status:** Approved design, ready for implementation planning
**App:** `com.medoapps.www.onlinequran` (My Stream)

## Summary

Rebuild the first-launch welcome flow into a modern, theme-aware onboarding that
(1) showcases the app's five feature pillars — including the two distinct Quran
features and the new athan feature — and (2) lets the user turn features on/off
during onboarding via a dedicated "Personalize" step that writes to the app's
real settings stores.

This is a **one-time onboarding** redesign (shown once, gated by the existing
`IsFirstTimeLaunch` flag). It is not a permanent re-openable hub; every toggle
persists to the same preference stores the in-app Settings screens already use,
so users change them later in Settings as they do today.

## Goals

- Replace the outdated 4-slide carousel (whose slides 2–3 still advertise a
  "share what people watch" YouTube-reaction concept) with a relevant, polished
  feature tour.
- Explain the **two separate Quran features** clearly and emphasize that both
  work **offline**:
  - **Merged Mushaf** — read *and* listen together; the verse highlights in sync
    with the recitation on the rendered Madani page.
  - **Quran by Reciters** — listen-only; 150+ qaris, stream or download surahs.
- Surface the new **athan** feature and let the user enable it (plus the seven
  notification reminders) right from onboarding.
- Modern, "fancy" visual treatment that adapts to light/dark.
- Full RTL/LTR correctness (bilingual Arabic/English app).

## Non-goals

- No permanent/re-openable "Features dashboard" surface. (Considered and
  rejected during brainstorming — scope is onboarding only.)
- No change to `SplashScreen`, `MainActivity`, or the underlying settings
  screens (`AthanSettingsActivity`, `NotificationSettingsActivity`).
- No migration to Jetpack Compose. The app is XML Views + Material 3; we stay
  consistent with it.
- No new athan/notification scheduling logic — we reuse the existing schedulers.

## Visual direction

- **Light mode — "Warm Gold Glass":** cream/gold radial background, frosted-glass
  cards, soft gold shadows. On-brand with the current `madani` gold/cream palette,
  elevated and airier.
- **Dark mode — "Midnight Premium":** deep navy radial background, gold glow halo,
  gradient-gold headings.
- The onboarding follows the active day/night mode (driven by the user's theme
  choice / system setting). Implemented with `values/` + `values-night/` resources.
- Rainbow-gradient logo and bilingual accents retained throughout.

## Flow & screens

`SplashScreen` (unchanged) → **`WelcomeActivity`** (rebuilt) → `MainActivity`.

`WelcomeActivity` hosts a single **`ViewPager2`** with 8 ordered steps:

| #   | Step          | Content |
|-----|---------------|---------|
| 0   | **Intro**     | Branded animated hero, app name, bilingual tagline, "Get Started". |
| 1   | **Tour: Merged Mushaf** | "Read & listen, even offline." Page mock with a synced-highlighted ayah + play badge. "⤓ Available offline" badge. |
| 2   | **Tour: Quran by Reciters** | "150+ reciters, online or offline." Reciter list with download ticks. Offline badge. |
| 3   | **Tour: Radio** | Live Quran radio stations. |
| 4   | **Tour: Athan & Prayer Times** | "Never miss a prayer." Headline new feature — accurate offline prayer times + real athan. |
| 5   | **Tour: Islamic Tools** | Qibla, Tasbih, Athkar, Zakat, 99 Names, Duas & more. |
| 6   | **Personalize** | Scrollable feature-setup (see below). |
| 7   | **Ready**     | Contextual permission requests, then "Enter app". |

Tour steps (1–5) share chrome: framed illustration, title, description, **Skip**
link (jumps to finish), page-dot indicator, and a circular **Next** button that
becomes **Start** on step 5. Steps 0–5 are swipeable; steps 6–7 advance via
buttons (swipe-to-leave still allowed forward).

### Personalize step (step 6)

Scrollable page, three sections in this order:

1. **Prayer Times**
   - **Athan & Prayer Alarms** — prominent "hero" master toggle.
   - When on, a dashed shortcut row **"Set your location & athan voice →"**
     launches `AthanSettingsActivity`.
2. **Daily Reminders** — all seven listed individually, each its own toggle:
   Daily Ayah, Morning Athkar, Evening Athkar, Dua of the Day, Daily Hadith,
   Asmaul Husna, Suhoor Reminder.
3. **Appearance** — Light / Dark / System segmented selector.

Sticky **Continue** button advances to Ready.

**Default states** (match the app's current shipping defaults so onboarding never
silently spams notifications):

- Athan: **on** (`athan_feature_enabled` already defaults `true`).
- All 7 reminders: **off** (the notification schedulers ship disabled today).
- Theme: **System**.

## Architecture

- **`WelcomeActivity`** (rewritten) + new `activity_welcome.xml`: hosts
  `ViewPager2` + a custom dot indicator + Skip/Next/Continue controls.
- **`FragmentStateAdapter`** over `ViewPager2`. `ViewPager2` is chosen over the
  current `androidx.viewpager.widget.ViewPager` specifically for native RTL
  support and modern paging.
- Fragments:
  - `OnboardingIntroFragment` — step 0.
  - `OnboardingTourFragment` — data-driven; one instance per pillar, configured
    via arguments (title/desc/art/offline-badge/string keys). Renders steps 1–5.
  - `OnboardingPersonalizeFragment` — step 6; folds in the existing
    `ThemesFragment` theme logic for the Appearance section.
  - `OnboardingReadyFragment` — step 7; permission requests + finish.
- A small plain (testable) class **`OnboardingPreferences`** (helper, no Android
  UI deps where avoidable) maps the final toggle state to the concrete writes
  and scheduler calls (see Data flow). This is the unit-tested seam.

## Data flow (toggles → real settings)

On Continue/Finish, the chosen state is committed to the **existing** stores —
no new preference schema:

- **Athan** → `PrayerSettings.setAthanFeatureEnabled(context, enabled)` followed
  by `AthanScheduler.rescheduleAll(...)` (the same call `AthanSettingsActivity`
  uses).
- **7 reminders** → reuse the exact per-reminder enable + schedule path that
  `NotificationSettingsActivity` already performs (each reminder's boolean pref
  + its `AlarmManager.setInexactRepeating` scheduler). Implementation must call
  the same code, not duplicate scheduling logic. *(Implementation note: confirm
  the precise pref keys and scheduler entry points in
  `NotificationSettingsActivity` during planning and reuse them directly.)*
- **Theme** → `AppCompatDelegate.setDefaultNightMode(...)` + the persistence the
  current `ThemesFragment` performs.
- **Athan location/voice shortcut** → `startActivity(AthanSettingsActivity)`.
- **First-launch flag** → `PreferenceManager` `IsFirstTimeLaunch = false` on both
  finish and Skip (preserving current behavior).

## Permissions (Ready step, contextual)

Requested only as the chosen toggles require them; never block finishing:

- **`POST_NOTIFICATIONS`** (Android 13+) — requested if athan or any reminder is
  enabled.
- **Exact alarms** — if athan is enabled, route through the athan feature's
  existing grant flow, which already checks `canScheduleExactAlarms()` and falls
  back to a Doze-proof inexact alarm when denied (fixed 2026-06-13). No
  unconditional `setExact*` calls are introduced.
- **Location** — deferred to the athan location/voice shortcut and the existing
  auto (fused + reverse-geocode) / manual-city flow. Not requested inline; not
  blocking.

## Error handling

- If the user denies notification permission, toggles still persist and the
  feature schedules as today; it simply won't post notifications until granted —
  matching existing app behavior. No crash, no dead-end.
- Exact-alarm denial is already handled by the athan scheduler's fallback.
- The "Set location & athan voice" shortcut is optional; skipping it leaves the
  athan feature on with its existing location-resolution defaults.

## RTL / LTR

- `ViewPager2` provides correct RTL paging; swipe direction follows layout
  direction (RTL advances right-to-left, consistent with the rest of the app).
- All layouts use `start`/`end` (never `left`/`right`); chevrons/arrows set
  `android:autoMirrored="true"`.
- The page-dot indicator and Next/Skip placement mirror under RTL.
- Every user-facing string added to **both** `values/strings.xml` (en) and
  `values-ar/strings.xml` (ar). No hardcoded text. Arabic copy for each tour
  pillar and Personalize label included.

## Cleanup

- Replace `WelcomeActivity.java` and `activity_welcome.xml`.
- Retire the obsolete `slide_screen1.xml`–`slide_screen4.xml` (and the
  `layout-v14` slide variant) once the new fragments replace them.
- `SplashScreen`, `MainActivity`, `AthanSettingsActivity`,
  `NotificationSettingsActivity`, `ThemesFragment` core logic: unchanged
  (`ThemesFragment`'s theme code is reused, not deleted, unless fully folded in).

## Testing

- **Unit:** `OnboardingPreferences` — given a toggle state, asserts the correct
  pref writes and that the athan reschedule / reminder-schedule entry points are
  invoked (with the schedulers faked/verified).
- **Instrumented (Espresso):**
  - Full navigation Intro → Tour ×5 → Personalize → Ready → finish; assert
    `IsFirstTimeLaunch` is set false and `MainActivity` launches.
  - Skip from a tour step finishes and sets the flag.
  - One run under an Arabic locale to verify RTL layout direction and mirrored
    controls render without clipping.
- Manual on-device check on the API 35 emulator in both light and dark, en + ar,
  confirming athan + a reminder actually schedule after onboarding.

## Open implementation details (resolve during planning)

- Exact pref keys + scheduler method signatures in `NotificationSettingsActivity`
  to reuse for the 7 reminders.
- Whether to fully fold `ThemesFragment` into the Personalize section or embed it.
- Final illustration assets for the 5 tour pillars (mock art used in design;
  production may use existing app vectors/logo).
