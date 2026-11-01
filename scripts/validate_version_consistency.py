#!/usr/bin/env python3
"""Keep app-version details out of public docs; Gradle remains authoritative."""
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
    version_name = re.search(r"(?m)^\\s*versionName\\s*=\\s*\"([^\"]+)\"", gradle)
    version_code = re.search(r"(?m)^\\s*versionCode\\s*=\\s*(\\d+)", gradle)
    if not version_name or not version_code:
        raise VersionConsistencyError("Gradle must declare the Android version name and version code")

    forbidden = (
        "img.shields.io/badge/APP%20VERSION-",
        "Android app version:",
        "versionCode `",
        "github/v/release/AssayFlux/Director-X",
        "releases/latest",
    )
    if any(item in readme for item in forbidden):
        raise VersionConsistencyError("README duplicates Gradle-owned version data or advertises a potentially mismatched release")

    settings_path = root / "app" / "src" / "main" / "java" / "com" / "directorx" / "editor" / "ui" / "settings" / "SettingsScreen.kt"
    settings = settings_path.read_text(encoding="utf-8")
    if "\"v${BuildConfig.VERSION_NAME}\"" not in settings:
        raise VersionConsistencyError("Settings must display BuildConfig.VERSION_NAME rather than a hardcoded app version")
    if re.search(r"\"v\\d+\\.\\d+\\.\\d+\"", settings):
        raise VersionConsistencyError("Settings contains a hardcoded semantic app version")
    return version_name.group(1), version_code.group(1)

def main() -> int:
    try:
        validate()
    except (OSError, VersionConsistencyError) as error:
        print(f"version policy validation failed: {error}", file=sys.stderr)
        return 1
    print("Application version remains Gradle-authoritative and is not duplicated in public documentation.")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
