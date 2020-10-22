/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.tiers

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.mockito.Mock

internal class TiersSynchronizationKeyBuilderTest : BaseUnitTest() {

    @Mock
    lateinit var rewardsSynchronizedVersions: RewardsSynchronizedVersions

    private lateinit var keyBuilder: TiersSynchronizationKeyBuilder

    override fun setup() {
        super.setup()

        keyBuilder = TiersSynchronizationKeyBuilder(rewardsSynchronizedVersions)
    }

    @Test
    fun `build returns SynchronizeAccountKey with value from rewardsSynchronizedVersions`() {
        val expectedValue = 543
        whenever(rewardsSynchronizedVersions.tiersCatalogVersion()).thenReturn(expectedValue)

        val synchronizeKey = keyBuilder.build()

        assertEquals(SynchronizableKey.TIERS_CATALOG, synchronizeKey.key)
        assertEquals(expectedValue, synchronizeKey.version)
    }
}
