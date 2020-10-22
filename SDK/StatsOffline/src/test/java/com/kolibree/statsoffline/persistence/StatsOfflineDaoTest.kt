/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.atEndOfDay
import com.kolibree.statsoffline.models.DayWithSessions
import com.kolibree.statsoffline.models.MonthWithDayStats
import com.kolibree.statsoffline.models.WeekWithDayStats
import com.kolibree.statsoffline.models.YearWeek
import com.kolibree.statsoffline.persistence.models.MonthAggregatedStatsEntity
import com.kolibree.statsoffline.persistence.models.WeekAggregatedStatsEntity
import com.kolibree.statsoffline.persistence.models.createBrushingDayStat
import com.kolibree.statsoffline.test.DEFAULT_PROFILE_ID
import com.kolibree.statsoffline.test.createDayAggregatedStatsEntity
import com.kolibree.statsoffline.test.createSessionStatsEntity
import com.kolibree.statsoffline.toYearWeek
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

class StatsOfflineDaoTest : BaseUnitTest() {
    private val sessionStatDao: BrushingSessionStatDao = mock()
    private val monthStatDao: MonthAggregatedStatsDao = mock()
    private val dayStatDao: DayAggregatedStatsDao = mock()
    private val weekStatDao: WeekAggregatedStatsDao = mock()

    private val statsOfflineDao = spy(TestStatsOfflineDao())

    override fun setup() {
        super.setup()

        statsOfflineDao.monthStatDao = monthStatDao
        statsOfflineDao.dayStatDao = dayStatDao
        statsOfflineDao.sessionStatDao = sessionStatDao
        statsOfflineDao.weekStatDao = weekStatDao
    }

    /*
    datePeriodDaysWithSessions
     */

    @Test(expected = IllegalArgumentException::class)
    fun `datePeriodDaysWithSessions throws IllegalArgumentException if startDate and endDate are on the same day`() {
        val profileId = DEFAULT_PROFILE_ID
        val date = TrustedClock.getNowLocalDate()

        statsOfflineDao.datePeriodDaysWithSessions(profileId, date, date)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `datePeriodDaysWithSessions throws IllegalArgumentException if startDate is after endDate`() {
        val profileId = DEFAULT_PROFILE_ID
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.plusDays(1)

        statsOfflineDao.datePeriodDaysWithSessions(profileId, startDate, endDate)
    }

    @Test
    fun `datePeriodDaysWithSessions invokes daysWithSessions with all dates in the period`() {
        val profileId = DEFAULT_PROFILE_ID
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(5)

        doReturn(listOf<DayWithSessions>()).whenever(statsOfflineDao).daysWithSessions(eq(profileId), any())

        val expectedDates = listOf<LocalDate>(
            startDate,
            startDate.plusDays(1),
            startDate.plusDays(2),
            startDate.plusDays(3),
            startDate.plusDays(4),
            endDate
        )
        argumentCaptor<List<LocalDate>> {
            statsOfflineDao.datePeriodDaysWithSessions(profileId, startDate, endDate)

            verify(statsOfflineDao).daysWithSessions(eq(profileId), capture())

            assertEquals(expectedDates, firstValue)
        }
    }

    @Test
    fun `datePeriodDaysWithSessions returns DayWithSessions only for a given profile`() {
        val firstProfileId = DEFAULT_PROFILE_ID
        val secondProfileId = DEFAULT_PROFILE_ID + 1
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(5)
        val expectedDates = listOf<LocalDate>(
            startDate,
            startDate.plusDays(1),
            startDate.plusDays(2),
            startDate.plusDays(3),
            startDate.plusDays(4),
            endDate
        )

        val firstProfileAggregatedStats = createDayAggregatedStatsEntity(profileId = firstProfileId, day = startDate)
        val secondProfileAggregatedStats = createDayAggregatedStatsEntity(profileId = secondProfileId, day = startDate)

        whenever(dayStatDao.readByDate(firstProfileId, expectedDates))
            .thenReturn(listOf(firstProfileAggregatedStats))

        whenever(dayStatDao.readByDate(secondProfileId, expectedDates))
            .thenReturn(listOf(secondProfileAggregatedStats))

        val expectedSessionForFirstProfile = listOf(
            createSessionStatsEntity(
                profileId = firstProfileId,
                creationTime = TrustedClock.getNowLocalDateTime().minusHours(1)
            )
        )
        val expectedSessionForSecondProfile = listOf(
            createSessionStatsEntity(
                profileId = secondProfileId,
                creationTime = TrustedClock.getNowLocalDateTime().minusHours(2)
            )
        )

        whenever(sessionStatDao.readByKolibreeDay(firstProfileId, startDate))
            .thenReturn(expectedSessionForFirstProfile)

        whenever(sessionStatDao.readByKolibreeDay(secondProfileId, startDate))
            .thenReturn(expectedSessionForSecondProfile)

        assertEquals(
            listOf(
                DayWithSessions(
                    dayStats = firstProfileAggregatedStats,
                    brushingSessions = expectedSessionForFirstProfile
                )
            ),
            statsOfflineDao.datePeriodDaysWithSessions(firstProfileId, startDate, endDate)
        )

        assertEquals(
            listOf(
                DayWithSessions(
                    dayStats = secondProfileAggregatedStats,
                    brushingSessions = expectedSessionForSecondProfile
                )
            ),
            statsOfflineDao.datePeriodDaysWithSessions(secondProfileId, startDate, endDate)
        )
    }

    /*
    dayWithSessions
     */

    @Test
    fun `dayWithSessions returns null if dayStatDao readByDate returns empty list`() {
        val profileId = DEFAULT_PROFILE_ID
        val day = TrustedClock.getNowLocalDate()

        whenever(dayStatDao.readByDate(profileId, listOf(day))).thenReturn(listOf())

        assertNull(statsOfflineDao.dayWithSessions(profileId, day))
    }

    @Test
    fun `dayWithSessions returns null if dayStatDao readByDate returns list with more than 1 element`() {
        val profileId = DEFAULT_PROFILE_ID
        val day = TrustedClock.getNowLocalDate()

        whenever(dayStatDao.readByDate(profileId, listOf(day))).thenReturn(
            listOf(
                createDayAggregatedStatsEntity(),
                createDayAggregatedStatsEntity()
            )
        )

        assertNull(statsOfflineDao.dayWithSessions(profileId, day))
    }

    @Test
    fun `dayWithSessions returns DayWithSessions with values from sessionStatDao and dayStatDao if dayStatDao returns a list with single value`() {
        val profileId = DEFAULT_PROFILE_ID
        val day = TrustedClock.getNowLocalDate()

        val dayAggregatedStats = createDayAggregatedStatsEntity()
        whenever(dayStatDao.readByDate(profileId, listOf(day))).thenReturn(listOf(dayAggregatedStats))

        val expectedSession1 = createSessionStatsEntity(creationTime = TrustedClock.getNowLocalDateTime().minusHours(1))
        val expectedSession2 = createSessionStatsEntity(creationTime = TrustedClock.getNowLocalDateTime().minusHours(2))
        val expectedSessions = listOf(expectedSession1, expectedSession2)
        whenever(sessionStatDao.readByKolibreeDay(DEFAULT_PROFILE_ID, day)).thenReturn(expectedSessions)

        val expectedDayWithSessions = DayWithSessions(
            dayStats = dayAggregatedStats,
            brushingSessions = expectedSessions
        )

        assertEquals(expectedDayWithSessions, statsOfflineDao.dayWithSessions(profileId, day))
    }

    @Test
    fun `dayWithSessions returns DayWithSessions only for a given profile`() {
        val firstProfileId = DEFAULT_PROFILE_ID
        val secondProfileId = DEFAULT_PROFILE_ID + 1
        val day = TrustedClock.getNowLocalDate()

        val firstProfileAggregatedStats = createDayAggregatedStatsEntity(profileId = firstProfileId)
        val secondProfileAggregatedStats = createDayAggregatedStatsEntity(profileId = secondProfileId)
        whenever(dayStatDao.readByDate(firstProfileId, listOf(day)))
            .thenReturn(listOf(firstProfileAggregatedStats))
        whenever(dayStatDao.readByDate(secondProfileId, listOf(day)))
            .thenReturn(listOf(secondProfileAggregatedStats))

        val expectedSessionForFirstProfile =
            listOf(createSessionStatsEntity(creationTime = TrustedClock.getNowLocalDateTime().minusHours(1)))
        val expectedSessionForSecondProfile =
            listOf(createSessionStatsEntity(creationTime = TrustedClock.getNowLocalDateTime().minusHours(2)))

        whenever(sessionStatDao.readByKolibreeDay(firstProfileId, day)).thenReturn(expectedSessionForFirstProfile)
        whenever(sessionStatDao.readByKolibreeDay(secondProfileId, day)).thenReturn(expectedSessionForSecondProfile)

        val firstProfileDayWithSessions = DayWithSessions(
            dayStats = firstProfileAggregatedStats,
            brushingSessions = expectedSessionForFirstProfile
        )

        assertEquals(firstProfileDayWithSessions, statsOfflineDao.dayWithSessions(firstProfileId, day))

        val secondProfileDayWithSessions = DayWithSessions(
            dayStats = secondProfileAggregatedStats,
            brushingSessions = expectedSessionForSecondProfile
        )

        assertEquals(secondProfileDayWithSessions, statsOfflineDao.dayWithSessions(secondProfileId, day))
    }

    /*
    removeBrushingSessionStat
     */

    @Test
    fun `removeBrushingSessionStat invokes removeByCreationTime`() {
        val sessionStat = createSessionStatsEntity()
        statsOfflineDao.removeBrushingSessionStat(sessionStat)

        verify(sessionStatDao).removeByCreationTime(sessionStat.profileId, sessionStat.creationTime)

        verifyNoMoreInteractions(monthStatDao)
        verifyNoMoreInteractions(dayStatDao)
    }

    /*
    DAY WITH SESSIONS
     */

    @Test
    fun `dayWithSessions creates DayWithSessions from returned BrushingDayStatEntity`() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val dateList = listOf(today, yesterday)

        val dayEntityToday =
            createBrushingDayStat(DEFAULT_PROFILE_ID, today)
        val dayEntityYesterday =
            createBrushingDayStat(DEFAULT_PROFILE_ID, yesterday)

        whenever(dayStatDao.readByDate(DEFAULT_PROFILE_ID, dateList)).thenReturn(
            listOf(
                dayEntityToday,
                dayEntityYesterday
            )
        )

        val todayExpectedSessions = listOf(
            createSessionStatsEntity(creationTime = today.atStartOfDay()),
            createSessionStatsEntity(creationTime = today.atEndOfDay())
        )
        whenever(sessionStatDao.readByKolibreeDay(DEFAULT_PROFILE_ID, today)).thenReturn(todayExpectedSessions)

        val yesterdayExpectedSessions = listOf(
            createSessionStatsEntity(creationTime = yesterday.atTime(1, 15))
        )
        whenever(sessionStatDao.readByKolibreeDay(DEFAULT_PROFILE_ID, yesterday)).thenReturn(yesterdayExpectedSessions)

        val returnedList = statsOfflineDao.daysWithSessions(DEFAULT_PROFILE_ID, dateList)

        assertEquals(2, returnedList.size)

        val expectedDayWithSessionsToday =
            DayWithSessions(dayStats = dayEntityToday, brushingSessions = todayExpectedSessions)
        val expectedDayWithSessionsYesterday =
            DayWithSessions(dayStats = dayEntityYesterday, brushingSessions = yesterdayExpectedSessions)

        assertTrue(returnedList.containsAll(listOf(expectedDayWithSessionsToday, expectedDayWithSessionsYesterday)))
    }

    /*
    daysWithSessionsFromWeeks
     */

    @Test
    fun `daysWithSessionsFromWeeks creates DayWithSessions from returned BrushingDayStatEntity`() {
        val today = TrustedClock.getNowLocalDate()
        val pastWeekDate = today.minusWeeks(1)

        val currentWeek = today.toYearWeek()
        val pastWeek = pastWeekDate.toYearWeek()

        assertNotSame(currentWeek, pastWeek)

        val weekList = listOf(currentWeek, pastWeek)

        val dayEntityToday = createBrushingDayStat(DEFAULT_PROFILE_ID, today)
        val dayEntityPastWeek = createBrushingDayStat(DEFAULT_PROFILE_ID, pastWeekDate)

        whenever(dayStatDao.readByWeeks(DEFAULT_PROFILE_ID, weekList))
            .thenReturn(listOf(dayEntityToday, dayEntityPastWeek))

        val todayExpectedSessions = listOf(
            createSessionStatsEntity(creationTime = today.atStartOfDay()),
            createSessionStatsEntity(creationTime = today.atEndOfDay())
        )
        whenever(sessionStatDao.readByKolibreeDay(DEFAULT_PROFILE_ID, today)).thenReturn(todayExpectedSessions)

        val yesterdayExpectedSessions = listOf(
            createSessionStatsEntity(creationTime = pastWeekDate.atTime(1, 15))
        )
        whenever(sessionStatDao.readByKolibreeDay(DEFAULT_PROFILE_ID, pastWeekDate)).thenReturn(
            yesterdayExpectedSessions
        )

        val returnedList = statsOfflineDao.daysWithSessionsFromWeeks(DEFAULT_PROFILE_ID, weekList)

        assertEquals(2, returnedList.size)

        val expectedDayWithSessionsToday =
            DayWithSessions(dayStats = dayEntityToday, brushingSessions = todayExpectedSessions)
        val expectedDayWithSessionsYesterday =
            DayWithSessions(dayStats = dayEntityPastWeek, brushingSessions = yesterdayExpectedSessions)
        assertTrue(returnedList.containsAll(listOf(expectedDayWithSessionsToday, expectedDayWithSessionsYesterday)))
    }

    /*
    weekWithDays
     */

    @Test
    fun `weekWithDays returns null if there's no entity for given week`() {
        assertNull(statsOfflineDao.weekWithDays(DEFAULT_PROFILE_ID, YearWeek.now()))
    }

    @Test
    fun `weekWithDays returns WeekWithDayStats filled with sessions for given wek`() {
        val today = TrustedClock.getNowLocalDate()
        val yesterday = today.minusDays(1)

        val currentWeek = today.toYearWeek()
        val profileId = DEFAULT_PROFILE_ID

        val existingWeekEntity = WeekAggregatedStatsEntity(profileId, currentWeek)
        whenever(weekStatDao.readByWeek(profileId, currentWeek)).thenReturn(existingWeekEntity)

        val dayWithSessions = mockDayWithSessions(listOf(today, yesterday))
        doReturn(dayWithSessions).whenever(statsOfflineDao)
            .daysWithSessionsFromWeeks(DEFAULT_PROFILE_ID, listOf(currentWeek))

        val expectedWeekWithDayStats = WeekWithDayStats(
            weekStats = existingWeekEntity,
            dayStats = mapOf(
                today to dayWithSessions.single { it.day == today },
                yesterday to dayWithSessions.single { it.day == yesterday })
        )

        assertEquals(expectedWeekWithDayStats, statsOfflineDao.weekWithDays(profileId, currentWeek))
    }

    /*
    monthWithDays
     */

    @Test
    fun `monthWithDays returns null if there's no entity for given month`() {
        assertNull(statsOfflineDao.monthWithDays(DEFAULT_PROFILE_ID, YearMonth.now()))
    }

    @Test
    fun `monthWithDays returns MonthWithDayStats filled with sessions for given day`() {
        val currentMonth = YearMonth.now()
        val profileId = DEFAULT_PROFILE_ID

        val existingMonthEntity =
            MonthAggregatedStatsEntity(profileId, currentMonth)
        whenever(monthStatDao.readByMonth(profileId, currentMonth)).thenReturn(existingMonthEntity)

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val dayWithSessions = mockDayWithSessions(listOf(today, yesterday))
        doReturn(dayWithSessions).whenever(statsOfflineDao)
            .daysWithSessions(DEFAULT_PROFILE_ID, existingMonthEntity.dates)

        val expectedMonthWithDayStats = MonthWithDayStats(
            monthStats = existingMonthEntity,
            dayStats = mapOf(
                today to dayWithSessions.single { it.day == today },
                yesterday to dayWithSessions.single { it.day == yesterday })
        )

        assertEquals(expectedMonthWithDayStats, statsOfflineDao.monthWithDays(profileId, currentMonth))
    }

    /*
    monthsWithDays
     */

    @Test
    fun `monthsWithDays returns a List of MonthWithDays from returned MonthAggregatedStatsEntity`() {
        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)
        val profileId = DEFAULT_PROFILE_ID

        val previousMonthEntity =
            MonthAggregatedStatsEntity(profileId, previousMonth)
        whenever(monthStatDao.readByMonth(profileId, listOf(currentMonth, previousMonth)))
            .thenReturn(listOf(previousMonthEntity))

        val today = LocalDate.now().minusMonths(1).withDayOfMonth(9)
        val yesterday = today.minusDays(1)
        val dayWithSessions = mockDayWithSessions(listOf(today, yesterday))
        doReturn(dayWithSessions).whenever(statsOfflineDao)
            .daysWithSessions(DEFAULT_PROFILE_ID, previousMonthEntity.dates)

        val monthsWithDays = statsOfflineDao.monthsWithDays(profileId, setOf(currentMonth, previousMonth))

        val expectedMonthWithDayStats = MonthWithDayStats(
            monthStats = previousMonthEntity,
            dayStats = mapOf(
                today to dayWithSessions.single { it.day == today },
                yesterday to dayWithSessions.single { it.day == yesterday })
        )

        assertEquals(expectedMonthWithDayStats, monthsWithDays.single())
    }

    /*
    GET OT CREATE MONTH
     */
    @Test
    fun `getOrCreateMonth returns new MonthEntity if monthWithDays returns null`() {
        val currentMonth = YearMonth.now()
        val profileId = DEFAULT_PROFILE_ID

        val expectedMonthWithStats = mock<MonthWithDayStats>()
        doReturn(expectedMonthWithStats).whenever(statsOfflineDao).insertSanitizedEntities(profileId, currentMonth)

        doReturn(null).whenever(statsOfflineDao).monthWithDays(profileId, currentMonth)

        assertEquals(expectedMonthWithStats, statsOfflineDao.getOrCreateMonthStats(profileId, currentMonth))
    }

    @Test
    fun `getOrCreateMonth returns existing MonthEntity if present`() {
        val currentMonth = YearMonth.now()
        val profileId = DEFAULT_PROFILE_ID

        val expectedMonthWithStats = mock<MonthWithDayStats>()
        doReturn(expectedMonthWithStats).whenever(statsOfflineDao).monthWithDays(profileId, currentMonth)

        assertEquals(expectedMonthWithStats, statsOfflineDao.getOrCreateMonthStats(profileId, currentMonth))

        verify(statsOfflineDao, never()).insertSanitizedEntities(any(), any())
    }

    /*
    getOrCreateWeekStats
     */
    @Test
    fun `getOrCreateWeekStats returns new WeekEntity if weekWithDays returns null`() {
        val currentWeek = YearWeek.now()
        val profileId = DEFAULT_PROFILE_ID

        val expectedWeekWithStats = mock<WeekWithDayStats>()
        doReturn(expectedWeekWithStats).whenever(statsOfflineDao).createWeekAndDays(profileId, currentWeek)

        doReturn(null).whenever(statsOfflineDao).weekWithDays(profileId, currentWeek)

        assertEquals(expectedWeekWithStats, statsOfflineDao.getOrCreateWeekStats(profileId, currentWeek))
    }

    @Test
    fun `getOrCreateWeekStats returns existing WeekEntity if present`() {
        val currentWeek = YearWeek.now()
        val profileId = DEFAULT_PROFILE_ID

        val expectedWeekWithStats = mock<WeekWithDayStats>()
        doReturn(expectedWeekWithStats).whenever(statsOfflineDao).weekWithDays(profileId, currentWeek)

        assertEquals(expectedWeekWithStats, statsOfflineDao.getOrCreateWeekStats(profileId, currentWeek))

        verify(statsOfflineDao, never()).createWeekAndDays(any(), any())
    }

    /*
    UPDATE
     */

    @Test
    fun `update inserts items in order`() {
        val currentMonth = YearMonth.now()
        val profileId = DEFAULT_PROFILE_ID

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val dayEntityToday = createBrushingDayStat(DEFAULT_PROFILE_ID, today)

        val todayExpectedSessions = listOf(createSessionStatsEntity(creationTime = today.atStartOfDay()))

        val yesterdayExpectedSessions = listOf(createSessionStatsEntity(creationTime = yesterday.atEndOfDay()))

        val expectedMonthEntities = listOf(MonthAggregatedStatsEntity(profileId, currentMonth))
        val expectedWeekEntities = listOf(WeekAggregatedStatsEntity(profileId, today.toYearWeek()))
        val expectedDayEntities = listOf(dayEntityToday)
        val expectedSessionEntities = todayExpectedSessions + yesterdayExpectedSessions

        statsOfflineDao.update(
            monthEntities = expectedMonthEntities,
            weekEntities = expectedWeekEntities,
            dayEntities = expectedDayEntities,
            brushingSessionEntities = expectedSessionEntities
        )

        // order is important or foreign keys will fail
        inOrder(monthStatDao, weekStatDao, dayStatDao, sessionStatDao) {
            verify(monthStatDao).insert(expectedMonthEntities)

            verify(weekStatDao).insert(expectedWeekEntities)

            verify(dayStatDao).insert(expectedDayEntities)

            verify(sessionStatDao).insert(expectedSessionEntities)
        }
    }

    /*
    CREATE MONTH AND DAYS
     */

    @Test
    fun `createMonthAndDays inserts a new BrushingMonthStatEntity for given month`() {
        val currentMonth = YearMonth.now()
        val profileId = DEFAULT_PROFILE_ID

        val montyWithDayStatsEntity = mock<MonthWithDayStats>()
        doReturn(montyWithDayStatsEntity).whenever(statsOfflineDao).monthWithDays(profileId, currentMonth)

        val expectedNewMonthEntity =
            MonthAggregatedStatsEntity(profileId, currentMonth)

        statsOfflineDao.insertSanitizedEntities(profileId, currentMonth)

        verify(monthStatDao).insert(expectedNewMonthEntity)
    }

    @Test
    fun `createMonthAndDays inserts a new list of BrushingDayStatEntity`() {
        val currentMonth = YearMonth.now()
        val profileId = DEFAULT_PROFILE_ID

        val monthWithDayStatsEntity = mock<MonthWithDayStats>()
        doReturn(monthWithDayStatsEntity).whenever(statsOfflineDao).monthWithDays(profileId, currentMonth)

        val expectedDayStats = MonthAggregatedStatsEntity(
            profileId,
            currentMonth
        ).createEmptyDayStats()

        statsOfflineDao.insertSanitizedEntities(profileId, currentMonth)

        verify(dayStatDao).insert(expectedDayStats)
    }

    @Test
    fun `createMonthAndDays returns value from monthWithDays`() {
        val currentMonth = YearMonth.now()
        val profileId = DEFAULT_PROFILE_ID

        val expectedMonthWithDayStatsEntity = mock<MonthWithDayStats>()
        doReturn(expectedMonthWithDayStatsEntity).whenever(statsOfflineDao).monthWithDays(profileId, currentMonth)

        assertEquals(expectedMonthWithDayStatsEntity, statsOfflineDao.insertSanitizedEntities(profileId, currentMonth))
    }

    @Test(expected = IllegalStateException::class)
    fun `createMonthAndDays throws IllegalStateException if monthWithDays returns null`() {
        val currentMonth = YearMonth.now()
        val profileId = DEFAULT_PROFILE_ID

        statsOfflineDao.insertSanitizedEntities(profileId, currentMonth)
    }

    /*
    createWeekAndDays
     */

    @Test
    fun `createWeekAndDays inserts a new WeekAggregatedStatsEntity for given week`() {
        val currentWeek = YearWeek.now()
        val profileId = DEFAULT_PROFILE_ID

        val montyWithDayStatsEntity = mock<WeekWithDayStats>()
        doReturn(montyWithDayStatsEntity).whenever(statsOfflineDao).weekWithDays(profileId, currentWeek)

        val expectedNewWeekEntity = WeekAggregatedStatsEntity(profileId, currentWeek)

        statsOfflineDao.createWeekAndDays(profileId, currentWeek)

        verify(weekStatDao).insert(expectedNewWeekEntity)
    }

    @Test
    fun `createWeekAndDays never inserts a new list of WeekAggregatedStatsEntity`() {
        val currentWeek = YearWeek.now()
        val profileId = DEFAULT_PROFILE_ID

        val weekWithDayStatsEntity = mock<WeekWithDayStats>()
        doReturn(weekWithDayStatsEntity).whenever(statsOfflineDao).weekWithDays(profileId, currentWeek)

        statsOfflineDao.createWeekAndDays(profileId, currentWeek)

        verifyNoMoreInteractions(dayStatDao)
    }

    @Test
    fun `createWeekAndDays returns value from weekWithDays`() {
        val currentWeek = YearWeek.now()
        val profileId = DEFAULT_PROFILE_ID

        val expectedWeekWithDayStatsEntity = mock<WeekWithDayStats>()
        doReturn(expectedWeekWithDayStatsEntity).whenever(statsOfflineDao).weekWithDays(profileId, currentWeek)

        assertEquals(expectedWeekWithDayStatsEntity, statsOfflineDao.createWeekAndDays(profileId, currentWeek))
    }

    @Test(expected = IllegalStateException::class)
    fun `createWeekAndDays throws IllegalStateException if weekWithDays returns null`() {
        val currentWeek = YearWeek.now()
        val profileId = DEFAULT_PROFILE_ID

        doReturn(null).whenever(statsOfflineDao).weekWithDays(profileId, currentWeek)

        statsOfflineDao.createWeekAndDays(profileId, currentWeek)
    }

    /*
    UTILS
     */

    private fun mockDayWithSessions(dates: List<LocalDate>): List<DayWithSessions> {
        return dates.map {
            val dayWithSessions = mock<DayWithSessions>()
            whenever(dayWithSessions.day).thenReturn(it)

            dayWithSessions
        }
    }
}

private class TestStatsOfflineDao : StatsOfflineDao()
