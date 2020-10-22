/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.rewards.feedback.ChallengeCompletedFeedback
import com.kolibree.android.rewards.feedback.FeedbackAction
import com.kolibree.android.rewards.feedback.FeedbackRepository
import com.kolibree.android.rewards.morewaystoearnpoints.model.CompleteEarnPointsChallenge
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

@VisibleForApp
interface EarnPointsChallengeUseCase {

    fun observeForCompleteChallenges(
        challengesToObserve: List<EarnPointsChallenge.Id> = EarnPointsChallenge.Id.all()
    ): Flowable<List<CompleteEarnPointsChallenge>>

    fun markAsConsumed(
        challenges: List<CompleteEarnPointsChallenge>
    ): Completable
}

internal class EarnPointsChallengeUseCaseImpl @Inject constructor(
    private val currentProfileProvider: CurrentProfileProvider,
    private val feedbackRepository: FeedbackRepository
) : EarnPointsChallengeUseCase {

    override fun observeForCompleteChallenges(
        challengesToObserve: List<EarnPointsChallenge.Id>
    ): Flowable<List<CompleteEarnPointsChallenge>> {
        return currentProfileProvider.currentProfileFlowable()
            .distinctUntilChanged()
            .switchMap { feedbackRepository.feedbackNotConsumedStream(it.id) }
            .map { it.filterChallenges(challengesToObserve) }
            .filter { it.isNotEmpty() }
    }

    override fun markAsConsumed(challenges: List<CompleteEarnPointsChallenge>): Completable {
        val feedbackIds = challenges
            .map(CompleteEarnPointsChallenge::feedbackId)
            .distinct()

        return feedbackRepository.markAsConsumed(feedbackIds)
    }

    private fun List<FeedbackAction>.filterChallenges(
        challengesToObserve: List<EarnPointsChallenge.Id>
    ): List<CompleteEarnPointsChallenge> {
        return filterIsInstance<ChallengeCompletedFeedback>()
            .flatMap { CompleteEarnPointsChallenge.from(it) }
            .filter { challengesToObserve.contains(it.id) }
    }
}
