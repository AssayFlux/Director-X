package com.directorx.editor.engine

/** Stable terminal cause reported by export engines and rendered by UI copy policy. */
enum class ExportFailureCause {
    /** The composition or transformer could not be built. */
    SETUP_FAILED,

    /** Media3 Transformer reported an ExportException while encoding. */
    ENCODER_FAILED,

    /** Encoding reported success but wrote a zero-byte file. */
    EMPTY_OUTPUT,

    /** The written file failed post-export track verification. */
    VERIFICATION_FAILED,

    /** No progress for the stall timeout; the export was treated as hung. */
    STALLED,

    /** Android stopped the media-processing foreground service before the export finished. */
    SERVICE_TIMEOUT,

    /** Storage preflight refused the write (space, path, or permission). */
    STORAGE,

    /** The audio-only or stems encode failed. */
    AUDIO_ENCODE_FAILED,

    /** The mixed FFmpeg/Transformer render path failed. */
    MIXED_RENDER_FAILED,

    /** Burning subtitles into the video failed. */
    SUBTITLE_BURN_IN_FAILED,

    /** A stage refused to change render intent and stopped the export. */
    STAGE_REFUSED,

    /** A GPU effect used a visual fallback during export. */
    GPU_EFFECT_DEGRADED,

    /** No cause was recorded — the only outcome that may use generic copy. */
    UNKNOWN,
}

