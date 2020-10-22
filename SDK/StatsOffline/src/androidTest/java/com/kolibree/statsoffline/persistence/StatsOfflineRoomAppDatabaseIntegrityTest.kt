/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.test.BaseRoomSchemaIntegrityTest
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("SpreadOperator")
@RunWith(AndroidJUnit4::class)
internal class StatsOfflineRoomAppDatabaseIntegrityTest :
    BaseRoomSchemaIntegrityTest<StatsOfflineRoomAppDatabase>(
        StatsOfflineRoomAppDatabase::class,
        StatsOfflineRoomAppDatabase.DATABASE_NAME,
        StatsOfflineRoomAppDatabase.DATABASE_VERSION,
        *StatsOfflineRoomAppDatabase.migrations
    ) {

    @Test
    override fun checkDatabaseIntegrity() {
        migrateAll()
    }
}
