#!/usr/bin/env python3
"""Monitor pinned native dependencies via OSV, NVD and FFmpeg's upstream security page.

This is an advisory-discovery gate, never an automatic security approval. New or
unclassified advisories require a human reachability/fix review before lock changes.
"""
from __future__ import annotations

import datetime as dt
import json
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LOCK = ROOT / "third_party/ffmpeg-kit-next/native-lock.json"
OUT = ROOT / "app/build/reports/native-security/advisory-monitor.json"
OSV_API = "https://api.osv.dev/v1/query"
NVD_API = "https://services.nvd.nist.gov/rest/json/cves/2.0"
FFMPEG_SECURITY = "https://ffmpeg.org/security.html"
USER_AGENT = "Director-X-native-security-monitor/2.0"


def get_json(url: str, body: dict | None = None) -> dict:
    data = json.dumps(body).encode() if body is not None else None
    headers = {"User-Agent": USER_AGENT, "Accept": "application/json"}
    if data is not None:
        headers["Content-Type"] = "application/json"
    request = urllib.request.Request(url, data=data, headers=headers)
    for attempt in range(4):
        try:
            with urllib.request.urlopen(request, timeout=45) as response:
                return json.load(response)
        except (urllib.error.URLError, TimeoutError, json.JSONDecodeError):
            if attempt == 3:
                raise
            time.sleep(2 ** attempt)
    raise RuntimeError("network retries exhausted")


def get_text(url: str) -> str:
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    for attempt in range(4):
        try:
            with urllib.request.urlopen(request, timeout=45) as response:
                return response.read().decode("utf-8", errors="replace")
        except (urllib.error.URLError, TimeoutError):
            if attempt == 3:
                raise
            time.sleep(2 ** attempt)
    raise RuntimeError("network retries exhausted")


def severity(vulnerability: dict) -> str:
    values = []
    database = vulnerability.get("database_specific", {})
    if isinstance(database, dict):
        values.append(str(database.get("severity", "")))
    for item in vulnerability.get("severity", []):
        if isinstance(item, dict):
            values.append(str(item.get("score", "")))
            values.append(str(item.get("type", "")))
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
    components = lock.get("components", [])
    reviewed = {item.get("id") for item in lock.get("advisories", [])}
    today = dt.datetime.now(dt.timezone.utc).date()
    review_until = dt.date.fromisoformat(lock["security"]["reviewUntil"])
    osv_findings = []
    seen = set()
    for component in components:
        purl = component.get("purl")
        if not purl or purl in seen:
            continue
        seen.add(purl)
        result = get_json(OSV_API, {"package": {"purl": purl}})
        for item in result.get("vulns", []):
            osv_findings.append({
                "package": component.get("name"),
                "version": component.get("version"),
                "id": item.get("id", "UNKNOWN"),
                "severity": severity(item),
                "summary": item.get("summary", ""),
                "references": [x.get("url") for x in item.get("references", []) if x.get("url")],
            })

    # Query a bounded recent NVD window; keep API use to one request per run.
    end = dt.datetime.now(dt.timezone.utc).replace(microsecond=0)
    start = end - dt.timedelta(days=120)
    params = urllib.parse.urlencode({
        "keywordSearch": "FFmpeg",
        "pubStartDate": start.isoformat(timespec="milliseconds").replace("+00:00", "Z"),
        "pubEndDate": end.isoformat(timespec="milliseconds").replace("+00:00", "Z"),
        "resultsPerPage": "2000",
    })
    nvd = get_json(NVD_API + "?" + params)
    nvd_findings = []
    for item in nvd.get("vulnerabilities", []):
        cve = item.get("cve", {})
        metrics = cve.get("metrics", {})
        scores = []
        for key in ("cvssMetricV40", "cvssMetricV31", "cvssMetricV30", "cvssMetricV2"):
            for metric in metrics.get(key, []):
                data = metric.get("cvssData", {})
                scores.append(data.get("baseSeverity", ""))
        nvd_findings.append({
            "id": cve.get("id", "UNKNOWN"),
            "severity": next((x.upper() for x in scores if x), "UNKNOWN"),
            "published": cve.get("published", ""),
            "description": next((x.get("value", "") for x in cve.get("descriptions", []) if x.get("lang") == "en"), ""),
            "url": "https://nvd.nist.gov/vuln/detail/" + cve.get("id", ""),
        })

    upstream_html = get_text(FFMPEG_SECURITY)
    upstream_ids = sorted(set(re.findall(r"CVE-\d{4}-\d{4,}", upstream_html)))
    all_ids = set(reviewed)
    all_ids.update(item["id"] for item in osv_findings)
    all_ids.update(item["id"] for item in nvd_findings)
    all_ids.update(upstream_ids)
    candidate_ids = {item["id"] for item in osv_findings + nvd_findings}
    unreviewed = sorted(item for item in candidate_ids if item not in reviewed)
    high_unknown = [
        item for item in osv_findings + nvd_findings
        if item["severity"] in {"HIGH", "CRITICAL", "UNKNOWN"} and item["id"] not in reviewed
    ]
    report = {
        "generated_at": end.isoformat(),
        "is_security_approval": False,
        "component_count": len(seen),
        "sources": {
            "osv": OSV_API,
            "nvd": NVD_API,
            "ffmpeg_upstream": FFMPEG_SECURITY,
            "nvd_window_start": start.isoformat(),
            "nvd_window_end": end.isoformat(),
        },
        "review_until": review_until.isoformat(),
        "review_expired": review_until < today,
        "osv_findings": osv_findings,
        "nvd_findings": nvd_findings,
        "ffmpeg_upstream_cve_ids": upstream_ids,
        "unreviewed_advisory_ids": unreviewed,
        "limitations": [
            "Advisory feeds can be incomplete, delayed, duplicated or incorrectly mapped.",
            "Keyword matches are candidates, not proof that a CVE affects the pinned build.",
            "A human must verify upstream fixes/backports, affected code paths and exact Android build reachability.",
            "No findings never means the native library is vulnerability-free.",
            "This workflow cannot extend review dates or approve an artifact.",
        ],
    }
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))
    if report["review_expired"]:
        print(f"::error::Native security review expired on {review_until}; human review required")
    if unreviewed:
        print("::error::Unreviewed advisory candidates: " + ", ".join(unreviewed))
    if high_unknown:
        print(f"::error::{len(high_unknown)} unreviewed HIGH/CRITICAL/UNKNOWN finding(s) require triage")
    return 1 if report["review_expired"] or unreviewed or high_unknown else 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as error:
        print(f"::error::Native advisory monitoring failed closed: {error}", file=sys.stderr)
        raise
