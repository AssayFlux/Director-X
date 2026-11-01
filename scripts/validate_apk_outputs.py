#!/usr/bin/env python3
"""Validate universal and ABI-split APK outputs, including native payloads."""
from __future__ import annotations

import json
import sys
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
EXPECTED_ABIS = {"armeabi-v7a", "arm64-v8a", "x86", "x86_64"}


class ApkOutputError(RuntimeError):
    pass


def validate_variant(root: Path, variant: str) -> list[Path]:
    output_dir = root / "app" / "build" / "outputs" / "apk" / variant
    metadata_path = output_dir / "output-metadata.json"
    if not metadata_path.is_file():
        raise ApkOutputError(f"Missing APK output metadata: {metadata_path}")

    try:
        metadata = json.loads(metadata_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise ApkOutputError(f"Cannot read APK output metadata: {error}") from error

    elements = metadata.get("elements")
    if not isinstance(elements, list) or not elements:
        raise ApkOutputError("APK output metadata must contain at least one element")

    found_abis: set[str] = set()
    universal_count = 0
    apk_paths: list[Path] = []

    for element in elements:
        if not isinstance(element, dict):
            raise ApkOutputError("APK output metadata contains a malformed element")
        output_name = element.get("outputFile")
        if not isinstance(output_name, str) or not output_name.endswith(".apk"):
            raise ApkOutputError("APK output metadata contains an invalid outputFile")
        apk_path = output_dir / output_name
        if not apk_path.is_file() or apk_path.stat().st_size <= 0:
            raise ApkOutputError(f"APK is missing or empty: {apk_path}")
        apk_paths.append(apk_path)

        filters = element.get("filters", [])
        if not isinstance(filters, list):
            raise ApkOutputError(f"Malformed ABI filters for {output_name}")
        abis = {
            item.get("value")
            for item in filters
            if isinstance(item, dict) and item.get("filterType") == "ABI"
        }
        if len(abis) > 1:
            raise ApkOutputError(f"APK output {output_name} declares multiple ABI filters")
        if abis:
            found_abis.update(abis)
        else:
            universal_count += 1

        try:
            with zipfile.ZipFile(apk_path) as archive:
                names = archive.namelist()
                if len(names) != len(set(names)):
                    raise ApkOutputError(f"Duplicate ZIP entries detected in {output_name}")
                native_paths = [
                    name for name in names
                    if name.startswith("lib/") and name.endswith(".so")
                ]
        except (OSError, zipfile.BadZipFile) as error:
            raise ApkOutputError(f"Invalid APK archive {output_name}: {error}") from error

        if not native_paths:
            raise ApkOutputError(f"APK contains no packaged native .so libraries: {output_name}")
        packaged_abis = {
            name.split("/")[1]
            for name in native_paths
            if len(name.split("/")) > 2
        }
        required_abis = EXPECTED_ABIS if not abis else abis
        missing_native = required_abis - packaged_abis
        if missing_native:
            raise ApkOutputError(
                f"{output_name} is missing native libraries for: {', '.join(sorted(missing_native))}"
            )

    if universal_count != 1:
        raise ApkOutputError(f"Expected exactly one universal APK, found {universal_count}")
    missing_abis = EXPECTED_ABIS - found_abis
    if missing_abis:
        raise ApkOutputError(f"Missing ABI APK splits: {', '.join(sorted(missing_abis))}")

    return apk_paths


def _write_fixture(root: Path, variant: str) -> None:
    output_dir = root / "app" / "build" / "outputs" / "apk" / variant
    output_dir.mkdir(parents=True, exist_ok=True)
    elements = []
    universal = output_dir / "app-universal.apk"
    with zipfile.ZipFile(universal, "w") as archive:
        for abi in sorted(EXPECTED_ABIS):
            archive.writestr(f"lib/{abi}/libfixture.so", b"fixture")
    elements.append({"outputFile": universal.name, "filters": []})

    for abi in sorted(EXPECTED_ABIS):
        apk = output_dir / f"app-{abi}.apk"
        with zipfile.ZipFile(apk, "w") as archive:
            archive.writestr(f"lib/{abi}/libfixture.so", b"fixture")
        elements.append({
            "outputFile": apk.name,
            "filters": [{"filterType": "ABI", "value": abi}],
        })

    (output_dir / "output-metadata.json").write_text(
        json.dumps({"elements": elements}), encoding="utf-8"
    )


def main(argv: list[str]) -> int:
    if len(argv) == 2 and argv[1] == "--self-test":
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            _write_fixture(root, "debug")
            result = validate_variant(root, "debug")
            if len(result) != len(EXPECTED_ABIS) + 1:
                raise AssertionError("APK output validator self-test returned the wrong APK count")
        print("APK output metadata and native ABI self-tests passed.")
        return 0
    if len(argv) != 2 or argv[1] not in {"debug", "qa", "streaming"}:
        print("Usage: validate_apk_outputs.py <debug|qa|streaming> | --self-test", file=sys.stderr)
        return 2
    try:
        paths = validate_variant(ROOT, argv[1])
    except ApkOutputError as error:
        print(f"APK output validation failed: {error}", file=sys.stderr)
        return 1
    print(f"Validated {len(paths)} APKs for {argv[1]}: universal + all four ABI splits, native libraries present.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
