# Director X

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
| FFmpegKitNext / FFmpeg | 8.1.0 (FFmpeg 8.1.2) | LGPL FFmpeg paths not covered by Media3 Transformer |
| AndroidX Benchmark/ProfileInstaller | 1.5.0-beta01 / 1.4.1 | Baseline profile generation and installation |
| Sherpa-ONNX | 1.13.2 target | Future native Moonshine v2 ASR path (target) |
| SAM 2.1 ONNX | Targeted | Future tracked-mask path; MobileSAM fallback (target) |
<!-- capability-registry:dependencies:end -->

## Project status

The authoritative engineering record is [docs/ENGINEERING_STATUS.md](docs/ENGINEERING_STATUS.md). Build instructions are in [docs/BUILD.md](docs/BUILD.md). Privacy disclosures are in [docs/privacy-policy.md](docs/privacy-policy.md).

Do not infer feature acceptance from the presence of a UI control or implementation class. The status document distinguishes implemented code, automated verification, and physical-device acceptance.

## Android 17 audio hardening smoke check

- Target SDK: 37 (Android 17).
- Android 17 audio hardening: on a supported test device, run `adb shell cmd audio set-hardening throw`.
- Exercise TTS preview, voiceover, and export while observing `AudioHardening` diagnostics. The export foreground service must remain a media-processing service and must not request microphone or media-playback foreground-service types.

## Build

See [docs/BUILD.md](docs/BUILD.md). APKs are built manually by the maintainer; CI is not an APK distribution channel.

## Branding and assets

The supplied files in `Assets/` are the approved design sources:
- `Assets/Director-X Logo.svg`
- `Assets/UI Design Texture.svg`

The dashboard and Settings surfaces may use the approved texture. The editing workspace is outside the current UI redesign scope. Preserve the existing theme choices and icon language.

## Compatibility and notices

The application identity is Director X. Required third-party licences, copyright notices, upstream attribution, and compatibility metadata remain intact where legally or technically required. These are not product branding.

### Package identity and upgrade policy

The application ID is `com.directorx.editor`. Changing the package identity requires a **new install with an explicit export/import path**; it is not an in-place rename. The signing certificate and project/archive compatibility contracts must be preserved.

## Contributions

Changes must preserve project/archive compatibility, database migrations, original-media export, and explicit device capability checks. Do not claim 5.3K, 8K, HDR, or device acceptance without reproducible evidence.
