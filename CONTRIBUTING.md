# Contributing to Director X

Thank you for taking an interest in Director X.

## Project identity and provenance

Director X is a derivative Android video editor based on the ClearCut project associated with [SysAdminDoc](https://github.com/SysAdminDoc). AssayFlux maintains the Director X product identity and subsequent project work. Preserve upstream copyright statements, the upstream MIT notice and all third-party licence/source obligations.

Read [ACKNOWLEDGMENTS.md](ACKNOWLEDGMENTS.md), [NOTICE](NOTICE), and [the branding and compatibility policy](docs/BRANDING_AND_COMPATIBILITY.md) before changing inherited code, archive formats, package identifiers or bundled native components.

## Before opening a pull request

- Search existing issues and pull requests.
- Explain the problem and expected behavior.
- Keep changes focused and avoid unrelated formatting or dependency upgrades.
- Preserve project/archive/database compatibility unless a migration is explicitly designed.
- Update documentation and the changelog when behavior, configuration or release procedures change.
- Include reproducible steps and relevant logs for bug fixes.
- Do not add unverified screenshots, fake test results, generated APKs, signing secrets or private credentials.

## Android verification policy

GitHub Actions has three distinct lanes: Android unit tests (`:app:testQaUnitTest`) on relevant pushes and pull requests; pull-request device/performance QA (`:app:lintQa`, API 37 managed-device instrumentation and macrobenchmark); and repository-hygiene validation. CI does not publish a user-installable APK or replace physical-device acceptance testing.

The maintainer builds installable APKs manually in Termux or another configured Android environment and performs physical-device testing. Contributors should state exactly which commands they ran and whether they completed successfully. Do not describe a change as tested if only source inspection was performed.

For a maintainer build, follow [Build Director X in Termux](docs/TERMUX_BUILD.md). Useful optional local commands include:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testQaUnitTest
./gradlew :app:lintQa
```

These commands are examples for a correctly configured Android toolchain; contributors are not expected to claim they ran them when the environment is unavailable.

## Dependency and native-code changes

For new dependencies or native binaries:

- Record exact coordinates, versions, source repositories and immutable commit IDs.
- Review licence identifiers and required notice/source materials.
- Update the version catalog, native lock and relevant documentation consistently.
- Preserve checksums and reproducible build metadata.
- Explain ABI, 16 KB page-size, security and size implications where applicable.

## Pull-request checklist

- [ ] The change is scoped and explained.
- [ ] Compatibility and upstream provenance have been considered.
- [ ] Documentation and changelog are updated where needed.
- [ ] Licence and notice obligations are preserved.
- [ ] Verification commands and actual results are recorded.
- [ ] No secrets, private media or generated build artifacts are included.
