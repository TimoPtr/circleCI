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
import com.kolibree.statsoffline.models.MonthAggregatedStats
import com.kolibree.statsoffline.models.emptyAverageCheckup
import com.kolibree.statsoffline.toYearWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

@Entity(
    tableName = "brushing_month_stat",
    primaryKeys = ["profileId", "month"]
)
internal data class MonthAggregatedStatsEntity constructor(
    override val profileId: Long,
    override val month: YearMonth,
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
) : MonthAggregatedStats {
    @Ignore
    val dates: List<LocalDate> =
        month.lengthOfMonth().downTo(1).map { dayNumber -> month.atDay(dayNumber) }

    fun createEmptyDayStats(): List<DayAggregatedStatsEntity> = dates.createEmptyDayStats(profileId)

    fun createEmptyWeekStats(): List<WeekAggregatedStatsEntity> =
        dates
            .distinctBy { it.toYearWeek() }
            .map { localDate ->
            createWeekAggregatedStatsEntity(
                profileId,
                localDate
            )
        }
}
