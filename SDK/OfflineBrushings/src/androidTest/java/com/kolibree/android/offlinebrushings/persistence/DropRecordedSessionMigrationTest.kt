/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.persistence

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.test.BaseRoomMigrationTest
import java.io.IOException
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class DropRecordedSessionMigrationTest :
    BaseRoomMigrationTest<OfflineBrushingsRoomDatabase>(
        OfflineBrushingsRoomDatabase::class,
        OfflineBrushingsRoomDatabase.DATABASE_NAME
    ) {

    @Test
    @Throws(IOException::class)
    fun migrationFrom1To2_dropsRecordedSessionsTable() {
        initializeDatabaseWith(schemaVersion = 1) {
            queryTableExists(this).use {
                it.moveToFirst()
                assertEquals(1, it.count)
            }
        }

        runMigrationAndCheck(OfflineBrushingsRoomModule.migrationFrom1To2) {
            queryTableExists(this).use {
                it.moveToFirst()
                assertEquals(0, it.count)
            }
        }
    }

    companion object {

        private fun queryTableExists(db: SupportSQLiteDatabase): Cursor {
            return db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='recorded_session'")
        }
    }
}
