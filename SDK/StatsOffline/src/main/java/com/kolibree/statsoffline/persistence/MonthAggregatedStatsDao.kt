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
import com.kolibree.statsoffline.persistence.models.MonthAggregatedStatsEntity
import io.reactivex.Flowable
import org.threeten.bp.YearMonth

@Dao
internal interface MonthAggregatedStatsDao {
    /**
     * Used to detect when the Months table has changed for the given [profileId] and [months]
     */
    @Query("SELECT COUNT(*) FROM brushing_month_stat WHERE profileId = :profileId AND month IN (:months)")
    @TypeConverters(YearMonthConverters::class)
    fun monthsUpdated(profileId: Long, months: List<YearMonth>): Flowable<Int>

    @Query("SELECT * FROM brushing_month_stat WHERE profileId = :profileId AND month IN (:months)")
    @TypeConverters(YearMonthConverters::class)
    fun readByMonth(profileId: Long, months: List<YearMonth>): List<MonthAggregatedStatsEntity>

    @Query("SELECT * FROM brushing_month_stat WHERE profileId = :profileId AND month = :month")
    @TypeConverters(YearMonthConverters::class)
    fun readByMonth(profileId: Long, month: YearMonth): MonthAggregatedStatsEntity?

    @Update(onConflict = REPLACE)
    fun update(monthStats: List<MonthAggregatedStatsEntity>)

    @Query("SELECT * FROM brushing_month_stat")
    @VisibleForTesting
    fun readAll(): List<MonthAggregatedStatsEntity>

    @Insert(onConflict = REPLACE)
    fun insert(monthStats: MonthAggregatedStatsEntity)

    @Insert(onConflict = REPLACE)
    fun insert(monthStats: List<MonthAggregatedStatsEntity>)

    @Query("DELETE FROM brushing_month_stat")
    fun truncate()
}
