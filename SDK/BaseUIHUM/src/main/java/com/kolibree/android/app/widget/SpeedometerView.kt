/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.Keep
import androidx.core.content.withStyledAttributes
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.baseui.hum.R
import kotlin.math.sin

@VisibleForApp
class SpeedometerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.speedometerViewStyle
) : View(context, attrs, defStyleAttr) {

    private val perfectPaint = strokeRoundedPaint()
    private val trackPaint = strokeRoundedPaint()
    private val dotPaint = strokeRoundedPaint()

    private var trackColor: Int = Color.WHITE
    private var perfectTrackColor: Int = Color.WHITE
    private var disabledTrackColor: Int = Color.WHITE
    private var dotColor: Int = Color.WHITE
    private var disabledDotColor: Int = Color.WHITE
    private var fadeDuration: Long = 0

    private var perfectRange = 0f..0f
    private val circleBounds = RectF()
    private var dotAnimator: Animator? = null
    private var trackAnimator: ObjectAnimator? = null

    var min: Float = DEFAULT_MIN
        set(value) {
            field = value
            invalidate()
        }
    var max: Float = DEFAULT_MAX
        set(value) {
            field = value
            invalidate()
        }
    var perfectMin: Float = DEFAULT_PERFECT_MIN
        set(value) {
            field = value
            updatePerfectRange()
        }
    var perfectMax: Float = DEFAULT_PERFECT_MAX
        set(value) {
            field = value
            updatePerfectRange()
        }
    var dotAnimationDuration: Long = 0
        set(value) {
            field = value
            invalidate()
        }
    var snapDotToZones: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    private fun updatePerfectRange() {
        perfectRange = perfectMin..perfectMax
        invalidate()
    }

    var position: Float = DEFAULT_MIN + ((DEFAULT_MAX - DEFAULT_MIN) / 2f)
        @Keep
        set(value) {
            if (!isEnabled) return
            if (perfectRange.contains(value) && !perfectRange.contains(field)) {
                stopTrackAnimation()
            } else if (!perfectRange.contains(value) && perfectRange.contains(field)) {
                startTrackAnimation()
            }
            field = value.coerceIn(min..max)
            invalidate()
        }

    fun smoothPositionTo(newPosition: Float) {
        dotAnimator?.takeIf { it.isRunning }?.cancel()
        dotAnimator = ObjectAnimator.ofFloat(this, "position", position, newPosition).apply {
            duration = dotAnimationDuration
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private var currentTrackColor = perfectTrackColor
        @Keep
        set(value) {
            perfectPaint.color = value
            field = value
            invalidate()
        }

    private fun startTrackAnimation() {
        trackAnimator?.takeIf { it.isRunning }?.cancel()
        trackAnimator = ObjectAnimator.ofArgb(
            this, CURRENT_TRACK_COLOR, perfectTrackColor, trackColor
        ).apply {
            duration = fadeDuration
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    }

    private fun stopTrackAnimation() {
        val stopAnimDuration = trackAnimator?.let {
            calculateStopAnimDuration(it.currentPlayTime, fadeDuration)
        } ?: fadeDuration
        trackAnimator?.takeIf { it.isRunning }?.cancel()
        trackAnimator = ObjectAnimator.ofArgb(
            this, CURRENT_TRACK_COLOR, currentTrackColor, perfectTrackColor
        ).apply {
            duration = stopAnimDuration
            start()
        }
    }

    init {
        attrs?.also {
            context.withStyledAttributes(it, R.styleable.SpeedometerView, defStyleAttr) {
                setupTrack()
                setupDot()
                min = getFloat(R.styleable.SpeedometerView_min, DEFAULT_MIN)
                max = getFloat(R.styleable.SpeedometerView_max, DEFAULT_MAX)
                perfectMin = getFloat(R.styleable.SpeedometerView_perfect_min, DEFAULT_PERFECT_MIN)
                perfectMax = getFloat(R.styleable.SpeedometerView_perfect_max, DEFAULT_PERFECT_MAX)
                dotAnimationDuration =
                    getInt(R.styleable.SpeedometerView_dot_animation_duration, 0).toLong()
                fadeDuration = getInt(R.styleable.SpeedometerView_fade_duration, 0).toLong()
            }
        }
    }

    private fun TypedArray.setupTrack() {
        trackColor = getColor(R.styleable.SpeedometerView_track_color, Color.WHITE)
        perfectTrackColor = getColor(R.styleable.SpeedometerView_track_color_perfect, Color.WHITE)
        disabledTrackColor = getColor(R.styleable.SpeedometerView_track_color_disabled, Color.WHITE)
        trackPaint.apply {
            color = trackColor
            strokeWidth = getDimension(R.styleable.SpeedometerView_track_width, DEFAULT_WIDTH)
        }
        perfectPaint.apply {
            color = perfectTrackColor
            strokeWidth = trackPaint.strokeWidth
        }
        currentTrackColor = perfectTrackColor
    }

    private fun TypedArray.setupDot() {
        dotColor = getColor(R.styleable.SpeedometerView_dot_color, Color.WHITE)
        disabledDotColor = getColor(R.styleable.SpeedometerView_dot_color_disabled, Color.WHITE)
        dotPaint.apply {
            color = dotColor
            strokeWidth = trackPaint.strokeWidth
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val insetWidth = perfectPaint.strokeWidth / 2f
        val symmetricalVerticalOffset = (measuredWidth / 2f * sin(HEIGHT_ANGLE_RADIANS)).toFloat()
        val calculatedHeight = if (heightMode == MeasureSpec.EXACTLY) {
            val verticalFraction =
                measuredWidth / ((measuredWidth / 2f) + symmetricalVerticalOffset)
            val newBoundsHeight = (heightSize * verticalFraction) - insetWidth
            circleBounds.set(0f, 0f, measuredWidth.toFloat(), newBoundsHeight)
            heightSize
        } else {
            circleBounds.set(0f, 0f, measuredWidth.toFloat(), measuredWidth.toFloat())
            (measuredWidth / 2f + symmetricalVerticalOffset + insetWidth).toInt()
        }
        circleBounds.inset(insetWidth, insetWidth)
        setMeasuredDimension(measuredWidth, calculatedHeight)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (isEnabled) {
            dotPaint.color = dotColor
            trackPaint.color = trackColor
            perfectPaint.color = currentTrackColor
            dotAnimator?.resume()
        } else {
            dotPaint.color = disabledDotColor
            trackPaint.color = disabledTrackColor
            perfectPaint.color = currentTrackColor
            dotAnimator?.pause()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawArc(circleBounds, PERFECT_START_ANGLE, PERFECT_SWEEP_ANGLE, false, perfectPaint)
        canvas.drawArc(circleBounds, HIGH_START_ANGLE, IMPERFECT_SWEEP_ANGLE, false, trackPaint)
        canvas.drawArc(circleBounds, LOW_START_ANGLE, -IMPERFECT_SWEEP_ANGLE, false, trackPaint)
        if (isEnabled) canvas.drawArc(circleBounds, dotAngle(), DOT_SWEEP_ANGLE, false, dotPaint)
    }

    private fun dotAngle(): Float =
        when {
            !snapDotToZones ->
                calculateAngle(DEFAULT_MIN, DEFAULT_MAX, FULL_ARC_START, FULL_ARC_SWEEP)
            perfectRange.contains(position) ->
                calculateAngle(perfectMin, perfectMax, PERFECT_START_ANGLE, PERFECT_SWEEP_ANGLE)
            position < perfectMin -> calculateAngle(
                min,
                perfectMin,
                LOW_START_ANGLE - IMPERFECT_SWEEP_ANGLE,
                IMPERFECT_SWEEP_ANGLE
            )
            else -> calculateAngle(perfectMax, max, HIGH_START_ANGLE, IMPERFECT_SWEEP_ANGLE)
        }

    private fun calculateAngle(min: Float, max: Float, start: Float, sweep: Float) =
        start + ((position - min) / (max - min) * sweep)

    private companion object {
        private const val FULL_ARC_START = -220f
        private const val FULL_ARC_SWEEP = 260f
        private const val PERFECT_START_ANGLE = -147.6f
        private const val LOW_START_ANGLE = -162.5f
        private const val HIGH_START_ANGLE = -17.5f
        private const val PERFECT_SWEEP_ANGLE = 115.2f
        private const val IMPERFECT_SWEEP_ANGLE = 57.6f
        private const val ONE_HUNDRED_AND_EIGHTY_DEGREES = 180f
        private const val HEIGHT_ANGLE_RADIANS =
            (HIGH_START_ANGLE + IMPERFECT_SWEEP_ANGLE) / ONE_HUNDRED_AND_EIGHTY_DEGREES * Math.PI
        private const val DOT_SWEEP_ANGLE = 0.1f
        private const val DEFAULT_WIDTH = 64f

        private const val DEFAULT_MIN = 0f
        private const val DEFAULT_MAX = 100f
        private const val DEFAULT_PERFECT_MIN = 40f
        private const val DEFAULT_PERFECT_MAX = 60f

        private const val CURRENT_TRACK_COLOR = "currentTrackColor"

        private fun strokeRoundedPaint(): Paint = Paint().apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
        }

        private fun calculateStopAnimDuration(currentPlayTime: Long, totalPlayTime: Long) =
            if (currentPlayTime % totalPlayTime > totalPlayTime / 2) totalPlayTime - (currentPlayTime % totalPlayTime)
            else (currentPlayTime % totalPlayTime)
    }
}
