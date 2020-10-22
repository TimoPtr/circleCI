/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.persistence.model

import com.kolibree.android.SHARED_MODE_PROFILE_ID
import com.kolibree.android.test.mocks.AccountToothbrushBuilder
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class AccountToothbrushTest {
    @Test
    fun `isSharedToothbrush returns true if profileId is SHARED_MODE_PROFILE_ID`() {
        val accountToothbrush = AccountToothbrushBuilder.builder().withDefaultState()
            .withProfileId(SHARED_MODE_PROFILE_ID)
            .build()

        assertTrue(accountToothbrush.isSharedToothbrush)
    }

    @Test
    fun `isSharedToothbrush returns false if profileId is different than SHARED_MODE_PROFILE_ID`() {
        (0L until 100L).filterNot { it == SHARED_MODE_PROFILE_ID }.forEach { profileId ->
            val accountToothbrush = AccountToothbrushBuilder.builder().withDefaultState()
                .withProfileId(profileId)
                .build()

            assertFalse("Was true for $profileId", accountToothbrush.isSharedToothbrush)
        }
    }
}
