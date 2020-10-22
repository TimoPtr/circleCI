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
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.room.longValueForColumn
import com.kolibree.sdkws.brushing.persistence.models.TABLE_NAME
import java.util.UUID

internal const val START_VERSION = 20
internal const val END_VERSION = 21

@Suppress("MagicNumber")
internal object V2BrushingsMigration : Migration(START_VERSION, END_VERSION) {

    override fun migrate(database: SupportSQLiteDatabase) {
        val addIdempotencyColumnQuery =
            "ALTER TABLE $TABLE_NAME ADD COLUMN `idempotency_key` TEXT NOT NULL DEFAULT ''"

        database.execSQL(addIdempotencyColumnQuery)

        setUuidOnOldRows(database)
    }

    private fun setUuidOnOldRows(database: SupportSQLiteDatabase) {
        database.query("SELECT * FROM $TABLE_NAME").use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    cursor.longValueForColumn("id")?.let { id ->
                        database.execSQL("UPDATE $TABLE_NAME SET `idempotency_key` = '${UUID.randomUUID()}' WHERE id = $id")
                    } ?: FailEarly.fail("Id was null. Unexpected")
                } while (cursor.moveToNext())
            }
        }
    }
}
