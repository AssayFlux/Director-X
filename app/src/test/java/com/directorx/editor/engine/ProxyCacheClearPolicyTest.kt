package com.directorx.editor.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProxyCacheClearPolicyTest {
    @Test
    fun activeProxyMappingIsRetained() {
        assertTrue(
            shouldRetainProxyMapping(
                key = "active",
                activeKeys = setOf("active"),
                failedKeys = emptySet(),
                backingFileExists = false,
            )
        )
    }

    @Test
    fun mappingIsRetainedWhenDeletionFailed() {
        assertTrue(
            shouldRetainProxyMapping(
                key = "failed",
                activeKeys = emptySet(),
                failedKeys = setOf("failed"),
                backingFileExists = true,
            )
        )
    }

    @Test
    fun mappingIsRetainedWhenItsBackingFileStillExists() {
        assertTrue(
            shouldRetainProxyMapping(
                key = "present",
                activeKeys = emptySet(),
                failedKeys = emptySet(),
                backingFileExists = true,
            )
        )
    }

    @Test
    fun staleMappingIsRemovedOnlyWhenFileIsGoneAndNotActiveOrFailed() {
        assertFalse(
            shouldRetainProxyMapping(
                key = "stale",
                activeKeys = emptySet(),
                failedKeys = emptySet(),
                backingFileExists = false,
            )
        )
    }
}
