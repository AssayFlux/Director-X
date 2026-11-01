# Build Director X in Termux

This guide documents the maintainer's manual Android build workflow. GitHub Actions does not build APKs, run Android tests, or run Android lint.

> **Important:** Android SDK/Gradle builds on Android depend on the host toolchain. A normal desktop Linux SDK installation is not automatically executable on Android's Bionic environment. Use a Termux-compatible Android SDK/build-tools installation (for example, one supplied by a compatible Android IDE/build environment) and confirm that its tools actually execute before attempting a full build. Do not assume that installing the `android-tools` package alone provides the Android SDK.

## Project configuration

| Setting | Value |
|---|---|
| Application ID | `com.directorx.editor` |
| Minimum Android API | 26 |
| Compile / target API | 37 |
| Java toolchain | 17 |

The Android application identity and required build-tool versions are defined by the Gradle project files. Read those files before selecting SDK packages; this guide deliberately avoids copying version numbers that can become stale.

## 1. Prepare Termux

Install Termux from a trusted current distribution and update its packages:

```bash
pkg update && pkg upgrade
pkg install git openjdk-17 wget curl unzip zip tar python
```

If your Termux repository provides the package, install Android platform tools for ADB:

```bash
pkg install android-tools
```

Confirm Java:

```bash
java -version
javac -version
```

Use the Java toolchain required by the current Gradle project. If a different Java version is selected, correct `JAVA_HOME` and `PATH` before using Gradle.

## 2. Prepare a Termux-compatible Android SDK

You need a working Android SDK installation that is executable from Termux and includes:

- The Android SDK platform required by `compileSdk` in `app/build.gradle.kts`.
- The Android SDK Build Tools version required by the Android Gradle Plugin configuration.
- Platform Tools.
- A working `aapt2`, `aidl`, `zipalign` and other build-tools executables required by AGP.

Set the SDK location to the actual path provided by your Android development environment. Example only:

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin"
```

Do not copy this example path unless it exists on your device. Check the installation:

```bash
find "$ANDROID_HOME/platforms" -maxdepth 1 -mindepth 1 -type d -print
find "$ANDROID_HOME/build-tools" -maxdepth 1 -mindepth 1 -type d -print
command -v adb || true
```

If your SDK manager is compatible with Termux, install the required packages and accept their licences using that manager. If its executables fail with an ELF, linker, or Bionic/glibc error, stop: that SDK distribution is not a usable Termux build toolchain. Use a Termux-compatible SDK provider rather than repeatedly retrying Gradle.

## 3. Clone the repository

```bash
git clone https://github.com/AssayFlux/Director-X.git
cd Director-X
chmod +x gradlew
```

For an existing checkout:

```bash
cd ~/Director-X
git fetch origin
git switch main
git pull --ff-only origin main
```

## 4. Configure the local SDK path

Create `local.properties` in the repository root. Replace the example with your actual SDK path:

```bash
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
```

This file is machine-specific and must not be committed.

## 5. Check Gradle and Android toolchain

```bash
java -version
./gradlew --version
printf 'ANDROID_HOME=%s\n' "$ANDROID_HOME"
```

If Gradle cannot launch, first resolve the Java version or executable compatibility issue. If Gradle reports missing SDK platforms or build tools, verify the SDK paths and tool binaries before retrying.

## 6. Build a debug APK

```bash
./gradlew --no-daemon --stacktrace --console=plain :app:assembleDebug
```

The build may take considerable time and use substantial RAM, storage and battery. Keep the device cool, close memory-heavy apps, maintain free storage, and avoid interrupting native dependency configuration.

Find generated APKs:

```bash
find app/build/outputs/apk -type f -name '*.apk' -print
```

The normal debug output directory is:

```text
app/build/outputs/apk/debug/
```

ABI splits or variant configuration may produce more than one APK. Inspect the generated filenames and device ABI before installation.

## 7. Install and inspect

With a working ADB connection:

```bash
adb devices
./gradlew :app:installDebug
```

Alternatively, copy the APK to shared storage and install it through Android's package installer:

```bash
termux-setup-storage
cp app/build/outputs/apk/debug/*.apk ~/storage/downloads/
```

If multiple APKs are generated, copy/install the correct APK for your device rather than blindly selecting the first file.

## 8. Optional local QA commands

These commands are available for deliberate maintainer-run checks. They are not run automatically by GitHub Actions:

```bash
./gradlew :app:testQaUnitTest
./gradlew :app:lint
```

Instrumentation tests require a compatible connected device or emulator and must be invoked with the appropriate Gradle task for the configured test variant. Check registered tasks before running a guessed task:

```bash
./gradlew tasks --all | grep -E 'connected.*AndroidTest|AndroidTest|test.*UnitTest'
```

## 9. Capture useful diagnostics

For a Gradle failure:

```bash
./gradlew :app:assembleDebug --no-daemon --stacktrace --info 2>&1 | tee directorx-build.log
```

For an app crash after installation:

```bash
adb logcat -c
adb shell monkey -p com.directorx.editor 1
adb logcat -d -v threadtime > directorx-logcat.txt
```

Share the complete failing Gradle task and relevant logcat section when reporting a problem. Remove personal paths, tokens, signing secrets and private media before sharing logs.

## Release and signing caution

A debug APK is for local testing. Do not distribute it as a production release. A production update must use the established release application ID and the original release signing identity. Never commit keystores, passwords, API keys, signing properties or private credentials.

## Scope of GitHub Actions

Only repository-hygiene checks remain in GitHub Actions. Android SDK setup, Gradle builds, APK creation, Android unit tests, instrumentation tests and lint are intentionally manual maintainer tasks.
