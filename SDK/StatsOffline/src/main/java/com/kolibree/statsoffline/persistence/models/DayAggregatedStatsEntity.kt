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
import androidx.room.ForeignKey
import androidx.room.Index
import com.kolibree.statsoffline.models.AverageCheckup
import com.kolibree.statsoffline.models.DayAggregatedStats
import com.kolibree.statsoffline.models.YearWeek
import com.kolibree.statsoffline.models.emptyAverageCheckup
import com.kolibree.statsoffline.models.validate
import com.kolibree.statsoffline.toYearWeek
import java.lang.IllegalArgumentException
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import timber.log.Timber

@Entity(
    tableName = "brushing_day_stat",
    foreignKeys = [
        ForeignKey(
            entity = MonthAggregatedStatsEntity::class,
            parentColumns = arrayOf("profileId", "month"),
            childColumns = arrayOf("profileId", "month"),
            onDelete = ForeignKey.NO_ACTION,
            deferred = true
        ),
        ForeignKey(
            entity = WeekAggregatedStatsEntity::class,
            parentColumns = arrayOf("profileId", "week"),
            childColumns = arrayOf("profileId", "week"),
            onDelete = ForeignKey.NO_ACTION,
            deferred = true
        )],
    primaryKeys = ["profileId", "day"],
    indices = [
        Index(value = ["profileId", "month"]),
        Index(value = ["profileId", "week"])
    ]
)
internal data class DayAggregatedStatsEntity(
    override val profileId: Long,
    override val day: LocalDate,
    override val averageDuration: Double = 0.0,
    override val averageSurface: Double = 0.0,
    override val averageCheckup: AverageCheckup = emptyAverageCheckup(),
    override val isPerfectDay: Boolean = false,
    override val totalSessions: Int = 0,
    val month: YearMonth,
    val week: YearWeek,
    override val correctMovementAverage: Double = 0.0,
    override val underSpeedAverage: Double = 0.0,
    override val correctSpeedAverage: Double = 0.0,
    override val overSpeedAverage: Double = 0.0,
    override val correctOrientationAverage: Double = 0.0,
    override val overPressureAverage: Double = 0.0
) : DayAggregatedStats {
    init {
        averageCheckup.validate()
    }
}

internal fun createBrushingDayStat(profileId: Long, day: LocalDate) =
    DayAggregatedStatsEntity(
        profileId = profileId,
        day = day,
        month = YearMonth.from(day),
        week = day.toYearWeek()
    )

internal fun List<LocalDate>.createEmptyDayStats(profileId: Long): List<DayAggregatedStatsEntity> =
    mapNotNull { localDate ->
        try {
            createBrushingDayStat(profileId = profileId, day = localDate)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
            null
        }
    }
