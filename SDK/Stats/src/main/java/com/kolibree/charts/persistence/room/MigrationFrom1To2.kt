/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.persistence.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
object MigrationFrom1To2 : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS in_off_brushings_count (`profileId` INTEGER NOT NULL, 
|               `offlineBrushingCount` INTEGER NOT NULL, `onlineBrushingCount` INTEGER NOT NULL,
|                PRIMARY KEY(`profileId`))""".trimMargin()
        )
    }
}
