/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.Month
import org.threeten.bp.YearMonth

internal class FrequencyCardViewStateTest : BaseUnitTest() {

    @Test
    fun `nextMonth returns next index if still in range`() {
        val initial = FrequencyCardViewState.initial(DynamicCardPosition.ZERO)
        val viewState4 = initial.copy(currentMonthFromNow = 4)
        assertEquals(3, viewState4.nextMonth())

        val viewState3 = initial.copy(currentMonthFromNow = 3)
        assertEquals(2, viewState3.nextMonth())

        val viewState2 = initial.copy(currentMonthFromNow = 2)
        assertEquals(1, viewState2.nextMonth())

        val viewState1 = initial.copy(currentMonthFromNow = 1)
        assertEquals(0, viewState1.nextMonth())

        val viewState0 = initial.copy(currentMonthFromNow = 0)
        assertEquals(0, viewState0.nextMonth())
    }

    @Test
    fun `previousMonth returns previous index if still in range`() {
        val initial = FrequencyCardViewState.initial(DynamicCardPosition.ZERO).copy(
            monthsData = listOf(
                FrequencyChartViewState(), FrequencyChartViewState(), FrequencyChartViewState()
            )
        )
        val viewState0 = initial.copy(currentMonthFromNow = 0)
        assertEquals(1, viewState0.previousMonth())

        val viewState1 = initial.copy(currentMonthFromNow = 1)
        assertEquals(2, viewState1.previousMonth())

        val viewState2 = initial.copy(currentMonthFromNow = 2)
        assertEquals(2, viewState2.previousMonth())
    }

    @Test
    fun `month returns selected month`() {
        val viewState = FrequencyCardViewState.initial(DynamicCardPosition.ZERO).copy(
            currentMonthFromNow = 2
        )
        viewState.today = YearMonth.of(2020, Month.MAY)
        val expected = YearMonth.of(2020, Month.MARCH)
        assertEquals(expected, viewState.month())
    }
}
