package com.directorx.editor.engine

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ProxyFileDeletePolicyTest {
    private lateinit var root: File

    @Before
    fun setUp() {
        root = Files.createTempDirectory("directorx-proxy-cache").toFile()
    }

    @After
    fun tearDown() {
        root.deleteRecursively()
    }

    @Test
    fun deletesOnlyARegularManagedProxyFile() {
        val proxy = File(root, "proxy_test123.mp4").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        val outcome = deleteManagedProxyFile(root, proxy)

        assertEquals(ProxyFileDeleteOutcome.DELETED, outcome)
        assertFalse(proxy.exists())
    }

    @Test
    fun reportsAnAlreadyMissingManagedProxySeparately() {
        val missing = File(root, "proxy_missing.mp4")

        assertEquals(ProxyFileDeleteOutcome.MISSING, deleteManagedProxyFile(root, missing))
    }

    @Test
    fun rejectsFilesOutsideTheProxyCache() {
        val outside = Files.createTempDirectory("directorx-outside-cache").toFile()
        try {
            val candidate = File(outside, "proxy_outside.mp4").apply { writeText("do not delete") }

            assertEquals(ProxyFileDeleteOutcome.INVALID, deleteManagedProxyFile(root, candidate))
            assertTrue(candidate.exists())
        } finally {
            outside.deleteRecursively()
        }
    }

    @Test
    fun rejectsUnmanagedNamesAndDirectories() {
        val unrelated = File(root, "source.mp4").apply { writeText("keep") }
        val directory = File(root, "proxy_directory.mp4").apply { mkdirs() }

        assertEquals(ProxyFileDeleteOutcome.INVALID, deleteManagedProxyFile(root, unrelated))
        assertEquals(ProxyFileDeleteOutcome.INVALID, deleteManagedProxyFile(root, directory))
        assertTrue(unrelated.exists())
        assertTrue(directory.isDirectory)
    }
}
