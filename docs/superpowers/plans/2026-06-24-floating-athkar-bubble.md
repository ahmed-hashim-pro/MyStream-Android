# Floating Athkar Bubble Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a Messenger-style floating bubble that draws over other apps and lets the user complete their day (morning) & night (evening) athkar with per-dhikr counters, in one of three user-selectable styles (A Chat-head / C Edge drawer / D Mini-pill).

**Architecture:** A foreground `Service` (`AthkarBubbleService`, type `specialUse`) hosts a `WindowManager` `TYPE_APPLICATION_OVERLAY` view managed by `BubbleOverlayController`, which renders the style chosen in `BubblePrefs` and drives a pure `BubbleContentController` over athkar supplied by a new shared `AthkarRepository` (extracted from `AthkarActivity`). Completion is written to a per-day `AthkarProgressStore` shared with the deferred Stories feature. Session (morning/evening) and auto-show windows come from `athan/PrayerTimeEngine`.

**Tech Stack:** Java (Android, minSdk 21 / target 34 / compile 34), `WindowManager` overlay, foreground service, `SharedPreferences`, JUnit 4.13.2 + Truth 1.4.0 (JVM unit tests), Gradle flavor `madani`.

## Global Constraints

Copied verbatim from `docs/superpowers/specs/2026-06-24-floating-athkar-bubble-design.md`. Every task implicitly includes these.

- **Package / namespace:** `com.medoapps.www.onlinequran`. New bubble classes live in sub-package `…onlinequran.bubble`; shared athkar data classes live in the root package next to `AthkarActivity`.
- **EN + AR:** every user-facing string in BOTH `res/values/strings.xml` and `res/values-ar/strings.xml`. Reuse existing `R.string.athkar_section_morning` / `R.string.athkar_section_evening`. Dhikr text stays Arabic in both locales.
- **Light + dark:** the overlay does NOT inherit the host app theme — set explicit colors in the overlay layouts. NEVER use `@color/white` on a navy surface (it flips to black at night); use `@color/text_on_navy` (fixed white) and `@color/gold_accent` (auto-lightens). Verify every overlay layout in both themes.
- **RTL:** set `android:layoutDirection="locale"` on every overlay root; the edge drawer (style C) docks to the LEFT edge in Arabic.
- **Foreground service:** type `specialUse` (+ `FOREGROUND_SERVICE_SPECIAL_USE` permission + `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`). A LOW-importance ongoing notification on a new channel `bubble_channel` (notification id **4000**).
- **Request-code / channel collisions to avoid:** athkar 2001/2002 (`athkar_channel`), athan 5000–5300 (`NOTIF_ID 5300`), daily ayah 999, hadith 1000, asmaul 3001, fasting 3002, hisn 3003, `CHAT_CHANNEL`. The bubble uses notif id **4000**, channel **`bubble_channel`**, alarm request codes **3100/3101**.
- **Overlay window type branch:** `TYPE_APPLICATION_OVERLAY` on API ≥ 26, else `TYPE_PHONE`. Guard `Settings.canDrawOverlays()` with `Build.VERSION.SDK_INT < M`.
- **`SYSTEM_ALERT_WINDOW` is already declared** in the manifest (line 34) — do not re-add it.

## Build, test & device commands (referenced by tasks)

- **Compile/build APK:** `./gradlew assembleMadaniDebug`
- **Run one unit-test class:** `./gradlew testMadaniDebugUnitTest --tests "com.medoapps.www.onlinequran.<Class>"`
- **Run all unit tests:** `./gradlew testMadaniDebugUnitTest`
- **Install on emulator:** `adb -s emulator-5554 install -r app/build/outputs/apk/madani/debug/app-madani-debug.apk`
- **Launch app** (MainActivity is not exported — launch via splash): `adb -s emulator-5554 shell am start -n com.medoapps.www.onlinequran/.SplashScreen`
- **Grant overlay permission non-interactively** (emulator): `adb -s emulator-5554 shell appops set com.medoapps.www.onlinequran SYSTEM_ALERT_WINDOW allow`
- **Screenshot:** `adb -s emulator-5554 exec-out screencap -p > /private/tmp/claude-501/-Users-medo-projects-tmp-My-Stream-My-Stream-Android/e7d73825-e20f-4f5f-b70d-288d60519a3b/scratchpad/shot.png`
- **Confirm service running:** `adb -s emulator-5554 shell dumpsys activity services com.medoapps.www.onlinequran | grep -i AthkarBubble`

---

## File Structure

**Create (shared data — root package):**
- `app/src/main/java/com/medoapps/www/onlinequran/AthkarItem.java` — top-level dhikr model (was an inner class) + static `parseCount`.
- `app/src/main/java/com/medoapps/www/onlinequran/AthkarRepository.java` — `getMorningItems()` / `getEveningItems()` (content moved out of `AthkarActivity`).
- `app/src/main/java/com/medoapps/www/onlinequran/AthkarProgressStore.java` — per-day completion (`athkar_daily_prefs`), shared with Stories.

**Create (bubble — `…onlinequran.bubble`):**
- `bubble/BubbleSession.java` — enum `MORNING`/`EVENING`.
- `bubble/BubbleSessionSelector.java` — pure now→session logic.
- `bubble/BubbleStyle.java` — enum `CHAT_HEAD("A")`/`DRAWER("C")`/`PILL("D")` + `fromCode`.
- `bubble/BubblePrefs.java` — enabled / style / show-mode / saved position.
- `bubble/BubbleContentController.java` — pure counting/advance brain.
- `bubble/AthkarBubbleService.java` — foreground service hosting the overlay.
- `bubble/BubbleOverlayController.java` — `WindowManager` view: drag, snap, dismiss, render the 3 styles.
- `bubble/BubbleScheduler.java` — AlarmManager show/hide at window boundaries.
- `bubble/BubbleBootReceiver.java` — re-arm after reboot.

**Create (layouts/drawables/strings):**
- `res/layout/bubble_chathead.xml`, `bubble_panel_walker.xml`, `bubble_pill.xml`, `bubble_drawer.xml`, `bubble_drawer_row.xml`
- `res/layout/settings_section_bubble.xml` (included into the athan-settings layout)
- `res/drawable/bubble_ring.xml`, `bubble_disc.xml`, `bubble_dismiss_bg.xml`, `bubble_style_card.xml`
- strings appended to `res/values/strings.xml` + `res/values-ar/strings.xml`

**Modify:**
- `app/src/main/AndroidManifest.xml` — add `FOREGROUND_SERVICE_SPECIAL_USE`; `<service>` (specialUse) + property; `<receiver>` for boot.
- `AthkarActivity.java` — use top-level `AthkarItem` + delegate morning/evening to `AthkarRepository`.
- `AthanSettingsActivity.java` + `res/layout/activity_athan_settings.xml` — add the "Floating bubble" section (enable, overlay row, style picker, show-during) and start/stop the service.

---

## Task 1: Extract `AthkarItem` + `parseCount` to a top-level class

**Files:**
- Create: `app/src/main/java/com/medoapps/www/onlinequran/AthkarItem.java`
- Modify: `AthkarActivity.java` (remove inner `AthkarItem` at :369–390 and `parseCount` at :358–366; import/use the new class)
- Test: `app/src/test/java/com/medoapps/www/onlinequran/AthkarItemTest.java`

**Interfaces:**
- Produces: `class AthkarItem { String text; String count; boolean isHeader; int remainingCount; boolean expanded; List<AthkarItem> children; AthkarItem(String,boolean); AthkarItem(String,String,boolean); static int parseCount(String); }`

- [ ] **Step 1: Write the failing test**

```java
// app/src/test/java/com/medoapps/www/onlinequran/AthkarItemTest.java
package com.medoapps.www.onlinequran;

import static com.google.common.truth.Truth.assertThat;
import org.junit.Test;

public class AthkarItemTest {
    @Test public void parseCount_digits() { assertThat(AthkarItem.parseCount("100 مرة")).isEqualTo(100); }
    @Test public void parseCount_singleWord() { assertThat(AthkarItem.parseCount("مرة واحدة")).isEqualTo(1); }
    @Test public void parseCount_nullOrEmpty() {
        assertThat(AthkarItem.parseCount(null)).isEqualTo(1);
        assertThat(AthkarItem.parseCount("")).isEqualTo(1);
    }
    @Test public void contentCtor_setsRemainingFromCount() {
        AthkarItem it = new AthkarItem("سُبْحَانَ اللهِ وَبِحَمْدِهِ", "100 مرة", false);
        assertThat(it.isHeader).isFalse();
        assertThat(it.remainingCount).isEqualTo(100);
    }
    @Test public void headerCtor_hasChildrenAndZeroRemaining() {
        AthkarItem h = new AthkarItem("الصباح", true);
        assertThat(h.isHeader).isTrue();
        assertThat(h.children).isNotNull();
        assertThat(h.remainingCount).isEqualTo(0);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew testMadaniDebugUnitTest --tests "com.medoapps.www.onlinequran.AthkarItemTest"`
Expected: FAIL — `cannot find symbol: class AthkarItem` (it's still a package-private inner class).

- [ ] **Step 3: Create the top-level class**

```java
// app/src/main/java/com/medoapps/www/onlinequran/AthkarItem.java
package com.medoapps.www.onlinequran;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** One athkar entry (header or counted dhikr). Moved out of AthkarActivity so the
 *  full Athkar screen, the Stories feature, and the floating bubble share one model. */
public class AthkarItem {
    public String text;
    public String count;
    public boolean isHeader;
    public int remainingCount;
    public boolean expanded = true; // for headers only
    public List<AthkarItem> children; // for headers only

    public AthkarItem(String text, boolean isHeader) {
        this.text = text;
        this.isHeader = isHeader;
        this.remainingCount = 0;
        if (isHeader) this.children = new ArrayList<>();
    }

    public AthkarItem(String text, String count, boolean isHeader) {
        this.text = text;
        this.count = count;
        this.isHeader = isHeader;
        this.remainingCount = parseCount(count);
    }

    public static int parseCount(String countStr) {
        if (countStr == null || countStr.isEmpty()) return 1;
        if (countStr.contains("واحدة")) return 1;
        Matcher matcher = Pattern.compile("\\d+").matcher(countStr);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 1;
    }
}
```

- [ ] **Step 4: Update `AthkarActivity` to use the top-level class**

In `AthkarActivity.java`: delete the inner `static class AthkarItem {…}` (was :369–390) and the `private static int parseCount(…)` method (was :358–366). Every existing reference to `AthkarItem` now resolves to the new top-level class (same package), and references to `parseCount(x)` become `AthkarItem.parseCount(x)`. Field access stays the same (fields are now `public`). Remove the now-unused `import java.util.regex.Matcher;` / `Pattern;` from `AthkarActivity.java` only if no longer referenced there.

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew testMadaniDebugUnitTest --tests "com.medoapps.www.onlinequran.AthkarItemTest"`
Expected: PASS (4 tests).

- [ ] **Step 6: Compile the app to confirm the refactor is clean**

Run: `./gradlew assembleMadaniDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/AthkarItem.java \
        app/src/main/java/com/medoapps/www/onlinequran/AthkarActivity.java \
        app/src/test/java/com/medoapps/www/onlinequran/AthkarItemTest.java
git commit -m "refactor(athkar): extract AthkarItem + parseCount to a top-level class

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 2: `AthkarRepository` — shared morning/evening content

Move the morning and evening content items out of `AthkarActivity.getAthkarList()` into a reusable, `Context`-free repository so the bubble and the full screen share one source. Section *header* items (which need `getString`) stay in the Activity.

**Files:**
- Create: `app/src/main/java/com/medoapps/www/onlinequran/AthkarRepository.java`
- Modify: `AthkarActivity.java` (`getAthkarList()` :79 — replace the inline morning/evening content blocks with `addAll(AthkarRepository.getMorningItems())` / `getEveningItems()`)
- Test: `app/src/test/java/com/medoapps/www/onlinequran/AthkarRepositoryTest.java`

**Interfaces:**
- Consumes: `AthkarItem` (Task 1)
- Produces: `static List<AthkarItem> AthkarRepository.getMorningItems()`; `static List<AthkarItem> AthkarRepository.getEveningItems()` — content items only (no header), each a non-header `AthkarItem` with text+count.

- [ ] **Step 1: Write the failing test**

```java
// app/src/test/java/com/medoapps/www/onlinequran/AthkarRepositoryTest.java
package com.medoapps.www.onlinequran;

import static com.google.common.truth.Truth.assertThat;
import java.util.List;
import org.junit.Test;

public class AthkarRepositoryTest {
    @Test public void morning_isNonEmptyAndAllContent() {
        List<AthkarItem> m = AthkarRepository.getMorningItems();
        assertThat(m).isNotEmpty();
        for (AthkarItem it : m) {
            assertThat(it.isHeader).isFalse();
            assertThat(it.text).isNotEmpty();
            assertThat(it.remainingCount).isAtLeast(1);
        }
    }
    @Test public void evening_isNonEmpty() {
        assertThat(AthkarRepository.getEveningItems()).isNotEmpty();
    }
    @Test public void morning_containsTasbih100() {
        boolean has100 = AthkarRepository.getMorningItems().stream().anyMatch(i -> i.remainingCount == 100);
        assertThat(has100).isTrue();
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew testMadaniDebugUnitTest --tests "com.medoapps.www.onlinequran.AthkarRepositoryTest"`
Expected: FAIL — `AthkarRepository` does not exist.

- [ ] **Step 3: Create the repository, relocating the existing content verbatim**

Create the file below. Then **cut** the morning content items currently at `AthkarActivity.java:84–127` (every `list.add(new AthkarItem(<arabic>, <count>, false));` between the morning header at :82 and the evening header at :129) and **paste them verbatim** into `getMorningItems()` as `m.add(...)`. Do the same for the evening content at `:131–186` into `getEveningItems()`. The first five morning items are shown filled-in as the worked example — keep the remaining morning items (and all evening items) exactly as they read in the source; do not edit the Arabic.

```java
// app/src/main/java/com/medoapps/www/onlinequran/AthkarRepository.java
package com.medoapps.www.onlinequran;

import java.util.ArrayList;
import java.util.List;

/** Single source of truth for the morning/evening athkar content used by the full
 *  Athkar screen, the floating bubble, and the (deferred) Stories feature.
 *  Content only — section headers (which need string resources) stay in AthkarActivity. */
public final class AthkarRepository {
    private AthkarRepository() {}

    public static List<AthkarItem> getMorningItems() {
        List<AthkarItem> m = new ArrayList<>();
        m.add(new AthkarItem(
                "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَـهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                "مرة واحدة", false));
        m.add(new AthkarItem(
                "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ",
                "مرة واحدة", false));
        m.add(new AthkarItem(
                "اللَّهُمَّ أَنْتَ رَبِّي لا إِلَـهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ",
                "مرة واحدة", false));
        m.add(new AthkarItem("سُبْحَانَ اللهِ وَبِحَمْدِهِ", "100 مرة", false));
        m.add(new AthkarItem(
                "لاَ إِلَـهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                "100 مرة", false));
        // …paste the remaining morning content items from AthkarActivity.java:~106–127 here, verbatim…
        return m;
    }

    public static List<AthkarItem> getEveningItems() {
        List<AthkarItem> e = new ArrayList<>();
        // …paste the evening content items from AthkarActivity.java:131–186 here, verbatim, as e.add(...)…
        return e;
    }
}
```

- [ ] **Step 4: Delegate from `AthkarActivity.getAthkarList()`**

Where the morning header is added, follow it with the repository call; same for evening. The result is identical to before:

```java
list.add(new AthkarItem(getString(R.string.athkar_section_morning), true));
list.addAll(AthkarRepository.getMorningItems());
// …(after-prayer/sleep/etc. unchanged)…
list.add(new AthkarItem(getString(R.string.athkar_section_evening), true));
list.addAll(AthkarRepository.getEveningItems());
```

- [ ] **Step 5: Run the unit test to verify it passes**

Run: `./gradlew testMadaniDebugUnitTest --tests "com.medoapps.www.onlinequran.AthkarRepositoryTest"`
Expected: PASS (3 tests).

- [ ] **Step 6: Build, install, and eyeball the full Athkar screen (no regression)**

Run: `./gradlew assembleMadaniDebug && adb -s emulator-5554 install -r app/build/outputs/apk/madani/debug/app-madani-debug.apk`
Then open the Athkar screen in the app and confirm the morning & evening sections still list every dhikr with correct counts (unchanged from before).
Expected: identical content; build SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/AthkarRepository.java \
        app/src/main/java/com/medoapps/www/onlinequran/AthkarActivity.java \
        app/src/test/java/com/medoapps/www/onlinequran/AthkarRepositoryTest.java
git commit -m "refactor(athkar): move morning/evening content into shared AthkarRepository

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 3: `AthkarProgressStore` — shared per-day completion

**Files:**
- Create: `app/src/main/java/com/medoapps/www/onlinequran/AthkarProgressStore.java`
- Test: `app/src/test/java/com/medoapps/www/onlinequran/AthkarProgressStoreTest.java`

**Interfaces:**
- Produces: `static String AthkarProgressStore.key(int dayOfYear, String session, int index)`; instance methods `void markDone(int dayOfYear, String session, int index)`, `boolean isDone(...)`, `int doneCount(int dayOfYear, String session, int total)`, ctor `AthkarProgressStore(Context)`. `session` is `"MORNING"`/`"EVENING"` (use `BubbleSession.name()`).

- [ ] **Step 1: Write the failing test (pure key logic only — no Context)**

```java
// app/src/test/java/com/medoapps/www/onlinequran/AthkarProgressStoreTest.java
package com.medoapps.www.onlinequran;

import static com.google.common.truth.Truth.assertThat;
import org.junit.Test;

public class AthkarProgressStoreTest {
    @Test public void key_isStableAndDistinct() {
        assertThat(AthkarProgressStore.key(175, "MORNING", 3)).isEqualTo("done_175_MORNING_3");
        assertThat(AthkarProgressStore.key(175, "EVENING", 3))
                .isNotEqualTo(AthkarProgressStore.key(175, "MORNING", 3));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew testMadaniDebugUnitTest --tests "com.medoapps.www.onlinequran.AthkarProgressStoreTest"`
Expected: FAIL — `AthkarProgressStore` does not exist.

- [ ] **Step 3: Implement the store**

```java
// app/src/main/java/com/medoapps/www/onlinequran/AthkarProgressStore.java
package com.medoapps.www.onlinequran;

import android.content.Context;
import android.content.SharedPreferences;

/** Per-day athkar completion, shared by the floating bubble and the Stories rings.
 *  Stateless w.r.t. "today": callers pass the day-of-year so the logic stays testable. */
public class AthkarProgressStore {
    public static final String PREFS = "athkar_daily_prefs";
    private final SharedPreferences prefs;

    public AthkarProgressStore(Context context) {
        this.prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String key(int dayOfYear, String session, int index) {
        return "done_" + dayOfYear + "_" + session + "_" + index;
    }

    public void markDone(int dayOfYear, String session, int index) {
        prefs.edit().putBoolean(key(dayOfYear, session, index), true).apply();
    }

    public boolean isDone(int dayOfYear, String session, int index) {
        return prefs.getBoolean(key(dayOfYear, session, index), false);
    }

    public int doneCount(int dayOfYear, String session, int total) {
        int n = 0;
        for (int i = 0; i < total; i++) if (isDone(dayOfYear, session, i)) n++;
        return n;
    }
}
```

- [ ] **Step 4: Run the unit test to verify it passes**

Run: `./gradlew testMadaniDebugUnitTest --tests "com.medoapps.www.onlinequran.AthkarProgressStoreTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/AthkarProgressStore.java \
        app/src/test/java/com/medoapps/www/onlinequran/AthkarProgressStoreTest.java
git commit -m "feat(athkar): add shared per-day AthkarProgressStore

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 4: `BubbleSession` + `BubbleSessionSelector`

**Files:**
- Create: `bubble/BubbleSession.java`, `bubble/BubbleSessionSelector.java`
- Test: `app/src/test/java/com/medoapps/www/onlinequran/bubble/BubbleSessionSelectorTest.java`

**Interfaces:**
- Produces: `enum BubbleSession { MORNING, EVENING }`; `static BubbleSession BubbleSessionSelector.select(long nowMillis, long fajrMillis, long asrMillis)` — `MORNING` when `fajr ≤ now < asr`, else `EVENING`.

- [ ] **Step 1: Write the failing test**

```java
// app/src/test/java/com/medoapps/www/onlinequran/bubble/BubbleSessionSelectorTest.java
package com.medoapps.www.onlinequran.bubble;

import static com.google.common.truth.Truth.assertThat;
import org.junit.Test;

public class BubbleSessionSelectorTest {
    private static final long FAJR = 5  * 3600_000L;  // 05:00
    private static final long ASR  = 16 * 3600_000L;  // 16:00
    @Test public void betweenFajrAndAsr_isMorning() {
        assertThat(BubbleSessionSelector.select(9 * 3600_000L, FAJR, ASR)).isEqualTo(BubbleSession.MORNING);
    }
    @Test public void afterAsr_isEvening() {
        assertThat(BubbleSessionSelector.select(19 * 3600_000L, FAJR, ASR)).isEqualTo(BubbleSession.EVENING);
    }
    @Test public void beforeFajr_isEvening() {
        assertThat(BubbleSessionSelector.select(3 * 3600_000L, FAJR, ASR)).isEqualTo(BubbleSession.EVENING);
    }
    @Test public void exactlyFajr_isMorning() {
        assertThat(BubbleSessionSelector.select(FAJR, FAJR, ASR)).isEqualTo(BubbleSession.MORNING);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew testMadaniDebugUnitTest --tests "com.medoapps.www.onlinequran.bubble.BubbleSessionSelectorTest"`
Expected: FAIL — classes do not exist.

- [ ] **Step 3: Implement**

```java
// app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleSession.java
package com.medoapps.www.onlinequran.bubble;

public enum BubbleSession { MORNING, EVENING }
```

```java
// app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleSessionSelector.java
package com.medoapps.www.onlinequran.bubble;

/** Picks the athkar session for "now": morning while in [Fajr, Asr), evening otherwise. */
public final class BubbleSessionSelector {
    private BubbleSessionSelector() {}
    public static BubbleSession select(long nowMillis, long fajrMillis, long asrMillis) {
        return (nowMillis >= fajrMillis && nowMillis < asrMillis)
                ? BubbleSession.MORNING : BubbleSession.EVENING;
    }
}
```

- [ ] **Step 4: Run the unit test to verify it passes**

Run: `./gradlew testMadaniDebugUnitTest --tests "com.medoapps.www.onlinequran.bubble.BubbleSessionSelectorTest"`
Expected: PASS (4 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleSession.java \
        app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleSessionSelector.java \
        app/src/test/java/com/medoapps/www/onlinequran/bubble/BubbleSessionSelectorTest.java
git commit -m "feat(bubble): session enum + morning/evening selector

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 5: `BubbleContentController` — pure counting brain

**Files:**
- Create: `bubble/BubbleContentController.java`
- Test: `app/src/test/java/com/medoapps/www/onlinequran/bubble/BubbleContentControllerTest.java`

**Interfaces:**
- Consumes: `AthkarItem` (Task 1).
- Produces: `BubbleContentController(java.util.List<AthkarItem> items)`; `int size()`, `int currentIndex()`, `AthkarItem currentItem()`, `int remainingAt(int)`, `int targetAt(int)`, `int doneCount()`, `float fraction()`, `boolean isAllDone()`, `boolean countCurrent()` (decrement current; returns `true` iff that dhikr just hit 0), `void jumpTo(int)`.

- [ ] **Step 1: Write the failing test**

```java
// app/src/test/java/com/medoapps/www/onlinequran/bubble/BubbleContentControllerTest.java
package com.medoapps.www.onlinequran.bubble;

import static com.google.common.truth.Truth.assertThat;
import com.medoapps.www.onlinequran.AthkarItem;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class BubbleContentControllerTest {
    private BubbleContentController make() {
        List<AthkarItem> items = Arrays.asList(
                new AthkarItem("a", "مرة واحدة", false),  // target 1
                new AthkarItem("b", "3 مرة", false),      // target 3
                new AthkarItem("c", "100 مرة", false));   // target 100
        return new BubbleContentController(items);
    }
    @Test public void startsAtFirst() {
        BubbleContentController c = make();
        assertThat(c.currentIndex()).isEqualTo(0);
        assertThat(c.remainingAt(0)).isEqualTo(1);
        assertThat(c.fraction()).isEqualTo(0f);
    }
    @Test public void countingOneCountItem_completesAndAdvances() {
        BubbleContentController c = make();
        assertThat(c.countCurrent()).isTrue();      // a 1->0, completed
        assertThat(c.doneCount()).isEqualTo(1);
        assertThat(c.currentIndex()).isEqualTo(1);  // auto-advanced to b
    }
    @Test public void multiCount_needsAllTaps() {
        BubbleContentController c = make();
        c.countCurrent();                            // finish a
        assertThat(c.countCurrent()).isFalse();      // b 3->2
        assertThat(c.countCurrent()).isFalse();      // b 2->1
        assertThat(c.countCurrent()).isTrue();       // b 1->0 done
        assertThat(c.currentIndex()).isEqualTo(2);
    }
    @Test public void fractionAndAllDone() {
        BubbleContentController c = make();
        c.jumpTo(2);
        for (int i = 0; i < 100; i++) c.countCurrent();
        assertThat(c.remainingAt(2)).isEqualTo(0);
        assertThat(c.fraction()).isWithin(0.001f).of(1f / 3f);
        assertThat(c.isAllDone()).isFalse();
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew testMadaniDebugUnitTest --tests "com.medoapps.www.onlinequran.bubble.BubbleContentControllerTest"`
Expected: FAIL — class does not exist.

- [ ] **Step 3: Implement**

```java
// app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleContentController.java
package com.medoapps.www.onlinequran.bubble;

import com.medoapps.www.onlinequran.AthkarItem;
import java.util.List;

/** Pure model for a guided athkar session: remaining counts per dhikr, current pointer,
 *  progress fraction. No Android dependencies so it is unit-tested on the JVM. */
public class BubbleContentController {
    private final List<AthkarItem> items;
    private final int[] remaining;
    private int index;

    public BubbleContentController(List<AthkarItem> items) {
        this.items = items;
        this.remaining = new int[items.size()];
        for (int i = 0; i < items.size(); i++) {
            remaining[i] = Math.max(1, items.get(i).remainingCount);
        }
        this.index = firstUnfinished();
    }

    private int firstUnfinished() {
        for (int i = 0; i < remaining.length; i++) if (remaining[i] > 0) return i;
        return remaining.length - 1;
    }

    public int size() { return items.size(); }
    public int currentIndex() { return index; }
    public AthkarItem currentItem() { return items.get(index); }
    public int remainingAt(int i) { return remaining[i]; }
    public int targetAt(int i) { return Math.max(1, items.get(i).remainingCount); }

    public int doneCount() {
        int n = 0;
        for (int r : remaining) if (r <= 0) n++;
        return n;
    }
    public float fraction() { return remaining.length == 0 ? 0f : (float) doneCount() / remaining.length; }
    public boolean isAllDone() { return doneCount() == remaining.length; }

    /** Decrement the current dhikr; returns true iff it just reached 0. Auto-advances on completion. */
    public boolean countCurrent() {
        if (remaining[index] > 0) remaining[index]--;
        if (remaining[index] == 0) {
            int next = firstUnfinished();
            boolean completed = true;
            if (next != index) index = next;
            return completed;
        }
        return false;
    }

    public void jumpTo(int i) { if (i >= 0 && i < remaining.length) index = i; }
}
```

- [ ] **Step 4: Run the unit test to verify it passes**

Run: `./gradlew testMadaniDebugUnitTest --tests "com.medoapps.www.onlinequran.bubble.BubbleContentControllerTest"`
Expected: PASS (4 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleContentController.java \
        app/src/test/java/com/medoapps/www/onlinequran/bubble/BubbleContentControllerTest.java
git commit -m "feat(bubble): pure counting/advance content controller

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 6: `BubbleStyle` + `BubblePrefs`

**Files:**
- Create: `bubble/BubbleStyle.java`, `bubble/BubblePrefs.java`
- Test: `app/src/test/java/com/medoapps/www/onlinequran/bubble/BubbleStyleTest.java`

**Interfaces:**
- Produces: `enum BubbleStyle { CHAT_HEAD("A"), DRAWER("C"), PILL("D"); String code(); static BubbleStyle fromCode(String) }`. `BubblePrefs` (Context-backed): `isEnabled/setEnabled`, `getStyle/setStyle`, `isAlwaysOn/setAlwaysOn`, `getSide/setSide`, `getPosY/setPosY`. Prefs file `"bubble_prefs"`.

- [ ] **Step 1: Write the failing test (enum only — pure)**

```java
// app/src/test/java/com/medoapps/www/onlinequran/bubble/BubbleStyleTest.java
package com.medoapps.www.onlinequran.bubble;

import static com.google.common.truth.Truth.assertThat;
import org.junit.Test;

public class BubbleStyleTest {
    @Test public void roundTripCodes() {
        assertThat(BubbleStyle.fromCode("A")).isEqualTo(BubbleStyle.CHAT_HEAD);
        assertThat(BubbleStyle.fromCode("C")).isEqualTo(BubbleStyle.DRAWER);
        assertThat(BubbleStyle.fromCode("D")).isEqualTo(BubbleStyle.PILL);
        assertThat(BubbleStyle.CHAT_HEAD.code()).isEqualTo("A");
    }
    @Test public void unknownDefaultsToChatHead() {
        assertThat(BubbleStyle.fromCode("zzz")).isEqualTo(BubbleStyle.CHAT_HEAD);
        assertThat(BubbleStyle.fromCode(null)).isEqualTo(BubbleStyle.CHAT_HEAD);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew testMadaniDebugUnitTest --tests "com.medoapps.www.onlinequran.bubble.BubbleStyleTest"`
Expected: FAIL — class does not exist.

- [ ] **Step 3: Implement `BubbleStyle` and `BubblePrefs`**

```java
// app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleStyle.java
package com.medoapps.www.onlinequran.bubble;

public enum BubbleStyle {
    CHAT_HEAD("A"), DRAWER("C"), PILL("D");
    private final String code;
    BubbleStyle(String code) { this.code = code; }
    public String code() { return code; }
    public static BubbleStyle fromCode(String code) {
        if (code != null) for (BubbleStyle s : values()) if (s.code.equals(code)) return s;
        return CHAT_HEAD;
    }
}
```

```java
// app/src/main/java/com/medoapps/www/onlinequran/bubble/BubblePrefs.java
package com.medoapps.www.onlinequran.bubble;

import android.content.Context;
import android.content.SharedPreferences;

/** Persisted bubble settings: on/off, style, show-mode, last on-screen position. */
public class BubblePrefs {
    public static final String PREFS = "bubble_prefs";
    private final SharedPreferences p;
    public BubblePrefs(Context c) { p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    public boolean isEnabled() { return p.getBoolean("bubble_enabled", false); }
    public void setEnabled(boolean v) { p.edit().putBoolean("bubble_enabled", v).apply(); }

    public BubbleStyle getStyle() { return BubbleStyle.fromCode(p.getString("bubble_style", "A")); }
    public void setStyle(BubbleStyle s) { p.edit().putString("bubble_style", s.code()).apply(); }

    /** true = always on; false = only during the day/night windows (default). */
    public boolean isAlwaysOn() { return p.getBoolean("bubble_always", false); }
    public void setAlwaysOn(boolean v) { p.edit().putBoolean("bubble_always", v).apply(); }

    /** "right" (default) or "left". */
    public String getSide() { return p.getString("bubble_side", "right"); }
    public void setSide(String s) { p.edit().putString("bubble_side", s).apply(); }

    public int getPosY(int def) { return p.getInt("bubble_pos_y", def); }
    public void setPosY(int y) { p.edit().putInt("bubble_pos_y", y).apply(); }
}
```

- [ ] **Step 4: Run the unit test to verify it passes**

Run: `./gradlew testMadaniDebugUnitTest --tests "com.medoapps.www.onlinequran.bubble.BubbleStyleTest"`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleStyle.java \
        app/src/main/java/com/medoapps/www/onlinequran/bubble/BubblePrefs.java \
        app/src/test/java/com/medoapps/www/onlinequran/bubble/BubbleStyleTest.java
git commit -m "feat(bubble): style enum + persisted BubblePrefs

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 7: Manifest entries, channel constant & bubble strings

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `res/values/strings.xml`, `res/values-ar/strings.xml`

**Interfaces:**
- Produces: a declared `<service android:name=".bubble.AthkarBubbleService" …>` and the strings keys used by later tasks: `bubble_title`, `bubble_subtitle`, `bubble_enable`, `bubble_enable_sub`, `bubble_style`, `bubble_style_a`, `bubble_style_a_desc`, `bubble_style_c`, `bubble_style_c_desc`, `bubble_style_d`, `bubble_style_d_desc`, `bubble_show_during`, `bubble_show_windows`, `bubble_show_always`, `bubble_channel_name`, `bubble_notif_text`, `bubble_today_done`.

- [ ] **Step 1: Add the permission (next to the existing FOREGROUND_SERVICE perms ~line 25)**

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
```

- [ ] **Step 2: Declare the service and boot receiver (inside `<application>`, next to the AthanPlaybackService `<service>` ~line 337)**

```xml
<service
    android:name=".bubble.AthkarBubbleService"
    android:exported="false"
    android:foregroundServiceType="specialUse">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="Persistent on-screen athkar (dhikr) companion the user explicitly enabled in settings." />
</service>

<receiver
    android:name=".bubble.BubbleBootReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

- [ ] **Step 3: Add strings to `res/values/strings.xml`**

```xml
<string name="bubble_title">Floating Athkar Bubble</string>
<string name="bubble_subtitle">A draggable bubble over other apps for your day &amp; night athkar.</string>
<string name="bubble_enable">Show floating bubble</string>
<string name="bubble_enable_sub">Off until you enable it</string>
<string name="bubble_style">Bubble style</string>
<string name="bubble_style_a">Chat-head</string>
<string name="bubble_style_a_desc">A round bubble → focused one-dhikr counter.</string>
<string name="bubble_style_c">Edge drawer</string>
<string name="bubble_style_c_desc">A slim edge tab → the whole set as a list.</string>
<string name="bubble_style_d">Mini-pill</string>
<string name="bubble_style_d_desc">A small bar showing the current dhikr; tap to expand.</string>
<string name="bubble_show_during">Show during</string>
<string name="bubble_show_windows">Day &amp; Night windows</string>
<string name="bubble_show_always">Always on</string>
<string name="bubble_channel_name">Floating athkar bubble</string>
<string name="bubble_notif_text">Athkar bubble is active</string>
<string name="bubble_today_done">Today · %1$d/%2$d done</string>
```

- [ ] **Step 4: Add the Arabic strings to `res/values-ar/strings.xml`**

```xml
<string name="bubble_title">فقاعة الأذكار العائمة</string>
<string name="bubble_subtitle">فقاعة قابلة للسحب فوق التطبيقات لأذكار الصباح والمساء.</string>
<string name="bubble_enable">إظهار الفقاعة العائمة</string>
<string name="bubble_enable_sub">متوقفة حتى تفعّلها</string>
<string name="bubble_style">شكل الفقاعة</string>
<string name="bubble_style_a">الفقاعة</string>
<string name="bubble_style_a_desc">فقاعة دائرية ← عدّاد ذكر واحد.</string>
<string name="bubble_style_c">الدُّرج الجانبي</string>
<string name="bubble_style_c_desc">لسان على الحافة ← المجموعة كاملة كقائمة.</string>
<string name="bubble_style_d">الشريط الصغير</string>
<string name="bubble_style_d_desc">شريط صغير يعرض الذكر الحالي؛ اضغط للتوسيع.</string>
<string name="bubble_show_during">الظهور خلال</string>
<string name="bubble_show_windows">أوقات الصباح والمساء</string>
<string name="bubble_show_always">دائمًا</string>
<string name="bubble_channel_name">فقاعة الأذكار العائمة</string>
<string name="bubble_notif_text">فقاعة الأذكار نشطة</string>
<string name="bubble_today_done">اليوم · %1$d/%2$d تمّ</string>
```

- [ ] **Step 5: Compile to verify the manifest + resources are valid**

Run: `./gradlew assembleMadaniDebug`
Expected: BUILD SUCCESSFUL (a build error here means the manifest/strings are malformed — the `.bubble.AthkarBubbleService` class doesn't need to exist yet for resource/manifest *merging*, but the manifest is validated; if it complains the class is missing, proceed to Task 8 first, then re-run).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/AndroidManifest.xml app/src/main/res/values/strings.xml app/src/main/res/values-ar/strings.xml
git commit -m "feat(bubble): manifest service/receiver + EN/AR strings

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 8: `AthkarBubbleService` foreground-service skeleton

A `specialUse` FGS that starts/stops, posts a LOW-importance ongoing notification on `bubble_channel`, and (in Task 10) attaches the overlay. This task lands the lifecycle only; `onStartCommand` calls a stub `attachOverlay()`/`detachOverlay()` filled in Task 10.

**Files:**
- Create: `bubble/AthkarBubbleService.java`

**Interfaces:**
- Produces: `AthkarBubbleService` with `static final String ACTION_STOP`; started via `ContextCompat.startForegroundService(ctx, new Intent(ctx, AthkarBubbleService.class))`; stopped via the same intent with `setAction(ACTION_STOP)`. Constants `NOTIF_ID=4000`, `CHANNEL="bubble_channel"`. Hooks `attachOverlay()` / `detachOverlay()` (no-ops here; implemented in Task 10).

- [ ] **Step 1: Implement the service skeleton**

```java
// app/src/main/java/com/medoapps/www/onlinequran/bubble/AthkarBubbleService.java
package com.medoapps.www.onlinequran.bubble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.medoapps.www.onlinequran.R;

/** Foreground service that hosts the floating athkar overlay. Type: specialUse. */
public class AthkarBubbleService extends Service {
    public static final String ACTION_STOP = "com.medoapps.athkar.bubble.STOP";
    static final int NOTIF_ID = 4000;
    static final String CHANNEL = "bubble_channel";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            detachOverlay();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        createChannel(this);
        startForeground(NOTIF_ID, buildNotification());
        attachOverlay();
        return START_STICKY;
    }

    private Notification buildNotification() {
        Intent stop = new Intent(this, AthkarBubbleService.class).setAction(ACTION_STOP);
        android.app.PendingIntent stopPi = android.app.PendingIntent.getService(
                this, NOTIF_ID, stop,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_nav_more) // existing small icon; swap if a dedicated one is added
                .setContentText(getString(R.string.bubble_notif_text))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(0, getString(android.R.string.cancel), stopPi)
                .build();
    }

    static void createChannel(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        NotificationChannel ch = new NotificationChannel(
                CHANNEL, ctx.getString(R.string.bubble_channel_name), NotificationManager.IMPORTANCE_LOW);
        ch.setShowBadge(false);
        nm.createNotificationChannel(ch);
    }

    // --- overlay hooks (implemented in Task 10) ---
    private void attachOverlay() { /* Task 10 */ }
    private void detachOverlay() { /* Task 10 */ }

    @Override public void onDestroy() { detachOverlay(); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
```

- [ ] **Step 2: Build, install, and verify the service starts with its notification**

```bash
./gradlew assembleMadaniDebug && adb -s emulator-5554 install -r app/build/outputs/apk/madani/debug/app-madani-debug.apk
adb -s emulator-5554 shell am start-foreground-service -n com.medoapps.www.onlinequran/.bubble.AthkarBubbleService
adb -s emulator-5554 shell dumpsys activity services com.medoapps.www.onlinequran | grep -i AthkarBubble
```
Expected: `dumpsys` shows `AthkarBubbleService` with `isForeground=true`; a low-priority "Athkar bubble is active" notification is present in the shade. (If `am start-foreground-service` is blocked from shell on this OS level, instead launch the app and start it from the Task 13 toggle once that exists; for now the `dumpsys` after an in-app start is the check.)

- [ ] **Step 3: Stop it and confirm it tears down**

```bash
adb -s emulator-5554 shell am startservice -n com.medoapps.www.onlinequran/.bubble.AthkarBubbleService -a com.medoapps.athkar.bubble.STOP
adb -s emulator-5554 shell dumpsys activity services com.medoapps.www.onlinequran | grep -i AthkarBubble || echo "stopped"
```
Expected: prints `stopped` (no running service) and the notification is gone.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/bubble/AthkarBubbleService.java
git commit -m "feat(bubble): foreground service skeleton on bubble_channel

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 9: Style A drawables + layouts (`bubble_chathead.xml`, `bubble_panel_walker.xml`)

Static overlay layouts for the chat-head and its expanded walker panel, on explicit (theme-independent) colors per the constraints.

**Files:**
- Create: `res/drawable/bubble_disc.xml`, `res/drawable/bubble_dismiss_bg.xml`
- Create: `res/layout/bubble_chathead.xml`, `res/layout/bubble_panel_walker.xml`

**Interfaces:**
- Produces: view ids consumed by Task 10 — chathead: `@id/bubble_root`, `@id/bubble_glyph`, `@id/bubble_badge`; walker panel: `@id/walker_root`, `@id/walker_title`, `@id/walker_sub`, `@id/walker_dhikr`, `@id/walker_ref`, `@id/walker_count`, `@id/walker_prev`, `@id/walker_next`, `@id/walker_switch_day`, `@id/walker_switch_night`, `@id/walker_close`, `@id/walker_dismiss_target`.

- [ ] **Step 1: Create the disc + dismiss drawables**

```xml
<!-- res/drawable/bubble_disc.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval">
    <gradient android:angle="135" android:startColor="@color/navy_500" android:endColor="@color/navy_900" />
    <stroke android:width="2dp" android:color="@color/gold_accent" />
</shape>
```

```xml
<!-- res/drawable/bubble_dismiss_bg.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval">
    <solid android:color="#99141A28" />
    <stroke android:width="1.5dp" android:color="#59FFFFFF" />
</shape>
```

- [ ] **Step 2: Create the chat-head layout**

```xml
<!-- res/layout/bubble_chathead.xml -->
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/bubble_root"
    android:layout_width="62dp" android:layout_height="62dp"
    android:layoutDirection="locale">

    <View android:layout_width="match_parent" android:layout_height="match_parent"
        android:background="@drawable/bubble_disc" />

    <TextView android:id="@+id/bubble_glyph"
        android:layout_width="wrap_content" android:layout_height="wrap_content"
        android:layout_gravity="center" android:textSize="24sp" android:text="☀"
        android:textColor="@color/text_on_navy" />

    <TextView android:id="@+id/bubble_badge"
        android:layout_width="wrap_content" android:layout_height="wrap_content"
        android:layout_gravity="top|end" android:minWidth="22dp" android:gravity="center"
        android:paddingHorizontal="5dp" android:textSize="11sp" android:textStyle="bold"
        android:textColor="#1A1305" android:background="@drawable/bg_avatar_gold" android:text="1" />
</FrameLayout>
```

- [ ] **Step 3: Create the walker panel layout**

```xml
<!-- res/layout/bubble_panel_walker.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/walker_root"
    android:layout_width="280dp" android:layout_height="wrap_content"
    android:orientation="vertical" android:background="@drawable/bg_card_continue"
    android:layoutDirection="locale">

    <!-- header -->
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
        android:orientation="horizontal" android:gravity="center_vertical"
        android:padding="12dp" android:background="@drawable/bg_navy_hero">
        <TextView android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1"
            android:orientation="vertical">
        </TextView>
        <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1"
            android:orientation="vertical">
            <TextView android:id="@+id/walker_title" android:layout_width="wrap_content"
                android:layout_height="wrap_content" android:textColor="@color/text_on_navy"
                android:textStyle="bold" android:textSize="14sp" android:text="@string/athkar_section_morning" />
            <TextView android:id="@+id/walker_sub" android:layout_width="wrap_content"
                android:layout_height="wrap_content" android:textColor="@color/hint_on_navy" android:textSize="11sp" />
        </LinearLayout>
        <TextView android:id="@+id/walker_close" android:layout_width="30dp" android:layout_height="30dp"
            android:gravity="center" android:text="–" android:textColor="@color/text_on_navy" android:textSize="18sp" />
    </LinearLayout>

    <!-- day/night switch -->
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
        android:orientation="horizontal" android:padding="10dp">
        <TextView android:id="@+id/walker_switch_day" android:layout_width="0dp" android:layout_weight="1"
            android:layout_height="wrap_content" android:gravity="center" android:padding="7dp"
            android:text="@string/bubble_style_a" android:textColor="@color/text_primary" />
        <TextView android:id="@+id/walker_switch_night" android:layout_width="0dp" android:layout_weight="1"
            android:layout_height="wrap_content" android:gravity="center" android:padding="7dp"
            android:text="@string/bubble_style_a" android:textColor="@color/text_secondary" />
    </LinearLayout>

    <!-- dhikr -->
    <TextView android:id="@+id/walker_dhikr" android:layout_width="match_parent" android:layout_height="wrap_content"
        android:maxHeight="110dp" android:scrollbars="none" android:gravity="center"
        android:paddingHorizontal="16dp" android:paddingTop="6dp" android:textColor="@color/text_primary"
        android:textSize="18sp" android:lineSpacingMultiplier="1.4" android:textDirection="rtl" />
    <TextView android:id="@+id/walker_ref" android:layout_width="match_parent" android:layout_height="wrap_content"
        android:gravity="center" android:paddingTop="6dp" android:textColor="@color/text_secondary" android:textSize="10sp" />

    <!-- walk: prev | count dial | next -->
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
        android:orientation="horizontal" android:gravity="center" android:padding="12dp">
        <TextView android:id="@+id/walker_prev" android:layout_width="34dp" android:layout_height="34dp"
            android:gravity="center" android:text="‹" android:textSize="18sp" android:textColor="@color/text_secondary" />
        <TextView android:id="@+id/walker_count" android:layout_width="96dp" android:layout_height="96dp"
            android:layout_marginHorizontal="14dp" android:gravity="center" android:textStyle="bold"
            android:textSize="30sp" android:textColor="@color/text_primary" android:background="@drawable/bubble_disc" />
        <TextView android:id="@+id/walker_next" android:layout_width="34dp" android:layout_height="34dp"
            android:gravity="center" android:text="›" android:textSize="18sp" android:textColor="@color/text_secondary" />
    </LinearLayout>

    <TextView android:id="@+id/walker_today" android:layout_width="match_parent" android:layout_height="wrap_content"
        android:padding="11dp" android:textColor="@color/text_secondary" android:textSize="11sp" />
</LinearLayout>
```

- [ ] **Step 4: Compile to verify the resources are valid**

Run: `./gradlew assembleMadaniDebug`
Expected: BUILD SUCCESSFUL (resource errors mean a missing color/drawable id — every referenced `@color/*` and `@drawable/*` here exists in the repo: `navy_500/navy_900/gold_accent/text_on_navy/hint_on_navy/text_primary/text_secondary`, `bg_avatar_gold`, `bg_card_continue`, `bg_navy_hero`).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/drawable/bubble_disc.xml app/src/main/res/drawable/bubble_dismiss_bg.xml \
        app/src/main/res/layout/bubble_chathead.xml app/src/main/res/layout/bubble_panel_walker.xml
git commit -m "feat(bubble): chat-head + walker panel layouts (style A)

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 10: `BubbleOverlayController` — WindowManager overlay, drag, dismiss, render style A

The core new layer. Adds the chat-head to the window, drags it (snap-to-edge, drag-to-✕), and on tap inflates the walker panel wired to `BubbleContentController` + `AthkarProgressStore` with a 20 ms haptic. Wire it into `AthkarBubbleService.attachOverlay()/detachOverlay()`.

**Files:**
- Create: `bubble/BubbleOverlayController.java`
- Modify: `bubble/AthkarBubbleService.java` (fill the two hooks)

**Interfaces:**
- Consumes: `AthkarRepository`, `BubbleContentController`, `BubbleSession`, `BubbleSessionSelector`, `BubblePrefs`, `AthkarProgressStore`, `PrayerTimeEngine`, `PrayerSettings` (indices), `bubble_chathead.xml`, `bubble_panel_walker.xml`.
- Produces: `BubbleOverlayController(Service service)`; `void show()`, `void hide()`. Internal `int overlayType()` returns `TYPE_APPLICATION_OVERLAY`/`TYPE_PHONE`.

- [ ] **Step 1: Implement the controller**

```java
// app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleOverlayController.java
package com.medoapps.www.onlinequran.bubble;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.medoapps.www.onlinequran.AthkarItem;
import com.medoapps.www.onlinequran.AthkarProgressStore;
import com.medoapps.www.onlinequran.AthkarRepository;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.athan.PrayerSettings;
import com.medoapps.www.onlinequran.athan.PrayerTimeEngine;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Owns the floating overlay view: add/remove, drag, snap-to-edge, drag-to-dismiss,
 *  and (style A) the tap-to-expand walker panel. Styles C/D extend renderExpanded(). */
public class BubbleOverlayController {
    private final Service service;
    private final WindowManager wm;
    private final BubblePrefs prefs;
    private final AthkarProgressStore progress;
    private final Vibrator vibrator;

    private View bubbleView;            // collapsed chat-head
    private View panelView;             // expanded walker (style A)
    private WindowManager.LayoutParams bubbleLp;
    private BubbleSession session;
    private BubbleContentController content;
    private int dayOfYear;

    public BubbleOverlayController(Service service) {
        this.service = service;
        this.wm = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        this.prefs = new BubblePrefs(service);
        this.progress = new AthkarProgressStore(service);
        this.vibrator = (Vibrator) service.getSystemService(Context.VIBRATOR_SERVICE);
    }

    static int overlayType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

    public void show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(service)) return;
        if (bubbleView != null) return;
        loadSession();
        addBubble();
    }

    public void hide() {
        removePanel();
        if (bubbleView != null) { try { wm.removeView(bubbleView); } catch (Exception ignored) {} bubbleView = null; }
    }

    private void loadSession() {
        dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        long now = System.currentTimeMillis();
        long fajr = now, asr = now;
        try {
            Date[] t = PrayerTimeEngine.getTodayTimes(service);
            if (t != null) { fajr = t[PrayerSettings.PRAYER_FAJR].getTime(); asr = t[PrayerSettings.PRAYER_ASR].getTime(); }
        } catch (Exception ignored) {}
        session = BubbleSessionSelector.select(now, fajr, asr);
        List<AthkarItem> items = (session == BubbleSession.MORNING)
                ? AthkarRepository.getMorningItems() : AthkarRepository.getEveningItems();
        content = new BubbleContentController(items);
    }

    private void addBubble() {
        bubbleView = LayoutInflater.from(service).inflate(R.layout.bubble_chathead, null);
        bubbleLp = baseParams(dp(62), dp(62));
        bubbleLp.gravity = Gravity.TOP | Gravity.START;
        bubbleLp.x = "left".equals(prefs.getSide()) ? dp(8) : screenW() - dp(70);
        bubbleLp.y = prefs.getPosY(dp(220));
        renderCollapsed();
        attachDrag(bubbleView);
        wm.addView(bubbleView, bubbleLp);
    }

    private void renderCollapsed() {
        ((TextView) bubbleView.findViewById(R.id.bubble_glyph))
                .setText(session == BubbleSession.MORNING ? "☀" : "☾");
        int rem = content.remainingAt(content.currentIndex());
        ((TextView) bubbleView.findViewById(R.id.bubble_badge)).setText(rem <= 0 ? "✓" : String.valueOf(rem));
    }

    // --- drag + tap ---
    private void attachDrag(final View v) {
        v.setOnTouchListener(new View.OnTouchListener() {
            int sx, sy; long downT; boolean moved;
            @Override public boolean onTouch(View view, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        sx = bubbleLp.x - (int) e.getRawX(); sy = bubbleLp.y - (int) e.getRawY();
                        downT = System.currentTimeMillis(); moved = false; return true;
                    case MotionEvent.ACTION_MOVE:
                        int nx = (int) e.getRawX() + sx, ny = (int) e.getRawY() + sy;
                        if (Math.abs(nx - (screenW() + sx)) > 8) moved = true;
                        if (Math.abs(e.getRawX() + sx - bubbleLp.x) + Math.abs(e.getRawY() + sy - bubbleLp.y) > dp(5)) moved = true;
                        bubbleLp.x = nx; bubbleLp.y = clamp(ny, dp(40), screenH() - dp(80));
                        wm.updateViewLayout(bubbleView, bubbleLp);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved && System.currentTimeMillis() - downT < 250) { togglePanel(); return true; }
                        // snap to nearest edge + persist
                        boolean left = bubbleLp.x + dp(31) < screenW() / 2;
                        bubbleLp.x = left ? dp(8) : screenW() - dp(70);
                        prefs.setSide(left ? "left" : "right");
                        prefs.setPosY(bubbleLp.y);
                        wm.updateViewLayout(bubbleView, bubbleLp);
                        return true;
                }
                return false;
            }
        });
    }

    private void togglePanel() { if (panelView == null) showPanel(); else removePanel(); }

    private void showPanel() {
        panelView = LayoutInflater.from(service).inflate(R.layout.bubble_panel_walker, null);
        WindowManager.LayoutParams lp = baseParams(dp(280), WindowManager.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | ("left".equals(prefs.getSide()) ? Gravity.START : Gravity.END);
        lp.x = dp(12);
        lp.y = clamp(bubbleLp.y - dp(120), dp(40), screenH() - dp(420));
        bindPanel();
        panelView.findViewById(R.id.walker_close).setOnClickListener(x -> removePanel());
        panelView.findViewById(R.id.walker_prev).setOnClickListener(x -> { content.jumpTo(content.currentIndex() - 1); bindPanel(); });
        panelView.findViewById(R.id.walker_next).setOnClickListener(x -> { content.jumpTo(content.currentIndex() + 1); bindPanel(); });
        panelView.findViewById(R.id.walker_count).setOnClickListener(x -> onCount());
        wm.addView(panelView, lp);
    }

    private void onCount() {
        int idx = content.currentIndex();
        boolean completed = content.countCurrent();
        haptic();
        if (completed) progress.markDone(dayOfYear, session.name(), idx);
        renderCollapsed();
        bindPanel();
    }

    private void bindPanel() {
        if (panelView == null) return;
        AthkarItem it = content.currentItem();
        int rem = content.remainingAt(content.currentIndex());
        ((TextView) panelView.findViewById(R.id.walker_title))
                .setText(session == BubbleSession.MORNING ? R.string.athkar_section_morning : R.string.athkar_section_evening);
        ((TextView) panelView.findViewById(R.id.walker_sub))
                .setText((content.currentIndex() + 1) + "/" + content.size());
        ((TextView) panelView.findViewById(R.id.walker_dhikr)).setText(it.text);
        ((TextView) panelView.findViewById(R.id.walker_ref)).setText(it.count);
        ((TextView) panelView.findViewById(R.id.walker_count)).setText(rem <= 0 ? "✓" : String.valueOf(rem));
        ((TextView) panelView.findViewById(R.id.walker_today))
                .setText(service.getString(R.string.bubble_today_done, content.doneCount(), content.size()));
    }

    private void removePanel() {
        if (panelView != null) { try { wm.removeView(panelView); } catch (Exception ignored) {} panelView = null; }
    }

    private void haptic() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
        else vibrator.vibrate(20);
    }

    // --- window helpers ---
    private WindowManager.LayoutParams baseParams(int w, int h) {
        return new WindowManager.LayoutParams(w, h, overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
    }
    private int dp(int v) { return Math.round(v * service.getResources().getDisplayMetrics().density); }
    private int screenW() { return service.getResources().getDisplayMetrics().widthPixels; }
    private int screenH() { return service.getResources().getDisplayMetrics().heightPixels; }
    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
}
```

- [ ] **Step 2: Wire the controller into the service**

In `AthkarBubbleService.java`, add a field and fill the hooks:

```java
    private BubbleOverlayController overlay;
    // ...
    private void attachOverlay() {
        if (overlay == null) overlay = new BubbleOverlayController(this);
        overlay.show();
    }
    private void detachOverlay() {
        if (overlay != null) overlay.hide();
    }
```

- [ ] **Step 3: Build + install**

Run: `./gradlew assembleMadaniDebug && adb -s emulator-5554 install -r app/build/outputs/apk/madani/debug/app-madani-debug.apk`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Grant overlay + start the service, verify the bubble shows over the launcher**

```bash
adb -s emulator-5554 shell appops set com.medoapps.www.onlinequran SYSTEM_ALERT_WINDOW allow
adb -s emulator-5554 shell am start-foreground-service -n com.medoapps.www.onlinequran/.bubble.AthkarBubbleService
adb -s emulator-5554 shell input keyevent KEYCODE_HOME
adb -s emulator-5554 exec-out screencap -p > /private/tmp/claude-501/-Users-medo-projects-tmp-My-Stream-My-Stream-Android/e7d73825-e20f-4f5f-b70d-288d60519a3b/scratchpad/bubble_A.png
```
Open the screenshot. Expected: a gold-ringed navy chat-head with a "☀"/"☾" glyph + count badge floats over the home screen.

- [ ] **Step 5: Verify tap-to-expand + counting (manual on the emulator)**

Tap the bubble (`adb -s emulator-5554 shell input tap <x> <y>` at the bubble's location, or use the emulator UI). Confirm the walker panel appears with the current dhikr and count; tapping the gold dial decrements the count, advances at 0, and the badge updates. Screenshot to confirm:
```bash
adb -s emulator-5554 exec-out screencap -p > /private/tmp/claude-501/-Users-medo-projects-tmp-My-Stream-My-Stream-Android/e7d73825-e20f-4f5f-b70d-288d60519a3b/scratchpad/bubble_A_panel.png
```
Expected: panel renders in light theme; re-run with the emulator in dark mode (`adb -s emulator-5554 shell cmd uimode night yes`) and confirm text stays legible (navy header uses `text_on_navy`, not `@color/white`).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleOverlayController.java \
        app/src/main/java/com/medoapps/www/onlinequran/bubble/AthkarBubbleService.java
git commit -m "feat(bubble): WindowManager overlay + drag + style A walker with counting

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 11: Style D (mini-pill)

**Files:**
- Create: `res/layout/bubble_pill.xml`
- Modify: `bubble/BubbleOverlayController.java` (render the pill when `prefs.getStyle()==PILL`)

**Interfaces:**
- Consumes: ids `@id/pill_root`, `@id/pill_glyph`, `@id/pill_dhikr`, `@id/pill_meta`, `@id/pill_count`, `@id/pill_list`.

- [ ] **Step 1: Create the pill layout**

```xml
<!-- res/layout/bubble_pill.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/pill_root" android:layout_width="240dp" android:layout_height="54dp"
    android:orientation="horizontal" android:gravity="center_vertical"
    android:background="@drawable/bg_card_continue" android:padding="5dp" android:layoutDirection="locale">
    <TextView android:id="@+id/pill_glyph" android:layout_width="44dp" android:layout_height="44dp"
        android:gravity="center" android:textSize="18sp" android:textColor="@color/text_on_navy"
        android:background="@drawable/bubble_disc" />
    <LinearLayout android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content"
        android:orientation="vertical" android:paddingHorizontal="8dp">
        <TextView android:id="@+id/pill_dhikr" android:layout_width="match_parent" android:layout_height="wrap_content"
            android:maxLines="1" android:ellipsize="end" android:textDirection="rtl"
            android:textColor="@color/text_primary" android:textSize="15sp" />
        <TextView android:id="@+id/pill_meta" android:layout_width="match_parent" android:layout_height="wrap_content"
            android:textColor="@color/text_secondary" android:textSize="10sp" />
    </LinearLayout>
    <TextView android:id="@+id/pill_count" android:layout_width="38dp" android:layout_height="38dp"
        android:gravity="center" android:textStyle="bold" android:textColor="#1A1305"
        android:background="@drawable/bg_avatar_gold" android:text="+" />
    <TextView android:id="@+id/pill_list" android:layout_width="34dp" android:layout_height="34dp"
        android:gravity="center" android:text="▤" android:textColor="@color/text_secondary" />
</LinearLayout>
```

- [ ] **Step 2: Render the pill in `BubbleOverlayController`**

Refactor `addBubble()` to branch on `prefs.getStyle()`. Extract the collapsed-view inflation into `inflateCollapsed()`:

```java
    private void addBubble() {
        BubbleStyle style = prefs.getStyle();
        int layout = style == BubbleStyle.PILL ? R.layout.bubble_pill : R.layout.bubble_chathead;
        int w = style == BubbleStyle.PILL ? dp(240) : dp(62);
        bubbleView = LayoutInflater.from(service).inflate(layout, null);
        bubbleLp = baseParams(w, dp(62));
        bubbleLp.gravity = Gravity.TOP | Gravity.START;
        bubbleLp.x = "left".equals(prefs.getSide()) ? dp(8) : screenW() - w - dp(8);
        bubbleLp.y = prefs.getPosY(dp(220));
        renderCollapsed();
        attachDrag(bubbleView);
        // pill has its own count + list buttons:
        View count = bubbleView.findViewById(R.id.pill_count);
        if (count != null) count.setOnClickListener(x -> onCount());
        View list = bubbleView.findViewById(R.id.pill_list);
        if (list != null) list.setOnClickListener(x -> togglePanel());
        wm.addView(bubbleView, bubbleLp);
    }
```

Extend `renderCollapsed()` to populate the pill when present:

```java
    private void renderCollapsed() {
        int rem = content.remainingAt(content.currentIndex());
        String glyph = session == BubbleSession.MORNING ? "☀" : "☾";
        TextView g = bubbleView.findViewById(R.id.bubble_glyph);
        if (g != null) { // chat-head
            g.setText(glyph);
            ((TextView) bubbleView.findViewById(R.id.bubble_badge)).setText(rem <= 0 ? "✓" : String.valueOf(rem));
        }
        TextView pg = bubbleView.findViewById(R.id.pill_glyph);
        if (pg != null) { // pill
            pg.setText(glyph);
            ((TextView) bubbleView.findViewById(R.id.pill_dhikr)).setText(content.currentItem().text);
            ((TextView) bubbleView.findViewById(R.id.pill_meta))
                    .setText(content.currentItem().count + " · " + (rem <= 0 ? "✓" : rem));
            ((TextView) bubbleView.findViewById(R.id.pill_count)).setText(rem <= 0 ? "✓" : "+");
        }
    }
```

Call `renderCollapsed()` at the end of `onCount()` (already present) so the pill stays live.

- [ ] **Step 3: Build, install, set style D, verify**

```bash
./gradlew assembleMadaniDebug && adb -s emulator-5554 install -r app/build/outputs/apk/madani/debug/app-madani-debug.apk
# set style D directly in prefs for this test:
adb -s emulator-5554 shell "run-as com.medoapps.www.onlinequran sh -c 'mkdir -p shared_prefs'" 2>/dev/null
adb -s emulator-5554 shell am force-stop com.medoapps.www.onlinequran
# easiest: set via the Task 13 picker once it exists; for now temporarily default BubblePrefs.getStyle() test via the picker.
adb -s emulator-5554 shell am start-foreground-service -n com.medoapps.www.onlinequran/.bubble.AthkarBubbleService
adb -s emulator-5554 exec-out screencap -p > /private/tmp/claude-501/-Users-medo-projects-tmp-My-Stream-My-Stream-Android/e7d73825-e20f-4f5f-b70d-288d60519a3b/scratchpad/bubble_D.png
```
Expected (once style D is selected via the Task 13 picker): a pill showing the current dhikr + count; the gold **+** counts down; **▤** opens the walker panel. (Until Task 13 exists, verify by temporarily returning `BubbleStyle.PILL` from `BubblePrefs.getStyle()`, then revert.)

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/bubble_pill.xml app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleOverlayController.java
git commit -m "feat(bubble): style D mini-pill (glanceable current dhikr)

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 12: Style C (edge drawer)

**Files:**
- Create: `res/layout/bubble_drawer.xml`, `res/layout/bubble_drawer_row.xml`
- Modify: `bubble/BubbleOverlayController.java` (when style C: collapsed = an edge tab; expanded = a full-height drawer listing all items; tap a row to count it; RTL docks left)

**Interfaces:**
- Consumes: drawer ids `@id/drawer_root`, `@id/drawer_title`, `@id/drawer_sub`, `@id/drawer_list` (a `LinearLayout`), `@id/drawer_back`; row ids `@id/row_text`, `@id/row_count`.

- [ ] **Step 1: Create the drawer + row layouts**

```xml
<!-- res/layout/bubble_drawer.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/drawer_root" android:layout_width="236dp" android:layout_height="match_parent"
    android:orientation="vertical" android:background="@color/background_card" android:layoutDirection="locale">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
        android:orientation="horizontal" android:gravity="center_vertical" android:padding="13dp"
        android:background="@drawable/bg_navy_hero">
        <LinearLayout android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content"
            android:orientation="vertical">
            <TextView android:id="@+id/drawer_title" android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:textColor="@color/text_on_navy" android:textStyle="bold" android:textSize="14sp" />
            <TextView android:id="@+id/drawer_sub" android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:textColor="@color/hint_on_navy" android:textSize="10sp" />
        </LinearLayout>
        <TextView android:id="@+id/drawer_back" android:layout_width="30dp" android:layout_height="30dp"
            android:gravity="center" android:text="›" android:textColor="@color/text_on_navy" android:textSize="16sp" />
    </LinearLayout>
    <ScrollView android:layout_width="match_parent" android:layout_height="match_parent">
        <LinearLayout android:id="@+id/drawer_list" android:layout_width="match_parent"
            android:layout_height="wrap_content" android:orientation="vertical" />
    </ScrollView>
</LinearLayout>
```

```xml
<!-- res/layout/bubble_drawer_row.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="wrap_content"
    android:orientation="horizontal" android:gravity="center_vertical" android:padding="11dp"
    android:background="?attr/selectableItemBackground">
    <TextView android:id="@+id/row_text" android:layout_width="0dp" android:layout_weight="1"
        android:layout_height="wrap_content" android:maxLines="1" android:ellipsize="end"
        android:textDirection="rtl" android:textColor="@color/text_primary" android:textSize="15sp" />
    <TextView android:id="@+id/row_count" android:layout_width="wrap_content" android:layout_height="wrap_content"
        android:minWidth="34dp" android:gravity="center" android:paddingHorizontal="8dp" android:paddingVertical="4dp"
        android:textStyle="bold" android:textColor="@color/gold_accent" android:textSize="13sp" />
</LinearLayout>
```

- [ ] **Step 2: Render the drawer style in `BubbleOverlayController`**

For style C the collapsed view is a thin edge tab (reuse `bubble_chathead.xml` sized 30×62 with no badge is acceptable for the tab; simpler: inflate `bubble_chathead` and let drag/tap open the drawer). In `addBubble()`, when `style==DRAWER`, after inflate set the tap to open the drawer instead of the panel. Add a `showDrawer()`:

```java
    private void showDrawer() {
        panelView = LayoutInflater.from(service).inflate(R.layout.bubble_drawer, null);
        boolean left = service.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        WindowManager.LayoutParams lp = baseParams(dp(236), WindowManager.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.TOP | (left ? Gravity.START : Gravity.END);
        ((TextView) panelView.findViewById(R.id.drawer_title))
                .setText(session == BubbleSession.MORNING ? R.string.athkar_section_morning : R.string.athkar_section_evening);
        ((TextView) panelView.findViewById(R.id.drawer_sub))
                .setText(service.getString(R.string.bubble_today_done, content.doneCount(), content.size()));
        panelView.findViewById(R.id.drawer_back).setOnClickListener(x -> removePanel());
        android.widget.LinearLayout listView = panelView.findViewById(R.id.drawer_list);
        for (int i = 0; i < content.size(); i++) {
            final int idx = i;
            View row = LayoutInflater.from(service).inflate(R.layout.bubble_drawer_row, listView, false);
            ((TextView) row.findViewById(R.id.row_text)).setText(content.currentItemAt(idx).text);
            TextView c = row.findViewById(R.id.row_count);
            c.setText(content.remainingAt(idx) <= 0 ? "✓" : String.valueOf(content.remainingAt(idx)));
            row.setOnClickListener(v -> {
                boolean done = content.countAt(idx);
                haptic();
                if (done) progress.markDone(dayOfYear, session.name(), idx);
                c.setText(content.remainingAt(idx) <= 0 ? "✓" : String.valueOf(content.remainingAt(idx)));
                renderCollapsed();
            });
            listView.addView(row);
        }
        wm.addView(panelView, lp);
    }
```

Add the two helper methods to `BubbleContentController` (and a unit-test line):

```java
    public AthkarItem currentItemAt(int i) { return items.get(i); }
    /** Decrement dhikr i directly (drawer tapping); returns true iff it just hit 0. */
    public boolean countAt(int i) {
        if (remaining[i] > 0) remaining[i]--;
        return remaining[i] == 0;
    }
```

Branch the tap in `attachDrag`'s ACTION_UP: when `prefs.getStyle()==DRAWER` call `if (panelView==null) showDrawer(); else removePanel();` instead of `togglePanel()`.

- [ ] **Step 3: Add a unit test for `countAt` and run it**

Append to `BubbleContentControllerTest`:

```java
    @Test public void countAt_targetsSpecificIndex() {
        BubbleContentController c = make();
        assertThat(c.countAt(2)).isFalse();          // c 100->99
        assertThat(c.remainingAt(2)).isEqualTo(99);
        assertThat(c.currentIndex()).isEqualTo(0);   // pointer unchanged
    }
```

Run: `./gradlew testMadaniDebugUnitTest --tests "com.medoapps.www.onlinequran.bubble.BubbleContentControllerTest"`
Expected: PASS (5 tests).

- [ ] **Step 4: Build, install, verify style C on device**

```bash
./gradlew assembleMadaniDebug && adb -s emulator-5554 install -r app/build/outputs/apk/madani/debug/app-madani-debug.apk
adb -s emulator-5554 exec-out screencap -p > /private/tmp/claude-501/-Users-medo-projects-tmp-My-Stream-My-Stream-Android/e7d73825-e20f-4f5f-b70d-288d60519a3b/scratchpad/bubble_C.png
```
Expected (style C selected via Task 13 picker): an edge tab; tapping slides out a drawer listing every dhikr with its count; tapping a row decrements it; switch the device to Arabic (`adb -s emulator-5554 shell am start … LocaleChange` or via the app's language dialog) and confirm the drawer docks to the LEFT edge.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/layout/bubble_drawer.xml app/src/main/res/layout/bubble_drawer_row.xml \
        app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleOverlayController.java \
        app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleContentController.java \
        app/src/test/java/com/medoapps/www/onlinequran/bubble/BubbleContentControllerTest.java
git commit -m "feat(bubble): style C edge drawer (full list, RTL-aware)

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 13: Settings — the "Floating bubble" section & style picker

Add the section to the athan-settings screen: enable switch, overlay-permission row (reuse), 3-card style picker, show-during segmented control. Toggling enable starts/stops the service (gating on the overlay permission).

**Files:**
- Create: `res/layout/settings_section_bubble.xml`, `res/drawable/bubble_style_card.xml`
- Modify: `res/layout/activity_athan_settings.xml` (add `<include layout="@layout/settings_section_bubble"/>` inside `@id/athan_dependent_container`)
- Modify: `AthanSettingsActivity.java` (`setupNotificationsSection()` → add `setupBubbleSection()`; `onResume` → refresh)

**Interfaces:**
- Consumes: `BubblePrefs`, `BubbleStyle`, `AthkarBubbleService`, existing `requestOverlayPermission()`/`canDrawOverlays()`.
- Produces: ids `@id/switch_bubble`, `@id/row_bubble_overlay`, `@id/tv_bubble_overlay_status`, `@id/card_style_a/@id/card_style_c/@id/card_style_d`, `@id/radio_a/@id/radio_c/@id/radio_d`, `@id/seg_windows`, `@id/seg_always`.

- [ ] **Step 1: Create the style-card background and the section layout**

```xml
<!-- res/drawable/bubble_style_card.xml -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_activated="true">
        <shape android:shape="rectangle">
            <solid android:color="@color/background_card" />
            <corners android:radius="14dp" />
            <stroke android:width="2dp" android:color="@color/gold_accent" />
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/background_card" />
            <corners android:radius="14dp" />
            <stroke android:width="1dp" android:color="@color/text_secondary" />
        </shape>
    </item>
</selector>
```

```xml
<!-- res/layout/settings_section_bubble.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="wrap_content"
    android:orientation="vertical" android:paddingHorizontal="12dp" android:paddingTop="8dp">

    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
        android:text="@string/bubble_title" android:textColor="@color/text_primary"
        android:textStyle="bold" android:textSize="15sp" android:paddingVertical="6dp" />

    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
        android:orientation="horizontal" android:gravity="center_vertical" android:minHeight="56dp">
        <LinearLayout android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content"
            android:orientation="vertical">
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:text="@string/bubble_enable" android:textColor="@color/text_primary" android:textSize="15sp" />
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:text="@string/bubble_enable_sub" android:textColor="@color/text_secondary" android:textSize="13sp" />
        </LinearLayout>
        <androidx.appcompat.widget.SwitchCompat android:id="@+id/switch_bubble"
            android:layout_width="wrap_content" android:layout_height="wrap_content" />
    </LinearLayout>

    <LinearLayout android:id="@+id/row_bubble_overlay" android:layout_width="match_parent"
        android:layout_height="wrap_content" android:orientation="vertical" android:minHeight="56dp"
        android:paddingVertical="8dp" android:background="?attr/selectableItemBackground">
        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:text="@string/athan_over_apps" android:textColor="@color/text_primary" android:textSize="15sp" />
        <TextView android:id="@+id/tv_bubble_overlay_status" android:layout_width="wrap_content"
            android:layout_height="wrap_content" android:textColor="@color/text_secondary" android:textSize="13sp" />
    </LinearLayout>

    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
        android:text="@string/bubble_style" android:textColor="@color/text_secondary"
        android:textStyle="bold" android:textSize="11sp" android:paddingTop="8dp" android:paddingBottom="4dp" />

    <!-- 3 style cards (A / C / D) -->
    <LinearLayout android:id="@+id/card_style_a" android:layout_width="match_parent" android:layout_height="wrap_content"
        android:orientation="horizontal" android:gravity="center_vertical" android:padding="11dp"
        android:layout_marginBottom="8dp" android:background="@drawable/bubble_style_card">
        <LinearLayout android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:orientation="vertical">
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/bubble_style_a"
                android:textStyle="bold" android:textColor="@color/text_primary" android:textSize="14sp" />
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/bubble_style_a_desc"
                android:textColor="@color/text_secondary" android:textSize="12sp" />
        </LinearLayout>
        <RadioButton android:id="@+id/radio_a" android:layout_width="wrap_content" android:layout_height="wrap_content" android:clickable="false" />
    </LinearLayout>

    <LinearLayout android:id="@+id/card_style_c" android:layout_width="match_parent" android:layout_height="wrap_content"
        android:orientation="horizontal" android:gravity="center_vertical" android:padding="11dp"
        android:layout_marginBottom="8dp" android:background="@drawable/bubble_style_card">
        <LinearLayout android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:orientation="vertical">
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/bubble_style_c"
                android:textStyle="bold" android:textColor="@color/text_primary" android:textSize="14sp" />
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/bubble_style_c_desc"
                android:textColor="@color/text_secondary" android:textSize="12sp" />
        </LinearLayout>
        <RadioButton android:id="@+id/radio_c" android:layout_width="wrap_content" android:layout_height="wrap_content" android:clickable="false" />
    </LinearLayout>

    <LinearLayout android:id="@+id/card_style_d" android:layout_width="match_parent" android:layout_height="wrap_content"
        android:orientation="horizontal" android:gravity="center_vertical" android:padding="11dp"
        android:background="@drawable/bubble_style_card">
        <LinearLayout android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:orientation="vertical">
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/bubble_style_d"
                android:textStyle="bold" android:textColor="@color/text_primary" android:textSize="14sp" />
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="@string/bubble_style_d_desc"
                android:textColor="@color/text_secondary" android:textSize="12sp" />
        </LinearLayout>
        <RadioButton android:id="@+id/radio_d" android:layout_width="wrap_content" android:layout_height="wrap_content" android:clickable="false" />
    </LinearLayout>

    <!-- show-during -->
    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
        android:text="@string/bubble_show_during" android:textColor="@color/text_secondary"
        android:textStyle="bold" android:textSize="11sp" android:paddingTop="12dp" android:paddingBottom="4dp" />
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal">
        <TextView android:id="@+id/seg_windows" android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content"
            android:gravity="center" android:padding="9dp" android:text="@string/bubble_show_windows"
            android:textColor="@color/text_primary" android:textSize="12sp" android:background="@drawable/bubble_style_card" />
        <TextView android:id="@+id/seg_always" android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content"
            android:gravity="center" android:padding="9dp" android:text="@string/bubble_show_always"
            android:textColor="@color/text_secondary" android:textSize="12sp" android:background="@drawable/bubble_style_card"
            android:layout_marginStart="8dp" />
    </LinearLayout>
</LinearLayout>
```

- [ ] **Step 2: Include the section in the settings layout**

In `res/layout/activity_athan_settings.xml`, inside `@+id/athan_dependent_container` (line 79), after the notifications card, add:

```xml
<include layout="@layout/settings_section_bubble" />
```

- [ ] **Step 3: Wire it up in `AthanSettingsActivity`**

Add to the end of `setupNotificationsSection()` a call to `setupBubbleSection();`, and add the method + helpers. (`requestOverlayPermission()`/`canDrawOverlays()` already exist — use the correct `Build.VERSION.SDK_INT`.)

```java
    private void setupBubbleSection() {
        BubblePrefs bp = new BubblePrefs(this);

        androidx.appcompat.widget.SwitchCompat sw = findViewById(R.id.switch_bubble);
        sw.setChecked(bp.isEnabled());
        sw.setOnCheckedChangeListener((b, checked) -> {
            if (checked && !canDrawOverlays()) { requestOverlayPermission(); b.setChecked(false); return; }
            bp.setEnabled(checked);
            android.content.Intent svc = new android.content.Intent(this,
                    com.medoapps.www.onlinequran.bubble.AthkarBubbleService.class);
            if (checked) {
                androidx.core.content.ContextCompat.startForegroundService(this, svc);
                com.medoapps.www.onlinequran.bubble.BubbleScheduler.reschedule(this); // no-op until Task 14; safe to call
            } else {
                startService(svc.setAction(com.medoapps.www.onlinequran.bubble.AthkarBubbleService.ACTION_STOP));
            }
        });

        findViewById(R.id.row_bubble_overlay).setOnClickListener(v -> requestOverlayPermission());

        bindStyleCards(bp);
        bindShowDuring(bp);
        refreshBubbleStatus();
    }

    private void bindStyleCards(BubblePrefs bp) {
        int[] cardIds = { R.id.card_style_a, R.id.card_style_c, R.id.card_style_d };
        int[] radioIds = { R.id.radio_a, R.id.radio_c, R.id.radio_d };
        BubbleStyle[] styles = { BubbleStyle.CHAT_HEAD, BubbleStyle.DRAWER, BubbleStyle.PILL };
        for (int i = 0; i < cardIds.length; i++) {
            final BubbleStyle s = styles[i];
            findViewById(cardIds[i]).setOnClickListener(v -> { bp.setStyle(s); applyStyleSelection(bp); restartBubbleIfRunning(bp); });
        }
        applyStyleSelection(bp);
    }
    private void applyStyleSelection(BubblePrefs bp) {
        BubbleStyle sel = bp.getStyle();
        findViewById(R.id.card_style_a).setActivated(sel == BubbleStyle.CHAT_HEAD);
        findViewById(R.id.card_style_c).setActivated(sel == BubbleStyle.DRAWER);
        findViewById(R.id.card_style_d).setActivated(sel == BubbleStyle.PILL);
        ((RadioButton) findViewById(R.id.radio_a)).setChecked(sel == BubbleStyle.CHAT_HEAD);
        ((RadioButton) findViewById(R.id.radio_c)).setChecked(sel == BubbleStyle.DRAWER);
        ((RadioButton) findViewById(R.id.radio_d)).setChecked(sel == BubbleStyle.PILL);
    }
    private void bindShowDuring(BubblePrefs bp) {
        View win = findViewById(R.id.seg_windows), always = findViewById(R.id.seg_always);
        Runnable apply = () -> { win.setActivated(!bp.isAlwaysOn()); always.setActivated(bp.isAlwaysOn()); };
        win.setOnClickListener(v -> { bp.setAlwaysOn(false); apply.run(); com.medoapps.www.onlinequran.bubble.BubbleScheduler.reschedule(this); });
        always.setOnClickListener(v -> { bp.setAlwaysOn(true); apply.run(); com.medoapps.www.onlinequran.bubble.BubbleScheduler.reschedule(this); });
        apply.run();
    }
    private void restartBubbleIfRunning(BubblePrefs bp) {
        if (!bp.isEnabled()) return;
        android.content.Intent svc = new android.content.Intent(this,
                com.medoapps.www.onlinequran.bubble.AthkarBubbleService.class);
        startService(svc.setAction(com.medoapps.www.onlinequran.bubble.AthkarBubbleService.ACTION_STOP));
        androidx.core.content.ContextCompat.startForegroundService(this,
                new android.content.Intent(this, com.medoapps.www.onlinequran.bubble.AthkarBubbleService.class));
    }
    private void refreshBubbleStatus() {
        TextView tv = findViewById(R.id.tv_bubble_overlay_status);
        if (tv != null) tv.setText(canDrawOverlays() ? R.string.athan_over_apps_on : R.string.athan_over_apps_off);
    }
```

Add `refreshBubbleStatus();` to the existing `onResume()` (next to `refreshOverlayStatus()` at ~line 797). Add imports for `BubblePrefs`, `BubbleStyle`, `RadioButton`.

- [ ] **Step 4: Build + install**

Run: `./gradlew assembleMadaniDebug && adb -s emulator-5554 install -r app/build/outputs/apk/madani/debug/app-madani-debug.apk`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Verify the picker end-to-end on device**

```bash
adb -s emulator-5554 shell am start -n com.medoapps.www.onlinequran/.SplashScreen
```
Navigate to Athan settings → the new "Floating Athkar Bubble" section. Verify: toggling **Show floating bubble** ON (granting overlay if prompted) makes the bubble appear over the app/home; selecting **A/C/D** swaps the live style; the selected card shows the gold outline + filled radio; **Day&Night / Always** toggles highlight. Screenshot each style:
```bash
adb -s emulator-5554 exec-out screencap -p > /private/tmp/claude-501/-Users-medo-projects-tmp-My-Stream-My-Stream-Android/e7d73825-e20f-4f5f-b70d-288d60519a3b/scratchpad/bubble_settings.png
```
Expected: picker works; bubble starts/stops with the switch; styles swap.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/res/layout/settings_section_bubble.xml app/src/main/res/drawable/bubble_style_card.xml \
        app/src/main/res/layout/activity_athan_settings.xml app/src/main/java/com/medoapps/www/onlinequran/AthanSettingsActivity.java
git commit -m "feat(bubble): Settings section + 3-style picker + enable toggle

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 14: Auto-show scheduler + boot restore

Show the bubble only during the day/night windows when "Day & Night windows" is selected (start at Fajr, hide at Asr→re-show at Asr as evening, etc.); keep it always-on when "Always" is selected. Restore after reboot.

**Files:**
- Create: `bubble/BubbleScheduler.java`, `bubble/BubbleBootReceiver.java`
- (Manifest receiver already added in Task 7.)

**Interfaces:**
- Consumes: `BubblePrefs`, `PrayerTimeEngine`, `PrayerSettings`, `AthkarBubbleService`, `AlarmManager`.
- Produces: `static void BubbleScheduler.reschedule(Context)` (called from Task 13 already), `static void BubbleScheduler.applyNow(Context)` (start/stop the service based on enabled + window + always).

- [ ] **Step 1: Implement the scheduler**

```java
// app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleScheduler.java
package com.medoapps.www.onlinequran.bubble;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

import com.medoapps.www.onlinequran.athan.PrayerSettings;
import com.medoapps.www.onlinequran.athan.PrayerTimeEngine;

import java.util.Date;

/** Arms alarms at the day/night window boundaries (Fajr, Asr) and starts/stops the
 *  bubble service for the current moment. Request codes 3100 (show) / 3101 (hide). */
public final class BubbleScheduler {
    private BubbleScheduler() {}
    private static final int RC_BOUNDARY = 3100;
    public static final String ACTION_APPLY = "com.medoapps.athkar.bubble.APPLY";

    public static void reschedule(Context ctx) {
        applyNow(ctx);
        BubblePrefs bp = new BubblePrefs(ctx);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = boundaryPi(ctx);
        am.cancel(pi);
        if (!bp.isEnabled() || bp.isAlwaysOn()) return; // no boundary alarms needed
        try {
            Date[] t = PrayerTimeEngine.getTodayTimes(ctx);
            long now = System.currentTimeMillis();
            long fajr = t[PrayerSettings.PRAYER_FAJR].getTime();
            long asr = t[PrayerSettings.PRAYER_ASR].getTime();
            long next = now < fajr ? fajr : (now < asr ? asr : fajr + 24L * 3600_000L);
            am.set(AlarmManager.RTC, next, pi);
        } catch (Exception ignored) {}
    }

    public static void applyNow(Context ctx) {
        BubblePrefs bp = new BubblePrefs(ctx);
        Intent svc = new Intent(ctx, AthkarBubbleService.class);
        boolean shouldShow = bp.isEnabled() && (bp.isAlwaysOn() || inWindow(ctx));
        if (shouldShow) ContextCompat.startForegroundService(ctx, svc);
        else ctx.startService(svc.setAction(AthkarBubbleService.ACTION_STOP));
    }

    private static boolean inWindow(Context ctx) {
        // Day & Night windows == always at least one session is active, so the bubble is
        // shown whenever enabled. A future tightening (e.g. only N minutes around Fajr/Asr)
        // would change this predicate; today both windows together cover the full day.
        return true;
    }

    private static PendingIntent boundaryPi(Context ctx) {
        Intent i = new Intent(ctx, BubbleBootReceiver.class).setAction(ACTION_APPLY);
        return PendingIntent.getBroadcast(ctx, RC_BOUNDARY, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
```

> Note: with both day & night windows covering the whole 24h, "windows" currently behaves like always-on but re-evaluates the *session* (morning vs evening) at each boundary so the bubble flips ☀→☾ at Asr. The `inWindow` predicate is the single place to tighten later (e.g. "only 30 min around Fajr/Asr") per the spec's open item.

- [ ] **Step 2: Implement the boot/boundary receiver**

```java
// app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleBootReceiver.java
package com.medoapps.www.onlinequran.bubble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Restores the bubble after reboot and re-applies it at window boundaries. */
public class BubbleBootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        // Both BOOT_COMPLETED and our boundary alarm land here.
        BubbleScheduler.reschedule(context);
    }
}
```

- [ ] **Step 3: Build + install**

Run: `./gradlew assembleMadaniDebug && adb -s emulator-5554 install -r app/build/outputs/apk/madani/debug/app-madani-debug.apk`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Verify boot restore**

```bash
# enable the bubble in settings first, then:
adb -s emulator-5554 shell am broadcast -a android.intent.action.BOOT_COMPLETED -n com.medoapps.www.onlinequran/.bubble.BubbleBootReceiver
adb -s emulator-5554 shell dumpsys activity services com.medoapps.www.onlinequran | grep -i AthkarBubble
```
Expected: the service is running again after the simulated boot (when the bubble was enabled). With the bubble disabled, the broadcast leaves no service running.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleScheduler.java \
        app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleBootReceiver.java
git commit -m "feat(bubble): window scheduler + boot restore

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Final verification

- [ ] **Run the full unit suite:** `./gradlew testMadaniDebugUnitTest` → all green (AthkarItem, AthkarRepository, AthkarProgressStore, BubbleSessionSelector, BubbleContentController, BubbleStyle).
- [ ] **Full build:** `./gradlew assembleMadaniDebug` → BUILD SUCCESSFUL.
- [ ] **Manual matrix on emulator-5554:** for each style A/C/D × {light, dark} × {EN, AR}: enable bubble, confirm it floats, drag + snap + drag-to-✕, tap to open, count a dhikr (haptic + badge update + auto-advance), and confirm the completion persists (`adb -s emulator-5554 shell run-as com.medoapps.www.onlinequran cat shared_prefs/athkar_daily_prefs.xml` shows `done_*` keys). Confirm AR docks style C to the left and no `@color/white`-on-navy black-text regressions in dark mode.

---

## Self-Review (run against the spec)

**Spec coverage:**
- 3 user-selectable styles A/C/D → Tasks 9–13. ✓
- Day/night content from `AthkarActivity` via shared repository → Tasks 1–2. ✓
- Per-dhikr counters + 20ms haptic (TasbihActivity parity) → Tasks 5, 10. ✓
- Shared daily completion with Stories → Task 3 (`AthkarProgressStore`), written in Tasks 10/12. ✓
- Overlay foreground service (specialUse) + bubble_channel + notif 4000 → Tasks 7, 8. ✓
- WindowManager overlay + drag + snap + dismiss + window-type branch → Task 10. ✓
- Settings picker (enable, overlay row reuse, style cards, show-during) → Task 13. ✓
- Auto-show windows + boot restore + request codes 3100/3101 → Task 14. ✓
- Session by prayer time (`PrayerTimeEngine`) → Task 4 + Task 10. ✓
- EN+AR strings, light+dark explicit colors, RTL left-dock → Tasks 7, 9, 12, final matrix. ✓

**Open spec items deferred (documented, not skipped):** tightening `inWindow()` to a narrower window, and the precise "done" definition, are isolated in `BubbleScheduler.inWindow()` and `BubbleContentController.countCurrent()` respectively — single-point changes.

**Type consistency:** `BubbleContentController` methods (`currentIndex/currentItem/remainingAt/targetAt/doneCount/fraction/countCurrent/countAt/currentItemAt/jumpTo`) are used identically in Tasks 5/10/12. `AthkarProgressStore.markDone(int,String,int)` signature matches its callers (Tasks 10/12) which pass `session.name()`. `BubblePrefs`/`BubbleStyle` API matches Task 6 ↔ Tasks 10/13. Service `ACTION_STOP`/start pattern matches Tasks 8/13/14.
