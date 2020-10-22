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
import com.kolibree.android.room.booleanValueForColumn
import com.kolibree.android.test.BaseRoomMigrationTest
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.sdkws.brushing.persistence.models.TABLE_NAME
import com.kolibree.sdkws.room.ApiRoomDatabase
import java.util.UUID
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class AddFakeBrushingMigrationTest : BaseRoomMigrationTest<ApiRoomDatabase>(
    ApiRoomDatabase::class,
    TABLE_NAME
) {

    @Test
    fun migration_addsDirtyColumnWithValueZero() {
        initializeDatabaseWith(schemaVersion = FAKE_BRUSHING_START_VERSION) {
            val insertQuery =
                "INSERT OR REPLACE INTO `brushing` (`game`,`duration`,`timestamp`,`timestampZoneOffset`,`profileid`,`coins`,`issync`,`goal_duration`,`processed_data`,`points`,`kolibree_id`,`is_deleted_locally`,`serial`,`mac`,`app_version`,`app_build`,`idempotency_key`) VALUES ('%s',%s,%s,'%s',%s,%s,%s,%s,'%s',%s,%s,%s,'%s','%s','%s','%s','%s')"

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
                    "1200", // app build
                    UUID.randomUUID()
                )
            )

            queryBrushings().use { cursor ->
                assertEquals(1, cursor.count)

                cursor.moveToFirst()

                assertEquals(-1, cursor.getColumnIndex("is_fake_brushing"))
            }
        }

        runMigrationAndCheck(AddFakeBrushingMigration) {
            queryBrushings().use { cursor ->
                cursor.apply {
                    assertEquals(1, count)

                    assertTrue(cursor.moveToFirst())

                    assertEquals(false, booleanValueForColumn("is_fake_brushing"))
                }
            }
        }
    }

    private fun SupportSQLiteDatabase.queryBrushings() =
        query("SELECT * FROM $TABLE_NAME", emptyArray())
}
