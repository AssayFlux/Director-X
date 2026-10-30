package com.directorx.editor.engine.ai

/**
 * On-device AI execution boundary for Director X.
 *
 * Existing model engines (Whisper/ONNX, MediaPipe, audio cleanup, enhancement,
 * and other local implementations) can be registered behind this contract.
 * This router never performs network access or falls back to a remote service.
 */
enum class AiFeaturePurpose {
    TRANSCRIPTION,
    CAPTION_TRANSLATION,
    AUDIO_CLEANUP,
    VIDEO_ENHANCEMENT,
    EDITING_SUGGESTIONS,
    GENERATIVE_MEDIA
}

data class AiExecutionRequest(
    val featureId: String,
    val purpose: AiFeaturePurpose,
    val payload: ByteArray
) {
    init { require(featureId.isNotBlank()) }
}

data class AiExecutionResult(
    val providerId: String,
    val output: ByteArray
)

interface OnDeviceAiProvider {
    val id: String
    val displayName: String
    fun supports(purpose: AiFeaturePurpose): Boolean
    suspend fun execute(request: AiExecutionRequest): AiExecutionResult
}

interface AiUsageRecorder {
    suspend fun record(
        featureId: String,
        providerId: String,
        purpose: AiFeaturePurpose,
        inputBytes: Long,
        outputBytes: Long
    )
}

class AiProviderUnavailableException(purpose: AiFeaturePurpose) :
    IllegalStateException("No installed on-device AI provider supports $purpose.")

class OnDeviceAiRouter(
    providers: List<OnDeviceAiProvider>,
    private val usageRecorder: AiUsageRecorder
) {
    private val providers = providers.toList()

    init {
        require(this.providers.map { it.id }.distinct().size == this.providers.size) {
            "AI provider IDs must be unique."
        }
    }

    suspend fun execute(request: AiExecutionRequest): AiExecutionResult {
        val provider = providers.firstOrNull { it.supports(request.purpose) }
            ?: throw AiProviderUnavailableException(request.purpose)
        val result = provider.execute(request)
        usageRecorder.record(
            featureId = request.featureId,
            providerId = result.providerId,
            purpose = request.purpose,
            inputBytes = request.payload.size.toLong(),
            outputBytes = result.output.size.toLong()
        )
        return result
    }
}
