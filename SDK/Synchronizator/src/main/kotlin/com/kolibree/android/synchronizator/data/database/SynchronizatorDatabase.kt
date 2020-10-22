/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration

@Database(
    entities = [
        SynchronizableTrackingEntity::class
    ],
    version = SynchronizatorDatabase.DATABASE_VERSION
)
@TypeConverters(
    UploadStatusConverters::class,
    UuidConverters::class,
    SynchronizableKeyConverter::class
)
internal abstract class SynchronizatorDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "synchronizator.db"

        val migrations = arrayOf<Migration>()
    }

    abstract fun synchronizatorEntityDao(): SynchronizableTrackingEntityDao
}
