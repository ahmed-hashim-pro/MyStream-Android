# My-Stream-Android — UI Inspection Report

**App:** `com.medoapps.www.onlinequran` (Madani debug variant)
**Inspected:** 2026-04-21, on the running emulator
**Starting state:** fresh install — no Quran page images downloaded, no translations installed

## Screens walked

### 1. Home (QuranActivity)
- Top toolbar shows three actions: **Last page**, **Search**, and a **More options** (⋮) overflow. The overflow contains **Go to page**, **Settings**, **Help**, **About Us**.
- Three tabs: **SURAHS**, **JUZ'**, **BOOKMARKS**.
- **SURAHS** tab is a single scrolling list that interleaves juz' separators ("Juz' 1 1", "Juz' 2 22", …) with surah rows (e.g., "1 Surah Al-Fâtihah — Makki — 7 verses"). All 30 juz' and 114 surahs are enumerated.
- **JUZ'** tab shows each juz' expanded with the surahs/ayat it contains.
- **BOOKMARKS** tab has a **Sort** affordance in the toolbar; empty in the fresh install since nothing is bookmarked yet.

### 2. Home → Go to page
A modal "Jump to page" dialog with a number field (1–604, the Madani mushaf count) and Go / Cancel buttons.

### 3. Home → Last page
Resumes to the most recently viewed page. On first launch it lands on page 1 (Al-Fâtihah); after the walkthrough it correctly restored whichever page I had last opened.

### 4. Settings (QuranPreferenceActivity)
Categories observed: **Display**, **Reading**, **Dual Page**, **Translation**, **Download**, **Advanced**.
- **Display:** theme, page orientation, background image options.
- **Reading:** reciter defaults, repeat options.
- **Dual Page:** toggle for side-by-side tablet layout.
- **Translation:** entry point that launches the Translations Manager.
- **Download:** storage location and cellular-data controls.
- **Advanced:** opens its own activity (see next).

### 5. Settings → Advanced Options
Five entries: **Import**, **Export**, **Export CSV**, **Quran data directory**, **Send logs**. The directory row shows the current data path; Send logs is the diagnostic hook that ships a zip of the app's internal logs.

### 6. Translations Manager (TranslationManagerActivity)
Long download list grouped by language. English section lists multiple options (Sahih International, Muhammad Muhsin Khan, Pickthall, etc.), followed by Arabic tafsir entries and then other languages. Each row has a download icon on the right; nothing was installed during inspection so every row was in the "not downloaded" state. This is also the screen the app routes you to from the "Get Translations" CTA elsewhere.

### 7. Surah reader (PagerActivity)
- Opened from "1 Surah Al-Fâtihah".
- Displays the Madani page image. Since no page images are downloaded, the reader shows placeholder/skeleton content — real mushaf rendering is gated behind a first-run download that I intentionally did not trigger.
- Tapping once on the page reveals the chrome: top toolbar (Search, Bookmark, globe/translation, Play, overflow ⋮) and bottom reciter bar with a qari spinner and a play button.
- The bottom bar's qari spinner opens a list of reciters.
- RTL swipe to advance pages didn't navigate — expected, since the underlying images aren't on disk yet.
- **Long-press on an ayah** (the FAQ-advertised ayah action menu) intercepts to a **Download** dialog first, because ayah menu features (bookmark/tag/share/copy/translation/recite) need page data to resolve ayah bounds. Gated behind download, as designed.

### 8. Reader overflow menu
Contextual items: **Search**, **Night mode** (toggle — verified it swaps the theme), **Go to page**, **Settings**, **Focus Mode**, **Help**. Focus Mode hides chrome for distraction-free reading.

### 9. Search (QuranSearchActivity)
- Standard Android SearchView in the toolbar with voice-input mic.
- Typed "mercy" → result screen showed "No results found for 'mercy'" plus a prominent **Get Translations** call-to-action that deep-links into the Translations Manager. This is a nice graceful-degradation path: search over English text requires an English translation to be installed first.

### 10. About Us
- Styled with a distinct purple/lavender theme (noticeably different from the rest of the app's cream/brown palette — worth noting for design-system consistency).
- Header: "My Stream" logo (rainbow-gradient mark + lowercase "stream" wordmark).
- Tagline: *"Quran for Android is a free Quran application. Please do not forget the contributors in your prayers."*
- **Data Sources** section credits: King Fahd Quran Printing Complex (page images), QuranicAudio, Electronic Moshaf Project (King Saud University), Al-Bāḥith al-Qurʾānī (tafsir.app), Noble Quran Encyclopedia (QuranEnc), Tanzil, Noorhidayat.
- **Open Source Projects** section credits: Kotlin, Dagger 2, OkHttp, RxJava, RxAndroid, Moshi, AndroidSlidingUpPanel, Timber, Material Components for Android, dnsjava, Number Picker (each with its GitHub URL).

### 11. Help
FAQ page titled "My Stream" in the toolbar (same purple theme as About Us). Sections: *How do I play audio?*, *How do I view translation?*, *How do I bookmark a page?*, *How do I make the text larger?*, *How do I share an ayah?*, *Malayalam/Tamil/Bengali/Urdu fonts don't work!* Content matches the gestures I verified in the reader (single tap to reveal chrome, long-press for ayah menu, landscape for larger Arabic text, bookmark icon in top-right).

## Observations worth flagging

**Theming inconsistency.** About Us and Help render on a purple background while the rest of the app uses the cream/brown `madani` theme. Either they're meant to be themed separately (fine, intentional) or they missed a theme override — worth checking `AboutActivity` / `HelpActivity` styles against the others. The About Us toolbar title reads "My Stream" rather than "About Us", and Help does the same — the action bar title looks like it's inheriting the app name instead of setting its own label.

**Download-gated UX is well-handled.** Search without translations, long-press without page data, swipe without images — all three fall through to a clear "download X" prompt rather than broken states. The "Get Translations" CTA on empty search results is a nice touch.

**Nothing broken under the walkthrough.** No crashes, ANRs, or logcat stack traces during navigation. The `ClassNotFoundException` from the prior build-cache issue has stayed fixed across cold launches.

**Untouched flows (require downloads, intentionally skipped):**
- Actually rendering a Madani page with real content.
- Qari audio playback.
- Page bookmarks (needs a rendered page to bookmark).
- Ayah actions (bookmark/tag/share/copy/translation/recite).
- Translation overlay via the globe icon.
- Dual-page tablet layout.

---

## Second pass: download-gated flows

After the first pass I went back and actually exercised the flows I had marked untested. Two of my original assumptions were wrong and worth correcting.

**Pages actually were already rendered.** Tapping Al-Fâtihah went straight into a fully rendered Madani page — ornate surah header, full Uthmani script, verse markers. I had read the blank-page first impression as "images not yet downloaded" but it was just the unstyled initial state before chrome was revealed. Image data was already on disk.

**Swipe pagination works; I was using too small a swipe.** With a 100→1000 px horizontal swipe (left-to-right = "next" in this RTL app) the view advances cleanly. Right-to-left returns to the previous page. I was able to walk from Al-Fâtihah through the opening pages of Al-Baqarah and back again.

**Revealed chrome with a single tap.** Top toolbar shows "Surah Al-Baqarah / Page 2, Juz' 1" with icons for **Bookmark**, **Show Translation** (globe), and **More options**. Bottom bar shows a play button at the left and the qari spinner ("Minshawi Murattal (gapless)") in the center.

**Bookmark a page.** Tapping the bookmark icon adds the current page to the BOOKMARKS tab. Home → BOOKMARKS now shows two sections:
- **Last page** — auto-tracked most-recent position (always present).
- **Page Bookmarks** — the manual bookmark I just added, displayed with a filled bookmark glyph, "Surah Al-Baqarah, Page 2, Juz' 1".
The BOOKMARKS tab has its own toolbar with **Sort** and a **Last page** shortcut.

**Ayah long-press menu.** First long-press intercepted with a dialog: *"We need to download one or two small files to support sharing and translation. Download now?"* Tapping Yes installed those. After that, a second long-press on ayah 1 worked perfectly — the ayah highlighted in cyan and a floating action pill appeared above it with five icons: **Bookmark (ribbon), Tag, Share, Translation (globe), Play**. Tapping **Share** expanded into a sub-toolbar with three finer-grained options: **Link (chain), Share (system share sheet), Copy (clipboard)** — nice progressive disclosure rather than dumping everything at once.

**Audio playback.** Tapping the play button at the far left of the reciter bar kicks off a qari-audio download — bottom bar shows "Downloading…" with a progress bar and a cancel X. When the download completes, the bottom bar transforms into a full transport control: **Stop, Previous, Pause (playing), Next, Repeat, Settings**, and the currently-playing ayah is highlighted in green on the page. The highlight advances ayah-by-ayah in sync with audio, which is exactly what you want from a page reciter. The reciter spinner is gone while the transport is active.

**Minor UI note:** once the "sharing/translation" small files are installed, the page chrome changes — instead of the chunky dark toolbar, the page shows a minimal "Surah Al-Baqarah / Juz' 1" header strip and a page number ("2") at the bottom. Tapping once still swaps back to the full chrome. This is a nice reading mode but it isn't discoverable — it just turns on silently after that one-time download.

## Flows still not exercised

- **Tag** action on the ayah menu (would create a tag/category list).
- **Translation overlay** via the globe icon (would require installing at least one translation from the Translations Manager first).
- **Night mode** actually enabled (only verified it's an item in the reader overflow).
- **Focus Mode** (same — item present in the reader overflow but not enabled).
- **Dual page** tablet layout (phone emulator doesn't have the screen width).
- **Settings → Import / Export / Export CSV / Send logs** (destructive / external-side-effect items I didn't want to fire on the dev emulator).

---

## Third pass: the wrapper shell (I'd been inside a sub-activity the whole time)

Tapping back from the Quran reader surfaced a completely different screen — the **actual** "My Stream" home. What I had documented in passes 1 and 2 was only the **Open Mushaf** sub-activity of a larger bottom-tab shell.

### 12. My Stream wrapper shell

- **Bottom tab bar** with four tabs: **Holy Quran | Radio | Other | Open Mushaf**.
- **Header** shows the My Stream logo (rainbow gradient mark + lowercase wordmark), the Hijri date *"4 ذو القعدة 1447 هـ"* (≈ 2026-04-21), a yellow **star badge**, and a hamburger menu that opens the wrapper-level Settings.
- The UI here is fully bilingual (Arabic / English) and visually distinct from the cream/brown Quran reader — which explains the "purple theme" inconsistency I flagged in pass 1: About Us and Help are inherited from this wrapper, not from the reader.

### 13. Holy Quran tab (reciters list)

- Scrolling list of qaris, each row showing the reciter's name in Arabic, a share icon, and a bookmark icon. Observed: محمد صديق المنشاوي, ابو بكر الشاطري, خالد الجليل, أحمد خليل شاهين, بيشه وا قادر الكردي, أحمد بن علي العجمي, عبدالباسط عبدالصمد, and more below the fold.
- Toolbar has **Search** and a filter/settings icon.
- Test/placeholder **ads** are interspersed in the list (APNIC, "ZHONGNENG Oil Purifier") — harmless in debug, but worth noting they're enabled in this build variant.

### 14. Reciter detail (Al-Shatri)

- Header with the reciter's name and a large **"Download all list"** CTA.
- Per-surah rows for الفاتحة, البقرة, آل عمران, النساء, المائدة, الأنعام, الأعراف, الأنفال, … Each row has four actions: **download**, **share**, **live stream ("بث مباشر")**, and a **gold play button**.
- This is a separate audio flow from the Quran reader's reciter bar — here you're browsing qari → surah MP3s directly, rather than listening inline with the page.

### 15. Radio tab

- List of live Quran radio stations, each row showing the station name (*إذاعة X*) with the *qira'a* (Qur'anic reading tradition) as a subtitle — **المصحف المجود**, **قالون عن نافع**, **حفص عن عاصم**, **ورش عن نافع**. Observed stations: إذاعة محمد صديق المنشاوي, إذاعة الدوكالي محمد العالم, إذاعة أحمد الطرابلسي, إذاعة أحمد خضر الطرابلسي, إذاعة أحمد عامر, إذاعة ابراهيم الدوسري, إذاعة الفاتح محمد الزبير, and more below the fold.
- Toolbar has **Search** and an **All** filter spinner (likely filters by qira'a).
- Test/placeholder ads are interspersed in the list, same as the Holy Quran tab.

### 16. Radio station detail

- Header: back arrow, station name, "Radio" subtitle, bookmark icon.
- A **Streaming** badge (gold pill with Wi‑Fi glyph) sits under the title.
- Below it: a large **test ad card** (Massey Ferguson / "ZHONGNENG Oil Purifier") — the ad is integrated into the player page, not just the list.
- An **Offline** chip (green, with a phone glyph) and a **red trash can** icon appear below the ad — likely a cache indicator and a clear-cached-audio action.
- **قائمة السور** heading with a numbered list of entries, each with a gold play button on the right.
- At the bottom of the screen: a full transport bar — **skip to start, skip back, big play button, skip forward, skip to end**. This is a pure audio player with no tie into the Mushaf reader (unlike the reciter bar inside the Quran reader, which highlights the playing ayah on the page).
- First-open side effect: a **"Battery Optimization Info"** bottom-sheet modal appears with copy *"To ensure that the audio run without losing connection, we prefer to allow My stream Battery Optimization Disable."* and **Cancel / OK** actions. OK would launch the Android battery-optimization exempt flow. The phrasing is slightly awkward ("allow … Disable") — a UX-copy fix candidate.
- Worth flagging: during this flow the emulator surfaced a system **"Messages isn't responding"** ANR. My Stream's own UI stayed responsive, so this is most likely the ad SDK hammering something that disturbed the system Messages app — but it's a signal the ad loader is aggressive.

### 17. Wrapper Settings (hamburger menu)

Bilingual settings panel, distinct from the Quran reader's `QuranPreferenceActivity`. Observed categories:

- **General** — Lists Language toggle (Arabic/English), Startup sound, Titles Animations, Dark Mode.
- **Notifications** — **Reminder** (prayer alarms) and **Notification settings** (ayah of the day, adhkar, hadith).
- **Data** — Downloads, Go to bookmark.
- **About** — Share My Stream.

## Flows still not exercised (third pass)

Android-MCP device bridge disconnected twice mid-exploration (persistent *Snapshot timed out* after waits). Radio was mapped before the second drop; the remainder is still unmapped:

- **Other tab** — purpose unknown.
- **Notifications → Reminder / Notification settings** sub-screens (prayer alarm config, ayah-of-the-day config).
- **Star badge** on the wrapper header — likely a favorites or premium marker.
- **Share My Stream** from About.
- **Radio → station playback confirmed working** (transport present, battery modal prompts on first stream) but actual stream quality/behavior was not exercised because the battery dialog was cancelled and the bridge died.

To finish mapping these, the emulator/ADB bridge needs a restart so Android-MCP can reconnect.

## Revised understanding

The passes above built up the wrong mental model. Corrected:

- "My Stream" is the wrapper app; the Quran reader I mapped in passes 1–2 is the **Open Mushaf** tab of four.
- The purple-themed About Us and Help I flagged as a design-system inconsistency are actually rendered by the wrapper — they match the wrapper's visual language, not the reader's. So it's not a theme bug in the reader; it's two different UIs coexisting.
- There are **two** Settings surfaces: the wrapper's bilingual General/Notifications/Data/About panel and the reader's Display/Reading/Dual Page/Translation/Download/Advanced panel. Changes in one don't obviously propagate to the other — worth verifying.

---

## Fourth pass: the remaining flows (2026-06-13)

Emulator (`Medium_Phone_API_35`, Android 15) restarted and driven over raw adb. Everything left unmapped after pass 3 is now exercised. One hard crash found.

### 18. Other tab

A 3-column card grid, 13 tools, matching `OtherCategoryListLanguageClass.java:92-174` one-for-one:

اتجاه القبلة (Qibla) · مواقيت الصلاة (Prayer Times) · live streaming · سجل القراءة (Reading Progress) · المسبحة الرقمية (Tasbih) · أذكار الصباح والمساء (Athkar) · حاسبة الزكاة (Zakat Calculator) · أسماء الله الحسنى (99 Names) · الأدعية (Duas) · المناسبات الإسلامية (Islamic Events) · حديث اليوم (Daily Hadith) · متتبع الصيام (Fasting Tracker) · حصن المسلم (Hisn Al Muslim, below the fold)

A test ad banner sits under the grid. Spot checks:

- **Asmaul Husna** — works. Clean numbered card list, each name with a one-line explanation. No toolbar; relies on system back.
- **Prayer Times** — **crashes, 100% reproducible** (see below).

### 🔴 Crash: Prayer Times kills the app on Android 14+

Open مواقيت الصلاة → grant location → spinner shows "13 June 2026 / مكة المكرمة" with six `--:--` rows → as soon as times resolve, the app dies with "My Stream keeps stopping". Reproduced twice (with and without a GPS fix; without a fix it falls back to Mecca and still crashes after fetch).

```
java.lang.SecurityException: Caller com.medoapps.www.onlinequran needs to hold
android.permission.SCHEDULE_EXACT_ALARM or android.permission.USE_EXACT_ALARM to set exact alarms.
  at android.app.AlarmManager.setExactAndAllowWhileIdle(AlarmManager.java:1312)
  at com.medoapps.www.onlinequran.PrayerAlarmScheduler.schedulePrayerAlarms(PrayerAlarmScheduler.java:87)
  at com.medoapps.www.onlinequran.PrayerTimesActivity.lambda$fetchPrayerTimes$2(PrayerTimesActivity.java:174)
```

Root cause: the manifest declares `SCHEDULE_EXACT_ALARM` (AndroidManifest.xml:32), but since Android 14 that permission is **denied by default** for new installs, and `PrayerAlarmScheduler` never checks `alarmManager.canScheduleExactAlarms()` before calling `setExactAndAllowWhileIdle()`. The activity schedules alarms unconditionally right after the times fetch succeeds, so the screen is unusable on any Android 14+ device. Fix options: gate on `canScheduleExactAlarms()` and fall back to `setWindow()`/inexact alarms, request the grant via `ACTION_REQUEST_SCHEDULE_EXACT_ALARM`, or declare `USE_EXACT_ALARM` (justifiable for a prayer-alarm app per Play policy). Note the first crash presents as a *silent* close (the crash dialog can lag behind the activity teardown), which is what pass 3's "screen closed itself" would have looked like.

### 19. Wrapper Settings → Reminder (prayer alarm)

`TimePicker` activity. One card: a Reminder toggle (was already on, set to 20:01) and a "Select Time…" row, plus an Arabic status line ("المنبه مفعّل — الساعة 20:01"). Tapping Select Time opens a gold-themed Material 24-hour clock dialog. Two nits: the dialog opens at the current wall-clock time rather than the saved alarm time, and the screen stacks a purple "My Stream" header above a second white "Reminder" toolbar — double chrome.

### 20. Wrapper Settings → إعدادات الإشعارات (Notification settings)

`NotificationSettingsActivity`. Three sections, seven rows, all "غير مفعّل" (disabled) by default — exactly the seven schedulers in code:

- **القرآن الكريم**: آية اليوم (Daily Ayah)
- **الأذكار والأدعية**: أذكار الصباح, أذكار المساء, دعاء اليوم (Hisn)
- **تذكيرات يومية**: حديث اليوم, اسم الله اليوم (Asmaul Husna), تذكير السحور (fasting)

These all use `AlarmManager.setInexactRepeating`, so they are *not* exposed to the exact-alarm crash above — only `PrayerAlarmScheduler` is.

### 21. Star badge = rewarded ad (and the reward is a no-op)

The header badge (renders as an animated starburst) is not favorites or premium: tapping it loads and plays an AdMob **rewarded video** (`MainActivity.java:475-483`). Verified live: test rewarded video plays ("Ad 1 of 2"), end-card, close. But `onUserEarnedReward` (`MainActivity.java:1128-1133`) is a stub — it logs the reward amount and does nothing else. Users sit through a full video and receive nothing. Either wire a real reward (the `onStarClicked` Firebase transaction nearby is a separate post-starring feature, currently unrelated) or remove the badge.

### 22. Share My Stream

Settings → حول التطبيق → Share My Stream opens the system share sheet with:

> "My Stream : I want to share with you My stream app , you can listen to holy quran from more than 150 reciters, listen to radio channels all day , react to youtube videos and more . you can download free now https://mystream.page.link/sJRm"

That's a **Firebase Dynamic Link, and FDL was shut down August 25, 2025**. Verified today: the link 302s to an FDL preview endpoint that renders a blank page titled "My Stream" — no Play Store redirect, no install button. The share flow works mechanically but the link strands recipients. Migrate to a plain Play Store URL or Android App Links (`SeparateFunctions.generateAppShareLink`, `util/SeparateFunctions.java:74-100`). The share copy also needs an editing pass (stray spaces before commas, lowercase "stream", "react to youtube videos").

### 23. Radio playback — confirmed working end-to-end

Tapped play on إذاعة محمد صديق المنشاوي from the Radio list → `NewQuranPlayer` opens and streaming starts. Verified objectively, not just visually:

- MediaSession `AudioPlayer` active, `state=PLAYING(3)`, metadata title = station name.
- Live `AudioTrack` (44.1 kHz stereo, USAGE_MEDIA) in `state:started`.
- Foreground media notification (id 101, channel `HASHIM_CHANNEL-ID_02`, transport category, 4 actions).
- Hardware media key `KEYCODE_MEDIA_PAUSE` pauses correctly (~41 s of stream played).

**Battery Optimization flow** (the one pass 3 couldn't finish): the bottom sheet's OK correctly launches the system "Let app always run in background?" dialog, and Allow lands the package in the deviceidle whitelist. **Bug:** the app's own bottom sheet does not dismiss when OK is tapped — after returning from the system dialog it's still sitting there and needs a second Cancel/OK. The awkward copy ("…we prefer to allow My stream Battery Optimization Disable.") is confirmed unchanged.

Two more findings on the station page:

- The track-list section is headed **قائمة السور** ("surah list") but on a radio station it actually lists *other radio stations* — the layout is reused from the reciter detail without relabeling.
- The media metadata artist field is hardcoded to **"Ahmed HAshim"** (`NewQuranPlayer.kt:366,368`), which surfaces in the system Now Playing UI. Should be the station/reciter name or the app name.

Ad pressure on this screen is heavy in debug: a banner, a native video ad mid-page, and the AdMob native-ad-validator overlay all render above the fold; the actual transport bar is the last element on screen.

### Status after four passes

Every flow from pass 3's "still not exercised" list is now mapped. Still untouched (from pass 2, all reader-side): ayah Tag action, translation overlay, night mode enabled, Focus Mode, dual-page tablet layout, Settings → Import/Export/Export CSV/Send logs.

**Priority fixes:** 1) ~~Prayer Times crash~~ **fixed 2026-06-13** — `PrayerAlarmScheduler` now checks `canScheduleExactAlarms()` and falls back to `setAndAllowWhileIdle()` when exact alarms are denied (verified on-device in both the denied and granted states); 2) dead share link (FDL sunset); 3) no-op rewarded ad; 4) battery sheet not dismissing; 5) hardcoded artist metadata.

**2026-06-13 — Prayer Times rebuilt as a full athan feature.** The API-based screen was replaced by an offline athan system: on-device calculation (adhan 1.2.1; 10 methods, Shafiʿi/Hanafi asr, high-latitude rules, per-prayer ±59-min corrections), per-prayer notification modes (off/silent/notification/full athan with per-prayer sound URIs via ringtone picker), pre-prayer and iqama reminders, post-athan dua in the notification, foreground playback service (USAGE_ALARM, audio focus, Stop action, vibration), exact alarms with the granted-permission flow + Doze-proof inexact fallback, nightly maintenance alarm + boot/timezone/time-change rescheduling, POST_NOTIFICATIONS and exact-alarm permission UX, auto (fused + reverse geocode) or manual-city location, hijri header with adjustable offset, live next-prayer countdown, monthly timetable screen, and the home-screen widget now computes from the engine directly. New package `athan/` + `AthanSettingsActivity` + `MonthlyPrayerTimesActivity`; strings localized en+ar. Verified end-to-end on the API 35 emulator including a real AlarmManager-fired athan (notification + audio + Stop action) and legacy-alarm cleanup from the old scheduler.

**Also fixed 2026-06-13 — stale location in Prayer Times.** The screen used only `getLastLocation()` (cached, often empty → silent Mecca fallback). It now requests an actual current fix via `getCurrentLocation()` — accepts a cached fix ≤5 min old, otherwise actively acquires for up to 10 s (high accuracy with fine permission, balanced with coarse), then degrades to last-known location, then Mecca; cancellation wired to `onDestroy`. Verified on-device: emulator moved to Cairo → "Africa/Cairo" times; after the cache aged out, moved to Istanbul → "Europe/Istanbul" times. Remaining known limitations (not addressed): the Mecca fallback is still silent, and the calculation method is hardcoded to Umm Al-Qura (`method=4`) regardless of region.
