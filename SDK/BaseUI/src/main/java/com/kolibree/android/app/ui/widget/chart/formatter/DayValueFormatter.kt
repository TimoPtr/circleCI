/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.widget.chart.formatter

import com.github.mikephil.charting.formatter.ValueFormatter
import com.kolibree.android.annotation.VisibleForApp
import java.util.Locale
import org.threeten.bp.DayOfWeek
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.WeekFields

/**
 * Value is the index of the Day in the current Locale.
 */
@VisibleForApp
class DayValueFormatter(pattern: String = "EEE", locale: Locale = Locale.getDefault()) : ValueFormatter() {
    private val formatter = DateTimeFormatter.ofPattern(pattern, locale)

    private val weekOrder: List<DayOfWeek>

    init {
        val wf = WeekFields.of(locale)
        weekOrder = (0 until NUMBER_OF_DAYS).map { wf.firstDayOfWeek.plus(it.toLong()) }
    }

    /**
     * value is the index in weekOrder
     */
    override fun getFormattedValue(value: Float): String =
        if (value >= 0 && value < NUMBER_OF_DAYS) {
            val day = weekOrder[value.toInt()]
            formatter.format(day)
        } else {
            ""
        }

    @VisibleForApp
    companion object {
        @JvmStatic
        fun create(): DayValueFormatter = DayValueFormatter()

        /**
         * Get the index of the day based on the Locale
         */
        fun indexOfDayOfWeek(day: DayOfWeek, locale: Locale = Locale.getDefault()): Int {
            val wf = WeekFields.of(locale)
            return (0 until NUMBER_OF_DAYS).indexOfFirst { wf.firstDayOfWeek.plus(it.toLong()) == day }
        }
    }
}

private const val NUMBER_OF_DAYS = 7
