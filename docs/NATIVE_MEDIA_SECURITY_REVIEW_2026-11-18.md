# Director X — AssayFlux Native Media Security Review

**Product owner:** AssayFlux  
**Product:** Director X  
**Review date:** 18 November 2026  
**Status:** OPEN — release gate remains expired  
**Reviewed artifact:** `third_party/ffmpeg-kit-next/ffmpeg-kit-next-8.1.0.aar`  
**Pinned FFmpeg:** 8.1.2, source revision `38b88335f99e76ed89ff3c93f877fdefce736c13`  
**Native lock reviewUntil:** 2026-10-31 (expired)

## Executive decision

Do not extend the native review date and do not mark the native dependency release gate as passed. Public advisories published after the last recorded review identify multiple vulnerabilities affecting FFmpeg versions through 8.1.2. The repository's existing custom configure patch disables some codecs/encoders, but it does not explicitly document or prove mitigation for every affected component below. The AAR's exact enabled decoder/filter/encoder set has not been verified from a reproducible build transcript or device-side capability inventory.

The new Director X editing-proxy fallback is restricted to source tracks reported by Android as `video/avc` or `video/hevc`; it uses FFmpeg's default software decode path and does not request Vulkan hardware acceleration. This narrows the feature's input surface but is not a substitute for auditing the shipped AAR.

## Current native build record

The native lock records:
- FFmpegKitNext 8.1.0 / FFmpeg 8.1.2
- Android NDK r27d
- Android API 24
- ABIs: armeabi-v7a, armeabi-v7a-neon, arm64-v8a, x86 and x86_64
- Build options include Android MediaCodec, Android zlib, OpenH264, libass and libjxl
- Custom patch: `third_party/ffmpeg-kit-next/clearcut-security.patch`

The patch currently disables VobSub demuxing, TDSC/MACE6/ADX/RASC/IAMF components, PNG/APNG encoders and the standalone ZLIB decoder. It does not explicitly disable the LCL decoder, Vulkan HEVC decoder or quirc filter. Do not infer those components are absent without inspecting the exact generated FFmpeg configuration.

## Advisory review

| Advisory | Affected component / versions | Current assessment |
|---|---|---|
| CVE-2026-64831 | Vulkan HEVC decoder; FFmpeg 8.0 through 8.1.2 | Not proven mitigated. The new proxy path does not request Vulkan acceleration, but the shipped build's decoder inventory is unknown. |
| CVE-2026-66038 | LCL/ZLIB video decoder; FFmpeg through 8.1.2 | Not proven mitigated. Disabling the standalone `zlib` decoder is not proof that the LCL decoder is disabled. |
| CVE-2026-66040 | PNG/APNG encoders; FFmpeg through 8.1.2 | Existing patch disables PNG and APNG encoders; verify the built binary's encoder list before recording as mitigated. |
| CVE-2026-66041 | `vf_quirc` filter; FFmpeg 7.0 through 8.1.2 | Not proven mitigated. The current patch does not explicitly disable this filter; verify whether libquirc/filter support was built. |
| CVE-2026-8461 | FFmpeg 8.1.2 | Listed by upstream as fixed in 8.1.2. |
| CVE-2026-30999 | FFmpeg 8.1.2 | Listed by upstream as fixed in 8.1.2. |

Sources:
- [Official FFmpeg security page](https://ffmpeg.org/security.html) — FFmpeg 8.1.2 and 9.0 fix lists
- [NVD CVE-2026-64831](https://nvd.nist.gov/vuln/detail/CVE-2026-64831)
- [GitHub Advisory CVE-2026-66038](https://github.com/advisories/GHSA-8mx4-8xfc-qr43)
- [GitHub Advisory CVE-2026-66041](https://github.com/advisories/GHSA-frhp-m687-9g3q)
- [ALT Linux advisory: FFmpeg through 8.1.2 PNG/APNG encoder issue](https://errata.altlinux.org/ALT-PU-2026-20098)

## Required closure work

1. Obtain the exact FFmpeg configure transcript and generated `config.h` for the AAR's source commit and build flags.
2. Run the shipped AAR's `-decoders`, `-encoders` and `-filters` inventories on every supported ABI; retain the output as review evidence.
3. Explicitly disable or backport the upstream fixes for affected components not required by Director X. At minimum assess `lcl`, `hevc_vulkan`, `quirc`, PNG and APNG components.
4. Rebuild the AAR from the pinned source with the revised security patch; record new AAR SHA-256, patch SHA-256, configure transcript, source commit, NDK and ABI coverage in `native-lock.json`.
5. Run native smoke tests for the media operations Director X actually uses: H.264/HEVC import/proxy, AAC audio processing, subtitle burn-in, image-sequence encoding, stream copy, cancellation and error recovery.
6. Re-run `scripts/verify_native_supply_chain.py` and the full Android unit/lint/instrumentation suite.
7. Only after those checks pass, record a new review date and expiry supported by the evidence.

## Release decision

**BLOCKED.** Keep `reviewUntil=2026-10-31` expired until the native binary is rebuilt or its exact component inventory and patch coverage are independently verified. Do not edit the date alone.
