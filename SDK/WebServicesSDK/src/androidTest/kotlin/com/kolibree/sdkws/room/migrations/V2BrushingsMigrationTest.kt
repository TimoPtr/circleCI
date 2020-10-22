/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.room.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.room.ZoneOffsetConverter
import com.kolibree.android.room.stringValueForColumn
import com.kolibree.android.test.BaseRoomMigrationTest
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.sdkws.brushing.persistence.models.TABLE_NAME
import com.kolibree.sdkws.room.ApiRoomDatabase
import java.util.UUID
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class V2BrushingsMigrationTest : BaseRoomMigrationTest<ApiRoomDatabase>(
    ApiRoomDatabase::class,
    TABLE_NAME
) {
    @Test
    fun addIdempotencyKey() {
        initializeDatabaseWith(schemaVersion = START_VERSION) {
            val insertQuery =
                "INSERT OR REPLACE INTO `brushing` (`game`,`duration`,`timestamp`,`timestampZoneOffset`,`profileid`,`coins`,`issync`,`goal_duration`,`processed_data`,`points`,`kolibree_id`,`is_deleted_locally`,`serial`,`mac`,`app_version`,`app_build`) VALUES ('%s',%s,%s,'%s',%s,%s,%s,%s,'%s',%s,%s,%s,'%s','%s','%s','%s')"

            execSQL(
                insertQuery.format(
                    "co", // game
                    120, // duration
                    1593671204, // timestamp
                    ZoneOffsetConverter.fromZoneOffset(TrustedClock.systemZoneOffset),
                    ProfileBuilder.DEFAULT_ID,
                    0, // coins
                    0, // isSync
                    120, // goal duration
                    "{}", // processed data
                    0, // points
                    0, // kolibree id
                    0, // is deleted locallly
                    KLTBConnectionBuilder.DEFAULT_SERIAL,
                    KLTBConnectionBuilder.DEFAULT_MAC,
                    "1", // app version
                    "1200" // app build
                )
            )

            execSQL(
                insertQuery.format(
                    "co+", // game
                    55, // duration
                    1593671204, // timestamp
                    ZoneOffsetConverter.fromZoneOffset(TrustedClock.systemZoneOffset),
                    ProfileBuilder.DEFAULT_ID,
                    0, // coins
                    0, // isSync
                    120, // goal duration
                    "{}", // processed data
                    0, // points
                    0, // kolibree id
                    0, // is deleted locallly
                    KLTBConnectionBuilder.DEFAULT_SERIAL,
                    KLTBConnectionBuilder.DEFAULT_MAC,
                    "1", // app version
                    "1200" // app build
                )
            )

            queryBrushings().use { cursor ->
                assertEquals(2, cursor.count)

                assertTrue(cursor.moveToFirst())

                while (cursor.moveToNext()) {
                    assertEquals(-1, cursor.getColumnIndex("idempotency_key"))
                }
            }
        }

        runMigrationAndCheck(V2BrushingsMigration) {
            queryBrushings().use { cursor ->
                assertEquals(2, cursor.count)

                assertTrue(cursor.moveToFirst())

                while (cursor.moveToNext()) {
                    val idempotencyValue = cursor.stringValueForColumn("idempotency_key")

                    assertNotNull(idempotencyValue)

                    assertTrue(idempotencyValue!!.isNotBlank())

                    UUID.fromString(idempotencyValue)
                }
            }
        }
    }

    private fun SupportSQLiteDatabase.queryBrushings() =
        query("SELECT * FROM $TABLE_NAME", emptyArray())
}
