package com.directorx.editor.model

/**
 * Resolves project-canvas dimensions for an export unless the user explicitly
 * selected a delivery/platform preset, which remains authoritative.
 */
internal fun ExportConfig.withProjectCanvas(
    project: Project,
    outputAspectRatio: AspectRatio,
): ExportConfig {
    val canvas = project.canvasDimensions
    val useProjectCanvas = project.aspectRatio == AspectRatio.CUSTOM && platformPreset == null
    return copy(
        aspectRatio = outputAspectRatio,
        customOutputWidth = canvas.width.takeIf { useProjectCanvas },
        customOutputHeight = canvas.height.takeIf { useProjectCanvas },
    )
}
