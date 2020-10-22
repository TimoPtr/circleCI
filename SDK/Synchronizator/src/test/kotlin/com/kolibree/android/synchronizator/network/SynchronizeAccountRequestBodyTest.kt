/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.synchronizator.network

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizeAccountKey
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SynchronizeAccountRequestBodyTest : BaseUnitTest() {
    @Test
    fun `toMap returns empty map if parameter is empty`() {
        assertTrue(SynchronizeAccountRequestBody(setOf()).toMap().isEmpty())
    }

    @Test
    fun `toMap returns expected map from parameters`() {
        val key1 = SynchronizeAccountKey(SynchronizableKey.PROFILES, 1)
        val key2 = SynchronizeAccountKey(SynchronizableKey.ACCOUNT, 2)

        val expectedMap = mapOf(key1.key.value to key1.version, key2.key.value to key2.version)

        assertEquals(expectedMap, SynchronizeAccountRequestBody(setOf(key1, key2)).toMap())
    }
}
