# Stage 4 — Performance and usability measurement

## Measurement principles

- Record measurements on the same device model, OS build, app commit, fixture set, thermal state, and compilation mode when comparing revisions.
- Keep cold-start and warm-start results separate.
- Use repeated Macrobenchmark iterations and report median plus the full observed range; do not compare a single run.
- Record frame timing percentiles and frame-overrun/jank counts for interactive journeys.
- Record memory and storage observations separately; Macrobenchmark timing alone does not establish memory or storage safety.
- Do not describe an optimization as a speedup until the before/after measurements are available.
- Emulators are useful for repeatable regression signals, but do not replace physical-device acceptance.

## Existing automated coverage

`baselineprofile/StartupAndEditorMacrobenchmark.kt` measures:
- Cold startup with default compilation.
- Cold startup with the baseline profile.
- Warm startup with the baseline profile.
- Blank-editor entry and timeline viewport scrubbing.
- Export-sheet opening frame timing.
- Settings and privacy-dashboard navigation frame timing.

The baseline profile journey now includes project gallery, blank editor, export sheet, timeline scrubbing, and settings/privacy navigation.

## Required measurement record

For each benchmark run, record:

| Field | Value |
|---|---|
| Commit SHA | Pending |
| Device model | Pending |
| Android build / API | Pending |
| Build variant and compilation mode | Pending |
| Fixture / project size | Pending |
| Thermal and battery conditions | Pending |
| Iteration count | Defined by benchmark |
| Startup median and range | Pending |
| Frame timing percentiles / jank | Pending |
| Memory observations | Pending |
| Storage before / after | Pending |
| Export duration and output metadata | Pending |

## Large-project and constrained-resource scenarios

Physical-device acceptance should include:
- A project with many timeline clips and visible thumbnails.
- Long-GOP and high-resolution source media.
- A project with multiple audio and video tracks.
- Repeated open/close and timeline scrub cycles.
- Low free-storage and interrupted export scenarios.
- Background/foreground transitions during preview and export.

Record crashes, ANRs, dropped frames, memory pressure, cache growth, export duration, and output metadata. Never delete source media as part of cache cleanup.

## Current status

Benchmark coverage has been extended, but no new benchmark has been executed as part of this source-only change. No performance improvement is claimed yet. The configured managed Pixel 6 API 37 benchmark device is a repeatable CI signal; physical-device performance acceptance remains a Stage 5 requirement.
