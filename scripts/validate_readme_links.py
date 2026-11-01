#!/usr/bin/env python3
"""Check local Markdown/HTML links and images referenced by README.md."""
from __future__ import annotations

import re
import sys
from pathlib import Path
from urllib.parse import unquote, urlsplit

ROOT = Path(__file__).resolve().parents[1]


def local_targets(readme: str):
    fence = chr(96) * 3
    readme = re.sub(re.escape(fence) + r".*?" + re.escape(fence), "", readme, flags=re.DOTALL)
    patterns = (
        r"\]\(([^)]+)\)",
        r"""(?:href|src)\s*=\s*["']([^"']+)["']""",
    )
    for pattern in patterns:
        for match in re.finditer(pattern, readme, flags=re.IGNORECASE):
            target = match.group(1).strip().split()[0]
            if target.startswith("<") and target.endswith(">"):
                target = target[1:-1]
            yield target


def validate(root: Path = ROOT) -> list[str]:
    readme_path = root / "README.md"
    if not readme_path.is_file():
        return ["README.md is missing"]
    readme = readme_path.read_text(encoding="utf-8")
    errors: list[str] = []
    checked: set[str] = set()

    for target in local_targets(readme):
        parsed = urlsplit(target)
        if parsed.scheme or parsed.netloc or not parsed.path:
            continue
        path = unquote(parsed.path)
        if path.startswith("/"):
            path = path.lstrip("/")
        resolved = (root / path).resolve()
        try:
            resolved.relative_to(root.resolve())
        except ValueError:
            errors.append(f"README link escapes repository root: {target}")
            continue
        if target in checked:
            continue
        checked.add(target)
        if not resolved.exists():
            errors.append(f"README local link does not exist: {target}")

    return errors


def main() -> int:
    errors = validate()
    if errors:
        for error in errors:
            print(error, file=sys.stderr)
        return 1
    print("README local links and referenced assets are valid.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
