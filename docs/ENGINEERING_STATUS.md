# Director X — Engineering status

**Branch:** `Alpha`  
**Current source baseline before P1 CI integration:** `bd7875884a600f1ea30511d6ee5269c6aaabde9c`  
**Application:** Director X  
**Version:** `v0.1.0` (`versionCode 1`)  
**Application ID:** `com.directorx.editor`

This record distinguishes source implementation, automated verification, and physical-device acceptance. A source file or green unit-test job alone does not establish end-to-end product acceptance.

## Current verification evidence

| Area | Evidence | State |
|---|---|---|
| FFmpeg native build | Native workflow run `44905134014`; five Android ABIs passed | **Passed** |
| FFmpeg AAR assembly | Assembly workflow run `44909600734` passed | **Passed** |
| FFmpeg promotion | Promotion workflow run `44909728954` promoted the 9.0.2 artifact to `Alpha` | **Passed** |
| QA app integration | Run [44914650116](https://github.com/AssayFlux/Director-X/actions/runs/44914650116) on `bd7875884a600f1ea30511d6ee5269c6aaabde9c`; AAR verification, 1,852 JVM tests, QA assembly, 16 KB APK alignment and artifact upload passed | **Passed** |
| Android lint | Not included in run 44914650116; now added to `android-full-ci.yml` | **New gate; run required** |
| Managed-device instrumentation | Not included in run 44914650116; now added to `android-full-ci.yml` | **New gate; run required** |
| Native SBOM and resolved advisory floors | Gradle tasks exist but were not invoked by run 44914650116; now added to `android-full-ci.yml` | **New gate; run required** |
| QA APK artifacts | Run 44914650116 uploaded QA APKs with checksums; artifacts expire after 14 days | **Passed** |
| Physical-device acceptance | No current recorded physical-device test evidence | **Pending maintainer testing** |
| Production release signing | Must use the established private signing identity | **Not demonstrated by QA workflow** |

## CI contract

`.github/workflows/android-full-ci.yml` is the general Android verification workflow. It separates static/supply-chain checks, JVM unit tests, API 37 managed-device instrumentation, and QA packaging into independent jobs. It runs on pushes and pull requests targeting `Alpha`, and can be started manually.

The older `ffmpeg-9-app-verify.yml` remains the focused FFmpeg/app integration lane. A pass in that workflow must not be represented as a pass of lint, managed-device instrumentation, or the native SBOM/advisory tasks.

## Native media dependency

- FFmpegKitNext / FFmpeg: **9.0.2**.
- AAR size: 62,703,876 bytes.
- AAR SHA-256: `44e96b4cb4a1b35e7a38d8cdc1a9707aaae17fffd83cee7f1da56663bd87b2f6`.
- The five-ABI native build, AAR assembly, and promotion workflows passed.
- Advisory inventory reconciliation was recorded in `third_party/ffmpeg-kit-next/native-lock.json` on 2026-11-21, with review horizon 2027-02-19.
- The advisory reconciliation is not an independent binary security certification. The new CI lane must run the native SBOM/provenance verifier and resolved dependency advisory-floor task; independent binary reachability evidence remains a separate acceptance concern.

## Product and compatibility acceptance still pending

- Room schema and migration source are checked in; managed-device instrumentation is now a CI gate. Real upgrade-path acceptance against supported historical user databases remains pending until that test evidence is reviewed.
- Custom canvas dimensions are implemented in source; full persisted-project/archive/preview/export/final-media round-trip acceptance is not yet recorded.
- Proxy generation policy and worker implementation are present; cancellation, retry/recovery, low-storage, playback/export concurrency, and original-media-only export need device-level acceptance.
- Physical-device playback, proxy, export, navigation, large-project, and performance checks remain maintainer responsibilities.
- Release signing identity must be verified before a production release.

## Repository policy and boundaries

- Preserve application ID `com.directorx.editor`, version, version code, established signing identity, database/archive compatibility, and required third-party notices.
- Do not claim physical-device acceptance, independent native security certification, or production release readiness without retained evidence.
- CI QA APKs are validation artifacts only; production signing remains a separate controlled release operation.
