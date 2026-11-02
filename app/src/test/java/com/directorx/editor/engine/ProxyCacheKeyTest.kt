package com.directorx.editor.engine

import android.net.Uri
import com.directorx.editor.model.ProxyResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ProxyCacheKeyTest {
    private val source = Uri.parse("content://media/external/video/42")

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
    fun differentSourcesNeverShareCacheIdentity() {
        assertNotEquals(
            proxyCacheKey(source, ProxyResolution.QUARTER),
            proxyCacheKey(Uri.parse("content://media/external/video/43"), ProxyResolution.QUARTER)
        )
    }
}
