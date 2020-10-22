/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.room

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.test.BaseRoomSchemaIntegrityTest
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("SpreadOperator")
@RunWith(AndroidJUnit4::class)
internal class ApiRoomDatabaseSchemaIntegrityTest : BaseRoomSchemaIntegrityTest<ApiRoomDatabase>(
    ApiRoomDatabase::class,
    ApiRoomDatabase.DATABASE_NAME,
    ApiRoomDatabase.DATABASE_VERSION,
    *ApiRoomDatabase.migrations
) {

    @Test
    override fun checkDatabaseIntegrity() {
        migrateAll()
    }
}
