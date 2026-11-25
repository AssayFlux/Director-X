package com.directorx.editor.engine.billing

import com.directorx.editor.model.ExportConfig
import com.directorx.editor.model.ExportQuality
import com.directorx.editor.model.Resolution
import com.directorx.editor.model.VideoCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportTierPolicyTest {
    @Test fun freeExportIsCappedAt720p30H264AndBalancedQuality() {
        val result = ExportTierPolicy.enforce(
            ExportConfig(resolution = Resolution.UHD_4K, frameRate = 60, codec = VideoCodec.HEVC, quality = ExportQuality.HIGH),
            SubscriptionTier.FREE,
        )
        assertEquals(Resolution.HD_720P, result.resolution)
        assertEquals(30, result.frameRate)
        assertEquals(VideoCodec.H264, result.codec)
        assertEquals(ExportQuality.MEDIUM, result.quality)
    }

    @Test fun plusExportIsCappedAt1080p60ButRetainsCodecChoice() {
        val result = ExportTierPolicy.enforce(
            ExportConfig(resolution = Resolution.UHD_4K, frameRate = 120, codec = VideoCodec.HEVC),
            SubscriptionTier.PLUS,
        )
        assertEquals(Resolution.FHD_1080P, result.resolution)
        assertEquals(60, result.frameRate)
        assertEquals(VideoCodec.HEVC, result.codec)
    }

    @Test fun proKeepsRequestedResolutionAndFrameRateWithinDevicePreflight() {
        val requested = ExportConfig(resolution = Resolution.UHD_4K, frameRate = 60, codec = VideoCodec.HEVC)
        assertEquals(requested, ExportTierPolicy.enforce(requested, SubscriptionTier.PRO))
    }

    @Test fun customDimensionsAreCappedWithoutChangingAspectRatioMaterially() {
        val result = ExportTierPolicy.enforce(
            ExportConfig(customOutputWidth = 3840, customOutputHeight = 2160),
            SubscriptionTier.FREE,
        )
        assertTrue(result.customOutputWidth!! <= 1280)
        assertTrue(result.customOutputHeight!! <= 1280)
        assertEquals(16f / 9f, result.customOutputWidth.toFloat() / result.customOutputHeight!!, 0.01f)
    }

    @Test fun batchExportIsProOnly() {
        assertFalse(ExportTierPolicy.allowsBatchExport(SubscriptionTier.FREE))
        assertFalse(ExportTierPolicy.allowsBatchExport(SubscriptionTier.PLUS))
        assertTrue(ExportTierPolicy.allowsBatchExport(SubscriptionTier.PRO))
    }
}
