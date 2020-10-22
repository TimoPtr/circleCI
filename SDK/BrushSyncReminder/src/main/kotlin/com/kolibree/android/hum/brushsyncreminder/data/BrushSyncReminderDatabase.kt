/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.hum.brushsyncreminder.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.brushsyncreminder.data.BrushSyncReminderDao
import com.kolibree.android.brushsyncreminder.data.BrushSyncReminderEntity
import com.kolibree.android.hum.brushsyncreminder.data.BrushSyncReminderDatabase.Companion.DATABASE_VERSION

@Database(entities = [BrushSyncReminderEntity::class], version = DATABASE_VERSION)
@VisibleForApp
abstract class BrushSyncReminderDatabase : RoomDatabase() {

    internal companion object {
        const val DATABASE_NAME = "kolibree-brush-sync-reminder.db"
        const val DATABASE_VERSION = 1
        val migrations = emptyArray<Migration>()
    }

    internal abstract fun brushSyncReminderDao(): BrushSyncReminderDao
}
