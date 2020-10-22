/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.test.BaseRoomSchemaIntegrityTest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * If this test fails, it means you broke the database
 *
 * The most probably cause is that you updated a database version without providing migration.
 *
 * Related
 * - https://github.com/kolibree-git/android-monorepo/pull/628
 * - https://kolibree.atlassian.net/browse/KLTB002-10254
 */
@Suppress("SpreadOperator")
@RunWith(AndroidJUnit4::class)
internal class SynchronizatorDatabaseIntegrityTest :
    BaseRoomSchemaIntegrityTest<SynchronizatorDatabase>(
        SynchronizatorDatabase::class,
        SynchronizatorDatabase.DATABASE_NAME,
        SynchronizatorDatabase.DATABASE_VERSION,
        *SynchronizatorDatabase.migrations
    ) {

    @Test
    override fun checkDatabaseIntegrity() {
        migrateAll()
    }
}
