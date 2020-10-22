/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.domain.logic

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingStat
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingType
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate

internal class BrushingStatsComputation : BaseUnitTest() {

    @Test
    fun `lastWeekOnly returns Brushings for last week`() {
        val today = LocalDate.now()
        val lastWeekDate1 = createBrushingStat(today)
        val lastWeekDate2 = createBrushingStat(today.minusDays(1))
        val lastWeekDate3 = createBrushingStat(today.minusDays(2))
        val lastWeekDate4 = createBrushingStat(today.minusDays(3))
        val lastWeekDate5 = createBrushingStat(today.minusDays(4))
        val lastWeekDate6 = createBrushingStat(today.minusDays(5))
        val lastWeekDate7 = createBrushingStat(today.minusDays(6))
        val notLastWeek1 = createBrushingStat(today.minusDays(7))
        val notLastWeek2 = createBrushingStat(today.minusDays(8))
        val brushings = listOf(
            lastWeekDate1,
            lastWeekDate2,
            lastWeekDate3,
            lastWeekDate4,
            lastWeekDate5,
            lastWeekDate6,
            lastWeekDate7,
            notLastWeek1,
            notLastWeek2
        )

        val result = lastWeekOnly(brushings)
        assertTrue(result.contains(lastWeekDate1))
        assertTrue(result.contains(lastWeekDate2))
        assertTrue(result.contains(lastWeekDate3))
        assertTrue(result.contains(lastWeekDate4))
        assertTrue(result.contains(lastWeekDate5))
        assertTrue(result.contains(lastWeekDate5))
        assertTrue(result.contains(lastWeekDate6))
        assertTrue(result.contains(lastWeekDate7))
        assertFalse(result.contains(notLastWeek1))
        assertFalse(result.contains(notLastWeek2))
    }

    @Test
    fun `secondWeekOnly returns Brushings for second week`() {
        val today = LocalDate.now()
        val lastWeekDate1 = createBrushingStat(today)
        val lastWeekDate2 = createBrushingStat(today.minusDays(1))
        val lastWeekDate3 = createBrushingStat(today.minusDays(2))
        val lastWeekDate4 = createBrushingStat(today.minusDays(3))
        val lastWeekDate5 = createBrushingStat(today.minusDays(4))
        val lastWeekDate6 = createBrushingStat(today.minusDays(5))
        val lastWeekDate7 = createBrushingStat(today.minusDays(6))
        val secondWeekDate1 = createBrushingStat(today.minusDays(7))
        val secondWeekDate2 = createBrushingStat(today.minusDays(8))
        val secondWeekDate3 = createBrushingStat(today.minusDays(9))
        val secondWeekDate4 = createBrushingStat(today.minusDays(10))
        val secondWeekDate5 = createBrushingStat(today.minusDays(11))
        val secondWeekDate6 = createBrushingStat(today.minusDays(12))
        val secondWeekDate7 = createBrushingStat(today.minusDays(13))
        val thirdWeekDate1 = createBrushingStat(today.minusDays(14))
        val thirdWeekDate2 = createBrushingStat(today.minusDays(15))
        val brushings = listOf(
            lastWeekDate1,
            lastWeekDate2,
            lastWeekDate3,
            lastWeekDate4,
            lastWeekDate5,
            lastWeekDate6,
            lastWeekDate7,
            secondWeekDate1,
            secondWeekDate2,
            secondWeekDate3,
            secondWeekDate4,
            secondWeekDate5,
            secondWeekDate6,
            secondWeekDate7,
            thirdWeekDate1,
            thirdWeekDate2
        )

        val result = secondWeekOnly(brushings)
        assertFalse(result.contains(lastWeekDate1))
        assertFalse(result.contains(lastWeekDate2))
        assertFalse(result.contains(lastWeekDate3))
        assertFalse(result.contains(lastWeekDate4))
        assertFalse(result.contains(lastWeekDate5))
        assertFalse(result.contains(lastWeekDate5))
        assertFalse(result.contains(lastWeekDate6))
        assertFalse(result.contains(lastWeekDate7))
        assertTrue(result.contains(secondWeekDate1))
        assertTrue(result.contains(secondWeekDate2))
        assertTrue(result.contains(secondWeekDate3))
        assertTrue(result.contains(secondWeekDate4))
        assertTrue(result.contains(secondWeekDate5))
        assertTrue(result.contains(secondWeekDate6))
        assertTrue(result.contains(secondWeekDate7))
        assertFalse(result.contains(thirdWeekDate1))
        assertFalse(result.contains(thirdWeekDate2))
    }

    @Test
    fun `lastTwoWeeksOnly returns Brushings for second week`() {
        val today = LocalDate.now()
        val lastTwoWeeksDate1 = createBrushingStat(today)
        val lastTwoWeeksDate2 = createBrushingStat(today.minusDays(1))
        val lastTwoWeeksDate3 = createBrushingStat(today.minusDays(2))
        val lastTwoWeeksDate4 = createBrushingStat(today.minusDays(3))
        val lastTwoWeeksDate5 = createBrushingStat(today.minusDays(4))
        val lastTwoWeeksDate6 = createBrushingStat(today.minusDays(5))
        val lastTwoWeeksDate7 = createBrushingStat(today.minusDays(6))
        val lastTwoWeeksDate8 = createBrushingStat(today.minusDays(7))
        val lastTwoWeeksDate9 = createBrushingStat(today.minusDays(8))
        val lastTwoWeeksDate10 = createBrushingStat(today.minusDays(9))
        val lastTwoWeeksDate11 = createBrushingStat(today.minusDays(10))
        val lastTwoWeeksDate12 = createBrushingStat(today.minusDays(11))
        val lastTwoWeeksDate13 = createBrushingStat(today.minusDays(12))
        val lastTwoWeeksDate14 = createBrushingStat(today.minusDays(13))
        val thirdWeekDate1 = createBrushingStat(today.minusDays(14))
        val thirdWeekDate2 = createBrushingStat(today.minusDays(15))
        val brushings = listOf(
            lastTwoWeeksDate1,
            lastTwoWeeksDate2,
            lastTwoWeeksDate3,
            lastTwoWeeksDate4,
            lastTwoWeeksDate5,
            lastTwoWeeksDate6,
            lastTwoWeeksDate7,
            lastTwoWeeksDate8,
            lastTwoWeeksDate9,
            lastTwoWeeksDate10,
            lastTwoWeeksDate11,
            lastTwoWeeksDate12,
            lastTwoWeeksDate13,
            lastTwoWeeksDate14,
            thirdWeekDate1,
            thirdWeekDate2
        )

        val result = lastTwoWeeksOnly(brushings)
        assertTrue(result.contains(lastTwoWeeksDate1))
        assertTrue(result.contains(lastTwoWeeksDate2))
        assertTrue(result.contains(lastTwoWeeksDate3))
        assertTrue(result.contains(lastTwoWeeksDate4))
        assertTrue(result.contains(lastTwoWeeksDate5))
        assertTrue(result.contains(lastTwoWeeksDate6))
        assertTrue(result.contains(lastTwoWeeksDate7))
        assertTrue(result.contains(lastTwoWeeksDate8))
        assertTrue(result.contains(lastTwoWeeksDate9))
        assertTrue(result.contains(lastTwoWeeksDate10))
        assertTrue(result.contains(lastTwoWeeksDate11))
        assertTrue(result.contains(lastTwoWeeksDate12))
        assertTrue(result.contains(lastTwoWeeksDate13))
        assertTrue(result.contains(lastTwoWeeksDate14))
        assertFalse(result.contains(thirdWeekDate1))
        assertFalse(result.contains(thirdWeekDate2))
    }

    @Test
    fun `lastThreeDaysOnly returns Brushings for last 3 days`() {
        val today = LocalDate.now()
        val lastDayDate1 = createBrushingStat(today)
        val lastDayDate2 = createBrushingStat(today.minusDays(1))
        val lastDayDate3 = createBrushingStat(today.minusDays(2))
        val lastDayDate4 = createBrushingStat(today.minusDays(3))
        val lastDayDate5 = createBrushingStat(today.minusDays(4))
        val brushings = listOf(
            lastDayDate1,
            lastDayDate2,
            lastDayDate3,
            lastDayDate3,
            lastDayDate4,
            lastDayDate5
        )

        val result = lastThreeDaysOnly(brushings)
        assertTrue(result.contains(lastDayDate1))
        assertTrue(result.contains(lastDayDate2))
        assertTrue(result.contains(lastDayDate3))
        assertFalse(result.contains(lastDayDate4))
        assertFalse(result.contains(lastDayDate5))
    }

    @Test
    fun `isDateInPeriod returns true if date is between begin and end dates`() {
        val date = LocalDate.now()
        val begin = date.minusDays(3)
        val end = date.minusDays(1)
        assertFalse(isDateInPeriod(date.minusDays(4), begin, end))
        assertTrue(isDateInPeriod(date.minusDays(3), begin, end))
        assertTrue(isDateInPeriod(date.minusDays(2), begin, end))
        assertTrue(isDateInPeriod(date.minusDays(1), begin, end))
        assertFalse(isDateInPeriod(date.minusDays(0), begin, end))
    }

    private fun createBrushingStat(date: LocalDate) = BrushingStat(
        date = date,
        coverage = 0,
        type = BrushingType.OfflineBrushing
    )
}
