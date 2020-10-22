/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profilesmiles

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.models.ProfileSmilesEntity
import com.kolibree.android.rewards.persistence.ProfileSmilesDao
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.mockito.Mock

internal class ProfileSmilesSynchronizableReadOnlyDatastoreTest : BaseUnitTest() {
    @Mock
    lateinit var profileSmilesDao: ProfileSmilesDao

    @Mock
    lateinit var rewardsSynchronizedVersions: RewardsSynchronizedVersions

    private lateinit var datastore: ProfileSmilesSynchronizableReadOnlyDatastore

    override fun setup() {
        super.setup()

        datastore = ProfileSmilesSynchronizableReadOnlyDatastore(profileSmilesDao, rewardsSynchronizedVersions)
    }

    @Test
    fun `replace does nothing if parameter is not SmilessCatalogApi `() {
        datastore.replace(mock())

        verify(profileSmilesDao, never()).insert(any())
    }

    @Test
    fun `replace invokes replace with ProfileSmilesEntity parameter`() {
        val profileSmilesApi = ProfileSmilesEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            smiles = 5346
        )

        datastore.replace(profileSmilesApi)

        verify(profileSmilesDao).insert(profileSmilesApi)
    }

    /*
    UPDATE VERSION
     */

    @Test
    fun `updateVersion invokes rewardsSynchronizedVersions with expected value`() {
        val expectedVersion = 543
        datastore.updateVersion(expectedVersion)

        verify(rewardsSynchronizedVersions).setProfileSmilesVersion(expectedVersion)
    }
}
