package com.directorx.editor.engine

import com.directorx.editor.model.VideoCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EncoderCapabilityProbeHdrTest {

    @Test
    fun capabilityDistinguishesHardwareSoftwareAndUnverifiedPaths() {
        val hardware = EncoderCapabilityProbe.Capability(
            supported = true,
            encoderPath = EncoderCapabilityProbe.EncoderPath.HARDWARE,
            encoderNames = listOf("vendor.hevc.encoder"),
            maxWidth = 7680,
            maxHeight = 4320,
            maxBitrate = 120_000_000,
            widthAlignment = 2,
            heightAlignment = 2,
            profileLevels = listOf(1 to 2, 1 to 4),
        )
        assertTrue(hardware.hardwareAccelerated)
        assertFalse(hardware.softwareOnly)
        assertTrue(hardware.performanceVerified)
        assertEquals(7680, hardware.maxWidth)
        assertEquals(4320, hardware.maxHeight)
        assertEquals(listOf(1 to 2, 1 to 4), hardware.profileLevels)

        val software = hardware.copy(encoderPath = EncoderCapabilityProbe.EncoderPath.SOFTWARE)
        assertFalse(software.hardwareAccelerated)
        assertTrue(software.softwareOnly)
        assertFalse(software.performanceVerified)

        val unknown = hardware.copy(encoderPath = EncoderCapabilityProbe.EncoderPath.UNKNOWN)
        assertFalse(unknown.hardwareAccelerated)
        assertFalse(unknown.softwareOnly)
        assertFalse(unknown.performanceVerified)
    }

    @Test
    fun capabilityNeverReportsInvalidOrMissingEncoderAsHardwareSupported() {
        val none = EncoderCapabilityProbe.Capability(
            supported = false,
            reason = "No encoder",
            encoderPath = EncoderCapabilityProbe.EncoderPath.NONE,
        )
        assertFalse(none.supported)
        assertFalse(none.hardwareAccelerated)
        assertFalse(none.performanceVerified)

        val unknown = EncoderCapabilityProbe.Capability(
            supported = false,
            known = false,
            reason = "Probe unavailable",
            encoderPath = EncoderCapabilityProbe.EncoderPath.UNKNOWN,
        )
        assertFalse(unknown.supported)
        assertFalse(unknown.known)
        assertFalse(unknown.performanceVerified)
    }

    @Test
    fun hdrFeatureSupportRequiresEitherPlatformEditingFeature() {
        assertFalse(EncoderCapabilityProbe.HdrFeatureSupport().canPreserveHdr)
        assertTrue(
            EncoderCapabilityProbe.HdrFeatureSupport(hdrEditing = true).canPreserveHdr
        )
        assertTrue(
            EncoderCapabilityProbe.HdrFeatureSupport(hlgEditing = true).canPreserveHdr
        )
    }

    @Test
    fun advertisedFeatureNamesExposeThePlatformContracts() {
        val support = EncoderCapabilityProbe.HdrFeatureSupport(
            hdrEditing = true,
            hlgEditing = true,
        )

        assertEquals(
            setOf("FEATURE_HdrEditing", "FEATURE_HlgEditing"),
            support.advertisedFeatureNames,
        )
    }

    @Test
    fun profileNamesDoNotBypassTheStrictFeatureGate() {
        val support = EncoderCapabilityProbe.HdrProfileSupport(
            codec = VideoCodec.HEVC,
            supportedFormats = setOf(EncoderCapabilityProbe.HdrExportFormat.HDR10),
            featureSupport = EncoderCapabilityProbe.HdrFeatureSupport(),
        )

        assertTrue(support.hasAnyHdr)
        assertFalse(support.canPreserveHdr)
        assertTrue(support.featureFailureReason().contains("FEATURE_HdrEditing"))
    }
}
