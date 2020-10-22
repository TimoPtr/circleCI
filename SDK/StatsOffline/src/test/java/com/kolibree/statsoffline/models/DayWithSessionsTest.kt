/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.kml.MouthZone16
import com.kolibree.statsoffline.calculateAverageForDaysNotInTheFuture
import com.kolibree.statsoffline.test.createAverageCheckup
import com.kolibree.statsoffline.test.createDayAggregatedStatsEntity
import com.kolibree.statsoffline.test.createSessionStatsEntity
import com.kolibree.statsoffline.test.mockDayWithSessions
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DayWithSessionsTest : BaseUnitTest() {

    /*
    withNewBrushingSessions
     */
    @Test
    fun `withNewBrushingSessions returns an instance with the sessions added`() {
        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = listOf()
        )

        assertTrue(dayWithSessions.brushingSessions.isEmpty())

        val session1 = createSessionStatsEntity(duration = 50)
        val session2 = createSessionStatsEntity(duration = 80)

        val newSessions = listOf(session1, session2)

        val newDayWithSessions = dayWithSessions.withNewBrushingSessions(newSessions)

        assertEquals(newSessions.size, newDayWithSessions.brushingSessions.size)
        assertTrue(newDayWithSessions.brushingSessions.containsAll(newSessions))
    }

    @Test
    fun `withNewBrushingSessions adds sessions to preexisting list`() {
        val previousSession = createSessionStatsEntity(duration = 10)
        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = listOf(previousSession)
        )

        val session1 = createSessionStatsEntity(duration = 50)
        val session2 = createSessionStatsEntity(duration = 80)

        val newSessions = listOf(session1, session2)

        val newDayWithSessions = dayWithSessions.withNewBrushingSessions(newSessions)

        assertEquals(newSessions.size + 1, newDayWithSessions.brushingSessions.size)
        assertTrue(newDayWithSessions.brushingSessions.containsAll(newSessions + previousSession))
    }

    @Test
    fun `withNewBrushingSessions does not touch totalSessions`() {
        val previousSession = createSessionStatsEntity(duration = 10)
        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(totalSessions = 1),
            brushingSessions = listOf(previousSession)
        )

        val session1 = createSessionStatsEntity(duration = 50)
        val session2 = createSessionStatsEntity(duration = 80)

        val newSessions = listOf(session1, session2)

        assertEquals(1, dayWithSessions.totalSessions)

        val newDayWithSessions = dayWithSessions.withNewBrushingSessions(newSessions)

        assertEquals(1, newDayWithSessions.totalSessions)
    }

    /*
    calculateAverage
     */

    @Test
    fun `calculateAverage returns instance with updated totalSessions`() {
        val sessions = listOf(
            createSessionStatsEntity(duration = 10),
            createSessionStatsEntity(duration = 50),
            createSessionStatsEntity(duration = 80)
        )
        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = sessions
        )

        assertEquals(0, dayWithSessions.totalSessions)

        val newDayWithSessions = dayWithSessions.calculateAverage()

        assertEquals(3, newDayWithSessions.totalSessions)
    }

    @Test
    fun `calculateAverage returns instance with BrushingDayStatEntity with new isPerfectDay`() {
        val session1 = createSessionStatsEntity(averageSurface = 98)
        val session2 = createSessionStatsEntity(averageSurface = 80)

        val sessions = listOf(session1, session2)

        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = sessions
        )

        assertFalse(dayWithSessions.isPerfectDay)

        val newDayWithSessions = dayWithSessions.calculateAverage()

        assertTrue(newDayWithSessions.isPerfectDay)
    }

    @Test
    fun `calculateAverage returns instance with BrushingDayStatEntity with new aggregated duration`() {
        val session1 = createSessionStatsEntity(duration = 50)
        val session2 = createSessionStatsEntity(duration = 80)

        val sessions = listOf(session1, session2)

        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = sessions
        )

        val expectedAverageDuration = sessions.map { it.duration }.average()

        val newDayWithSessions = dayWithSessions.calculateAverage()

        assertEquals(expectedAverageDuration, newDayWithSessions.averageDuration)
    }

    @Test
    fun `calculateAverage returns instance with BrushingDayStatEntity with new aggregated average surface`() {
        val session1 = createSessionStatsEntity(averageSurface = 50)
        val session2 = createSessionStatsEntity(averageSurface = 80)

        val sessions = listOf(session1, session2)

        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = sessions
        )

        val expectedAverageSurface = sessions.map { it.averageSurface }.average()

        val newDayWithSessions = dayWithSessions.calculateAverage()

        assertEquals(expectedAverageSurface, newDayWithSessions.averageSurface)
    }

    @Test
    fun `calculateAverage returns instance with BrushingDayStatEntity with new average surface map ignoring sessions without checkup data`() {
        val mouthZone1 = MouthZone16.UpMolRiOcc
        val mouthZone2 = MouthZone16.LoMolLeInt
        val mouthZone3 = MouthZone16.UpIncExt
        val mouthZone4 = MouthZone16.UpIncInt
        val mapSession1 =
            createAverageCheckup(mapOf(mouthZone1 to 80f, mouthZone2 to 5f, mouthZone3 to 2f))
        val mapSession2 =
            createAverageCheckup(mapOf(mouthZone1 to 23f, mouthZone2 to 78f, mouthZone4 to 10f))
        val session1 = createSessionStatsEntity(checkup = mapSession1)
        val session2 = createSessionStatsEntity(checkup = mapSession2)
        val sessionToBeIgnored = createSessionStatsEntity()

        val sessions = listOf(session1, session2, sessionToBeIgnored)

        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = sessions
        )

        val newDayWithSessions = dayWithSessions.calculateAverage()

        val expectedAvgZone1 = (80f + 23f) / 2
        val expectedAvgZone2 = (5f + 78f) / 2
        val expectedAvgZone3 = (2f + 0f) / 2
        val expectedAvgZone4 = (0f + 10f) / 2

        val expectedIMap = MouthZone16.values().associate { mouthZone ->
            mouthZone to when (mouthZone) {
                mouthZone1 -> expectedAvgZone1
                mouthZone2 -> expectedAvgZone2
                mouthZone3 -> expectedAvgZone3
                mouthZone4 -> expectedAvgZone4
                else -> 0f
            }
        }

        assertEquals(expectedIMap, newDayWithSessions.averageCheckup)
    }

    @Test
    fun `calculateAverage returns new instance with average correctMovementAverage from DayWithSessions`() {
        val correctMovementAverage1 = 84.3
        val correctMovementAverage2 = 13.6

        val sessions = listOf(
            createSessionStatsEntity(correctMovementAverage = correctMovementAverage1),
            createSessionStatsEntity(correctMovementAverage = correctMovementAverage2),
            createSessionStatsEntity(correctMovementAverage = 0.0) // should be ignored
        )

        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = sessions
        )

        val newDaysWithSessions = dayWithSessions.calculateAverage()

        val expected = 48.9 // 48.95
        assertEquals(expected, newDaysWithSessions.correctMovementAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average correctOrientationAverage from DayWithSessions`() {
        val correctOrientationAverage1 = 76.0
        val correctOrientationAverage2 = 25.1

        val sessions = listOf(
            createSessionStatsEntity(correctOrientationAverage = correctOrientationAverage1),
            createSessionStatsEntity(correctOrientationAverage = correctOrientationAverage2),
            createSessionStatsEntity(correctOrientationAverage = 0.0)
        )

        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = sessions
        )

        val newDaysWithSessions = dayWithSessions.calculateAverage()

        val expected = 50.6 // 50.55

        assertEquals(expected, newDaysWithSessions.correctOrientationAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average underSpeedAverage from DayWithSessions`() {
        val underSpeedAverage1 = 0.4
        val underSpeedAverage2 = 7.98
        val underSpeedAverage3 = 99.7

        val sessions = listOf(
            createSessionStatsEntity(underSpeedAverage = underSpeedAverage1),
            createSessionStatsEntity(underSpeedAverage = underSpeedAverage2),
            createSessionStatsEntity(underSpeedAverage = underSpeedAverage3),
            createSessionStatsEntity(underSpeedAverage = 0.0)
        )

        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = sessions
        )

        val newDaysWithSessions = dayWithSessions.calculateAverage()

        val expected = 36.0 // 36.026

        assertEquals(expected, newDaysWithSessions.underSpeedAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average correctSpeedAverage from DayWithSessions`() {
        val correctSpeedAverage1 = 56.1
        val correctSpeedAverage2 = 88.13
        val correctSpeedAverage3 = 6.8

        val sessions = listOf(
            createSessionStatsEntity(correctSpeedAverage = correctSpeedAverage1),
            createSessionStatsEntity(correctSpeedAverage = correctSpeedAverage2),
            createSessionStatsEntity(correctSpeedAverage = correctSpeedAverage3),
            createSessionStatsEntity(correctSpeedAverage = 0.0)
        )

        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = sessions
        )

        val newDaysWithSessions = dayWithSessions.calculateAverage()

        val expected = 50.3 // 50,34

        assertEquals(expected, newDaysWithSessions.correctSpeedAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average overSpeedAverage from DayWithSessions`() {
        val overSpeedAverage1 = 0.3
        val overSpeedAverage2 = 5.99
        val overSpeedAverage3 = 37.23

        val sessions = listOf(
            createSessionStatsEntity(overSpeedAverage = overSpeedAverage1),
            createSessionStatsEntity(overSpeedAverage = overSpeedAverage2),
            createSessionStatsEntity(overSpeedAverage = overSpeedAverage3),
            createSessionStatsEntity(overSpeedAverage = 0.0)
        )

        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = sessions
        )

        val newDaysWithSessions = dayWithSessions.calculateAverage()

        val expected = 14.5 // 14.506

        assertEquals(expected, newDaysWithSessions.overSpeedAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average overPressureAverage from DayWithSessions`() {
        val overPressureAverage1 = 23.9
        val overPressureAverage2 = 8.64
        val overPressureAverage3 = 5.23

        val sessions = listOf(
            createSessionStatsEntity(overPressureAverage = overPressureAverage1),
            createSessionStatsEntity(overPressureAverage = overPressureAverage2),
            createSessionStatsEntity(overPressureAverage = overPressureAverage3),
            createSessionStatsEntity(overPressureAverage = 0.0)
        )

        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = sessions
        )

        val newDaysWithSessions = dayWithSessions.calculateAverage()

        val expected = 12.6 // 12.59

        assertEquals(expected, newDaysWithSessions.overPressureAverage)
    }

    /*
    calculateAverageSurface
     */
    @Test
    fun `calculateAverageSurface returns 0 if there are 0 sessions`() {
        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = listOf()
        )

        assertEquals(0.toDouble(), dayWithSessions.calculateAverageSurface())
    }

    /*
    calculateAverageDuration
     */

    @Test
    fun `calculateAverageDuration returns 0 if there are 0 sessions`() {
        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = listOf()
        )

        assertEquals(0.toDouble(), dayWithSessions.calculateAverageDuration())
    }

    /*
    calculateAverageForDaysNotInTheFuture
     */
    @Test
    fun `calculateAverageForDaysNotInTheFuture only invokes calculateAverage on days that are not in the future`() {
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

        dayWithSessions.calculateAverageForDaysNotInTheFuture()

        verify(todayWithSessions).calculateAverage()
        verify(yesterdayWithSessions).calculateAverage()
        verify(tomorrowWithSessions, never()).calculateAverage()
    }

    @Test
    fun `calculateAverageForDaysNotInTheFuture returns a map only with days that are not in the future`() {
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

        val dayWithSessionsMap = dayWithSessions.calculateAverageForDaysNotInTheFuture()
        assertEquals(2, dayWithSessionsMap.size)

        assertTrue(dayWithSessionsMap.containsKey(today))
        assertTrue(dayWithSessionsMap.containsKey(yesterday))
    }

    /**
     * isPerfectDay
     */
    private val perfectDayForSingleBrushing: (averageSurface: Int) -> Boolean = { averageSurface ->
        val previousSession = createSessionStatsEntity(averageSurface = averageSurface)
        val dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = listOf(previousSession)
        )
        dayWithSessions.calculateAverage().isPerfectDay
    }

    private val perfectDayForMultipleBrushings: (averageSurfaces: IntArray) -> Boolean =
        { averageSurfaces ->
            val dayWithSessions = DayWithSessions(
                dayStats = createDayAggregatedStatsEntity(),
                brushingSessions = averageSurfaces.map { createSessionStatsEntity(averageSurface = it) }
            )

            dayWithSessions.calculateAverage().isPerfectDay
        }

    @Test
    fun `isPerfectDay returns true for day with two or more brushings with coverage 80 or above`() {
        assertTrue(perfectDayForMultipleBrushings(intArrayOf(80, 80)))
        assertTrue(perfectDayForMultipleBrushings(intArrayOf(100, 80)))
        assertTrue(perfectDayForMultipleBrushings(intArrayOf(80, 100)))
        assertTrue(perfectDayForMultipleBrushings(intArrayOf(80, 0, 80)))
        assertTrue(perfectDayForMultipleBrushings(intArrayOf(100, 0, 80)))
        assertTrue(perfectDayForMultipleBrushings(intArrayOf(100, 0, 100)))
        assertTrue(perfectDayForMultipleBrushings(intArrayOf(100, 100, 100)))
    }

    @Test
    fun `isPerfectDay returns false for day with only one brushing, regardless of coverage`() {
        assertFalse(perfectDayForSingleBrushing(0))
        assertFalse(perfectDayForSingleBrushing(10))
        assertFalse(perfectDayForSingleBrushing(50))
        assertFalse(perfectDayForSingleBrushing(80))
        assertFalse(perfectDayForSingleBrushing(100))
    }

    @Test
    fun `isPerfectDay returns false for day with multiple brushings if the coverage is too low`() {
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(0, 0)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(0, 50)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(0, 70)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(0, 80)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(0, 100)))

        assertFalse(perfectDayForMultipleBrushings(intArrayOf(70, 0)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(70, 50)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(70, 70)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(70, 80)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(70, 100)))

        assertFalse(perfectDayForMultipleBrushings(intArrayOf(80, 0)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(80, 50)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(80, 70)))

        assertFalse(perfectDayForMultipleBrushings(intArrayOf(100, 0)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(100, 50)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(100, 70)))

        assertFalse(perfectDayForMultipleBrushings(intArrayOf(100, 0, 70)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(100, 50, 50)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(100, 79, 0)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(79, 79, 79)))
        assertFalse(perfectDayForMultipleBrushings(intArrayOf(79, 100, 79)))
    }

    @Test
    fun `isPerfectDay is recalculated after adding new brushings with 80 percent coverage`() {
        var dayWithSessions = DayWithSessions(
            dayStats = createDayAggregatedStatsEntity(),
            brushingSessions = listOf()
        )
        assertFalse(dayWithSessions.isPerfectDay)

        val session1 = createSessionStatsEntity(averageSurface = 80)
        dayWithSessions = dayWithSessions.withReculatedAverageFromSessions(listOf(session1))
        assertFalse(dayWithSessions.isPerfectDay)

        val session2 = createSessionStatsEntity(averageSurface = 80)
        dayWithSessions =
            dayWithSessions.withReculatedAverageFromSessions(listOf(session1, session2))
        assertTrue(dayWithSessions.isPerfectDay)
    }
}
