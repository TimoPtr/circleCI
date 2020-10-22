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
import com.kolibree.statsoffline.test.createSessionStatsEntity
import com.kolibree.statsoffline.test.createWeekAggregatedStatEntity
import com.kolibree.statsoffline.test.createWeekWithDayStats
import com.kolibree.statsoffline.test.mockDayWithSessions
import com.kolibree.statsoffline.toYearWeek
import com.nhaarman.mockitokotlin2.spy
import java.util.Locale
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.TemporalField
import org.threeten.bp.temporal.WeekFields

class WeekWithDayStatsTest : BaseUnitTest() {
    private lateinit var defaultLocale: Locale

    override fun setup() {
        super.setup()

        defaultLocale = Locale.getDefault()

        Locale.setDefault(Locale.CHINA) // weeks start on Monday
    }

    override fun tearDown() {
        super.tearDown()

        Locale.setDefault(defaultLocale)
    }

    /*
    withNewSessions
     */
    @Test(expected = IllegalArgumentException::class)
    fun `withNewSessions throws IllegalArgumentException if sessions is empty and we try to add sessions`() {
        val emptyWeekWithDays =
            WeekWithDayStats(weekStats = createWeekAggregatedStatEntity(), dayStats = mapOf())

        emptyWeekWithDays.withNewSessions(TrustedClock.getNowLocalDate(), listOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `withNewSessions throws IllegalArgumentException if localDate doesn't exist in sessions map`() {
        val weekWithDays =
            createWeekWithDayStats(dayStats = mapOf(TrustedClock.getNowLocalDate() to createDayWithSessions()))

        weekWithDays.withNewSessions(TrustedClock.getNowLocalDate().minusWeeks(1), listOf())
    }

    @Test
    fun `withNewSessions adds sessions to DayWithSessions corresponding to date even if it's empty`() {
        val date = TrustedClock.getNowLocalDate()
        val weekWithDays = createWeekWithDayStats(dayStats = mapOf(date to createDayWithSessions()))

        assertTrue(weekWithDays.dayStats.getValue(date).brushingSessions.isEmpty())

        val newSession = createSessionStatsEntity(creationTime = date.atTime(12, 0), duration = 50)
        val newWeekWithDays = weekWithDays.withNewSessions(date, listOf(newSession))

        assertEquals(newSession, newWeekWithDays.dayStats.getValue(date).brushingSessions.single())
    }

    @Test
    fun `withNewSessions adds sessions to DayWithSessions corresponding to date`() {
        val date = TrustedClock.getNowLocalDate()
        val preexistingSession = createSessionStatsEntity(duration = 50)
        val weekWithDays = createWeekWithDayStats(
            dayStats = mapOf(date to createDayWithSessions(sessions = listOf(preexistingSession)))
        )

        assertTrue(weekWithDays.dayStats.getValue(date).brushingSessions.isNotEmpty())

        val newSession = createSessionStatsEntity(creationTime = date.atTime(12, 0), duration = 20)
        val newWeekWithDays = weekWithDays.withNewSessions(date, listOf(newSession))

        val sessions = newWeekWithDays.dayStats.getValue(date).brushingSessions
        assertEquals(2, sessions.size)
        assertTrue(sessions.containsAll(listOf(preexistingSession, newSession)))
    }

    @Test
    fun `withNewSessions does not touch total sessions value`() {
        val firstDaysOfWeek = firstTwoDaysOfWeekFromDate(TrustedClock.getNowZonedDateTime())
        val secondDay = firstDaysOfWeek.secondDay
        val firstDay = firstDaysOfWeek.firstDay
        val weekWithDays = createWeekWithDayStats(
            dayStats = mapOf(
                secondDay to createDayWithSessions(
                    sessions = listOf(
                        createSessionStatsEntity(),
                        createSessionStatsEntity()
                    )
                ),
                firstDay to createDayWithSessions(sessions = listOf(createSessionStatsEntity()))
            )
        )

        assertEquals(0, weekWithDays.totalSessions)

        val newSession = createSessionStatsEntity(creationTime = secondDay.atTime(9, 0))
        val newWeekWithDays = weekWithDays.withNewSessions(secondDay, listOf(newSession))

        assertEquals(0, newWeekWithDays.totalSessions)
    }

    /*
    calculateAverage
     */

    @Test
    fun `calculateAverage returns instance with updated total sessions value`() {
        val firstDaysOfWeek = firstTwoDaysOfWeekFromDate(TrustedClock.getNowZonedDateTime())
        val secondDay = firstDaysOfWeek.secondDay
        val firstDay = firstDaysOfWeek.firstDay

        // ensure secondDay is not in the future. Otherwise we'd discard its sessions
        TrustedClock.setFixedDate(firstDay.atStartOfDay(ZoneId.systemDefault()))
        if (secondDay.isAfter(TrustedClock.getNowZonedDateTime().toLocalDate())) {
            TrustedClock.setFixedDate(secondDay.atStartOfDay(ZoneId.systemDefault()))
        }

        val weekWithDays = createWeekWithDayStats(
            dayStats = mapOf(
                secondDay to createDayWithSessions(
                    sessions = listOf(
                        createSessionStatsEntity(),
                        createSessionStatsEntity()
                    )
                ),
                firstDay to createDayWithSessions(sessions = listOf(createSessionStatsEntity()))
            )
        )

        assertEquals(0, weekWithDays.totalSessions)

        assertEquals(3, weekWithDays.calculateAverage().totalSessions)
    }

    @Test
    fun `calculateAverage returns instance with exact same dates`() {
        val fixedDate = TrustedClock.getNowZonedDateTime()
        TrustedClock.setFixedDate(fixedDate)

        val firstDaysOfWeek = firstTwoDaysOfWeekFromDate(fixedDate)
        val secondDay = firstDaysOfWeek.secondDay
        val firstDay = firstDaysOfWeek.firstDay

        val dayWithSessionsMap =
            mapOf(secondDay to mockDayWithSessions(), firstDay to mockDayWithSessions())
        val weekWithDays = createWeekWithDayStats(dayStats = dayWithSessionsMap)

        val dates = weekWithDays.dayStats.keys
        // Usually 7, but not for first & last week of the year, which may be shorter
        val numberOfDatesInCurrentWeek = YearWeek.from(fixedDate.toLocalDate()).dates().size
        assertEquals(numberOfDatesInCurrentWeek, dates.size)

        val newWeekWithDays = weekWithDays.calculateAverage()

        assertEquals(dates, newWeekWithDays.dayStats.keys)
    }

    @Test
    fun `calculateAverage ignores duration of future days`() {
        val fixedDate = TrustedClock.getNowZonedDateTime()

        val firstDaysOfWeek = firstTwoDaysOfWeekFromDate(fixedDate)
        val secondDay = firstDaysOfWeek.secondDay
        val firstDay = firstDaysOfWeek.firstDay

        // Now, let's set 2nd day of week as fixed day to test if we ignore future days
        TrustedClock.setFixedDate(secondDay.atStartOfDay(TrustedClock.systemZone).withHour(5))

        val averageDurationToday = 80.0
        val todaySession = mockDayWithSessions(averageDuration = averageDurationToday)

        val averageDurationYesterday = 41.0
        val yesterdaySession = mockDayWithSessions(averageDuration = averageDurationYesterday)

        val dayWithSessionsMap = mapOf(firstDay to todaySession, secondDay to yesterdaySession)
        val weekWithDays = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(
                yearWeek = YearWeek.from(
                    firstDay
                )
            ),
            dayStats = dayWithSessionsMap
        )

        val newWeekWithDays = weekWithDays.calculateAverage()

        val today = TrustedClock.getNowLocalDate()
        val nbOfDatesNotInFuture = weekWithDays.dayStats.filterKeys { !it.isAfter(today) }.size

        val expectedDuration =
            (averageDurationToday + averageDurationYesterday) / nbOfDatesNotInFuture
        assertEquals(expectedDuration.roundOneDecimal(), newWeekWithDays.averageDuration)
    }

    @Test
    fun `calculateAverage returns new instance with average duration of DayWithSessions`() {
        val pair = firstTwoDaysOfPastWeek(minusWeeks = 1L)
        val today = pair.secondDay
        val yesterday = pair.firstDay

        val averageDurationToday = 80.0
        val todaySession = mockDayWithSessions(averageDuration = averageDurationToday)

        val averageDurationYesterday = 41.0
        val yesterdaySession = mockDayWithSessions(averageDuration = averageDurationYesterday)

        val dayWithSessionsMap = mapOf(today to todaySession, yesterday to yesterdaySession)
        val weekWithDays = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(
                yearWeek = YearWeek.from(
                    today
                )
            ),
            dayStats = dayWithSessionsMap
        )

        val newWeekWithDays = weekWithDays.calculateAverage()

        val expectedDuration = (averageDurationToday + averageDurationYesterday) / listOf(
            averageDurationToday,
            averageDurationYesterday
        )
            .filter { it != 0.toDouble() }.size.toDouble()
        assertEquals(expectedDuration.roundOneDecimal(), newWeekWithDays.averageDuration)
    }

    @Test
    fun `calculateAverage returns new instance with average surface of DayWithSessions`() {
        val pair = firstTwoDaysOfPastWeek(minusWeeks = 1L)
        val today = pair.secondDay
        val yesterday = pair.firstDay

        val averageSurface1 = 80.0
        val dayWithSessions1 = mockDayWithSessions(averageSurface = averageSurface1)

        val averageSurface2 = 41.0
        val dayWithSessions2 = mockDayWithSessions(averageSurface = averageSurface2)

        val dayWithSessionsMap = mapOf(today to dayWithSessions1, yesterday to dayWithSessions2)
        val weekWithDays = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(
                yearWeek = YearWeek.from(today)
            ),
            dayStats = dayWithSessionsMap
        )

        val newWeekWithDays = weekWithDays.calculateAverage()

        val expectedSurface =
            (averageSurface1 + averageSurface2) / listOf(averageSurface1, averageSurface2)
                .filter { it != 0.toDouble() }.size.toDouble()
        assertEquals(expectedSurface.roundOneDecimal(), newWeekWithDays.averageSurface)
    }

    @Test
    fun `calculateAverage returns new instance with average correctMovementAverage from DayWithSessions`() {
        val daysOfWeek = daysOfWeekFromDate(TrustedClock.getNowZonedDateTime())

        val today = daysOfWeek.fourthDay

        // Now, let's set 3rd day of week as fixed day
        TrustedClock.setFixedDate(today.atStartOfDay(TrustedClock.systemZone).withHour(5))

        val correctMovementAverage1 = 84.3
        val correctMovementAverage2 = 13.6

        val dayWithSessionsMap = mapOf(
            daysOfWeek.thirdDay to mockDayWithSessions(correctMovementAverage = correctMovementAverage1),
            daysOfWeek.secondDay to mockDayWithSessions(correctMovementAverage = correctMovementAverage2),
            daysOfWeek.firstDay to mockDayWithSessions(correctMovementAverage = 0.0) // should be ignored
        )
        val weekWithDays = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(
                yearWeek = YearWeek.from(today)
            ),
            dayStats = dayWithSessionsMap
        )

        val newWeekWithDays = weekWithDays.calculateAverage()

        val expected = 48.9 // 48.95
        assertEquals(expected, newWeekWithDays.correctMovementAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average correctOrientationAverage from DayWithSessions`() {
        val daysOfWeek = daysOfWeekFromDate(TrustedClock.getNowZonedDateTime())

        val today = daysOfWeek.fourthDay

        // Now, let's set 3rd day of week as fixed day
        TrustedClock.setFixedDate(today.atStartOfDay(TrustedClock.systemZone).withHour(5))

        val correctOrientationAverage1 = 76.0
        val correctOrientationAverage2 = 25.1

        val dayWithSessionsMap = mapOf(
            daysOfWeek.thirdDay to mockDayWithSessions(correctOrientationAverage = correctOrientationAverage1),
            daysOfWeek.secondDay to mockDayWithSessions(correctOrientationAverage = correctOrientationAverage2),
            daysOfWeek.fourthDay to mockDayWithSessions(correctOrientationAverage = 0.0)
        )

        val weekWithDays = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(
                yearWeek = YearWeek.from(today)
            ),
            dayStats = dayWithSessionsMap
        )

        val newWeekWithDays = weekWithDays.calculateAverage()

        val expected = 50.6 // 50.55

        assertEquals(expected, newWeekWithDays.correctOrientationAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average underSpeedAverage from DayWithSessions`() {
        val daysOfWeek = daysOfWeekFromDate(TrustedClock.getNowZonedDateTime())

        val today = daysOfWeek.fourthDay

        // Now, let's set 3rd day of week as fixed day
        TrustedClock.setFixedDate(today.atStartOfDay(TrustedClock.systemZone).withHour(5))

        val underSpeedAverage1 = 0.4
        val underSpeedAverage2 = 7.98
        val underSpeedAverage3 = 99.7

        val dayWithSessionsMap = mapOf(
            today to mockDayWithSessions(underSpeedAverage = underSpeedAverage1),
            daysOfWeek.firstDay to mockDayWithSessions(underSpeedAverage = underSpeedAverage2),
            daysOfWeek.secondDay to mockDayWithSessions(underSpeedAverage = underSpeedAverage3),
            daysOfWeek.thirdDay to mockDayWithSessions(underSpeedAverage = 0.0)
        )
        val weekWithDays = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(
                yearWeek = YearWeek.from(today)
            ),
            dayStats = dayWithSessionsMap
        )

        val newWeekWithDays = weekWithDays.calculateAverage()

        val expected = 36.0 // 36.026

        assertEquals(expected, newWeekWithDays.underSpeedAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average correctSpeedAverage from DayWithSessions`() {
        val daysOfWeek = daysOfWeekFromDate(TrustedClock.getNowZonedDateTime())

        val today = daysOfWeek.fourthDay

        // Now, let's set 3rd day of week as fixed day
        TrustedClock.setFixedDate(today.atStartOfDay(TrustedClock.systemZone).withHour(5))

        val correctSpeedAverage1 = 56.1
        val correctSpeedAverage2 = 88.13
        val correctSpeedAverage3 = 6.8

        val dayWithSessionsMap = mapOf(
            today to mockDayWithSessions(correctSpeedAverage = correctSpeedAverage1),
            daysOfWeek.secondDay to mockDayWithSessions(correctSpeedAverage = correctSpeedAverage2),
            daysOfWeek.thirdDay to mockDayWithSessions(correctSpeedAverage = correctSpeedAverage3),
            daysOfWeek.firstDay to mockDayWithSessions(correctSpeedAverage = 0.0) // should be ignored
        )
        val weekWithDays = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(
                yearWeek = YearWeek.from(today)
            ),
            dayStats = dayWithSessionsMap
        )

        val newWeekWithDays = weekWithDays.calculateAverage()

        val expected = 50.3 // 50,34

        assertEquals(expected, newWeekWithDays.correctSpeedAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average overSpeedAverage from DayWithSessions`() {
        val daysOfWeek = daysOfWeekFromDate(TrustedClock.getNowZonedDateTime())

        val today = daysOfWeek.fourthDay

        // Now, let's set 3rd day of week as fixed day
        TrustedClock.setFixedDate(today.atStartOfDay(TrustedClock.systemZone).withHour(5))

        val overSpeedAverage1 = 0.3
        val overSpeedAverage2 = 5.99
        val overSpeedAverage3 = 37.23

        val dayWithSessionsMap = mapOf(
            today to mockDayWithSessions(overSpeedAverage = overSpeedAverage1),
            daysOfWeek.secondDay to mockDayWithSessions(overSpeedAverage = overSpeedAverage2),
            daysOfWeek.thirdDay to mockDayWithSessions(overSpeedAverage = overSpeedAverage3),
            daysOfWeek.firstDay to mockDayWithSessions(overSpeedAverage = 0.0) // should be ignored
        )
        val weekWithDays = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(
                yearWeek = YearWeek.from(today)
            ),
            dayStats = dayWithSessionsMap
        )

        val newWeekWithDays = weekWithDays.calculateAverage()

        val expected = 14.5 // 14.506

        assertEquals(expected, newWeekWithDays.overSpeedAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average overPressureAverage from DayWithSessions`() {
        val daysOfWeek = daysOfWeekFromDate(TrustedClock.getNowZonedDateTime())

        val today = daysOfWeek.fourthDay

        // Now, let's set 3rd day of week as fixed day
        TrustedClock.setFixedDate(today.atStartOfDay(TrustedClock.systemZone).withHour(5))

        val overPressureAverage1 = 12.86
        val overPressureAverage2 = 56.33
        val overPressureAverage3 = 25.63

        val dayWithSessionsMap = mapOf(
            today to mockDayWithSessions(overPressureAverage = overPressureAverage1),
            daysOfWeek.secondDay to mockDayWithSessions(overPressureAverage = overPressureAverage2),
            daysOfWeek.thirdDay to mockDayWithSessions(overPressureAverage = overPressureAverage3),
            daysOfWeek.firstDay to mockDayWithSessions(overPressureAverage = 0.0) // should be ignored
        )
        val weekWithDays = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(
                yearWeek = YearWeek.from(today)
            ),
            dayStats = dayWithSessionsMap
        )

        val newWeekWithDays = weekWithDays.calculateAverage()

        val expected = 31.6

        assertEquals(expected, newWeekWithDays.overPressureAverage)
    }

    @Test
    fun `calculateAverage returns new instance with average checkup from DayWithSessions`() {
        val date1 = TrustedClock.getNowLocalDate()
        val date2 = TrustedClock.getNowLocalDate().minusDays(1)

        TrustedClock.setFixedDate(TrustedClock.getNowZonedDateTime().plusDays(9))

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
        val weekWithDays = spy(createWeekWithDayStats(dayStats = dayWithSessionsMap))

        val newWeekWithDays = weekWithDays.calculateAverage()

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

        assertEquals(expectedAverageCheckup, newWeekWithDays.averageCheckup)
    }

    @Test
    fun `calculateAverage doesn't take into account next year values`() {
        val dec31 = LocalDate.of(
            2019,
            Month.DECEMBER,
            31
        )
        val dec30 = dec31.minusDays(1)

        val weekWithDays = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(yearWeek = dec31.toYearWeek()),
            dayStats = mapOf(
                dec30 to createDayWithSessions(
                    sessions = listOf(
                        createSessionStatsEntity(),
                        createSessionStatsEntity()
                    )
                ),
                dec31 to createDayWithSessions(
                    sessions = listOf(
                        createSessionStatsEntity()
                    )
                )
            )
        )

        assertEquals(0, weekWithDays.totalSessions)
        assertEquals(0.0, weekWithDays.sessionsPerDay)

        /*
        There are more brushings than days in the week (30, 31 is Mon, Tue), so we should get 1
         */
        val expectedBrushingsPerDay = 1.0
        assertEquals(expectedBrushingsPerDay, weekWithDays.calculateAverage().sessionsPerDay, 0.0)
    }

    @Test
    fun `calculateAverage returns a new instance with sessions per day`() {
        val firstDaysOfPreviousWeek =
            firstTwoDaysOfWeekFromDate(TrustedClock.getNowZonedDateTime().minusWeeks(1))
        val secondDayPreviousWeek = firstDaysOfPreviousWeek.secondDay
        val firstDayPreviousWeek = firstDaysOfPreviousWeek.firstDay

        val weekWithDays = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(yearWeek = firstDayPreviousWeek.toYearWeek()),
            dayStats = mapOf(
                secondDayPreviousWeek to createDayWithSessions(
                    sessions = listOf(
                        createSessionStatsEntity(),
                        createSessionStatsEntity()
                    )
                ),
                firstDayPreviousWeek to createDayWithSessions(
                    sessions = listOf(
                        createSessionStatsEntity()
                    )
                )
            )
        )

        assertEquals(0, weekWithDays.totalSessions)
        assertEquals(0.0, weekWithDays.sessionsPerDay)

        val expectedBrushingsPerDay = (3.0 / weekWithDays.dayStats.size).roundOneDecimal()
        assertEquals(expectedBrushingsPerDay, weekWithDays.calculateAverage().sessionsPerDay, 0.0)
    }

    @Test
    fun `calculateAverage returns a new instance with sessions per day but doesn't take into account future dates`() {
        val firstDaysOfWeek = firstTwoDaysOfWeekFromDate(TrustedClock.getNowZonedDateTime())
        val secondDayWeek = firstDaysOfWeek.secondDay
        val firstDayWeek = firstDaysOfWeek.firstDay

        TrustedClock.setFixedDate(secondDayWeek.atStartOfDay(TrustedClock.systemZone).withHour(12))

        val weekWithDays = createWeekWithDayStats(
            dayStats = mapOf(
                secondDayWeek to createDayWithSessions(
                    sessions = listOf(
                        createSessionStatsEntity(),
                        createSessionStatsEntity()
                    )
                ),
                firstDayWeek to createDayWithSessions(
                    sessions = listOf(
                        createSessionStatsEntity()
                    )
                )
            )
        )

        assertEquals(0.0, weekWithDays.sessionsPerDay)

        val expectedBrushingsPerDay = (3.0 / 2).roundOneDecimal()
        assertEquals(expectedBrushingsPerDay, weekWithDays.calculateAverage().sessionsPerDay, 0.0)
    }

    /*
    sumSessions
     *
     * Given a List<WeekWithDays> that contains
     * - Week11, mapOf(Monday -> (session1, session2), Wednesday -> (session3))
     * - Week11, mapOf(Monday -> (session4), Saturday -> (session5))
     *
     * The returned [WeekWithDayStats] should be
     *
     * Week11, mapOf(Monday -> (session1, session2, session4), Wednesday -> (session3), Saturday -> (session5))
     */
    @Test
    fun `sumSessions returns a WeekWithSessions with expected sessions`() {
        val week = YearWeek.now()

        val weekField = WeekFields.of(Locale.getDefault())
        val weekOfYearField: TemporalField = weekField.weekOfYear()

        val monday = TrustedClock.getNowLocalDateTime().with(weekOfYearField, week.week.toLong())
        val wednesday = monday.plusDays(2)
        val saturday = monday.plusDays(5)

        val session1 = createSessionStatsEntity(
            creationTime = monday
        )
        val session2 = createSessionStatsEntity(
            creationTime = monday.plusHours(1)
        )
        val session3 = createSessionStatsEntity(
            creationTime = wednesday
        )
        val session4 = createSessionStatsEntity(
            creationTime = monday
        )
        val session5 = createSessionStatsEntity(
            creationTime = saturday
        )

        val mondayDayWithSessionsForFirstWeek =
            createDayWithSessions(
                dayAggregatedEntity = createDayAggregatedStatsEntity(day = monday.toLocalDate()),
                sessions = listOf(session1, session2)
            )

        val mondayDayWithSessionsForSecondWeek =
            createDayWithSessions(
                dayAggregatedEntity = createDayAggregatedStatsEntity(day = monday.toLocalDate()),
                sessions = listOf(session4)
            )

        val wednesdayDayWithSessionsForFirstWeek =
            createDayWithSessions(
                dayAggregatedEntity = createDayAggregatedStatsEntity(day = wednesday.toLocalDate()),
                sessions = listOf(session3)
            )

        val saturdayDayWithSessionsForSecondWeek =
            createDayWithSessions(
                dayAggregatedEntity = createDayAggregatedStatsEntity(day = saturday.toLocalDate()),
                sessions = listOf(session5)
            )

        val weekWithDaysStats1 = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(yearWeek = week),
            dayStats = mapOf(
                monday.toLocalDate() to mondayDayWithSessionsForFirstWeek,
                wednesday.toLocalDate() to wednesdayDayWithSessionsForFirstWeek
            )
        )

        val weekWithDaysStats2 = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(yearWeek = week),
            dayStats = mapOf(
                monday.toLocalDate() to mondayDayWithSessionsForSecondWeek,
                saturday.toLocalDate() to saturdayDayWithSessionsForSecondWeek
            )
        )

        val unionWeekWithDays = listOf(weekWithDaysStats1, weekWithDaysStats2).sumSessions()

        assertEquals(week, unionWeekWithDays.week)

        val expectedMondayDayWithSessions =
            createDayWithSessions(
                dayAggregatedEntity = createDayAggregatedStatsEntity(day = monday.toLocalDate()),
                sessions = listOf(session1, session2, session4)
            )

        val sessionsMap = unionWeekWithDays.sessionsMap
        assertEquals(expectedMondayDayWithSessions, sessionsMap[monday.toLocalDate()])
        assertEquals(wednesdayDayWithSessionsForFirstWeek, sessionsMap[wednesday.toLocalDate()])
        assertEquals(saturdayDayWithSessionsForSecondWeek, sessionsMap[saturday.toLocalDate()])
    }

    @Test(expected = AssertionError::class)
    fun `sumSessions throws IllegalStateException if the List contains WeekWithDayStats referring to different weeks`() {
        val dateCurrentWeek = TrustedClock.getNowLocalDate()
        val weekWithDaysStatsCurrentWeek = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(yearWeek = dateCurrentWeek.toYearWeek())
        )

        val weekWithDaysStatsPastWeek = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(
                yearWeek = dateCurrentWeek.minusDays(8).toYearWeek()
            )
        )

        listOf(weekWithDaysStatsCurrentWeek, weekWithDaysStatsPastWeek).sumSessions()
    }

    /*
    UTILS
     */
    private fun firstTwoDaysOfPastWeek(minusWeeks: Long = 0L): FirstTwoDaysOfWeek {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

        var firstDay = TrustedClock.getNowZonedDateTime().minusWeeks(minusWeeks).toLocalDate()
        while (firstDay.dayOfWeek != firstDayOfWeek) {
            firstDay = firstDay.minusDays(1)
        }

        return FirstTwoDaysOfWeek(firstDay = firstDay, secondDay = firstDay.plusDays(1))
    }

    private fun firstTwoDaysOfWeekFromDate(zonedDateTime: ZonedDateTime): FirstTwoDaysOfWeek {
        val daysOfWeek = daysOfWeekFromDate(zonedDateTime)

        return FirstTwoDaysOfWeek(firstDay = daysOfWeek.firstDay, secondDay = daysOfWeek.secondDay)
    }

    private fun daysOfWeekFromDate(zonedDateTime: ZonedDateTime): DaysOfWeek {
        val firstDayOfWeek = YearWeek.from(zonedDateTime.toLocalDate()).dates()[0]

        var firstDay = zonedDateTime.toLocalDate()
        while (firstDay.dayOfWeek != firstDayOfWeek.dayOfWeek) {
            firstDay = firstDay.minusDays(1)
        }

        val daysOfWeek = (0L until 7).map { firstDay.plusDays(it) }.toSet()

        return DaysOfWeek.create(daysOfWeek)
    }
}

private data class FirstTwoDaysOfWeek(val firstDay: LocalDate, val secondDay: LocalDate)

@Suppress("DataClassPrivateConstructor")
private data class DaysOfWeek private constructor(
    val firstDay: LocalDate,
    val secondDay: LocalDate,
    val thirdDay: LocalDate,
    val fourthDay: LocalDate,
    val fifthDay: LocalDate,
    val sixthDay: LocalDate,
    val seventhDay: LocalDate
) {
    companion object {
        fun create(days: Set<LocalDate>): DaysOfWeek {
            val daysAsList = days.toList()

            check(days.size == 7) {
                throw IllegalArgumentException("Must have 7 days")
            }

            return DaysOfWeek(
                firstDay = daysAsList[0],
                secondDay = daysAsList[1],
                thirdDay = daysAsList[2],
                fourthDay = daysAsList[3],
                fifthDay = daysAsList[4],
                sixthDay = daysAsList[5],
                seventhDay = daysAsList[6]
            )
        }
    }
}
