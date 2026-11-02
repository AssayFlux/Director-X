# Director X changelog

## v0.1.0

- Set the Android application version to v0.1.0.
- Advanced Android versionCode from 301 to 302 so installs of earlier builds can upgrade in place.
- Kept the Android application ID and Kotlin namespace at com.directorx.editor.
- Updated remaining user-facing build and media-capture naming to Director X.


## Project identity and repository maintenance — 1 November 2026

- Corrected package identity metadata, generated capability paths, launcher shortcut targets and QA references for `com.directorx.editor`.
- Recorded `com.novacut.editor` as migration history; moving projects from that separate package requires export/import.
- Updated product identity, source paths and capability documentation.
- Removed CI metadata-push behavior that could interfere with artifact handling.

## Repository workflow change — 1 November 2026

- Removed the GitHub Actions Android tests and lint workflow at the maintainer's request.
- Removed the GitHub Actions APK build/release workflow in the preceding repository change.
- Retained only lightweight repository-hygiene validation in GitHub Actions.
- Android APK compilation, installation, unit tests, instrumentation tests and lint are now maintainer-run tasks in Termux or another locally configured Android development environment.
- Reworked the README and added a Termux build guide so automation badges and documentation no longer imply that GitHub Actions builds or tests the Android application.

## Rebrand and design-system pass

- Set the Android display name to Director X and added the “Designed by AssayFlux” credit to Settings → About.
- Applied the warm-cream vintage darkroom palette: cream canvas, beige surfaces, charcoal text, taupe borders, amber highlights and sage actions.
- Introduced serif display headings with sans-serif controls and metadata.
- Replaced launcher and store identity artwork with the Director X camera/aperture motif.
- Removed old promotional, development and unverified screenshots.
- Preserved inherited editing code, project formats, upstream attribution, native dependency locks and licence notices.

Builds and runtime workflows must be verified on a real device before a release is described as tested or production-ready.
