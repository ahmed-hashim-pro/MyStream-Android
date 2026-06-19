# Modern Home Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the tabbed-ViewPager home with a modern "Today" hub, migrate to a 5-tab Jetpack Navigation bottom bar, and establish a reusable list-row/style foundation for the rest of the app.

**Architecture:** Stay in XML Views + Material 3. A new collapsing-header Home hub fragment shows greeting, next-prayer countdown, continue-reading, quick actions, and a reciters carousel. `MainActivity` swaps its manual `ViewPager` + page-sync for a `NavHostFragment` + `BottomNavigationView` bound via `NavigationUI` over five destinations (Home · Quran · Radio · Mushaf · More). A new `styles_mystream.xml` holds reusable styles; `Widget.MyStream.ListRow` restyles the surah/reciter rows as the template later screens adopt.

**Tech Stack:** Java + Kotlin, Android XML Views, Material Components 1.11.0, AndroidX Navigation, Firebase Realtime DB, Glide, view binding. Build: Gradle wrapper. Unit tests: JUnit + Truth + Mockito (pure-JVM only; no Robolectric).

## Global Constraints

- UI stays in **XML Views + Material 3** — no Compose. (Spec §2)
- **minSdk 21, target/compile 34**, Material Components **1.11.0**, view binding enabled. (Spec §2)
- Palette: gold primary `#B8860B` / `#D4A44C`, surface `#FFF8F0`; new secondary navy `#1f2a44`→`#2c3a5e`. Use M3 tokens/theme attrs — no hardcoded values where a token exists. (Spec §3)
- **NavigationUI requirement:** each bottom-nav menu item id MUST equal its nav-graph destination id (`nav_home`, `nav_quran`, `nav_radio`, `nav_mushaf`, `nav_more`). (Spec §5)
- `AppTheme.MainActivity` is a NoActionBar theme — required for `CollapsingToolbarLayout`. (verified)
- Every task must end with a **green build** (`./gradlew :app:assembleDebug`) and, where UI changes, an on-device check. Commit at the end of every task.
- On-device launch (MainActivity is not exported): `adb shell am start -n com.medoapps.www.onlinequran/.SplashScreen` (splash routes to MainActivity).

---

## File Structure

**Create:**
- `app/src/main/res/values/styles_mystream.xml` — reusable component styles
- `app/src/main/res/values-night/styles_mystream.xml` — night overrides (only where they differ)
- `app/src/main/java/com/medoapps/www/onlinequran/ui/home/HomeCountdown.java` — pure countdown logic (tested)
- `app/src/test/java/com/medoapps/www/onlinequran/ui/home/HomeCountdownTest.java` — unit tests
- `app/src/main/java/com/medoapps/www/onlinequran/ui/home/HomeReciterAdapter.java` — carousel adapter
- `app/src/main/res/layout/item_home_reciter.xml` — carousel item
- `app/src/main/res/drawable/ic_home_24.xml` — home tab vector icon

**Modify:**
- `app/src/main/res/values/colors.xml` — add navy colors
- `app/src/main/res/layout/index_sura_row.xml` — restyle to `Widget.MyStream.ListRow`
- `app/src/main/res/layout/fragment_home.xml` — full hub layout
- `app/src/main/java/com/medoapps/www/onlinequran/ui/home/HomeFragment.java` — hub wiring
- `app/src/main/java/com/medoapps/www/onlinequran/ui/PagerActivity.java` — persist last-read page
- `app/src/main/res/menu/menu_bottom_navigation.xml` — 5-item set
- `app/src/main/res/navigation/mobile_navigation.xml` — 5 top-level destinations
- `app/src/main/java/com/medoapps/www/onlinequran/MainActivity.java` — NavHost wiring (replace ViewPager)
- the `activity_main` content layout (the file holding `@id/viewpager` + `@id/bottom_navigation` — confirm with grep in Task 8)

---

## Task 1: Design-language foundation (colors + styles)

No behavior change — adds reusable resources later tasks depend on.

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/values/styles_mystream.xml`
- Create: `app/src/main/res/values-night/styles_mystream.xml`

**Interfaces:**
- Produces (consumed by Tasks 2,5,7): styles `Widget.MyStream.ListRow`, `Widget.MyStream.SectionHeader`, `Widget.MyStream.QuickAction`, `Widget.MyStream.Card`, `Widget.MyStream.Card.Feature`; colors `@color/navy_700`, `@color/navy_500`, `@color/divider_warm`.

- [ ] **Step 1: Add navy + divider colors**

In `app/src/main/res/values/colors.xml`, add these next to the existing `gold_accent` entries:

```xml
<!-- Redesign: navy secondary + warm divider -->
<color name="navy_700">#1F2A44</color>
<color name="navy_500">#2C3A5E</color>
<color name="divider_warm">#EFE6D2</color>
<color name="badge_gold_bg">#F3E7C9</color>
```

- [ ] **Step 2: Create the reusable styles file**

Create `app/src/main/res/values/styles_mystream.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <!-- Classic dense list row: number/icon badge + 2-line title + trailing action -->
    <style name="Widget.MyStream.ListRow">
        <item name="android:layout_width">match_parent</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:minHeight">64dp</item>
        <item name="android:gravity">center_vertical</item>
        <item name="android:orientation">horizontal</item>
        <item name="android:paddingStart">16dp</item>
        <item name="android:paddingEnd">16dp</item>
        <item name="android:background">?attr/selectableItemBackground</item>
    </style>

    <style name="Widget.MyStream.ListRow.Badge">
        <item name="android:layout_width">30dp</item>
        <item name="android:layout_height">30dp</item>
        <item name="android:gravity">center</item>
        <item name="android:textColor">@color/gold_accent</item>
        <item name="android:textStyle">bold</item>
        <item name="android:textSize">13sp</item>
        <item name="android:background">@drawable/bg_listrow_badge</item>
    </style>

    <style name="Widget.MyStream.ListRow.Title">
        <item name="android:textColor">?attr/colorOnSurface</item>
        <item name="android:textSize">15sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:maxLines">1</item>
        <item name="android:ellipsize">end</item>
    </style>

    <style name="Widget.MyStream.ListRow.Meta">
        <item name="android:textColor">@color/sura_details_color</item>
        <item name="android:textSize">13sp</item>
        <item name="android:maxLines">1</item>
        <item name="android:ellipsize">end</item>
    </style>

    <!-- Section header: "Title ... See all" -->
    <style name="Widget.MyStream.SectionHeader">
        <item name="android:layout_width">match_parent</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:orientation">horizontal</item>
        <item name="android:gravity">center_vertical</item>
        <item name="android:paddingTop">12dp</item>
        <item name="android:paddingBottom">8dp</item>
    </style>

    <style name="Widget.MyStream.SectionHeader.Title">
        <item name="android:textColor">?attr/colorOnSurface</item>
        <item name="android:textStyle">bold</item>
        <item name="android:textSize">16sp</item>
    </style>

    <!-- Quick-action tile -->
    <style name="Widget.MyStream.QuickAction">
        <item name="android:layout_width">0dp</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:layout_weight">1</item>
        <item name="android:orientation">vertical</item>
        <item name="android:gravity">center</item>
        <item name="android:paddingTop">12dp</item>
        <item name="android:paddingBottom">12dp</item>
        <item name="android:background">?attr/selectableItemBackgroundBorderless</item>
    </style>

    <!-- Standard content card -->
    <style name="Widget.MyStream.Card" parent="Widget.Material3.CardView.Elevated">
        <item name="cardCornerRadius">16dp</item>
        <item name="cardElevation">2dp</item>
        <item name="contentPadding">14dp</item>
    </style>

    <!-- Navy feature card (continue reading) -->
    <style name="Widget.MyStream.Card.Feature" parent="Widget.MyStream.Card">
        <item name="cardBackgroundColor">@color/navy_700</item>
    </style>

</resources>
```

- [ ] **Step 3: Create the badge background + night styles file**

Create `app/src/main/res/drawable/bg_listrow_badge.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <corners android:radius="9dp" />
    <solid android:color="@color/badge_gold_bg" />
</shape>
```

Create `app/src/main/res/values-night/styles_mystream.xml` (navy already dark; only the warm badge needs toning down for night):

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Night: keep components inheriting day styles; nothing to override yet.
         File exists so future night-only overrides have a home. -->
</resources>
```

- [ ] **Step 4: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. (aapt resolves all new resource references.)

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/values/colors.xml app/src/main/res/values/styles_mystream.xml app/src/main/res/values-night/styles_mystream.xml app/src/main/res/drawable/bg_listrow_badge.xml
git commit -m "feat(ui): add MyStream design-language styles + navy palette"
```

---

## Task 2: Restyle the surah list row (default template)

**Files:**
- Modify: `app/src/main/res/layout/index_sura_row.xml`

**Interfaces:**
- Consumes: `Widget.MyStream.ListRow*` styles, `@drawable/bg_listrow_badge` (Task 1).
- Produces: row keeps view ids `suraNumber`, `rowIcon`, `title`, `metadata`, `tags`, `pageNumber` so `QuranListAdapter.kt` binds unchanged.

- [ ] **Step 1: Replace the row layout, preserving all view ids**

Overwrite `app/src/main/res/layout/index_sura_row.xml` with the restyled version (same ids, classic dense look, trailing page number, divider via background):

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    style="@style/Widget.MyStream.ListRow">

    <TextView
        android:id="@+id/suraNumber"
        style="@style/Widget.MyStream.ListRow.Badge"
        android:layout_marginEnd="14dp"
        tools:text="23" />

    <ImageView
        android:id="@+id/rowIcon"
        android:layout_width="36dp"
        android:layout_height="36dp"
        android:layout_marginEnd="14dp"
        android:scaleType="centerInside"
        android:visibility="gone"
        android:contentDescription="@null" />

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical"
        android:paddingTop="10dp"
        android:paddingBottom="10dp">

        <TextView
            android:id="@+id/title"
            style="@style/Widget.MyStream.ListRow.Title"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            tools:text="Al-Baqara" />

        <TextView
            android:id="@+id/metadata"
            style="@style/Widget.MyStream.ListRow.Meta"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="2dp"
            tools:text="Makki · 286 Verses" />

        <com.medoapps.www.onlinequran.view.TagsViewGroup
            android:id="@+id/tags"
            android:layout_width="match_parent"
            android:layout_height="24dp"
            android:layout_marginTop="4dp"
            android:gravity="start" />
    </LinearLayout>

    <TextView
        android:id="@+id/pageNumber"
        style="@style/Widget.MyStream.ListRow.Meta"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp"
        tools:text="1" />
</LinearLayout>
```

- [ ] **Step 2: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: On-device verification**

Install + launch, open the surah list (Quran). Confirm rows render with gold number badge, bold title, single-line metadata, and trailing page number; tap a row to confirm navigation still works.

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.medoapps.www.onlinequran/.SplashScreen
```

Expected: surah list shows restyled rows; tapping a surah still opens it.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/index_sura_row.xml
git commit -m "feat(ui): restyle surah row to Widget.MyStream.ListRow"
```

---

## Task 3: HomeCountdown logic (TDD)

Pure-JVM logic for "time until next prayer" → display string. No Android types, so it is unit-tested directly.

**Files:**
- Create: `app/src/main/java/com/medoapps/www/onlinequran/ui/home/HomeCountdown.java`
- Test: `app/src/test/java/com/medoapps/www/onlinequran/ui/home/HomeCountdownTest.java`

**Interfaces:**
- Produces (consumed by Task 6):
  - `static long remainingMillis(long nowMillis, long targetMillis)` — millis until target; if target already passed, wraps to next day (+86,400,000).
  - `static String format(long millisRemaining)` — `"now"` if ≤0, `"47m"` under an hour, `"2h 14m"` otherwise.

- [ ] **Step 1: Write the failing tests**

Create `app/src/test/java/com/medoapps/www/onlinequran/ui/home/HomeCountdownTest.java`:

```java
package com.medoapps.www.onlinequran.ui.home;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class HomeCountdownTest {

    private static final long HOUR = 3_600_000L;
    private static final long MIN = 60_000L;
    private static final long DAY = 86_400_000L;

    @Test
    public void remaining_targetInFuture_isSimpleDiff() {
        assertThat(HomeCountdown.remainingMillis(1_000L, 1_000L + 2 * HOUR))
                .isEqualTo(2 * HOUR);
    }

    @Test
    public void remaining_targetPassed_wrapsToNextDay() {
        // target was 1h ago -> should report ~23h until tomorrow's occurrence
        assertThat(HomeCountdown.remainingMillis(5 * HOUR, 4 * HOUR))
                .isEqualTo(DAY - HOUR);
    }

    @Test
    public void format_underOneHour_minutesOnly() {
        assertThat(HomeCountdown.format(47 * MIN)).isEqualTo("47m");
    }

    @Test
    public void format_overOneHour_hoursAndMinutes() {
        assertThat(HomeCountdown.format(2 * HOUR + 14 * MIN)).isEqualTo("2h 14m");
    }

    @Test
    public void format_exactlyOneHour_padsMinutes() {
        assertThat(HomeCountdown.format(HOUR)).isEqualTo("1h 00m");
    }

    @Test
    public void format_zeroOrNegative_isNow() {
        assertThat(HomeCountdown.format(0L)).isEqualTo("now");
        assertThat(HomeCountdown.format(-5L)).isEqualTo("now");
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests "com.medoapps.www.onlinequran.ui.home.HomeCountdownTest"`
Expected: FAIL — `HomeCountdown` does not exist / cannot resolve symbol.

- [ ] **Step 3: Implement `HomeCountdown`**

Create `app/src/main/java/com/medoapps/www/onlinequran/ui/home/HomeCountdown.java`:

```java
package com.medoapps.www.onlinequran.ui.home;

import java.util.Locale;

/** Pure helpers for the home next-prayer countdown. No Android dependencies. */
public final class HomeCountdown {

    private static final long DAY_MILLIS = 86_400_000L;
    private static final long HOUR_MILLIS = 3_600_000L;
    private static final long MINUTE_MILLIS = 60_000L;

    private HomeCountdown() {}

    /** Millis from now until target; wraps to the next day if target already passed. */
    public static long remainingMillis(long nowMillis, long targetMillis) {
        long diff = targetMillis - nowMillis;
        if (diff < 0) {
            diff += DAY_MILLIS;
        }
        return diff;
    }

    /** "now" / "47m" / "2h 14m". */
    public static String format(long millisRemaining) {
        if (millisRemaining <= 0) {
            return "now";
        }
        long hours = millisRemaining / HOUR_MILLIS;
        long minutes = (millisRemaining % HOUR_MILLIS) / MINUTE_MILLIS;
        if (hours == 0) {
            return minutes + "m";
        }
        return String.format(Locale.US, "%dh %02dm", hours, minutes);
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.medoapps.www.onlinequran.ui.home.HomeCountdownTest"`
Expected: PASS (6 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/ui/home/HomeCountdown.java app/src/test/java/com/medoapps/www/onlinequran/ui/home/HomeCountdownTest.java
git commit -m "feat(home): add tested HomeCountdown helper"
```

---

## Task 4: Persist last-read page (continue-reading source)

`PagerActivity` currently saves `LAST_READ_PAGE` only into the instance-state `Bundle`, so nothing survives process death for the hub. Add a persistent SharedPreferences write.

**Files:**
- Modify: `app/src/main/java/com/medoapps/www/onlinequran/ui/PagerActivity.java`

**Interfaces:**
- Produces (consumed by Task 6): default-SharedPreferences key `"home_last_read_page"` (int, 1-based Quran page; absent/`-1` = none).

- [ ] **Step 1: Locate the exact save site**

Run: `grep -n "LAST_READ_PAGE\|onPause\|lastPage\|getDefaultSharedPreferences" app/src/main/java/com/medoapps/www/onlinequran/ui/PagerActivity.java`
Note the `onPause()` body and the variable holding the current page (`lastPage`).

- [ ] **Step 2: Add the persistent write in `onPause()`**

Add this constant near the other key constants (after `LAST_READ_PAGE` at line ~168):

```java
    /** Persistent last-read page for the home "continue reading" card. */
    public static final String HOME_LAST_READ_PAGE = "home_last_read_page";
```

Inside `onPause()`, right after the existing `state.putInt(LAST_READ_PAGE, lastPage);` line, add:

```java
        // Persist for the home hub's continue-reading card (survives process death).
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putInt(HOME_LAST_READ_PAGE, lastPage)
                .apply();
```

(`PreferenceManager` is already imported and used in this file.)

- [ ] **Step 3: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: On-device verification**

Open the Mushaf/reader, navigate to a page, leave the reader, then dump the pref:

```bash
adb shell run-as com.medoapps.www.onlinequran cat /data/data/com.medoapps.www.onlinequran/shared_prefs/com.medoapps.www.onlinequran_preferences.xml | grep home_last_read_page
```

Expected: a `<int name="home_last_read_page" value="N"/>` entry with the page you left on.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/ui/PagerActivity.java
git commit -m "feat(reader): persist last-read page for home continue-reading"
```

---

## Task 5: Home hub layout

Build the hub layout: collapsing navy header + scrolling body (continue card, quick actions, reciters section, streak chip). Wiring is Task 6 — this task is structure only.

**Files:**
- Modify: `app/src/main/res/layout/fragment_home.xml`
- Create: `app/src/main/res/drawable/ic_home_24.xml` (used by Task 8; created here so it exists)

**Interfaces:**
- Produces (consumed by Task 6): view ids `home_appbar`, `home_collapsing`, `home_greeting`, `home_name`, `home_avatar`, `home_hijri`, `home_countdown_label`, `home_countdown_value`, `home_streak`, `card_continue`, `continue_title`, `continue_subtitle`, `qa_quran`, `qa_radio`, `qa_athan`, `qa_athkar`, `reciters_recycler`, `reciters_see_all`.

- [ ] **Step 1: Confirm quick-action drawable names exist**

Run: `ls app/src/main/res/drawable* | grep -iE "holyquran|radio|athan|athkar|category|ramadan_quran"`
Use confirmed names below. Defaults assumed present from the bottom-nav menu: `holyquran`, `outline_radio_24`, `ramadan_quran_24px`. For Athan/Athkar pick the matching results from this grep; if none, reuse `ramadan_quran_24px` for both and note it for follow-up.

- [ ] **Step 2: Create the home tab icon**

Create `app/src/main/res/drawable/ic_home_24.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z" />
</vector>
```

- [ ] **Step 3: Write the hub layout**

Overwrite `app/src/main/res/layout/fragment_home.xml`. Replace the `qa_*` icon `srcCompat` values with the confirmed drawables from Step 1.

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ui.home.HomeFragment">

    <com.google.android.material.appbar.AppBarLayout
        android:id="@+id/home_appbar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@android:color/transparent"
        app:elevation="0dp">

        <com.google.android.material.appbar.CollapsingToolbarLayout
            android:id="@+id/home_collapsing"
            android:layout_width="match_parent"
            android:layout_height="200dp"
            app:contentScrim="@color/gold_accent"
            app:statusBarScrim="@color/gold_accent"
            app:titleEnabled="false"
            app:layout_scrollFlags="scroll|exitUntilCollapsed|snap">

            <!-- Expanded navy header -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="@color/navy_700"
                android:orientation="vertical"
                android:paddingStart="16dp"
                android:paddingEnd="16dp"
                android:paddingTop="40dp"
                android:paddingBottom="16dp"
                app:layout_collapseMode="parallax">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:orientation="horizontal">

                    <de.hdodenhof.circleimageview.CircleImageView
                        android:id="@+id/home_avatar"
                        android:layout_width="40dp"
                        android:layout_height="40dp"
                        android:layout_marginEnd="10dp"
                        android:src="@mipmap/ic_launcher" />

                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical">

                        <TextView
                            android:id="@+id/home_greeting"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="@string/home_greeting"
                            android:textColor="#FFFFFF"
                            android:textSize="13sp"
                            android:alpha="0.85" />

                        <TextView
                            android:id="@+id/home_name"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:textColor="#FFFFFF"
                            android:textSize="20sp"
                            android:textStyle="bold"
                            tools:text="Hashim" />
                    </LinearLayout>

                    <TextView
                        android:id="@+id/home_streak"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:background="@drawable/bg_listrow_badge"
                        android:paddingStart="10dp"
                        android:paddingEnd="10dp"
                        android:paddingTop="5dp"
                        android:paddingBottom="5dp"
                        android:textColor="@color/gold_accent"
                        android:textStyle="bold"
                        android:textSize="12sp"
                        tools:text="🔥 7" />
                </LinearLayout>

                <TextView
                    android:id="@+id/home_hijri"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="14dp"
                    android:textColor="#FFFFFF"
                    android:alpha="0.85"
                    android:textSize="13sp"
                    tools:text="14 Dhul-Hijjah 1447" />

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="6dp"
                    android:gravity="center_vertical"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/home_countdown_label"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:textColor="#FFFFFF"
                        android:textSize="14sp"
                        tools:text="Next · Maghrib" />

                    <TextView
                        android:id="@+id/home_countdown_value"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:textColor="#FFFFFF"
                        android:textStyle="bold"
                        android:textSize="16sp"
                        tools:text="in 2h 14m" />
                </LinearLayout>
            </LinearLayout>

            <androidx.appcompat.widget.Toolbar
                android:id="@+id/home_toolbar"
                android:layout_width="match_parent"
                android:layout_height="?attr/actionBarSize"
                app:layout_collapseMode="pin"
                app:title="@string/menu_home"
                app:titleTextColor="#FFFFFF" />
        </com.google.android.material.appbar.CollapsingToolbarLayout>
    </com.google.android.material.appbar.AppBarLayout>

    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clipToPadding="false"
        android:padding="16dp"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <!-- Continue reading -->
            <com.google.android.material.card.MaterialCardView
                android:id="@+id/card_continue"
                style="@style/Widget.MyStream.Card.Feature"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="14dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical">

                    <TextView
                        android:id="@+id/continue_subtitle"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="@string/home_continue_reading"
                        android:textColor="#FFFFFF"
                        android:alpha="0.7"
                        android:textSize="12sp" />

                    <TextView
                        android:id="@+id/continue_title"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:textColor="#FFFFFF"
                        android:textStyle="bold"
                        android:textSize="17sp"
                        tools:text="Page 255" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- Quick actions -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_marginBottom="6dp">

                <LinearLayout android:id="@+id/qa_quran" style="@style/Widget.MyStream.QuickAction">
                    <ImageView android:layout_width="26dp" android:layout_height="26dp"
                        app:srcCompat="@drawable/holyquran" app:tint="@color/gold_accent"
                        android:contentDescription="@string/HolyQuran" />
                    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                        android:layout_marginTop="6dp" android:text="@string/HolyQuran" android:textSize="11sp" />
                </LinearLayout>

                <LinearLayout android:id="@+id/qa_radio" style="@style/Widget.MyStream.QuickAction">
                    <ImageView android:layout_width="26dp" android:layout_height="26dp"
                        app:srcCompat="@drawable/outline_radio_24" app:tint="@color/gold_accent"
                        android:contentDescription="@string/Radio" />
                    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                        android:layout_marginTop="6dp" android:text="@string/Radio" android:textSize="11sp" />
                </LinearLayout>

                <LinearLayout android:id="@+id/qa_athan" style="@style/Widget.MyStream.QuickAction">
                    <ImageView android:layout_width="26dp" android:layout_height="26dp"
                        app:srcCompat="@drawable/ramadan_quran_24px" app:tint="@color/gold_accent"
                        android:contentDescription="@string/athan_prayer_fajr" />
                    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                        android:layout_marginTop="6dp" android:text="@string/athan_prayer_fajr" android:textSize="11sp" />
                </LinearLayout>

                <LinearLayout android:id="@+id/qa_athkar" style="@style/Widget.MyStream.QuickAction">
                    <ImageView android:layout_width="26dp" android:layout_height="26dp"
                        app:srcCompat="@drawable/ramadan_quran_24px" app:tint="@color/gold_accent"
                        android:contentDescription="@string/OtherCategories" />
                    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                        android:layout_marginTop="6dp" android:text="@string/OtherCategories" android:textSize="11sp" />
                </LinearLayout>
            </LinearLayout>

            <!-- Reciters section header -->
            <LinearLayout style="@style/Widget.MyStream.SectionHeader">
                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    style="@style/Widget.MyStream.SectionHeader.Title"
                    android:text="@string/HolyQuran" />
                <TextView
                    android:id="@+id/reciters_see_all"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/see_all"
                    android:textColor="@color/gold_accent"
                    android:textSize="13sp"
                    android:padding="4dp" />
            </LinearLayout>

            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/reciters_recycler"
                android:layout_width="match_parent"
                android:layout_height="120dp"
                android:clipToPadding="false"
                android:orientation="horizontal"
                app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />

        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

- [ ] **Step 4: Add the new string resources**

Append to `app/src/main/res/values/strings.xml`:

```xml
    <string name="home_greeting">Assalamu alaikum</string>
    <string name="home_continue_reading">Continue reading</string>
    <string name="home_start_reading">Start reading</string>
    <string name="home_next_prayer">Next · %1$s</string>
    <string name="home_countdown_in">in %1$s</string>
    <string name="see_all">See all</string>
```

Confirm `@string/menu_home` already exists (it is referenced by the existing nav graph). If `ls`/grep shows it missing, also add `<string name="menu_home">Home</string>`.

- [ ] **Step 5: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/res/layout/fragment_home.xml app/src/main/res/drawable/ic_home_24.xml app/src/main/res/values/strings.xml
git commit -m "feat(home): hub layout — collapsing header, continue card, quick actions, reciters"
```

---

## Task 6: Wire the Home hub

Populate the hub: profile (Firebase), Hijri date, next-prayer countdown (using `HomeCountdown`), reading streak, continue-reading card, and quick-action click targets. The reciters carousel data is Task 7 (this task sets an empty adapter).

**Files:**
- Modify: `app/src/main/java/com/medoapps/www/onlinequran/ui/home/HomeFragment.java`

**Interfaces:**
- Consumes: `HomeCountdown.remainingMillis/format` (Task 3); `PagerActivity.HOME_LAST_READ_PAGE` (Task 4); layout ids (Task 5); `PrayerTimeEngine.getNextPrayerIndex/getTodayTimes/PRAYER_NAME_RES`; `HijriDate.todayString`; reading-streak pref (`reading_progress` / `streak_days`).
- Produces: a functioning Home destination for the nav graph (Task 8).

- [ ] **Step 1: Confirm how to open the reader and the reciters destination**

Run: `grep -rn "class QuranDataActivity\|class RadioFragment\|class RecitesName\|class AthanSettingsActivity" app/src/main/java | head`
Note the exact classes for the quick-action intents (`QuranDataActivity` for Quran/continue; `AthanSettingsActivity` for Athan if present — otherwise drop the Athan click target and leave the tile inert for now).

- [ ] **Step 2: Replace HomeFragment with the wired version**

Overwrite `app/src/main/java/com/medoapps/www/onlinequran/ui/home/HomeFragment.java`:

```java
package com.medoapps.www.onlinequran.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medoapps.www.onlinequran.QuranDataActivity;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.athan.HijriDate;
import com.medoapps.www.onlinequran.athan.PrayerTimeEngine;
import com.medoapps.www.onlinequran.databinding.FragmentHomeBinding;
import com.medoapps.www.onlinequran.models.User;
import com.medoapps.www.onlinequran.ui.PagerActivity;

import java.util.Date;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindProfile();
        bindHijri();
        bindCountdown();
        bindStreak();
        bindContinueReading();
        bindQuickActions();
        binding.recitersRecycler.setAdapter(new HomeReciterAdapter()); // filled in Task 7
    }

    @Override
    public void onResume() {
        super.onResume();
        // Countdown + continue card can change while away.
        bindCountdown();
        bindContinueReading();
    }

    private void bindProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            binding.homeName.setText("");
            return;
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                User u = snapshot.getValue(User.class);
                if (u == null) return;
                binding.homeName.setText(u.firstname != null ? u.firstname : "");
                if (u.photourl != null && !u.photourl.isEmpty()) {
                    Glide.with(HomeFragment.this).load(u.photourl)
                            .placeholder(R.mipmap.ic_launcher)
                            .into(binding.homeAvatar);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void bindHijri() {
        binding.homeHijri.setText(HijriDate.todayString(requireContext()));
    }

    private void bindCountdown() {
        Context ctx = requireContext();
        int idx = PrayerTimeEngine.getNextPrayerIndex(ctx);
        Date[] times = PrayerTimeEngine.getTodayTimes(ctx);
        String name = getString(PrayerTimeEngine.PRAYER_NAME_RES[idx]);
        long remaining = HomeCountdown.remainingMillis(
                System.currentTimeMillis(), times[idx].getTime());
        binding.homeCountdownLabel.setText(getString(R.string.home_next_prayer, name));
        binding.homeCountdownValue.setText(
                getString(R.string.home_countdown_in, HomeCountdown.format(remaining)));
    }

    private void bindStreak() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("reading_progress", Context.MODE_PRIVATE);
        int streak = prefs.getInt("streak_days", 0);
        binding.homeStreak.setText("🔥 " + streak);
    }

    private void bindContinueReading() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int page = prefs.getInt(PagerActivity.HOME_LAST_READ_PAGE, -1);
        if (page > 0) {
            binding.continueSubtitle.setText(R.string.home_continue_reading);
            binding.continueTitle.setText(getString(R.string.quran_page) + " " + page);
        } else {
            binding.continueSubtitle.setText(R.string.home_start_reading);
            binding.continueTitle.setText(R.string.HolyQuran);
        }
        binding.cardContinue.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), QuranDataActivity.class)));
    }

    private void bindQuickActions() {
        binding.qaQuran.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), QuranDataActivity.class)));
        binding.qaRadio.setOnClickListener(v -> openTab(R.id.nav_radio));
        binding.qaAthkar.setOnClickListener(v -> openTab(R.id.nav_more));
        binding.recitersSeeAll.setOnClickListener(v -> openTab(R.id.nav_quran));
        // qa_athan wired in a follow-up once the Athan entry class is confirmed (Step 1).
    }

    private void openTab(int destinationId) {
        try {
            androidx.navigation.fragment.NavHostFragment
                    .findNavController(this).navigate(destinationId);
        } catch (Exception ignored) {
            // Nav graph not present yet (pre-Task 8); ignore.
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```

- [ ] **Step 3: Confirm referenced symbols resolve**

Run: `grep -rn "public String firstname\|public String photourl" app/src/main/java/com/medoapps/www/onlinequran/models/User.java`
Run: `grep -rn "name=\"quran_page\"" app/src/main/res/values/strings.xml`
If `quran_page` is absent, add `<string name="quran_page">Page</string>` to strings.xml. If `User` field names differ (e.g. getters instead of public fields), adjust `bindProfile()` accordingly.

- [ ] **Step 4: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. (HomeFragment references `R.id.nav_radio` etc. — these resolve once Task 8 adds them. If building Task 6 before Task 8, temporarily the ids won't exist; this plan executes Task 8 after, so add the menu/nav ids in Task 8. To keep Task 6 independently buildable, the `openTab` ids `nav_radio/nav_quran/nav_more` must exist — they are added in Task 8's menu. **Therefore: if executing strictly in order, move Step 4's full build to after Task 8, and here run only `./gradlew :app:compileDebugJavaWithJavac` after stubbing.**) Simpler: add the three id placeholders now via `app/src/main/res/values/ids.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <item name="nav_quran" type="id"/>
    <item name="nav_radio" type="id"/>
    <item name="nav_more" type="id"/>
</resources>
```

Create that file, then `./gradlew :app:assembleDebug` → `BUILD SUCCESSFUL`. (Task 8's menu reuses these same ids, so no duplication conflict — menu items reference existing ids.)

- [ ] **Step 5: On-device verification**

Launch the app. The Home destination isn't in the bottom bar until Task 8, but you can verify no crash by temporarily setting the nav start destination is already `nav_home`. Launch and confirm the hub renders: name, Hijri date, prayer countdown ("Next · <prayer> in Xh Ym"), streak chip, continue card, quick actions.

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.medoapps.www.onlinequran/.SplashScreen
adb exec-out screencap -p > /tmp/home.png
```

Expected: hub populated with live data; no crash in `adb logcat | grep HomeFragment`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/ui/home/HomeFragment.java app/src/main/res/values/ids.xml app/src/main/res/values/strings.xml
git commit -m "feat(home): wire hub data — profile, hijri, countdown, streak, continue"
```

---

## Task 7: Reciters carousel

Back the hub's horizontal reciter strip with the existing `youtube-posts` Firebase node (top N), mapping `Post.title` → name, `Post.Thumb_Url` → image.

**Files:**
- Create: `app/src/main/res/layout/item_home_reciter.xml`
- Create: `app/src/main/java/com/medoapps/www/onlinequran/ui/home/HomeReciterAdapter.java`
- Modify: `app/src/main/java/com/medoapps/www/onlinequran/ui/home/HomeFragment.java`

**Interfaces:**
- Consumes: `Post` (`title`, `Thumb_Url`, `id`); `QuranDataActivity`/reciter screen.
- Produces: `HomeReciterAdapter` with `void submit(java.util.List<Post> posts)`.

- [ ] **Step 1: Confirm which Post fields render as reciter name/image**

Run: `sed -n '500,820p' app/src/main/java/com/medoapps/www/onlinequran/RecitesName.java`
Confirm the field used as the displayed reciter name (default assumed `title`; could be `author`) and image (`Thumb_Url`). Use the confirmed fields in Step 3's `onBindViewHolder`.

- [ ] **Step 2: Create the carousel item layout**

Create `app/src/main/res/layout/item_home_reciter.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="84dp"
    android:layout_height="wrap_content"
    android:layout_marginEnd="12dp"
    android:orientation="vertical"
    android:background="?attr/selectableItemBackgroundBorderless">

    <ImageView
        android:id="@+id/reciter_image"
        android:layout_width="84dp"
        android:layout_height="84dp"
        android:scaleType="centerCrop"
        android:background="@drawable/bg_listrow_badge"
        android:contentDescription="@null" />

    <TextView
        android:id="@+id/reciter_name"
        android:layout_width="84dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="6dp"
        android:maxLines="1"
        android:ellipsize="end"
        android:textSize="11sp"
        android:textColor="?attr/colorOnSurface" />
</LinearLayout>
```

- [ ] **Step 3: Create the adapter**

Create `app/src/main/java/com/medoapps/www/onlinequran/ui/home/HomeReciterAdapter.java`:

```java
package com.medoapps.www.onlinequran.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.models.Post;

import java.util.ArrayList;
import java.util.List;

public class HomeReciterAdapter extends RecyclerView.Adapter<HomeReciterAdapter.VH> {

    public interface OnReciterClick { void onClick(Post post); }

    private final List<Post> items = new ArrayList<>();
    private OnReciterClick clickListener;

    public void setOnReciterClick(OnReciterClick l) { this.clickListener = l; }

    public void submit(List<Post> posts) {
        items.clear();
        if (posts != null) items.addAll(posts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_reciter, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Post p = items.get(position);
        h.name.setText(p.title != null ? p.title : p.author);   // confirm field in Step 1
        Glide.with(h.image.getContext())
                .load(p.Thumb_Url)
                .placeholder(R.mipmap.ic_launcher)
                .into(h.image);
        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(p);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;
        VH(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.reciter_image);
            name = v.findViewById(R.id.reciter_name);
        }
    }
}
```

- [ ] **Step 4: Load data in HomeFragment**

In `HomeFragment.java`, add a field and a loader, and call it from `onViewCreated` (replace the `setAdapter(new HomeReciterAdapter())` line):

```java
    private final HomeReciterAdapter reciterAdapter = new HomeReciterAdapter();
```

Replace the carousel line in `onViewCreated` with:

```java
        binding.recitersRecycler.setAdapter(reciterAdapter);
        reciterAdapter.setOnReciterClick(p ->
                startActivity(new android.content.Intent(requireContext(), QuranDataActivity.class)));
        loadReciters();
```

Add the method (uses imports already present from Task 6: `FirebaseDatabase`, `DataSnapshot`, `ValueEventListener`, `DatabaseError`, `Post`):

```java
    private void loadReciters() {
        FirebaseDatabase.getInstance().getReference()
                .child("youtube-posts").orderByKey().limitToFirst(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (binding == null) return;
                        java.util.List<Post> posts = new java.util.ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Post p = child.getValue(Post.class);
                            if (p != null) posts.add(p);
                        }
                        reciterAdapter.submit(posts);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
```

Add the import: `import com.medoapps.www.onlinequran.models.Post;`

- [ ] **Step 5: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: On-device verification**

Launch, view the hub. Confirm the reciters strip populates with images + names from Firebase and scrolls horizontally; tapping an item opens the reader/reciter screen.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/ui/home/HomeReciterAdapter.java app/src/main/res/layout/item_home_reciter.xml app/src/main/java/com/medoapps/www/onlinequran/ui/home/HomeFragment.java
git commit -m "feat(home): reciters carousel backed by youtube-posts"
```

---

## Task 8: Migrate bottom navigation to Jetpack Navigation (HIGHEST RISK — LAST)

Replace `MainActivity`'s manual `ViewPager` + page-sync with a `NavHostFragment` + `BottomNavigationView` over five destinations. Keep all other `MainActivity` startup logic intact.

**Files:**
- Modify: `app/src/main/res/menu/menu_bottom_navigation.xml`
- Modify: `app/src/main/res/navigation/mobile_navigation.xml`
- Modify: the `activity_main` content layout (file holding `@id/viewpager` + `@id/bottom_navigation`)
- Modify: `app/src/main/java/com/medoapps/www/onlinequran/MainActivity.java`

**Interfaces:**
- Consumes: `HomeFragment` (Tasks 5–7); fragments `RecitesName`, `RadioFragment`, `OtherCategoryFragment`; `QuranDataActivity`.
- Produces: 5-tab nav; `MainActivity` no longer references `viewPager`, `mPagerAdapter`, `setupViewPager`, `prevMenuItem`.

- [ ] **Step 1: Confirm fragment classes + content layout file**

Run: `grep -rn "class RecitesName\|class RadioFragment\|class OtherCategoryFragment" app/src/main/java`
Run: `grep -rln "@+id/viewpager" app/src/main/res/layout*`
Run: `grep -rln "@+id/bottom_navigation" app/src/main/res/layout*`
Note the fully-qualified fragment names and the exact layout file(s) holding the ViewPager + bottom nav (there may be a base `layout/` file plus density variants — update each that contains `@id/viewpager`).

- [ ] **Step 2: Rewrite the bottom-nav menu (ids match nav destinations)**

Overwrite `app/src/main/res/menu/menu_bottom_navigation.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <item
        android:id="@id/nav_home"
        android:icon="@drawable/ic_home_24"
        android:title="@string/menu_home" />
    <item
        android:id="@id/nav_quran"
        android:icon="@drawable/holyquran"
        android:title="@string/HolyQuran" />
    <item
        android:id="@id/nav_radio"
        android:icon="@drawable/outline_radio_24"
        android:title="@string/Radio" />
    <item
        android:id="@+id/nav_mushaf"
        android:icon="@drawable/ramadan_quran_24px"
        android:title="@string/TheHolyQuran" />
    <item
        android:id="@id/nav_more"
        android:icon="@drawable/outline_category_24"
        android:title="@string/OtherCategories" />
</menu>
```

(`nav_home` is declared in `mobile_navigation.xml`; `nav_quran/nav_radio/nav_more` were declared in `ids.xml` in Task 6 — hence `@id/...`. `nav_mushaf` is new — `@+id`.)

- [ ] **Step 3: Rewrite the nav graph top-level destinations**

In `app/src/main/res/navigation/mobile_navigation.xml`, keep the admin destinations, set `startDestination` to `nav_home`, and ensure the five top-level destinations exist. Replace the leading section (the `<navigation ...>` open tag through the `nav_home` fragment) with:

```xml
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/mobile_navigation"
    app:startDestination="@id/nav_home">

    <fragment
        android:id="@id/nav_home"
        android:name="com.medoapps.www.onlinequran.ui.home.HomeFragment"
        android:label="@string/menu_home"
        tools:layout="@layout/fragment_home" />

    <fragment
        android:id="@id/nav_quran"
        android:name="com.medoapps.www.onlinequran.RecitesName"
        android:label="@string/HolyQuran" />

    <fragment
        android:id="@id/nav_radio"
        android:name="com.medoapps.www.onlinequran.RadioFragment"
        android:label="@string/Radio" />

    <fragment
        android:id="@id/nav_more"
        android:name="com.medoapps.www.onlinequran.OtherCategoryFragment"
        android:label="@string/OtherCategories" />

    <activity
        android:id="@+id/nav_mushaf"
        android:name="com.medoapps.www.onlinequran.QuranDataActivity"
        android:label="@string/TheHolyQuran" />
```

Leave the remaining admin `<fragment>` entries and the closing `</navigation>` as-is. Use the fully-qualified fragment names confirmed in Step 1 (adjust package if they differ).

- [ ] **Step 4: Replace the ViewPager with a NavHostFragment in the content layout**

In the content layout file(s) from Step 1, replace the `<androidx.viewpager.widget.ViewPager android:id="@+id/viewpager" .../>` element with:

```xml
<androidx.fragment.app.FragmentContainerView
    android:id="@+id/nav_host_fragment"
    android:name="androidx.navigation.fragment.NavHostFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:defaultNavHost="true"
    app:navGraph="@navigation/mobile_navigation" />
```

Keep the `BottomNavigationView` (`@id/bottom_navigation`) and any `AppBarLayout`/`Toolbar` siblings. If the ViewPager sat inside a `NestedScrollView`/`CoordinatorLayout`, place the `FragmentContainerView` where the ViewPager was, sized to fill the content area above the bottom nav. Ensure `xmlns:app` is declared on the root.

- [ ] **Step 5: Refactor MainActivity — remove ViewPager wiring, add NavHost**

In `MainActivity.java`:

(a) **Delete** these blocks (from the verified line ranges):
- `viewPager = (ViewPager) findViewById(R.id.viewpager);` and `viewPager.setOffscreenPageLimit(8);` (≈245–246)
- the entire `bottomNavigationView.setOnNavigationItemSelectedListener(...)` block (≈252–290)
- the entire `viewPager.addOnPageChangeListener(...)` block (≈292–321)
- the call to `setupViewPager(viewPager);` (find with grep)
- the whole `setupViewPager(...)` method (≈1375–1432)
- the `private ViewPager viewPager;` field and any `prevMenuItem`/`mPagerAdapter` fields now unused

(b) **Add** these imports near the existing ones:

```java
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
```

(c) **Add** NavHost wiring where the ViewPager setup used to be (after `bottomNavigationView = findViewById(R.id.bottom_navigation);`):

```java
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
```

`NavigationUI` routes `nav_mushaf` (an `<activity>` destination) by launching `QuranDataActivity`; the other four are fragment destinations. Keep every other line of `onCreate` (Firebase user listener, profile cards, ads, etc.) untouched.

- [ ] **Step 6: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. If it fails on a leftover `viewPager`/`prevMenuItem`/`mPagerAdapter`/`quranFragment` reference, remove that reference (grep for each symbol).

- [ ] **Step 7: On-device verification (full nav sweep)**

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.medoapps.www.onlinequran/.SplashScreen
```

Confirm:
- App opens to the **Home** hub (collapsing header collapses to a gold bar on scroll).
- Bottom bar shows 5 tabs: Home · Quran · Radio · Mushaf · More.
- Tapping **Quran** shows the reciter/surah list; **Radio** shows radio; **More** shows other categories.
- Tapping **Mushaf** opens `QuranDataActivity`; pressing back returns to the previously selected tab.
- Rotating the device and switching tabs does not crash (`adb logcat | grep -iE "AndroidRuntime|MainActivity"` is clean).

- [ ] **Step 8: Commit**

```bash
git add app/src/main/res/menu/menu_bottom_navigation.xml app/src/main/res/navigation/mobile_navigation.xml app/src/main/java/com/medoapps/www/onlinequran/MainActivity.java app/src/main/res/layout*/activity_main.xml app/src/main/res/layout*/content_main_activity.xml
git commit -m "feat(nav): 5-tab Jetpack Navigation bottom bar (Home·Quran·Radio·Mushaf·More)"
```

---

## Self-Review

**Spec coverage:**
- §3 design language → Task 1 (styles + navy). ✓
- §4 home hub (header, continue, quick actions, carousel, streak, last-read hook) → Tasks 3,4,5,6,7. ✓
- §5 navigation (5 tabs, Jetpack Navigation) → Task 8. ✓
- §6 list rows (classic template) → Task 2. ✓
- §7 build sequence (foundation → list row → hub → nav last) → task order matches. ✓
- §8 testing (build + on-device, athan/dumpsys) → verification steps present. ✓

**Known integration risks flagged for the executor (not placeholders — each has a confirm-then-fill step):**
- Reciter display field (`title` vs `author`) — Task 7 Step 1 confirms before binding.
- Exact `User` field access (`firstname`/`photourl` public vs getters) — Task 6 Step 3 confirms.
- The `activity_main` content layout may have density variants — Task 8 Step 1 greps all files holding `@id/viewpager`.
- Athan quick-action target deferred until the Athan entry class is confirmed (Task 6 Step 1) — tile present, click wired in a follow-up.

**Type consistency:** `HomeCountdown.remainingMillis/format` signatures used in Task 6 match Task 3. `HomeReciterAdapter.submit(List<Post>)` defined in Task 7 Step 3 and called in Step 4. `PagerActivity.HOME_LAST_READ_PAGE` defined in Task 4, read in Task 6. Menu ids (`nav_home/nav_quran/nav_radio/nav_mushaf/nav_more`) equal nav destination ids per the Global Constraint. ✓
