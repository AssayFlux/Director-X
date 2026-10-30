# Director X branding and compatibility policy

**Product brand:** Director X  
**Maintainer:** AssayFlux  
**Repository:** https://github.com/AssayFlux/Director-X

This policy distinguishes user-visible product branding from technical identifiers that must remain stable for compatibility.

## Names used for new work

- Use **Director X** in user-facing documents, UI copy, store listings, generated artwork, release notes, and newly introduced user-visible filenames.
- Use **AssayFlux** for maintainer, design, and product-credit references.
- Use `DirectorX` for new internal helper filenames and symbols where a product-specific name is useful.
- Keep repository paths descriptive, lowercase, and purpose-based. Do not add branding to generic folders such as `app/`, `docs/`, `scripts/`, or `fastlane/` merely to make their names look branded.

## Current Android identity

The current Android build uses the same product package for its installed identity and Kotlin namespace:

- Product name: **Director X**
- Maintainer: **AssayFlux**
- Application ID: `com.directorx.editor`
- Kotlin namespace and source package: `com.directorx.editor`
- Previous application/source package recorded for migration history: `com.novacut.editor`
- Version name/code: read from `app/build.gradle.kts`; do not duplicate or independently increment these values in scripts.

The move from the previous NovaCut package identity to `com.directorx.editor` is a new-install migration, not an Android in-place package update. Existing users of the previous package must export projects from that installation and import them into Director X. Future updates to Director X must retain the pinned release signing identity.

## Compatibility identifiers intentionally retained

The following identifiers are legacy data/API compatibility surfaces, not public product branding:

- `com.clearcut.*` serialized schema identifiers, metadata keys, and benchmark namespace.
- `.clearcut` and `.clearcut-template` archive extensions and their MIME associations.
- Existing database names, project, backup, export, and interchange formats.
- Upstream source names, attribution, copyright statements, and required licence notices.

Do not rename or rewrite these values without a versioned, backwards-compatible migration, import/export tests, and a documented release strategy. Do not remove upstream attribution or required third-party licence/source notices to make the repository appear wholly AssayFlux-authored.

## Path cleanup rules

Before renaming or deleting a path, search all Gradle files, Kotlin/Java package declarations, manifests, resource references, scripts, tests, CI workflows, documentation, release metadata, and generated-artifact tooling. Apply path moves atomically where possible, update every reference, then run the relevant unit tests, lint, instrumentation tests, and release build.

Identical file bytes are not sufficient evidence that files are redundant. Android density/round icons, localized store metadata, generated copies, legal notices, fixtures, and source-versus-distribution assets may have distinct consumers and must remain separate when referenced.

## Current migration note

The benchmark helper filename `ClearCutBenchmarkFlows.kt` was renamed to `DirectorXBenchmarkFlows.kt`. The Android application ID is `com.directorx.editor`; the source namespace remains `com.directorx.editor`. Benchmark launch targets and shortcut actions use the new application ID. The package identity registry records the separate source namespace and new-install migration policy. The signing key must be retained for future Director X updates.

This migration changes source/package and schema snapshot paths only; it does not change the installed Android application ID. Build, instrumentation, signing, and device verification are still required.
