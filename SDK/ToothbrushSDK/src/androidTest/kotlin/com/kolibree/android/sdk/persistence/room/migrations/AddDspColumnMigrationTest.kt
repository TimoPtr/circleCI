/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.persistence.room.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.room.booleanValueForColumn
import com.kolibree.android.room.longValueForColumn
import com.kolibree.android.room.stringValueForColumn
import com.kolibree.android.sdk.persistence.model.AccountToothbrush.Companion.TABLE_NAME
import com.kolibree.android.sdk.persistence.room.AccountToothbrushConverters
import com.kolibree.android.sdk.persistence.room.ToothbrushSDKRoomAppDatabase
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.android.test.BaseRoomMigrationTest
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class AddDspColumnMigrationTest :
    BaseRoomMigrationTest<ToothbrushSDKRoomAppDatabase>(
        ToothbrushSDKRoomAppDatabase::class,
        ToothbrushSDKRoomAppDatabase.DATABASE_NAME
    ) {

    @Test
    fun migration_addsDspColumnWithValueZero() {
        val converters = AccountToothbrushConverters()
        initializeDatabaseWith(schemaVersion = 3) {
            val insertQuery =
                "INSERT OR REPLACE INTO `$TABLE_NAME` (`mac`,`name`,`model`,`account_id`,`profile_id`,`serial`,`hardware_version`,`firmware_version`,`bootloader_version`,`dirty`) VALUES ('%s','%s','%s',%s,%s,'%s',%s,%s,%s,%s)"

            val queryWithValues = insertQuery.format(
                MAC,
                NAME,
                MODEL,
                ACCOUNT_ID,
                PROFILE_ID,
                SERIAL,
                converters.fromHardwareVersion(HW_VERSION),
                converters.fromSoftwareVersion(FW_VERSION),
                converters.fromSoftwareVersion(BL_VERSION),
                0
            )

            execSQL(queryWithValues)

            queryAccountToothbrush().use { cursor ->
                assertEquals(1, cursor.count)

                cursor.moveToFirst()

                assertEquals(-1, cursor.getColumnIndex("dsp_version"))
            }
        }

        runMigrationAndCheck(AddDspColumnMigration) {
            queryAccountToothbrush().use { cursor ->
                cursor.apply {
                    assertEquals(1, count)

                    moveToFirst()

                    assertEquals(MAC, stringValueForColumn("mac"))
                    assertEquals(SERIAL, stringValueForColumn("serial"))
                    assertEquals(NAME, stringValueForColumn("name"))
                    assertEquals(MODEL, ToothbrushModel.valueOf(stringValueForColumn("model")!!))
                    assertEquals(PROFILE_ID, longValueForColumn("profile_id"))
                    assertEquals(ACCOUNT_ID, longValueForColumn("account_id"))

                    assertEquals(
                        FW_VERSION,
                        converters.toSoftwareVersion(longValueForColumn("firmware_version")!!)
                    )
                    assertEquals(
                        BL_VERSION,
                        converters.toSoftwareVersion(longValueForColumn("bootloader_version")!!)
                    )
                    assertEquals(
                        HW_VERSION,
                        converters.toHardwareVersion(longValueForColumn("hardware_version")!!)
                    )

                    assertEquals(false, booleanValueForColumn("dirty"))
                    val dspVersion = longValueForColumn("dsp_version")
                    assertEquals(0L, dspVersion)

                    assertEquals(
                        DspVersion.NULL,
                        converters.toDspVersion(dspVersion!!)
                    )
                }
            }
        }
    }

    private fun SupportSQLiteDatabase.queryAccountToothbrush() =
        query("SELECT * FROM $TABLE_NAME", emptyArray())
}

private const val MAC = "00:D0:56:F2:B5:12"
private const val SERIAL = "ktb02eb00947-999999"
private const val NAME = "YOUR TEST toothbrush"
private val MODEL = ToothbrushModel.CONNECT_E2
private val FW_VERSION = SoftwareVersion(1, 2, 7)
private val HW_VERSION = HardwareVersion(7, 2)
private val BL_VERSION = SoftwareVersion(6, 2, 2)
private const val PROFILE_ID = 87L
private const val ACCOUNT_ID = 55L
