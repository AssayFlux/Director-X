package com.directorx.editor.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SubtitleLanguageRegistryTest {
    @Test
    fun registryContainsExactlyThe36ProductTargets() {
        assertEquals(36, SubtitleLanguageRegistry.all.size)
        assertEquals(36, SubtitleLanguageRegistry.all.map { it.tag }.distinct().size)
        assertEquals(12, SubtitleLanguageRegistry.byPriority(SubtitleLanguageRegistry.Priority.P0).size)
        assertEquals(24, SubtitleLanguageRegistry.byPriority(SubtitleLanguageRegistry.Priority.P1).size)
    }

    @Test
    fun rtlTargetsAreExplicit() {
        assertEquals(
            setOf("ar", "ur", "fa", "he"),
            SubtitleLanguageRegistry.rightToLeft().map { it.tag }.toSet(),
        )
    }

    @Test
    fun registryDoesNotOverstateRuntimeAvailability() {
        assertEquals(
            SubtitleLanguageRegistry.AsrAvailability.ACTIVE_ENGLISH_ONLY,
            SubtitleLanguageRegistry.find("en")?.asrAvailability,
        )
        assertTrue(
            SubtitleLanguageRegistry.all.filter { it.tag != "en" }
                .all { it.asrAvailability == SubtitleLanguageRegistry.AsrAvailability.NOT_INTEGRATED },
        )
        assertTrue(
            SubtitleLanguageRegistry.all.all {
                it.translationAvailability == SubtitleLanguageRegistry.TranslationAvailability.NOT_INTEGRATED
            },
        )
    }

    @Test
    fun candidateModelCodesAreNotRuntimeAvailability() {
        assertEquals("zh", SubtitleLanguageRegistry.find("zh-Hans")?.whisperCandidateCode)
        assertEquals("zh", SubtitleLanguageRegistry.find("zh-Hant")?.whisperCandidateCode)
        assertNull(SubtitleLanguageRegistry.find("mr")?.whisperCandidateCode)
        assertNotNull(SubtitleLanguageRegistry.find("en"))
        assertFalse(SubtitleLanguageRegistry.find("EN") != null)
    }
}
