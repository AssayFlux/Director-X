package com.directorx.editor.ui.export

import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.directorx.editor.R
import com.directorx.editor.engine.EncoderCapabilityProbe
import com.directorx.editor.engine.ExportStoragePolicy
import com.directorx.editor.model.ExportConfig
import com.directorx.editor.model.ExportQuality
import com.directorx.editor.model.FrameCaptureFormat
import com.directorx.editor.model.VideoCodec
import com.directorx.editor.model.WatermarkPosition
import com.directorx.editor.ui.theme.LocalDirectorXColors

@Composable
internal fun exportChipColors(accent: Color): androidx.compose.material3.SelectableChipColors {
    val semanticColors = LocalDirectorXColors.current
    return FilterChipDefaults.filterChipColors(
        containerColor = semanticColors.panelRaised,
        labelColor = semanticColors.subtext,
        selectedContainerColor = accent.copy(alpha = 0.16f),
        selectedLabelColor = accent
    )
}

@Composable
internal fun localizedExportQuality(quality: ExportQuality): String = stringResource(
    when (quality) {
        ExportQuality.LOW -> R.string.export_quality_small_file
        ExportQuality.MEDIUM -> R.string.export_quality_balanced
        ExportQuality.HIGH -> R.string.export_quality_best
    }
)

@Composable
internal fun localizedFrameCaptureFormat(format: FrameCaptureFormat): String = stringResource(
    when (format) {
        FrameCaptureFormat.PNG -> R.string.export_capture_format_png
        FrameCaptureFormat.JPEG -> R.string.export_capture_format_jpeg_small
    }
)

@Composable
internal fun localizedWatermarkPosition(position: WatermarkPosition): String = stringResource(
    when (position) {
        WatermarkPosition.TOP_LEFT -> R.string.watermark_position_top_left
        WatermarkPosition.TOP_RIGHT -> R.string.watermark_position_top_right
        WatermarkPosition.BOTTOM_LEFT -> R.string.watermark_position_bottom_left
        WatermarkPosition.BOTTOM_RIGHT -> R.string.watermark_position_bottom_right
        WatermarkPosition.CENTER -> R.string.watermark_position_center
    }
)

@Composable
internal fun localizedDeviceTier(tier: EncoderCapabilityProbe.DeviceEncodingTier): String = stringResource(
    when (tier) {
        EncoderCapabilityProbe.DeviceEncodingTier.STANDARD -> R.string.export_device_tier_standard
        EncoderCapabilityProbe.DeviceEncodingTier.ADVANCED -> R.string.export_device_tier_advanced
        EncoderCapabilityProbe.DeviceEncodingTier.PREMIUM -> R.string.export_device_tier_premium
    }
)

internal fun estimateExportBytes(totalDurationMs: Long, config: ExportConfig): Long {
    if (totalDurationMs <= 0L) return 0L
    return ExportStoragePolicy.estimate(
        ExportStoragePolicy.Request(
            durationMs = totalDurationMs,
            videoBitrate = config.videoBitrate,
            audioBitrate = config.audioBitrate,
            mode = ExportStoragePolicy.Mode.VIDEO,
            targetSizeBytes = config.targetSizeBytes,
        )
    ).finalOutputBytes
}

internal fun estimateExportSize(
    totalDurationMs: Long,
    config: ExportConfig
): String? {
    if (totalDurationMs <= 0L) return null

    val estimatedBytes = estimateExportBytes(totalDurationMs, config)
    return when {
        estimatedBytes >= 1_073_741_824L -> "%.1f GB".format(estimatedBytes / 1_073_741_824.0)
        estimatedBytes >= 1_048_576L -> "%.0f MB".format(estimatedBytes / 1_048_576.0)
        else -> "%.0f KB".format(estimatedBytes / 1024.0)
    }
}

/**
 * Heuristic encode-time estimate before export starts. Calibrated against mid-range
 * Android devices — 1080p30 runs at ~1.2x real-time with H.264, HEVC/AV1 are slower.
 * Pixel count and bitrate scale the estimate roughly linearly.
 */
internal fun estimateExportEtaSeconds(totalDurationMs: Long, config: ExportConfig): Long {
    if (totalDurationMs <= 0L) return 0L
    val durationSec = totalDurationMs / 1000.0
    val pixels = config.resolution.width.toLong() * config.resolution.height.toLong()
    val refPixels = 1920L * 1080L
    val resolutionFactor = (pixels.toDouble() / refPixels).coerceAtLeast(0.25)
    val codecFactor = when (config.codec) {
        VideoCodec.H264 -> 1.0
        VideoCodec.HEVC -> 1.6
        VideoCodec.AV1 -> 2.4
        VideoCodec.VP9 -> 1.9
    }
    val fpsFactor = config.frameRate / 30.0
    // Base rate: 1080p30 H.264 ≈ 0.85x realtime on mid devices (so encode takes ~1.17x).
    val encodeMultiplier = 1.17 * resolutionFactor * codecFactor * fpsFactor
    return (durationSec * encodeMultiplier).toLong().coerceAtLeast(1L)
}

internal fun formatEtaSeconds(seconds: Long): String = when {
    seconds >= 3600 -> "%dh %dm".format(seconds / 3600, (seconds % 3600) / 60)
    seconds >= 60 -> "%dm %02ds".format(seconds / 60, seconds % 60)
    else -> "${seconds}s"
}

internal fun formatHistoryBytes(bytes: Long): String = when {
    bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576L -> "%.0f MB".format(bytes / 1_048_576.0)
    bytes >= 1024L -> "%.0f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}
