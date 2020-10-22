/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.kolibree.android.shop.data.ShopRoomDatabase.Companion.DATABASE_VERSION
import com.kolibree.android.shop.data.persitence.CartDao
import com.kolibree.android.shop.data.persitence.model.CartEntryEntity

@Database(
    entities = [CartEntryEntity::class],
    version = DATABASE_VERSION
)
internal abstract class ShopRoomDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "shop.db"
        const val DATABASE_VERSION = 1

        val migrations = arrayOf<Migration>()
    }

    abstract fun cartDao(): CartDao
}
