package com.directorx.editor.engine

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProxyArtifactCommitPolicyTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun nonEmptyCommittedProxyIsAccepted() {
        val file = File(temporaryFolder.root, "proxy_0123456789abcdef.mp4")
        file.writeBytes(byteArrayOf(1, 2, 3))

        assertTrue(isCompletedProxyArtifact(file))
    }

    @Test
    fun nonEmptyInterruptedTransformerOutputIsNeverAccepted() {
        val file = File(temporaryFolder.root, "proxy_tmp_0123456789abcdef.mp4")
        file.writeBytes(byteArrayOf(1, 2, 3))

        assertFalse(isCompletedProxyArtifact(file))
    }

    @Test
    fun nonEmptyTemporaryProxyOutputCanBePromotedAfterSuccessfulEncode() {
        val file = File(temporaryFolder.root, "proxy_tmp_0123456789abcdef.mp4")
        file.writeBytes(byteArrayOf(1, 2, 3))

        assertTrue(isUsableProxyTemporaryArtifact(file))
    }

    @Test
    fun emptyTemporaryProxyOutputCannotBePromoted() {
        val file = File(temporaryFolder.root, "proxy_tmp_0123456789abcdef.mp4")
        file.createNewFile()

        assertFalse(isUsableProxyTemporaryArtifact(file))
    }

    @Test
    fun emptyCommittedProxyIsNotAccepted() {
        val file = File(temporaryFolder.root, "proxy_0123456789abcdef.mp4")
        file.createNewFile()

        assertFalse(isCompletedProxyArtifact(file))
    }

    @Test
    fun unrelatedFilesAreNotAcceptedAsProxyArtifacts() {
        val file = File(temporaryFolder.root, "original.mp4")
        file.writeBytes(byteArrayOf(1, 2, 3))

        assertFalse(isCompletedProxyArtifact(file))
    }
    @Test
    fun abandonedTemporaryProxyIsRecognizedOnlyAfterTheGracePeriod() {
        val file = File(temporaryFolder.root, "proxy_tmp_abandoned.mp4")
        file.writeBytes(byteArrayOf(1, 2, 3))
        val now = 10_000_000L
        file.setLastModified(now - 60_000L)

        assertTrue(isAbandonedProxyTemporaryArtifact(file, now, 60_000L))
        assertFalse(isAbandonedProxyTemporaryArtifact(file, now, 60_001L))
    }

    @Test
    fun abandonedProxySweepRemovesOnlyOldTemporaryArtifacts() {
        val stalePartial = File(temporaryFolder.root, "proxy_tmp_stale.mp4").apply {
            writeBytes(byteArrayOf(1))
            setLastModified(1_000L)
        }
        val freshPartial = File(temporaryFolder.root, "proxy_tmp_fresh.mp4").apply {
            writeBytes(byteArrayOf(1))
            setLastModified(9_500L)
        }
        val committed = File(temporaryFolder.root, "proxy_committed.mp4").apply {
            writeBytes(byteArrayOf(1))
            setLastModified(1_000L)
        }
        val unrelated = File(temporaryFolder.root, "original.mp4").apply {
            writeBytes(byteArrayOf(1))
            setLastModified(1_000L)
        }

        val removed = sweepAbandonedProxyTemporaryArtifacts(
            proxyRoot = temporaryFolder.root,
            nowMillis = 10_000L,
            maxAgeMillis = 1_000L,
        )

        assertEquals(1, removed)
        assertFalse(stalePartial.exists())
        assertTrue(freshPartial.exists())
        assertTrue(committed.exists())
        assertTrue(unrelated.exists())
    }

}
