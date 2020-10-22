/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.persistence.room

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.test.BaseRoomSchemaIntegrityTest
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("SpreadOperator")
@RunWith(AndroidJUnit4::class)
internal class StatsRoomAppDatabaseIntegrityTest :
    BaseRoomSchemaIntegrityTest<StatsRoomAppDatabase>(
        StatsRoomAppDatabase::class,
        StatsRoomAppDatabase.DATABASE_NAME,
        StatsRoomAppDatabase.DATABASE_VERSION,
        *StatsRoomAppDatabase.migrations
    ) {

    @Test
    override fun checkDatabaseIntegrity() {
        migrateAll()
    }
}
