package com.directorx.editor.engine

import android.media.AudioAttributes
import android.media.AudioManager
import androidx.media3.common.C
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioFocusPolicyTest {

    @Test
    fun previewPlayback_usesMediaMovieAttributes() {
        val attributes = DirectorXAudioFocusPolicy.buildPreviewAttributes()

        assertEquals(C.USAGE_MEDIA, DirectorXAudioFocusPolicy.PREVIEW_USAGE)
        assertEquals(C.AUDIO_CONTENT_TYPE_MOVIE, DirectorXAudioFocusPolicy.PREVIEW_CONTENT_TYPE)
        assertEquals(C.USAGE_MEDIA, attributes.usage)
        assertEquals(C.AUDIO_CONTENT_TYPE_MOVIE, attributes.contentType)
    }

    @Test
    fun ttsPreview_usesTransientDuckFocus() {
        assertEquals(
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
            DirectorXAudioFocusPolicy.TTS_PREVIEW_FOCUS_GAIN,
        )
        assertEquals(
            AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY,
            DirectorXAudioFocusPolicy.TTS_PREVIEW_USAGE,
        )
        assertEquals(
            AudioAttributes.CONTENT_TYPE_SPEECH,
            DirectorXAudioFocusPolicy.TTS_PREVIEW_CONTENT_TYPE,
        )
    }

    @Test
    fun voiceover_usesExclusiveTransientFocus() {
        assertEquals(
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE,
            DirectorXAudioFocusPolicy.VOICEOVER_FOCUS_GAIN,
        )
        assertEquals(
            AudioAttributes.USAGE_VOICE_COMMUNICATION,
            DirectorXAudioFocusPolicy.VOICEOVER_USAGE,
        )
        assertEquals(
            AudioAttributes.CONTENT_TYPE_SPEECH,
            DirectorXAudioFocusPolicy.VOICEOVER_CONTENT_TYPE,
        )
    }
}
