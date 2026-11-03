# Director X changelog

## v0.1.0 (Android version code 302)

- Set the Android application version to v0.1.0.
- Advanced Android versionCode from 301 to 302 so installs of earlier builds can upgrade in place.
- Kept the Android application ID and Kotlin namespace at com.directorx.editor.
- Updated remaining user-facing build and media-capture naming to Director X.


## Project identity and repository maintenance — 1 November 2026

- Corrected package identity metadata, generated capability paths, launcher shortcut targets and QA references for `com.directorx.editor`.
- Recorded `com.novacut.editor` as migration history; moving projects from that separate package requires export/import.
- Updated product identity, source paths and capability documentation.
- Removed CI metadata-push behavior that could interfere with artifact handling.

## Repository workflow correction — 3 November 2026

The earlier 1 November workflow description is historical and was superseded by the current workflow set:

- `android-verification.yml` runs `:app:testQaUnitTest` on relevant application/build pushes and pull requests; it uploads test reports.
- `android-device-qa.yml` runs `:app:lintQa`, API 37 managed-device instrumentation and API 37 startup/editor macrobenchmark for relevant pull requests. It can create temporary QA/benchmark APKs, not distributable app APKs.
- `repository-hygiene.yml` validates repository identity, version consistency, README links/assets, capability registry, public feature claims, audio API policy and validator self-tests.
- Installable debug APK creation and physical-device acceptance remain manual maintainer tasks. No GitHub Actions workflow publishes a user-installable APK or production release.

## Rebrand and design-system pass

- Set the Android display name to Director X and added the “Designed by AssayFlux” credit to Settings → About.
- Applied the warm-cream vintage darkroom palette: cream canvas, beige surfaces, charcoal text, taupe borders, amber highlights and sage actions.
- Introduced serif display headings with sans-serif controls and metadata.
- Replaced launcher and store identity artwork with the Director X camera/aperture motif.
- Removed old promotional, development and unverified screenshots.
- Preserved inherited editing code, project formats, upstream attribution, native dependency locks and licence notices.

## Proxy reliability update — 3 November 2026

- PR #47 made proxy cache writes restart-safe by rendering to a temporary artifact and promoting only completed output.
- PR #48 introduced a versioned proxy artifact cache-key namespace so legacy artifacts are not reused.
- PR #49 fenced Media3 Transformer cancellation before releasing the shared codec lease.
- All three changes are merged into `main`. Android unit-test workflow run [41556330546](https://github.com/AssayFlux/Director-X/actions/runs/41556330546) passed on main commit `1a3b7b8`. Repository-hygiene run [41556330569](https://github.com/AssayFlux/Director-X/actions/runs/41556330569) also passed.
- API 37 device/performance QA run [41553185619](https://github.com/AssayFlux/Director-X/actions/runs/41553185619) was cancelled. Full proxy device acceptance remains open; see `Debugging/ISSUES.md`.

The current Android identity is `0.1.0` / version code `302`. Builds and runtime workflows must be verified on a real device before a release is described as tested or production-ready.
