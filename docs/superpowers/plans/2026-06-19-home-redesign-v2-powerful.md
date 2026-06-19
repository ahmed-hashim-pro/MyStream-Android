# Home Redesign v2 (Bayt al-Noor) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Make the Home tab feel powerful and full — a single commanding collapsing header with a glass next-prayer hero + live countdown ring, a rich body (continue, streak/goal arc, 5-prayer timeline, now-playing radio, verse of the day, quick actions, reciters, discover), and a floating gold "pill-dock" bottom bar — fixing the empty page, double header, and plain tab bar.

**Architecture:** XML Views + Material 3. The `nav_home` destination's own `CollapsingToolbarLayout` becomes the sole header (the legacy `@id/appbar` is hidden on Home). A custom Canvas view renders the countdown ring. Sections are view-types/children in `fragment_home.xml` wired by `HomeFragment`. Two small new persistence hooks (last-played radio; bundled verse-of-day).

**Tech Stack:** Java, Android XML Views, Material Components 1.11.0, AndroidX Navigation, Glide, Firebase, view binding. Build: Gradle wrapper. Unit tests: JUnit + Truth (pure-JVM only).

## Global Constraints

- XML Views + Material 3 only. minSdk 21 / target/compile 34, Material 1.11.0, view binding.
- Palette: gold `#B8860B`/`#D4A44C`, surface `#FFF8F0`, navy `#1F2A44`→`#2C3A5E`, divider `#EFE6D2`, badge `#F3E7C9`. Use tokens/styles — no hardcoded values where one exists (exception: intentional white-on-navy header/hero text).
- Reuse existing pieces: `HomeCountdown` (remainingMillis/format), `HomeReciterAdapter`, `PagerActivity.HOME_LAST_READ_PAGE`, `PrayerTimeEngine` (`getTodayTimes` Date[6] idx 0=Fajr,1=Sunrise,2=Dhuhr,3=Asr,4=Maghrib,5=Isha; `getNextPrayerIndex`; `PRAYER_NAME_RES`; `formatTime`), `HijriDate.todayString`, `reading_progress` prefs (`today_pages`,`daily_goal` default 5,`streak_days`).
- Every task ends with a green `./gradlew :app:assembleDebug` and a commit; UI tasks add an on-device check (emulator-5554, launch via `.SplashScreen`).
- Visual detail is governed by the committed spec `docs/superpowers/specs/2026-06-19-home-redesign-v2-powerful.md` — sections referenced as "spec §N".
- YOUTUBE is deprecated — never re-add it.

## File Structure

**Create:**
- `app/src/main/java/.../ui/home/PrayerCountdownRingView.java` — Canvas ring view
- `app/src/main/java/.../ui/home/RingMath.java` + test — pure sweep-fraction math
- `app/src/main/res/drawable/bg_pill_dock.xml`, `bg_glass_card.xml`, `ic_filigree_arch.xml`, `bg_prayer_node_next.xml`, `bg_prayer_node_past.xml`, `bg_prayer_node_future.xml`, `bg_stat_arc_track.xml`
- `app/src/main/res/values/arrays_verse_of_day.xml` — bundled ayat

**Modify:**
- `app/src/main/java/.../MainActivity.java` — destination listener (double-header fix)
- `app/src/main/res/values/styles_mystream.xml` — new `Widget.MyStream.*`
- `app/src/main/res/layout/content_main_activity.xml` + `layout-mdpi/activity_main.xml` — pill-dock BottomNavigationView
- `app/src/main/res/layout/fragment_home.xml` — full redesign
- `app/src/main/java/.../ui/home/HomeFragment.java` — header + sections wiring
- `app/src/main/java/.../RadioFragment.java` — persist last-played station
- `app/src/main/res/values/strings.xml` — new strings

---

## Task 1: Double-header fix (hide legacy app bar on Home)

**Files:** Modify `app/src/main/java/com/medoapps/www/onlinequran/MainActivity.java`

**Interfaces:** Consumes existing `navController` (field line 116, set line 230) and `appbar` (`AppBarLayout`, field 164, found line 219).

- [ ] **Step 1: Add the destination listener** after the `NavigationUI.setupWithNavController(bottomNavigationView, navController);` line (≈231):

```java
        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            // Home owns its own collapsing header — hide the legacy activity app bar there.
            if (appbar != null) {
                appbar.setVisibility(destination.getId() == R.id.nav_home ? View.GONE : View.VISIBLE);
            }
        });
```

- [ ] **Step 2: Build** — `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL.
- [ ] **Step 3: On-device** — launch; confirm Home shows NO "My Stream + hamburger" top bar above the hub; tap Quran/Radio/More and confirm the activity toolbar reappears there. `adb logcat -d -t 200 | grep -iE "FATAL"` clean.
- [ ] **Step 4: Commit** — `fix(home): hide legacy app bar on Home destination (single header)`

---

## Task 2: Styles + drawable assets foundation

**Files:** Modify `styles_mystream.xml`; Create the drawables listed in File Structure.

**Interfaces (produced, consumed by Tasks 3–8):** styles `Widget.MyStream.GlassCard`, `Widget.MyStream.StatArc`, `Widget.MyStream.PrayerNode`, `Widget.MyStream.RadioNowPlaying`, `Widget.MyStream.Discover`, `Widget.MyStream.BottomNav.PillDock`, `Widget.MyStream.BottomNav.ActiveIndicator`; drawables `bg_pill_dock`, `bg_glass_card`, `ic_filigree_arch`, `bg_prayer_node_*`, `bg_stat_arc_track`.

- [ ] **Step 1: Create drawables.** `bg_glass_card.xml` (rounded 18dp, `solid #22FFFFFF`, `stroke 1dp #55D4A44C`); `bg_pill_dock.xml` (rounded 28dp, `solid @color/md_theme_light_surface`/`#FFF8F0`, subtle stroke `#EFE6D2`); `bg_prayer_node_next.xml` (oval/rounded, solid gold), `bg_prayer_node_past.xml` (solid `#33B8860B`), `bg_prayer_node_future.xml` (stroke gold, transparent fill); `bg_stat_arc_track.xml` (ring shape, faint gold); `ic_filigree_arch.xml` (a simple symmetric arch vector in `#1AD4A44C` — decorative). Concrete shape XML per the palette; keep simple.
- [ ] **Step 2: Add styles** to `styles_mystream.xml` per spec §5 (GlassCard → MaterialCardView with `bg_glass_card`; StatArc; PrayerNode text; RadioNowPlaying card; Discover card; BottomNav.PillDock + ActiveIndicator gold lozenge using `badge_gold_bg`). Reuse existing `Widget.MyStream.Card`.
- [ ] **Step 3: Build** → BUILD SUCCESSFUL (aapt resolves all). **Step 4: Commit** — `feat(ui): styles + drawables for powerful home (glass, pill-dock, timeline, arc)`

---

## Task 3: Floating gold pill-dock bottom bar

**Files:** Modify `content_main_activity.xml` + `layout-mdpi/activity_main.xml` (the `@id/bottom_navigation` BottomNavigationView).

**Interfaces:** consumes `Widget.MyStream.BottomNav.PillDock` + `.ActiveIndicator` (Task 2). Keep id `@id/bottom_navigation` and all 5 menu items unchanged.

- [ ] **Step 1: Restyle** the `BottomNavigationView` in BOTH files: add `android:layout_margin="12dp"`, `android:background="@drawable/bg_pill_dock"`, `app:elevation="8dp"`, `app:labelVisibilityMode="selected"`, `app:itemActiveIndicatorStyle="@style/Widget.MyStream.BottomNav.ActiveIndicator"`, keep `app:menu="@menu/menu_bottom_navigation"` and the gold `app:itemIconTint`/`itemTextColor` selectors. Round the bar by sitting it above a transparent gutter (the ad banner tucks beneath; keep existing relative positioning).
- [ ] **Step 2: Build** → SUCCESSFUL. **Step 3: On-device** — confirm the bottom bar is a floating rounded pill, active tab shows a gold lozenge behind the icon+label, inactive tabs are icon-only. Switch tabs; indicator moves. **Step 4: Commit** — `feat(nav): floating gold pill-dock bottom bar`

---

## Task 4: Countdown ring view (+ TDD math)

**Files:** Create `ui/home/RingMath.java`, `ui/home/PrayerCountdownRingView.java`; Test `app/src/test/java/.../home/RingMathTest.java`.

**Interfaces (produced, consumed by Task 5):**
- `RingMath.sweepFraction(long prevPrayerMillis, long nextPrayerMillis, long nowMillis)` → float 0..1 (elapsed fraction of the interval; clamps to [0,1]; if interval ≤0 returns 1).
- `PrayerCountdownRingView`: `setProgress(float fraction)` (0..1, invalidates), `setCenterText(String big, String small)` (e.g. "باقي" / "2:14:30"), gold sweep arc over a faint track, navy-safe.

- [ ] **Step 1: Write failing test** `RingMathTest`:

```java
package com.medoapps.www.onlinequran.ui.home;
import static com.google.common.truth.Truth.assertThat;
import org.junit.Test;
public class RingMathTest {
  @Test public void midway_isHalf() {
    assertThat(RingMath.sweepFraction(0L, 1000L, 500L)).isWithin(1e-4f).of(0.5f);
  }
  @Test public void beforeStart_clampsToZero() {
    assertThat(RingMath.sweepFraction(100L, 1000L, 50L)).isEqualTo(0f);
  }
  @Test public void afterEnd_clampsToOne() {
    assertThat(RingMath.sweepFraction(0L, 1000L, 2000L)).isEqualTo(1f);
  }
  @Test public void nonPositiveInterval_isOne() {
    assertThat(RingMath.sweepFraction(1000L, 1000L, 1000L)).isEqualTo(1f);
  }
}
```

- [ ] **Step 2: Run → FAIL** (`RingMath` missing): `./gradlew :app:testDebugUnitTest --tests "*RingMathTest"`.
- [ ] **Step 3: Implement `RingMath`:**

```java
package com.medoapps.www.onlinequran.ui.home;
public final class RingMath {
  private RingMath() {}
  public static float sweepFraction(long prevPrayerMillis, long nextPrayerMillis, long nowMillis) {
    long interval = nextPrayerMillis - prevPrayerMillis;
    if (interval <= 0) return 1f;
    float f = (float) (nowMillis - prevPrayerMillis) / (float) interval;
    if (f < 0f) return 0f;
    if (f > 1f) return 1f;
    return f;
  }
}
```

- [ ] **Step 4: Run → PASS** (4 tests).
- [ ] **Step 5: Implement `PrayerCountdownRingView`** — a `View` drawing: a track arc (faint gold `#33D4A44C`, stroke ~6dp, full 360 from top) and a gold (`#D4A44C`→`#B8860B`) sweep arc from -90° spanning `progress*360`, rounded cap; center two-line text (small label + big countdown) in white. Expose `setProgress(float)` (clamp, invalidate) and `setCenterText(String,String)`. Handle wrap_content via a default size (~96dp). Standard `onDraw` with `Paint`/`RectF`/`drawArc`/`drawText`.
- [ ] **Step 6: Build** → SUCCESSFUL. **Step 7: Commit** — `feat(home): tested PrayerCountdownRingView + RingMath`

---

## Task 5: Collapsing header — glass next-prayer hero + ring (spec §3)

**Files:** Modify `fragment_home.xml` (replace the header block, lines 10–136: AppBarLayout/CollapsingToolbarLayout) and `HomeFragment.java` (header wiring).

**Interfaces (produced, layout ids HomeFragment binds):** `home_collapsing` (raise to ~300dp), `home_avatar`, `home_greeting`, `home_name`, `home_streak`, `home_hijri`, glass hero: `hero_ring` (`PrayerCountdownRingView`), `hero_next_label`, `hero_prayer_name`, `hero_prayer_time`, `prayer_dots` (a 5-dot `LinearLayout`), `home_toolbar` (collapsed pin).

- [ ] **Step 1: Header layout** — author per spec §3 in `fragment_home.xml`: `CollapsingToolbarLayout` 300dp, navy gradient background (`@drawable` or `android:background` gradient), `ic_filigree_arch` watermark `ImageView`, `contentScrim`/`statusBarScrim` = navy, `titleEnabled="false"`, parallax inner container with: top row (avatar gold-ring + greeting/name + `home_streak` pill), the **glass hero** (`Widget.MyStream.GlassCard`) containing `hero_next_label`("التالي"), `hero_prayer_name`+`hero_prayer_time`, the `PrayerCountdownRingView` `@id/hero_ring`, `home_hijri` caption, and `prayer_dots` (5 small dots). A pinned `home_toolbar` (`?attr/actionBarSize`) for the collapsed state. Use white text (intentional on navy).
- [ ] **Step 2: HomeFragment header wiring** — in a `bindHeaderHero()` called from `onViewCreated` + `onResume`:
  - profile/greeting/name/avatar/hijri/streak as today (already implemented; keep).
  - Compute next prayer: `idx = getNextPrayerIndex`; `times = getTodayTimes`; set `hero_prayer_name = getString(PRAYER_NAME_RES[idx])`, `hero_prayer_time = PrayerTimeEngine.formatTime(ctx, times[idx])`.
  - Find previous prayer time (the prayer before `idx`, skipping sunrise; if next is Fajr-tomorrow use today's Isha) to compute the ring fraction via `RingMath.sweepFraction(prevMillis, nextMillis, now)`; `hero_ring.setProgress(fraction)`.
  - Start a `CountDownTimer` (or `Handler` postDelayed every 1000ms, cancelled in `onPause`/`onDestroyView`) that updates `hero_ring.setCenterText("باقي", formatHms(remaining))` each second, where `formatHms` renders `H:MM:SS` from `HomeCountdown.remainingMillis(now, nextMillis)`; re-evaluate next prayer when it reaches 0.
  - Render `prayer_dots`: 5 dots, filled for prayers already passed today, outline for upcoming (use the 5 non-sunrise indices 0,2,3,4,5).
- [ ] **Step 3: Build** → SUCCESSFUL. **Step 4: On-device** — Home header is the sole header, navy with glass hero, prayer name/time, a live ticking countdown (seconds change), gold ring partially swept, 5 dots; scroll collapses it to the navy pin bar. Screenshot. **Step 5: Commit** — `feat(home): collapsing header with glass next-prayer hero + live ring`

---

## Task 6: Mid sections — continue, streak/goal arc, 5-prayer timeline (spec §4.2–4.4)

**Files:** Modify `fragment_home.xml` (body) + `HomeFragment.java`.

**Interfaces:** layout ids `card_continue`,`continue_title`,`continue_subtitle`,`continue_progress` (existing continue card, restyled); `stat_arc` (a `ProgressBar` style="...Circular" or a small custom arc via `bg_stat_arc_track`), `stat_pages` ("4 / 7"), `streak_dots` (7 day-dots), `streak_count`; `prayer_timeline` (horizontal container) with 5 node sub-views (name + time + node bg by state).

- [ ] **Step 1: Continue card** — restyle existing continue card to spec §4.2 (navy `Widget.MyStream.Card.Feature`, thin gold progress bar `continue_progress`, gold circular resume button). Keep existing `bindContinueReading()` logic (reads `HOME_LAST_READ_PAGE`); set progress = page/604.
- [ ] **Step 2: Streak/goal arc** — add a compact card: a circular determinate `ProgressBar` (gold tint, track `bg_stat_arc_track`) showing `today_pages/daily_goal`, `stat_pages` text "today_pages / daily_goal صفحات", a 7-dot `streak_dots` row (today highlighted gold), `streak_count` "streak_days يوم". Wire `bindStreakGoal()` reading `reading_progress` prefs. Arc is **static** (no animation).
- [ ] **Step 3: 5-prayer timeline** — add a horizontal rail `prayer_timeline` of 5 nodes (Fajr/Dhuhr/Asr/Maghrib/Isha = indices 0,2,3,4,5) each: name + `formatTime` + a node dot styled `bg_prayer_node_next|past|future` by comparing to now/`getNextPrayerIndex`. Build it programmatically in `bindPrayerTimeline()` (inflate a node item per prayer) or static 5 nodes updated in code. Connectors via a thin divider between nodes.
- [ ] **Step 4: Build** → SUCCESSFUL. **Step 5: On-device** — continue card restyled; streak/goal arc shows pages-vs-goal + streak dots; prayer timeline shows 5 times with the next elevated gold. Screenshot. **Step 6: Commit** — `feat(home): continue card, streak/goal arc, 5-prayer timeline`

---

## Task 7: Now-playing radio (+ last-played hook) & Verse of the Day (spec §4.5–4.6, §6)

**Files:** Modify `fragment_home.xml` + `HomeFragment.java` + `RadioFragment.java`; Create `res/values/arrays_verse_of_day.xml`.

**Interfaces (produced):** default-prefs keys `home_radio_name`,`home_radio_img`,`home_radio_server` (written by RadioFragment); layout ids `radio_card`,`radio_name`,`radio_thumb`,`radio_live_dot`,`radio_eq` (now-playing); `verse_arabic`,`verse_ref`,`verse_translation`,`verse_copy`,`verse_listen`.

- [ ] **Step 1: Last-played persistence hook** — in `RadioFragment.java` at the station launch (≈line 354, where `Intent intent = new Intent(getActivity(), NewQuranPlayer.class)` is built, using the clicked `AuthorClass`), add before `startActivity`:

```java
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putString("home_radio_name", clicked.RealName)
                .putString("home_radio_img", clicked.ImgUrl)
                .putString("home_radio_server", clicked.ServerName)
                .apply();
```

(Use the exact variable holding the tapped station — confirm its name near line 354/556; `import androidx.preference.PreferenceManager;` if missing.)

- [ ] **Step 2: Now-playing card** — add `radio_card` (navy `Widget.MyStream.RadioNowPlaying`): `radio_thumb` (Glide from `home_radio_img`), `radio_name`, a red `radio_live_dot` ("مباشر") and `radio_eq` equalizer bars. `bindNowPlaying()` reads the prefs; if absent, fall back to a featured station (`RadioLanguageClass.AutherList().get(0)` — confirm the static accessor) and label it without the live dot. Tapping opens `RadioFragment`/radio destination.
- [ ] **Step 3: Verse of the day** — create `arrays_verse_of_day.xml` with parallel string-arrays `verse_arabic`, `verse_ref`, `verse_translation` (~20–30 short ayat). Add `verse_card` to layout (`Widget.MyStream.Card`, gold quote glyph, `verse_arabic`, `verse_ref` chip, `verse_translation`, `verse_copy`/`verse_listen` ghost buttons). `bindVerseOfDay()`: pick index = `dayOfYear % count` (use `Calendar.get(DAY_OF_YEAR)`), set the three texts; copy button copies arabic+ref to clipboard.
- [ ] **Step 4: Build** → SUCCESSFUL. **Step 5: On-device** — play a radio station once, return to Home, confirm now-playing shows that station; verse-of-day renders an ayah + reference + translation. Screenshot. **Step 6: Commit** — `feat(home): now-playing radio (+ last-played hook) and verse of the day`

---

## Task 8: Tail sections polish — quick actions, reciters, discover (spec §4.7–4.9)

**Files:** Modify `fragment_home.xml` + `HomeFragment.java`.

**Interfaces:** existing `qa_quran/qa_radio/qa_athan/qa_athkar` (restyled to §4.7), `reciters_recycler`+`reciters_see_all` (existing carousel, glossier per §4.8), new `discover_athkar`,`discover_events` cards (§4.9).

- [ ] **Step 1: Quick actions** — restyle the 4 tiles per §4.7 (rounded elevated, gold line-icons on warm surface, gold-faint press). Keep ids + existing click wiring. Labels: المصحف · راديو · الأذكار · القبلة (Qibla target: if a Qibla activity exists wire it, else open Athan/More — confirm by grep, leave inert+noted if none).
- [ ] **Step 2: Reciters carousel** — enlarge/gloss the `item_home_reciter` cards (rounded frame, subtle elevation, gold play badge). Keep `HomeReciterAdapter` contract.
- [ ] **Step 3: Discover band** — two feature cards `discover_athkar` ("أذكار الصباح والمساء") and `discover_events` ("مناسبات إسلامية") per §4.9; wire to existing Athkar / IslamicEvents activities (grep to confirm class names).
- [ ] **Step 4: Build** → SUCCESSFUL. **Step 5: On-device** — full page from header to discover, no dead space, scrolls richly. Screenshot the whole scroll. **Step 6: Commit** — `feat(home): refined quick actions, glossier reciters, discover band`

---

## Task 9: Motion polish (spec §8)

**Files:** Modify `HomeFragment.java` (+ small drawables/anim if needed).

- [ ] **Step 1:** Add the ring sweep `ObjectAnimator`/per-minute update, soft pulse under 5 min; equalizer bar animation for the live radio state; the pill-dock indicator is Material-native. Keep continue progress fill on load. Respect that animations cancel in `onPause`/`onDestroyView`.
- [ ] **Step 2: Build** → SUCCESSFUL. **Step 3: On-device** — countdown ticks/sweeps, equalizer animates when radio live. **Step 4: Commit** — `feat(home): motion polish (ring sweep, equalizer, pulse)`

---

## Self-Review

- Spec §3 header → Tasks 4,5. §4.2–4.9 sections → Tasks 5,6,7,8. §5 components → Tasks 2,4. §6 data hooks → Tasks 6,7. §7 double-header → Task 1. §8 motion → Task 9. §9 build sequence → task order matches. §10 testing → per-task on-device. ✓
- Integration confirm-then-fill steps (not placeholders): exact radio station variable (Task 7 S1), Qibla/Athkar/Events class names (Task 8), featured-station accessor (Task 7 S2). Layout XML authored per the committed spec §3–§4 (the visual contract), with explicit required view-ids listed as interfaces so sections compose and HomeFragment binds correctly.
- Type consistency: `RingMath.sweepFraction(long,long,long)→float` and `PrayerCountdownRingView.setProgress(float)/setCenterText(String,String)` defined Task 4, used Task 5. Prefs keys `home_radio_*` defined Task 7 S1, read Task 7 S2. ✓
