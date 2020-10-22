/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.persistence

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.room.ZoneOffsetConverter

internal object OfflineBrushingMigrationFrom2To3 : Migration(START_VERSION, END_VERSION) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE orphan_brushing RENAME TO orphan_brushing_old")
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `orphan_brushing` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " `duration` INTEGER NOT NULL, `goal_duration` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL," +
                " `timestampZoneOffset` TEXT NOT NULL, `toothbrush_mac` TEXT NOT NULL, " +
                "`toothbrush_serial` TEXT NOT NULL, `processed_data` TEXT, `kolibree_id` INTEGER," +
                " `is_deleted_locally` INTEGER NOT NULL, `is_synchronized` INTEGER NOT NULL," +
                " `assigned_profile_id` INTEGER)"
        )

        val currentOffset = ZoneOffsetConverter.fromZoneOffset(TrustedClock.systemZoneOffset)
        database.execSQL(
            "INSERT INTO orphan_brushing (id, duration, goal_duration, timestamp, timestampZoneOffset," +
                " toothbrush_mac, toothbrush_serial, processed_data, kolibree_id, is_deleted_locally," +
                " is_synchronized, assigned_profile_id) " +

                "SELECT id, duration, goal_duration, utc_timestamp / 1000, '$currentOffset', toothbrush_mac," +
                " toothbrush_serial, processed_data, kolibree_id, " +
                "is_deleted_locally, is_synchronized, assigned_profile_id FROM orphan_brushing_old"
        )

        database.execSQL("DROP TABLE orphan_brushing_old")
    }
}

private const val START_VERSION = 2
private const val END_VERSION = 3
