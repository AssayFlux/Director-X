package com.directorx.editor.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FFmpegEditingProxyArgumentsTest {
    @Test
    fun softwareProxyFallbackIsLimitedToH264AndHevcCameraMedia() {
        assertTrue(isSupportedEditingProxyMimeType("video/avc"))
        assertTrue(isSupportedEditingProxyMimeType("video/hevc"))
        assertFalse(isSupportedEditingProxyMimeType("video/x-vnd.on2.vp9"))
        assertFalse(isSupportedEditingProxyMimeType("video/quicktime"))
        assertFalse(isSupportedEditingProxyMimeType(null))
    }

    @Test
    fun proxyArgumentsScaleToBoundedEvenHeightAndPreserveOptionalAudio() {
        val args = buildEditingProxyArguments(
            inputPath = "/private/source.mov",
            outputPath = "/private/proxy.mp4",
            targetHeight = 1494,
            videoEncoder = "mpeg4",
            includeAudio = true,
        )

        assertTrue(args.containsAll(listOf("-map", "0:v:0", "-map", "0:a:0?")))
        assertTrue(args.contains("scale=-2:1494:flags=lanczos"))
        assertTrue(args.containsAll(listOf("-c:v", "mpeg4", "-pix_fmt", "yuv420p")))
        assertTrue(args.containsAll(listOf("-c:a", "aac", "-b:a", "128k")))
        assertTrue(args.containsAll(listOf("-movflags", "+faststart", "/private/proxy.mp4")))
    }

    @Test
    fun proxyArgumentsOmitAudioWhenSourceHasNoAudio() {
        val args = buildEditingProxyArguments(
            inputPath = "/private/source.mp4",
            outputPath = "/private/proxy.mp4",
            targetHeight = 900,
            videoEncoder = "mpeg4",
            includeAudio = false,
        )

        assertFalse(args.contains("0:a:0?"))
        assertFalse(args.contains("-c:a"))
        assertTrue(args.contains("scale=-2:900:flags=lanczos"))
    }

    @Test
    fun proxyHeightIsClampedToSupportedEvenRange() {
        val args = buildEditingProxyArguments(
            inputPath = "/private/source.mp4",
            outputPath = "/private/proxy.mp4",
            targetHeight = 9999,
            videoEncoder = "mpeg4",
            includeAudio = false,
        )

        assertTrue(args.contains("scale=-2:2160:flags=lanczos"))
    }
}
