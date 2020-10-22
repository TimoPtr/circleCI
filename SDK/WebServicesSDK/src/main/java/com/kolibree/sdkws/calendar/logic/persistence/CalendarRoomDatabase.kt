/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.calendar.logic.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kolibree.android.calendar.logic.persistence.CalendarRoomDatabase.Companion.DATABASE_VERSION
import com.kolibree.android.calendar.logic.persistence.model.BrushingStreakEntity

@Database(
    entities = [
        BrushingStreakEntity::class
    ],
    version = DATABASE_VERSION
)
internal abstract class CalendarRoomDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "calendar.db"
        const val DATABASE_VERSION = 1
    }

    abstract fun brushingStreaksDao(): BrushingStreaksDao
}
