/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kolibree.android.game.gameprogress.data.persistence.GameProgressDao
import com.kolibree.android.game.gameprogress.data.persistence.model.GameProgressEntity
import com.kolibree.android.game.migration.MigrationFrom1To2
import com.kolibree.android.game.persistence.GamesRoomDatabase.Companion.DATABASE_VERSION
import com.kolibree.android.game.shorttask.data.persistence.ShortTaskDao
import com.kolibree.android.game.shorttask.data.persistence.model.ShortTaskEntity

@Database(
    entities = [GameProgressEntity::class, ShortTaskEntity::class],
    version = DATABASE_VERSION
)
internal abstract class GamesRoomDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "game.db"
        const val DATABASE_VERSION = 2
        val migrations = arrayOf(MigrationFrom1To2)
    }

    abstract fun gameProgressDao(): GameProgressDao

    abstract fun shortTaskDao(): ShortTaskDao
}
