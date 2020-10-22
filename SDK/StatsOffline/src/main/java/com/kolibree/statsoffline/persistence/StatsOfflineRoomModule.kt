/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence

import android.content.Context
import androidx.room.Room
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.statsoffline.persistence.migrations.AddOverPressureKpiColumnMigration4To5
import com.kolibree.statsoffline.persistence.migrations.AddSpeedAndAngleKpiColumnsMigration
import com.kolibree.statsoffline.persistence.migrations.AddTotalSessionsAndSessionsPerDayMigration1To2
import com.kolibree.statsoffline.persistence.migrations.RemoveBrushingSessionGeneratedKeyMigration
import dagger.Module
import dagger.Provides

@Module
object StatsOfflineRoomModule {

    @Provides
    @AppScope
    internal fun providesStatsOfflineDatabase(context: Context): StatsOfflineRoomAppDatabase {
        return Room.databaseBuilder(
            context,
            StatsOfflineRoomAppDatabase::class.java,
            StatsOfflineRoomAppDatabase.DATABASE_NAME
        )
            // NOTE this is the only way to deal with corrupted data in Chinese SDK 4.8.x
            // TODO remove in SDK 4.11.0 or later
            .fallbackToDestructiveMigration()
            .addMigrations(
                AddTotalSessionsAndSessionsPerDayMigration1To2(),
                RemoveBrushingSessionGeneratedKeyMigration,
                AddSpeedAndAngleKpiColumnsMigration,
                AddOverPressureKpiColumnMigration4To5
            )
            .build()
    }

    @Provides
    internal fun providesBrushingSessionStatDao(database: StatsOfflineRoomAppDatabase): BrushingSessionStatDao {
        return database.sessionStatDao()
    }

    @Provides
    internal fun providesStatsOfflineDao(database: StatsOfflineRoomAppDatabase): StatsOfflineDao {
        val statsOfflineDao = database.statsOfflineDao()

        statsOfflineDao.sessionStatDao = database.sessionStatDao()
        statsOfflineDao.dayStatDao = database.dayStatDao()
        statsOfflineDao.weekStatDao = database.weekStatDao()
        statsOfflineDao.monthStatDao = database.monthStatDao()

        return statsOfflineDao
    }
}
