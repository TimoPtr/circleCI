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
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.databinding.BindingAdapter
import kotlin.math.min

internal class FrequencyWeekView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var weekDaysType = WeekDaysType()

    private var interaction: FrequencyChartInteraction? = null

    private val rect = RectF()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            val totalSpace = width - DAYS * height
            val spaceBetweenDays = totalSpace / (DAYS - 1)
            for (day in 0 until DAYS.toInt()) {
                calcRect(day, spaceBetweenDays)
                val renderer = DayRendererFactory.create(context, weekDaysType.dayAt(day), day)
                renderer.render(canvas, rect)
            }
        }
    }

    private fun calcRect(day: Int, spaceBetweenDays: Float) {
        val dayWidth = height + spaceBetweenDays
        val start = day * dayWidth
        val end = min(width.toFloat(), start + dayWidth)
        rect.set(start, 0f, end, height.toFloat())
    }

    fun setWeekDaysType(weekDaysType: WeekDaysType) {
        this.weekDaysType = weekDaysType
        invalidate()
    }

    fun setInteraction(interaction: FrequencyChartInteraction?) {
        this.interaction = interaction
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            calcDayIndex(event.x)?.let { dayIndex ->
                val day = weekDaysType.dayAt(dayIndex).date
                if (day != null) {
                    interaction?.onDayClick(day)
                }
            }
        }

        return true
    }

    private fun calcDayIndex(x: Float): Int? {
        val daysInWeek = DAYS.toInt()
        val dayWidth = width / daysInWeek
        return if (dayWidth > 0) {
            val dayIndex = x.toInt() / dayWidth
            min(daysInWeek - 1, dayIndex)
        } else {
            null
        }
    }
}

@BindingAdapter("weekDaysType")
internal fun FrequencyWeekView.bindWeekDaysType(weekDaysType: WeekDaysType) {
    setWeekDaysType(weekDaysType)
}

@BindingAdapter("interaction")
internal fun FrequencyWeekView.bindInteraction(interaction: FrequencyChartInteraction?) {
    setInteraction(interaction)
}

private const val DAYS = 7f
