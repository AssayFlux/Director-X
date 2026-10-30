package com.directorx.editor.engine.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AiProviderRouterTest {
    private class FakeProvider(
        override val id: String,
        private val supported: Set<AiFeaturePurpose>
    ) : OnDeviceAiProvider {
        override val displayName = id
        override fun supports(purpose: AiFeaturePurpose) = purpose in supported
        override suspend fun execute(request: AiExecutionRequest) =
            AiExecutionResult(id, byteArrayOf(9))
    }

    private class UsageRecorder : AiUsageRecorder {
        var count = 0
        var providerId: String? = null
        override suspend fun record(
            featureId: String,
            providerId: String,
            purpose: AiFeaturePurpose,
            inputBytes: Long,
            outputBytes: Long
        ) {
            count++
            this.providerId = providerId
        }
    }

    @Test fun routesToRegisteredLocalProviderAndRecordsUsage() = runBlocking {
        val usage = UsageRecorder()
        val router = OnDeviceAiRouter(
            listOf(FakeProvider("whisper-local", setOf(AiFeaturePurpose.TRANSCRIPTION))),
            usage
        )
        val result = router.execute(
            AiExecutionRequest("auto_captions", AiFeaturePurpose.TRANSCRIPTION, byteArrayOf(1, 2))
        )
        assertEquals("whisper-local", result.providerId)
        assertEquals("whisper-local", usage.providerId)
        assertEquals(1, usage.count)
    }

    @Test fun missingLocalCapabilityFailsWithoutFallback() = runBlocking {
        val usage = UsageRecorder()
        val router = OnDeviceAiRouter(
            listOf(FakeProvider("whisper-local", setOf(AiFeaturePurpose.TRANSCRIPTION))),
            usage
        )
        assertThrows(AiProviderUnavailableException::class.java) {
            runBlocking {
                router.execute(
                    AiExecutionRequest("video_upscale", AiFeaturePurpose.VIDEO_ENHANCEMENT, byteArrayOf(1))
                )
            }
        }
        assertEquals(0, usage.count)
    }

    @Test fun duplicateProviderIdsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            OnDeviceAiRouter(
                listOf(
                    FakeProvider("duplicate", emptySet()),
                    FakeProvider("duplicate", emptySet())
                ),
                UsageRecorder()
            )
        }
    }
}
