/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.charts

import com.kolibree.android.KOLIBREE_DAY_START_HOUR
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.charts.models.Stat
import com.kolibree.charts.models.WeeklyStat
import com.kolibree.charts.persistence.repo.StatRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.threeten.bp.temporal.ChronoUnit

/**
 * Created by guillaumeagis on 22/05/18.
 */
class DashboardCalculatorTest : BaseUnitTest() {

    @Mock
    private lateinit var statRepository: StatRepository

    private lateinit var weeklyStatCalculator: WeeklyStatCalculator

    @Throws(Exception::class)
    override fun setup() {
        super.setup()

        weeklyStatCalculator = Mockito.spy(
            WeeklyStatCalculator(statRepository, TrustedClock.utcClock)
        )
    }

    @Test
    fun adjustedStartDate_currentTimeBeforeKolibreeTime_returnsCurrentTimeMinus7DaysAtStartOfKolibreeDay() {
        val endDate = TrustedClock.getNowZonedDateTime()
            .truncatedTo(ChronoUnit.DAYS)
            .withHour(KOLIBREE_DAY_START_HOUR - 1)

        val expectedTime = endDate.minusDays(7)
            .truncatedTo(ChronoUnit.DAYS)
            .withHour(KOLIBREE_DAY_START_HOUR)

        assertEquals(expectedTime, weeklyStatCalculator.adjustedStartDate(endDate))
    }

    @Test
    fun adjustedStartDate_currentTimeAfterKolibreeTime_returnsCurrentTimeMinus7DaysAtStartOfKolibreeDay() {
        val endDate = TrustedClock.getNowZonedDateTime()
            .truncatedTo(ChronoUnit.DAYS)
            .withHour(KOLIBREE_DAY_START_HOUR)
            .withSecond(1)

        val expectedTime = endDate.minusDays(6)
            .truncatedTo(ChronoUnit.DAYS)
            .withHour(KOLIBREE_DAY_START_HOUR)

        assertEquals(expectedTime, weeklyStatCalculator.adjustedStartDate(endDate))
    }

    /*
  WEEKLY BRUSHING FOR PROFILE
   */
    @Test
    fun weeklyBrushingForProfile_nullProfile_returnsEmptyWeeklyBrushing() {
        val weeklyBrushing = weeklyStatCalculator.getWeeklyStatForProfile(null).test()
            .assertValueCount(1)
            .values()[0]

        assertEquals(0, weeklyBrushing.averageBrushingTime.toLong())
        assertEquals(0, weeklyBrushing.getAverageSurface().toLong())
        for (dayStat in weeklyBrushing.data) {
            assertEquals(0, dayStat.count().toLong())
        }

        verify(weeklyStatCalculator, never()).createWeeklyStatFromBrushingData(anyList())
    }

    @Test
    fun weeklyBrushingForProfile_nonNullProfile_returnsWeeklyBrushingWithData() {
        val profileId = 54L

        val endDate = TrustedClock.getNowZonedDateTime()
        doReturn(endDate).whenever(weeklyStatCalculator).currentTime()

        doReturn(endDate).whenever(weeklyStatCalculator).adjustedStartDate(endDate)

        val expectedList = mock<List<Stat>>()
        whenever(statRepository.getStatsSince(endDate, profileId))
            .thenReturn(Flowable.just<List<Stat>>(expectedList))

        val expectedWeeklyBrushing = mock<WeeklyStat>()
        doReturn(expectedWeeklyBrushing).whenever(weeklyStatCalculator)
            .createWeeklyStatFromBrushingData(anyList())

        val observer = weeklyStatCalculator
            .getWeeklyStatForProfile(profileId).test()
            .assertValueCount(1)

        assertEquals(expectedWeeklyBrushing, observer.values()[0])
    }
}
