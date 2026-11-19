package com.directorx.editor.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenSourceLicensesTest {
    @Test
    fun registryIsNotEmptyAndEveryNoticeHasRequiredDisplayFields() {
        assertTrue(OpenSourceLicenses.notices.isNotEmpty())

        OpenSourceLicenses.notices.forEach { notice ->
            assertFalse(notice.name.isBlank())
            assertFalse(notice.version.isBlank())
            assertFalse(notice.artifact.isBlank())
            assertFalse(notice.licenseName.isBlank())
            assertFalse(notice.licenseText.isBlank())
            assertFalse(notice.licenseUrl.isBlank())
            assertFalse(notice.projectUrl.isBlank())
        }
    }

    @Test
    fun registryContainsReleaseNoticeObligationDependencies() {
        assertNotNull(OpenSourceLicenses.noticeForArtifact("io.github.kaleyravideo:android-deepfilternet"))
    }

    @Test
    fun noticeVersionsComeFromTheCapabilityRegistry() {
        CapabilityRegistry.notices.forEach { curated ->
            val current = OpenSourceLicenses.noticeForArtifact(curated.artifact)
            assertNotNull(current)
            assertEquals(curated.version, current!!.version)
        }
        assertEquals("5.5.0", OpenSourceLicenses.noticeForArtifact("com.squareup.okhttp3:okhttp")?.version)
        assertEquals("3.5.0", OpenSourceLicenses.noticeForArtifact("io.coil-kt.coil3:coil-compose")?.version)
        assertEquals("1.0.0", OpenSourceLicenses.noticeForArtifact("com.google.mediapipe:tasks-vision")?.version)
        assertEquals("2.60.1", OpenSourceLicenses.noticeForArtifact("com.google.dagger:hilt-android")?.version)
    }

    @Test
    fun generatedInventoryCoversResolvedRuntimeComponentsWithoutVendoredFfmpeg() {
        val generatedArtifacts = RuntimeOpenSourceLicensesGenerated.notices.map { it.artifact }

        assertEquals(generatedArtifacts.size, generatedArtifacts.distinct().size)
        assertTrue(RuntimeOpenSourceLicensesGenerated.notices.none { it.artifact.startsWith("native:") })
        assertTrue(RuntimeOpenSourceLicensesGenerated.notices.count { !it.artifact.startsWith("native:") } >= 200)
        RuntimeOpenSourceLicensesGenerated.notices.forEach { generated ->
            assertNotNull(OpenSourceLicenses.noticeForArtifact(generated.artifact))
        }
    }

    @Test
    fun capabilityRegistrySeparatesReachabilityFromPublicStatus() {
        val objectRemoval = requireNotNull(CapabilityRegistry.capabilityFor("object_removal"))

        assertEquals("available", objectRemoval.status)
        assertEquals("model_gated", objectRemoval.reachability)
        assertEquals("available", requireNotNull(CapabilityRegistry.capabilityFor("audio_denoise")).status)
    }

    @Test
    fun requiredApacheDependenciesAreMarkedApacheLicensed() {
        val apacheArtifacts = listOf(
            "com.google.mediapipe:tasks-vision",
            "com.airbnb.android:lottie-compose",
            "com.squareup.okhttp3:okhttp",
            "io.github.kaleyravideo:android-deepfilternet",
        )

        apacheArtifacts.forEach { artifact ->
            val notice = OpenSourceLicenses.noticeForArtifact(artifact)
            assertNotNull(notice)
            assertTrue(notice!!.licenseName.contains("Apache", ignoreCase = true))
        }
    }

    @Test
    fun noVendoredFfmpegSourceOfferRemains() {
        assertTrue(OpenSourceLicenses.dependenciesWithSourceOffers().isEmpty())
    }
}
