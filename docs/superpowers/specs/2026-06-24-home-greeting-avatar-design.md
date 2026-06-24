# Home hero — greeting + avatar redesign

**Date:** 2026-06-24
**Status:** Approved, implementing.
**Screen:** Home (`fragment_home.xml` / `ui/home/HomeFragment.java`) — the navy hero top row.

## Problem
The avatar is a plain solid-gold disc (no initials/photo/glyph → an empty gold blob), and when
the user isn't signed in the name line is blank, so the greeting looks sparse/broken.

## Design (Direction A: ringed circle + glyph)

**Avatar:**
- New `res/drawable/bg_avatar_navy_ring.xml` — oval, dark-navy fill (`#16203A`, fixed) + 2dp
  `gold_accent` ring, so it stands out on the navy header.
- New `res/drawable/ic_avatar_person.xml` — simple person glyph vector (gold-tinted default).
- Avatar FrameLayout in `fragment_home.xml`: 42→46dp, background `bg_avatar_navy_ring`; children
  = `home_avatar_glyph` (ImageView, person glyph, gold) + `home_avatar_initial` (TextView, gold) +
  `home_avatar` (CircleImageView, photo). Priority photo > initial > glyph.

**Greeting:**
- Keep `home_greeting` (salam) small/muted on top.
- `home_name` (bold) shows the user's name, or new `home_welcome` ("أهلاً بك" / "Welcome") when
  not signed in.

**HomeFragment binding:**
- Not signed in: `homeName = home_welcome`; show glyph, hide initial + photo.
- Signed in (name, no photo): set name + initial; show initial, hide glyph + photo.
- Signed in with photo: Glide into `home_avatar`; show photo, hide initial + glyph.

## Theming / i18n
Header fixed navy both themes; avatar fill/ring/glyph fixed-on-navy gold (`gold_accent` adapts but
stays gold). Mirrors EN/AR via existing layout direction. `home_welcome` added to `values/` +
`values-ar/`.

## Files
- new: `bg_avatar_navy_ring.xml`, `ic_avatar_person.xml`
- `fragment_home.xml` — avatar FrameLayout (add glyph, swap bg, resize)
- `HomeFragment.java` — photo/initial/glyph logic + welcome fallback (around lines 155-180)
- `home_welcome` string in `values/strings.xml` + `values-ar/strings.xml`

## Verification
Build, open home, confirm avatar shows the gold-ringed glyph + "أهلاً بك" when not signed in;
check EN + AR × light + dark.
