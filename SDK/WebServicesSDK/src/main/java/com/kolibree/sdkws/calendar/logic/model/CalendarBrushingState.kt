/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.calendar.logic.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.statsoffline.models.MonthAggregatedStatsWithSessions
import java.util.SortedMap
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

@Keep
@Parcelize
data class CalendarBrushingDayState(
    val date: LocalDate,
    val numberOfBrushings: Int,
    val isPerfectDay: Boolean = false
) : Parcelable

@Keep
@Parcelize
data class CalendarBrushingState(
    private val brushings: Map<LocalDate, CalendarBrushingDayState>,
    private val streaks: Set<BrushingStreak> = emptySet()
) : Parcelable {

    @Keep
    companion object {

        fun empty(): CalendarBrushingState =
            CalendarBrushingState(emptyMap())

        fun from(
            monthStats: Set<MonthAggregatedStatsWithSessions>,
            streaks: Set<BrushingStreak>
        ): CalendarBrushingState {
            return CalendarBrushingState(transformBrushings(monthStats), streaks)
        }

        private fun transformBrushings(
            monthStats: Set<MonthAggregatedStatsWithSessions>
        ): SortedMap<LocalDate, CalendarBrushingDayState> {
            val brushings = mutableMapOf<LocalDate, CalendarBrushingDayState>()
            monthStats.forEach { monthStat ->
                monthStat.sessionsMap.forEach { entry ->
                    brushings[entry.key] = CalendarBrushingDayState(
                        date = entry.key,
                        numberOfBrushings = entry.value.totalSessions,
                        isPerfectDay = entry.value.isPerfectDay
                    )
                }
            }
            return brushings.toSortedMap()
        }
    }

    @VisibleForTesting
    fun copyWithAdditionalBrushings(
        newBrushings: Map<LocalDate, CalendarBrushingDayState>
    ): CalendarBrushingState = copy(brushings = this.brushings + newBrushings)

    @VisibleForTesting
    fun copyWithAdditionalStreaks(
        newStreaks: Set<BrushingStreak>
    ): CalendarBrushingState = copy(streaks = this.streaks + newStreaks)

    fun numberOfPerfectDays(month: YearMonth): Int {
        var numberOfPerfectDays = 0
        for (dayOfTheMonth in 1..month.lengthOfMonth()) {
            val day = LocalDate.of(month.year, month.monthValue, dayOfTheMonth)
            if (brushings[day]?.isPerfectDay == true) {
                numberOfPerfectDays++
            }
        }
        return numberOfPerfectDays
    }

    fun numberOfBrushings(date: LocalDate): Int {
        return brushings[date]?.numberOfBrushings ?: 0
    }

    fun hasAnyBrushing(date: LocalDate): Boolean {
        return brushings[date]?.numberOfBrushings ?: 0 > 0 || belongsToStreak(date)
    }

    fun hasOneBrushing(date: LocalDate): Boolean {
        return brushings[date]?.numberOfBrushings ?: 0 == 1
    }

    fun hasMultipleBrushings(date: LocalDate): Boolean {
        return brushings[date]?.numberOfBrushings ?: 0 > 1
    }

    fun belongsToStreak(date: LocalDate): Boolean {
        return streaks.any { streak ->
            streak.start == date ||
                streak.end == date ||
                date.isAfter(streak.start) && date.isBefore(streak.end)
        }
    }

    fun isFirstDayOfStreak(date: LocalDate): Boolean {
        return streaks.any { streak -> streak.start == date }
    }

    fun isLastDayOfStreak(date: LocalDate): Boolean {
        return streaks.any { streak -> streak.end == date }
    }

    override fun toString(): String {
        return "CalendarBrushingState(brushings=${brushings.size}, streaks=${streaks.size})"
    }
}
