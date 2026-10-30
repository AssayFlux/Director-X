# Director X changelog

## v3.82.0

- Android version name: `3.82.0`; version code: `301`.
- Corrected the package identity registry, generated capability source path, launcher shortcut targets, and QA/test references to match the actual `com.directorx.editor` namespace and application ID.
- Documented the previous `com.novacut.editor` package as migration history; project transfer from that separate installation requires export/import.
- Updated public documentation and the model registry to use Director X product identity and current source paths.
- Removed the CI metadata-push operation that could fail after APK compilation and prevent artifact collection/upload.

## Rebrand and design-system pass

- Set the Android display name to Director X and added the “Designed by AssayFlux” credit to Settings → About.
- Replaced the dark palette with the warm-cream vintage darkroom palette: cream canvas, beige surfaces, charcoal text, taupe borders, amber highlights, and sage actions.
- Introduced serif display headings while retaining sans-serif controls and metadata.
- Replaced launcher and store icon artwork with an original film-camera/aperture motif.
- Removed old promotional, development, and visual-capture screenshots. Visual verification tests can generate fresh captures from a running build.
- Preserved editing code, project formats, tests, native dependency locks, and licence notices.
- Disabled upstream release checking until a Director X repository is configured.

This is an initial rebrand/design-system pass. Android builds and runtime workflows still require verification before release.
