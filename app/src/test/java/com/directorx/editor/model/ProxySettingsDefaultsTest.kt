package com.directorx.editor.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProxySettingsDefaultsTest {
    @Test
    fun newProjectsEnableAutomaticHighResolutionProxyEditing() {
        val settings = ProxySettings()

        assertTrue(settings.enabled)
        assertTrue(settings.autoGenerate)
        assertEquals(ProxyResolution.QUARTER, settings.resolution)
    }
}
