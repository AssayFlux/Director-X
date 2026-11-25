#!/usr/bin/env python3
"""Validate distribution metadata bounds and reject unearned channel claims."""
from __future__ import annotations

import argparse
import re
import sys
import tempfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
FASTLANE_LOCALES = ("en-US", "es-ES")


class DistributionError(RuntimeError):
    pass


def rel(path: Path) -> str:
    try:
        return str(path.relative_to(ROOT)).replace("\\", "/")
    except ValueError:
        return str(path)


def read_text(path: Path) -> str:
    if not path.is_file():
        raise DistributionError(f"missing required file: {rel(path)}")
    return path.read_text(encoding="utf-8")


def parse_gradle_version(root: Path = ROOT) -> tuple[int, str]:
    text = read_text(root / "app" / "build.gradle.kts")
    code_match = re.search(r"versionCode\s*=\s*(\d+)", text)
    name_match = re.search(r'versionName\s*=\s*"([^"]+)"', text)
    if not code_match or not name_match:
        raise DistributionError("could not parse Gradle versionCode/versionName")
    return int(code_match.group(1)), name_match.group(1)


def require_no_unearned_channel_claims(path: Path) -> None:
    text = read_text(path)
    denied = (
        r"\bavailable on f-droid\b",
        r"\bdownload from f-droid\b",
        r"\bf-droid release is live\b",
        r"\bdeveloper verification complete\b",
        r"\bverified developer account is complete\b",
        r"\bdisponible en f-droid\b",
        r"\bdescarga desde f-droid\b",
        r"\bverificación del desarrollador está completa\b",
        r"\bel registro de desarrollador verificado está completo\b",
    )
    for pattern in denied:
        match = re.search(pattern, text, re.IGNORECASE)
        if match:
            raise DistributionError(f"{rel(path)} contains unearned distribution claim: {match.group(0)!r}")


def verify_readme(root: Path = ROOT) -> None:
    # README is a product/engineering overview, not a distribution-status
    # ledger. Keep unearned claims out without forcing mutable channel policy
    # into the product README.
    require_no_unearned_channel_claims(root / "README.md")


def verify_fastlane(root: Path = ROOT, locale: str = "en-US") -> None:
    fastlane = root / "fastlane" / "metadata" / "android" / locale
    full_description = fastlane / "full_description.txt"
    require_no_unearned_channel_claims(full_description)

    title = read_text(fastlane / "title.txt").strip()
    short = read_text(fastlane / "short_description.txt").strip()
    full = read_text(full_description).strip()
    if not title or len(title) > 30:
        raise DistributionError("fastlane title must be 1-30 characters")
    if not short or len(short) > 80:
        raise DistributionError("fastlane short description must be 1-80 characters")
    if not full or len(full) > 4000:
        raise DistributionError("fastlane full description must be 1-4000 characters")


def verify_latest_changelog(root: Path = ROOT, locale: str = "en-US") -> None:
    version_code, _version_name = parse_gradle_version(root)
    changelog = root / "fastlane" / "metadata" / "android" / locale / "changelogs" / f"{version_code}.txt"
    text = read_text(changelog).strip()
    if not text:
        raise DistributionError(f"{rel(changelog)} must not be empty")
    if len(text) > 500:
        raise DistributionError(f"{rel(changelog)} is {len(text)} characters, expected <= 500")


def verify_changelog_history(root: Path = ROOT, locale: str = "en-US") -> None:
    changelog_dir = root / "fastlane" / "metadata" / "android" / locale / "changelogs"
    for changelog in sorted(changelog_dir.glob("*.txt")):
        text = read_text(changelog).strip()
        if not text:
            raise DistributionError(f"{rel(changelog)} must not be empty")
        if len(text) > 500:
            raise DistributionError(f"{rel(changelog)} is {len(text)} characters, expected <= 500")


def validate(root: Path = ROOT) -> None:
    verify_readme(root)
    for locale in FASTLANE_LOCALES:
        verify_fastlane(root, locale)
        verify_latest_changelog(root, locale)
        verify_changelog_history(root, locale)


def write_fixture(root: Path, relative: str, text: str) -> None:
    path = root / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def write_minimal_repo(root: Path, readme: str, full_description: str) -> None:
    write_fixture(
        root,
        "app/build.gradle.kts",
        'android { defaultConfig { versionCode = 200\nversionName = "3.74.63" } }\n',
    )
    write_fixture(root, "README.md", readme)
    write_fixture(root, "fastlane/metadata/android/en-US/title.txt", "ClearCut\n")
    write_fixture(
        root,
        "fastlane/metadata/android/en-US/short_description.txt",
        "Professional video editor with AI tools and GLSL effects.\n",
    )
    write_fixture(root, "fastlane/metadata/android/en-US/full_description.txt", full_description)
    write_fixture(root, "fastlane/metadata/android/en-US/changelogs/200.txt", "- Distribution readiness gate\n")
    write_fixture(root, "fastlane/metadata/android/es-ES/title.txt", "ClearCut\n")
    write_fixture(
        root,
        "fastlane/metadata/android/es-ES/short_description.txt",
        "Editor de vídeo profesional.\n",
    )
    write_fixture(
        root,
        "fastlane/metadata/android/es-ES/full_description.txt",
        "Estado de distribución: GitHub Releases es el canal directo. Google Play está preparado. "
        "Los metadatos de F-Droid están presentes, pero el registro de desarrollador verificado no está completo.\n",
    )
    write_fixture(root, "fastlane/metadata/android/es-ES/changelogs/200.txt", "- Preparación de distribución\n")


def run_self_tests() -> None:
    simple_readme = "# Director X\\nAndroid video editor.\\n"
    simple_description = "Director X is an Android video editor.\\n"
    with tempfile.TemporaryDirectory() as temp:
        root = Path(temp)
        write_minimal_repo(root, simple_readme, simple_description)
        validate(root)

    with tempfile.TemporaryDirectory() as temp:
        root = Path(temp)
        write_minimal_repo(root, simple_readme, simple_description)
        write_fixture(root, "fastlane/metadata/android/en-US/changelogs/199.txt", "x" * 501)
        try:
            validate(root)
        except DistributionError as error:
            if "199.txt" not in str(error):
                raise DistributionError(f"self-test reported the wrong changelog: {error}")
        else:
            raise DistributionError("self-test expected an overlong historical changelog to fail")

    with tempfile.TemporaryDirectory() as temp:
        root = Path(temp)
        write_minimal_repo(root, "F-Droid release is live.", simple_description)
        try:
            validate(root)
        except DistributionError:
            pass
        else:
            raise DistributionError("self-test expected unearned F-Droid claim to fail")

    with tempfile.TemporaryDirectory() as temp:
        root = Path(temp)
        write_minimal_repo(root, simple_readme, "F-Droid release is live.")
        try:
            validate(root)
        except DistributionError:
            pass
        else:
            raise DistributionError("self-test expected unearned store-description claim to fail")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--self-test", action="store_true", help="run built-in fixture checks")
    args = parser.parse_args()
    try:
        if args.self_test:
            run_self_tests()
            print("distribution readiness self-tests passed.")
            return 0
        validate()
    except DistributionError as error:
        print(f"distribution readiness validation failed: {error}", file=sys.stderr)
        return 1
    print("Distribution readiness metadata verified.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
