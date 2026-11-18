package com.directorx.editor.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ExportConfigCanvasTest {
    @Test
    fun exactCustomCanvasDimensionsOverridePresetSizing() {
        val config = ExportConfig(
            resolution = Resolution.UHD_4K,
            aspectRatio = AspectRatio.RATIO_16_9,
            customOutputWidth = 5312,
            customOutputHeight = 2988,
        )

        assertEquals(5312 to 2988, config.outputDimensions())
    }

    @Test
    fun customOutputDimensionsMustBeProvidedAsAValidPair() {
        assertThrows(IllegalArgumentException::class.java) {
            ExportConfig(customOutputWidth = 5312)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ExportConfig(customOutputWidth = 5313, customOutputHeight = 2988)
        }
    }

    @Test
    fun customProjectCanvasIsCopiedIntoExportConfiguration() {
        val project = Project(
            aspectRatio = AspectRatio.CUSTOM,
            customWidth = 5312,
            customHeight = 2988,
        )

        val config = ExportConfig().withProjectCanvas(project, AspectRatio.CUSTOM)

        assertEquals(5312 to 2988, config.outputDimensions())
    }

    @Test
    fun explicitPlatformPresetOverridesProjectCustomCanvas() {
        val project = Project(
            aspectRatio = AspectRatio.CUSTOM,
            customWidth = 5312,
            customHeight = 2988,
        )
        val presetConfig = ExportConfig().withPlatformPreset(PlatformPreset.YOUTUBE_4K)
        val resolved = presetConfig.withProjectCanvas(project, AspectRatio.RATIO_16_9)

        assertNull(resolved.customOutputWidth)
        assertNull(resolved.customOutputHeight)
        assertEquals(Resolution.UHD_4K.forAspect(AspectRatio.RATIO_16_9), resolved.outputDimensions())
    }

    @Test
    fun selectingPlatformPresetClearsProjectCustomDimensions() {
        val config = ExportConfig(
            customOutputWidth = 5312,
            customOutputHeight = 2988,
        ).withPlatformPreset(PlatformPreset.YOUTUBE_4K)

        assertNull(config.customOutputWidth)
        assertNull(config.customOutputHeight)
        assertEquals(Resolution.UHD_4K.forAspect(AspectRatio.RATIO_16_9), config.outputDimensions())
    }
}
