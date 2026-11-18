# Director X — Documentation Baseline

**Original review date:** 2026-11-03
**Status refresh:** 2026-11-18  
**Repository:** [AssayFlux/Director-X](https://github.com/AssayFlux/Director-X)  
**Branch used for source review:** `main`  
**Original source/code reference baseline:** `1a3b7b8fe6a744780bbe202c07087e0eac5add2f`
**Current main reference checked for this refresh:** `69fe420a1de7d7de584afcdcbdb76475735fa1ab`  
**Scope:** Documentation, release text and repository-facing descriptions only.

This document records the factual reference state used to align the repository documentation. The 2026-11-03 inventory and source measurements remain historical evidence; the 2026-11-18 refresh updates repository status and points to the current acceptance matrix. It does not claim that every historical measurement was re-measured. The exact documentation baseline is the Git commit that contains this file after the documentation-only change set is merged to `main`. The source reference SHA above identifies the code/configuration state against which these documents were checked.

## 1. Verified Android identity and build configuration

| Item | Current repository value |
|---|---|
| Product name | Director X |
| Product credit | Designed by AssayFlux |
| Application ID | `com.directorx.editor` |
| Kotlin namespace | `com.directorx.editor` |
| Minimum SDK | API 26 |
| Compile SDK | API 37 |
| Target SDK | API 37 |
| Java source/target | 17 |
| Android app version name | `0.1.0` |
| Android app version code | `302` |
| Gradle wrapper | `9.8.0` |
| Android Gradle Plugin | `9.4.1` |
| Kotlin | `2.4.10` |
| KSP | `2.3.11` |

The authoritative version and Android build settings remain in `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties` and the Android manifest. Documentation must be refreshed from those files rather than used as an independent version source.

## 2. Current GitHub Actions behavior

| Workflow | Trigger | Actual responsibility |
|---|---|---|
| `android-verification.yml` | Relevant app/build pushes and pull requests; manual dispatch | Runs `:app:testQaUnitTest`; uploads test reports |
| `android-device-qa.yml` | Relevant pull requests; manual dispatch | Runs `:app:lintQa`, API 37 managed-device instrumentation, and API 37 startup/editor macrobenchmark; uploads QA reports |
| `repository-hygiene.yml` | Pushes and pull requests; manual dispatch | Repository duplicate, package identity, version policy, README link, capability registry, public-claim, audio-policy and validator checks |

CI may assemble temporary QA or benchmark APKs to execute instrumentation/performance tests. It does **not** publish a user-installable debug APK or production release. Installable APK assembly and physical-device acceptance remain manual maintainer tasks.

## 3. Verification evidence at the source reference baseline

- Android unit tests: [run 41556330546](https://github.com/AssayFlux/Director-X/actions/runs/41556330546) — completed successfully on main commit `1a3b7b8fe6a744780bbe202c07087e0eac5add2f`.
- Repository hygiene: [run 41556330569](https://github.com/AssayFlux/Director-X/actions/runs/41556330569) — completed successfully on the same main commit.
- Android device/performance QA for the latest proxy cancellation change: [run 41553185619](https://github.com/AssayFlux/Director-X/actions/runs/41553185619) — cancelled; this is not a pass.
- Physical-device acceptance: not established by these CI results. The intended manual device target is Moto G54 5G / Android 15.

A passing unit-test workflow does not establish that every instrumentation test, proxy workflow, UI state, export path or physical device has passed.

## 4. Proxy implementation status reflected in documentation

The following source changes are merged into `main`:

- PR [#47](https://github.com/AssayFlux/Director-X/pull/47): write proxy output to a temporary artifact and promote only after successful completion.
- PR [#48](https://github.com/AssayFlux/Director-X/pull/48): use a versioned cache-key namespace to prevent reuse of legacy cache artifacts.
- PR [#49](https://github.com/AssayFlux/Director-X/pull/49): fence Media3 Transformer cancellation before releasing the shared codec lease.

These are implementation facts, not a declaration that proxy acceptance is complete. High-resolution output verification, actual background-worker setting selection, cancellation during active transcode, concurrent playback/export/proxy stress, process-restart recovery, cache pressure/deletion failure, and original-source export still require their specified instrumentation/device evidence. See `Debugging/ISSUES.md`.

## 5. Documentation inventory and handling

The recursive Git tree review covered **866 tracked files** and identified **33 Markdown/text-oriented paths**, including current documentation, historical localized store metadata, a generated baseline profile, and a native source-offer resource.

Current-facing documentation was reconciled where necessary:

- `README.md`, `CHANGELOG.md`, `CONTRIBUTING.md`, `ACKNOWLEDGMENTS.md`
- `Debugging/ISSUES.md`
- `Upgradations/README.md`, `BASELINE_MEASUREMENTS.md`, `UI_LAYOUT_UPGRADE_PLAN.md`, `UPGRADE_ROADMAP.md`
- `docs/BRANDING_AND_COMPATIBILITY.md`, `TERMUX_BUILD.md`, `models.md`, `play-data-safety.md`, `privacy-policy.md`
- Current English and Spanish Fastlane changelog entries for version code 302.

Historical localized changelog entries and legal/source-offer materials are retained. Root `LICENSE`, `UPSTREAM-LICENSE-MIT.txt`, `NOTICE`, native lock data and packaged third-party license/source materials are not product-copy surfaces and must not be rewritten to erase upstream attribution or legal obligations.

## 6. Privacy and public-claim boundaries

- The app is local-first; selected model downloads are user-initiated.
- MediaPipe Tasks performance metrics are transmitted to Google only after the versioned opt-in consent path. The Play Data safety worksheet is marked as requiring policy classification review before submission; it must not assert “no data collected” without that review.
- The generic cloud AI routing/disclosure contracts exist, but no production cloud provider adapter is registered in the current model/provider inventory. Cloud generative-video processing is not advertised as available and no media upload is claimed for that path.
- `UPDATE_CHECK_AVAILABLE` is `false` in the current app build configuration; update-check network activity is compile-time disabled in this build.
- Planned AI models/features remain labelled planned or dependency-missing. A dependency or roadmap row is not proof of runtime availability.

## 7. Documentation-only change boundary

This documentation baseline must not change Kotlin/Java source, Android resources, manifests, Gradle files, dependency versions, Room schemas, scripts, CI workflow definitions, native binaries, signing configuration or app behavior. If a factual discrepancy requires one of those changes, record it as a separate engineering issue and obtain separate approval.

## 8. Baseline update procedure

For future gradual upgrades:

1. Read the current `main` commit before editing.
2. Re-check the authoritative Gradle identity/version, manifests, dependency catalog, workflow definitions, capability registry and relevant source implementation.
3. Update only the documentation that is affected by the verified change.
4. Preserve historical release notes and third-party legal notices.
5. Run repository-hygiene validation and inspect the complete diff.
6. Confirm that only documentation/release-text paths changed.
7. Record the resulting merged `main` commit as the next Director X baseline.

Do not copy the source reference SHA above into future reports as if it were the final documentation commit. The merged commit containing this document is the documentation baseline for this review.


## 9. Status refresh — 18 November 2026

The authoritative current engineering state is maintained in [Project Status and Acceptance Matrix](PROJECT_STATUS.md). It supersedes this baseline wherever current implementation or acceptance status has changed.

Key corrections for this refresh:

- The current `main` baseline observed is `69fe420a1de7d7de584afcdcbdb76475735fa1ab`; the prior source baseline remains historical.
- The README repository map previously described JVM and instrumentation tests as manual-only. The maintenance patch corrects that wording to reflect the Android verification and device-QA workflows.
- Proxy PRs #47–#49 remain merged implementation improvements, not completed physical-device acceptance.
- The API 37 device/performance QA run remains cancelled; do not represent it as passing.
- Main still lacks end-to-end custom canvas propagation and the AssayFlux splash implementation.
- Room database version 11 has no committed schema 11 JSON in the tracked schema directory.
- The native FFmpeg security review expiry is 2026-10-31 and must be renewed through an actual dependency/advisory review before release.
- A confirmed CUSTOM-default project-creation defect has a defensive correction prepared on a separate maintenance branch; compile and regression verification remain pending.

The maintenance patch intentionally keeps code changes and documentation changes reviewable in one branch, but this document does not authorize broad application redesign or removal of upstream legal notices.
