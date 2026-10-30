package com.directorx.editor.engine.ai

/**
 * Privacy-first hybrid AI routing.
 *
 * Local providers are preferred by default. A cloud provider is considered only
 * when the user explicitly selects cloud processing, has granted the current
 * disclosure consent, and the caller confirms network availability.
 *
 * There is deliberately no silent local-to-cloud fallback.
 */
enum class AiProcessingMode { ON_DEVICE_ONLY, CLOUD_ONLY, ON_DEVICE_PREFERRED }

data class HybridAiPolicy(
    val mode: AiProcessingMode = AiProcessingMode.ON_DEVICE_ONLY,
    val cloudConsentGranted: Boolean = false,
    val networkAvailable: Boolean = false,
)

interface CloudAiProvider {
    val id: String
    val displayName: String
    fun supports(purpose: AiFeaturePurpose): Boolean
    suspend fun execute(request: AiExecutionRequest): AiExecutionResult
}

class CloudAiConsentRequiredException :
    SecurityException("Cloud AI requires explicit, current user consent.")

class CloudAiNetworkUnavailableException :
    IllegalStateException("Cloud AI is unavailable without a network connection.")

class HybridAiProviderUnavailableException(purpose: AiFeaturePurpose) :
    IllegalStateException("No permitted AI provider supports $purpose.")

class HybridAiRouter(
    private val localRouter: OnDeviceAiRouter,
    private val localProviders: List<OnDeviceAiProvider>,
    private val cloudProviders: List<CloudAiProvider>,
) {
    init {
        require(localProviders.map { it.id }.distinct().size == localProviders.size) {
            "Local AI provider IDs must be unique."
        }
        require(cloudProviders.map { it.id }.distinct().size == cloudProviders.size) {
            "Cloud AI provider IDs must be unique."
        }
    }

    suspend fun execute(
        request: AiExecutionRequest,
        policy: HybridAiPolicy,
    ): AiExecutionResult {
        val local = localProviders.firstOrNull { it.supports(request.purpose) }
        val cloudAllowed = policy.cloudConsentGranted && policy.networkAvailable
        val cloud = cloudProviders.firstOrNull { it.supports(request.purpose) }

        return when (policy.mode) {
            AiProcessingMode.ON_DEVICE_ONLY -> localRouter.execute(request)
            AiProcessingMode.CLOUD_ONLY -> {
                if (!policy.cloudConsentGranted) throw CloudAiConsentRequiredException()
                if (!policy.networkAvailable) throw CloudAiNetworkUnavailableException()
                cloud?.execute(request) ?: throw HybridAiProviderUnavailableException(request.purpose)
            }
            AiProcessingMode.ON_DEVICE_PREFERRED -> {
                if (local != null) localRouter.execute(request)
                else {
                    if (!policy.cloudConsentGranted) throw CloudAiConsentRequiredException()
                    if (!policy.networkAvailable) throw CloudAiNetworkUnavailableException()
                    cloud?.execute(request) ?: throw HybridAiProviderUnavailableException(request.purpose)
                }
            }
        }
    }
}
