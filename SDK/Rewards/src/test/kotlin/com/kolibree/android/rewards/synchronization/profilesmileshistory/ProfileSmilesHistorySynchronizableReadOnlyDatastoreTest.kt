/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.profilesmileshistory

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.feedback.HistoryEventsConsumer
import com.kolibree.android.rewards.models.EVENT_TYPE_CHALLENGE_COMPLETED
import com.kolibree.android.rewards.models.SmilesHistoryEventEntity
import com.kolibree.android.rewards.persistence.RewardsSynchronizedVersions
import com.kolibree.android.rewards.persistence.SmilesHistoryEventsDao
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.mockito.Mock

internal class ProfileSmilesHistorySynchronizableReadOnlyDatastoreTest : BaseUnitTest() {
    @Mock
    lateinit var smilesHistoryEventsDao: SmilesHistoryEventsDao

    @Mock
    lateinit var rewardsSynchronizedVersions: RewardsSynchronizedVersions

    @Mock
    lateinit var mapper: SmilesHistoryEventMapper

    @Mock
    lateinit var historyEventsConsumer: HistoryEventsConsumer

    private lateinit var datastore: ProfileSmilesHistorySynchronizableReadOnlyDatastore

    override fun setup() {
        super.setup()

        datastore =
            ProfileSmilesHistorySynchronizableReadOnlyDatastore(
                smilesHistoryEventsDao,
                rewardsSynchronizedVersions,
                historyEventsConsumer,
                mapper
            )
    }

    @Test
    fun `replace does nothing if parameter is not ProfileSmilesHistoryApiWithProfileId`() {
        datastore.replace(mock())

        verify(smilesHistoryEventsDao, never()).replace(any())
    }

    @Test
    fun `replace invokes replace with list of SmilesHistoryEventEntity extracted from ProfileSmilesHistoryApiWithProfileId`() {
        val smilesHistoryApiWithProfileId = ProfileSmilesHistoryApiWithProfileId(1L, ProfileSmilesHistoryApi(listOf()))

        val expectedEventsToInsert = listOf<SmilesHistoryEventEntity>()
        whenever(mapper.map(smilesHistoryApiWithProfileId)).thenReturn(expectedEventsToInsert)

        datastore.replace(smilesHistoryApiWithProfileId)

        verify(smilesHistoryEventsDao).replace(expectedEventsToInsert)
    }

    /*
    CONSUME SMILES HISTORY EVENT FOR PROFILE
     */

    @Test
    fun `consumeSmilesHistoryEventForProfile filters events which are not related to profileId`() {
        val profileId = 8080L
        val smileHistoryEventForCurrentProfile1 = createSmileHistoryEvent(profileId, 1L)
        val smileHistoryEventForCurrentProfile2 = createSmileHistoryEvent(profileId, 7L)
        whenever(smilesHistoryEventsDao.read()).thenReturn(
            listOf(
                createSmileHistoryEvent(128L, 2L),
                smileHistoryEventForCurrentProfile1,
                createSmileHistoryEvent(129L, 3L),
                createSmileHistoryEvent(130L, 4L),
                createSmileHistoryEvent(234L, 5L),
                createSmileHistoryEvent(6969L, 6L),
                smileHistoryEventForCurrentProfile2
            )
        )

        datastore.consumeSmilesHistoryEventForProfile(profileId)

        verify(historyEventsConsumer).accept(
            listOf(
                smileHistoryEventForCurrentProfile1,
                smileHistoryEventForCurrentProfile2
            )
        )
    }

    private fun createSmileHistoryEvent(profileId: Long, id: Long) = SmilesHistoryEventEntity(
        id = id,
        brushingId = null,
        brushingType = null,
        challengeId = 12L,
        creationTime = TrustedClock.getNowZonedDateTime(),
        eventType = EVENT_TYPE_CHALLENGE_COMPLETED,
        message = "message",
        profileId = profileId,
        relatedProfileId = null,
        rewardsId = null,
        smiles = 12,
        tierLevel = null
    )
    /*
    UPDATE VERSION
     */

    @Test
    fun `updateVersion invokes setSmilesHistoryVersion with expected value`() {
        val expectedVersion = 543
        datastore.updateVersion(expectedVersion)

        verify(rewardsSynchronizedVersions).setSmilesHistoryVersion(expectedVersion)
    }
}
