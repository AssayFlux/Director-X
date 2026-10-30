# Director X

**Designed and maintained by AssayFlux**

Director X is an Android video-editing application maintained by AssayFlux. It builds on an existing editing-engine foundation and presents a distinct Director X product identity and visual system.

## Product identity

- Product: Director X
- Maintainer and design credit: AssayFlux
- Brand palette: warm cream, beige, charcoal, taupe, amber/brass and sage.
- Android application ID: `com.directorx.editor`.
- Android source namespace: `com.novacut.editor` (retained internally to avoid an unsafe bulk source-package migration).
- This is a new-install identity; it does not update an existing `com.novacut.editor` installation or migrate its private data automatically.

### Package identity and upgrade policy

The installed Android application ID is `com.directorx.editor`. This is a **new install**; it does not replace an existing `com.novacut.editor` installation. Users who need existing projects must export them from the old app and import them into Director X using the documented project-transfer workflow. This is a new install with an explicit export/import path. The internal Kotlin source namespace remains `com.novacut.editor` during this staged migration.

## Branding and compatibility policy

See [BRANDING_AND_COMPATIBILITY.md](docs/BRANDING_AND_COMPATIBILITY.md) for naming conventions, safe path-renaming rules, and legacy identifiers intentionally retained for Android install/data compatibility.

## Project history and technical attribution

See [ACKNOWLEDGMENTS.md](ACKNOWLEDGMENTS.md) for the detailed project history, original developer acknowledgment, current technical inventory, upstream-versus-maintainer boundaries, dependency families, native component versions and upgrade documentation requirements.

## Licence and source notices

This is an AssayFlux-maintained derivative project. AssayFlux branding does not claim ownership of inherited or third-party contributions. Preserve all applicable copyright notices, licence texts, source-offer obligations and corresponding-source access.

- [Root licence](LICENSE) — applies only to material its named holder is entitled to license.
- [Upstream MIT licence](UPSTREAM-LICENSE-MIT.txt) — retained upstream notice.
- [Third-party NOTICE](NOTICE) — project-level notices and distribution reminders.
- [Native dependency lock](third_party/ffmpeg-kit-next/native-lock.json) — pinned native component and source metadata.

Do not remove these files as duplicates. Acknowledgment summaries do not replace the original licence texts or distribution obligations.

## Build and verification

Open the project in a compatible Android Studio installation and allow Gradle to sync. The project uses Kotlin, Jetpack Compose, Android media APIs and pinned native media dependencies. Run the repository's unit tests, instrumentation tests, lint, native supply-chain checks and Android builds before distributing it.

A repository documentation update does not establish that every inherited feature has been independently revalidated or that a release is legally cleared for distribution.
