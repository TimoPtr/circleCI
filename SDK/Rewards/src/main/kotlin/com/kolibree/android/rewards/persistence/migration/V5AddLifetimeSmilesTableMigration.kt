/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.persistence.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal const val INITIAL_VERSION = 4
internal const val FINAL_VERSION = 5

internal object V5AddLifetimeSmilesTableMigration : Migration(INITIAL_VERSION, FINAL_VERSION) {

    override fun migrate(database: SupportSQLiteDatabase) {
        val request = """
            CREATE TABLE IF NOT EXISTS `lifetime_stats` (`profileId` INTEGER NOT NULL, `lifetimePoints` INTEGER NOT NULL, PRIMARY KEY(`profileId`))
        """.trimIndent()

        database.execSQL(request)
    }
}
