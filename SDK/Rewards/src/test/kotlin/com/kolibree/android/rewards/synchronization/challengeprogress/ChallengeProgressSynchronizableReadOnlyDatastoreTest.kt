package com.kolibree.android.rewards.synchronization.challengeprogress

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.persistence.ChallengeProgressDao
import com.kolibree.android.rewards.persistence.ChallengeProgressProfileCatalogInternal
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.mockito.Mock

internal class ChallengeProgressSynchronizableReadOnlyDatastoreTest : BaseUnitTest() {
    @Mock
    lateinit var challengeProgressDao: ChallengeProgressDao

    @Mock
    lateinit var rewardsSynchronizedVersions: RewardsSynchronizedVersions

    private lateinit var datastore: ChallengeProgressSynchronizableReadOnlyDatastore

    override fun setup() {
        super.setup()

        datastore = ChallengeProgressSynchronizableReadOnlyDatastore(challengeProgressDao, rewardsSynchronizedVersions)
    }

    /*
    UPDATE VERSION
     */

    @Test
    fun `update version invokes setChallengeProgressVersion`() {
        val expectedVersion = 432
        datastore.updateVersion(expectedVersion)

        verify(rewardsSynchronizedVersions).setChallengeProgressVersion(expectedVersion)
    }

    /*
    REPLACE
     */
    @Test
    fun `replace does nothing when parameter is not ChallengeProgressProfileCatalogInternal`() {
        datastore.replace(object : SynchronizableReadOnly {})

        verifyNoMoreInteractions(challengeProgressDao)
    }

    @Test
    fun `replace inserts ChallengeProgressProfileCatalogInternal`() {
        val expectedCatalog = ChallengeProgressProfileCatalogInternal()

        datastore.replace(expectedCatalog)

        verify(challengeProgressDao).replace(expectedCatalog)
    }
}
