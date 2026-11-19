# Director X — Engineering Status

**Current branch:** Alpha  
**Current repository baseline:** commit `282fbb57534e3bfbcb96b1e6e61102288570185a`  
**Application version:** `v0.1.0`  
**Android version code:** `001`  
**Application ID:** `com.directorx.editor`

This is the single authoritative current-status document. Update it whenever implementation or verification state changes.

## Status rules

- **Implemented** means code exists; it does not mean accepted.
- **Automated pass** means the named CI job passed on the exact commit.
- **Device accepted** means tested on the named physical device with evidence.
- **Blocked** means required evidence or a dependency is unavailable.
- Never mark an item complete based only on a source-code inspection.

## Current known work

| Area | Current evidence | State |
|---|---|---|
| CI | Previous Alpha verification run reported three failing tests; latest post-change run not yet observed | Re-run required |
| Dashboard and Settings | Approved SVG texture is referenced by current UI; editing workspace remains unchanged | Inspect on device |
| Branding | Application ID is Director X; old internal names remain in source and tests | Rename pending |
| Launcher icon | Supplied SVG bytes could not be retrieved through the connected GitHub file reader | Blocked on asset retrieval |
| Artwork | Root-level texture PNG was verified byte-identical to Assets copy and deleted; canonical Assets artwork retained | Partial |
| Splash | Entry delay aligned to 1,300 ms; exit duration previously 1,300 ms | Automated/lifecycle verification required |
| Room | Database version 11 and migration 10→11 were previously identified; schema snapshot 11 was missing | Generate and verify |
| Proxies | Pipeline exists; HALF/QUARTER/EIGHTH, rotation, cancellation, recovery, low-storage, and export-source acceptance remain required | Device acceptance pending |
| Custom canvas | Model/config path exists; end-to-end persisted dimensions and encoded output metadata require acceptance | Verification pending |
| Native media | FFmpeg native build review and component reachability require evidence-based renewal | Security review pending |
| 5.3K/8K | 5.3K source editing is not equivalent to 8K output capability | Capability claims must remain constrained |

## Required acceptance matrix

### Build and identity
- Gradle resolves exactly `v0.1.0` for debug, QA, streaming, and release variants.
- Android version code is 1 (displayed as 001 in documentation).
- Package/application ID remains `com.directorx.editor`.
- Signing identity matches the existing release identity; never silently change signing keys.

### Database and project compatibility
- Generate Room schema version 11 from the actual KSP output.
- Compare the generated snapshot to migration 10→11.
- Test upgrades from every supported schema version and verify project data, autosave state, and trash state.
- Preserve archive formats and serialized identifiers.

### Custom canvas
- Verify dimensions at creation, persistence, archive round-trip, preview, export planning, encoder configuration, and final MP4 metadata.
- Cover portrait, landscape, square, and unusual aspect ratios.
- Reject invalid/unsupported dimensions before export with actionable feedback.

### Proxy media
- Verify HALF, QUARTER, and EIGHTH output dimensions and the selected tier at every worker entry point.
- Test rotated video, H.264 and HEVC inputs, cancellation during transformation, interruption/retry, cache cleanup, low storage, and playback during export.
- Prove final export resolves original media rather than proxy media.

### Native media security
- Audit enabled decoders, encoders, filters, external libraries, and ABI packaging from the actual native build configuration.
- Record source revisions, build commands, patch set, artifact SHA-256, and advisory reachability.
- Do not renew a security review based on assumptions or a patch filename alone.

### Device acceptance
- Run the app on a physical Moto G54 5G / Android 15.
- Record device build fingerprint, media fixtures, output metadata, memory/storage observations, and results.
- Managed emulator results are not a substitute for physical-device acceptance.

## Recent verified repository edits

- Entry splash delay set to 1,300 ms to match the existing exit delay.
- Gradle version set to `v0.1.0`, version code set to 1.
- Settings now displays the Gradle version string without adding a duplicate `v` prefix.
- Build-variant suffixes removed so variant names do not drift from the approved version.
- Verified duplicate root `UI Design Texture.png` removed; canonical asset under `Assets/` retained.

## Important boundaries

- Do not change version, version code, application ID, or release signing identity without explicit authorization.
- Do not delete legal notices or required upstream attribution.
- Do not change the editor/timeline/preview/export UI as part of the Dashboard and Settings refresh.
- Do not claim a physical-device result unless it has actually been run and recorded.
