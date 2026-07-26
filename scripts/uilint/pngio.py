"""Minimal PNG read/write. Python stdlib only (zlib, struct) -- no PIL, no numpy.

Handles exactly what `adb exec-out screencap -p` emits: 8-bit, non-interlaced,
colour type 2 (RGB) or 6 (RGBA). Alpha is dropped on load: screencap alpha is
always 255 and carrying it into diffs is dead weight.

Image is represented as (w, h, px) where px is a bytearray of w*h*3 RGB bytes.
"""

import struct
import zlib

_CHANNELS = {0: 1, 2: 3, 4: 2, 6: 4}


class PngError(Exception):
    pass


def load_rgb(path):
    """Decode a PNG file to (w, h, px) with px = bytearray of RGB triples."""
    with open(path, "rb") as fh:
        data = fh.read()
    if data[:8] != b"\x89PNG\r\n\x1a\x0a":
        raise PngError("%s: not a PNG file" % path)

    pos = 8
    idat = []
    w = h = depth = ctype = None
    interlace = 0
    while pos + 8 <= len(data):
        (length,) = struct.unpack(">I", data[pos : pos + 4])
        ctype_tag = data[pos + 4 : pos + 8]
        body = data[pos + 8 : pos + 8 + length]
        if ctype_tag == b"IHDR":
            w, h, depth, ctype, _, _, interlace = struct.unpack(">IIBBBBB", body)
        elif ctype_tag == b"IDAT":
            idat.append(body)          # list + join: += on bytes is O(n^2)
        elif ctype_tag == b"IEND":
            break
        pos += 12 + length

    if w is None:
        raise PngError("%s: no IHDR chunk" % path)
    if depth != 8:
        raise PngError(
            "%s: bit depth %d unsupported (need 8). adb screencap emits 8-bit; "
            "was this file re-encoded?" % (path, depth)
        )
    if interlace != 0:
        raise PngError("%s: interlaced PNGs unsupported" % path)
    if ctype not in (2, 6):
        raise PngError(
            "%s: colour type %d unsupported (need 2=RGB or 6=RGBA)" % (path, ctype)
        )

    nch = _CHANNELS[ctype]
    raw = zlib.decompress(b"".join(idat))
    stride = w * nch
    if len(raw) < h * (stride + 1):
        raise PngError("%s: truncated image data" % path)

    rows = []
    prev = bytearray(stride)
    p = 0
    for _ in range(h):
        ftype = raw[p]
        p += 1
        line = bytearray(raw[p : p + stride])
        p += stride
        if ftype == 0:
            pass
        elif ftype == 1:
            for i in range(nch, stride):
                line[i] = (line[i] + line[i - nch]) & 255
        elif ftype == 2:
            for i in range(stride):
                line[i] = (line[i] + prev[i]) & 255
        elif ftype == 3:
            for i in range(nch):
                line[i] = (line[i] + (prev[i] >> 1)) & 255
            for i in range(nch, stride):
                line[i] = (line[i] + ((line[i - nch] + prev[i]) >> 1)) & 255
        elif ftype == 4:
            for i in range(nch):
                line[i] = (line[i] + prev[i]) & 255
            for i in range(nch, stride):
                a = line[i - nch]
                b = prev[i]
                c = prev[i - nch]
                pp = a + b - c
                pa = abs(pp - a)
                pb = abs(pp - b)
                pc = abs(pp - c)
                if pa <= pb and pa <= pc:
                    pr = a
                elif pb <= pc:
                    pr = b
                else:
                    pr = c
                line[i] = (line[i] + pr) & 255
        else:
            raise PngError("%s: bad filter type %d" % (path, ftype))
        rows.append(line)
        prev = line

    if nch == 3:
        px = bytearray().join(rows)
    else:  # drop alpha
        px = bytearray(w * h * 3)
        o = 0
        for line in rows:
            for i in range(0, stride, 4):
                px[o] = line[i]
                px[o + 1] = line[i + 1]
                px[o + 2] = line[i + 2]
                o += 3
    return w, h, px


def save_rgb(path, w, h, px):
    """Write (w, h, px) RGB bytes as a PNG (filter 0, deflate level 6)."""
    stride = w * 3
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        raw += px[y * stride : (y + 1) * stride]

    def chunk(tag, body):
        return (
            struct.pack(">I", len(body))
            + tag
            + body
            + struct.pack(">I", zlib.crc32(tag + body) & 0xFFFFFFFF)
        )

    out = b"\x89PNG\r\n\x1a\x0a"
    out += chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 2, 0, 0, 0))
    out += chunk(b"IDAT", zlib.compress(bytes(raw), 6))
    out += chunk(b"IEND", b"")
    with open(path, "wb") as fh:
        fh.write(out)


def pixel(px, w, x, y):
    i = (y * w + x) * 3
    return px[i], px[i + 1], px[i + 2]


def luminance(rgb):
    """WCAG 2.x relative luminance for an sRGB triple."""
    out = []
    for c in rgb:
        s = c / 255.0
        out.append(s / 12.92 if s <= 0.03928 else ((s + 0.055) / 1.055) ** 2.4)
    return 0.2126 * out[0] + 0.7152 * out[1] + 0.0722 * out[2]


def contrast_ratio(rgb1, rgb2):
    l1 = luminance(rgb1)
    l2 = luminance(rgb2)
    if l1 < l2:
        l1, l2 = l2, l1
    return (l1 + 0.05) / (l2 + 0.05)


def hexstr(rgb):
    return "#%02X%02X%02X" % rgb
