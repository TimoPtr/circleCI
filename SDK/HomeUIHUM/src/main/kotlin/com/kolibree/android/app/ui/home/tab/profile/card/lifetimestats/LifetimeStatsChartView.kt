/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.card.lifetimestats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.extensions.getColorFromAttr
import com.kolibree.android.homeui.hum.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@VisibleForApp
class LifetimeStatsChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 0f
    private val offlinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val inAppPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    init {
        inAppPaint.color = context.getColorFromAttr(R.attr.inAppBrushingsColor)
        inAppPaint.style = Paint.Style.FILL

        offlinePaint.color = context.getColorFromAttr(R.attr.offlineBrushingsColor)
        offlinePaint.style = Paint.Style.FILL

        strokePaint.color = ContextCompat.getColor(context, R.color.white)
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.strokeWidth = resources.getDimension(R.dimen.dot_quarter)
    }

    fun setStats(inAppBrushings: Int, offlineBrushings: Int) {
        progress = when {
            inAppBrushings == 0 -> 0f
            offlineBrushings == 0 -> 1f
            else -> inAppBrushings / (inAppBrushings + offlineBrushings).toFloat()
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            updateRect()
            drawBackground(it)
            drawForeground(it)
            if (innerStrokeNeeded()) {
                drawInnerStrokes(it)
            }
        }
    }

    private fun innerStrokeNeeded(): Boolean = progress != 0f && progress != 1f

    private fun drawForeground(canvas: Canvas) {
        canvas.drawArc(
            rect,
            START_ANGLE,
            calcSweepAngle(),
            true,
            inAppPaint
        )
    }

    private fun updateRect() {
        rect.left = 0f
        rect.top = 0f
        rect.right = width.toFloat()
        rect.bottom = height.toFloat()
    }

    private fun drawInnerStrokes(canvas: Canvas) {
        val radius = width / 2f
        val centerX = width / 2f
        val centerY = height / 2f

        val angleDegree = FULL_PROGRESS_ANGLE * progress
        val normalizeDegree = normalizeDegree(angleDegree)
        val angleRadians = toRadians(normalizeDegree)

        val x = centerX + radius * cos(angleRadians)
        val y = centerY - radius * sin(angleRadians)

        canvas.drawLine(centerX, centerY, x.toFloat(), y.toFloat(), strokePaint)
        canvas.drawLine(centerX, centerY, width / 2f, 0f, strokePaint)
    }

    private fun normalizeDegree(angleDegree: Float): Float {
        return QUARTER_PROGRESS_ANGLE - angleDegree
    }

    private fun toRadians(angleDegree: Float) = angleDegree * (Math.PI / HALF_PROGRESS_ANGLE)

    private fun drawBackground(canvas: Canvas) {
        canvas.drawArc(
            rect,
            START_ANGLE,
            FULL_PROGRESS_ANGLE,
            true,
            offlinePaint
        )
    }

    private fun calcSweepAngle() = min(FULL_PROGRESS_ANGLE, FULL_PROGRESS_ANGLE * progress)
}

private const val START_ANGLE = 270f
private const val FULL_PROGRESS_ANGLE = 360f
private const val HALF_PROGRESS_ANGLE = 180f
private const val QUARTER_PROGRESS_ANGLE = 90f

@BindingAdapter(value = ["in_app_stats", "offline_stats"], requireAll = true)
internal fun LifetimeStatsChartView.bindStats(inAppCount: Int, offlineCount: Int) {
    setStats(inAppCount, offlineCount)
}
