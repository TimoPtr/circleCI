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

internal class AddTotalSessionsAndSessionsPerDayMigration1To2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        addTotalSessionsToDay(database)

        addTotalSessionsToMonth(database)

        addTotalSessionsToWeek(database)

        // totalSessions and sessionsPerDay will be updated on the next brushing update/remove
    }

    private fun addTotalSessionsToMonth(database: SupportSQLiteDatabase) {
        val addTotalSessions =
            "ALTER TABLE brushing_month_stat ADD COLUMN totalSessions INTEGER NOT NULL DEFAULT 0 "
        val addSessionsPerDay =
            "ALTER TABLE brushing_month_stat ADD COLUMN sessionsPerDay REAL NOT NULL DEFAULT 0"
        database.execSQL(addTotalSessions)
        database.execSQL(addSessionsPerDay)
    }

    private fun addTotalSessionsToWeek(database: SupportSQLiteDatabase) {
        val addTotalSessions =
            "ALTER TABLE brushing_week_stat ADD COLUMN totalSessions INTEGER NOT NULL DEFAULT 0"
        val addSessionsPerDay =
            "ALTER TABLE brushing_week_stat ADD COLUMN sessionsPerDay REAL NOT NULL DEFAULT 0"
        database.execSQL(addTotalSessions)
        database.execSQL(addSessionsPerDay)
    }

    private fun addTotalSessionsToDay(database: SupportSQLiteDatabase) {
        val addTotalSessions =
            "ALTER TABLE brushing_day_stat ADD COLUMN totalSessions INTEGER NOT NULL DEFAULT 0"
        database.execSQL(addTotalSessions)
    }
}
