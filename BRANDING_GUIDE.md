# Director X Branding Studio

This is a **repository-side** configuration tool. It does not add a Branding Studio screen to the Android app.

## Change your assets

1. Put the replacement files in `Assets/`.
2. Edit `branding/branding.json`.
3. Set the exact filename (including extension) for the launcher, splash sequence, signature, and optional UI texture.
4. Run:

```bash
python3 scripts/validate_branding.py
```

The validator checks the configuration's required fields, allowed values, and that every selected file exists in `Assets/`. Keep asset names as filenames; do not put paths or URLs in the configuration.

## Configuration map

- `launcher.asset`: launcher icon source.
- `splash.sequence`: ordered splash assets and their display durations in milliseconds.
- `splash.transitionDurationMs`: retained transition setting for future splash artwork changes.
- `splash.background`: splash background color.
- `about.authorName`, `about.signatureAsset`, `about.assayFluxLine`: About page author identity.
- `placements.dashboard/settings/editor`: intended logo and texture placement choices.

The launcher icon is generated from `Assets/App Icon.png`. The launch screen displays only the Director X SVG logo; the AssayFlux GIF is not shown. Keep the configured splash sequence consistent with the app implementation.\n\nThe placement values are configuration selections. A UI placement is only active once its matching Android screen integration is implemented; do not assume changing this JSON alone moves UI elements.

## Build

After validation, build using the repository's normal Gradle workflow:

```bash
./gradlew :app:assembleDebug
```

Version remains `v0.1.0` (version code `1`) unless explicitly changed by the project owner.
