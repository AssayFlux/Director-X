package com.directorx.editor.ui.editor

import com.directorx.editor.engine.CaptionTranslationEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptionTranslationRequestIdentityTest {
    private val identity = CaptionTranslationRequestIdentity(
        requestId = 7L,
        clipId = "clip-1",
        sourceLanguage = "en",
        targetLanguage = "fr",
        modelVariant = CaptionTranslationEngine.ModelVariant.NLLB_600M,
    )

    @Test
    fun matchesOnlyTheClipAndLanguageModelContextThatStartedTheRequest() {
        assertTrue(identity.matches(
            7L, "clip-1", "en", "fr", CaptionTranslationEngine.ModelVariant.NLLB_600M
        ))
        assertFalse(identity.matches(
            8L, "clip-1", "en", "fr", CaptionTranslationEngine.ModelVariant.NLLB_600M
        ))
        assertFalse(identity.matches(
            7L, "clip-2", "en", "fr", CaptionTranslationEngine.ModelVariant.NLLB_600M
        ))
        assertFalse(identity.matches(
            7L, "clip-1", "en", "de", CaptionTranslationEngine.ModelVariant.NLLB_600M
        ))
        assertFalse(identity.matches(
            7L, "clip-1", "es", "fr", CaptionTranslationEngine.ModelVariant.NLLB_600M
        ))
        assertFalse(identity.matches(
            7L, "clip-1", "en", "fr", CaptionTranslationEngine.ModelVariant.MADLAD_400_3B
        ))
    }
}
