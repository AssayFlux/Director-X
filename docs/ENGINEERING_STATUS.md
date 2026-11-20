# Director X — Engineering Status

**Branch:** `Alpha`  
**Reviewed source baseline:** `785fd0a864db7ee8fce2cf69cfd0a394b487d376`  
**Application:** `Director X`  
**Developer:** `Designed by AssayFlux`  
**Version:** `v0.1.0`  
**Android version code:** `1` (release notation: `001`)  
**Application ID:** `com.directorx.editor`

This is an evidence-based status record. A source implementation is not accepted until the relevant automated checks and, where required, physical-device checks pass.

## Evidence rules

- **Implemented** means the source contains the implementation.
- **Automated pass** means a named workflow passed on the exact commit.
- **Device accepted** means a named physical device was tested and evidence was recorded.
- **Blocked / pending** means required evidence is unavailable. Do not infer acceptance from a UI control, source file, or emulator result.

## Current repository work

| Area | Current evidence | State |
|---|---|---|
| Repository hygiene | Run `44508933074` is running on the current `Alpha` baseline; latest completed hygiene run `44508826009` passed | **Current verification pending** |
| README architecture and Tech Stack | Representative source map and version-catalog rows are present | **Committed; Android test run pending** |
| Model registry | `docs/models.md` restored; FFmpeg AAR row now states the locked version, exact size, SHA-256, LGPL profile, and GPL/x264 exclusions | **Committed; Android test run pending** |
| Native source-offer metadata | Lock, app source-offer resource, capability registry JSON, and generated Kotlin now name `directorx-security.patch`; patch SHA-256 is `0dfaec621b5c1b8e37face076d3cc64a09cd098120921da1f4034d6bc8c8d080` | **Path/checksum automated pass** |
| Native security review | Review horizon in the lock expired on 2026-10-31. Current advisory sources confirm affected FFmpeg 8.1.2 components that the patch disables; shipped binary configure transcript and exact feature reachability have not been independently verified | **Blocked — not security-cleared** |
| Legal notices | Root `LICENSE`, `UPSTREAM-LICENSE-MIT.txt`, `NOTICE`, `ACKNOWLEDGMENTS.md`, FFmpeg LGPL materials and other third-party notices are retained | **Preserved; do not remove required attribution** |
| Product identity | Gradle still declares `com.directorx.editor`, `v0.1.0`, version code `1`; developer credit remains AssayFlux | **Source verified** |
| Legacy identifiers | `clearcut.db`, `.clearcut` archive extensions, notification-channel IDs, and serialized OpenFX/C2PA keys remain compatibility identifiers; retired public product names remain only in migration/audit checks | **Intentionally retained** |
| Launcher icon | Launcher generation now uses the supplied `Assets/App Icon.png` as its source for adaptive and legacy launcher resources; generated output still requires build and device visual verification | **Source updated; packaged verification pending** |
| Splash | MainActivity renders the Director X SVG logo at 280 dp / 1.624 aspect ratio for a 700 ms entry and controlled exit; the unused GIF resource and legacy composable have been removed | **Source updated; current Android CI and physical-device timing/visual acceptance pending** |
| Room database | `ProjectDatabase` is version 11, defines `MIGRATION_10_11`, and the generated version-11 schema snapshot is now checked in | **Snapshot committed; migration CI and real-database acceptance pending** |
| Custom canvas | Dimension validation, project canvas fields, export configuration propagation, and migration SQL are present; complete persisted-project/archive/encoder/final-media round-trip is not yet evidenced | **Implemented; acceptance incomplete** |
| Proxies | HALF/QUARTER/EIGHTH policy, rotation-aware dimensions, worker, artifact-commit rules, and unit tests are present; cancellation, retry/recovery, low-storage, playback/export concurrency, and original-media-only export are not accepted on device | **Implemented; acceptance incomplete** |
| Physical device | No current evidence for Moto G54 5G / Android 15 in this audit | **Pending maintainer device test** |

## Automated verification history

- Current verification on the 2026-11-19 `Alpha` baseline is running as Android verification run `44508933281`; do not report it green until unit tests, lint, and managed-device instrumentation all complete.
- Earlier Android verification run `44462304995` on `97814cb0a60af0ea87fe634b79d79e90e0124144` completed with **1,848 tests and 5 failures**:
  - Settings category navigation/scroll position.
  - Archive-transfer README wording.
  - FFmpeg native dependency documentation mismatch.
  - Crash-report banner copy transition.
  - Crash-report document-picker launch assertion.
- Source fixes were committed on the `Alpha` branch:
  - Settings scroll resets when changing categories.
  - README describes local file transfer without claiming an online backup service.
  - FFmpeg model-registry row matches the locked AAR size, checksum, and LGPL profile.
  - Capability registry JSON, generated Kotlin, and licence test now consistently name `directorx-security.patch`.
  - Crash-report UI tests now allow the exit animation and ActivityResult launcher dispatch to settle.
- Android verification run `44466317343` on `c5f4af7156d096cd0317fc114ae15886ddfdd8fb` completed with **1,848 tests and 3 failures**: Settings category navigation and the two crash-report banner copy/save UI tests.
- A follow-up run `44467371760` was cancelled by later source pushes before its final result was recorded.
- The latest splash integration is tracked in Android verification run `44469283816` on `62fde180e8a3afeb581515f893e0bd79fbd24524`; repository hygiene run `44469283880` initially exposed the expected duplicate asset pair, now allowlisted because the same approved GIF must exist both as a design source in `Assets/` and as an Android `res/raw` build resource. **Do not report the new revision as green until its rerun completes successfully.**
- Repository hygiene run `44467371766` passed on `dce400461e3f0ccec45af59913814bbdf73e903e`; the latest run `44467648000` is pending after the splash cleanup.
- Build instructions now use the same QA verification tasks as CI: `:app:testQaUnitTest` and `:app:lintQa`.

## Native media security gate

The native AAR, POM, lock and patch are build inputs and must remain in `third_party/ffmpeg-kit-next/`. The lock and app source-offer resource point to the tracked `directorx-security.patch`; the repository-hygiene workflow verifies the patch file and checksum.

The FFmpeg 8.1.2 upstream security page records fixes for CVE-2026-8461 and CVE-2026-30999. Other advisories affecting versions through 8.1.2 include vulnerabilities in VobSub, TDSC, MACE6, ADX/AAX, PNG/APNG, RASC, IAMF and ZLIB paths. The downstream patch disables the corresponding demuxers, decoders and encoders; CVE-2026-64832 is recorded as not reachable because the Android recipe does not enable CUDA/NVDEC. A fresh upstream/Debian advisory review has since identified additional 2026 CVE families, and even FFmpeg 9.0.2 has open advisories. See `docs/RELEASE_READINESS.md` for the expanded review scope. This is a source/configuration review only. The exact AAR's configure transcript, exported symbols and packaged component reachability have not been independently re-established.

The security review horizon remains expired. Updating the date would not constitute a security review. Until the exact binary and build configuration are independently checked and evidence is recorded, native security status remains **expired / not cleared**.

## Branding, legal and compatibility policy

- Keep user-visible product branding as **Director X** and developer credit as **Designed by AssayFlux**.
- Keep required inherited copyright, licence, attribution and corresponding-source materials. These are provenance obligations, not product branding.
- Do not remove `UPSTREAM-LICENSE-MIT.txt`, the root `LICENSE`, `NOTICE`, or component-specific notices.
- Preserve stable database names, archive extensions/MIME associations, serialized metadata keys, notification channels, and external-format fields until an explicit migration is designed and tested.
- ClearCut references in legal derivative attribution and compatibility contracts are not defects. Stale human-facing product copy and generated SBOM identity should be corrected without breaking compatibility.
- Previous application IDs and retired product labels in migration-validation metadata are historical evidence; retain them where identity checks depend on them.

## Release acceptance matrix

### Identity and build
- Version remains `v0.1.0`; version code remains `1` (displayed as `001`).
- Application ID remains `com.directorx.editor`.
- Release signing certificate must match the established identity.
- `android-verification.yml` runs Room schema generation, JVM unit tests, Android lint, and database/instrumentation checks. `release-candidate.yml` runs native supply-chain and package gates; it builds the APK only after all preceding gates pass. The native security gate is currently expired, so no APK has been built.

### Database and custom canvas
- The Room version 11 schema snapshot is checked in from KSP output.
- Verify every supported migration, including 10→11, against a real Room database.
- Verify custom dimensions through project creation, persistence, archive import/export, preview, export planning, encoder configuration and final media metadata.
- Cover portrait, landscape, square and unusual ratios; reject invalid dimensions before export.

### Proxy media
- Verify HALF, QUARTER and EIGHTH output dimensions and selected-tier propagation.
- Test rotated media, H.264/HEVC, actual Transformer cancellation, interruption/retry, cleanup, low storage and playback during export.
- Prove final exports use original media, never proxy files.

### Splash and launcher
- Verify the Director X SVG mark renders at the intended 280 dp width and 1.624 aspect ratio for the 700 ms entry and controlled-exit interval on a physical device.
- Confirm the obsolete AssayFlux GIF asset and animation helper are absent from the packaged app.
- Compare all packaged launcher densities/adaptive layers with the approved brand source.

### Physical-device acceptance
- Test on Moto G54 5G / Android 15.
- Record build fingerprint, media fixtures, output metadata, memory/storage observations and results.
- Emulator results do not substitute for physical-device acceptance.

## Non-negotiable boundaries

- Do not change version, version code, application ID or signing identity without explicit approval.
- Do not remove required legal notices or inherited attribution.
- Do not redesign editor/timeline/preview/export surfaces as part of the Dashboard and Settings scope.
- Do not claim 5.3K, 8K, HDR, native security clearance or physical-device acceptance without reproducible evidence.
