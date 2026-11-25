package com.directorx.editor.engine

import android.content.Context
import com.directorx.editor.engine.AppLog
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.CancellationException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker for background proxy generation.
 *
 * Generates proxies at the user's saved proxy resolution for registered high-resolution media.
 * Reports progress via setProgress() so the UI
 * can display a progress indicator.
 *
 * Enqueued by EditorViewModel when proxy editing is enabled and
 * new high-res clips are imported.
 *
 * Dependencies:
 *   - androidx.work:work-runtime-ktx (Tier 4, now activated)
 *   - ProxyWorkflowEngine (@Singleton, injected via Hilt)
 */
@HiltWorker
class ProxyGenerationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val proxyWorkflowEngine: ProxyWorkflowEngine
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // WorkManager can recreate this worker in a fresh process. Never rely on
            // ProxyWorkflowEngine's in-memory registry being populated by an editor VM.
            val clipId = inputData.getString(KEY_CLIP_ID)?.takeIf(String::isNotBlank)
                ?: return Result.failure(workDataOf(KEY_ERROR to "Missing clip ID"))
            val sourceUri = inputData.getString(KEY_SOURCE_URI)?.takeIf(String::isNotBlank)
                ?: return Result.failure(workDataOf(KEY_ERROR to "Missing source URI"))
            val width = inputData.getInt(KEY_WIDTH, 0)
            val height = inputData.getInt(KEY_HEIGHT, 0)
            val mediaVersion = inputData.getString(KEY_MEDIA_VERSION)?.takeIf(String::isNotBlank)
                ?: sourceUri
            if (width <= 0 || height <= 0) {
                return Result.failure(workDataOf(KEY_ERROR to "Invalid source dimensions"))
            }

            AppLog.d(TAG, "Starting durable proxy generation for clip $clipId")

            // Process this request in isolation. Registering an old WorkManager
            // input under the live clip ID could overwrite a newer relinked source.
            val batch = proxyWorkflowEngine.generateProxyForJob(
                clipId = clipId,
                uri = android.net.Uri.parse(sourceUri),
                width = width,
                height = height,
                mediaVersion = mediaVersion,
                onProgress = { progress ->
                    setProgressAsync(workDataOf(KEY_PROGRESS to progress))
                },
            )

            val storageBytes = batch.storageBytes
            AppLog.d(
                TAG,
                "Proxy batch finished: attempted=${batch.attempted}, generated=${batch.generated}, " +
                    "skipped=${batch.skipped}, failed=${batch.failed}; storage=${storageBytes / 1024}KB"
            )
            val output = workDataOf(
                KEY_PROGRESS to 1f,
                KEY_STORAGE_BYTES to storageBytes,
                KEY_ATTEMPTED to batch.attempted,
                KEY_GENERATED to batch.generated,
                KEY_SKIPPED to batch.skipped,
                KEY_FAILED to batch.failed,
            )
            when {
                batch.failed == 0 -> Result.success(output)
                runAttemptCount < MAX_RETRIES -> Result.retry()
                else -> Result.failure(output)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e(TAG, "Proxy generation failed", e)
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Unknown error")))
            }
        }
    }

    companion object {
        const val TAG = "ProxyGeneration"
        const val WORK_NAME = "proxy_generation"
        const val KEY_CLIP_ID = "clip_id"
        const val KEY_SOURCE_URI = "source_uri"
        const val KEY_WIDTH = "source_width"
        const val KEY_HEIGHT = "source_height"
        const val KEY_MEDIA_VERSION = "media_version"
        const val KEY_PROGRESS = "progress"

        fun uniqueWorkName(clipId: String, mediaVersion: String): String {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(mediaVersion.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
                .take(16)
            return "$WORK_NAME:$clipId:$digest"
        }
        const val KEY_STORAGE_BYTES = "storage_bytes"
        const val KEY_ATTEMPTED = "attempted"
        const val KEY_GENERATED = "generated"
        const val KEY_SKIPPED = "skipped"
        const val KEY_FAILED = "failed"
        const val KEY_ERROR = "error"
        private const val MAX_RETRIES = 2
    }
}
