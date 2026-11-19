# Stage 5 — Release readiness

## Purpose

Stage 5 is the final release-candidate gate. APK packaging is reserved for an explicit manual release-candidate run after source, test, package-policy, and device checks pass. Pull-request validation runs tests and lint without assembling APKs.

## Media pipeline

Director X uses Android platform media APIs and AndroidX Media3:
- Media3 Transformer for video export, proxy generation, mixed-render concatenation, image-sequence assembly, and timeline text effects.
- MediaExtractor and MediaCodec for audio decode and platform AAC encoding.
- MediaMuxer for platform container assembly and stream-copy paths.
- DeepFilterNet 3 with the spectral-gate fallback for on-device noise reduction.

The bundled FFmpegKit/FFmpeg AAR and its associated source-offer, native-lock, and security-review artifacts have been removed. Do not reintroduce a bundled command-line transcoder as a shortcut. Any new media capability must use Media3 or Android platform codecs where supported, and must fail clearly when a device cannot provide the required codec.

Reverse video rendering is not currently available: Media3 Transformer has no public reverse-playback effect. Export preflight must disclose this limitation and obtain the user's acceptance before using the documented forward-playback fallback. Do not claim that a reversed clip was rendered when it was not.

## Automated gates

The `.github/workflows/release-candidate.yml` workflow:
1. Validates package identity, version consistency, public claims, and APK/alignment validator self-tests.
2. Runs QA unit tests and Android lint on pull requests.
3. Builds debug APKs only for an explicit `workflow_dispatch` release-candidate run.
4. Validates APK metadata and native ABI contents, checks 16 KB ELF LOAD alignment, and uploads the APKs and runtime open-source notice inventory.

The workflow does not sign a release with a repository-stored or fallback key. A production release must use the established private signing identity in the maintainer's secure environment.

## Remaining release acceptance

- Resolve all QA unit-test, lint, and instrumentation failures.
- Complete physical-device acceptance for playback, proxy generation, audio processing, inpainting, subtitle overlays, export, navigation, and large projects.
- Confirm unsupported reverse-render behavior is clearly disclosed in preflight and UI.
- Run startup/editor/export Macrobenchmarks on recorded hardware and retain results.
- Verify the release signing certificate matches the established published identity.
- Build and inspect the final release-signed APK set after all gates pass.
- Install and smoke-test the exact release APKs on supported physical devices.

## APK build policy

Pull-request validation must not assemble APKs. The automated workflow builds debug-signed APKs only when manually dispatched for release-candidate validation. The production release-signed APK must be built by the maintainer with the established private key. No keystore or password belongs in the repository.

## Current status

The FFmpeg removal and platform-media migration are in progress. Release readiness is **not established** until the remaining migration work, test gates, and physical-device acceptance are complete. This document makes no release-ready claim.
