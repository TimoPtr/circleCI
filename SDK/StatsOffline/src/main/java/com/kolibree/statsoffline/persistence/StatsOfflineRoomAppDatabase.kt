/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.statsoffline.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kolibree.android.room.DateConvertersLong
import com.kolibree.statsoffline.persistence.StatsOfflineRoomAppDatabase.Companion.DATABASE_VERSION
import com.kolibree.statsoffline.persistence.migrations.AddOverPressureKpiColumnMigration4To5
import com.kolibree.statsoffline.persistence.migrations.AddSpeedAndAngleKpiColumnsMigration
import com.kolibree.statsoffline.persistence.migrations.RemoveBrushingSessionGeneratedKeyMigration
import com.kolibree.statsoffline.persistence.models.BrushingSessionStatsEntity
import com.kolibree.statsoffline.persistence.models.DayAggregatedStatsEntity
import com.kolibree.statsoffline.persistence.models.MonthAggregatedStatsEntity
import com.kolibree.statsoffline.persistence.models.WeekAggregatedStatsEntity

/**
 * Created by guillaumeagis on 21/05/2018.
 * Creation of the Room Database
 */

@Database(
    entities = [
        BrushingSessionStatsEntity::class,
        DayAggregatedStatsEntity::class,
        MonthAggregatedStatsEntity::class,
        WeekAggregatedStatsEntity::class
    ],
    version = DATABASE_VERSION
)
@TypeConverters(
    DateConvertersLong::class,
    MouthZoneCheckupConverter::class,
    YearMonthConverters::class,
    YearWeekConverters::class,
    PlaqueAggregateConverter::class
)
internal abstract class StatsOfflineRoomAppDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "stats-offline.db"
        const val DATABASE_VERSION = 5

        val migrations = arrayOf(
            RemoveBrushingSessionGeneratedKeyMigration,
            AddSpeedAndAngleKpiColumnsMigration,
            AddOverPressureKpiColumnMigration4To5
        )
    }

    abstract fun sessionStatDao(): BrushingSessionStatDao
    abstract fun dayStatDao(): DayAggregatedStatsDao
    abstract fun weekStatDao(): WeekAggregatedStatsDao
    abstract fun monthStatDao(): MonthAggregatedStatsDao
    abstract fun statsOfflineDao(): StatsOfflineDao
}
