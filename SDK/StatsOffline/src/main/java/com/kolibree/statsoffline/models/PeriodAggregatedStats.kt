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
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.statsoffline.calculateAverageCheckup
import com.kolibree.statsoffline.calculateAverageDuration
import com.kolibree.statsoffline.calculateAverageSurface
import com.kolibree.statsoffline.calculateCorrectMovementAverage
import com.kolibree.statsoffline.calculateCorrectOrientationAverage
import com.kolibree.statsoffline.calculateCorrectSpeedAverage
import com.kolibree.statsoffline.calculateOverPressureAverage
import com.kolibree.statsoffline.calculateOverSpeedAverage
import com.kolibree.statsoffline.calculateUnderSpeedAverage
import com.kolibree.statsoffline.dateRangeBetween
import com.kolibree.statsoffline.roundOneDecimal
import org.threeten.bp.LocalDate

/**
 * Aggregated data for [IBrushing] in [dateRange], both ends of the range included.
 */
@Keep
data class PeriodAggregatedStats @VisibleForTesting internal constructor(
    override val profileId: Long,
    private val dateRange: SanitizedDateRange,
    /**
     * Contains a [DayAggregatedStats] for each of the [LocalDate] in [dateRange]
     */
    val dayAggregatedStats: Set<DayAggregatedStatsWithSessions>,
    override val averageDuration: Double,
    override val averageSurface: Double,
    override val averageCheckup: AverageCheckup,
    override val correctMovementAverage: Double = 0.0,
    override val underSpeedAverage: Double = 0.0,
    override val correctSpeedAverage: Double = 0.0,
    override val overSpeedAverage: Double = 0.0,
    override val correctOrientationAverage: Double = 0.0,
    override val overPressureAverage: Double = 0.0
) : MultiDayAggregatedStats, DateRange by dateRange {
    override val totalSessions: Int = dayAggregatedStats.map { it.sessions }.flatten().size

    override val sessionsPerDay: Double =
        (totalSessions.toDouble() / dateRange.datesInRange().size).roundOneDecimal()

    init {
        validateDayAggregatedStatsDates()
    }

    /**
     * Validates that [dayAggregatedStats] contains the exact same dates as [dateRange], both ends included
     *
     * @throws IllegalArgumentException if [dayAggregatedStats] does not contain a [DayWithSessions]
     * for every [LocalDate] in the [dateRange]
     */
    private fun validateDayAggregatedStatsDates() {
        val datesInRange = dateRangeBetween(startDate, endDate)

        if (datesInRange.size != dayAggregatedStats.size) {
            throw IllegalArgumentException(
                "Missing or Too Many DayAggregatedStats. " +
                    "Expected ${datesInRange.size}, got ${dayAggregatedStats.size}"
            )
        }

        if (!datesInRange.containsAll(dayAggregatedStats.map { it.day })) {
            throw IllegalArgumentException("Missing some LocalDate. Expected $datesInRange, got $dayAggregatedStats")
        }
    }

    private fun calculateAverage(): PeriodAggregatedStats {
        return copy(
            averageDuration = dayAggregatedStats.calculateAverageDuration(excludeZero = false),
            averageSurface = dayAggregatedStats.calculateAverageSurface(excludeZero = false),
            averageCheckup = dayAggregatedStats.calculateAverageCheckup(),
            correctMovementAverage = dayAggregatedStats.calculateCorrectMovementAverage(excludeZero = false),
            underSpeedAverage = dayAggregatedStats.calculateUnderSpeedAverage(excludeZero = false),
            correctSpeedAverage = dayAggregatedStats.calculateCorrectSpeedAverage(excludeZero = false),
            overSpeedAverage = dayAggregatedStats.calculateOverSpeedAverage(excludeZero = false),
            correctOrientationAverage = dayAggregatedStats.calculateCorrectOrientationAverage(
                excludeZero = false
            ),
            overPressureAverage = dayAggregatedStats.calculateOverPressureAverage(excludeZero = false)
        )
    }

    companion object {

        /**
         * Given a [dateRange] of N [LocalDate] and a List<[DayWithSessions]> with `[0, N]` [DayWithSessions], returns a
         * [PeriodAggregatedStats] with N [DayWithSessions], one for each [LocalDate] in [dateRange]
         *
         * If necessary, it creates empty [DayWithSessions].
         *
         * It assumes each [DayWithSessions] in the [List] refers to a unique date
         *
         * @return [PeriodAggregatedStats] with average data for the period
         */
        internal fun fromDaysWithSessions(
            profileId: Long,
            startDate: LocalDate,
            endDate: LocalDate,
            daysWithSessions: List<DayWithSessions>
        ): PeriodAggregatedStats {
            val dateRange = SanitizedDateRange(startDate, endDate)
            val dateRangeWithSessions =
                DateRangeWithSessions(profileId, dateRange, daysWithSessions)

            return PeriodAggregatedStats(
                profileId = profileId,
                dateRange = dateRange,
                dayAggregatedStats = dateRangeWithSessions.sanitizedDaysWithSessions(),
                averageSurface = 0.0,
                averageDuration = 0.0,
                averageCheckup = emptyAverageCheckup()
            ).calculateAverage()
        }
    }
}

private class DateRangeWithSessions(
    private val profileId: Long,
    private val dateRange: DateRange,
    private val daysWithSessions: List<DayWithSessions>
) : DateRange by dateRange {

    /**
     * Given a [dateRange] of N [LocalDate] and a List<[DayWithSessions]> with `[0, N]` [DayWithSessions], returns a
     * Set<[DayWithSessions]> with N [DayWithSessions], one for each [LocalDate] in [dateRange]
     *
     * If necessary, it creates empty [DayWithSessions].
     *
     * It assumes each [DayWithSessions] in the [List] refers to a unique date
     */
    fun sanitizedDaysWithSessions(): Set<DayWithSessions> {
        val daysWithSessionsMap = daysWithSessions
            .groupBy { it.day }
            .mapValues { it.value.single() }

        val sanitizedDaysWithSessions = mutableListOf<DayWithSessions>().apply {
            dateRange.datesInRange().forEach { day ->
                add(daysWithSessionsMap.getOrElse(day, { DayWithSessions.empty(profileId, day) }))
            }
        }

        return sanitizedDaysWithSessions.toSet()
    }
}
