# Aya List — Design C (Rich Calligraphy)

**Date:** 2026-06-22
**Status:** Approved (design), pending implementation
**Screen:** `AyaList` — the surah/track list shown when a reciter is pressed.

## Goal

Restyle the **List view mode** row of the aya list (the per-reciter surah list) to match
design **C ("Rich Calligraphy")** from the list-rows brainstorm
(`.superpowers/brainstorm/96972-1781886409/content/listviews.html`): a decorative gold
medallion bearing the surah number, the Arabic surah name as the visual lead, and metadata
as chips — while preserving the screen's existing actions (play / share / download / multi-select).

Grid and Compact view modes are **out of scope** and remain unchanged.

## Context (verified)

- The list is **always the canonical 114 surahs in fixed order**, built in
  `LnaguageClass.GuranAya(...)` as `AuthorClass(ServerName, RealName)` where:
  - `ServerName` = zero-padded surah number (`"001"`…`"114"`).
  - `RealName` = Arabic surah name (e.g. `"الفاتحة"`).
  - `StateName` = download/availability status string (set by the adapter; shown today in `textView2`).
- The List row layout is `res/layout/single_rowayalist.xml`, inflated via the shared
  `VivzAdapter.ViewHolder` in `AyaList.java`. The **same ViewHolder is reused for all three
  modes**, so any new view IDs must be null-guarded in `onBindViewHolder` (they won't exist in
  the grid/compact layouts).

## Design

Row sits inside the existing rounded `background_card` CardView (`cardContent`). Content, RTL:

1. **Medallion (leading):** a `FrameLayout` (~46dp) containing
   - `ImageView` src = new vector `ic_medallion_diamond` (double-outline diamond, tinted `gold_accent`), and
   - a centered `TextView` `@+id/surahNumber` showing the surah number (bold gold, from `ServerName`).
2. **Middle (`weight=1`):**
   - `@id/textView1` (`RealName`) — enlarged (~20sp) bold as the lead.
   - Chip row:
     - `@+id/chipAyah` — `"<n> آية"`, filled cream chip (`@drawable/bg_listrow_badge`, `gold_accent` text) — mirrors the surah-index badge pattern.
     - `@id/textView2` (`StateName`) — restyled as an **outlined** gold chip (`@drawable/bg_chip_gold_outline`).
3. **Trailing actions (unchanged IDs):** `@id/imageView` (play, gold), `@id/buttonShare`,
   `@id/button` (download), `@id/buttonDelete` (hidden), `@id/selectCheckBox` (multi-select).
   Also keep `@id/statusIcon` and the `@id/cardviewad` ad card.

**All existing IDs are preserved** so adapter wiring keeps working without behavioral changes.

## Theming (light + dark)

- Surface/text via adaptive tokens: `background_card`, `text_primary`, `gold_accent` (all have
  `values-night` overrides).
- Ayah chip reuses `badge_gold_bg` (fixed cream) + `gold_accent` text — proven readable in both
  modes (same as `Widget.MyStream.ListRow.Badge`).
- New `bg_chip_gold_outline.xml`: transparent fill + `gold_accent` stroke + `gold_accent` text.
- Medallion vector tinted `gold_accent`.

## New data

`AyaList` gains a static `int[] AYAH_COUNTS` (index 1..114, canonical fixed counts). The ayah
chip text is built from `AYAH_COUNTS[Integer.parseInt(ServerName)]`, wrapped in try/catch.

## Strings

- `ayah_count_chip` = `"%1$d آية"` (`values-ar/`) / `"%1$d ayahs"` (`values/`).

## Files

- `res/layout/single_rowayalist.xml` — restructure to design C (preserve IDs).
- `res/drawable/ic_medallion_diamond.xml` — new vector.
- `res/drawable/bg_chip_gold_outline.xml` — new outlined chip.
- `AyaList.java` — `AYAH_COUNTS` array; ViewHolder fields `surahNumber`, `chipAyah` + null-guarded binding.
- `res/values/strings.xml` + `res/values-ar/strings.xml` — `ayah_count_chip`.

## Verification

Build the `Madani` debug flavor, install, open a reciter, and screenshot the List mode in
**light and dark** (`adb shell cmd uimode night yes|no`) confirming: medallion number, Arabic
name prominence, both chips legible, actions functional. Confirm grid/compact unchanged.
