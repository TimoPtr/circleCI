/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.AT_MOST
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.UNSPECIFIED
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import kotlin.math.min

/**
 * Ring chart view.
 *
 * @param context non null [Context]
 * @param attrs nullable [AttributeSet]
 */
@Keep
class CoachPlusProgressView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    /** Lifetime paint.  */
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)

    /** View's drawing area.  */
    private val drawingArea = RectF()

    /** Background circle color.  */
    @ColorInt
    private var backgroundColor: Int = 0

    /** Border color.  */
    @ColorInt
    var borderColor: Int = 0
        /**
         * Set the view's border color.
         *
         * @param borderColor @[ColorInt] color
         */
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * Get the default size of the view sides.
     *
     * @return default size in pixels
     */
    private val defaultSizePixel: Int
        get() = (DEFAULT_SIZE * resources.displayMetrics.density).toInt()

    /**
     * Set the view's background color.
     *
     * @param backgroundColor @[ColorInt] color
     */
    override fun setBackgroundColor(backgroundColor: Int) {
        this.backgroundColor = backgroundColor
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        // Draw background
        super.onDraw(canvas)

        updateDrawingArea()

        // Draw background
        paint.color = backgroundColor
        paint.style = Style.FILL
        canvas.drawCircle(
            drawingArea.centerX(), drawingArea.centerY(), drawingArea.width() / 2f, paint
        )

        // Draw border
        paint.color = borderColor
        paint.style = Style.STROKE
        paint.strokeWidth = calculateBorderThickness()
        canvas.drawArc(drawingArea, 0f, 360f, false, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = parseMeasureSpec(widthMeasureSpec)
        val desiredHeight = parseMeasureSpec(heightMeasureSpec)

        if (desiredWidth == -1 && desiredHeight == -1) {
            val defaultSize = defaultSizePixel
            setMeasuredDimension(defaultSize, defaultSize)
        } else if (desiredWidth == -1) {
            setMeasuredDimension(desiredHeight, desiredHeight)
        } else if (desiredHeight == -1) {
            setMeasuredDimension(desiredWidth, desiredWidth)
        } else {
            setMeasuredDimension(desiredWidth, desiredHeight)
        }
    }

    /**
     * Parse a measure spec and compute a desired size.
     *
     * @param measureSpec int measure spec
     * @return int size in pixel or -1 if the mode is wrap_content
     */
    private fun parseMeasureSpec(measureSpec: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val desiredMeasure = MeasureSpec.getSize(measureSpec)

        return when (mode) {
            EXACTLY -> desiredMeasure
            AT_MOST -> min(defaultSizePixel, desiredMeasure)
            UNSPECIFIED -> -1
            else -> -1
        }
    }

    /** Update the drawing area according to the ring's thickness and padding.  */
    private fun updateDrawingArea() {
        val halfThickness = calculateBorderThickness() / 2f
        val intrinsicDiameter = min(
            width - paddingStart - paddingEnd,
            height - paddingTop - paddingBottom
        ).toFloat()

        drawingArea.set(
            (width - intrinsicDiameter) / 2 + halfThickness,
            (height - intrinsicDiameter) / 2 + halfThickness,
            width.toFloat() - (width - intrinsicDiameter) / 2 - halfThickness,
            height.toFloat() - (height - intrinsicDiameter) / 2 - halfThickness
        )
    }

    /**
     * The thickness of the border is size / 10.
     *
     * @return border thickness in pixels
     */
    private fun calculateBorderThickness(): Float = width / 10f

    companion object {

        /** Default size if not specified (in DP).  */
        private const val DEFAULT_SIZE = 120f
    }
}

@Keep
@BindingAdapter("borderColor")
fun setBorderColor(view: CoachPlusProgressView, @ColorInt color: Int) {
    view.borderColor = color
}

@Keep
@BindingAdapter("backgroundColor")
fun setBackgroundColor(view: CoachPlusProgressView, @ColorInt color: Int) {
    view.setBackgroundColor(color)
}
