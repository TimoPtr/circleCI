/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import com.kolibree.android.app.ui.home.tab.view.WeekDayLabels
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.YearMonth

@Parcelize
internal data class FrequencyCardViewState(
    override val visible: Boolean,
    override val position: DynamicCardPosition,
    val weekDayLabels: WeekDayLabels,
    val monthsData: List<FrequencyChartViewState>,
    val currentMonthFromNow: Int,
    val pulsingDotVisible: Boolean,
    val profileId: Long?
) : DynamicCardViewState {

    override fun asBindingModel() = FrequencyCardBindingModel(this)

    @IgnoredOnParcel
    @VisibleForTesting
    var today = YearMonth.now()

    fun month(): YearMonth = today.minusMonths(currentMonthFromNow.toLong())

    fun nextMonth(): Int {
        return max(0, currentMonthFromNow - 1)
    }

    fun previousMonth(): Int {
        return min(monthsData.size - 1, currentMonthFromNow + 1)
    }

    companion object {
        fun initial(
            position: DynamicCardPosition
        ): FrequencyCardViewState {
            return FrequencyCardViewState(
                visible = true,
                position = position,
                weekDayLabels = WeekDayLabels.create(Locale.getDefault()),
                monthsData = emptyList(),
                currentMonthFromNow = 0,
                pulsingDotVisible = false,
                profileId = null
            )
        }
    }
}
