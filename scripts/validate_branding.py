#!/usr/bin/env python3
"""Validate Director X branding selections against files in Assets/."""
from __future__ import annotations
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "Assets"
CONFIG = ROOT / "branding" / "branding.json"

def fail(message: str) -> None:
    print(f"[branding] ERROR: {message}", file=sys.stderr)
    raise SystemExit(1)

def main() -> None:
    try:
        data = json.loads(CONFIG.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        fail(f"cannot read {CONFIG}: {exc}")
    if data.get("brand", {}).get("appName") != "Director X":
        fail("brand.appName must remain Director X")
    if data.get("brand", {}).get("developer") != "AssayFlux":
        fail("brand.developer must remain AssayFlux")
    references = [data["launcher"]["asset"], data["about"]["signatureAsset"]]
    references.extend(item["asset"] for item in data["splash"]["sequence"])
    for area in ("dashboard", "settings", "editor"):
        placement = data["placements"][area]
        texture = placement["texture"]
        if texture != "none":
            references.append(texture)
    for name in references:
        if Path(name).name != name or "/" in name or "\\" in name:
            fail(f"asset must be a filename, not a path: {name!r}")
        candidate = ASSETS / name
        if not candidate.is_file():
            fail(f"selected asset does not exist in Assets/: {name}")
        if candidate.stat().st_size == 0:
            fail(f"selected asset is empty: {name}")
    if not re.fullmatch(r"#[0-9A-Fa-f]{6}", data["splash"]["background"]):
        fail("splash.background must be a six-digit hex color")
    for item in data["splash"]["sequence"]:
        if not isinstance(item["durationMs"], int) or not 250 <= item["durationMs"] <= 15000:
            fail("splash durationMs must be between 250 and 15000")
    print("[branding] Configuration valid; all selected assets exist in Assets/.")
    print(f"[branding] Launcher: {data['launcher']['asset']}")
    print("[branding] Splash: " + " -> ".join(f"{x['asset']} ({x['durationMs']} ms)" for x in data["splash"]["sequence"]))
    print(f"[branding] Author: {data['about']['authorName']}")

if __name__ == "__main__":
    main()
