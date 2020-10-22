/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.persistence.room.migrations

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.sdk.persistence.room.ToothbrushSDKRoomAppDatabase
import com.kolibree.android.test.BaseRoomSchemaIntegrityTest
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("SpreadOperator")
@RunWith(AndroidJUnit4::class)
internal class AccountToothbrushDatabaseIntegrityTest :
    BaseRoomSchemaIntegrityTest<ToothbrushSDKRoomAppDatabase>(
        ToothbrushSDKRoomAppDatabase::class,
        ToothbrushSDKRoomAppDatabase.DATABASE_NAME,
        ToothbrushSDKRoomAppDatabase.VERSION,
        *ToothbrushSDKRoomAppDatabase.migrations
    ) {

    @Test
    override fun checkDatabaseIntegrity() {
        migrateAll()
    }
}
