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
import com.kolibree.android.rewards.models.SmilesHistoryEventEntity
import com.kolibree.android.rewards.test.createSmilesHistoryEventApi
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SmilesHistoryEventMapperTest : BaseUnitTest() {
    private val mapper = SmilesHistoryEventMapper()

    @Test
    fun `empty ProfileSmilesHistoryApiWithId returns empty list of events`() {
        val smilesHistoryApiWithProfileId = ProfileSmilesHistoryApiWithProfileId(1L, ProfileSmilesHistoryApi(listOf()))

        assertTrue(mapper.map(smilesHistoryApiWithProfileId).isEmpty())
    }

    @Test
    fun `maps items in ProfileSmilesHistoryApiWithId to SmilesHistoryEventEntity`() {
        val challengeCompletedType = "Challenge completed"
        val brushingSessionType = "Brushing session"
        val tierReachedType = "Tier reached"
        val smilesRedeemedType = "Smiles redeemed"
        val smilesTransferType = "Smiles transfer"
        val crownCompletedType = "Crown completed"
        val streakCompletedType = "Streak completed"

        val challengeCompleted = createSmilesHistoryEventApi(
            eventType = challengeCompletedType,
            message = "You have completed a challenge",
            smilesRewards = 1,
            brushingType = "Coach",
            brushingId = 23705
        )

        val brushingSession = createSmilesHistoryEventApi(
            eventType = brushingSessionType,
            message = "You have a brushing session",
            smilesRewards = 2,
            challengeId = 2
        )

        val tierReached = createSmilesHistoryEventApi(
            eventType = tierReachedType,
            message = "You have reached Silver Tier",
            smilesRewards = 3,
            tierLevel = 2
        )

        val smilesRedeemed = createSmilesHistoryEventApi(
            eventType = smilesRedeemedType,
            message = "You have redeemed smiles",
            smilesRewards = 4,
            rewardsId = 2
        )

        val smilesTransfer = createSmilesHistoryEventApi(
            eventType = smilesTransferType,
            message = "You have redeemed smiles",
            smilesRewards = 5,
            relatedProfileId = 2
        )

        val crownCompleted = createSmilesHistoryEventApi(
            eventType = crownCompletedType,
            message = "You have completed a crown",
            smilesRewards = 6,
            relatedProfileId = 2
        )

        val streakCompleted = createSmilesHistoryEventApi(
            eventType = streakCompletedType,
            message = "You have completed a streak",
            smilesRewards = 7,
            relatedProfileId = 2
        )

        val expectedProfileId = 543L
        val smilesHistoryApiWithProfileId = ProfileSmilesHistoryApiWithProfileId(
            expectedProfileId,
            ProfileSmilesHistoryApi(
                listOf(
                    challengeCompleted,
                    brushingSession,
                    tierReached,
                    smilesRedeemed,
                    smilesTransfer,
                    crownCompleted,
                    streakCompleted
                )
            )
        )

        val entities = mapper.map(smilesHistoryApiWithProfileId)
        assertEquals(7, entities.size)

        assertMappedToEntity(challengeCompletedType, expectedProfileId, challengeCompleted, entities)
        assertMappedToEntity(brushingSessionType, expectedProfileId, brushingSession, entities)
        assertMappedToEntity(tierReachedType, expectedProfileId, tierReached, entities)
        assertMappedToEntity(smilesRedeemedType, expectedProfileId, smilesRedeemed, entities)
        assertMappedToEntity(smilesTransferType, expectedProfileId, smilesTransfer, entities)
        assertMappedToEntity(crownCompletedType, expectedProfileId, crownCompleted, entities)
        assertMappedToEntity(streakCompletedType, expectedProfileId, streakCompleted, entities)
    }

    private fun assertMappedToEntity(
        type: String,
        expectedProfileId: Long,
        eventApi: SmilesHistoryEventApi,
        entities: List<SmilesHistoryEventEntity>
    ) {
        val entity = entities.find { it.eventType == type }

        assertNotNull("No type found for $type. Types present ${entities.map { it.eventType }}", entity)

        entity?.apply {
            assertEquals(eventApi.message, message)
            assertEquals(eventApi.smilesRewards, smiles)
            assertEquals(eventApi.creationTime, creationTime)
            assertEquals(expectedProfileId, profileId)
            assertEquals(eventApi.challengeId, challengeId)
            assertEquals(eventApi.brushingId, brushingId)
            assertEquals(eventApi.brushingType, brushingType)
            assertEquals(eventApi.tierLevel, tierLevel)
            assertEquals(eventApi.rewardsId, rewardsId)
            assertEquals(eventApi.relatedProfileId, relatedProfileId)
        }
    }
}
