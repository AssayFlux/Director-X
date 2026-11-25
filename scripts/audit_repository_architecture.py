#!/usr/bin/env python3
"""Produce a reproducible, read-only source architecture audit report."""

from __future__ import annotations

import argparse
import json
import re
import subprocess
from pathlib import Path
from typing import Any

ENGINE_UI_IMPORT = re.compile(
    r"^\s*import\s+com\.directorx\.editor\.ui(?:\.|\s|$)"
)
MODEL_PLATFORM_IMPORT = re.compile(
    r"^\s*import\s+(?:android\.|androidx\.(?:compose|room\d*))"
)
GLOBAL_SCOPE = re.compile(r"\bGlobalScope\b")
TODO_MARKER = re.compile(r"\b(?:TODO|FIXME|XXX)\b")
LARGE_FILE_LINES = 1_000


def _relative(path: Path, root: Path) -> str:
    return path.relative_to(root).as_posix()


def audit_source(source_root: Path, revision: str) -> dict[str, Any]:
    source_root = source_root.resolve()
    app_root = source_root / "app"
    main_root = app_root / "src/main/java"
    test_root = app_root / "src/test/java"
    engine_root = main_root / "com/directorx/editor/engine"
    model_root = main_root / "com/directorx/editor/model"

    if not main_root.is_dir():
        raise ValueError(f"Android Kotlin source root not found: {main_root}")

    kotlin_files = sorted(main_root.rglob("*.kt"))
    test_files = sorted(test_root.rglob("*.kt")) if test_root.is_dir() else []
    engine_files = sorted(engine_root.rglob("*.kt")) if engine_root.is_dir() else []

    engine_ui_imports: list[dict[str, Any]] = []
    model_platform_imports: list[dict[str, Any]] = []
    global_scope_uses: list[dict[str, Any]] = []
    todo_markers: list[dict[str, Any]] = []
    file_sizes: list[dict[str, Any]] = []

    for path in kotlin_files:
        lines = path.read_text(encoding="utf-8", errors="replace").splitlines()
        file_sizes.append({
            "path": _relative(path, source_root),
            "lines": len(lines),
            "bytes": path.stat().st_size,
        })
        for line_number, line in enumerate(lines, start=1):
            if GLOBAL_SCOPE.search(line):
                global_scope_uses.append({
                    "path": _relative(path, source_root),
                    "line": line_number,
                    "text": line.strip(),
                })
            if TODO_MARKER.search(line):
                todo_markers.append({
                    "path": _relative(path, source_root),
                    "line": line_number,
                    "text": line.strip()[:240],
                })

    if model_root.is_dir():
        for path in sorted(model_root.rglob("*.kt")):
            lines = path.read_text(encoding="utf-8", errors="replace").splitlines()
            for line_number, line in enumerate(lines, start=1):
                if MODEL_PLATFORM_IMPORT.search(line):
                    model_platform_imports.append({
                        "path": _relative(path, source_root),
                        "line": line_number,
                        "text": line.strip(),
                    })

    for path in engine_files:
        lines = path.read_text(encoding="utf-8", errors="replace").splitlines()
        for line_number, line in enumerate(lines, start=1):
            if ENGINE_UI_IMPORT.search(line):
                engine_ui_imports.append({
                    "path": _relative(path, source_root),
                    "line": line_number,
                    "text": line.strip(),
                })

    largest = sorted(file_sizes, key=lambda item: (-item["lines"], item["path"]))[:20]
    oversized = [item for item in file_sizes if item["lines"] >= LARGE_FILE_LINES]

    findings: list[dict[str, Any]] = []
    if engine_ui_imports:
        findings.append({
            "id": "engine-ui-dependency",
            "severity": "high",
            "count": len(engine_ui_imports),
            "detail": "Engine package imports UI-layer types; invert the dependency or move UI-specific code.",
            "occurrences": engine_ui_imports,
        })
    if model_platform_imports:
        findings.append({
            "id": "model-platform-coupling",
            "severity": "medium",
            "count": len(model_platform_imports),
            "detail": "Model package imports Android, Compose, or Room APIs; separate persistence/platform adapters where compatibility permits.",
            "occurrences": model_platform_imports,
        })
    if global_scope_uses:
        findings.append({
            "id": "unowned-global-coroutines",
            "severity": "high",
            "count": len(global_scope_uses),
            "detail": "GlobalScope bypasses lifecycle ownership.",
            "occurrences": global_scope_uses,
        })
    if oversized:
        findings.append({
            "id": "large-source-concentration",
            "severity": "medium",
            "count": len(oversized),
            "detail": f"{len(oversized)} Kotlin source files contain at least {LARGE_FILE_LINES} lines.",
            "occurrences": sorted(oversized, key=lambda item: (-item["lines"], item["path"])),
        })

    return {
        "schemaVersion": 1,
        "revision": revision,
        "scope": "app/src/main/java and app/src/test/java Kotlin source",
        "summary": {
            "productionKotlinFiles": len(kotlin_files),
            "testKotlinFiles": len(test_files),
            "engineKotlinFiles": len(engine_files),
            "largeFilesAtLeast1000Lines": len(oversized),
            "engineUiImports": len(engine_ui_imports),
            "modelPlatformImports": len(model_platform_imports),
            "globalScopeOccurrences": len(global_scope_uses),
            "todoMarkers": len(todo_markers),
            "findings": len(findings),
        },
        "findings": findings,
        "largestFiles": largest,
        "todoMarkers": todo_markers,
    }


def render_markdown(report: dict[str, Any]) -> str:
    summary = report["summary"]
    lines = [
        "# Director X repository source audit",
        "",
        f"- **Audited revision:** `{report['revision']}`",
        f"- **Scope:** {report['scope']}",
        "- **Method:** deterministic read-only inventory and dependency-pattern scan; no source files modified.",
        "",
        "## Summary",
        "",
        "| Metric | Count |",
        "|---|---:|",
    ]
    for key, value in summary.items():
        lines.append(f"| {key} | {value} |")
    lines.extend(["", "## Findings", ""])
    if not report["findings"]:
        lines.append("No findings detected by the configured checks.")
    for finding in report["findings"]:
        lines.append(f"### {finding['id']} — {finding['severity']}")
        lines.append(f"{finding['detail']} (occurrences: {finding['count']})")
        lines.append("")
        for occurrence in finding.get("occurrences", [])[:50]:
            location = f"{occurrence['path']}:{occurrence.get('line', 1)}"
            lines.append(f"- `{location}`")
        lines.append("")
    lines.extend(["## Largest source files", "", "| File | Lines | Bytes |", "|---|---:|---:|"])
    for item in report["largestFiles"]:
        lines.append(f"| `{item['path']}` | {item['lines']} | {item['bytes']} |")
    lines.extend([
        "",
        "## Interpretation",
        "",
        "This report is a source-level audit snapshot, not a claim that the application builds, passes tests, or is device-accepted. Findings require engineering review and remediation.",
        "",
    ])
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--revision", default=None)
    parser.add_argument("--output-dir", type=Path, required=True)
    args = parser.parse_args()

    revision = args.revision
    if revision is None:
        revision = subprocess.check_output(
            ["git", "rev-parse", "HEAD"], cwd=args.source_root, text=True
        ).strip()
    report = audit_source(args.source_root, revision)
    args.output_dir.mkdir(parents=True, exist_ok=True)
    (args.output_dir / "repository-audit.json").write_text(
        json.dumps(report, indent=2) + "\n", encoding="utf-8"
    )
    (args.output_dir / "repository-audit.md").write_text(
        render_markdown(report), encoding="utf-8"
    )
    print(render_markdown(report))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
