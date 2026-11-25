package com.directorx.editor.ui.editor

import com.directorx.editor.engine.AppLog
import com.directorx.editor.engine.CaptionTranslationEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns the caption-translation request lifecycle independently from the editor
 * ViewModel. It keeps the editor-facing state contract while rejecting stale
 * results after clip, language, model, or request-generation changes.
 */
internal class CaptionTranslationCoordinator(
    private val stateFlow: MutableStateFlow<EditorState>,
    private val scope: CoroutineScope,
    private val engine: CaptionTranslationEngine,
    private val networkAvailable: StateFlow<Boolean>,
) {
    private var job: Job? = null
    private var requestId: Long = 0L

    fun targets(): List<String> =
        engine.getSupportedLanguages(stateFlow.value.captionTranslationVariant)

    fun isPossible(): Boolean = engine.isTranslationPossible()

    fun setTarget(targetLang: String) {
        val current = stateFlow.value
        if (current.captionTranslationTargetLang != targetLang) {
            requestId++
            job?.cancel()
        }
        val quality = engine.pairQuality(
            current.captionTranslationVariant,
            current.captionTranslationSourceLang,
            targetLang,
        )
        stateFlow.update { state ->
            state.copyCaption { caption ->
                caption.copy(targetLang = targetLang, quality = quality)
            }
        }
    }

    fun runTranslation(targetLang: String) {
        setTarget(targetLang)
        job?.cancel()
        val identity = identity(targetLang)
        if (!networkAvailable.value) {
            updateUnavailable(identity, offline = true)
            return
        }
        if (!engine.isModelReady()) {
            updateUnavailable(identity, offline = false)
            return
        }

        val requestState = stateFlow.value
        val clip = identity.clipId?.let { id ->
            requestState.tracks.asSequence().flatMap { it.clips.asSequence() }.firstOrNull { it.id == id }
        }
        val segments = captionsToTranslationSegments(clip?.captions ?: emptyList())
        if (segments.isEmpty()) {
            stateFlow.update { current ->
                if (!isCurrent(identity, current)) current
                else current.copyCaption { caption ->
                    caption.copy(
                        translationRows = emptyList(),
                        translationUnavailable = false,
                        translationOffline = false,
                    )
                }
            }
            return
        }

        job = scope.launch {
            val translated = try {
                engine.translate(
                    segments = segments,
                    sourceLang = identity.sourceLanguage,
                    targetLang = identity.targetLanguage,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (unavailable: CaptionTranslationEngine.TranslationUnavailableException) {
                updateUnavailable(identity, offline = false)
                return@launch
            } catch (error: Exception) {
                AppLog.w("CaptionTranslation", "Caption translation failed", error)
                updateUnavailable(identity, offline = false)
                return@launch
            }
            val rows = engine.buildEditorRows(
                segments = translated,
                variant = identity.modelVariant,
                sourceLang = identity.sourceLanguage,
                targetLang = identity.targetLanguage,
            )
            stateFlow.update { current ->
                if (!isCurrent(identity, current)) current
                else current.copyCaption { caption ->
                    caption.copy(
                        translationRows = rows,
                        translationUnavailable = false,
                        translationOffline = false,
                        quality = rows.firstOrNull()?.quality ?: current.captionTranslationQuality,
                    )
                }
            }
        }
    }

    fun applyUserEdit(rowIndex: Int, newTargetText: String) {
        val rows = stateFlow.value.captionTranslationRows
        if (rowIndex !in rows.indices) return
        job?.cancel()
        requestId++
        val updated = engine.applyUserEdit(
            segments = rows.map { it.segment },
            index = rowIndex,
            newTargetText = newTargetText,
        )
        stateFlow.update { current ->
            current.copyCaption { caption ->
                caption.copy(
                    translationRows = updated.mapIndexed { index, segment ->
                        CaptionTranslationEngine.EditorRow(
                            index = index,
                            segment = segment,
                            quality = rows.getOrNull(index)?.quality
                                ?: current.captionTranslationQuality
                                ?: CaptionTranslationEngine.LanguagePairQuality.UNKNOWN,
                        )
                    },
                )
            }
        }
    }

    fun regenerate(rowIndex: Int) {
        val requestState = stateFlow.value
        val targetLanguage = requestState.captionTranslationTargetLang ?: return
        job?.cancel()
        val identity = identity(targetLanguage)
        if (!networkAvailable.value) {
            updateUnavailable(identity, offline = true, clearRows = false)
            return
        }
        if (!engine.isModelReady()) {
            updateUnavailable(identity, offline = false, clearRows = false)
            return
        }

        val rows = requestState.captionTranslationRows
        val row = rows.getOrNull(rowIndex) ?: return
        val pending = engine.markRegeneratePending(
            segments = rows.map { it.segment },
            index = rowIndex,
        )
        stateFlow.update { current ->
            if (!isCurrent(identity, current)) current
            else current.copyCaption { caption ->
                caption.copy(
                    translationRows = pending.mapIndexed { index, segment ->
                        CaptionTranslationEngine.EditorRow(
                            index = index,
                            segment = segment,
                            quality = rows.getOrNull(index)?.quality
                                ?: current.captionTranslationQuality
                                ?: CaptionTranslationEngine.LanguagePairQuality.UNKNOWN,
                        )
                    },
                )
            }
        }

        job = scope.launch {
            val translated = try {
                engine.translate(
                    segments = listOf(translatedSegmentToTranscriptionSegment(row.segment)),
                    sourceLang = identity.sourceLanguage,
                    targetLang = identity.targetLanguage,
                ).firstOrNull()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (unavailable: CaptionTranslationEngine.TranslationUnavailableException) {
                completeRegenerate(rowIndex, row.segment.targetText, identity, row.segment, unavailable = true)
                return@launch
            } catch (error: Exception) {
                AppLog.w("CaptionTranslation", "Caption translation regenerate failed", error)
                completeRegenerate(rowIndex, row.segment.targetText, identity, row.segment, unavailable = true)
                return@launch
            }
            completeRegenerate(
                rowIndex = rowIndex,
                newTargetText = translated?.targetText ?: row.segment.targetText,
                identity = identity,
                expectedSegment = row.segment,
                unavailable = translated == null,
            )
        }
    }

    private fun identity(targetLanguage: String): CaptionTranslationRequestIdentity {
        val current = stateFlow.value
        return CaptionTranslationRequestIdentity(
            requestId = ++requestId,
            clipId = current.selectedClipId,
            sourceLanguage = current.captionTranslationSourceLang,
            targetLanguage = targetLanguage,
            modelVariant = current.captionTranslationVariant,
        )
    }

    private fun isCurrent(
        identity: CaptionTranslationRequestIdentity,
        current: EditorState,
    ): Boolean = identity.matches(
        currentRequestId = requestId,
        currentClipId = current.selectedClipId,
        currentSourceLanguage = current.captionTranslationSourceLang,
        currentTargetLanguage = current.captionTranslationTargetLang,
        currentModelVariant = current.captionTranslationVariant,
    )

    private fun updateUnavailable(
        identity: CaptionTranslationRequestIdentity,
        offline: Boolean,
        clearRows: Boolean = true,
    ) {
        stateFlow.update { current ->
            if (!isCurrent(identity, current)) current
            else current.copyCaption { caption ->
                caption.copy(
                    translationRows = if (clearRows) emptyList() else caption.translationRows,
                    translationUnavailable = true,
                    translationOffline = offline,
                )
            }
        }
    }

    private fun completeRegenerate(
        rowIndex: Int,
        newTargetText: String,
        identity: CaptionTranslationRequestIdentity,
        expectedSegment: CaptionTranslationEngine.TranslatedSegment,
        unavailable: Boolean,
    ) {
        stateFlow.update { current ->
            if (!isCurrent(identity, current)) return@update current
            val rows = current.captionTranslationRows
            val currentRow = rows.getOrNull(rowIndex) ?: return@update current
            if (currentRow.segment.editorState != CaptionTranslationEngine.EditorRowState.REGENERATE_PENDING ||
                currentRow.segment.sourceText != expectedSegment.sourceText ||
                currentRow.segment.startTimeMs != expectedSegment.startTimeMs ||
                currentRow.segment.endTimeMs != expectedSegment.endTimeMs ||
                currentRow.segment.words != expectedSegment.words
            ) {
                return@update current
            }
            val updated = engine.completeRegenerate(
                segments = rows.map { it.segment },
                index = rowIndex,
                newTargetText = newTargetText,
            )
            current.copyCaption { caption ->
                caption.copy(
                    translationRows = updated.mapIndexed { index, segment ->
                        CaptionTranslationEngine.EditorRow(
                            index = index,
                            segment = segment,
                            quality = rows.getOrNull(index)?.quality
                                ?: current.captionTranslationQuality
                                ?: CaptionTranslationEngine.LanguagePairQuality.UNKNOWN,
                        )
                    },
                    translationUnavailable = unavailable,
                    translationOffline = false,
                )
            }
        }
    }
}
