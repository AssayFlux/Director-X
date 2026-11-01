<div align="center">

<p>
  <img src="assets/icon.svg" alt="Director X application icon" width="168" />
</p>

# DIRECTOR X

### THE ANDROID VIDEO EDITOR

**Designed by AssayFlux**

*A warm, tactile editing experience. Built for Android.*

<p>
  <a href="https://github.com/AssayFlux/Director-X">SOURCE</a>
  &nbsp; · &nbsp;
  <a href="https://github.com/AssayFlux/Director-X/issues">SUPPORT</a>
</p>

[![Repository checks](https://img.shields.io/github/actions/workflow/status/AssayFlux/Director-X/repository-hygiene.yml?branch=main&label=REPOSITORY%20CHECKS&logo=githubactions&logoColor=white&style=for-the-badge)](https://github.com/AssayFlux/Director-X/actions/workflows/repository-hygiene.yml)

[![Android](https://img.shields.io/badge/PLATFORM-ANDROID-3DDC84?logo=android&logoColor=white&style=for-the-badge)](https://developer.android.com/)
[![Min SDK](https://img.shields.io/badge/MIN%20SDK-26-52796F?logo=android&logoColor=white&style=for-the-badge)](https://developer.android.com/about/versions)
[![Compile / Target](https://img.shields.io/badge/COMPILE%20%2F%20TARGET-37-52796F?logo=android&logoColor=white&style=for-the-badge)](https://developer.android.com/about/versions)

[![License](https://img.shields.io/github/license/AssayFlux/Director-X?label=LICENSE&style=for-the-badge)](LICENSE)
[![Last commit](https://img.shields.io/github/last-commit/AssayFlux/Director-X/main?label=LAST%20UPDATE&logo=git&style=for-the-badge)](https://github.com/AssayFlux/Director-X/commits/main)

[![Stars](https://img.shields.io/github/stars/AssayFlux/Director-X?label=STARS&style=for-the-badge)](https://github.com/AssayFlux/Director-X/stargazers)
[![Forks](https://img.shields.io/github/forks/AssayFlux/Director-X?label=FORKS&style=for-the-badge)](https://github.com/AssayFlux/Director-X/network/members)
[![Open issues](https://img.shields.io/github/issues/AssayFlux/Director-X?label=OPEN%20ISSUES&style=for-the-badge)](https://github.com/AssayFlux/Director-X/issues)
[![Contributors](https://img.shields.io/github/contributors/AssayFlux/Director-X?label=CONTRIBUTORS&style=for-the-badge)](https://github.com/AssayFlux/Director-X/graphs/contributors)

</div>

---

## The project

Director X is a native Android video editor built with Kotlin and Jetpack Compose. It combines media playback, editing and export infrastructure, local project storage, and selected on-device media-intelligence tools.

The interface follows a vintage darkroom direction: warm cream, paper beige, charcoal, muted taupe, brass gold and sage.

## Editing toolkit

| Editing and media | Intelligent media tools |
|---|---|
| Media import and playback | Auto captions (Whisper / ONNX path) |
| Timeline, trimming and previews | Background removal (MediaPipe) |
| Video transformation and export | Object removal (LaMa; model download required) |
| Effects, composition and muxing | Smart reframe and motion tracking |
| Audio and voiceover workflows | Audio denoising (DeepFilterNet) |
| Project files and sharing | Scene detection, stabilization and auto color |

**Capability note:** A dependency or roadmap entry is not proof that every feature is available on every device. Some features require model downloads; others remain planned. See the capability registry in the source and verify features on a real device before relying on them.

## Capability status

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

## Technology

| Area | Stack |
|---|---|
| Language and UI | Kotlin, Jetpack Compose, Material 3 |
| Media | AndroidX Media3, ExoPlayer, Transformer, Effects, Muxer |
| Persistence | Room, DataStore |
| Background work | WorkManager, Android foreground services |
| Dependency injection | Hilt / Dagger |
| Image loading | Coil |
| Machine learning | ONNX Runtime, MediaPipe |
| Audio | DeepFilterNet |
| Native media | FFmpegKitNext / FFmpeg |
| Build | Gradle Wrapper, Android Gradle Plugin, Java toolchain (project-managed) |

## Dependency registry

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

## Android identity

| Setting | Value |
|---|---|
| Application ID | `com.directorx.editor` |
| Kotlin namespace | `com.directorx.editor` |
| Minimum SDK | API 26 |
| Compile SDK | API 37 |
| Target SDK | API 37 |
| Java toolchain | 17 |

### Package identity and upgrade policy

The installed application ID and Kotlin namespace are both `com.directorx.editor`. Changing an Android application ID creates a separate installation; it is not an in-place update. The move from the previous package identity is a **new install with an explicit export/import path**: export projects from the previous installation, install Director X, then import those projects. Future updates must retain the established release signing identity.

## Build and test on Android with Termux

Director X is built and tested manually by the maintainer on an Android device using Termux. GitHub Actions does **not** build APKs, run Android unit tests, or run Android lint.

See the complete, device-oriented guide: **[Build Director X in Termux](docs/TERMUX_BUILD.md)**.

Quick build command after the Android SDK and JDK 17 are configured:

```bash
git clone https://github.com/AssayFlux/Director-X.git
cd Director-X
chmod +x gradlew
./gradlew :app:assembleDebug
```

Debug APK output is normally under `app/build/outputs/apk/debug/`. Check the actual generated filename and ABI split before installing. For installation, use `./gradlew :app:installDebug` with a supported connected device, or install the generated APK through Android's package installer.

Useful local checks, when deliberately requested:

```bash
./gradlew :app:testQaUnitTest
./gradlew :app:lint
```

These commands are optional maintainer-run checks; they are not run by GitHub Actions.

## GitHub automation policy

Only lightweight repository-hygiene validation remains enabled in GitHub Actions. It checks repository metadata and documentation policy; it does not configure an Android SDK or invoke Gradle.

- No APK build workflow.
- No Android unit-test workflow.
- No Android lint workflow.
- No automated APK signing, release upload or publishing.
- Android builds, installation and device testing are performed manually by the maintainer in Termux.

The repository-check badge above reports only the repository-hygiene workflow. It must not be interpreted as an Android build or app-test result.

## Repository map

```text
Director-X/
├── .github/workflows/       # Repository hygiene only
├── app/
│   ├── src/main/            # Android application source and resources
│   ├── src/test/            # JVM tests (run manually)
│   ├── src/androidTest/     # Instrumentation tests (run manually)
│   └── build.gradle.kts     # Android identity and build configuration
├── assets/                  # Director X brand assets
├── baselineprofile/
├── docs/                    # Build, privacy and compatibility documents
├── gradle/                  # Wrapper and version catalog
├── scripts/                 # Repository and release validation utilities
├── third_party/             # Pinned native dependency and notices
├── ACKNOWLEDGMENTS.md
├── CHANGELOG.md
├── LICENSE
├── NOTICE
└── README.md
```

## Design language

| Token | Colour | Use |
|---|---|---|
| Canvas | `#F5F0E6` | Warm cream background |
| Surface | `#EAE3D2` | Cards and panels |
| Primary | `#2C2621` | Main text |
| Secondary | `#8C8275` | Borders and supporting text |
| Accent | `#C29B38` | Vintage brass highlights |
| Action | `#5A6B57` | Muted sage actions |

Serif display headings pair with clear sans-serif controls and metadata. Components use restrained tactile surfaces and practical touch targets.

## Project lineage and licences

Director X is a derivative project based on the **ClearCut** Android video editor associated with **[SysAdminDoc](https://github.com/SysAdminDoc)**. We acknowledge the original developer and upstream contributors. Director X is independently maintained by AssayFlux; rebranding does not transfer upstream copyright.

Read [Acknowledgments](ACKNOWLEDGMENTS.md), [Branding and compatibility policy](docs/BRANDING_AND_COMPATIBILITY.md), [NOTICE](NOTICE), [upstream MIT licence](UPSTREAM-LICENSE-MIT.txt), and the root [LICENSE](LICENSE). Preserve all applicable upstream and third-party notices when modifying or redistributing the project.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for repository workflow, manual Android verification expectations and pull-request guidance.

## Support

- [Report a bug](https://github.com/AssayFlux/Director-X/issues/new/choose)
- [Browse issues](https://github.com/AssayFlux/Director-X/issues)
- [View source history](https://github.com/AssayFlux/Director-X/commits/main)

---

<div align="center">

<img src="assets/icon.svg" alt="Director X" width="64" />

### DIRECTOR X

**Designed by AssayFlux**

*Crafted for creators. Engineered for Android.*

</div>
