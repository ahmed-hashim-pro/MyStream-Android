# Mushaf Re-theme Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement task-by-task. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Re-skin the embedded Quran-for-Android reader (package `com.medoapps.www.onlinequran`) into My Stream's navy + gold Material-3 design language, correct in light & dark and EN & AR, matching the approved mockups in `docs/superpowers/mockups/mushaf-retheme/index.html`.

**Architecture:** The visual mismatch is concentrated in the **chrome**, not the content lists (surah/juz rows already use `Widget.MyStream.ListRow`). Fix order: (1) navy system-bar/theme foundation → (2) index header → (3) reader overlay + audio chrome → (4) reading-mode paper backgrounds → (5) ayah action panel → (6) chrome sweep of the remaining secondary screens. Each task is build-and-screenshot-verified on the emulator in all four states before commit.

**Tech Stack:** Android XML themes/layouts/drawables, AppCompat/Material3 DayNight, custom views (`SlidingTabLayout`, `AudioStatusBar`, `AyahToolBar`), Kotlin/Java activities. Build variant: `madaniDebug`. Device: `emulator-5554`.

## Global Constraints

- **No hardcoded colors in changed code** — use existing tokens: `navy_700 #1F2A44`, `navy_500 #2C3A5E`, `navy_900 #172033`, `gold_accent #B8860B`(light)/`#D4A44C`(dark), `text_on_navy #FFFFFF`, `hint_on_navy #99FFFFFF`, `background_main/surface/card`, `text_primary/secondary`, `gold_accent_faint/semi`, `divider_gold`. Add new tokens only with both `values/` and `values-night/` entries.
- **Light + dark:** every change must be verified in both. Navy hero/chrome is FIXED (same hex both modes); only content surfaces flip. (memory: new-ui-light-dark-themes)
- **EN + AR:** any new user-facing string goes in `values/strings.xml` AND `values-ar/strings.xml`; Quran content stays Arabic. Layouts mirror via `layoutDirection="locale"` — never hardcode left/right. Verify both locales. (memory: new-ui-en-ar-translation)
- **Icons:** match the mockup's icon intents using existing vector drawables where present (`ic_*`); only add a vector if none fits. Tint chrome icons `text_on_navy`/`gold_accent`.
- **Reuse** the navy gradient drawable used by the hero (`bg_header_navy`) for toolbars/headers rather than flat fills, to match the mockup hero.
- Build before each commit: `./gradlew :app:installMadaniDebug -x lint -x test`. Verify with `adb -s emulator-5554 exec-out screencap -p > shot.png`; toggle dark via `adb -s emulator-5554 shell cmd uimode night yes|no`; toggle locale via the app's language setting or `adb -s emulator-5554 shell cmd locale set-app-locales com.medoapps.www.onlinequran --locales ar|en`.
- Commit per task. End commit messages with the Co-Authored-By trailer.

---

### Task 1: Navy chrome foundation (theme + system bars)

**Files:**
- Modify: `app/src/main/res/values/themes.xml:14-27` (`QuranToolBar`, `QuranToolBar.Overlay`)
- Modify: `app/src/main/res/values-night/themes.xml:3-12` (`QuranToolBar` night)
- Verify: `app/src/main/res/values/colors.xml` (navy tokens already exist), `app/src/main/AndroidManifest.xml` (~25 activities use `QuranToolBar`)

**Interfaces:**
- Produces: navy system bars (status + navigation) + light-on-dark status icons on every Quran activity, in light and dark.

- [ ] **Step 1:** In `values/themes.xml` `QuranToolBar`, change `android:statusBarColor` and `android:navigationBarColor` from `@color/mushaf_header_background` to `@color/navy_900`, and `android:windowLightStatusBar` from `true` to `false`.
- [ ] **Step 2:** In `values-night/themes.xml` `QuranToolBar`, change `android:statusBarColor`/`android:navigationBarColor` to `@color/navy_900` (already `windowLightStatusBar=false`).
- [ ] **Step 3:** Build + install. Launch the Mushaf tab; screencap. Confirm status/nav bars are navy with light icons in both `uimode night no` and `night yes`.
- [ ] **Step 4:** Commit `feat(mushaf): navy system bars across Quran screens`.

---

### Task 2: Index header — navy toolbar + gold tab strip

**Files:**
- Modify: `app/src/main/res/layout/quran_index.xml` (Toolbar + `SlidingTabLayout` backgrounds + Toolbar overlay)
- Read first: `app/src/main/java/com/medoapps/www/onlinequran/view/SlidingTabLayout.java` (find the indicator/text color setters), `app/src/main/java/com/medoapps/www/onlinequran/ui/QuranActivity.kt` (where the toolbar/tabs are configured)
- Reference mockup: screens 01/02/03 (`parts/01-index-surah.html`)

**Interfaces:**
- Consumes: navy tokens, `bg_header_navy` drawable.
- Produces: navy index header with white title, gold-tinted toolbar icons, gold tab indicator, white/gold tab labels — rows below already correct.

- [ ] **Step 1:** In `quran_index.xml`, change the `Toolbar` `android:background` from `@color/mushaf_header_background` to `@drawable/bg_header_navy`, and add `android:theme="@style/ThemeOverlay.Material3.Dark.ActionBar"` + `app:popupTheme="@style/ThemeOverlay.Material3.Light"` so the title/menu icons render light. Change the `SlidingTabLayout` `android:background` to `@color/navy_700`.
- [ ] **Step 2:** Set the tab indicator + text colors to gold/white. If `SlidingTabLayout` exposes `setSelectedIndicatorColors(...)`/a colorizer, call it with `gold_accent` in `QuranActivity.kt` where the indicator is bound; set tab text via its text-color API to `text_on_navy` (selected gold). If colors are XML-attr driven, set them in the layout. (Read the class to pick the exact API.)
- [ ] **Step 3:** Tint the toolbar menu icons (`quran_menu.xml` search/overflow) — ensure they inherit the Dark overlay (white) or set `android:iconTint`/`app:iconTint` to `@color/text_on_navy`; the search lens inside the field gold per mockup.
- [ ] **Step 4:** Build + install; open index; screencap in light/dark × EN/AR. Compare to mockup 01–03 (navy header, gold underline on active tab, gold number badges already present).
- [ ] **Step 5:** Commit `feat(mushaf): navy index header with gold tab strip`.

---

### Task 3: Reader chrome — navy overlay toolbar + audio bar

**Files:**
- Modify: `app/src/main/res/layout/quran_page_activity.xml:23-58` (`toolbar_area` + `audio_area` backgrounds)
- Read first: `app/src/main/java/com/medoapps/www/onlinequran/view/AudioStatusBar.java` (play/control colors), `app/src/main/java/com/medoapps/www/onlinequran/ui/PagerActivity.java` (toolbar setup, status_bg height)
- Reference mockup: screen 04 (`parts/04-reader.html`)

**Interfaces:**
- Produces: navy reader chrome (top toolbar overlay + bottom audio bar) with white text + gold play button; the page text area unchanged (handled in Task 4).

- [ ] **Step 1:** Change `toolbar_area` `android:background` and `audio_area` `android:background` from `@color/transparent_actionbar_color` to `@color/navy_900` (or a `bg_header_navy` variant for the top bar). Keep the toolbar's existing `Dark.ActionBar` overlay (white spinner text).
- [ ] **Step 2:** In `AudioStatusBar`, tint the play/pause/next/prev controls to `text_on_navy` and the primary play button background to `gold_accent` (match mockup `.play`). Reciter name `text_on_navy`, sub-line `hint_on_navy`, progress `gold_accent`. (Read the class; change its color constants/tints to token lookups.)
- [ ] **Step 3:** Build + install; open a sura in the reader, start audio; screencap light/dark × EN/AR. Compare to mockup 04 (navy bars, gold play, white text).
- [ ] **Step 4:** Commit `feat(mushaf): navy reader toolbar + audio bar`.

---

### Task 4: Reading-mode paper backgrounds (day / sepia / night)

**Files:**
- Read first: `app/src/main/java/com/medoapps/www/onlinequran/ui/PagerActivity.java` (reading-mode / night logic, ~lines 809-1132), the page fragment that draws `page_background`
- Modify: `app/src/main/res/values/colors.xml` + `values-night/colors.xml` (`page_background` keep sepia for light; add a `page_background` night value or a dedicated `page_background_night`)
- Reference mockup: screen 04 paper modes

**Interfaces:**
- Produces: page reading area shows sepia paper in light app theme and a dark page in dark app theme; existing in-reader night toggle still works.

- [ ] **Step 1:** Add a `values-night/colors.xml` override for `page_background` to a dark paper tone (e.g. `#15171C`), so the reading page is dark in dark mode instead of staying `#FFF4CB`. Keep light `#FFF4CB` (sepia).
- [ ] **Step 2:** Verify the page-image night inversion path still applies on top (the app inverts page images in night mode); ensure the surrounding page background uses the theme `page_background` not a hardcoded color. If a hardcoded `#FFF4CB` exists in Java, replace with the color resource.
- [ ] **Step 3:** Build + install; open reader in dark mode; screencap. Confirm the page no longer shows a bright sepia block in dark.
- [ ] **Step 4:** Commit `feat(mushaf): dark reading-page background in dark mode`.

---

### Task 5: Ayah action panel (sliding sheet) chrome

**Files:**
- Read first: `app/src/main/res/layout/quran_page_activity_slider.xml`, `ayah_action_panel_layout.xml`, `translation_panel.xml`, `audio_panel.xml`, `tag_dialog.xml`
- Modify: those layouts' header/background/indicator colors → tokens
- Reference mockup: screens 05/06/07

**Interfaces:**
- Produces: the sliding panel + its 3 tabs (Translation/Playback/Tags) use surface bg, gold tab dots, gold primary buttons, themed inputs.

- [ ] **Step 1:** Set the panel background to `@color/background_surface`, the panel header text to `?attr/colorOnSurface`, the page-indicator/active dot to `gold_accent`, the Apply/Save primary buttons to `gold_accent`, and tag checkboxes to `gold_accent`. Replace any hardcoded colors with tokens.
- [ ] **Step 2:** Build + install; select an ayah; open each tab; screencap light/dark × EN/AR vs mockup 05–07.
- [ ] **Step 3:** Commit `feat(mushaf): themed ayah action panel`.

---

### Task 6: Secondary screens chrome sweep

Most secondary activities use `QuranToolBar` (system bars fixed by Task 1) but still draw their own ActionBar/Toolbar/headers in warm colors. Re-theme each toolbar/header to navy + white/gold and replace any hardcoded warm colors with tokens. Build once at the end of the batch and screencap each.

**Files / screens (modify the toolbar/header + any hardcoded color in each layout):**
- `search.xml` / `search_result.xml` (mockup 09) — navy search bar, gold lens, themed result rows, themed warning notice.
- `translation_manager.xml` / `translation_row.xml` (15) — navy header, gold download/cloud icons, gold progress.
- Reciter audio manager layouts (16) — navy header; reciter cards already card-style; gold accents + progress.
- `jump_dialog.xml` (10) — themed dialog, gold Go button, focused input gold outline.
- `about_us.xml` / `activity_about_app.xml` (14) — navy header, gold app glyph, themed cards.
- `activity_downloadsplayer.xml` (18) — full navy now-playing, gold ring + gold play.
- `QuranDataActivity` download/permission dialogs (17) — navy header, gold buttons/progress (these are `AlertDialog`s; apply `@style/QuranDialog` themed to gold or build the gate layout per mockup).
- Settings (12/13): `AppTheme.Settings` already navy system bars; confirm preference categories/`bookmarks_widget.xml` use tokens (replace its `mushaf_header_background`).
- `bookmarks_widget.xml` — replace `mushaf_header_background` with a token.

- [ ] **Step 1:** For each screen, change toolbar/header background to `@drawable/bg_header_navy` (or `navy_700`), title to `text_on_navy`, icons tinted `text_on_navy`/`gold_accent`; replace any `@color/mushaf_header_background` / `@color/transparent_actionbar_color` / hardcoded warm hex with tokens.
- [ ] **Step 2:** Build + install; screencap each screen light/dark × EN/AR vs its mockup.
- [ ] **Step 3:** Commit `feat(mushaf): navy chrome for secondary Quran screens`.

---

### Task 7: Final verification pass

- [ ] **Step 1:** Walk every Mushaf screen on-device in all four states (light/dark × EN/AR); screencap; diff against the mockup gallery. Note any residual warm/sepia chrome or unmirrored RTL.
- [ ] **Step 2:** `grep -rnE '#FFF8F0|#FFF4CB|#AA000000|mushaf_header_background|transparent_actionbar_color'` across `app/src/main/res/layout` + Quran Java/Kotlin; confirm none remain in chrome (page sepia in light is intentional).
- [ ] **Step 3:** Final commit / open PR.
