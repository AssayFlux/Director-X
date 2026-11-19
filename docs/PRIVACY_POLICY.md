# Director X Privacy Notice

**Product:** Director X  
**Developer:** AssayFlux  
**Application ID:** `com.directorx.editor`

This notice describes the privacy posture of the application as implemented. It must be reviewed whenever data flows, SDKs, permissions, AI providers, or network features change.

## Project media and local data

Video, audio, image, project, timeline, and export data selected or created by the user are processed for editing features. The application uses app storage and Android media/document access APIs as required by the selected workflow. Users should retain independent backups of irreplaceable source media and project files.

## Diagnostics

The application includes crash, health, and diagnostic facilities. Their exact collection, retention, export, and sharing behaviour must match the current implementation and user controls. A user-initiated diagnostic export may contain device/app state and should be reviewed before sharing.

## AI and network features

AI features may depend on locally installed models or separately configured providers. A local model workflow should not be described as offline if a provider or model download requires network access. Network-dependent functionality must disclose the destination, data sent, and purpose before enabling it. No third-party provider should be implied unless the feature actually invokes it.

## Permissions

Android permissions are requested only when needed for the corresponding feature. Access to media and documents should remain scoped to user-selected content wherever the platform permits.

## Sharing and retention

The user controls whether to export, share, or transmit project media and diagnostics. The app should not silently upload user media. Retention depends on the user's device, app storage, backups, and any explicitly configured external provider.

## Contact and review

Before public distribution, publish this notice at the privacy URL configured in the store metadata and ensure the URL serves this exact current policy. This document is a product notice, not legal advice.
