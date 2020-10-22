/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.headspace.data.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.partnerships.headspace.data.persistence.HeadspaceDatabase.Companion.DATABASE_VERSION
import com.kolibree.android.partnerships.headspace.data.persistence.model.HeadspacePartnershipEntity

@Database(
    entities = [
        HeadspacePartnershipEntity::class
    ], version = DATABASE_VERSION
)
@VisibleForApp
abstract class HeadspaceDatabase : RoomDatabase() {

    internal companion object {
        const val DATABASE_NAME = "headspace-partnership.db"

        const val DATABASE_VERSION = 1

        val migrations = emptyArray<Migration>()
    }

    internal abstract fun headspacePartnershipDao(): HeadspacePartnershipDao
}
