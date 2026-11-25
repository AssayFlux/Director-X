package com.directorx.editor.engine.ai

/**
 * Privacy-first hybrid AI routing.
 *
 * Local providers are preferred by default. Cloud execution requires a
 * provider-specific, purpose-specific, disclosure-version-specific consent
 * grant that has not expired, plus explicit cloud mode (or local-preferred
 * mode with no matching local provider) and network availability.
 *
 * There is deliberately no silent local-to-cloud fallback.
 */
enum class AiProcessingMode { ON_DEVICE_ONLY, CLOUD_ONLY, ON_DEVICE_PREFERRED }

data class CloudAiConsent(
    val providerId: String,
    val purposes: Set<AiFeaturePurpose>,
    val disclosureVersion: String,
    val grantedAtEpochMs: Long,
    val expiresAtEpochMs: Long,
) {
    init {
        require(providerId.isNotBlank()) { "Consent provider ID must not be blank." }
        require(purposes.isNotEmpty()) { "Consent must name at least one AI purpose." }
        require(disclosureVersion.isNotBlank()) { "Consent disclosure version must not be blank." }
        require(grantedAtEpochMs >= 0L) { "Consent grant time must be non-negative." }
        require(expiresAtEpochMs > grantedAtEpochMs) { "Consent must expire after it is granted." }
    }

    fun isValidFor(provider: CloudAiProvider, purpose: AiFeaturePurpose, nowEpochMs: Long): Boolean =
        providerId == provider.id &&
            purpose in purposes &&
            disclosureVersion == provider.disclosureVersion &&
            nowEpochMs >= grantedAtEpochMs &&
            nowEpochMs < expiresAtEpochMs
}

data class HybridAiPolicy(
    val mode: AiProcessingMode = AiProcessingMode.ON_DEVICE_ONLY,
    val cloudConsent: CloudAiConsent? = null,
    val networkAvailable: Boolean = false,
    val nowEpochMs: Long? = null,
)

interface CloudAiProvider {
    val id: String
    val displayName: String
    /** Increment whenever the user-facing data-use disclosure materially changes. */
    val disclosureVersion: String get() = "1"
    fun supports(purpose: AiFeaturePurpose): Boolean
    suspend fun execute(request: AiExecutionRequest): AiExecutionResult
}

class CloudAiConsentRequiredException :
    SecurityException("Cloud AI requires current consent for this provider and processing purpose.")

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
        require(cloudProviders.all { it.id.isNotBlank() && it.disclosureVersion.isNotBlank() }) {
            "Cloud provider IDs and disclosure versions must not be blank."
        }
    }

    suspend fun execute(
        request: AiExecutionRequest,
        policy: HybridAiPolicy,
    ): AiExecutionResult {
        val local = localProviders.firstOrNull { it.supports(request.purpose) }
        val cloud = cloudProviders.firstOrNull { it.supports(request.purpose) }

        return when (policy.mode) {
            AiProcessingMode.ON_DEVICE_ONLY -> localRouter.execute(request)
            AiProcessingMode.CLOUD_ONLY -> executeCloud(request, cloud, policy)
            AiProcessingMode.ON_DEVICE_PREFERRED -> {
                if (local != null) {
                    localRouter.execute(request)
                } else {
                    executeCloud(request, cloud, policy)
                }
            }
        }
    }

    private suspend fun executeCloud(
        request: AiExecutionRequest,
        provider: CloudAiProvider?,
        policy: HybridAiPolicy,
    ): AiExecutionResult {
        if (provider == null) throw HybridAiProviderUnavailableException(request.purpose)
        val consent = policy.cloudConsent
        if (consent == null || !consent.isValidFor(provider, request.purpose, policy.nowEpochMs ?: System.currentTimeMillis())) {
            throw CloudAiConsentRequiredException()
        }
        if (!policy.networkAvailable) throw CloudAiNetworkUnavailableException()
        return provider.execute(request)
    }
}
