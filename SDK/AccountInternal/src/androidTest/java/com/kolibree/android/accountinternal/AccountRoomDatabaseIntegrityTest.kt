/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.test.BaseRoomSchemaIntegrityTest
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("SpreadOperator")
@RunWith(AndroidJUnit4::class)
internal class AccountRoomDatabaseIntegrityTest :
    BaseRoomSchemaIntegrityTest<AccountRoomDatabase>(
        AccountRoomDatabase::class,
        AccountRoomDatabase.DATABASE_NAME,
        AccountRoomDatabase.DATABASE_VERSION,
        *AccountRoomDatabase.migrations
    ) {

    @Test
    override fun checkDatabaseIntegrity() {
        migrateAll()
    }
}
