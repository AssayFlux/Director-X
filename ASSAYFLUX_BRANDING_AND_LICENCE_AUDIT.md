# AssayFlux / Director X Branding and Licence Audit

Audit date: 2026-10-30

## Product identity
- Product: Director X
- Brand credit: Designed by AssayFlux
- Android namespace: `com.assayflux.directorx`
- Android application ID: `org.assayflux.directorx`

## Branding policy
Replace old product-facing names, logos, splash screens, onboarding copy, app labels, navigation labels, store assets, and maintainer-controlled URLs with Director X / AssayFlux. Do not indiscriminately replace identifiers in persisted schemas, protocol fields, file formats, or third-party source.

## Licence and attribution policy
This repository is an AssayFlux-maintained fork. Maintainer branding does not transfer copyright ownership. Preserve upstream copyright notices, licence texts, NOTICE files, SPDX identifiers, source-offer obligations, and third-party attributions. Never substitute AssayFlux as copyright holder without written rights transfer or explicit relicensing authority.

## Known compatibility-sensitive legacy identifiers
Some `novaCut*` JSON keys are used in OpenFX effect descriptors and C2PA-related metadata. These are serialization/schema identifiers, not visible branding. They must remain readable until a versioned migration supports old and new keys with backwards-compatible aliases.

## Verification limits
The complete source archive was inspected locally, but this GitHub branch currently contains only the repository landing page and this audit documentation plus the SVG artwork. The full 75 MB source tree and PNG binary have not yet been uploaded to GitHub. No Android build or device test is claimed here.
