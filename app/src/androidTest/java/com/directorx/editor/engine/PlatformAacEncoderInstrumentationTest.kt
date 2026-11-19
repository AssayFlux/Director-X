package com.directorx.editor.engine

import android.media.MediaExtractor
import android.media.MediaFormat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * Physical-device contract for the Android platform AAC encoder used by
 * noise reduction. Verifies the PCM -> MediaCodec -> MediaMuxer path produces
 * a readable audio-only M4A, without a bundled command-line transcoder.
 */
@RunWith(AndroidJUnit4::class)
class PlatformAacEncoderInstrumentationTest {

    @Test
    fun encodesPcmToPlayableAacM4a() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val pcmFile = File(context.cacheDir, "platform-aac-input-${System.nanoTime()}.pcm")
        val outputFile = File(context.cacheDir, "platform-aac-output-${System.nanoTime()}.m4a")
        val sampleRate = 48_000
        val channels = 1
        val frames = sampleRate

        val buffer = ByteBuffer.allocate(frames * channels * 2).order(ByteOrder.LITTLE_ENDIAN)
        repeat(frames) { index ->
            val sample = (sin(2.0 * PI * 440.0 * index / sampleRate) * Short.MAX_VALUE * 0.25)
                .toInt().toShort()
            buffer.putShort(sample)
        }
        pcmFile.writeBytes(buffer.array())

        try {
            val engine = AudioEngine(context, MemoryTrimRegistry())
            assertTrue(
                "Android MediaCodec AAC encoder should produce a valid M4A",
                engine.encodePcm16leToM4a(
                    inputFile = pcmFile,
                    outputFile = outputFile,
                    sampleRate = sampleRate,
                    channels = channels,
                ),
            )
            assertTrue(outputFile.isFile && outputFile.length() > 0L)

            val extractor = MediaExtractor()
            try {
                extractor.setDataSource(outputFile.absolutePath)
                val audioFormats = (0 until extractor.trackCount).map { extractor.getTrackFormat(it) }
                    .filter { it.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true }
                assertTrue("Encoded M4A must contain an audio track", audioFormats.isNotEmpty())
                val durationUs = audioFormats.first().getLong(MediaFormat.KEY_DURATION)
                assertTrue("Encoded audio duration must be positive", durationUs > 0L)
            } finally {
                extractor.release()
            }
        } finally {
            pcmFile.delete()
            outputFile.delete()
        }
    }
}
