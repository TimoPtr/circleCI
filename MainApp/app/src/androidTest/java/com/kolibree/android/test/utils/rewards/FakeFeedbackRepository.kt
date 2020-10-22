/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils.rewards

import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.rewards.feedback.FeedbackAction
import com.kolibree.android.rewards.feedback.FeedbackRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable

class FakeFeedbackRepository : FeedbackRepository {

    private val items = BehaviorRelay.create<List<FeedbackAction>>()

    fun mock(feedbackActions: List<FeedbackAction>) {
        items.accept(feedbackActions)
    }

    fun mock(vararg feedbackActions: FeedbackAction) {
        items.accept(feedbackActions.toList())
    }

    override fun markAsConsumed(feedbackIds: List<Long>): Completable {
        return Completable.fromCallable {
            items.value
                ?.filterNot { feedbackIds.contains(it.id) }
                ?.let(items::accept)
        }
    }

    override fun feedbackNotConsumedStream(profileId: Long): Flowable<List<FeedbackAction>> {
        return items.toFlowable(BackpressureStrategy.LATEST)
    }
}
