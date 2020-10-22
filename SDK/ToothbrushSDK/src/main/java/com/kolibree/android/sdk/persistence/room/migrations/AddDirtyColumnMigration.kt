/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.persistence.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.android.sdk.persistence.model.AccountToothbrush

@Suppress("MagicNumber")
internal object AddDirtyColumnMigration : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        val addDirtyColumnQuery =
            "ALTER TABLE ${AccountToothbrush.TABLE_NAME} ADD COLUMN `dirty` INTEGER NOT NULL DEFAULT 0"

        database.execSQL(addDirtyColumnQuery)
    }
}
