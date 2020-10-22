/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.feedback

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.feedback.personal.BackendChallengeCompleted
import com.kolibree.android.rewards.models.TierEntity
import com.kolibree.android.rewards.persistence.FeedbackDao
import com.kolibree.android.rewards.persistence.TiersDao
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mock
import org.threeten.bp.ZonedDateTime

internal class FeedbackRepositoryImplTest : BaseUnitTest() {

    @Mock
    lateinit var feedbackDao: FeedbackDao

    @Mock
    lateinit var tiersDao: TiersDao

    @Mock
    lateinit var completedChallengesProvider: CompletedChallengesProvider

    lateinit var feedbackRepository: FeedbackRepositoryImpl

    override fun setup() {
        super.setup()

        feedbackRepository = FeedbackRepositoryImpl(feedbackDao, tiersDao, completedChallengesProvider)
    }

    @Test
    fun `flagAsRead action will avoid stream`() {
        val feedbackEntity: FeedbackEntity = mock()
        val feedbackAction: FeedbackAction = mock()
        whenever(feedbackAction.id).thenReturn(1L)
        whenever(feedbackDao.getFeedback(any())).thenReturn(feedbackEntity)
        doNothing().whenever(feedbackDao).update(feedbackEntity)

        feedbackRepository.markAsConsumed(listOf(feedbackAction.id))

        verify(feedbackDao, times(1)).markAsConsumed(listOf(feedbackAction.id))
    }

    @Test
    fun `feedbackNotConsumedStream emit lists of feedbackAction`() {
        val feedbackSmilesEarned: FeedbackEntity = mock()
        val feedbackSmilesEarnedId = 0L
        val smilesEarned = 10

        val feedbackNoFeedback: FeedbackEntity = mock()

        val feedbackProcessor = PublishProcessor.create<List<FeedbackEntity>>()

        whenever(feedbackDao.oldestFeedbackStream(any())).thenReturn(feedbackProcessor)

        // Smiles earned
        whenever(feedbackSmilesEarned.isSmilesEarned()).thenReturn(true)
        whenever(feedbackSmilesEarned.id).thenReturn(feedbackSmilesEarnedId)
        whenever(feedbackSmilesEarned.smilesEarned).thenReturn(smilesEarned)

        // NoFeedBack
        whenever(feedbackNoFeedback.isSmilesEarned()).thenReturn(false)
        whenever(feedbackNoFeedback.isChallengesCompleted()).thenReturn(false)
        whenever(feedbackNoFeedback.isTierReached()).thenReturn(false)

        val testSubscriber = feedbackRepository.feedbackNotConsumedStream(1L).test()

        feedbackProcessor.onNext(listOf(feedbackSmilesEarned, feedbackNoFeedback))

        testSubscriber.assertNotComplete()
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(listOf(SmilesEarnedFeedback(feedbackSmilesEarnedId, smilesEarned), NoFeedback))
    }

    @Test
    fun `createChallengeCompletedFeedback from FeedbackEntity`() {
        val feedbackChallengeCompleted: FeedbackEntity = mock()
        val feedbackChallengeCompletedId = 0L
        val challengeId = 0L
        val completedChallenge = BackendChallengeCompleted(
            id = 1L,
            category = "category1",
            pictureUrl = "picture1",
            smilesReward = 0,
            greetingMessage = "message1",
            description = "description1",
            name = "name1",
            action = null
        )

        whenever(feedbackChallengeCompleted.id).thenReturn(feedbackChallengeCompletedId)
        whenever(feedbackChallengeCompleted.challengesCompleted).thenReturn(listOf(challengeId))
        whenever(feedbackChallengeCompleted.isSmilesEarned()).thenReturn(false)
        whenever(feedbackChallengeCompleted.isChallengesCompleted()).thenReturn(true)

        whenever(completedChallengesProvider.provide(listOf(challengeId))).thenReturn(listOf(completedChallenge))

        whenever(feedbackDao.oldestFeedbackStream(any())).thenReturn(Flowable.just(listOf(feedbackChallengeCompleted)))

        val testSubscriber = feedbackRepository.feedbackNotConsumedStream(1L).test()

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(
            listOf(
                ChallengeCompletedFeedback(
                    feedbackChallengeCompletedId,
                    listOf(BackendChallengeCompleted(completedChallenge))
                )
            )
        )
    }

    @Test
    fun `createTierReachedFeedback from FeedbackEntity`() {
        val feedbackTierReached: FeedbackEntity = mock()
        val feedbackTierReachedId = 0L
        val tierLevelReached = 1
        val challengeId = 0L
        val completedChallenge = BackendChallengeCompleted(
        id = 2L,
        category = "category2",
        pictureUrl = "picture2",
        smilesReward = 0,
        greetingMessage = "message2",
        description = "description2",
        name = "name2",
        action = null
        )
        val tierReachedEntity: TierEntity = mock()

        whenever(feedbackTierReached.id).thenReturn(feedbackTierReachedId)
        whenever(feedbackTierReached.tierReached).thenReturn(tierLevelReached)
        whenever(feedbackTierReached.challengesCompleted).thenReturn(listOf(challengeId))
        whenever(feedbackTierReached.isSmilesEarned()).thenReturn(false)
        whenever(feedbackTierReached.isChallengesCompleted()).thenReturn(false)
        whenever(feedbackTierReached.isTierReached()).thenReturn(true)

        whenever(completedChallengesProvider.provide(listOf(challengeId))).thenReturn(listOf(completedChallenge))
        whenever(tiersDao.read(tierLevelReached)).thenReturn(tierReachedEntity)

        whenever(feedbackDao.oldestFeedbackStream(any())).thenReturn(Flowable.just(listOf(feedbackTierReached)))

        val testSubscriber = feedbackRepository.feedbackNotConsumedStream(1L).test()

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(
            listOf(
                TierReachedFeedback(
                    feedbackTierReachedId,
                    tierReachedEntity,
                    listOf(BackendChallengeCompleted(completedChallenge))
                )
            )
        )
    }

    @Test
    fun `NoSmilesEarned feedback`() {
        val expectedId = 87L
        val noSmilesEarned = FeedbackEntity(
            id = expectedId,
            profileId = ProfileBuilder.DEFAULT_ID,
            historyEventDateTime = TrustedClock.getNowZonedDateTime(),
            smilesEarned = 0
        )

        val feedbackProcessor = PublishProcessor.create<List<FeedbackEntity>>()

        whenever(feedbackDao.oldestFeedbackStream(any())).thenReturn(feedbackProcessor)

        val testSubscriber = feedbackRepository.feedbackNotConsumedStream(1L).test()

        feedbackProcessor.onNext(listOf(noSmilesEarned))

        testSubscriber.assertNotComplete()
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(listOf(NoSmilesEarnedFeedback(expectedId)))
    }

    @Test
    fun `OfflineBrushingsSyncedFeedback feedback`() {
        val expectedId = 123L
        val expectedOfflineBrushings = 23
        val expectedSmiles = 15
        val offlineBrushingsSyncedFeedback = FeedbackEntity(
            id = expectedId,
            profileId = ProfileBuilder.DEFAULT_ID,
            historyEventDateTime = TrustedClock.getNowZonedDateTime(),
            smilesEarned = expectedSmiles,
            offlineSyncBrushings = expectedOfflineBrushings
        )

        val feedbackProcessor = PublishProcessor.create<List<FeedbackEntity>>()

        whenever(feedbackDao.oldestFeedbackStream(any())).thenReturn(feedbackProcessor)

        val testSubscriber = feedbackRepository.feedbackNotConsumedStream(1L).test()

        feedbackProcessor.onNext(listOf(offlineBrushingsSyncedFeedback))

        testSubscriber.assertNotComplete()
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(
            listOf(
                OfflineBrushingsSyncedFeedback(expectedId, expectedOfflineBrushings, expectedSmiles)
            )
        )
    }

    @Test
    fun `StreakCompletedFeedback feedback`() {
        val expectedId = 123L
        val smiles = 23
        val streakCompleted = FeedbackEntity(
            id = expectedId,
            profileId = ProfileBuilder.DEFAULT_ID,
            historyEventDateTime = TrustedClock.getNowZonedDateTime(),
            streakSmilesEarned = smiles
        )

        val feedbackProcessor = PublishProcessor.create<List<FeedbackEntity>>()

        whenever(feedbackDao.oldestFeedbackStream(any())).thenReturn(feedbackProcessor)

        val testSubscriber = feedbackRepository.feedbackNotConsumedStream(1L).test()

        feedbackProcessor.onNext(listOf(streakCompleted))

        testSubscriber.assertNotComplete()
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(listOf(StreakCompletedFeedback(expectedId, smiles)))
    }

    @Test
    fun `mapToFeedbackAction returns OfflineBrushingsSyncedFeedback`() {
        val entity = FeedbackEntity.createOfflineSyncEntity(
            profileId = 123L,
            smilesEarned = 20,
            offlineSyncBrushings = 15,
            historyEventDateTime = ZonedDateTime.now()
        )

        val action = feedbackRepository.mapToFeedbackAction(entity)
        assertTrue(action is OfflineBrushingsSyncedFeedback)
    }

    @Test
    fun `mapToFeedbackAction returns StreakCompletedFeedback`() {
        val entity = FeedbackEntity.createStreakCompletedEntity(
            relatedProfileId = 123L,
            streakSmilesEarned = 20,
            historyEventDateTime = ZonedDateTime.now()
        )

        val action = feedbackRepository.mapToFeedbackAction(entity)
        assertTrue(action is StreakCompletedFeedback)
    }

    @Test
    fun `mapToFeedbackAction returns NoSmilesEarnedFeedback`() {
        val entity = FeedbackEntity.createNoSmilesEarnedEntity(
            profileId = 123L,
            historyEventDateTime = ZonedDateTime.now()
        )

        val action = feedbackRepository.mapToFeedbackAction(entity)
        assertTrue(action is NoSmilesEarnedFeedback)
    }

    @Test
    fun `mapToFeedbackAction returns SmilesEarnedFeedback`() {
        val entity = FeedbackEntity.createSmilesEarnedEntity(
            profileId = 123L,
            historyEventDateTime = ZonedDateTime.now(),
            smilesEarned = 23
        )

        val action = feedbackRepository.mapToFeedbackAction(entity)
        assertTrue(action is SmilesEarnedFeedback)
    }

    @Test
    fun `mapToFeedbackAction returns ChallengeCompletedFeedback`() {
        val entity = FeedbackEntity.createChallengeCompletedEntity(
            profileId = 123L,
            historyEventDateTime = ZonedDateTime.now(),
            challengesCompleted = listOf(1L, 2L, 3L)
        )

        val action = feedbackRepository.mapToFeedbackAction(entity)
        assertTrue(action is ChallengeCompletedFeedback)
    }

    @Test
    fun `mapToFeedbackAction returns TierReachedFeedback`() {
        val tierLevel = 3
        val tierEntity = mock<TierEntity>()
        whenever(tiersDao.read(tierLevel)).thenReturn(tierEntity)
        val challenges = listOf(1L, 2L, 3L)
        whenever(completedChallengesProvider.provide(challenges)).thenReturn(emptyList())
        val entity = FeedbackEntity.createTierReachedEntity(
            profileId = 123L,
            historyEventDateTime = ZonedDateTime.now(),
            challengesCompleted = challenges,
            tierReached = tierLevel
        )

        val action = feedbackRepository.mapToFeedbackAction(entity)
        assertTrue(action is TierReachedFeedback)
    }

    @Test
    fun `mapToFeedbackAction returns NoFeedback`() {
        val entity = FeedbackEntity(
            profileId = 123L,
            historyEventDateTime = ZonedDateTime.now(),
            offlineSyncBrushings = -1,
            smilesEarned = 1
        )

        val action = feedbackRepository.mapToFeedbackAction(entity)
        assertTrue(action is NoFeedback)
    }

    @Test
    fun `createChallengeCompletedFeedback invokes completedChallengesProvider`() {
        val challengeId = 9090L
        val id = 8080L
        val entity = FeedbackEntity(
            profileId = 123L,
            historyEventDateTime = ZonedDateTime.now(),
            offlineSyncBrushings = 0,
            smilesEarned = 0,
            challengesCompleted = listOf(challengeId)
        )
        whenever(completedChallengesProvider.provide(listOf(id))).thenReturn(listOf())

        feedbackRepository.createChallengeCompletedFeedback(entity)

        verify(completedChallengesProvider).provide(listOf(challengeId))
    }

    @Test
    fun `createTierReachedFeedback invokes completedChallengesProvider`() {
        val challengeId = 7077L
        val id = 222L
        val tier = 3
        val entity = FeedbackEntity(
            id = id,
            profileId = 135L,
            historyEventDateTime = ZonedDateTime.now(),
            offlineSyncBrushings = 0,
            smilesEarned = 0,
            challengesCompleted = listOf(challengeId),
            tierReached = tier
        )
        whenever(tiersDao.read(tier)).thenReturn(mock())
        whenever(completedChallengesProvider.provide(listOf(id))).thenReturn(emptyList())

        feedbackRepository.createTierReachedFeedback(entity)

        verify(completedChallengesProvider).provide(listOf(challengeId))
    }
}
