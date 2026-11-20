# FFmpeg 8.1.3 Candidate Assessment — Director X

**Status: preferred 8.x candidate; native security approval BLOCKED pending source and binary verification.**

Date: 2026-11-20  
Alpha baseline: `9eeb3ebf3cb009bcee623e35b36b3d398e480110`  
Working branch: `work/ffmpeg-8.1.3-build-candidate`

## Decision

Plan A is FFmpeg 8.1.3 plus a verified mitigation for the `hqdn3d` issue and any advisory found after the release tag. Plan B is the current FFmpeg 8.1.2 source with individually verified 8.x backports. Do not ship an unmodified 8.1.3 artifact based only on its version string.

## Current shipped baseline — unchanged

- FFmpegKitNext 8.1.0 source commit: `3e223118e6e8fb6208693ecf3952e77cd096f587`
- FFmpeg 8.1.2 revision: `38b88335f99e76ed89ff3c93f877fdefce736c13`
- AAR: `third_party/ffmpeg-kit-next/ffmpeg-kit-next-8.1.0.aar`
- AAR SHA-256: `7971240aff84ce59a4ab28400bb4af59d24c20ce68c25525d41d910246ccff62`
- Existing review horizon: `2026-10-31` (expired)

The AAR, POM, hashes, lock and review dates are not changed by this assessment.

## Evidence for FFmpeg 8.1.3

FFmpeg's official download page identifies 8.1.3 as the 8.1 release published 2026-09-21.

The openSUSE FFmpeg 8.1.3 package changelog records removal of downstream backport patches after updating to 8.1.3. The removed patch set includes fixes for:
- CVE-2026-58049
- CVE-2026-64833 and CVE-2026-64834
- CVE-2026-65703, CVE-2026-65704, CVE-2026-65705 and CVE-2026-65706
- CVE-2026-66037
- CVE-2026-70628, CVE-2026-70629, CVE-2026-70630, CVE-2026-70631 and CVE-2026-70632
- CVE-2026-75141 through CVE-2026-75147

This is useful corroboration that those fixes are present in the FFmpeg 8.1.3 source used by that distributor. It is not a substitute for validating the exact official tag against the FFmpegKitNext checkout and the resulting Android binaries. Reconcile every advisory against upstream commits and the exact tag before marking it fixed.

## Special case: CVE-2026-66036 (`hqdn3d`)

The same openSUSE changelog says the main `hqdn3d` feature patch was not accepted upstream for 8.x, while a companion shim was merged. Therefore do not assume FFmpeg 8.1.3 fully resolves CVE-2026-66036 simply because it includes the merged shim.

A repository code search found no literal `hqdn3d` filter invocation. However, `FFmpegEngine.execute(command)` is a general command entry point, so this alone does not prove the filter unreachable.

Required decision:
1. Audit every call site and command construction path for caller-controlled filter strings.
2. If the filter is not a product requirement, disable `hqdn3d` at configure time and verify it is absent from the built filter registry.
3. If it is required, review the merged shim against the advisory and exact 8.1.3 source. Do not blindly transplant the rejected feature patch.
4. Keep the advisory unresolved until the chosen mitigation is built and tested.

## Plan A — FFmpeg 8.1.3

1. Keep the FFmpegKitNext wrapper pinned to commit `3e223118e6e8fb6208693ecf3952e77cd096f587`. Its `scripts/source.sh` pins FFmpeg with `SOURCE_ID="n8.1.2"`; the new `ffmpeg-source-tag-n8.1.3.patch` changes only that tag to `n8.1.3`.
2. The `arthenica/FFmpeg` `n8.1.3` annotated tag is signature-verified and dereferences to commit `1041abdc962f4cc4f394aa8de9dc5236c0c3b9e7`. The patch is applied only when the build script is invoked with `-FfmpegTag n8.1.3`; the default remains `n8.1.2` and the script reverses the candidate patch when returning to Plan B.
3. Reconcile all current advisories against that exact tag, including any disclosures after the tag date.
4. Resolve CVE-2026-66036 as described above; retain the existing Director X disabled-format profile unless a documented feature audit justifies changes.
5. Build the FFmpegKitNext wrapper and FFmpeg for all five configured ABIs using the pinned NDK r27d/API 24 recipe.
6. Inspect configure output, linked objects and AAR contents. Verify required Director X codecs/filters remain available.
7. Update AAR/POM, lock hashes, source provenance, patch digest, SBOM and advisory evidence atomically.
8. Run native malformed-input tests, FFmpegEngine regressions, Android unit tests, lint, instrumentation, supply-chain checks and 16 KB alignment verification.

## Plan B — patched FFmpeg 8.1.2

Use only if 8.1.3 has an unresolved integration/regression issue:
- Keep the pinned 8.1.2 baseline.
- Backport individually verified fixes that apply cleanly to the exact revision.
- Disable only affected components that Director X does not require, with a documented compatibility assessment.
- Rebuild and run the same five-ABI, native and Android test gates.

## Build and verification constraints

- `scripts/build_ffmpeg_kit_next.ps1` requires Windows + WSL and a local checkout of the exact FFmpegKitNext source commit.
- The build script now accepts `-FfmpegTag n8.1.2` (default/fallback) or `-FfmpegTag n8.1.3` (candidate). It retains the exact wrapper commit and applies/reverses a separate, one-line source-tag patch.
- CI now checks that the source-tag patch applies to the exact FFmpegKitNext commit, selects `n8.1.3`, and reverses cleanly back to `n8.1.2`.
- `scripts/verify_native_supply_chain.py` rejects the expired review and validates AAR/POM hashes and patch inventory. Do not weaken this gate or extend dates to bypass it.
- GitHub text-file operations cannot rebuild or attest the vendored native AAR.
- No replacement AAR/POM, configure transcript, five-ABI payload inventory or native regression results exist yet.

## Acceptance criteria

Do not merge a native artifact update to Alpha or build a release APK until:
- exact source/tag provenance is verified;
- all applicable HIGH/CRITICAL advisories are fixed or proven unreachable with build evidence;
- CVE-2026-66036 has a documented, tested mitigation;
- AAR/POM sizes and hashes match the updated lock;
- all five ABIs and required product features are present;
- native and Android regression/security gates pass;
- review dates are based on completed evidence, not manually extended.

## References

- FFmpeg release page: https://ffmpeg.org/download.html
- FFmpeg security page: https://ffmpeg.org/security.html
- openSUSE FFmpeg 8.1.3 changelog: https://fr2.rpmfind.net/linux/RPM/opensuse/tumbleweed/x86_64/ffmpeg-8-libavformat-devel-8.1.3-4.1.x86_64.html
- openSUSE FFmpeg 8.1.3 mini-libs changelog: https://www.rpmfind.net/linux/RPM/opensuse/tumbleweed/x86_64/ffmpeg-8-mini-libs-8.1.3-2.1.x86_64.html


## Verified source-pin implementation

- FFmpegKitNext source commit: `3e223118e6e8fb6208693ecf3952e77cd096f587`
- FFmpeg source repository used by that wrapper: `https://github.com/arthenica/FFmpeg`
- Signed FFmpeg tag: `n8.1.3`
- Tag target commit: `1041abdc962f4cc4f394aa8de9dc5236c0c3b9e7`
- Director X patch: `third_party/ffmpeg-kit-next/ffmpeg-source-tag-n8.1.3.patch`

The build driver preserves the default 8.1.2 behavior and accepts `-FfmpegTag n8.1.3` for Plan A. The reverse operation is deliberately included so a reused local checkout can return to 8.1.2 for Plan B. CI checks apply, selected source tag, reverse-apply and restored fallback tag. This verifies patch mechanics only; it does not build FFmpeg or attest an AAR.
