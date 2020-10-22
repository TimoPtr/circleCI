/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.user

import com.kolibree.android.SHARED_MODE_PROFILE_ID
import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsOwnerByOrSharedTest : BaseUnitTest() {
    @Test
    fun `isOwnedByOrShared returns true if parameter is equals to value`() {
        (100L until SHARED_MODE_PROFILE_ID).forEach {
            assertTrue(it.isOwnedByOrShared(it))
        }
    }

    @Test
    fun `isOwnedByOrShared returns true if value is SHARED_MODE_PROFILE_ID`() {
        (100L until SHARED_MODE_PROFILE_ID).forEach {
            assertTrue(SHARED_MODE_PROFILE_ID.isOwnedByOrShared(it))
        }
    }

    @Test
    fun `isOwnedByOrShared returns false if value is not SHARED_MODE_PROFILE_ID and parameter is different than value`() {
        (100L until SHARED_MODE_PROFILE_ID).forEach {
            assertFalse("$it returned true", it.isOwnedByOrShared(it + 1))
        }
    }
}
