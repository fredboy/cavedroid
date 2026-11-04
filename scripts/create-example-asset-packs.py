#!/usr/bin/env python3
"""Create original CC0 checkerboard asset packs using only the Python standard library."""
import argparse
import json
from pathlib import Path
import struct
import zlib
import zipfile


def png(size, colors):
    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data))

    rows = bytearray()
    for y in range(size):
        rows.append(0)
        for x in range(size):
            rows.extend(colors[(x // (size // 4) + y // (size // 4)) % 2])
    header = struct.pack(">IIBBBBB", size, size, 8, 6, 0, 0, 0)
    return b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", header) + chunk(b"IDAT", zlib.compress(rows)) + chunk(b"IEND", b"")


def create(destination):
    destination.mkdir(parents=True, exist_ok=True)
    palettes = {
        16: ((210, 80, 80, 255), (90, 20, 20, 255)),
        32: ((80, 210, 80, 255), (20, 90, 20, 255)),
        64: ((80, 80, 210, 255), (20, 20, 90, 255)),
    }
    for size, colors in palettes.items():
        metadata = {
            "formatVersion": 1,
            "name": f"Checker Stone {size}x",
            "version": "1.0.0",
            "author": "CaveDroid example authors",
            "license": "CC0-1.0",
            "icon": "icon.png",
        }
        image = png(size, colors)
        with zipfile.ZipFile(destination / f"checker-stone-{size}x.zip", "w", zipfile.ZIP_DEFLATED) as archive:
            archive.writestr("pack.json", json.dumps(metadata, indent=2) + "\n")
            archive.writestr("icon.png", image)
            archive.writestr("textures/textures/blocks/stone.png", image)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, default=Path("build/asset-pack-examples"))
    create(parser.parse_args().output)
