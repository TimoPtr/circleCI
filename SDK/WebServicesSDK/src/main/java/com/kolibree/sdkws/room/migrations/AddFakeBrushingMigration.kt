/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.sdkws.brushing.persistence.models.TABLE_NAME

internal const val FAKE_BRUSHING_START_VERSION = 21
internal const val FAKE_BRUSHING_END_VERSION = 22

internal object AddFakeBrushingMigration :
    Migration(FAKE_BRUSHING_START_VERSION, FAKE_BRUSHING_END_VERSION) {

    override fun migrate(database: SupportSQLiteDatabase) {
        val addFakeBrushingColumnQuery =
            "ALTER TABLE $TABLE_NAME ADD COLUMN `is_fake_brushing` INTEGER NOT NULL DEFAULT 0"

        database.execSQL(addFakeBrushingColumnQuery)
    }
}
