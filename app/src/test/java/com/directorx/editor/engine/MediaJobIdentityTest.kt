package com.directorx.editor.engine

import android.net.FakeUri
import com.directorx.editor.model.Clip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaJobIdentityTest {

    @Test
    fun timelineEditsChangeTheCapturedMediaIdentity() {
        val original = clip()
        val identity = original.timelineMediaJobIdentity()

        assertTrue(shouldApplyMediaJobResult(identity, identity))
        assertFalse(
            shouldApplyMediaJobResult(
                identity,
                original.copy(trimStartMs = 100L).timelineMediaJobIdentity(),
            )
        )
        assertFalse(
            shouldApplyMediaJobResult(
                identity,
                original.copy(timelineStartMs = 500L).timelineMediaJobIdentity(),
            )
        )
        assertFalse(
            shouldApplyMediaJobResult(
                identity,
                original.copy(audioSyncOffsetMs = 500L).timelineMediaJobIdentity(),
            )
        )
        assertFalse(
            shouldApplyMediaJobResult(
                identity,
                identity.copy(sourceUri = "content://media/relinked.mp4"),
            )
        )
    }

    @Test
    fun deletedClipAndReusedIdCannotAcceptAnOldResult() {
        val identity = clip().timelineMediaJobIdentity()

        assertFalse(shouldApplyMediaJobResult(identity, null))
        assertFalse(
            shouldApplyMediaJobResult(
                identity,
                clip().copy(sourceDurationMs = 2_000L, trimEndMs = 2_000L)
                    .timelineMediaJobIdentity(),
            )
        )
    }

    @Test
    fun proxyResultRequiresTheSameNonEmptyVersion() {
        val version = clip().timelineMediaJobIdentity().version

        assertTrue(shouldApplyProxyResult(version, version))
        assertFalse(shouldApplyProxyResult(version, "new-version"))
        assertFalse(shouldApplyProxyResult(version, null))
        assertFalse(shouldApplyProxyResult("", ""))
    }


    @Test
    fun proxyMediaVersionIgnoresTimelineOnlyEdits() {
        val original = clip()
        val edited = original.copy(
            trimStartMs = 100L,
            trimEndMs = 900L,
            timelineStartMs = 500L,
            speed = 1.5f,
            audioSyncOffsetMs = 120L,
        )

        assertEquals(original.proxyMediaVersion(), edited.proxyMediaVersion())
    }

    @Test
    fun proxyMediaVersionChangesWhenTheAssetIdentityChanges() {
        val original = clip()
        val replacedAsset = original.copy(assetId = "different-asset")
        val changedDuration = original.copy(sourceDurationMs = 2_000L, trimEndMs = 2_000L)

        assertFalse(original.proxyMediaVersion() == replacedAsset.proxyMediaVersion())
        assertFalse(original.proxyMediaVersion() == changedDuration.proxyMediaVersion())
    }

    private fun clip(): Clip = Clip(
        sourceUri = FakeUri,
        sourceDurationMs = 1_000L,
        timelineStartMs = 0L,
        trimEndMs = 1_000L,
    )
}
