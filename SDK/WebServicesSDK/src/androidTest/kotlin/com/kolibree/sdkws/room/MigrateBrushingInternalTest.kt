/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.room

import android.database.sqlite.SQLiteException
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.room.ZoneOffsetConverter
import com.kolibree.android.room.booleanValueForColumn
import com.kolibree.android.room.intValueForColumn
import com.kolibree.android.room.longValueForColumn
import com.kolibree.android.room.stringValueForColumn
import com.kolibree.android.test.BaseRoomMigrationTest
import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import java.util.UUID
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.Test

internal class MigrateBrushingInternalTest :
    BaseRoomMigrationTest<ApiRoomDatabase>(ApiRoomDatabase::class, ApiRoomDatabase.DATABASE_NAME) {

    @Test
    fun brushingInternal_have_current_system_offset_and_no_quality_and_timestamp_divide_by_1000() {
        val brushingInternal = BrushingInternal(
            42,
            "coach",
            1,
            TrustedClock.getNowOffsetDateTime(),
            2,
            3,
            true,
            4,
            "hello",
            5,
            6,
            true,
            "serial",
            "mac",
            "app",
            "build",
            idempotencyKey = UUID.randomUUID()
        )
        initializeDatabaseWith(19) {
            insertBrushingInternalInSchemaVersion19(brushingInternal)
            val cursor = fetchBrushingInternalInSchemaVersion19()
            assertEquals(1, cursor.count)
        }

        runMigrationAndCheck(MigrateBrushingInternal) {
            try {
                fetchBrushingInternalInSchemaVersion19()
                fail("old table still exist")
            } catch (_: SQLiteException) {
                // no-op
            }
            val cursor = fetchBrushingInternalInSchemaVersion20()
            assertEquals(1, cursor.count)

            assertEquals(-1, cursor.getColumnIndex("quality"))
            assertEquals(-1, cursor.getColumnIndex("date"))

            cursor.moveToFirst()

            assertEquals(brushingInternal.id, cursor.longValueForColumn("id"))
            assertEquals(brushingInternal.game, cursor.stringValueForColumn("game"))
            assertEquals(brushingInternal.duration, cursor.longValueForColumn("duration"))
            assertEquals(brushingInternal.timestamp / 1000, cursor.longValueForColumn("timestamp"))
            assertEquals(
                ZoneOffsetConverter.fromZoneOffset(brushingInternal.timestampZoneOffset),
                cursor.stringValueForColumn("timestampZoneOffset")
            )
            assertEquals(brushingInternal.profileId, cursor.longValueForColumn("profileid"))
            assertEquals(brushingInternal.coins, cursor.intValueForColumn("coins"))
            assertEquals(brushingInternal.isSynchronized, cursor.booleanValueForColumn("issync"))
            assertEquals(brushingInternal.goalDuration, cursor.intValueForColumn("goal_duration"))
            assertEquals(brushingInternal.processedData, cursor.stringValueForColumn("processed_data"))
            assertEquals(brushingInternal.points, cursor.intValueForColumn("points"))
            assertEquals(brushingInternal.kolibreeId, cursor.longValueForColumn("kolibree_id"))
            assertEquals(brushingInternal.isDeletedLocally, cursor.booleanValueForColumn("is_deleted_locally"))
            assertEquals(brushingInternal.toothbrushSerial, cursor.stringValueForColumn("serial"))
            assertEquals(brushingInternal.toothbrushMac, cursor.stringValueForColumn("mac"))
            assertEquals(brushingInternal.appVersion, cursor.stringValueForColumn("app_version"))
            assertEquals(brushingInternal.appBuild, cursor.stringValueForColumn("app_build"))
        }
    }

    private fun SupportSQLiteDatabase.insertBrushingInternalInSchemaVersion19(
        brushingInternal: BrushingInternal,
        quality: Int = 0
    ) {
        with(brushingInternal) {
            execSQL(
                "INSERT INTO brushingnew (id, game, duration, date, quality, profileid, coins, issync, " +
                    "goal_duration, processed_data, points, kolibree_id," +
                    " is_deleted_locally, serial, mac, app_version, app_build) VALUES " +
                    "($id, '$game', $duration, $timestamp, $quality, $profileId, " +
                    "$coins, ${if (isSynchronized) 1 else 0}, $goalDuration, " +
                    "'$processedData', $points, $kolibreeId, ${if (isDeletedLocally) 1 else 0}," +
                    " '$toothbrushSerial', '$toothbrushMac', '$appVersion', '$appBuild')"
            )
        }
    }

    private fun SupportSQLiteDatabase.fetchBrushingInternalInSchemaVersion19() =
        query("SELECT * FROM brushingnew", emptyArray())

    private fun SupportSQLiteDatabase.fetchBrushingInternalInSchemaVersion20() =
        query("SELECT * FROM brushing", emptyArray())
}
