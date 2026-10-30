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

The current Android namespace and application ID are both `com.novacut.editor`. They are deliberately retained to preserve the existing install/update lineage, signing identity, provider authorities, shortcuts, and app-private user data. Changing the displayed product name does not change the Android package identity.

The following legacy identifiers are data/API compatibility surfaces, not user-facing brand claims:

- `com.novacut.editor` package and source-directory paths.
- `com.clearcut.*` serialized schema identifiers, metadata keys, and benchmark namespace.
- `.clearcut` and `.clearcut-template` archive extensions and their MIME associations.
- Existing database, project, backup, export, and interchange formats.
- Upstream source names, attribution, copyright statements, and required licence notices.

Do not rename or rewrite these values without a versioned, backwards-compatible migration, import/export tests, and a documented release strategy. Do not remove upstream attribution or required third-party licence/source notices to make the repository appear wholly AssayFlux-authored.

## Path cleanup rules

Before renaming or deleting a path, search all Gradle files, Kotlin/Java package declarations, manifests, resource references, scripts, tests, CI workflows, documentation, release metadata, and generated-artifact tooling. Apply path moves atomically where possible, update every reference, then run the relevant unit tests, lint, instrumentation tests, and release build.

Identical file bytes are not sufficient evidence that files are redundant. Android density/round icons, localized store metadata, generated copies, legal notices, fixtures, and source-versus-distribution assets may have distinct consumers and must remain separate when referenced.

## Current migration note

The benchmark helper filename `ClearCutBenchmarkFlows.kt` was renamed to `DirectorXBenchmarkFlows.kt`. Its package and test selectors remain unchanged for compatibility. Both Fastlane locale privacy-policy URLs now point to the actual `docs/privacy-policy.md` path. The package identity registry now reports Director X as the public product name while preserving the current Android application ID.

This is a naming and documentation policy, not a claim that a full Android package migration or a complete build verification has occurred.
