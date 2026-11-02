# Director X — Debugging & Issue Register

**Repository:** [AssayFlux/Director-X](https://github.com/AssayFlux/Director-X)  
**Branch audited:** `main`  
**Audit baseline:** `63bd0fbf0fdda89630781338548e3ec0613728b4`  
**Last repository review:** 2026-11-02  
**Register status:** Initial source audit recorded; fixes are not claimed until implemented and verified.

This register tracks reproducible source-level findings, their impact, the proposed correction, and the evidence required to close each item. A source inspection is not a substitute for Android device testing.

## Status definitions

- **OPEN — CONFIRMED:** The source code demonstrates the issue; correction and verification are pending.
- **OPEN — INVESTIGATE:** A risk or behavior needs further tracing or device-level evidence before it can be called a defect.
- **IN PROGRESS:** A change is being implemented but has not passed its required verification.
- **RESOLVED — VERIFIED:** The fix is committed and the listed verification evidence has passed.

## Proxy video workflow

### DX-PROXY-001 — Proxy cache key ignores selected resolution

- **Severity:** High
- **Status:** OPEN — CONFIRMED
- **Affected file:** `app/src/main/java/com/directorx/editor/engine/ProxyEngine.kt`
- **Finding:** `keyFor(sourceUri)` derives the cache key only from the source URI. The output filename and cache lookup therefore do not distinguish HALF, QUARTER, and EIGHTH resolutions. `generateProxy()` returns an existing non-empty cached file before evaluating the requested resolution.
- **Impact:** After a proxy is generated at one resolution, requesting another resolution for the same URI can return the earlier proxy. The setting may appear to change without producing a proxy at the newly selected quality.
- **Required correction:** Make proxy identity include all output-affecting parameters, at minimum the canonical source identity/version and selected resolution (and any future codec/format profile that changes output). Ensure lookup, generation, deletion, and cache accounting use the same identity. Do not delete or overwrite a different resolution's valid proxy.
- **Verification required:** Add unit tests proving distinct resolutions produce distinct cache identities; add instrumentation coverage that generates two resolutions for the same source and checks their dimensions; verify repeat requests reuse only the matching resolution.

### DX-PROXY-002 — Background workflow hardcodes QUARTER resolution

- **Severity:** High
- **Status:** OPEN — CONFIRMED
- **Affected file:** `app/src/main/java/com/directorx/editor/engine/ProxyWorkflowEngine.kt`
- **Finding:** `generateAllProxies()` calls `proxyEngine.generateProxy(entry.originalUri, ProxyResolution.QUARTER)` directly. The workflow does not receive or read the user's selected proxy resolution.
- **Impact:** Background-generated proxies can remain quarter-resolution even when the user selects HALF or EIGHTH. This makes the setting inconsistent across proxy-generation entry points.
- **Required correction:** Pass the effective user-selected resolution into the workflow (or obtain it from the single authoritative settings source). Avoid a second hardcoded default that can drift from the setting. Keep export bound to the original media.
- **Verification required:** Test each supported setting through the actual background workflow, confirm resulting proxy dimensions, and confirm export still resolves to the original URI.

### DX-PROXY-003 — Background worker can report success when individual proxy generation fails

- **Severity:** Medium
- **Status:** OPEN — CONFIRMED
- **Affected files:** `app/src/main/java/com/directorx/editor/engine/ProxyWorkflowEngine.kt`, `app/src/main/java/com/directorx/editor/engine/ProxyGenerationWorker.kt`
- **Finding:** The workflow catches per-clip exceptions and continues. A null result from `generateProxy()` leaves that entry without a generated proxy, but the worker receives no failure summary and returns `Result.success()` after the loop.
- **Impact:** WorkManager/UI may report the batch as completed even though one or more clips did not receive proxies.
- **Required correction:** Return a structured batch result with attempted, generated, skipped, and failed counts (plus safe per-item error details). Map retryable batch failures to retry only under a defined policy; do not turn a partial failure into an unqualified success.
- **Verification required:** Add tests for all-success, partial-failure, all-failure, skipped-small-source, and cancellation cases. Verify WorkManager output data and UI status match the result.

### DX-PROXY-004 — Proxy cleanup result is not propagated by workflow deletion

- **Severity:** Medium
- **Status:** OPEN — CONFIRMED
- **Affected file:** `app/src/main/java/com/directorx/editor/engine/ProxyWorkflowEngine.kt`
- **Finding:** `deleteAllProxies()` ignores the Boolean returned by `proxyEngine.deleteProxyUri()` and clears each entry's proxy state regardless of whether the underlying file was deleted.
- **Impact:** A failed deletion can be presented as cleared while an orphaned proxy file remains on disk and is no longer represented in workflow state.
- **Required correction:** Aggregate deletion outcomes, retain or explicitly mark entries whose files could not be removed, and expose an honest cleanup result to Settings/UI. Coordinate with the engine's in-use protection.
- **Verification required:** Test successful deletion, failed deletion, missing files, and proxies currently in use; verify reported counts and actual cache contents.

### DX-PROXY-005 — Preview high-resolution eligibility and portrait orientation need end-to-end validation

- **Severity:** Medium
- **Status:** OPEN — INVESTIGATE
- **Affected file:** `app/src/main/java/com/directorx/editor/engine/ProxyWorkflowEngine.kt`
- **Finding:** Preview selection checks `entry.originalHeight > 1080`, while proxy planning in `ProxyEngine` derives displayed height from media metadata and rotation. The workflow's stored `originalHeight` is supplied by its caller.
- **Impact / uncertainty:** If callers store coded rather than displayed dimensions, rotated portrait clips may be treated differently by preview selection than by proxy planning. The current source review does not establish what every caller supplies.
- **Required investigation:** Trace all `registerMedia()` callers and document whether width/height are coded or display-oriented. Prefer one shared orientation-aware eligibility policy if the values can diverge.
- **Verification required:** Test landscape, portrait, rotated portrait, and sources around the 1080-pixel threshold through import, background generation, preview selection, and export.

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
