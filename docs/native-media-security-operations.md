# Native media dependency security operations

Director X's FFmpeg/FFmpegKitNext artifact is a pinned, locally built native dependency. Its security state must be maintained continuously; a date in the lock file is not a claim that the code becomes unsafe on that date or safe when the date is extended.

## Automated monitoring

- `.github/workflows/native-security-monitor.yml` runs weekly (Monday 06:17 UTC), on relevant changes to `Alpha`, and on demand.
- It queries OSV for every package URL in `third_party/ffmpeg-kit-next/native-lock.json`.
- HIGH/CRITICAL findings fail the monitor and are retained as a 90-day JSON artifact.
- The monitor also fails when the human security-review horizon has elapsed. It never edits or renews that date.
- Network/API failures fail closed rather than being interpreted as “no vulnerabilities.”

## Required human review

OSV coverage is incomplete, especially for generic/native builds. Before adopting or renewing a native artifact, a reviewer must also:

1. Review the FFmpeg upstream security page and FFmpegKitNext source/release changes.
2. Check relevant NVD, GitHub Security Advisory and vendor advisories for every shipped native component.
3. Map each applicable issue to the exact source revision, enabled configure options, reachable demuxers/decoders/encoders/protocols and shipped ABIs.
4. Backport only verified upstream fixes, or disable affected functionality with a documented compatibility decision.
5. Rebuild from pinned sources; verify AAR/POM identity, ABI contents, SHA-256, source offer and SBOM.
6. Run native and Android tests, plus media-format regression tests.
7. Update the lock's review date only together with the review evidence, reviewer, reviewed source revisions, advisory decisions and test results.

A clean automated scan is evidence of monitoring only. It is not a security approval. Never move `reviewUntil` forward merely to make CI green. Keep release gates blocking while review evidence is stale or a HIGH/CRITICAL issue is unresolved.
