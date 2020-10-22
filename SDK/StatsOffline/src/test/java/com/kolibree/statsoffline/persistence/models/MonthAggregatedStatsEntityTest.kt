/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.statsoffline.models.YearWeek
import com.kolibree.statsoffline.test.DEFAULT_PROFILE_ID
import com.kolibree.statsoffline.test.createDayAggregatedStatsEntity
import com.kolibree.statsoffline.test.createWeekAggregatedStatEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.Month
import org.threeten.bp.YearMonth

class MonthAggregatedStatsEntityTest : BaseUnitTest() {
    @Test
    fun `dates returns a LocalDate for each day of the month`() {
        val august = YearMonth.of(2019, Month.AUGUST)
        val monthAggregatedStatsEntity = MonthAggregatedStatsEntity(DEFAULT_PROFILE_ID, august)

        val dates = monthAggregatedStatsEntity.dates
        assertEquals(31, dates.size)

        (1..31).forEach { day ->
            assertTrue(dates.contains(august.atDay(day)))
        }
    }

    @Test
    fun `createEmptyDayStats returns a DayAggregatedStatsEntity for each day of the month`() {
        val august = YearMonth.of(2019, Month.AUGUST)
        val profileId = 4343L
        val monthAggregatedStatsEntity = MonthAggregatedStatsEntity(profileId, august)

        val emptyDayStats = monthAggregatedStatsEntity.createEmptyDayStats()

        (1..31).forEach { day ->
            val expectedDay = createDayAggregatedStatsEntity(profileId = profileId, day = august.atDay(day))
            assertTrue(emptyDayStats.contains(expectedDay))
        }
    }

    @Test
    fun `createEmptyWeekStats returns 5 WeekAggregatedStatsEntity for august 2019`() {
        val year = 2019
        val august = YearMonth.of(year, Month.AUGUST)
        val profileId = 4343L
        val monthAggregatedStatsEntity = MonthAggregatedStatsEntity(profileId, august)

        val emptyWeekStats = monthAggregatedStatsEntity.createEmptyWeekStats()

        assertEquals(5, emptyWeekStats.size)

        val firstAugustWeek = YearWeek.from(august.atDay(1)).week
        (0..4).forEach {
            val expectedWeek = createWeekAggregatedStatEntity(
                profileId = profileId,
                yearWeek = YearWeek.of(year, firstAugustWeek + it)
            )
            assertTrue(emptyWeekStats.contains(expectedWeek))
        }
    }

    @Test
    fun `createEmptyWeekStats returns 6 WeekAggregatedStatsEntity for December 2018`() {
        val year = 2018
        val december = YearMonth.of(year, Month.DECEMBER)
        val profileId = 4343L
        val monthAggregatedStatsEntity = MonthAggregatedStatsEntity(profileId, december)

        val emptyWeekStats = monthAggregatedStatsEntity.createEmptyWeekStats()

        assertEquals(6, emptyWeekStats.size)

        val firstDecemberWeek = YearWeek.from(december.atDay(1)).week
        (0..5).forEach {
            val expectedWeek = createWeekAggregatedStatEntity(
                profileId = profileId,
                yearWeek = YearWeek.of(year, firstDecemberWeek + it)
            )
            assertTrue(emptyWeekStats.contains(expectedWeek))
        }
    }
}
