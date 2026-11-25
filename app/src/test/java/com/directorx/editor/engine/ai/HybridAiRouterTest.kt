package com.directorx.editor.engine.ai

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

    private class Cloud(
        override val id: String,
        private val purposes: Set<AiFeaturePurpose>,
        override val disclosureVersion: String = "2026-01",
    ) : CloudAiProvider {
        override val displayName = id
        override fun supports(purpose: AiFeaturePurpose) = purpose in purposes
        override suspend fun execute(request: AiExecutionRequest) = AiExecutionResult(id, byteArrayOf(2))
    }

    private class Recorder : AiUsageRecorder {
        override suspend fun record(
            featureId: String,
            providerId: String,
            purpose: AiFeaturePurpose,
            inputBytes: Long,
            outputBytes: Long,
        ) = Unit
    }

    private fun consent(
        providerId: String = "cloud",
        purposes: Set<AiFeaturePurpose> = setOf(AiFeaturePurpose.TRANSCRIPTION),
        disclosureVersion: String = "2026-01",
        grantedAt: Long = 100L,
        expiresAt: Long = 500L,
    ) = CloudAiConsent(providerId, purposes, disclosureVersion, grantedAt, expiresAt)

    private fun router(
        local: List<OnDeviceAiProvider> = emptyList(),
        cloud: List<CloudAiProvider> = listOf(Cloud("cloud", setOf(AiFeaturePurpose.TRANSCRIPTION))),
    ) = HybridAiRouter(OnDeviceAiRouter(local, Recorder()), local, cloud)

    private fun request() = AiExecutionRequest("captions", AiFeaturePurpose.TRANSCRIPTION, byteArrayOf(1))

    @Test
    fun localProviderIsPreferredWithoutCloudConsent() = runBlocking {
        val local = Local("local", setOf(AiFeaturePurpose.TRANSCRIPTION))
        assertEquals(
            "local",
            router(local = listOf(local)).execute(
                request(),
                HybridAiPolicy(mode = AiProcessingMode.ON_DEVICE_PREFERRED, nowEpochMs = 200L),
            ).providerId,
        )
    }

    @Test
    fun cloudOnlyRequiresExplicitConsent() {
        assertThrows(CloudAiConsentRequiredException::class.java) {
            runBlocking {
                router().execute(
                    request(),
                    HybridAiPolicy(mode = AiProcessingMode.CLOUD_ONLY, networkAvailable = true, nowEpochMs = 200L),
                )
            }
        }
    }

    @Test
    fun cloudOnlyRequiresNetworkAfterValidConsent() {
        assertThrows(CloudAiNetworkUnavailableException::class.java) {
            runBlocking {
                router().execute(
                    request(),
                    HybridAiPolicy(mode = AiProcessingMode.CLOUD_ONLY, cloudConsent = consent(), nowEpochMs = 200L),
                )
            }
        }
    }

    @Test
    fun cloudRequiresConsentForTheSelectedProvider() {
        assertThrows(CloudAiConsentRequiredException::class.java) {
            runBlocking {
                router().execute(
                    request(),
                    HybridAiPolicy(
                        mode = AiProcessingMode.CLOUD_ONLY,
                        cloudConsent = consent(providerId = "different-provider"),
                        networkAvailable = true,
                        nowEpochMs = 200L,
                    ),
                )
            }
        }
    }

    @Test
    fun cloudRequiresConsentForTheExactPurpose() {
        assertThrows(CloudAiConsentRequiredException::class.java) {
            runBlocking {
                router().execute(
                    request(),
                    HybridAiPolicy(
                        mode = AiProcessingMode.CLOUD_ONLY,
                        cloudConsent = consent(purposes = setOf(AiFeaturePurpose.GENERATIVE_MEDIA)),
                        networkAvailable = true,
                        nowEpochMs = 200L,
                    ),
                )
            }
        }
    }

    @Test
    fun cloudRequiresCurrentDisclosureVersion() {
        assertThrows(CloudAiConsentRequiredException::class.java) {
            runBlocking {
                router().execute(
                    request(),
                    HybridAiPolicy(
                        mode = AiProcessingMode.CLOUD_ONLY,
                        cloudConsent = consent(disclosureVersion = "old-version"),
                        networkAvailable = true,
                        nowEpochMs = 200L,
                    ),
                )
            }
        }
    }

    @Test
    fun cloudConsentExpiresAndCannotBeUsedBeforeGrantTime() {
        val r = router()
        assertThrows(CloudAiConsentRequiredException::class.java) {
            runBlocking {
                r.execute(
                    request(),
                    HybridAiPolicy(
                        mode = AiProcessingMode.CLOUD_ONLY,
                        cloudConsent = consent(),
                        networkAvailable = true,
                        nowEpochMs = 500L,
                    ),
                )
            }
        }
        assertThrows(CloudAiConsentRequiredException::class.java) {
            runBlocking {
                r.execute(
                    request(),
                    HybridAiPolicy(
                        mode = AiProcessingMode.CLOUD_ONLY,
                        cloudConsent = consent(),
                        networkAvailable = true,
                        nowEpochMs = 99L,
                    ),
                )
            }
        }
    }

    @Test
    fun cloudIsUsedOnlyWhenLocalCapabilityIsMissingAndConsentExists() = runBlocking {
        assertEquals(
            "cloud",
            router().execute(
                request(),
                HybridAiPolicy(
                    AiProcessingMode.ON_DEVICE_PREFERRED,
                    cloudConsent = consent(),
                    networkAvailable = true,
                    nowEpochMs = 200L,
                ),
            ).providerId,
        )
    }

    @Test
    fun noCloudProviderIsReportedAsUnavailableBeforeConsent() {
        assertThrows(HybridAiProviderUnavailableException::class.java) {
            runBlocking {
                router(cloud = emptyList()).execute(
                    request(),
                    HybridAiPolicy(
                        AiProcessingMode.CLOUD_ONLY,
                        cloudConsent = consent(),
                        networkAvailable = true,
                        nowEpochMs = 200L,
                    ),
                )
            }
        }
    }
}
