# FFmpeg 8.x Native Security Review — 2026-11

**Review state: BLOCKED — do not renew the shipped artifact's security attestation.**

Review date: 2026-11-20  
Repository baseline: `AssayFlux/Director-X` branch `Alpha`  
Baseline commit: `16344a866544c2e4a60e0ab5862a6b8dee2f8dd9`  
Native artifact: `third_party/ffmpeg-kit-next/ffmpeg-kit-next-8.1.0.aar`  
Pinned FFmpeg source revision: `38b88335f99e76ed89ff3c93f877fdefce736c13`  
Pinned FFmpeg version: `8.1.2`  
FFmpegKitNext source commit: `3e223118e6e8fb6208693ecf3952e77cd096f587`  
Pinned AAR SHA-256: `7971240aff84ce59a4ab28400bb4af59d24c20ce68c25525d41d910246ccff62`  
Pinned security patch SHA-256 (existing lock): `3a8dcc1aac92be38857dc89c6d6dcaed4457933855340c2fc1a948b42f2f0da7`  
Existing review horizon: `2026-10-31` (expired)

## Decision

The existing AAR cannot be given a renewed security review date. The pinned 8.1.2 source is affected by additional advisories disclosed after the prior lock inventory. Several affected components are not disabled by the currently shipped configure patch. No rebuilt AAR, configure transcript, per-ABI native verification, regression results, or new artifact hash has been produced in this review. Extending `reviewUntil` would therefore be an unsupported attestation.

This report intentionally does **not** change the lock's review dates, advisory statuses, AAR hash, or patch hash.

## Exact shipped profile

The lock records these disabled components:

- `vobsub_demuxer`
- `tdsc_decoder`
- `mace6_decoder`
- `adpcm_adx_decoder`
- `adx_demuxer`
- `aax_demuxer`
- `png_encoder`
- `apng_encoder`
- `rasc_decoder`
- `iamf_demuxer`
- `zlib_decoder`

The pinned build targets `armeabi-v7a`, `armeabi-v7a-neon`, `arm64-v8a`, `x86`, and `x86_64`, using NDK `27.3.13750724` and Android API 24. The build script in this repository expects the exact FFmpegKitNext source commit and applies `directorx-security.patch` before running the native build.

## Newly identified advisory exposure requiring remediation

| Advisory | Affected component / path | Current-profile assessment | Required action before renewal |
|---|---|---|---|
| CVE-2026-66036 | `hqdn3d` video filter | Not listed as disabled; 8.1.2 is identified as affected | Backport upstream fix commit `5d7112c60e6f0f0742ce47d448e6da0718a70f4c` to the pinned source and test it, or disable the filter if product usage permits |
| CVE-2026-65704 | TY demuxer and Shorten decoder | Neither appears in the disabled list | Backport upstream fix `de771bd52774a52d45b0e2c82e56995a1ef40df7` if applicable to the pinned branch, or disable both affected components |
| CVE-2026-70628 | DVB subtitle parser, reachable from crafted WTV input | Parser is not listed as disabled | Backport and validate the upstream fix; otherwise disable the affected parser and assess WTV/DVB subtitle compatibility |
| CVE-2026-70629 | RSCC decoder | Not listed as disabled | Backport and validate the upstream fix, or disable the RSCC decoder |
| CVE-2026-70631 | TIFF decoder | Not listed as disabled | Backport and validate the upstream fix, or disable the TIFF decoder |

These are actionable source-level findings, not claims that a specific exploit has been demonstrated against the Android app. Final reachability must be proven against the exact configure output and the actual native artifact. A format disabled only in source configuration but not reflected in the rebuilt AAR is not a mitigation.

## Previously inventoried advisories

The existing lock already disables components associated with CVE-2026-64830, CVE-2026-65703, CVE-2026-66039, CVE-2026-64835, CVE-2026-66040, CVE-2026-58049, CVE-2026-12706, CVE-2026-66037, and CVE-2026-66038. These decisions must be rechecked against the pinned source, actual configure transcript, and rebuilt binary. The CVE-2026-64832 NVDEC non-reachability rationale must likewise be verified against the generated configuration and linked objects.

FFmpeg's official security page lists CVE-2026-8461 and CVE-2026-30999 among the fixes in FFmpeg 8.1.2. The lock already records those advisories as resolved. That upstream release-note evidence does not establish that the additional advisories above are fixed in 8.1.2.

## Required completion gates

1. Obtain the exact FFmpegKitNext source commit and verify the source tree is clean before patching.
2. Backport the upstream fixes listed above where a clean, applicable 8.x backport exists. For fixes not safely backportable, disable only the affected component(s), after checking Director X feature use and documenting compatibility impact.
3. Update the security patch and native lock with a complete advisory inventory, exact upstream commit IDs, component mappings, reachability evidence, and rationale. Do not change review dates yet.
4. Build the native library from the pinned FFmpegKitNext commit using the documented NDK/build command.
5. Verify the generated AAR and POM, record their new sizes and SHA-256 hashes, inspect the configure transcript for all intended disabled/enabled components, and verify all five ABI payloads.
6. Run native malformed-input/regression tests for the remediated paths, app unit tests, lint, instrumentation tests, and native supply-chain/SBOM verification.
7. Only after all evidence is attached should a reviewer set a new review horizon and individual advisory review dates.

## Build limitation in this review

The repository's checked-in build driver, `scripts/build_ffmpeg_kit_next.ps1`, invokes Windows WSL and builds from a local source checkout. Repository inspection and advisory research alone cannot generate or attest a replacement 62 MB AAR. No native build runner, source checkout, configure transcript, or resulting ABI artifacts were available to this review session. Therefore the binary has not been changed and the review remains blocked.

## Sources consulted

- FFmpeg official security page: https://ffmpeg.org/security.html
- CVE-2026-66036 / upstream fix: https://code.ffmpeg.org/FFmpeg/FFmpeg/commit/5d7112c60e6f0f0742ce47d448e6da0718a70f4c
- CVE-2026-65704 / upstream fix: https://code.ffmpeg.org/FFmpeg/FFmpeg/commit/de771bd52774a52d45b0e2c82e56995a1ef40df7
- CVE-2026-70628: https://nvd.nist.gov/vuln/detail/CVE-2026-70628
- CVE-2026-70629: https://security-tracker.debian.org/tracker/CVE-2026-70629
- CVE-2026-70631: https://nvd.nist.gov/vuln/detail/CVE-2026-70631
