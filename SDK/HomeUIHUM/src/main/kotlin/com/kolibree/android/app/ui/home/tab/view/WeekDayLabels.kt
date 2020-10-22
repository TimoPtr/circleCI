/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.view

import android.os.Parcelable
import java.util.Locale
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.DayOfWeek
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.WeekFields

@Parcelize
internal data class WeekDayLabels(
    val dayLabels: List<String> = emptyList()
) : Parcelable {
    fun dayLabelAt(index: Int): String = dayLabels.getOrNull(index) ?: ""

    internal companion object {
        fun create(locale: Locale): WeekDayLabels {
            val weekFields = WeekFields.of(locale)
            val labels = mutableListOf<String>()
            val daysInWeek = DayOfWeek.values().size
            for (day in 0 until daysInWeek) {
                labels += weekFields.firstDayOfWeek.plus(day.toLong())
                    .getDisplayName(TextStyle.NARROW, locale)
            }
            return WeekDayLabels(labels)
        }
    }
}
