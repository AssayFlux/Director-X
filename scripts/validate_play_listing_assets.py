#!/usr/bin/env python3
"""Validate Director X's committed Google Play listing package."""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import struct
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
LOCALE = ROOT / "fastlane" / "metadata" / "android" / "en-US"
IMAGES = LOCALE / "images"
MANIFEST = ROOT / "app" / "src" / "main" / "AndroidManifest.xml"
STREAMING_MANIFEST = ROOT / "app" / "src" / "streaming" / "AndroidManifest.xml"


class ListingError(RuntimeError):
    pass


def rel(path: Path) -> str:
    return str(path.relative_to(ROOT)).replace("\\", "/")


def read_text(path: Path) -> str:
    if not path.is_file():
        raise ListingError(f"missing required file: {rel(path)}")
    return path.read_text(encoding="utf-8")


def png_info(path: Path) -> tuple[int, int, int, int]:
    if not path.is_file():
        raise ListingError(f"missing image: {rel(path)}")
    with path.open("rb") as handle:
        signature = handle.read(8)
        if signature != b"\x89PNG\r\n\x1a\n":
            raise ListingError(f"{rel(path)} is not a PNG")
        length = struct.unpack(">I", handle.read(4))[0]
        chunk_type = handle.read(4)
        if chunk_type != b"IHDR" or length != 13:
            raise ListingError(f"{rel(path)} has no valid IHDR chunk")
        width, height, bit_depth, color_type = struct.unpack(">IIBB", handle.read(10))
    return width, height, bit_depth, color_type


def require_png(path: Path, width: int, height: int, color_type: int | None = None, max_bytes: int | None = None) -> None:
    actual_width, actual_height, bit_depth, actual_color_type = png_info(path)
    if (actual_width, actual_height) != (width, height):
        raise ListingError(
            f"{rel(path)} dimensions are {actual_width}x{actual_height}, expected {width}x{height}"
        )
    if bit_depth != 8:
        raise ListingError(f"{rel(path)} must be 8-bit PNG, got bit depth {bit_depth}")
    if color_type is not None and actual_color_type != color_type:
        raise ListingError(f"{rel(path)} PNG color type is {actual_color_type}, expected {color_type}")
    if max_bytes is not None and path.stat().st_size > max_bytes:
        raise ListingError(f"{rel(path)} is {path.stat().st_size} bytes, expected <= {max_bytes}")


def require_text_bounds() -> None:
    title = read_text(LOCALE / "title.txt").strip()
    short = read_text(LOCALE / "short_description.txt").strip()
    full = read_text(LOCALE / "full_description.txt").strip()
    if not title or len(title) > 30:
        raise ListingError("title.txt must be 1-30 characters")
    if not short or len(short) > 80:
        raise ListingError("short_description.txt must be 1-80 characters")
    if not full or len(full) > 4000:
        raise ListingError("full_description.txt must be 1-4000 characters")


def jpeg_dimensions(path: Path) -> tuple[int, int]:
    """Read JPEG dimensions without adding an imaging dependency to the validator."""
    data = path.read_bytes()
    if not data.startswith(b"\xff\xd8"):
        raise ListingError(f"{rel(path)} has a .jpg/.jpeg extension but is not a JPEG")
    index = 2
    start_of_frame = {0xC0, 0xC1, 0xC2, 0xC3, 0xC5, 0xC6, 0xC7, 0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF}
    while index < len(data):
        if data[index] != 0xFF:
            index += 1
            continue
        while index < len(data) and data[index] == 0xFF:
            index += 1
        if index >= len(data):
            break
        marker = data[index]
        index += 1
        if marker in (0xD9, 0xDA):
            break
        if marker == 0x01 or 0xD0 <= marker <= 0xD7:
            continue
        if index + 2 > len(data):
            break
        segment_length = int.from_bytes(data[index:index + 2], "big")
        if marker in start_of_frame:
            if segment_length < 7 or index + 7 > len(data):
                break
            height = int.from_bytes(data[index + 3:index + 5], "big")
            width = int.from_bytes(data[index + 5:index + 7], "big")
            return width, height
        if segment_length < 2:
            break
        index += segment_length
    raise ListingError(f"{rel(path)} has no readable JPEG dimensions")


def require_phone_screenshots() -> list[Path]:
    """Validate 2–8 real phone screenshots against Google's mandatory limits."""
    directory = IMAGES / "phoneScreenshots"
    if not directory.is_dir():
        raise ListingError("missing required phoneScreenshots directory; add real app captures")
    files = sorted([
        *directory.glob("*.png"), *directory.glob("*.PNG"),
        *directory.glob("*.jpg"), *directory.glob("*.JPG"),
        *directory.glob("*.jpeg"), *directory.glob("*.JPEG"),
    ])
    if not 2 <= len(files) <= 8:
        raise ListingError(f"{rel(directory)} must contain 2-8 phone screenshots; found {len(files)}")
    for path in files:
        if path.suffix.lower() in (".jpg", ".jpeg"):
            width, height = jpeg_dimensions(path)
        else:
            width, height, bit_depth, color_type = png_info(path)
            if bit_depth != 8 or color_type != 2:
                raise ListingError(f"{rel(path)} must be an 8-bit 24-bit RGB PNG with no alpha")
        if min(width, height) < 320 or max(width, height) > 3840 or max(width, height) > 2.3 * min(width, height):
            raise ListingError(f"{rel(path)} dimensions {width}x{height} are outside Google Play phone screenshot bounds (320–3840 px; max 2.3:1)")
        if path.stat().st_size > 8 * 1024 * 1024:
            raise ListingError(f"{rel(path)} exceeds the 8 MB screenshot limit")
    return files


def require_optional_tablet_screenshots() -> list[Path]:
    """If tablet assets are supplied, require at least four correctly sized captures."""
    screenshots: list[Path] = []
    for kind in ("sevenInchScreenshots", "tenInchScreenshots"):
        directory = IMAGES / kind
        if not directory.exists():
            continue
        files = sorted([*directory.glob("*.png"), *directory.glob("*.PNG")])
        if len(files) < 4:
            raise ListingError(f"{rel(directory)} must contain at least 4 tablet screenshots when supplied")
        for path in files:
            width, height, bit_depth, color_type = png_info(path)
            if bit_depth != 8 or color_type != 2:
                raise ListingError(f"{rel(path)} must be an 8-bit 24-bit RGB PNG with no alpha")
            if min(width, height) < 1080 or max(width, height) > 7680:
                raise ListingError(f"{rel(path)} dimensions are outside Google Play tablet screenshot bounds")
            ratio = width / height
            if min(abs(ratio - 16 / 9), abs(ratio - 9 / 16)) > 0.02:
                raise ListingError(f"{rel(path)} must use a 16:9 or 9:16 tablet screenshot aspect ratio")
            if path.stat().st_size > 8 * 1024 * 1024:
                raise ListingError(f"{rel(path)} exceeds the 8 MB screenshot limit")
        screenshots.extend(files)
    return screenshots


def require_inventory(image_paths: list[Path]) -> None:
    path = IMAGES / "asset_inventory.json"
    inventory = json.loads(read_text(path))
    if inventory.get("schema") not in ("com.clearcut.play-listing-assets.v1", "org.assayflux.directorx.play-listing-assets.v1"):
        raise ListingError("asset_inventory.json has an unexpected schema")
    entries = inventory.get("assets")
    if not isinstance(entries, list):
        raise ListingError("asset_inventory.json must contain an assets list")

    by_path = {entry.get("path"): entry for entry in entries if isinstance(entry, dict)}
    expected = {str(image.relative_to(IMAGES)).replace("\\", "/") for image in image_paths}
    missing = sorted(expected - set(by_path))
    if missing:
        raise ListingError(f"asset_inventory.json is missing entries for: {', '.join(missing)}")
    for relative_path in sorted(expected):
        entry = by_path[relative_path]
        for field in ("altText", "caption", "source"):
            if not isinstance(entry.get(field), str) or not entry[field].strip():
                raise ListingError(f"{relative_path} inventory entry must include {field}")
        source = IMAGES / entry["source"]
        if not source.is_file():
            raise ListingError(f"{relative_path} inventory source is missing: {entry['source']}")

        if relative_path.startswith(("phoneScreenshots/", "tenInchScreenshots/")):
            if entry.get("sourceType") != "device-capture":
                raise ListingError(f"{relative_path} must be sourced from a device capture")
            if source.suffix.lower() == ".svg":
                raise ListingError(f"{relative_path} must not use a synthetic SVG source")

        # Freshness: the inventory records the hash of the source the committed
        # PNG was rendered from. Editing a source without re-running
        # scripts/generate_play_listing_assets.py leaves a stale listing asset behind,
        # which is how retired branding survived past a rename before.
        recorded = entry.get("sourceSha256")
        if not isinstance(recorded, str) or not recorded:
            raise ListingError(f"{relative_path} inventory entry must record sourceSha256")
        actual = hashlib.sha256(source.read_bytes()).hexdigest()
        if actual != recorded:
            raise ListingError(
                f"{entry['source']} has changed since {relative_path} was rendered "
                f"(sha256 {actual[:12]}… != recorded {recorded[:12]}…); "
                "re-run scripts/generate_play_listing_assets.py"
            )

        require_current_branding(source)
        require_current_branding(IMAGES / relative_path)


RETIRED_BRAND_NAMES = (b"NovaCut", b"novacut", b"NOVACUT", b"ClearCut", b"CLEARCUT")


def require_current_branding(path: Path) -> None:
    """Reject retired product branding in anything user-visible.

    The installed application ID is `com.directorx.editor`. Listing assets and copy must use the Director X identity; legacy identifiers remain only in compatibility data and attribution.
    """
    data = path.read_bytes()
    for name in RETIRED_BRAND_NAMES:
        if name in data:
            raise ListingError(f"{rel(path)} contains retired branding {name.decode()!r}")


def require_listing_copy_branding() -> None:
    for name in ("title.txt", "short_description.txt", "full_description.txt"):
        require_current_branding(LOCALE / name)
    require_current_branding(ROOT / "docs" / "privacy-policy.md")
    require_current_branding(ROOT / "docs" / "play-data-safety.md")


def require_privacy_docs() -> None:
    privacy_url = read_text(LOCALE / "privacy_policy_url.txt").strip()
    if not privacy_url.startswith("https://"):
        raise ListingError("privacy_policy_url.txt must contain an https URL")

    privacy = read_text(ROOT / "docs" / "privacy-policy.md").lower()
    for required in ("director x", "contact", "deletion", "retention"):
        if required not in privacy:
            raise ListingError(f"docs/privacy-policy.md must mention {required}")

    data_safety = read_text(ROOT / "docs" / "play-data-safety.md")
    manifest = read_text(MANIFEST)
    streaming_manifest = read_text(STREAMING_MANIFEST)
    permissions = sorted(set(re.findall(r'android:name="(android\.permission\.[A-Z_]+)"', manifest)))
    missing = [permission for permission in permissions if permission not in data_safety]
    if missing:
        raise ListingError(f"docs/play-data-safety.md is missing manifest permissions: {', '.join(missing)}")
    local_permissions = {
        "android.permission.NEARBY_WIFI_DEVICES",
        "android.permission.ACCESS_LOCAL_NETWORK",
    }
    if any(permission in manifest for permission in local_permissions):
        raise ListingError("normal builds must not declare dormant local-network permissions")
    missing_streaming = sorted(permission for permission in local_permissions if permission not in streaming_manifest)
    if missing_streaming:
        raise ListingError(f"streaming manifest is missing permissions: {', '.join(missing_streaming)}")
    undocumented_streaming = sorted(permission for permission in local_permissions if permission not in data_safety)
    if undocumented_streaming:
        raise ListingError(f"data safety worksheet is missing streaming permissions: {', '.join(undocumented_streaming)}")
    for required in (
        "Project content (clips, overlays, timelines, captions)",
        "Media metadata (durations, codecs, dimensions)",
        "Downloaded ML models (Whisper, MediaPipe)",
        "Diagnostic logs and export-incident summaries (redacted)",
        "Crash records (fatal exception breadcrumbs)",
        "Cloud generative video calls (consent-gated)",
        "AI usage ledger (per-project disclosure history)",
    ):
        if required not in data_safety:
            raise ListingError(f"docs/play-data-safety.md is missing dashboard category: {required}")


def validate() -> None:
    require_text_bounds()
    icon = IMAGES / "icon.png"
    feature = IMAGES / "featureGraphic.png"
    require_png(icon, 512, 512, color_type=6, max_bytes=1024 * 1024)
    require_png(feature, 1024, 500, color_type=2)
    phone_screenshots = require_phone_screenshots()
    tablet_screenshots = require_optional_tablet_screenshots()
    require_inventory([icon, feature, *phone_screenshots, *tablet_screenshots])
    require_listing_copy_branding()
    require_privacy_docs()


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--quiet", action="store_true", help="suppress success output")
    args = parser.parse_args()
    try:
        validate()
    except ListingError as error:
        print(f"play listing validation failed: {error}", file=sys.stderr)
        return 1
    if not args.quiet:
        print("Play listing assets, privacy policy, and Data safety worksheet verified.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
