# Director X — Engineering Status

**Branch:** `Alpha`  
**Baseline recorded:** `662b0eb8ace3b6215dcf6ffdb9419759fee569fd`  
**Application:** `Director X`  
**Developer:** `Designed by AssayFlux`  
**Version:** `v0.1.0`  
**Android version code:** `1` (displayed as `001`)  
**Application ID:** `com.directorx.editor`

This is the current engineering record. A source-level implementation is not accepted until its automated and, where relevant, physical-device checks pass.

## Evidence rules

- **Implemented** means the source contains the implementation.
- **Automated pass** means the named workflow passed on the exact commit.
- **Device accepted** means a named physical device was tested and evidence was recorded.
- **Blocked / pending** means required evidence is not available. Do not convert a pending item into a pass based on assumption.

## Current repository work

| Area | Current evidence | State |
|---|---|---|
| Repository hygiene | Run `44462073338` passed on `aa7351eba27dadc65445858ff0e4c2bcaceb7171`; later source-offer path edit is commit `8e57210` | Re-run on current HEAD |
| README architecture | Added a representative source map with checked-in Kotlin paths | Verify in Android unit-test workflow |
| README Tech Stack | Added catalog-version rows for Kotlin, Media3, ONNX Runtime, DeepFilterNet, MediaPipe, Lottie, Benchmark and Room | Verify in Android unit-test workflow |
| Model registry | Restored `docs/models.md` from the last tracked registry revision and retained pinned source/checksum records; remote model bytes were not re-downloaded | Documentation tests pending; revalidate source artifacts before release |
| Tracked-doc audit | Updated the allowlist to the six Markdown files currently tracked in the repository | Verify in Android unit-test workflow |
| Native patch path | `native-lock.json` and LGPL source-offer text now name `directorx-security.patch` | Checksum still requires independent verification |
| Native SBOM branding | Generated SPDX document now identifies Director X / AssayFlux rather than NovaCut/ClearCut placeholder identity | Automated native supply-chain execution still required |
| Native security review | Review horizon remains `2026-10-31`, which is expired as of 2026-11-19 | **Blocked — no renewed security clearance** |
| Launcher icon | Adaptive-icon XML references foreground and monochrome mipmaps; binary artwork has not been compared against `Assets/Director-X Logo.png` | Pending visual/binary verification |
| Brand references | Public product identity is Director X; required ClearCut derivative attribution and stable legacy storage/archive identifiers remain | Reconcile by category; do not rename compatibility keys blindly |
| Splash | MainActivity contains the branded splash composition and 1,300 ms entry delay | Lifecycle and physical-device acceptance pending |
| Database | Current tracked Room schema snapshots are versions 1–10; schema 11 is not present in the tree | Generate/verify current schema and migration |
| Proxies | Proxy implementation exists; tier, rotation, cancellation, recovery, storage-pressure and original-media export acceptance are not proven here | Device acceptance pending |
| Custom canvas | Custom dimensions are represented in project/export paths; full persistence/archive/encoded-output round trip is not accepted here | Automated and device verification pending |
| Physical device | No new Moto G54 5G / Android 15 evidence was produced in this review | Pending |

## Automated test history

- Android verification run `44460366801` on commit `7c32d85fcbea61fadaa67d58b57c09cc430206d2` failed with 16 reported test failures.
- The subsequent documentation repair added README Architecture and Tech Stack sections, restored `docs/models.md`, and corrected the tracked-Markdown audit allowlist.
- Android verification run `44462038704` on commit `c32893a371e9de8f18aa8d4332ada4633b105a77` was still running when this record was prepared. It does not include later native-lock/SBOM/source-offer edits.
- Repository hygiene run `44462073338` passed on commit `aa7351eba27dadc65445858ff0e4c2bcaceb7171`. Native patch/source-offer verification passed on `c9cc26e` and `0e7eb31` after correcting the SHA-256 and path.
- Do not report the Android test suite as green until a completed run on the latest HEAD is successful.

## Native media security gate

The native AAR, POM, lock and patch are build inputs and must remain in `third_party/ffmpeg-kit-next/`. The lock now points to the tracked `directorx-security.patch`, and the app's LGPL source-offer text points to the same file.

The patch SHA-256 was independently calculated by the GitHub Actions runner and matched the corrected lock value `3a8dcc1aac92be38857dc89c6d6dcaed4457933855340c2fc1a948b42f2f0da7`. Repository-hygiene run `44462388307` passed the native patch/source-offer check on the lock-fix commit. The security review horizon expired on 2026-10-31. Updating a filename or extending a date would not constitute a security review. Renewal still requires inspecting the exact native build inputs and shipped binaries, verifying patch application and build flags, reviewing current advisories and reachability, and recording reproducible evidence. Until then, native security status is **expired / not cleared**.

## Branding and compatibility policy

- Keep user-visible product branding as **Director X** and developer credit as **Designed by AssayFlux**.
- Keep required inherited copyright, licence, attribution and corresponding-source materials. These are provenance obligations, not product branding.
- Preserve stable storage names, database names, archive extensions/MIME associations, serialized metadata keys, notification channels, and external-format fields until an explicit migration is designed and tested.
- ClearCut references in legal derivative attribution and compatibility contracts are not automatically defects. Human-facing stale product copy and generated SBOM identity are defects and should be corrected without breaking compatibility.
- Previous application IDs and retired product labels in migration-validation metadata are historical migration evidence; do not erase them if identity checks depend on them.

## Release acceptance matrix

### Identity and build
- Version remains `v0.1.0`; version code remains `1`.
- Application ID remains `com.directorx.editor`.
- Release signing certificate must match the established identity.
- CI must not build or publish APKs; APK creation remains a maintainer-run task.

### Database and project compatibility
- Generate the current Room schema snapshot from KSP output.
- Verify every supported migration and project/archive round trip.
- Preserve autosave, trash, media references and serialized identifiers.

### Custom canvas
- Verify dimensions through project creation, persistence, archive import/export, preview, export planning, encoder configuration and final media metadata.
- Cover portrait, landscape, square and unusual ratios.
- Reject invalid or unsupported dimensions before export.

### Proxy media
- Verify HALF, QUARTER and EIGHTH output dimensions and selected-tier propagation.
- Test rotated media, H.264/HEVC, cancellation, interruption/retry, cleanup, low storage and playback during export.
- Prove final exports use original media, never proxy files.

### Physical-device acceptance
- Test on Moto G54 5G / Android 15.
- Record build fingerprint, media fixtures, output metadata, memory/storage observations and results.
- Emulator results do not substitute for physical-device acceptance.

## Non-negotiable boundaries

- Do not change version, version code, application ID or signing identity without explicit approval.
- Do not remove required legal notices or inherited attribution.
- Do not redesign the editor/timeline/preview/export surfaces as part of the Dashboard and Settings scope.
- Do not claim 5.3K, 8K, HDR, native security clearance or physical-device acceptance without reproducible evidence.
