package com.novacut.editor.engine.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HybridAiRouterTest {
    private class Local(override val id: String, private val purposes: Set<AiFeaturePurpose>) : OnDeviceAiProvider {
        override val displayName = id
        override fun supports(purpose: AiFeaturePurpose) = purpose in purposes
        override suspend fun execute(request: AiExecutionRequest) = AiExecutionResult(id, byteArrayOf(1))
    }
    private class Cloud(override val id: String, private val purposes: Set<AiFeaturePurpose>) : CloudAiProvider {
        override val displayName = id
        override fun supports(purpose: AiFeaturePurpose) = purpose in purposes
        override suspend fun execute(request: AiExecutionRequest) = AiExecutionResult(id, byteArrayOf(2))
    }
    private class Recorder : AiUsageRecorder {
        override suspend fun record(featureId: String, providerId: String, purpose: AiFeaturePurpose, inputBytes: Long, outputBytes: Long) = Unit
    }

    @Test fun localProviderIsPreferredWithoutCloudConsent() = runBlocking {
        val local = Local("local", setOf(AiFeaturePurpose.TRANSCRIPTION))
        val router = HybridAiRouter(OnDeviceAiRouter(listOf(local), Recorder()), listOf(local),
            listOf(Cloud("cloud", setOf(AiFeaturePurpose.TRANSCRIPTION))))
        assertEquals("local", router.execute(
            AiExecutionRequest("captions", AiFeaturePurpose.TRANSCRIPTION, byteArrayOf(1)),
            HybridAiPolicy(mode = AiProcessingMode.ON_DEVICE_PREFERRED)
        ).providerId)
    }

    @Test fun cloudOnlyRequiresExplicitConsent() {
        val router = HybridAiRouter(OnDeviceAiRouter(emptyList(), Recorder()), emptyList(),
            listOf(Cloud("cloud", setOf(AiFeaturePurpose.TRANSCRIPTION))))
        assertThrows(CloudAiConsentRequiredException::class.java) {
            runBlocking { router.execute(AiExecutionRequest("captions", AiFeaturePurpose.TRANSCRIPTION, byteArrayOf()),
                HybridAiPolicy(mode = AiProcessingMode.CLOUD_ONLY, networkAvailable = true)) }
        }
    }

    @Test fun cloudOnlyRequiresNetworkAfterConsent() {
        val router = HybridAiRouter(OnDeviceAiRouter(emptyList(), Recorder()), emptyList(),
            listOf(Cloud("cloud", setOf(AiFeaturePurpose.TRANSCRIPTION))))
        assertThrows(CloudAiNetworkUnavailableException::class.java) {
            runBlocking { router.execute(AiExecutionRequest("captions", AiFeaturePurpose.TRANSCRIPTION, byteArrayOf()),
                HybridAiPolicy(mode = AiProcessingMode.CLOUD_ONLY, cloudConsentGranted = true)) }
        }
    }

    @Test fun cloudIsUsedOnlyWhenLocalCapabilityIsMissingAndConsentExists() = runBlocking {
        val router = HybridAiRouter(OnDeviceAiRouter(emptyList(), Recorder()), emptyList(),
            listOf(Cloud("cloud", setOf(AiFeaturePurpose.TRANSCRIPTION))))
        assertEquals("cloud", router.execute(AiExecutionRequest("captions", AiFeaturePurpose.TRANSCRIPTION, byteArrayOf()),
            HybridAiPolicy(AiProcessingMode.ON_DEVICE_PREFERRED, cloudConsentGranted = true, networkAvailable = true)).providerId)
    }
}
