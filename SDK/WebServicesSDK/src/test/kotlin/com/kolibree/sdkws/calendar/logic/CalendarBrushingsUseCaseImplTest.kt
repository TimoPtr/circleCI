/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.calendar.logic

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.calendar.logic.model.BrushingStreak
import com.kolibree.android.calendar.logic.model.CalendarBrushingDayState
import com.kolibree.android.calendar.logic.model.CalendarBrushingState
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.statsoffline.models.DayAggregatedStatsWithSessions
import com.kolibree.statsoffline.models.MonthAggregatedStatsWithSessions
import com.kolibree.statsoffline.models.api.AggregatedStatsRepository
import com.kolibree.statsoffline.models.emptyAverageCheckup
import com.kolibree.statsoffline.persistence.models.StatsSession
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.YearMonth
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

internal class CalendarBrushingsUseCaseImplTest : BaseUnitTest() {

    private val accountId = 101L

    private val profileId = 1234L

    private val startDate: LocalDate = LocalDate.of(2018, 1, 5)

    private val endDate: LocalDate = LocalDate.of(2018, 4, 8)

    private val account = mock<AccountInternal> {
        whenever(it.id).thenReturn(accountId)
    }

    private val profile = mock<Profile> {
        whenever(it.id).thenReturn(profileId)
        whenever(it.pictureUrl).thenReturn("file://")
        whenever(it.firstName).thenReturn("John")
    }

    private val aggregatedStatsRepository = mock<AggregatedStatsRepository>()

    private val brushingRepository = mock<BrushingsRepository>()

    private val kolibreeConnector = mock<InternalKolibreeConnector>()

    private val calendarBrushingsRepository = mock<CalendarBrushingsRepository>()

    private lateinit var useCase: CalendarBrushingsUseCaseImpl

    override fun setup() {
        super.setup()

        whenever(profile.getCreationDate()).thenReturn(
            OffsetDateTime.of(
                2017,
                4,
                9, 0, 0, 0, 0, ZoneOffset.UTC
            )
        )
        whenever(kolibreeConnector.currentAccount()).thenReturn(account)
        whenever(aggregatedStatsRepository.monthStatsStream(eq(profileId), any()))
            .thenReturn(Flowable.just(emptyMonthStats(profileId, startDate)))

        useCase = spy(
            CalendarBrushingsUseCaseImpl(
                calendarBrushingsRepository,
                aggregatedStatsRepository,
                brushingRepository,
                kolibreeConnector,
                false
            )
        )
    }

    @Test
    fun `today returns current date`() {
        setToday(LocalDate.of(2018, 4, 3))
        assertEquals(LocalDate.of(2018, 4, 3), useCase.today)
    }

    @Test
    fun `currentMonth returns current correct data`() {
        setToday(LocalDate.of(2018, 4, 3))
        assertEquals(YearMonth.of(2018, 4), useCase.currentMonth)
    }

    @Test
    fun `calendarStartDate returns 1st day of month user made first brushing if user made it before profile creation`() {
        setupFirstBrushingMock()
        assertEquals(LocalDate.of(2017, 1, 1), useCase.calendarStartDate(profile))
    }

    @Test
    fun `calendarStartDate returns 1st day of month based on profile creation date if user has no brushings`() {
        whenever(brushingRepository.getFirstBrushingSession(any())).thenReturn(null)

        val profile = mock<Profile>()
        whenever(profile.getCreationDate()).thenReturn(
            OffsetDateTime.of(
                2017,
                4,
                9, 0, 0, 0, 0, ZoneOffset.UTC
            )
        )

        assertEquals(LocalDate.of(2017, 4, 1), useCase.calendarStartDate(profile))
    }

    @Test
    fun `calendarEndDate returns last day of the current month`() {
        setToday(LocalDate.of(2017, 5, 6))
        assertEquals(
            LocalDate.of(2017, 5, 31),
            CalendarBrushingsUseCaseImpl.calendarEndDate(useCase.today)
        )
    }

    @Test
    fun `calendarStartDateSingle returns date of first brushing if it was earlier than profile creation date`() {
        setupFirstBrushingMock()

        val subscriber = useCase.calendarStartDateSingle(profile).test()
        subscriber.assertValue(LocalDate.of(2017, 1, 1))
    }

    @Test
    fun `getBrushingDateRangeForCurrentProfile returns month range between first brushing month and current month`() {
        setToday(LocalDate.of(2018, 5, 5))
        setupFirstBrushingMock()

        val subscriber = useCase.getBrushingDateRange(profile).test()
        subscriber.assertValue(Pair(YearMonth.of(2017, 1), YearMonth.of(2018, 5)))
    }

    @Test
    fun `getBrushingsForCurrentProfile returns brushings and streaks from repository`() {
        setToday(LocalDate.of(2018, 5, 5))
        setupFirstBrushingMock()

        val streakStart = TrustedClock.getNowLocalDate()
        val streakEnd = streakStart.minusDays(1)

        whenever(calendarBrushingsRepository.getStreaksForProfile(profileId))
            .thenReturn(Flowable.just(setOf(BrushingStreak(streakStart, streakEnd))))

        val subscriber = useCase.getBrushingState(profile).test()

        subscriber.assertValue(
            CalendarBrushingState(
                brushings = mapOf(startDate to CalendarBrushingDayState(startDate, 0, false)),
                streaks = setOf(BrushingStreak(streakStart, streakEnd))
            )
        )
    }

    @Test
    fun `fetchPreviousBrushings recovers from error thrown from API`() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val month = YearMonth.of(2018, 2)

        doReturn(mock<Brushing> {
            whenever(it.dateTime).thenReturn(month.atDay(1).atStartOfDay(ZoneId.of("UTC")).toOffsetDateTime())
        }).whenever(brushingRepository).getFirstBrushingSession(eq(profileId))

        doReturn(Single.error<Unit>(RuntimeException("To be catched")))
            .whenever(brushingRepository).fetchRemoteBrushings(
                eq(accountId),
                eq(profileId),
                eq(LocalDate.of(2018, 1, 1)),
                eq(LocalDate.of(2018, 2, 28)),
                eq(null)
            )

        val subscriber = useCase.fetchPreviousBrushings(profile, month).test()

        subscriber.assertError(RuntimeException::class.java)
    }

    @Test
    fun `maybeFetchBrushingsBeforeMonth doesn't trigger fetchPreviousBrushings if previousBrushingsAlreadyChecked returns true`() {
        val month = YearMonth.of(2018, 2)

        doReturn(true).whenever(useCase).previousBrushingsAlreadyChecked(profileId, month)

        val subscriber = useCase.maybeFetchBrushingsBeforeMonth(profile, month).test()

        verify(useCase, times(0)).fetchPreviousBrushings(profile, month)
        subscriber.assertComplete()
    }

    @Test
    fun `getOldestLocalBrushingDate returns date of first brushing session`() {
        val today = LocalDate.of(2018, 5, 5)
        val latestBrushingSessionDate = LocalDate.of(2018, 3, 23)
        val latestBrushingSession = mock<Brushing> {
            whenever(it.dateTime).thenReturn(
                latestBrushingSessionDate.atStartOfDay(ZoneId.of("UTC")).toOffsetDateTime()
            )
        }
        setToday(today)

        doReturn(latestBrushingSession)
            .whenever(brushingRepository).getFirstBrushingSession(eq(profileId))

        assertEquals(latestBrushingSessionDate, useCase.getOldestLocalBrushingDate(profileId))
    }

    @Test
    fun `getOldestLocalBrushingDate returns today if we don't have brushing sessions`() {
        val today = LocalDate.of(2018, 5, 5)
        setToday(today)

        doReturn(null).whenever(brushingRepository).getFirstBrushingSession(eq(profileId))

        assertEquals(today, useCase.getOldestLocalBrushingDate(profileId))
    }

    @Test
    fun `previousBrushingsAlreadyChecked returns true if we have brushings from earlier date`() {
        val date = YearMonth.of(2018, 6)
        doReturn(LocalDate.of(2018, 4, 11))
            .whenever(useCase).getOldestLocalBrushingDate(eq(profileId))

        assertEquals(true, useCase.previousBrushingsAlreadyChecked(profileId, date))
    }

    @Test
    fun `previousBrushingsAlreadyChecked returns false if we don't have earlier brushings`() {
        val date = YearMonth.of(2018, 5)
        doReturn(LocalDate.of(2018, 5, 11))
            .whenever(useCase).getOldestLocalBrushingDate(eq(profileId))

        assertEquals(false, useCase.previousBrushingsAlreadyChecked(profileId, date))
    }

    private fun setupFirstBrushingMock() {
        val firstBrushingDate = LocalDate.of(2017, 1, 6).atStartOfDay(ZoneId.of("UTC")).toOffsetDateTime()
        val firstBrushing = mock<Brushing>()
        whenever(firstBrushing.dateTime).thenReturn(firstBrushingDate)
        whenever(brushingRepository.getFirstBrushingSession(any())).thenReturn(firstBrushing)
    }

    private fun setToday(todayDate: LocalDate) {
        val todayTime = LocalDateTime.of(todayDate, LocalTime.NOON)
        val todayZoned = ZonedDateTime.of(todayTime, ZoneId.of("UTC"))
        TrustedClock.setFixedDate(todayZoned)
    }

    private fun emptyMonthStats(
        profileId: Long,
        date: LocalDate
    ): Set<MonthAggregatedStatsWithSessions> =
        setOf(object : MonthAggregatedStatsWithSessions {
            override val month = YearMonth.of(date.year, date.monthValue)
            override val profileId = profileId
            override val averageDuration = 0.0
            override val averageSurface = 0.0
            override val averageCheckup = emptyAverageCheckup()
            override val sessionsMap = mapOf(date to object : DayAggregatedStatsWithSessions {
                override val day = date
                override val sessions = emptyList<StatsSession>()
                override val totalSessions = 0
                override val profileId = profileId
                override val averageDuration = 0.0
                override val averageSurface = 0.0
                override val averageCheckup = emptyAverageCheckup()
                override val isPerfectDay = false
                override val correctMovementAverage: Double = 0.0
                override val correctOrientationAverage: Double = 0.0
                override val underSpeedAverage: Double = 0.0
                override val correctSpeedAverage: Double = 0.0
                override val overSpeedAverage: Double = 0.0
                override val overPressureAverage: Double = 0.0
            })
            override val totalSessions: Int = 0
            override val sessionsPerDay: Double = 0.0
            override val correctMovementAverage: Double = 0.0
            override val correctOrientationAverage: Double = 0.0
            override val underSpeedAverage: Double = 0.0
            override val correctSpeedAverage: Double = 0.0
            override val overSpeedAverage: Double = 0.0
            override val overPressureAverage: Double = 0.0
        })
}
