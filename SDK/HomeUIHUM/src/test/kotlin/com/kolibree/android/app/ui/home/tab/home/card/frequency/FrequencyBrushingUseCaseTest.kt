/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.calendar.logic.model.BrushingStreak
import com.kolibree.android.calendar.logic.model.CalendarBrushingDayState
import com.kolibree.android.calendar.logic.model.CalendarBrushingState
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.sdkws.calendar.logic.CalendarBrushingsUseCase
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.util.Locale
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.YearMonth
import org.threeten.bp.ZonedDateTime

internal class FrequencyBrushingUseCaseTest : BaseUnitTest() {

    lateinit var useCase: FrequencyBrushingUseCase

    private val calendarBrushingsUseCase = mock<CalendarBrushingsUseCase>()

    private val currentProfileProvider = mock<CurrentProfileProvider>()

    private val profileCreationDate = ZonedDateTime.now().minusDays(20)

    private val mockProfile = ProfileBuilder.create()
        .withCreationDate(profileCreationDate)
        .build()

    override fun setup() {
        super.setup()

        whenever(currentProfileProvider.currentProfile()).thenReturn(mockProfile)
        useCase = FrequencyBrushingUseCase(calendarBrushingsUseCase, currentProfileProvider)
    }

    /*
    isFutureDay
     */

    @Test
    fun `isFutureDay returns true if day from the future`() {
        val today = TrustedClock.getNowLocalDate()

        assertTrue(useCase.isFutureDay(today.plusDays(1)))
        assertTrue(useCase.isFutureDay(today.plusDays(2)))
        assertTrue(useCase.isFutureDay(today.plusDays(12)))
    }

    @Test
    fun `isFutureDay returns false if day from the past or today`() {
        val today = TrustedClock.getNowLocalDate()

        assertFalse(useCase.isFutureDay(today))
        assertFalse(useCase.isFutureDay(today.minusDays(1)))
        assertFalse(useCase.isFutureDay(today.minusDays(2)))
        assertFalse(useCase.isFutureDay(today.minusDays(9)))
    }

    /*
    dateRange
     */

    @Test
    fun `dateRange invokes getBrushingDateRangeForCurrentProfile on calendar brushing use case`() {
        val start = YearMonth.now()
        val end = start.minusMonths(1L)
        whenever(calendarBrushingsUseCase.getBrushingDateRange(mockProfile))
            .thenReturn(Single.just(start to end))

        val range = useCase.dateRange(mockProfile).blockingGet()
        assertEquals(start, range.first)
        assertEquals(end, range.second)

        verify(calendarBrushingsUseCase).getBrushingDateRange(mockProfile)
    }

    /*
    adjustToFirstDayOfWeek
     */

    @Test
    fun `adjustToFirstDayOfWeek adds 4 days if first day in month is Friday`() {
        val month = YearMonth.of(2020, Month.MAY)
        val adjustedDays = useCase.adjustToFirstDayOfWeek(month, emptyList(), Locale.UK)
        assertEquals(4, adjustedDays.size)
    }

    @Test
    fun `adjustToFirstDayOfWeek adds 0 days if first day in month is Monday`() {
        val month = YearMonth.of(2020, Month.JUNE)
        val adjustedDays = useCase.adjustToFirstDayOfWeek(month, emptyList(), Locale.UK)
        assertEquals(0, adjustedDays.size)
    }

    @Test
    fun `adjustToFirstDayOfWeek adds 6 days if first day in month is Sunday`() {
        val month = YearMonth.of(2020, Month.MARCH)
        val adjustedDays = useCase.adjustToFirstDayOfWeek(month, emptyList(), Locale.UK)
        assertEquals(6, adjustedDays.size)
    }

    /*
    toDateType
     */

    @Test
    fun `toDateType returns FutureDay if day is from the future`() {
        val today = TrustedClock.getNowLocalDate()

        val day = today.plusDays(1)
        val type = useCase.toDateType(day, CalendarBrushingState.empty())
        assertEquals(DayType.FutureDay, type)
    }

    @Test
    fun `toDateType returns NoBrushingDay if day has no brushings`() {
        val today = TrustedClock.getNowLocalDate()

        val day = today.minusDays(3)
        val type = useCase.toDateType(day, CalendarBrushingState.empty())
        assertEquals(DayType.NoBrushingDay, type)
    }

    @Test
    fun `toDateType returns SingleBrushingDay if day has only one brushing`() {
        val today = TrustedClock.getNowLocalDate()

        val day = today.minusDays(3)
        val type = useCase.toDateType(
            day,
            createCalendarBrushingState(day, 1)
        )
        assertEquals(DayType.SingleBrushingDay(day), type)
    }

    @Test
    fun `toDateType returns PerfectDay if day has at least 2 brushing`() {
        val today = TrustedClock.getNowLocalDate()

        val day = today.minusDays(5)
        val type = useCase.toDateType(
            day,
            createCalendarBrushingState(day, 2)
        )
        assertEquals(DayType.PerfectDay(day, 2), type)
    }

    @Test
    fun `toDateType returns PerfectDay if day belongs to streak`() {
        val today = TrustedClock.getNowLocalDate()

        val day = today.minusDays(3)
        val type = useCase.toDateType(
            day,
            createCalendarBrushingState(day, 1, belongsToStreak = true)
        )
        assertEquals(DayType.PerfectDay(day, 1), type)
    }

    @Test
    fun `toDateType returns NotAvailableDay if day is from past and there is no brushings`() {
        val today = profileCreationDate.toLocalDate()

        val day = today.minusDays(10)
        val type = useCase.toDateType(day, CalendarBrushingState.empty())
        assertEquals(DayType.NotAvailableDay, type)
    }

    /**
    isNewUser
     */

    @Test
    fun `isOlderThanProfile returns true if day is before profile creation date`() {
        val today = profileCreationDate.toLocalDate()

        val dayFromPast = today.minusDays(1)
        assertTrue(useCase.isOlderThanProfile(dayFromPast))

        val dayFromFuture = today.plusDays(1)
        assertFalse(useCase.isOlderThanProfile(dayFromFuture))
    }

    /*
    isPerfectDay
     */
    @Test
    fun `when day has at least 2 brushings then is perfect`() {
        val today = TrustedClock.getNowLocalDate()
        val perfectState = createCalendarBrushingState(today, 2)
        assertTrue(useCase.isPerfectDay(today, perfectState))

        val notPerfectState = createCalendarBrushingState(today, 1)
        assertFalse(useCase.isPerfectDay(today, notPerfectState))
    }

    @Test
    fun `when day belongs to streak then is perfect`() {
        val today = TrustedClock.getNowLocalDate()
        val yesterday = today.minusDays(1)
        val beforeYesterday = yesterday.minusDays(1)
        val brushingMap = mapOf(
            beforeYesterday to CalendarBrushingDayState(beforeYesterday, 1, false),
            yesterday to CalendarBrushingDayState(today, 4, true),
            today to CalendarBrushingDayState(today, 1, true)
        )
        val state = CalendarBrushingState.empty()
            .copyWithAdditionalBrushings(brushingMap)
            .copyWithAdditionalStreaks(setOf(BrushingStreak(yesterday, today)))

        assertTrue(useCase.isPerfectDay(today, state))
        assertTrue(useCase.isPerfectDay(yesterday, state))
        assertFalse(useCase.isPerfectDay(beforeYesterday, state))
    }

    /*
    utils
     */

    private fun createCalendarBrushingState(
        date: LocalDate,
        numberOfBrushings: Int,
        belongsToStreak: Boolean = false
    ): CalendarBrushingState {
        val map = mutableMapOf<LocalDate, CalendarBrushingDayState>()
        map[date] = createCalendarBrushingDate(date, numberOfBrushings)
        val state = CalendarBrushingState(brushings = map)
        if (belongsToStreak) {
            return state.copyWithAdditionalStreaks(setOf(BrushingStreak(date, date)))
        }
        return state
    }

    private fun createCalendarBrushingDate(date: LocalDate, brushings: Int) =
        CalendarBrushingDayState(
            date = date,
            isPerfectDay = true,
            numberOfBrushings = brushings
        )
}
