/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.kolibree.android.brushreminder.data.BrushReminderDatabase.Companion.DATABASE_VERSION

@Database(entities = [BrushReminderEntity::class], version = DATABASE_VERSION)
internal abstract class BrushReminderDatabase : RoomDatabase() {

    internal companion object {
        const val DATABASE_NAME = "kolibree-brush-reminder.db"
        const val DATABASE_VERSION = 1
        val migrations = emptyArray<Migration>()
    }

    abstract fun brushSyncReminderDao(): BrushReminderDao
}
