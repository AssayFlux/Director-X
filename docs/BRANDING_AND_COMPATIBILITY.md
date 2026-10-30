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

## Compatibility identifiers intentionally retained

The Android application ID is now `com.directorx.editor`. This is a new install identity, not an in-place update of `com.directorx.editor`; existing users must install separately and use an explicit export/import path for project transfer. The source namespace remains `com.directorx.editor` for this migration stage, so Kotlin source packages and generated-code namespace remain stable. Provider authorities are generated from `${applicationId}` and therefore follow the new installed identity. The existing signing key must still be retained for future updates to Director X builds.

The following legacy identifiers are data/API compatibility surfaces, not user-facing brand claims:

- `com.directorx.editor` source namespace and source-directory paths (internal implementation namespace, not the installed app ID).
- `com.clearcut.*` serialized schema identifiers, metadata keys, and benchmark namespace.
- `.clearcut` and `.clearcut-template` archive extensions and their MIME associations.
- Existing database, project, backup, export, and interchange formats.
- Upstream source names, attribution, copyright statements, and required licence notices.

Do not rename or rewrite these values without a versioned, backwards-compatible migration, import/export tests, and a documented release strategy. Do not remove upstream attribution or required third-party licence/source notices to make the repository appear wholly AssayFlux-authored.

## Path cleanup rules

Before renaming or deleting a path, search all Gradle files, Kotlin/Java package declarations, manifests, resource references, scripts, tests, CI workflows, documentation, release metadata, and generated-artifact tooling. Apply path moves atomically where possible, update every reference, then run the relevant unit tests, lint, instrumentation tests, and release build.

Identical file bytes are not sufficient evidence that files are redundant. Android density/round icons, localized store metadata, generated copies, legal notices, fixtures, and source-versus-distribution assets may have distinct consumers and must remain separate when referenced.

## Current migration note

The benchmark helper filename `ClearCutBenchmarkFlows.kt` was renamed to `DirectorXBenchmarkFlows.kt`. The Android application ID is `com.directorx.editor`; the source namespace remains `com.directorx.editor`. Benchmark launch targets and shortcut actions use the new application ID. The package identity registry records the separate source namespace and new-install migration policy. The signing key must be retained for future Director X updates.

This migration changes the installed Android application ID, while retaining the internal Kotlin source namespace. Build, instrumentation, signing, and device verification are still required.
