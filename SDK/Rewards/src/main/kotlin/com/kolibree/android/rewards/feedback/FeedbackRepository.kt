/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.feedback

import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.rewards.persistence.FeedbackDao
import com.kolibree.android.rewards.persistence.TiersDao
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

@VisibleForApp
interface FeedbackRepository {

    /**
     * Flag the given action to consumed so next time we will subscribe to
     * the stream we will not get this action
     *
     * @param feedbackIds list of [FeedbackAction.id]
     */
    fun markAsConsumed(feedbackIds: List<Long>): Completable

    /**
     * Return a stream of not consumed action
     */
    fun feedbackNotConsumedStream(profileId: Long): Flowable<List<FeedbackAction>>
}

internal class FeedbackRepositoryImpl @Inject constructor(
    private val feedbackDao: FeedbackDao,
    private val tiersDao: TiersDao,
    private val completedChallengesProvider: CompletedChallengesProvider
) : FeedbackRepository {

    override fun markAsConsumed(feedbackIds: List<Long>): Completable {
        return feedbackDao.markAsConsumed(feedbackIds)
    }

    override fun feedbackNotConsumedStream(profileId: Long): Flowable<List<FeedbackAction>> =
        feedbackDao.oldestFeedbackStream(profileId).map { feedBackEntities ->
            feedBackEntities.map { mapToFeedbackAction(it) }
        }

    @VisibleForTesting
    fun mapToFeedbackAction(feedbackEntity: FeedbackEntity): FeedbackAction = when {
        feedbackEntity.isOfflineSync() -> createOfflineBrushingsSyncedFeedback(feedbackEntity)
        feedbackEntity.isStreakCompleted() -> createStreakCompletedFeedback(feedbackEntity)
        feedbackEntity.isNoSmilesEarned() -> NoSmilesEarnedFeedback(feedbackEntity.id)
        feedbackEntity.isSmilesEarned() -> SmilesEarnedFeedback(
            feedbackEntity.id,
            feedbackEntity.smilesEarned
        )
        feedbackEntity.isChallengesCompleted() -> createChallengeCompletedFeedback(feedbackEntity)
        feedbackEntity.isTierReached() -> createTierReachedFeedback(feedbackEntity)
        else -> NoFeedback
    }

    @VisibleForTesting
    fun createChallengeCompletedFeedback(feedbackEntity: FeedbackEntity): ChallengeCompletedFeedback {
        return ChallengeCompletedFeedback(
            feedbackEntity.id,
            completedChallengesProvider.provide(feedbackEntity.challengesCompleted)
        )
    }

    @VisibleForTesting
    fun createTierReachedFeedback(feedbackEntity: FeedbackEntity): TierReachedFeedback {
        return TierReachedFeedback(
            feedbackEntity.id,
            tiersDao.read(feedbackEntity.tierReached),
            completedChallengesProvider.provide(feedbackEntity.challengesCompleted)
        )
    }

    private fun createStreakCompletedFeedback(feedbackEntity: FeedbackEntity): StreakCompletedFeedback {
        return StreakCompletedFeedback(
            feedbackEntity.id,
            feedbackEntity.streakSmilesEarned
        )
    }

    private fun createOfflineBrushingsSyncedFeedback(feedbackEntity: FeedbackEntity): OfflineBrushingsSyncedFeedback {
        return OfflineBrushingsSyncedFeedback(
            feedbackEntity.id,
            feedbackEntity.offlineSyncBrushings,
            feedbackEntity.smilesEarned
        )
    }
}
