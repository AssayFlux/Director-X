#!/usr/bin/env python3
"""Promote a verified FFmpegKitNext 9.0.2 CI artifact into the pinned app dependency."""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WRAPPER_COMMIT = "9f04bf1e3c97def70b98e198cb80a57b43ef5b45"
FFMPEG_COMMIT = "946fcce07b6dcd0331c8cc609192aeff5e1924f8"
OLD_AAR = "third_party/ffmpeg-kit-next/ffmpeg-kit-next-8.1.0.aar"
OLD_POM = "third_party/ffmpeg-kit-next/ffmpeg-kit-next-8.1.0.pom"
NEW_AAR = "third_party/ffmpeg-kit-next/ffmpeg-kit-next-9.0.2.aar"
NEW_POM = "third_party/ffmpeg-kit-next/ffmpeg-kit-next-9.0.2.pom"
OLD_AAR_SHA = "7971240aff84ce59a4ab28400bb4af59d24c20ce68c25525d41d910246ccff62"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def replace_text(relative: str, old: str, new: str, expected: int | None = None) -> None:
    path = ROOT / relative
    text = path.read_text(encoding="utf-8")
    count = text.count(old)
    if count == 0 or (expected is not None and count != expected):
        raise SystemExit(f"{relative}: expected {expected or 'at least one'} occurrence of {old!r}, found {count}")
    path.write_text(text.replace(old, new), encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--artifact-dir", required=True)
    parser.add_argument("--run-id", required=True)
    parser.add_argument("--head-sha", required=True)
    parser.add_argument("--built-at", required=True)
    args = parser.parse_args()

    artifact_dir = Path(args.artifact_dir)
    aar_source = artifact_dir / "build-output" / "ffmpeg-kit-next-9.0.2.aar"
    pom_source = artifact_dir / "build-output" / "ffmpeg-kit-next-9.0.2.pom"
    if not aar_source.is_file() or not pom_source.is_file():
        raise SystemExit("Successful native workflow artifact is missing the 9.0.2 AAR or POM")
    pom_text = pom_source.read_text(encoding="utf-8")
    if "<version>9.0.2</version>" not in pom_text or "<artifactId>ffmpeg-kit-next</artifactId>" not in pom_text:
        raise SystemExit("Native POM does not identify the FFmpegKitNext 9.0.2 artifact")

    aar_target = ROOT / NEW_AAR
    pom_target = ROOT / NEW_POM
    aar_target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(aar_source, aar_target)
    shutil.copy2(pom_source, pom_target)
    for old in (OLD_AAR, OLD_POM):
        old_path = ROOT / old
        if old_path.exists():
            old_path.unlink()

    aar_hash = sha256(aar_target)
    pom_hash = sha256(pom_target)
    sums_path = artifact_dir / "build-output" / "SHA256SUMS"
    if not sums_path.is_file():
        raise SystemExit("Native evidence is missing SHA256SUMS")
    sums = {}
    for line in sums_path.read_text(encoding="utf-8").splitlines():
        digest, filename = line.split(maxsplit=1)
        sums[Path(filename.strip().lstrip("*")).name] = digest
    if sums.get(aar_source.name) != aar_hash or sums.get(pom_source.name) != pom_hash:
        raise SystemExit("Promoted AAR/POM hashes do not match the native workflow SHA256SUMS")
    aar_size = aar_target.stat().st_size
    pom_size = pom_target.stat().st_size
    if aar_hash == OLD_AAR_SHA:
        raise SystemExit("Promoted AAR unexpectedly has the previous 8.1.0 checksum")

    lock_path = ROOT / "third_party/ffmpeg-kit-next/native-lock.json"
    lock = json.loads(lock_path.read_text(encoding="utf-8"))
    lock["artifact"].update({
        "path": NEW_AAR,
        "size": aar_size,
        "sha256": aar_hash,
        "pomPath": NEW_POM,
        "pomSize": pom_size,
        "pomSha256": pom_hash,
        "buildRunId": str(args.run_id),
    })
    lock["source"].update({
        "commit": WRAPPER_COMMIT,
        "ffmpegRevision": FFMPEG_COMMIT,
        "ffmpegVersion": "9.0.2",
        "builtAt": args.built_at,
        "buildRunId": str(args.run_id),
        "buildHeadSha": args.head_sha,
    })
    lock["source"]["securityPatchSha256"] = sha256(ROOT / lock["source"]["securityPatch"])
    lock["build"]["abis"] = ["armeabi-v7a", "armeabi-v7a-neon", "arm64-v8a", "x86", "x86_64"]
    lock["build"]["command"] = (
        "./nix-android.sh -p android-r27d --arch=<ABI> --enable-lib-libass "
        "--enable-lib-libjxl --enable-lib-android-media-codec --enable-lib-android-zlib "
        "--enable-lib-openh264 --jobs=8"
    )
    for component in lock["components"]:
        if component["name"] == "ffmpeg-kit-next":
            component.update({
                "version": "9.0.2",
                "commit": WRAPPER_COMMIT,
                "purl": f"pkg:github/arthenica/ffmpeg-kit-next@{WRAPPER_COMMIT}",
            })
        elif component["name"] == "FFmpeg":
            component.update({
                "version": "9.0.2",
                "commit": FFMPEG_COMMIT,
                "purl": f"pkg:generic/ffmpeg@9.0.2",
            })
    for advisory in lock["advisories"]:
        if advisory["id"] == "CVE-2026-8461":
            advisory["fixedVersion"] = "9.0.2"
            advisory["rationale"] = "The shipped FFmpeg 9.0.2 source includes the upstream fix."
    prep = (ROOT / "scripts/prepare_ffmpeg_kit_next_9_source.sh").read_text(encoding="utf-8")
    match = re.search(r"FFMPEG_SECURITY_COMMITS=\(\s*(.*?)\s*\)", prep, re.S)
    if not match:
        raise SystemExit("Could not read the pinned FFmpeg security backport inventory")
    commits = re.findall(r"(?m)^\s*([0-9a-f]{40})\s*$", match.group(1))
    lock["security"]["backportedCommits"] = commits + [
        "96eea3824100b195294ebba9570c6a66427a4920",
        "439b452f9fe916a9f7d8788ab6afee8fac846b0b",
    ]
    lock["security"]["backportMethod"] = (
        "Pinned upstream commits plus an idempotent source-equivalent ProRes slice-reserve port; "
        "review horizon remains unchanged pending full advisory review."
    )
    lock_path.write_text(json.dumps(lock, indent=2) + "\n", encoding="utf-8")

    replace_text("app/build.gradle.kts", OLD_AAR, NEW_AAR, 1)
    replace_text("app/build.gradle.kts", OLD_AAR_SHA, aar_hash, 1)
    replace_text("app/build.gradle.kts",
                 "FFmpegKitNext 8.1.0 / FFmpeg 8.1.2, five Android ABIs.",
                 "FFmpegKitNext 9.0.2 / FFmpeg 9.0.2, five Android ABIs.", 1)
    replace_text("app/src/main/java/com/directorx/editor/engine/OpenSourceLicenses.kt", OLD_AAR, NEW_AAR, 1)
    replace_text("app/src/test/java/com/directorx/editor/engine/OpenSourceLicensesTest.kt", OLD_AAR, NEW_AAR, 2)
    replace_text("app/src/test/java/com/directorx/editor/engine/OpenSourceLicensesTest.kt", "8.1.0", "9.0.2")
    replace_text("app/src/test/java/com/directorx/editor/engine/OpenSourceLicensesTest.kt", "FFmpeg 8.1.2", "FFmpeg 9.0.2")
    replace_text("app/src/test/java/com/directorx/editor/engine/ModelRegistryDocumentationTest.kt", "ffmpeg-kit-next-8.1.0.aar", "ffmpeg-kit-next-9.0.2.aar", 1)
    replace_text("app/src/main/java/com/directorx/editor/engine/FFmpegEngine.kt", "8.1.0", "9.0.2")
    replace_text("app/src/main/java/com/directorx/editor/engine/FFmpegEngine.kt", "8.1.2", "9.0.2")

    registry_path = ROOT / "scripts/capability_registry.json"
    registry = json.loads(registry_path.read_text(encoding="utf-8"))
    ffmpeg_entry = next(item for item in registry["dependencies"] if item["id"] == "ffmpeg")
    ffmpeg_entry["version"] = "9.0.2 (FFmpeg 9.0.2)"
    ffmpeg_entry["coordinate"] = NEW_AAR
    ffmpeg_entry["notice"]["sourceOfferText"] = (
        "Exact corresponding-source recipe for this LGPL v3.0-or-later build:\\n"
        f"https://github.com/arthenica/ffmpeg-kit-next/tree/{WRAPPER_COMMIT}\\n"
        f"FFmpeg source revision {FFMPEG_COMMIT}; configure command: "
        "./nix-android.sh -p android-r27d --arch=<ABI> --enable-lib-libass --enable-lib-libjxl "
        "--enable-lib-android-media-codec --enable-lib-android-zlib --enable-lib-openh264 --jobs=8\\n"
        "The security patch, complete recipe, checksums, disabled components, and source revisions "
        "are recorded in third_party/ffmpeg-kit-next/native-lock.json and the installed source-offer resource."
    )
    registry_path.write_text(json.dumps(registry, indent=2) + "\\n", encoding="utf-8")

    replace_text("NOTICE", "FFmpegKitNext 8.1.0 with FFmpeg 8.1.2", "FFmpegKitNext 9.0.2 with FFmpeg 9.0.2", 1)
    replace_text("ACKNOWLEDGMENTS.md", "ffmpeg-kit-next-8.1.0.aar", "ffmpeg-kit-next-9.0.2.aar")
    replace_text("ACKNOWLEDGMENTS.md", "8.1.0", "9.0.2")
    replace_text("ACKNOWLEDGMENTS.md", "8.1.2", "9.0.2")
    replace_text("app/src/main/res/raw/ffmpeg_kit_next_source_offer.txt",
                 "FFmpegKitNext 8.1.0 with FFmpeg 8.1.2.",
                 "FFmpegKitNext 9.0.2 with FFmpeg 9.0.2.", 1)
    replace_text("app/src/main/res/raw/ffmpeg_kit_next_source_offer.txt",
                 "https://github.com/arthenica/ffmpeg-kit-next/tree/3e223118e6e8fb6208693ecf3952e77cd096f587",
                 f"https://github.com/arthenica/ffmpeg-kit-next/tree/{WRAPPER_COMMIT}", 1)
    replace_text("app/src/main/res/raw/ffmpeg_kit_next_source_offer.txt",
                 "Android NDK 27.3.13750724, Android API 24, OpenH264 2.3.1",
                 f"FFmpeg source revision {FFMPEG_COMMIT}; Android NDK 27.3.13750724, Android API 24, OpenH264 2.3.1", 1)
    replace_text("app/src/main/res/raw/ffmpeg_kit_next_source_offer.txt",
                 "./nix-android.sh -p android-r27d --enable-libass --enable-libjxl --enable-android-media-codec --enable-android-zlib --enable-openh264 --jobs=8",
                 "./nix-android.sh -p android-r27d --arch=<ABI> --enable-lib-libass --enable-lib-libjxl --enable-lib-android-media-codec --enable-lib-android-zlib --enable-lib-openh264 --jobs=8", 1)

    import subprocess, sys
    subprocess.run([sys.executable, "scripts/generate_capability_registry.py"], cwd=ROOT, check=True)

    models_path = ROOT / "docs/models.md"
    models_text = models_path.read_text(encoding="utf-8")
    old_row = next((line for line in models_text.splitlines() if "ffmpeg-kit-next-8.1.0.aar" in line), None)
    if old_row is None:
        raise SystemExit("docs/models.md is missing the active FFmpeg native dependency row")
    new_row = (
        f"| `ffmpeg-kit-next-9.0.2.aar` / FFmpeg 9.0.2 | Active source-pinned native dep | "
        f"Vendored build from `arthenica/ffmpeg-kit-next@{WRAPPER_COMMIT}`; exact recipe and component revisions in "
        f"`third_party/ffmpeg-kit-next/native-lock.json` | Five Android build targets; 16 KB LOAD alignment verified "
        f"for arm64-v8a and x86_64 | LGPL-3.0-or-later build; security review remains open pending advisory reconciliation | "
        f"The recorded artifact is {aar_size:,}-byte AAR with SHA-256 `{aar_hash}`; native SBOM is generated from the updated lock. |"
    )
    models_path.write_text(models_text.replace(old_row, new_row), encoding="utf-8")

    print(f"Promoted FFmpegKitNext 9.0.2: AAR={aar_size} bytes sha256={aar_hash}; POM={pom_size} bytes sha256={pom_hash}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
