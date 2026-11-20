# FFmpeg 8.1.3 Candidate Assessment — Director X

**Status: preferred 8.x candidate; native security approval BLOCKED pending source and binary verification.**

Date: 2026-11-20  
Alpha baseline: `e243c6e833796cc24203b7ed64f55085aaa5730a`  
Status update: PR #119 merged into Alpha; source/patch CI checks passed on 2026-11-20.

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

## CVE-2026-66036 (`hqdn3d`) — source-level mitigation present in 8.1.3

The main upstream change `5d7112c60e6f0f0742ce47d448e6da0718a70f4c` supports dynamic frame sizes. The 8.x release branch did not take that feature patch, but the companion safety shim `e3d0c719fddd259709c8e275942ab56af3afc35d` was merged.

Direct inspection of `libavfilter/vf_hqdn3d.c` at the signed `n8.1.3` tag confirms:
- `config_input()` records the configured pixel format, width and height in the filter context.
- `filter_frame()` checks incoming frame format and dimensions against those recorded values before calling the denoiser.
- If a frame changes dimensions/format without filter-graph reinitialization, the filter logs an error, frees the frame and returns `AVERROR(EINVAL)` rather than processing it with undersized buffers.

That is the required fail-closed mitigation for the reported out-of-bounds write when reinitialization is disabled. **Do not disable `hqdn3d` by default**; retain the filter and add a native regression test using a variable-resolution input with `-reinit_filter 0`, confirming a clean error and no crash or sanitizer finding. This is source-level evidence only until the built Android AAR is tested.

## Post-8.1.3 advisory sweep — 2026-11-20

The 8.1.3 release fixes several advisories disclosed in November whose affected-version ranges end before 8.1.3, including CVE-2026-125174 (HAP decoder), CVE-2026-125209 (DVD subtitle encoder), CVE-2026-125228 (Vorbis encoder), CVE-2026-125244 (VVC decoder) and CVE-2026-125267 (APAC decoder). These version-range claims still need to be reconciled with the exact source tree and build configuration.

Three additional findings have a concrete mitigation in the Director X candidate profile:

| Advisory | Affected path | Candidate mitigation | Status |
|---|---|---|---|
| CVE-2026-125213 | PSD decoder; heap buffer overflow on 32-bit builds | Backport upstream guard rejecting `uncompressed_size > INT_MAX` before allocation/decode | Backport patch and wrapper hook merged; CI apply/reverse passed on both n8.1.2 and n8.1.3 (run 44651701118) |
| CVE-2026-125197 | LCL/ZLIB multithread decoder path | Existing profile disables the ZLIB decoder (`--disable-decoder=zlib`) | Must verify in configure transcript and final AAR |
| CVE-2026-125218 | APNG encoder | Existing profile disables the APNG encoder (`--disable-encoder=apng`) | Must verify in configure transcript and final AAR |

The PSD source at both n8.1.2 and n8.1.3 contains the vulnerable size calculation without the `INT_MAX` guard. The upstream fix is commit `aa4b80b54ee8645b5a77fe835ab2bbae1c9c465c`. Director X now carries that small backport in `ffmpeg-psd-cve-2026-125213.patch`, plus a wrapper hook that applies it after the FFmpeg source checkout. The build script applies this patch for both Plan A and Plan B. CI checks apply/reverse against both exact FFmpeg tags and checks the hook against the pinned FFmpegKitNext commit; these checks passed on Alpha after PR #119.

This is not yet a complete advisory inventory. Debian's FFmpeg tracker and current CVE records list many further 2026 advisories; each must be mapped to the exact 8.1.3 source, build-enabled components and Director X media surface before any security review can be renewed. Do not infer that the list above is exhaustive.

## Plan A — FFmpeg 8.1.3

1. Keep the FFmpegKitNext wrapper pinned to commit `3e223118e6e8fb6208693ecf3952e77cd096f587`. Its `scripts/source.sh` pins FFmpeg with `SOURCE_ID="n8.1.2"`; the new `ffmpeg-source-tag-n8.1.3.patch` changes only that tag to `n8.1.3`.
2. The `arthenica/FFmpeg` `n8.1.3` annotated tag is signature-verified and dereferences to commit `1041abdc962f4cc4f394aa8de9dc5236c0c3b9e7`. The patch is applied only when the build script is invoked with `-FfmpegTag n8.1.3`; the default remains `n8.1.2` and the script reverses the candidate patch when returning to Plan B.
3. Reconcile all current advisories against that exact tag, including post-release CVEs; the 2026-11-20 sweep found CVE-2026-125213 requiring a PSD decoder backport, while the existing zlib/APNG disablements cover CVE-2026-125197 and CVE-2026-125218 if confirmed in the built configuration.
4. Preserve `hqdn3d` with the 8.1.3 merged fail-closed guard and add the variable-resolution malformed-input regression test described above; retain the existing Director X disabled-format profile.
5. Build the FFmpegKitNext wrapper and FFmpeg for all five configured ABIs using the pinned NDK r27d/API 24 recipe. The build driver applies the PSD backport for both 8.1.2 and 8.1.3, and removes a stale source checkout when switching tags.
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
- The build script now accepts `-FfmpegTag n8.1.2` (default/fallback) or `-FfmpegTag n8.1.3` (candidate). It retains the exact wrapper commit, applies/reverses the source-tag patch, and discards a stale FFmpeg source checkout when switching tags.
- The build script applies a separate PSD decoder backport for both versions through a pinned hook in `scripts/android/ffmpeg.sh`.
- CI checks the wrapper hook against the exact FFmpegKitNext commit and the PSD backport against both FFmpeg tags, including reverse-apply; this source-level validation passed after PR #119 merged.
- `scripts/verify_native_supply_chain.py` rejects the expired review and validates AAR/POM hashes and patch inventory. Do not weaken this gate or extend dates to bypass it.
- GitHub text-file operations cannot rebuild or attest the vendored native AAR.
- No replacement AAR/POM, configure transcript, five-ABI payload inventory or native regression results exist yet.

## Acceptance criteria

Do not merge a native artifact update to Alpha or build a release APK until:
- exact source/tag provenance is verified;
- all applicable HIGH/CRITICAL advisories are fixed or proven unreachable with build evidence;
- the `hqdn3d` guard passes the variable-resolution malformed-input regression test;
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

Source evidence for the `hqdn3d` guard: `https://github.com/arthenica/FFmpeg/blob/n8.1.3/libavfilter/vf_hqdn3d.c` and merged shim commit `https://github.com/arthenica/FFmpeg/commit/e3d0c719fddd259709c8e275942ab56af3afc35d`.


Additional advisory sources: https://nvd.nist.gov/vuln/detail/CVE-2026-125213 ; https://nvd.nist.gov/vuln/detail/CVE-2026-125209 ; https://nvd.nist.gov/vuln/detail/CVE-2026-125228 ; https://nvd.nist.gov/vuln/detail/CVE-2026-125244 ; https://nvd.nist.gov/vuln/detail/CVE-2026-125267 ; https://security-tracker.debian.org/tracker/source-package/ffmpeg .
