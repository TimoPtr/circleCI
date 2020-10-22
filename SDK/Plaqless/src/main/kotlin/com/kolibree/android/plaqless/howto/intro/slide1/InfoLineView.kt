/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slide1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.kolibree.android.plaqless.R

class InfoLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintLine = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circleRadius: Float

    private var startX: Float = 0f
    private var startY: Float = 0f
    private var stopX: Float = 0f
    private var stopY: Float = 0f

    init {
        paintLine.color = ContextCompat.getColor(context, R.color.white)
        paintLine.strokeWidth = context.resources.getDimension(R.dimen.line_width)
        circleRadius = context.resources.getDimension(R.dimen.line_radius)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            drawLine(it)
        }
    }

    fun drawLineBetween(startView: View, stopView: View) {
        startX = startView.x + startView.width / 2f
        startY = startView.y + startView.height / 2f

        val endType = calcEndType(startView, stopView)
        stopX = when (endType) {
            EndType.RIGHT -> stopView.x + stopView.width
            else -> stopView.x + END_POINT_POSITION * stopView.width
        }
        stopY = when (endType) {
            EndType.TOP -> stopView.y
            EndType.RIGHT -> stopView.y + stopView.height / 2f
            EndType.BOTTOM -> stopView.y + stopView.height
        }

        invalidate()
    }

    private fun calcEndType(startView: View, stopView: View): EndType {
        val centerStartY = startView.y + startView.height / 2f
        val topStopY = stopView.y
        val bottomStopY = stopView.y + stopView.height
        return when {
            centerStartY < topStopY -> EndType.TOP
            centerStartY > bottomStopY -> EndType.BOTTOM
            else -> EndType.RIGHT
        }
    }

    private fun drawLine(canvas: Canvas) {
        canvas.drawColor(Color.TRANSPARENT)

        if (startX == 0f && startY == 0f) {
            return
        }

        val distance = Math.abs(stopX - startX)
        val startLineWidth = distance * START_LINE_WIDTH
        val endLineX = startX - startLineWidth
        val endLineY = startY

        canvas.drawLine(startX, startY, endLineX, endLineY, paintLine)
        canvas.drawCircle(endLineX, endLineY, paintLine.strokeWidth / 2f, paintLine)

        canvas.drawLine(endLineX, endLineY, stopX, stopY, paintLine)
        canvas.drawCircle(stopX, stopY, circleRadius, paintLine)
    }

    private enum class EndType {
        TOP,
        BOTTOM,
        RIGHT
    }
}

private const val START_LINE_WIDTH = 0.35f
private const val END_POINT_POSITION = 0.75f
