/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.statsoffline.models.AggregatedStats
import com.kolibree.statsoffline.test.createDayWithSessions
import com.kolibree.statsoffline.test.mockDayWithSessions
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate

class AggregatedStatsExtensionsKtTest : BaseUnitTest() {

    /*
    calculateSessionsPerDay
     */
    @Test
    fun `calculateSessionsPerDay returns the number of sessions per day`() {
        val today = TrustedClock.getNowZonedDateTime().toLocalDate()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)

        val todayWithSessions = createDayWithSessions(
            sessions = listOf(mock(), mock(), mock(), mock())
        )
        val yesterdayWithSessions = createDayWithSessions(
            sessions = listOf(mock())
        )
        val tomorrowWithSessions = createDayWithSessions(
            sessions = listOf()
        )

        val dayWithSessions = mapOf(
            yesterday to yesterdayWithSessions,
            today to todayWithSessions,
            tomorrow to tomorrowWithSessions
        )

        val expectedSessionsPerDay = (5.0 / 3).roundOneDecimal()

        TestCase.assertEquals(expectedSessionsPerDay, dayWithSessions.calculateSessionsPerDay())
    }

    @Test
    fun `calculateSessionsPerDay returns 0 if map is empty`() {
        assertEquals(0.0, mapOf<LocalDate, AggregatedStats>().calculateSessionsPerDay(), 0.0)
    }

    /*
    totalSessions
     */
    @Test
    fun `totalSessions returns the total number of BrushingSessionStatEntity in the map`() {
        val today = TrustedClock.getNowZonedDateTime().toLocalDate()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)

        val todayWithSessions = createDayWithSessions(
            sessions = listOf(mock(), mock())
        )
        val yesterdayWithSessions = createDayWithSessions(
            sessions = listOf(mock())
        )
        val tomorrowWithSessions = createDayWithSessions(
            sessions = listOf()
        )

        val dayWithSessions = mapOf(
            yesterday to yesterdayWithSessions,
            today to todayWithSessions,
            tomorrow to tomorrowWithSessions
        )

        TestCase.assertEquals(3, dayWithSessions.totalSessions())
    }

    /*
    filterNotFuture
     */
    @Test
    fun `filterNotFuture returns all BrushingSessionStatEntity in the list`() {
        val currentMonthDay2 = TrustedClock.getNowZonedDateTime().withDayOfMonth(2)
        TrustedClock.setFixedDate(currentMonthDay2)

        val today = currentMonthDay2.toLocalDate()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)

        val todayWithSessions = mockDayWithSessions()
        val yesterdayWithSessions = mockDayWithSessions()
        val tomorrowWithSessions = mockDayWithSessions()

        val dayWithSessions = mapOf(
            yesterday to yesterdayWithSessions,
            today to todayWithSessions,
            tomorrow to tomorrowWithSessions
        )

        val mapWithoutFutureDates = dayWithSessions.filterNotFuture()

        TestCase.assertEquals(2, mapWithoutFutureDates.size)
        TestCase.assertTrue(mapWithoutFutureDates.containsKey(today))
        TestCase.assertTrue(mapWithoutFutureDates.containsKey(yesterday))
    }
}
