package com.directorx.editor.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportConfigPresetTest {

    @Test
    fun applyingPresetCopiesEveryDeliverySetting() {
        val config = ExportConfig().withPlatformPreset(PlatformPreset.TIKTOK)

        assertEquals(Resolution.FHD_1080P, config.resolution)
        assertEquals(AspectRatio.RATIO_9_16, config.aspectRatio)
        assertEquals(30, config.frameRate)
        assertEquals(VideoCodec.H264, config.codec)
        assertEquals(PlatformPreset.TIKTOK, config.platformPreset)
        assertTrue(config.requiresStreamSafeOutput("mp4"))
        assertFalse(config.requiresStreamSafeOutput("webm"))
    }

    @Test
    fun selectedPresetWinsOverProjectCanvasForOutput() {
        val presetConfig = ExportConfig().withPlatformPreset(PlatformPreset.INSTAGRAM_FEED)
        val customConfig = ExportConfig()

        assertEquals(
            AspectRatio.RATIO_1_1,
            presetConfig.outputAspectRatio(AspectRatio.RATIO_16_9),
        )
        assertEquals(
            AspectRatio.RATIO_9_16,
            customConfig.outputAspectRatio(AspectRatio.RATIO_9_16),
        )
        assertFalse(customConfig.requiresStreamSafeOutput("mp4"))
    }

    @Test
    fun highResolutionPresetsResolveToExpectedDimensionsAndBitrates() {
        val fivePointThreeK = ExportConfig(resolution = Resolution.UHD_5_3K)
        val eightK = ExportConfig(resolution = Resolution.UHD_8K)

        assertEquals(5312 to 2988, fivePointThreeK.outputDimensions())
        assertEquals(7680 to 4320, eightK.outputDimensions())
        assertEquals(2988 to 5312, fivePointThreeK.copy(aspectRatio = AspectRatio.RATIO_9_16).outputDimensions())
        assertEquals(4320 to 7680, eightK.copy(aspectRatio = AspectRatio.RATIO_9_16).outputDimensions())
        assertEquals(100_000_000, fivePointThreeK.videoBitrate)
        assertEquals(160_000_000, eightK.videoBitrate)
    }

    @Test
    fun everyNamedPlatformPresetRequiresProgressiveMp4Metadata() {
        assertTrue(PlatformPreset.entries.all { it.requiresStreamSafeMp4 })
    }
}
