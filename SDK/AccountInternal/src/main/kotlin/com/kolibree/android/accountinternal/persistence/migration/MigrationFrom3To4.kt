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

// add picture_last_modifier field
@Suppress("MagicNumber")
internal object MigrationFrom3To4 : Migration(3, 4) {

    override fun migrate(database: SupportSQLiteDatabase) {
        val request =
            "ALTER TABLE `${ProfileInternal.TABLE_NAME}`" +
            " ADD COLUMN `${ProfileInternal.FIELD_PICTURE_LAST_MODIFIER}` TEXT"
        database.execSQL(request)
    }
}
