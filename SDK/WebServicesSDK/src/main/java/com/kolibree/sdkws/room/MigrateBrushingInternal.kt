/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.room.ZoneOffsetConverter

internal object MigrateBrushingInternal : Migration(START_VERSION, END_VERSION) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `brushing` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`game` TEXT NOT NULL, `duration` INTEGER NOT NULL, " +
                "`timestamp` INTEGER NOT NULL, `timestampZoneOffset` TEXT NOT NULL, " +
                "`profileid` INTEGER NOT NULL, `coins` INTEGER NOT NULL, `issync` INTEGER NOT NULL," +
                " `goal_duration` INTEGER NOT NULL, `processed_data` TEXT, `points` INTEGER NOT NULL, " +
                "`kolibree_id` INTEGER NOT NULL, `is_deleted_locally` INTEGER NOT NULL, `serial` TEXT, " +
                "`mac` TEXT, `app_version` TEXT, `app_build` TEXT)"
        )
        val currentOffset = ZoneOffsetConverter.fromZoneOffset(TrustedClock.systemZoneOffset)
        database.execSQL(
            "INSERT INTO brushing (id, game, duration, timestamp, timestampZoneOffset, " +
                "profileid, coins, issync, goal_duration, processed_data, points, kolibree_id, " +
                "is_deleted_locally, serial, mac, app_version, app_build) " +

                "SELECT id, game, duration, date / 1000, '$currentOffset', profileid, coins, " +
                "issync, goal_duration, processed_data," +
                " points, kolibree_id, is_deleted_locally, serial, mac, app_version, app_build FROM brushingnew"
        )
        database.execSQL("DROP TABLE brushingnew")
    }
}

private const val START_VERSION = 19
private const val END_VERSION = 20
