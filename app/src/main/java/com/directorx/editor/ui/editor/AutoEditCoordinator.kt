package com.directorx.editor.ui.editor

import android.net.Uri
import com.directorx.editor.R
import com.directorx.editor.ai.AutoEditClip
import com.directorx.editor.ai.AutoEditIntent
import com.directorx.editor.ai.AutoEditResult
import com.directorx.editor.engine.AiUsageLedger
import com.directorx.editor.engine.AppLog
import com.directorx.editor.model.Clip
import com.directorx.editor.model.TrackType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns the asynchronous Auto Edit proposal lifecycle and its apply transaction.
 * The ViewModel supplies state/persistence callbacks; proposal generation,
 * cancellation, stale-result protection, and usage-ledger updates stay together.
 */
internal class AutoEditCoordinator(
    private val stateFlow: MutableStateFlow<EditorState>,
    private val scope: CoroutineScope,
    private val generateAutoEdit: suspend (
        clips: List<AutoEditClip>,
        musicUri: Uri?,
        targetDurationMs: Long,
        intent: AutoEditIntent,
    ) -> AutoEditResult,
    private val fingerprint: (Clip) -> String,
    private val saveUndoState: (String) -> Unit,
    private val recalculateDuration: (EditorState) -> EditorState,
    private val rebuildTimeline: () -> Unit,
    private val saveProject: () -> Unit,
    private val showToast: (String) -> Unit,
    private val message: (Int, List<Any>) -> String,
    private val hidePanel: (PanelId) -> Unit,
) {
    private var job: Job? = null
    private var generationId: Long = 0L

    fun run(
        intent: AutoEditIntent = AutoEditIntent.HIGHLIGHT_REEL,
        targetDurationMs: Long = 60_000L,
    ) {
        val clips = stateFlow.value.tracks
            .filter { it.type == TrackType.VIDEO }
            .flatMap { it.clips }
            .sortedWith(compareBy<Clip> { it.timelineStartMs }.thenBy { it.id })
        if (clips.isEmpty()) {
            generationId++
            job?.cancel()
            job = null
            stateFlow.update { current ->
                current.copyAi { ai -> ai.copy(isAutoEditing = false, autoEditProposal = null) }
            }
            showToast(message(R.string.vm_add_video_clips_first_toast, emptyList()))
            return
        }

        job?.cancel()
        val currentGeneration = ++generationId
        stateFlow.update { current ->
            current.copyAi { ai -> ai.copy(isAutoEditing = true, autoEditProposal = null) }
        }
        job = scope.launch {
            try {
                val autoClips = clips.mapIndexed { index, clip ->
                    AutoEditClip(
                        clipId = clip.id,
                        clipFingerprint = fingerprint(clip),
                        uri = clip.sourceUri,
                        sourceStartMs = clip.trimStartMs,
                        sourceEndMs = clip.trimEndMs,
                        sourceOrder = index,
                    )
                }
                val musicUri = stateFlow.value.tracks
                    .filter { it.type == TrackType.AUDIO }
                    .flatMap { it.clips }
                    .firstOrNull()?.sourceUri
                val result = generateAutoEdit(autoClips, musicUri, targetDurationMs, intent)

                if (currentGeneration != generationId) return@launch
                if (result.segments.isNotEmpty()) {
                    stateFlow.update { current ->
                        current.copyAi { ai -> ai.copy(isAutoEditing = false, autoEditProposal = result) }
                    }
                } else {
                    stateFlow.update { current ->
                        current.copyAi { ai -> ai.copy(isAutoEditing = false, autoEditProposal = null) }
                    }
                    showToast(message(R.string.vm_auto_edit_failed_toast, emptyList()))
                }
            } catch (cancelled: CancellationException) {
                if (currentGeneration == generationId) {
                    stateFlow.update { current ->
                        current.copyAi { ai -> ai.copy(isAutoEditing = false) }
                    }
                }
            } catch (error: Exception) {
                AppLog.e("AutoEditCoordinator", "Auto Edit proposal failed", error)
                if (currentGeneration == generationId) {
                    stateFlow.update { current ->
                        current.copyAi { ai -> ai.copy(isAutoEditing = false, autoEditProposal = null) }
                    }
                    showToast(message(R.string.vm_auto_edit_error_toast, emptyList()))
                }
            } finally {
                if (currentGeneration == generationId) job = null
            }
        }
    }

    fun cancel() {
        generationId++
        job?.cancel()
        job = null
        stateFlow.update { current ->
            current.copyAi { ai -> ai.copy(isAutoEditing = false, autoEditProposal = null) }
        }
    }

    fun applyProposal() {
        val snapshot = stateFlow.value
        val proposal = snapshot.autoEditProposal ?: return
        val sourceClips = snapshot.tracks.filter { it.type == TrackType.VIDEO }.flatMap { it.clips }
        val primaryVideoTrack = snapshot.tracks.firstOrNull { it.type == TrackType.VIDEO } ?: return
        val newClips = try {
            buildAutoEditExcerptClips(sourceClips, proposal)
        } catch (error: Exception) {
            AppLog.w("AutoEditCoordinator", "Auto Edit proposal became stale", error)
            stateFlow.update { current ->
                current.copyAi { ai -> ai.copy(autoEditProposal = null) }
            }
            showToast(message(R.string.vm_auto_edit_stale_toast, emptyList()))
            return
        }

        val recordedAt = System.currentTimeMillis()
        val usageEntries = newClips.map { clip ->
            AiUsageRecordFactory.forClip(
                clip = clip,
                effectKind = AiUsageLedger.EffectKind.AUTO_EDIT_LOCAL,
                modelName = "Director X Auto Edit window scorer",
                recordedAtEpochMs = recordedAt,
            )
        }
        saveUndoState("Apply Auto Edit")
        stateFlow.update { current ->
            recalculateDuration(
                current.copy(
                    tracks = current.tracks.map { track ->
                        when {
                            track.id == primaryVideoTrack.id -> track.copy(clips = newClips)
                            track.type == TrackType.VIDEO -> track.copy(clips = emptyList())
                            else -> track
                        }
                    },
                    selectedClipId = null,
                    selectedClipIds = emptySet(),
                    ai = current.ai.copy(
                        autoEditProposal = null,
                        usageLedger = AiUsageLedger.mergeOverlaps(current.aiUsageLedger + usageEntries),
                    ),
                )
            )
        }
        rebuildTimeline()
        saveProject()
        showToast(message(R.string.vm_auto_edit_created_toast, listOf(newClips.size)))
        hidePanel(PanelId.AUTO_EDIT)
    }
}
