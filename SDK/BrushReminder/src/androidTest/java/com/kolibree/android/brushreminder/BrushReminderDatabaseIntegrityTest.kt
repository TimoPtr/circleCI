/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.brushreminder.data.BrushReminderDatabase
import com.kolibree.android.test.BaseRoomSchemaIntegrityTest
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("SpreadOperator")
@RunWith(AndroidJUnit4::class)
internal class BrushReminderDatabaseIntegrityTest :
    BaseRoomSchemaIntegrityTest<BrushReminderDatabase>(
        BrushReminderDatabase::class,
        BrushReminderDatabase.DATABASE_NAME,
        BrushReminderDatabase.DATABASE_VERSION,
        *BrushReminderDatabase.migrations
    ) {

    @Test
    override fun checkDatabaseIntegrity() {
        migrateAll()
    }
}
