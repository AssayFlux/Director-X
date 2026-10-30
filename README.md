# Director X

<div align="center">

**A powerful Android video-editing studio built for precise creative workflows.**

Director X combines a modern native Android interface with a capable media-processing foundation and on-device intelligent tools.

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-blue)](https://developer.android.com/)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-37-blue)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Version](https://img.shields.io/badge/Version-v0.1.0-informational)](https://github.com/AssayFlux/Director-X)
[![Build](https://img.shields.io/badge/Build-Manual%20APK-lightgrey)](https://github.com/AssayFlux/Director-X/actions)

**Designed by AssayFlux**

</div>

---

## Overview

Director X is a native Android video editor built with Kotlin and Jetpack Compose. It brings together media playback, editing and export infrastructure, local project data, and on-device intelligent media capabilities in one application.

## Key Features

### Editing and Media
- **Media playback** — Native playback foundation for previewing media during editing.
- **Media transformation** — Video transformation and export workflows using Android media APIs.
- **Effects pipeline** — Media effects infrastructure for visual processing.
- **Media muxing** — Output composition and media muxing support.
- **Media import and sharing** — Android content intents for opening and receiving supported media and project-related files.

### AI and Audio
- **On-device inference** — Local model execution through the integrated inference stack.
- **Media intelligence** — Vision and media-processing capabilities for supported workflows.
- **Audio enhancement** — Integrated audio-processing functionality.
- **Local-first AI** — AI functionality is designed around locally integrated engines, without requiring a cloud AI service.

### Application Experience
- **Modern interface** — Jetpack Compose and Material 3.
- **Persistent data** — Room database and DataStore preferences.
- **Background work** — WorkManager and Android foreground-service infrastructure.
- **Dependency injection** — Hilt-based application dependency management.
- **Performance tooling** — Baseline profile and performance measurement infrastructure.

### Security and Offline Functionality
- **Private app storage** — App-managed data is stored within Android application storage.
- **Local processing** — Core media and AI workflows are designed to run on-device.
- **Network policy** — Network permissions and cleartext-traffic policy are declared explicitly.
- **Build integrity** — Build-time integrity checks are included for pinned build inputs.
- **Free application** — Billing functionality is not part of the current product plan.

## Tech Stack and Architecture

| Area | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Design system | Material 3 |
| Media playback | AndroidX Media3 ExoPlayer |
| Media transformation | AndroidX Media3 Transformer |
| Effects and output | Media3 Effects and Muxer |
| Local database | Room |
| Preferences | DataStore |
| Background tasks | WorkManager |
| Dependency injection | Hilt |
| Image loading | Coil |
| On-device inference | ONNX Runtime |
| Media and vision | MediaPipe |
| Audio processing | DeepFilterNet |
| Build system | Gradle |
| Android Gradle Plugin | 9.4.1 |
| Kotlin plugin | 2.4.10 |
| Java toolchain | 17 |

### Architecture

The codebase separates UI, application state, persistence, and media-processing responsibilities.

```text
Presentation
  Jetpack Compose · Material 3
          |
          v
Application and UI State
  Screens · ViewModels · Flows
          |
          v
Data
  Repositories · Room · DataStore
          |
          v
Media and AI Processing
  Media3 · Native processing · AI engines
          |
          v
Android Platform
  Storage · Services · WorkManager
```

## Build Configuration

| Setting | Value |
|---|---|
| Application ID | `com.directorx.editor` |
| Kotlin namespace | `com.directorx.editor` |
| Version | `v0.1.0` |
| Version code | `301` |
| Minimum Android API | `26` |
| Target Android API | `37` |
| Compile SDK | `37` |
| Java | `17` |
| Gradle wrapper | `9.8.0` |
| Android Gradle Plugin | `9.4.1` |
| Kotlin | `2.4.10` |

## Getting Started

### Requirements
- JDK 17
- Android SDK Platform 37
- Android SDK Build Tools compatible with the project
- Git
- Android device or emulator running API 26 or newer

### 1. Clone the repository

```bash
git clone https://github.com/AssayFlux/Director-X.git
cd Director-X
```

### 2. Configure the Android SDK

Set `ANDROID_HOME` or `ANDROID_SDK_ROOT`. If needed, create `local.properties`:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

Do not commit machine-specific SDK paths.

### 3. Verify Java and Gradle

```bash
java -version
./gradlew --version
```

Use JDK 17. On Windows, use `gradlew.bat` instead of `./gradlew`.

### 4. Build the debug APK

```bash
./gradlew :app:assembleDebug
```

APK output:

```text
app/build/outputs/apk/
```

The configured ABI split may produce multiple APK files, including a universal APK.

### 5. Run unit tests

```bash
./gradlew :app:testDebugUnitTest
```

### 6. Install on a connected device

Enable USB debugging, connect the device, and run:

```bash
./gradlew :app:installDebug
```

## Build an APK with GitHub Actions

The GitHub Actions workflow builds a debug APK automatically when changes are pushed to `main`, and can also be started manually.

**[Open the latest build runs and download the APK artifact](https://github.com/AssayFlux/Director-X/actions/workflows/build-debug-apk.yml).**

For the current successful build, open [Build Debug APK run #10](https://github.com/AssayFlux/Director-X/actions/runs/41046091269), scroll to **Artifacts**, and download `director-x-debug-apk`.

To start another build manually:
1. Open the repository's **Actions** tab.
2. Select **Build Debug APK**.
3. Select **Run workflow** and choose the build variant.
4. After the run succeeds, download the APK from its **Artifacts** section.

Artifacts are temporary GitHub Actions downloads and expire after 14 days. The workflow does not publish a permanent GitHub Release.

## Project Structure

```text
Director-X/
├── .github/
│   └── workflows/
│       ├── build-debug-apk.yml
│       └── repository-hygiene.yml
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/directorx/editor/
│   │   │   │   ├── ai/
│   │   │   │   ├── engine/
│   │   │   │   ├── model/
│   │   │   │   ├── ui/
│   │   │   │   ├── ClearCutApp.kt
│   │   │   │   └── MainActivity.kt
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
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── README.md
```

## Roadmap

- [x] Native Android application foundation
- [x] Jetpack Compose and Material 3 interface foundation
- [x] Media playback, transformation, effects, and muxing integration
- [x] Local persistence and background-work infrastructure
- [x] Local AI and audio-processing integrations
- [x] Manual debug APK workflow
- [ ] Validate debug builds on a range of physical Android devices
- [ ] Continue refining editing and export workflows
- [ ] Unify local AI engine controls and capability management
- [ ] Improve performance and memory behavior on resource-constrained devices
- [ ] Prepare signed release distribution when release credentials are supplied

## Contributing

1. Search [existing issues](https://github.com/AssayFlux/Director-X/issues) before reporting a problem.
2. Include reproduction steps, device model, Android version, and relevant logs.
3. Discuss substantial changes in an issue before implementation.
4. Submit focused pull requests against `main`.
5. Include tests or verification steps for behavior changes.

## License

See the repository's `LICENSE` file for the terms that apply to this project.

---

<div align="center">

**Director X — Designed by AssayFlux**

[Repository](https://github.com/AssayFlux/Director-X) · [Issues](https://github.com/AssayFlux/Director-X/issues)

</div>
