"""Detectors for uilint. Pure stdlib.

Pixel channel : seam/band detection, WCAG contrast, baseline diff.
Bounds channel: touch-target size, occlusion by bottom nav, ellipsis/overlap.

Every detector returns a list of finding dicts:
    {"rule", "severity", "msg", "rect": (x0,y0,x1,y1) or None}
"""

import collections

from pngio import contrast_ratio, hexstr, luminance, pixel

CRIT, WARN = "ERROR", "WARN"


def _f(rule, sev, msg, rect=None):
    return {"rule": rule, "severity": sev, "msg": msg, "rect": rect}


# --------------------------------------------------------------------------
# 1. Seam / unexpected colour band
# --------------------------------------------------------------------------

def _runs(px, w, x, y0, y1, tol):
    """Run-length encode column x over [y0,y1) -> [(rgb, ytop, ybot)]."""
    out = []
    cur = pixel(px, w, x, y0)
    start = y0
    for y in range(y0 + 1, y1):
        c = pixel(px, w, x, y)
        if (abs(c[0] - cur[0]) > tol or abs(c[1] - cur[1]) > tol
                or abs(c[2] - cur[2]) > tol):
            out.append((cur, start, y))
            cur, start = c, y
        else:
            cur = c  # track drift within tolerance
    out.append((cur, start, y1))
    return out


def find_seams(px, w, h, cfg):
    """A seam is a thin horizontal run sandwiched between two tall runs,
    appearing in nearly every sampled column (so glyphs don't trigger it)."""
    cols = [max(0, min(w - 1, int(w * p))) for p in cfg["seam_columns"]]
    thin_lo, thin_hi = cfg["seam_min_px"], cfg["seam_max_px"]
    slab = cfg["seam_slab_px"]
    aa = cfg["seam_aa_px"]

    per_col = []
    for x in cols:
        runs = [r for r in _runs(px, w, x, 0, h, cfg["seam_tol"])
                if r[2] - r[1] > aa]              # drop antialiasing runs
        cands = []
        for i in range(1, len(runs) - 1):
            rgb, a, b = runs[i]
            if not (thin_lo <= b - a <= thin_hi):
                continue
            if (runs[i - 1][2] - runs[i - 1][1] >= slab
                    and runs[i + 1][2] - runs[i + 1][1] >= slab):
                cands.append((a, b, rgb, runs[i - 1][0], runs[i + 1][0]))
        per_col.append(cands)

    # keep only bands seen in >= quorum of columns, grouped by y-overlap
    quorum = max(2, int(round(len(cols) * cfg["seam_quorum"])))
    seen, bands = set(), []
    for ci, cands in enumerate(per_col):
        for (a, b, rgb, above, below) in cands:
            if (ci, a) in seen:
                continue
            hits, ys, ye = 1, a, b
            for cj in range(len(per_col)):
                if cj == ci:
                    continue
                for (a2, b2, _r, _u, _d) in per_col[cj]:
                    if a2 < b and b2 > a:          # overlaps
                        hits += 1
                        seen.add((cj, a2))
                        ys, ye = min(ys, a2), max(ye, b2)
                        break
            if hits >= quorum:
                bands.append((ys, ye, rgb, above, below))

    # A divider motif repeats; a seam is singular. Without this, every list
    # separator in the app is reported as a seam.
    by_colour = collections.defaultdict(list)
    for band in bands:
        key = tuple(c // (cfg["seam_tol"] * 2 + 1) for c in band[2])
        by_colour[key].append(band)

    out = []
    for key, group in by_colour.items():
        if len(group) >= cfg["divider_repeat"]:
            continue                       # repeating divider / list separator
        for (ys, ye, rgb, above, below) in group:
            out.append(_f(
                "seam", CRIT,
                "%dpx band of %s at y=%d..%d spanning the full width, "
                "between %s above and %s below" % (
                    ye - ys, hexstr(rgb), ys, ye,
                    hexstr(above), hexstr(below)),
                (0, ys, w, ye)))
    return out


def check_status_bar_seam(px, w, h, cfg, status_bottom):
    """Hard assert: the strip directly under the status bar must match the
    header colour, not the activity-root background."""
    if not status_bottom:
        return []
    probe = cfg["seam_slab_px"]
    top = pixel(px, w, w // 2, max(0, status_bottom - 4))
    xs = [max(0, min(w - 1, int(w * p))) for p in cfg["seam_columns"]]
    hits = []
    for x in xs:
        band = _runs(px, w, x, status_bottom, min(h, status_bottom + probe),
                     cfg["seam_tol"])
        first = band[0]
        if first[2] - first[1] <= cfg["seam_max_px"] and len(band) > 1:
            hits.append((x, first, band[1][0]))
    # Require a quorum: a rounded header corner clips only the edge columns.
    if len(hits) < max(2, int(round(len(xs) * cfg["seam_quorum"]))):
        return []
    x, first, below = hits[len(hits) // 2]
    return [_f(
        "status_bar_seam", CRIT,
        "a %dpx strip of %s sits between the status bar (%s) and the header "
        "(%s) across %d/%d sampled columns -- suspect a stray margin on the "
        "nav host or a mismatched activity root background" % (
            first[2] - first[1], hexstr(first[0]), hexstr(top),
            hexstr(below), len(hits), len(xs)),
        (0, status_bottom, w, first[2]))]


# --------------------------------------------------------------------------
# 2. WCAG contrast for text nodes (needs bounds from uiautomator)
# --------------------------------------------------------------------------

def _fg_bg(px, w, rect, cfg):
    """Split a text crop into (fg, bg) by percentile, never min/max --
    antialiased edge pixels sit between the two and would flatter the ratio."""
    x0, y0, x1, y1 = rect
    counts = collections.Counter()
    pxs = []
    for y in range(y0, y1):
        for x in range(x0, x1):
            c = pixel(px, w, x, y)
            counts[c] += 1
            pxs.append(c)
    if not pxs:
        return None, None, 0.0
    bg = counts.most_common(1)[0][0]
    lbg = luminance(bg)
    far = sorted(((abs(luminance(c) - lbg), c) for c in pxs),
                 key=lambda t: -t[0])
    ink = [t for t in far if t[0] > cfg["text_min_lum_delta"]]
    if len(ink) < max(8, int(len(pxs) * cfg["text_min_coverage"])):
        return None, None, 0.0        # no text found in this crop
    fg = ink[min(len(ink) - 1, int(len(ink) * cfg["text_core_pct"]))][1]
    return fg, bg, len(ink) / float(len(pxs))


def _is_prose(s):
    """Emoji and lone punctuation are decoration, not text to score."""
    return any(ch.isalpha() or ch.isdigit() for ch in s)


def check_contrast(px, w, nodes, cfg, scale):
    out = []
    for n in nodes:
        if not _is_prose(n["text"]):
            continue
        x0, y0, x1, y1 = n["bounds"]
        if x1 - x0 < 8 or y1 - y0 < 8:
            continue
        fg, bg, cov = _fg_bg(px, w, n["bounds"], cfg)
        if fg is None:
            continue
        ratio = contrast_ratio(fg, bg)
        # Conservative: only relax to 3.0 when the line box is unambiguously
        # large text (WCAG large = 18pt/24dp regular).
        dp = (y1 - y0) / scale
        need = 3.0 if dp >= cfg["large_text_dp"] else 4.5
        if ratio < need:
            out.append(_f(
                "contrast", CRIT if ratio < need - 0.7 else WARN,
                "%.2f:1 (need %.1f:1) for %r -- fg %s on bg %s, line box %.0fdp"
                % (ratio, need, n["text"][:40], hexstr(fg), hexstr(bg), dp),
                n["bounds"]))
    return out


# --------------------------------------------------------------------------
# 3. Baseline diff
# --------------------------------------------------------------------------

def _masked(x, y, masks):
    for (a, b, c, d) in masks:
        if a <= x < c and b <= y < d:
            return True
    return False


def diff_baseline(cur, base, cfg, masks):
    """Returns (findings, overlay_px). Reports diff *clusters with bounding
    boxes* -- a full-width band reads as a seam, a small box as one tile."""
    (w, h, px) = cur
    (bw, bh, bpx) = base
    if (w, h) != (bw, bh):
        return [_f("diff_shape", CRIT,
                   "refusing to diff: capture is %dx%d, baseline is %dx%d "
                   "(different device or density)" % (w, h, bw, bh))], None

    tol = cfg["diff_channel_tol"]
    hits = bytearray(w * h)
    n = 0
    for y in range(h):
        row = y * w
        for x in range(w):
            i = (row + x) * 3
            if (abs(px[i] - bpx[i]) > tol or abs(px[i + 1] - bpx[i + 1]) > tol
                    or abs(px[i + 2] - bpx[i + 2]) > tol):
                if _masked(x, y, masks):
                    continue
                hits[row + x] = 1
                n += 1

    pct = 100.0 * n / (w * h)
    out = []
    overlay = bytearray(px)
    for i in range(w * h):
        if hits[i]:
            overlay[i * 3] = 255
            overlay[i * 3 + 1] = 0
            overlay[i * 3 + 2] = 255

    if pct <= cfg["diff_pct_budget"]:
        return out, overlay

    for (x0, y0, x1, y1, cnt) in _clusters(hits, w, h, cfg):
        kind = "full-width band" if (x1 - x0) > w * 0.9 else "region"
        out.append(_f("diff", CRIT,
                      "%s x=%d..%d y=%d..%d (%d px) differs from baseline"
                      % (kind, x0, x1, y0, y1, cnt), (x0, y0, x1, y1)))
    out.insert(0, _f("diff_total", CRIT,
                     "%.3f%% of pixels differ from baseline (budget %.3f%%)"
                     % (pct, cfg["diff_pct_budget"])))
    return out, overlay


def _clusters(hits, w, h, cfg):
    """Coarse connected components on a downsampled grid -- exact CC on 2.6M
    px is needlessly slow and we only need bounding boxes."""
    g = cfg["cluster_grid_px"]
    gw, gh = (w + g - 1) // g, (h + g - 1) // g
    cell = [0] * (gw * gh)
    for y in range(h):
        row = y * w
        for x in range(w):
            if hits[row + x]:
                cell[(y // g) * gw + (x // g)] += 1
    live = set(i for i, c in enumerate(cell) if c >= cfg["cluster_min_cell"])
    out = []
    while live:
        seed = live.pop()
        comp, stack = [seed], [seed]
        while stack:
            i = stack.pop()
            cx, cy = i % gw, i // gw
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = cx + dx, cy + dy
                j = ny * gw + nx
                if 0 <= nx < gw and 0 <= ny < gh and j in live:
                    live.discard(j)
                    comp.append(j)
                    stack.append(j)
        xs = [i % gw for i in comp]
        ys = [i // gw for i in comp]
        cnt = sum(cell[i] for i in comp)
        if cnt >= cfg["cluster_min_px"]:
            out.append((min(xs) * g, min(ys) * g,
                        min(w, (max(xs) + 1) * g), min(h, (max(ys) + 1) * g),
                        cnt))
    out.sort(key=lambda t: -t[4])
    return out[: cfg["cluster_max_report"]]


# --------------------------------------------------------------------------
# 4-6. Bounds channel (uiautomator XML)
# --------------------------------------------------------------------------

def check_touch_targets(nodes, cfg, scale):
    """Pixels measure the drawn button; only bounds measure the touch target."""
    need = int(round(cfg["min_touch_dp"] * scale))
    out = []
    for n in nodes:
        if not (n["clickable"] or n["long_clickable"]):
            continue
        x0, y0, x1, y1 = n["bounds"]
        bw, bh = x1 - x0, y1 - y0
        if bw <= 0 or bh <= 0:
            continue
        if bw < need or bh < need:
            out.append(_f(
                "touch_target", CRIT,
                "%s is %.0fx%.0fdp, under the %ddp minimum (%s)" % (
                    n["rid"] or n["cls"], bw / scale, bh / scale,
                    cfg["min_touch_dp"], n["desc"] or n["text"] or "no label"),
                n["bounds"]))
    return out


def check_occlusion(nodes, cfg, scale):
    """Content hidden behind the floating bottom nav."""
    nav = None
    for n in nodes:
        rid = n["rid"].lower()
        if any(k in rid for k in cfg["bottom_nav_ids"]):
            if nav is None or n["bounds"][1] < nav["bounds"][1]:
                nav = n
    if nav is None:
        return []
    nx0, ny0, nx1, ny1 = nav["bounds"]
    out = []
    for n in nodes:
        # skip the nav itself, its ancestors, and its own children (tabs+labels)
        if n is nav or _is_ancestor(n, nav) or _is_ancestor(nav, n):
            continue
        if not (n["text"].strip() or n["clickable"]):
            continue
        x0, y0, x1, y1 = n["bounds"]
        if y1 <= ny0 or y0 >= ny1 or x1 <= nx0 or x0 >= nx1:
            continue
        overlap = min(y1, ny1) - max(y0, ny0)
        if overlap >= cfg["occlusion_min_px"]:
            out.append(_f(
                "occlusion", CRIT,
                "%r overlaps the bottom nav by %.0fdp (content y=%d..%d, "
                "nav top y=%d) -- add bottom padding + clipToPadding=false" % (
                    (n["text"] or n["rid"] or n["cls"])[:40],
                    overlap / scale, y0, y1, ny0),
                n["bounds"]))
    return out


def _is_ancestor(a, b):
    x0, y0, x1, y1 = a["bounds"]
    bx0, by0, bx1, by1 = b["bounds"]
    return x0 <= bx0 and y0 <= by0 and x1 >= bx1 and y1 >= by1


def check_text_fit(nodes, cfg, scale):
    """Truncation and collisions -- the locale-variance defect. Arabic labels
    are shorter, so this only ever fires under --locale en."""
    out = []
    for n in nodes:
        t = n["text"]
        # EditText hints legitimately end in an ellipsis ("Search tools…"),
        # so only real labels are candidates for truncation.
        if n["cls"] in cfg["ellipsis_exempt_classes"]:
            continue
        if t.endswith("…") or t.endswith("..."):
            out.append(_f("text_truncated", CRIT,
                          "%r is ellipsized -- label does not fit its tile"
                          % t[:48], n["bounds"]))
    texts = [n for n in nodes if _is_prose(n["text"])]
    for i in range(len(texts)):
        for j in range(i + 1, len(texts)):
            a, b = texts[i], texts[j]
            if _is_ancestor(a, b) or _is_ancestor(b, a):
                continue
            ax0, ay0, ax1, ay1 = a["bounds"]
            bx0, by0, bx1, by1 = b["bounds"]
            ox = min(ax1, bx1) - max(ax0, bx0)
            oy = min(ay1, by1) - max(ay0, by0)
            if ox > cfg["collide_min_px"] and oy > cfg["collide_min_px"]:
                out.append(_f("text_collision", CRIT,
                              "%r and %r overlap by %dx%dpx"
                              % (a["text"][:24], b["text"][:24], ox, oy),
                              (max(ax0, bx0), max(ay0, by0),
                               min(ax1, bx1), min(ay1, by1))))
    return out
