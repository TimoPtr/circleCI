/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence.migrations

import android.database.sqlite.SQLiteException
import androidx.annotation.VisibleForTesting
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

@Suppress("MagicNumber")
internal object RemoveBrushingSessionGeneratedKeyMigration : Migration(1, 3) {

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            removeBrushingSessionGeneratedKey(database)
        } catch (e: SQLiteException) {
            Timber.w(e, "Simple migration failed, fallback to full DB wipe")
            fallbackToFullDatabaseWipe(database)
        }
    }

    @VisibleForTesting
    internal fun removeBrushingSessionGeneratedKey(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `brushing_session_stat_new` (" +
                "`profileId` INTEGER NOT NULL, " +
                "`creationTime` INTEGER NOT NULL, " +
                "`duration` INTEGER NOT NULL," +
                "`averageSurface` INTEGER NOT NULL, " +
                "`_averageCheckupMap` TEXT NOT NULL, " +
                "`assignedDate` INTEGER NOT NULL, " +
                "`cleanPercent` INTEGER, " +
                "`missedPercent` INTEGER, " +
                "`plaqueLeftPercent` INTEGER, " +
                "`plaqueAggregate` TEXT, " +
                "PRIMARY KEY(`profileId`, `creationTime`), " +
                "FOREIGN KEY(`profileId`, `assignedDate`) " +
                "REFERENCES `brushing_day_stat`(`profileId`, `day`)" +
                "ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED" +
                ")"
        )

        database.execSQL(
            "INSERT INTO brushing_session_stat_new (" +
                "profileId, " +
                "creationTime, " +
                "duration, " +
                "averageSurface, " +
                "_averageCheckupMap, " +
                "assignedDate, " +
                "cleanPercent, " +
                "missedPercent, " +
                "plaqueLeftPercent, " +
                "plaqueAggregate" +
                ") " +
                "SELECT DISTINCT " +
                "profileId, " +
                "creationTime, " +
                "duration, " +
                "averageSurface, " +
                "_averageCheckupMap, " +
                "assignedDate, " +
                "cleanPercent, " +
                "missedPercent, " +
                "plaqueLeftPercent, " +
                "plaqueAggregate " +
                "FROM brushing_session_stat " +
                "GROUP BY profileId, creationTime"
        )

        database.execSQL("DROP TABLE brushing_session_stat")

        database.execSQL("ALTER TABLE brushing_session_stat_new RENAME TO brushing_session_stat")

        database.execSQL(
            "CREATE INDEX `index_brushing_session_stat_profileId_assignedDate` " +
                "ON `brushing_session_stat` (`profileId`, `assignedDate`)"
        )
    }

    private fun fallbackToFullDatabaseWipe(database: SupportSQLiteDatabase) {
        dropAllTables(database)
        recreateAllTables(database)
    }

    private fun dropAllTables(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS brushing_session_stat_new")

        database.execSQL("DROP TABLE IF EXISTS brushing_session_stat")

        database.execSQL("DROP TABLE IF EXISTS brushing_day_stat")

        database.execSQL("DROP TABLE IF EXISTS brushing_month_stat")

        database.execSQL("DROP TABLE IF EXISTS brushing_week_stat")
    }

    private fun recreateAllTables(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE `brushing_session_stat` (" +
                "`profileId` INTEGER NOT NULL, " +
                "`creationTime` INTEGER NOT NULL, " +
                "`duration` INTEGER NOT NULL," +
                "`averageSurface` INTEGER NOT NULL, " +
                "`_averageCheckupMap` TEXT NOT NULL, " +
                "`assignedDate` INTEGER NOT NULL, " +
                "`cleanPercent` INTEGER, " +
                "`missedPercent` INTEGER, " +
                "`plaqueLeftPercent` INTEGER, " +
                "`plaqueAggregate` TEXT, " +
                "PRIMARY KEY(`profileId`, `creationTime`), " +
                "FOREIGN KEY(`profileId`, `assignedDate`) " +
                "REFERENCES `brushing_day_stat`(`profileId`, `day`) " +
                "ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED" +
                ")"
        )

        database.execSQL(
            "CREATE INDEX `index_brushing_session_stat_profileId_assignedDate` " +
                "ON `brushing_session_stat` (`profileId`, `assignedDate`)"
        )

        database.execSQL(
            "CREATE TABLE `brushing_day_stat` (" +
                "`profileId` INTEGER NOT NULL, " +
                "`day` INTEGER NOT NULL, " +
                "`averageDuration` REAL NOT NULL, " +
                "`averageSurface` REAL NOT NULL, " +
                "`averageCheckup` TEXT NOT NULL, " +
                "`isPerfectDay` INTEGER NOT NULL, " +
                "`totalSessions` INTEGER NOT NULL, " +
                "`month` INTEGER NOT NULL, " +
                "`week` TEXT NOT NULL, " +
                "PRIMARY KEY(`profileId`, `day`), " +
                "FOREIGN KEY(`profileId`, `month`) " +
                "REFERENCES `brushing_month_stat`(`profileId`, `month`) " +
                "ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED, " +
                "FOREIGN KEY(`profileId`, `week`) " +
                "REFERENCES `brushing_week_stat`(`profileId`, `week`) " +
                "ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED" +
                ")"
        )

        database.execSQL(
            "CREATE INDEX `index_brushing_day_stat_profileId_month` " +
                "ON `brushing_day_stat` (`profileId`, `month`)"
        )

        database.execSQL(
            "CREATE INDEX `index_brushing_day_stat_profileId_week` " +
                "ON `brushing_day_stat` (`profileId`, `week`)"
        )

        database.execSQL(
            "CREATE TABLE `brushing_month_stat` (" +
                "`profileId` INTEGER NOT NULL, " +
                "`month` INTEGER NOT NULL, " +
                "`averageDuration` REAL NOT NULL, " +
                "`averageSurface` REAL NOT NULL, " +
                "`averageCheckup` TEXT NOT NULL, " +
                "`totalSessions` INTEGER NOT NULL, " +
                "`sessionsPerDay` REAL NOT NULL, " +
                "PRIMARY KEY(`profileId`, `month`)" +
                ")"
        )

        database.execSQL(
            "CREATE TABLE `brushing_week_stat` (" +
                "`profileId` INTEGER NOT NULL, " +
                "`week` TEXT NOT NULL, " +
                "`averageDuration` REAL NOT NULL, " +
                "`averageSurface` REAL NOT NULL, " +
                "`averageCheckup` TEXT NOT NULL, " +
                "`totalSessions` INTEGER NOT NULL, " +
                "`sessionsPerDay` REAL NOT NULL, " +
                "PRIMARY KEY(`profileId`, `week`)" +
                ")"
        )
    }
}
