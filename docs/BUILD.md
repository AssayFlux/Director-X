# Director X — Build guide

## Approved identity

- App: Director X
- Version: `v0.1.0`
- Android version code: `1` (release notation: `001`)
- Application ID: `com.directorx.editor`
- Java: 17
- Build system: Gradle wrapper

## Maintainer build

From the repository root:

```sh
chmod +x ./gradlew
./gradlew --version
./gradlew :app:assembleDebug
```

APK outputs are under `app/build/outputs/apk/`.

## CI verification

The general Android CI workflow is `.github/workflows/android-full-ci.yml`. It runs independent jobs for native SBOM/provenance and resolved advisory checks, Android lint, JVM unit tests, API 37 managed-device instrumentation (including Android migration tests), and QA APK packaging with native 16 KB alignment checks.

The focused FFmpeg integration workflow remains `.github/workflows/ffmpeg-9-app-verify.yml`; it is not a replacement for the full-app workflow.

A successful QA run uploads debug/QA APKs and a SHA-256 manifest to the GitHub Actions run's Artifacts section. These artifacts are temporary validation builds, not production releases.

## Android SDK

Configure the Android SDK using the local Android development environment. Do not commit machine-specific `local.properties`. CI installs compile SDK 37 and the required build tools.

## Release signing

Release builds must use the established signing identity. Configure secrets locally or through the maintainer's secure environment using the `DIRECTORX_*` environment-variable names defined in `app/build.gradle.kts`. Never commit keystore files or passwords.

A release build must fail rather than silently fall back to a different signing key.

## Verification commands

```sh
./gradlew :app:generateNativeSbom
./gradlew :app:verifyResolvedAdvisoryFloors
./gradlew :app:lintQa
./gradlew :app:testQaUnitTest
./gradlew :app:directorXApi37QaAndroidTest
./gradlew :app:assembleQa
```

Run `python scripts/check_16kb_alignment.py <apk>` on each packaged APK. A successful build or emulator run is not evidence of physical-device playback, proxy, custom-canvas, or export acceptance. See [ENGINEERING_STATUS.md](ENGINEERING_STATUS.md) and [RELEASE_READINESS.md](RELEASE_READINESS.md).
