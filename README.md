# Director X
<p align="center">
  <img src="Assets/Director-X Logo.svg" alt="Director X — AssayFlux" width="400">
</p>

**Designed by AssayFlux**  
Android-first, non-linear video editor.

- Application ID: `com.directorx.editor`
- Release version: `v0.1.0`
- Android version code: `001`
- Primary development branch: `Alpha`

Director X provides a multi-track editing workflow with preview, project persistence, media management, effects, audio tools, proxy media, custom canvas dimensions, and export.

## AI and media capabilities

<!-- capability-registry:ai-tools:begin -->
| Tool | Engine | On-Device? |
|------|--------|------------|
| **Auto Captions** | ONNX Runtime Whisper tiny.en (English; multilingual Sherpa/Whisper path gated) | Yes |
| **Background Removal** | MediaPipe Selfie Segmentation (~1-7MB, ~30fps) | Yes |
| **AI Green Screen** | Planned: RobustVideoMatting requires model integration | Planned |
| **Object Removal** | LaMa-Dilated inpainting with rectangle, ellipse, and freehand mask rendering for stills and motion clips | Yes (explicit ~174 MB model download) |
| **Video Upscaling** | Planned: Real-ESRGAN requires model integration | Planned |
| **Frame Interpolation** | Planned: RIFE v4.6 requires the NCNN dependency | Planned |
| **Style Transfer** | Planned: AnimeGANv2 and Fast NST require model integration | Planned |
| **Stabilization** | Built-in offline motion analysis with bounded translation search and shared preview/export transforms | Yes |
| **Smart Reframe** | MediaPipe BlazeFace detection, EMA-smoothed crop trajectory, 3 strategies (stationary/pan/track) | Yes |
| **Tap-to-Segment** | Planned: SAM 2.1 Hiera Tiny with a MobileSAM fallback | Planned |
| **Scene Detection** | Content-aware frame difference analysis with auto-split | Yes |
| **Auto Color** | Histogram-based brightness/contrast/saturation/temperature | Yes |
| **HDR Encoder Feature Gate** | MediaCodecInfo.CodecCapabilities FEATURE_HdrEditing / FEATURE_HlgEditing | Yes |
| **Motion Tracking** | Template matching with position keyframe generation | Yes |
| **Audio Denoise** | DeepFilterNet 3 with spectral-gate fallback | Yes |
<!-- capability-registry:ai-tools:end -->

## Runtime dependencies

<!-- capability-registry:dependencies:begin -->
| Dependency | Version | Purpose |
|-----------|---------|---------|
| ONNX Runtime | 1.28.0 | Whisper ASR and LaMa inpainting |
| MediaPipe | 1.0.0 | Selfie segmentation and smart reframe |
| Lottie Compose | 6.7.1 | Animated title templates |
| OkHttp | 5.5.0 | Model downloads and future opt-in provider calls |
| Media3 | 1.11.1 | Transformer, ExoPlayer, effects, and muxing |
| Coil Compose | 3.5.0 | Image and video thumbnails |
| Hilt / Dagger | 2.60.1 | Dependency injection |
| Android DeepFilterNet | 0.0.8 | On-device voiceover noise reduction |
| FFmpegKitNext / FFmpeg | 9.0.2 (FFmpeg 9.0.2) | LGPL FFmpeg paths not covered by Media3 Transformer |
| AndroidX Benchmark/ProfileInstaller | 1.5.0-beta01 / 1.4.1 | Baseline profile generation and installation |
| Sherpa-ONNX | 1.13.2 target | Future native Moonshine v2 ASR path (target) |
| SAM 2.1 ONNX | Targeted | Future tracked-mask path; MobileSAM fallback (target) |
<!-- capability-registry:dependencies:end -->


## Tech Stack

| Area | Version / implementation |
|---|---|
| Language | Kotlin 2.4.10 |
| Video | Media3 1.11.1 |
| Speech-to-Text | ONNX Runtime 1.28.0 |
| Noise Reduction | Android DeepFilterNet 0.0.8 |
| Segmentation | MediaPipe Tasks Vision 1.0.0 |
| Animated Titles | Lottie Compose 6.7.1 |
| Startup performance | AndroidX Benchmark 1.5.0-beta01 |
| Database | Room 3.0.1 |

## Architecture

The following is a representative source map, not an exhaustive API or feature-availability promise. Entries are checked-in Kotlin source paths under `app/src/main/java/com/directorx/editor`.

```text
├── engine/AiDisclosurePolicy.kt
├── engine/AiThumbnailEngine.kt
├── engine/AiToolRequirements.kt
├── engine/AiUsageLedger.kt
├── engine/Android15MediaPolicy.kt
├── engine/AndroidProjectDependencyProbe.kt
├── engine/AppLog.kt
├── engine/AppModule.kt
├── engine/AppVersion.kt
├── engine/AtomicFiles.kt
├── engine/AudioDecodeBudget.kt
├── engine/AudioDescriptionEngine.kt
├── engine/AudioEffectsAudioProcessor.kt
├── engine/AudioEffectsEngine.kt
├── engine/AudioEngine.kt
├── engine/AudioFocusPolicy.kt
├── engine/AudioMasteringEngine.kt
├── engine/AutoChapterEngine.kt
├── engine/BatchExportPlanStore.kt
├── engine/BeatDetectionEngine.kt
├── engine/BidiTextPolicy.kt
├── engine/BoundedIo.kt
├── engine/C2paExportEngine.kt
├── engine/CapabilityRegistry.kt
├── engine/CapabilityRegistryGenerated.kt
├── engine/CaptionFontFallbackPolicy.kt
├── engine/CaptionImportEngine.kt
├── engine/CaptionTextLayoutPolicy.kt
├── engine/CaptionTranslationEngine.kt
├── engine/CodecInstanceBudget.kt
├── engine/ColorBlindGlEffect.kt
└── engine/ColorBlindPreviewEngine.kt
```

## Archive Transfer

Projects can be moved between devices using an explicit local archive export/import flow. This is a local file-based transfer flow, not an online backup or synchronization service. The archive includes app-managed source media and project dependencies. Android automatic cloud backup is separate and intentionally excludes imported/generated media, custom fonts, and LUTs to stay within its per-app size limit; use the archive export or device-to-device transfer when those files must move with the project.

## Project status

The authoritative engineering record is [docs/ENGINEERING_STATUS.md](docs/ENGINEERING_STATUS.md). Build instructions are in [docs/BUILD.md](docs/BUILD.md). Privacy disclosures are in [docs/privacy-policy.md](docs/privacy-policy.md).

## Engineering quality programmes

The current P2 product-quality audit covers responsiveness, memory/lifecycle, database scale, privacy/security, accessibility, and automated quality gates. Its evidence and open acceptance criteria are tracked in [docs/P2_PRODUCT_QUALITY_AUDIT_2026-11-24.md](docs/P2_PRODUCT_QUALITY_AUDIT_2026-11-24.md). This is an engineering audit, separate from the product feature-upgrade roadmap; it does not imply P2 is complete or release-ready.

## Director X product upgrade programme

The separate three-upgrade product programme (Upgrade 1: Foundation and Monetization; Upgrade 2: Complete AI Ecosystem; Upgrade 3: Cloud AI and Professional Expansion) is specified in [docs/DIRECTOR_X_PRODUCT_UPGRADE_PLAN.md](docs/DIRECTOR_X_PRODUCT_UPGRADE_PLAN.md). It is independent of the original five-stage engineering roadmap in [docs/UPGRADE_ROADMAP.md](docs/UPGRADE_ROADMAP.md). The product-upgrade plan defines target scope and acceptance gates; it does not imply that planned features are already implemented or verified.

Do not infer feature acceptance from the presence of a UI control or implementation class. The status document distinguishes implemented code, automated verification, and physical-device acceptance.

## Android 17 audio hardening smoke check

- Target SDK: 37 (Android 17).
- Android 17 audio hardening: on a supported test device, run `adb shell cmd audio set-hardening throw`.
- Exercise TTS preview, voiceover, and export while observing `AudioHardening` diagnostics. The export foreground service must remain a media-processing service and must not request microphone or media-playback foreground-service types.

## Build and verification

See [docs/BUILD.md](docs/BUILD.md). The `android-full-ci.yml` workflow runs lint, JVM tests, managed-device instrumentation, native supply-chain checks, and QA APK packaging. QA APKs are temporary validation artifacts downloadable from the successful GitHub Actions run; they are not production releases. Production release signing remains a controlled maintainer operation.

Google Play listing preparation, store asset requirements, privacy/contact blockers, and owner-only submission steps are tracked in [Google Play listing preparation](docs/RELEASE_READINESS.md#google-play-listing-preparation).

## Branding and assets

The supplied files in `Assets/` are the approved design sources:
- `Assets/Director-X Logo.svg`
- `Assets/UI Design Texture.svg`

The dashboard and Settings surfaces may use the approved texture. The editing workspace is outside the current UI redesign scope. Preserve the existing theme choices and icon language.

## Compatibility and notices

The application identity is Director X. Required third-party licences, copyright notices, upstream attribution, and compatibility metadata remain intact where legally or technically required. These are not product branding.

### Package identity and upgrade policy

The application ID is `com.directorx.editor`. Changing the package identity requires a **new install with an explicit export/import path**; it is not an in-place rename. The signing certificate and project/archive compatibility contracts must be preserved.

## Maintenance

Changes must preserve project/archive compatibility, database migrations, original-media export, and explicit device capability checks. Do not claim 5.3K, 8K, HDR, or device acceptance without reproducible evidence.


## Current QA status (2026-11-24)

The previous detailed API 35/API 37 device findings are recorded against source revision `b44ed4cb66fb60674dabb16f28dc0ff72018364a` in [workflow run #349](https://github.com/AssayFlux/Director-X/actions/runs/45303741071). The subsequent screenshot-integration revision `46d293e10bdb80be82be641a06565ba4b5d60593` was checked by [Android workflow run #370](https://github.com/AssayFlux/Director-X/actions/runs/45314862774): QA APK assembly/native alignment passed, while the duplicate audit and two tracked-file JVM policy tests failed because their allowlists had not been updated for intentional screenshot copies, the CodeQL workflow, and newly public release docs. [Code quality run #17](https://github.com/AssayFlux/Director-X/actions/runs/45314862797) passed.

PR [#150](https://github.com/AssayFlux/Director-X/pull/150) updates those narrow policy allowlists. Its full Android workflow is running; no pass is claimed yet. The Android instrumentation and API 37 reliability findings remain separate unresolved release blockers. See [the engineering status record](docs/ENGINEERING_STATUS.md) for the source-revision-specific evidence. Production release remains blocked pending physical-device acceptance and independent signing-key custody verification.
