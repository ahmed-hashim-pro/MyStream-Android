# Mushaf re-theme — feature-preserving mockup redesign

**Goal:** Rebuild the proposed-UX mockups (`docs/superpowers/mockups/mushaf-retheme/parts/*.html`) so they preserve **every real feature** of the app while keeping the navy + gold Material-3 look. The current mockups were idealized and silently dropped large amounts of real functionality; this redesign **revives, never cancels** — and removes anything the mockups invented that the app does not have.

**Source of truth:** the real running app (`feature/mushaf-retheme`) — its as-built screenshots, layouts (`res/layout`), menus (`res/menu`), and preference XML (`res/xml`). A per-screen feature audit (18 agents reading screenshot + source) backs every item below.

## Locked decisions
1. **Reader = two faithful mode screens.** `04` = the real page-**IMAGE** (scanned madani look: ornamental sura band, ۝ ayah-end roundels, Day/Sepia/Night tint overlays). `08` = the **TEXT** mode (Arabic + translation rows). Both display the real globe **Show Translation / Show Quran** toggle that swaps between them. No invented segmented switch.
2. **Full preservation (~18 → ~30 frames).** Every main frame is enriched with all its real inline controls, AND new frames are added for each distinct state / dialog / sub-screen the app has (below). Pure *behaviors* that can't be drawn (auto-scroll, volume-key nav, tap-to-toggle immersive, keepScreenOn) are kept as small caption notes on the relevant frame.
3. **EN + AR and light + dark** continue to work via the existing toggles; every revived element gets `data-en`/`data-ar` and theme-aware tokens (per the project's light/dark + EN/AR rules).

## New frames to add (beyond the existing 18)
- **01b — Surahs · inline search active:** navy "N results for «q»" count band + verse-hit cards (gold-highlighted match + "Found in Surah X: ayah (page P)") + "No results / keep typing" states. (This is the index's real inline search; `09` stays the standalone SearchActivity results.)
- **03b — Bookmarks · default (ungrouped):** "Page Bookmarks" / "Ayah Bookmarks" section headers + rows (the default view; the existing frame becomes the grouped-by-tags variant).
- **07a — Ayah floating action toolbar (the REAL one):** the navy 5-icon popup bar (Bookmark · Tag · Share · Translate · Play, + Recite when available) with its directional pip — this is the actual subject; the tags bottom-sheet becomes a secondary frame reached from it.
- **11b — Tag Bookmark dialog:** multi-select tag checklist (gold checkboxes) + "New Tag (+)" row + OK/Cancel; plus the New-Tag name-entry dialog.
- **xx — Multi-select CAB (bookmarks):** Tag / Edit / Delete / New-Tag contextual bar + "N deleted · Undo" snackbar.
- **16b — Per-reciter surah screen:** the whole SheikhAudioManager screen (surah list, per-surah download/delete, "Download all" action, count subtitle).
- **16c — Reciter bulk-download dialog + multi-select CAB + delete-confirm sheet.**
- **15b — Translation manager states:** update-available row, pull-to-refresh/loading, error snackbar, delete-confirm sheet.
- **17 flow — Data gate:** Download-Required prompt → storage-permission rationale → downloading (live MB) → processing/extracting → error+retry (replaces the single static frame).
- **18 states — Audio bar:** the three real states stacked — stopped (reciter spinner + play), playing (stop/pause/repeat-cycle/settings gear), and downloading (progress + status + cancel ✕).

---

## Per-screen redesign

Each screen lists **Revive** (dropped real features to bring back), **Fix** (shown but wrong), **Remove** (invented — app doesn't have it), and **Build actions** (concrete edits).

### 01-index-surah — Quran Index — Surahs list

**Revive (lost features — bring back):**
- Interleaved Juz section headers in the Surahs list. The real Surahs tab shows 'Juz' N / الجزء N' band headers (with the juz start page on the end) between the surahs of each juz; the mockup renders a flat surah list with no juz headers at all.
- The 'Last page' toolbar book icon (ic_goto_quran) that jumps to the last read page.
- Real overflow-menu contents: 'Go to page', 'Settings', 'Help', 'About Us' (the mockup shows a bare ⋮ with no defined items).
- The entire inline search-results experience: the navy 'N results for "query"' count band, verse-hit result rows (highlighted matched text + 'Found in Surah X: ayah (page P)'), the 'No results / Try a different word, or download a translation to search its text.' empty state, and the 'Keep typing to search the Quran…' state.
- The fact that search opens a highlighted ayah in the reader (and jumps to translation for non-Arabic queries) — no result-tap behavior is represented.

**Fix (misrepresented):**
- Search placeholder: mockup says 'Search surah, juz' or page' / 'ابحث عن سورة أو جزء أو صفحة'. Real hint is 'Search the Quran' / 'بحث في القرآن', and the search is full-text verse/translation search — not a surah/juz/page lookup.
- Toolbar actions: mockup shows 🔍 + ⋮. The real toolbar has a 'Last page' book icon + ⋮; search is the always-visible field below, NOT a toolbar icon, so the 🔍 toolbar button is wrong.
- Surah row title: mockup shows the bare name 'الفاتحة'. The app shows the name with the 'سورة' prefix and full tashkeel ('سُورَةُ الفَاتِحَةِ') per show_surat_prefix.
- Surah row metadata: mockup uses 'Al-Fatihah · Meccan · 7 verses' (dot separators, repeats the transliterated name). Real metadata is 'Makki/Madani - N verses' / 'مكية - ٧ آيات' (dash separator, no name repeated); the only Latin/origin info is on this single line.
- Trailing element of a surah row: mockup labels it 'Page 1 / Page 50'. The real row shows just the localized starting page NUMBER (e.g. ١, ٢, ٥٠) with no 'Page' word.
- Continue-card subtitle: mockup 'Al-Baqarah · page 49' omits the juz; the real subtitle includes it (e.g. 'البقرة · صفحة ٤، جزء ١' / 'Al-Baqarah · Page 49, Juz 1'). Also the whole card is tappable, not just the Read chip.

**Remove (invented — not in the app):**
- A back chevron '‹' in the nav bar. The index/home toolbar has no up/back arrow (no setDisplayHomeAsUpEnabled); the start hosts only the title+subtitle.
- A per-row '★' star/bookmark control on row #2 (Al-Baqarah). The Surahs tab has no per-row bookmark/star affordance (isEditable=false, no touch/long-press listener, no tags shown).
- A trailing '›' chevron on surah rows. Real rows have no chevron — the trailing view is the page number.
- Searching by 'surah / juz / page' as implied by the placeholder — this capability does not exist.

**Build actions:**
- Add interleaved Juz header rows to the Surahs list: a thin bluish band (list_header_background equivalent) with 'Juz' 1 / الجزء ١' on the start and the juz start page on the end, placed before the first surah of each juz (Juz' 1 above Al-Fatihah; Juz' 2 / page 22; Juz' 3 / page 42; etc., matching the screenshot). Style them as tappable rows distinct from surah cards.
- Change the search field placeholder to 'Search the Quran' / 'بحث في القرآن'.
- Add a second mockup state (or annotated panel) for active search: a navy count band 'N results for "word"', a list of verse-hit cards (gold-highlighted Arabic match text + secondary line 'Found in Surah X: ayah (page P)'), plus an empty state with the lens icon, bold 'No results' and 'Try a different word, or download a translation to search its text.', and a 'Keep typing to search the Quran…' variant.
- Replace the toolbar 🔍 icon with the 'Last page' book icon (ic_goto_quran); keep search only as the field. Keep the ⋮ overflow and document its items: Go to page, Settings, Help, About Us.
- Remove the back chevron '‹' from the nav row; left only the title 'Mushaf/المصحف' + subtitle 'The Holy Quran/القرآن الكريم' on the start (right in RTL) and the actions on the end.
- Fix surah rows: remove the '›' chevron and the '★' star; make the trailing element the localized page number only (e.g. ١ / ٢ / ٥٠). Set the title to the Arabic name with 'سورة' prefix + tashkeel (سُورَةُ الفَاتِحَةِ). Set the metadata line to 'Makki/Madani - N verses' / 'مكية - ٧ آيات' (dash separator), without repeating the surah name.
- Update the Continue-card subtitle to include the juz: 'Al-Baqarah · Page 49, Juz 1' / 'البقرة · صفحة ٤، جزء ١', and treat the whole card (not just the Read chip) as the tap target.
- Keep the three tab labels Surahs/Juz'/Bookmarks and ensure RTL mirroring so the selected 'سورة' tab sits at the right; gold underline indicator under the active tab.

### 02-index-juz — Index — Juz' list (+ quarter markers)

**Revive (lost features — bring back):**
- The entire rub'-al-hizb quarter structure: the 8 quarter rows per juz (240 rows). The mockup reduces the whole tab to 30 flat 'juz' rows.
- The JuzView circular pie icon and its four quarter-fill states (full / ¼ / ½ / ¾) that mark hizb-start, ربع, نصف, ثلاثة أرباع.
- The hizb-number overlay (1..60) drawn on each full circle (the '١' seen in the screenshot).
- The Arabic ayah-START phrase as the row's main line (e.g. 'بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ', 'إِنَّ اللَّهَ لَا يَسْتَحْيِي أَن').
- The per-quarter 'سورة X، آية Y' metadata line.
- The 'Continue reading'/'متابعة القراءة' card: gold-framed mushaf icon, subtitle '<sura> · صفحة N، جزء N', and the gold 'قراءة'/'Read' chip.
- The toolbar 'go to last read page' book icon (ic_goto_quran / menu_jump_last_page 'الصفحة الأخيرة').
- The real overflow contents: Jump-to-page (JumpFragment dialog), Settings, Help, About (mockup draws a bare ⋮ with no indicated items).
- The starting-page number shown on the real juz header band (mockup's group header carries no page number).
- The inline live-search behaviour: results overlay, navy results-count band, empty/keep-typing state, and tap-to-open-with-highlight (only a static search bar is drawn).
- Auto-scroll to the current juz on open, and the header/row activated+pressed (gold_accent_faint) states.

**Fix (misrepresented):**
- Toolbar actions: mockup shows a 🔍 search icon + ⋮; the real toolbar has the 'go to last read page' book icon (ic_goto_quran) + ⋮ overflow, and NO toolbar search icon (search lives in the persistent hero field).
- Section header style/semantics: mockup shows a 'Juz' 1–10'/'الأجزاء ١-١٠' RANGE label with a thin divider line; the real header is a filled pale band (#E8EBF1) for a SINGLE juz, titled 'الجزء N' with the juz's start-page number at the far end.
- Row content model is wrong: mockup row = sequential num badge + ordinal name 'الجزء الأول' + 'Starts at Al-Fatihah : 1' + a 'Page 1' chip; the real row = pie/quarter icon + Arabic ayah-start text + 'سورة X، آية Y' + a bare page number.
- Arabic tab labels: mockup uses 'السور / الأجزاء / العلامات'; the real tabs are 'سورة / الجزء / المرجعيات'.
- Search placeholder text: mockup 'Search surah, juz' or page'/'ابحث عن سورة أو جزء أو صفحة'; the real hint is 'Search the Quran'/'بحث في القرآن' (search_hint).
- Page indicator: mockup styles it as a 'Page N' pill/chip with the word 'Page'; the real app shows a bare localized digit with no 'Page' label.

**Remove (invented — not in the app):**
- 'Juz' 1–10'/'الأجزاء ١-١٠' range grouping — the app groups strictly by single juz ('الجزء ١', 'الجزء ٢', …), never by 1–10 ranges.
- Per-row chevron '›' disclosure arrow — index_sura_row contains no chevron/arrow view; rows are flat.
- Ordinal juz names 'الجزء الأول / الثاني / الثالث' as row titles — the real list never uses ordinal names; the digit form 'الجزء ١' appears only in the header band, and rows show ayah text.
- Sequential num badge (1,2,3…) counting juz on each row — juz rows hide the number badge (replaced by the pie icon); the only number on a row is the page number.
- A back chevron '‹' in the toolbar — QuranActivity is the top-level home index and never enables an Up/back button (no setDisplayHomeAsUpEnabled); the screenshot top-left is the menu icons, not a back arrow.
- The literal word 'Page' as a label — the app shows only the localized page digit.

**Build actions:**
- Replace the flat 30-row juz list with the real two-level structure: for each juz, render one header band then 8 quarter rows. For the mockup, show at least Juz' 1 fully expanded (header + its 8 quarter rows) so the pattern is unambiguous, then optionally Juz' 2's header to show repetition.
- Change the section header from the 'Juz' 1–10 + divider line' style to a filled pale band (#E8EBF1 light / dark navy band at night, navy_700/gold text) titled 'الجزء ١'/'Juz' 1' (juz2_description) at the start, with the juz START PAGE number ('١'/'1') at the far end. Make it visually a tappable band, not a label-with-rule.
- Rebuild each quarter row as: leading circular JuzView icon + Arabic ayah-start text (title) + 'سورة <name>، آية <n>' (metadata) + bare page number at the end. Remove the num badge, the ordinal 'الجزء الأول' title, the 'Starts at …' subtitle, the 'Page N' chip, and the '›' chevron.
- Draw the JuzView circular icon with gold_accent (#B8860B) arc on a faint-gold disc (gold_accent_faint, 0x26B8860B), filled clockwise from top. Cycle the 8 rows as full / 25% / 50% / 75% / full / 25% / 50% / 75%. Put a navy hizb number overlay ('١', then '٢' on the 5th row) on the two full circles only.
- Use real ayah-start phrases for the 8 quarter titles of Juz' 1 from quarter_prefix_array: 'بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ', 'إِنَّ اللَّهَ لَا يَسْتَحْيِي أَن', 'أَتَأْمُرُونَ النَّاسَ بِالْبِرِّ', 'وَإِذِ اسْتَسْقَىٰ مُوسَىٰ لِقَوْمِهِ', 'أَفَتَطْمَعُونَ أَن يُؤْمِنُوا لَكُمْ', 'وَلَقَدْ جَاءَكُم مُّوسَىٰ', and continue per the array. Keep these Arabic in BOTH locales (non-translatable). Set matching metadata 'سورة الفاتحة، آية ١', 'سورة البقرة، آية ٢٦', 'سورة البقرة، آية ٤٤', 'سورة البقرة، آية ٦٠', … and page numbers ١, ٥, ٧, ٩, …
- Add the 'Continue reading' card above the list: gold-framed mushaf icon, bold 'متابعة القراءة'/'Continue reading', subtitle 'سُورَةُ البَقَرَةِ · صفحة ٤، جزء ١' (EN 'Al-Baqarah · Page 4, Juz' 1'), and a gold 'قراءة'/'Read' chip. Match the screenshot's card styling (cream/navy surface, 14dp radius).
- Fix the toolbar actions: remove the 🔍 icon and the '‹' back chevron. Show the 'go to last page' book icon (ic_goto_quran) before the ⋮. Indicate the overflow contains Jump to page, Settings, Help, About (e.g. in a comment or a small popover mock).
- Correct the Arabic tab labels to 'سورة / الجزء / المرجعيات' (keep EN 'Surahs / Juz' / Bookmarks'), with the gold underline under 'الجزء'.
- Correct the search placeholder to 'بحث في القرآن'/'Search the Quran' (search_hint) and keep the multicolour lens (ic_search_lens) rather than a flat gold/emoji magnifier.
- Render page numbers as bare localized digits (no 'Page' word, no pill) in both the juz header and the quarter rows, using Arabic-Indic digits in the AR view.

### 03-bookmarks — Index — Bookmarks tab

**Revive (lost features — bring back):**
- Continue-reading card entirely missing — no 'Continue reading'/'متابعة القراءة' title, no gold 'Read'/'قراءة' button, no sura/page subtitle, no gold icon frame. This is one of the two dominant elements in the as-built screenshot.
- Recent-pages section entirely missing — no 'Recent pages'/'آخر المتصفحات' header and no recently-read page rows (current-page glyph + sura name + 'Page X, Juz Y' + trailing page number). This is the main list content actually shown on the device.
- Sort overflow submenu missing — no Date-Added vs Location-in-Quran radio, no Group-by-Tags toggle, no Recent-pages (show recents) toggle, no Show-Date toggle.
- Default ungrouped layout missing — no 'Page Bookmarks'/'مرجعيات الصفحات' and no 'Ayah Bookmarks'/'مرجعيات الآيات' section headers (the mockup only depicts the grouped-by-tags view).
- 'Not Tagged'/'غير مصنف' header (untagged group in grouped-by-tags mode) missing.
- Long-press contextual multi-select action mode missing — no Tag Bookmark / Edit Tag / Delete Tag / New Tag CAB and no 'Deleted N items' + 'Undo' snackbar.
- Live Quran search results overlay missing — no count band, no results list, and no 'No results'/'لا توجد نتائج' empty state with its hint.
- Toolbar Sort icon and Last-page (ic_goto_quran) icon missing from the hero actions.
- Distinct row iconography missing — real app differentiates page bookmark (ic_favorite), ayah bookmark (gold-tinted favorite), and recent page (current-page glyph); the mockup uses one generic star for all.
- Loading/empty placeholder state ('Loading…'/'جاري التحميل…') not represented.

**Fix (misrepresented):**
- Search placeholder shown as 'Search bookmarks or tags'/'ابحث في العلامات أو الوسوم' — the real field is a whole-Quran search with hint 'Search the Quran'/'بحث في القرآن' (search_hint); it does not search bookmarks or tags.
- Bookmarks tab labelled 'العلامات' in Arabic — the real string is 'المرجعيات' (menu_bookmarks). EN 'Bookmarks' is correct.
- Surahs/Juz' tab Arabic labels shown as 'السور'/'الأجزاء' — real strings are 'سورة' (quran_sura) and 'الجزء' (quran_juz2).
- Hero subtitle EN shown as 'The Noble Qur'an' — real string is 'The Holy Quran' (home_quran_section); Arabic 'القرآن الكريم' is correct.
- Trailing per-row label shown as 'Page 42'/'صفحة 42' — the real trailing element is just the localized page NUMBER (e.g. '42'); the 'Page X, Juz Y' text belongs in the subtitle metadata, not as a separate trailing 'Page N' chip.
- Row leading icon shown as a uniform '★' star — real leading icon is ic_favorite for bookmarks (gold-tinted for ayah bookmarks) and a distinct current-page bookmark glyph for recent pages.

**Remove (invented — not in the app):**
- Tag-group headers 'Memorizing'/'الحفظ' and 'Favorites'/'المفضلة' presented as the default/only view — real default is NOT grouped by tags; grouping is an optional toggle and tag names are user-created (these specific names are invented).
- Per-row chevron '›' — real bookmark/recent rows have no chevron.
- Relative date strings '2 days ago'/'قبل يومين', 'last week', 'last Friday' — real date display is an absolute 'MMM dd, HH:mm' timestamp and only appears when the 'Show Date' toggle is on.
- Back chevron '‹' in the hero — the real toolbar has no navigation/back button (contentInsetStart=0, no nav icon).
- A '🔍' search icon among the toolbar actions — real layout has no separate search toolbar icon; search is the always-visible inline field.
- Specific tag chips 'Ayat al-Kursi'/'آية الكرسي' and 'Du'a'/'دعاء' — inline tag chips ARE a real feature, but these labels are invented sample data, not fixed app content.

**Build actions:**
- Add a Continue-reading card at the very top of the body (consistent with the Surahs-tab card): gold rounded icon frame (book glyph), title 'Continue reading'/'متابعة القراءة', subtitle 'Al-Fatihah · Page 1, Juz 1'/'سورة الفاتحة · صفحة ١، جزء ١', and a gold pill button 'Read'/'قراءة' on the leading side; make the whole card read as tappable.
- Add a 'Recent pages'/'آخر المتصفحات' section as the first list group, with 2–3 rows mirroring the as-built shot (Al-Fatihah p1, Al-Baqarah p4, p6): current-page bookmark glyph at the start, sura-name title, 'Page X, Juz Y'/'صفحة X، جزء Y' subtitle, and a plain localized page number at the trailing edge (drop the 'Page N' wording there).
- Replace the invented 'Memorizing'/'Favorites' tag groups with the real DEFAULT ungrouped layout: a 'Page Bookmarks'/'مرجعيات الصفحات' header with page-bookmark rows and an 'Ayah Bookmarks'/'مرجعيات الآيات' header with ayah-bookmark rows; keep inline tag chips on rows (chips are real). Optionally add a secondary state illustrating the grouped-by-tags view ending with a 'Not Tagged'/'غير مصنف' header.
- Change the search field placeholder to 'Search the Quran'/'بحث في القرآن'. Optionally add a search-active state showing the navy count band, a results list, and a 'No results'/'لا توجد نتائج' empty state (gold lens + hint 'Try a different word, or download a translation to search its text.').
- Fix tab labels: Arabic Bookmarks → 'المرجعيات', Surahs → 'سورة', Juz' → 'الجزء' (keep EN Surahs/Juz'/Bookmarks).
- Change hero subtitle EN to 'The Holy Quran'.
- Remove the back chevron '‹' and the 🔍 search icon from the hero; show the real end actions instead: a Sort icon (sort lines), a Last-page/go-to icon (open-book ic_goto_quran), and the ⋮ overflow.
- Add an overflow-menu mock or annotation listing the Sort submenu — radio 'Date Added'/'Location in Quran', checkboxes 'Group by Tags', 'Recent pages' (show recents), 'Show Date' — plus 'Last page', 'Go to page', 'Settings', 'Help', 'About Us'.
- Remove the per-row chevrons '›' from all rows.
- Replace the relative dates with the real absolute format (e.g. 'Jun 27, 16:35') and present them only as an optional 'Show Date'-on state, or drop them.
- Differentiate the row icons: ic_favorite for page bookmarks, a gold-tinted favorite for ayah bookmarks, and the current-page glyph for recent pages — instead of one generic star.
- Add a long-press contextual action-bar mock/annotation showing the multi-select CAB (Tag Bookmark/'تصنيف المرجعية', Edit Tag/'تعديل الصنف', Delete Tag/'حذف التصنيف', New Tag/'صنف جديد') and the resulting 'Deleted N items'/'تم حذف...' + 'Undo'/'تراجع' snackbar.
- Add a loading/empty placeholder state ('Loading…'/'جاري التحميل…') for when no bookmarks exist yet (matches the as-built device, which shows only recent pages + the continue card).

### 04-reader — Page reader — chrome, view modes, paper modes, audio bar

**Revive (lost features — bring back):**
- The Show Translation / Show Quran toggle (globe, ic_translation) — and the entire translation text view, which is the reader's second view mode.
- The Search action and its collapsible SearchView.
- The overflow (⋮) menu contents: Jump/Go-to-page (انتقال سريع), Focus mode (وضع التركيز), Settings (الإعدادات), Help (مساعدة) — and Show Quran (إلى التلاوة) when in translation mode.
- The translation-mode Sura/Juz jump spinner that replaces the title.
- The ayah long-press contextual toolbar entirely: Bookmark ayah, Tag ayah, Share (link/text/Copy), Translation, Play from here, Recite from here.
- Audio bar: the Qari/reciter selection spinner (stopped mode), the Stop button, the Repeat button + repeat-count cycling (1/2/3/∞), the Audio-settings gear, the Cancel (X), and the entire DOWNLOADING/LOADING state (which is what the as-built screenshot shows), the download-over-mobile-data prompt state, and the recitation (mic/transcript/hide-page/end-session) mode.
- Tap-to-toggle (immersive) chrome behavior and Focus mode hiding all chrome.
- The fact that the page is a printed-mushaf IMAGE (ornamental band, ayah-end roundels) with a separate translation text mode — the mockup shows only one rendered text page.
- Settings-gated layouts (dual-page spread, split page+translation, page type), volume-key navigation, and keepScreenOn.

**Fix (misrepresented):**
- The audio bar is drawn as a music 'now-playing card' (reciter name + '1:02 / 2:48' + a progress/waveform scrubber + prev/play/next + mic avatar). The real AudioStatusBar is a compact, mode-dependent control strip: the playing strip is Stop / Prev / Play-Pause / Next / Repeat / Settings (no title, no time, no scrubber); the stopped strip is Play + an inline reciter dropdown.
- Night mode is shown as a static moon icon; the real control is a checkable toggle whose icon flips (ic_day_mode <-> ic_night_mode) and which is equivalent to the pill's Night chip (they stay in sync).
- The bookmark star is shown static-outline; the real favorite_item is a toggle that fills (ic_favorite) when the page is bookmarked.
- Reading-mode pill order: the mockup lays out Day/Sepia/Night left-to-right, but the as-built Arabic screenshot mirrors it (نهار/Day sits on the RIGHT). The pill, toolbar and audio strip should flip with locale.
- Top-bar visible icon set is wrong: the as-built screenshot shows the globe (translation toggle) as the prominent visible icon, whereas the mockup shows a moon (night) and no globe.
- Play/pause is plain in the mockup; the real play/pause is gold-tinted (gold_accent) while other transport buttons stay white-on-navy.
- Back control is a left chevron '‹' on the left; in Arabic the as-built up/home arrow points right and sits on the right, and it ends the reading session (not a plain browser back).

**Remove (invented — not in the app):**
- A reciter name text label in the audio bar ('Mishary Alafasy' / 'مشاري العفاسي') — the real bar has no name label; the reciter is chosen via a dropdown spinner.
- An elapsed/total time readout ('1:02 / 2:48') — no such time text exists in AudioStatusBar.
- An audio progress/waveform scrubber bar (aprog) in the playing state — the real progress bar appears only in the DOWNLOADING/LOADING state, not as a playback scrubber.
- A mic-avatar 'now playing' thumbnail (🎙️) in the audio bar — not present in the real control strip.

**Build actions:**
- Replace the audio 'now-playing card' with the real PLAYING control strip: [Stop ■] [Prev ⏮] [Play/Pause ▶/⏸ gold-tinted] [Next ⏭] [Repeat ⟳ with a small 1/2/3/∞ badge] [Settings ⚙]. Remove the reciter name label, the '1:02 / 2:48' time, and the waveform/scrubber. Gold-tint ONLY play/pause.
- Add a STOPPED-state variant of the audio bar: [Play ▶] + a reciter dropdown chip (e.g. 'مشاري العفاسي ▾') for picking the Qari (optionally a mic button) — this is the correct home for the reciter name, as a dropdown not a label.
- Add the real LOADING state to match the as-built screenshot: a thin gold progress line + 'جاري التحميل…/Loading…' text + a trailing X (cancel) — since this is the state the screenshot captures.
- Top bar: add the Show Translation toggle (globe/ic_translation) as a visible action (it is the prominent visible icon in the screenshot) and a Search icon; keep a toggleable bookmark star and the overflow; represent night-mode as a checkable day/night toggle (sun while in night, moon while in day) and note it mirrors the pill's Night.
- Sketch/annotate the overflow (⋮) contents: Jump (انتقال سريع), Focus mode (وضع التركيز, checkable), Settings (الإعدادات), Help (مساعدة) — and Show Quran (إلى التلاوة) when in translation mode.
- Add a translation-mode variant of the screen: the title becomes a Sura/Juz dropdown spinner, the page region becomes scrollable ayah+translation text, and the globe action flips to a 'Show Quran' icon — making explicit that the reader has two view modes (page image vs translation text).
- Add an ayah long-press contextual toolbar mock: a floating bar pinned over a gold-highlighted selected ayah with icons Bookmark, Tag, Share (▾ link / text / copy), Translation, Play from here, and Recite from here (mic, shown only when recitation is enabled).
- Annotate tap-to-toggle immersive chrome: tapping the page slides the toolbar, audio bar and pill away; Focus mode does this persistently.
- Render the page as a printed-mushaf surrogate: keep the ornamental surah band and ayah-end roundel markers, use a localized page number (٤٩), and show one ayah with a gold highlight to represent the bookmark/selection highlight.
- Mirror the reading-mode pill order for RTL so Day (نهار) sits on the right exactly as the screenshot; ensure the pill, toolbar and audio strip all flip with EN/AR locale (layoutDirection=locale).
- Keep the Day/Sepia/Night pill but clarify Sepia applies a warm page overlay; keep the selected chip gold-filled.
- Optionally annotate settings-gated layouts (dual-page spread, split page+translation, page type) as reachable via Settings, even though the single-phone mockup shows one page.

### 05-ayah-translation — Ayah panel — translation/tafsir

**Revive (lost features — bring back):**
- The 3-tab SWIPEABLE indicator. The mockup reduces it to three decorative plain dots (one 'on') — it drops the Tag/Bookmark tab, the Audio/Playback tab, the gold active-underline, and the fact that the user can swipe between Bookmark · Translation · Audio panels.
- Multi-select of translations: the ability to enable several translations/tafaseer at once and see them STACKED, each under its bold translator-name header.
- The 'More Translations' / 'المزيد من الترجمات والتفاسير' dropdown entry that opens the Translation Manager.
- The empty state: @string/need_translation message + 'Get Translations' button (when nothing is downloaded).
- The loading ProgressBar state.
- Selectable translation text (long-press select/copy).
- Multi-ayah RANGE mode (several ayahs stacked in one panel; Prev/Next hidden for ranges).
- The selected-ayah HIGHLIGHT on the reader page behind the panel (the tan/gold band over the chosen ayah).
- Scroll affordance for long tafsir content (the panel is a ScrollView).

**Fix (misrepresented):**
- Translator spinner shown as 'Saheeh International ▾' (a single chosen translation name). The real spinner ALWAYS shows the static label 'Translations' / 'التفسير والترجمة' (bold gold) and opens a multi-select checklist — it does not display the selected translator's name.
- Sheet header shows surah name + ayah ref 'البقرة ٢:٢' with an ✕ close button inside the sheet. Real app: the per-ayah header inside the scroll is NUMERIC only ('1:2' / '١:٢', no surah name), and the close/dismiss control is the chevron (ic_action_expand) in the TOP action bar, not an ✕ in a sheet title row.
- Page indicator drawn as three small dots placed BELOW the header. The real indicator is the TOP action bar with three tab ICONS (bookmark/tag, globe, play) and a gold underline under the active one.
- Prev/Next shown as two text pill buttons ('Prev'/'Next') inside the content body. Real controls are icon-ONLY ImageButtons (ic_translation_next / ic_translation_previous) pinned to the bottom-left and bottom-right corners, and they appear only for a single-ayah selection.
- Close glyph: mockup uses ✕; real control uses an expand/collapse chevron (ic_action_expand).

**Remove (invented — not in the app):**
- The centered Arabic ayah line inside the panel ('ذَٰلِكَ ٱلْكِتَٰبُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ', line 45). This sliding translation panel never shows Arabic — InlineTranslationPresenter requests getVerses(false) and InlineTranslationView only renders the translation text. (Arabic-above-translation exists only in the separate full-page Translation reading mode.)
- A separate 'Translation' / 'الترجمة' field LABEL above the spinner. There is no such label in the app — the spinner's static text IS the only label.
- Treating the panel as a single-translation viewer (one translation, one Arabic line, prev/next) — the real screen is one of three swipeable action panels with multi-translation stacking.

**Build actions:**
- Replace the decorative `<div class="dots">` row with the REAL top action bar placed at the top of the sheet (right under the grab handle): a horizontal row of three tab ICONS — bookmark/tag, globe (translation), play/audio — with the globe (active) underlined in gold (gold_accent). Add a label/comment noting these are swipeable tabs: Bookmark · Translation · Audio. Put the dismiss control (a down/expand chevron, mapping ic_action_expand) at the end of this bar (right side for RTL).
- Remove the sheet header row that shows 'البقرة ٢:٢' + ✕. Move the close affordance into the top action bar as the chevron (not an ✕).
- Inside the scroll content, render each ayah's label as the BOLD numeric 'sura:ayah' string — '1:2' (EN) / '١:٢' (AR) — not the surah name; style it as white/gold text-on-navy.
- Change the translator field value from 'Saheeh International ▾' to the static label 'Translations' / 'التفسير والترجمة' in bold gold with a dropdown caret. Remove the separate 'Translation'/'الترجمة' field label. Optionally illustrate the open dropdown as a multi-select CHECKLIST (e.g. two checked translations) ending with a 'More Translations' / 'المزيد من الترجمات والتفاسير' row that links to the Translation Manager.
- Delete the centered Arabic ayah line (line 45) — this panel is translation-only.
- Demonstrate multi-translation STACKING: show two translation blocks, each preceded by a bold translator-name header (e.g. 'Saheeh International' then text, blank line, 'Pickthall' then text), so the design accommodates multiple selected translations/tafaseer.
- Replace the 'Prev'/'Next' pill buttons with two icon-only buttons pinned to the bottom-left and bottom-right corners (chevron-style, mapping ic_translation_next / ic_translation_previous), and note they appear only for a single-ayah selection (hidden for ranges).
- Add an EMPTY-STATE variant of the panel: centered message 'You don\'t have any translations/tafaseer downloaded yet.' / 'لم يتم تحميل أي تفسير أو ترجمة' plus a gold 'Get Translations' / 'حمّل التفسير أو الترجمة' button that opens the Translation Manager (Prev/Next hidden).
- Show the selected ayah HIGHLIGHTED on the reader page behind the sheet (a tan/gold band over the chosen ayah) to match the as-built behaviour.
- Make the translation content area scrollable (overflow) to convey long tafsir, and keep the navy panel surface with white text-on-navy and gold accents (this panel is fixed-navy, so use text_on_navy white, not @color/white which flips at night).

### 06-ayah-playback — Ayah panel — playback config

**Revive (lost features — bring back):**
- The icon TAB STRIP (Play / Translation / Tag) that switches between the three ayah-action panels — replaced by three meaningless carousel dots, losing both the labeled destinations and the gold-underline active indicator on the Play tab.
- The chevron-down (⌄) COLLAPSE control — the real way to dismiss the panel.
- The ∞ (infinity / repeat-indefinitely) option for both repeat counts — the stepper chips show only finite '2×'/'1×' with no ∞ affordance.
- The full 1..25 range of the repeat counts — the wheel-picker capability to scroll up to 25 is reduced to a tiny stepper.
- The dynamic 'Apply and Play' (تطبيق وتشغيل) button label state — mockup only ever shows 'Apply'.
- Dropdown/spinner affordance on the From/To selectors — they are real spinners (114-sura name list + dynamic ayah-number list), shown in the mockup as plain text boxes with no ▼.

**Fix (misrepresented):**
- Repeat-row labels: real 'Play set of verses:' / 'تشغيل مجموعة الآيات:' and 'Play each verse:' / 'تشغيل كل آية:' vs mockup 'Repeat range' / 'تكرار النطاق' and 'Repeat each verse' / 'تكرار كل آية'.
- Order of the two repeat rows is swapped: real layout lists 'Play set of verses' (range) FIRST then 'Play each verse'; mockup lists 'Repeat each verse' first then 'Repeat range'.
- Checkbox text: real 'Only play the above verses' / 'شغل ما تم اختياره من الآيات فقط' vs mockup 'Restrict reading to range' / 'حصر القراءة في النطاق'.
- Repeat control type: real is a horizontal scrolling NumberPicker wheel (neighbors visible, gold underline, up to 25 then ∞); mockup uses '− value +' stepper chips.
- Apply button: real initial state is 'Apply and Play' / 'تطبيق وتشغيل' and is a right/end-aligned button; mockup shows a full-width gold 'Apply' block.
- From/To selectors shown as text inputs rather than dropdown spinners; the value-suffix '2×'/'1×' is invented formatting — the real wheel shows plain (Arabic-Indic) numbers and ∞.

**Remove (invented — not in the app):**
- A 'Playback range' / 'نطاق التشغيل' sheet TITLE — no such title exists in the real panel.
- A ✕ close button in the sheet header — real uses the chevron-down to collapse, there is no ✕.
- A drag grab-handle bar at the top of the sheet — the real panel has none.
- Three carousel pager DOTS — the real navigation is an icon tab strip.
- A dark SCRIM dimming the reader page behind the sheet — the as-built keeps the sepia reader page fully visible/un-dimmed.
- The '2×' / '1×' multiplier suffix on the repeat values.

**Build actions:**
- Replace the carousel dots with the real icon TAB STRIP across the top of the panel: three tabs using ic_play (active, with the gold underline indicator), ic_translation (globe), ic_tag — and add a chevron-down (⌄) at the end as the collapse affordance. Remove the ✕.
- Remove the invented 'Playback range' title and the grab handle so the chrome matches the real tabbed slide-up panel (or, if a title is kept for the redesign, keep it secondary and still render the tab strip + chevron).
- Lighten/remove the scrim so the sepia mushaf page-image stays visible behind the panel, as in the as-built.
- Relabel and reorder the two repeat rows to match source: row 1 = 'Play set of verses:' / 'تشغيل مجموعة الآيات:' (range repeat), row 2 = 'Play each verse:' / 'تشغيل كل آية:' (per-verse repeat).
- Replace the −/+ stepper chips with a control that conveys a SCROLLABLE 1..25 picker AND a reachable ∞ option (e.g. a horizontal wheel/segment with the selected value enlarged + gold underline and ∞ as the value past 25), so repeat-indefinitely is preserved; drop the '2×/1×' suffix and use plain numbers (Arabic-Indic in AR).
- Relabel the checkbox to the real string: 'Only play the above verses' / 'شغل ما تم اختياره من الآيات فقط' (not 'Restrict reading to range').
- Make the primary button show the real dynamic label: default 'Apply and Play' / 'تطبيق وتشغيل' (as in the as-built), with a noted alternate 'Apply' / 'تطبيق' when already playing; align it end/right (gold styling is fine).
- Render the From/To fields as dropdown spinners (add a ▼ chevron): the wider field = sura by name from the 114-sura list ('1. الفاتحة'), the narrower field = ayah number from a list limited to that sura; keep sura flex:2 / ayah flex:1.
- Optionally annotate the behavior that choosing 'Play set of verses' > 1 auto-enables the 'Only play the above verses' checkbox.

### 07-ayah-tags — Ayah action toolbar (bookmark/tag/share/translate/play)

**Revive (lost features — bring back):**
- The entire FLOATING AYAH ACTION TOOLBAR — the navy 5-icon popup bar with its directional pip that is the actual subject of the as-built screenshot — is completely absent. The mockup jumps straight to a Tags bottom-sheet.
- BOOKMARK action and its toggle state (outline ic_not_favorite ↔ filled ic_favorite) — not represented anywhere.
- SHARE action and its inline submenu (Share Ayah Link / Share Ayah Text / Copy Ayah) — dropped entirely.
- TRANSLATE / Tafsir (globe) action — dropped from the toolbar (the mockup's panel only covers Tags, not the Translation page it would open).
- PLAY-from-here audio action — dropped.
- RECITE-from-here (mic) conditional action — dropped.
- Long-press-to-reveal-label affordance — dropped.
- The pip/pointer that anchors the toolbar to the ayah and flips above/below it — dropped.
- The fact that the panel is a 3-page swipeable pager (Tags ↔ Translation/Tafsir ↔ Audio) — only the Tags page is shown; the Translation and Audio pages are not represented despite being reachable by swiping within the same panel.
- The 'New Tag' row leading to a name-entry text dialog (tag_dialog.xml, hint 'Name') — the mockup shows a 'New tag' row but not that it opens an input dialog.

**Fix (misrepresented):**
- Page indicator is drawn as plain dots; the real IconPageIndicator shows the page ICONS (tag glyph, translation glyph, audio/play glyph) tinted gold, not anonymous dots.
- Panel close control is drawn as an '✕'; the real panel close is a collapse chevron (ic_action_expand, actionModeCloseButtonStyle, labelled 'cancel'), not an X.
- Tag rows are drawn with per-tag COLORED category dots (green/gold/orange/red); real tag rows have NO color swatch — just a gold-tinted CheckBox + name.
- The checkbox is drawn as a custom gold rounded '✓' chip; the real control is a standard Material CheckBox with gold buttonTint.
- The mockup's top app bar shows star/moon/overflow icons (the READER header from another screen). That chrome is not part of the ayah action toolbar/panel and conflates two different surfaces here.

**Remove (invented — not in the app):**
- Per-tag color swatches / colored categories — the app's Tag model is only (id, name); tags have no color attribute.
- 'Cancel' and 'Save' buttons inside the in-reader Tags panel — the in-reader (non-dialog) Tags page has no buttons; it toggles live and persists on close. OK/Cancel exist only in the separate Bookmarks-list dialog mode (strings 'dialog_ok' / 'cancel').
- The sheet title 'Add to tags' / 'إضافة إلى الوسوم' — the real in-reader panel header has no title text (just the close chevron + page-icon indicator).
- Preset named tags 'Memorizing / Favorites / Tafsir / Difficult' presented as built-in colored categories — the app ships with only user-created plain tags; these names plus colors imply a preset-category system that does not exist.

**Build actions:**
- ADD the primary missing element: a floating navy action toolbar overlay anchored to the highlighted ayah, with a small triangular pip pointing at it (and note it can flip above/below). Use the navy toolbar background and gold/white icons to match the as-built screenshot.
- In that toolbar, render all 5 default icons in order: bookmark (outline star), tag, share, translate (globe), play. Keep them as icon buttons in a single navy bar — this is the real entry point that the Tags sheet opens from.
- Show the bookmark icon's two states (outline = not bookmarked, filled gold = bookmarked) so the toggle behaviour is clear; label via tooltip 'Bookmark this Ayah'.
- Represent the SHARE submenu: add a second toolbar state (or callout) showing the 3 inline options 'Share Ayah Link', 'Share Ayah Text', 'Copy Ayah' that replace the toolbar icons when Share is tapped. Note Share is hidden in the qaloon flavor.
- Add a note/variant for the conditional 'Recite from here' (mic) icon shown only when recitation is enabled.
- Make the panel indicator ICON-BASED, not dots: show three small gold glyphs (tag, translation, play/audio) with the active one highlighted, and label the panel as a swipeable 3-page pager (Tags · Translation/Tafsir · Audio). Indicate the Translation and Audio pages exist (e.g. peeking edges or a caption 'swipe for Tafsir / Audio').
- Replace the '✕' close with a collapse chevron matching ic_action_expand (the real close/collapse control), positioned per the real header.
- REMOVE the per-tag colored dots from every tag row; make each row a standard checkbox (gold tick) + tag name only, matching tag_row.xml.
- Restyle the check control as a standard gold-tinted Material CheckBox rather than a custom gold chip.
- REMOVE the invented 'Cancel'/'Save' buttons from the in-reader Tags panel (it saves on close). If you want to show buttons, scope them to a separate 'tag dialog (from Bookmarks list)' variant labelled 'OK'/'cancel'.
- REMOVE or relabel the 'Add to tags' header title — the real in-reader panel has no title; keep only the close chevron + page-icon indicator.
- Keep the 'New tag' (+) row but show that it opens a small text-input dialog with hint 'Name' to create a tag; treat the example tag names as neutral user tags (no implied preset categories/colors).
- Drop or clearly separate the star/moon/overflow top app bar — it belongs to the reader header screen, not the ayah toolbar/panel; for this part show the dimmed reader page + the floating toolbar + the Tags panel only.
- Apply the navy_700 panel background and gold accents (gold_accent) consistent with the real panel/indicator colors while keeping the Material-3 navy+gold look.

### 08-translation-page — Translation/Arabic-text reading view (the TEXT mode)

**Revive (lost features — bring back):**
- Sura-header band row (centered sura name on a cream/navy band) — mockup jumps straight to ayahs with no sura header.
- Basmallah row before ayah 1 of a sura (mockup's first row is Al-Baqarah ayah 1 'الٓمٓ' with no basmallah line shown).
- 'Back to page' (goto_quran) toggle and any indication that this content also has an Arabic PAGE-IMAGE mode (the app has BOTH image and text modes for the same page).
- Multi-select translation chooser (the spinner dropdown with checkmarks for multiple downloaded translations/tafaseer) — mockup shows a single fixed 'Saheeh International'.
- Multiple simultaneous translations, each with its own bold ALL-CAPS gold TRANSLATOR header.
- Long-tafseer 'المزيد…' truncate/expand link.
- 'see tafseer of verse N' cross-reference expand link and clickable hyperlinks within translation text.
- Selection state: long-press highlight (gold-faint background) and the floating AyahToolBar (Tag / Translation / Audio / Transcript).
- Audio-playback highlight + auto-scroll-to-playing-ayah.
- User-adjustable font size and night-mode brightness; night-mode rendering of the text view (dark sura-header band, dark selection, brightness-adjusted text).
- Toolbar actions: bookmark/favorite, search (SearchView), night-mode toggle, overflow (Jump / Settings / Focus mode / Help), AudioStatusBar, and the floating Day/Sepia/Night switcher.
- The 'Ayah before translation' preference (the Arabic ayah line is an optional toggle, not always present).
- The entire slide-up ayah translation panel surface (InlineTranslationView) shown in the as-built screenshot: translator spinner, 'sura:ayah' header, selectable text, next/previous-ayah chevrons, Tag/Translation/Audio/Transcript tabs, and the 'Get Translations' empty state.

**Fix (misrepresented):**
- Number badge: mockup shows a plain badge with only the ayah number ('1','2','3'); the real AyahNumberView shows the full 'sura:ayah' reference (e.g. '2:1') in a small square box (grey day / dark night) that also serves as the selection-toolbar anchor.
- Header subtitle: mockup labels the subtitle 'Saheeh International' (a translator); the real toolbar spinner subtitle is the page/juz descriptor (sura name as title + page subtitle), and the translation is chosen inside the multi-select dropdown.
- Row structure: mockup uses a left-gutter number + a right column stacking Arabic over translation; the real layout is full-width stacked rows in order number-box → Arabic → (translator) → translation → divider.
- Standalone centered 'Saheeh International' chip: a translator label appears in the real text mode ONLY when >1 translation is active, rendered as an inline bold ALL-CAPS gold per-ayah header — not as a single centered outlined chip for a single translation.
- The 📖 book icon shown as a generic action: in reality it is specifically goto_quran 'Back to page', the mode switch back to the Arabic page-image — an important toggle, not decoration.

**Remove (invented — not in the app):**
- The standalone centered 'Saheeh International' outlined chip below the header — no such single-translation chip exists in the real text mode.
- Implicitly framing the screen as text-only with no awareness that the same page also has an Arabic page-image mode AND a separate slide-up ayah translation panel (the surface the screenshot actually shows).
- (Verification, not an invention to keep) menu_view_mode.xml's List/Grid/Compact toggle belongs to the reciter/audio surah lists (RecitesName/AyaList), NOT this screen; the mockup correctly omits it — do NOT add a view-mode switcher here.

**Build actions:**
- Add a sura-header band row (centered sura name, cream --paper band day / navy band night, ~16sp) before the first ayah of each sura, matching translation_sura_header.
- Add a basmallah line row (Arabic, Uthmani style) before ayah 1 for every sura except Al-Fatiha and At-Tawbah.
- Change the number badge from '1/2/3' to the 'sura:ayah' reference (e.g. '2:1') in a small square gold-outlined box (navy+gold), and keep it positioned as the anchor where the selection toolbar would appear.
- Replace the static 'Saheeh International' header subtitle with the page/juz descriptor (e.g. title 'البقرة' + subtitle 'Juz 1 · Page 2') and add a caret affordance showing the title is a tappable MULTI-SELECT translation chooser; show selected translation names inside that dropdown rather than as a chip.
- Remove the standalone centered translator chip; instead include a multi-translation example block where each translation is preceded by a bold ALL-CAPS gold translator label, to convey the real multi-translation layout (single-translation case shows no label).
- Make the 📖 header icon explicitly 'Back to page' (goto_quran) with a tooltip, and add the real toolbar set: bookmark/star, search, day/night toggle, and an overflow (⋮) listing Jump / Settings / Focus mode / Help; visually communicate that the page also has an Arabic image mode.
- Add interaction-state variants: a selected-ayah row with a gold-faint highlight background plus a floating AyahToolBar (Tag, Translation, Audio, Transcript icons), and an audio-playing highlight variant.
- Add an example of a long tafseer truncated with a gold 'المزيد…' expand link, and a 'see tafseer of verse N' cross-reference link, to preserve those expand affordances.
- Add a NIGHT-mode variant of the full text view (navy/dark paper, brightness-adjusted text, dark sura-header band, dark gold-faint selection) per the light+dark requirement.
- Reflect user-adjustable font size and the Arabic = 1.4× translation-size relationship rather than hardcoding 21px/13.5px; keep Arabic visibly larger than translation.
- Create a separate mockup (or panel state) for the slide-up ayah translation panel the screenshot shows: gold translator/tafsir selector spinner, 'sura:ayah' header, selectable translation text, next/previous-ayah chevrons, the Tag/Translation/Audio/Transcript tab-icon row (selected tab underlined gold), and the empty 'Get Translations' state — so that surface is not dropped by only mocking the full-text mode.
- Keep palette tokens that already ship in the app (gold_accent #B8860B, navy_900 #172033, cream sura header #F1ECD8, gold_accent_faint selection) so the re-theme matches existing colors.

### 09-search — Search results

**Revive (lost features — bring back):**
- Hero title "Search"/"بحث" and subtitle "The Holy Quran"/"القرآن الكريم" — the mockup collapses everything into a single back+search row and shows no title/subtitle.
- The entire themed empty / no-results state: gold lens-in-frame icon, "No results"/"لا توجد نتائج" title, the "Try a different word, or download a translation..." hint, and the conditional "Get Translations"/"حمّل التفسير أو الترجمة" button.
- The gold inline match highlight in verse snippets (the mockup uses bold black <b> instead of the real gold-colored highlight).
- The full real location string format "Found in Surah X: ayah (page N)" — the mockup abbreviates to "Al-An'am : 54 · Page 134".
- The searched-query echo + trailing colon in the count band (real: "12 results for \"mercy\":" ; mockup shows only "12 results").

**Fix (misrepresented):**
- Count band styling: real is a full-width SOLID navy_700 band with bold white text; the mockup renders it as a thin section divider ("12 results" + hairline rule), not a navy band.
- Result row hierarchy is flipped: real card puts the GOLD location line on TOP and the verse snippet below it; the mockup makes the verse the primary top line and the location a muted secondary line (class "s") underneath.
- Location-line color: real verseLocation is gold_accent; the mockup styles it as muted/secondary, not gold.
- Match highlight: real highlight is GOLD-colored text (translation_highlight); the mockup shows the matched word as bold black <b>.
- Arabic-DB download affordance: real is a gold pill BUTTON labeled "Get Arabic Search Database"/"حمل قاعدة البيانات"; the mockup shows it as a small inline text link "Download"/"تنزيل".
- Warning text is paraphrased: real no_arabic_search_available is "You have not downloaded the Arabic search pack. Please download it and try your search again."; the mockup shortens it to "Arabic search needs a data file."
- Hero structure: real layout is two rows (back + title/subtitle row, then a full-width search field row); the mockup merges back button and the search field onto one row.

**Remove (invented — not in the app):**
- A 📍 location-pin icon on every result row — the real search_result.xml card has no leading icon.
- A › chevron on every result row — the real card has no trailing chevron.
- An inline "Download"/"تنزيل" text link for the Arabic warning — the real screen uses a gold download button (btnGetTranslations), not a text link.

**Build actions:**
- Split the hero into two rows to match search.xml: row 1 = gold back chevron + a vertical text stack with title "Search"/"بحث" (bold, text_on_navy) and subtitle "The Holy Quran"/"القرآن الكريم" (small, hint_on_navy); row 2 = the full-width navy search field (lens + placeholder + ✕ clear). Add the missing title/subtitle.
- Replace the inline "Download" txtlink in the warning with a gold pill button (gold bg, text_on_gold, like bg_download_all_button) labeled "Get Arabic Search Database"/"حمل قاعدة البيانات", and expand the warning copy to the real string "You have not downloaded the Arabic search pack. Please download it and try your search again."/Arabic.
- Convert the "12 results" divider into a full-width SOLID navy (navy_700) band with bold white text, and echo the query with a trailing colon: "12 results for \"mercy\":" / "١٢ نتيجة في البحث عن \"رحمة\"".
- In each result row, REMOVE the 📍 pin icon and the › chevron. Reorder so the GOLD location line is on TOP (gold_accent, ~12sp bold) using the full format "Found in Surah Al-An'am: 54 (page 134)" / "سورة الأنعام: ٥٤ (صفحة 134)", and put the Arabic verse snippet BELOW it as the larger body text (~15sp, line-spacing ~1.3).
- Change the matched-word styling from bold (<b>) to GOLD-colored text (gold_accent / translation_highlight) so it matches the app's Html gold highlight.
- Keep each result as a rounded navy card (bg_search_result_card look) with ~12dp horizontal / ~6dp vertical margins and ~14dp padding, but with no leading icon and no chevron.
- Add a second mockup state (or a toggled variant) for the empty / no-results screen: a centered gold circular icon frame (bg_icon_frame_gold) holding the lens icon, title "No results"/"لا توجد نتائج", hint "Try a different word, or download a translation to search its text."/Arabic, and a gold "Get Translations"/"حمّل التفسير أو الترجمة" button below it.

### 10-jump — Quick-jump dialog

**Revive (lost features — bring back):**
- The second section label "انتقال سريع / Go to page" (string menu_jump) — the mockup replaced it with a "— or —" divider.
- The Surah item NUMBER PREFIX format "{n}. {name}" (e.g. "١. الفَاتِحَةِ") — mockup shows just "البقرة" with no number.
- The fact that the Surah field is a TYPEABLE searchable autocomplete (infix + number + diacritic-insensitive filtering) — mockup renders it as a static picker with only a ▾ caret.
- The single shared-row layout of Surah + Ayah (one row, one shared label) — mockup splits them into two stacked full-width fields.
- Bidirectional auto-fill linking (page→sura/ayah and sura/ayah→page-hint) — not represented.
- Keyboard GO-action submit on the page field, and the auto-shown soft keyboard.
- jumpToAndHighlight behaviour (target ayah is highlighted on arrival) and the page field's dynamic hint = computed page.
- maxLength=3 constraint on the page input.

**Fix (misrepresented):**
- Dialog title: mockup shows "Jump to / الانتقال إلى"; real title is "Go to page" (EN) / "انتقال سريع" (AR, string menu_jump).
- Primary button: mockup labels it "Go / انتقال"; the real button is "OK / موافق" (string dialog_ok).
- Page field label: mockup "Page number / رقم الصفحة"; real label is "Go to page" (EN) / "أدخل رقم الصفحة" (AR, string gotoPage).
- Surah field shown as a non-editable dropdown picker (just a ▾ caret); the real control is a typeable autocomplete-with-search.
- Surah value missing its leading number prefix (real shows e.g. "٢. البَقَرَة").
- Layout: Surah and Ayah presented as two separate stacked full-width fields, each with its own label; real app has them on ONE horizontal row (wide sura + narrow ~64dp ayah) under a single shared label.

**Remove (invented — not in the app):**
- A "Cancel / إلغاء" button — the real dialog has no negative button at all (only one OK button).
- The "— or —" divider between page and surah — does not exist in the app.
- A static "1–604" placeholder in the page field — the real field has no fixed range hint; its hint is the dynamically computed page number.
- A static "1–286" placeholder in the ayah field — no such placeholder exists in the app.
- Separate "Surah / السورة" and "Ayah / الآية" field labels — the app has neither; both inputs share the single menu_jump section header.

**Build actions:**
- Change the dialog header (.dh, line 34) from "Jump to / الانتقال إلى" to the real title: EN "Go to page", AR "انتقال سريع" (string menu_jump).
- Change the page field label (.lab, line 37) from "Page number / رقم الصفحة" to EN "Go to page" / AR "أدخل رقم الصفحة" (string gotoPage). Note the EN quirk: gotoPage and menu_jump both read "Go to page" in English — keep the real strings.
- Remove the static "1–604" placeholder span (line 38). Keep the page input numeric, single-line, maxLength 3, and if any hint is shown make it the dynamically computed page number, not a fixed range.
- Replace the "— or —" divider (lines 41–43) with the real second section label: EN "Go to page" / AR "انتقال سريع" (string menu_jump).
- Merge the two separate Surah and Ayah fields (lines 45–53) into ONE horizontal row under that single label: a wide Surah autocomplete on the right + a narrow centered Ayah number box (~64dp min) on the left in RTL. Delete the standalone "Surah/السورة" and "Ayah/الآية" labels.
- Render the Surah field as a searchable autocomplete (typeable + filtered dropdown), not a static picker: keep a caret but add an affordance/hint that you can type to filter (by name, diacritic-insensitive, or by number), and show the value WITH its number prefix, e.g. AR "٢. البَقَرَة" / EN "2. Al-Baqarah".
- Remove the "1–286" ayah placeholder; show only the numeric value (e.g. 255) center-aligned in the narrow box.
- Footer (lines 55–58): delete the "Cancel / إلغاء" button entirely (the real dialog has a single button), and relabel the remaining button from "Go / انتقال" to EN "OK" / AR "موافق" (string dialog_ok), styled as the gold/primary action and RTL-aligned per the screenshot.
- Annotate or wire the bidirectional behaviour: entering a page auto-fills Surah+Ayah; choosing Surah/Ayah updates the page field hint and clears its text; the keyboard Go key submits; OK navigates AND highlights the chosen ayah (jumpToAndHighlight).
- Keep the navy+gold Material-3 surface but preserve the real structure: three stacked inputs (page, then the shared sura+ayah row), one OK button, RTL mirroring, and an auto-shown keyboard context.

### 11-tags-manage — Bookmark/tag multi-select (CAB)

**Revive (lost features — bring back):**
- The 4th CAB action 'New Tag' (ic_new / 'New Tag' / 'صنف جديد') is missing — the mockup shows only three action icons (🏷️ ✎ 🗑️). New Tag is ALWAYS present in the real CAB.
- The entire Tag Bookmark dialog that the 🏷️ action opens is not depicted in this part: a multi-select checklist of all tags (gold checkboxes), the bottom 'New Tag' (+) row, and OK/Cancel buttons. tag_row.xml was given as source for this screen but nothing in the mockup represents it.
- The Undo affordance on delete: real delete is deferred and shows a Snackbar 'N deleted' + UNDO. The mockup has no snackbar/undo state.
- The ability to select a whole TAG-GROUP HEADER (tagId>=0) — not just individual bookmarks — to rename (Edit Tag) or delete the tag. The mockup only shows bookmark rows being selected, so the header-selection capability (and the state where ✎ Edit Tag appears) is absent.

**Fix (misrepresented):**
- Action set does not match the selected type. The mockup shows Tag + Edit + Delete together while BOOKMARK rows are selected. Real gating: with bookmarks selected, Edit Tag is HIDDEN and Tag Bookmark + Delete + New Tag are shown; Edit Tag only appears for a single tag-header selection (in which case Tag Bookmark is hidden).
- Selected-row affordance is wrong: the mockup swaps the ★ star for a gold ✓ CHECKBOX inside each selected row. The real list has no per-row checkboxes — selection is shown by an activated/highlighted row background while the star icon is retained (gold-faint background is fine; the ✓ checkbox swap is not how it renders). Per-row checkboxes exist ONLY in the Tag Bookmark dialog.
- Delete icon/label: the mockup uses a 🗑️ trash glyph; the real menu uses @drawable/ic_cancel (an X/cancel glyph) and the label is specifically 'Delete Tag' (delete_tag), not a generic delete.

**Remove (invented — not in the app):**
- The CAB title '2 selected' / '2 محدد' — the bookmarks CAB code never calls setTitle/setSubtitle, so the real CAB shows no selected-count title. (A reasonable enhancement, but it is not in the current app.)
- The specific tag chip labels (Memorize/Tafsir/Favorites/Daily / حفظ/تفسير/مفضلة/يومي) are illustrative sample data; there are no built-in preset tags by these names in the source.

**Build actions:**
- Add a 4th CAB action icon for New Tag using ic_new (a '+'/new glyph) labelled 'New Tag' / 'صنف جديد', and keep it visible in every selection state.
- Show two CAB states (or annotate the conditional) to match real gating: STATE A — bookmark rows selected → show 🏷️ Tag Bookmark + 🗑️ Delete + ➕ New Tag, and REMOVE the ✎ Edit Tag icon. STATE B — a single tag-group header selected → show ✎ Edit Tag + 🗑️ Delete + ➕ New Tag, and HIDE Tag Bookmark.
- Add a sub-mockup/panel for the Tag Bookmark dialog: a title-less alert with a scrollable list of tags, each row 48dp tall with a gold-tinted checkbox (gold_accent) + tag name (use tag_row layout), a bottom 'New Tag' row showing a '+' (ic_new) icon instead of a checkbox, and OK / cancel buttons (strings dialog_ok 'OK'/'موافق', cancel 'cancel'/'إلغاء'). Note it supports batch tagging of multiple selected bookmarks.
- Fix the selection affordance in the bookmark list: keep the ★ star icon on selected rows and indicate selection only via the gold-faint activated row background — remove the per-row ✓ checkbox swap (checkboxes belong only to the Tag Bookmark dialog).
- Add a Delete → Undo state: after Delete, show a Snackbar reading the plural bookmark_tag_deleted ('N deleted') with an UNDO button (undo / 'تراجع') styled on @color/snackbar_background_color.
- Add at least one selectable tag-GROUP HEADER row example so the 'select a whole tag to rename/delete' capability is visible, and so STATE B's Edit Tag action has a referent.
- For the delete action, label/semantics should read 'Delete Tag' (delete_tag); the source icon is ic_cancel — if a trash glyph is kept for clarity, keep the 'Delete Tag' label and the deferred+undo behaviour.
- Decide on the 'N selected' title: either drop it to match the current app, or keep it as an intentional, EN/AR-translated enhancement ('2 selected' / '2 محدد') and flag it as new.
- Verify the whole part in BOTH light and dark themes and EN+AR per project rules; ensure the gold checkbox tint maps to gold_accent and Arabic surah/ayah content stays Arabic.

### 12-settings — Settings — display section

**Revive (lost features — bring back):**
- Night mode toggle (القراءة الليلية / 'Use dark background and light fonts') — completely absent.
- Text brightness slider (إضاءة الخط) including its numeric value readout and the 'معاينة'/Preview action — absent.
- Background brightness slider (إضاءة الخلفية) including its colour/black preview swatch — absent.
- Surah translated name switch (ترجمة أسماء السور / 'Show the translation of surah name') — absent (the mockup's 'Arabic surah names' row is actually the mislabeled Arabic-mode pref, see misrepresents).
- Landscape orientation switch (الوضع اﻷفقي) and its dependency-disabled state under Lock orientation — absent.
- Highlight bookmarks switch (تعليم المرجعيات / 'Highlight bookmarked ayahs while reading') — absent.
- Volume key navigation switch (تصفح بمفاتيح الصوت / 'Navigate between pages using volume keys') — absent.
- Entire 'Dual Page Preferences' category: Dual Page Mode (وضع الصفحتين المتجاورتين) and 'Quran and translation in dual mode' (عرض صفحة من المصحف وأخرى من الترجمة) — absent.
- Ayah before translation switch (عرض اﻵية مع التفسير / 'Show ayah in Arabic above the translation') — absent.
- Entire 'Download Options' category: Streaming (استماع عبر اﻻنترنت), Download amount ListPreference (كم التحميل: Page/Sura/Juz), and Audio Manager (إدارة الصوتيات) — all absent (the mockup replaces them with an invented 'Default reciter').
- The actual 'Arabic mode' app-language toggle as a distinct feature — effectively dropped because the row that should be it is relabeled 'Arabic surah names'.
- Representation of dependency/disabled (greyed) rows — the mockup has no greyed/disabled state pattern, so dependent prefs (Landscape, brightness sliders, Split) lose their visible disabled behaviour.
- Two real category headers are dropped/merged: 'Dual Page Preferences' and 'Download Options' have no equivalent section in the mockup.

**Fix (misrepresented):**
- Mockup row 'Arabic surah names / Show names in Arabic script' (toggle ON) misrepresents prefs_use_arabic_names, whose real title is 'Arabic mode (الوضع العربي)' and whose real effect is switching the WHOLE APP interface to Arabic ('Use Arabic for application interface'), not toggling surah-name script. It also collides conceptually with the separate, dropped 'Surah translated name' pref.
- Mockup row 'Reading background / Paper tone for the page' with a value 'Sepia' misrepresents prefs_new_background, which is a plain on/off SwitchPreferenceCompat titled 'New background' (الخلفية الجديدة) with NO summary and NO paper-tone value. The Sepia/paper-tone selector does not exist in Settings (paper tone is chosen in the reader, not here).
- Mockup row 'Lock orientation / Keep portrait while reading' uses a wrong summary; the real summary is 'Adaptive to current orientation mode' (off) / 'Use fixed orientation mode' (on), and the real title is 'Lock screen orientation'. It also omits the dependent Landscape orientation child.
- Mockup row 'Ayah marker popups / Tap a marker for actions' (ON) misrepresents prefs_display_marker_popup: the real behaviour is 'Display popup on reaching juz, hizb, etc.' (an automatic popup at juz/hizb boundaries) — not a tap-a-marker-for-actions feature. Title should be 'Display marker popups' / 'التنبيه'.
- Mockup row 'Page type / Mushaf image set' with value 'Madani' drops the real '(experimental)' qualifier ('Page Type (experimental)' / 'نوع طباعة المصحف (تجريبي)') and replaces the real summary 'Select the type of reading pages'.
- Mockup row 'Translation text size' with value 'Medium' misrepresents prefs_translation_text_size, which is a SLIDER (SeekBarTextSizePreference, 0-40, default 15), not a Small/Medium/Large value picker. Real title 'Translation text size' / 'خط التفسير والترجمة'.
- Mockup row 'Manage translations / 3 installed' invents the '3 installed' count; the real summary is 'Download and manage translations' / 'حمّل واختر التفاسير والتراجم' and the real title is 'Translations' / 'التفسير والترجمة'.
- Mockup 'Advanced settings / Backup, storage, logs' is acceptable but the real title is 'Advanced Options' / 'خيارات متقدمة' and the real summary is 'Import/export bookmarks, set Quran data directory, etc.'

**Remove (invented — not in the app):**
- 'Default reciter / Mishary Alafasy' (value row) — no such preference exists in quran_preferences.xml. There is no default-reciter setting; the nearest real items are Audio Manager, Streaming, and Download amount, all of which the mockup omits.
- 'About & help' navigation row inside the Audio & advanced card — there is NO 'About' item in QuranPreferenceActivity. An About screen (about.xml / AboutFragment) exists in the app but is reached from the home/Quran overflow menu, not from this Settings screen, and there is no 'help' entry at all.
- 'Reading background: Sepia' as a value/picker — invents a paper-tone chooser in Settings that does not exist (real pref is an on/off 'New background' switch).
- '3 installed' translations count on the Translations row — invented dynamic count not present in the real summary.
- 'Audio & advanced' as a category name — invented grouping; the real screen has separate 'Download Options' and 'Advanced Options' categories and no combined audio/advanced section.

**Build actions:**
- Keep the navy hero header + gold back chevron and the existing card grouping, but restructure into the 6 REAL categories in order: Display Settings (إعدادات العرض), Reading Preferences (إعدادات القراءة), Dual Page Preferences (تفضيلات الصفحتين المتجاورتين), Translation Preferences (إعدادات التفسير والترجمة), Download Options (خيارات التحميل), Advanced Options (خيارات متقدمة). Use a gold section label + hairline divider for each, matching the as-built category headers.
- Display card — fix and complete to 8 rows in source order: (1) toggle 'Arabic mode' / 'الوضع العربي', summary 'Use Arabic for application interface' / 'سيتم تحويل التطبيق إلى اللغة العربية' (relabel the current 'Arabic surah names' row); (2) toggle 'New background' / 'الخلفية الجديدة' with NO value chip — remove the 'Sepia' paper-tone value; (3) toggle 'Lock screen orientation' / 'اﻹبقاء على وضع العرض' summary 'Adaptive to current orientation mode' / 'وضع العرض يتغير تلقائيا'; (4) ADD toggle 'Landscape orientation' / 'الوضع اﻷفقي' shown in a DISABLED/greyed state (depends on Lock); (5) ADD toggle 'Surah translated name' / 'ترجمة أسماء السور' summary 'Show the translation of surah name' / 'إظهار أسماء السور مترجمة'; (6) ADD toggle 'Night mode' / 'القراءة الليلية' summary 'Use dark background and light fonts' / 'الخلفية باللون اﻷسود والخطوط باللون اﻷبيض'; (7) ADD a SLIDER row 'Text brightness' / 'إضاءة الخط' with a numeric value readout (e.g. 255) and a small gold 'Preview / معاينة' action, shown greyed (depends on Night mode); (8) ADD a SLIDER row 'Background brightness' / 'إضاءة الخلفية' with a colour/black preview swatch at the leading edge, greyed (depends on Night mode).
- Reading card — make it 5 rows: (1) 'Page Type (experimental)' / 'نوع طباعة المصحف (تجريبي)', summary 'Select the type of reading pages', value chip 'Madani' OK — add '(experimental)'; (2) toggle 'Show page info' / 'عرض بيانات الصفحة' summary 'Overlay page number, surah name, and juz number while reading'; (3) toggle 'Display marker popups' / 'التنبيه' with CORRECT summary 'Display popup on reaching juz, hizb, etc.' / 'عرض تنبيه عند الوصول إلى بداية الربع أو الجزء' (rewrite the 'Tap a marker for actions' text); (4) ADD toggle 'Highlight bookmarks' / 'تعليم المرجعيات' summary 'Highlight bookmarked ayahs while reading'; (5) ADD toggle 'Volume key navigation' / 'تصفح بمفاتيح الصوت' summary 'Navigate between pages using volume keys'.
- ADD a new 'Dual Page Preferences' card with 2 rows: (1) toggle 'Dual Page Mode' / 'وضع الصفحتين المتجاورتين' summaryOn 'In landscape, two pages will appear side by side.'; (2) toggle 'Quran and translation in dual mode' / 'عرض صفحة من المصحف وأخرى من الترجمة', shown greyed (depends on Dual Page Mode).
- Translation card — make it 3 rows: (1) nav row 'Translations' / 'التفسير والترجمة' summary 'Download and manage translations' (drop the invented '3 installed'); (2) ADD toggle 'Ayah before translation' / 'عرض اﻵية مع التفسير' summary 'Show ayah in Arabic above the translation'; (3) convert 'Translation text size' from a value chip to a SLIDER row (range ~0-40) labelled 'Translation text size' / 'خط التفسير والترجمة'.
- REPLACE the invented 'Audio & advanced' card with a real 'Download Options' card (3 rows): (1) toggle 'Streaming' / 'استماع عبر اﻻنترنت' summary 'Stream audio instead of downloading'; (2) dialog-picker row 'Download amount' / 'كم التحميل' value chip showing the chosen option (Page/Sura/Juz), summary 'Preferred download amount for non-gapless audio'; (3) nav row 'Audio Manager' / 'إدارة الصوتيات' summary 'Manage and download Quranic audio'. DELETE the invented 'Default reciter / Mishary Alafasy' row.
- ADD an 'Advanced Options' card with a single nav row 'Advanced Options' / 'خيارات متقدمة' summary 'Import/export bookmarks, set Quran data directory, etc.' and a chevron. DELETE the invented 'About & help' row (About is not part of this Settings screen).
- Replace emoji glyphs with gold-tinted Material vector icons matching each real drawable: ic_pref_language, ic_pref_image, ic_pref_screen_lock, ic_pref_landscape, ic_pref_label, ic_pref_dark_mode, ic_pref_brightness (x2), ic_pref_description, ic_pref_info, ic_pref_marker, ic_pref_bookmark, ic_pref_volume, ic_pref_tablet, ic_pref_split_screen, ic_pref_translate, ic_pref_sort, ic_pref_text_size, ic_pref_stream, ic_pref_download, ic_pref_headphones, ic_pref_advanced.
- Introduce a reusable DISABLED row style (reduced-opacity icon/title/control) so dependent rows (Landscape, both brightness sliders, Split-in-dual-mode) render greyed when their parent toggle is off, matching the as-built screenshot. Verify the whole screen in BOTH light and dark themes and in EN + AR (RTL), since this is an Arabic-first screen.
- Update the header subtitle to reflect real scope, e.g. 'Display · reading · dual · translation · download' (or keep generic) rather than implying only 'audio'; ensure every added string has both values/ (EN) and values-ar/ (AR) entries using the exact real strings listed above.

### 13-settings-advanced — Settings — advanced/reading section

**Revive (lost features — bring back):**
- ENTIRE lower main-settings content that ab-settings-adv.png actually shows is absent from this mockup part — it was replaced wholesale by the Advanced sub-screen. Specifically dropped:
- Dual Page Mode toggle ('وضع الصفحتين المتجاورتين') and its summaryOn/Off describing landscape side-by-side pages.
- 'Quran and translation in dual mode' toggle ('عرض صفحة من المصحف وأخرى من الترجمة'), including its dependency on Dual Page Mode.
- Translations/tafsir manager row 'Translations' ('التفسير والترجمة' — حمّل واختر التفاسير والتراجم).
- 'Ayah before translation' toggle ('عرض اﻵية مع التفسير').
- 'Translation text size' slider with the live 'Preview / معاينة' sample ('خط التفسير والترجمة').
- Streaming toggle ('استماع عبر اﻻنترنت').
- 'Download amount' single-choice list ('كم التحميل').
- 'Audio Manager' row ('إدارة الصوتيات').
- Within the sub-screen the mockup also drops the real CONDITIONAL behaviour of the storage row (it is hidden when there is ≤1 storage option) and the BUILD-GATED nature of Send logs (debug/beta only).
- The data-size value's true location: it is appended into the storage-location row's summary ('Current data size is N MB'), not a feature of its own — the mockup loses that linkage.

**Fix (misrepresented):**
- 'Export reading progress (CSV)' with subtitle 'Khatmah & session history' — WRONG. The real 'Export CSV' exports the same bookmarks+tags as the JSON export, just in CSV (summary 'Export a copy of bookmarks and tags in CSV'). It has nothing to do with khatmah/reading-progress/session history.
- 'App storage location' shown as a row with a static value 'Internal' — it is actually the 'Quran data directory' / 'مكان التطبيق' DataListPreference: a radio dialog of storage mounts (each with free space), and the row is hidden entirely on single-storage devices. Subtitle 'Where pages & audio are kept' is invented wording; real summary is 'Choose where to store Quran files' / 'اختر الذاكرة التي سيتم تخزين الملفات عليها'.
- 'Send logs to developers' with subtitle 'Help diagnose a problem' — real title is just 'Send logs' / 'إرسال السجلات', summary 'Send debug logs to the developer' / 'إرسال سجلات التصحيح للمطور'; and crucially this row only exists in debug/beta builds, not release.
- 'Import bookmarks' / 'Export bookmarks' titles — real titles are simply 'Import' / 'استيراد' and 'Export' / 'تصدير' (real summaries mention 'bookmarks and tags', not just bookmarks).
- Header subtitle 'Backup · storage · logs' implies a full backup feature set; the real screen's own summary is 'Import/export bookmarks, set Quran data directory, etc.' / 'استيراد وتصدير المرجعيات وإعدادات أخرى'.

**Remove (invented — not in the app):**
- 'Reset all settings — Restore defaults, can't be undone' (red/danger row). No reset-to-defaults / clear-all preference exists anywhere in the app — fully invented.
- 'Quran data size — 248 MB' as a standalone row. There is no separate data-size preference; the data size only appears as a line inside the storage-location row's summary ('Current data size is N MB').
- 'Export reading progress / Khatmah & session history' as a CSV target — no such export exists; CSV export is bookmarks+tags.
- A populated 'Diagnostics' section in the general (release) build — its only real member, Send logs, is stripped from release builds, and Reset all settings is invented, so this section has no guaranteed real content.

**Build actions:**
- Decide scope explicitly: keep part 13 (13-settings-advanced.html) as the Advanced SUB-screen, but move the dual-page/translation/download categories that ab-settings-adv.png actually shows into the main-settings mockup (part 12, 12-settings.html), since they belong to the parent screen. Right now they have no home in either part.
- In 13-settings-advanced.html, DELETE the 'Reset all settings' prow entirely (HTML lines 51-54) — no such feature.
- DELETE the standalone 'Quran data size / 248 MB' prow (lines 39-42). Instead append the size into the storage row's subtitle, e.g. data-en='Choose where to store Quran files · current data 248 MB' / data-ar='اختر الذاكرة التي سيتم تخزين الملفات عليها · الحجم الحالي 248 م.ب'.
- Rename the storage row title to data-en='Quran data directory' / data-ar='مكان التطبيق'. Render its value as a selectable list (radio dialog) rather than a fixed 'Internal' label — show the chosen mount, and add a note that this row is hidden on single-storage devices.
- Fix the CSV row: title data-en='Export CSV' / data-ar='تصدير CSV', subtitle data-en='Export a copy of bookmarks and tags in CSV' / data-ar='تصدير نسخة من المرجعيات والتصنيفات بصيغة CSV'. Remove the 'reading progress / Khatmah & session history' wording (and add the missing AR strings prefs_export_csv_title/summary in values-ar).
- Rename 'Import bookmarks'→ title 'Import' / 'استيراد', subtitle 'Import bookmarks and tags' / 'استيراد الإشارات المرجعية والعلامات'. Rename 'Export bookmarks'→ title 'Export' / 'تصدير', subtitle 'Export a copy of bookmarks and tags' / 'تصدير نسخة من المرجعيات والتصنيفات'.
- Fix the logs row: title 'Send logs' / 'إرسال السجلات', subtitle 'Send debug logs to the developer' / 'إرسال سجلات التصحيح للمطور'. Mark it debug/beta-only (or omit from the default-build mockup) so the design doesn't imply it ships in release.
- Set the sub-screen header to data-en='Advanced Options' / data-ar='خيارات متقدمة', subtitle data-en='Import/export · storage' / data-ar='استيراد وتصدير · تخزين'. Gold-tint icons mapping to the real drawables: ic_pref_import (import), ic_pref_export (export), ic_pref_csv (CSV), ic_pref_folder (storage), ic_pref_bug_report (logs).
- Add the missing main-settings categories (target part 12) using real labels: (a) Dual Page Preferences — 'Dual Page Mode' toggle + dependent 'Quran and translation in dual mode' toggle; (b) Translation Preferences — 'Translations' (manage tafsir/translations), 'Ayah before translation' toggle, 'Translation text size' slider WITH a live 'Preview / معاينة' sample; (c) Download Options — 'Streaming' toggle, 'Download amount' single-choice list, 'Audio Manager' row.
- Verify the whole redesign in BOTH light and dark themes and in EN and AR (RTL): the danger/red treatment is being removed with Reset, so re-check that no stray red token remains; keep the navy+gold Material-3 chrome over the existing card layout.

### 14-about — About screen

**Revive (lost features — bring back):**
- QuranicAudio data-source row and its https://quranicaudio.com link.
- Electronic Moshaf Project / 'مشروع المصحف الإلكتروني' (KSU) row and its https://quran.ksu.edu.sa link plus its descriptive summary.
- Al-Bāḥith al-Qur'ānī (tafsir.app) row and its https://tafsir.app link.
- Noble Quran Encyclopedia (QuranEnc) / 'موسوعة القرآن الكريم' row and https://quranenc.com link.
- Tanzil / 'تنزيل' row and http://tanzil.net link.
- Noorhidayat / 'نور وهداية' row and http://www.noorehidayat.org link.
- The ENTIRE 'Open Source Projects' / 'مشاريع مفتوحة المصدر' category with all 12 library entries (AndroidX, Kotlin, Dagger 2, OkHttp, RxJava, RxAndroid, Moshi, AndroidSlidingUpPanel, Timber, Material Components for Android, dnsjava, Number Picker) and their URL subtitles.
- The behavior that data-source and open-source rows are tappable links that open external web pages.
- The real app logo image (@drawable/icon) in the header.
- The about_description tagline in the header ('هذا التطبيق مجاني. لا تنسونا من صالح دعائكم.').
- The flavor-specific Images summary text and its link (e.g. about_madani_images_url for the madani build).

**Fix (misrepresented):**
- Toolbar title: mockup shows 'About & Help' / 'حول والمساعدة'; the real title is 'About Us' / 'عنا' (string menu_about).
- Header app name: mockup shows 'My Stream — Mushaf' / 'ماي ستريم — المصحف'; real header shows just 'My Stream' (string app_name).
- 'Quran text sources' card lists four rows (Madani, Naskh, Qaloon, Warsh) all attributed to 'King Fahd Complex'. Reality: only ONE Images row is shown (the active flavor), titled simply 'Images'/'الصور'; and the sources differ — naskh = SHL Info Systems, qaloon = Nous-Mêmes Éditions (Tunisia), only madani = King Fahd Complex. The card both over-lists and mis-attributes.
- Section grouping/title: mockup splits content into 'Quran text sources' + 'Support'; the real first category is 'Data Sources' / 'مصادر المعلومات' which mixes image, audio, tafsir and translation sources (QuranicAudio, KSU, tafsir.app, QuranEnc, Tanzil, Noorhidayat), not only text/images.
- The 'About' paragraph card text is invented marketing copy and does not match the real header tagline (about_description); the real screen has no standalone About description paragraph card — the description lives in the header summary.

**Remove (invented — not in the app):**
- 'v4.2.1' version number — no version string appears anywhere on the real About screen.
- The 'About' descriptive paragraph ('A calm navy-and-gold Qur'an reader…') — invented copy not present in the app.
- 'Warsh' as a Quran text source — there is no Warsh image string/source in the app (only madani/naskh/qaloon variants exist).
- The entire 'Support' / 'الدعم' section with 'Help & FAQ' / 'المساعدة والأسئلة', 'Contact us' / 'تواصل معنا', and 'Rate the app' / 'قيّم التطبيق' — none of these exist in about.xml or AboutFragment; this screen has no help, contact, or rate actions.
- The book emoji (📖) header icon — the real header uses the app's actual logo drawable.

**Build actions:**
- Rename the title bar to the real label: data-en 'About Us', data-ar 'عنا' (string menu_about). Keep the existing navy bar and back chevron.
- Header: replace the 📖 emoji tile with the real app logo (@drawable/icon equivalent), set the name to data-en 'My Stream' / data-ar 'My Stream' (app_name), and REMOVE the 'v4.2.1' version line. Add the real tagline below the name: data-en 'Quran for Android is a free Quran application. Please do not forget the contributors in your prayers.' / data-ar 'هذا التطبيق مجاني. لا تنسونا من صالح دعائكم.' (about_description).
- Delete the invented 'About' paragraph card entirely (its copy duplicates/replaces the header tagline).
- Rename the 'Quran text sources' section to 'Data Sources' / 'مصادر المعلومات' (about_data_sources) and rebuild its card to contain ONE Images row plus all the other source rows, in this order: 1) 'Images'/'الصور' with summary 'King Fahd Quran Printing Complex'/'مجمع الملك فهد...' (madani flavor — show only one Images row, not four; drop Warsh); 2) 'QuranicAudio' — 'Gapless mp3 Quran recitations'/'التلاوات الصوتية المتصلة'; 3) 'Electronic Moshaf Project'/'مشروع المصحف الإلكتروني' — KSU summary; 4) 'Al-Bāḥith al-Qur'ānī (tafsir.app)' — 'Uthmani text and Arabic Tafaseer'/'النص العثماني والتفاسير العربية'; 5) 'Noble Quran Encyclopedia (QuranEnc)'/'موسوعة القرآن الكريم' — 'Translations for many languages'/'الترجمات لعدد من اللغات'; 6) 'Tanzil'/'تنزيل' — 'Translations for a few languages'/'ترجمات لبعض اللغات'; 7) 'Noorhidayat'/'نور وهداية (Noorhidayat)' — 'Noorehira font and Mufti Taqi Translation'/'خط نورحِرا وترجمة مفتي تقي'.
- Show each Data-Sources row as an external-link row (e.g. an open-in-browser/↗ affordance instead of a ›/chevron) since tapping opens a web URL; note that the Images row is a link only for madani/naskh flavors (qaloon Images has no link).
- Add a new section 'Open Source Projects' / 'مشاريع مفتوحة المصدر' (about_open_source) as a card with 12 link rows, each title = library name and subtitle = its URL: AndroidX, Kotlin, Dagger 2, OkHttp, RxJava, RxAndroid, Moshi, AndroidSlidingUpPanel, Timber, 'Material Components for Android', dnsjava, 'Number Picker'. Give each the external-link affordance.
- Delete the entire invented 'Support' section (Help & FAQ, Contact us, Rate the app) — these actions do not exist on this screen.
- Drop leading per-row icons to match the app (rows reserve no icon space) or keep them only as decorative; ensure layout mirrors correctly for AR (RTL) since this is primarily an Arabic screen.
- Verify in both light and dark themes: the header name is white-on-navy in the toolbar/header region, and grey summaries remain legible (per the navy+gold Material-3 palette).

### 15-translations-manager — Tafsir & Translation manager

**Revive (lost features — bring back):**
- The leading globe icon (ic_translation) that appears on EVERY row in the real app — the mockup shows no leading icon on available rows and replaces it with a drag handle on downloaded rows.
- The 'Update Available'/'هناك تحديثات' upgrade state (installed-but-outdated translation shown with a gold download icon + that subtitle, tap to upgrade).
- Pull-to-refresh (SwipeRefreshLayout) to reload the server list, plus the loading spinner and the error Snackbar ('Unable to download the list of translations. Please try again later.').
- The delete-confirmation bottom sheet (title 'Remove Translation?', message 'Are you sure you would like to remove the {name}?', Remove / cancel buttons).
- The real secondary-line content: the translator/scholar name (e.g. 'Sahih International', 'Hasan Nahi', Arabic names like 'تفسير ابن كثير'). The mockup shows language descriptors instead.

**Fix (misrepresented):**
- Reorder mechanism: the mockup puts inline ▲/▼ buttons and drag handles (⠿) on each downloaded row with the hint 'tap & hold to reorder'. Reality: long-press a downloaded row to enter a TOP contextual action bar (ActionMode) holding Move Up / Move Down / Remove icons; there are no per-row arrows and no drag handles, and the selected row is highlighted.
- Delete: the mockup shows an inline 🗑 trash icon per downloaded row. Reality: removal is the 'Remove Translation' action inside the contextual action bar, gated by a confirmation bottom sheet.
- Download progress: the mockup shows an inline progress bar at 64% plus a 'downloading…' subtitle on a row. Reality: there is NO inline progress in this screen — download progress is surfaced only in the system notification.
- Secondary line meaning: the mockup labels rows by language ('English', 'Arabic · Al-Muyassar'). Reality: the secondary line is the translator/author name (translatorNameLocalized or translator), not the language.
- Row titles: the mockup uses short labels ('Saheeh International', 'Pickthall'). Reality: titles are full display names (e.g. 'English Translation (Sahih International)').
- Header/section labels: AR title is 'الترجمات' but the real title string (prefs_translations) is 'التفسير والترجمة'; the 'Available' section should read 'Available for download'/'الملفات المتاحة للتحميل' and 'Downloaded' AR is 'الملفات المحملة'.
- Header subtitle: the mockup adds '3 installed · tap & hold to reorder' under the title — the real hero has a single-line title with no subtitle.

**Remove (invented — not in the app):**
- A search lens icon (🔍) in the header — this screen has no search and no options/overflow menu (only the long-press contextual menu exists).
- The header subtitle line '3 installed · tap & hold to reorder'.
- Drag handles (⠿) on downloaded rows.
- Inline per-row ▲ / ▼ / 🗑 action buttons on downloaded rows.
- An inline download progress bar with '64%' on a 'Pickthall · downloading…' row.

**Build actions:**
- Remove the 🔍 icon from the header .acts — leave only the gold back chevron + title (mirror the chevron to point right in the AR/RTL render).
- Set the header title to prefs_translations: EN 'Translations', and FIX data-ar to 'التفسير والترجمة' (not 'الترجمات'). Delete the subtitle div '3 installed · tap & hold to reorder' (no subtitle exists in the real hero).
- Keep the two gold underlined section headers but use the real labels: 'Downloaded'/'الملفات المحملة' and 'Available for download'/'الملفات المتاحة للتحميل'.
- Add a muted/grey leading globe icon (ic_translation) at the START of EVERY row — both downloaded and available — matching the screenshot.
- On downloaded rows: delete the ⠿ drag handle and the inline ▲ ▼ 🗑 controls. A downloaded-and-current row has NO trailing control. Show title = full display name (e.g. 'English Translation (Sahih International)') and subtitle = the translator name (e.g. 'Sahih International'), not the language.
- Depict reorder + delete as a contextual selection bar: render one downloaded row in a selected/highlighted state and draw a contextual action bar over the navy hero containing three gold icon+label actions — Move Up (arrow_circle_up / 'Move Up' / 'تحريك للأعلى'), Move Down (arrow_circle_down / 'Move Down' / 'تحريك للأسفل'), Remove Translation (ic_cancel / 'Remove Translation' / 'حذف الترجمة'). Add a caption noting these appear on long-press.
- Replace the fake 'downloading… 64%' inline bar with a real UPGRADE-state row instead: an installed item showing a gold download icon plus subtitle 'Update Available'/'هناك تحديثات'. If a downloading affordance is desired, show it as a system notification, not an in-list progress bar.
- Available rows: keep the gold download icon (ic_download) on the END side and make the whole row the download tap target. Use full real titles + translator subtitles; for Arabic tafsirs show the Arabic translator/scholar name (e.g. row 'Arabic Jalalayn Tafseer' with Arabic name 'تفسير الجلالين').
- Optionally add a secondary state mockup for the delete-confirmation bottom sheet: title 'Remove Translation?'/'امسح التفسير/الترجمة؟', message 'Are you sure you would like to remove the {name}?'/'هل ترغب في حذف {name}؟', buttons 'Remove'/'احذف' and 'cancel'/'إلغاء'.
- Optionally indicate the server-loaded nature: a pull-to-refresh spinner affordance and the error Snackbar text ('Unable to download the list of translations. Please try again later.'/'لم يتم تحميل الملف المطلوب، من فضلك أعد المحاولة').
- Ensure full RTL mirroring in the AR render: globe icon on the right, gold download arrow on the left, section headers right-aligned, back chevron right-pointing.

### 16-reciters-manager — Reciters audio manager (list + per-reciter surahs)

**Revive (lost features — bring back):**
- The ENTIRE per-reciter surahs screen (SheikhAudioManagerActivity / prop-05) is absent from the mockup — no surah list, no 'Download all' toolbar action, no per-surah download/delete, no count subtitle.
- The bulk-download dialog (Download all → from-surah / to-surah autocomplete pickers + 'Download selection') is not depicted.
- The multi-select contextual mode (long-press to select, 'Download selection' / 'Delete selection' contextual bar, gold-faint selected-row highlight) is not depicted.
- The delete-confirmation bottom sheet ('Remove Surah? / حذف الملف الصوتي؟' with Remove/Cancel) is not depicted.
- The list-row 'files_downloaded' plural copy is dropped — including the empty state 'No surahs downloaded yet / لم يتم تحميل سور بعد' that is actually the default state shown for every reciter in prop-04.
- The partial-download leading-icon state is dropped: the real list shows a gold CHECK icon for any reciter with ≥1 surah downloaded (not only 114/114); the mockup only shows a chip for fully-downloaded.
- The reciter-name style annotations seen in prop-04 ('(متصل)', '(مجود، متصل)', '(معلم، متصل)', 'ترجمة انجليزية') are not represented on the mock reciter names.
- The loading ProgressBar (spinner) state is not represented.
- The delete-result navy+gold Snackbar is not depicted.

**Fix (misrepresented):**
- List-row subtitle format: mockup shows 'X / 114 surahs / X / ١١٤ سورة' on the reciter rows, but that X/114 fraction is actually the PER-RECITER screen's toolbar subtitle. Real reciter rows use the files_downloaded plural ('X surahs downloaded' / 'No surahs downloaded yet').
- Row interaction model: mockup puts per-row controls on the reciter list (download ⬇ arrow, progress bar, chip), implying you download a whole reciter from the list. In the real app the list row's only action is to NAVIGATE into the surah manager; downloading happens on the per-reciter screen.
- Leading element: real rows have a FUNCTIONAL circular gold button (download icon, or check when something is downloaded); the mockup replaces it with a decorative 🎙️ mic avatar.
- Row container: real rows are flat rows on a cream surface with a thin scrollbar; mockup renders them as elevated rounded cards (acceptable as a re-theme, but should not imply different behavior).
- Header subtitle wording: real is 'Reciters · tap to manage surahs / القرّاء · اضغط لإدارة السور' (communicates the tap-to-open affordance); mockup uses 'Manage downloaded audio / إدارة الصوتيات المُنزّلة', losing the tap-to-manage hint. (Mock title 'Reciters/القرّاء' vs app 'Audio Manager/إدارة الصوتيات' also differs.)

**Remove (invented — not in the app):**
- A search icon (🔍) in the header — the reciter list has no Toolbar menu and no SearchView; there is no search on this screen.
- A 🎙️ microphone avatar per reciter — the app has no reciter avatar; that slot is the functional download/check button.
- A 'Downloaded / مُثبّت' status chip — the app has no such chip; downloaded state is conveyed by the check icon + the 'X surahs downloaded' count.
- An inline per-reciter download progress bar with a percentage (e.g. '72% / ٧٢٪') on the list rows — the list rows show no progress bar or percentage; download progress is handled on the per-reciter screen via the download service/notification.
- A per-reciter ⬇ download arrow on the list rows implying a one-tap 'download this whole reciter' action from the list — no such control exists on the list screen.

**Build actions:**
- Add a SECOND mockup section for the per-reciter surahs screen (mirror prop-05): navy toolbar with gold back arrow, title = reciter name (e.g. 'أبو بكر الشاطري (متصل)'), subtitle = 'X / 114 surahs / X / ١١٤ سورة', and a gold 'Download all / تحميل الكل' toolbar icon on the leading edge. Below it a list of surah rows, each: surah Arabic name (e.g. 'سُورَةُ الفَاتِحَةِ'), status text 'Download surah / تحميل السورة' (or 'Delete surah / حذف السورة'), and a gold circular download icon (or a RED delete icon for downloaded surahs).
- Add a mockup of the bulk-download dialog: title 'Download all / تحميل الكل', a 'Play from / من' surah-picker field and a 'Play to / إلى' surah-picker field (autocomplete over surah names), positive button 'Download selection / تحميل التحديد'.
- Add a multi-select contextual-mode state to the surah screen: selected rows highlighted with gold_accent_faint, and a contextual bar showing gold 'Download selection / تحميل التحديد' and 'Delete selection / حذف التحديد' actions (note each appears conditionally on the selection mix).
- Add the delete-confirmation bottom sheet mock: title 'Remove Surah? / حذف الملف الصوتي؟', message referencing the surah name, buttons 'Remove / احذف' and 'Cancel / إلغاء' (navy+gold styling).
- On the RECITER LIST: remove the 🔍 search icon from the header (no search exists on this screen).
- Replace the list-row subtitle '114 / 114 surahs' with the real plural copy: 'X surahs downloaded / تم تحميل X سورة', and show the default empty state 'No surahs downloaded yet / لم يتم تحميل سور بعد'.
- Replace the 🎙️ mic avatar with the real functional leading control: a gold circular download icon for reciters with nothing downloaded, and a gold circular CHECK icon for any reciter with ≥1 surah downloaded.
- Remove the inline progress bar + percentage and the per-reciter ⬇ download arrow from the list rows; the list row has no download control — tapping the whole row opens the surah manager.
- Remove the 'Downloaded / مُثبّت' chip; convey downloaded-ness via the check icon plus the count subtitle.
- Keep reciter names with their style annotations verbatim ('(متصل)', '(مجود، متصل)', '(معلم، متصل)', 'ترجمة انجليزية') so the variant info survives.
- Set the list header to the real strings: title 'Audio Manager / إدارة الصوتيات' and subtitle 'Reciters · tap to manage surahs / القرّاء · اضغط لإدارة السور' to preserve the tap-to-manage hint.
- Card vs flat row is acceptable as a re-theme choice, but ensure the design reads as 'tap row to open' (single navigation affordance), and optionally show a loading spinner state.

### 17-data-download — First-run data download & permissions gate

**Revive (lost features — bring back):**
- The actual download-prompt step: a non-cancelable Yes/No bottom-sheet titled 'Download Required Files?' (downloadPrompt_title) with Yes (downloadPrompt_ok) / No (downloadPrompt_no). The mockup has no Yes/No prompt at all.
- Storage-permission rationale bottom-sheet (storage_permission_rationale, OK/Cancel) + the system permission request + the 'Please restart the app…' toast.
- Migration 'Please wait…' indeterminate gold-spinner dialog (migration_upgrade.xml) used on Android 11+ while moving files.
- The Processing/Extracting phase ('Processing…' = extracting_title, 'Processing file N / M' = process_progress) — half of the real progress flow.
- The indeterminate progress state (progress == -1).
- The non-fatal auto-retry message 'Corrupted file, attempting to re-download' (download_error_invalid_download_retry, STATE_ERROR_WILL_RETRY).
- The fatal-error bottom-sheet with Retry (download_retry) / Cancel (download_cancel).
- The Cancel button on the progress dialog (R.string.cancel) that cancels the in-flight download.
- The three context-specific prompt copies (fresh full download vs tablet-images vs patch update).
- The 'resume supported' messaging from downloading_message.
- The real live status text (Downloaded X MB / Y MB; '…of surah N'; 'Downloading surah N ayah M').

**Fix (misrepresented):**
- Progress detail 'page 374 of 604': the app NEVER shows a page-of-604 counter here. It shows MB ('Downloaded 12.34 MB / 45.00 MB') or surah/ayah ('Downloaded … of surah N' / 'Downloading surah N ayah M'). 604 is not used on this gate.
- 'Download' + 'Use streaming instead' shown as two co-equal buttons under a live progress card: in reality the prompt (Yes/No bottom-sheet) and the progress dialog are SEPARATE, mutually-exclusive modal states; they never appear together, and there is no ghost streaming button.
- Renders the gate as a styled full-screen surface (navy gradient + book glyph + headline): the real activity has no content view — every step is a dialog/bottom-sheet over the launching window.
- Gold '62%' as the headline progress figure: the real UI is a standard horizontal determinate bar; the numeric percentage is the bar's own, and the text line carries the MB / surah info rather than a big gold percent.
- Top app bar titled 'Mushaf' over this gate: this gate has no toolbar — that bar belongs to other screens, not the data-download/permissions step.

**Remove (invented — not in the app):**
- The entire full-screen download page (navy gradient background, 📖 glyph, headline 'Download Quran pages').
- 'Madani Mushaf · about 45 MB' pre-download size label — no size estimate is shown before download; size only appears live during download as MB/MB.
- 'Use streaming instead' button — page/image streaming is not an option on this gate. 'Streaming' exists only as a separate AUDIO preference (prefs_streaming_title 'Streaming' / prefs_streaming_summary 'Stream audio instead of downloading'). Declining here is simply 'No'.
- The 'page 374 of 604' page counter.
- A persistent gold 'Download' CTA coexisting with a progress card.
- The 'Mushaf' top app bar header on this screen.

**Build actions:**
- Delete the invented full-screen surface: remove the 'Mushaf' top bar, the 📖 glyph/hero block, the 'Download Quran pages' headline and the 'Madani Mushaf · about 45 MB' subtitle. Re-render this part as the real sequence of navy+gold Material bottom-sheets/dialogs over a dimmed backdrop, matching the app's bottom_sheet_confirm + migration_upgrade styling (and verify both light and dark themes).
- State A — Download-prompt bottom-sheet: title 'Download Required Files?' (data-ar 'حمل الملفات المطلوبه؟'), body text from downloadPrompt, gold filled primary 'Yes' (data-ar 'نعم') + ghost 'No' (data-ar 'لا'); mark it non-cancelable. Add a small note that the body has two alternate copies: tablet ('We recently added improved images for tablets…') and patch ('There is a small yet important update… download this patch now?').
- State B — Determinate progress dialog: navy card titled 'Downloading…' (downloading_title, AR existing), subtitle 'Please wait for the files to download (resume supported).' (downloading_message), a gold determinate horizontal bar, and a live status line that must read 'Downloaded 12.34 MB / 45.00 MB' (and note the variants '…of surah N' and 'Downloading surah N ayah M') — NOT 'page 374 of 604'. Add a text 'Cancel' button (data-ar 'إلغاء') that cancels the download.
- State B2 — Processing/extracting variant of the same card: title 'Processing…' (extracting_title) with status 'Processing file 312 / 604' (process_progress) and a determinate gold bar.
- Remove the 'Use streaming instead' button entirely; the only decline path is the 'No' button on State A. Do not label anything 'streaming' on this gate.
- State C — Permission-rationale bottom-sheet (caption it 'older devices that store on the SD card'): empty title, body = storage_permission_rationale full text, gold 'OK' + ghost 'Cancel'.
- State D — Migration spinner: a small navy dialog with an INDETERMINATE gold spinner (gold_accent) + 'Please wait…' (please_wait, AR existing), captioned 'moving files on Android 11+'.
- State E — Error bottom-sheet: an error message line, gold 'Retry' (download_retry / 'أعد المحاولة') + ghost 'Cancel' (download_cancel / 'إلغاء'); plus show the inline non-fatal note 'Corrupted file, attempting to re-download' (download_error_invalid_download_retry) inside the progress card.
- Add a caption noting the silent auto-skip: if page images already exist the gate shows nothing and goes straight to the reader — so this mockup represents only the first-run / data-missing path.
- Use the EXACT existing strings for every label in EN + AR (titles, Yes/No, Retry/Cancel, Downloading…/Processing…, please_wait); keep navy surfaces with gold accents and confirm contrast in both light and dark.

### 18-audio-player — Audio playback bar / now-playing

**Revive (lost features — bring back):**
- Reciter (Qari) PICKER: the mockup shows the reciter name 'Mishary Alafasy / مشاري العفاسي' only as a static label, dropping the actual dropdown spinner that lets the user choose a reciter and persists PREF_DEFAULT_QARI.
- Stop button (ic_stop): real PLAYING/PAUSED row has a dedicated Stop; the mockup has no stop control.
- Settings gear (ic_action_settings) and the entire slide-up audio-settings panel it opens: From/To sura+ayah range spinners, 'Play set of verses' range-repeat picker (1-25), 'Play each verse' per-verse-repeat picker (1-25), 'Only play the above verses' checkbox, and 'Apply' button — all absent from the mockup.
- Repeat COUNT semantics: the mockup's plain 🔁 icon drops the numeric superscript cycle (1/2/3/∞) that the real RepeatButton shows.
- DOWNLOADING / LOADING state (progress bar + status text + Cancel X) — the most common real state and the one actually captured in the as-built screenshot — is not represented.
- PROMPT_DOWNLOAD (non-Wi-Fi) state with Accept/Cancel and the 'You are not on Wi-Fi. Download data anyway?' prompt is not represented.
- Recitation/Mic feature entirely dropped: Mic, Transcript, Hide-page buttons and the three recitation modes (listening/stopped/playing), plus mic long-press.
- Compact docked-bar form factor: the mockup omits that audio playback is a thin 48dp bottom bar overlaid on the visible Quran page (not a full screen).

**Fix (misrepresented):**
- Form factor: the mockup is a full-screen now-playing surface; the real audio UI is a 48dp bottom-docked status bar over the reader page.
- Seek/scrubber: the mockup shows a draggable progress thumb with elapsed '1:02' and total '2:48' time labels. The real audio bar has NO playback-position scrubber and NO time readouts; the only horizontal progress element is the download/loading ProgressBar (not user-seekable), so the gold line in the screenshot is download progress, not a track scrubber.
- Repeat: shown as a simple loop toggle 🔁, but the real control is a tap-to-cycle repeat-count button that overlays 1/2/3/∞.
- Play/Pause: the mockup renders it as a 64px gold hero circle. Real is one gold-tinted icon among 6 equal icons in a single row (Stop, Prev, Play/Pause, Next, Repeat, Settings).
- 'Now playing / يُشغّل الآن' header label is shown but no such header text exists in the real bar.

**Remove (invented — not in the app):**
- Shuffle control (🔀) — the app has no shuffle.
- Circular album-art disc with the ۩ glyph — reciters have no artwork; the bar shows no art.
- Draggable seek thumb + elapsed/total time readouts (1:02 / 2:48) — no playback-position seeking or time display exists.
- Collapse chevron (⌄) at top-left — there is no full-screen player to collapse.
- Overflow (⋮) menu at top-right — the bar has no kebab menu; the real entry point is the settings gear opening the From/To range panel.
- The full-screen now-playing screen itself — no such screen exists in the app.

**Build actions:**
- Replace the full-screen layout with a compact bottom-docked bar mockup (~48dp tall row) sitting over a faint Quran-page background, navy_900 fill, to match the real `AudioStatusBar`. Drop the artwork disc, the 'Now playing' header, the collapse chevron, and the ⋮ overflow.
- Render the PLAYING state as a single horizontal row of 6 controls in this order: Stop, Previous, Play/Pause (gold-tinted), Next, Repeat (with a small superscript count badge like '3' or '∞'), Settings gear. Remove Shuffle.
- Add a STOPPED-state variant of the bar showing: gold Play button + a reciter dropdown control (chevron + 'مشاري العفاسي / Mishary Alafasy') to represent the Qari spinner, + (optionally) the Mic button when recitation is on. Make clear the reciter name is a PICKER, not a static caption.
- Replace the fake seek scrubber+time labels with the correct progress semantics: show a DOWNLOADING/LOADING variant = thin gold ProgressBar + text 'جاري التحميل… / Downloading…' + a Cancel (X) button (this matches the as-built screenshot). Do NOT show elapsed/total track time.
- Add a PROMPT-DOWNLOAD variant: Accept (✓) + prompt text 'You are not on Wi-Fi. Download data anyway? / أنت لست على واي فاي. تحميل البيانات على أية حال؟' + Cancel (✗).
- Add a recitation variant (mic feature): Mic (active = cyan/gold tint) + Transcript + Hide-page icons, with a note that mic supports long-press; cover listening/stopped/playing sub-states.
- Add a slide-up Audio Settings panel mockup reached from the gear: 'From/من' (sura+ayah spinners), 'To/إلى' (sura+ayah spinners), 'Play set of verses:/تشغيل مجموعة الآيات:' number picker (1-25, default 7), 'Play each verse:/تشغيل كل آية:' number picker (1-25, default 3), 'Only play the above verses/شغل ما تم اختياره من الآيات فقط' checkbox, and 'Apply/تطبيق' button — styled navy+gold, verified for EN+AR and light/dark.
- Keep only the genuinely-shared cue: gold accent on the Play/Pause icon. Mirror the whole control row for RTL (Arabic), matching the isRtl branches in AudioStatusBar.
- If this mockup part is meant to also show the surrounding chrome, include the floating Day/Sepia/Night (نهار/ورقي/ليل) reading-mode pill just above the bar; otherwise leave it to the reader-chrome part but do not contradict it.

---

## Build approach
- Edit `parts/NN-*.html` in place; add new `parts/NNx-*.html` partials for the new frames; keep the `data-en`/`data-ar` + theme-token conventions and the existing CSS kit in `parts/00-head.html`.
- Reassemble `index.html` (full gallery), re-render every frame to `compare-shots/ux/ux-*.png` (Arabic/light, via Playwright), and regenerate `compare-ux.html` so the proposed-UX column reflects the revived designs against the real as-built screenshots.
- Verify each frame renders in light **and** dark, EN **and** AR (RTL), before finalizing.
