/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.app.ui.home.tab.profile.card.lifetimestats.sync

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.rewards.synchronization.lifetimesmiles.LifetimeSmilesSynchronizationKeyBuilder
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class LifetimeSmilesKeyBuilderTest : BaseUnitTest() {

    private val rewardsSynchronizedVersions: RewardsSynchronizedVersions = mock()

    private lateinit var keyBuilder: LifetimeSmilesSynchronizationKeyBuilder

    override fun setup() {
        super.setup()

        keyBuilder = LifetimeSmilesSynchronizationKeyBuilder(rewardsSynchronizedVersions)
    }

    @Test
    fun `build returns SynchronizeAccountKey with value from lifetimeSmilesVersion`() {
        val expectedValue = 543
        whenever(rewardsSynchronizedVersions.lifetimeSmilesVersion()).thenReturn(expectedValue)

        val synchronizeKey = keyBuilder.build()

        assertEquals(SynchronizableKey.LIFETIME_SMILES, synchronizeKey.key)
        assertEquals(expectedValue, synchronizeKey.version)
    }
}
