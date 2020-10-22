/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import androidx.annotation.VisibleForTesting
import androidx.core.util.rangeTo
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.calendar.logic.model.CalendarBrushingState
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.isFirstDayOfTheMonth
import com.kolibree.android.extensions.isLastDayOfTheMonth
import com.kolibree.sdkws.calendar.logic.CalendarBrushingsUseCase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.Locale
import javax.inject.Inject
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.WeekFields

internal class FrequencyBrushingUseCase @Inject constructor(
    private val calendarBrushingsUseCase: CalendarBrushingsUseCase,
    private val currentProfileProvider: CurrentProfileProvider
) {

    fun getBrushingStateForCurrentProfile(): Flowable<Pair<Profile, List<FrequencyChartViewState>>> {
        return currentProfileProvider.currentProfileFlowable()
            .getBrushingStateAndDateRange()
            .map { (profile, brushingState, dateRange) ->
                val today = YearMonth.now().atEndOfMonth()
                val endDate = today.plusDays(1)
                var current = dateRange.first.atDay(1)
                val monthDays = MonthDays()

                while (current.isBefore(endDate)) {
                    val dayType = toDateType(current, brushingState)
                    monthDays.put(dayType, current)
                    current = current.plusDays(1)
                }

                profile to toResult(monthDays)
            }
    }

    private fun Flowable<Profile>.getBrushingStateAndDateRange():
        Flowable<Triple<Profile, CalendarBrushingState, Pair<YearMonth, YearMonth>>> {
        return this
            .switchMap { profile ->
                calendarBrushingsUseCase
                    .getBrushingState(profile)
                    .map { brushingState -> profile to brushingState }
            }
            .switchMapSingle { (profile, brushingState) ->
                dateRange(profile).map { dateRange -> Triple(profile, brushingState, dateRange) }
            }
    }

    fun prepareDataBeforeMonth(month: YearMonth): Completable {
        return currentProfileProvider
            .currentProfileSingle()
            .flatMapCompletable { profile ->
                calendarBrushingsUseCase.maybeFetchBrushingsBeforeMonth(profile, month)
            }
    }

    private fun toResult(monthDays: MonthDays): List<FrequencyChartViewState> {
        return monthDays.toMonthsList().map {
            val adjustedToFirstDayOfWeek = adjustToFirstDayOfWeek(it.month, it.days)
            FrequencyChartViewState(adjustedToFirstDayOfWeek)
        }
    }

    @VisibleForTesting
    fun adjustToFirstDayOfWeek(
        month: YearMonth,
        days: List<DayType>,
        locale: Locale = Locale.getDefault()
    ): List<DayType> {
        val weekFields = WeekFields.of(locale)
        val shiftDays = mutableListOf<DayType>()
        val firstDay = month.atDay(1)
        while (firstDay.dayOfWeek != weekFields.firstDayOfWeek.plus(shiftDays.size.toLong())) {
            shiftDays += DayType.EmptyDay
        }
        return shiftDays + days
    }

    @VisibleForTesting
    fun toDateType(day: LocalDate, state: CalendarBrushingState): DayType {
        return when {
            isOlderThanProfile(day) -> DayType.NotAvailableDay
            isFutureDay(day) -> DayType.FutureDay
            state.numberOfBrushings(day) == 0 -> DayType.NoBrushingDay
            isPerfectDay(day, state) -> perfectDay(day, state)
            state.hasOneBrushing(day) -> DayType.SingleBrushingDay(day)
            else -> DayType.EmptyDay
        }
    }

    @VisibleForTesting
    fun isOlderThanProfile(day: LocalDate): Boolean {
        val profileCreationDate = currentProfileProvider.currentProfile()
            .getCreationDate()
            .toLocalDate()

        return day.isBefore(profileCreationDate)
    }

    @VisibleForTesting
    fun isPerfectDay(day: LocalDate, state: CalendarBrushingState): Boolean {
        return state.hasMultipleBrushings(day) || state.belongsToStreak(day)
    }

    private fun perfectDay(day: LocalDate, state: CalendarBrushingState): DayType {
        val streak = state.belongsToStreak(day)
        val isPerfectDayBefore = streak &&
            !state.isFirstDayOfStreak(day) &&
            !day.isFirstDayOfTheMonth()
        val isPerfectDayAfter = streak &&
            !state.isLastDayOfStreak(day) &&
            !day.isLastDayOfTheMonth()
        return DayType.PerfectDay(
            day,
            state.numberOfBrushings(day),
            isPerfectDayBefore,
            isPerfectDayAfter
        )
    }

    @VisibleForTesting
    fun isFutureDay(day: LocalDate): Boolean {
        return day.isAfter(TrustedClock.getNowLocalDate())
    }

    @VisibleForTesting
    fun dateRange(profile: Profile): Single<Pair<YearMonth, YearMonth>> {
        return calendarBrushingsUseCase.getBrushingDateRange(profile)
    }
}

internal class MonthDays {
    private val monthType: MutableMap<YearMonth, MutableList<DayType>> = mutableMapOf()

    fun put(type: DayType, date: LocalDate) {
        val month = YearMonth.from(date)
        if (!monthType.containsKey(month)) {
            monthType[month] = mutableListOf()
        }
        monthType[month]?.add(type)
    }

    fun toMonthsList(): List<MonthDayType> {
        val keys = monthType.keys.sorted()
        val result = mutableListOf<MonthDayType>()
        for (key in keys) {
            result += MonthDayType(
                month = key,
                days = monthType[key] ?: emptyList()
            )
        }
        return result
    }
}

internal data class MonthDayType(
    val month: YearMonth,
    val days: List<DayType>
)
