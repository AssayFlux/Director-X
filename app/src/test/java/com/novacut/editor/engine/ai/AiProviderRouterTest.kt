package com.novacut.editor.engine.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AiProviderRouterTest {
    private class FakeProvider(
        override val id: String,
        override val isOnDevice: Boolean,
        private val supported: Set<AiFeaturePurpose>
    ) : AiProvider {
        override val displayName: String = id
        override fun supports(purpose: AiFeaturePurpose): Boolean = purpose in supported
        override suspend fun execute(request: AiExecutionRequest) = AiExecutionResult(
            providerId = id,
            output = byteArrayOf(7),
            executedOnDevice = isOnDevice
        )
    }

    private class RecordingUsage : AiUsageRecorder {
        var calls = 0
        var provider: String? = null
        override suspend fun record(
            featureId: String,
            providerId: String,
            purpose: AiFeaturePurpose,
            executedOnDevice: Boolean,
            inputBytes: Long,
            outputBytes: Long
        ) {
            calls++
            provider = providerId
        }
    }

    private val purpose = AiFeaturePurpose.TRANSCRIPTION
    private val disclosure = AiDataDisclosure(
        providerName = "Example Cloud",
        destination = "https://example.invalid",
        dataDescription = "Selected audio for transcription",
        estimatedUploadBytes = 1024,
        retentionDescription = "Deleted after processing"
    )
    private val fingerprint = AiConsentFingerprint { "fingerprint-1" }

    private fun request(
        mode: AiExecutionMode = AiExecutionMode.ON_DEVICE_PREFERRED,
        allowCloud: Boolean = false,
        consent: AiCloudConsent? = null
    ) = AiExecutionRequest(
        featureId = "auto_captions",
        purpose = purpose,
        payload = byteArrayOf(1, 2, 3),
        mode = mode,
        allowCloud = allowCloud,
        disclosure = disclosure.takeIf { allowCloud },
        cloudConsent = consent
    )

    @Test fun onDeviceProviderIsPreferredWithoutCloudConsent() = runBlocking {
        val usage = RecordingUsage()
        val router = AiProviderRouter(
            listOf(
                FakeProvider("cloud", false, setOf(purpose)),
                FakeProvider("local", true, setOf(purpose))
            ),
            fingerprint,
            usage
        )

        val result = router.execute(request())

        assertEquals("local", result.providerId)
        assertTrue(result.executedOnDevice)
        assertEquals("local", usage.provider)
        assertEquals(1, usage.calls)
    }

    @Test fun cloudFallbackIsDeniedWhenCloudWasNotEnabled() = runBlocking {
        val usage = RecordingUsage()
        val router = AiProviderRouter(
            listOf(FakeProvider("cloud", false, setOf(purpose))),
            fingerprint,
            usage
        )

        assertThrows(AiRoutingException.CloudNotAllowed::class.java) {
            runBlocking { router.execute(request()) }
        }
        assertEquals(0, usage.calls)
    }

    @Test fun cloudExecutionRequiresExplicitMatchingConsent() = runBlocking {
        val usage = RecordingUsage()
        val router = AiProviderRouter(
            listOf(FakeProvider("cloud", false, setOf(purpose))),
            fingerprint,
            usage
        )

        assertThrows(AiRoutingException.ConsentRequired::class.java) {
            runBlocking {
                router.execute(
                    request(
                        mode = AiExecutionMode.CLOUD_ONLY,
                        allowCloud = true,
                        consent = AiCloudConsent("other-provider", purpose, "fingerprint-1", 1)
                    )
                )
            }
        }
        assertEquals(0, usage.calls)
    }

    @Test fun cloudExecutionSucceedsWithMatchingConsentAndRecordsUsage() = runBlocking {
        val usage = RecordingUsage()
        val router = AiProviderRouter(
            listOf(FakeProvider("cloud", false, setOf(purpose))),
            fingerprint,
            usage
        )

        val result = router.execute(
            request(
                mode = AiExecutionMode.CLOUD_ONLY,
                allowCloud = true,
                consent = AiCloudConsent("cloud", purpose, "fingerprint-1", 1)
            )
        )

        assertEquals("cloud", result.providerId)
        assertFalse(result.executedOnDevice)
        assertEquals("cloud", usage.provider)
        assertEquals(1, usage.calls)
    }

    @Test fun onDeviceOnlyNeverUsesCloud() = runBlocking {
        val usage = RecordingUsage()
        val router = AiProviderRouter(
            listOf(FakeProvider("cloud", false, setOf(purpose))),
            fingerprint,
            usage
        )

        assertThrows(AiRoutingException.NoCapableProvider::class.java) {
            runBlocking {
                router.execute(
                    request(
                        mode = AiExecutionMode.ON_DEVICE_ONLY,
                        allowCloud = true,
                        consent = AiCloudConsent("cloud", purpose, "fingerprint-1", 1)
                    )
                )
            }
        }
        assertEquals(0, usage.calls)
    }
}
