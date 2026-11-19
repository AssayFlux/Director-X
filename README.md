# Director X

**Designed by AssayFlux**  
Android-first, non-linear video editor.

- Application ID: `com.directorx.editor`
- Release version: `v0.1.0`
- Android version code: `001`
- Primary development branch: `Alpha`

Director X provides a multi-track editing workflow with preview, project persistence, media management, effects, audio tools, proxy media, custom canvas dimensions, and export.

## Project status

The authoritative engineering record is [docs/ENGINEERING_STATUS.md](docs/ENGINEERING_STATUS.md). Build instructions are in [docs/BUILD.md](docs/BUILD.md). Privacy disclosures are in [docs/privacy-policy.md](docs/privacy-policy.md).

Do not infer feature acceptance from the presence of a UI control or implementation class. The status document distinguishes implemented code, automated verification, and physical-device acceptance.

## Build

See [docs/BUILD.md](docs/BUILD.md). APKs are built manually by the maintainer; CI is not an APK distribution channel.

## Branding and assets

The supplied files in `Assets/` are the approved design sources:
- `Assets/Director-X Logo.svg`
- `Assets/UI Design Texture.svg`

The dashboard and Settings surfaces may use the approved texture. The editing workspace is outside the current UI redesign scope. Preserve the existing theme choices and icon language.

## Compatibility and notices

The application identity is Director X. Required third-party licences, copyright notices, upstream attribution, and compatibility metadata remain intact where legally or technically required. These are not product branding.

### Package identity and upgrade policy

The application ID is `com.directorx.editor`. Changing the package identity requires a **new install with an explicit export/import path**; it is not an in-place rename. The signing certificate and project/archive compatibility contracts must be preserved.

## Contributions

Changes must preserve project/archive compatibility, database migrations, original-media export, and explicit device capability checks. Do not claim 5.3K, 8K, HDR, or device acceptance without reproducible evidence.
