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
import androidx.annotation.VisibleForTesting
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
import com.kolibree.statsoffline.persistence.models.MonthAggregatedStatsEntity
import com.kolibree.statsoffline.persistence.models.createBrushingDayStat
import com.kolibree.statsoffline.sumsDaysWithSessions
import com.kolibree.statsoffline.totalSessions
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

/**
 * Aggregated data for [IBrushing] in a [YearMonth]
 */
@Keep
interface MonthAggregatedStats : MultiDayAggregatedStats {
    val month: YearMonth
}

/**
 * Expanded [MonthAggregatedStats] that includes the [DayAggregatedStatsWithSessions] from which the aggregated stats
 * were calculated
 *
 * [sessionsMap] might contain future [LocalDate] as keys. Example: If today is July 28th,
 * [sessionsMap] length will be 31 and will include [LocalDate] until July 31st.
 * Future dates are not taken into account for average calculation
 */
@Keep
interface MonthAggregatedStatsWithSessions : MonthAggregatedStats {
    val sessionsMap: Map<LocalDate, DayAggregatedStatsWithSessions>
}

/**
 * Aggregated stats for [IBrushing] in a [YearMonth], as well as the Map<[LocalDate], [DayWithSessions]> from which the
 * aggregated stats were calculated
 */
internal data class MonthWithDayStats constructor(
    internal val monthStats: MonthAggregatedStatsEntity,
    val dayStats: Map<LocalDate, DayWithSessions>
) : MonthAggregatedStatsWithSessions, MonthAggregatedStats by monthStats {
    override val sessionsMap: Map<LocalDate, DayAggregatedStatsWithSessions> = dayStats

    /**
     * Returns all [DayWithSessions] contained in [dayStats]
     */
    val sessions: List<DayWithSessions> = dayStats.values.toList()

    /*
    Having separated withNewBrushingSessions and calculateAverage allows us to only calculate
    averages once when dealing with lists containing multiple instances referring to the same month.

    For example in StatsOfflineProcessor.updatedMonthsFromNewSessions, it can happen having
    multiple MonthWithDays instances for the same YearMonth.

    This decision might be questionable, since withNewBrushingSessions leaves the DayWithSessions
    instance in an incorrect state, with stale average values
     */

    fun withNewSessions(
        localDate: LocalDate,
        newSessions: List<BrushingSessionStatsEntity>
    ): MonthWithDayStats {
        val newDayStats = dayStats.withRecalculatedAverageFromSessions(localDate, newSessions)

        return copy(dayStats = newDayStats)
    }

    /*
    Check https://github.com/kolibree-git/iOS-KLSDK/blob/68b1650c8e583c8ddbc82a95e70927c18c93c550/KLSDKStats/KLSDKStats/Models/KLSDKBrushingMonthStat.m
     */
    fun calculateAverage(): MonthWithDayStats {
        val dayStatsWithRecalculatedAverage = dayStats.calculateAverageForDaysNotInTheFuture()

        return copy(
            /*
            https://kotlinlang.org/docs/reference/map-operations.html

            When the right-hand side operand contains entries with keys present in the left-hand side Map,
            the result map contains the entries from the right side.
             */
            dayStats = dayStats + dayStatsWithRecalculatedAverage,
            monthStats = monthStats.copy(
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

    companion object {
        fun empty(profileId: Long, month: YearMonth): MonthWithDayStats {
            val emptyMonthAggregatedEntity =
                MonthAggregatedStatsEntity(profileId, month)

            val emptyDayAggregatedEntities = emptyMonthAggregatedEntity.createEmptyDayStats()

            return MonthWithDayStats(
                monthStats = emptyMonthAggregatedEntity,
                dayStats = emptyDayAggregatedEntities
                    .groupBy { it.day }
                    .mapValues {
                        DayWithSessions(
                            dayStats = createBrushingDayStat(
                                profileId = profileId,
                                day = it.key
                            ),
                            brushingSessions = listOf()
                        )
                    }
            )
        }
    }
}

/**
 * Given a List<[MonthWithDayStats]> where all items share the same YearMonth, return a single [MonthWithDayStats]
 * containing all sessions in the initial list
 *
 * Example
 *
 * List contains
 * - June, mapOf(june1st -> (session1, session2), june2nd -> (session3))
 * - June, mapOf(june1st -> (session4), june15th -> (session5))
 *
 * The returned [MonthWithDayStats] will be
 *
 * June, mapOf(june1st -> (session1, session2, session4), june2nd -> (session3), june15th -> (session5)
 *
 * @throws AssertionError on debug if the List contains MonthWithDayStats referring to different months
 */
@VisibleForTesting
internal fun List<MonthWithDayStats>.sumSessions(): MonthWithDayStats {
    failIfListContainsDifferentMonths()

    val newDayStats: Map<LocalDate, DayWithSessions> = map { it.dayStats }.sumsDaysWithSessions()

    return first().copy(dayStats = newDayStats)
}

private fun List<MonthWithDayStats>.failIfListContainsDifferentMonths() {
    val listContainsDifferentMonths = map { it.month }.distinct().size > 1
    FailEarly.failInConditionMet(
        listContainsDifferentMonths,
        STATS_TAG,
        "MonthWithDayStats in list should refer to the same YearMonth"
    )
}
