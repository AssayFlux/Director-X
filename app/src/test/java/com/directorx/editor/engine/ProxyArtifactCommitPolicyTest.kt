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
    fun nonEmptyTemporaryTransformerOutputIsAcceptedForPromotionButNotCacheReuse() {
        val file = File(temporaryFolder.root, "proxy_tmp_0123456789abcdef.mp4")
        file.writeBytes(byteArrayOf(1, 2, 3))

        assertTrue(isValidProxyTransformerOutput(file))
        assertFalse(isCompletedProxyArtifact(file))
    }

    @Test
    fun emptyTemporaryTransformerOutputIsRejectedForPromotion() {
        val file = File(temporaryFolder.root, "proxy_tmp_0123456789abcdef.mp4")
        file.createNewFile()

        assertFalse(isValidProxyTransformerOutput(file))
        assertFalse(isCompletedProxyArtifact(file))
    }

    @Test
    fun committedFilenameIsNotAcceptedAsTemporaryTransformerOutput() {
        val file = File(temporaryFolder.root, "proxy_0123456789abcdef.mp4")
        file.writeBytes(byteArrayOf(1, 2, 3))

        assertFalse(isValidProxyTransformerOutput(file))
        assertTrue(isCompletedProxyArtifact(file))
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

        assertFalse(isValidProxyTransformerOutput(file))
        assertFalse(isCompletedProxyArtifact(file))
    }
}
