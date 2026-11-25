package com.directorx.editor.ui.editor

import android.graphics.Bitmap
import android.net.Uri
import com.directorx.editor.engine.MixedRenderComposer
import com.directorx.editor.engine.ExportFailureCause
import com.directorx.editor.engine.billing.SubscriptionTier
import com.directorx.editor.engine.VideoEngine
import com.directorx.editor.model.ExportConfig
import com.directorx.editor.model.GlobalTransition
import com.directorx.editor.model.ImageOverlay
import com.directorx.editor.engine.LottieOverlaySpec
import com.directorx.editor.model.TextOverlay
import com.directorx.editor.model.Track
import com.directorx.editor.model.TrackedObject
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Export-facing media boundary. Export orchestration depends on this contract,
 * not on the preview/player implementation or the concrete Media3 engine.
 */
interface VideoExportPort {
    val exportFailureCause: StateFlow<ExportFailureCause?>

    fun getVideoResolution(uri: Uri): Pair<Int, Int>
    fun hasAudioTrack(uri: Uri): Boolean
    fun extractThumbnail(uri: Uri, timeUs: Long, width: Int = 160, height: Int = 90): Bitmap?
    fun isReverseRenderAvailable(): Boolean
    fun cancelExport(preservePartial: Boolean = false): File?
    fun resetExportState()

    suspend fun export(
        tracks: List<Track>,
        config: ExportConfig,
        outputFile: File,
        timelineDurationMsOverride: Long? = null,
        resumeFromFile: File? = null,
        textOverlays: List<TextOverlay> = emptyList(),
        imageOverlays: List<ImageOverlay> = emptyList(),
        lottieOverlays: List<LottieOverlaySpec> = emptyList(),
        trackedObjects: List<TrackedObject> = emptyList(),
        globalTransitions: List<GlobalTransition> = emptyList(),
        resumableOnInterruption: Boolean = false,
        onProgress: (Float) -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (Exception) -> Unit = {},
        onFallbackApplied: (String) -> Unit = {},
        tier: SubscriptionTier = SubscriptionTier.FREE,
    )

    suspend fun exportAudio(
        tracks: List<Track>,
        config: ExportConfig,
        outputFile: File,
        timelineDurationMsOverride: Long? = null,
        onProgress: (Float) -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (Exception) -> Unit = {},
        onFallbackApplied: (String) -> Unit = {},
    )

    suspend fun exportAudioStems(
        tracks: List<Track>,
        config: ExportConfig,
        outputFileFor: (index: Int, trackName: String) -> File,
        timelineDurationMsOverride: Long? = null,
        onProgress: (Float) -> Unit = {},
        onComplete: (List<File>) -> Unit = {},
        onError: (Exception) -> Unit = {},
        onFallbackApplied: (String) -> Unit = {},
    )

    suspend fun exportMixed(
        plan: MixedRenderComposer.CompositionPlan,
        tracks: List<Track>,
        config: ExportConfig,
        outputFile: File,
        textOverlays: List<TextOverlay> = emptyList(),
        imageOverlays: List<ImageOverlay> = emptyList(),
        lottieOverlays: List<LottieOverlaySpec> = emptyList(),
        trackedObjects: List<TrackedObject> = emptyList(),
        onProgress: (Float) -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (Exception) -> Unit = {},
        onFallbackApplied: (String) -> Unit = {},
        tier: SubscriptionTier = SubscriptionTier.FREE,
    ): Boolean
}

/** Production adapter that keeps the existing VideoEngine behaviour unchanged. */
internal class VideoEngineExportPort(
    private val engine: VideoEngine,
) : VideoExportPort {
    override val exportFailureCause: StateFlow<ExportFailureCause?>
        get() = engine.exportFailureCause

    override fun getVideoResolution(uri: Uri): Pair<Int, Int> = engine.getVideoResolution(uri)

    override fun hasAudioTrack(uri: Uri): Boolean = engine.hasAudioTrack(uri)

    override fun extractThumbnail(uri: Uri, timeUs: Long, width: Int, height: Int): Bitmap? =
        engine.extractThumbnail(uri, timeUs, width, height)

    override fun isReverseRenderAvailable(): Boolean = engine.isReverseRenderAvailable()

    override fun cancelExport(preservePartial: Boolean): File? = engine.cancelExport(preservePartial)

    override fun resetExportState() = engine.resetExportState()

    override suspend fun export(
        tracks: List<Track>,
        config: ExportConfig,
        outputFile: File,
        timelineDurationMsOverride: Long?,
        resumeFromFile: File?,
        textOverlays: List<TextOverlay>,
        imageOverlays: List<ImageOverlay>,
        lottieOverlays: List<LottieOverlaySpec>,
        trackedObjects: List<TrackedObject>,
        globalTransitions: List<GlobalTransition>,
        resumableOnInterruption: Boolean,
        onProgress: (Float) -> Unit,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit,
        onFallbackApplied: (String) -> Unit,
        tier: SubscriptionTier,
    ) = engine.export(
        tracks, config, outputFile, timelineDurationMsOverride, resumeFromFile,
        textOverlays, imageOverlays, lottieOverlays, trackedObjects, globalTransitions,
        resumableOnInterruption, onProgress, onComplete, onError, onFallbackApplied, tier,
    )

    override suspend fun exportAudio(
        tracks: List<Track>,
        config: ExportConfig,
        outputFile: File,
        timelineDurationMsOverride: Long?,
        onProgress: (Float) -> Unit,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit,
        onFallbackApplied: (String) -> Unit,
    ) = engine.exportAudio(
        tracks, config, outputFile, timelineDurationMsOverride,
        onProgress, onComplete, onError, onFallbackApplied,
    )

    override suspend fun exportAudioStems(
        tracks: List<Track>,
        config: ExportConfig,
        outputFileFor: (index: Int, trackName: String) -> File,
        timelineDurationMsOverride: Long?,
        onProgress: (Float) -> Unit,
        onComplete: (List<File>) -> Unit,
        onError: (Exception) -> Unit,
        onFallbackApplied: (String) -> Unit,
    ) = engine.exportAudioStems(
        tracks, config, outputFileFor, timelineDurationMsOverride,
        onProgress, onComplete, onError, onFallbackApplied,
    )

    override suspend fun exportMixed(
        plan: MixedRenderComposer.CompositionPlan,
        tracks: List<Track>,
        config: ExportConfig,
        outputFile: File,
        textOverlays: List<TextOverlay>,
        imageOverlays: List<ImageOverlay>,
        lottieOverlays: List<LottieOverlaySpec>,
        trackedObjects: List<TrackedObject>,
        onProgress: (Float) -> Unit,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit,
        onFallbackApplied: (String) -> Unit,
        tier: SubscriptionTier,
    ): Boolean = engine.exportMixed(
        plan, tracks, config, outputFile, textOverlays, imageOverlays, lottieOverlays,
        trackedObjects, onProgress, onComplete, onError, onFallbackApplied, tier,
    )
}
