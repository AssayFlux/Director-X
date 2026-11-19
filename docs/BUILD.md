# Director X — Build Guide

## Approved identity

- App: Director X
- Version: `v0.1.0`
- Android version code: `1` (release notation: `001`)
- Application ID: `com.directorx.editor`
- Java: 17
- Build system: Gradle wrapper

## Maintainer build

The maintainer builds APKs manually. CI is used for source verification and tests, not APK publishing.

From the repository root:

```sh
chmod +x ./gradlew
./gradlew --version
./gradlew :app:assembleDebug
```

APK outputs are under:

```
app/build/outputs/apk/
```

Build the ABI-specific or universal artifacts using the Gradle split configuration already defined by the project. Inspect the output APK's package name, version name, version code, signing certificate, supported ABIs, and native library alignment before installation.

## Release signing

Release builds must use the established signing identity. Configure secrets locally or through the maintainer's secure environment using the `DIRECTORX_*` environment-variable names defined in `app/build.gradle.kts`. Never commit keystore files or passwords.

A release build must fail rather than silently fall back to a different signing key.

## Android SDK

Configure the Android SDK using the local Android development environment. Do not commit machine-specific `local.properties`. Ensure the installed SDK includes the project's compile/target SDK and required build tools.

## Verification

```sh
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

Run the repository's verification scripts where relevant. A successful build is not evidence of device playback, proxy, custom-canvas, or export acceptance. See [ENGINEERING_STATUS.md](ENGINEERING_STATUS.md).
