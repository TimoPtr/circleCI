/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline

import com.kolibree.statsoffline.models.DayWithSessions
import com.kolibree.statsoffline.persistence.models.BrushingSessionStatsEntity
import org.threeten.bp.LocalDate

internal fun Map<LocalDate, DayWithSessions>.calculateAverageForDaysNotInTheFuture(): Map<LocalDate, DayWithSessions> {
    return filterNotFuture().mapValues { it.value.calculateAverage() }
}

/**
 * Given a List<Map<[LocalDate], [DayWithSessions]>>, return a Map<[LocalDate], [DayWithSessions]> that contains all
 * keys in the original list, as well as every session in the associated DayWithSessions
 *
 * Example
 *
 * List contains
 * - mapOf(june1st -> (session1, session2), june2nd -> (session3))
 * - mapOf(june1st -> (session4), june15th -> (session5))
 *
 * The returned Map<[LocalDate], [DayWithSessions]> will contain
 *
 * mapOf(june1st -> (session1, session2, session4), june2nd -> (session3), june15th -> (session5)
 */
internal fun List<Map<LocalDate, DayWithSessions>>.sumsDaysWithSessions(): Map<LocalDate, DayWithSessions> {
    return asSequence()
        .flatMap { it.asSequence() }
        .groupBy({ it.key }, { it.value })
        .mapValues {
            val brushingSessions = it.value.toBrushingSessionStatsEntityList()
            val firstDay = it.value.first()
            firstDay.copy(
                brushingSessions = brushingSessions,
                dayStats = firstDay.dayStats.copy(totalSessions = brushingSessions.size)
            )
        }
}

/**
 * @return a List<[BrushingSessionStatsEntity]> containing all [BrushingSessionStatsEntity]
 */
internal fun List<DayWithSessions>.toBrushingSessionStatsEntityList(): List<BrushingSessionStatsEntity> {
    return map { dayWithSessions -> dayWithSessions.brushingSessions }.flatten()
}
