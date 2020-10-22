/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.headspace.data.persistence

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.test.BaseRoomSchemaIntegrityTest
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("SpreadOperator")
@RunWith(AndroidJUnit4::class)
internal class HeadspaceDatabaseIntegrityTest :
    BaseRoomSchemaIntegrityTest<HeadspaceDatabase>(
        HeadspaceDatabase::class,
        HeadspaceDatabase.DATABASE_NAME,
        HeadspaceDatabase.DATABASE_VERSION,
        *HeadspaceDatabase.migrations
    ) {

    @Test
    override fun checkDatabaseIntegrity() {
        migrateAll()
    }
}
