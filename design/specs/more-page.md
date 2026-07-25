# More page — implementation spec

**Feature:** `more-page` · **Mockup (frozen):** `design/mockups/more-page.html`
**Screen:** `OtherCategoryFragment`, destination `@id/nav_more` in `mobile_navigation.xml`
**Status:** spec — not implemented. All open questions resolved 2026-07-26; decisions recorded below.

## Resolved decisions

| # | Question | Decision |
|---|---|---|
| Q1 | Search behaviour | **Always visible.** Superseded by the Q2 revision — a static hero does not collapse, so the search field never scrolls away. This is what the frozen mockup draws. |
| Q2 | Static or collapsing hero | ~~Collapsing~~ → **REVISED to `hero_static`.** See the correction note below. |
| Q3 | Live Streaming's group | **Demoted to a tile** in Read & Listen. "Daily" stays honestly daily. |
| Q4 | Live Streaming when offline | **Stays enabled**, subtitle reads "Offline". Never a greyed dead row. |
| Q5 | Long-press | **Nothing in v1.** No pin, no reorder, no shortcut. |
| Q6 | State holder | **ViewModel + LiveData.** This screen introduces the pattern (see §9). |
| Q7 | Read & Listen's orphaned 5th tile | **Hadith of the Day promoted to a row** — it changes daily, so it satisfies the has-state rule. Yields a clean 4 rows / 4 tiles / 4 tiles. |
| Q8 | The AdView banner, present in code but absent from the mockup | **Kept**, docked below the list, wiring untouched. It is currently `visibility="gone"` with `loadAd` commented out, so it does not alter the rendered screen. |

### Correction — Q2 was decided on a false premise

The question as originally put claimed collapsing would be *"consistent with the other 11 screens."*
**That was wrong.** Verified against the repo:

- All eleven hero screens (`activity_{asmaul_husna,athkar,daily_hadith,dua,hisn_al_muslim,islamic_events,fasting_tracker,qibla,reading_progress,settings_new,notification_settings}.xml`)
  include **`hero_static`**.
- **`hero_collapsing.xml` is included by zero layouts.** It ships in the repo unused.
- Every one of those eleven is an **Activity** that owns its window. This screen is a **Fragment**
  inside `MainActivity`, whose layout is already a `CoordinatorLayout` containing an `AppBarLayout`
  with `android:id="@+id/appbar"` — the *same id* as `hero_collapsing.xml`'s root. Two views with
  that id in one hierarchy makes `HeroController.attach(activity)`'s `findViewById` ambiguous.

Collapsing was therefore both the *less* consistent option and a new-architecture change, not
reuse. Revised to **`hero_static`**, which is what the frozen mockup already draws.

---

## 0. Stack this spec targets

Detected from the repo, not assumed:

| | |
|---|---|
| UI toolkit | **XML views + ViewBinding.** Zero Compose in the repo (`grep androidx.compose` → no hits; `buildFeatures { viewBinding true }`, no `compose true`). |
| Material | **Material 3 via XML** — `com.google.android.material:material:1.11.0`, `AppTheme parent="Theme.Material3.DayNight"`. M3 *components and theme attrs*, not `MaterialTheme` (that is Compose-only). |
| SDK | `compileSdk 34`, `minSdkVersion 21`, `targetSdkVersion 34`, one flavour `madani` |
| Modules | `:app` (this screen) + `:common:*` (11) + `:feature:*` (3) + `:pages:madani` |
| Theme location | `app/src/main/res/values/{colors,dimens,styles,styles_mystream,themes}.xml`, dark overrides in `values-night/{colors,styles_mystream,themes}.xml`. **No `ui/theme/*.kt`** — that is the Compose layout and does not exist here. |
| Language | Java for this screen (`OtherCategoryFragment.java`), Kotlin used elsewhere |

**Consequence for this spec:** everything the brief phrased in Compose terms is translated —
`Composable tree` → *view tree*, `MaterialTheme.typography` → *`TextAppearance`/`Widget.MyStream.*`
styles*, `MaterialTheme.colorScheme.primary` → *`?attr/colorPrimary`*. The intent is kept exactly:
**named tokens, never raw dp where a token exists; theme attr roles, never literal hex.**

### Tokens available (verified present)

- **Spacing:** `spacing_xs` 4dp · `spacing_sm` 8dp · `spacing_md` 16dp · `spacing_lg` 24dp · `spacing_xl` 32dp
- **M3 roles mapped in `AppTheme`:** `colorPrimary`, `colorPrimaryContainer`, `colorSurface`,
  `colorOnSurface`, `colorSurfaceVariant`, `colorOnSurfaceVariant`, `colorOutline`, `colorOutlineVariant`
  (31 `md_theme_*` colors defined, light + `values-night`)
- **Brand:** `navy_500` `#2C3A5E`, `navy_700` `#1F2A44`, `navy_900` `#172033`, `gold_accent`,
  `gold_light`, `gold_accent_semi`, `gold_accent_faint`, `text_on_navy`, `hint_on_navy`
- **Existing widget styles:** `Widget.MyStream.ListRow{,.Title,.Meta,.Badge}`,
  `Widget.MyStream.SectionHeader{,.Title}`, `Widget.MyStream.Card{,.Feature}`,
  `Widget.MyStream.QuickAction`, `Widget.MyStream.BottomNav{,.PillDock}`, `Widget.MyStream.GlassCard`

### New dimens this screen needs

| Name | Value | Why not an existing token |
|---|---|---|
| `more_icon_chip_size` | 36dp | row leading chip; no existing token |
| `more_tile_chip_size` | 52dp | tile chip; larger than the row chip by design |
| `more_row_min_height` | 56dp | rows are denser than `Widget.MyStream.ListRow`'s 64dp minHeight (see §3) |
| `more_context_card_height` | 72dp | context card |
| `more_group_gap` | 16dp | equals `spacing_md`; **use `spacing_md` instead — do not add this one** |

---

## 1. View tree

One line per node. Indentation = nesting. `[M3]` = Material component, `[custom]` = ours,
`[reuse]` = already exists in the project.

```
RelativeLayout  @id/EntireLayoutCategory       [reuse] KEPT — LiveList replaces into this container
└─ LinearLayout (vertical)                     root content column
   ├─ include @layout/hero_static              [reuse] navy hero (Q2 revised), via HeroController
   │  ├─ ImageView   avatar                    [reuse] HeroController.avatar()
   │  ├─ TextView    title "More/المزيد"        [reuse] HeroController.title()
   │  ├─ ImageView   action trophy             [reuse] HeroController.action()
   │  ├─ ImageView   action settings           [reuse] HeroController.action()
   │  └─ SearchView  hero search               [reuse] HeroController.search(); always visible (Q1)
   ├─ RecyclerView  rv_more                    [M3] single list, GridLayoutManager(span 4)
│  │                                            SpanSizeLookup: header/context/row = 4, tile = 1
│  ├─ VIEW_TYPE_CONTEXT   item_more_context    [custom] the next-prayer card (1, position 0)
│  │  └─ MaterialCardView                      [M3] Widget.MyStream.Card.Feature
│  │     └─ LinearLayout (horizontal)
│  │        ├─ FrameLayout  ring               [custom] 46dp circle, gold stroke
│  │        │  └─ ImageView  ic_prayer_times
│  │        ├─ LinearLayout (vertical, weight 1)
│  │        │  ├─ TextView  prayer name
│  │        │  └─ TextView  city · countdown
│  │        └─ TextView     time (tabular)
│  ├─ VIEW_TYPE_HEADER    item_more_header     [custom] group header + hairline
│  │  └─ LinearLayout (horizontal)
│  │     ├─ TextView  label                    Widget.MyStream.SectionHeader.Title
│  │     └─ View      hairline rule            1dp, ?attr/colorOutlineVariant
│  ├─ VIEW_TYPE_ROW       item_more_row        [custom] stateful row
│  │  └─ LinearLayout (horizontal)             Widget.MyStream.ListRow (minHeight overridden)
│  │     ├─ FrameLayout  chip                  [custom] 36dp rounded square, gold tint
│  │     │  └─ ImageView  icon                 20dp
│  │     ├─ LinearLayout (vertical, weight 1)
│  │     │  ├─ TextView  title                 Widget.MyStream.ListRow.Title
│  │     │  └─ TextView  subtitle (GONE-able)  Widget.MyStream.ListRow.Meta
│  │     ├─ TextView    state (GONE-able)      [custom] gold, tabular
│  │     └─ ImageView   chevron (GONE-able)    autoMirrored
│  └─ VIEW_TYPE_TILE      item_more_tile       [custom] stateless tile
│     └─ LinearLayout (vertical, gravity center)
│        ├─ FrameLayout  chip                  [custom] 52dp rounded square, outlined
│        │  └─ ImageView  icon                 24dp
│        └─ TextView     label                 maxLines 2
   └─ AdView  @id/adView                       [reuse] KEPT (Q8), below the list, gone by default

BottomNavigationView                           [reuse] owned by MainActivity, not this layout
```

**One RecyclerView, four view types.** Not nested RecyclerViews — a single
`GridLayoutManager(context, 4)` with a `SpanSizeLookup` returning 4 for context/header/row and
1 for tile. This keeps one scroll container, one adapter, correct recycling, and lets search
filter across rows *and* tiles uniformly.

---

## 2. Layout, spacing, arrangement

All values are tokens. Raw dp appears only where no token exists and a new dimen is listed in §0.

| Node | Layout | Padding / spacing | Alignment |
|---|---|---|---|
| `rv_more` | GridLayoutManager span 4 | `paddingHorizontal=spacing_md`, `paddingTop=spacing_md`, `clipToPadding=false`, bottom pad = nav height + `spacing_sm` | — |
| context card | LinearLayout horizontal | `padding=spacing_md`, height `more_context_card_height` | `center_vertical` |
| ↳ ring → text | — | `marginEnd=spacing_md` (start/end aware) | — |
| ↳ text → time | — | `marginStart=spacing_sm` | time `center_vertical` |
| header | LinearLayout horizontal | `marginTop=spacing_md`, `marginBottom=spacing_sm`, `paddingHorizontal=spacing_xs` | `center_vertical` |
| rows container | MaterialCardView wrapping N rows | `cornerRadius=14dp`, `strokeWidth=1dp` | — |
| row | LinearLayout horizontal | `paddingHorizontal=spacing_md`, `paddingVertical=spacing_sm`, `minHeight=more_row_min_height` | `center_vertical` |
| ↳ chip → text | — | `marginEnd=spacing_md` | — |
| ↳ state / chevron | — | `marginStart=spacing_sm` | `center_vertical` |
| row divider | View 1dp | inset start = chip + `spacing_md` (aligns under text) | last row: GONE |
| tile | LinearLayout vertical | `paddingVertical=spacing_sm` | `center` |
| ↳ chip → label | — | `marginTop=spacing_sm` | — |
| tile grid gaps | ItemDecoration | horizontal `spacing_xs`, vertical `spacing_sm` | — |

**Start/end, never left/right.** Every margin and padding uses `Start`/`End` so RTL mirrors for free.

---

## 3. Typography and color, per text node

Color is a **theme attr role**. No literal hex anywhere in the layouts.

| Node | Style | Size / weight | Color role |
|---|---|---|---|
| hero title | existing hero style | 19sp bold | `@color/text_on_navy` |
| hero search hint | existing | 13.5sp | `@color/hint_on_navy` |
| context prayer name | `TextAppearance.Material3.TitleMedium` | 15sp bold | `@color/text_on_navy` |
| context city · countdown | `TextAppearance.Material3.BodySmall` | 11.5sp | `@color/hint_on_navy` |
| context time | `TextAppearance.Material3.TitleLarge` + `tabular-nums` | 21sp bold | `@color/gold_light` |
| group header | `Widget.MyStream.SectionHeader.Title` | 15sp bold, `textAllCaps=false` | `?attr/colorPrimary` |
| row title | `Widget.MyStream.ListRow.Title` | 17sp bold → **override to 15sp** (see note) | `?attr/colorOnSurface` |
| row subtitle | `Widget.MyStream.ListRow.Meta` | 12sp | `?attr/colorOnSurfaceVariant` |
| row state | `TextAppearance.Material3.LabelMedium` + `tabular-nums` | 11.5sp bold | `?attr/colorPrimary` |
| tile label | `TextAppearance.Material3.LabelMedium` | 11sp | `?attr/colorOnSurface` |
| row chevron | — | 15dp | `?attr/colorOnSurfaceVariant` @ 55% alpha |

> **Note on the row title:** `Widget.MyStream.ListRow.Title` is 17sp/bold and its parent
> `Widget.MyStream.ListRow` has `minHeight=64dp`. The mockup's rows are 15sp at 56dp so four rows
> plus the context card clear the fold. Extend rather than fork:
> `Widget.MyStream.ListRow.Compact` (parent `Widget.MyStream.ListRow`, `minHeight=more_row_min_height`)
> and `Widget.MyStream.ListRow.Title.Compact` (parent `…Title`, `textSize=15sp`).
> **Do not edit the existing styles** — surah and reciter lists depend on them.

**Icon chips.** Row chip = `@drawable/bg_more_chip_row` (new, corner 10dp, solid
`?attr/colorPrimary` @ 11% — express as `gold_accent_faint`, which already exists).
Tile chip = `@drawable/bg_more_chip_tile` (new, corner 16dp, `?attr/colorSurface` fill +
1dp `?attr/colorOutlineVariant` stroke). Icons tinted `?attr/colorPrimary` via `app:tint`.

---

## 4. Content model — which items are rows, which are tiles

**The rule, and it must stay visible to the user:** a tool that has live state is a **row**;
a tool that has none is a **tile**. Groups are ordered as listed.

| Group | Item | Shape | State shown |
|---|---|---|---|
| Daily / يومي | Morning & Evening Athkar | row | "Not read today" / "Read ✓" subtitle |
| | Digital Tasbih | row | current count, e.g. `33` |
| | Reading Progress | row | streak, e.g. `12 days` |
| | Hadith of the Day | row | `NEW` until opened today; subtitle = collection name |
| Read & Listen | Live Streaming · Duas · Names of Allah · Hisn al-Muslim | tile | — |
| Tools | Qibla · Zakat · Fasting Tracker · Islamic Events | tile | — |

**4 / 4 / 4.** Each group is a multiple of the 4-column span, so no group ever wraps to a
stranded single tile. Adding a 5th item to either tile group re-introduces the orphan — new
entries should be added in pairs of four, or the group split.

**Live Streaming is a tile (Q3)** even though it has an on-air state: the `LIVE` affordance is
lost by that choice, which was accepted deliberately to keep "Daily" meaning *daily*. Its offline
subtitle behaviour in §5 applies to the tile's label region, not a row.

**Prayer Times is not in the list** — it is the context card at position 0. **Settings** stays
in the hero action, not the list. That accounts for all 13 current entries plus settings.

---

## 5. States — exhaustive

| State | Treatment |
|---|---|
| **Loading** | The list is local and synchronous; there is no spinner state for it. Only the **context card** loads: show it with the prayer name and time replaced by a 2-line shimmer/placeholder for ≤300ms, never a blocking spinner. |
| **Empty** | Not reachable — the catalogue is a hardcoded array. If a future remote gate empties a group, that group's **header is hidden too** (never a header with nothing under it). |
| **Empty (search)** | Query matches nothing → replace list body with centred `TextView`, `?attr/colorOnSurfaceVariant`, "No tool matches “<query>”" + a text button "Clear". Hero and context card stay. |
| **Error** | Only the context card can fail (no location / engine throws). Card stays, shows "Set your location" + chevron, tapping opens Athan settings. The tool list is never in an error state. |
| **Buffering** | Not applicable to this screen. Live Streaming buffering belongs to the destination, not here. |
| **Offline / no network** | Tool list unaffected — every destination except Live Streaming works offline. The Live Streaming **tile** stays **enabled** (Q4) and gains a second label line reading "Offline" in `?attr/colorOnSurfaceVariant`; tapping opens the destination, which explains the real reason. Never greyed out: a dead control with no explanation is worse than a tap that reports the truth. The tile label region is already `maxLines=2`, so the extra line costs no layout change. |
| **Permission denied (location)** | Context card degrades exactly as the "Error" row above: "Set your location". Never a permission dialog from this screen — this screen does not request permissions. |
| **Long text / overflow** | Row title `maxLines=1 ellipsize=end`; row subtitle `maxLines=1 ellipsize=end`; tile label `maxLines=2 ellipsize=end`. State text never ellipsizes — it is `wrap_content` and the title shrinks first (`layout_weight` on the text column only). |
| **Large font / display size** | At `fontScale ≥ 1.3` the tile grid drops from 4 columns to 3 (`SpanSizeLookup` reads `resources.configuration.fontScale`); rows grow vertically and are allowed to. |
| **RTL / Arabic** | Whole screen mirrors via start/end. Chevron `android:autoMirrored="true"`. State text and numerals stay `tabular-nums`; Arabic-Indic digits come from the locale-formatted string, not from a manual map. `android:textDirection="locale"` on all text nodes. |

---

## 6. Interactions

| Target | Gesture | Behaviour |
|---|---|---|
| Context card | click | → `PrayerTimesActivity`. Ripple `?attr/selectableItemBackground` on the card. |
| Row | click | → destination activity. Ripple on the row, not the card container. |
| Row / tile | long-press | **Nothing in v1 (Q5).** No handler is registered at all — do not add `setOnLongClickListener` returning `false`, which would still consume the a11y long-press affordance. |
| Tile | click | → destination activity. Ripple bounded to the tile, `?attr/selectableItemBackgroundBorderless` on the chip would clip the label — use bounded on the whole tile. |
| Hero search | click | expands the `SearchView`; filters as-you-type across all groups. Headers of empty groups hide while filtering. |
| Hero actions | click | trophy → achievements, gear → settings. |
| Back (while searching) | system back | collapses search and restores the full list before leaving the screen. |
| Scroll | vertical | Single RecyclerView below a **static** hero (Q2 revised). No `CoordinatorLayout`, no scroll flags, no nested scrolling host — the hero is a fixed-height sibling in a vertical `LinearLayout`, exactly as the other 11 hero screens do it. Search is always reachable (Q1). |

**Touch targets.** Row = 56dp tall × full width ✓. Tile = 52dp chip + label inside a cell of
~88dp × 78dp ✓. Context card = 72dp ✓. Hero icons must be padded to **48dp** minimum even though
the glyph is 22dp. Every target ≥ 48dp in its smaller dimension.

**Enabled/disabled.** Nothing on this screen is ever disabled. A tool that cannot run right now
(Live Streaming offline) stays enabled and lets its own destination explain — a greyed row with
no explanation is worse than a working tap that reports the real reason.

**Transitions.** No shared-element transitions in v1. Default activity open/close. The context
card's countdown updates in place on a 1-minute tick while resumed; it must not animate the whole
list (`notifyItemChanged(0, PAYLOAD_COUNTDOWN)` with a payload, never `notifyDataSetChanged`).

---

## 7. Window size classes

The project has **no** `values-sw600dp`, no `layout-land`, and no `window-size-class` dependency —
this is currently a compact-only app. This spec does not change that, but defines behaviour so the
screen does not break:

| Class | Width | Behaviour |
|---|---|---|
| **Compact** | < 600dp | The frozen mockup. Tiles 4 across. |
| **Medium** | 600–839dp | Tiles **6 across**; rows keep full width but the whole `rv_more` is capped at `520dp` and centred (matches the existing audio-bar precedent, commit `7f7ea94`). Context card full width of that cap. |
| **Expanded** | ≥ 840dp | Same as Medium — content capped and centred. **No two-pane / list-detail** in v1; there is no detail pane to show. |

Implemented as a single `SpanSizeLookup` + a `maxWidth` on the RecyclerView, not as duplicate
layouts. No new resource-qualifier folders.

---

## 8. Dark theme, per surface

`values-night/colors.xml` already redefines the tokens; every surface below resolves through a
role, so dark comes for free **except** the two places that are hardcoded navy by design.

| Surface | Light | Dark |
|---|---|---|
| Screen background | `onb_bg_top → onb_bg_bottom` gradient (existing) | `#1F2A44 → #0B0F1C` (already in `values-night`) |
| Hero | `navy_700` — **fixed in both themes** | `navy_700`, unchanged |
| Context card | `navy_700` — **fixed in both themes** | slightly darker `navy_900` so it separates from the hero |
| Text on hero + context card | `text_on_navy` / `hint_on_navy` — **fixed** | unchanged |
| Rows container | `?attr/colorSurface` | resolves darker automatically |
| Row divider / tile stroke | `?attr/colorOutlineVariant` | resolves automatically |
| Row title / tile label | `?attr/colorOnSurface` | resolves automatically |
| Row subtitle | `?attr/colorOnSurfaceVariant` | resolves automatically |
| Chip fill + icons + state | `gold_accent` `#B8860B` | `gold_accent` `#D4A44C` (already overridden in `values-night`) |

> **The standing trap:** `@color/white` is black at night in this project. On the hero and the
> context card — the two permanently-navy surfaces — use `text_on_navy`, never `white` and never
> `?attr/colorOnSurface`.

---

## 9. State holder

**Decision (Q6): ViewModel + LiveData.** Worth stating plainly — the project has no ViewModel
pattern in real use today. The only `ViewModel` in the repo is `ui/home/HomeViewModel.java`, the
untouched Android Studio template ("This is home fragment"); real screens use plain Fragments with
static settings classes, or MVP presenters under `presenter/`. **This screen is therefore the
precedent**, and reviewers should treat the state class below as a pattern other screens may copy.

`androidx.lifecycle` is already on the classpath (that template compiles), so no new dependency.

The contract: 

```java
// state
final class MoreUiState {
    final ContextCard  contextCard;   // nullable = still loading
    final List<Group>  groups;        // header + items, already filtered
    final String       query;         // "" when not searching
    final boolean      searchActive;
}

final class ContextCard {
    final String  prayerName;         // localized
    final String  timeText;           // locale-formatted, e.g. "4:12" / "٤:١٢"
    final String  cityText;           // "" when unknown
    final String  countdownText;      // "in 41 minutes" / "" when unknown
    final boolean needsLocation;      // true → render the "Set your location" variant
}

final class Group {
    final int          titleRes;
    final List<Entry>  entries;
}

final class Entry {
    final String   id;                // stable, for DiffUtil + analytics
    final int      titleRes;
    final int      iconRes;
    final Shape    shape;             // ROW | TILE
    final String   subtitle;          // nullable
    final String   state;             // nullable, e.g. "33", "12 days", "LIVE"
    final Class<? extends Activity> destination;
}

enum Shape { ROW, TILE }

// events (fragment → holder)
void onScreenResumed();               // recompute state + start the countdown tick
void onScreenPaused();                // stop the tick
void onQueryChanged(String query);
void onSearchDismissed();
void onEntryClicked(String entryId);
void onContextCardClicked();
void onCountdownTick();               // 1/min while resumed
```

Rendering is `LiveData<MoreUiState>` + `DiffUtil` on `Entry.id`. The countdown must arrive as a
payload update to position 0 only.

---

## 10. Reuse vs. new

### Reuse as-is — no changes

| Thing | Where | Used for |
|---|---|---|
| `hero_static.xml` + `HeroController` | `res/layout/`, `HeroController.java` | The whole hero (Q2 revised). `.avatar() .title() .action() .action() .search()` covers every hero node. **This is the variant all 11 existing hero screens use.** |
| `SearchView` in `OtherCategoryFragment` | already constructed, never shown | The hero search. It exists — this just surfaces it. |
| `BottomNavigationView` | `MainActivity` | Unchanged; not owned by this screen. |
| `Widget.MyStream.SectionHeader.Title` | `styles_mystream.xml` | Group headers, verbatim. |
| `Widget.MyStream.ListRow.Meta` | `styles_mystream.xml` | Row subtitle, verbatim. |
| `Widget.MyStream.Card` / `.Feature` | `styles_mystream.xml` | Context card and rows container. |
| `spacing_xs…xl` | `dimens.xml` | Every gap in §2. |
| `gold_accent_faint` | `colors.xml` | Row chip fill — no new colour needed. |
| `OtherCategory` model + `OtherCategoryListLanguageClass` | `…/OtherCategory*.java` | The catalogue itself; extended with group + shape, not replaced. |
| `ic_live_tv`, `ic_prayer_times`, `ic_qibla`, `ic_athkar`, `ic_tasbih`, `ic_reading_progress`, `ic_dua`, `ic_asmaul_husna`, `ic_zakat`, `ic_fasting`, `ic_hadith`, `ic_islamic_events`, `ic_hisn` | `res/drawable/` | All 13 icons already exist. **No new icon work.** |

### Extend — new style, existing parent

| New | Parent | Justification |
|---|---|---|
| `Widget.MyStream.ListRow.Compact` | `Widget.MyStream.ListRow` | Existing minHeight is 64dp; this screen needs 56dp to clear the fold. Editing the parent would shift surah/reciter lists that ship today. |
| `Widget.MyStream.ListRow.Title.Compact` | `…ListRow.Title` | Same reason, 17sp → 15sp. |

### Genuinely new — each justified

| New | Why it cannot be reused |
|---|---|
| `item_more_context.xml` | No existing layout pairs a circular icon ring with a right-aligned tabular time on a fixed-navy card. `Widget.MyStream.Card.Feature` styles the container but there is no matching content layout. |
| `item_more_header.xml` | `Widget.MyStream.SectionHeader` is a *style*, not a layout, and this header adds a trailing hairline rule that the style does not describe. |
| `item_more_row.xml` | No existing row layout has the four-slot shape *chip + text column + state + chevron*. The closest, `other_ticket.xml`, is a vertical tile. |
| `item_more_tile.xml` | `other_ticket.xml` is the current tile but is 132dp tall with a 52dp icon and no chip; retrofitting it would change every current caller. New layout, old icons. |
| `bg_more_chip_row.xml`, `bg_more_chip_tile.xml` | Two shape drawables. No equivalent rounded-square chip background exists. |
| `MoreAdapter` (4 view types + `SpanSizeLookup`) | `CategoryAdapter` (inner class, `OtherCategoryFragment:208`) is single-view-type and grid-uniform. The row/tile split is the whole design; this is the real work. |
| `MoreItemDecoration` | Grid gaps differ for full-span vs. quarter-span items; no existing decoration handles a mixed-span grid. |

**Not needed:** any new icon, colour, or font. Everything visual resolves to tokens that ship today.

---

## 11. Out of scope for this spec

- Moving the athkar bubble's rest position off the top-trailing tile (own change, `AthkarBubbleService`)
- Replacing the emoji bottom-nav glyphs (own change, `menu_bottom_navigation.xml`)
- Pinned/reorderable shortcuts (cut from direction C)
- Tablet two-pane
