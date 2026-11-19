# Director X — Data Safety Notes

This document is the current engineering data-safety declaration source for the Play listing. It must be reconciled with the actual manifest, runtime SDK inventory, and data flows before each release.

## Data categories

- **User content:** media selected for editing, project documents, timelines, thumbnails, exports, and autosave state.
- **Diagnostics:** crash records, performance/health events, and diagnostic bundles produced by the app.
- **Device/app information:** Android version, app build, ABI, codec and capability probes used to make local processing decisions.
- **AI inputs:** text, media, or prompts submitted to an explicitly configured AI provider. Provider/network features must disclose the destination and transmitted content.

## Processing and sharing

Local editing and export operate on user-selected media on the device. The app must not silently upload source media. Any remote model, update, or provider integration must be described according to its actual implementation and must not be represented as offline.

Users decide whether to export or share content and diagnostic bundles. Diagnostic bundles can contain device/app information and must be reviewed before sharing.

## Permissions and retention

Permissions are requested for the feature that needs them. Media/document access should use Android's scoped picker APIs where available. Local project and cache retention follows app storage and user deletion actions; Android backup and user-configured provider retention are outside the app's direct control.

## Release checklist

- Inventory every runtime SDK and its data collection.
- Compare the manifest permissions and network security configuration with this declaration.
- Verify AI/provider disclosures and consent gates.
- Verify diagnostic export contents and user controls.
- Reconcile every Play Console Data Safety answer with observed runtime behaviour.
