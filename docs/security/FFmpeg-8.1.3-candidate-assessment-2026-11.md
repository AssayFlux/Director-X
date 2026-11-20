# FFmpeg 8.1.3 Candidate Assessment — Director X

**Status: candidate selected for engineering validation; native security approval remains BLOCKED.**

Date: 2026-11-20  
Base branch: `Alpha`  
Base commit: `16344a866544c2e4a60e0ab5862a6b8dee2f8dd9`  
Working branch: `work/ffmpeg-8x-remediation-2026-11`

## Decision

Plan A remains FFmpeg 8.1.3, but **not an unmodified 8.1.3 build**. Use the 8.1.3 release source and verify its included fixes, then add a narrowly scoped mitigation for the outstanding `hqdn3d` issue if the exact source and app feature audit confirm it is needed. Plan B remains the existing 8.1.2 source with individually verified 8.x backports.

Do not update the shipped AAR, POM, lock hashes, build timestamp, or review horizon until a real native build has completed and its outputs have been verified. Do not change `reviewUntil` to bypass the expired-review gate.

## Current shipped baseline (unchanged)

- FFmpegKitNext 8.1.0, source commit `3e223118e6e8fb6208693ecf3952e77cd096f587`
- FFmpeg 8.1.2, source revision `38b88335f99e76ed89ff3c93f877fdefce736c13`
- AAR: `third_party/ffmpeg-kit-next/ffmpeg-kit-next-8.1.0.aar`
- AAR SHA-256: `7971240aff84ce59a4ab28400bb4af59d24c20ce68c25525d41d910246ccff62`
- Existing security review horizon: `2026-10-31` (expired)
- Existing profile disables 11 named components; it does not disable `hqdn3d`, TY demuxing, Shorten, DVB subtitle parsing, RSCC, or TIFF.
- The shipped binary and Alpha branch are not changed by this assessment.

## 8.1.3 source evidence

FFmpeg's official download page identifies 8.1.3 as the latest 8.1 release, published 2026-09-21. The version changes library ABI minor versions within the 8.x family; this still requires rebuilding the FFmpegKitNext wrapper and checking JNI/native compatibility rather than swapping the FFmpeg version string.

The openSUSE FFmpeg 8.1.3 package changelog records that the 8.1.3 source update allowed removal of downstream patches for CVE-2026-58049, CVE-2026-64833, CVE-2026-64834, CVE-2026-65703/04/05/06, CVE-2026-66037, CVE-2026-70628/29/30/31/32, and CVE-2026-75141 through CVE-2026-75147. This is useful corroborating evidence that these fixes are present in the 8.1.3 source used by that distributor. It is not a substitute for comparing the exact FFmpeg 8.1.3 tag against the FFmpegKitNext build checkout and verifying the resulting Android binary.

### Special case: CVE-2026-66036 (`hqdn3d`)

The same openSUSE changelog explicitly says the main `hqdn3d` feature patch was not accepted upstream for 8.x, while a companion shim was merged. Therefore 8.1.3 must **not** be assumed to fully resolve CVE-2026-66036 solely because it contains the merged shim.

The Director X repository code search did not find an explicit `hqdn3d` filter invocation. However, `FFmpegEngine.execute(command)` is a general command entry point, so absence of a literal call in current source is not sufficient proof that the filter is unreachable. Before building Plan A:
1. Audit all call sites and command construction for user-controlled filter strings.
2. If the filter is not a product requirement, disable `hqdn3d` at configure time and verify it is absent from the built filter registry.
3. If it is required, review the merged shim against the advisory and exact 8.1.3 source; do not invent or blindly transplant the rejected feature patch. A compatible fix must be sourced, reviewed and regression-tested.
4. Keep CVE-2026-66036 unresolved until that evidence exists.

## Plan A — FFmpeg 8.1.3

1. Obtain the exact FFmpegKitNext source and determine how its build recipe pins/downloads FFmpeg. The current repository script hard-pins FFmpegKitNext commit `3e223118e6e8fb6208693ecf3952e77cd096f587`; changing the lock alone does not update the source built by that script.
2. Pin the official FFmpeg 8.1.3 tag and verify its tag/signature and source revision.
3. Reconcile the full advisory inventory against the exact tag, including post-8.1.3 advisories and the `hqdn3d` exception above.
4. Retain the current Director X disabled-format profile unless feature-use analysis supports a narrower/different profile. Add only justified mitigations.
5. Build all five configured ABIs with the documented NDK r27d/API 24 recipe.
6. Inspect configure output and linked objects; verify the AAR contains the expected five ABI payloads and that required codecs/filters used by Director X remain available.
7. Update AAR/POM, artifact paths, checksums, source revision, patch digest, SBOM and advisory evidence atomically.
8. Run native malformed-input tests, FFmpegEngine regression tests, Gradle unit tests, lint, instrumentation tests, and the 16 KB alignment/supply-chain checks.

## Plan B — patched FFmpeg 8.1.2

Use only if 8.1.3 has an unresolved regression or cannot be integrated safely:
- Keep the pinned 8.1.2 source baseline.
- Backport individually verified fixes from upstream/distributor patches that apply cleanly to the exact revision.
- For fixes that cannot be safely backported, disable only the affected component after confirming Director X does not depend on it.
- Rebuild and run the same five-ABI and regression gates. A patch list or a renewed date is not a substitute for a rebuilt binary.

## Repository-specific build and verification blockers

- `scripts/build_ffmpeg_kit_next.ps1` requires Windows + WSL and a local checkout of the exact FFmpegKitNext source commit.
- The current checked-in script rejects a different FFmpegKitNext commit. The wrapper/source pin and build recipe must be deliberately updated together for 8.1.3.
- `scripts/verify_native_supply_chain.py` correctly rejects the expired review and validates the current AAR/POM hashes and patch inventory. Do not weaken or bypass these checks.
- The current vendored AAR is a binary artifact; GitHub text-file operations cannot rebuild or attest it.
- No replacement AAR, POM, configure transcript, per-ABI payload inventory, or native regression output is available yet.

## Acceptance criteria

No merge to Alpha and no APK release until all are true:
- Exact source/tag provenance is verified.
- All applicable HIGH/CRITICAL advisories are fixed or proven unreachable with build evidence.
- The `hqdn3d` decision is documented and tested.
- AAR and POM hashes/sizes match the updated lock.
- All five ABIs and required product features are present.
- Native and Android regression/security gates pass.
- Review dates are set by evidence-based review, not manually extended to silence CI.

## References

- FFmpeg 8.1.3 release: https://ffmpeg.org/download.html
- FFmpeg security page: https://ffmpeg.org/security.html
- openSUSE FFmpeg 8.1.3 package changelog and patch reconciliation: https://fr2.rpmfind.net/linux/RPM/opensuse/tumbleweed/x86_64/ffmpeg-8-libavformat-devel-8.1.3-4.1.x86_64.html
- openSUSE FFmpeg 8.1.3 mini-libs changelog: https://www.rpmfind.net/linux/RPM/opensuse/tumbleweed/x86_64/ffmpeg-8-mini-libs-8.1.3-2.1.x86_64.html
