/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("MagicNumber")
internal object AddOverPressureKpiColumnMigration4To5 : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        addOverPressureColumnToBrushingSessionEntity(database)
        addOverPressureColumnToMonthAggregatedStatsEntity(database)
        addOverPressureColumnToWeekAggregatedStatsEntity(database)
        addOverPressureColumnToDayAggregatedStatsEntity(database)
    }

    private fun addOverPressureColumnToBrushingSessionEntity(database: SupportSQLiteDatabase) {
        addOverPressureColumnToTable(database, tableName = "brushing_session_stat")
    }

    private fun addOverPressureColumnToMonthAggregatedStatsEntity(database: SupportSQLiteDatabase) {
        addOverPressureColumnToTable(database, tableName = "brushing_month_stat")
    }

    private fun addOverPressureColumnToWeekAggregatedStatsEntity(database: SupportSQLiteDatabase) {
        addOverPressureColumnToTable(database, tableName = "brushing_week_stat")
    }

    private fun addOverPressureColumnToDayAggregatedStatsEntity(database: SupportSQLiteDatabase) {
        addOverPressureColumnToTable(database, tableName = "brushing_day_stat")
    }

    private fun addOverPressureColumnToTable(
        database: SupportSQLiteDatabase,
        tableName: String
    ) {

        val addOverPressureAverageColumn =
            "ALTER TABLE $tableName ADD COLUMN overPressureAverage REAL NOT NULL DEFAULT 0"

        database.execSQL(addOverPressureAverageColumn)
    }
}
