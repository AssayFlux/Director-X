# Director X — Current UI Measurement Ledger

**Purpose:** Separate source-observed current values from proposed redesign targets.  
**Reviewed source:** `main` at `1a3b7b8fe6a744780bbe202c07087e0eac5add2f` on 2026-11-03.  
**Important:** These are Compose dp/sp declarations, not physical screen-pixel measurements. Device density, font scale, viewport, and system insets affect rendered pixels.

## A. Dashboard — verified current source values

Source: `app/src/main/java/com/directorx/editor/ui/projects/ProjectListScreen.kt`

| Element | Current source value / behavior | Evidence |
|---|---|---|
| Root layout | Full-size background + vertical Column | `ProjectListScreen` |
| Hero maximum height | 50% of `screenHeightDp`, coerced to at least 280dp | `ProjectHomeHero` |
| Hero scroll | Its own verticalScroll container | `ProjectHomeHero` |
| Hero outer padding | 20dp horizontal, 16dp vertical | literal horizontal padding + `Spacing.lg` vertical |
| Hero child spacing | 12dp | `Arrangement.spacedBy(Spacing.md)` |
| Brand/movie icon | 24dp | `Modifier.size(24.dp)` |
| Brand-to-title gap | 8dp | `Spacing.sm` |
| Settings control min hit target | 48dp | Shared `ClearCutChromeIconButton` |
| Settings control default icon | 18dp | Shared `ClearCutChromeIconButton` |
| Headline style | Material `headlineLarge` | Typography token |
| Supporting copy style | Material `bodyLarge` | Typography token |
| Search icon | 20dp | Conditional search field |
| Search clear control | 40dp surface, inside 48dp minimum hit target | Search trailing action |
| Sort chip icon | 16dp | Shared filter chip |
| Search/sort visibility | Conditional on collection state | Keep conditions unchanged |

The hero's child order is: app header; divider; welcome heading and body; primary create-project + secondary import action row; optional search; optional sort chips; divider.

**Not yet source-measured in this pass:** exact project row/card height, thumbnail dimensions, project row padding, list section spacing, trash card geometry, and all empty/loading/error-state dimensions. These are outside the hero helper and must be measured in their own composables plus runtime screenshots before implementation.

## B. Settings — verified current source values

Source: `app/src/main/java/com/directorx/editor/ui/settings/SettingsScreen.kt`

| Element | Current source value / behavior |
|---|---|
| Settings structure | One Settings entry with a category overview and four in-screen category views (Export & Delivery; Editing & Timeline; AI & Project Storage; App & Preferences) |
| SettingsHero outer padding | 16dp horizontal × 12dp vertical |
| Back control | Shared minimum touch target 48dp; default icon 18dp |
| Back-to-title gap | 8dp |
| Title style | Material `headlineLarge` |
| Subtitle style | Material `bodyMedium` |
| Title/subtitle gap | 4dp |
| Divider top gap | 16dp |
| Overview stats divider | 1dp wide × 56dp high |
| Overview stat horizontal padding | 12dp |
| Overview stat vertical padding | 16dp |
| Overview stat icon | 24dp |
| Overview stat internal gap | 12dp |
| SettingsSection outer padding | 20dp horizontal × 10dp vertical |
| Section description | Optional description rendered beneath the title with 3dp top and 4dp bottom spacing; the content is inside a panel with 12dp padding |

Current 13 section groups in source:
1. Export Defaults
2. Export Notifications
3. Timeline
4. AI Models
5. Project Storage
6. Appearance
7. Editor
8. Tutorial
9. Diagnostics
10. Privacy
11. Open-source Licenses
12. Updates
13. About

The current category overview groups these as 2 export sections, 3 editing/timeline sections, 2 AI/storage sections, and 6 app/preferences sections.

**Not yet source-measured in this pass:** every individual SettingsTile/SettingsRow height, icon-container dimensions, control-specific padding, and expanded/permission/download states. These vary by component and state; do not describe one generic value as the current row height.

The category overview uses 20dp horizontal padding, 12dp vertical padding, 12dp inter-card spacing, and category cards with a 76dp minimum height.

## C. Shared design tokens — verified current values

Source: `app/src/main/java/com/directorx/editor/ui/theme/Tokens.kt`

### Spacing
| Token | Value |
|---|---:|
| xxs | 2dp |
| xs | 4dp |
| sm | 8dp |
| md | 12dp |
| lg | 16dp |
| xl | 20dp |
| xxl | 24dp |
| xxxl | 32dp |

### Radius
| Token | Value |
|---|---:|
| xs | 4dp |
| sm | 6dp |
| md | 8dp |
| lg | 10dp |
| xl | 12dp |
| xxl | 12dp |

The current token comments explicitly cap the radius system at 12dp and disallow capsule-like shapes.

### Elevation
| Token | Value |
|---|---:|
| flat | 0dp |
| card | 1dp |
| raised | 3dp |
| sheet | 6dp |
| toast | 8dp |

### Touch targets
| Token | Value |
|---|---:|
| minimum | 48dp |
| comfortable | 56dp |

### Motion
| Token | Duration |
|---|---:|
| fast | 120ms |
| standard | 200ms |
| medium | 280ms |
| large | 400ms |

## D. Existing project/export model facts relevant to future roadmap (not UI measurements)

Source: `app/src/main/java/com/directorx/editor/model/Project.kt` and `ExportConfig.kt`

- Project aspect ratios currently include 16:9, 9:16, 1:1, 4:3, 3:4, 4:5, and 21:9. No user-entered custom ratio is defined in this enum.
- Export resolution enum currently tops out at 4K (3840×2160); entries also include 480p, 720p, 1080p, and 1440p.
- ExportConfig defaults to 1080p, 30fps, H.264, High quality, AAC audio, and 16:9.
- ExportConfig model includes codec, frame rate, bitrate override, target file size, and color policy fields. Actual runtime encoder support is a separate question and must be capability-probed.

## E. How to complete the measurement audit

Before changing UI code:
1. Capture baseline screenshots on a known device/emulator; record physical resolution, density, Android version, font scale, orientation, and navigation mode.
2. Use Layout Inspector / Compose semantics and source helpers to measure project cards and each representative settings row.
3. Record both dp layout dimensions and screenshot pixel bounds; do not mix them.
4. Measure normal, empty, loading, error, expanded, permission-denied, and model-download states.
5. Add screenshot tests for the five in-scope surfaces and compare at identical viewport/density/font-scale settings.
