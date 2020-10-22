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

// Add personal challenge table
internal object V2AddPersonalChallengeTableMigration : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        val request = "CREATE TABLE IF NOT EXISTS `personal_challenges` (" +
            "`id` INTEGER NOT NULL, " +
            "`profileId` INTEGER NOT NULL, " +
            "`objectiveType` TEXT NOT NULL, " +
            "`difficultyLevel` TEXT NOT NULL, " +
            "`duration` INTEGER NOT NULL, " +
            "`durationUnit` TEXT NOT NULL, " +
            "`creationDate` TEXT NOT NULL, " +
            "`completionDate` TEXT, " +
            "`progress` INTEGER NOT NULL, " +
            "PRIMARY KEY(`id`)" +
            ")"
        database.execSQL(request)
    }
}
