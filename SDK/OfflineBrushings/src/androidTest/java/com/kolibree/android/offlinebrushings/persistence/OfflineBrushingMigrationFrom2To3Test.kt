/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.persistence

import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.offlinebrushings.OrphanBrushing
import com.kolibree.android.room.ZoneOffsetConverter
import com.kolibree.android.room.booleanValueForColumn
import com.kolibree.android.room.intValueForColumn
import com.kolibree.android.room.longValueForColumn
import com.kolibree.android.room.stringValueForColumn
import com.kolibree.android.test.BaseRoomMigrationTest
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class OfflineBrushingMigrationFrom2To3Test : BaseRoomMigrationTest<OfflineBrushingsRoomDatabase>(
    OfflineBrushingsRoomDatabase::class,
    OfflineBrushingsRoomDatabase.DATABASE_NAME
) {
    @Test
    fun orphanBrushing_have_current_system_offset_and_no_quality_and_timestamp_divide_by_1000() {
        val orphanBrushing =
            OrphanBrushing(42, 1, 2, 3000, TrustedClock.systemZoneOffset, "mac", "serial", "data", null, true, true, 10)

        initializeDatabaseWith(2) {
            insertOfflineBrushingInSchemaVersion2(orphanBrushing)
            val cursor = fetchOrphanBrushing()
            assertEquals(1, cursor.count)
        }

        runMigrationAndCheck(OfflineBrushingMigrationFrom2To3) {
            val cursor = fetchOrphanBrushing()
            assertEquals(1, cursor.count)

            assertEquals(-1, cursor.getColumnIndex("quality"))
            assertEquals(-1, cursor.getColumnIndex("utc_timestamp"))

            cursor.moveToFirst()

            assertEquals(orphanBrushing.id, cursor.longValueForColumn("id"))
            assertEquals(orphanBrushing.duration, cursor.longValueForColumn("duration"))
            assertEquals(orphanBrushing.goalDuration, cursor.intValueForColumn("goal_duration"))
            assertEquals(orphanBrushing.timestamp / 1000, cursor.longValueForColumn("timestamp"))
            assertEquals(
                ZoneOffsetConverter.fromZoneOffset(orphanBrushing.timestampZoneOffset),
                cursor.stringValueForColumn("timestampZoneOffset")
            )
            assertEquals(orphanBrushing.toothbrushMac, cursor.stringValueForColumn("toothbrush_mac"))
            assertEquals(orphanBrushing.toothbrushSerial, cursor.stringValueForColumn("toothbrush_serial"))
            assertEquals(orphanBrushing.processedData, cursor.stringValueForColumn("processed_data"))
            assertEquals(orphanBrushing.kolibreeId, cursor.longValueForColumn("kolibree_id"))
            assertEquals(orphanBrushing.isDeletedLocally, cursor.booleanValueForColumn("is_deleted_locally"))
            assertEquals(orphanBrushing.isSynchronized, cursor.booleanValueForColumn("is_synchronized"))
            assertEquals(orphanBrushing.assignedProfileId, cursor.longValueForColumn("assigned_profile_id"))
        }
    }

    private fun SupportSQLiteDatabase.insertOfflineBrushingInSchemaVersion2(
        orphanBrushing: OrphanBrushing,
        quality: Int = 0
    ) {
        with(orphanBrushing) {
            execSQL(
                "INSERT INTO orphan_brushing (id, duration, quality, goal_duration, utc_timestamp, " +
                    "toothbrush_mac, toothbrush_serial, processed_data, kolibree_id, is_deleted_locally, " +
                    "is_synchronized, assigned_profile_id)" +
                    "VALUES ($id, $duration, $quality, $goalDuration, $timestamp, '$toothbrushMac', " +
                    "'$toothbrushSerial', '$processedData', $kolibreeId, ${if (isDeletedLocally) 1 else 0}, " +
                    "${if (isSynchronized) 1 else 0}, $assignedProfileId)"
            )
        }
    }

    private fun SupportSQLiteDatabase.fetchOrphanBrushing() =
        query("SELECT * FROM orphan_brushing", emptyArray())
}
