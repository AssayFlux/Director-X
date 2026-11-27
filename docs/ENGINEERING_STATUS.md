# Director X Engineering Status

**Status snapshot:** 27 November 2026  
**Repository:** `AssayFlux/Director-X`  
**Branch audited:** `Alpha`  
**Audit basis:** `Director-X-Deep-Audit-Report.md` supplied for the 27 November 2026 audit.

This document tracks engineering verification separately from product capability claims. A source file, UI control, static validator, or CI workflow definition is not evidence that the corresponding Android runtime path passed.

## Current verification status

| Area | Status | Evidence / next action |
|---|---|---|
| Repository inventory and static syntax | Pass in supplied audit | 899 files inventoried; XML, JSON and Python syntax checks passed. |
| Python regression suite | Pass in supplied audit | 21/21 tests passed. Re-run in CI for every change. |
| Branding, package identity, public claims, localization and native supply-chain policy | Pass in supplied audit | Existing validators passed; native verifier covered 15 components. |
| Android compilation, lint and JVM tests | Not verified by supplied audit | The audit environment could not download Gradle 9.8.0 from `services.gradle.org`. The hosted Android CI workflow is the authoritative execution path. |
| Android instrumentation and Room migration tests | Not verified by supplied audit | Run the emulator/managed-device jobs and retain their reports. |
| APK/AAB packaging, size and native alignment | Not verified by supplied audit | Build the exact candidate artifacts, run the size budget, 16 KB alignment and signing-fingerprint checks against those artifacts. |
| OpenTimelineIO interoperability | Not verified by supplied audit | Install the pinned dependency from `scripts/requirements.txt`; run the adapter self-test and validate actual Director X exports. |
| Release version-code monotonicity | **Release blocker — external confirmation required** | Gradle currently declares version code 1. Read the highest version code ever uploaded to the target Play application, then set the next greater integer. Do not guess from the marketing version or Git tag. |
| Release signing identity and custody | **Release blocker — owner verification required** | The pinned SHA-256 is `76cf4c647590f1dc17b37175b9dd65cac6d0a1e7ffff6a86f8ea8a29f67f75f6`. The recorded alias/subject are debug-named, but the archive does not prove whether the private key is a standard debug key or a dedicated historical release key. Compare the digest with Play Console and a trusted keystore copy; document custody and recovery before any rotation. |
| Privacy / Play Data Safety | Engineering reference only | Reconcile the exact merged release manifest, bundled SDK inventory and observed network behaviour with the submitted Play Data Safety declaration. |

## Release signing: verification and recovery procedure

1. In Play Console, identify whether Play App Signing is enabled and record the app-signing certificate and upload certificate fingerprints separately.
2. Obtain the historical keystore through the owner's trusted secret-storage process. Never commit the keystore, passwords, or their path.
3. Inspect the keystore certificate with `keytool -list -v` and compare its SHA-256 digest with the pinned fingerprint above and the relevant Play Console certificate.
4. Confirm who can access the private key, where encrypted backups are held, and how access is recovered. Record custodians and the last successful recovery test in the private release runbook, not in this public repository.
5. Keep `app/release-signing-identity.json` and the Gradle fingerprint gate unchanged unless a deliberate, Play-supported signing-key migration has been approved and its user-update implications are understood.

The certificate fingerprint is public identity metadata. The private keystore and credentials are secrets and must remain outside Git.

## Version-code procedure

The current source value is `versionCode = 1`; the supplied audit cannot determine the previous maximum Play version code. Before producing a Play upload:

1. Read the maximum version code across all tracks and uploaded artifacts for `com.directorx.editor` in Play Console.
2. Set `versionCode` to a literal integer strictly greater than that maximum.
3. Run `scripts/validate_version_consistency.py` and the full Android release pipeline.
4. Record the chosen value and the Play Console evidence in the private release record.

Do not use the example version code from the audit report.

## Planned capabilities — not current availability claims

The following remain explicitly planned in the product capability registry and public README: AI Green Screen / RobustVideoMatting, Video Upscaling / Real-ESRGAN, Frame Interpolation / RIFE, Style Transfer / AnimeGAN/Fast NST, Tap-to-Segment / SAM 2.1 or MobileSAM, and the future Sherpa-ONNX Moonshine v2 ASR path. Keep them labelled Planned until model integration, runtime gating, tests and device acceptance are complete.

The normal build also disables the update-check and local-network-streaming flags; the QA timeline harness is enabled only in the QA variant. Cloud generative AI remains conditional on provider configuration and consent.

## Security and privacy acceptance still required

- Exercise incoming ZIP, XML, JSON and media import paths with malformed, oversized and adversarial fixtures on Android instrumentation.
- Run 16 KB ELF alignment and APK/AAB size checks on the exact candidate artifact, not only on source AARs.
- Inspect the merged manifest and SDK inventory for the exact release variant.
- Observe network traffic with optional metrics consent both disabled and enabled; verify media, projects and diagnostics are not transmitted without the disclosed user action.
- Reconcile the observations with `docs/privacy-policy.md` and `docs/play-data-safety.md` before Play submission.

## CI and release evidence

The repository's `.github/workflows/android-full-ci.yml` defines static/supply-chain, lint, JVM, emulator instrumentation, API 37 managed-device and QA packaging jobs. A workflow being defined is not a pass: check the run for the candidate commit and retain its test reports and APK artifacts. The audit's inability to reach Gradle was an environment limitation, not proof that those CI jobs fail.

## Release decision

**Not release-cleared** until the version-code and signing-custody questions above are resolved and a complete hosted Android CI plus exact-artifact verification run passes.
