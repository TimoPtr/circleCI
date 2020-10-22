/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence.models

import androidx.room.Entity
import androidx.room.Ignore
import com.kolibree.statsoffline.models.AverageCheckup
import com.kolibree.statsoffline.models.WeekAggregatedStats
import com.kolibree.statsoffline.models.YearWeek
import com.kolibree.statsoffline.models.emptyAverageCheckup
import com.kolibree.statsoffline.toYearWeek
import org.threeten.bp.LocalDate

@Entity(
    tableName = "brushing_week_stat",
    primaryKeys = ["profileId", "week"]
)
internal data class WeekAggregatedStatsEntity(
    override val profileId: Long,
    override val week: YearWeek,
    override val averageDuration: Double = 0.0,
    override val averageSurface: Double = 0.0,
    override val averageCheckup: AverageCheckup = emptyAverageCheckup(),
    override val totalSessions: Int = 0,
    override val sessionsPerDay: Double = 0.0,
    override val correctMovementAverage: Double = 0.0,
    override val underSpeedAverage: Double = 0.0,
    override val correctSpeedAverage: Double = 0.0,
    override val overSpeedAverage: Double = 0.0,
    override val correctOrientationAverage: Double = 0.0,
    override val overPressureAverage: Double = 0.0
) : WeekAggregatedStats {

    fun createEmptyDayStats(): List<DayAggregatedStatsEntity> =
        week.dates().createEmptyDayStats(profileId)

    @Ignore
    val dates = week.dates()
}

internal fun createWeekAggregatedStatsEntity(profileId: Long, day: LocalDate) =
    WeekAggregatedStatsEntity(
        profileId = profileId,
        week = day.toYearWeek()
    )
