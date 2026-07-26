#!/usr/bin/env python3
"""
Screen audit — find visual defects by measurement, not by looking.

Every defect this exists to catch was real, shipped, and was caught by a human eye
after several rounds of "looks right to me":

  seam      an 8px band of #FFF8F0 between the navy status bar and the header
  target    hero action buttons at 40dp against a 48dp minimum
  overlap   tile labels colliding in English (Arabic labels were shorter, so it
            was invisible in the only locale anyone tested)
  contrast  a group header rendered washed-out against cream

All four are measurable. None of them needed an opinion.

Usage
  python3 scripts/screen_audit.py capture <name>       grab screen + view tree
  python3 scripts/screen_audit.py audit   <name>       run every check
  python3 scripts/screen_audit.py diff    <a> <b>      pixel-diff two captures

Captures land in design/baselines/<name>.{png,xml}. Commit them; a diff against a
committed baseline is what turns "did this shift?" into a yes/no.
"""

import os
import subprocess
import sys
import xml.etree.ElementTree as ET

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "design", "baselines")
PKG = "com.medoapps.www.onlinequran"

# 48dp is the Material/a11y minimum touch target.
MIN_TOUCH_DP = 48
# WCAG AA for normal-size text.
MIN_CONTRAST = 4.5


def sh(cmd):
    return subprocess.run(cmd, shell=True, capture_output=True, text=True).stdout


def density():
    out = sh("adb shell wm density").strip()
    for tok in out.replace(":", " ").split():
        if tok.isdigit() and int(tok) >= 120:
            return int(tok) / 160.0
    return 1.0


# ----------------------------------------------------------------- capture

def capture(name):
    os.makedirs(BASE, exist_ok=True)
    png = os.path.join(BASE, name + ".png")
    xml = os.path.join(BASE, name + ".xml")
    with open(png, "wb") as f:
        f.write(subprocess.run("adb exec-out screencap -p", shell=True, capture_output=True).stdout)
    sh("adb shell uiautomator dump /sdcard/_audit.xml")
    with open(xml, "w", encoding="utf-8") as f:
        f.write(sh("adb shell cat /sdcard/_audit.xml"))
    print("captured %s (%d bytes) + view tree" % (png, os.path.getsize(png)))


def load(name):
    from PIL import Image
    png = os.path.join(BASE, name + ".png")
    xml = os.path.join(BASE, name + ".xml")
    img = Image.open(png).convert("RGB")
    tree = ET.parse(xml).getroot() if os.path.exists(xml) else None
    return img, tree


def bounds_of(node):
    b = node.get("bounds", "")
    try:
        a, c = b.split("][")
        x1, y1 = (int(v) for v in a.strip("[").split(","))
        x2, y2 = (int(v) for v in c.strip("]").split(","))
        return x1, y1, x2, y2
    except ValueError:
        return None


# -------------------------------------------------------------- the checks

def check_seams(img, findings):
    """A thin band of a colour that appears nowhere else around it is a seam.

    Scans the centre column for runs of constant colour and flags any run shorter
    than 24px that sits between two runs of a *different* colour — which is exactly
    the shape of the 8px cream line that survived three rounds of eyeballing.
    """
    w, h = img.size
    x = w // 2
    runs = []
    prev, start = None, 0
    for y in range(h):
        px = img.getpixel((x, y))
        if px != prev:
            if prev is not None:
                runs.append((start, y - 1, prev))
            prev, start = px, y
    runs.append((start, h - 1, prev))

    # A divider and a seam look identical locally — both are a thin band between two
    # thicker runs. What separates them: a SEAM is a thin sliver of a colour that is a
    # *surface* elsewhere on the screen (the cream page background leaking through),
    # whereas a divider colour only ever appears as a thin line. So: flag a thin band
    # only if that same colour also appears as a thick run somewhere. Without this the
    # check drowns in every row divider and reports nothing usable.
    thick = {col for (a, b, col) in runs if (b - a + 1) > 40}

    for i in range(1, len(runs) - 1):
        y0, y1, col = runs[i]
        height = y1 - y0 + 1
        before, after = runs[i - 1][2], runs[i + 1][2]
        if not (2 <= height <= 24 and before != col and after != col):
            continue
        if col not in thick:
            continue  # a genuine divider/stroke, not background leaking through
        if (runs[i - 1][1] - runs[i - 1][0]) > 24 and (runs[i + 1][1] - runs[i + 1][0]) > 8:
            findings.append(
                "SEAM   y=%d..%d (%dpx) #%02X%02X%02X between #%02X%02X%02X and #%02X%02X%02X"
                % (y0, y1, height, col[0], col[1], col[2],
                   before[0], before[1], before[2], after[0], after[1], after[2]))


def check_touch_targets(tree, dens, findings):
    if tree is None:
        return
    need = int(MIN_TOUCH_DP * dens)
    for node in tree.iter("node"):
        if node.get("clickable") != "true":
            continue
        b = bounds_of(node)
        if not b:
            continue
        wpx, hpx = b[2] - b[0], b[3] - b[1]
        if wpx < need or hpx < need:
            label = node.get("content-desc") or node.get("text") or node.get("resource-id") or "?"
            findings.append("TARGET %-34s %dx%dpx = %.0fx%.0fdp (min %ddp)"
                            % (label[:34], wpx, hpx, wpx / dens, hpx / dens, MIN_TOUCH_DP))


def check_overlaps(tree, findings):
    """Two leaf views with text whose boxes intersect are colliding."""
    if tree is None:
        return
    boxes = []
    for node in tree.iter("node"):
        txt = node.get("text") or ""
        if not txt.strip() or len(list(node)) > 0:
            continue
        b = bounds_of(node)
        if b:
            boxes.append((txt, b))
    for i in range(len(boxes)):
        for j in range(i + 1, len(boxes)):
            (t1, a), (t2, c) = boxes[i], boxes[j]
            if a[0] < c[2] and c[0] < a[2] and a[1] < c[3] and c[1] < a[3]:
                findings.append("OVERLAP %r intersects %r" % (t1[:24], t2[:24]))


def _lum(c):
    def ch(v):
        v /= 255.0
        return v / 12.92 if v <= 0.03928 else ((v + 0.055) / 1.055) ** 2.4
    return 0.2126 * ch(c[0]) + 0.7152 * ch(c[1]) + 0.0722 * ch(c[2])


def check_contrast(img, tree, findings):
    """Sample each text node's box: darkest vs lightest pixel is a fair proxy for
    glyph-vs-background, and catches the washed-out-header class of defect."""
    if tree is None:
        return
    for node in tree.iter("node"):
        txt = (node.get("text") or "").strip()
        if not txt or len(list(node)) > 0:
            continue
        b = bounds_of(node)
        if not b or b[2] - b[0] < 8 or b[3] - b[1] < 8:
            continue
        px = [img.getpixel((x, y))
              for y in range(b[1], min(b[3], img.size[1]), 2)
              for x in range(b[0], min(b[2], img.size[0]), 2)]
        if not px:
            continue
        lo = min(px, key=_lum)
        hi = max(px, key=_lum)
        ratio = (_lum(hi) + 0.05) / (_lum(lo) + 0.05)
        if ratio < MIN_CONTRAST:
            findings.append("CONTRAST %-30s %.1f:1 (min %.1f) #%02X%02X%02X on #%02X%02X%02X"
                            % (txt[:30], ratio, MIN_CONTRAST,
                               lo[0], lo[1], lo[2], hi[0], hi[1], hi[2]))


# ------------------------------------------------------------------ driver

def audit(name):
    img, tree = load(name)
    dens = density()
    findings = []
    check_seams(img, findings)
    check_touch_targets(tree, dens, findings)
    check_overlaps(tree, findings)
    check_contrast(img, tree, findings)

    print("audit %s  (%dx%d, density %.2f)" % (name, img.size[0], img.size[1], dens))
    if not findings:
        print("  clean")
        return 0
    for f in findings:
        print("  " + f)
    print("  %d finding(s)" % len(findings))
    return 1


def diff(a, b):
    """Pixel diff against a committed baseline — answers 'did this shift?'."""
    import numpy as np
    ia, _ = load(a)
    ib, _ = load(b)
    if ia.size != ib.size:
        print("SIZE %s %s vs %s %s" % (a, ia.size, b, ib.size))
        return 1
    d = np.abs(np.asarray(ia, int) - np.asarray(ib, int)).sum(axis=2)
    changed = int((d > 12).sum())
    pct = 100.0 * changed / d.size
    print("diff %s vs %s: %d px changed (%.3f%%)" % (a, b, changed, pct))
    if changed:
        ys, xs = np.nonzero(d > 12)
        print("  region x=%d..%d y=%d..%d" % (xs.min(), xs.max(), ys.min(), ys.max()))
    return 1 if pct > 0.1 else 0


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(2)
    cmd = sys.argv[1]
    if cmd == "capture":
        capture(sys.argv[2]); sys.exit(0)
    if cmd == "audit":
        sys.exit(audit(sys.argv[2]))
    if cmd == "diff":
        sys.exit(diff(sys.argv[2], sys.argv[3]))
    print(__doc__); sys.exit(2)
