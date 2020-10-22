/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// add short_task table
internal object MigrationFrom1To2 : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS `short_tasks` (`profileId` INTEGER NOT NULL, `shortTask` 
                TEXT NOT NULL, `creationTimestamp` INTEGER NOT NULL, `creationZoneOffset` TEXT NOT NULL, 
                `uuid` TEXT, PRIMARY KEY(`profileId`, `creationTimestamp`))""".trimMargin()
        )
    }
}
