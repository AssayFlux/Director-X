# Director X — Debugging & Issue Register

**Repository:** [AssayFlux/Director-X](https://github.com/AssayFlux/Director-X)  
**Branch audited:** `main`  
**Audit baseline:** `7760187420b25776c2af624aef1df6a6a34b1559` (source audit baseline; follow-up fixes are listed below)  
**Last repository review:** 2026-11-02 (Step 7 validation in progress)  
**Register status:** Initial source audit recorded; fixes are not claimed until implemented and verified.

## Product direction reminder — UI upgrade scope is strictly limited

> **Do not forget or expand this direction:** The user has authorized UI modernization for exactly five supplied screens: **(1) the Dashboard screen and (2–5) the four Settings screens**. These are the only UI surfaces in scope for this upgrade.

- The user prefers a **modern UI**. Use the existing Jetpack Compose / Material 3 architecture and modernize layout, hierarchy, cards, spacing, typography application, and control presentation only where appropriate.
- **Preserve the existing icon style exactly.** Do not replace, redraw, restyle, or migrate the current Material filled icon family (`Icons.Default` / `Icons.Filled`) to outlined, line-art, duotone, thin-stroke, or another icon catalogue. Keep existing icon choices and their recognizable visual treatment unless the user explicitly approves a change.
- Preserve the existing Director X cream/vintage visual identity and established palette unless the user approves a deliberate adjustment.
- Recommend/select a suitable UI design catalogue for these five screens only; do not apply a generic redesign that conflicts with the existing identity.
- Do **not** redesign or modify the video editor, timeline, preview/player, media import, project creation flow, templates, export flow, playback, proxy workflow, AI features, billing, navigation outside the five named screens, or any other application screen/function.
- Do not refactor unrelated code, rename features, change behavior, remove controls, or alter settings semantics as part of this UI task.
- Keep existing functionality, settings values, persistence, callbacks, and navigation behavior intact. UI-only changes must be isolated and reviewed.
- Before implementation, inspect the current Dashboard and Settings source/layout files and map each supplied screenshot to its corresponding screen. Record the exact in-scope files before editing.
- After implementation, review the diff and confirm that only the Dashboard and four Settings UI surfaces (plus strictly necessary UI resources/tests) changed. If an unrelated file appears in the diff, stop and explain why before proceeding.
- This is a scope reminder, not authorization to begin implementation before the catalogue/design direction is agreed.

This scope restriction takes precedence over broader redesign ideas or previous project suggestions.

This register tracks reproducible source-level findings, their impact, the proposed correction, and the evidence required to close each item. A source inspection is not a substitute for Android device testing.

## Status definitions

- **OPEN — CONFIRMED:** The source code demonstrates the issue; correction and verification are pending.
- **OPEN — INVESTIGATE:** A risk or behavior needs further tracing or device-level evidence before it can be called a defect.
- **IN PROGRESS:** A change is being implemented but has not passed its required verification.
- **RESOLVED — VERIFIED:** The fix is committed and the listed verification evidence has passed.

## Proxy video workflow

### DX-PROXY-001 — Proxy cache key ignores selected resolution

- **Severity:** High
- **Status:** IN PROGRESS
- **Affected file:** `app/src/main/java/com/directorx/editor/engine/ProxyEngine.kt`
- **Finding:** `keyFor(sourceUri)` derives the cache key only from the source URI. The output filename and cache lookup therefore do not distinguish HALF, QUARTER, and EIGHTH resolutions. `generateProxy()` returns an existing non-empty cached file before evaluating the requested resolution.
- **Impact:** After a proxy is generated at one resolution, requesting another resolution for the same URI can return the earlier proxy. The setting may appear to change without producing a proxy at the newly selected quality.
- **Required correction:** Make proxy identity include all output-affecting parameters, at minimum the canonical source identity/version and selected resolution (and any future codec/format profile that changes output). Ensure lookup, generation, deletion, and cache accounting use the same identity. Do not delete or overwrite a different resolution's valid proxy.
- **Verification required:** Add unit tests proving distinct resolutions produce distinct cache identities; add instrumentation coverage that generates two resolutions for the same source and checks their dimensions; verify repeat requests reuse only the matching resolution.

### DX-PROXY-002 — Background workflow hardcodes QUARTER resolution

- **Severity:** High
- **Status:** IN PROGRESS
- **Affected file:** `app/src/main/java/com/directorx/editor/engine/ProxyWorkflowEngine.kt`
- **Finding:** `generateAllProxies()` calls `proxyEngine.generateProxy(entry.originalUri, ProxyResolution.QUARTER)` directly. The workflow does not receive or read the user's selected proxy resolution.
- **Impact:** Background-generated proxies can remain quarter-resolution even when the user selects HALF or EIGHTH. This makes the setting inconsistent across proxy-generation entry points.
- **Required correction:** Pass the effective user-selected resolution into the workflow (or obtain it from the single authoritative settings source). Avoid a second hardcoded default that can drift from the setting. Keep export bound to the original media.
- **Verification required:** Test each supported setting through the actual background workflow, confirm resulting proxy dimensions, and confirm export still resolves to the original URI.

### DX-PROXY-003 — Background worker can report success when individual proxy generation fails

- **Severity:** Medium
- **Status:** IN PROGRESS
- **Affected files:** `app/src/main/java/com/directorx/editor/engine/ProxyWorkflowEngine.kt`, `app/src/main/java/com/directorx/editor/engine/ProxyGenerationWorker.kt`
- **Finding:** The workflow catches per-clip exceptions and continues. A null result from `generateProxy()` leaves that entry without a generated proxy, but the worker receives no failure summary and returns `Result.success()` after the loop.
- **Impact:** WorkManager/UI may report the batch as completed even though one or more clips did not receive proxies.
- **Required correction:** Return a structured batch result with attempted, generated, skipped, and failed counts (plus safe per-item error details). Map retryable batch failures to retry only under a defined policy; do not turn a partial failure into an unqualified success.
- **Verification required:** Add tests for all-success, partial-failure, all-failure, skipped-small-source, and cancellation cases. Verify WorkManager output data and UI status match the result.

### DX-PROXY-004 — Proxy cleanup result is not propagated by workflow deletion

- **Severity:** Medium
- **Status:** IN PROGRESS
- **Affected file:** `app/src/main/java/com/directorx/editor/engine/ProxyWorkflowEngine.kt`
- **Finding:** `deleteAllProxies()` ignores the Boolean returned by `proxyEngine.deleteProxyUri()` and clears each entry's proxy state regardless of whether the underlying file was deleted.
- **Impact:** A failed deletion can be presented as cleared while an orphaned proxy file remains on disk and is no longer represented in workflow state.
- **Required correction:** Aggregate deletion outcomes, retain or explicitly mark entries whose files could not be removed, and expose an honest cleanup result to Settings/UI. Coordinate with the engine's in-use protection.
- **Verification required:** Test successful deletion, failed deletion, missing files, and proxies currently in use; verify reported counts and actual cache contents.

### DX-PROXY-005 — Preview high-resolution eligibility and portrait orientation need end-to-end validation

- **Severity:** Medium
- **Status:** OPEN — INVESTIGATE
- **Affected file:** `app/src/main/java/com/directorx/editor/engine/ProxyWorkflowEngine.kt`
- **Finding:** The first implementation pass updated preview selection to use the long coded dimension, but a follow-up source review found the background batch filter still used `originalHeight > 1080`. That left portrait/rotated sources inconsistently eligible.
- **Correction committed:** Both preview selection and background batch filtering now call `ProxyResolutionPolicy.requiresProxy(originalWidth, originalHeight)`, which uses the larger coded dimension.
- **Remaining uncertainty:** The current source review still needs to trace every `registerMedia()` caller and establish whether width/height are consistently coded dimensions. The proxy planner separately uses displayed height derived from rotation metadata.
- **Verification required:** Test landscape, portrait, rotated portrait, and sources around the 1080-pixel threshold through import, background generation, preview selection, and export.

## Follow-up implementation log — 2026-11-02

The following corrections have been committed to `main`. They remain **IN PROGRESS** until the Android unit/instrumentation test suite and relevant device checks pass; repository-hygiene CI alone is not sufficient to close them.

- **DX-PROXY-001:** Cache identity now hashes source URI plus selected `ProxyResolution`; preview cache checks and URI lookup pass the requested resolution. Added `ProxyCacheKeyTest.kt` for stable keys, resolution separation, and source separation. Commits: `3691b0b`, `170cb6f`, `f4024dd`, `4b30a01`, `885878b`, `13c1c98`, `58ed5d4`.
- **DX-PROXY-002:** `ProxyWorkflowEngine` now reads the persisted `SettingsRepository.settings.proxyResolution` and plans/generates with that value rather than hardcoding QUARTER. Commit: `cb4ea44`.
- **DX-PROXY-003:** Workflow now returns attempted/generated/skipped/failed counts; the worker includes counts in output data, retries partial failures under a bounded policy, and fails after the retry limit. Cancellation is rethrown rather than incorrectly treated as a retryable exception. Added result contract tests. Commits: `8aeaba3`, `747d6aa`, `9f396bd`, `7760187`.
- **DX-PROXY-004:** Workflow cleanup now clears entry state only after deletion succeeds; failed deletions retain their URI/state and are counted as failures. Commit: `d2fe6a4`.
- **Additional cache safety:** Existing cache files from the old URI-only key format may remain as orphaned files until the user clears the proxy cache; the existing cache-wide cleanup scans managed proxy files. Do not delete user media or unrelated cache files.

**Verification status:** The repository-hygiene workflow passed for commit `885878b`. Later commits have triggered new repository-hygiene runs; the latest result must be checked separately. No Android Gradle test/build or physical-device test is claimed by this log.

## Existing proxy test coverage observed

The audited branch already contains:

- `app/src/test/java/com/directorx/editor/engine/ProxyResolutionPolicyTest.kt`
- `app/src/androidTest/java/com/directorx/editor/engine/ProxyResolutionInstrumentationTest.kt`

These cover resolution scaling, rotation-aware dimensions, source fallback, cached repeat requests, and direct editing when a proxy is unnecessary. They do not, in the inspected tests, establish resolution-specific cache separation or the full background WorkManager result contract.

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
- **Status:** FIXED IN SOURCE — CI verification pending
- **Affected file:** `.github/workflows/android-verification.yml`
- **Finding:** The workflow passed `app/build/outputs/apk` to `scripts/validate_apk_outputs.py`, but the validator accepts a build variant (`debug`, `qa`, or `streaming`) and resolves the repository output path itself. The workflow therefore could not correctly invoke the validator.
- **Correction:** The workflow now runs `python3 scripts/validate_apk_outputs.py debug`. It also runs the validator's `--self-test` before Gradle tests to catch fixture/metadata validation regressions.
- **Verification:** Repository hygiene passed on the workflow-fix commit. The Android verification run for that commit is still in progress; this issue must not be marked verified until unit tests, APK assembly, APK validation, and artifact upload complete successfully.

### DX-PROXY-005 implementation update — 2026-11-02

- **Source corrections committed:** Preview selection, background batch filtering, and the EditorViewModel enqueue trigger now use `ProxyResolutionPolicy.requiresProxy(width, height)` rather than checking height alone. This prevents rotated 1920×1080 coded media from being missed by the background generation trigger or batch planner.
- **Regression coverage added:** `ProxyResolutionPolicyTest.proxyEligibilityUsesTheLongDimensionForPortraitAndLandscapeMedia` checks landscape, portrait, 4K, square, and threshold cases.
- **Commits:** `0cf735d`, `1c63a26`, `f0cee32`, `7ac6ce8`, `2733fd8`, `92420a4`, `f829928`.
- **Status:** IN PROGRESS — source and unit test added; Android CI and device/instrumentation verification pending. The current implementation treats the supplied width/height as coded dimensions; verify this contract against the video metadata provider and test rotated clips end to end.


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
- **Status:** IN PROGRESS — model and project-document persistence already exist; end-to-end UI, database, preview and export behavior still require verification.
- **Confirmed implementation:** `Project.kt` contains `customWidth`, `customHeight`, `AspectRatio.CUSTOM`, validation through `CanvasDimensions`, and a computed `canvasDimensions` property. `ProjectDocument.kt` writes and reads the custom dimensions.
- **Correction to previous finding:** It is inaccurate to say custom dimensions are absent from the project model or project-document serializer. Those foundations are already present and must be preserved.
- **Remaining investigation and work:** Confirm the project-creation/edit UI; Room entity/schema/migration coverage; autosave and reopen; archive/import; templates; preview/crop sizing; and actual export renderer dimensions. `ExportConfig.kt` still has independent preset `resolution` and `aspectRatio` fields, so verify how the project canvas dimensions reach the renderer and how platform presets intentionally override them.
- **Verification required:** Legacy project round trips; custom portrait, landscape and square dimensions; invalid/odd/boundary values; database migration tests; preview dimension assertions; and exported-file dimension inspection. Do not duplicate existing model/document fields or mark this resolved from source presence alone.

## Dashboard redesign — 2026-11-02

- **Status:** IN PROGRESS — first layout refinement committed; visual and regression verification pending.
- **Scope:** `app/src/main/java/com/directorx/editor/ui/projects/ProjectListScreen.kt` only.
- **Change:** Dashboard hero horizontal inset increased from 16dp to 20dp, vertical padding tightened to 12dp, child spacing set to 14dp, and the welcome heading reduced from `displayLarge` to `displayMedium` for a more balanced first-screen hierarchy.
- **Preserved:** Existing Material filled icons, theme-driven colors, callbacks, search/sort visibility conditions, project list states, import/create actions, trash/restore, crash-report actions, and all other Dashboard behavior were not intentionally changed.
- **Commit:** `d1149f3e1ac9fe4143526e522fe1243f4397a774`.
- **Not verified yet:** Gradle build, Compose/UI tests, screenshots at narrow and wide viewports, large font scale, orientation, and physical-device behavior. This is a source-level layout refinement, not completion of the Dashboard redesign.


## Settings redesign — 2026-11-02

- **Status:** IMPLEMENTED — four-category navigation committed; Android verification and device/UI regression checks remain pending.
- **Scope:** `app/src/main/java/com/directorx/editor/ui/settings/SettingsScreen.kt` only.
- **Change:** Settings sections now use the existing theme semantic panel/border colors, established 12dp radius token, 20dp horizontal page inset, and clearer title hierarchy. The previously accepted `description` parameter is now rendered beneath each section heading instead of being discarded.
- **Preserved by this edit:** Existing section bodies, setting controls, callbacks, ViewModel state, DataStore values, appearance options and current navigation entry point were not intentionally changed.
- **Commit:** `339a6c375acbbabd13b8a712be2976099742b49f`.
- **Still required:** Verify Android unit tests/build, category back-stack/state restoration, every setting control, and device/font-scale behavior. Do not mark Step 6 fully verified until these pass.


## Settings category navigation — 2026-11-02

- **Implemented:** Added four Settings category views in `SettingsScreen.kt`: Export & Delivery, Editing & Timeline, AI & Project Storage, and App & Preferences.
- **Mapping:** All 13 existing `SettingsSection` blocks remain in place and are grouped once: 2 export, 1 editing/timeline, 2 AI/storage, and 8 app/preferences sections.
- **Navigation:** The Settings header back action returns from a selected category to the category overview; from the overview it invokes the existing parent back callback.
- **Preservation:** Existing setting controls, ViewModel callbacks, dialogs, and underlying settings state were not replaced.
- **Commit:** `81e82444` — Add Settings category navigation.
- **Validation status:** Android verification and repository hygiene workflows have been triggered for this commit. Do not close Step 6 until Android verification passes and UI/back-navigation behaviour is checked on-device, including font scaling.


## Step 7 — final validation and closure audit — 2026-11-02

- **Status:** IN PROGRESS — CI failure diagnosed; corrective commit pushed; clean Android verification pending.
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
- **Correction:** README now distinguishes Android verification from repository hygiene. It documents QA unit tests, debug APK assembly, APK validation and 14-day artifact upload, and clarifies that lint, production signing, publishing and physical-device testing are not performed by that workflow.
- **Commit:** `7a525a8c29bf70b946f80f2b585929cdd1298084`.

### Step 1 acceptance gate

- [ ] Current Android verification completes successfully: unit tests, debug APK assembly, APK validation and artifact upload.
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
- **Status:** OPEN — CONFIRMED
- **Affected files:** `ui/projects/ProjectListViewModel.kt`, `ui/projects/ProjectListScreen.kt`, `ui/projects/ProjectTemplateSheet.kt`
- **Finding:** The current `createProject` API accepts aspect ratio, frame rate, and preset resolution only. It does not accept custom width/height; template creation copies the template's preset ratio/resolution only.
- **Impact:** The model supports custom canvases, but the normal project-creation workflow cannot create one.
- **Required correction:** Add validated custom width/height entry to the existing project setup path, propagate values through the ViewModel and project constructor, and define sensible defaults when switching between preset and custom modes. Keep the user's existing project creation/navigation behavior intact.
- **Verification:** UI tests for valid/invalid dimensions, mode switching, project creation and reopening; test tags/accessibility and small-screen layout.

### DX-CANVAS-002 — Custom canvas dimensions are not resolved by export configuration

- **Severity:** Critical
- **Status:** OPEN — CONFIRMED
- **Affected files:** `model/ExportConfig.kt`, `ui/export/ExportSheet.kt`, export renderer call sites (to be traced before edits)
- **Finding:** `ExportSheet` calculates output dimensions using `config.resolution.forAspect(outputAspectRatio)`. `ExportConfig` has no explicit custom width/height and its output aspect-ratio helper returns a ratio, not the project's validated canvas dimensions.
- **Impact:** A custom project canvas can be silently exported using a preset resolution rather than its requested pixel dimensions.
- **Required correction:** Define one authoritative output-dimensions resolver that distinguishes (a) project-canvas export from (b) explicit platform/delivery presets. Carry the resolved width and height through preview, export planning, renderer configuration, bitrate/codec capability checks, and metadata. Do not let a preset override custom dimensions unless the user explicitly selects that delivery preset.
- **Verification:** Assert exact width/height at the final renderer boundary for landscape, portrait, square and nonstandard valid dimensions; verify platform presets still produce their documented sizes and legacy projects remain unchanged.

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
- **Status:** IN PROGRESS — awaiting Android unit-test CI. Instrumentation and actual cache-pressure/device behavior remain unverified.
