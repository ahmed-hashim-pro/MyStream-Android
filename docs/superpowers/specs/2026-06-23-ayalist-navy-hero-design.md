# AyaList — Navy Hero page redesign

**Date:** 2026-06-23
**Status:** Approved (design), pending implementation
**Screen:** `AyaList` — surahs-per-reciter list.

## Problem

The flat navy header (added 2026-06-23) clashes with the cream body — a hard dark strip on
white. Chosen direction: **B · Navy hero** — make navy an intentional rounded hero block that
carries reciter identity + the download action, so the cards flow out of it naturally.

## Design

Replace the amber/flat-navy toolbar with a **navy hero** (top of the `AppBarLayout`):

- **Background:** new `bg_navy_hero` — subtle navy gradient (`navy_700`→`navy_500`), rounded
  **bottom** corners (~22dp). Flows from the navy status bar (already set). `AppBarLayout`
  background → transparent so the cream shows beneath the rounded corners.
- **Row 1 (controls):** `backBTN` (start) · `toggleViewBTN` + `search` (end) — all `gold_accent`.
  Search stays iconified + styled for navy (`styleSearchViewForNavy`, already added).
- **Row 2 (identity + action):** gold-ring **avatar** (`heroAvatar`, app glyph via CircleImageView)
  · stacked **reciter name** (`ActivityReciter`, white bold) + **subtitle** (`heroSubtitle`,
  e.g. "١١٤ سورة") · the **download pill** at the end.

**Download pill = the existing `downloadAllBTN` CardView relocated into Row 2**, restyled compact
(wrap_content, ~20dp corners, `bg_download_all_button` gold gradient, `text_on_gold`). Its children
keep their IDs — `downloadAllText`, `selectAllBTN`, `downloadAllArrow` — so all existing logic
(`enterSelectionMode`, `updateDownloadCard`, select-all, `startDownloadSelected`) works unchanged.

**Selection mode:** in `updateDownloadCard()`, hide the identity (`heroAvatar` + the name/subtitle
container `heroIdentity`) while `selectionMode` is true, so the pill + select-all toggle get the
full row width (contextual selection bar). Restore on exit. This is the only new Java behavior.

**Download progress:** the existing `LayoutLoading` progress card stays below the hero on the cream
(appears only while downloading). The old standalone gold download button is removed from there
(its job moved into the hero pill).

**Body:** design-C rows unchanged.

## Theming / i18n

`navy_700`/`navy_500` fixed (both themes); `gold_accent` adapts; white identity text via fixed
`text_on_navy`-family. Mirrors via `layoutDirection`. New string `surah_count` ("%1$d سورة" /
"%1$d surahs", locale-formatted digits) in `values/` + `values-ar/`.

## Files

- `res/drawable/bg_navy_hero.xml` — new.
- `res/layout/activity_aya_list.xml` — restructure AppBarLayout into the navy hero; move
  `downloadAllBTN` into Row 2; remove it from `downloadLayout`; AppBarLayout bg transparent.
- `res/values/strings.xml` + `res/values-ar/strings.xml` — `surah_count`.
- `AyaList.java` — `heroAvatar`/`heroSubtitle`/`heroIdentity` refs; set subtitle on load; toggle
  identity visibility in `updateDownloadCard()`.

## Verification

Build Madani debug, install, open a reciter; confirm hero look + that the cream flows under the
rounded corners, the download pill works (normal → selection → select-all → download → progress),
in **light + dark** and **EN + AR**.
