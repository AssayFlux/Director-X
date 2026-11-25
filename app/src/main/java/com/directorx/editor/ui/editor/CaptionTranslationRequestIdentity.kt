package com.directorx.editor.ui.editor

import com.directorx.editor.engine.CaptionTranslationEngine

/**
 * Captures the editor context for one asynchronous caption-translation request.
 * Results from a previous clip, language pair, or model selection must never
 * overwrite the currently visible translation rows.
 */
internal data class CaptionTranslationRequestIdentity(
    val requestId: Long,
    val clipId: String?,
    val sourceLanguage: String,
    val targetLanguage: String,
    val modelVariant: CaptionTranslationEngine.ModelVariant,
) {
    fun matches(
        currentRequestId: Long,
        currentClipId: String?,
        currentSourceLanguage: String,
        currentTargetLanguage: String?,
        currentModelVariant: CaptionTranslationEngine.ModelVariant,
    ): Boolean =
        requestId == currentRequestId &&
            clipId == currentClipId &&
            sourceLanguage == currentSourceLanguage &&
            targetLanguage == currentTargetLanguage &&
            modelVariant == currentModelVariant
}
