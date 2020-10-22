/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.persistence.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.android.accountinternal.internal.AccountInternal

// add amazon_drs_enabled field
@Suppress("MagicNumber")
internal object MigrationFrom5To6 : Migration(5, 6) {

    override fun migrate(database: SupportSQLiteDatabase) {
        val request =
            "ALTER TABLE `${AccountInternal.TABLE_NAME}`" +
            " ADD COLUMN `${AccountInternal.COLUMN_ACCOUNT_AMAZON_DRS_ENABLED}` INTEGER NOT NULL DEFAULT 0"
        database.execSQL(request)
    }
}
