# Home Redesign v2 — "Bayt al-Noor" (Powerful Home) — Design Spec

**Date:** 2026-06-19
**Branch:** `feature/advanced-athan` (My Stream Android, `com.medoapps.www.onlinequran`)
**Status:** Approved design direction (pending spec sign-off) — iteration on the shipped Phase-1 hub
**Supersedes the Home-screen portion of:** `2026-06-19-home-redesign-design.md` (foundation, list row, nav migration all remain)

## 1. Why

The shipped Phase-1 hub tested poorly with the product owner: it **felt empty** (large dead whitespace), carried a **redundant double header** (the legacy `MainActivity` toolbar stacked above the hub's own header), and the **bottom tab bar was plain default-Material**. This spec makes Home feel *powerful, full, and premium* while staying 100% XML Views + Material 3.

A 21-agent design panel generated and scored five directions. Chosen: **"Bayt al-Noor" (Serene Premium)** as the base, with **C's glass next-prayer hero** in the header, plus three grafts selected by the owner: **live "now playing" radio strip**, **reading streak + daily-goal arc**, and a **5-prayer timeline with an active node**.

## 2. Decisions

| Decision | Choice |
|----------|--------|
| Base direction | Bayt al-Noor (serene-premium): single commanding navy header, gold accents, full-but-calm content |
| Header centerpiece | C's **glass next-prayer hero**: NEXT label, prayer name + time, **live ticking countdown with seconds**, animated **gold sweeping ring** |
| Grafts | (1) live now-playing radio strip, (2) streak + daily-goal arc, (3) 5-prayer timeline w/ active node |
| Double header | **Hide the legacy `@id/appbar` AppBarLayout on the `nav_home` destination only** (destination-changed listener); other tabs keep it |
| Tab bar | **Floating gold "pill dock"**: rounded, elevated, inset warm-white bar; gold lozenge active-indicator; `labelVisibilityMode=selected` |
| Stack | XML Views + Material 3, minSdk 21 / target 34, Material 1.11.0, view binding |

## 3. Header (single commanding, collapsing)

`AppBarLayout` + `CollapsingToolbarLayout` (~300dp expanded), navy gradient `#1F2A44 → #2C3A5E` with a faint gold filigree-arch watermark. `contentScrim`/`statusBarScrim` = navy (continuous surface), edge-to-edge under the status bar.

**Expanded contents:**
- Top row: circular avatar (gold ring) + "السلام عليكم" overline + user name; gold **streak pill** ("🔥 7") pinned end.
- **Glass next-prayer hero card** (centerpiece): translucent card over the gradient — `NEXT` overline, prayer name + time (e.g. "المغرب 18:42"), a **live ticking countdown including seconds** ("باقي 2:14:30"), and a **gold ring** sweeping toward the prayer time.
- Hijri + Gregorian date caption ("الخميس · ١٤ ذو الحجة ١٤٤٧").
- A **5-dot prayer progress strip** (passed = filled, upcoming = outline).

**Collapsed (pin, ~56dp):** navy bar with small avatar + "المغرب · بعد ٢:١٤" title + one action (search/bell). Ring + greeting parallax up and fade; collapsed title cross-fades in (`titleEnabled=false`, manual alpha).

## 4. Section order (the full page)

1. **Living header** (hero + glass next-prayer + ring) — §3
2. **Continue Reading** — navy card, thin gold progress bar, "صفحة N · سورة …", circular gold resume button (reopens last-read Mushaf page from `home_last_read_page`)
3. **Reading streak + daily-goal arc** — compact card: a static progress **arc** of today's pages vs goal ("٤ / ٧ صفحات") + streak ("٧ أيام" with 7 day-dots). Data: `reading_progress` prefs `today_pages` / `daily_goal` / `streak_days`. The arc is **static** (no animation) so it doesn't compete with the hero ring.
4. **Today's prayers — 5-prayer timeline** — connected rail Fajr·Dhuhr·Asr·Maghrib·Isha with real times; next = elevated filled-gold node, past = dim + check, upcoming = outline. Data: `PrayerTimeEngine.getTodayTimes()` (Date[6], skip index 1 sunrise) + `getNextPrayerIndex()`.
5. **Live "now playing" radio strip** — navy card: station thumbnail + name, red "مباشر" pulse dot + equalizer mini-bars. Shows the **last-played** station; if none, a featured station (first of `RadioLanguageClass.AutherList()`).
6. **Verse of the Day** — warm-white card: gold quotation glyph, one ayah (Arabic), reference chip, one-line translation, ghost actions (نسخ / استماع).
7. **Quick Actions** — 4 refined rounded tiles (gold line-icons on warm surface): المصحف · راديو · الأذكار · القبلة.
8. **Reciters carousel** — section header "القرّاء" + "الكل"; horizontal RecyclerView of Firebase reciter cards (existing `HomeReciterAdapter`, made glossier/larger).
9. **Discover** — closing band: two feature cards "أذكار الصباح والمساء" + "مناسبات إسلامية" (event with date chip).

## 5. New components / assets

- **Countdown ring** — a custom `View` (Canvas: track arc + gold sweep arc + center text) or a layered ring drawable driven by `ObjectAnimator`. The seconds tick is driven by a `CountDownTimer`/`Handler` updating the center text every 1s; the sweep updates each minute. Lives in `ui/home/PrayerCountdownRingView.java`.
- **Glass hero card** — translucent `MaterialCardView` (semi-opaque white overlay over the navy gradient, blurred-look via low-alpha fill + 1px gold hairline).
- **5-prayer timeline** — a horizontal `LinearLayout`/custom view with connectors + nodes; reusable `Widget.MyStream.PrayerTimeline.Node` styles.
- **Equalizer bars** — small animated bars (`AnimationDrawable` or value animator) for the radio strip live state.
- **Pill-dock tab bar** — restyle `BottomNavigationView`: inset rounded elevated background drawable, gold lozenge `itemActiveIndicatorStyle`, `labelVisibilityMode="selected"`, navy-muted inactive icons. Reuses existing `Widget.MyStream.BottomNav.*`.
- **Gold filigree-arch watermark** — a vector drawable behind the header.
- New `Widget.MyStream.*` styles for: GlassCard, StatArc, PrayerTimeline, RadioNowPlaying, Discover cards — added to `styles_mystream.xml`.

## 6. New data hooks

- **Last-played radio** (NEEDS-BUILDING): persist the launched station's `RealName`, `ImgUrl`, `ServerName` to default prefs when a radio station starts (hook in the radio launch path, mirroring the Task-4 last-read hook). Home reads it; fallback to a featured station. Keys: `home_radio_name` / `home_radio_img` / `home_radio_server`.
- **Verse of the Day** (PARTIAL→bundled): ship a small curated `string-array` of ~30 short ayat as `{arabic, reference, translation}` triplets in resources; select by **day-of-year modulo count** so it rotates daily and is deterministic. (Future upgrade: reuse `DailyAyahNotificationReceiver`'s ayah so the home verse matches the daily notification — out of scope for v2.)
- **Streak + goal** (AVAILABLE): read `reading_progress` prefs (`today_pages`, `daily_goal` default 5, `streak_days`).
- **Prayer times** (AVAILABLE): `PrayerTimeEngine`.

## 7. Double-header fix

In `MainActivity`, add `navController.addOnDestinationChangedListener`: when destination `== R.id.nav_home`, set `findViewById(R.id.appbar)` (the legacy AppBarLayout) `View.GONE`; otherwise `View.VISIBLE`. This removes the redundant top bar on Home (the hub's CollapsingToolbar is the sole header) while Quran/Radio/Mushaf/More keep the activity toolbar. The promo cards (`completeProfileCard`, `subscribeCard`) live inside `appbar`, so they're hidden on Home automatically and continue to show on other tabs.

## 8. Motion

Collapsing parallax (header art + ring translate ~0.5×, greeting/ring alpha→0, collapsed title cross-fade); gold ring sweep (per-minute) + per-second center tick, soft pulse under 5 min; card reveal (12dp up + fade, staggered ~60ms); continue-reading progress fill on load; reciter skeleton shimmer; radio equalizer + red live pulse; pill-dock active-indicator width animation + icon scale bounce. All 180–300ms, eased, respect reduced-motion.

## 9. Build sequence (risk-ordered; each step leaves app runnable)

1. **Double-header fix** — destination listener hides `@id/appbar` on Home. (Immediate visible win, low risk.)
2. **Styles + assets** — new `Widget.MyStream.*` styles, filigree vector, glass/arc/timeline/pill drawables.
3. **Pill-dock tab bar** — restyle BottomNavigationView.
4. **Countdown ring view** + glass hero card in the header.
5. **Header assembly** — collapsing header with hero, greeting, streak pill, Hijri, 5-dot.
6. **Body sections** — continue, streak/goal arc, prayer timeline, now-playing radio (+ last-played hook), verse-of-day (+ bundled set), quick actions, reciters, discover.
7. **Motion polish.**

## 10. Testing

On-device per the emulator workflow (launch via `.SplashScreen`, ANDROID_SERIAL pinned, dumpsys/uiautomator for state, screenshots for visual). Verify: no double header on Home; hub fills the screen (no dead space); countdown ticks; tab bar pill-dock renders + switches; other tabs unaffected. Build green at each step.

## 11. Out of scope

Reconciling the activity toolbar for non-Home tabs (kept as-is); DailyAyah-DB-backed verse; per-tab independent headers; the rest of the app's screens (later "generalize" phase).
