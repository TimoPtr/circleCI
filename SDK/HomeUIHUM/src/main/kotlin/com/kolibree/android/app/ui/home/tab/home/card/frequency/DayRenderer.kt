/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.kolibree.android.extensions.getColorFromAttr
import com.kolibree.android.homeui.hum.R

internal abstract class DayRenderer {
    abstract fun render(canvas: Canvas, rect: RectF)

    protected fun createTextPaint(context: Context): TextPaint {
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        textPaint.typeface = ResourcesCompat.getFont(context, R.font.hind_semibold)
        textPaint.textSize = context.resources.getDimension(R.dimen.text_18pt)
        return textPaint
    }

    protected fun drawCircle(canvas: Canvas, paint: Paint, rect: RectF) {
        val strokeWidth = paint.strokeWidth
        val radius = rect.height() / 2f - strokeWidth / 2f
        val cx = rect.left + rect.centerY()
        val cy = rect.centerY()
        canvas.drawCircle(cx, cy, radius, paint)
    }

    protected fun drawNumber(
        canvas: Canvas,
        rect: RectF,
        number: Int,
        textPaint: TextPaint
    ) {
        val radius = rect.centerY()
        val x = rect.left + radius
        val y = rect.centerY()
        val text = number.toString()
        val xPos = x - (textPaint.measureText(text) / 2)
        val yPos = (y - (textPaint.descent() + textPaint.ascent()) / 2)
        val bottomMargin = textPaint.textSize * BOTTOM_MARGIN_PERCENT
        canvas.drawText(text, xPos, yPos + bottomMargin, textPaint)
    }
}

internal class PerfectDayRenderer(
    context: Context,
    private val type: DayType.PerfectDay,
    dayOfWeekIndex: Int
) : DayRenderer() {
    private val firstWeekDay: Boolean = dayOfWeekIndex == FIRST_DAY_OF_WEEK_INDEX
    private val lastWeekDay: Boolean = dayOfWeekIndex == LAST_DAY_OF_WEEK_INDEX
    private val background = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strike = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = createTextPaint(context)

    init {
        background.color = context.getColorFromAttr(R.attr.colorPrimary)
        background.style = Paint.Style.FILL
        textPaint.color = ContextCompat.getColor(context, R.color.white)
        strike.color = context.getColorFromAttr(R.attr.colorSecondaryLight)
        strike.style = Paint.Style.FILL
    }

    override fun render(canvas: Canvas, rect: RectF) {
        val radius = rect.centerY()
        if (type.isPerfectDayBefore && !firstWeekDay) {
            canvas.drawRect(rect.left, rect.top, rect.left + radius, rect.bottom, strike)
        }
        if (type.isPerfectDayAfter && !lastWeekDay) {
            canvas.drawRect(rect.left + radius, rect.top, rect.right, rect.bottom, strike)
        }
        drawCircle(canvas, background, rect)
        drawNumber(canvas, rect, type.brushings, textPaint)
    }
}

internal class SingleBrushingDayRenderer(context: Context) : DayRenderer() {
    private val background = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = createTextPaint(context)

    init {
        background.color = context.getColorFromAttr(R.attr.colorSecondaryLight)
        background.style = Paint.Style.FILL
        textPaint.color = context.getColorFromAttr(R.attr.colorPrimary)
    }

    override fun render(canvas: Canvas, rect: RectF) {
        drawCircle(canvas, background, rect)
        drawNumber(canvas, rect, 1, textPaint)
    }
}

internal class NoBrushingDayRenderer(context: Context) : DayRenderer() {
    private val background = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = createTextPaint(context)

    init {
        val color = context.getColorFromAttr(R.attr.colorBackgroundDark)
        background.color = color
        background.strokeWidth = context.resources.getDimension(R.dimen.dot_quarter)
        background.style = Paint.Style.STROKE
        textPaint.color = color
    }

    override fun render(canvas: Canvas, rect: RectF) {
        drawCircle(canvas, background, rect)
        drawNumber(canvas, rect, 0, textPaint)
    }
}

internal class NotAvailableDayRenderer(context: Context) : DayRenderer() {
    private val background = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        background.color = context.getColorFromAttr(R.attr.backgroundColor)
        background.style = Paint.Style.FILL
    }

    override fun render(canvas: Canvas, rect: RectF) {
        drawCircle(canvas, background, rect)
    }
}

internal class FutureDayRenderer(context: Context) : DayRenderer() {
    private val background = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        val color = context.getColorFromAttr(R.attr.colorBackgroundDark)
        background.color = color
        background.strokeWidth = context.resources.getDimension(R.dimen.dot_quarter)
        background.style = Paint.Style.STROKE
    }

    override fun render(canvas: Canvas, rect: RectF) {
        drawCircle(canvas, background, rect)
    }
}

internal object EmptyDayRenderer : DayRenderer() {
    override fun render(canvas: Canvas, rect: RectF) {
        // no-op
    }
}

private const val BOTTOM_MARGIN_PERCENT = 0.1f
private const val LAST_DAY_OF_WEEK_INDEX = 6
private const val FIRST_DAY_OF_WEEK_INDEX = 0
