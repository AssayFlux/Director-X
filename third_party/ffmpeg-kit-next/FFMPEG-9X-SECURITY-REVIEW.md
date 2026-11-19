# FFmpeg 9.x native security review worksheet

## Candidate source

- FFmpegKitNext wrapper: 9.0.2, source commit 9f04bf1e3c97def70b98e198cb80a57b43ef5b45
- FFmpeg source repository: https://github.com/FFmpeg/FFmpeg
- Exact FFmpeg source commit: 2473872b7ceadbdf3235c0a482f58753e3f6dacb
- Source date: 2026-11-17
- This is an exact upstream master commit in the FFmpeg 9.x development line, not the FFmpeg 9.0.2 release tag.
- The source commit is pinned; the build helper fails closed if the checkout does not match it.

## Advisory triage

The following 29 direct FFmpeg CPE candidates were returned by NVD for the previous 8.1.2 native lock. This table records the upstream fix or build-profile mitigation present in the candidate source. It is a source-level triage record, not a security approval.

| Advisory | Candidate disposition | Upstream fix / evidence |
|---|---|---|
| CVE-2026-65703 | TDSC decoder disabled in the Director X profile | Existing security patch disables tdsc_decoder |
| CVE-2026-65704 | Fixed | de771bd52774a52d45b0e2c82e56995a1ef40df7 |
| CVE-2026-65705 | Fixed | 24c322fdb232 |
| CVE-2026-65706 | Fixed | a7e38b617b32 |
| CVE-2026-66036 | Fixed | 5d7112c60e |
| CVE-2026-66037 | IAMF demuxer disabled in the Director X profile | Existing security patch disables iamf_demuxer |
| CVE-2026-66038 | Fixed | 8670835 |
| CVE-2026-66039 | MACE6 decoder disabled in the Director X profile | Existing security patch disables mace6_decoder |
| CVE-2026-66040 | PNG and APNG encoders disabled in the Director X profile | Existing security patch disables png_encoder and apng_encoder |
| CVE-2026-66041 | Fixed | 4da9812e2589 |
| CVE-2026-70628 | Fixed | 93f2a525ec6c |
| CVE-2026-70629 | Fixed | cd1f545cf27b |
| CVE-2026-70630 | Fixed | 705890061467 |
| CVE-2026-70631 | Fixed | 2f234ea34c81 |
| CVE-2026-70632 | Fixed | a3b487844381 |
| CVE-2026-52295 | Fixed | 8439e0203744 |
| CVE-2026-52296 | Fixed | 23227a444de4 |
| CVE-2026-52297 | Fixed | 8439e0203744 |
| CVE-2026-96611 | Fixed | 2cc7b87bdb75 |
| CVE-2026-30754 | Fixed | d03483bd265b |
| CVE-2026-107660 | Fixed | 57a2e704b4a6 |
| CVE-2026-107675 | SFTP/libssh path is not intended to be built: libssh is not an enabled external dependency and autodetection is disabled. Confirm the generated configure summary has no libssh or SFTP support before release. | Build-profile reachability check still pending |
| CVE-2026-107676 | Fixed | 2c2f6e96e317 |
| CVE-2026-107677 | Fixed | ed27bfcbbbc0 |
| CVE-2026-107678 | Fixed | 969c7eb09198, d1434518fbd0, 71403a21b02a |
| CVE-2026-107695 | Fixed | c364ab176f72 |
| CVE-2026-107696 | Fixed | f5d4fd186c68 |
| CVE-2026-107697 | Fixed | 23602df9cd1b |
| CVE-2026-107698 | Not affected by the published affected-version ranges (before 7.1.4 and 8.0.x before 8.0.2); current RTSP code also validates redirect URLs through ff_format_check_set_url. | Version applicability and source check |

The exact FFmpeg source pin is a descendant of the listed upstream fix commits. The native build helper also checks selected source-level markers before compilation.

## Release gates still required

1. Complete the native build for every configured Android ABI.
2. Inspect the generated configure summary and prove libssh/SFTP and unneeded TLS backends are absent.
3. Generate and verify the AAR, POM, ABI contents, checksums, and reproducible source offer.
4. Run targeted regression tests for the fixes above, including MOV PSSH, HLS, RTSP redirects, DASH empty templates, codec/filter bounds, and disabled-component checks.
5. Pass Android unit tests and media import/export regression tests.
6. Update the native lock only with the generated artifact hashes and a completed review record.
7. Obtain explicit security reviewer sign-off before setting a new review horizon.

## Review decision

- Review status: PENDING
- Reviewer: PENDING
- Review date: PENDING
- Generated AAR SHA-256: PENDING
- Configure reachability evidence: PENDING
- Targeted regression evidence: PENDING
- Android/media regression result: PENDING
- Release approval: NOT APPROVED

This worksheet does not extend the expired 2026-10-31 review date and does not authorize replacing the currently pinned production artifact.
