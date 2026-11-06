#!/usr/bin/env python3
"""Rebuild the gameplay Scene2D atlas from its pixel geometry and palette.

Uses only Python's standard library. Coordinates are atlas pixels with Y down;
libGDX's atlas reader handles the conversion to Scene2D coordinates.
"""

import struct
import zlib
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SIZE = 128
PIXELS = bytearray(SIZE * SIZE * 4)
REGIONS = []


def color(value):
    value = value.lstrip("#")
    return bytes.fromhex(value + ("ff" if len(value) == 6 else ""))


def rect(x, y, width, height, fill):
    pixel = color(fill)
    for py in range(y, y + height):
        start = (py * SIZE + x) * 4
        PIXELS[start:start + width * 4] = pixel * width


def region(name, width=16, height=16, split=None, pad=None):
    index = len(REGIONS)
    x, y = 2 + index % 6 * 20, 2 + index // 6 * 20
    assert x + width < SIZE and y + height < SIZE
    REGIONS.append((name, x, y, width, height, split, pad))
    return x, y


def bevel(name, fill, inset=False, accent=None, edge=2):
    x, y = region(name, split=(edge,) * 4, pad=(5,) * 4)
    rect(x, y, 16, 16, "242220")
    rect(x + 1, y + 1, 14, 14, "34312e" if inset else "a5a197")
    rect(x + edge, y + edge, 16 - edge * 2, 16 - edge * 2, fill)
    shadow = "8e8c88" if inset else "34312e"
    rect(x + edge, y + 16 - edge, 16 - edge - 1, edge - 1, shadow)
    rect(x + 16 - edge, y + edge, edge - 1, 16 - edge - 1, shadow)
    if accent:
        rect(x + edge, y + edge, 16 - edge * 2, 1, accent)


def solid(name, fill):
    x, y = region(name, 1, 1)
    rect(x, y, 1, 1, fill)


def icon(name, kind, fill):
    x, y = region(name, 12, 12)
    if kind == "close":
        for step in range(8):
            rect(x + 2 + step, y + 2 + step, 2, 2, fill)
            rect(x + 9 - step, y + 2 + step, 2, 2, fill)
    else:
        for step in range(5):
            offset = step if kind == "previous" else 4 - step
            rect(x + 3 + offset, y + 5 - step, 2, 2, fill)
            rect(x + 3 + offset, y + 5 + step, 2, 2, fill)


def png_chunk(kind, data):
    return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data))


def main():
    bevel("window-frame", "54504c", edge=3)
    bevel("panel", "47433f", inset=True)
    for name, fill, inset in [
        ("button", "6c6760", False),
        ("button-hover", "827d73", False),
        ("button-down", "54504c", True),
        ("button-disabled", "47433f", True),
    ]:
        bevel(name, fill, inset)
    for name, fill, inset in [
        ("primary", "4a5261", False),
        ("primary-hover", "5b6980", False),
        ("primary-down", "3b4351", True),
    ]:
        bevel(name, fill, inset, "c6a461")
    bevel("destination", "47433f", inset=True)
    bevel("destination-hover", "56534e", inset=True)
    bevel("destination-selected", "3e4857", inset=True, accent="d8b661")
    bevel("destination-selected-hover", "4b5970", inset=True, accent="e7c77a")
    bevel("field", "34312e", inset=True)
    bevel("field-focused", "34312e", inset=True, accent="d8b661")
    bevel("field-disabled", "47433f", inset=True)
    bevel("scroll-track", "34312e", inset=True)
    bevel("scroll-knob", "928f87")
    solid("cursor", "eeeae2")
    solid("selection", "526f9d")
    solid("dim", "00000066")
    for kind in ("close", "previous", "next"):
        icon(kind, kind, "ddd8c8")
        icon(kind + "-disabled", kind, "7d7972")
    bevel("progress-track", "34312e", inset=True)
    bevel("progress-fill", "526f9d", accent="c6a461")
    solid("separator", "34312e")

    raw = b"".join(b"\x00" + PIXELS[y * SIZE * 4:(y + 1) * SIZE * 4] for y in range(SIZE))
    png = b"\x89PNG\r\n\x1a\n"
    png += png_chunk(b"IHDR", struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0))
    png += png_chunk(b"IDAT", zlib.compress(raw, 9))
    png += png_chunk(b"IEND", b"")
    (ROOT / "assets/skin/gameplay.png").write_bytes(png)

    atlas = ["gameplay.png", f"size: {SIZE}, {SIZE}", "format: RGBA8888", "filter: Nearest, Nearest", "repeat: none"]
    for name, x, y, width, height, split, pad in REGIONS:
        atlas.extend([name, "  rotate: false", f"  xy: {x}, {y}", f"  size: {width}, {height}"])
        if split:
            atlas.append("  split: " + ", ".join(map(str, split)))
        if pad:
            atlas.append("  pad: " + ", ".join(map(str, pad)))
        atlas.extend([f"  orig: {width}, {height}", "  offset: 0, 0", "  index: -1"])
    (ROOT / "assets/skin/gameplay.atlas").write_text("\n".join(atlas) + "\n")


if __name__ == "__main__":
    main()
