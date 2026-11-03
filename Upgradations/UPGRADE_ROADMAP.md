# Director X — Product Upgrade Roadmap

**Status:** Directional backlog only. This document records requested goals and known engineering work; it is not authorization to implement all items at once.

## Guardrails

- The current UI layout plan is limited to Dashboard + four Settings category views.
- Video engine, editor, timeline, proxy, export, AI, and storage work must be delivered as separate feature changes with tests.
- Never silently lower export quality, substitute proxy media for original export media, or claim a device can encode a format before probing its codec capabilities.
- Every phase requires a source audit, implementation, tests, device verification, and a documented result.

## Priority 1 — Smart Proxy Editing Engine (4K–5.3K source workflows)

**User requirement:** Edit 4K–5.3K mobile footage smoothly using device-appropriate proxy previews while retaining original media for final-quality export. Support both automatic and manual proxy workflows.

### Desired behavior
- Automatic proxy generation when source resolution/complexity and device capability indicate it will help; manual generation and regeneration remain available.
- User-selectable proxy presets: 480p, 720p, 1080p, and 1440p. The current source model has scale factors 1/2, 1/4, and 1/8; reconcile the requested output-size presets with those existing scale factors rather than relabeling them inaccurately.
- Keep original source URI and metadata as the authoritative export source.
- Map each proxy deterministically to its original source and project/clip identity.
- Preview uses a matching proxy only when it is ready and valid; otherwise fall back to original media safely.
- Export always resolves to the original source and selected output resolution/quality.
- Expose honest progress, partial failures, retry, cancellation, storage use, and cache cleanup status.
- Avoid unnecessary proxy creation for sources that do not benefit from it.
- Preserve proxy settings and behavior across app restart.

### Source implementation status — 2026-11-03

The proxy source corrections for resolution-specific cache identity, persisted background resolution, structured batch outcomes, deletion-failure retention, orientation-aware eligibility, non-blocking codec lease acquisition, atomic temporary-file commits, cache-key namespace invalidation, and Transformer cancellation fencing are merged into `main` (PRs #47–#49 completed the latest cache/cancellation work). The current main-branch Android unit-test workflow passed (run [41556330546](https://github.com/AssayFlux/Director-X/actions/runs/41556330546)).

**Acceptance is not complete:** the API 37 device/performance QA run for PR #49 was cancelled (run [41553185619](https://github.com/AssayFlux/Director-X/actions/runs/41553185619)); high-resolution three-tier generation, real WorkManager tier selection, active-transform cancellation, playback/export/proxy stress, process-restart recovery, storage-pressure cleanup, and original-source export still require explicit instrumentation/device evidence. Track each gate in `../Debugging/ISSUES.md`.

## Priority 2 — 8K export and high-resolution pipeline

**Requested target:** Up to 8K UHD (7680×4320), while retaining the original-quality export path. The current `Resolution` enum only goes through 4K, so 8K is a new capability, not an existing feature.

### Required engineering work
1. Add an 8K output profile only after validating Media3 Transformer and Android codec support on actual devices.
2. Capability probe must inspect supported encoder MIME types, width/height alignment, maximum supported dimensions, frame rates, bitrate ranges, profile/level, hardware acceleration, and whether software fallback exists.
3. Provide a clear per-device capability result: supported hardware encode, supported software encode, unsupported, or not verified.
4. Never promise 8K export solely because the source is 8K or because the UI option exists.
5. Use safe encoder dimension alignment and aspect-ratio calculations; test landscape, portrait, rotated media, and custom canvas dimensions.
6. Add HEVC/H.265 and AV1 capability negotiation where supported; keep H.264 as compatibility fallback. Codec choice must be explicit and visible.
7. Validate 10-bit/HDR only where the full decode → effects/composition → encode path preserves the requested color format. Do not label an SDR render as HDR.
8. Estimate storage, memory, heat, and render time before export. Offer warning/confirmation for high resource demand; never silently downgrade resolution or quality.
9. Build device test matrix including 1080p, 4K, and 8K dimensions, 24/30/60fps where supported, long-form output, cancellation, low storage, thermal throttling, and process interruption.
10. Verify resulting file dimensions, codec/profile, frame rate, bitrate, color metadata, audio sync, and playback in an independent media inspector.

### Acceptance criteria
- 7680×4320 is offered only on devices where the actual output path is verified.
- Unsupported devices receive an accurate explanation and safe alternatives; no false success.
- Export uses original media, never proxy files.
- Tests prove no silent resolution/codec/bit-depth fallback.

## Priority 3 — Custom aspect ratio and canvas dimensions

**Requested feature:** Let users enter custom width and height instead of being limited to built-in ratios.

### Design and validation
- Provide width and height integer inputs and a live reduced aspect-ratio preview.
- Validate positive values, sensible maximums, even encoder-compatible dimensions, and device/codec maximums at export time.
- Persist custom canvas dimensions with project metadata and migrate older projects without changing their existing ratio.
- Keep project canvas ratio separate from export resolution; cropping/letterboxing behavior must be explicit.
- Test portrait, landscape, square, extreme ratios, odd dimensions, rotation metadata, reopen, template save/import, and export.
- Do not overload the existing enum with arbitrary values; introduce a backward-compatible value object or persisted custom-dimensions model.

## Priority 4 — Export capability and quality negotiation

- Audit current H.264, HEVC, and AV1 paths and their real device capability probes.
- Show codec, resolution, frame rate, bitrate/quality, audio, and color intent clearly.
- Separate requested settings from negotiated output; report any change before rendering and require user approval for any quality reduction.
- Verify 4K/8K export estimates against measured render performance; do not rely on a simplistic linear estimate alone.
- Add export failure diagnostics for codec rejection, unsupported dimensions, memory pressure, storage exhaustion, and thermal conditions.

## Priority 5 — Performance, reliability, and resource management

- Profile editor startup, project open, timeline scrubbing, preview decode, proxy generation, memory peaks, and export.
- Add memory-aware thumbnail/preview cache budgets and deterministic cleanup.
- Use background work with cancellation, retry rules, and structured per-item results.
- Verify large source media and long timelines on mid-range Android hardware, including the user's Moto G54 5G / Android 15 test environment when available.
- Avoid claiming real-time 4K/8K editing on devices that cannot sustain it; use proxies to keep preview responsive.

## Priority 6 — AI model and provider reliability

- Audit existing AI model download, storage, consent, offline behavior, and failure recovery before adding providers.
- Keep model state visible: not installed, downloading, ready, failed, update available.
- Make network/cloud use explicit and consent-controlled; local/offline behavior must be documented separately.
- Preserve user projects and editor functionality when an AI model is unavailable.
- Any new provider integration requires a separate provider/security/cost plan and explicit user approval.

## Priority 7 — Project and media management

- Improve project recovery, import error handling, media relinking, proxy cache visibility, and storage cleanup reporting.
- Preserve project IDs and media references during migrations.
- Test backup/restore and interrupted operations before expanding the feature set.

## Priority 8 — Release engineering and documentation

- Keep version name/code consistent across Gradle, README, release metadata, and APK artifacts.
- Run APK builds on structural/build-relevant changes and release workflows, not every ordinary documentation or content commit.
- Fix actual CI failures; do not treat a badge-only change as a repair.
- Publish APK downloads only from a verified successful artifact, with checksum and build commit.
- Keep README status badges truthful and link to the underlying workflows.
- Add device/API test evidence and known limitations to each release.

## Suggested delivery order

1. Complete the remaining proxy instrumentation and physical-device acceptance gates for the source fixes already merged (DX-PROXY-001 through DX-PROXY-007).
2. Complete the UI-only Dashboard + Settings layout upgrade as its own isolated change.
3. Add custom aspect ratio as a separate model/migration/UI/export feature.
4. Complete export capability negotiation and 8K feasibility/device matrix.
5. Add verified 8K support only after the previous gate passes.
6. Improve performance and reliability based on profiling data.
7. Continue AI/provider and project-management work as separately scoped milestones.

This order is a risk-reduction proposal. The user may reprioritize it, but scope boundaries and verification gates remain mandatory.
