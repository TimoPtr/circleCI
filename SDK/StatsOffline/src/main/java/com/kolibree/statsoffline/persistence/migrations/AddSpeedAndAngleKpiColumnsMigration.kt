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
internal object AddSpeedAndAngleKpiColumnsMigration : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        addStatsAndAngleColumnsToBrushingSessionEntity(database)
        addStatsAndAngleColumnsToMonthAggregatedStatsEntity(database)
        addStatsAndAngleColumnsToWeekAggregatedStatsEntity(database)
        addStatsAndAngleColumnsToDayAggregatedStatsEntity(database)
    }

    private fun addStatsAndAngleColumnsToBrushingSessionEntity(database: SupportSQLiteDatabase) {
        addStatsAndAngleColumnsToTable(database, tableName = "brushing_session_stat")
    }

    private fun addStatsAndAngleColumnsToMonthAggregatedStatsEntity(database: SupportSQLiteDatabase) {
        addStatsAndAngleColumnsToTable(database, tableName = "brushing_month_stat")
    }

    private fun addStatsAndAngleColumnsToWeekAggregatedStatsEntity(database: SupportSQLiteDatabase) {
        addStatsAndAngleColumnsToTable(database, tableName = "brushing_week_stat")
    }

    private fun addStatsAndAngleColumnsToDayAggregatedStatsEntity(database: SupportSQLiteDatabase) {
        addStatsAndAngleColumnsToTable(database, tableName = "brushing_day_stat")
    }

    private fun addStatsAndAngleColumnsToTable(
        database: SupportSQLiteDatabase,
        tableName: String
    ) {

        val addCorrectMovementAverageSessions =
            "ALTER TABLE $tableName ADD COLUMN correctMovementAverage REAL NOT NULL DEFAULT 0"
        val addUnderSpeedAverageSessions =
            "ALTER TABLE $tableName ADD COLUMN underSpeedAverage REAL NOT NULL DEFAULT 0"
        val addCorrectSpeedAverageSessions =
            "ALTER TABLE $tableName ADD COLUMN correctSpeedAverage REAL NOT NULL DEFAULT 0"
        val addOverSpeedAverageSessions =
            "ALTER TABLE $tableName ADD COLUMN overSpeedAverage REAL NOT NULL DEFAULT 0"
        val addCorrectOrientationAverageSessions =
            "ALTER TABLE $tableName ADD COLUMN correctOrientationAverage REAL NOT NULL DEFAULT 0"

        database.execSQL(addCorrectMovementAverageSessions)

        database.execSQL(addUnderSpeedAverageSessions)

        database.execSQL(addCorrectSpeedAverageSessions)

        database.execSQL(addOverSpeedAverageSessions)

        database.execSQL(addCorrectOrientationAverageSessions)
    }
}
