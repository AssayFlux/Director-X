<div align="center">

<p>
  <img src="assets/icon.svg" alt="Director X application icon" width="176" />
</p>

# DIRECTOR X

### CINEMATIC VIDEO EDITING FOR ANDROID

**Designed by AssayFlux**

A native Android video editor for media creation, timeline editing, intelligent tools and local-first processing.

<p>
  <a href="https://github.com/AssayFlux/Director-X/releases/latest"><strong>⬇ DOWNLOAD LATEST APK</strong></a>
  &nbsp; · &nbsp;
  <a href="https://github.com/AssayFlux/Director-X">SOURCE CODE</a>
  &nbsp; · &nbsp;
  <a href="https://github.com/AssayFlux/Director-X/issues">REPORT AN ISSUE</a>
</p>

<!-- Live badges: workflow badges report real workflow conclusions; version is checked against Gradle. -->
[![APK build, tests and lint](https://img.shields.io/github/actions/workflow/status/AssayFlux/Director-X/build-debug-apk.yml?branch=main&label=APK%20BUILD%20%2B%20TESTS%20%2B%20LINT&logo=android&logoColor=white&style=for-the-badge)](https://github.com/AssayFlux/Director-X/actions/workflows/build-debug-apk.yml)
[![Repository validation](https://img.shields.io/github/actions/workflow/status/AssayFlux/Director-X/repository-hygiene.yml?branch=main&label=REPOSITORY%20VALIDATION&logo=githubactions&logoColor=white&style=for-the-badge)](https://github.com/AssayFlux/Director-X/actions/workflows/repository-hygiene.yml)
[![Latest release](https://img.shields.io/github/v/release/AssayFlux/Director-X?label=LATEST%20RELEASE&logo=github&style=for-the-badge)](https://github.com/AssayFlux/Director-X/releases/latest)

[![Application version](https://img.shields.io/badge/APP%20VERSION-3.82.0-C29B38?logo=android&logoColor=2C2621&style=for-the-badge)](https://github.com/AssayFlux/Director-X/blob/main/app/build.gradle.kts)
[![Android](https://img.shields.io/badge/PLATFORM-ANDROID-3DDC84?logo=android&logoColor=white&style=for-the-badge)](https://developer.android.com/)
[![Minimum SDK](https://img.shields.io/badge/MIN%20SDK-26-52796F?logo=android&logoColor=white&style=for-the-badge)](https://developer.android.com/about/versions)
[![Target SDK](https://img.shields.io/badge/TARGET%20SDK-37-52796F?logo=android&logoColor=white&style=for-the-badge)](https://developer.android.com/about/versions)

[![Kotlin](https://img.shields.io/badge/KOTLIN-2.4.10-7F52FF?logo=kotlin&logoColor=white&style=for-the-badge)](https://kotlinlang.org/)
[![Java](https://img.shields.io/badge/JAVA-17-ED8B00?logo=openjdk&logoColor=white&style=for-the-badge)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/GRADLE-9.8.0-02303A?logo=gradle&logoColor=white&style=for-the-badge)](https://gradle.org/)
[![License](https://img.shields.io/github/license/AssayFlux/Director-X?label=LICENSE&style=for-the-badge)](https://github.com/AssayFlux/Director-X/blob/main/LICENSE)

[![Latest commit](https://img.shields.io/github/last-commit/AssayFlux/Director-X/main?label=LATEST%20COMMIT&logo=git&logoColor=white&style=for-the-badge)](https://github.com/AssayFlux/Director-X/commits/main)
[![Stars](https://img.shields.io/github/stars/AssayFlux/Director-X?label=STARS&style=for-the-badge)](https://github.com/AssayFlux/Director-X/stargazers)
[![Forks](https://img.shields.io/github/forks/AssayFlux/Director-X?label=FORKS&style=for-the-badge)](https://github.com/AssayFlux/Director-X/network/members)
[![Open issues](https://img.shields.io/github/issues/AssayFlux/Director-X?label=OPEN%20ISSUES&style=for-the-badge)](https://github.com/AssayFlux/Director-X/issues)

Android app version: `3.82.0` · versionCode `301`

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
| Android build `versionName` | `3.82.0` (Gradle/package metadata) |
| Android build `versionCode` | `301` |
| Minimum SDK | API 26 |
| Compile SDK | API 37 |
| Target SDK | API 37 |
| Java | 17 |
| Gradle wrapper | 9.8.0 |
| Android Gradle Plugin | 9.4.1 |
| Kotlin | 2.4.10 |

### Android 17 audio hardening smoke test

For API 37 devices and emulators, verify that audio capture and playback remain tied to visible user actions:

1. Enable strict audio hardening:
   `adb shell cmd audio set-hardening throw`
2. Open Director X and test a **TTS preview** from the visible editing interface.
3. Start and stop a **voiceover** recording while the app is in the foreground.
4. Run an **export** and confirm the export foreground service does not request microphone or media-playback service types.
5. Inspect system logs for `AudioHardening` messages and investigate any thrown policy violations.

This smoke test is separate from the APK compilation and JVM unit-test steps.

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

### Director X · Permanent public release

[![Download latest APK](https://img.shields.io/badge/DOWNLOAD-LATEST%20APK-C29B38?style=for-the-badge&logo=android&logoColor=2C2621)](https://github.com/AssayFlux/Director-X/releases/latest)

**Stable download page:** [github.com/AssayFlux/Director-X/releases/latest](https://github.com/AssayFlux/Director-X/releases/latest)

</div>

The latest successful **debug** build from the `main` branch is published as a GitHub Release only after strict dependency verification, QA unit tests, lint, APK signature verification and package/version metadata validation succeed.

Each release includes:
- APK outputs for the supported ABI packaging configuration.
- `SHA256SUMS.txt` for integrity verification.
- `build-info.txt` with the source commit, build date, application version and variant.
- Release notes identifying the exact source commit.

QA and streaming builds are validation builds and are not automatically published as public releases. Failed or unvalidated builds are never promoted to the latest release.

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
| Complete | Strictly validated APK build and permanent GitHub Release pipeline |
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

[Repository](https://github.com/AssayFlux/Director-X) · [Issues](https://github.com/AssayFlux/Director-X/issues) · [Latest APK](https://github.com/AssayFlux/Director-X/releases/latest)

*Crafted for creators. Engineered for Android.*

</div>
