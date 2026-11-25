package com.directorx.editor.ui.editor

import android.net.FakeUri
import com.directorx.editor.ai.AutoEditClip
import com.directorx.editor.ai.AutoEditIntent
import com.directorx.editor.ai.AutoEditResult
import com.directorx.editor.model.Clip
import com.directorx.editor.model.Track
import com.directorx.editor.model.TrackType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoEditCoordinatorTest {
    @Test
    fun removingAllVideoClipsInvalidatesAndCancelsAnInFlightProposal() {
        val clip = Clip(
            id = "source",
            sourceUri = FakeUri,
            sourceDurationMs = 1_000L,
            timelineStartMs = 0L,
            trimStartMs = 0L,
            trimEndMs = 1_000L,
        )
        val state = kotlinx.coroutines.flow.MutableStateFlow(
            EditorState(tracks = listOf(Track(type = TrackType.VIDEO, index = 0, clips = listOf(clip))))
        )
        val gate = CompletableDeferred<AutoEditResult>()
        var observedCancellation = false
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val coordinator = AutoEditCoordinator(
            stateFlow = state,
            scope = scope,
            generateAutoEdit = { _: List<AutoEditClip>, _: android.net.Uri?, _: Long, _: AutoEditIntent ->
                try {
                    gate.await()
                } catch (cancelled: CancellationException) {
                    observedCancellation = true
                    throw cancelled
                }
            },
            fingerprint = ::autoEditClipFingerprint,
            saveUndoState = {},
            recalculateDuration = { it },
            rebuildTimeline = {},
            saveProject = {},
            showToast = {},
            message = { _, _ -> "message" },
            hidePanel = {},
        )

        coordinator.run()
        assertTrue(state.value.ai.isAutoEditing)
        state.value = state.value.copy(tracks = emptyList())
        coordinator.run()

        assertTrue(observedCancellation)
        assertFalse(state.value.ai.isAutoEditing)
        assertFalse(state.value.ai.autoEditProposal != null)
        scope.cancel()
    }
}
