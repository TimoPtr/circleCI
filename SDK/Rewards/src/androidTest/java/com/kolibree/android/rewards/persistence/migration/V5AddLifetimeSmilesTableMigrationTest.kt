/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.persistence.migration

import com.kolibree.android.rewards.models.LifetimeSmilesEntity
import com.kolibree.android.rewards.persistence.RewardsRoomDatabase
import com.kolibree.android.test.BaseRoomMigrationTest
import com.kolibree.android.test.tableExists
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class V5AddLifetimeSmilesTableMigrationTest :
    BaseRoomMigrationTest<RewardsRoomDatabase>(
        RewardsRoomDatabase::class,
        RewardsRoomDatabase.DATABASE_NAME
    ) {

    @Test
    fun migration_addsDirtyColumnWithValueZero() {
        initializeDatabaseWith(schemaVersion = INITIAL_VERSION) {
            assertFalse(tableExists(LifetimeSmilesEntity.TABLE_NAME))
        }

        runMigrationAndCheck(V5AddLifetimeSmilesTableMigration) {
            assertTrue(tableExists(LifetimeSmilesEntity.TABLE_NAME))
        }
    }
}
