# Director X — Project Status and Acceptance Matrix

**Review date:** 18 November 2026  
**Repository:** [AssayFlux/Director-X](https://github.com/AssayFlux/Director-X)  
**Audited baseline:** `main` at `73f704792e4572cd456723dd262f88098a5543f5`  
**Maintenance work:** merged to `main` in PR [#53](https://github.com/AssayFlux/Director-X/pull/53)

This is the authoritative status summary. It deliberately separates implementation presence, automated verification, and real-device acceptance. A feature is not complete merely because its model, UI setting, dependency or branch exists.

## Executive status

| Area | Main-branch status | Evidence / next gate |
|---|---|---|
| App identity | Aligned | Director X, AssayFlux, `com.directorx.editor`, versionName `0.1.0`, versionCode `302` |
| Proxy reliability foundations | Merged | PRs #47–#49; end-to-end/device acceptance remains open |
| Proxy acceptance | **Not complete** | High-resolution tier, cancellation, restart, storage pressure, concurrent workload, original-media export and Moto G54 checks |
| Custom canvas model/database | Partial foundation | Project dimensions and Room migration 10→11 exist |
| Custom canvas creation-to-export | **Not complete** | UI and dimensions must flow through preview and actual encoder output |
| CUSTOM global default crash | Confirmed defect on audited main | Correction merged in PR #53; Android unit-test run pending |
| AssayFlux splash | **Not on main** | Implementation candidate is on unmerged branch; review and device checks required |
| Room schema snapshot | Incomplete | Database version 11, tracked schema JSON only through 10; generate/inspect/commit 11 |
| Native FFmpeg security review | **Expired release gate** | `reviewUntil=2026-10-31`; conduct new review before release |
| CI unit tests | Last recorded run passed | Run 41556330546; not proof of device acceptance |
| Repository hygiene | Last recorded run passed | Run 41574172284 |
| API 37 device/performance QA | Not passed | Run 41553185619 was cancelled |
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

### Confirmed defect and prepared correction

The Settings screen exposed `CUSTOM` as a global default. Project creation applied that default without dimensions, while the Project model requires valid custom dimensions. This could throw during ordinary project creation.

PR #53 merged a defensive correction: custom dimensions are project-scoped, global defaults exclude CUSTOM, legacy CUSTOM values display/fall back to 16:9, and project creation refuses to instantiate a dimensionless CUSTOM project.

This correction still requires compilation and regression tests before it can be called fixed.

### Remaining integration

Custom dimensions must be propagated through project creation/editing, document/archive round-trip, preview sizing, export configuration, VideoEngine/ExportDelegate and final encoder output. Verify actual exported width and height, not only the UI label or project metadata.

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

## CI truth

| Workflow | Responsibility | Does not prove |
|---|---|---|
| Android verification | QA JVM unit tests and reports | Physical-device runtime correctness |
| Android device/performance QA | Lint, API 37 managed-device instrumentation, startup/editor macrobenchmark | Moto G54 acceptance |
| Repository hygiene | Identity, docs, metadata, feature claims and policy checks | Android compilation or runtime behavior |

Recorded evidence at the time of audit:
- Android unit verification on the maintenance PR: [run 44344294946](https://github.com/AssayFlux/Director-X/actions/runs/44344294946) — in progress at this update. The previous completed main run [41556330546](https://github.com/AssayFlux/Director-X/actions/runs/41556330546) passed.
- Repository hygiene for the maintenance PR: [run 44344295278](https://github.com/AssayFlux/Director-X/actions/runs/44344295278) — passed, including README link/asset validation.
- API 37 device/performance QA on the maintenance PR: [run 44344294891](https://github.com/AssayFlux/Director-X/actions/runs/44344294891) — in progress at this update. The earlier main run [41553185619](https://github.com/AssayFlux/Director-X/actions/runs/41553185619) was cancelled.

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
