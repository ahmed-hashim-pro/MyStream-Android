# Generalize Design Language to Quran/Radio/More — Plan

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development. Checkbox steps.

**Goal:** Bring the Home redesign's design language to the other three bottom-nav tabs (Quran=`RecitesName`, Radio=`RadioFragment`, More=`OtherCategoryFragment`): one modern gold/navy header per tab (no more stacked legacy "My Stream" toolbar), and restyled list/grid items — without orphaning Settings/Profile/Rewards.

**Architecture:** Hide the legacy `@id/appbar` on ALL main fragment tabs (not just Home). Each fragment gets a single compact modern header (avatar → profile, screen title, action icons: search/filter where applicable, a settings gear → Settings, a rewards trophy → rewarded ad). Settings also gets a permanent entry in the More tab. List/grid item layouts adopt the `Widget.MyStream.*` styles.

**Tech Stack:** Java, XML Views, Material 3, view binding, Glide, Firebase.

## Global Constraints
- XML Views + Material 3. Palette: gold `#B8860B`/`#D4A44C`, navy `#1F2A44`→`#2C3A5E`, surface `#FFF8F0`. Use `Widget.MyStream.*` styles; no hardcoded values where a token/style exists.
- Decided header strategy: ONE modern header per tab; legacy toolbar hidden on all main tabs; Settings/Profile/Rewards relocated into headers + a Settings entry in More.
- Keep all existing list functionality (search, rewayah filter, view-mode toggle, share, ads) working.
- Each task: green `./gradlew :app:assembleDebug` + on-device check (emulator-5554, launch `.SplashScreen`) + commit.
- Reuse existing classes: `Settings.class` (settings), `UserProfileFragment` (profile — confirm how it's hosted/launched), rewarded ad via a new `MainActivity.showRewardedAd()`.

## Task G1: Foundation — hide legacy bar on all tabs, expose rewards, Settings in More, header style

**Files:** Modify `MainActivity.java`; Modify `OtherCategoryFragment.java`; Modify `styles_mystream.xml`; create `res/drawable/ic_settings_gear.xml` if none exists.

- [ ] **Step 1: Hide legacy app bar on all main fragment tabs.** In `MainActivity`'s `addOnDestinationChangedListener` (added in v2 at ~line 235), change the condition so `appbar` is GONE for `R.id.nav_home`, `R.id.nav_quran`, `R.id.nav_radio`, AND `R.id.nav_more` (VISIBLE otherwise, e.g. admin destinations):
```java
        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            if (appbar == null) return;
            int id = destination.getId();
            boolean mainTab = id == R.id.nav_home || id == R.id.nav_quran
                    || id == R.id.nav_radio || id == R.id.nav_more;
            appbar.setVisibility(mainTab ? View.GONE : View.VISIBLE);
        });
```
- [ ] **Step 2: Expose rewarded ad for fragment headers.** Add to `MainActivity`:
```java
    /** Trigger the rewarded ad from a tab header. */
    public void showRewardedAd() {
        loadRewardedAd();
        showRewardedVideo();
    }
```
- [ ] **Step 3: Add Settings (+ Profile) to the More tab.** In `OtherCategoryFragment`, add a "الإعدادات" (Settings → `Settings.class`) category to the category list/grid (and optionally "الملف الشخصي" Profile). Confirm the category-list construction site and mirror the existing item pattern (icon + title + target). Use a settings/gear icon (`@drawable/ic_settings_gear` — create a simple Material gear vector if missing; gold tint).
- [ ] **Step 4: Add a reusable header style.** In `styles_mystream.xml` add `Widget.MyStream.TabHeader` (a compact bar: navy or surface background, `?attr/actionBarSize`+ height, rounded bottom optional) and `Widget.MyStream.TabHeader.Title` (bold, on-navy or on-surface). Keep it flexible enough for the three fragments to share.
- [ ] **Step 5: Build + on-device.** Confirm: tapping Quran/Radio/More shows NO legacy "My Stream" toolbar (only each fragment's own header for now), Home unaffected; More shows a Settings tile that opens Settings. Build green.
- [ ] **Step 6: Commit** — `feat(nav): single header on all tabs + Settings in More + rewards hook`

## Task G2: Quran (RecitesName) — modern header + reciter rows

**Files:** Modify `app/src/main/res/layout/activity_recites_name.xml`; Modify `recites_ticket.xml` (+ `recites_ticket_grid.xml`, `recites_ticket_compact.xml` as needed); maybe `RecitesName.java` for header action wiring.

**Interfaces:** keep ids `@id/search`, `@id/toggleViewBTN`, `@id/recyclerView`, item ids `@id/cardContent`/`@id/entireCard`/`@id/imgchannel`/`@id/txtRecitesName`/`@id/buttonShare` (adapter binds them).

- [ ] **Step 1: Modern header.** Restyle the header (`@id/watchtitleCard` area) to `Widget.MyStream.TabHeader`: a compact gold/navy bar with an avatar (left, gold ring → opens profile via the same path MainActivity uses; confirm), the title "القرّاء", and right-side actions: the existing `@id/toggleViewBTN` (view mode), a search affordance (`@id/search` — keep, restyle; it may expand on tap), a settings gear (→ `startActivity(new Intent(getActivity(), Settings.class))`), and a rewards trophy (→ `((MainActivity) requireActivity()).showRewardedAd()`). Keep search + filter behavior intact.
- [ ] **Step 2: Reciter row restyle.** Restyle `recites_ticket.xml` to a modern reciter card using the design language (rounded `Widget.MyStream.Card` or `Widget.MyStream.ListRow` look: gold-tinted leading icon/avatar in a rounded frame, bold name, a gold play/share trailing). Keep the bound ids. Apply matching polish to the grid/compact variants (at minimum keep them building; full polish optional).
- [ ] **Step 3: Build + on-device.** Open Quran tab: single modern header (no legacy bar), search + view-toggle still work, reciter rows restyled. Settings gear opens Settings; trophy shows ad. Build green. Screenshot.
- [ ] **Step 4: Commit** — `feat(quran): modern header + restyled reciter rows`

## Task G3: Radio (RadioFragment) — modern header + station rows

**Files:** Modify `activity_radio_fragment.xml`; Modify `radio_ticket.xml`; maybe `RadioFragment.java`.

**Interfaces:** keep `@id/search`, `@id/rewayah_spinner`, `@id/listView`, item ids `@id/cardContent`/`@id/imgchannel`/`@id/txtRecitesName`/`@id/txtRadioRewayah`/`@id/buttonShare`.

- [ ] **Step 1: Modern header.** Same `Widget.MyStream.TabHeader` pattern: avatar + title "الإذاعات" + search + the rewayah `@id/rewayah_spinner` (restyle the spinner with a modern gold/navy background) + settings gear + rewards trophy. Keep filter behavior.
- [ ] **Step 2: Station row restyle.** Restyle `radio_ticket.xml` to a modern 2-line card (gold leading icon in rounded frame, bold station name, muted rewayah subtitle, trailing share/play). Keep bound ids.
- [ ] **Step 3: Build + on-device.** Radio tab: single modern header, search + rewayah filter work, station rows restyled. Build green. Screenshot.
- [ ] **Step 4: Commit** — `feat(radio): modern header + restyled station rows`

## Task G4: More (OtherCategoryFragment) — modern header + polished grid

**Files:** Modify `activity_other_category_fragment.xml`; Modify `other_ticket.xml`.

**Interfaces:** keep `@id/listView` (3-col grid), item ids `@id/entireCardOtherCategory`/`@id/icon`/`@id/itemtxt`. The Settings/Profile entries from G1 should appear in the grid.

- [ ] **Step 1: Modern header.** Replace the plain text header (`@id/recitelisttxt`) with the `Widget.MyStream.TabHeader`: avatar + title "المزيد" + settings gear + rewards trophy (no search needed).
- [ ] **Step 2: Grid card polish.** Refine `other_ticket.xml` (already MaterialCardView) to match the design language: consistent gold-tinted icon, `Widget.MyStream.Card`-aligned corners/elevation, gold-faint stroke, on-surface title. Ensure the new Settings/Profile tiles look consistent.
- [ ] **Step 3: Build + on-device.** More tab: modern header, polished category grid incl. Settings tile (opens Settings). Build green. Screenshot.
- [ ] **Step 4: Commit** — `feat(more): modern header + polished category grid`

## Self-Review
- Header strategy (single modern header, no legacy bar, utilities relocated) → G1 (hide bar, Settings in More, rewards hook) + per-tab headers (G2–G4). ✓
- List/grid restyle → G2 (reciter), G3 (station), G4 (category). ✓
- No-orphan guarantee: Settings reachable via header gear (all tabs) + More tile; Profile via header avatar; Rewards via header trophy (`showRewardedAd`). ✓
- Confirm-then-fill: profile launch path (G2 S1), category-list construction site (G1 S3), exact header restyle keeping search/filter/toggle ids. Layout authored per the established design language; existing bound ids preserved so adapters keep working.
