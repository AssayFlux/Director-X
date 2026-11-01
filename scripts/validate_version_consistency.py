#!/usr/bin/env python3
"""Validate that the public README application version matches Gradle metadata."""
from __future__ import annotations

import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


class VersionConsistencyError(RuntimeError):
    pass


def validate(root: Path = ROOT) -> tuple[str, str]:
    gradle = (root / "app" / "build.gradle.kts").read_text(encoding="utf-8")
    readme = (root / "README.md").read_text(encoding="utf-8")

    version_name = re.search(r'(?m)^\s*versionName\s*=\s*"([^"]+)"', gradle)
    version_code = re.search(r"(?m)^\s*versionCode\s*=\s*(\d+)", gradle)
    if not version_name or not version_code:
        raise VersionConsistencyError("Gradle must declare literal versionName and versionCode values")

    name = version_name.group(1)
    code = version_code.group(1)
    badge = re.search(r"img\.shields\.io/badge/APP(?:LICATION)?%20VERSION-([^ -]+)-", readme)
    if not badge:
        raise VersionConsistencyError("README is missing its application-version badge")
    if badge.group(1) != name:
        raise VersionConsistencyError(
            f"README application-version badge is {badge.group(1)!r}; Gradle declares {name!r}"
        )

    expected_metadata = f"Android app version: `{name}` · versionCode `{code}`"
    if expected_metadata not in readme:
        raise VersionConsistencyError(
            f"README must contain the authoritative application metadata: {expected_metadata}"
        )

    settings = (root / "app" / "src" / "main" / "java" / "com" / "directorx" / "editor" / "ui" / "settings" / "SettingsScreen.kt").read_text(encoding="utf-8")
    if '"v${BuildConfig.VERSION_NAME}"' not in settings:
        raise VersionConsistencyError("Settings must display BuildConfig.VERSION_NAME rather than a hardcoded app version")
    if re.search(r'"v\d+\.\d+\.\d+"', settings):
        raise VersionConsistencyError("Settings contains a hardcoded semantic app version")
    return name, code


def main() -> int:
    try:
        name, code = validate()
    except (OSError, VersionConsistencyError) as error:
        print(f"version consistency validation failed: {error}", file=sys.stderr)
        return 1
    print(f"Application version is consistent: {name} (versionCode {code}).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
