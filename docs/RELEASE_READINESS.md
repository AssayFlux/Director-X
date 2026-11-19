# Stage 5 — Release readiness

## Purpose

Stage 5 is the final release-candidate gate. It is the first stage allowed to assemble APKs. APK packaging must happen only after source, test, native supply-chain, and package-policy checks pass.

## Automated gates added

The `.github/workflows/release-candidate.yml` workflow:
1. Verifies the pinned FFmpegKitNext AAR, provenance, security patch, advisory inventory, review horizon, and reachability decisions.
2. Generates CycloneDX and SPDX SBOMs.
3. Validates package identity, version consistency, public claims, and APK/alignment validator self-tests.
4. Runs QA unit tests and Android lint.
5. Only after those gates pass, assembles debug APKs (universal plus ABI splits).
6. Validates APK metadata and native ABI contents, checks 16 KB ELF LOAD alignment, and uploads APK/SBOM artifacts.

The workflow deliberately does not sign a release with a repository-stored or fallback key. A production release must use the established private signing identity in the maintainer's secure environment.

## Blocking issue: FFmpeg native security review expired

The checked-in native lock records both source and security review horizons as `2026-10-31`. The current date is after that horizon. The supply-chain validator correctly rejects an expired review.

This must not be "fixed" by merely extending the date. Before a release candidate can pass:
- Re-review the exact packaged AAR SHA-256 and its source commit/build recipe.
- Reconcile the complete advisory inventory against current upstream advisories.
- Validate disabled-component and reachability claims against the actual packaged native binaries and build configuration.
- Record reviewer, evidence, and a new review horizon in the native lock only after that work is complete.
- Run `python scripts/verify_native_supply_chain.py` and retain generated SBOMs.

The existing lock identifies AAR SHA-256 `7971240aff84ce59a4ab28400bb4af59d24c20ce68c25525d41d910246ccff62`, size 62,247,665 bytes, and FFmpeg source revision `38b88335f99e76ed89ff3c93f877fdefce736c13`. These are provenance references, not proof of a current security clearance.

## Advisory drift requiring the renewed review

A fresh upstream advisory check on 2026-11-19 found that the prior review scope is no longer sufficient. FFmpeg's current security page lists additional fixes in 9.0 that are not listed in the 8.1.2 release section, including CVE-2026-12706, CVE-2026-66038, CVE-2026-70629, and CVE-2026-70631. Debian's tracker also records multiple vulnerabilities affecting FFmpeg through 8.1.2, including CVE-2026-66038, CVE-2026-66039, CVE-2026-66040, and the later 2026-70628 through 2026-70632 family.

The reviewer must reconcile the exact shipped AAR against, at minimum, the newer advisory families below and any subsequent upstream updates:

- CVE-2026-65704, CVE-2026-65705, CVE-2026-65706
- CVE-2026-66036 and CVE-2026-66041
- CVE-2026-70628 through CVE-2026-70632
- CVE-2026-75141 through CVE-2026-75146
- Any newer advisories published after the AAR's 2026-08-02 build date

These are review leads, not claims that every affected component is reachable in the Android package. Do not mark them `not_affected` from configure flags alone. Confirm the exact binary's compiled component surface and the actual application input paths, then record a reasoned decision and evidence per advisory. Keep the release gate closed until that work is completed by a qualified reviewer.

Sources:
- FFmpeg upstream security advisories: https://ffmpeg.org/security.html
- Debian FFmpeg security tracker: https://security-tracker.debian.org/tracker/source-package/ffmpeg
- Debian advisory detail for current FFmpeg findings: https://tracker.debian.org/action-items/2390585

## Remaining release acceptance

- Resolve all QA unit-test failures and managed-device/instrumentation failures.
- Complete physical-device acceptance for playback, proxy generation, export, navigation, and large projects.
- Run startup/editor/export Macrobenchmarks on recorded hardware and retain results.
- Verify the release signing certificate matches the established published identity.
- Build and inspect the final release-signed APK set after all gates pass.
- Install and smoke-test the exact release APKs on supported physical devices.

## APK build policy

The automated workflow builds debug-signed APKs for validation and artifact inspection only. The production release-signed APK must be built by the maintainer with the established private key. No keystore or password belongs in the repository.

## Current status

Stage 5 automation is prepared. Release acceptance is **blocked** until the native security review is renewed with evidence and the automated/device test gates pass. No release-ready claim is made by this document.
