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
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal

// add brushing number field
internal object MigrationFrom1To2 : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        val request =
            "ALTER TABLE `${ProfileInternal.TABLE_NAME}`" +
            " ADD COLUMN `${ProfileInternal.FIELD_BUSHING_NB}` INTEGER NOT NULL DEFAULT 0"
        database.execSQL(request)
    }
}
