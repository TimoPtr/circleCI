/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.models

import androidx.annotation.Keep
import com.kolibree.android.failearly.FailEarly
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.statsoffline.STATS_TAG
import com.kolibree.statsoffline.calculateAverageCheckup
import com.kolibree.statsoffline.calculateAverageDuration
import com.kolibree.statsoffline.calculateAverageForDaysNotInTheFuture
import com.kolibree.statsoffline.calculateAverageSurface
import com.kolibree.statsoffline.calculateCorrectMovementAverage
import com.kolibree.statsoffline.calculateCorrectOrientationAverage
import com.kolibree.statsoffline.calculateCorrectSpeedAverage
import com.kolibree.statsoffline.calculateOverPressureAverage
import com.kolibree.statsoffline.calculateOverSpeedAverage
import com.kolibree.statsoffline.calculateSessionsPerDay
import com.kolibree.statsoffline.calculateUnderSpeedAverage
import com.kolibree.statsoffline.persistence.models.BrushingSessionStatsEntity
import com.kolibree.statsoffline.persistence.models.WeekAggregatedStatsEntity
import com.kolibree.statsoffline.persistence.models.createBrushingDayStat
import com.kolibree.statsoffline.sumsDaysWithSessions
import com.kolibree.statsoffline.totalSessions
import org.threeten.bp.LocalDate

/**
 * Aggregated data for [IBrushing] in a [YearWeek]
 *
 * A week can have `[1-7]` days. Please read [YearWeek] documentation carefully to understand the aggregated data
 * exposed by this interface
 */
@Keep
interface WeekAggregatedStats : MultiDayAggregatedStats {
    val week: YearWeek
}

/**
 * Expanded [WeekAggregatedStats] that includes the [DayAggregatedStatsWithSessions] from which the aggregated stats
 * were calculated
 *
 * A week can have `[1-7]` days. Please read [YearWeek] documentation carefully to understand the aggregated data
 * exposed by this interface
 *
 * [sessionsMap] might contain future [LocalDate] as keys. Example: Assuming it's not Week 0, if today is
 * Wednesday, [sessionsMap] length will be 7 and will include [LocalDate] until last day of week.
 * Future dates are not taken into account for average calculation
 */
@Keep
interface WeekAggregatedStatsWithSessions : WeekAggregatedStats {
    val sessionsMap: Map<LocalDate, DayAggregatedStatsWithSessions>
}

/**
 * Aggregated stats for [IBrushing] in a [YearWeek], as well as the Map<[LocalDate], [DayWithSessions]> from which the
 * aggregated stats were calculated
 */
internal data class WeekWithDayStats constructor(
    internal val weekStats: WeekAggregatedStatsEntity,
    val dayStats: Map<LocalDate, DayWithSessions>
) : WeekAggregatedStatsWithSessions, WeekAggregatedStats by weekStats {
    override val sessionsMap: Map<LocalDate, DayAggregatedStatsWithSessions> = dayStats

    fun withNewSessions(
        localDate: LocalDate,
        newSessions: List<BrushingSessionStatsEntity>
    ): WeekWithDayStats {
        val newDayStats = dayStats.withRecalculatedAverageFromSessions(localDate, newSessions)

        return copy(dayStats = newDayStats)
    }

    fun calculateAverage(): WeekWithDayStats {
        val dayStatsWithRecalculatedAverage = dayStats.calculateAverageForDaysNotInTheFuture()

        return copy(
            /*
            https://kotlinlang.org/docs/reference/map-operations.html

            When the right-hand side operand contains entries with keys present in the left-hand side Map,
            the result map contains the entries from the right side.
             */
            dayStats = dayStats + dayStatsWithRecalculatedAverage,
            weekStats = weekStats.copy(
                averageDuration = dayStatsWithRecalculatedAverage.calculateAverageDuration(),
                averageSurface = dayStatsWithRecalculatedAverage.calculateAverageSurface(),
                averageCheckup = dayStatsWithRecalculatedAverage.calculateAverageCheckup(),
                sessionsPerDay = dayStatsWithRecalculatedAverage.calculateSessionsPerDay(),
                totalSessions = dayStatsWithRecalculatedAverage.totalSessions(),
                correctMovementAverage = dayStatsWithRecalculatedAverage.calculateCorrectMovementAverage(),
                underSpeedAverage = dayStatsWithRecalculatedAverage.calculateUnderSpeedAverage(),
                correctSpeedAverage = dayStatsWithRecalculatedAverage.calculateCorrectSpeedAverage(),
                overSpeedAverage = dayStatsWithRecalculatedAverage.calculateOverSpeedAverage(),
                correctOrientationAverage = dayStatsWithRecalculatedAverage.calculateCorrectOrientationAverage(),
                overPressureAverage = dayStatsWithRecalculatedAverage.calculateOverPressureAverage()
            )
        )
    }

    /**
     * @return true if [date] belongs to this week, false otherwise
     */
    fun accept(date: LocalDate): Boolean = date in dayStats.keys

    companion object {
        fun empty(profileId: Long, week: YearWeek): WeekWithDayStats {
            val emptyWeekAggregatedEntity = WeekAggregatedStatsEntity(profileId, week)

            val emptyDayAggregatedEntities = emptyWeekAggregatedEntity.createEmptyDayStats()

            return WeekWithDayStats(
                weekStats = emptyWeekAggregatedEntity,
                dayStats = emptyDayAggregatedEntities
                    .groupBy { it.day }
                    .mapValues {
                        DayWithSessions(
                            dayStats = createBrushingDayStat(profileId = profileId, day = it.key),
                            brushingSessions = listOf()
                        )
                    }
            )
        }
    }
}

/**
 * Given a List<[WeekWithDayStats]> where all items share the same YearWeek, return a single [WeekWithDayStats]
 * containing all sessions in the initial list
 *
 * Example
 *
 * List contains
 * - Week11, mapOf(Monday -> (session1, session2), Wednesday -> (session3))
 * - Week11, mapOf(Monday -> (session4), Saturday -> (session5))
 *
 * The returned [WeekWithDayStats] will be
 *
 * Week11, mapOf(Monday -> (session1, session2, session4), Wednesday -> (session3), Saturday -> (session5)
 *
 * @throws AssertionError on debug if the List contains WeekWithDayStats referring to different weeks
 */
internal fun List<WeekWithDayStats>.sumSessions(): WeekWithDayStats {
    failIfListContainsDifferentWeeks()

    val newDayStats: Map<LocalDate, DayWithSessions> = map { it.dayStats }.sumsDaysWithSessions()

    return first().copy(dayStats = newDayStats)
}

private fun List<WeekWithDayStats>.failIfListContainsDifferentWeeks() {
    val listContainsDifferentWeeks = map { it.week }.distinct().size > 1
    FailEarly.failInConditionMet(
        listContainsDifferentWeeks,
        STATS_TAG,
        "WeekWithDayStats in list should refer to the same YearMonth"
    )
}
