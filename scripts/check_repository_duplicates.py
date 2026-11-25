#!/usr/bin/env python3
"""Detect exact duplicate tracked-working-tree files while preserving intentional copies.

The allowlist documents files that must remain byte-identical because Android,
Fastlane, or locale tooling consumes them at distinct paths.
"""
from __future__ import annotations

import argparse
import hashlib
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
IGNORED_DIRS = {".git", ".gradle", ".idea", ".venv", "build", "__pycache__"}

# These are not removable duplicates: each path has a distinct consumer.
INTENTIONAL_DUPLICATES = {
    frozenset({
        "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml",
        "app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml",
    }),
    *(
        frozenset({
            f"app/src/main/res/mipmap-{density}/ic_launcher.png",
            f"app/src/main/res/mipmap-{density}/ic_launcher_round.png",
        })
        for density in ("hdpi", "mdpi", "xhdpi", "xxhdpi", "xxxhdpi")
    ),
    frozenset({
        "fastlane/metadata/android/en-US/privacy_policy_url.txt",
        "fastlane/metadata/android/es-ES/privacy_policy_url.txt",
    }),
    frozenset({
        "Assets/UI Design Texture.png",
        "UI Design Texture.png",
    }),
    frozenset({
        "fastlane/metadata/android/en-US/title.txt",
        "fastlane/metadata/android/es-ES/title.txt",
    }),
    # Original owner captures are retained under Assets/ for provenance while
    # Fastlane consumes named byte-identical copies from the listing directory.
    frozenset({
        "Assets/Play Console Publishing Screenshots/20261124_075536.jpg",
        "fastlane/metadata/android/en-US/images/phoneScreenshots/01-home.jpg",
    }),
    frozenset({
        "Assets/Play Console Publishing Screenshots/20261124_075616.jpg",
        "fastlane/metadata/android/en-US/images/phoneScreenshots/08-export-defaults.jpg",
    }),
    frozenset({
        "Assets/Play Console Publishing Screenshots/20261124_075637.jpg",
        "fastlane/metadata/android/en-US/images/phoneScreenshots/06-timeline-settings.jpg",
    }),
    frozenset({
        "Assets/Play Console Publishing Screenshots/20261124_075724.jpg",
        "fastlane/metadata/android/en-US/images/phoneScreenshots/05-ai-models.jpg",
    }),
    frozenset({
        "Assets/Play Console Publishing Screenshots/20261124_075742.jpg",
        "fastlane/metadata/android/en-US/images/phoneScreenshots/07-appearance.jpg",
    }),
    frozenset({
        "Assets/Play Console Publishing Screenshots/20261124_075817.jpg",
        "fastlane/metadata/android/en-US/images/phoneScreenshots/02-new-project.jpg",
    }),
    frozenset({
        "Assets/Play Console Publishing Screenshots/20261124_075845.jpg",
        "fastlane/metadata/android/en-US/images/phoneScreenshots/03-editor.jpg",
    }),
    frozenset({
        "Assets/Play Console Publishing Screenshots/20261124_075909.jpg",
        "fastlane/metadata/android/en-US/images/phoneScreenshots/04-export.jpg",
    }),
}


def files_under(root: Path):
    for path in root.rglob("*"):
        if not path.is_file() or any(part in IGNORED_DIRS for part in path.parts):
            continue
        if path.is_symlink():
            continue
        yield path


def duplicate_groups(root: Path) -> list[tuple[str, ...]]:
    by_digest: dict[str, list[str]] = defaultdict(list)
    for path in files_under(root):
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        by_digest[digest].append(path.relative_to(root).as_posix())
    return sorted(
        (tuple(sorted(paths)) for paths in by_digest.values() if len(paths) > 1),
        key=lambda group: group,
    )


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=ROOT)
    parser.add_argument("--allowlist-only", action="store_true",
                        help="report unexpected duplicates without failing")
    args = parser.parse_args()
    root = args.root.resolve()
    if not root.is_dir():
        print(f"repository root does not exist: {root}", file=sys.stderr)
        return 2

    unexpected = []
    groups = duplicate_groups(root)
    for group in groups:
        if frozenset(group) in INTENTIONAL_DUPLICATES:
            print("INTENTIONAL: " + " == ".join(group))
        else:
            unexpected.append(group)
            print("UNEXPECTED: " + " == ".join(group), file=sys.stderr)

    if unexpected:
        print(f"found {len(unexpected)} unapproved exact-duplicate group(s)", file=sys.stderr)
        return 0 if args.allowlist_only else 1
    print(f"repository hygiene passed: {len(groups)} intentional duplicate group(s); none unapproved")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
