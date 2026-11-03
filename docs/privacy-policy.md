# Director X Privacy Policy

**Last updated: 2026-11-03**

Director X is a local-first Android video editor. Your projects, media, and edits
stay on your device. Director X has no account system, no advertising, and no
analytics SDK. Nothing in this policy is aspirational: every statement below
describes what the shipped app does.

## What Director X stores, and where

Everything in this section is stored in Director X's private app storage on your
device. None of it is transmitted anywhere unless a section below says
otherwise.

| Data | Why it exists | Retention |
| --- | --- | --- |
| Project content (clips, overlays, timelines, captions) | The projects you create | Kept until you delete the project or clear app storage |
| Media metadata (durations, codecs, dimensions) | Timeline layout and export planning | Discarded when the source clip leaves every project |
| Downloaded ML models (Whisper, MediaPipe segmentation/BlazeFace, and LaMa inpainting) | On-device speech-to-text and background removal | Kept until you remove the model in Settings → AI Models |
| App preferences | Theme, export defaults, editor layout | Kept until you clear app storage |
| Saved templates and effect packs | Reusable looks you created or imported | Kept until you delete the template |
| Settings reset reports | Recovering from corrupted preferences | Capped at the 16 most recent resets |
| Diagnostic logs and export-incident summaries | Diagnosing a failed export | Capped at the 10 most recent incidents |
| Crash records | Fatal-exception breadcrumbs | Capped at the 8 most recent records |
| Process-death history | Android 11+ ANR / low-memory / native-crash summaries | Capped at the 16 most recent unique records |
| AI usage ledger | Per-project record of which AI tools touched a project | Stored inside the project autosave; clearable from the export disclosure review |

Diagnostic, crash, and process-death records are never sent anywhere. They are
included only in a diagnostic ZIP that **you** create and share, and paths and
URIs in them are redacted.

## What leaves your device

Only these, and each one requires you to act first:

- **Cloud generative video**: the current build includes a generic routing
  contract and a disclosure policy, but no production cloud provider adapter is
  registered. Cloud generative-video processing is therefore not currently
  available, and this route does not currently upload project media. Any future
  provider integration must disclose the destination, payload size and retention
  policy and obtain explicit consent before sending media.
- **MediaPipe on-device task metrics**: off by default. If you grant consent in
  Settings → Privacy, Google's MediaPipe Tasks SDK uploads anonymous performance
  metrics (app id/version, task and mode, invocation and drop counts, latency,
  initialization errors). Your frames and pixels never leave the device.
  Revoking consent closes any running task and blocks it from starting again.
- **App update check**: unavailable in the current build configuration.
  `UPDATE_CHECK_AVAILABLE` is `false` in `app/build.gradle.kts`, so the optional
  GitHub Releases lookup is compile-time disabled in this build. No update-check
  request is made by the current configuration.
- **Model downloads**: fetching a Whisper, MediaPipe, or LaMa model contacts the
  model host recorded in `docs/models.md`. You start the download; nothing about
  your projects is sent with it.

Director X integrates no advertising SDK, no attribution SDK, and no usage
analytics. The "opt-in usage telemetry" row in the in-app privacy dashboard is a
placeholder for a future provider: no telemetry provider is integrated today,
and the row exists so it cannot be added silently.

## Backup and device transfer

Director X participates in Android's built-in backup. The **cloud** backup scope is
deliberately limited to your project documents (the project database and autosave
files). Android Auto Backup is capped at 25 MB per app and fails the whole backup
when that is exceeded, so generated timeline media: freeze frames, voiceovers,
text-to-speech audio, noise-reduced audio, stabilized clips: is excluded. A
cloud restore therefore returns your projects with that generated media missing.

**Device-to-device transfer** has no such quota and carries everything, including
your imported media copies. You can also export a project archive yourself at any
time, which contains the complete project.

Cloud backup is disabled entirely if your device lacks backup encryption
capabilities. You can turn Android backup off for Director X in your device's
system settings.

## Permissions

| Permission | What it is used for |
| --- | --- |
| `RECORD_AUDIO` | Recording a voiceover, only while you are recording one |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PROCESSING`, `FOREGROUND_SERVICE_DATA_SYNC` | Keeping an export running when you leave the app |
| `POST_NOTIFICATIONS` | Export progress and completion notifications |
| `VIBRATE` | Haptic feedback on timeline edits |
| `INTERNET`, `ACCESS_NETWORK_STATE` | User-initiated model downloads; the optional update check is compile-time disabled in the current build, and no cloud provider adapter is registered |

Media is read through the Android photo picker and Storage Access Framework, so
Director X requests no broad storage permission and can only see files you pick.

The `NEARBY_WIFI_DEVICES` and `ACCESS_LOCAL_NETWORK` permissions appear **only**
in the separate `streaming` build flavor, which is not what is published on Play
or GitHub Releases. Normal builds declare neither, and the release gate fails if
they appear.

## Deletion

- **A project**: delete it from the Projects screen; it moves to trash, and
  emptying the trash removes the project, its autosave, and its managed media
  copies.
- **AI models**: Settings → AI Models → Remove model.
- **Diagnostic, crash, and process-death records**: clearing app storage removes
  them; they are also capped and rotate automatically.
- **Everything**: Android Settings → Apps → Director X → Storage → Clear storage
  removes all Director X data from the device. Uninstalling does the same.

Because Director X has no account and stores nothing about you on a server, there
is no server-side copy to request deletion of.

## Children

Director X is not directed at children and collects no personal information from
anyone, including children.

## Changes to this policy

Changes should be published in the public [Director X repository](https://github.com/AssayFlux/Director-X). Update the "Last updated" date above whenever the substance changes.

## Contact

Questions about this policy, or about anything Director X does with your data, can be raised through the [Director X GitHub issue tracker](https://github.com/AssayFlux/Director-X/issues). Do not include private media, personal data, credentials, or diagnostic archives in a public issue.
