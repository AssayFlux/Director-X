package com.novacut.editor.engine.ai

/**
 * Shared execution boundary for AI features.
 *
 * The router prefers a capable on-device provider. Cloud execution is never an
 * automatic fallback: it requires a configured provider and a consent receipt
 * that matches the provider, feature purpose, and disclosure shown to the user.
 */
enum class AiExecutionMode { ON_DEVICE_ONLY, ON_DEVICE_PREFERRED, CLOUD_ONLY }

enum class AiFeaturePurpose {
    TRANSCRIPTION,
    CAPTION_TRANSLATION,
    AUDIO_CLEANUP,
    VIDEO_ENHANCEMENT,
    EDITING_SUGGESTIONS,
    GENERATIVE_MEDIA
}

data class AiDataDisclosure(
    val providerName: String,
    val destination: String,
    val dataDescription: String,
    val estimatedUploadBytes: Long,
    val retentionDescription: String
) {
    init {
        require(providerName.isNotBlank())
        require(destination.isNotBlank())
        require(dataDescription.isNotBlank())
        require(estimatedUploadBytes >= 0)
        require(retentionDescription.isNotBlank())
    }
}

data class AiCloudConsent(
    val providerId: String,
    val purpose: AiFeaturePurpose,
    val disclosureFingerprint: String,
    val grantedAtEpochMillis: Long
) {
    init {
        require(providerId.isNotBlank())
        require(disclosureFingerprint.isNotBlank())
        require(grantedAtEpochMillis > 0)
    }
}

data class AiExecutionRequest(
    val featureId: String,
    val purpose: AiFeaturePurpose,
    val payload: ByteArray,
    val mode: AiExecutionMode = AiExecutionMode.ON_DEVICE_PREFERRED,
    val allowCloud: Boolean = false,
    val disclosure: AiDataDisclosure? = null,
    val cloudConsent: AiCloudConsent? = null
) {
    init {
        require(featureId.isNotBlank())
    }
}

data class AiExecutionResult(
    val providerId: String,
    val output: ByteArray,
    val executedOnDevice: Boolean
)

interface AiProvider {
    val id: String
    val displayName: String
    val isOnDevice: Boolean
    fun supports(purpose: AiFeaturePurpose): Boolean
    suspend fun execute(request: AiExecutionRequest): AiExecutionResult
}

fun interface AiConsentFingerprint {
    fun create(disclosure: AiDataDisclosure): String
}

interface AiUsageRecorder {
    suspend fun record(
        featureId: String,
        providerId: String,
        purpose: AiFeaturePurpose,
        executedOnDevice: Boolean,
        inputBytes: Long,
        outputBytes: Long
    )
}

sealed class AiRoutingException(message: String) : IllegalStateException(message) {
    class NoCapableProvider : AiRoutingException("No configured AI provider supports this feature.")
    class CloudNotAllowed : AiRoutingException("Cloud AI is not allowed for this request.")
    class ConsentRequired : AiRoutingException("Explicit, matching cloud consent is required.")
    class InvalidDisclosure : AiRoutingException("Cloud data disclosure is missing or does not match consent.")
}

/**
 * No implicit network fallback is possible: the cloud provider is considered
 * only after local capability lookup and only when request-level opt-in,
 * disclosure, and matching consent are all present.
 */
class AiProviderRouter(
    providers: List<AiProvider>,
    private val fingerprint: AiConsentFingerprint,
    private val usageRecorder: AiUsageRecorder
) {
    private val providers = providers.toList()

    init {
        require(this.providers.map { it.id }.distinct().size == this.providers.size) {
            "AI provider IDs must be unique."
        }
    }

    suspend fun execute(request: AiExecutionRequest): AiExecutionResult {
        val local = providers.firstOrNull { it.isOnDevice && it.supports(request.purpose) }
        val cloud = providers.firstOrNull { !it.isOnDevice && it.supports(request.purpose) }

        val selected = when (request.mode) {
            AiExecutionMode.ON_DEVICE_ONLY -> local ?: throw AiRoutingException.NoCapableProvider()
            AiExecutionMode.ON_DEVICE_PREFERRED -> local ?: authorizedCloud(request, cloud)
            AiExecutionMode.CLOUD_ONLY -> authorizedCloud(request, cloud)
        }

        val result = selected.execute(request)
        usageRecorder.record(
            featureId = request.featureId,
            providerId = result.providerId,
            purpose = request.purpose,
            executedOnDevice = result.executedOnDevice,
            inputBytes = request.payload.size.toLong(),
            outputBytes = result.output.size.toLong()
        )
        return result
    }

    private fun authorizedCloud(
        request: AiExecutionRequest,
        provider: AiProvider?
    ): AiProvider {
        if (!request.allowCloud) throw AiRoutingException.CloudNotAllowed()
        val cloudProvider = provider ?: throw AiRoutingException.NoCapableProvider()
        val disclosure = request.disclosure ?: throw AiRoutingException.InvalidDisclosure()
        val consent = request.cloudConsent ?: throw AiRoutingException.ConsentRequired()
        if (consent.providerId != cloudProvider.id ||
            consent.purpose != request.purpose ||
            consent.disclosureFingerprint != fingerprint.create(disclosure)
        ) {
            throw AiRoutingException.ConsentRequired()
        }
        return cloudProvider
    }
}
