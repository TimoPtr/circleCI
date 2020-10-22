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
import com.kolibree.android.rewards.models.ProfileTierEntity
import com.kolibree.android.rewards.persistence.ProfileTierDao
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.mockito.Mock

internal class ProfileTierSynchronizableReadOnlyDatastoreTest : BaseUnitTest() {
    @Mock
    lateinit var profileTierDao: ProfileTierDao

    @Mock
    lateinit var rewardsSynchronizedVersions: RewardsSynchronizedVersions

    private lateinit var catalogDatastore: ProfileTierSynchronizableReadOnlyDatastore

    override fun setup() {
        super.setup()

        catalogDatastore = ProfileTierSynchronizableReadOnlyDatastore(profileTierDao, rewardsSynchronizedVersions)
    }

    @Test
    fun `replace does nothing if parameter is not TiersCatalogApi `() {
        catalogDatastore.replace(mock())

        verify(profileTierDao, never()).insert(any())
    }

    @Test
    fun `replace invokes replace with ProfileTierEntity parameter`() {
        val profileTierApi = ProfileTierEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            tierLevel = 5346
        )

        catalogDatastore.replace(profileTierApi)

        verify(profileTierDao).insert(profileTierApi)
    }

    /*
    UPDATE VERSION
     */

    @Test
    fun `updateVersion invokes rewardsSynchronizedVersions with expected value`() {
        val expectedVersion = 543
        catalogDatastore.updateVersion(expectedVersion)

        verify(rewardsSynchronizedVersions).setProfileTiersVersion(expectedVersion)
    }
}
