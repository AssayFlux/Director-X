# Director X — Debugging & Issue Register

**Product owner:** AssayFlux  
**Repository:** [AssayFlux/Director-X](https://github.com/AssayFlux/Director-X)  
**Default branch:** `Alpha` at `b4dd5d71138dad6d926df6a612ed66127fff02cb`  
**Active implementation:** `assayflux/5.3k-media-and-branding` — [draft PR #59](https://github.com/AssayFlux/Director-X/pull/59), head `f5570fea5287acb297691decf8c08249ba904788`  
**Review date:** 18 November 2026

This register distinguishes historical findings, current source corrections, automated test results, managed-device QA and physical-device acceptance. PR #59 is still a draft and is not merged.

## Product direction and authorized scope

- All current product-facing branding must identify **Director X — Designed by AssayFlux**. Use the supplied `Assets/Director-X Logo.svg` and `Assets/UI Design Texture.svg`; do not introduce another product identity or third-party brand treatment.
- Use the logo in the 1.3-second entry/exit splash and Dashboard/Settings headers. Use the texture subtly on Dashboard and Settings only.
- Preserve the existing color system and icon style. The editing workspace, timeline, preview/player, import, export and editor controls are outside this UI change.
- Keep Dashboard and Settings navigation/functionality intact. This is a focused layout/artwork integration, not a general redesign.
- Preserve all legally required upstream license and attribution notices. Legal provenance is not product branding and must not be removed to make the app appear wholly dependency-free.
- 5.3K source-media support is a required product goal. Prefer automatic selected-tier proxies for 5.3K-class sources and preserve original media for final export.

## Current repository snapshot — 2026-11-18

- **Default branch:** `Alpha`; its current baseline is `b4dd5d71138dad6d926df6a612ed66127fff02cb`.
- **Active draft:** PR #59 contains custom canvas end-to-end wiring, 5.3K-class proxy handling, AVC/HEVC software fallback, AssayFlux SVG artwork integration, splash, cancellation improvements and refreshed documentation.
- **Branch discipline:** only Alpha plus the single active PR branch should be maintained. Do not create additional branches for the same work. Delete the PR branch only after the work is merged or abandoned.
- **CI:** latest-head Android verification, repository hygiene and API 37 QA must all complete successfully. Earlier cancelled/in-progress run IDs are not evidence of a pass.
- **Room:** database version 11 and migration 10→11 exist, but the schema 11 snapshot still needs to be generated, inspected and committed.
- **Native media security:** FFmpeg review expiry is 2026-10-31. The release gate remains blocked pending component inventory, advisory reachability review and reproducible native build evidence.
- **Device acceptance:** not performed by this repository review. Moto G54 5G / Android 15 remains the manual target for 5.3K footage, proxy playback, export, memory/storage and splash visual checks.
- **Export scope:** 8K output is not implemented; custom canvas dimensions up to 8192 do not prove that an encoder can produce 8K output.

The 2026-11-03 snapshot below is retained as historical audit evidence only. Use the 2026-11-18 snapshot above for current branch and CI state.

## Status definitions

- **OPEN — CONFIRMED:** The source code demonstrates the issue; correction and verification are pending.
- **OPEN — INVESTIGATE:** A risk or behavior needs further tracing or device-level evidence before it can be called a defect.
- **IN PROGRESS:** A change is being implemented but has not passed its required verification.
- **RESOLVED — VERIFIED:** The fix is committed and the listed verification evidence has passed.

## Proxy video workflow

The issue titles and defect descriptions below preserve the original findings. Where the current status says **SOURCE FIX MERGED**, the described defect is historical and the correction is in current `main`; the remaining verification bullets are still acceptance requirements, not proof that the original defect remains in the source.

### DX-PROXY-001 — Proxy cache key ignores selected resolution

- **Severity:** High
- **Status:** SOURCE FIX MERGED — Android unit-test suite passed on current main; end-to-end/device acceptance pending.
- **Affected file:** `app/src/main/java/com/directorx/editor/engine/ProxyEngine.kt`
- **Finding:** `keyFor(sourceUri)` derives the cache key only from the source URI. The output filename and cache lookup therefore do not distinguish HALF, QUARTER, and EIGHTH resolutions. `generateProxy()` returns an existing non-empty cached file before evaluating the requested resolution.
- **Impact:** After a proxy is generated at one resolution, requesting another resolution for the same URI can return the earlier proxy. The setting may appear to change without producing a proxy at the newly selected quality.
- **Required correction:** Make proxy identity include all output-affecting parameters, at minimum the canonical source identity/version and selected resolution (and any future codec/format profile that changes output). Ensure lookup, generation, deletion, and cache accounting use the same identity. Do not delete or overwrite a different resolution's valid proxy.
- **Verification required:** Add unit tests proving distinct resolutions produce distinct cache identities; add instrumentation coverage that generates two resolutions for the same source and checks their dimensions; verify repeat requests reuse only the matching resolution.

### DX-PROXY-002 — Background workflow hardcodes QUARTER resolution

- **Severity:** High
- **Status:** SOURCE FIX MERGED — Android unit-test suite passed on current main; actual WorkManager tier-selection test remains required.
- **Affected file:** `app/src/main/java/com/directorx/editor/engine/ProxyWorkflowEngine.kt`
- **Finding:** `generateAllProxies()` calls `proxyEngine.generateProxy(entry.originalUri, ProxyResolution.QUARTER)` directly. The workflow does not receive or read the user's selected proxy resolution.
- **Impact:** Background-generated proxies can remain quarter-resolution even when the user selects HALF or EIGHTH. This makes the setting inconsistent across proxy-generation entry points.
- **Required correction:** Pass the effective user-selected resolution into the workflow (or obtain it from the single authoritative settings source). Avoid a second hardcoded default that can drift from the setting. Keep export bound to the original media.
- **Verification required:** Test each supported setting through the actual background workflow, confirm resulting proxy dimensions, and confirm export still resolves to the original URI.

### DX-PROXY-003 — Background worker can report success when individual proxy generation fails

- **Severity:** Medium
- **Status:** SOURCE FIX MERGED — Android unit-test suite passed on current main; worker-level partial-failure/retry acceptance remains required.
- **Affected files:** `app/src/main/java/com/directorx/editor/engine/ProxyWorkflowEngine.kt`, `app/src/main/java/com/directorx/editor/engine/ProxyGenerationWorker.kt`
- **Finding:** The workflow catches per-clip exceptions and continues. A null result from `generateProxy()` leaves that entry without a generated proxy, but the worker receives no failure summary and returns `Result.success()` after the loop.
- **Impact:** WorkManager/UI may report the batch as completed even though one or more clips did not receive proxies.
- **Required correction:** Return a structured batch result with attempted, generated, skipped, and failed counts (plus safe per-item error details). Map retryable batch failures to retry only under a defined policy; do not turn a partial failure into an unqualified success.
- **Verification required:** Add tests for all-success, partial-failure, all-failure, skipped-small-source, and cancellation cases. Verify WorkManager output data and UI status match the result.

### DX-PROXY-004 — Proxy cleanup result is not propagated by workflow deletion

- **Severity:** Medium
- **Status:** SOURCE FIX MERGED — Android unit-test suite passed on current main; storage-pressure and real deletion-failure acceptance remain required.
- **Affected file:** `app/src/main/java/com/directorx/editor/engine/ProxyWorkflowEngine.kt`
- **Finding:** `deleteAllProxies()` ignores the Boolean returned by `proxyEngine.deleteProxyUri()` and clears each entry's proxy state regardless of whether the underlying file was deleted.
- **Impact:** A failed deletion can be presented as cleared while an orphaned proxy file remains on disk and is no longer represented in workflow state.
- **Required correction:** Aggregate deletion outcomes, retain or explicitly mark entries whose files could not be removed, and expose an honest cleanup result to Settings/UI. Coordinate with the engine's in-use protection.
- **Verification required:** Test successful deletion, failed deletion, missing files, and proxies currently in use; verify reported counts and actual cache contents.

### DX-PROXY-005 — Preview high-resolution eligibility and portrait orientation need end-to-end validation

- **Severity:** Medium
- **Status:** SOURCE FIX MERGED — Android unit-test suite passed on current main; metadata/import/rotation end-to-end device verification remains required.
- **Affected file:** `app/src/main/java/com/directorx/editor/engine/ProxyWorkflowEngine.kt`
- **Finding:** The first implementation pass updated preview selection to use the long coded dimension, but a follow-up source review found the background batch filter still used `originalHeight > 1080`. That left portrait/rotated sources inconsistently eligible.
- **Correction committed:** Both preview selection and background batch filtering now call `ProxyResolutionPolicy.requiresProxy(originalWidth, originalHeight)`, which uses the larger coded dimension.
- **Remaining uncertainty:** The current source review still needs to trace every `registerMedia()` caller and establish whether width/height are consistently coded dimensions. The proxy planner separately uses displayed height derived from rotation metadata.
- **Verification required:** Test landscape, portrait, rotated portrait, and sources around the 1080-pixel threshold through import, background generation, preview selection, and export.

## Follow-up implementation log — 2026-11-02

The following corrections were committed during the 2026-11-02 implementation sequence. The source fixes are now merged to current `main`; the latest Android unit-test suite passed, but end-to-end/instrumentation/device acceptance remains incomplete. Repository-hygiene CI alone is not sufficient to close them.

- **DX-PROXY-001:** Cache identity now hashes source URI plus selected `ProxyResolution`; preview cache checks and URI lookup pass the requested resolution. Added `ProxyCacheKeyTest.kt` for stable keys, resolution separation, and source separation. Commits: `3691b0b`, `170cb6f`, `f4024dd`, `4b30a01`, `885878b`, `13c1c98`, `58ed5d4`.
- **DX-PROXY-002:** `ProxyWorkflowEngine` now reads the persisted `SettingsRepository.settings.proxyResolution` and plans/generates with that value rather than hardcoding QUARTER. Commit: `cb4ea44`.
- **DX-PROXY-003:** Workflow now returns attempted/generated/skipped/failed counts; the worker includes counts in output data, retries partial failures under a bounded policy, and fails after the retry limit. Cancellation is rethrown rather than incorrectly treated as a retryable exception. Added result contract tests. Commits: `8aeaba3`, `747d6aa`, `9f396bd`, `7760187`.
- **DX-PROXY-004:** Workflow cleanup now clears entry state only after deletion succeeds; failed deletions retain their URI/state and are counted as failures. Commit: `d2fe6a4`.
- **Additional cache safety:** Existing cache files from the old URI-only key format may remain as orphaned files until the user clears the proxy cache; the existing cache-wide cleanup scans managed proxy files. Do not delete user media or unrelated cache files.

**Historical verification status at the time of this entry:** repository hygiene passed for commit `885878b`; later results were not yet known. Current evidence is recorded in the 2026-11-03 repository snapshot above.

## Existing proxy test coverage observed

The initial audit branch contained: 

- `app/src/test/java/com/directorx/editor/engine/ProxyResolutionPolicyTest.kt`
- `app/src/androidTest/java/com/directorx/editor/engine/ProxyResolutionInstrumentationTest.kt`

The current test tree also contains resolution-specific cache-key and proxy artifact commit-policy tests. The main-branch Android unit-test suite passed on 2026-11-03. However, the suite does not replace the outstanding high-resolution Media3 generation, full WorkManager tier-selection, cancellation-during-transcode, concurrency, process-restart, cache-pressure, and original-source export acceptance listed below.

## Closure requirements

An issue may be marked **RESOLVED — VERIFIED** only when all of the following are recorded:

1. The correction is committed to the repository.
2. Relevant automated tests are added or updated and pass.
3. Android instrumentation/device checks pass where required.
4. The verification evidence (test command, CI run, device/API level, and result) is added to the issue entry.
5. Related user-facing status and documentation accurately reflect the implementation.

Do not close issues based only on a successful build, a green badge, or code that has not been exercised.

## Audit scope and limitations

This is an initial source-level review of the proxy engine, proxy workflow, background worker, and the existing proxy policy tests on the recorded `main` baseline. It is not a complete application audit, and it does not claim that the listed issues are the only defects in Director X. Re-audit the affected files and tests before implementing fixes if the branch has advanced.

## CI follow-up — 2026-11-02

### DX-CI-001 — APK validator was called with an unsupported argument

- **Severity:** High
- **Status:** Historical workflow defect — current workflow configuration has been restructured; see the 2026-11-03 workflow snapshot above.
- **Affected file:** `.github/workflows/android-verification.yml`
- **Finding:** The workflow passed `app/build/outputs/apk` to `scripts/validate_apk_outputs.py`, but the validator accepts a build variant (`debug`, `qa`, or `streaming`) and resolves the repository output path itself. The workflow therefore could not correctly invoke the validator.
- **Correction:** The workflow at that time was corrected to call the validator with its supported variant argument. The current Android verification workflow now runs QA unit tests and uploads test reports; APK assembly and APK validation are not part of that workflow.
- **Verification:** Repository hygiene passed on the workflow-fix commit. At the time, the Android run was pending. The current unit-test workflow result is recorded in the 2026-11-03 snapshot; the current workflow no longer assembles a distributable APK.

### DX-PROXY-005 implementation update — 2026-11-02

- **Source corrections committed:** Preview selection, background batch filtering, and the EditorViewModel enqueue trigger now use `ProxyResolutionPolicy.requiresProxy(width, height)` rather than checking height alone. This prevents rotated 1920×1080 coded media from being missed by the background generation trigger or batch planner.
- **Regression coverage added:** `ProxyResolutionPolicyTest.proxyEligibilityUsesTheLongDimensionForPortraitAndLandscapeMedia` checks landscape, portrait, 4K, square, and threshold cases.
- **Commits:** `0cf735d`, `1c63a26`, `f0cee32`, `7ac6ce8`, `2733fd8`, `92420a4`, `f829928`.
- **Status:** Source fix merged; current main Android unit-test suite passed. The coded-dimension contract still requires metadata/import/rotation end-to-end device verification.




## Proxy reliability follow-up — 2026-11-03

### DX-PROXY-007 — Interrupted Transformer output can be mistaken for a completed proxy

- **Severity:** High
- **Status:** SOURCE FIX MERGED — Android unit-test suite passed on current main; real Transformer cancellation and restart/device verification remain pending
- **Affected file:** `app/src/main/java/com/directorx/editor/engine/ProxyEngine.kt`
- **Finding:** Before this correction, Media3 Transformer wrote directly to the final cache filename and cache lookup accepted any non-empty file. A process kill or power loss before the cancellation callback could leave a truncated, non-empty MP4 that a later process accepted as a valid cache hit.
- **Correction:** Proxy renders now write to a distinct `proxy_tmp_<key>.mp4` artifact. The engine promotes it to `proxy_<key>.mp4` only after Media3 reports completion and the temporary file is non-empty. Cancellation, Transformer error, and coroutine failure remove the temporary artifact. Startup generation removes an orphaned temporary artifact before retrying. Cache cleanup recognizes temporary artifacts and does not mistake them for active final-cache mappings. The cache-key namespace also includes `artifact=v2`, invalidating final-path artifacts produced by the pre-fix implementation so an old interrupted file cannot be reused after upgrade.
- **Regression test added:** `ProxyArtifactCommitPolicyTest.kt` verifies that a non-empty temporary artifact, a zero-byte final artifact, and unrelated files are not accepted as completed proxies. The current main-branch Android unit-test workflow passed after this test was merged (run 41556330546).
- **Commits:** `13ad483` (implementation), `0502d5e` (unit test); follow-up cache-key and cancellation changes are in merged PRs #48 and #49.

### Remaining proxy acceptance work — not yet verified

The source-level fixes for resolution-specific cache identity, saved background resolution, batch result reporting, deletion retention, non-blocking codec-permit acquisition, atomic temporary-file commit, cache-key namespace invalidation and cancellation fencing are merged. The following are still required before this workflow can be called complete:

1. Generate HALF, QUARTER and EIGHTH from one actual high-resolution source whose dimensions exceed all three policy thresholds; inspect each output file's container, codec, dimensions, duration, decodability and non-zero size. The existing `trim-boundary.mp4` fixture is only 320×240 and cannot prove this.
2. Exercise each persisted user-selected tier through the real WorkManager/background workflow, not only a direct `ProxyEngine.generateProxy()` call.
3. Cancel after the actual Media3 Transformer has started writing output; prove the temporary file is removed, the final cache entry is absent, the codec lease is released, and the worker reports cancellation.
4. Run playback, export and proxy generation concurrently on an emulator/device; capture codec lease snapshots, completion/failure outcomes, UI responsiveness and resource release.
5. Kill/restart during a proxy transcode, reopen the project, and prove an orphaned partial file is discarded and generation can complete again.
6. Exercise cache cleanup with active files, orphaned temporary files, low free storage, failed deletion and failed directory enumeration. Confirm mappings survive failed deletion and unrelated/original media is never removed.
7. Attach a generated proxy to a clip, run a real final export, inspect the exported file, and prove the export pipeline read the original source rather than the proxy.
8. Re-run API 37 instrumentation and the macrobenchmark lane. A green unit-test or repository-hygiene workflow alone does not close these checks.
9. Perform physical acceptance on the Moto G54 5G / Android 15. Emulator results are not a substitute for this device check.

### Historical evidence from the 2026-11-02 review

- Main baseline at that time: `cef21be9805d0eb28225829370588d4a2cfea821`.
- Android unit-test workflow run `41549620339`: passed on the baseline before the temporary-file correction.
- Repository-hygiene workflow run `41549620327`: passed on the baseline before the temporary-file correction.
- API 37 / macrobenchmark run `41549501956`: macrobenchmark bootstrap was cancelled and benchmark execution was skipped; API 37 instrumentation was still in progress when last inspected.
- No physical-device proxy stress test is claimed.

## Professional camera media compatibility

### DX-MEDIA-001 — Professional-camera codec and profile compatibility is not yet established

- **Severity:** Critical for the requested professional-camera workflow
- **Status:** IN PROGRESS — source inspection extended; device and camera-corpus verification pending
- **Affected areas:** Media import, Media3 preview/Transformer, FFmpegKitNext fallback, proxy generation, export preflight, media metadata persistence, Media Manager diagnostics.
- **Finding:** A container being importable does not prove its video profile, bit depth, chroma subsampling, frame rate, audio streams, timecode, HDR metadata or camera-specific RAW payload can be decoded and rendered.
- **Implemented on 2026-11-02:** Media track diagnostics now record profile, level, reported bit depth/chroma, bitrate, audio channel count/sample rate and a device-local decoder lookup against the actual extractor format using Android's regular codec catalogue. The Media Manager diagnostics surface displays the selected decoder (or a clear no-match/unknown result), profile/level, reported bit depth/chroma and audio stream properties. This is a capability hint for the current device, not a guarantee of successful playback or export.
- **Still required:** Distinguish import, preview, proxy and final-export capability end to end. Preserve originals and offer a proxy/transcode path only when a decoder can actually read the source. Verify whether Media3 and FFmpeg fallback can decode each sample and whether export conforms.
- **Test corpus required:** Representative camera samples for H.264, HEVC Main/Main10, 4:2:2 variants, ProRes variants, high-bitrate Long-GOP, variable frame rate, multichannel/PCM audio and HDR. Test RAW formats separately; do not claim BRAW, R3D or ARRIRAW support without a verified decoder and licensing review.
- **Verification required:** Record source file, codec/profile, device/API, preview result, proxy result, export result and output conformance for each sample. Hardware-dependent support must be reported as device-specific.

## Custom aspect ratio

### DX-PROJECT-001 — Custom canvas dimensions are only partially propagated

- **Severity:** High
- **Status:** IN PROGRESS — model and project-document persistence exist; end-to-end UI, database, preview and export behavior still require verification.
- **Confirmed implementation:** `Project.kt` contains `customWidth`, `customHeight`, `AspectRatio.CUSTOM`, validation through `CanvasDimensions`, and a computed `canvasDimensions` property. `ProjectDocument.kt` writes and reads the custom dimensions.
- **Correction to previous finding:** It is inaccurate to say custom dimensions are absent from the project model or project-document serializer. Those foundations are already present and must be preserved.
- **Remaining investigation and work:** Confirm the project-creation/edit UI; Room entity/schema/migration coverage; autosave and reopen; archive/import; templates; preview/crop sizing; and actual export renderer dimensions. `ExportConfig.kt` still has independent preset `resolution` and `aspectRatio` fields, so verify how the project canvas dimensions reach the renderer and how platform presets intentionally override them.
- **Verification required:** Legacy project round trips; custom portrait, landscape and square dimensions; invalid/odd/boundary values; database migration tests; preview dimension assertions; and exported-file dimension inspection. Do not duplicate existing model/document fields or mark this resolved from source presence alone.

## Dashboard redesign — historical implementation log (2026-11-02)

- **Status:** Layout refinements are present in source; matched-device screenshots, accessibility and full UI regression verification remain pending.
- **Scope:** `app/src/main/java/com/directorx/editor/ui/projects/ProjectListScreen.kt` only.
- **Change at that commit:** Dashboard hero layout was refined. Current source has a 50%-of-screen-height cap (minimum 280dp), 20dp horizontal × 16dp vertical padding, and 12dp child spacing; see `Upgradations/BASELINE_MEASUREMENTS.md`.
- **Preserved:** Existing Material filled icons, theme-driven colors, callbacks, search/sort visibility conditions, project list states, import/create actions, trash/restore, crash-report actions, and all other Dashboard behavior were not intentionally changed.
- **Commit:** `d1149f3e1ac9fe4143526e522fe1243f4397a774`.
- **Not verified yet:** Gradle build, Compose/UI tests, screenshots at narrow and wide viewports, large font scale, orientation, and physical-device behavior. This is a source-level layout refinement, not completion of the Dashboard redesign.


## Settings redesign — historical implementation log (2026-11-02)

- **Status:** Four-category navigation is present in source; Android unit-test CI has since passed, but device/UI regression checks remain pending.
- **Scope:** `app/src/main/java/com/directorx/editor/ui/settings/SettingsScreen.kt` only.
- **Change:** Settings sections now use the existing theme semantic panel/border colors, established 12dp radius token, 20dp horizontal page inset, and clearer title hierarchy. The previously accepted `description` parameter is now rendered beneath each section heading instead of being discarded.
- **Preserved by this edit:** Existing section bodies, setting controls, callbacks, ViewModel state, DataStore values, appearance options and current navigation entry point were not intentionally changed.
- **Commit:** `339a6c375acbbabd13b8a712be2976099742b49f`.
- **Still required:** Verify Android unit tests/build, category back-stack/state restoration, every setting control, and device/font-scale behavior. Do not mark Step 6 fully verified until these pass.


## Settings category navigation — historical implementation log (2026-11-02)

- **Implemented:** Added four Settings category views in `SettingsScreen.kt`: Export & Delivery, Editing & Timeline, AI & Project Storage, and App & Preferences.
- **Mapping:** All 13 existing `SettingsSection` blocks remain in place and are grouped once: 2 export, 3 editing/timeline, 2 AI/storage, and 6 app/preferences sections.
- **Navigation:** The Settings header back action returns from a selected category to the category overview; from the overview it invokes the existing parent back callback.
- **Preservation:** Existing setting controls, ViewModel callbacks, dialogs, and underlying settings state were not replaced.
- **Commit:** `81e82444` — Add Settings category navigation.
- **Validation status:** Android verification and repository hygiene workflows have been triggered for this commit. Do not close Step 6 until Android verification passes and UI/back-navigation behaviour is checked on-device, including font scaling.


## Step 7 — final validation and closure audit — historical snapshot (2026-11-02)

> This section records the state before the 2026-11-03 merged proxy fixes and current CI runs. Its pending-run statements and release gate are historical; use the current repository snapshot at the top of this file.

- **Status at the time:** CI failure diagnosed and corrected. Subsequent main-branch Android unit-test verification passed on 2026-11-03; see current snapshot.
- **Repository hygiene:** Passed for commit `6dac3b48` (run 41423237862). This validates package identity, version consistency, README links/assets, capability registry, policy checks and validator self-tests; it does not prove the Android app compiles.
- **Android verification failure:** Run 41423237875 failed during `:app:compileQaKotlin` at `SettingsScreen.kt:889` with `Syntax error: Expecting a top level declaration`. Unit tests did not start; APK assembly and APK validation were skipped.
- **Correction:** Removed two extra closing braces introduced around the Settings category/dialog scope. Commit: `f5a184df`.
- **Revalidation:** Android verification run 41424241742 and repository-hygiene run 41424241710 were queued at the time this register was updated. No passing build or APK is claimed until the Android run completes.
- **Other unresolved project work:** `DX-PROJECT-001` custom canvas dimensions remains OPEN — CONFIRMED. `DX-MEDIA-001` professional-camera codec compatibility remains IN PROGRESS pending a representative camera corpus and device-level preview/proxy/export checks. Proxy items DX-PROXY-001 through DX-PROXY-005 still require their listed automated/instrumentation/device verification; source commits alone do not close them.
- **Dashboard / Settings UI verification:** Physical-device checks, narrow/wide layouts, font scaling, orientation, category back navigation, settings persistence and control-by-control regression checks have not been performed by this audit. Do not describe them as passed.
- **Release decision:** No stable APK download link or release artifact should be published until the current Android verification succeeds and the APK validator passes.


## Step 1 — foundation stabilization update — 2026-11-02

### DX-SETTINGS-001 — SettingsScreen category scope prevented Kotlin compilation

- **Severity:** Critical — blocks Android verification, unit-test compilation and debug APK assembly.
- **Root cause confirmed:** The `if (selectedCategory == SettingsCategory.APP)` block was not closed after the About section. The missing closure caused the following dialog content and helper declarations to remain nested in the wrong scope. A compensating trailing brace at end-of-file had masked the structure and did not restore top-level helper declarations.
- **Correction committed:** Added the missing close immediately after the App/Preferences category content, then removed the obsolete trailing file-level brace. Helper composables and private declarations now return to file scope.
- **Commits:** `20a77abb5c071558cca6cc680f45b813d8a6cbc5` (close category scope); `abc4bde34462df2521deedd2b6a5f1fde4f30cc6` (remove obsolete trailing brace).
- **Verification:** Android verification was triggered by the source change. Await the current workflow result before marking the compile blocker fixed. The previous failing run `41424630145` showed unresolved helper references and private-local declaration errors; its unit tests and APK assembly did not complete.

### DX-DOCS-001 — README described obsolete GitHub Actions behavior

- **Severity:** Medium
- **Status:** SOURCE UPDATED — repository-hygiene verification pending.
- **Correction:** This historical documentation correction was superseded by the current three-workflow description in README. Current CI does not publish a user-installable APK; device QA is a separate pull-request workflow.
- **Commit:** `7a525a8c29bf70b946f80f2b585929cdd1298084`.

### Step 1 acceptance gate — historical checklist (superseded by the current snapshot above)

- [x] Current main-branch Android unit-test workflow completed successfully (run 41556330546).
- [ ] API 37 managed-device instrumentation and macrobenchmark complete successfully; latest PR run was cancelled.
- [ ] Manual physical-device acceptance is recorded.
- [ ] Repository-hygiene workflow passes after README and issue-register corrections.
- [ ] Review any new Kotlin/compiler/test failures from the corrected build; do not assume the brace fix is the only possible blocker.
- [ ] Confirm APK artifact metadata and package/version identity from the generated output.
- [ ] Keep device acceptance, instrumentation and release-signing checks explicitly separate from CI success.


## Step 2 — Custom canvas dimensions audit (2026-11-02)

**Status: IN PROGRESS — source audit; no end-to-end fix is claimed yet.**

### Confirmed foundations

- `Project` already stores `customWidth` and `customHeight`, validates even dimensions from 2 through 8192 and an aspect ratio from 1:4 through 4:1, and exposes `canvasDimensions`.
- Room schema version 11 adds both columns with a zero default; legacy projects therefore retain preset-based behavior.
- `ProjectDocumentApplicator` encodes and decodes both custom fields. Project archives and autosave use the project-document path, but custom-size round trips still need explicit regression tests.
- Existing `ProjectCanvasDimensionsTest` covers model validation and preset fallback; it does not cover UI creation/edit, template propagation, or export output dimensions.

### DX-CANVAS-001 — Custom dimensions cannot be selected during project creation

- **Severity:** High
- **Status:** IMPLEMENTED IN DRAFT PR #59 — NOT ACCEPTED
- **Affected files:** `ui/projects/ProjectListViewModel.kt`, `ui/projects/ProjectListScreen.kt`, `ui/projects/ProjectTemplateSheet.kt`
- **Finding:** Draft PR #59 adds a Custom Canvas project template, width/height entry, validation and ViewModel-to-Project propagation.
- **Impact:** The source path is now present in the draft, but UI behavior has not yet passed Android tests or physical-device checks.
- **Required correction:** Verify valid/invalid input, keyboard behavior, accessibility, small-screen layout, project creation/reopen and project-document round trips. Generate and commit Room schema 11 from the configured Room/KSP build.
- **Verification:** Android unit tests, UI/instrumentation coverage, Room migration test, archive/autosave round trip and device review.

### DX-CANVAS-002 — Custom canvas dimensions are not resolved by export configuration

- **Severity:** Critical
- **Status:** IMPLEMENTED IN DRAFT PR #59 — NOT ACCEPTED
- **Affected files:** `model/ExportConfig.kt`, `ui/editor/PreviewPanel.kt`, `ui/editor/ExportDelegate.kt`, `engine/VideoEngine.kt`, `engine/ExportStoragePolicy.kt`, `engine/ExportHistoryStore.kt`
- **Finding:** Draft PR #59 adds explicit custom output width/height to ExportConfig and uses them in preview geometry, smart-crop ratio, encoder-safe renderer sizing, output verification/history and storage estimates. Selecting a platform preset clears the project custom dimensions.
- **Impact:** The source path now carries the custom canvas dimensions to the encoder boundary, but actual output files have not yet been inspected.
- **Required correction:** Verify exact exported width/height for portrait, landscape, square and nonstandard dimensions; confirm encoder-safe rounding, bitrate estimates, platform preset overrides and legacy project behavior. Keep Room schema 11 and migration validation as separate acceptance gates.
- **Verification:** Unit tests, Android instrumentation, exported-file metadata inspection and physical-device thermal/memory checks.

### DX-CANVAS-003 — Custom dimensions lack end-to-end persistence and migration regression coverage

- **Severity:** High
- **Status:** OPEN — INVESTIGATE
- **Affected files:** `engine/ProjectDocument.kt`, `engine/ProjectArchive.kt`, `engine/ProjectAutoSave.kt`, `engine/db/ProjectDatabase.kt`, related tests
- **Finding:** The fields are present in Room schema v11 and project-document serialization. Existing migration tests assert zero defaults for old schema rows, but the inspected project-document/archive tests do not explicitly round-trip non-default custom dimensions through every lifecycle.
- **Required correction:** Add focused tests for database insert/read/update, autosave/recovery, duplicate/rekey, archive export/import and legacy archive migration with custom and preset projects. Preserve custom dimensions exactly; never coerce them to a preset during rekey/import.
- **Verification:** Run the complete Room migration matrix and project-document/autosave/archive tests; verify old schema fixtures retain their prior behavior.

### DX-CANVAS-004 — Preview/crop renderer propagation is not yet proven

- **Severity:** High
- **Status:** OPEN — INVESTIGATE
- **Affected files:** preview composition and export/render pipeline (call-site trace pending)
- **Finding:** The model exposes `project.canvasDimensions`, but a repository-wide source search found no consumer of that property outside its declaration. The preview and crop path therefore needs explicit tracing before claiming custom dimensions affect the visible canvas.
- **Required correction:** Use the same resolved canvas geometry for editor preview bounds, crop/fit calculations, overlays and export. Keep source-media dimensions separate from output-canvas dimensions.
- **Verification:** Test preview and rendered output with portrait, landscape, square and nonstandard canvases; confirm crop/fit and overlay alignment.

**Audit evidence:** Current `main` source files inspected on 2026-11-02: `Project.kt`, `ExportConfig.kt`, `ProjectDocument.kt`, `ProjectDatabase.kt`, `ProjectListScreen.kt`, `ProjectListViewModel.kt`, `ProjectTemplateSheet.kt`, `ExportSheet.kt`, `ProjectCanvasDimensionsTest.kt`, `ProjectDocumentApplicatorTest.kt`, `ProjectArchiveRoundTripTest.kt`, and `ProjectDatabaseMigrationTest.kt`. This is a source-level finding, not a passing build or device result.


### DX-PROXY-004 implementation update — 2026-11-02

- **Source correction in PR #43:** Proxy deletion now returns an explicit `DELETED`, `MISSING`, `FAILED`, or `INVALID` outcome. An already-missing managed cache file clears its stale workflow reference and increments `missing`, rather than being reported as a deletion failure.
- **Failure safety:** The engine retains its in-memory cache mapping when deletion fails. Workflow cleanup retains the URI for failed/invalid files and only clears a reference if it still matches the URI being deleted, preventing a stale cleanup pass from erasing a newer proxy assignment.
- **Regression tests added:** successful deletion, already-missing files, outside-cache path rejection, unmanaged filenames, and directories.
- **Status:** Source correction merged; current main Android unit-test suite passed. Instrumentation and actual cache-pressure/device behavior remain unverified.


### DX-PROXY-004 follow-up — engine-wide cache clear mapping retention

- **Additional finding:** `clearProxies()` removed every non-active in-memory proxy mapping after scanning the cache, including mappings whose backing file deletion had failed. If cache enumeration itself failed, it could also discard mappings without evidence that the files were gone.
- **Correction in PR #45:** Preserve mappings for active proxies, failed deletions, and any backing file still present. If the cache directory cannot be enumerated, return an explicit incomplete cleanup result and leave mappings intact.
- **Regression coverage:** Added pure policy tests for active, failed, still-present, and genuinely stale mappings.
- **Status:** Source correction merged; current main Android unit-test suite passed. Instrumentation and actual cache-pressure/device behavior remain unverified.


### DX-PROXY-006 — Proxy generation can block the main dispatcher while waiting for codec capacity

- **Severity:** High
- **Status:** SOURCE FIX MERGED — Android unit-test suite passed on current main; contention/stress and UI responsiveness device verification remain required.
- **Affected files:** `ProxyEngine.kt`, `CodecInstanceBudget.kt`
- **Finding:** `generateProxy()` switches to `Dispatchers.Main` for Media3 Transformer lifecycle work, then calls the blocking `acquirePipelineBlocking()` semaphore API before creating the Transformer. If another codec-backed operation owns the final permit, this blocks the UI thread while proxy generation waits.
- **Impact:** A busy codec budget can freeze the editor UI during background proxy generation, defeating the resource queue's intended behavior.
- **Correction in PR #46:** Replace the blocking pipeline acquisition API with a suspending lease acquisition. The lease pool already moves permit waiting to `Dispatchers.IO`; Transformer construction/start remains on Main.
- **Verification required:** Android unit tests, API 37 proxy instrumentation, and a stress test where another codec lease is held while proxy generation waits; confirm the main dispatcher remains responsive and the lease is released on success, failure, and cancellation.


## Current audit addendum — 2026-11-18

This addendum supersedes the 2026-11-03 snapshot above wherever dates, branch state, CI status or current-main findings differ. The historical findings and their original evidence are retained for traceability.

### Repository baseline and change-control

- Audited default branch: `main`, HEAD `efa84e0c55b902ab59b8d2b83df60f2812192b43`.
- The maintenance work for this review was merged into `main` through [PR #53](https://github.com/AssayFlux/Director-X/pull/53).
- The branch `fix/proxy-splash-custom-canvas-2026-11-04` contains a larger, unmerged implementation candidate. Do not merge it wholesale: review each source change and run the required checks. It is not evidence that the feature is present on `main`.
- No claim in this register should be upgraded to RESOLVED — VERIFIED without the evidence required by that issue.

### DX-CANVAS-006 — CUSTOM is invalid as a global default aspect ratio

- **Severity:** High
- **Status:** FIX MERGED — automated verification pending.
- **Affected files:** `ProjectListViewModel.kt`, `SettingsScreen.kt`.
- **Finding:** Settings exposed `CUSTOM` as a global default aspect ratio, but a project created from that default received no custom width or height. The `Project` model validates custom dimensions and can reject this project during creation.
- **Correction merged:** Treat custom dimensions as project-scoped. Exclude `CUSTOM` from global default choices, display a safe 16:9 fallback for legacy persisted `CUSTOM` settings, and defensively fall back to 16:9 in project creation if an older setting still contains `CUSTOM`.
- **Regression coverage merged:** PR #55 adds tests for the resolver and construction of a valid 16:9 Project from a legacy CUSTOM default.
- **CI finding/correction:** The first PR #53 Android verification run failed because `ProjectListViewModel.kt` lacked the import for `resolveDefaultProjectAspectRatio`. The import was added in main commit `5572f9a`; the follow-up Android verification run [44345410795](https://github.com/AssayFlux/Director-X/actions/runs/44345410795) is still running and has not yet reported a result.

### DX-CANVAS-005 — Custom canvas is not end-to-end on main

- **Severity:** High
- **Status:** OPEN — CONFIRMED.
- **Finding:** Main has model/database/document foundations for custom dimensions, including Room version 11 and migration 10→11. It does not yet provide the complete creation-to-renderer path: custom-canvas UI and custom dimensions are not consistently carried through export configuration and final encoder sizing.
- **Impact:** Persisted dimensions alone do not guarantee that a user can create, preview and export a custom-size project at the selected dimensions.
- **Required correction:** Review and integrate the custom-canvas candidate in small, auditable changes. Draft [PR #56](https://github.com/AssayFlux/Director-X/pull/56) contains the candidate, but GitHub currently reports it as dirty/non-mergeable; its branch also imports `resolveDefaultProjectAspectRatio` without defining that helper and lacks the tracked Room schema 11 snapshot. Propagate validated dimensions through project creation, document/archive round-trip, editor preview, export configuration, engine/delegate and final encoder output. Preserve preset behavior for existing projects.
- **Verification required:** Unit tests for bounds/even dimensions and archive round-trip; UI/project creation tests; export tests that inspect actual output dimensions; regression tests for preset projects and rotated media.

### DX-SPLASH-002 — AssayFlux SVG splash integration

- **Severity:** Medium
- **Status:** IMPLEMENTED IN DRAFT PR #59 — NOT ACCEPTED.
- **Finding:** Draft PR #59 packages the supplied `Assets/Director-X Logo.svg` and displays it in a white, centered 1.3-second Compose entry/exit splash overlay. It does not use the previously proposed GIF.
- **Required correction:** Validate the WebView SVG renderer, asset packaging, cold-start cost, lifecycle/background behavior and accessibility. Preserve the requested 1.3-second presentation and verify it on a physical device.
- **Verification required:** Android build, cold-start and background/foreground tests, navigation tests, low-memory behavior and physical-device visual/performance checks. Do not mark the splash accepted before these pass.

### DX-PROXY-ACCEPTANCE-001 — Proxy source fixes are not full acceptance

- **Severity:** Critical
- **Status:** OPEN — DEVICE / END-TO-END ACCEPTANCE PENDING.
- **Merged foundations:** PRs #47, #48 and #49 address restart-safe temporary artifacts, cache-key namespace invalidation and Transformer cancellation fencing.
- **Still required:** Generate and inspect HALF, QUARTER and EIGHTH proxies from real high-resolution camera footage; prove selected-tier propagation through the actual WorkManager path; cancel during active transcoding; stress playback + export + proxy concurrently; kill/restart the process during work; exercise storage pressure and cache cleanup; verify exports always read original media rather than proxy media; complete acceptance on the target Moto G54 5G / Android 15.
- **Fixture limitation:** The existing 320×240 trim fixture cannot prove high-resolution tier behavior.
- **Closure rule:** A merged implementation or passing JVM suite is not a substitute for the listed end-to-end and physical-device evidence.

### DX-DB-011 — Room schema 11 snapshot is not committed

- **Severity:** Medium
- **Status:** OPEN — REPOSITORY SNAPSHOT INCOMPLETE; build impact not yet established.
- **Finding:** `ProjectDatabase` declares version 11 and migration 10→11, but tracked schema snapshots stop at `10.json`.
- **Required correction:** Generate schema 11 with the configured Room/KSP toolchain, inspect it against the entity and migration, commit `11.json`, and verify schema validation in CI/instrumentation.
- **Caution:** The absence of the committed snapshot is a repository-maintenance gap; it is not, by itself, proof that the current unit-test workflow fails.

### DX-SEC-FFMPEG-001 — Native dependency security review expired

- **Severity:** Critical for release
- **Status:** OPEN — RELEASE GATE.
- **Finding:** `third_party/ffmpeg-kit-next/native-lock.json` has a top-level `reviewUntil` of 2026-10-31, which is earlier than this audit date. The release verification script rejects expired review dates and the release build attaches this verification.
- **Required correction:** Perform a fresh review of the pinned native source revisions, enabled/disabled FFmpeg components, listed CVEs, reachability evidence, licences and generated SBOM. Update the lock and evidence only after the review is actually completed; do not extend the date mechanically.

- **Additional finding — 2026-11-18:** The advisory inventory is not exhaustive. NVD search records for FFmpeg through 8.1.2 include additional entries not present in `security.requiredAdvisories`, including CVE-2026-64834 (RTP/ASF demuxer), CVE-2026-65704 (TY demuxer/Shorten decoder), CVE-2026-64833 (S/PDIF muxer), CVE-2026-64831 (Vulkan HEVC decoder), CVE-2026-66036 (HQDN3D filter), CVE-2026-70628 (DVB subtitle parser), CVE-2026-70631 (TIFF decoder), CVE-2026-52295/52296/52297, CVE-2026-107697 and CVE-2026-96611. See [NVD FFmpeg 8.1.2 results](https://nvd.nist.gov/vuln/search?cpeFilterMode=cpe&cpeName=cpe%3A2.3%3Aa%3Affmpeg%3Affmpeg%3A3.1%3Adev%3A%2A%3A%2A%3A%2A%3A%2A%3A%2A%3A%2A%3A%2A&resultType=records) and [NVD FFmpeg 8.1.2 CVE results](https://nvd.nist.gov/vuln/search?cpeFilterMode=cpe&cpeName=cpe%3A2.3%3Aa%3Affmpeg%3Affmpeg%3A0.5.4.5%3A%2A%3A%2A%3A%2A%3A%2A%3A%2A%3A%2A%3A%2A&resultType=records). Some may be unreachable because of the shipped build profile, but the current lock and patch do not contain a disposition for them. Do not label them all exploitable; do treat them as unreviewed until component reachability is proven.
- **Patch gap:** `clearcut-security.patch` currently disables 11 named components; it does not show corresponding disable flags or fixed-source evidence for the additional advisory components. The current binary remains FFmpeg 8.1.2, built 2026-08-02.
- **Release decision:** Keep the native release gate open. Complete the full advisory inventory, confirm actual configure/build flags and binary reachability, then either move to a fixed upstream revision or rebuild with verified component exclusions. Update the native lock, patch hash, artifact hash/size, SBOM and release verification together.

- **Impact:** Release readiness is blocked until the security review is renewed and the release verification passes. This finding does not automatically imply that an ordinary debug build is blocked.

### DX-CI-DOC-001 — Repository map and automation descriptions drifted

- **Severity:** Low
- **Status:** CORRECTION MERGED — repository hygiene passed.
- **Finding:** README described `src/test` and `src/androidTest` as manually run despite the workflows automating unit tests and managed-device instrumentation.
- **Correction merged:** README repository map now identifies the relevant workflows while retaining local execution as an option.
- **Workflow truth:** Android verification runs `:app:testQaUnitTest`; device/performance QA runs lint, API 37 managed-device instrumentation and macrobenchmark on relevant pull requests; repository hygiene checks repository policy and documentation. QA workflows may create temporary test/benchmark APKs. No workflow publishes a user-installable product APK or production release.
- **Evidence limitation:** The prior main-branch unit-test run passed, while the maintenance PR's Android unit-test run and API 37 device/performance QA are still in progress as of this register update. Repository hygiene passed. Do not call device QA passed until its run completes.

### DX-BRANCH-001 — Stale maintenance branches

- **Severity:** Low
- **Status:** OPEN — CLEANUP AFTER REVIEW.
- **Finding:** Several branches remain after their associated fixes were merged, alongside the larger unmerged splash/custom-canvas candidate.
- **Required correction:** Compare each branch to main, preserve any unique commits, close/reconcile superseded work and delete only branches confirmed redundant. Never delete the candidate branch before its unique changes have been reviewed or safely ported.

### Product capability boundaries

- Export resolution presets currently top out at 4K. 8K export is not implemented or accepted.
- Professional-camera compatibility is not established by generic Media3/FFmpeg support. Validate a representative corpus and codec/profile/bit-depth/chroma combinations before making broad compatibility claims.
- Proxy, custom canvas and splash status must be reported independently: a source change, a CI result, and physical-device acceptance are different evidence levels.


### DX-DOC-README-002 — README hero image pointed to a missing asset

- **Severity:** Low
- **Status:** CORRECTED ON MAIN — repository hygiene passed in run [44344295278](https://github.com/AssayFlux/Director-X/actions/runs/44344295278).
- **Finding:** Two README image references pointed to `assets/icon.svg`, which does not exist in the current repository tree. The first two PR validation attempts failed at README link validation for this reason.
- **Correction merged:** Both README image references now use the existing `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` artwork.
- **Verification evidence:** README link/asset validation passed in repository-hygiene run [44344295278](https://github.com/AssayFlux/Director-X/actions/runs/44344295278).


### DX-DOC-README-003 — README repository map listed a removed assets directory

- **Severity:** Low
- **Status:** CORRECTED ON MAIN.
- **Finding:** The repository map still listed a root `assets/` directory after its last tracked icon asset had been removed. The README hero now uses the actual launcher resource under `app/src/main/res/`.
- **Correction merged:** Removed the obsolete root `assets/` entry from the README repository map.
- **Verification evidence:** README path and asset validation passed in repository-hygiene run [44345956760](https://github.com/AssayFlux/Director-X/actions/runs/44345956760).


### DX-HYGIENE-001 — Duplicate allowlist retained a removed root icon path

- **Severity:** Low
- **Status:** CORRECTED ON MAIN — repository hygiene passed in run [44345956760](https://github.com/AssayFlux/Director-X/actions/runs/44345956760).
- **Finding:** `scripts/check_repository_duplicates.py` still listed `assets/icon.png` as an intentional duplicate after the root assets directory and icon had been removed.
- **Correction merged:** Removed the obsolete allowlist group; the Fastlane store icon remains governed by its actual path.
- **Verification evidence:** Repository hygiene passed in run [44345956760](https://github.com/AssayFlux/Director-X/actions/runs/44345956760), after removal of the obsolete allowlist entry.


### DX-DEP-001 — Dependency freshness snapshot exceeded its 30-day review horizon

- **Severity:** Medium
- **Status:** RESOLVED — authoritative metadata refreshed 2026-11-18; pinned versions unchanged.
- **Finding:** The snapshot and fact reviews were last dated 2026-10-05, 44 days before the audit, while the offline policy requires a review within 30 days. The Android unit suite correctly failed `DependencyFreshnessTest`.
- **Correction merged:** Ran `scripts/refresh_dependency_freshness.py` against configured Maven metadata. The snapshot now covers 21 dependencies and records current latest-stable candidates. No version catalog pins were changed.
- **Refresh-script correction:** Held-candidate reasons and unblock conditions are regenerated when metadata changes; previous reasoning is retained under `previousReview` rather than leaving stale candidate versions in active guidance.
- **Decision:** Newer versions remain held until their compatibility probes pass. In particular, the AGP/Kotlin pair, Room 3 migration, ONNX per-ABI size, and MediaPipe/protobuf generated-code compatibility are not approved for automatic upgrades.


### DX-HYGIENE-002 — Mirrored user-supplied design texture

- **Severity:** Low
- **Status:** RESOLVED — repository hygiene passed in run [44350163729](https://github.com/AssayFlux/Director-X/actions/runs/44350163729).
- **Finding:** The exact-duplicate validator found `Assets/UI Design Texture.png` and `UI Design Texture.png` to be byte-identical. Both are user-supplied design-reference assets; no application or documentation reference currently selects one as the canonical path.
- **Correction:** Preserved both files and added the pair to the explicit intentional-duplicate allowlist with a comment. The README repository map now documents the mirror.
- **Verification evidence:** All repository-hygiene validators passed on the current main commit in run [44350935702](https://github.com/AssayFlux/Director-X/actions/runs/44350935702).


### DX-TEST-CRASH-001 — Crash-report version fixture no longer exercised the version-change branch

- **Severity:** Low
- **Status:** FIXED IN MAIN — Android unit-test rerun pending.
- **Finding:** Replacing historical version strings with the current app version made the event version and current-device version identical, so the test incorrectly expected a “Version it happened in” line that production code correctly omits for same-version reports.
- **Correction:** Keep the event version at the current release fixture `0.1.0`; model an upgraded install with `0.1.1` only as a simulated test value, and assert that the prior event version is shown only when it differs.
- **Verification:** Rerun [44351352630](https://github.com/AssayFlux/Director-X/actions/runs/44351352630).


## Current Alpha refresh — 18 November 2026

**Product owner:** AssayFlux  
**Product:** Director X  
**Branch:** `Alpha` (default and only branch at the time of review)  
**Base audited:** `b4dd5d71138dad6d926df6a612ed66127fff02cb`

The older `main`-branch references and workflow run IDs in this register are historical. Current verification must be performed against Alpha and the maintenance branch before any issue is marked resolved.

### DX-MEDIA-053K-001 — 5.3K source decoding and editing is not accepted

- **Severity:** Critical for the requested feature
- **Status:** OPEN — implementation and device acceptance required
- **Finding:** The project resolution enum currently ends at 4K. Media diagnostics can inspect actual track dimensions and query a decoder for a source format, but the current proxy path delegates source decoding to Media3 Transformer and only tries alternate Android decoders. This does not establish a software decode fallback when every device decoder rejects a 5.3K profile/raster.
- **Impact:** Some 5.3K camera files may import and display metadata but fail during preview, scrubbing or proxy generation on devices without a decoder capable of the source's exact dimensions/profile/bit depth.
- **Required correction:** Add an explicit source capability decision based on actual codec, profile, level, raster, frame rate, bit depth, chroma and color metadata. For unsupported native decode, implement a bounded FFmpeg software-decode proxy path with SAF URI support, cancellation, progress, storage budgeting, atomic output promotion and restart recovery. Preserve original media references and guarantee final export uses originals.
- **Verification required:** Representative 5.3K camera corpus; native and software fallback tests; HALF/QUARTER/EIGHTH proxy dimensions; audio/timestamp/rotation/HDR checks; active cancellation; process death; low-storage behavior; concurrent playback/export/proxy stress; final output inspection; physical-device acceptance. Do not claim support before these gates pass.

### DX-BRAND-ASSET-001 — supplied SVG artwork is not yet verified as in-app UI artwork

- **Severity:** High
- **Status:** ASSETS PRESENT — APP INTEGRATION UNVERIFIED
- **Assets:** `Assets/Director-X Logo.svg` and `Assets/UI Design Texture.svg`
- **Finding:** Both supplied assets exist in the repository. The redundant root SVG copy was removed in draft PR #59; the canonical asset remains under `Assets/`.
- **Required correction:** Use the supplied logo and texture as the authoritative artwork for the app's own surfaces. Inspect the SVG source, dimensions, viewBox, filters, embedded resources and licensing before converting or loading them. Remove the duplicate only after confirming all references use the canonical `Assets/` path.
- **Verification required:** App launcher/splash and approved UI-surface rendering on Android, density/scaling checks, accessibility contrast and repository duplicate validation.

### DX-CANVAS-002 — custom canvas dimensions are not propagated to encoder output

- **Severity:** High
- **Status:** OPEN — CONFIRMED
- **Finding:** `Project.canvasDimensions` is defined, but export code still resolves dimensions from `config.resolution.forAspect(config.aspectRatio)`. The current project model's maximum dimension of 8192 is a validation ceiling, not an encoder support statement.
- **Required correction:** Carry explicit canvas dimensions through project creation/edit, persistence, preview composition, export configuration, render planner, encoder-safe dimension calculation, export history and output verification. Platform presets may override the project canvas only when the user explicitly selects that preset.
- **Verification required:** Portrait, landscape, square and non-standard canvas round trips; actual output width/height inspection; encoder capability fallback; memory/thermal behavior on physical devices.


### Implementation update — draft PR #59

- DX-MEDIA-053K-001: FFmpeg software-decoded proxy fallback has been added after Media3's available hardware-decoder attempts fail. This closes the missing fallback implementation path in source, but the 5.3K source corpus and physical-device acceptance remain open.
- DX-CANVAS-002: custom canvas creation, project dimension persistence, preview geometry, smart-crop ratio, export dimensions, storage estimates and output-history dimensions have been wired in the draft. Room schema 11 generation, migration validation, encoder output inspection and physical-device validation remain open.
- Proxy artifact promotion: a source-level defect was found during this work. The code tested a temporary `proxy_tmp_*.mp4` using a predicate that deliberately rejects temporary filenames, preventing the successful artifact from being promoted. A dedicated temporary-artifact predicate and regression tests are included in the draft.
- CI: draft PR #59 has repository hygiene and API 37 QA queued/in progress at the time of this note. No passing result is claimed here.


### DX-NATIVE-001 — FFmpeg 8.1.2 native component security review is expired

- **Severity:** Critical — release gate
- **Status:** OPEN — BLOCKED
- **Finding:** The native lock review expired on 2026-10-31. Upstream advisories published after the prior review affect FFmpeg 8.1.2 components including Vulkan HEVC decode, LCL/ZLIB decode, PNG/APNG encode and the quirc filter. The existing patch disables some components, but the exact AAR component inventory is not available in the current build record.
- **Required correction:** Inspect the AAR's exact configure transcript and decoder/encoder/filter inventories; explicitly disable or backport fixes for affected components; rebuild the AAR; update artifact/patch hashes and lock metadata; run native smoke tests and the complete Android suite.
- **Evidence:** `docs/NATIVE_MEDIA_SECURITY_REVIEW_2026-11-18.md`
- **Policy:** Do not extend `reviewUntil` without this evidence.


### AssayFlux artwork and 5.3K implementation update — PR #59

- The supplied logo SVG now drives a 1.3-second white splash and the Dashboard/Settings brand marks.
- The supplied UI background SVG is used subtly on Dashboard and Settings only; the editor workspace and its existing color system are not redesigned.
- New projects default to proxy editing enabled. 5.3K-class and larger imports auto-start selected-tier proxy generation; the software fallback accepts AVC/HEVC tracks only.
- These changes are implemented in the draft PR, not yet accepted. Validate SVG rendering/performance and 5.3K camera footage on the target Android device before closing the issues.
