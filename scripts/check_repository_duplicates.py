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
        "assets/icon.png",
        "fastlane/metadata/android/en-US/images/icon.png",
    }),
    frozenset({
        "fastlane/metadata/android/en-US/privacy_policy_url.txt",
        "fastlane/metadata/android/es-ES/privacy_policy_url.txt",
    }),
    frozenset({
        "fastlane/metadata/android/en-US/title.txt",
        "fastlane/metadata/android/es-ES/title.txt",
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
