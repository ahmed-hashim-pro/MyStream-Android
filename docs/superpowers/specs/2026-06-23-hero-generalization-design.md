# Generalized Navy Hero — app-wide header component

**Date:** 2026-06-23
**Status:** Approved (design), pending review → implementation plan
**Goal:** Extract the AyaList navy hero into a reusable component and apply it across the app so
every page shares one consistent, themed, localized header.

## Background

The navy hero (rounded-bottom navy block, gold controls, avatar + title + subtitle, optional
action pill, search, collapse-on-scroll) currently lives only in `activity_aya_list.xml` +
`AyaList.java`. The app otherwise has three inconsistent header styles (flat `TabHeader`, simple
`Toolbar`, and **8 detail screens with no header at all**). This spec generalizes the hero into a
shared component and maps it onto every affected page.

## The three variants (one hero, optional slots)

All variants share the navy/gold language and the same sub-structure (a controls row + an identity
row). A page enables only the slots it needs: `back`, `avatar`, `title`, `subtitle`, `search`,
`actions[]` / `pill`, `collapsing`.

- **A · Collapsing list** — back + avatar + title + subtitle + search; collapses on scroll. For
  detail screens that have a scrolling list.
- **B · Static centered** — back + title (+ optional subtitle); no avatar/search, no collapse. For
  detail screens whose content is centered/non-list.
- **C · Tab** — avatar + title + search + action icons; **no back**, fixed (no collapse). For the
  top-level bottom-nav tabs.

## Components

### 1. `res/layout/hero_collapsing.xml`
The `AppBarLayout > CollapsingToolbarLayout` scaffold extracted from AyaList:
- `bg_navy_hero` background + `contentScrim`, `titleEnabled=false`,
  `scrollFlags="scroll|enterAlways|exitUntilCollapsed|snap"`, `layoutDirection="locale"`.
- Expanded identity row (`collapseMode=parallax`): `heroAvatar` (CircleImageView, gold ring) +
  `heroIdentity` (`heroTitle` + `heroSubtitle`) + an optional action slot (`heroActionPill` /
  `heroActionIcons`).
- Pinned `heroToolbar` (`collapseMode=pin`): `heroBack` + `heroActions` container +
  `heroCollapsedTitle` (fades in) + `heroSearch` (SearchView).
- Host screen supplies the scrolling content with
  `app:layout_behavior="@string/appbar_scrolling_view_behavior"`.

### 2. `res/layout/hero_static.xml`
The same navy hero block WITHOUT the AppBarLayout/collapse machinery — a single rounded navy
`LinearLayout` (controls row + identity row) for non-list screens. Same slot ids as above
(minus collapse-only views), so `HeroController` configures it the same way. Host places it at the
top of a vertical layout with content below.

### 3. `HeroController` (Java helper, new class)
One fluent call wires any host, centralizing the logic that screens must NOT re-implement:

```java
HeroController.of(activity)               // finds hero views by id within the activity
    .back()                               // shows back chevron -> finish() (or custom Runnable)
    .title(R.string.x)                    // or .title(String)
    .subtitle(textOrNull)                 // hides the slot when null
    .avatar(R.drawable.x)                 // or .noAvatar()
    .search(onQueryTextChange)            // wires + styles SearchView for navy; null = no search
    .action(R.drawable.icon, onClick)     // 0..N trailing gold action icons
    .pill(R.string.label, R.drawable.icon, onClick)   // optional gold action pill
    .apply();
```

Responsibilities folded into the controller (today duplicated/ad-hoc in AyaList):
- Gold tinting of back/actions; `text_on_navy` title, `hint_on_navy` subtitle.
- **Search-on-navy styling** (the `styleSearchViewForNavy()` logic) + hide collapsed title while
  search is open.
- **Collapse fade**: if the host has an `AppBarLayout`, add the `OnOffsetChangedListener` that
  fades the identity out and `heroCollapsedTitle` in.
- **Navy status bar** with light icons (`WindowInsetsControllerCompat`).

Drawables/tokens reused: `bg_navy_hero`, `gold_accent`, `text_on_navy`, `hint_on_navy`,
`text_on_gold`, `bg_download_all_button` (pill).

## Page map (slot matrix)

| Page | Variant | back | avatar | subtitle | search | actions | collapse |
|---|---|---|---|---|---|---|---|
| AyaList (reference, migrate first) | A | ✓ | ✓ | count | ✓ | — | ✓ + download pill |
| Athkar, Dua, Hisn, Asmaul-Husna | A | ✓ | ✓ | ✓ | ✓ | — | ✓ |
| Islamic Events | A | ✓ | ✓ | ✓ | — | — | ✓ |
| Qibla, Fasting, Reading-Progress, Daily-Hadith | B | ✓ | — | optional | — | — | — |
| Settings, Notification-Settings | B | ✓ | — | — | — | — | — |
| Reciters | C | — | ✓ | — | ✓ | toggle, settings, rewards | — |
| Radio | C | — | ✓ | — | ✓ | settings, rewards (+ rewayah spinner) | — |
| Home | custom (tokens only) | — | ✓ | — | — | — | ✓ |

## Theming / i18n / behavior

- `navy_700`/`navy_500` fixed (identical light+dark); `gold_accent` adapts. Title `text_on_navy`,
  subtitle `hint_on_navy` (both fixed-on-navy).
- `layoutDirection="locale"` everywhere → mirrors EN (LTR) / AR (RTL).
- Navy status bar + light icons on every hero screen.
- Collapse flags `scroll|enterAlways|exitUntilCollapsed|snap` (expands on any upward scroll).
- **Strings:** the 8 header-less screens need `*_title` (and where used `*_subtitle`) in
  `values/strings.xml` **and** `values-ar/strings.xml`. Reuse existing title strings where present;
  add only what's missing. Keep religious content Arabic.

## Phasing (each phase ships + verifies independently)

1. **Component + reference parity:** build `hero_collapsing.xml`, `hero_static.xml`,
   `HeroController`; migrate **AyaList** onto them and confirm pixel/behaviour parity (incl. the
   download pill + selection mode + collapse).
2. **List detail screens:** Athkar, Dua, Hisn al-Muslim, Asmaul Husna, Islamic Events.
3. **Centered detail screens:** Qibla, Fasting Tracker, Reading Progress, Daily Hadith, Settings,
   Notification Settings.
4. **Tabs:** Reciters, Radio onto variant C; Home adopts the shared tokens/drawable only.

Each migrated screen verified **EN + AR × light + dark** on device, and existing behavior
(search filter, actions, lists, back) confirmed intact.

## Non-goals

- No change to each screen's content/list rows (only the header).
- Home's bespoke hero (prayer ring, greeting, dots) is not replaced — it only aligns tokens.
- No new navigation or features; this is a header consolidation.

## Risks

- Screens currently with NO header gain one → verify their content isn't clipped under the hero
  and that back navigation works.
- `HeroController` must support both Activities and the tab **Fragments** (Reciters/Radio) — the
  `of()` factory takes a root `View` + optional `Activity` (for status bar) to cover both.
- Collapsing requires a genuine scrolling child; variant-B screens must NOT use the collapsing host.
