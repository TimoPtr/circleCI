/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.feedback

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.models.BrushingSessionHistoryEvent
import com.kolibree.android.rewards.models.ChallengeCompletedHistoryEvent
import com.kolibree.android.rewards.models.TierReachedHistoryEvent
import com.kolibree.android.rewards.test.createBrushingSessionHistoryEvent
import com.kolibree.android.rewards.test.createChallengeCompletedHistoryEvent
import com.kolibree.android.rewards.test.createFeedbackEntity
import com.kolibree.android.rewards.test.createSmilesHistoryEventEntity
import com.kolibree.android.rewards.test.createStreakCompletedHistoryEvent
import com.kolibree.android.rewards.test.createTierReachedHistoryEvent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import timber.log.Timber

class HistoryToFeedbackMapperTest : BaseUnitTest() {

    @Test
    fun `extractFeedbackActions returns empty list when input is empty`() {
        assertTrue(mockedHistoryToFeedbackMapper(mock()).map(listOf()).isEmpty())
    }

    @Test
    fun `extractFeedbackActions returns list with non null items consumed by state machine`() {
        val event1 = createSmilesHistoryEventEntity(message = "msg1")
        val event2 = createSmilesHistoryEventEntity(message = "msg2")

        val feedbackStateMachine: FeedbackStateMachine = mock()
        val historyToFeedbackMapper = mockedHistoryToFeedbackMapper(feedbackStateMachine)

        val expectedItem = createFeedbackEntity(profileId = 1L)
        whenever(feedbackStateMachine.consume(event1)).thenReturn(null)
        whenever(feedbackStateMachine.consume(event2)).thenReturn(expectedItem)

        val returnedList = historyToFeedbackMapper.map(listOf(event1, event2))
        assertEquals(expectedItem, returnedList.single())
    }

    private fun mockedHistoryToFeedbackMapper(feedbackStateMachine: FeedbackStateMachine) =
        HistoryToFeedbackMapper(feedbackStateMachine)

    /*
    INTEGRATION TESTS
     */

    @Test
    fun `extractFeedbackActions returns SmiledEarned action from single BrushingSession event`() {
        val mapper = integrationHistoryToFeedbackMapper()

        assertTrue(mapper.map(listOf(createBrushingSessionHistoryEvent())).single().isSmilesEarned())
    }

    @Test
    fun `extractFeedbackActions returns 2 SmiledEarned action from 2 BrushingSession events`() {
        val mapper = integrationHistoryToFeedbackMapper()

        val expectedSmiles1 = 546
        val expectedSmiles2 = 65467
        val brushingEvent1 = createBrushingSessionHistoryEvent(brushingId = 1L, smiles = expectedSmiles1)
        val brushingEvent2 = createBrushingSessionHistoryEvent(brushingId = 2L, smiles = expectedSmiles2)
        val actions = mapper.map(listOf(brushingEvent1, brushingEvent2))

        assertEquals(2, actions.size)

        assertTrue(
            "$actions\nexpected ${brushingEvent1.toSmilesEarnedEntity()}",
            actions.contains(brushingEvent1.toSmilesEarnedEntity())
        )

        assertTrue(
            "$actions\nexpected ${brushingEvent2.toSmilesEarnedEntity()}",
            actions.contains(brushingEvent2.toSmilesEarnedEntity())
        )
    }

    @Test
    fun `extractFeedbackActions returns single ChallengeCompleted action from BrushingSession + ChallengeCompleted events`() {
        val mapper = integrationHistoryToFeedbackMapper()

        val brushingEvent = createBrushingSessionHistoryEvent()
        val challengeEvent = createChallengeCompletedHistoryEvent()
        val actions = mapper.map(listOf(brushingEvent, challengeEvent))

        assertEquals(challengeEvent.toChallengeCompletedEntity(), actions.single())
    }

    @Test
    fun `extractFeedbackActions returns single SmilesEarned action from BrushingSession + TierReached events`() {
        val mapper = integrationHistoryToFeedbackMapper()

        val brushingEvent = createBrushingSessionHistoryEvent()
        val tierReachedEvent = createTierReachedHistoryEvent()
        val actions = mapper.map(listOf(brushingEvent, tierReachedEvent))

        assertTrue(actions.single().isSmilesEarned())
    }

    @Test
    fun `extractFeedbackActions returns single ChallengeCompleted action with both IDs from ChallengeCompleted + ChallengeCompleted events`() {
        val mapper = integrationHistoryToFeedbackMapper()

        val expectedId1 = 56L
        val expectedId2 = 34L
        val oldestDate = TrustedClock.getNowZonedDateTime().minusSeconds(5)
        val mostRecentDate = TrustedClock.getNowZonedDateTime().minusSeconds(1)
        val challengeEvent1 = createChallengeCompletedHistoryEvent(challengeId = expectedId1, creationTime = oldestDate)
        val challengeEvent2 =
            createChallengeCompletedHistoryEvent(challengeId = expectedId2, creationTime = mostRecentDate)
        Timber.d("Pre failure")
        val actions = mapper.map(listOf(challengeEvent1, challengeEvent2))

        val expectedChallengeCompletedEntity = FeedbackEntity.createChallengeCompletedEntity(
            profileId = challengeEvent1.profileId,
            historyEventDateTime = mostRecentDate,
            challengesCompleted = listOf(expectedId1, expectedId2)
        )

        assertEquals(expectedChallengeCompletedEntity, actions.single())
    }

    @Test
    fun `extractFeedbackActions returns TierReached action with single challenge from BrushingSession + ChallengeCompleted + TierReached events`() {
        val mapper = integrationHistoryToFeedbackMapper()

        val brushingDate = TrustedClock.getNowZonedDateTime().minusHours(1)
        val brushingEvent = createBrushingSessionHistoryEvent(creationTime = brushingDate)

        val challengeDate = brushingDate.plusMinutes(1)
        val challengeEvent = createChallengeCompletedHistoryEvent(creationTime = challengeDate)

        val tierDate = challengeDate.plusMinutes(2)
        val tierReachedEvent = createTierReachedHistoryEvent(creationTime = tierDate)

        val actions = mapper.map(listOf(brushingEvent, challengeEvent, tierReachedEvent))

        val expectedAction = FeedbackEntity(
            profileId = tierReachedEvent.profileId,
            historyEventDateTime = tierDate,
            tierReached = tierReachedEvent.tierLevel,
            challengesCompleted = listOf(challengeEvent.challengeId)
        )

        assertEquals(expectedAction, actions.single())
    }

    @Test
    fun `extractFeedbackActions returns TierReached action with multiple challenges from BrushingSession + ChallengeCompleted + ChallengeCompleted + TierReached events`() {
        val mapper = integrationHistoryToFeedbackMapper()

        val brushingEvent = createBrushingSessionHistoryEvent()
        val firstChallengeId = 4L
        val firstChallengeEvent = createChallengeCompletedHistoryEvent(challengeId = firstChallengeId)
        val secondChallengeId = 5L
        val secondChallengeEvent = createChallengeCompletedHistoryEvent(challengeId = secondChallengeId)
        val tierReachedEvent = createTierReachedHistoryEvent()
        val actions = mapper.map(listOf(brushingEvent, firstChallengeEvent, secondChallengeEvent, tierReachedEvent))

        val expectedAction = FeedbackEntity(
            profileId = tierReachedEvent.profileId,
            historyEventDateTime = tierReachedEvent.creationTime,
            tierReached = tierReachedEvent.tierLevel,
            challengesCompleted = listOf(firstChallengeId, secondChallengeId)
        )

        assertEquals(expectedAction, actions.single())
    }

    @Test
    fun `extractFeedbackActions returns single StreakCompleted action from StreakCompleted + StreakCompleted events`() {
        val mapper = integrationHistoryToFeedbackMapper()

        val streakCompletedEvent1 = createStreakCompletedHistoryEvent()
        val streakCompletedEvent2 = createStreakCompletedHistoryEvent()
        val actions = mapper.map(listOf(streakCompletedEvent1, streakCompletedEvent2))

        assertTrue(actions.single().isStreakCompleted())
    }

    @Test
    fun `extractFeedbackActions returns 2 actions from BrushingEvent + StreakCompleted events`() {
        val mapper = integrationHistoryToFeedbackMapper()

        val brushingEvent = createBrushingSessionHistoryEvent()
        val streakCompletedEvent = createStreakCompletedHistoryEvent()
        val actions = mapper.map(listOf(brushingEvent, streakCompletedEvent))

        assertTrue(actions[1].isStreakCompleted())
        assertTrue(actions[0].isSmilesEarned())
    }

    private fun integrationHistoryToFeedbackMapper() =
        HistoryToFeedbackMapper(feedbackStateMachine = FeedbackStateMachine())
}

private fun BrushingSessionHistoryEvent.toSmilesEarnedEntity(): FeedbackEntity {
    return FeedbackEntity.createSmilesEarnedEntity(
        profileId = profileId,
        historyEventDateTime = creationTime,
        smilesEarned = smiles
    )
}

private fun ChallengeCompletedHistoryEvent.toChallengeCompletedEntity(): FeedbackEntity {
    return FeedbackEntity.createChallengeCompletedEntity(
        profileId = profileId,
        historyEventDateTime = creationTime,
        challengesCompleted = listOf(challengeId)
    )
}

private fun TierReachedHistoryEvent.toTierReachedEntity(challengesCompleted: List<Long>): FeedbackEntity {
    check(challengesCompleted.isNotEmpty())

    return FeedbackEntity.createTierReachedEntity(
        profileId = profileId,
        historyEventDateTime = creationTime,
        tierReached = tierLevel,
        challengesCompleted = challengesCompleted
    )
}
