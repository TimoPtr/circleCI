/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import android.view.View
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.commons.MONTH_YEAR_FORMATTER
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class FrequencyCardBindingModel(
    val data: FrequencyCardViewState,
    override val layoutId: Int = R.layout.home_card_frequency
) : DynamicCardBindingModel(data) {

    @IgnoredOnParcel
    val adapter = FrequencyChartAdapter()

    fun formatMonth(): String = MONTH_YEAR_FORMATTER.format(data.month())

    fun items(): List<FrequencyChartViewState> = data.monthsData

    private fun monthsCount(): Int = data.monthsData.size

    fun isNextMonthEnabled(): Boolean {
        val currentMonth = data.currentMonthFromNow
        val nextMonth = currentMonth - 1
        return nextMonth >= 0
    }

    fun isPreviousMonthEnabled(): Boolean {
        val currentMonth = data.currentMonthFromNow
        val previousMonth = currentMonth + 1
        return previousMonth < monthsCount()
    }

    fun currentItem(): Int {
        val lastIndex = data.monthsData.size - 1
        return lastIndex - data.currentMonthFromNow
    }

    fun pulsingDotVisibility(): Int = if (data.pulsingDotVisible) View.VISIBLE else View.GONE
}
