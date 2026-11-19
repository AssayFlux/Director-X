# Architecture Boundaries and Refactoring Rules

This document records the incremental architecture direction for Director X. It is intentionally a package-level boundary plan, not a proposal to split the Android application into Gradle modules in one risky change.

## Current high-coupling areas

The following implementation files are unusually large and should be decomposed by responsibility, with behavior-preserving extractions first:

| Area | Current concentration | Intended boundary |
|---|---|---|
| Editor orchestration | `ui/editor/EditorViewModel.kt` | Thin screen state/orchestration plus focused project, media-import, AI-assistance, and export coordinators |
| Timeline presentation | `ui/editor/Timeline.kt` | Timeline rendering, gestures, selection, and viewport state separated into focused Compose components |
| Export orchestration | `engine/ExportDelegate.kt` / export UI | Export request validation, execution coordination, output verification, and UI state separated behind interfaces |
| Media processing | `engine/VideoEngine.kt` | Preview/player lifecycle, Media3 transformation, effect composition, and device capability probing separated by responsibility |
| Project persistence | `engine/ProjectAutoSave.kt` | Autosave scheduling, durable file I/O, recovery policy, canonical fingerprints, and media-reference collection separated |
| Project UI | `ui/editor/EditorScreen.kt` and settings screens | Screen composition kept separate from application workflows and persistence |

## Dependency direction

- Compose UI depends on ViewModels and application-facing interfaces; it should not own file-format or media-engine implementation details.
- ViewModels coordinate user intent and expose observable state; they should not become the implementation home for persistence, codec policy, or archive parsing.
- Application coordinators depend on domain/model contracts and injected engine interfaces.
- Media and persistence implementations own Android framework, Media3, Room, file-system, and codec details.
- Project model and serialization contracts remain independent of Compose UI.
- Tests should target pure policies at the JVM layer and Android integration at instrumentation boundaries.

## Stage 2 extraction completed

### Project fingerprints

The canonical project-state and persistence fingerprint functions were moved from `ProjectAutoSave.kt` into `ProjectFingerprints.kt`.

This is a behavior-preserving extraction:
- The same canonical fields are included and excluded.
- The same SHA-256 representation is retained.
- The existing `ProjectStateFingerprintTest` remains the regression contract.
- The autosave coordinator no longer owns fingerprint implementation details.

## Refactoring rules

1. Extract one cohesive responsibility at a time.
2. Preserve public APIs and persisted project/archive formats unless a separately reviewed migration is provided.
3. Add or retain regression tests before moving call sites.
4. Do not introduce service locators or global mutable state to hide dependencies.
5. Prefer constructor-injected interfaces where a boundary materially improves isolation.
6. Avoid speculative abstractions for one caller.
7. Do not combine large file moves with behavior changes.
8. Run targeted JVM tests first, then the complete QA unit suite, lint, and affected instrumentation tests.
9. Do not assemble APKs during Stages 1–4; APK packaging is reserved for Stage 5.
