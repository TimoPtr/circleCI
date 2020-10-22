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
import com.kolibree.android.rewards.models.TierEntity
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.rewards.persistence.TiersDao
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.mockito.Mock
import org.threeten.bp.LocalDate

internal class TiersSynchronizableCatalogDatastoreTest : BaseUnitTest() {
    @Mock
    lateinit var tiersDao: TiersDao

    @Mock
    lateinit var rewardsSynchronizedVersions: RewardsSynchronizedVersions

    private lateinit var catalogDatastore: TiersSynchronizableCatalogDatastore

    override fun setup() {
        super.setup()

        catalogDatastore = TiersSynchronizableCatalogDatastore(tiersDao, rewardsSynchronizedVersions)
    }

    @Test
    fun `replace does nothing if parameter is not TiersCatalogApi `() {
        catalogDatastore.replace(mock())

        verify(tiersDao, never()).replace(any())
    }

    @Test
    fun `replace TiersCatalogApi invokes replace with TierEntity list`() {
        val tierApi = TierApi(
            smilesPerBrushing = 1,
            challengesNeeded = 0,
            pictureUrl = "https://dyg43gvcnlkrm.cloudfront.net/rewards/Tier_Ivory.png",
            rank = "Ivory",
            creationDate = LocalDate.of(2018, 1, 18),
            message = "Congratulations! You reached Ivory tier"
        )

        val expectedId = 1
        catalogDatastore.replace(TiersCatalogApi(mapOf(Pair(expectedId, tierApi))))

        val expectedTierEntity = TierEntity(
            level = expectedId,
            smilesPerBrushing = tierApi.smilesPerBrushing,
            challengesNeeded = tierApi.challengesNeeded,
            pictureUrl = tierApi.pictureUrl,
            rank = tierApi.rank,
            creationDate = tierApi.creationDate,
            message = tierApi.message
        )

        argumentCaptor<List<TierEntity>> {
            verify(tiersDao).replace(capture())

            assertEquals(1, firstValue.size)
            assertEquals(expectedTierEntity, firstValue.first())
        }
    }

    /*
    UPDATE VERSION
     */

    @Test
    fun `updateVersion invokes rewardsSynchronizedVersions with expected value`() {
        val expectedVersion = 543
        catalogDatastore.updateVersion(expectedVersion)

        verify(rewardsSynchronizedVersions).setTiersCatalogVersion(expectedVersion)
    }
}
