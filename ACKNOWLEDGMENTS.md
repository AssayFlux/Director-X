# Director X — Acknowledgments, Project History, Technical Inventory and Attribution

**Product:** Director X  
**Maintainer and product brand:** AssayFlux  
**Repository:** https://github.com/AssayFlux/Director-X  
**Primary branch:** `main`  
**Project type:** Android video-editing application  
**Brand credit:** Designed and maintained by AssayFlux

This document records the known project lineage, the current repository's technical composition, the distinction between inherited and AssayFlux-maintained work, and the locations of the applicable licence notices. It is intended to make future upgrades and release reviews easier. It is not a substitute for the licence texts themselves.

---

## 1. Acknowledgment to the original developer

Director X is a derivative project based on the **ClearCut** Android video-editor codebase associated with **SysAdminDoc**.

We sincerely thank SysAdminDoc, the original developer, for creating and sharing the foundation from which this project has been developed. We also thank the contributors and maintainers of the upstream projects and open-source components identified in the repository's dependency metadata.

This acknowledgment does not imply endorsement, sponsorship, partnership, or affiliation by SysAdminDoc. Director X and AssayFlux are independently maintained product identities.

The upstream MIT notice currently retained in this repository identifies:

- Copyright holder: SysAdminDoc
- Notice file: `UPSTREAM-LICENSE-MIT.txt`
- Licence: MIT

That notice must remain with the portions of the project to which it applies and in copies or substantial portions distributed under its terms.

## 2. What belongs to which project

### 2.1 Upstream foundation

The inherited ClearCut codebase is the foundation of this derivative project. Its original source, implementation choices, historical contributions, copyright notices, and applicable licence terms are not converted into AssayFlux-authored work merely by changing the product name, artwork, repository, package metadata, or maintainer.

Where a file or component has upstream authorship, retain its original notices and licence conditions. A future rewrite or substantial replacement should be documented by file/component and commit rather than assumed from a branding change.

### 2.2 AssayFlux / Director X maintenance and product work

The repository identifies AssayFlux as the current product maintainer and brand. The following work is represented in the current project documentation and repository changes:

- Director X product identity and the “Designed by AssayFlux” brand credit.
- Director X visual identity, including the warm cream, beige, charcoal, taupe, amber/brass and sage design palette.
- Director X camera/aperture brand artwork and launcher/store identity assets.
- Product-facing rebrand and associated Android/store metadata work.
- Repository organization and maintenance of the Director X project.
- Documentation, release-readiness and verification tooling present in this repository.
- Subsequent fixes, upgrades, replacements and original contributions made by AssayFlux maintainers and contributors.

These are project-level categories, not a claim that every source file, line of code, dependency, test, model, or asset was originally authored by AssayFlux. For future upgrades, record new AssayFlux-authored modules and substantial rewrites in the changelog and commit history with their paths and dates.

### 2.3 Rights and ownership boundary

The root `LICENSE` names **Md Safarujjaman** and grants rights only to material for which that copyright holder is legally entitled to grant them. It must not be read as relicensing upstream code or every dependency in the repository.

No copyright assignment, transfer, waiver, or new permission from SysAdminDoc or another contributor is asserted by this document. If AssayFlux later obtains written assignment or relicensing permission, record the scope, author, date, and evidence separately.

## 3. Current Android project identity — verified source of truth

The current `app/build.gradle.kts` declares:

- Android source namespace: `com.directorx.editor`
- Android application ID: `com.directorx.editor` (new-install identity; not an in-place update of the former package)
- Compile SDK: 37
- Target SDK: 37
- Minimum SDK: 26
- Java source/target compatibility: 17
- Version code: 301
- Version name: 3.82.0
- Main UI/build technology: Kotlin and Jetpack Compose

The installed application ID is `com.directorx.editor`. The Kotlin source namespace remains `com.directorx.editor` as an internal implementation namespace for this staged migration. Existing installations under the previous application ID are separate apps; project transfer requires export/import. Keep the existing signing key for future updates to the Director X package.

The current project also retains legacy identifiers in source paths and data/schema names. Do not bulk-rename persisted database schemas, serialization keys, media/project formats, protocol fields or package identifiers without a backwards-compatible migration plan.

## 4. Main application technologies and capabilities represented in the source

The build configuration, version catalog, source tree, tests and project scripts show the following technology areas. The presence of a library or test is evidence of integration or intended capability, not proof that every feature works on every device.

### Android application and UI foundation

- Kotlin and the Android Gradle Plugin.
- Jetpack Compose UI, Foundation, Material 3 and extended Material icons.
- AndroidX Activity, Core KTX, Lifecycle, Navigation, Window and related AndroidX libraries.
- Kotlin coroutines.
- Hilt dependency injection and KSP processing.
- Room database tooling and schema snapshots.
- DataStore preferences and Android WorkManager.
- Coil 3 image loading, including video-related support.
- OkHttp networking.
- Lottie animations.
- Protobuf support and JSON utilities.

### Media playback, editing and export

- AndroidX Media3 modules including ExoPlayer, Transformer, effects, common, UI and muxer.
- FFmpegKitNext 8.1.0, bundled as a local AAR and checksum-verified by the Gradle build.
- FFmpeg 8.1.2 in the pinned native build metadata.
- OpenH264, libass and supporting native media libraries recorded in the native lock.
- Media import, playback, timeline, trim, audio, export and media-processing test areas represented in the source tree.

### On-device machine learning and media intelligence

- ONNX Runtime Android.
- MediaPipe Tasks Vision.
- DeepFilterNet Android integration is referenced by the build and notice policy for on-device audio noise reduction.
- The exact availability of each model, native library, feature flag and runtime path must be checked against the release variant being built.

### Persistence, reliability and application infrastructure

- Room database and versioned schema snapshots.
- Database migration instrumentation tests.
- Android lifecycle and background-work integration.
- Release signing configuration and release-signing identity verification tooling.
- ABI split configuration and APK size checks.
- Lint configuration and baseline.
- Build-time verification of the vendored FFmpegKitNext AAR checksum.
- Runtime open-source notice generation from resolved release dependencies and the native component lock.

## 5. Native media component inventory recorded in the repository

The file `third_party/ffmpeg-kit-next/native-lock.json` is the source of truth for the pinned native bundle. It records FFmpegKitNext 8.1.0, FFmpeg 8.1.2 and the following named native components:

| Component | Locked version | Licence expression in native lock |
|---|---:|---|
| FFmpegKitNext | 8.1.0 | LGPL-3.0-or-later |
| FFmpeg | 8.1.2 | LGPL-3.0-or-later |
| OpenH264 | 2.3.1 | BSD-2-Clause |
| libass | 0.17.4 | ISC |
| cpu-features | 0.11.0 | Apache-2.0 |
| expat | 2.8.1 | MIT |
| fontconfig | 2.18.1 | MIT |
| freetype | 2.14.3 | FTL |
| fribidi | 1.0.16 | LGPL-2.1-or-later |
| harfbuzz | 14.2.1 | MIT |
| libiconv | 1.17 | LGPL-2.1-or-later |
| libjxl | 0.11.2 | BSD-3-Clause |
| libpng | 1.6.58 | Libpng |
| libuuid | 1.0.3 | BSD-3-Clause |
| Android NDK libc++ | 27.3.13750724 | Apache-2.0-with-LLVM-exception |

The lock also records source repository/commit information, build recipe, Android NDK/API/ABI configuration, AAR and POM checksums, security profile, disabled format components and advisory review metadata. Consult that machine-readable file for exact values; do not manually substitute guessed versions.

The top-level `NOTICE` describes the FFmpegKitNext/FFmpeg and DeepFilterNet notice requirements currently recorded for the project. The Android source also contains runtime notice-generation logic in `app/build.gradle.kts`, including mappings for Apache, MIT, BSD, ISC, LGPL, FreeType, libpng and the LLVM exception. The generated runtime inventory is intended to reflect the resolved release runtime classpath; it should be regenerated and reviewed for each release.

## 6. Third-party Android/JVM dependency families

The Gradle version catalog and app build declare or configure dependency families including:

- AndroidX Core, Activity, Lifecycle, Window, Navigation, Room, SQLite, DataStore, WorkManager and ProfileInstaller.
- Jetpack Compose Foundation, UI, Material 3, tooling and UI tests.
- AndroidX Media3 playback, transformation, effects, UI and muxing.
- Kotlin, Kotlin coroutines and Kotlin serialization/runtime ecosystem components represented by the version catalog.
- Dagger/Hilt and KSP.
- Coil 3.
- ONNX Runtime Android.
- MediaPipe Tasks Vision.
- Protobuf.
- OkHttp.
- Lottie.
- DeepFilterNet Android.
- Test and QA tooling including JUnit, AndroidX Test, Espresso, UI Automator, Robolectric, Roborazzi and AndroidX Benchmark.

This is a dependency-family summary, not a complete bill of materials. Transitive artifacts and their exact versions can change when Gradle resolves a different graph. For a release-grade inventory, run the repository's runtime notice-generation task against the exact release variant and archive its generated report alongside the release provenance.

## 7. Tests, QA and maintenance tooling

The repository contains unit and Android instrumentation test source sets, media fixtures, Room schema snapshots, and scripts for build/release verification. The visible test coverage areas include media playback/export, audio processing, trimming, timeline behavior, database migration, UI layout, accessibility/QA assertions, media decoder fallback, project color policy and other editor workflows.

The `scripts/` directory includes tooling for areas such as:

- Capability registry generation and validation.
- Dependency freshness snapshots and upgrade probing.
- APK size and native 16 KB alignment checks.
- Android API 37 emulator setup and QA orchestration.
- Play listing asset generation and validation.
- Package identity and public-claim validation.
- Native supply-chain verification.
- OpenTimelineIO/export verification.
- Release artifact, sidecar, checksum and signing-fingerprint verification.
- CI provenance generation.

The existence of these tests and scripts does not mean they have all passed in the current environment. Record actual CI run links, build variants, device/API levels, test outcomes and release artifact hashes for each release. Do not claim a successful build or device test without its result.

## 8. Brand and design system

The current Director X brand documentation uses:

- Canvas: `#F5F0E6` — warm cream.
- Surface: `#EAE3D2` — warm beige.
- Primary text: `#2C2621` — charcoal brown.
- Secondary text/borders: `#8C8275` — muted taupe.
- Accent: `#C29B38` — vintage amber/brass.
- Primary action/success: `#5A6B57` — muted sage.
- Typography: serif display headings with sans-serif controls and metadata.
- Interaction: restrained tactile surfaces and phone-friendly touch targets.

Brand artwork and store assets are product identity materials. Their presence does not change the copyright or licence status of the inherited source or bundled libraries.

## 9. Licence and notice file map — do not remove required files

| File or location | Purpose |
|---|---|
| `LICENSE` | Root MIT licence naming Md Safarujjaman; scope is limited to material its holder may license. |
| `UPSTREAM-LICENSE-MIT.txt` | Retained upstream MIT notice naming SysAdminDoc. |
| `NOTICE` | Project-level notices and distribution reminders for identified native/media components. |
| `third_party/ffmpeg-kit-next/native-lock.json` | Pinned native component inventory, versions, source commits, licence expressions, checksums and security/build metadata. |
| `third_party/ffmpeg-kit-next/ffmpeg-kit-next-8.1.0.aar` and POM | Vendored native dependency and its Maven metadata. |
| Android `res/raw` licence/source resources | The NOTICE records that relevant upstream licence texts, source information and the source-offer recipe are packaged there; verify they remain present in the actual release artifact. |
| Generated runtime open-source notices | Dependency-derived notice inventory generated by the Gradle task for the resolved release runtime graph. |

Do not delete, merge away, overwrite or replace a licence file merely because its contents look repetitive. Some licences require the original copyright and permission text to accompany copies or substantial portions; some native components have distinct licence texts and source-offer obligations. A summary in this document or in `NOTICE` is not a substitute for those required texts.

## 10. Upgrade and contribution record

For each future Director X upgrade, add a dated changelog entry that records:

1. New AssayFlux-authored modules, files or substantial rewrites, with paths.
2. Upstream files retained, modified, replaced or removed, with their provenance.
3. New dependencies, exact coordinates and versions, licence identifiers and notice locations.
4. Native binaries/models, source repositories, immutable commit IDs, checksums and build recipes.
5. Database/schema or project-format changes and backwards-compatibility migrations.
6. Tests executed, exact commands/variants, CI links, devices/API levels and results.
7. Release artifact hashes and the notice/source materials shipped with that release.

Prefer commit history and a generated software bill of materials (SBOM) over manually asserting that an entire directory belongs to one author. Preserve provenance when copying, replacing or substantially modifying files.

## 11. Important legal and verification limits

This file is a technical and historical acknowledgment, not legal advice, a copyright assignment, a relicensing instrument, or a complete legal review of the repository.

The current dependency list and licence mapping must be checked against the exact release-resolved dependency graph and the contents of the APK/AAB being distributed. Confirm the required licence texts, notices, source code or source-offer access for each shipped component. Obtain qualified legal review if the distribution model or obligations are uncertain.

A branding pass, documentation edit, passing static check, or successful compile alone does not prove that every feature is functional, that every component's licence obligations have been met, or that a release is cleared for distribution.

---

**Thank you again to SysAdminDoc, upstream contributors, and the maintainers of the open-source technologies used by Director X.** AssayFlux will document its own future contributions separately and preserve the provenance and notices of the work on which the project builds.
