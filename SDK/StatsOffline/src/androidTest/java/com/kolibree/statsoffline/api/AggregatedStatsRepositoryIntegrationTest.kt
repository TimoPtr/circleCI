/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.api

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.rules.UnitTestImmediateRxSchedulersOverrideRule
import com.kolibree.statsoffline.models.MonthWithDayStats
import com.kolibree.statsoffline.models.WeekWithDayStats
import com.kolibree.statsoffline.models.api.AggregatedStatsRepositoryImpl
import com.kolibree.statsoffline.persistence.BrushingSessionStatDao
import com.kolibree.statsoffline.persistence.DayAggregatedStatsDao
import com.kolibree.statsoffline.persistence.MonthAggregatedStatsDao
import com.kolibree.statsoffline.persistence.StatsOfflineDao
import com.kolibree.statsoffline.persistence.StatsOfflineRoomAppDatabase
import com.kolibree.statsoffline.persistence.StatsOfflineRoomModule
import com.kolibree.statsoffline.persistence.WeekAggregatedStatsDao
import com.kolibree.statsoffline.test.DEFAULT_PROFILE_ID
import com.kolibree.statsoffline.test.toYearMonth
import com.kolibree.statsoffline.toYearWeek
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AggregatedStatsRepositoryIntegrationTest : BaseInstrumentationTest() {

    @get:Rule
    val overrideSchedulersRule =
        UnitTestImmediateRxSchedulersOverrideRule()

    private lateinit var sessionStatDao: BrushingSessionStatDao
    private lateinit var monthStatDao: MonthAggregatedStatsDao
    private lateinit var weekStatDao: WeekAggregatedStatsDao
    private lateinit var dayStatDao: DayAggregatedStatsDao
    private lateinit var statsOfflineDatabase: StatsOfflineRoomAppDatabase

    private lateinit var statsOfflineDao: StatsOfflineDao

    private val debounceScheduler = TestScheduler()

    private lateinit var aggregatedStatsRepository: AggregatedStatsRepositoryImpl

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun setUp() {
        super.setUp()

        initRoom()

        aggregatedStatsRepository =
            AggregatedStatsRepositoryImpl(statsOfflineDao, debounceScheduler = debounceScheduler)
    }

    private fun initRoom() {
        statsOfflineDatabase =
            Room.inMemoryDatabaseBuilder(context(), StatsOfflineRoomAppDatabase::class.java)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        dayStatDao = statsOfflineDatabase.dayStatDao()
        monthStatDao = statsOfflineDatabase.monthStatDao()
        weekStatDao = statsOfflineDatabase.weekStatDao()
        sessionStatDao = statsOfflineDatabase.sessionStatDao()

        statsOfflineDao = StatsOfflineRoomModule.providesStatsOfflineDao(statsOfflineDatabase)
    }

    @Test
    fun monthStats_forMultipleMonths_returnsSetWithSpecifiedMonths() {
        val currentMonth = TrustedClock.getNowLocalDate().toYearMonth()
        val twoMonthsAgo = currentMonth.minusMonths(2)

        val profileId = DEFAULT_PROFILE_ID
        statsOfflineDao.getOrCreateMonthStats(profileId, currentMonth)
        statsOfflineDao.getOrCreateMonthStats(profileId, twoMonthsAgo)

        val monthsData =
            aggregatedStatsRepository.monthStats(profileId, setOf(twoMonthsAgo, currentMonth))
                .test()
                .values()
                .first()

        assertEquals(2, monthsData.size)

        monthsData.single { it.month == currentMonth }
        monthsData.single { it.month == twoMonthsAgo }
    }

    /*
    monthStatDao.monthsUpdated
     */

    @Test
    fun monthsUpdated_emitsANewValueEachTimeAMonthIsInserted() {
        val currentMonth = TrustedClock.getNowLocalDate().toYearMonth()
        val twoMonthsAgo = currentMonth.minusMonths(2)

        val profileId = DEFAULT_PROFILE_ID

        val monthObserver =
            monthStatDao.monthsUpdated(profileId, listOf(currentMonth, twoMonthsAgo))
                .test()
                .assertValues(0)

        statsOfflineDao.getOrCreateMonthStats(profileId, currentMonth)

        monthObserver.assertValues(0, 1)

        statsOfflineDao.getOrCreateMonthStats(profileId, twoMonthsAgo)

        monthObserver.assertValues(0, 1, 2)
    }

    /*
    weekStatDao.weeksUpdated
     */

    @Test
    fun weeksUpdated_emitsANewValueEachTimeAMonthIsInserted() {
        val currentWeek = TrustedClock.getNowLocalDate().toYearWeek()
        val pastWeek = TrustedClock.getNowLocalDate().minusWeeks(1).toYearWeek()

        val profileId = DEFAULT_PROFILE_ID

        val weekObserver =
            weekStatDao.weeksUpdated(profileId, listOf(currentWeek, pastWeek))
                .test()
                .assertValues(0)

        statsOfflineDao.getOrCreateWeekStats(profileId, currentWeek)

        weekObserver.assertValues(0, 1)

        statsOfflineDao.getOrCreateWeekStats(profileId, pastWeek)

        weekObserver.assertValues(0, 1, 2)
    }

    /*
    weekStatsStream
     */

    @Test
    fun weekStatsStream_returnsSetWithSpecifiedWeeks_evenIfDBIsEmpty() {
        val currentWeek = TrustedClock.getNowLocalDate().toYearWeek()
        val twoWeeksAgo = TrustedClock.getNowLocalDate().minusWeeks(2).toYearWeek()

        val profileId = DEFAULT_PROFILE_ID

        assertTrue(
            weekStatDao.readByWeek(profileId, listOf(currentWeek, twoWeeksAgo)).isEmpty()
        )

        val result =
            aggregatedStatsRepository.weekStatsStream(profileId, setOf(twoWeeksAgo, currentWeek))
                .test()
                .assertEmpty()
                .also {
                    advanceTimeDebounce()
                }
                .values().single()

        result.single { it.week == currentWeek }
        result.single { it.week == twoWeeksAgo }
    }

    @Test
    fun weekStatsStream_returnsSetWithSpecifiedWeeksEachTimeTheDBIsUpdated() {
        val currentWeek = TrustedClock.getNowLocalDate().toYearWeek()
        val twoWeeksAgo = TrustedClock.getNowLocalDate().minusWeeks(2).toYearWeek()

        val profileId = DEFAULT_PROFILE_ID

        statsOfflineDao.getOrCreateWeekStats(profileId, currentWeek)
        statsOfflineDao.getOrCreateWeekStats(profileId, twoWeeksAgo)

        val observer =
            aggregatedStatsRepository.weekStatsStream(profileId, setOf(twoWeeksAgo, currentWeek))
                .test()
                .assertEmpty()
                .also {
                    advanceTimeDebounce()
                }
                .assertValueCount(1)

        val firstResult = observer.values().first()

        assertEquals(2, firstResult.size)

        val currentWeekStats = firstResult.single { it.week == currentWeek } as WeekWithDayStats
        val previousWeekStats =
            firstResult.single { it.week == twoWeeksAgo } as WeekWithDayStats

        val updatedCurrentWeekAggregatedStats =
            currentWeekStats.weekStats.copy(averageSurface = 57.9)
        val updatedPreviousWeekAggregatedStats =
            previousWeekStats.weekStats.copy(averageDuration = 110.8)

        weekStatDao.insert(
            listOf(
                updatedCurrentWeekAggregatedStats,
                updatedPreviousWeekAggregatedStats
            )
        )

        observer
            .assertValueCount(1)
            .also {
                advanceTimeDebounce()
            }.assertValueCount(2)

        val secondResult = observer.values()[1]

        assertEquals(2, secondResult.size)

        val secondResultCurrentWeekStats =
            secondResult.single { it.week == currentWeek } as WeekWithDayStats
        val secondResultPreviousWeekStats =
            secondResult.single { it.week == twoWeeksAgo } as WeekWithDayStats

        assertEquals(updatedCurrentWeekAggregatedStats, secondResultCurrentWeekStats.weekStats)
        assertEquals(updatedPreviousWeekAggregatedStats, secondResultPreviousWeekStats.weekStats)
    }

    /*
    monthStatsStream
     */

    @Test
    fun monthStatsStream_returnsSetWithSpecifiedMonths_evenIfDBIsEmpty() {
        val currentMonth = TrustedClock.getNowLocalDate().toYearMonth()
        val twoMonthsAgo = currentMonth.minusMonths(2)

        val profileId = DEFAULT_PROFILE_ID

        assertTrue(
            monthStatDao.readByMonth(profileId, listOf(currentMonth, twoMonthsAgo)).isEmpty()
        )

        val result =
            aggregatedStatsRepository.monthStatsStream(profileId, setOf(twoMonthsAgo, currentMonth))
                .test()
                .assertEmpty()
                .also {
                    advanceTimeDebounce()
                }
                .values().single()

        result.single { it.month == currentMonth }
        result.single { it.month == twoMonthsAgo }
    }

    @Test
    fun monthStatsStream_returnsSetWithSpecifiedMonthsEachTimeTheDBIsUpdated() {
        val currentMonth = TrustedClock.getNowLocalDate().toYearMonth()
        val twoMonthsAgo = currentMonth.minusMonths(2)

        val profileId = DEFAULT_PROFILE_ID

        statsOfflineDao.getOrCreateMonthStats(profileId, currentMonth)
        statsOfflineDao.getOrCreateMonthStats(profileId, twoMonthsAgo)

        val observer =
            aggregatedStatsRepository.monthStatsStream(profileId, setOf(twoMonthsAgo, currentMonth))
                .test()
                .assertEmpty()
                .also {
                    advanceTimeDebounce()
                }
                .assertValueCount(1)

        val firstResult = observer.values().first()

        assertEquals(2, firstResult.size)

        val currentMonthStats = firstResult.single { it.month == currentMonth } as MonthWithDayStats
        val previousMonthStats =
            firstResult.single { it.month == twoMonthsAgo } as MonthWithDayStats

        val updatedCurrentMonthAggregatedStats =
            currentMonthStats.monthStats.copy(averageSurface = 57.9)
        val updatedPreviousMonthAggregatedStats =
            previousMonthStats.monthStats.copy(averageDuration = 110.8)

        monthStatDao.insert(
            listOf(
                updatedCurrentMonthAggregatedStats,
                updatedPreviousMonthAggregatedStats
            )
        )

        observer
            .assertValueCount(1)
            .also {
                advanceTimeDebounce()
            }.assertValueCount(2)

        val secondResult = observer.values()[1]

        assertEquals(2, secondResult.size)

        val secondResultCurrentMonthStats =
            secondResult.single { it.month == currentMonth } as MonthWithDayStats
        val secondResultPreviousMonthStats =
            secondResult.single { it.month == twoMonthsAgo } as MonthWithDayStats

        assertEquals(updatedCurrentMonthAggregatedStats, secondResultCurrentMonthStats.monthStats)
        assertEquals(updatedPreviousMonthAggregatedStats, secondResultPreviousMonthStats.monthStats)
    }

    /*
    dayStats
     */

    @Test
    fun dayStats_forMultipleDays_returnsSetWithSepecifiedDays() {
        val today = TrustedClock.getNowLocalDate().withDayOfMonth(8)
        val twoDaysAgo = today.minusDays(2)

        val profileId = DEFAULT_PROFILE_ID
        statsOfflineDao.getOrCreateMonthStats(profileId, today.toYearMonth())

        val daysData = aggregatedStatsRepository.dayStats(profileId, setOf(twoDaysAgo, today))
            .test()
            .values()
            .first()

        assertEquals(2, daysData.size)

        daysData.single { it.day == today }
        daysData.single { it.day == twoDaysAgo }
    }

    /*
    week
     */

    @Test
    fun weekStats_forMultipleWeeks_returnsSetWithSpecifiedWeeks() {
        val now = TrustedClock.getNowLocalDate()
        val currentWeek = now.toYearWeek()
        val twoWeeksAgo = now.minusWeeks(2).toYearWeek()

        val profileId = DEFAULT_PROFILE_ID
        statsOfflineDao.getOrCreateWeekStats(profileId, currentWeek)
        statsOfflineDao.getOrCreateWeekStats(profileId, twoWeeksAgo)

        val weeksData =
            aggregatedStatsRepository.weekStats(profileId, setOf(twoWeeksAgo, currentWeek))
                .test()
                .values()
                .first()

        assertEquals(2, weeksData.size)

        weeksData.single { it.week == currentWeek }
        weeksData.single { it.week == twoWeeksAgo }
    }

    /*
    Utils
     */

    private fun advanceTimeDebounce() {
        debounceScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
    }
}
