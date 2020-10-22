/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline

import com.kolibree.android.clock.TrustedClock
import com.kolibree.statsoffline.models.AggregatedStats
import com.kolibree.statsoffline.models.AverageCheckup
import com.kolibree.statsoffline.models.DayWithSessions
import com.kolibree.statsoffline.models.calculateAverageCheckup
import com.kolibree.statsoffline.persistence.models.BrushingSessionStatsEntity
import org.threeten.bp.LocalDate

internal fun Map<LocalDate, AggregatedStats>.calculateAverageDuration(): Double =
    values.calculateAverageDuration()

internal fun Map<LocalDate, AggregatedStats>.calculateAverageSurface(): Double =
    values.calculateAverageSurface()

internal fun Map<LocalDate, AggregatedStats>.calculateAverageCheckup(): AverageCheckup =
    values.calculateAverageCheckup()

internal fun Collection<AggregatedStats>.calculateAverageDuration(excludeZero: Boolean = true): Double =
    map { it.averageDuration }.calculateStatsAverage(excludeZero)

internal fun Collection<AggregatedStats>.calculateAverageSurface(excludeZero: Boolean = true): Double =
    map { it.averageSurface }.calculateStatsAverage(excludeZero)

internal fun Collection<AggregatedStats>.calculateAverageCheckup(): AverageCheckup =
    asSequence()
        .map { it.averageCheckup }
        .calculateAverageCheckup()

internal fun Map<LocalDate, AggregatedStats>.calculateSessionsPerDay(): Double {
    return if (size == 0) 0.0 else (totalSessions().toDouble() / size).roundOneDecimal()
}

/**
 * @return the number of [BrushingSessionStatsEntity] in the map
 */
internal fun Map<LocalDate, AggregatedStats>.totalSessions(): Int {
    return values.sumBy { it.totalSessions }
}

/**
 * @return the [DayWithSessions] associated to [date]
 * @throws IllegalArgumentException if there's no key equals to [date]
 */
@Throws(IllegalArgumentException::class)
internal fun <T : AggregatedStats> Map<LocalDate, T>.getDate(date: LocalDate): T {
    return get(date) ?: throw IllegalArgumentException("$date does not belong in $keys")
}

internal fun <T : AggregatedStats> Map<LocalDate, T>.filterNotFuture(): Map<LocalDate, T> {
    val today = TrustedClock.getNowLocalDate()

    return filterKeys { !it.isAfter(today) }
}
