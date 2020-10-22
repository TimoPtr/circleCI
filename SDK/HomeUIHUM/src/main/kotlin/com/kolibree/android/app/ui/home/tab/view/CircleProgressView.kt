/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.databinding.BindingAdapter
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.extensions.withValueAnimator
import com.kolibree.android.extensions.getColorFromAttr
import com.kolibree.android.homeui.hum.R
import kotlin.math.min

@VisibleForApp
class CircleProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    internal var progress = 0f
    private val background = Paint(Paint.ANTI_ALIAS_FLAG)
    private val foreground = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    init {
        background.color = context.getColorFromAttr(R.attr.backgroundColorDark)
        background.style = Paint.Style.STROKE
        background.strokeCap = Paint.Cap.ROUND

        foreground.style = Paint.Style.STROKE
        foreground.strokeCap = Paint.Cap.ROUND

        setProgressWidth(context.resources.getDimension(R.dimen.dot_half))
    }

    fun setColor(@ColorInt color: Int) {
        foreground.color = color
        invalidate()
    }

    fun setProgressBackgroundColor(@ColorInt color: Int) {
        background.color = color
        invalidate()
    }

    fun setProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float) {
        this.progress = progress
        invalidate()
    }

    fun setProgressWidth(width: Float) {
        background.strokeWidth = width
        foreground.strokeWidth = width
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.drawColor(Color.TRANSPARENT)
            updateRect()
            drawBackground(it)
            drawForeground(it)
        }
    }

    private fun drawForeground(canvas: Canvas) {
        canvas.drawArc(
            rect,
            START_ANGLE,
            calcSweepAngle(),
            false,
            foreground
        )
    }

    private fun updateRect() {
        val halfStrokeWidth = background.strokeWidth / 2
        rect.left = halfStrokeWidth
        rect.top = halfStrokeWidth
        rect.right = width.toFloat() - halfStrokeWidth
        rect.bottom = height.toFloat() - halfStrokeWidth
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawArc(
            rect,
            START_ANGLE,
            FULL_PROGRESS_ANGLE,
            false,
            background
        )
    }

    private fun calcSweepAngle() = min(FULL_PROGRESS_ANGLE, FULL_PROGRESS_ANGLE * progress)
}

private const val START_ANGLE = 270f
private const val FULL_PROGRESS_ANGLE = 360f

@BindingAdapter("progress")
internal fun CircleProgressView.bindProgress(progress: Float) {
    setProgress(progress)
}

@Deprecated(
    replaceWith = ReplaceWith("bindProgressColor"),
    message = "Use bindProgressColor instead"
)
@BindingAdapter("color")
internal fun CircleProgressView.bindColor(@ColorInt color: Int) {
    setColor(color)
}

@BindingAdapter("progressColor")
internal fun CircleProgressView.bindProgressColor(@ColorInt color: Int) {
    setColor(color)
}

@BindingAdapter("progressBackgroundColor")
internal fun CircleProgressView.bindProgressBackgroundColor(@ColorInt color: Int) {
    setProgressBackgroundColor(color)
}

@BindingAdapter("progressWidth")
internal fun CircleProgressView.bindProgressWidth(width: Float) {
    setProgressWidth(width)
}

@BindingAdapter("updateProgress")
internal fun CircleProgressView.bindUpdateProgress(newProgress: Float) {
    if (progress == newProgress) return

    val diff = newProgress - progress
    val startProgress = progress
    val interpolator = if (diff > 0) AccelerateInterpolator() else DecelerateInterpolator()
    withValueAnimator(interpolator = interpolator) {
        val updatedProgress = startProgress + (diff * it)
        setProgress(updatedProgress)
    }
}
