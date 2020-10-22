/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.kolibree.statsoffline.models.YearWeek
import com.kolibree.statsoffline.persistence.models.DayAggregatedStatsEntity
import org.threeten.bp.LocalDate

@Dao
internal interface DayAggregatedStatsDao {
    @Query("SELECT * FROM brushing_day_stat WHERE profileId = :profileId AND day IN (:date)")
    fun readByDate(profileId: Long, date: List<LocalDate>): List<DayAggregatedStatsEntity>

    @Query("SELECT * FROM brushing_day_stat WHERE profileId = :profileId AND week IN (:weeks)")
    fun readByWeeks(profileId: Long, weeks: List<YearWeek>): List<DayAggregatedStatsEntity>

    @Update
    fun update(dayStats: List<DayAggregatedStatsEntity>)

    @Query("SELECT * FROM brushing_day_stat")
    @VisibleForTesting
    fun readAll(): List<DayAggregatedStatsEntity>

    @Insert(onConflict = REPLACE)
    fun insert(dayStats: List<DayAggregatedStatsEntity>)

    @Query("DELETE FROM brushing_day_stat")
    fun truncate()
}
