# FFmpeg native security review — 2026-11

## Status: NOT RENEWED — native rebuild and evidence required

The vendored FFmpegKitNext AAR remains the artifact built from FFmpeg 8.1.2 at commit
`38b88335f99e76ed89ff3c93f877fdefce736c13`, with the original security profile and
review horizon ending 2026-10-31. The artifact is not replaced by changes to the
source build recipe. Its provenance lock must continue to describe that exact
shipped artifact until a replacement AAR has been built, hashed, and verified.

## Findings

- **CVE-2026-66036 (HIGH):** affects the `hqdn3d` video filter. The pinned FFmpeg
  configure declares this filter GPL-only (`hqdn3d_filter_deps="gpl"`) and the
  pinned build command does not enable GPL. The updated build profile also
  explicitly disables the filter as defense in depth.
- **CVE-2026-66038:** affects the LCL/ZLIB decoder. In the pinned FFmpeg 8.1.2
  Makefile, `CONFIG_ZLIB_DECODER` compiles `lcldec.o`; the existing
  `--disable-decoder=zlib` exclusion therefore targets the affected decoder.
- **CVE-2026-70629:** affects the RSCC decoder. The source build profile now
  disables `rscc`; this is not yet evidence that the currently vendored AAR
  excludes it.
- **CVE-2026-70631:** affects the TIFF decoder. The source build profile now
  disables `tiff`; this is not yet evidence that the currently vendored AAR
  excludes it.
- **CVE-2026-12706:** the RASC decoder is already disabled by the existing profile.
- **CVE-2026-30997:** fixed before the pinned 8.1.2 revision (the FFmpeg fix is
  present in the 8.1.1 line).
- **CVE-2026-30998:** concerns the standalone `tools/zmqsend.c` utility rather
  than the Android libraries; confirm the final AAR contains no such utility
  before recording a definitive not-affected decision.
- **CVE-2026-30754:** affects FFmpeg versions before 8.1; the pinned source is
  8.1.2 and is outside the stated affected range.

## Source-profile hardening prepared

The checked-in `directorx-security.patch` now adds these configure exclusions:

- `--disable-filter=hqdn3d`
- `--disable-decoder=rscc`
- `--disable-decoder=tiff`

The native verifier now requires these flags and includes the corresponding
advisories in its required inventory. This intentionally makes the old artifact
fail the provenance/security gate: the old AAR was not built with these new
exclusions.

## Required completion steps

1. Rebuild FFmpegKitNext from the pinned source using
   `scripts/build_ffmpeg_kit_next.ps1` with the updated security patch.
2. Verify the configure transcript actually excludes the three components for
   every shipped ABI; inspect the resulting AAR contents and native symbols.
3. Run native decoder/filter regression tests and the full Android verification
   workflow against the rebuilt AAR.
4. Record the new AAR and POM sizes and SHA-256 hashes, source/build provenance,
   patch hash, advisory decisions, and a fresh review date in
   `native-lock.json`.
5. Run the supply-chain verifier and confirm the generated CycloneDX/SPDX SBOMs
   match the rebuilt artifact before renewing the review horizon.

**Do not extend `reviewUntil` or release the current AAR.** The source-profile
changes are remediation instructions for the next native build, not proof that
the currently shipped binary is already protected.
