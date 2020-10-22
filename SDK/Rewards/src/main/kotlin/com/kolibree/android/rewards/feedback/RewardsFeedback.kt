/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.feedback

import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

interface RewardsFeedback {
    fun feedbackAction(profileId: Long): Flowable<FeedbackAction>
    fun markAsRead(action: FeedbackAction): Completable
}

internal class RewardsFeedbackImpl @Inject constructor(private val feedbackRepository: FeedbackRepository) :
    RewardsFeedback {

    override fun markAsRead(action: FeedbackAction): Completable {
        return feedbackRepository.markAsConsumed(listOf(action.id))
    }

    override fun feedbackAction(profileId: Long): Flowable<FeedbackAction> =
        feedbackRepository.feedbackNotConsumedStream(profileId)
            .flatMap { actions ->
                Flowable.fromIterable(actions)
            }.onBackpressureBuffer()
}
