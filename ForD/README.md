# ForD — preserved upstream and protected material

This directory records legacy branding and protected legal/technical material that must not be silently erased or misrepresented during the Director X rebrand.

## Why these items are retained

- **Upstream MIT copyright:** the original copyright holder is retained exactly. AssayFlux must not replace that attribution without documented relicensing authority.
- **Third-party notices and source offers:** FFmpegKitNext/FFmpeg, DeepFilterNet, and other bundled components carry independent licence and source-distribution obligations.
- **Legacy technical identifiers:** Android package names, Kotlin package declarations, archive extensions, MIME types, database/file names, intent actions, schema IDs, signing identity, and migration keys may be persisted or externally referenced. Renaming them can break upgrades, shortcuts, project archives, file associations, saved projects, or release verification.
- **Historical product names in provenance:** old names may remain in upstream attribution and audit history to identify the source of inherited code. They are not current Director X product branding.

## Contents

- `UPSTREAM-LICENSE-MIT.txt` — verbatim upstream MIT licence and copyright.
- `UPSTREAM-NOTICE.txt` — preserved third-party notice summary.
- `LEGACY-IDENTIFIERS.md` — technical identifiers that require a separately tested migration before any rename.

## Compliance rule

This folder is an archival/audit copy, not a substitute for legally required notices in their required locations. The canonical `LICENSE`, `NOTICE`, bundled raw licence resources, source-offer files, and third-party licence directories must remain in the source/distribution locations required by their licences. Do not delete those canonical copies merely because a copy exists here.

## Rebrand boundary

Current user-facing product name and maintainer credit should be Director X / Designed by AssayFlux. Do not use this folder to conceal required attributions or imply that AssayFlux owns upstream contributions.
