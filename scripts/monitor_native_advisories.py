#!/usr/bin/env python3
"""Query OSV for every pinned native dependency; this is monitoring, not a security sign-off."""

from __future__ import annotations

import datetime as dt
import json
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LOCK = ROOT / "third_party/ffmpeg-kit-next/native-lock.json"
OUT = ROOT / "app/build/reports/native-security/osv-monitor.json"
API = "https://api.osv.dev/v1/query"


def query(purl: str) -> dict:
    body = json.dumps({"package": {"purl": purl}}).encode()
    request = urllib.request.Request(
        API, data=body, headers={"Content-Type": "application/json", "User-Agent": "Director-X-native-security-monitor"}
    )
    for attempt in range(4):
        try:
            with urllib.request.urlopen(request, timeout=30) as response:
                return json.load(response)
        except (urllib.error.URLError, TimeoutError):
            if attempt == 3:
                raise
            time.sleep(2 ** attempt)
    raise RuntimeError("OSV query retries exhausted")


def severity(vulnerability: dict) -> str:
    values = []
    database = vulnerability.get("database_specific", {})
    if isinstance(database, dict):
        values.append(str(database.get("severity", "")))
    for item in vulnerability.get("severity", []):
        if isinstance(item, dict):
            values.extend([str(item.get("score", "")), str(item.get("type", ""))])
    text = " ".join(values).upper()
    if "CRITICAL" in text:
        return "CRITICAL"
    if "HIGH" in text:
        return "HIGH"
    if "MEDIUM" in text or "MODERATE" in text:
        return "MEDIUM"
    if "LOW" in text:
        return "LOW"
    return "UNKNOWN"


def main() -> int:
    lock = json.loads(LOCK.read_text(encoding="utf-8"))
    results = []
    seen = set()
    review_until = dt.date.fromisoformat(lock["security"]["reviewUntil"])
    today = dt.datetime.now(dt.timezone.utc).date()
    review_expired = review_until < today
    for component in lock.get("components", []):
        purl = component.get("purl")
        if not purl or purl == "NOASSERTION":
            continue
        if purl in seen:
            continue
        seen.add(purl)
        response = query(purl)
        for vulnerability in response.get("vulns", []):
            results.append({
                "package": component.get("name"),
                "version": component.get("version"),
                "purl": purl,
                "id": vulnerability.get("id", "UNKNOWN"),
                "summary": vulnerability.get("summary", ""),
                "severity": severity(vulnerability),
                "modified": vulnerability.get("modified", ""),
                "references": [item.get("url") for item in vulnerability.get("references", []) if item.get("url")],
            })
    results.sort(key=lambda item: (item["severity"], item["package"], item["id"]))
    report = {
        "monitor": "OSV package URL queries",
        "is_security_approval": False,
        "component_count": len(seen),
        "security_review_until": review_until.isoformat(),
        "security_review_expired": review_expired,
        "finding_count": len(results),
        "findings": results,
        "limitations": [
            "OSV coverage varies by ecosystem and package; no findings do not prove absence of vulnerabilities.",
            "Review FFmpeg's upstream security page and vendor advisories as a separate mandatory step.",
            "Every finding requires human reachability, fixed-version and backport assessment before changing the native lock.",
        ],
    }
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))
    blockers = [item for item in results if item["severity"] in {"HIGH", "CRITICAL", "UNKNOWN"}]
    if blockers:
        print(f"::error::{len(blockers)} HIGH/CRITICAL/UNKNOWN OSV finding(s) require triage; unknown severity is not silently accepted")
    if review_expired:
        print(f"::error::Native security review expired on {review_until}; a documented human review is required")
    if blockers or review_expired:
        return 1
    print("OSV monitoring completed; this result is not a security approval.")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as error:
        print(f"::error::Native security monitoring failed closed: {error}", file=sys.stderr)
        raise
