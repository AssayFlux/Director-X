package com.directorx.editor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import android.graphics.Paint
import android.os.SystemClock
import android.view.View
import kotlin.math.min

internal const val ASSAYFLUX_SPLASH_DURATION_MS = 1_300L

/**
 * Full-screen, white AssayFlux brand splash.
 *
 * Movie is used instead of a newer-only animated drawable so the supplied GIF
 * also works on the app's older supported Android versions.
 */
internal class AssayFluxSplashView(context: Context) : View(context) {

    private val movie = context.resources.openRawResource(R.raw.assayflux_splash).use {
        Movie.decodeStream(it)
    }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val startedAt = SystemClock.uptimeMillis()

    init {
        setBackgroundColor(Color.WHITE)
        contentDescription = "AssayFlux logo"
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)

        val animation = movie ?: return
        val frameDuration = animation.duration().coerceAtLeast(1)
        val elapsed = (SystemClock.uptimeMillis() - startedAt)
            .coerceAtMost(ASSAYFLUX_SPLASH_DURATION_MS)
            .toInt()
        animation.setTime(elapsed.coerceAtMost(frameDuration - 1))

        if (width > 0 && height > 0 && animation.width() > 0 && animation.height() > 0) {
            val scale = min(
                width.toFloat() / animation.width().toFloat(),
                height.toFloat() / animation.height().toFloat()
            )
            val scaledWidth = animation.width() * scale
            val scaledHeight = animation.height() * scale
            val left = (width - scaledWidth) / 2f
            val top = (height - scaledHeight) / 2f

            val saveCount = canvas.save()
            canvas.translate(left, top)
            canvas.scale(scale, scale)
            animation.draw(canvas, 0f, 0f, paint)
            canvas.restoreToCount(saveCount)
        }

        if (elapsed < ASSAYFLUX_SPLASH_DURATION_MS) {
            postInvalidateOnAnimation()
        }
    }
}
