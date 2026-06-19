# Modern Home Redesign — Design Spec

**Date:** 2026-06-19
**Branch context:** `feature/advanced-athan` (My Stream Android, `com.medoapps.www.onlinequran`)
**Status:** Approved design — ready for implementation planning
**Phase:** 1 of a "redesign → generalize" effort

---

## 1. Summary

Modernize the app's home surface and establish a reusable design-system foundation, then roll those patterns out to the rest of the app in later specs. This spec covers four things only:

1. A reusable **design-language foundation** (XML styles/components).
2. A new **Home "Today" hub** screen.
3. Migration of the **5-tab bottom navigation** to Jetpack Navigation.
4. A restyled **default list row** applied to the surah and reciter lists as the template.

Everything else in the app is explicitly out of scope and becomes follow-up specs that reuse the components built here.

## 2. Constraints & decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| UI technology | **Stay in XML Views + Material 3** | App is 100% XML Views (~85 screens); no Compose migration. Lowest risk, reuses existing fragments/adapters. |
| Home direction | **Today Hub** dashboard | Greeting, prayer countdown, continue-reading, quick actions, carousels. Lists move into their own tabs. |
| Bottom nav set | **Home · Quran · Radio · Mushaf · More** (5) | Adds Home hub, keeps Mushaf as its own tab, folds legacy "Other" into More. |
| Header treatment | **Collapsing toolbar** | Navy expanded → slim gold collapsed bar on scroll. Maximizes content room, standard Material motion. |
| List row style | **Classic dense rows** | Number badge + two-line title + trailing action + thin divider. Best density for the 114-surah list. |
| Nav implementation | **Jetpack Navigation + NavHostFragment** | `BottomNavigationView` bound via `NavigationUI`; proper per-tab back-stack; replaces manual ViewPager + page-sync. |

**SDK baseline:** minSdk 21, target/compile 34, Material Components 1.11.0, view binding enabled.

## 3. Design language (reusable foundation)

Keep the existing Material 3 gold palette; add deep navy as a secondary accent.

- **Primary / gold:** `#B8860B`, lighter `#D4A44C`, surface `#FFF8F0` (existing M3 tokens in `res/values/colors.xml`).
- **Secondary / navy:** `#1f2a44` → `#2c3a5e` gradient. New. Used for the expanded header and the "continue reading" feature card. Add as named colors + a night-mode variant.

Define reusable styles in a new `res/values/styles_mystream.xml` (and `res/values-night/styles_mystream.xml`):

| Style | Purpose |
|-------|---------|
| `Widget.MyStream.Header.Collapsing` | Collapsing toolbar treatment (expanded navy / collapsed gold). |
| `Widget.MyStream.ListRow` | Classic dense row: leading number/icon badge, two-line title, trailing action, divider. |
| `Widget.MyStream.SectionHeader` | "Title … See all" row used above carousels/sections. |
| `Widget.MyStream.QuickAction` | Square icon tile with caption. |
| `Widget.MyStream.Card` | Standard elevated content card (white, rounded 16dp). |
| `Widget.MyStream.Card.Feature` | Navy gradient feature card (continue-reading). |

All spacing, corner radii, and type sizes reference M3 tokens / theme attributes — no hardcoded values where a token exists. These styles are the contract later screens consume; the "generalize" phase is largely "apply these styles."

## 4. Home hub

New content for `HomeFragment` (`app/src/main/java/.../ui/home/HomeFragment.java`, layout `res/layout/fragment_home.xml`).

### 4.1 Header
- `AppBarLayout` + `CollapsingToolbarLayout`.
- **Expanded:** navy gradient; greeting ("Assalamu alaikum"), user first name + avatar (Firebase `users/{uid}`: `firstname`, `photourl` via Glide), Hijri date (`HijriDate.todayString`), and a next-prayer countdown (`PrayerTimeEngine.getNextPrayerIndex` + `getTodayTimes`).
- **Collapsed:** slim gold bar with app name + compact countdown.

### 4.2 Body
Vertical scroll (`RecyclerView` with view-types, or NestedScrollView) containing:

1. **Continue reading** — `Widget.MyStream.Card.Feature` (navy). Resumes the last-read page in `QuranDataActivity`/`PagerActivity`. Falls back to "Start reading" when no last position exists.
2. **Quick actions** — four `Widget.MyStream.QuickAction` tiles: Quran, Radio, Athan, Athkar → existing destinations.
3. **Reciters carousel** — horizontal list reusing the existing Firebase `youtube-posts` data path (same source as `RecitesName`). `SectionHeader` with "See all" → Quran tab.

### 4.3 Header stat
Show the existing **reading streak** (from `reading_progress` SharedPrefs) as the header metric instead of a points count. The rewarded-ad trophy (`R.id.prize`) moves to the More tab.

### 4.4 New data work
Only one new persistence hook is required: capture the **last-read page** on exit from `PagerActivity` (SharedPrefs) so the continue-reading card has a target. All other data sources already exist:

- Prayer countdown — `athan/PrayerTimeEngine.java`, `athan/PrayerSettings.java`
- Hijri date — `athan/HijriDate.java`
- Reciters — Firebase Realtime DB `youtube-posts` (`RecitesName.java`)
- Profile — Firebase `users/{uid}` (`MainActivity` + `models/User.java`)
- Reading streak — `reading_progress` prefs (`ReadingProgressActivity.java`)

## 5. Navigation (5 tabs)

Replace the manual ViewPager + `OnNavigationItemSelectedListener` page-sync in `MainActivity` (~1,439 lines) with:

- A `NavHostFragment` + `BottomNavigationView` bound through `NavigationUI`.
- Nav graph (extend existing `res/navigation/mobile_navigation.xml`) with five top-level destinations:

| Tab | Destination | Backed by |
|-----|-------------|-----------|
| Home | new `HomeFragment` hub | this spec |
| Quran | surah / reciter browsing | wraps current `RecitesName` + `QuranListAdapter` |
| Radio | `RadioFragment` | existing |
| Mushaf | entry to `QuranDataActivity` | existing (launch destination) |
| More | `OtherCategoryFragment` + profile / settings / rewards | existing, reorganized |

`MainActivity` shrinks to host the NavHost + bottom bar. This is the **highest-risk change** and is sequenced **last**, done incrementally so each fragment keeps working throughout.

## 6. List rows (default template)

Apply the **Classic Rows** treatment as `Widget.MyStream.ListRow`:

- Leading 30dp rounded number/icon badge (gold-tinted).
- Two-line title: primary name + secondary metadata (e.g. "The Opening · 7 ayahs").
- Trailing circular play/action affordance.
- Thin `#efe6d2` divider between rows.

Applied first to the surah list (`QuranListAdapter` / `index_sura_row`) and the reciter list, establishing the row template that other lists adopt during the generalize phase.

## 7. Build sequence (risk-ordered)

Each step leaves the app runnable.

1. **Foundation styles** — colors (navy), `styles_mystream.xml` (+ night). No behavior change.
2. **List row restyle** — `Widget.MyStream.ListRow` on surah + reciter lists.
3. **Home hub** — `HomeFragment` content + collapsing header + cards/carousel + last-read persistence hook.
4. **Navigation migration** — NavHost + 5-tab graph, retire manual ViewPager sync. Last because it is the riskiest.

## 8. Testing & verification

- Manual on-device verification per the project emulator workflow (launch via `.SplashScreen`, drive with adb, verify state via `dumpsys`/prefs; use the athan-test trick for prayer countdown).
- Verify each build-sequence step independently before moving to the next.
- Confirm dark mode (night styles) on the new home + list row.

## 9. Out of scope (future "generalize" specs)

Ayah list, audio player, prayer/athan screens, admin dashboard, settings, search, onboarding, and remaining lists. Each adopts the component set from §3 in its own follow-up spec.
