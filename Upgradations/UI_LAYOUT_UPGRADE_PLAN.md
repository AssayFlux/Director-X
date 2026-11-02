# Director X — UI Layout Upgrade Plan

**Status:** Planning only — no application UI code changed by this plan  
**Repository:** https://github.com/AssayFlux/Director-X  
**Branch reviewed:** `main`  
**Design scope:** Dashboard + Settings only  
**Identity:** Existing Director X palette and existing Material filled icons are locked

## 1. Objective

Modernize the layout so the first screen is easier to scan and the settings are easier to navigate, while preserving every existing capability and its behavior. This is a layout and information-architecture upgrade, not a feature rewrite or a colour rebrand.

The visual blueprint is stored at [`assets/ui-blueprints/director-x-layout-blueprint.svg`](../assets/ui-blueprints/director-x-layout-blueprint.svg).

## 2. Non-negotiable scope and compatibility rules

1. Only the Dashboard / Project List and the four proposed Settings category views may be redesigned.
2. **Preserve the complete existing theme selector and every palette.** The source currently exposes ten selectable themes: Primary Brand, Midnight Cinema, Forest Atelier, Burgundy Editorial, Ocean Studio, Graphite Luxe, Sandstone, Lavender Film, Copper Studio, and Arctic Minimal. Keep all ten options, their exact palette mappings, current selected value, DataStore persistence, and preview/application behavior. Retain legacy persisted identifiers for compatibility. Do not hardcode the blueprint's cream sample colours into app UI or replace/reduce the theme selector. The blueprint colours are illustrative layout ink only.\n3. Keep the existing typography identity and current Material filled icon family and icon choices. Do not replace icons with outline or another icon pack.
4. Keep the existing Jetpack Compose + Material 3 architecture.
5. Preserve all callbacks, ViewModel state, persistence, navigation destinations, project operations, dialogs, import flows, crash-report actions, settings values, permissions, and accessibility semantics.
6. Do not redesign or change the editor, preview/player, timeline, media import flow, project creation/template sheet, export sheet, playback, proxy system, AI processing screens, billing, or unrelated screens.
7. No invented actions. A visual shortcut may appear only if it invokes an already existing action with the same semantics.
8. No setting may be deleted, silently reset, renamed in storage, or changed in meaning.
9. Treat all measurements as dp for layout and sp for text. Respect system insets, font scaling, screen width, display cutouts, and accessibility touch targets.
10. Do not change application version, package ID, build system, dependencies, or CI as part of the UI-only implementation.
11. Appearance is a relocation only: all current theme options, appearance controls, labels, and persistence must remain fully available in the new category page.

## 3. Current UI — source-verified baseline

### Dashboard / Project List

Source: `app/src/main/java/com/directorx/editor/ui/projects/ProjectListScreen.kt`

- The screen is a full-size `ClearCutScreenBackground` containing a vertical `Column`.
- `ProjectHomeHero` appears first. It is a vertically scrollable `Column`, fills available width, and is capped at 62% of screen height with a minimum cap of 320dp.
- Hero horizontal and vertical padding are both `Spacing.lg` = **16dp**.
- Hero children use `Arrangement.spacedBy(Spacing.lg)` = **16dp**.
- The brand/movie icon is **24dp**.
- The settings control uses the shared chrome icon button; shared minimum touch target is **48dp**, default icon size **18dp**.
- The hero contains: brand/title row, divider, large welcome heading/body, primary create-project and secondary import actions, conditional search, conditional sort chips, and a trailing divider.
- Search and sort are conditional on project collection state. They must remain conditional exactly as today.
- Below the hero, the screen conditionally displays crash-report notice, filters, operation status, loading/error/empty states, recent projects, trash/restore, and project-list controls. The redesign must preserve these state branches and their ordering/availability.
- The dashboard is not a simple static landing page: it is the Project List and owns meaningful project/import/recovery interactions.

### Settings

Source: `app/src/main/java/com/directorx/editor/ui/settings/SettingsScreen.kt`

- Current Settings is one Settings screen with a vertically arranged collection of sections, not four separate category composables.
- The current section groups observed in source are: Export Defaults; Export Notifications; Timeline; AI Models; Project Storage; Appearance; Editor; Tutorial; Diagnostics; Privacy; Open-source Licenses; Updates; About.
- `SettingsHero` uses **16dp horizontal × 12dp vertical** outer padding.
- Back control minimum touch target is **48dp**; shared icon size default is **18dp**.
- Back-to-title gap is **8dp** (`Spacing.sm`).
- Title/subtitle use Material typography tokens (`headlineLarge`, `bodyMedium`), not hard-coded sp sizes in this composable.
- Title/subtitle gap is **4dp**.
- Divider top spacing is **16dp** (`Spacing.lg`).
- Overview row has a **1dp × 56dp** vertical divider.
- Each overview stat uses **12dp horizontal × 16dp vertical** padding, **24dp** icon, and **12dp** internal horizontal gap.
- Each SettingsSection uses **16dp horizontal × 12dp vertical** padding; section title bottom spacing is **4dp**.
- Settings rows/cards are composed by helper components and individual setting content. Their heights vary with content; do not assume one universal fixed height.

### Shared layout tokens

Source: `app/src/main/java/com/directorx/editor/ui/theme/Tokens.kt`

| Token | Existing value |
|---|---:|
| Spacing.xxs / xs / sm / md | 2 / 4 / 8 / 12dp |
| Spacing.lg / xl / xxl / xxxl | 16 / 20 / 24 / 32dp |
| Radius.xs / sm / md / lg | 4 / 6 / 8 / 10dp |
| Radius.xl / xxl | 12 / 12dp |
| Elevation.flat / card / raised / sheet / toast | 0 / 1 / 3 / 6 / 8dp |
| TouchTarget.minimum / comfortable | 48 / 56dp |
| Motion fast / standard / medium / large | 120 / 200 / 280 / 400ms |

The existing radius system is intentionally capped at 12dp. Do not introduce capsule/pill shapes or larger card radii as part of this upgrade.

## 4. Proposed new information architecture

The four Settings views are proposed as category destinations inside the existing Settings navigation entry. The current 13 groups are reorganized, not removed:

| Proposed Settings view | Existing groups assigned to it |
|---|---|
| **1. Export & Delivery** | Export Defaults; Export Notifications |
| **2. Editing & Timeline** | Timeline; Editor; Tutorial |
| **3. AI & Project Storage** | AI Models; Project Storage |
| **4. App & Preferences** | Appearance; Diagnostics; Privacy; Open-source Licenses; Updates; About |

The current Settings overview becomes the category chooser. The existing back behavior returns from a category to the overview, then to the Dashboard. Preserve the current entry point and ensure deep links/back-stack behavior are explicitly tested before implementation.

Do not create new settings or invent new destinations. The grouping and labels above are navigation organization only.

## 5. Proposed layout blueprint and target measurements

These are **proposed target values**, not claims about current implementation. The final values must be checked against real device width, font scale, and screenshot tests.

### Dashboard target

| Element | Proposed target |
|---|---:|
| Page horizontal inset | 20dp |
| Top app bar content height | 56dp (system insets excluded) |
| Brand/movie icon | Keep existing 24dp |
| Settings hit target | At least 48 × 48dp |
| Main headline | 30sp, line height about 36sp; use typography token if available |
| Headline/body gap | 8dp |
| Primary action height | 56–64dp; full width on narrow phones |
| Quick action grid | 2 columns on phone; only use more columns on genuinely wide layouts |
| Quick action tile height | 72–88dp |
| Section separation | 16–20dp |
| Recent project row | 88–104dp target, content-driven if text wraps |
| Project thumbnail | 88–104dp wide; preserve source aspect ratio/crop behavior |
| Row internal padding | 12dp |
| Bottom system inset | Respect WindowInsets; do not add a new bottom bar unless an existing navigation destination/action supports it |

Dashboard content order:
1. Compact app bar (existing app title and Settings action).
2. Clear page heading and one short supporting line.
3. Dominant New Project action (existing callback).
4. Existing import/template entry point(s), with labels and action mapping taken from source.
5. Recent projects section with existing project actions and any current filter/search/sort affordances retained.
6. Existing trash/restore, loading, error, empty, crash notice, and operation states remain reachable and visually coherent.

No “AI Tools”, “Edit” bottom tab, “More” tab, or new dashboard action is to be added unless the repository already has that exact destination and action.

### Settings overview target

| Element | Proposed target |
|---|---:|
| Page horizontal inset | 20dp |
| App bar content height | 56dp |
| Screen title | 28–30sp |
| Category card height | 72–88dp |
| Category card internal horizontal padding | 16dp |
| Category card gap | 12dp |
| Category icon | Keep existing icon choice and filled style; 24dp target |
| Touch target | At least 48dp |
| Category count | Four groups only, as listed above |

### Settings category page target

| Element | Proposed target |
|---|---:|
| Page horizontal inset | 20dp |
| Category title | 24–28sp |
| Section gap | 16dp |
| Section heading-to-control gap | 8dp |
| Setting row minimum height | 64dp; grow naturally for multiline content |
| Row horizontal padding | 12–16dp |
| Row vertical padding | 10–12dp |
| Existing control hit target | At least 48dp |
| Grouped control spacing | 8–12dp |
| Card corner radius | Use existing Radius tokens; maximum 12dp |

Do not force every row to the same height. Descriptions, badges, permission state, model download state, diagnostics actions, and multiline controls must be allowed to grow.

## 6. Responsive and accessibility rules

- Use adaptive constraints rather than fixed device-pixel dimensions.
- On narrow phones, use one-column category and project layouts; do not squeeze labels into tiny columns.
- On larger widths, cap content width and center the content rather than stretching rows edge-to-edge.
- Keep controls usable at 1.0×, 1.3×, and 1.5× font scale; test at least 2.0× for clipping/scrollability.
- Keep touch targets at or above 48dp.
- Preserve content descriptions, semantics, live regions, test tags, focus order, and keyboard behavior.
- Avoid nested vertical scroll containers that trap gestures. The current hero has its own scroll; implementation must decide whether to replace it with a single screen scroll only after verifying all list states and interactions.
- Support portrait and landscape; verify gesture navigation and three-button navigation insets.

## 7. Implementation sequence (gated)

### Phase 0 — Baseline capture
- Record current branch HEAD and clean working state.
- Capture current Dashboard and Settings screenshots on at least one narrow phone and one larger emulator/device.
- Inventory all click handlers, test tags, state branches, dialogs, and navigation routes in the two source files.
- Record all 13 settings groups and every setting row/control in a mapping sheet.
- No code changes in this phase.

### Phase 1 — Navigation model
- Confirm the four-group mapping above against all existing settings controls.
- Implement category routing using the app's existing navigation approach only.
- Verify entry/back/back-stack behavior, state restoration, and process recreation.
- No setting semantics or persistence changes.

### Phase 2 — Dashboard layout
- Refactor only the Dashboard composable/layout helpers and strictly required resources/tests.
- Retain every existing callback and state branch.
- Use the target dimensions as adaptive constraints.
- Verify project creation, template import, document import, search, sort, filters, project selection, trash restore, empty/error/loading states, crash report export/share, and operation feedback.

### Phase 3 — Settings overview and four category layouts
- Split presentation into overview + four category views while retaining the same underlying settings state/ViewModel and callbacks.
- Move each of the 13 existing sections into exactly one category.
- Verify every existing control still reads/writes the same preference and persists across app restart.
- Verify notification permissions, AI model states/downloads, storage management, diagnostics export/share, privacy actions, licenses, updates, and About links.

### Phase 4 — Visual and accessibility verification
- Compare screenshots against blueprint at matching viewport sizes.
- Test font scale, screen widths, orientation, system insets, talkback semantics, keyboard/focus order, and touch target sizes.
- Add/update Compose UI tests for navigation and critical state branches.
- Verify no existing filled Material icons were replaced or restyled.

### Phase 5 — Regression and release gate
- Run relevant unit tests, Compose/UI tests, lint, and debug build.
- Run device tests on Android 15 and a lower supported API level.
- Verify project open/edit/export paths remain unchanged (smoke tests only; no editor UI redesign).
- Inspect git diff: only Dashboard/Settings UI, their strictly necessary resources/tests, and approved documentation may change.
- Stop and investigate any unrelated file changes.
- Record CI run URL, commit SHA, APK artifact, test results, device/API, and known limitations before marking the UI plan complete.

## 8. Acceptance criteria

- Dashboard is more scannable and New Project is visually obvious.
- All existing dashboard actions and states remain functional.
- Settings overview presents four understandable categories.
- All 13 current settings sections are present exactly once across those categories.
- All settings values, persistence, callbacks, permissions, model operations, and external links behave as before.
- Existing colour identity and filled icon family/choices remain unchanged.
- Layout adapts to narrow/wide screens and large text without clipping or unreachable controls.
- Tests/build pass and device evidence is recorded.
- No unrelated editor or feature behavior is changed.

## 9. Known limitations / items requiring measurement before implementation

- The repository source gives dp constraints, but actual rendered pixel measurements depend on density, viewport, font scale, system insets, and device configuration. Record screenshots and density before pixel-level comparison.
- The dashboard project-row thumbnail and row measurements are implemented in list-item helpers outside `ProjectHomeHero`; they must be measured from those exact helpers and runtime screenshots before finalizing their new values.
- Some Settings row heights depend on content and state; measure representative rows (simple preference, toggle, dropdown, status badge, multiline description, diagnostics action) rather than assigning one universal current height.
- The current app's exact navigation graph must be traced before the four category destinations are implemented.
- The blueprint is a layout direction, not a claim that every sample label/action is already in the app.

## 10. Change control

Any feature outside Dashboard + Settings requires a separate plan and approval. The broader product roadmap is documented in `UPGRADE_ROADMAP.md`; it does not authorize simultaneous implementation.
