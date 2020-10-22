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
import com.kolibree.android.commons.MINIMAL_AVERAGE_SURFACE_PER_BRUSHING_FOR_PERFECTION
import com.kolibree.android.commons.extensions.zeroIfNan
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.statsoffline.calculateCorrectMovementAverage
import com.kolibree.statsoffline.calculateCorrectOrientationAverage
import com.kolibree.statsoffline.calculateCorrectSpeedAverage
import com.kolibree.statsoffline.calculateOverPressureAverage
import com.kolibree.statsoffline.calculateOverSpeedAverage
import com.kolibree.statsoffline.calculateUnderSpeedAverage
import com.kolibree.statsoffline.getDate
import com.kolibree.statsoffline.persistence.models.BrushingSessionStatsEntity
import com.kolibree.statsoffline.persistence.models.DayAggregatedStatsEntity
import com.kolibree.statsoffline.persistence.models.StatsSession
import com.kolibree.statsoffline.persistence.models.createBrushingDayStat
import com.kolibree.statsoffline.roundOneDecimal
import org.threeten.bp.LocalDate

/**
 * Aggregated data for [IBrushing] belonging to the same Kolibree Day
 *
 * Kolibree days start at 4AM. The reasoning behind this is that some users that go to bed late (1AM) wanted those
 * brushings to belong to the day they have lived on, not the actual date.
 *
 * Examples
 *
 * - 1/10/2019 23:00 -> Kolibree day is 01/10/2019
 * - 1/10/2019 02:40 -> Kolibree day is 30/09/2019
 */
@Keep
interface DayAggregatedStats : AggregatedStats {
    val day: LocalDate

    /**
     * Flags the day as perfect.
     *
     * Perfect day means that a user (profile) brushed 2 times (or more) with 80% (or more) surface coverage
     * for a given day.
     */
    val isPerfectDay: Boolean
}

/**
 * Expanded [DayAggregatedStats] that includes the [StatsSession] from which the aggregated stats were calculated
 */
@Keep
interface DayAggregatedStatsWithSessions : DayAggregatedStats {
    val sessions: List<StatsSession>
}

/**
 * Perfect day means that a user (profile) brushed 2 times ...
 */
private const val MINIMAL_NUMBER_OF_BRUSHINGS_FOR_PERFECTION = 2

/**
 * Aggregated stats for all [IBrushing] created at a given [LocalDate], as well as the
 * List<[BrushingSessionStatsEntity]> from which the aggregated stats were calculated
 */
internal data class DayWithSessions(
    internal val dayStats: DayAggregatedStatsEntity,
    internal val brushingSessions: List<BrushingSessionStatsEntity>
) : DayAggregatedStatsWithSessions, DayAggregatedStats by dayStats {
    override val sessions: List<StatsSession> = brushingSessions

    fun withReculatedAverageFromSessions(newBrushingSessions: List<BrushingSessionStatsEntity>): DayWithSessions {
        return withNewBrushingSessions(newBrushingSessions).calculateAverage()
    }

    fun withNewBrushingSessions(newBrushingSessions: List<BrushingSessionStatsEntity>): DayWithSessions {
        return copy(
            brushingSessions = brushingSessions.toMutableList()
                .apply {
                    addAll(newBrushingSessions)
                }
                .toList()
        )
    }

    /*
    See https://github.com/kolibree-git/iOS-KLSDK/blob/68b1650c8e583c8ddbc82a95e70927c18c93c550/KLSDKStats/KLSDKStats/Models/KLSDKBrushingDayStat.m
     */
    fun calculateAverage(): DayWithSessions {
        return copy(
            dayStats = dayStats.copy(
                averageDuration = calculateAverageDuration(),
                averageSurface = calculateAverageSurface(),
                averageCheckup = calculateAverageCheckupValues(),
                isPerfectDay = calculateIsPerfectDay(),
                totalSessions = brushingSessions.size,
                correctMovementAverage = calculateCorrectMovementAverage(),
                underSpeedAverage = calculateUnderSpeedAverage(),
                correctSpeedAverage = calculateCorrectSpeedAverage(),
                overSpeedAverage = calculateOverSpeedAverage(),
                correctOrientationAverage = calculateCorrectOrientationAverage(),
                overPressureAverage = calculateOverPressureAverage()
            )
        )
    }

    @VisibleForTesting
    fun calculateAverageSurface() =
        brushingSessions.map { it.averageSurface }.calculateStatsAverageInt()

    @VisibleForTesting
    fun calculateAverageDuration() = brushingSessions.map { it.duration }.calculateStatsAverageInt()

    private fun calculateCorrectOrientationAverage() =
        brushingSessions.calculateCorrectOrientationAverage()

    private fun calculateOverSpeedAverage() =
        brushingSessions.calculateOverSpeedAverage()

    private fun calculateCorrectSpeedAverage() =
        brushingSessions.calculateCorrectSpeedAverage()

    private fun calculateUnderSpeedAverage() =
        brushingSessions.calculateUnderSpeedAverage()

    private fun calculateCorrectMovementAverage() =
        brushingSessions.calculateCorrectMovementAverage()

    private fun calculateOverPressureAverage() =
        brushingSessions.calculateOverPressureAverage()

    private fun calculateAverageCheckupValues(): AverageCheckup {
        return brushingSessions
            .asSequence()
            .map { it.averageCheckup }
            .calculateAverageCheckup()
    }

    @VisibleForTesting
    fun calculateIsPerfectDay(): Boolean {
        return brushingSessions
            .count { session ->
                session.averageSurface >= MINIMAL_AVERAGE_SURFACE_PER_BRUSHING_FOR_PERFECTION
            } >= MINIMAL_NUMBER_OF_BRUSHINGS_FOR_PERFECTION
    }

    companion object {
        fun empty(profileId: Long, day: LocalDate): DayWithSessions {
            val dayAggregatedStatsEntity = createBrushingDayStat(profileId, day)

            return DayWithSessions(dayStats = dayAggregatedStatsEntity, brushingSessions = listOf())
        }
    }
}

/**
 * @return a Map<[LocalDate], [DayWithSessions]> where [newSessions] have been added to the previous sessions associated
 * to [date]
 * @throws IllegalArgumentException if there's no key equals to [date]
 */
internal fun Map<LocalDate, DayWithSessions>.withRecalculatedAverageFromSessions(
    date: LocalDate,
    newSessions: List<BrushingSessionStatsEntity>
): Map<LocalDate, DayWithSessions> {
    val dayStat = getDate(date)

    val newDayStats = toMutableMap()
    newDayStats[date] = dayStat.withReculatedAverageFromSessions(newSessions)

    return newDayStats.toMap()
}

/**
 * @return the average value of the elements in the list, including zero values. Rounded using
 * [roundOneDecimal]. Returns 0 if the average result is NaN
 */
private fun List<Int>.calculateStatsAverageInt() = average().zeroIfNan().roundOneDecimal()
