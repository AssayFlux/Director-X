# Director X Privacy Notice

**Product:** Director X  
**Developer:** AssayFlux  
**Application ID:** com.directorx.editor  
**Last reviewed:** 24 November 2026

This notice describes the privacy behaviour represented by the current Director X source. It must be reviewed against the exact distributed build whenever data flows, SDKs, permissions, AI providers or network features change.

## Project media and local data

Video, audio, image, project, timeline, caption, thumbnail and export data selected or created by the user are processed for editing features. Core editing and project processing are designed to run on the device. Project databases and autosave documents may be included in Android backup, while imported source media and some generated dependencies are excluded to limit backup size. Use the explicit project archive export/import flow when moving a project with its app-managed media and dependencies.

## Media metadata and downloaded models

Media durations, codecs, dimensions and related clip metadata are extracted locally for editing. Optional machine-learning models are downloaded to app-private storage when the user chooses to install them. Users can remove downloaded models from Settings → AI Models. Model downloads require a network connection and consume device storage.

## Diagnostics and app preferences

App preferences, project recovery reports, crash breadcrumbs, process-exit history and diagnostic records are stored locally. Diagnostic bundles are user-initiated and may contain device/app state. Review a bundle before sharing it. Raw encoder text and heap-dump inclusion have separate settings controls. The app does not silently upload diagnostic bundles.

## AI and network features

AI processing that uses an installed on-device model processes the input media locally. Auto-caption model availability and language coverage depend on the installed model and build. Some model downloads require network access.

The Privacy dashboard includes a consent-gated MediaPipe metrics category. It is off by default. If enabled, the MediaPipe Tasks SDK may send anonymous task/performance metrics through Google Play Services DataTransport, including app ID/version, task/mode, invocation/drop counts, latency and initialization errors. Input media frames and pixels remain on the device. Consent can be revoked in Settings → Privacy.

Cloud generative features, where configured and enabled in a build, require a disclosure and consent step before each call. The provider and content sent must be shown before activation. Provider retention and deletion are governed by the selected provider's terms. Do not assume that a cloud feature is available in every build.

The optional update check is off by default. When enabled, Director X makes a TLS request to the public GitHub Releases API to compare the latest tag with the installed version. It does not download or install an APK. The remote service receives ordinary network request metadata.

No Sentry or Glean usage-telemetry provider is integrated in the current build. This notice must be updated if that changes.

## Permissions

The app requests Android permissions for the feature that needs them. These include microphone access for user-invoked audio recording, foreground-service permissions for media processing, notifications where required, haptic feedback, and network access for optional downloads and explicitly enabled network features. The optional streaming build has additional nearby-device and local-network permissions.

## Sharing, retention and data deletion

The app does not silently upload user-selected source media. Users control whether to export or share projects and diagnostic bundles. Local project data, preferences, models and diagnostic records can be removed through the relevant in-app controls; clearing app storage or uninstalling the app removes app-private data, subject to Android backup copies and files the user exported elsewhere. Removing a project does not delete copies already exported or shared.

If a user explicitly invokes a configured external AI provider, the provider may process the content described in the consent step. Requests to delete provider-held data must be directed to that provider under its published terms. Provider retention must be reviewed before enabling the integration.

## Contact and review

For privacy questions, data-deletion requests or support, contact the developer at **assayflux@gmail.com**. This inbox must be monitored and requests handled in accordance with applicable law and the retention practices described above. This notice is a product disclosure and not legal advice.
