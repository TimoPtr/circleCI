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
import androidx.room.TypeConverters
import androidx.room.Update
import com.kolibree.statsoffline.models.YearWeek
import com.kolibree.statsoffline.persistence.models.WeekAggregatedStatsEntity
import io.reactivex.Flowable

@Dao
internal interface WeekAggregatedStatsDao {
    /**
     * Used to detect when the Weeks table has changed for the given [profileId] and [weeks]
     */
    @Query("SELECT COUNT(*) FROM brushing_week_stat WHERE profileId = :profileId AND week IN (:weeks)")
    @TypeConverters(YearWeekConverters::class)
    fun weeksUpdated(profileId: Long, weeks: List<YearWeek>): Flowable<Int>

    @Query("SELECT * FROM brushing_week_stat WHERE profileId = :profileId AND week IN (:weeks)")
    @TypeConverters(YearWeekConverters::class)
    fun readByWeek(profileId: Long, weeks: List<YearWeek>): List<WeekAggregatedStatsEntity>

    @Query("SELECT * FROM brushing_week_stat WHERE profileId = :profileId AND week = :week")
    @TypeConverters(YearWeekConverters::class)
    fun readByWeek(profileId: Long, week: YearWeek): WeekAggregatedStatsEntity?

    @Update(onConflict = REPLACE)
    fun update(weekStats: List<WeekAggregatedStatsEntity>)

    @Query("SELECT * FROM brushing_week_stat")
    @VisibleForTesting
    fun readAll(): List<WeekAggregatedStatsEntity>

    @Insert(onConflict = REPLACE)
    fun insert(weekStats: WeekAggregatedStatsEntity)

    @Insert(onConflict = REPLACE)
    fun insert(weekStats: List<WeekAggregatedStatsEntity>)

    @Query("DELETE FROM brushing_week_stat")
    fun truncate()
}
