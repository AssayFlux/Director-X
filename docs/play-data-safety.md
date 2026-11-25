# Director X — Google Play Data Safety engineering worksheet

**Status: engineering reference only — not a completed Play Console declaration.** Reconcile this worksheet with the exact release variant, merged manifest, runtime SDK inventory, network traces and the current Google Play Data Safety form before submission. Do not infer a Play Console answer from this document alone.

## Data categories represented by the in-app Privacy dashboard

The Settings → Privacy dashboard is the implementation inventory. The following categories are present in its model; storage and collection behaviour differs by category.

- **Project content (clips, overlays, timelines, captions):** user-selected and user-created project data, stored in app-private project storage. Android backup includes project database/autosave data but excludes some imported source media and dependencies; archive export is the supported way to move a complete project.
- **Media metadata (durations, codecs, dimensions):** extracted locally for editing and removed from the project when its source clip is removed.
- **Downloaded ML models (Whisper, MediaPipe):** optional model files stored locally; users can remove them from Settings → AI Models.
- **App preferences (theme, export defaults):** stored locally in Android DataStore.
- **Saved templates / effect packs:** stored locally.
- **Settings reset reports (preferences recovery):** local diagnostics, included only in user-triggered diagnostic exports.
- **Diagnostic logs and export-incident summaries (redacted):** local records. Diagnostic bundles are user-initiated; raw encoder text and heap-dump inclusion have separate settings controls.
- **Crash records (fatal exception breadcrumbs):** retained locally and included only in user-triggered diagnostic exports.
- **Process-death history (ANR, low-memory, native crash):** retained locally and included only in user-triggered diagnostic exports.
- **Cloud generative video calls (consent-gated):** the dashboard reserves a category for provider-mediated calls. If a provider is configured and the feature is enabled, the consent sheet must identify the provider and the content sent. Provider retention and deletion are governed by that provider's terms. Do not declare this as active collection unless it is enabled in the submitted build.
- **MediaPipe on-device task metrics (Google, consent-gated):** off by default. If the user grants the versioned consent, the MediaPipe Tasks SDK may send anonymous task/performance metrics through Google Play Services DataTransport (app ID/version, task/mode, invocation/drop counts, latency and initialization errors). Input media frames/pixels remain on device. Consent can be revoked in Settings → Privacy.
- **AI usage ledger (per-project disclosure history):** local project/autosave data; may be included in user-exported disclosure sidecars. It does not send media or hashes to a provider by itself.
- **Opt-in usage telemetry (Sentry / Glean):** future integration only; no Sentry or Glean telemetry provider is integrated in the current build. Do not declare collection by those providers unless implementation changes.
- **App update check (sideload / GitHub-release version lookup):** disabled by default. When enabled, the app requests the public GitHub Releases API to compare a version tag; no media or project content is sent. The remote service receives ordinary network request metadata.

## Manifest permissions to reconcile

The normal app manifest currently declares:

- `android.permission.RECORD_AUDIO` — microphone access for user-invoked audio recording/voiceover workflows.
- `android.permission.FOREGROUND_SERVICE` — foreground work notification/service support.
- `android.permission.FOREGROUND_SERVICE_MEDIA_PROCESSING` — long-running media processing on supported Android versions.
- `android.permission.FOREGROUND_SERVICE_DATA_SYNC` — compatibility declaration capped at API 34 for the documented service fallback.
- `android.permission.POST_NOTIFICATIONS` — user-visible foreground-work notifications on Android versions that require it.
- `android.permission.VIBRATE` — haptic feedback.
- `android.permission.INTERNET` — optional model downloads, update checks and explicitly configured network features.
- `android.permission.ACCESS_NETWORK_STATE` — network availability checks.

The optional streaming source-set manifest additionally declares:

- `android.permission.NEARBY_WIFI_DEVICES` — nearby-device discovery for the optional local-network streaming build.
- `android.permission.ACCESS_LOCAL_NETWORK` — local-network access for that optional streaming build.

Confirm the merged manifest for the exact artifact being submitted. A permission's presence does not by itself establish that personal data is collected or shared; document the actual feature and SDK behaviour.

## Processing, sharing and retention

Core editing, media metadata extraction, model inference and project storage are local. User-selected media must not be silently uploaded. Model downloads and update checks require network access. Consent-enabled MediaPipe metrics are sent through Google Play Services DataTransport as described above. Any cloud AI integration must disclose the provider, transmitted data, purpose and provider retention terms before use.

Local records remain on the device until removed by the user, the relevant project/model is deleted, app storage is cleared or the app is uninstalled, subject to Android backup behaviour. External provider retention is outside the app's direct control and must be described per provider before enabling that integration.

## Release reconciliation checklist

- Compare the merged manifest for the exact release variant with the permission list above.
- Inventory every bundled and transitive runtime SDK, including Google Play Services DataTransport.
- Inspect network behaviour with metrics consent both disabled and enabled.
- Verify that no source media, thumbnails, project documents or diagnostic bundles are transmitted without a user action and disclosure.
- Confirm the final Data Safety answers for collection, sharing, purpose, optionality, encryption in transit and deletion against observed runtime behaviour.
- Revisit this worksheet whenever permissions, SDKs, AI providers, model delivery or diagnostics change.

Official guidance: https://support.google.com/googleplay/android-developer/answer/10787469
