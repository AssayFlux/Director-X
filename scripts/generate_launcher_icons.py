#!/usr/bin/env python3
"""Render selected Director X branding assets into Android resources."""
from __future__ import annotations
import argparse
import json
from pathlib import Path

try:
    import cairosvg
    from PIL import Image
except ImportError as exc:
    raise SystemExit("Install build renderers with: python3 -m pip install CairoSVG Pillow") from exc

ROOT = Path(__file__).resolve().parents[1]
CONFIG = ROOT / "branding" / "branding.json"
ASSETS = ROOT / "Assets"
DENSITIES = {"mdpi": (108, 48), "hdpi": (162, 72), "xhdpi": (216, 96),
             "xxhdpi": (324, 144), "xxxhdpi": (432, 192)}

def render_png(source: Path, width: int, height: int | None = None) -> bytes:
    height = height or width
    if source.suffix.lower() == ".svg":
        return cairosvg.svg2png(url=str(source), output_width=width, output_height=height)
    with Image.open(source) as image:
        image = image.convert("RGBA")
        image.thumbnail((width, height), Image.Resampling.LANCZOS)
        canvas = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        canvas.alpha_composite(image, ((width - image.width) // 2, (height - image.height) // 2))
        from io import BytesIO
        buffer = BytesIO()
        canvas.save(buffer, format="PNG")
        return buffer.getvalue()

def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", required=True, type=Path)
    output = parser.parse_args().output
    data = json.loads(CONFIG.read_text(encoding="utf-8"))
    launcher = ASSETS / data["launcher"]["asset"]
    signature = ASSETS / data["about"]["signatureAsset"]
    for source in (launcher, signature):
        if not source.is_file() or source.stat().st_size == 0:
            raise SystemExit(f"Selected branding asset is missing or empty: {source}")
    signature_output = output / "drawable-nodpi" / "assayflux_signature.png"
    signature_output.parent.mkdir(parents=True, exist_ok=True)
    signature_output.write_bytes(render_png(signature, 720, 360))
    for density, (foreground_size, legacy_size) in DENSITIES.items():
        folder = output / f"mipmap-{density}"
        folder.mkdir(parents=True, exist_ok=True)
        (folder / "ic_launcher_foreground.png").write_bytes(render_png(launcher, foreground_size))
        (folder / "ic_launcher.png").write_bytes(render_png(launcher, legacy_size))
    values = output / "values"
    values.mkdir(parents=True, exist_ok=True)
    background = data["splash"]["background"]
    # Launcher background is kept neutral and consistent with the selected branding.
    (values / "ic_launcher_colors.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\\n'
        f'<resources><color name="ic_launcher_background">{background}</color></resources>\\n',
        encoding="utf-8")
    adaptive = ('<?xml version="1.0" encoding="utf-8"?>\\n'
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\\n'
        '    <background android:drawable="@color/ic_launcher_background" />\\n'
        '    <foreground android:drawable="@mipmap/ic_launcher_foreground" />\\n'
        '</adaptive-icon>\\n')
    folder = output / "mipmap-anydpi-v26"
    folder.mkdir(parents=True, exist_ok=True)
    for name in ("ic_launcher.xml", "ic_launcher_round.xml"):
        (folder / name).write_text(adaptive, encoding="utf-8")

if __name__ == "__main__":
    main()
