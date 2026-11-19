# Director X — Engineering Status

**Target branch:** `Alpha` (work is being prepared on `ffmpeg-removal-complete`)  
**Application:** `Director X`  
**Developer credit:** `Designed by AssayFlux`  
**Version:** `v0.1.0`  
**Android version code:** `1`  
**Application ID:** `com.directorx.editor`

This is an evidence-based status record. Source changes are not accepted as complete until the relevant automated checks and, where required, physical-device checks pass.

## Evidence rules

- **Implemented** means the source contains the implementation.
- **Automated pass** means a named workflow passed on the exact commit.
- **Device accepted** means a named physical device was tested and evidence was recorded.
- **Blocked / pending** means required evidence is unavailable. Do not infer acceptance from a UI control, source file, or emulator result.

## FFmpeg removal and platform-media migration

The project is moving to Android platform media APIs and AndroidX Media3. The bundled FFmpegKit/FFmpeg AAR, native lock, patch, source-offer, and associated security-review gates are being removed rather than renewed.

| Area | Replacement | Current state |
|---|---|---|
| Normal export | Media3 Transformer | Existing primary export path retained |
| Proxy generation | Media3 Transformer only | FFmpeg fallback removed |
| Audio decode | MediaExtractor + MediaCodec via AudioEngine | Used by noise-reduction pipeline |
| Audio AAC encode | MediaCodec + MediaMuxer via AudioEngine | Platform encoder added; device validation pending |
| Noise reduction | DeepFilterNet 3 / spectral gate | Uses platform decode/encode; tests pending |
| Inpainting frame assembly | Media3 Transformer image/audio composition | Implemented; instrumentation validation pending |
| Mixed-render concat | Media3 Transformer sequence | Implemented; export validation pending |
| Constant frame rate | Media3 edited-item frame-rate control | FFmpeg pre-normalization removed; validation pending |
| Subtitle sidecar | MediaExtractor | Platform extraction and SRT/VTT validation implemented |
| Subtitle burn-in | Media3 text overlays | Caption overlays are added to the export composition; style parity validation pending |
| Reverse rendering | No public Media3 reverse effect | Not implemented; preflight must disclose the forward-playback fallback |

The migration is **not complete** while reverse rendering and any remaining FFmpeg references or artifacts remain. Do not report the app as FFmpeg-free until a repository-wide audit confirms that no runtime, build, documentation, licensing, or CI reference remains.

## Repository identity and compatibility

- Keep user-visible branding as **Director X** and developer credit as **Designed by AssayFlux**.
- Keep the application ID `com.directorx.editor`, version `v0.1.0`, and version code `1` unless explicitly approved.
- Preserve database names, archive extensions/MIME associations, serialized metadata keys, notification channels, and external-format fields until an explicit migration is designed and tested.
- Preserve all third-party notices for dependencies that remain bundled, including DeepFilterNet and other runtime dependencies.

## Automated verification

An earlier draft PR (#84) was merged into `Alpha` while the migration was still incomplete. Its CI exposed:
- A pre-existing FFmpeg native security-patch SHA mismatch in repository hygiene and release-candidate validation. The FFmpeg-only checks are being removed with the dependency.
- A Kotlin compile error in subtitle sidecar logging (private file-level `TAG` reference), corrected on the continuation branch.

That run did **not** pass. The unit-test job failed at compilation; no successful test result was established for that revision. The current continuation branch has not yet completed a fresh QA unit-test, lint, or instrumentation run.

Pull-request validation must run tests and lint without assembling APKs. APK assembly is reserved for a manual release-candidate workflow dispatch after the source and test gates pass.

## Remaining release acceptance

- Complete the repository-wide FFmpeg reference and artifact audit.
- Finish or explicitly disclose any remaining unsupported media capabilities; no silent forward-render substitutions.
- Run QA unit tests, Android lint, and relevant instrumentation tests on the final migration branch.
- Verify platform AAC encoding and audio/video muxing on physical devices across supported codec profiles.
- Verify inpainting retains source audio and correct duration.
- Verify mixed-render concatenation and CFR output metadata.
- Verify subtitle overlay timing, style, and export behavior.
- Complete playback, proxy generation, audio processing, export, navigation, and large-project acceptance on supported physical devices.
- Verify the release signing certificate matches the established published identity.
- Build and inspect the final release APK set only after all gates pass.

## Physical-device acceptance

No new physical-device acceptance is claimed in this migration. Record device model, Android version/build fingerprint, media fixtures, output metadata, memory/storage observations, and results. Emulator results do not substitute for physical-device acceptance.

## Non-negotiable boundaries

- Do not change version, version code, application ID, or signing identity without explicit approval.
- Do not remove required legal notices or attribution for dependencies that remain.
- Do not claim all tests pass, reverse rendering works, or release readiness is established without reproducible evidence.
- Do not build APKs during intermediate migration stages.
