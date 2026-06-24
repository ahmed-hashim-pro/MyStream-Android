---
title: Daily Athkar — Home "Stories" Bubbles (Instagram-style)
date: 2026-06-24
status: Designed — DEFERRED (store for later, not yet implemented)
branch_context: feature/advanced-athan
mockup: docs/superpowers/specs/2026-06-24-daily-athkar-stories-mockup.html
---

# Daily Athkar — Home "Stories" Bubbles

## Summary
An Instagram-stories-style strip on the Home feed surfacing **Morning (أذكار الصباح)** and
**Evening (أذكار المساء)** athkar as tappable circular bubbles. Tapping opens a full-screen,
swipeable **story viewer** — one dhikr per card with a live tasbih counter — and the bubbles
carry Instagram-like **seen / unseen rings** that reset daily.

## Design decisions baked into the mockup (confirm before build)
1. **Two bubbles** — Morning + Evening; the one in its active prayer-window glows gold, the other dims.
2. **Full guided session** — swipe the whole set, count each dhikr (not a single "dhikr of the day").
3. **Daily seen/unseen + completion ring** — gold glow (unseen) → conic fill (in-progress) → muted + ✓ (done); resets each day.
4. **Optional leading streak chip** (🔥) in the strip — easy to drop.

## UX
- Home strip sits **above "Verse of the Day"**, using the same dynamic horizontal-rail pattern as the prayer pills.
- **Story viewer:** segmented progress bars on top, bubble avatar + section title, dhikr in a Quranic serif,
  reference line, and a gold **count-down dial**. Tap the dial to decrement (auto-advances at 0),
  tap card sides to move prev/next, ✕ to close.
- **Ring states:** Unseen (gold gradient + pulse), In-progress (conic % fill), Done (muted + green ✓).

## Codebase mapping
- **Home:** `fragment_home.xml` + `HomeFragment.java`. Follow `bindPrayerTimeline()` (HomeFragment.java:520-566)
  inflate-in-a-loop; `HorizontalScrollView` with `android:layoutDirection="locale"`. Insert the section between
  the prayer block (~:791) and the Verse-of-the-Day card.
- **New layouts:** `item_athkar_bubble.xml` (clone `item_prayer_pill.xml`); story viewer = a new full-screen
  Activity with a `hero`-style header; ring drawable cloned from `bg_avatar_navy_ring.xml` / `bg_avatar_gold.xml`.
- **Content (single source of truth):** add accessors to `AthkarActivity.getAthkarList()`
  (morning section ~:82, evening ~:129) → `getMorningAthkar()` / `getEveningAthkar()`. Reuse the `AthkarItem`
  model (`text`, `count`, `remainingCount` tasbih). **Do NOT duplicate the Arabic strings.**
- **Daily / seen state:** NEW per-day SharedPreferences (the app's first per-day engagement state); reset by
  calendar day. The day-of-year pattern (`DailyAyahNotificationReceiver:96-98`) is available if a
  "dhikr of the day" variant is ever wanted instead.
- **Timing:** `PrayerTimeEngine.getTodayTimes()` → fajr=0 … isha=5. Morning active Fajr→Asr, Evening active Asr→Fajr.
- **Notifications:** none needed (UI-only). If added later, use request codes **2003-2004** + a distinct channel
  (athkar already owns 2001/2002 and channel `athkar_channel`).

## Constraints (mandatory, from project memory)
- **EN + AR** — strings in `values/` + `values-ar/`; reuse `athkar_section_morning` / `athkar_section_evening`;
  dhikr stays Arabic in both locales.
- **Light + dark** — never `@color/white` on navy (flips to black at night); use `text_on_navy` (fixed white) +
  `gold_accent` (auto-lightens).
- **RTL** — `layoutDirection="locale"` on the strip and the viewer.

## Open items before implementation
- Bubble icon treatment: sun/moon glyphs vs Arabic calligraphy vs single context-aware bubble.
- Keep or drop the streak chip; if kept, define "complete" (open the bubble vs finish all tasbih counts).
- Whether tapping opens the new story viewer (recommended) or deep-links into `AthkarActivity`
  (would need a new scroll-to-section capability that doesn't exist today).

## Mockup
Interactive prototype: `docs/superpowers/specs/2026-06-24-daily-athkar-stories-mockup.html`
(toggles for theme, EN/AR-RTL, and ring state; click a bubble → story viewer; tap the counter to count down).
