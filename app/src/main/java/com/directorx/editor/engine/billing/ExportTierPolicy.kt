package com.directorx.editor.engine.billing

import com.directorx.editor.model.ExportConfig
import com.directorx.editor.model.ExportQuality
import com.directorx.editor.model.Resolution
import com.directorx.editor.model.ProjectColorPolicy
import com.directorx.editor.model.VideoCodec

/**
 * Deterministic export limits shared by UI and execution paths.
 *
 * Limits are maximums, not device guarantees. Codec availability, encoder
 * capabilities, memory and thermal preflight remain authoritative.
 */
object ExportTierPolicy {
    fun enforce(config: ExportConfig, tier: SubscriptionTier): ExportConfig {
        val maxDimension = when (tier) {
            SubscriptionTier.FREE -> 1280
            SubscriptionTier.PLUS -> 1920
            SubscriptionTier.PRO -> 7680
        }
        val maxFrameRate = when (tier) {
            SubscriptionTier.FREE -> 30
            SubscriptionTier.PLUS, SubscriptionTier.PRO -> 60
        }
        val maxResolution = Resolution.entries
            .filter { maxOf(it.width, it.height) <= maxDimension }
            .maxByOrNull { it.width.toLong() * it.height.toLong() }
            ?: Resolution.HD_720P

        val cappedResolution = if (
            maxOf(config.resolution.width, config.resolution.height) > maxDimension
        ) maxResolution else config.resolution

        val customDimensions = if (config.customOutputWidth != null && config.customOutputHeight != null) {
            val width = config.customOutputWidth
            val height = config.customOutputHeight
            val scale = minOf(1f, maxDimension.toFloat() / maxOf(width, height))
            val nextWidth = ((width * scale).toInt() / 2) * 2
            val nextHeight = ((height * scale).toInt() / 2) * 2
            nextWidth to nextHeight
        } else null

        val changed = cappedResolution != config.resolution ||
            config.frameRate > maxFrameRate ||
            (tier == SubscriptionTier.FREE && config.codec != VideoCodec.H264) ||
            (tier == SubscriptionTier.FREE && config.quality == ExportQuality.HIGH) ||
            (customDimensions != null &&
                (customDimensions.first != config.customOutputWidth ||
                    customDimensions.second != config.customOutputHeight))

        return config.copy(
            resolution = cappedResolution,
            frameRate = config.frameRate.coerceIn(1, maxFrameRate),
            codec = if (tier == SubscriptionTier.FREE) VideoCodec.H264 else config.codec,
            quality = if (tier == SubscriptionTier.FREE && config.quality == ExportQuality.HIGH) {
                ExportQuality.MEDIUM
            } else config.quality,
            customOutputWidth = customDimensions?.first,
            customOutputHeight = customDimensions?.second,
            platformPreset = config.platformPreset.takeUnless { changed },
            bitrateOverride = config.bitrateOverride.takeUnless { changed },
            colorPolicy = if (tier == SubscriptionTier.PRO) config.colorPolicy else config.colorPolicy.withKeepHdr(false),
        )
    }

    fun allowsBatchExport(tier: SubscriptionTier): Boolean = tier == SubscriptionTier.PRO
}
