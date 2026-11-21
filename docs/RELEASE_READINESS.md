# Release readiness

## Current baseline

- Branch: `Alpha`
- FFmpegKitNext: FFmpeg 9.0.2
- Vendored AAR SHA-256: `44e96b4cb4a1b35e7a38d8cdc1a9707aaae17fffd83cee7f1da56663bd87b2f6`
- Latest recorded QA verification before the full-app workflow: run [44914650116](https://github.com/AssayFlux/Director-X/actions/runs/44914650116), commit `bd7875884a600f1ea30511d6ee5269c6aaabde9c`.
- That run passed `:app:verifyFfmpegKitAar`, `:app:testQaUnitTest` (1,852 tests), `:app:assembleQa`, packaged-native 16 KB alignment checks, and QA APK artifact upload.
- It did **not** run Android lint, Android emulator instrumentation, or the native SBOM/advisory Gradle tasks.

## Automated release-candidate gates

The dedicated `.github/workflows/android-full-ci.yml` workflow runs independent jobs for:

1. Native SBOM/provenance generation and resolved dependency advisory-floor verification.
2. Android `lintQa`.
3. The complete `testQaUnitTest` JVM suite.
4. Android emulator instrumentation (API 35) on the configured API 35 emulator. This includes the checked-in Android instrumentation tests, including Room migration coverage where present.
5. QA APK assembly, FFmpegKitNext AAR checksum verification, native 16 KB alignment checks, SHA-256 manifest generation, and artifact upload.

A successful QA artifact is a validation build, not a production release. The production release must use the established private signing identity; no keystore or password belongs in the repository.

## Native FFmpeg security posture

FFmpeg 9.0.2 promotion and its five-ABI native build completed successfully. The native lock's advisory inventory was reconciled against the upstream FFmpeg 9.0 security record on 2026-11-21, with review horizon recorded through 2027-02-19.

That reconciliation is a source/advisory review record, not an independent binary security certification. The exact packaged component reachability and native binary surface still require the configured native supply-chain verifier and, where stated by the verifier/reviewer, independent binary evidence. Do not describe the native package as independently security-certified until that evidence exists.

## Remaining release acceptance

- Require the new full-app workflow to pass on the exact release-candidate commit.
- Review and retain native SBOM, resolved dependency report, and advisory-floor results.
- Complete physical-device acceptance for playback, proxy generation/recovery, export, navigation, and large projects on supported hardware.
- Verify the established release signing certificate.
- Build, inspect, install, and smoke-test the exact release-signed APK set.
- Record device model, OS/build fingerprint, test fixtures, output metadata, and results.

## Artifact policy

CI-generated QA APKs are debug/QA validation artifacts only. They are downloadable from the successful GitHub Actions run's Artifacts section and expire according to the workflow retention setting. They are not signed production releases and are not a substitute for maintainer release acceptance.
