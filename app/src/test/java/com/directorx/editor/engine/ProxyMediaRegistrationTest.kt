package com.directorx.editor.engine

import android.net.FakeUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProxyMediaRegistrationTest {

    @Test
    fun registrationRehydratesCompletedProxyAfterProcessRecreation() {
        val cachedProxy = FakeUri

        val entry = mergeProxyMediaRegistration(
            existing = null,
            uri = FakeUri,
            mediaVersion = "clip|v4",
            width = 3840,
            height = 2160,
            cachedProxyUri = cachedProxy,
        )

        assertEquals(cachedProxy, entry.proxyUri)
        assertTrue(entry.proxyGenerated)
        assertFalse(entry.proxyGenerating)
    }

    @Test
    fun sameSourceRegistrationPreservesThumbnailAndInFlightState() {
        val existing = ProxyWorkflowEngine.MediaEntry(
            originalUri = FakeUri,
            mediaVersion = "clip|v4",
            thumbnailStripPath = "/cache/thumbs/clip.jpg",
            originalWidth = 1920,
            originalHeight = 1080,
            proxyGenerating = true,
        )

        val entry = mergeProxyMediaRegistration(
            existing = existing,
            uri = FakeUri,
            mediaVersion = "clip|v4",
            width = 3840,
            height = 2160,
            cachedProxyUri = null,
        )

        assertEquals("/cache/thumbs/clip.jpg", entry.thumbnailStripPath)
        assertTrue(entry.proxyGenerating)
    }

    @Test
    fun changedMediaVersionDoesNotReusePreviousProxyOrThumbnail() {
        val previousProxy = FakeUri
        val existing = ProxyWorkflowEngine.MediaEntry(
            originalUri = FakeUri,
            mediaVersion = "clip|v4",
            proxyUri = previousProxy,
            thumbnailStripPath = "/cache/thumbs/old.jpg",
            originalWidth = 3840,
            originalHeight = 2160,
            proxyGenerated = true,
        )

        val entry = mergeProxyMediaRegistration(
            existing = existing,
            uri = FakeUri,
            mediaVersion = "clip|v5",
            width = 3840,
            height = 2160,
            cachedProxyUri = null,
        )

        assertNull(entry.proxyUri)
        assertNull(entry.thumbnailStripPath)
        assertFalse(entry.proxyGenerated)
    }
}
