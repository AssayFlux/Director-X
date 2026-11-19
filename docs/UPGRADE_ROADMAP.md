# Director X — Five-Stage Upgrade Roadmap

This roadmap is the execution record for the staged engineering upgrade. Changes are developed on dedicated upgrade branches and are not merged into `Alpha` until their acceptance gates pass.

## Build policy

- Do not assemble APKs during Stages 1–4.
- Run focused JVM, lint, repository-hygiene, instrumentation, and benchmark tests as appropriate to each stage.
- Stage 5 performs the final APK build, package/signature/ABI/native-library inspection, and release-candidate validation.
- Preserve the application ID, version/version code, signing identity, project database compatibility, archive formats, original-media export, and required legal notices.

## Stage 1 — Stability and correctness

**Scope**
- Make the Room schema/migration chain executable and verifiable, including the current schema snapshot.
- Run JVM unit tests, Android lint, and API 37 managed-device instrumentation in CI.
- Strengthen regression coverage for project migration/recovery, export configuration, and failure paths.
- Fix confirmed regressions without broad refactoring.

**Acceptance**
- Current Room schema snapshot is generated and available to migration instrumentation tests.
- Every supported database migration preserves project and media-asset data.
- `:app:testQaUnitTest`, `:app:lintQa`, repository hygiene, and managed API 37 instrumentation pass on the same revision.
- No APK is assembled.

## Stage 2 — Architecture and maintainability

**Scope**
- Map dependencies and responsibilities in the largest UI, ViewModel, export, timeline, persistence, and media-engine classes.
- Extract cohesive components behind stable interfaces; keep behavior and public project formats unchanged.
- Add focused tests around extracted boundaries before moving call sites.
- Avoid a risky all-at-once Gradle multi-module conversion.

**Acceptance**
- No intentional behavior change without a regression test.
- Dependency direction and ownership are documented.
- Unit, lint, and affected instrumentation tests pass.
- No APK is assembled.

## Stage 3 — Media pipeline reliability

**Scope**
- Validate source-media versus proxy-media boundaries.
- Harden proxy creation, cancellation, retry/recovery, temporary-file cleanup, and low-storage behavior.
- Exercise timeline frame-rate/timebase, audio/video synchronization, codec fallback, and export interruption/recovery.
- Verify output metadata and that final exports use original media.

**Acceptance**
- Deterministic tests cover critical state transitions and failure paths.
- Instrumentation tests exercise Android media/database integration where practical.
- Output fixtures and metadata checks pass.
- No APK is assembled.

## Stage 4 — Performance and usability

**Scope**
- Measure cold start, project opening, timeline interaction, preview responsiveness, memory, storage, and export throughput.
- Optimize only measured bottlenecks; avoid speculative rewrites.
- Check large projects and constrained-memory/storage scenarios.
- Recheck accessibility, navigation, and visual regressions.

**Acceptance**
- Before/after measurements and device/test conditions are recorded.
- No material correctness or accessibility regression.
- Focused unit, lint, instrumentation, and benchmark checks pass.
- No APK is assembled.

## Stage 5 — Release readiness

**Scope**
- Complete native FFmpeg provenance/security review and dependency/license audit.
- Complete physical-device acceptance, including the target device listed in the engineering status record.
- Run full regression and performance acceptance.
- Build APKs once, then inspect package identity, version, signing certificate, ABI splits, native library alignment, assets, and output size.

**Acceptance**
- All required automated and device evidence is recorded on the release-candidate commit.
- APK inspection and installation smoke checks pass.
- Remaining limitations are explicit; no unsupported feature/security/device claims are made.

## Execution log

### Stage 1
- Started on `upgrade/phase-1-stability`, based on the requested-polish branch.
- CI is being expanded to generate/check the Room v11 schema, run QA unit tests and lint, and execute API 37 managed-device instrumentation.
- Acceptance remains pending until the workflow completes and any failures are fixed.
