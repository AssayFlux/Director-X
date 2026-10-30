# Director X

**Designed by AssayFlux**

Director X is an Android video-editing application built on an existing open-source editor foundation. This customized fork focuses on a warm, vintage darkroom visual language while retaining the inherited editing engine and its supporting workflows.

## Design system

- Canvas: `#F5F0E6` — warm cream / aged paper
- Surface: `#EAE3D2` — warm beige
- Primary text: `#2C2621` — charcoal brown
- Secondary text and borders: `#8C8275` — muted taupe
- Accent: `#C29B38` — vintage amber
- Primary action / success: `#5A6B57` — muted sage
- Typography: classic serif display headings with sans-serif controls and metadata
- Interaction: tactile, restrained surfaces and rounded containers designed for phone touch targets

## Source and attribution

This repository is a rebrand and modification of the open-source ClearCut project. It retains the original MIT licence and required third-party notices. See [LICENSE](LICENSE), [NOTICE](NOTICE), and the corresponding-source materials under `third_party/` for the licensing terms that must accompany redistribution.

## Build

Open this project in a compatible Android Studio installation and allow Gradle to sync. The project uses Kotlin, Jetpack Compose, Android media APIs, and its pinned native FFmpegKitNext dependency. Build and test status must be verified in the target environment before treating an APK as release-ready.

## Current status

This archive is a source-code rebrand and design-system pass, not a claim that every inherited feature has been independently revalidated. Run unit tests, lint, and Android builds before distributing it. Existing package identifiers and internal project formats are intentionally retained in this first pass to reduce compatibility risk.
