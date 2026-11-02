package com.directorx.editor.engine

import android.content.Context
import android.net.Uri
import com.directorx.editor.model.ProxyResolution
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 3-tier proxy media management following DaVinci Resolve / Premiere Pro patterns.
 *
 * Tiers:
 * 1. Thumbnail (JPEG strips for timeline scrubbing) — generated on import
 * 2. Proxy (quarter-height H.264, 540p from 4K, for editing) — background-generated via WorkManager
 * 3. Original (full-res for export) — always used for final render
 *
 * Auto-switch: Use proxy during preview if original > 1080p. Always original for export.
 * Storage: Proxy files stored in app's cache dir with .proxy.mp4 suffix.
 *
 * Dependencies:
 *   - ProxyEngine (existing) for actual transcoding via Media3 Transformer
 *   - WorkManager (androidx.work:work-runtime-ktx:2.9.+) for background generation
 */
@Singleton
class ProxyWorkflowEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val proxyEngine: ProxyEngine,
    private val settingsRepository: SettingsRepository,
) {
    enum class MediaTier { THUMBNAIL, PROXY, ORIGINAL }

    data class ProxyBatchResult(
        val attempted: Int,
        val generated: Int,
        val skipped: Int,
        val failed: Int,
    ) {
        val isComplete: Boolean get() = failed == 0
    }

    data class ProxyDeleteResult(
        val deleted: Int,
        val failed: Int,
        val missing: Int,
    ) {
        val isComplete: Boolean get() = failed == 0
    }

    data class MediaEntry(
        val originalUri: Uri,
        val mediaVersion: String = originalUri.toString(),
        val proxyUri: Uri? = null,
        val thumbnailStripPath: String? = null,
        val originalWidth: Int = 0,
        val originalHeight: Int = 0,
        val proxyGenerated: Boolean = false,
        val proxyGenerating: Boolean = false
    )

    private val generationMutex = Mutex()

    private val _entries = MutableStateFlow<Map<String, MediaEntry>>(emptyMap())
    val entries: StateFlow<Map<String, MediaEntry>> = _entries

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    /**
     * Register a new media source. Starts thumbnail extraction immediately.
     */
    suspend fun registerMedia(
        clipId: String,
        uri: Uri,
        width: Int,
        height: Int,
        mediaVersion: String = uri.toString(),
    ) {
        _entries.update { current -> current + (clipId to MediaEntry(
            originalUri = uri,
            mediaVersion = mediaVersion,
            originalWidth = width,
            originalHeight = height
        )) }
    }

    /**
     * Get the best available URI for a clip based on current mode.
     * During preview: return proxy if available and original > 1080p
     * During export: always return original
     */
    fun getMediaUri(clipId: String, forExport: Boolean = false): Uri {
        val entry = _entries.value[clipId] ?: return Uri.EMPTY
        if (forExport) return entry.originalUri
        // Use proxy during preview if original is high-res and proxy exists
        if (entry.proxyGenerated && entry.proxyUri != null && ProxyResolutionPolicy.requiresProxy(entry.originalWidth, entry.originalHeight)) {
            return entry.proxyUri
        }
        return entry.originalUri
    }

    /**
     * Generate proxies for all registered media that needs them.
     * Should be called via WorkManager for background processing.
     */
    suspend fun generateAllProxies(
        onProgress: (Float) -> Unit = {}
    ): ProxyBatchResult = withContext(Dispatchers.IO) {
        generationMutex.withLock {
            _isGenerating.value = true
            try {
                val resolution = settingsRepository.settings.first().proxyResolution
                val needsProxy = _entries.value.filter {
                    !it.value.proxyGenerated && ProxyResolutionPolicy.requiresProxy(it.value.originalWidth, it.value.originalHeight)
                }
                if (needsProxy.isEmpty()) {
                    onProgress(1f)
                    return@withLock ProxyBatchResult(0, 0, 0, 0)
                }
                var completed = 0
                var generated = 0
                var skipped = 0
                var failed = 0

                for ((clipId, entry) in needsProxy) {
                    // Check cancellation before each potentially multi-minute proxy job so
                    // WorkManager can stop the worker promptly without force-killing the process.
                    ensureActive()
                    val expectedVersion = entry.mediaVersion
                    val currentEntry = _entries.value[clipId]
                    if (!shouldApplyProxyResult(expectedVersion, currentEntry?.mediaVersion)) {
                        skipped++
                        completed++
                        onProgress(completed.toFloat() / needsProxy.size)
                        continue
                    }
                    try {
                        _entries.update { current ->
                            val liveEntry = current[clipId]
                            if (liveEntry == null || !shouldApplyProxyResult(expectedVersion, liveEntry.mediaVersion)) {
                                current
                            } else {
                                current + (clipId to liveEntry.copy(proxyGenerating = true))
                            }
                        }

                        val plan = proxyEngine.planProxy(entry.originalUri, resolution)
                        val proxyUri = when (plan) {
                            is ProxyResolutionPolicy.Plan.UseSource -> null
                            is ProxyResolutionPolicy.Plan.Generate -> proxyEngine.generateProxy(
                                entry.originalUri,
                                resolution,
                                plan,
                                onProgress = { /* per-clip progress */ },
                                mediaVersion = entry.mediaVersion,
                            )
                        }

                        _entries.update { current ->
                            val liveEntry = current[clipId]
                            if (liveEntry == null || !shouldApplyProxyResult(expectedVersion, liveEntry.mediaVersion)) {
                                current
                            } else {
                                current + (clipId to liveEntry.copy(
                                    proxyUri = proxyUri,
                                    proxyGenerated = proxyUri != null,
                                    proxyGenerating = false
                                ))
                            }
                        }
                        when {
                            plan is ProxyResolutionPolicy.Plan.UseSource -> skipped++
                            proxyUri != null -> generated++
                            else -> failed++
                        }
                    } catch (e: CancellationException) {
                        _entries.update { current ->
                            val liveEntry = current[clipId]
                            if (liveEntry == null || !shouldApplyProxyResult(expectedVersion, liveEntry.mediaVersion)) {
                                current
                            } else {
                                current + (clipId to liveEntry.copy(proxyGenerating = false))
                            }
                        }
                        throw e
                    } catch (e: Exception) {
                        _entries.update { current ->
                            val liveEntry = current[clipId]
                            if (liveEntry == null || !shouldApplyProxyResult(expectedVersion, liveEntry.mediaVersion)) {
                                current
                            } else {
                                current + (clipId to liveEntry.copy(proxyGenerating = false))
                            }
                        }
                        failed++
                        AppLog.w("ProxyWorkflowEngine", "Proxy generation failed for clip $clipId", e)
                    }
                    completed++
                    onProgress(completed.toFloat() / needsProxy.size)
                }
                ProxyBatchResult(
                    attempted = needsProxy.size,
                    generated = generated,
                    skipped = skipped,
                    failed = failed,
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    /**
     * Delete proxies while retaining entries whose files could not be removed.
     */
    suspend fun deleteAllProxies(): ProxyDeleteResult = withContext(Dispatchers.IO) {
        var deleted = 0
        var failed = 0
        var missing = 0
        _entries.value.forEach { (clipId, entry) ->
            val uri = entry.proxyUri ?: return@forEach
            when (proxyEngine.deleteProxyUri(uri)) {
                ProxyFileDeleteOutcome.DELETED -> {
                    deleted++
                    clearProxyReference(clipId, uri)
                }
                ProxyFileDeleteOutcome.MISSING -> {
                    // The file was already removed (for example by cache pressure).
                    // Clear the stale URI rather than reporting a failed deletion.
                    missing++
                    clearProxyReference(clipId, uri)
                }
                ProxyFileDeleteOutcome.FAILED,
                ProxyFileDeleteOutcome.INVALID -> {
                    // Preserve the reference so the user can retry or inspect it.
                    failed++
                }
            }
        }
        ProxyDeleteResult(deleted = deleted, failed = failed, missing = missing)
    }

    private fun clearProxyReference(clipId: String, expectedUri: Uri) {
        _entries.update { current ->
            current[clipId]
                ?.takeIf { it.proxyUri == expectedUri }
                ?.let { live ->
                    current + (clipId to live.copy(
                        proxyUri = null,
                        proxyGenerated = false,
                        proxyGenerating = false,
                    ))
                } ?: current
        }
    }

    /**
     * Get total proxy storage usage in bytes.
     */
    suspend fun getProxyStorageBytes(): Long = withContext(Dispatchers.IO) {
        _entries.value.values.sumOf { entry ->
            entry.proxyUri?.let { proxyEngine.proxyFileLength(it) } ?: 0L
        }
    }
}
