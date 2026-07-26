#!/usr/bin/env python3
"""uilint -- scripted pixel + bounds analysis of adb screencaps.

Turns the "decode the PNG column and print colour transitions" trick that found
the 8px cream seam into a repeatable gate over 4 variants (light/dark x en/ar).

Python stdlib only. PIL/numpy are present in this user's conda env but are NOT
required -- a full 1080x2400 decode costs ~0.9s, well inside budget.

    capture   grab screencap + uiautomator dump for one variant
    accept    promote the last capture to the stored baseline
    check     run every detector; exit 1 on any ERROR

Operator loop (one variant at a time -- theme/locale switches recreate the
activity, and MainActivity is not exported, so navigation stays manual):

    python3 scripts/uilint/uilint.py capture --screen more --theme light \
        --locale en --set-variant     # flips device, then re-navigate by hand
    python3 scripts/uilint/uilint.py capture --screen more --theme light --locale en
    python3 scripts/uilint/uilint.py check   --screen more    # all 4 variants

`check` with no --theme/--locale lints every variant that has a capture.
Once a variant looks right, `accept` freezes it as the regression baseline.

Baselines live in  scripts/uilint/baselines/<screen>/<screen>__<theme>-<locale>.png
with a sibling .xml (bounds) and .json sidecar recording device w/h, density,
git commit, and the mask rects for volatile content (clock, prayer countdown).
Captures under test go to scripts/uilint/.work/ (gitignored).
"""

import argparse
import json
import os
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import detectors as D
from pngio import load_rgb, save_rgb

HERE = os.path.dirname(os.path.abspath(__file__))
BASE = os.path.join(HERE, "baselines")
WORK = os.path.join(HERE, ".work")
PKG = "com.medoapps.www.onlinequran"

CFG = {
    # --- seam / band ---
    "seam_columns": [0.05, 0.25, 0.50, 0.75, 0.95],
    "seam_quorum": 0.8,      # band must show in >=80% of sampled columns
    "seam_tol": 6,           # per-channel tolerance when run-length encoding
    "seam_aa_px": 3,         # runs this thin are antialiasing, not bands
    "seam_min_px": 1,
    "seam_max_px": 12,       # the shipped defect was 8px
    "seam_slab_px": 40,      # neighbours must be real regions, not stripes
    "divider_repeat": 3,     # same-colour band N+ times = list divider, not seam
    # --- contrast ---
    "text_min_lum_delta": 0.03,
    "text_min_coverage": 0.02,
    "text_core_pct": 0.20,   # 20th pct from the far end = text core
    "large_text_dp": 24.0,
    # --- baseline diff ---
    "diff_channel_tol": 8,   # font rasterisation drifts across API levels
    "diff_pct_budget": 0.2,
    "cluster_grid_px": 16,
    "cluster_min_cell": 8,
    "cluster_min_px": 400,
    "cluster_max_report": 12,
    # --- bounds ---
    "min_touch_dp": 48,
    "bottom_nav_ids": ["bottom_nav", "bottomnav", "nav_view", "navigation_bar",
                       "bottom_navigation"],
    "occlusion_min_px": 6,
    "collide_min_px": 4,
    "ellipsis_exempt_classes": ["EditText", "AutoCompleteTextView",
                                "SearchView"],
}

BOUNDS_RE = re.compile(r"\[(-?\d+),(-?\d+)\]\[(-?\d+),(-?\d+)\]")


def sh(cmd, **kw):
    return subprocess.run(cmd, shell=True, capture_output=True, **kw)


def variant(theme, locale):
    return "%s-%s" % (theme, locale)


def paths(screen, theme, locale, root):
    d = os.path.join(root, screen)
    stem = "%s__%s" % (screen, variant(theme, locale))
    return (os.path.join(d, stem + ".png"),
            os.path.join(d, stem + ".xml"),
            os.path.join(d, stem + ".json"))


# ---------------------------------------------------------------- capture ---

def device_meta():
    sz = sh("adb shell wm size").stdout.decode().strip()
    dn = sh("adb shell wm density").stdout.decode().strip()
    w, h = re.search(r"(\d+)x(\d+)", sz).groups()
    dens = int(re.search(r"(\d+)\s*$", dn).group(1))
    return int(w), int(h), dens


def cmd_capture(a):
    out = BASE if a.accept else WORK
    png, xml, meta = paths(a.screen, a.theme, a.locale, out)
    os.makedirs(os.path.dirname(png), exist_ok=True)

    if a.set_variant:
        loc = "ar" if a.locale == "ar" else "en-US"
        sh("adb shell cmd locale set-app-locales %s --locales %s" % (PKG, loc))
        sh("adb shell cmd uimode night %s"
           % ("yes" if a.theme == "dark" else "no"))
        print("variant switches sent -- the activity was recreated; "
              "re-navigate to the screen, then re-run without --set-variant")
        time.sleep(2)
        return 0

    w, h, dens = device_meta()
    with open(png, "wb") as fh:
        fh.write(subprocess.run("adb exec-out screencap -p", shell=True,
                                capture_output=True).stdout)
    sh("adb shell uiautomator dump /sdcard/uilint.xml")
    with open(xml, "wb") as fh:
        fh.write(sh("adb exec-out cat /sdcard/uilint.xml").stdout)

    old = {}
    if os.path.exists(meta):
        old = json.load(open(meta))
    json.dump({
        "screen": a.screen, "theme": a.theme, "locale": a.locale,
        "width": w, "height": h, "density": dens,
        "status_bar_bottom": status_bar_bottom(xml),
        "commit": sh("git rev-parse --short HEAD").stdout.decode().strip(),
        "captured": time.strftime("%Y-%m-%dT%H:%M:%S"),
        # volatile regions (clock, prayer countdown) -- else every diff is noise
        "masks": old.get("masks", [[0, 0, w, 63]]),
    }, open(meta, "w"), indent=2)
    print("captured %s" % png)
    return 0


def cmd_accept(a):
    src = paths(a.screen, a.theme, a.locale, WORK)
    dst = paths(a.screen, a.theme, a.locale, BASE)
    os.makedirs(os.path.dirname(dst[0]), exist_ok=True)
    for s, d in zip(src, dst):
        if os.path.exists(s):
            open(d, "wb").write(open(s, "rb").read())
    print("baseline updated: %s" % dst[0])
    return 0


# ------------------------------------------------------------------ nodes ---

def parse_nodes(path):
    if not os.path.exists(path):
        return []
    try:
        root = ET.parse(path).getroot()
    except ET.ParseError:
        return []
    out = []
    for n in root.iter("node"):
        m = BOUNDS_RE.match(n.get("bounds", ""))
        if not m:
            continue
        out.append({
            "bounds": tuple(int(v) for v in m.groups()),
            "text": n.get("text", ""),
            "desc": n.get("content-desc", ""),
            "rid": (n.get("resource-id", "") or "").split("/")[-1],
            "cls": (n.get("class", "") or "").split(".")[-1],
            "clickable": n.get("clickable") == "true",
            "long_clickable": n.get("long-clickable") == "true",
        })
    return out


def status_bar_bottom(xml):
    """First content row under the status bar, from the hierarchy itself."""
    for n in parse_nodes(xml):
        y0 = n["bounds"][1]
        if y0 > 0:
            return y0
    return 0


# ------------------------------------------------------------------ check ---

def cmd_check(a):
    variants = ([(a.theme, a.locale)] if a.theme and a.locale
                else [(t, l) for t in ("light", "dark") for l in ("en", "ar")])
    fail = 0
    for theme, locale in variants:
        src = WORK if not a.from_baseline else BASE
        png, xml, meta = paths(a.screen, theme, locale, src)
        if not os.path.exists(png):
            print("-- %s/%s: no capture, skipped" % (a.screen,
                                                     variant(theme, locale)))
            continue
        fail |= run_one(a, png, xml, meta, theme, locale)
    return 1 if fail else 0


def run_one(a, png, xml, meta, theme, locale):
    info = json.load(open(meta)) if os.path.exists(meta) else {}
    scale = info.get("density", 420) / 160.0
    w, h, px = load_rgb(png)
    nodes = parse_nodes(xml)
    findings = []

    findings += D.find_seams(px, w, h, CFG)
    findings += D.check_status_bar_seam(px, w, h, CFG,
                                        info.get("status_bar_bottom", 0))
    if nodes:
        findings += D.check_contrast(px, w, nodes, CFG, scale)
        findings += D.check_touch_targets(nodes, CFG, scale)
        findings += D.check_occlusion(nodes, CFG, scale)
        findings += D.check_text_fit(nodes, CFG, scale)
    else:
        print("   (no uiautomator dump -- contrast/touch/occlusion/fit skipped)")

    bpng, _bx, bmeta = paths(a.screen, theme, locale, BASE)
    if os.path.exists(bpng) and os.path.abspath(bpng) != os.path.abspath(png):
        binfo = json.load(open(bmeta)) if os.path.exists(bmeta) else {}
        bw, bh, bpx = load_rgb(bpng)
        df, overlay = D.diff_baseline((w, h, px), (bw, bh, bpx), CFG,
                                      binfo.get("masks", []))
        findings += df
        if overlay and df:
            op = png.replace(".png", "__diff.png")
            save_rgb(op, w, h, overlay)
            print("   diff overlay: %s" % op)

    tag = "%s/%s" % (a.screen, variant(theme, locale))
    errs = [f for f in findings if f["severity"] == D.CRIT]
    if not findings:
        print("PASS %s  (%dx%d, %d nodes)" % (tag, w, h, len(nodes)))
        return 0
    print("%s %s  (%d finding%s)" % ("FAIL" if errs else "WARN", tag,
                                     len(findings),
                                     "" if len(findings) == 1 else "s"))
    for f in findings:
        r = (" @ [%d,%d][%d,%d]" % f["rect"]) if f["rect"] else ""
        print("  [%s] %-16s %s%s" % (f["severity"], f["rule"], f["msg"], r))
    return 1 if errs else 0


def main():
    p = argparse.ArgumentParser(prog="uilint")
    sub = p.add_subparsers(dest="cmd", required=True)

    c = sub.add_parser("capture")
    c.add_argument("--screen", required=True)
    c.add_argument("--theme", choices=["light", "dark"], required=True)
    c.add_argument("--locale", choices=["en", "ar"], required=True)
    c.add_argument("--set-variant", action="store_true",
                   help="flip device theme/locale, then exit (activity is recreated)")
    c.add_argument("--accept", action="store_true",
                   help="write straight to baselines/ instead of .work/")
    c.set_defaults(fn=cmd_capture)

    ac = sub.add_parser("accept")
    ac.add_argument("--screen", required=True)
    ac.add_argument("--theme", choices=["light", "dark"], required=True)
    ac.add_argument("--locale", choices=["en", "ar"], required=True)
    ac.set_defaults(fn=cmd_accept)

    k = sub.add_parser("check")
    k.add_argument("--screen", required=True)
    k.add_argument("--theme", choices=["light", "dark"])
    k.add_argument("--locale", choices=["en", "ar"])
    k.add_argument("--from-baseline", action="store_true",
                   help="lint the stored baselines themselves (no diff)")
    k.set_defaults(fn=cmd_check)

    a = p.parse_args()
    sys.exit(a.fn(a))


if __name__ == "__main__":
    main()
