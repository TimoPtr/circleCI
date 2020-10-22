/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mouthmap.widget.progress

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.kolibree.android.mouthmap.R

internal class CleanScoreProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ProgressBar(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var cacheBitmap: Bitmap? = null

    init {
        backgroundPaint.color = ContextCompat.getColor(context, R.color.progress_bar_clean_score_light)

        foregroundPaint.color = ContextCompat.getColor(context, R.color.progress_bar_clean_score_dark)

        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) {
            return
        }

        canvas?.let {
            it.drawColor(Color.WHITE)
            drawProgress(it)
        }
    }

    private fun drawProgress(canvas: Canvas) {
        drawBackground(canvas)
        drawForeground(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        drawFullProgress(canvas, backgroundPaint)
    }

    private fun drawFullProgress(canvas: Canvas, paint: Paint) {
        val radius = height / 2f
        val startX = radius
        val startY = radius
        canvas.drawCircle(startX, startY, radius, paint)

        val stopX = width - radius
        val stopY = radius
        canvas.drawCircle(stopX, stopY, radius, paint)

        canvas.drawRect(startX, 0f, stopX, height.toFloat(), paint)
    }

    private fun drawForeground(canvas: Canvas) {
        val layer = prepareBitmap()
        val layerCanvas = Canvas(layer)
        drawFullProgress(layerCanvas, foregroundPaint)

        val progressValue = progress / MAX
        val startProgressX = progressValue * width
        layerCanvas.drawRect(startProgressX, 0f, width.toFloat(), height.toFloat(), clearPaint)

        canvas.drawBitmap(layer, 0f, 0f, null)
    }

    private fun prepareBitmap(): Bitmap =
        cacheBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { cacheBitmap = this }

    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        invalidate()
    }
}

private const val MAX = 100f
