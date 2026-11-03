# Google Play Data safety worksheet: Director X

This is the engineering worksheet for preparing Director X's Play Console
**Data safety** form. It is derived from the in-app privacy dashboard
(`PrivacyDashboard.kt`) and the shipped manifests. It is **not yet an approved
Play Console submission**: optional MediaPipe Tasks performance metrics are
transmitted to Google when the user grants consent, so their classification under
the current Play Data safety definitions must be reviewed before submission.

See [privacy-policy.md](privacy-policy.md) for the user-facing policy.

## Form answers

| Question | Answer |
| --- | --- |
| Does your app collect or share any required user data types? | **Review required before submission**: consented MediaPipe Tasks performance metrics are sent to Google; classify them under current Play definitions. |
| Is all collected user data encrypted in transit? | **Review required** after the data-type classification above. |
| Do you provide a way for users to request deletion? | **Yes for on-device data**: users can delete it in-app or clear Android app storage. Review any third-party retention implications before submission. |
| Does your app contain ads? | **No** |
| Data types collected | **Pending Play policy classification** for opt-in MediaPipe performance metrics; project/media content remains on-device unless user exports/shares it. |
| Data types shared | **Pending Play policy classification** for opt-in MediaPipe metrics transmitted to Google. |

Director X is local-first: project media and edits stay in app-private storage.
There is no account system, app-operated backend, advertising SDK or general
analytics SDK. However, the optional MediaPipe Tasks metrics path transmits
anonymous performance measurements to Google after explicit consent. Do not
assume this is outside Play Data safety solely because the metrics are described
as anonymous; classify the data under the current Play definitions before
submitting the form.

## Optional, user-initiated network activity

| Path | Default | What is sent | Consent |
| --- | --- | --- | --- |
| Cloud generative video | Not available in the current build | No media is uploaded through this route because no production cloud provider adapter is registered | A future integration must implement provider, destination, payload-size and retention disclosure plus explicit consent |
| MediaPipe on-device task metrics (Google) | Off | Anonymous performance metrics: app id/version, task and mode, invocation and drop counts, latency, initialization errors. No frames or pixels | Versioned consent in Settings → Privacy; revoking closes running tasks |
| App update check | Compile-time disabled (`UPDATE_CHECK_AVAILABLE=false`) | No request in the current build | Not applicable until the feature is enabled in a future build |
| ML model download | User-initiated | An HTTPS GET for the model file recorded in `models.md` | The user starts the download |

No telemetry provider (Sentry, Glean, or otherwise) is integrated. The
"Opt-in usage telemetry" dashboard row is a placeholder so that adding one is a
visible change rather than a silent one.

## On-device data categories

These mirror the in-app privacy dashboard one-for-one. The categories below are
stored locally unless a row explicitly identifies a consented external service.
User-created diagnostic ZIPs and project archives leave the device only when the
user exports and shares them. The MediaPipe metrics row is an exception: the
SDK sends the disclosed performance measurements to Google after consent.

- **Project content** (clips, overlays, timelines, captions): kept until the
  project is deleted or app storage is cleared. Cloud backup carries the project
  documents only; generated media is device-transfer or archive-export only.
- **Media metadata** (durations, codecs, dimensions): discarded when the source
  clip leaves every project.
- **Downloaded ML models**: kept until removed in Settings → AI Models.
- **App preferences**: kept until app storage is cleared.
- **Saved templates / effect packs**: kept until the template is deleted.
- **Settings reset reports**: capped at the 16 most recent resets.
- **Diagnostic logs** and export-incident summaries: redacted, capped at the 10
  most recent incidents, included only in user-triggered diagnostic ZIPs.
- **Crash records**: capped at the 8 most recent records.
- **Process-death history**: capped at the 16 most recent unique records.
- **Cloud generative video**: not available in the current build; no production cloud provider adapter is registered.
- **MediaPipe on-device task metrics**: consent-gated; see the table above.
- **AI usage ledger**: stored inside the project autosave; clearable from the
  export disclosure review.
- **Opt-in usage telemetry**: not integrated; off.
- **App update check**: compile-time disabled in the current build (`UPDATE_CHECK_AVAILABLE=false`).

## Declared permissions

Normal (published) builds: `app/src/main/AndroidManifest.xml`:

| Permission | Purpose | Data safety impact |
| --- | --- | --- |
| `android.permission.RECORD_AUDIO` | Voiceover recording, only while recording | Audio is written to app-private storage; never uploaded |
| `android.permission.FOREGROUND_SERVICE` | Runs the export service | None |
| `android.permission.FOREGROUND_SERVICE_MEDIA_PROCESSING` | Export service type on API 35+ | None |
| `android.permission.FOREGROUND_SERVICE_DATA_SYNC` | Export service type fallback on API 34 (`maxSdkVersion="34"`) | None |
| `android.permission.POST_NOTIFICATIONS` | Export progress and completion notifications | None |
| `android.permission.VIBRATE` | Timeline haptics | None |
| `android.permission.INTERNET` | User-initiated model downloads; update check is compile-time disabled and no cloud provider is registered in the current build | See the optional-network table |
| `android.permission.ACCESS_NETWORK_STATE` | Wi-Fi-only model download preference | None |

Director X requests no broad storage permission. Media is read through the Android
photo picker and Storage Access Framework, so the app sees only files the user
picks.

### Streaming flavor only

The `streaming` build flavor (`app/src/streaming/AndroidManifest.xml`) is **not**
what is published to Google Play or GitHub Releases. It declares:

- `android.permission.NEARBY_WIFI_DEVICES` (`neverForLocation`): discovering
  local-network media sources
- `android.permission.ACCESS_LOCAL_NETWORK`: connecting to those sources

The release gate rejects a normal build that declares either permission, so they
cannot reach a published artifact without this worksheet changing first.
