package com.directorx.editor.engine

import com.directorx.editor.model.ProxyResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ProxyCacheKeyTest {
    private val source = "content://media/external/video/42"

    @Test
    fun sameSourceAndResolutionHaveStableIdentity() {
        assertEquals(
            proxyCacheKey(source, ProxyResolution.HALF),
            proxyCacheKey(source, ProxyResolution.HALF)
        )
    }

    @Test
    fun differentResolutionsNeverShareCacheIdentity() {
        val keys = ProxyResolution.entries.map { proxyCacheKey(source, it) }
        assertEquals(ProxyResolution.entries.size, keys.toSet().size)
    }

    @Test
    fun replacingContentAtTheSameUriInvalidatesTheCacheIdentity() {
        assertNotEquals(
            proxyCacheKey(source, ProxyResolution.QUARTER, "version-a"),
            proxyCacheKey(source, ProxyResolution.QUARTER, "version-b")
        )
    }

    @Test
    fun defaultVersionPreservesStableUriBasedCallers() {
        assertEquals(
            proxyCacheKey(source, ProxyResolution.HALF),
            proxyCacheKey(source, ProxyResolution.HALF, source)
        )
    }

    @Test
    fun differentSourcesNeverShareCacheIdentity() {
        assertNotEquals(
            proxyCacheKey(source, ProxyResolution.QUARTER),
            proxyCacheKey("content://media/external/video/43", ProxyResolution.QUARTER)
        )
    }
}
