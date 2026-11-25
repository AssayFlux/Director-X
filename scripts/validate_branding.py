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
    if Path(data["launcher"]["asset"]).suffix.lower() not in {".svg", ".png"}:
        fail("launcher.asset must be an SVG or PNG")
    splash_assets = [item["asset"] for item in data["splash"]["sequence"]]
    if data["launcher"]["asset"] not in splash_assets:
        fail("the launcher SVG must also appear in splash.sequence")
    references.extend(splash_assets)
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
    transition_ms = data["splash"]["transitionDurationMs"]
    if not isinstance(transition_ms, int) or not 1 <= transition_ms <= 1000:
        fail("splash.transitionDurationMs must be between 1 and 1000")
    for item in data["splash"]["sequence"]:
        if not isinstance(item["durationMs"], int) or not 250 <= item["durationMs"] <= 15000:
            fail("splash durationMs must be between 250 and 15000")
    artwork_source = ROOT / "app" / "src" / "main" / "java" / "com" / "directorx" / "editor" / "ui" / "theme" / "AssayFluxBrandArtwork.kt"
    if not artwork_source.is_file():
        fail(f"brand artwork composable is missing: {artwork_source}")
    artwork_text = artwork_source.read_text(encoding="utf-8")
    if any(token in artwork_text for token in ("android.webkit.WebView", "WebView(", "AndroidView(")):
        fail("brand artwork must use packaged Compose drawables; runtime WebView is prohibited")
    if "R.drawable.assayflux_ui_texture" not in artwork_text:
        fail("brand texture must use the generated assayflux_ui_texture drawable")
    print("[branding] Configuration valid; all selected assets exist in Assets/.")
    print(f"[branding] Launcher: {data['launcher']['asset']}")
    print("[branding] Splash: " + " -> ".join(f"{x['asset']} ({x['durationMs']} ms)" for x in data["splash"]["sequence"]))
    print(f"[branding] Transition: {transition_ms} ms")
    print(f"[branding] Author: {data['about']['authorName']}")

if __name__ == "__main__":
    main()
