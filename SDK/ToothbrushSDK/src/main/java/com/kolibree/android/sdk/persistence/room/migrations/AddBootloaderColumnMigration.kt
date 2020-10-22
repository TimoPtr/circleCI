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
internal object AddBootloaderColumnMigration : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        val addBootloaderColumnQuery =
            "ALTER TABLE ${AccountToothbrush.TABLE_NAME} ADD COLUMN `bootloader_version` INTEGER NOT NULL DEFAULT 0"

        database.execSQL(addBootloaderColumnQuery)
    }
}
