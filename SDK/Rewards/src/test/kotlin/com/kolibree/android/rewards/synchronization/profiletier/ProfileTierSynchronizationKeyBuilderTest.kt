/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profiletier

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.mockito.Mock

internal class ProfileTierSynchronizationKeyBuilderTest : BaseUnitTest() {

    @Mock
    lateinit var rewardsSynchronizedVersions: RewardsSynchronizedVersions

    private lateinit var keyBuilder: ProfileTierSynchronizationKeyBuilder

    override fun setup() {
        super.setup()

        keyBuilder = ProfileTierSynchronizationKeyBuilder(rewardsSynchronizedVersions)
    }

    @Test
    fun `build returns SynchronizeAccountKey with value from rewardsSynchronizedVersions`() {
        val expectedValue = 543
        whenever(rewardsSynchronizedVersions.profileTiersVersion()).thenReturn(expectedValue)

        val synchronizeKey = keyBuilder.build()

        assertEquals(SynchronizableKey.PROFILE_TIER, synchronizeKey.key)
        assertEquals(expectedValue, synchronizeKey.version)
    }
}
