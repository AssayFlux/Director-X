<div align="center">

<img src="assets/icon.png" alt="Director X application icon" width="176" />

# DIRECTOR X

### CINEMATIC VIDEO EDITING FOR ANDROID

**Designed by AssayFlux**

A native Android video-editing studio combining media creation, intelligent tools, and local-first processing.

[**View source**](https://github.com/AssayFlux/Director-X) · [**Report an issue**](https://github.com/AssayFlux/Director-X/issues) · [**Builds & APK**](https://github.com/AssayFlux/Director-X/actions/workflows/build-debug-apk.yml)

<br/>

<!-- LIVE STATUS BADGES: keep workflow badges tied to real workflows only. -->
[![APK build](https://img.shields.io/github/actions/workflow/status/AssayFlux/Director-X/build-debug-apk.yml?branch=main&label=APK%20BUILD&logo=android&logoColor=white&style=for-the-badge)](https://github.com/AssayFlux/Director-X/actions/workflows/build-debug-apk.yml)
[![Repository hygiene](https://img.shields.io/github/actions/workflow/status/AssayFlux/Director-X/repository-hygiene.yml?branch=main&label=REPOSITORY%20HYGIENE&logo=githubactions&logoColor=white&style=for-the-badge)](https://github.com/AssayFlux/Director-X/actions/workflows/repository-hygiene.yml)
[![Latest commit](https://img.shields.io/github/last-commit/AssayFlux/Director-X/main?label=LATEST%20COMMIT&logo=git&logoColor=white&style=for-the-badge)](https://github.com/AssayFlux/Director-X/commits/main)

[![Version](https://img.shields.io/badge/PROJECT%20VERSION-v0.1.0-B18B5E?logo=semver&logoColor=white&style=for-the-badge)](https://github.com/AssayFlux/Director-X)
[![Android](https://img.shields.io/badge/PLATFORM-ANDROID-3DDC84?logo=android&logoColor=white&style=for-the-badge)](https://developer.android.com/)
[![Minimum SDK](https://img.shields.io/badge/MIN%20SDK-26-52796F?logo=android&logoColor=white&style=for-the-badge)](https://developer.android.com/about/versions)
[![Target SDK](https://img.shields.io/badge/TARGET%20SDK-37-52796F?logo=android&logoColor=white&style=for-the-badge)](https://developer.android.com/about/versions)

[![Kotlin](https://img.shields.io/badge/KOTLIN-2.4.10-7F52FF?logo=kotlin&logoColor=white&style=for-the-badge)](https://kotlinlang.org/)
[![Java](https://img.shields.io/badge/JAVA-17-ED8B00?logo=openjdk&logoColor=white&style=for-the-badge)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/GRADLE-9.8.0-02303A?logo=gradle&logoColor=white&style=for-the-badge)](https://gradle.org/)
[![License](https://img.shields.io/github/license/AssayFlux/Director-X?label=LICENSE&style=for-the-badge)](https://github.com/AssayFlux/Director-X/blob/main/LICENSE)

[![Stars](https://img.shields.io/github/stars/AssayFlux/Director-X?label=STARS&style=for-the-badge)](https://github.com/AssayFlux/Director-X/stargazers)
[![Forks](https://img.shields.io/github/forks/AssayFlux/Director-X?label=FORKS&style=for-the-badge)](https://github.com/AssayFlux/Director-X/network/members)
[![Open issues](https://img.shields.io/github/issues/AssayFlux/Director-X?label=OPEN%20ISSUES&style=for-the-badge)](https://github.com/AssayFlux/Director-X/issues)
[![Repo size](https://img.shields.io/github/repo-size/AssayFlux/Director-X?label=REPOSITORY%20SIZE&style=for-the-badge)](https://github.com/AssayFlux/Director-X)

</div>

---

## About Director X

Director X is a native Android video editor built with Kotlin and Jetpack Compose. The project brings together media playback, editing and export infrastructure, local project data, and on-device intelligent media capabilities.

Its visual direction uses a warm, cinematic palette: ivory, charcoal, vintage gold, and muted sage.

## Creative toolkit

| Editing & media | AI, audio & intelligence |
|---|---|
| Media playback and editing previews | On-device inference through integrated engines |
| Video transformation and export workflows | Whisper-based captioning path |
| Visual effects and media processing | Background removal with MediaPipe |
| Media composition and muxing | Object removal with LaMa inpainting |
| Media import, project files and sharing | Audio denoising with DeepFilterNet |
| Stabilization, motion tracking and scene detection | Smart reframe and automatic color adjustments |

| Application experience | Privacy & reliability |
|---|---|
| Jetpack Compose and Material 3 interface | App-managed private storage |
| Room database and DataStore preferences | Local processing for supported workflows |
| WorkManager and Android foreground services | Explicit Android permissions and network policy |
| Hilt dependency injection | Dependency verification and build integrity |

> **Capability status:** Some tools are implemented, some require an explicit model download, and others remain planned. The registry below distinguishes these states; the presence of a tool in the roadmap does not mean it is already available in the app.

## AI capability registry

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

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Media playback | AndroidX Media3 ExoPlayer |
| Media transformation | AndroidX Media3 Transformer |
| Effects and muxing | Media3 Effects and Muxer |
| Database | Room |
| Preferences | DataStore |
| Background work | WorkManager |
| Dependency injection | Hilt / Dagger |
| Image loading | Coil |
| On-device inference | ONNX Runtime |
| Vision and segmentation | MediaPipe |
| Audio processing | DeepFilterNet |
| Build system | Gradle 9.8.0 |
| Android Gradle Plugin | 9.4.1 |
| Kotlin plugin | 2.4.10 |
| Java toolchain | 17 |

### Selected dependency versions

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

## Architecture

The application separates presentation, application state, persistence, and media processing into focused layers.

```text
                 PRESENTATION
           Jetpack Compose · Material 3
                        |
                        v
             APPLICATION & UI STATE
                Screens · ViewModels
                        |
                        v
                      DATA
              Repositories · Room
                  · DataStore
                        |
                        v
             MEDIA & AI PROCESSING
             Media3 · Native Processing
                    · AI Engines
                        |
                        v
               ANDROID PLATFORM
          Storage · Services · WorkManager
```

## Android build configuration

| Setting | Current value |
|---|---|
| Application ID | `com.directorx.editor` |
| Kotlin namespace | `com.directorx.editor` |
| Version name | `3.82.0` |
| Version code | `301` |
| Minimum SDK | API 26 |
| Compile SDK | API 37 |
| Target SDK | API 37 |
| Java | 17 |
| Gradle wrapper | 9.8.0 |
| Android Gradle Plugin | 9.4.1 |
| Kotlin | 2.4.10 |

### Package identity and upgrade policy

The current application ID and Kotlin namespace are `com.directorx.editor`. The previous package identity, `com.novacut.editor`, is retained in migration history. Android treats a different application ID as a separate app: moving projects from the previous package requires a new install with an explicit export/import path. Future Director X updates must preserve the release signing identity.

## Build from source

### Requirements

- JDK 17
- Android SDK Platform 37 and compatible Build Tools
- Git
- Android device or emulator running API 26 or newer

### 1. Clone the repository

```bash
git clone https://github.com/AssayFlux/Director-X.git
cd Director-X
```

### 2. Configure the Android SDK

Set `ANDROID_HOME` or `ANDROID_SDK_ROOT`. If needed, create a local `local.properties` file:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

Do not commit machine-specific SDK paths.

### 3. Check the toolchain

```bash
java -version
./gradlew --version
```

Use JDK 17. On Windows, run `gradlew.bat` instead of `./gradlew`.

### 4. Build a debug APK

```bash
./gradlew :app:assembleDebug
```

### 5. Run unit tests

```bash
./gradlew :app:testDebugUnitTest
```

### 6. Install on a connected device

```bash
./gradlew :app:installDebug
```

APK outputs are generated under `app/build/outputs/apk/`. Depending on the configured ABI splits and build variant, the output directory may contain more than one APK.

## Download the Android APK

<div align="center">

### Director X · Android builds

**Download the APK produced by the latest successful workflow run.**

[![Open APK build history](https://img.shields.io/badge/OPEN_APK_BUILDS-View_downloadable_artifacts-C79B2E?style=for-the-badge&logo=android&logoColor=2D2824)](https://github.com/AssayFlux/Director-X/actions/workflows/build-debug-apk.yml)

</div>

1. Open the [Build Debug APK workflow](https://github.com/AssayFlux/Director-X/actions/workflows/build-debug-apk.yml).
2. Select the latest run marked **Success**.
3. Scroll to **Artifacts**.
4. Download the `director-x-debug-apk` artifact.
5. Extract the ZIP and install the APK on your Android device.

**Important:** Workflow artifacts are temporary and currently have a 14-day retention period. This is an Actions artifact download, not a permanent GitHub Release. A failed or in-progress run will not provide a successful APK artifact.

## Repository structure

```text
Director-X/
├── .github/
│   └── workflows/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/directorx/editor/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   ├── debug/
│   │   ├── qa/
│   │   ├── streaming/
│   │   ├── test/
│   │   ├── androidTest/
│   │   └── androidTestQa/
│   └── build.gradle.kts
├── baselineprofile/
├── docs/
├── gradle/
│   ├── wrapper/
│   └── libs.versions.toml
├── scripts/
├── assets/
│   ├── icon.svg
│   └── icon.png
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── README.md
```

## Development roadmap

| Status | Milestone |
|---|---|
| Complete | Native Android application foundation |
| Complete | Compose and Material 3 interface foundation |
| Complete | Media playback, transformation, effects and muxing integration |
| Complete | Local persistence and background-work infrastructure |
| Complete | Local AI and audio-processing integrations |
| Complete | Manual debug APK workflow |
| Planned | Validate builds across physical Android devices |
| Planned | Continue refining editing and export workflows |
| Planned | Unify local AI engine controls and capability management |
| Planned | Improve performance and memory behavior on constrained devices |
| Planned | Prepare signed release distribution when credentials are configured |

## Contributing

Contributions, bug reports, and focused improvements are welcome.

Before opening a pull request:

- Search [existing issues](https://github.com/AssayFlux/Director-X/issues).
- Include reproduction steps, device model, Android version, and relevant logs.
- Discuss substantial changes before implementation.
- Submit focused pull requests against `main`.
- Include tests or verification steps for behavior changes.

## License

See the repository's [LICENSE](LICENSE) file for the terms that apply to this project. Third-party components remain subject to their respective licences and notices.

---

<div align="center">

<img src="assets/icon.svg" alt="Director X icon" width="64" />

### DIRECTOR X

**Designed by AssayFlux**

[Repository](https://github.com/AssayFlux/Director-X) · [Issues](https://github.com/AssayFlux/Director-X/issues) · [Builds & APK](https://github.com/AssayFlux/Director-X/actions/workflows/build-debug-apk.yml)

*Crafted for creators. Engineered for Android.*

</div>
