/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kolibree.android.accountinternal.AccountRoomDatabase.Companion.DATABASE_VERSION
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.dao.AccountDao
import com.kolibree.android.accountinternal.persistence.migration.MigrationFrom1To2
import com.kolibree.android.accountinternal.persistence.migration.MigrationFrom2To3
import com.kolibree.android.accountinternal.persistence.migration.MigrationFrom3To4
import com.kolibree.android.accountinternal.persistence.migration.MigrationFrom4To5
import com.kolibree.android.accountinternal.persistence.migration.MigrationFrom5To6
import com.kolibree.android.accountinternal.persistence.migration.MigrationFrom6To7
import com.kolibree.android.accountinternal.profile.persistence.dao.ProfileDao
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal

@Database(
    entities = [
        ProfileInternal::class,
        AccountInternal::class
    ],
    version = DATABASE_VERSION
)
internal abstract class AccountRoomDatabase : RoomDatabase() {

    internal companion object {
        const val DATABASE_NAME = "account.db"
        const val DATABASE_VERSION = 7
        val migrations = arrayOf(
            MigrationFrom1To2,
            MigrationFrom2To3,
            MigrationFrom3To4,
            MigrationFrom4To5,
            MigrationFrom5To6,
            MigrationFrom6To7
        )
    }

    abstract fun profileDao(): ProfileDao

    abstract fun accountDao(): AccountDao
}
