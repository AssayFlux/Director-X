package com.directorx.editor.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectCanvasDimensionsTest {

    @Test
    fun acceptsEvenDimensionsWithinBoundsAndAspectLimits() {
        assertTrue(CanvasDimensions.isValid(1920, 1080))
        assertTrue(CanvasDimensions.isValid(1080, 1920))
        assertTrue(CanvasDimensions.isValid(2048, 2048))
        assertTrue(CanvasDimensions.isValid(8192, 2048))
        assertTrue(CanvasDimensions.isValid(2048, 8192))
    }

    @Test
    fun rejectsOddOutOfBoundsAndExtremeAspectDimensions() {
        assertFalse(CanvasDimensions.isValid(1919, 1080))
        assertFalse(CanvasDimensions.isValid(0, 1080))
        assertFalse(CanvasDimensions.isValid(8194, 2048))
        assertFalse(CanvasDimensions.isValid(8192, 2))
        assertFalse(CanvasDimensions.isValid(2, 8192))
    }

    @Test
    fun customProjectRequiresValidDimensions() {
        val project = Project(
            aspectRatio = AspectRatio.CUSTOM,
            customWidth = 2560,
            customHeight = 1440
        )
        assertEquals(CanvasDimensions(2560, 1440), project.canvasDimensions)
        assertThrows(IllegalArgumentException::class.java) {
            Project(aspectRatio = AspectRatio.CUSTOM, customWidth = 1921, customHeight = 1080)
        }
    }

    @Test
    fun customProjectExposesItsExactAspectRatio() {
        val project = Project(
            aspectRatio = AspectRatio.CUSTOM,
            customWidth = 2048,
            customHeight = 858,
        )

        assertEquals(2048f / 858f, project.canvasAspectRatio, 0.0001f)
    }

    @Test
    fun scalingCustomCanvasFitsMaximumDimensionAndKeepsEvenDimensions() {
        val scaled = CanvasDimensions.requireValid(4096, 2160).scaledToFit(720)

        assertEquals(720, scaled.width)
        assertEquals(378, scaled.height)
        assertTrue(scaled.width % 2 == 0)
        assertTrue(scaled.height % 2 == 0)
        assertEquals(4096f / 2160f, scaled.width.toFloat() / scaled.height, 0.01f)
    }

    @Test
    fun presetProjectStillUsesPresetResolution() {
        val project = Project(
            aspectRatio = AspectRatio.RATIO_9_16,
            resolution = Resolution.FHD_1080P
        )
        assertEquals(CanvasDimensions(1080, 1920), project.canvasDimensions)
    }
}
