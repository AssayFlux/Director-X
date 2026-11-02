package com.directorx.editor.engine

import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecProfileLevel
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import com.directorx.editor.engine.AppLog
import com.directorx.editor.model.VideoCodec

/**
 * Device-aware encoder capability probe. Answers the question
 * "will this device *actually* encode at the requested spec?"
 * beyond the coarser "is the codec present at all?" check that
 * `ExportConfig.getAvailableCodecs` already performs.
 *
 * The goal is to surface a warning in the pre-flight report when
 * the user has picked a combination the encoder advertises it can't
 * handle (e.g. 4K HEVC on a phone whose HEVC encoder only goes to
 * 1080p), so the user can dial it back before burning 40 minutes of
 * render time and hitting a Transformer error mid-export.
 *
 * Queries are narrow on purpose: we only look at advertised
 * capabilities, not runtime probes. Real-world encoders sometimes
 * refuse configurations they claim to support, and that's left to
 * Media3 Transformer's own retry logic. This probe is an *early
 * warning*, not a guarantee.
 */
object EncoderCapabilityProbe {

    private const val TAG = "EncoderCapabilityProbe"
    private const val MIME_DOLBY_VISION = "video/dolby-vision"
    private const val FEATURE_HDR_EDITING = "FEATURE_HdrEditing"
    private const val FEATURE_HLG_EDITING = "FEATURE_HlgEditing"

    /**
     * APV (Advanced Professional Video) codec MIME type. Android 16 native;
     * Galaxy S26 Ultra is the first phone with hardware support. APV is an
     * intra-frame codec designed for pro post-production with 4:2:2 10-bit
     * at up to 2 Gbps. ClearCut treats APV as ingest-only — see R6.11.
     */
    const val MIME_APV = "video/apv"

    enum class HdrExportFormat(val displayName: String) {
        HDR10("HDR10"),
        HDR10_PLUS("HDR10+"),
        DOLBY_VISION_PROFILE_10("Dolby Vision Profile 10")
    }

    enum class DeviceEncodingTier(val displayName: String) {
        STANDARD("Standard"),
        ADVANCED("Advanced"),
        PREMIUM("Premium")
    }

    /**
     * HDR editing features explicitly advertised by an encoder. Profile
     * levels describe formats, but these feature flags are the platform's
     * contract that the encoder accepts HDR editing input.
     */
    data class HdrFeatureSupport(
        val hdrEditing: Boolean = false,
        val hlgEditing: Boolean = false,
    ) {
        val canPreserveHdr: Boolean get() = hdrEditing || hlgEditing

        val advertisedFeatureNames: Set<String>
            get() = buildSet {
                if (hdrEditing) add(FEATURE_HDR_EDITING)
                if (hlgEditing) add(FEATURE_HLG_EDITING)
            }
    }

    data class HdrProfileSupport(
        val codec: VideoCodec,
        val supportedFormats: Set<HdrExportFormat>,
        val maxWidth: Int = 0,
        val maxHeight: Int = 0,
        val maxBitrate: Int = 0,
        val encoderNames: Set<String> = emptySet(),
        val featureSupport: HdrFeatureSupport = HdrFeatureSupport(),
    ) {
        val hasAnyHdr: Boolean get() = supportedFormats.isNotEmpty()
        val canPreserveHdr: Boolean get() = featureSupport.canPreserveHdr
        val hasHdr10Plus: Boolean get() = HdrExportFormat.HDR10_PLUS in supportedFormats
        val hasDolbyVisionProfile10: Boolean
            get() = HdrExportFormat.DOLBY_VISION_PROFILE_10 in supportedFormats

        fun featureFailureReason(): String {
            return "HDR export is unavailable for ${codec.label}: the selected encoder does not report " +
                "$FEATURE_HDR_EDITING or $FEATURE_HLG_EDITING. Choose SDR or another codec."
        }
    }

    data class DeviceEncodingTierHint(
        val tier: DeviceEncodingTier,
        val detail: String,
        val availableCodecs: Set<VideoCodec>,
        val hdrFormats: Set<HdrExportFormat>,
        val hasHardwareHevc: Boolean,
        val hasHardwareAv1: Boolean,
        val hasHardwareVp9: Boolean
    )

    enum class EncoderPath {
        HARDWARE,
        SOFTWARE,
        UNKNOWN,
        NONE,
    }

    /**
     * Device capability snapshot for one requested output tuple.
     * A positive result is advisory: the platform may still reject a
     * configuration at runtime, so the finished file must be inspected.
     */
    data class Capability(
        val supported: Boolean,
        val reason: String? = null,
        /** False means the probe could not establish a positive capability claim. */
        val known: Boolean = true,
        val encoderPath: EncoderPath = EncoderPath.UNKNOWN,
        val encoderNames: List<String> = emptyList(),
        val maxWidth: Int = 0,
        val maxHeight: Int = 0,
        val maxBitrate: Int = 0,
        val widthAlignment: Int = 1,
        val heightAlignment: Int = 1,
        /** Android MediaCodec profile/level integer pairs advertised by the encoder. */
        val profileLevels: List<Pair<Int, Int>> = emptyList(),
    ) {
        val hardwareAccelerated: Boolean get() = encoderPath == EncoderPath.HARDWARE
        val softwareOnly: Boolean get() = encoderPath == EncoderPath.SOFTWARE
        val performanceVerified: Boolean get() = encoderPath == EncoderPath.HARDWARE
    }

    /**
     * Check whether any of the device's advertised encoders for
     * `codec` can handle the requested (w, h, fps, bitrate). Walks
     * every matching encoder so a device with multiple (e.g.
     * Qualcomm + Google-software) HEVC encoders is correctly
     * reported as supporting the higher of the two's capabilities.
     */
    fun check(
        codec: VideoCodec,
        width: Int,
        height: Int,
        framerate: Int,
        bitrate: Int
    ): Capability {
        if (width <= 0 || height <= 0 || framerate <= 0 || bitrate <= 0) {
            return Capability(false, "Invalid export dimensions, frame rate, or bitrate.", encoderPath = EncoderPath.NONE)
        }
        val mimeType = codec.mimeType
        val codecInfos = try {
            MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                .filter { it.isEncoder }
                .filter { it.supportedTypes.any { type -> type.equals(mimeType, ignoreCase = true) } }
                .sortedBy { info ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info.isHardwareAccelerated) 0 else 1
                }
        } catch (e: Exception) {
            AppLog.w(TAG, "MediaCodecList lookup failed for ${codec.label}", e)
            return Capability(
                supported = false,
                reason = "Encoder capability could not be queried; export support is unverified.",
                known = false,
                encoderPath = EncoderPath.UNKNOWN,
            )
        }
        if (codecInfos.isEmpty()) {
            return Capability(
                supported = false,
                reason = "No ${codec.label} encoder is advertised on this device.",
                encoderPath = EncoderPath.NONE,
            )
        }

        var firstReason: String? = null
        var bestRejected: Capability? = null
        for (info in codecInfos) {
            val caps = try {
                info.getCapabilitiesForType(mimeType)
            } catch (t: Throwable) {
                AppLog.w(TAG, "Could not inspect ${info.name} for $mimeType", t)
                continue
            }
            val videoCaps = caps.videoCapabilities ?: continue
            val path = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info.isHardwareAccelerated -> EncoderPath.HARDWARE
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info.isSoftwareOnly -> EncoderPath.SOFTWARE
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> EncoderPath.UNKNOWN
                else -> EncoderPath.UNKNOWN
            }
            val base = Capability(
                supported = false,
                encoderPath = path,
                encoderNames = listOf(info.name),
                maxWidth = videoCaps.supportedWidths.upper,
                maxHeight = videoCaps.supportedHeights.upper,
                maxBitrate = videoCaps.bitrateRange.upper,
                widthAlignment = videoCaps.widthAlignment.coerceAtLeast(1),
                heightAlignment = videoCaps.heightAlignment.coerceAtLeast(1),
                profileLevels = caps.profileLevels.orEmpty().map { it.profile to it.level },
            )
            val failure = when {
                width % base.widthAlignment != 0 || height % base.heightAlignment != 0 ->
                    "Requested dimensions must align to ${base.widthAlignment}×${base.heightAlignment} for ${codec.label}."
                !videoCaps.isSizeSupported(width, height) ->
                    "${codec.label} encoder ${info.name} does not support ${width}×${height}; advertised width/height bounds are ${base.maxWidth}×${base.maxHeight}."
                !videoCaps.areSizeAndRateSupported(width, height, framerate.toDouble()) ->
                    "${codec.label} at ${width}×${height} does not support ${framerate} fps on encoder ${info.name}."
                bitrate !in videoCaps.bitrateRange.lower..videoCaps.bitrateRange.upper ->
                    "${codec.label} bitrate must be within ${videoCaps.bitrateRange.lower / 1_000_000}–${videoCaps.bitrateRange.upper / 1_000_000} Mbps for encoder ${info.name}."
                else -> null
            }
            if (failure != null) {
                if (firstReason == null) firstReason = failure
                bestRejected = base.copy(reason = failure)
                continue
            }

            val pathNotice = when (path) {
                EncoderPath.SOFTWARE ->
                    "This output is supported only by a software encoder; export may be substantially slower and use more battery."
                EncoderPath.UNKNOWN ->
                    "The encoder reports support, but Android did not identify it as hardware-accelerated or software-only. Treat performance as unverified."
                else -> null
            }
            return base.copy(
                supported = true,
                reason = pathNotice,
                encoderPath = path,
                encoderNames = listOf(info.name),
            )
        }
        return (bestRejected ?: Capability(
            supported = false,
            reason = firstReason ?: "${codec.label} has no encoder with a verifiable video capability profile.",
            encoderPath = EncoderPath.NONE,
        )).copy(reason = firstReason ?: bestRejected?.reason)
    }

    /**
     * Returns the HDR profiles the device advertises for the selected export codec.
     *
     * Dolby Vision Profile 10 is AV1-based on Android (`dav1.10`), so ClearCut
     * reports it with AV1 rather than HEVC. This is still an advisory: Media3
     * and the platform encoder perform the authoritative negotiation at export
     * time.
     */
    fun queryHdrProfiles(codec: VideoCodec): HdrProfileSupport {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return HdrProfileSupport(codec = codec, supportedFormats = emptySet())
        }

        val mimeTypes = buildSet {
            add(codec.mimeType)
            if (codec == VideoCodec.AV1) add(MIME_DOLBY_VISION)
        }
        val formats = mutableSetOf<HdrExportFormat>()
        val encoders = mutableSetOf<String>()
        var maxWidth = 0
        var maxHeight = 0
        var maxBitrate = 0
        var featureSupport = HdrFeatureSupport()

        for ((info, mimeType) in matchingEncoderEntries(mimeTypes)) {
            val caps = try {
                info.getCapabilitiesForType(mimeType)
            } catch (_: IllegalArgumentException) {
                continue
            } catch (t: Throwable) {
                AppLog.w(TAG, "Capability lookup failed for ${info.name} / $mimeType", t)
                continue
            }

            val entryFeatureSupport = readHdrFeatureSupport(caps)
            featureSupport = featureSupport.merge(entryFeatureSupport)
            val discovered = caps.profileLevels
                ?.mapNotNull { classifyHdrProfile(mimeType, it) }
                ?.toSet()
                .orEmpty()

            if (discovered.isNotEmpty() || entryFeatureSupport.canPreserveHdr) {
                formats += discovered
                encoders += info.name
                caps.videoCapabilities?.let { videoCaps ->
                    maxWidth = maxOf(maxWidth, videoCaps.supportedWidths.upper)
                    maxHeight = maxOf(maxHeight, videoCaps.supportedHeights.upper)
                    maxBitrate = maxOf(maxBitrate, videoCaps.bitrateRange.upper)
                }
            }
        }

        return HdrProfileSupport(
            codec = codec,
            supportedFormats = formats,
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            maxBitrate = maxBitrate,
            encoderNames = encoders,
            featureSupport = featureSupport,
        )
    }

    /** A PII-free line for the local diagnostic bundle. */
    internal fun diagnosticHdrFeatureLine(info: MediaCodecInfo, mimeType: String): String {
        val support = try {
            info.getCapabilitiesForType(mimeType)?.let(::readHdrFeatureSupport)
        } catch (t: Throwable) {
            AppLog.w(TAG, "HDR feature lookup failed for ${info.name} / $mimeType", t)
            null
        }
        return buildString {
            append("encoder_features\t${info.name}\t$mimeType")
            append("\t$FEATURE_HDR_EDITING=${support?.hdrEditing ?: false}")
            append("\t$FEATURE_HLG_EDITING=${support?.hlgEditing ?: false}")
        }
    }

    /**
     * Coarse user-facing device tier. It is intentionally derived from actual
     * advertised encoders instead of model names so Tensor, Snapdragon, Exynos,
     * and future devices all get the same treatment.
     */
    fun deviceTierHint(): DeviceEncodingTierHint {
        val availableCodecs = VideoCodec.entries
            .filter { matchingEncoderEntries(setOf(it.mimeType)).isNotEmpty() }
            .toSet()
        val hdrFormats = VideoCodec.entries
            .flatMap { queryHdrProfiles(it).supportedFormats }
            .toSet()
        val hasHardwareHevc = hasHardwareEncoder(VideoCodec.HEVC.mimeType)
        val hasHardwareAv1 = hasHardwareEncoder(VideoCodec.AV1.mimeType)
        val hasHardwareVp9 = hasHardwareEncoder(VideoCodec.VP9.mimeType)

        val tier = when {
            hasHardwareAv1 && hasHardwareVp9 -> DeviceEncodingTier.PREMIUM
            hasHardwareHevc || hasHardwareAv1 || hasHardwareVp9 || hdrFormats.isNotEmpty() ->
                DeviceEncodingTier.ADVANCED
            else -> DeviceEncodingTier.STANDARD
        }
        val detail = when (tier) {
            DeviceEncodingTier.PREMIUM -> {
                val hdr = formatHdrList(hdrFormats)
                if (hdr.isNotBlank()) {
                    "Hardware AV1 and VP9 encoders detected with $hdr HDR profile support."
                } else {
                    "Hardware AV1 and VP9 encoders detected for efficient modern exports."
                }
            }
            DeviceEncodingTier.ADVANCED -> {
                val codecs = availableCodecs
                    .filter { it != VideoCodec.H264 }
                    .joinToString(", ") { it.label }
                    .ifBlank { "modern codec" }
                val hdr = formatHdrList(hdrFormats)
                if (hdr.isNotBlank()) {
                    "$codecs encode is available with $hdr HDR profile support."
                } else {
                    "$codecs encode is available. HDR support depends on the selected codec and source."
                }
            }
            DeviceEncodingTier.STANDARD ->
                "Baseline H.264 export path detected. Choose conservative settings for long renders."
        }

        return DeviceEncodingTierHint(
            tier = tier,
            detail = detail,
            availableCodecs = availableCodecs,
            hdrFormats = hdrFormats,
            hasHardwareHevc = hasHardwareHevc,
            hasHardwareAv1 = hasHardwareAv1,
            hasHardwareVp9 = hasHardwareVp9
        )
    }

    // --- R6.11 APV ingest probe ---

    data class ApvSupport(
        /** True iff the device advertises at least one APV decoder. */
        val hasDecoder: Boolean,
        /** True iff the decoder is hardware-accelerated (not software fallback). */
        val isHardwareDecoder: Boolean,
        /** Decoder codec names for the diagnostic bundle. */
        val decoderNames: List<String>,
    ) {
        val isUsable: Boolean get() = hasDecoder
    }

    /**
     * Probe APV (Advanced Professional Video) decoder availability.
     *
     * ClearCut treats APV as **ingest-only** (R6.11c): APV is intra-frame
     * coding designed for pro post-production; encoded files are 10–50×
     * larger than HEVC equivalents. We surface a "Source is APV — large
     * file" chip on import when this returns `hasDecoder = true`, and we
     * never encode to APV by default. Encoder probing is omitted intentionally.
     */
    fun probeApvIngest(): ApvSupport {
        val entries = matchingDecoderEntries(setOf(MIME_APV))
        if (entries.isEmpty()) {
            return ApvSupport(hasDecoder = false, isHardwareDecoder = false, decoderNames = emptyList())
        }
        val isHardware = entries.any { (info, _) -> info.isHardwareAcceleratedCompat() }
        val names = entries.map { (info, _) -> info.name }.distinct()
        return ApvSupport(hasDecoder = true, isHardwareDecoder = isHardware, decoderNames = names)
    }

    private fun matchingDecoderEntries(mimeTypes: Set<String>): List<Pair<MediaCodecInfo, String>> {
        return try {
            MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                .filter { !it.isEncoder }
                .flatMap { info ->
                    info.supportedTypes
                        .filter { supported ->
                            mimeTypes.any { wanted -> supported.equals(wanted, ignoreCase = true) }
                        }
                        .map { supported -> info to supported }
                }
        } catch (t: Throwable) {
            AppLog.w(TAG, "MediaCodecList decoder lookup failed", t)
            emptyList()
        }
    }

    private fun matchingEncoderEntries(mimeTypes: Set<String>): List<Pair<MediaCodecInfo, String>> {
        return try {
            MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                .filter { it.isEncoder }
                .flatMap { info ->
                    info.supportedTypes
                        .filter { supported ->
                            mimeTypes.any { wanted -> supported.equals(wanted, ignoreCase = true) }
                        }
                        .map { supported -> info to supported }
                }
        } catch (t: Throwable) {
            AppLog.w(TAG, "MediaCodecList lookup failed", t)
            emptyList()
        }
    }

    private fun hasHardwareEncoder(mimeType: String): Boolean {
        return matchingEncoderEntries(setOf(mimeType)).any { (info, _) -> info.isHardwareAcceleratedCompat() }
    }

    private fun readHdrFeatureSupport(caps: MediaCodecInfo.CodecCapabilities): HdrFeatureSupport {
        val hdrEditing = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isFeatureSupportedSafely(caps, MediaCodecInfo.CodecCapabilities.FEATURE_HdrEditing)
        } else {
            false
        }
        val hlgEditing = if (Build.VERSION.SDK_INT >= 35) {
            isFeatureSupportedSafely(caps, MediaCodecInfo.CodecCapabilities.FEATURE_HlgEditing)
        } else {
            false
        }
        return HdrFeatureSupport(hdrEditing = hdrEditing, hlgEditing = hlgEditing)
    }

    private fun isFeatureSupportedSafely(
        caps: MediaCodecInfo.CodecCapabilities,
        feature: String,
    ): Boolean {
        return try {
            caps.isFeatureSupported(feature)
        } catch (t: Throwable) {
            AppLog.w(TAG, "Codec feature lookup failed for $feature", t)
            false
        }
    }

    private fun HdrFeatureSupport.merge(other: HdrFeatureSupport): HdrFeatureSupport {
        return HdrFeatureSupport(
            hdrEditing = hdrEditing || other.hdrEditing,
            hlgEditing = hlgEditing || other.hlgEditing,
        )
    }

    private fun MediaCodecInfo.isHardwareAcceleratedCompat(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isHardwareAccelerated
        } else {
            val lower = name.lowercase()
            !lower.startsWith("omx.google.") &&
                !lower.startsWith("c2.android.") &&
                !lower.contains(".sw.") &&
                !lower.contains("software")
        }
    }

    private fun classifyHdrProfile(
        mimeType: String,
        profileLevel: CodecProfileLevel
    ): HdrExportFormat? {
        return when {
            mimeType.equals(MediaFormat.MIMETYPE_VIDEO_HEVC, ignoreCase = true) -> when (profileLevel.profile) {
                CodecProfileLevel.HEVCProfileMain10HDR10 -> HdrExportFormat.HDR10
                CodecProfileLevel.HEVCProfileMain10HDR10Plus -> HdrExportFormat.HDR10_PLUS
                else -> null
            }
            mimeType.equals(MediaFormat.MIMETYPE_VIDEO_AV1, ignoreCase = true) -> when (profileLevel.profile) {
                CodecProfileLevel.AV1ProfileMain10HDR10 -> HdrExportFormat.HDR10
                CodecProfileLevel.AV1ProfileMain10HDR10Plus -> HdrExportFormat.HDR10_PLUS
                else -> null
            }
            mimeType.equals(MediaFormat.MIMETYPE_VIDEO_VP9, ignoreCase = true) -> when (profileLevel.profile) {
                CodecProfileLevel.VP9Profile2HDR,
                CodecProfileLevel.VP9Profile3HDR -> HdrExportFormat.HDR10
                CodecProfileLevel.VP9Profile2HDR10Plus,
                CodecProfileLevel.VP9Profile3HDR10Plus -> HdrExportFormat.HDR10_PLUS
                else -> null
            }
            mimeType.equals(MIME_DOLBY_VISION, ignoreCase = true) -> when (profileLevel.profile) {
                CodecProfileLevel.DolbyVisionProfileDvav110 -> HdrExportFormat.DOLBY_VISION_PROFILE_10
                else -> null
            }
            else -> null
        }
    }

    private fun formatHdrList(formats: Set<HdrExportFormat>): String {
        return formats
            .map { it.displayName }
            .sorted()
            .joinToString(", ")
    }
}
