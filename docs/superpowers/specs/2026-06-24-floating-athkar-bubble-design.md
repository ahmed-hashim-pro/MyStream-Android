---
title: Floating Athkar Bubble (Messenger-style chat head, day & night athkar)
date: 2026-06-24
status: Designed — pending user spec review → implementation
branch: feature/floating-athkar-bubble
mockups:
  - docs/superpowers/specs/2026-06-24-floating-athkar-bubble-styles-mockup.html   # canonical (in-app style picker + live preview)
  - docs/superpowers/specs/2026-06-24-floating-athkar-bubble-options-mockup.html  # supporting (A–E exploration gallery)
related: docs/superpowers/specs/2026-06-24-daily-athkar-stories-design.md          # shares the same morning/evening content + daily completion
---

# Floating Athkar Bubble

## Summary
A Facebook-Messenger-style **floating chat-head bubble** that draws over other apps and surfaces the
user's **day (morning / أذكار الصباح)** and **night (evening / أذكار المساء)** athkar with per-dhikr
counters. The bubble auto-presents the right session by prayer time. The user **chooses one of three
visual styles** in Settings — the styles are not merged; they are interchangeable presentations of the
same content + counters.

It reuses the **same** morning/evening athkar and **daily completion state** as the deferred
[Daily Athkar Stories](2026-06-24-daily-athkar-stories-design.md) feature — one content source, one
"done today" store, two delivery surfaces.

## The three styles (user picks one in Settings)
- **A · Chat-head** *(default)* — a round draggable bubble → tap opens a focused single-dhikr counter panel. The most Messenger-like.
- **C · Edge drawer** — a slim edge tab → slides out the whole set as a tappable list; tap a row to count it.
- **D · Mini-pill** — a small bar that shows the *current* dhikr at rest (glanceable); tap to expand the same counter panel.

All three: drag to reposition, snap to edge, drag onto a bottom **✕** to dismiss; gold progress ring
= % of today's set done; badge/inline = current dhikr's remaining count.

## Product decisions (baked into the design — confirmable in the picker)
1. **Show during** — by default the bubble auto-shows in the **Day window (Fajr→Asr)** and **Night window (Asr→next Fajr)**, hidden otherwise. User can switch to **Always on** in the picker.
2. **Drag-to-✕** — **hides until the next window / next app launch** (Messenger "not now"); it does **not** disable the feature. Disable is only via the Settings toggle.
3. **Shared completion** — finishing athkar in the bubble marks the **same daily "done"** the Stories rings use (`AthkarProgressStore`).
4. **Default style** — **A (Chat-head)** out of the box.

## Architecture

### Overlay + service
- **`AthkarBubbleService`** — a foreground service (mirrors `athan/AthanPlaybackService` lifecycle) that owns a
  `WindowManager` overlay view and renders the chosen style. Copies `AthanPlaybackService.showOverApps()`'s
  runtime gate: `if (SDK_INT >= M && !Settings.canDrawOverlays(this)) return;` before `addView`.
  - Overlay window type branches by API: `TYPE_APPLICATION_OVERLAY` (API 26+), `TYPE_PHONE` (21–25);
    flags `FLAG_NOT_FOCUSABLE | FLAG_LAYOUT_NO_LIMITS`.
  - Drag tracks `event.getRawX/Y`; release → snap to nearest edge; a bottom ✕ target appears on drag-start,
    drop → `removeView` + hide. Tap (below drag threshold) → expand the style's panel/drawer.
- **`BubbleController`** — style-agnostic content/counting brain shared by all three style views: holds the
  active session, current dhikr, remaining counts; applies the 20 ms haptic (lifted from `TasbihActivity`);
  writes progress to `AthkarProgressStore`.
- **Style views** — `bubble_chathead.xml`, `bubble_pill.xml`, `bubble_drawer.xml`, plus the shared
  `bubble_panel_walker.xml` (header + Day/Night switch + dhikr + count dial + ‹ ›). A `BubbleStyle` enum
  (CHAT_HEAD / DRAWER / PILL) selects which view + interaction the service inflates.

### Content & completion (single source of truth)
- **`AthkarRepository`** — extract the morning (AthkarActivity.java ~:82) and evening (~:129) sets out of
  `AthkarActivity.getAthkarList()` into `getMorning()` / `getEvening()` returning the existing `AthkarItem`
  model (`text`, `count`, `remainingCount`, `parseCount()`). `AthkarActivity`, the Stories feature, and the
  bubble all consume this — **no duplicated Arabic strings**.
- **`AthkarProgressStore`** — per-day completion in a new `SharedPreferences` (`athkar_daily_prefs`),
  keyed by `dayOfYear + session + dhikr index`. Reset by calendar day. This is the app's first per-day
  engagement state and is **shared with the Stories rings**.

### Timing / auto-show
- Uses `athan/PrayerTimeEngine.getTodayTimes()` + `getNextPrayerIndex()` (indices: fajr0…isha5) to decide the
  active session and window. A small scheduler (AlarmManager, request codes in the free **3100+** range, or
  piggyback on existing athan alarms) shows/hides the service at window boundaries; `RECEIVE_BOOT_COMPLETED`
  (already granted) restores it after reboot. Reuse the Home hero's 1 s ticker pattern
  (`HomeFragment.startHeroTicker()`) for the in-panel countdown if shown.

### Settings (the picker)
- New **"Floating bubble"** section in `AthanSettingsActivity` (next to the existing overlay-permission row):
  enable toggle · "draw over other apps" permission row (reuse `requestOverlayPermission()` :333 +
  `canDrawOverlays()` :321 + on/off label idiom) · **style picker** (A/C/D cards) · **Show during**
  (Day&Night windows / Always).
- Persist in `bubble_prefs`: `bubble_enabled`, `bubble_style` (A|C|D), `bubble_show_mode` (windows|always),
  `bubble_pos_side`, `bubble_pos_y`.

## Files

**Create**
- `service/AthkarBubbleService.java`
- `bubble/BubbleController.java`, `bubble/BubbleStyle.java`
- `res/layout/bubble_chathead.xml`, `bubble_pill.xml`, `bubble_drawer.xml`, `bubble_panel_walker.xml`
- `data/AthkarRepository.java`, `data/AthkarProgressStore.java`
- bubble drawables (reuse `bg_avatar_gold.xml` / `bg_avatar_navy_ring.xml`; add ring/disc states as needed)
- strings in `res/values/strings.xml` + `res/values-ar/strings.xml`

**Modify**
- `AndroidManifest.xml` — add `FOREGROUND_SERVICE_SPECIAL_USE`; declare
  `<service android:name=".service.AthkarBubbleService" android:foregroundServiceType="specialUse">` with a
  `<property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" .../>` justification.
  (`SYSTEM_ALERT_WINDOW` is already present at line 34.)
- `AthkarActivity.java` — consume `AthkarRepository` instead of its private list.
- `AthanSettingsActivity.java` — add the bubble section + style picker + reuse the overlay-permission flow.

## Request codes / channels (avoid collisions)
- New FGS notification id **4000**, channel **`bubble_channel`** (LOW importance, ongoing).
- Alarm request codes in **3100+**. Do **not** reuse: athkar 2001/2002 (`athkar_channel`), athan 5000–5300,
  daily ayah 999, hadith 1000, asmaul 3001, fasting 3002, hisn 3003, `CHAT_CHANNEL`.

## Constraints (mandatory)
- **EN + AR** — all strings in `values/` + `values-ar/`; reuse `athkar_section_morning` / `_evening`; dhikr stays Arabic both locales.
- **Light + dark** — overlay does **not** inherit the host app theme; set explicit colors. Never `@color/white`
  on navy (flips to black at night) → use `text_on_navy` (fixed white) + `gold_accent` (auto-lightens).
- **RTL** — set `layoutDirection="locale"` on the bubble/drawer root; the **edge drawer docks to the left** in Arabic.
  Prefer launching a real Activity for any deep/long content so it picks up theme + locale correctly.

## Risks & mitigations
- **FGS type (highest):** targetSdk 34 forces a `foregroundServiceType`; existing `mediaPlayback`/`dataSync` don't
  fit → use **`specialUse`** (new permission + `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` + Play Console justification).
  Mis-declaring risks Play rejection. *Mitigation:* declare `specialUse` honestly ("persistent on-screen dhikr
  companion the user explicitly enabled").
- **Persistent notification:** any FGS shows an ongoing notification → use a **LOW-importance `bubble_channel`**,
  dismissed when the bubble is dismissed; get product sign-off that it's acceptable.
- **Overlay-permission friction:** manual Settings grant; reuse the app's existing grant UX + pre-gate dialog.
- **OEM background-kill** (Xiaomi/Huawei/Oppo/Samsung): extend the existing battery-exemption nudge
  (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, already granted); expect death on some OEMs.
- **Min/target window split:** branch overlay window type and guard `canDrawOverlays()` for `SDK_INT < M`.

## Open items
- Confirm the three product decisions above (or adjust the defaults).
- Confirm `specialUse` FGS is acceptable for the Play listing (vs. an in-app-only floating button fallback).
- Define exactly what "done" means for shared completion (finish all counts of a dhikr vs. open the bubble).

## Mockups
- Canonical: `docs/superpowers/specs/2026-06-24-floating-athkar-bubble-styles-mockup.html` — the in-app style
  picker + a live preview that floats the selected style; toggles theme / EN-AR-RTL / Day-Night.
- Supporting: `docs/superpowers/specs/2026-06-24-floating-athkar-bubble-options-mockup.html` — the A–E
  exploration that A/C/D were chosen from.
