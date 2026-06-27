# Onboarding — feature-gated permissions (Athan + Bubble)

**Date:** 2026-06-27
**Status:** Design approved-pending
**Scope:** Welcome / onboarding flow only (`WelcomeActivity` + `onboarding/` package)

## Problem

The welcome flow already requests `POST_NOTIFICATIONS` (gated on the Athan/reminder
toggles) and shows an exact-alarm settings link when Athan is on. But three feature
permissions are never surfaced during onboarding and are only requested in-feature later:

- **Location** — Athan / prayer-time calculation needs it (today requested only inside
  `AthanSettingsActivity` / `PrayerTimesActivity` / `QiblaActivity`).
- **Overlay / draw-over-apps** (`SYSTEM_ALERT_WINDOW`) — the **Floating Athkar Bubble**
  needs it (today requested only inside `BubbleSettingsActivity`). Onboarding has no bubble
  awareness at all.
- **Battery-optimization exemption** — improves Athan reliability under Doze (today
  requested only by the audio player/service).

Goal: ask for each permission **on the welcome page, gated on the feature that needs it**.

## Approach

Keep the existing two-stage onboarding pattern — **choose features on Personalize (page 6),
grant permissions on Ready (page 7)** — and extend it. No new dedicated permissions page
(the heavier "full permissions page" alternative was considered and rejected to match the
chosen lighter scope).

### 1. Personalize page — add a Floating Bubble toggle

- New "Athkar Bubble" card with a `MaterialSwitch` (`onb_switch_bubble`), same card style as
  the Athan card, in `fragment_onboarding_personalize.xml`.
- Bound to new `OnboardingState.bubbleEnabled`.
- **Default: ON** (feature-forward, per product decision). The bubble only actually starts if
  overlay permission is granted on the Ready page, so defaulting on is safe — it just surfaces
  the overlay grant for new users.

### 2. Ready page — request permissions by enabled feature

Runtime permission dialogs (requested **together, once**, via
`ActivityResultContracts.RequestMultiplePermissions`, guarded by a one-shot flag so returning
from a settings redirect doesn't re-prompt):

| Permission | Condition |
|---|---|
| `POST_NOTIFICATIONS` (existing) | athan OR any reminder on, SDK ≥ TIRAMISU, not granted |
| `ACCESS_FINE_LOCATION` + `ACCESS_COARSE_LOCATION` (**new**) | athan on, not granted |

Settings-redirect links (can't be plain dialogs — visible only when relevant, tap → system
settings, re-evaluated in `onResume` so they disappear once granted):

| Link | Condition |
|---|---|
| Exact-alarm (existing, `onb_exact_alarm_link`) | athan on, SDK ≥ S, `!AthanScheduler.canUseExactAlarms` |
| **Overlay / draw-over-apps** (`onb_overlay_link`, new) | bubble on, `!Settings.canDrawOverlays` |
| **Battery-optimization** (`onb_battery_link`, new) | athan on, `!PowerManager.isIgnoringBatteryOptimizations` |

### 3. Apply on finish

- `OnboardingState` gains `boolean bubbleEnabled` (default `true` via `defaults()`).
- `WelcomeActivity` seeds `athanEnabled`/theme/reminders from the gateway as today; the bubble
  toggle keeps its `true` default for the onboarding experience (not overridden from prefs).
- `FeatureGateway` gains `setBubbleEnabled(boolean)` + `isBubbleEnabled()`; implemented in
  `AndroidFeatureGateway` mirroring `BubbleSettingsActivity`'s enable/disable branches:
  - enable **only if** `Settings.canDrawOverlays`: `BubblePrefs.setEnabled(true)`,
    `setDismissUntil(0L)`, `startForegroundService(AthkarBubbleService)`,
    `BubbleScheduler.reschedule`. If the toggle was on but overlay wasn't granted, leave it
    **off** (don't enable a bubble that can't draw).
  - disable: `BubblePrefs.setEnabled(false)` + send `AthkarBubbleService.ACTION_STOP`.
- `OnboardingFeatureController.apply()` calls `gateway.setBubbleEnabled(state.bubbleEnabled)`
  alongside the existing athan/reminder/theme application.

### 4. i18n + theming

- All new user-facing strings added to `values/strings.xml` (EN) and `values-ar/strings.xml`
  (AR): bubble toggle title/desc, overlay link, battery link.
- Reuse existing `onb_*` colors/styles (already light/dark-safe) so both themes hold.

## Files touched

- `onboarding/OnboardingState.java` — add `bubbleEnabled` (default true).
- `onboarding/OnboardingPersonalizeFragment.java` + `res/layout/fragment_onboarding_personalize.xml`
  — bubble toggle.
- `onboarding/OnboardingReadyFragment.java` + `res/layout/fragment_onboarding_ready.xml`
  — location request, overlay link, battery link, one-shot request guard.
- `onboarding/FeatureGateway.java` + `onboarding/AndroidFeatureGateway.java` — bubble enable/disable.
- `onboarding/OnboardingFeatureController.java` — apply bubble.
- `WelcomeActivity.java` — seed bubble default.
- `res/values/strings.xml` + `res/values-ar/strings.xml` — new strings.

## Verification

- Build the Madani debug variant.
- Fresh-install onboarding on the emulator: Personalize shows Athan + **Bubble** (on) + reminders
  + theme; Ready requests notifications + location, and shows exact-alarm + **overlay** +
  **battery** links as applicable.
- Toggle bubble off on Personalize → overlay link does **not** appear on Ready.
- Grant overlay → finishing onboarding starts the bubble; deny overlay → bubble stays off.
- Verify EN and AR copy, and light + dark.

## Risks / notes

- Two runtime dialogs (notifications + location) appear back-to-back when athan is on — batched
  into one `RequestMultiplePermissions` call so it's a single sequenced prompt set.
- Battery-optimization intent (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) is already used
  elsewhere in the app; same usage here.
- Starting `AthkarBubbleService` from `finishOnboarding` happens while the app is foreground, so
  the foreground-service start is allowed.

## Out of scope

- No changes to `BubbleSettingsActivity` / `AthanSettingsActivity` in-feature permission flows.

---

## Revision r2 (2026-06-27) — dedicated gated permissions page

Superseding the "links on the Ready page" presentation above. The final onboarding page
becomes a **dedicated, gated permissions page** (`OnboardingPermissionsFragment`, replacing
`OnboardingReadyFragment`).

**Layout — organized by feature, each a card with an on/off toggle + its permission rows:**
- **Athan & Prayer Alarms** (toggle = `state.athanEnabled`): rows for Notifications
  (athan + daily reminders), Location (prayer-time calc), Exact alarms (precise timing),
  Unrestricted battery (reliable background delivery).
- **Floating Bubble** (toggle = `state.bubbleEnabled`): row for Display over apps.
- Each row shows a one-line "why" + either a **Grant** button or a **✓ Granted** status.

**Grant actions:** notifications/location → runtime dialog; if permanently denied (asked +
`!shouldShowRequestPermissionRationale`) → app-details Settings. Overlay/exact-alarm/battery →
their system settings screens. All re-checked in `onResume`.

**Gate (per user decision "everything required" + "grant or turn feature off"):**
`Enter app` is enabled only when, for every ON feature, ALL its permissions are granted:
`(!athan || notif&&loc&&exact&&batt) && (!bubble || overlay)`. The escape is each card's
toggle — turning a feature OFF hides its rows and drops its requirements (no dead-end even if
a permission was permanently denied). A hint line states the rule.

**Skip routing:** the onboarding `Skip` button (`WelcomeActivity.btnSkip`) now jumps to this
permissions page instead of calling `finishOnboarding()`, so the gate can't be bypassed by
skipping the tour. Bottom bar stays hidden on this page (it's past `LAST_TOUR_PAGE`), so the
only exit to the app is the gated `Enter app`.

**Removed:** the optional overlay/battery/exact links added to `fragment_onboarding_ready.xml`
in r1, and `OnboardingReadyFragment`/`fragment_onboarding_ready.xml` (retired). The
`bubbleEnabled` state, gateway `setBubbleEnabled`/`isBubbleEnabled`, controller wiring, and the
Personalize bubble toggle from r1 are unchanged and still used.

**Out of scope (r2):** still no dedicated celebration page after granting; no per-reminder
permission gating (reminders share the athan Notifications grant).
