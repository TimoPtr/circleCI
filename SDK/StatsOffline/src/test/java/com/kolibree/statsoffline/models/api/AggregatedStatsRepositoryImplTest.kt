package com.kolibree.statsoffline.models.api

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.RuntimeTestForcedException
import com.kolibree.kml.MouthZone16
import com.kolibree.statsoffline.models.MonthAggregatedStatsWithSessions
import com.kolibree.statsoffline.models.WeekAggregatedStatsWithSessions
import com.kolibree.statsoffline.models.YearWeek
import com.kolibree.statsoffline.models.emptyAverageCheckup
import com.kolibree.statsoffline.persistence.StatsOfflineDao
import com.kolibree.statsoffline.test.DEFAULT_PROFILE_ID
import com.kolibree.statsoffline.test.createAverageCheckup
import com.kolibree.statsoffline.test.createDayAggregatedStatsEntity
import com.kolibree.statsoffline.test.createDayWithSessions
import com.kolibree.statsoffline.test.createMonthAggregatedStatEntity
import com.kolibree.statsoffline.test.createMonthWithDayStats
import com.kolibree.statsoffline.test.createPeriodAggregatedStats
import com.kolibree.statsoffline.test.createWeekAggregatedStatEntity
import com.kolibree.statsoffline.test.createWeekWithDayStats
import com.kolibree.statsoffline.toYearWeek
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test
import org.threeten.bp.YearMonth

internal class AggregatedStatsRepositoryImplTest : BaseUnitTest() {
    private val statsOfflineDao = mock<StatsOfflineDao>()

    private val debounceScheduler = TestScheduler()

    private var aggregatedStatsRepository =
        AggregatedStatsRepositoryImpl(statsOfflineDao, debounceScheduler = debounceScheduler)

    /*
    monthStats
     */

    @Test
    fun `monthStats emits empty MonthAggregatedStats is statsOfflineDao returns null`() {
        val profileId = DEFAULT_PROFILE_ID
        val month = YearMonth.now()

        assertNull(statsOfflineDao.monthWithDays(profileId, month))

        val monthAggregatedStats =
            aggregatedStatsRepository.monthStats(profileId, month).test().values().single()

        assertEquals(month, monthAggregatedStats.month)
        assertEquals(profileId, monthAggregatedStats.profileId)
        assertEquals(0.0, monthAggregatedStats.averageDuration)
        assertEquals(0.0, monthAggregatedStats.averageSurface)
        assertEquals(emptyAverageCheckup(), monthAggregatedStats.averageCheckup)
    }

    @Test
    fun `monthStats emits MonthAggregatedStats with value returned from statsOfflineDao`() {
        val profileId = DEFAULT_PROFILE_ID
        val month = YearMonth.now().minusMonths(3)

        val expectedAverageDuration = 546.3
        val expectedAverageSurface = 87.6
        val expectedAverageCheckup =
            createAverageCheckup(mapOf(MouthZone16.LoIncExt to 5.9f, MouthZone16.UpIncExt to 54.0f))

        val monthWithDays = createMonthWithDayStats(
            monthAggregatedStatsEntity = createMonthAggregatedStatEntity(
                yearMonth = month,
                profileId = profileId,
                averageSurface = expectedAverageSurface,
                averageDuration = expectedAverageDuration,
                averageCheckup = expectedAverageCheckup
            )
        )

        whenever(statsOfflineDao.monthWithDays(profileId, month)).thenReturn(monthWithDays)

        val monthAggregatedStats =
            aggregatedStatsRepository.monthStats(profileId, month).test().values().single()

        assertEquals(monthWithDays.month, monthAggregatedStats.month)
        assertEquals(monthWithDays.profileId, monthAggregatedStats.profileId)
        assertEquals(monthWithDays.averageDuration, monthAggregatedStats.averageDuration)
        assertEquals(monthWithDays.averageSurface, monthAggregatedStats.averageSurface)
        assertEquals(monthWithDays.averageCheckup, monthAggregatedStats.averageCheckup)
    }

    @Test
    fun `monthStats emits error if StatsOfflineDao emits error`() {
        val profileId = DEFAULT_PROFILE_ID
        val month = YearMonth.now().minusMonths(3)

        val expectedError = RuntimeTestForcedException()
        whenever(statsOfflineDao.monthWithDays(profileId, month)).thenAnswer {
            throw expectedError
        }

        aggregatedStatsRepository.monthStats(profileId, month).test().assertError(expectedError)
    }

    /*
    dayStats
     */

    @Test
    fun `dayStats emits empty DayAggregatedStats is statsOfflineDao returns null`() {
        val profileId = DEFAULT_PROFILE_ID
        val day = TrustedClock.getNowLocalDate()

        assertNull(statsOfflineDao.dayWithSessions(profileId, day))

        val dayAggregatedStats =
            aggregatedStatsRepository.dayStats(profileId, day).test().values().single()

        assertEquals(day, dayAggregatedStats.day)
        assertEquals(profileId, dayAggregatedStats.profileId)
        assertEquals(0.0, dayAggregatedStats.averageDuration)
        assertEquals(0.0, dayAggregatedStats.averageSurface)
        assertEquals(emptyAverageCheckup(), dayAggregatedStats.averageCheckup)
    }

    @Test
    fun `dayStats emits DayAggregatedStats with value returned from statsOfflineDao`() {
        val profileId = DEFAULT_PROFILE_ID
        val day = TrustedClock.getNowLocalDate().minusMonths(3)

        val expectedAverageDuration = 546.3
        val expectedAverageSurface = 87.6
        val expectedAverageCheckup =
            createAverageCheckup(mapOf(MouthZone16.LoIncExt to 5.9f, MouthZone16.UpIncExt to 54.0f))

        val dayWithSessions = createDayWithSessions(
            dayAggregatedEntity = createDayAggregatedStatsEntity(
                day = day,
                profileId = profileId,
                averageSurface = expectedAverageSurface,
                averageDuration = expectedAverageDuration,
                averageCheckup = expectedAverageCheckup
            )
        )

        whenever(statsOfflineDao.dayWithSessions(profileId, day)).thenReturn(dayWithSessions)

        val dayAggregatedStats =
            aggregatedStatsRepository.dayStats(profileId, day).test().values().single()

        assertEquals(dayWithSessions.day, dayAggregatedStats.day)
        assertEquals(dayWithSessions.profileId, dayAggregatedStats.profileId)
        assertEquals(dayWithSessions.averageDuration, dayAggregatedStats.averageDuration)
        assertEquals(dayWithSessions.averageSurface, dayAggregatedStats.averageSurface)
        assertEquals(dayWithSessions.averageCheckup, dayAggregatedStats.averageCheckup)
    }

    @Test
    fun `dayStats emits error if StatsOfflineDao emits error`() {
        val profileId = DEFAULT_PROFILE_ID
        val day = TrustedClock.getNowLocalDate().minusMonths(3)

        val expectedError = RuntimeTestForcedException()
        whenever(statsOfflineDao.dayWithSessions(profileId, day)).thenAnswer {
            throw expectedError
        }

        aggregatedStatsRepository.dayStats(profileId, day).test().assertError(expectedError)
    }

    /*
    periodStats
     */

    @Test
    fun `periodStats emits IllegalArgumentException if dateRangeDaysWithSessions throws IllegalArgumentException`() {
        val profileId = DEFAULT_PROFILE_ID
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(3)

        whenever(
            statsOfflineDao.datePeriodDaysWithSessions(
                profileId,
                startDate,
                endDate
            )
        ).thenThrow(
            IllegalArgumentException()
        )

        aggregatedStatsRepository.periodStats(profileId, startDate, endDate).test()
            .assertError(IllegalArgumentException::class.java)
    }

    @Test
    fun `periodStats emits empty PeriodAggregatedStats is statsOfflineDao returns empty DayWithSessions for date range`() {
        val profileId = DEFAULT_PROFILE_ID
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(3)

        whenever(
            statsOfflineDao.datePeriodDaysWithSessions(
                profileId,
                startDate,
                endDate
            )
        ).thenReturn(listOf())

        val expectedPeriodAggregatedStats = createPeriodAggregatedStats(startDate, endDate)

        aggregatedStatsRepository.periodStats(profileId, startDate, endDate).test()
            .assertNoErrors()
            .assertValue(expectedPeriodAggregatedStats)
    }

    @Test
    fun `periodStats emits expected PeriodAggregatedStats when statsOfflineDao returns list of DayWithSessions for date range, including those for which StatsOfflineDao didn't return data`() {
        val profileId = DEFAULT_PROFILE_ID
        val endDate = TrustedClock.getNowLocalDate()
        val startDate = endDate.minusDays(3)

        val dayWithSessionsForEndDate = createDayWithSessions(
            dayAggregatedEntity = createDayAggregatedStatsEntity(day = endDate)
        )
        val dayWithSessionsForStartDate = createDayWithSessions(
            dayAggregatedEntity = createDayAggregatedStatsEntity(day = startDate)
        )

        whenever(
            statsOfflineDao.datePeriodDaysWithSessions(
                profileId,
                startDate,
                endDate
            )
        ).thenReturn(
            listOf(
                dayWithSessionsForEndDate,
                dayWithSessionsForStartDate
            )
        )

        val dayNotReturnedByDB1 = createDayWithSessions(
            dayAggregatedEntity = createDayAggregatedStatsEntity(day = endDate.minusDays(1))
        )
        val dayNotReturnedByDB2 = createDayWithSessions(
            dayAggregatedEntity = createDayAggregatedStatsEntity(day = endDate.minusDays(2))
        )

        val expectedPeriodAggregatedStats = createPeriodAggregatedStats(
            startDate,
            endDate,
            dayAggregatedStats = setOf(
                dayWithSessionsForEndDate,
                dayWithSessionsForStartDate,
                dayNotReturnedByDB1,
                dayNotReturnedByDB2
            )
        )

        aggregatedStatsRepository.periodStats(profileId, startDate, endDate).test()
            .assertNoErrors()
            .assertValue(expectedPeriodAggregatedStats)
    }

    /*
    weekStatsStream
     */

    @Test
    fun `weekStatsStream queries weekStats each time statsOfflineDao weeksUpdatedStream emits a value`() {
        aggregatedStatsRepository = spy(aggregatedStatsRepository)

        val profileId = DEFAULT_PROFILE_ID
        val weeks = setOf(YearWeek.now())

        val subject = BehaviorProcessor.createDefault(true)
        whenever(statsOfflineDao.weeksUpdatedStream(profileId, weeks)).thenReturn(subject)

        val firstExpectedWeekStats = setOf(mock<WeekAggregatedStatsWithSessions>())
        val secondExpectedWeekStats = setOf(mock<WeekAggregatedStatsWithSessions>())
        val thirdExpectedWeekStats = setOf(mock<WeekAggregatedStatsWithSessions>())
        doReturn(
            Single.just(firstExpectedWeekStats),
            Single.just(secondExpectedWeekStats),
            Single.just(thirdExpectedWeekStats)
        )
            .whenever(aggregatedStatsRepository).weekStatsSingle(profileId, weeks)

        val observer =
            aggregatedStatsRepository.weekStatsStream(profileId, weeks).test()
                .assertEmpty()
                .also {
                    advanceTimeDebounce()
                }
                .assertValues(firstExpectedWeekStats)

        subject.onNext(true)

        observer
            .assertValueCount(1)
            .also {
                advanceTimeDebounce()
            }.assertValues(firstExpectedWeekStats, secondExpectedWeekStats)

        subject.onNext(true)

        observer
            .assertValueCount(2)
            .also {
                advanceTimeDebounce()
            }.assertValues(
                firstExpectedWeekStats,
                secondExpectedWeekStats,
                thirdExpectedWeekStats
            )
    }

    /*
    monthStatsStream
     */

    @Test
    fun `monthStatsStream queries monthStats each time statsOfflineDao monthsUpdatedStream emits a value`() {
        aggregatedStatsRepository = spy(aggregatedStatsRepository)

        val profileId = DEFAULT_PROFILE_ID
        val months = setOf(YearMonth.now().minusMonths(3))

        val subject = BehaviorProcessor.createDefault(true)
        whenever(statsOfflineDao.monthsUpdatedStream(profileId, months)).thenReturn(subject)

        val firstExpectedMonthStats = setOf(mock<MonthAggregatedStatsWithSessions>())
        val secondExpectedMonthStats = setOf(mock<MonthAggregatedStatsWithSessions>())
        val thirdExpectedMonthStats = setOf(mock<MonthAggregatedStatsWithSessions>())
        doReturn(
            Single.just(firstExpectedMonthStats),
            Single.just(secondExpectedMonthStats),
            Single.just(thirdExpectedMonthStats)
        )
            .whenever(aggregatedStatsRepository).monthStatsSingle(profileId, months)

        val observer =
            aggregatedStatsRepository.monthStatsStream(profileId, months).test()
                .assertEmpty()
                .also {
                    advanceTimeDebounce()
                }
                .assertValues(firstExpectedMonthStats)

        subject.onNext(true)

        observer
            .assertValueCount(1)
            .also {
                advanceTimeDebounce()
            }
            .assertValues(firstExpectedMonthStats, secondExpectedMonthStats)

        subject.onNext(true)

        observer
            .assertValueCount(2)
            .also {
                advanceTimeDebounce()
            }.assertValues(
                firstExpectedMonthStats,
                secondExpectedMonthStats,
                thirdExpectedMonthStats
            )
    }

    /*
    monthStats for multiple months
     */

    @Test
    fun `monthStats for multiple months emits error if StatsOfflineDao emits error`() {
        val profileId = DEFAULT_PROFILE_ID
        val month = YearMonth.now().minusMonths(3)

        val expectedError = RuntimeTestForcedException()
        whenever(statsOfflineDao.monthsWithDays(profileId, setOf(month))).thenAnswer {
            throw expectedError
        }

        aggregatedStatsRepository.monthStats(profileId, setOf(month)).test()
            .assertError(expectedError)
    }

    @Test
    fun `monthStats for multiple months emits expected MonthAggregatedStats for every month for which statsOfflineDao returns a value`() {
        val profileId = DEFAULT_PROFILE_ID
        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)

        val previousMonthAverageDuration = 546.3
        val previousMonthAverageSurface = 87.6
        val previousMonthAverageCheckup =
            createAverageCheckup(mapOf(MouthZone16.LoIncExt to 5.9f, MouthZone16.UpIncExt to 54.0f))

        val previousMonthWithDays = createMonthWithDayStats(
            monthAggregatedStatsEntity = createMonthAggregatedStatEntity(
                yearMonth = previousMonth,
                profileId = profileId,
                averageSurface = previousMonthAverageSurface,
                averageDuration = previousMonthAverageDuration,
                averageCheckup = previousMonthAverageCheckup
            )
        )

        whenever(statsOfflineDao.monthsWithDays(profileId, setOf(currentMonth, previousMonth)))
            .thenReturn(setOf(previousMonthWithDays))

        val monthAggregatedStats =
            aggregatedStatsRepository.monthStats(profileId, setOf(currentMonth, previousMonth))
                .test().values().single()

        assertEquals(2, monthAggregatedStats.size)

        assertEquals(
            previousMonthWithDays,
            monthAggregatedStats.single { it.month == previousMonth })
    }

    @Test
    fun `monthStats for multiple months emits empty MonthAggregatedStats for every month for which statsOfflineDao doesn't return a value`() {
        val profileId = DEFAULT_PROFILE_ID
        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)

        val previousMonthWithDays = createMonthWithDayStats(
            monthAggregatedStatsEntity = createMonthAggregatedStatEntity(
                yearMonth = previousMonth,
                profileId = profileId
            )
        )

        whenever(statsOfflineDao.monthsWithDays(profileId, setOf(currentMonth, previousMonth)))
            .thenReturn(setOf(previousMonthWithDays))

        val monthAggregatedStats =
            aggregatedStatsRepository.monthStats(profileId, setOf(currentMonth, previousMonth))
                .test().values().single()

        assertEquals(2, monthAggregatedStats.size)

        val expectedCurrentMonthWithDays = createMonthWithDayStats(
            monthAggregatedStatsEntity = createMonthAggregatedStatEntity(
                yearMonth = currentMonth,
                profileId = profileId,
                averageSurface = 0.0,
                averageDuration = 0.0,
                averageCheckup = emptyAverageCheckup()
            )
        )

        assertEquals(
            expectedCurrentMonthWithDays,
            monthAggregatedStats.single { it.month == currentMonth })
    }

    @Test
    fun `monthStats for multiple months emits empty MonthAggregatedStats for every month for which statsOfflineDao doesn't return a value, even if months parameter isn't ordered`() {
        val profileId = DEFAULT_PROFILE_ID
        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)

        val previousMonthWithDays = createMonthWithDayStats(
            monthAggregatedStatsEntity = createMonthAggregatedStatEntity(
                yearMonth = previousMonth,
                profileId = profileId
            )
        )

        whenever(statsOfflineDao.monthsWithDays(profileId, setOf(currentMonth, previousMonth)))
            .thenReturn(setOf(previousMonthWithDays))

        val monthAggregatedStats =
            aggregatedStatsRepository.monthStats(profileId, setOf(previousMonth, currentMonth))
                .test().values().single()

        assertEquals(2, monthAggregatedStats.size)

        val expectedCurrentMonthWithDays = createMonthWithDayStats(
            monthAggregatedStatsEntity = createMonthAggregatedStatEntity(
                yearMonth = currentMonth,
                profileId = profileId,
                averageSurface = 0.0,
                averageDuration = 0.0,
                averageCheckup = emptyAverageCheckup()
            )
        )

        assertEquals(
            expectedCurrentMonthWithDays,
            monthAggregatedStats.single { it.month == currentMonth })
    }

    /*
    dayStats for multiple days
     */

    @Test
    fun `dayStats for multiple days emits error if StatsOfflineDao emits error`() {
        val profileId = DEFAULT_PROFILE_ID
        val today = TrustedClock.getNowLocalDate()

        val expectedError = RuntimeTestForcedException()
        whenever(statsOfflineDao.daysWithSessions(profileId, listOf(today))).thenAnswer {
            throw expectedError
        }

        aggregatedStatsRepository.dayStats(profileId, setOf(today)).test()
            .assertError(expectedError)
    }

    @Test
    fun `dayStats for multiple days emits empty DayAggregatedStats for every LocalDate for which statsOfflineDao doesn't return a value`() {
        val profileId = DEFAULT_PROFILE_ID
        val today = TrustedClock.getNowLocalDate()
        val yesterday = today.minusDays(1)

        val dayWithSessions = createDayWithSessions(
            dayAggregatedEntity = createDayAggregatedStatsEntity(day = today, profileId = profileId)
        )

        whenever(statsOfflineDao.daysWithSessions(profileId, listOf(today, yesterday))).thenReturn(
            listOf(
                dayWithSessions
            )
        )

        val dayAggregatedStats =
            aggregatedStatsRepository.dayStats(profileId, setOf(today, yesterday)).test().values()
                .single()

        assertEquals(2, dayAggregatedStats.size)

        val expectedDaysWithSessionsForYesterday = createDayWithSessions(
            dayAggregatedEntity = createDayAggregatedStatsEntity(
                day = yesterday,
                profileId = profileId
            )
        )

        assertEquals(
            expectedDaysWithSessionsForYesterday,
            dayAggregatedStats.single { it.day == yesterday })
    }

    @Test
    fun `dayStats for multiple days emits DayAggregatedStats for every LocalDate for which statsOfflineDao returns a value`() {
        val profileId = DEFAULT_PROFILE_ID
        val today = TrustedClock.getNowLocalDate()
        val yesterday = today.minusDays(1)

        val expectedAverageDuration = 546.3
        val expectedAverageSurface = 87.6
        val expectedAverageCheckup =
            createAverageCheckup(mapOf(MouthZone16.LoIncExt to 5.9f, MouthZone16.UpIncExt to 54.0f))

        val todayDayWithSessions = createDayWithSessions(
            dayAggregatedEntity = createDayAggregatedStatsEntity(
                day = today,
                profileId = profileId,
                averageSurface = expectedAverageSurface,
                averageDuration = expectedAverageDuration,
                averageCheckup = expectedAverageCheckup
            )
        )

        whenever(statsOfflineDao.daysWithSessions(profileId, listOf(today, yesterday)))
            .thenReturn(listOf(todayDayWithSessions))

        val dayAggregatedStats =
            aggregatedStatsRepository.dayStats(profileId, setOf(today, yesterday)).test().values()
                .single()

        assertEquals(2, dayAggregatedStats.size)

        assertEquals(todayDayWithSessions, dayAggregatedStats.single { it.day == today })
    }

    /*
    weekStats for multiple weeks
     */

    @Test
    fun `weekStats for multiple weeks emits error if StatsOfflineDao emits error`() {
        val profileId = DEFAULT_PROFILE_ID
        val week = YearWeek.now()

        val expectedError = RuntimeTestForcedException()
        whenever(statsOfflineDao.weeksWithDays(profileId, setOf(week))).thenAnswer {
            throw expectedError
        }

        aggregatedStatsRepository.weekStats(profileId, setOf(week)).test()
            .assertError(expectedError)
    }

    @Test
    fun `weekStats for multiple weeks emits expected WeekAggregatedStats for every week for which statsOfflineDao returns a value`() {
        val profileId = DEFAULT_PROFILE_ID
        val currentDate = TrustedClock.getNowLocalDate()
        val currentWeek = currentDate.toYearWeek()
        val previousWeek = currentDate.minusWeeks(1).toYearWeek()

        val previousWeekAverageDuration = 546.3
        val previousWeekAverageSurface = 87.6
        val previousWeekAverageCheckup =
            createAverageCheckup(mapOf(MouthZone16.LoIncExt to 5.9f, MouthZone16.UpIncExt to 54.0f))

        val previousWeekWithDays = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(
                yearWeek = previousWeek,
                profileId = profileId,
                averageSurface = previousWeekAverageSurface,
                averageDuration = previousWeekAverageDuration,
                averageCheckup = previousWeekAverageCheckup
            )
        )

        whenever(statsOfflineDao.weeksWithDays(profileId, setOf(currentWeek, previousWeek)))
            .thenReturn(setOf(previousWeekWithDays))

        val weekAggregatedStats =
            aggregatedStatsRepository.weekStats(profileId, setOf(currentWeek, previousWeek)).test()
                .values().single()

        assertEquals(2, weekAggregatedStats.size)

        assertEquals(previousWeekWithDays, weekAggregatedStats.single { it.week == previousWeek })
    }

    @Test
    fun `weekStats for multiple weeks emits empty WeekAggregatedStats for every week for which statsOfflineDao doesn't return a value`() {
        val profileId = DEFAULT_PROFILE_ID
        val currentDate = TrustedClock.getNowLocalDate()
        val currentWeek = currentDate.toYearWeek()
        val previousWeek = currentDate.minusWeeks(1).toYearWeek()

        val previousWeekWithDays = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(
                yearWeek = previousWeek,
                profileId = profileId
            )
        )

        whenever(statsOfflineDao.weeksWithDays(profileId, setOf(currentWeek, previousWeek)))
            .thenReturn(setOf(previousWeekWithDays))

        val weekAggregatedStats =
            aggregatedStatsRepository.weekStats(profileId, setOf(currentWeek, previousWeek)).test()
                .values().single()

        assertEquals(2, weekAggregatedStats.size)

        val expectedCurrentWeekWithDays = createWeekWithDayStats(
            weekAggregatedStatsEntity = createWeekAggregatedStatEntity(
                yearWeek = currentWeek,
                profileId = profileId,
                averageSurface = 0.0,
                averageDuration = 0.0,
                averageCheckup = emptyAverageCheckup()
            )
        )

        assertEquals(
            expectedCurrentWeekWithDays,
            weekAggregatedStats.single { it.week == currentWeek })
    }

    /*
    Utils
     */

    private fun advanceTimeDebounce() {
        debounceScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
    }
}
