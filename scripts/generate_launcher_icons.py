#!/usr/bin/env python3
"""Render the approved Director X master SVG into Android launcher densities."""
from __future__ import annotations
import argparse
from pathlib import Path

try:
    import cairosvg
except ImportError as exc:
    raise SystemExit("CairoSVG is required. Install with: python3 -m pip install CairoSVG") from exc

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "Assets" / "Director-X Logo.svg"
SIGNATURE = ROOT / "Assets" / "Author's Signature.svg"
DENSITIES = {"mdpi": (108, 48), "hdpi": (162, 72), "xhdpi": (216, 96),
             "xxhdpi": (324, 144), "xxxhdpi": (432, 192)}

def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", required=True, type=Path)
    output = parser.parse_args().output
    if not SOURCE.is_file() or SOURCE.stat().st_size < 100:
        raise SystemExit(f"Approved master logo is missing: {SOURCE}")
    svg = SOURCE.read_bytes()
    if not SIGNATURE.is_file() or SIGNATURE.stat().st_size < 100:
        raise SystemExit(f"Approved AssayFlux signature is missing: {SIGNATURE}")
    signature_output = output / "drawable-nodpi" / "assayflux_signature.png"
    signature_output.parent.mkdir(parents=True, exist_ok=True)
    signature_output.write_bytes(cairosvg.svg2png(bytestring=SIGNATURE.read_bytes(), output_width=720))
    for density, (foreground_size, legacy_size) in DENSITIES.items():
        folder = output / f"mipmap-{density}"
        folder.mkdir(parents=True, exist_ok=True)
        (folder / "ic_launcher_foreground.png").write_bytes(
            cairosvg.svg2png(bytestring=svg, output_width=foreground_size, output_height=foreground_size))
        (folder / "ic_launcher.png").write_bytes(
            cairosvg.svg2png(bytestring=svg, output_width=legacy_size, output_height=legacy_size))
    values = output / "values"
    values.mkdir(parents=True, exist_ok=True)
    (values / "ic_launcher_colors.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n<resources><color name="ic_launcher_background">#F5F0E6</color></resources>\n',
        encoding="utf-8")
    adaptive = ('<?xml version="1.0" encoding="utf-8"?>\n'
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@color/ic_launcher_background" />\n'
        '    <foreground android:drawable="@mipmap/ic_launcher_foreground" />\n'
        '</adaptive-icon>\n')
    folder = output / "mipmap-anydpi-v26"
    folder.mkdir(parents=True, exist_ok=True)
    for name in ("ic_launcher.xml", "ic_launcher_round.xml"):
        (folder / name).write_text(adaptive, encoding="utf-8")

if __name__ == "__main__":
    main()
