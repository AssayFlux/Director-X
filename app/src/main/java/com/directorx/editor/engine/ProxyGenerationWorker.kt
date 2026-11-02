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
            AppLog.d(TAG, "Starting proxy generation for all registered media")

            val batch = proxyWorkflowEngine.generateAllProxies { progress ->
                setProgressAsync(workDataOf(KEY_PROGRESS to progress))
            }

            val storageBytes = proxyWorkflowEngine.getProxyStorageBytes()
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
        const val KEY_PROGRESS = "progress"
        const val KEY_STORAGE_BYTES = "storage_bytes"
        const val KEY_ATTEMPTED = "attempted"
        const val KEY_GENERATED = "generated"
        const val KEY_SKIPPED = "skipped"
        const val KEY_FAILED = "failed"
        const val KEY_ERROR = "error"
        private const val MAX_RETRIES = 2
    }
}
