# Director X — Project Status and Acceptance Matrix

**Review date:** 18 November 2026  
**Repository:** [AssayFlux/Director-X](https://github.com/AssayFlux/Director-X)  
**Audited baseline:** `main` at `58ba5ccc712035fd53da687b3590e5fd2c92bf00`  
**Maintenance work:** merged to `main` in PR [#53](https://github.com/AssayFlux/Director-X/pull/53)

This is the authoritative status summary. It deliberately separates implementation presence, automated verification, and real-device acceptance. A feature is not complete merely because its model, UI setting, dependency or branch exists.

## Executive status

| Area | Main-branch status | Evidence / next gate |
|---|---|---|
| App identity | Aligned | Director X, AssayFlux, `com.directorx.editor`, versionName `0.1.0`, versionCode `302` |
| Proxy reliability foundations | Merged | PRs #47–#49; end-to-end/device acceptance remains open |
| Proxy acceptance | **Not complete** | High-resolution tier, cancellation, restart, storage pressure, concurrent workload, original-media export and Moto G54 checks |
| Custom canvas model/database | Partial foundation | Project dimensions and Room migration 10→11 exist; Room schema 11 JSON still needs generation |
| Custom canvas creation-to-export | **Implemented in draft PR #59; not accepted** | Project creation, preview geometry and export dimensions are wired; Room schema 11, exact output-file checks and device validation remain |
| CUSTOM global default crash | Correction and regression coverage merged | Import fixed in `5572f9a`; latest Android verification run `44349406491` is in progress |
| AssayFlux splash | **Not present on Alpha** | No splash implementation or raw GIF was found in the current Alpha tree |
| Room schema snapshot | Incomplete | Database version 11, tracked schema JSON only through 10; generate/inspect/commit 11 |
| Native FFmpeg security review | **Expired release gate** | `reviewUntil=2026-10-31`; conduct new review before release |
| CI unit tests | Current PR #59 run in progress | Check run 134991201705; no result claimed yet |
| Repository hygiene | Last recorded run passed | Run 41574172284 |
| API 37 device/performance QA | Current PR #59 run in progress | Check runs 134991181167 and 134991181852; not physical-device acceptance |
| Physical-device acceptance | Not established by this audit | Target device: Moto G54 5G / Android 15 |
| 8K export | Not implemented | Current preset enum ends at 4K |
| Professional camera compatibility | Not established | Requires a documented codec/profile test corpus |

## Identity and build contract

| Setting | Current value |
|---|---|
| Display name | Director X |
| Developer credit | Designed by AssayFlux |
| Application ID | `com.directorx.editor` |
| Kotlin namespace | `com.directorx.editor` |
| Minimum SDK | 26 |
| Compile / target SDK | 37 / 37 |
| Java toolchain | 17 |
| Gradle wrapper | 9.8.0 |
| Android versionName / versionCode | `0.1.0` / `302` |

The established package identity and release signing identity must remain stable for in-place upgrades. Historical upstream names, database identifiers and archive compatibility surfaces must not be renamed blindly; preserve required licence and attribution notices.

## Proxy workflow

### Merged foundations

- PR #47: restart-safe temporary proxy artifacts and promotion only after successful generation.
- PR #48: versioned cache-key namespace to prevent legacy cache artifacts from being reused.
- PR #49: cancellation fencing around Media3 Transformer and codec-lease release.

### Acceptance still required

1. Generate HALF, QUARTER and EIGHTH tiers from real high-resolution camera footage and inspect output dimensions/metadata.
2. Verify the selected tier reaches the actual WorkManager generation path.
3. Cancel while Transformer is actively transcoding; prove cleanup and lease release.
4. Run playback, export and proxy generation concurrently under memory/storage pressure.
5. Kill the process during generation and verify restart recovery and artifact consistency.
6. Verify cache cleanup under low-storage conditions without deleting active or valid artifacts.
7. Prove final export always reads original media, never a proxy.
8. Complete a physical-device pass on Moto G54 5G / Android 15.

The existing 320×240 trim fixture is not sufficient evidence for high-resolution proxy behavior. Until the gates above pass, report the proxy implementation as improved but not accepted.

## Custom canvas

### What exists on main

- `Project` stores `customWidth` and `customHeight` and validates even dimensions from 2 through 8192 with aspect ratio 1:4 through 4:1.
- Room database version 11 adds both columns in migration 10→11.
- Project-document serialization includes custom dimensions.
- Existing tests cover model validation and preset fallback, but do not establish the complete UI-to-export path.

### Corrected default and regression coverage

The Settings screen exposed `CUSTOM` as a global default. Project creation applied that default without dimensions, while the Project model requires valid custom dimensions. This could throw during ordinary project creation.

PR #53 merged a defensive correction: custom dimensions are project-scoped, global defaults exclude CUSTOM, legacy CUSTOM values display/fall back to 16:9, and project creation refuses to instantiate a dimensionless CUSTOM project.

The source correction and project-construction regression tests are merged. The Android unit-test workflow is still running; do not call the fix fully verified until it passes.

### Remaining integration

Draft [PR #56](https://github.com/AssayFlux/Director-X/pull/56) contains the candidate custom-canvas path, but GitHub currently reports it as dirty/non-mergeable. Its branch has an unresolved helper import and no tracked Room schema 11 snapshot; do not merge it until those issues are reconciled. Custom dimensions must ultimately be propagated through project creation/editing, document/archive round-trip, preview sizing, export configuration, VideoEngine/ExportDelegate and final encoder output. Verify actual exported width and height, not only the UI label or project metadata.

## Splash

The AssayFlux splash GIF and view implementation are absent from audited main. They exist on an unmerged candidate branch. Review asset provenance/licensing, dimensions, size and resource loading; then test cold start, lifecycle interruption, background/foreground, exit timing, navigation and low-memory behavior. The requested visual treatment is a white background, centered artwork and 1.3-second display, subject to lifecycle-safe implementation.

## Database schema

Room declares schema version 11 and migration 10→11. The repository currently tracks schema snapshots only through 10. Generate schema 11 with the configured Room/KSP build, inspect it against the entity and migration, commit it, and ensure CI/instrumentation validates the migration. The missing JSON is a repository snapshot gap; it is not by itself proof of an immediate test failure.

## Native dependency and release security

The native lock file's top-level security review expired on 31 October 2026. Release verification rejects expired reviews and is attached to the release build path.

Before a release:
- review pinned native revisions and current advisories;
- verify enabled/disabled FFmpeg components and reachability assumptions;
- validate licences and notices;
- regenerate/inspect the SBOM and lock evidence;
- update review dates only as a result of that review;
- run the release verification gate.

Do not mechanically extend the expiry date. The expired review is a release blocker; it does not automatically block an ordinary debug build.

## Dependency freshness

The dependency freshness snapshot was refreshed on **2026-11-18** by the repository's existing `scripts/refresh_dependency_freshness.py` using authoritative Maven metadata. The snapshot covers 21 dependencies: **5 current, 15 intentionally held, and 1 pre-release**. No dependency catalog pins were changed by this refresh.

Important held candidates remain explicit rather than being upgraded mechanically:

- AGP 9.5.1 and Kotlin Gradle Plugin 2.4.21 require a coordinated toolchain decision; the current AGP 9.4.1 / KGP 2.4.10 pair has a recorded passing strict probe, but the next pair has not been probed.
- Room 3.0.3 is on the new `androidx.room3` coordinate and requires a migration, not a version-only edit.
- ONNX Runtime 1.32.0 needs fresh per-ABI size, alignment, Whisper and inpainting evidence; the earlier 1.30.0 candidate exceeded the 2 MB per-ABI growth budget.
- MediaPipe 1.2.3 and protobuf-javalite 4.36.2 remain held pending generated-code/runtime compatibility checks.
- Other newer stable candidates remain held until their compatibility probes pass.

The refresh script now regenerates hold reasons and unblock conditions from current metadata and preserves the previous decision in `previousReview` when it changes. This prevents stale candidate versions from remaining in the active reason text. The 30-day evidence policy remains in force.

## CI truth

| Workflow | Responsibility | Does not prove |
|---|---|---|
| Android verification | QA JVM unit tests and reports | Physical-device runtime correctness |
| Android device/performance QA | Lint, API 37 managed-device instrumentation, startup/editor macrobenchmark | Moto G54 acceptance |
| Repository hygiene | Identity, docs, metadata, feature claims and policy checks | Android compilation or runtime behavior |

Recorded evidence at the time of audit:
- Android unit verification: earlier runs exposed a missing helper import and stale dependency/document allowlist evidence. The import was fixed, PR #55 merged the project-construction regression test, the dependency snapshot is refreshed, and the tracked-document allowlist is corrected. Android verification [44349953257](https://github.com/AssayFlux/Director-X/actions/runs/44349953257) exposed one test-fixture mismatch after the version cleanup. The test now models a simulated upgrade from 0.1.0 to 0.1.1; rerun [44351352630](https://github.com/AssayFlux/Director-X/actions/runs/44351352630) is in progress. The previous completed main run [41556330546](https://github.com/AssayFlux/Director-X/actions/runs/41556330546) passed.
- Repository hygiene for the maintenance PR: [run 44344295278](https://github.com/AssayFlux/Director-X/actions/runs/44344295278) — passed, including README link/asset validation.
- API 37 device/performance QA: follow-up run [44345410638](https://github.com/AssayFlux/Director-X/actions/runs/44345410638) is in progress. The earlier main run [41553185619](https://github.com/AssayFlux/Director-X/actions/runs/41553185619) was cancelled.

No workflow publishes an installable product APK or production release. CI may assemble temporary QA/benchmark APKs. The installable APK is built manually by the maintainer using the documented Termux workflow. The README and Termux guide should be updated whenever workflow triggers or tasks change.

## Capability boundaries

- Current resolution presets top out at 4K; 8K export is not implemented.
- Do not promise professional-camera compatibility without testing actual codecs, profiles, bit depth, chroma subsampling, container variants, rotation metadata and audio combinations.
- AI dependencies or model names are not proof that every feature is available offline or on every device. Follow the capability registry and feature-specific model/download requirements.
- Keep the approved UI scope narrow: Dashboard and Settings surfaces only for the previously requested layout modernization. Do not redesign the editor/timeline or change the established icon family, palette or visual identity without approval.

## Recommended closure order

1. Confirm the merged CUSTOM-default correction with the running Android unit-test workflow and the regression test.
2. Review and integrate custom canvas end-to-end, including real export dimensions.
3. Review and integrate the splash implementation.
4. Complete proxy end-to-end and physical-device acceptance.
5. Renew native dependency security review and release gate evidence.
6. Generate/commit Room schema 11 and validate migrations.
7. Reconcile workflow docs, stale branches, historical names and release metadata.
8. Run the documented manual build and physical-device acceptance before any release-readiness claim.

## Evidence policy

Use these labels consistently:
- **Implemented:** code exists on the branch being discussed.
- **Merged:** code is present on main.
- **Automated checks passed:** named workflow run completed successfully.
- **Device accepted:** the specified physical-device scenario was executed and passed.
- **Release ready:** all functional, compatibility, signing, native security and release checks passed.

Never substitute one label for another.

## Current repository refresh — 18 November 2026 (Alpha)

**Product owner:** AssayFlux  
**Product:** Director X  
**Current default branch:** `Alpha`  
**Audited base before this maintenance branch:** `b4dd5d71138dad6d926df6a612ed66127fff02cb`

This section supersedes earlier branch/status statements in this historical matrix where they conflict. Earlier run IDs and PR references below remain historical evidence only; they are not evidence about the current Alpha head.

### Current verified state

| Area | Current finding | Completion status |
|---|---|---|
| Repository branch | `Alpha` is the default and only branch | Current |
| CI branch triggers | Updated on the maintenance branch to target `Alpha` | Change made; workflow run still required |
| Logo/background assets | `Assets/Director-X Logo.svg` and `Assets/UI Design Texture.svg` exist; the redundant root SVG copy has been removed in draft PR #59 | Present; app integration not yet verified |
| 5.3K source video | Current preset enum ends at 4K; the project model's 8192 dimension limit is not a playback/export guarantee | Not accepted |
| Custom canvas | Model and Room migration exist; export still calculates dimensions from preset resolution/aspect ratio | Incomplete |
| Proxy acceptance | Cache/cancellation foundations exist; real high-resolution source, worker, recovery, storage and physical-device gates remain | Incomplete |
| Splash | No current Alpha splash implementation was found in the audited tree | Incomplete |
| Room schema | Database version 11 / migration 10→11 exist; tracked schema 11 snapshot was not found | Incomplete |
| Native FFmpeg review | Security review date 2026-10-31 is expired | Release blocker until reviewed |
| Physical device | No evidence of 5.3K acceptance on the target Android device | Not tested |

### 5.3K video support contract

Director X must accept high-resolution source media for import, metadata inspection, timeline editing, seeking and playback. A “5.3K” label is not a single universal raster: the implementation must use the dimensions and codec metadata read from each file (for example, 5312×2988 sources) rather than hard-code one assumed size.

Playback is device-dependent. The app must query the actual decoder's format support, frame-rate/size limits and profile/bit-depth capability. If native real-time decoding is unavailable or unstable, the editor must offer a proxy workflow that can decode the original through a supported fallback, create a bounded editing proxy, preserve source timebase/orientation/audio, and keep the original source attached for final export. It must never silently replace the original media with a proxy in the export path.

The current Media3 proxy path uses Transformer to decode the original. Its fallback currently tries alternate Android decoders; that is not proof of a software decode path for sources that exceed every device decoder's limits. A dedicated FFmpeg software-decode proxy fallback, cancellation fencing, progress, disk budgeting and recovery tests are required before claiming 5.3K editing on devices without a suitable hardware decoder.

### 5.3K acceptance gates

1. Import representative camera files across H.264, HEVC Main/Main10, AVC/HEVC high profiles, 8-bit/10-bit, SDR/HDR, variable/constant frame rate, rotation metadata and long-GOP footage.
2. Inspect the real raster, frame rate, codec profile/level, chroma, bit depth, color metadata, audio tracks and rotation without loading full-resolution frames into memory.
3. Query Android decoder capabilities against the actual source format; distinguish “container readable”, “decoder advertised”, and “sustained playback passed”.
4. Verify native playback where supported and an explicit proxy fallback where unsupported. Test scrubbing, seeking, audio sync, orientation, HDR/SDR behavior and lifecycle interruption.
5. Generate HALF/QUARTER/EIGHTH proxies from 5.3K sources; verify dimensions, duration, audio policy, timestamps, rotation and cache identity.
6. Cancel during active decode/encode, kill the process, restart and recover without promoting partial files.
7. Stress playback + export + proxy generation under memory and storage pressure; verify bounded memory and safe cache cleanup.
8. Export a project and inspect the output file. The selected project/export dimensions must be honored, and source media must remain the original.
9. Run the full acceptance corpus on the maintainer's physical Android device. A managed emulator or JVM test alone is insufficient.

Until all applicable gates pass, documentation and release notes must say **5.3K source support under validation**, not “fully supported”.

### AssayFlux asset and documentation rules

- Use the supplied Director X logo and UI background SVGs as the authoritative product artwork; do not substitute generated or unrelated brand artwork.
- Keep the app-facing product name **Director X** and product-owner credit **Designed by AssayFlux** consistent across README, in-app surfaces, release notes and current documentation.
- Do not introduce third-party product branding, logos or promotional copy into Director X surfaces.
- Preserve legally required open-source notices, licence texts, source offers and accurate upstream acknowledgements. Those are compliance materials, not competing product branding.
- Do not describe an upstream dependency as an AssayFlux original component. Keep dependency provenance and licence records factual.


### Maintenance implementation update — PR #59

A draft maintenance PR now carries these source changes:

- Custom canvas dimensions can be entered during new-project creation and are persisted on the existing Project model.
- Exact custom dimensions are carried through ExportConfig, Media3 encoder sizing, export output verification/history and export storage estimates. Explicit platform presets clear project-custom dimensions and remain authoritative.
- The editor preview and smart-crop aspect ratio now use the resolved project canvas geometry; GIF gap frames use the same exact ratio.
- Proxy temporary artifacts now have a distinct validation predicate. The prior implementation checked a `proxy_tmp_*.mp4` path with the committed-cache predicate, which rejects temporary filenames; successful temporary output can now be atomically promoted.
- Proxy generation now attempts an FFmpeg software-capable decode/encode fallback after Media3 hardware-decoder attempts fail. The fallback supports SAF content URIs through the existing FFmpeg input adapter and retains the existing proxy tier's target height.

These are implementation changes under review, not verified acceptance. The draft PR must pass unit tests, lint and instrumentation. Real 5.3K footage, physical-device playback, cancellation, process death, storage pressure and final original-source export still require evidence. Room schema 11, splash, in-app SVG integration and native security review remain open.
