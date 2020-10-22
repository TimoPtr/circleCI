/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.persistence

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.test.BaseRoomSchemaIntegrityTest
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("SpreadOperator")
@RunWith(AndroidJUnit4::class)
internal class OfflineBrushingRoomDatabaseSchemaIntegrityTest :
    BaseRoomSchemaIntegrityTest<OfflineBrushingsRoomDatabase>(
        OfflineBrushingsRoomDatabase::class,
        OfflineBrushingsRoomDatabase.DATABASE_NAME,
        OfflineBrushingsRoomDatabase.VERSION,
        *OfflineBrushingsRoomDatabase.migrations
    ) {

    @Test
    override fun checkDatabaseIntegrity() {
        migrateAll()
    }
}
