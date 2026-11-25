package com.directorx.editor.engine

import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector

/**
 * Android emulators expose a goldfish H.264 decoder that can initialize successfully and then
 * fail while consuming buffers. Prefer the platform software decoder on emulator images only.
 * Physical devices retain Android's native decoder order and hardware acceleration preference.
 */
@OptIn(UnstableApi::class)
internal object Media3CodecPolicy {
    fun selector(
        delegate: MediaCodecSelector = MediaCodecSelector.DEFAULT,
    ): MediaCodecSelector {
        if (!isEmulator()) return delegate
        return MediaCodecSelector { mimeType, requiresSecureDecoder, requiresTunnelingDecoder ->
            delegate.getDecoderInfos(mimeType, requiresSecureDecoder, requiresTunnelingDecoder)
                .sortedByDescending(MediaCodecInfo::softwareOnly)
        }
    }

    internal fun isEmulator(): Boolean {
        val hardware = Build.HARDWARE.orEmpty().lowercase()
        val model = Build.MODEL.orEmpty().lowercase()
        val fingerprint = Build.FINGERPRINT.orEmpty().lowercase()
        return hardware.contains("goldfish") ||
            hardware.contains("ranchu") ||
            model.contains("sdk_gphone") ||
            fingerprint.startsWith("generic")
    }
}
