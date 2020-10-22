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
import com.kolibree.android.rewards.models.LifetimeSmilesEntity
import com.kolibree.android.rewards.persistence.LifetimeSmilesDao
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.rewards.synchronization.lifetimesmiles.LifetimeSmilesSynchronizableReadOnlyDatastore
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.mockito.Mock

internal class LifetimeSmilesDatastoreTest : BaseUnitTest() {
    @Mock
    lateinit var lifetimeSmilesDao: LifetimeSmilesDao

    @Mock
    lateinit var rewardsSynchronizedVersions: RewardsSynchronizedVersions

    private lateinit var catalogDatastore: LifetimeSmilesSynchronizableReadOnlyDatastore

    override fun setup() {
        super.setup()

        catalogDatastore =
            LifetimeSmilesSynchronizableReadOnlyDatastore(
                lifetimeSmilesDao,
                rewardsSynchronizedVersions
            )
    }

    @Test
    fun `replace does nothing if parameter is not LifetimeSmilesEntity `() {
        catalogDatastore.replace(mock())

        verify(lifetimeSmilesDao, never()).insertOrReplace(any())
    }

    @Test
    fun `replace invokes replace with LifetimeSmilesEntity`() {
        val lifetimeSmilesEntity =
            LifetimeSmilesEntity(
                profileId = 12,
                lifetimePoints = 545
            )

        catalogDatastore.replace(lifetimeSmilesEntity)

        verify(lifetimeSmilesDao).insertOrReplace(lifetimeSmilesEntity)
    }

    /*
    UPDATE VERSION
     */

    @Test
    fun `updateVersion invokes lifetimeSmilesVersions with expected value`() {
        val expectedVersion = 543
        catalogDatastore.updateVersion(expectedVersion)

        verify(rewardsSynchronizedVersions).setLifetimeSmilesVersion(expectedVersion)
    }
}
