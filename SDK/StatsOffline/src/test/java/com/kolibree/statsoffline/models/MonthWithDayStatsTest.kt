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
import com.kolibree.statsoffline.roundOneDecimal
import com.kolibree.statsoffline.roundOneDecimalToFloat
import com.kolibree.statsoffline.test.createAverageCheckup
import com.kolibree.statsoffline.test.createDayAggregatedStatsEntity
import com.kolibree.statsoffline.test.createDayWithSessions
import com.kolibree.statsoffline.test.createMonthAggregatedStatEntity
import com.kolibree.statsoffline.test.createMonthWithDayStats
import com.kolibree.statsoffline.test.createSessionStatsEntity
import com.kolibree.statsoffline.test.mockDayWithSessions
import com.kolibree.statsoffline.test.toYearMonth
import com.nhaarman.mockitokotlin2.spy
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.YearMonth
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class MonthWithDayStatsTest : BaseUnitTest() {

    /*
    empty
     */
    @Test
    fun `empty returns empty aggregated data`() {
        val profileId = 45L
        val month = YearMonth.now()
        val emptyMonthWithDays = MonthWithDayStats.empty(profileId, month)

        assertEquals(profileId, emptyMonthWithDays.profileId)
        assertEquals(month, emptyMonthWithDays.month)
        assertEquals(0.0, emptyMonthWithDays.averageDuration)
        assertEquals(0.0, emptyMonthWithDays.averageSurface)
        assertEquals(emptyAverageCheckup(), emptyMonthWithDays.averageCheckup)

        val recalculatedStats = emptyMonthWithDays.calculateAverage()

        assertEquals(profileId, emptyMonthWithDays.profileId)
        assertEquals(month, emptyMonthWithDays.month)
        assertEquals(0.0, recalculatedStats.averageDuration)
        assertEquals(0.0, recalculatedStats.averageSurface)
        assertEquals(emptyAverageCheckup(), recalculatedStats.averageCheckup)
    }

    /*
    withNewSessions
     */
    @Test(expected = IllegalArgumentException::class)
    fun `withNewSessions throws IllegalArgumentException if sessions is empty and we try to add sessions`() {
        val emptyMonthWithDays =
            MonthWithDayStats(monthStats = createMonthAggregatedStatEntity(), dayStats = mapOf())

        emptyMonthWithDays.withNewSessions(TrustedClock.getNowLocalDate(), listOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `withNewSessions throws IllegalArgumentException if localDate doesn't exist in sessions map`() {
        val monthWithDays =
            createMonthWithDayStats(dayStats = mapOf(TrustedClock.getNowLocalDate() to createDayWithSessions()))

        monthWithDays.withNewSessions(TrustedClock.getNowLocalDate().minusMonths(1), listOf())
    }

    @Test
    fun `withNewSessions adds sessions to DayWithSessions corresponding to date even if it's empty`() {
        val date = TrustedClock.getNowLocalDate()
        val monthWithDays =
            createMonthWithDayStats(dayStats = mapOf(date to createDayWithSessions()))

        assertTrue(monthWithDays.dayStats.getValue(date).brushingSessions.isEmpty())

        val newSession = createSessionStatsEntity(creationTime = date.atTime(12, 0), duration = 50)
        val newMonthWithDays = monthWithDays.withNewSessions(date, listOf(newSession))

        assertEquals(newSession, newMonthWithDays.dayStats.getValue(date).brushingSessions.single())
    }

    @Test
    fun `withNewSessions adds sessions to DayWithSessions corresponding to date`() {
        val date = TrustedClock.getNowLocalDate()
        val preexistingSession = createSessionStatsEntity(duration = 50)
        val monthWithDays = createMonthWithDayStats(
            dayStats = mapOf(date to createDayWithSessions(sessions = listOf(preexistingSession)))
        )

        assertTrue(monthWithDays.dayStats.getValue(date).brushingSessions.isNotEmpty())

        val newSession = createSessionStatsEntity(creationTime = date.atTime(12, 0), duration = 20)
        val newMonthWithDays = monthWithDays.withNewSessions(date, listOf(newSession))

        val sessions = newMonthWithDays.dayStats.getValue(date).brushingSessions
        assertEquals(2, sessions.size)
        assertTrue(sessions.containsAll(listOf(preexistingSession, newSession)))
    }

    @Test
    fun `withNewSessions does not touch totalSessions`() {
        val date1 = TrustedClock.getNowLocalDate().withDayOfMonth(2)
        val date2 = date1.minusDays(1)
        val monthWithDays = createMonthWithDayStats(
            dayStats = mapOf(
                date1 to createDayWithSessions(
                    dayAggregatedEntity = createDayAggregatedStatsEntity(day = date1),
                    sessions = listOf(
                        createSessionStatsEntity(creationTime = date1.atTime(12, 0)),
                        createSessionStatsEntity(creationTime = date2.atTime(11, 0))
                    )
                ),
                date2 to createDayWithSessions(
                    dayAggregatedEntity = createDayAggregatedStatsEntity(day = date2),
                    sessions = listOf(
                        createSessionStatsEntity(
                            creationTime = date2.atTime(12, 0)
                        )
                    )
                )
            )
        )

        assertEquals(0, monthWithDays.totalSessions)

        val newSession = createSessionStatsEntity(creationTime = date1.plusDays(3).atTime(11, 0))
        val newMonthWithDays = monthWithDays.withNewSessions(date2, listOf(newSession))

        assertEquals(0, newMonthWithDays.totalSessions)
    }

    /*
    calculateAverage
     */

    @Test
    fun `calculateAverage returns instance with updated total sessions value`() {
        val day2 = TrustedClock.getNowLocalDate().withDayOfMonth(2)
        val day1 = day2.minusDays(1)

        TrustedClock.setFixedDate(
            ZonedDateTime.of(
                day2.atTime(5, 0), // ensure kolibree day == day2
                ZoneId.of("America/New_York")
            )
        )

        val monthWithDays = createMonthWithDayStats(
            dayStats = mapOf(
                day2 to createDayWithSessions(
                    dayAggregatedEntity = createDayAggregatedStatsEntity(day = day2),
                    sessions = listOf(
                        createSessionStatsEntity(creationTime = day2.atTime(12, 0)),
                        createSessionStatsEntity(creationTime = day1.atTime(11, 0))
                    )
                ),
                day1 to createDayWithSessions(
                    dayAggregatedEntity = createDayAggregatedStatsEntity(day = day1),
                    sessions = listOf(
                        createSessionStatsEntity(
                            creationTime = day1.atTime(12, 0)
                        )
                    )
                )
            )
        )

        assertEquals(0, monthWithDays.totalSessions)

        assertEquals(3, monthWithDays.calculateAverage().totalSessions)
    }

    @Test
    fun `calculateAverage returns instance with exact same dates`() {
        val fixedDate = TrustedClock.getNowZonedDateTime().withDayOfMonth(2)
        TrustedClock.setFixedDate(fixedDate)

        val today = fixedDate.toLocalDate()
        val yesterday = today.minusDays(1)

        val currentMonth = fixedDate.toYearMonth()

        val dayWithSessionsMap =
            mapOf(today to mockDayWithSessions(), yesterday to mockDayWithSessions())
        val monthWithDays = spy(createMonthWithDayStats(dayStats = dayWithSessionsMap))

        assertEquals(currentMonth.lengthOfMonth(), monthWithDays.dayStats.size)

        val newMonthWithDays = monthWithDays.calculateAverage()

        assertEquals(currentMonth.lengthOfMonth(), newMonthWithDays.dayStats.size)
    }

    @Test
    fun `calculateAverage returns new instance with average duration of DayWithSessions`() {
        val fixedDate = TrustedClock.getNowZonedDateTime().withDayOfMonth(2)
        TrustedClock.setFixedDate(fixedDate)

        val today = fixedDate.toLocalDate()
        val yesterday = today.minusDays(1)

        val averageDurationToday = 80.0
        val todaySession = mockDayWithSessions(averageDuration = averageDurationToday)

        val averageDurationYesterday = 41.0
        val yesterdaySession = mockDayWithSessions(averageDuration = averageDurationYesterday)

        val dayWithSessionsMap = mapOf(today to todaySession, yesterday to yesterdaySession)
        val monthWithDays = createMonthWithDayStats(dayStats = dayWithSessionsMap)

        val newMonthWithDays = monthWithDays.calculateAverage()

        val expectedDuration = (averageDurationToday + averageDurationYesterday) / today.dayOfMonth
        assertEquals(expectedDuration.roundOneDecimal(), newMonthWithDays.averageDuration)
    }

    @Test
    fun `calculateAverage returns new instance with average surface of DayWithSessions`() {
        val fixedDate = TrustedClock.getNowZonedDateTime().withDayOfMonth(2)
        TrustedClock.setFixedDate(fixedDate)

        val date1 = fixedDate.toLocalDate()
        val date2 = TrustedClock.getNowLocalDate().minusDays(1)

        val averageSurface1 = 80.0
        val dayWithSessions1 = mockDayWithSessions(averageSurface = averageSurface1)

        val averageSurface2 = 41.0
        val dayWithSessions2 = mockDayWithSessions(averageSurface = averageSurface2)

        val dayWithSessionsMap = mapOf(date1 to dayWithSessions1, date2 to dayWithSessions2)
        val monthWithDays = createMonthWithDayStats(dayStats = dayWithSessionsMap)

        val newMonthWithDays = monthWithDays.calculateAverage()

        val expectedSurface = (averageSurface1 + averageSurface2) / date1.dayOfMonth
        assertEquals(expectedSurface.roundOneDecimal(), newMonthWithDays.averageSurface)
    }

    @Test
    fun `calculateAverage returns new instance with average checkup from DayWithSessions`() {
        val date1 = TrustedClock.getNowLocalDate()
        val date2 = TrustedClock.getNowLocalDate().minusDays(1)

        val mouthZone1 = MouthZone16.UpMolRiOcc
        val mouthZone2 = MouthZone16.LoMolLeInt
        val mouthZone3 = MouthZone16.UpIncExt
        val mouthZone4 = MouthZone16.UpIncInt
        val averageCheckup1 =
            createAverageCheckup(mapOf(mouthZone1 to 80f, mouthZone2 to 5f, mouthZone3 to 2f))
        val averageCheckup2 =
            createAverageCheckup(mapOf(mouthZone1 to 23f, mouthZone2 to 78f, mouthZone4 to 10f))

        val dayWithSessionsMap = mapOf(
            date1 to mockDayWithSessions(averageCheckup = averageCheckup1),
            date2 to mockDayWithSessions(averageCheckup = averageCheckup2)
        )
        val monthWithDays = createMonthWithDayStats(dayStats = dayWithSessionsMap)

        val newMonthWithDays = monthWithDays.calculateAverage()

        val daysWithSessions = 2.0
        val expectedAvgZone1 = (80f + 23f) / daysWithSessions
        val expectedAvgZone2 = (5f + 78f) / daysWithSessions
        val expectedAvgZone3 = (2f + 0f) / daysWithSessions
        val expectedAvgZone4 = (0f + 10f) / daysWithSessions

        val expectedAverageCheckup = createAverageCheckup(
            mapOf(
                mouthZone1 to expectedAvgZone1.roundOneDecimalToFloat(),
                mouthZone2 to expectedAvgZone2.roundOneDecimalToFloat(),
                mouthZone3 to expectedAvgZone3.roundOneDecimalToFloat(),
                mouthZone4 to expectedAvgZone4.roundOneDecimalToFloat()
            )
        )

        assertEquals(expectedAverageCheckup, newMonthWithDays.averageCheckup)
    }

    @Test
    fun `calculateAverage returns new instance with average correctMovementAverage from DayWithSessions`() {
        val date1 = TrustedClock.getNowLocalDate()
        val date2 = TrustedClock.getNowLocalDate().minusDays(1)

        val correctMovementAverage1 = 84.3
        val correctMovementAverage2 = 13.6

        val dayWithSessionsMap = mapOf(
            date1 to mockDayWithSessions(correctMovementAverage = correctMovementAverage1),
            date2 to mockDayWithSessions(correctMovementAverage = correctMovementAverage2)
        )
        val monthWithDays = createMonthWithDayStats(dayStats = dayWithSessionsMap)

        val newMonthWithDays = monthWithDays.calculateAverage()

        val expected = 48.9 // 48.95
        assertEquals(expected, newMonthWithDays.correctMovementAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average correctOrientationAverage from DayWithSessions`() {
        val date1 = TrustedClock.getNowLocalDate()
        val date2 = TrustedClock.getNowLocalDate().minusDays(1)
        val date3 = TrustedClock.getNowLocalDate().minusDays(2)

        val correctOrientationAverage1 = 76.0
        val correctOrientationAverage2 = 25.1
        val correctOrientationAverage3 = 16.7

        val dayWithSessionsMap = mapOf(
            date1 to mockDayWithSessions(correctOrientationAverage = correctOrientationAverage1),
            date2 to mockDayWithSessions(correctOrientationAverage = correctOrientationAverage2),
            date3 to mockDayWithSessions(correctOrientationAverage = correctOrientationAverage3)
        )
        val monthWithDays = createMonthWithDayStats(dayStats = dayWithSessionsMap)

        val newMonthWithDays = monthWithDays.calculateAverage()

        val expected = 39.3 // 39.2666667

        assertEquals(expected, newMonthWithDays.correctOrientationAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average underSpeedAverage from DayWithSessions`() {
        val date1 = TrustedClock.getNowLocalDate()
        val date2 = TrustedClock.getNowLocalDate().minusDays(1)
        val date3 = TrustedClock.getNowLocalDate().minusDays(2)
        val date4 = TrustedClock.getNowLocalDate().minusDays(3)

        val underSpeedAverage1 = 0.4
        val underSpeedAverage2 = 7.98
        val underSpeedAverage3 = 99.7

        val dayWithSessionsMap = mapOf(
            date1 to mockDayWithSessions(underSpeedAverage = underSpeedAverage1),
            date2 to mockDayWithSessions(underSpeedAverage = underSpeedAverage2),
            date3 to mockDayWithSessions(underSpeedAverage = underSpeedAverage3),
            date4 to mockDayWithSessions(underSpeedAverage = 0.0) // should be ignored
        )
        val monthWithDays = createMonthWithDayStats(dayStats = dayWithSessionsMap)

        val newMonthWithDays = monthWithDays.calculateAverage()

        val expected = 36.0 // 36.026

        assertEquals(expected, newMonthWithDays.underSpeedAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average correctSpeedAverage from DayWithSessions`() {
        val date1 = TrustedClock.getNowLocalDate()
        val date2 = TrustedClock.getNowLocalDate().minusDays(1)
        val date3 = TrustedClock.getNowLocalDate().minusDays(2)
        val date4 = TrustedClock.getNowLocalDate().minusDays(3)

        val correctSpeedAverage1 = 56.1
        val correctSpeedAverage2 = 88.13
        val correctSpeedAverage3 = 6.8

        val dayWithSessionsMap = mapOf(
            date1 to mockDayWithSessions(correctSpeedAverage = correctSpeedAverage1),
            date2 to mockDayWithSessions(correctSpeedAverage = correctSpeedAverage2),
            date3 to mockDayWithSessions(correctSpeedAverage = correctSpeedAverage3),
            date4 to mockDayWithSessions(correctSpeedAverage = 0.0) // should be ignored
        )
        val monthWithDays = createMonthWithDayStats(dayStats = dayWithSessionsMap)

        val newMonthWithDays = monthWithDays.calculateAverage()

        val expected = 50.3 // 50,34

        assertEquals(expected, newMonthWithDays.correctSpeedAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average overSpeedAverage from DayWithSessions`() {
        val date1 = TrustedClock.getNowLocalDate()
        val date2 = TrustedClock.getNowLocalDate().minusDays(1)
        val date3 = TrustedClock.getNowLocalDate().minusDays(2)
        val date4 = TrustedClock.getNowLocalDate().minusDays(3)

        val overSpeedAverage1 = 0.3
        val overSpeedAverage2 = 5.99
        val overSpeedAverage3 = 37.23

        val dayWithSessionsMap = mapOf(
            date1 to mockDayWithSessions(overSpeedAverage = overSpeedAverage1),
            date2 to mockDayWithSessions(overSpeedAverage = overSpeedAverage2),
            date3 to mockDayWithSessions(overSpeedAverage = overSpeedAverage3),
            date4 to mockDayWithSessions(overSpeedAverage = 0.0) // should be ignored
        )
        val monthWithDays = createMonthWithDayStats(dayStats = dayWithSessionsMap)

        val newMonthWithDays = monthWithDays.calculateAverage()

        val expected = 14.5 // 14.506

        assertEquals(expected, newMonthWithDays.overSpeedAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average overPressureAverage from DayWithSessions`() {
        val date1 = TrustedClock.getNowLocalDate()
        val date2 = TrustedClock.getNowLocalDate().minusDays(1)
        val date3 = TrustedClock.getNowLocalDate().minusDays(2)
        val date4 = TrustedClock.getNowLocalDate().minusDays(3)

        val overPressureAverage1 = 1.2
        val overPressureAverage2 = 7.9
        val overPressureAverage3 = 15.86

        val dayWithSessionsMap = mapOf(
            date1 to mockDayWithSessions(overPressureAverage = overPressureAverage1),
            date2 to mockDayWithSessions(overPressureAverage = overPressureAverage2),
            date3 to mockDayWithSessions(overPressureAverage = overPressureAverage3),
            date4 to mockDayWithSessions(overPressureAverage = 0.0) // should be ignored
        )
        val monthWithDays = createMonthWithDayStats(dayStats = dayWithSessionsMap)

        val newMonthWithDays = monthWithDays.calculateAverage()

        val expected = 8.3

        assertEquals(expected, newMonthWithDays.overPressureAverage)
    }

    @Test
    fun `calculateAverage returns a new instance with sessions per day`() {
        val firstDayPreviousMonth = TrustedClock.getNowLocalDate().minusMonths(1).withDayOfMonth(1)

        val secondDayPreviousMonth = firstDayPreviousMonth.plusDays(1)
        val monthWithDays = createMonthWithDayStats(
            monthAggregatedStatsEntity = createMonthAggregatedStatEntity(yearMonth = firstDayPreviousMonth.toYearMonth()),
            dayStats = mapOf(
                secondDayPreviousMonth to createDayWithSessions(
                    sessions = listOf(
                        createSessionStatsEntity(),
                        createSessionStatsEntity()
                    )
                ),
                firstDayPreviousMonth to createDayWithSessions(
                    sessions = listOf(
                        createSessionStatsEntity()
                    )
                )
            )
        )

        assertEquals(0, monthWithDays.totalSessions)
        assertEquals(0.0, monthWithDays.sessionsPerDay)

        val expectedBrushingsPerDay =
            (3.0 / firstDayPreviousMonth.toYearMonth().lengthOfMonth()).roundOneDecimal()
        assertEquals(expectedBrushingsPerDay, monthWithDays.calculateAverage().sessionsPerDay, 0.0)
    }

    @Test
    fun `calculateAverage returns a new instance with sessions per day but doesn't take into account future dates`() {
        val monthDay2FixedDate = TrustedClock.getNowZonedDateTime().withDayOfMonth(2)
        TrustedClock.setFixedDate(monthDay2FixedDate)

        val firstDayPreviousMonth = monthDay2FixedDate.toLocalDate().minusDays(1)
        val secondDayPreviousMonth = monthDay2FixedDate.toLocalDate()
        val monthWithDays = createMonthWithDayStats(
            dayStats = mapOf(
                secondDayPreviousMonth to createDayWithSessions(
                    sessions = listOf(
                        createSessionStatsEntity(),
                        createSessionStatsEntity()
                    )
                ),
                firstDayPreviousMonth to createDayWithSessions(
                    sessions = listOf(
                        createSessionStatsEntity()
                    )
                )
            )
        )

        assertEquals(0.0, monthWithDays.sessionsPerDay)

        val expectedBrushingsPerDay = (3.0 / 2).roundOneDecimal()
        assertEquals(expectedBrushingsPerDay, monthWithDays.calculateAverage().sessionsPerDay, 0.0)
    }

    /*sumSessions
     *
     * Given a List<MonthWithDays> that contains
     * - June, mapOf(june1st -> (session1, session2), june2nd -> (session3))
     * - June, mapOf(june1st -> (session4), june15th -> (session5))
     *
     * The returned [MonthWithDayStats] should be
     *
     * June, mapOf(june1st -> (session1, session2, session4), june2nd -> (session3), june15th -> (session5))*/

    @Test
    fun `sumSessions`() {
        val month = YearMonth.now()
        val currentDate = TrustedClock.getNowLocalDateTime().withHour(12)

        val firstDayOfMonth = currentDate.withDayOfMonth(1)
        val secondDayOfMonth = currentDate.withDayOfMonth(2)
        val fifteenthDayOfMonth = currentDate.withDayOfMonth(15)

        val session1 = createSessionStatsEntity(
            creationTime = firstDayOfMonth
        )
        val session2 = createSessionStatsEntity(
            creationTime = firstDayOfMonth.plusHours(1)
        )
        val session3 = createSessionStatsEntity(
            creationTime = secondDayOfMonth
        )
        val session4 = createSessionStatsEntity(
            creationTime = firstDayOfMonth
        )
        val session5 = createSessionStatsEntity(
            creationTime = fifteenthDayOfMonth
        )

        val june1stDayWithSessionsForFirstMonth =
            createDayWithSessions(
                dayAggregatedEntity = createDayAggregatedStatsEntity(day = firstDayOfMonth.toLocalDate()),
                sessions = listOf(session1, session2)
            )

        val june1stDayWithSessionsForSecondMonth =
            createDayWithSessions(
                dayAggregatedEntity = createDayAggregatedStatsEntity(day = firstDayOfMonth.toLocalDate()),
                sessions = listOf(session4)
            )

        val june2ndDayWithSessionsForFirstMonth =
            createDayWithSessions(
                dayAggregatedEntity = createDayAggregatedStatsEntity(day = secondDayOfMonth.toLocalDate()),
                sessions = listOf(session3)
            )

        val june15thDayWithSessionsForSecondMonth =
            createDayWithSessions(
                dayAggregatedEntity = createDayAggregatedStatsEntity(day = fifteenthDayOfMonth.toLocalDate()),
                sessions = listOf(session5)
            )

        val monthWithDaysStats1 = createMonthWithDayStats(
            monthAggregatedStatsEntity = createMonthAggregatedStatEntity(yearMonth = month),
            dayStats = mapOf(
                firstDayOfMonth.toLocalDate() to june1stDayWithSessionsForFirstMonth,
                secondDayOfMonth.toLocalDate() to june2ndDayWithSessionsForFirstMonth
            )
        )

        val monthWithDaysStats2 = createMonthWithDayStats(
            monthAggregatedStatsEntity = createMonthAggregatedStatEntity(yearMonth = month),
            dayStats = mapOf(
                firstDayOfMonth.toLocalDate() to june1stDayWithSessionsForSecondMonth,
                fifteenthDayOfMonth.toLocalDate() to june15thDayWithSessionsForSecondMonth
            )
        )

        val unionMonthWithDays = listOf(monthWithDaysStats1, monthWithDaysStats2).sumSessions()

        assertEquals(month, unionMonthWithDays.month)

        val expectedJune1stDayWithSessions =
            createDayWithSessions(
                dayAggregatedEntity = createDayAggregatedStatsEntity(day = firstDayOfMonth.toLocalDate()),
                sessions = listOf(session1, session2, session4)
            )

        val sessionsMap = unionMonthWithDays.sessionsMap
        assertEquals(expectedJune1stDayWithSessions, sessionsMap[firstDayOfMonth.toLocalDate()])
        assertEquals(
            june2ndDayWithSessionsForFirstMonth,
            sessionsMap[secondDayOfMonth.toLocalDate()]
        )
        assertEquals(
            june15thDayWithSessionsForSecondMonth,
            sessionsMap[fifteenthDayOfMonth.toLocalDate()]
        )
    }

    @Test(expected = AssertionError::class)
    fun `sumSessions throws IllegalStateException if the List contains MonthWithDayStats referring to different months`() {
        val currentMonth = YearMonth.now()
        val monthWithDaysStatsCurrentMonth = createMonthWithDayStats(
            monthAggregatedStatsEntity = createMonthAggregatedStatEntity(yearMonth = currentMonth)
        )

        val monthWithDaysStatsPastMonth = createMonthWithDayStats(
            monthAggregatedStatsEntity = createMonthAggregatedStatEntity(
                yearMonth = currentMonth.minusMonths(
                    1
                )
            )
        )

        listOf(monthWithDaysStatsCurrentMonth, monthWithDaysStatsPastMonth).sumSessions()
    }
}
