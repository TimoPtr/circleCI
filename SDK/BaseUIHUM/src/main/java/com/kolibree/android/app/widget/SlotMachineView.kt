/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.ViewTreeObserver
import androidx.annotation.FloatRange
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.withStyledAttributes
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.baseui.hum.R
import kotlin.math.floor

@VisibleForApp
class SlotMachineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var textPaint: Paint = Paint()
    private val textBounds = Rect()
    private val clipBounds = RectF()
    private var textHeight: Int = -1
    private var reelPadding: Float = DEFAULT_VERTICAL_REEL_PADDING

    private var reels: Array<Array<String>> = emptyArray()
    private val reelWidths: MutableList<Int> = mutableListOf()
    private val digitOffsets: MutableList<Map<String, Int>> = mutableListOf()

    var startValue: Int = 0
        set(value) {
            field = value
            invalidateValues()
        }

    var endValue: Int = 0
        set(value) {
            field = value
            invalidateValues()
        }

    @Keep
    @FloatRange(from = 0.0, to = 1.0)
    var position: Float = 0f
        @Keep set(value) {
            field = value
            invalidate()
        }

    init {
        attrs?.also {
            context.withStyledAttributes(it, R.styleable.SlotMachineView, defStyleAttr) {
                endValue = getInteger(R.styleable.SlotMachineView_endValue, 0)
                startValue = getInteger(R.styleable.SlotMachineView_startValue, 0)
                reelPadding = getDimension(
                    R.styleable.SlotMachineView_reelVerticalPadding,
                    DEFAULT_VERTICAL_REEL_PADDING
                )
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var width = if (widthMode != MeasureSpec.EXACTLY) {
            measureReels()
            reelWidths.sum().coerceAtLeast(measuredWidth)
        } else {
            MeasureSpec.getSize(widthMeasureSpec)
        }
        width += paddingStart + paddingEnd
        setMeasuredDimension(width, measuredHeight)
    }

    private fun measureReels() {
        val bounds = Rect()
        digitOffsets.clear()
        val digitWidths = mutableMapOf<String, Int>()
        reelWidths.clear()
        digitOffsets.clear()
        reelWidths.addAll(measureReelWidths(digitWidths, bounds))
    }

    private fun measureReelWidths(digitWidths: MutableMap<String, Int>, bounds: Rect): List<Int> {
        return reels.map { reel ->
            digitWidths.clear()
            (reel.measureReelDigitWidths(digitWidths, bounds)
                .max() ?: 0)
                .also { maxWidth ->
                    buildDigitOffsets(digitWidths, maxWidth)
                }
        }.padToEndValueWidths()
    }

    private fun Array<String>.measureReelDigitWidths(
        digitWidths: MutableMap<String, Int>,
        bounds: Rect
    ): List<Int> =
        map { digitString ->
            paint.getTextBounds(digitString, 0, digitString.length, bounds)
            digitWidths[digitString] = bounds.width()
            bounds.width()
        }

    private fun buildDigitOffsets(digitWidths: MutableMap<String, Int>, maxWidth: Int) =
        digitOffsets.add(
            digitWidths.mapValues { entry ->
                (maxWidth - entry.value) ushr 1
            }
        )

    private fun List<Int>.padToEndValueWidths(): List<Int> {
        val endWidths = FloatArray(reels.size)
        paint.getTextWidths(endValue.toString(), endWidths)
        return mapIndexed { index, max ->
            max.coerceAtLeast(endWidths[index].toInt())
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (startValue == endValue) {
            super.onDraw(canvas)
            return
        }
        textPaint.set(paint)
        textPaint.color = textColors.getColorForState(drawableState, 0)

        var x = paddingStart.toFloat()
        val baseline = (height - lastBaselineToBottomHeight).toFloat()
        canvas.save()
        canvas.clipRect(clipBounds)
        reels.forEachIndexed { index, reel ->
            canvas.drawReel(index, x, reel, baseline)
            x += reelWidths[index]
        }
        canvas.restore()
    }

    private fun Canvas.drawReel(index: Int, xOffset: Float, reel: Array<String>, bottom: Float) {
        val offset = (reel.size - 1) * position
        val reelOffsets = digitOffsets[index]
        val reelPosition = floor(offset).toInt()
        val reelOffset = offset - reelPosition.toFloat()
        var reelText = reel[reelPosition]
        var yPosition = bottom - (textHeight * reelOffset) - (reelPadding * reelOffset)
        drawDigit(reelText, xOffset, yPosition, reelOffsets, textPaint)
        if (reelOffset != 0f && reelPosition < reel.size - 1) {
            reelText = reel[reelPosition + 1]
            yPosition =
                bottom + (textHeight * (1f - reelOffset)) + (reelPadding * (1f - reelOffset))
            drawDigit(reelText, xOffset, yPosition, reelOffsets, textPaint)
        }
    }

    private fun Canvas.drawDigit(
        text: String,
        xOffset: Float,
        yPosition: Float,
        reelOffsets: Map<String, Int>,
        paint: Paint
    ) {
        drawText(
            text,
            xOffset + (reelOffsets[text]?.toFloat() ?: 0f),
            yPosition,
            paint
        )
    }

    private fun invalidateTextBounds() {
        textHeight = -1
        viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    viewTreeObserver.removeOnPreDrawListener(this)
                    if (textHeight < 0) {
                        calculateTextHeight()
                    }
                    calculateDimensions()
                    return true
                }
            }
        )
    }

    private fun calculateDimensions() {
        textHeight = textBounds.height()
        val textString = text.toString()
        paint.getTextBounds(textString, 0, textString.length, textBounds)
        val baseline = (height - lastBaselineToBottomHeight).toFloat()
        clipBounds.set(
            0f,
            baseline - textHeight - reelPadding,
            width.toFloat() + CLIP_EXPANSION,
            baseline + reelPadding
        )
        measureReels()
    }

    private fun calculateTextHeight() {
        textHeight = textBounds.height()
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(endValue.toString(), type)
        invalidateTextBounds()
    }

    private fun invalidateValues() {
        val endDigits = getDigits(endValue)
        val startDigits = if (startValue > endValue) {
            getDigits(endValue, endDigits.size)
        } else {
            getDigits(startValue, endDigits.size)
        }
        super.setText(endValue.toString())
        reels = buildReels(startDigits, endDigits)
    }

    private companion object {
        private const val DEFAULT_VERTICAL_REEL_PADDING = 32f
        private const val CLIP_EXPANSION = 16f
    }
}
