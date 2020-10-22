/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.persistence.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Recreate personal challenge table
@Suppress("MagicNumber")
internal object V3RecreatePersonalChallengeTableMigration : Migration(2, 3) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE `personal_challenges`")
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `personal_challenges` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`backendId` INTEGER, " +
                "`profileId` INTEGER NOT NULL, " +
                "`objectiveType` TEXT NOT NULL, " +
                "`difficultyLevel` TEXT NOT NULL, " +
                "`duration` INTEGER NOT NULL, " +
                "`durationUnit` TEXT NOT NULL, " +
                "`creationDate` TEXT NOT NULL, " +
                "`updateDate` TEXT NOT NULL, " +
                "`completionDate` TEXT, " +
                "`progress` INTEGER NOT NULL, " +
                "`uploadStatus` TEXT NOT NULL, " +
                "`isDeletedLocally` INTEGER NOT NULL" +
                ")"
        )
    }
}
