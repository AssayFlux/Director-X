# Director X — Upgrade Plans

This directory records the approved upgrade direction, source-observed baselines, implementation state, and evidence still required. Statuses below reflect `main` at the 2026-11-03 documentation review.

## Documents

| Document | Purpose |
|---|---|
| [UI Layout Upgrade Plan](UI_LAYOUT_UPGRADE_PLAN.md) | Dashboard + four Settings category views: source baseline, proposed layout, measurements, implementation phases, and acceptance gates |
| [Baseline Measurements](BASELINE_MEASUREMENTS.md) | Source-verified current Compose dimensions and the values that still need runtime measurement |
| [Product Upgrade Roadmap](UPGRADE_ROADMAP.md) | Smart Proxy Editing, 8K export, custom aspect ratios, codec/capability work, performance, AI reliability, project management, and release engineering |

## Blueprint asset

The visual layout blueprint is stored at [`assets/ui-blueprints/director-x-layout-blueprint.svg`](../assets/ui-blueprints/director-x-layout-blueprint.svg).

## Status and change control

- The Dashboard and four Settings category views are present in the current source. Their physical-device, accessibility, screenshot and full regression acceptance gates remain open; see [UI Layout Upgrade Plan](UI_LAYOUT_UPGRADE_PLAN.md).
- The broader product roadmap remains a directional backlog, not a claim that all listed features are implemented.
- UI work is restricted to the Dashboard and four Settings views.
- The editor, timeline, preview, import, export, proxy, AI, billing, and other features are outside the UI redesign scope.
- Feature roadmap items must be implemented separately with their own tests and device verification.
- Update status only when the corresponding implementation and verification evidence exists.
