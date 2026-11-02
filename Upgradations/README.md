# Director X — Upgrade Plans

This directory is the source of truth for the approved upgrade direction and the evidence required before implementation.

## Documents

| Document | Purpose |
|---|---|
| [UI Layout Upgrade Plan](UI_LAYOUT_UPGRADE_PLAN.md) | Dashboard + four Settings category views: source baseline, proposed layout, measurements, implementation phases, and acceptance gates |
| [Baseline Measurements](BASELINE_MEASUREMENTS.md) | Source-verified current Compose dimensions and the values that still need runtime measurement |
| [Product Upgrade Roadmap](UPGRADE_ROADMAP.md) | Smart Proxy Editing, 8K export, custom aspect ratios, codec/capability work, performance, AI reliability, project management, and release engineering |

## Blueprint asset

The visual layout blueprint is stored at [`assets/ui-blueprints/director-x-layout-blueprint.svg`](../assets/ui-blueprints/director-x-layout-blueprint.svg).

## Status and change control

- These are plans, not claims that the features are implemented.
- UI work is restricted to the Dashboard and four Settings views.
- The editor, timeline, preview, import, export, proxy, AI, billing, and other features are outside the UI redesign scope.
- Feature roadmap items must be implemented separately with their own tests and device verification.
- Update status only when the corresponding implementation and verification evidence exists.
