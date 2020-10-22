/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.morewaystoearnpoints.model

import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.rewards.feedback.ChallengeCompletedFeedback
import com.kolibree.android.rewards.feedback.FeedbackAction
import com.kolibree.android.rewards.feedback.personal.BackendChallengeCompleted
import kotlinx.android.parcel.Parcelize

@VisibleForApp
@Parcelize
data class EarnPointsChallenge(
    val id: Id,
    val points: Int
) : Parcelable {

    /**
     * All types of `More ways to earn points` challenges.
     * Order of this enum determines priority on the card list
     * (cards with lower ordinal will be displayed first).
     */
    @VisibleForApp
    enum class Id(val backendId: Long) {
        COMPLETE_YOUR_PROFILE(backendId = 64),
        TURN_ON_EMAIL_NOTIFICATIONS(backendId = 65),
        TURN_ON_BRUSH_SYNC_REMINDERS(backendId = 63),
        TURN_ON_BRUSHING_REMINDERS(backendId = 62),
        RATE_THE_APP(backendId = 53),
        SUBSCRIBE_FOR_WEEKLY_REVIEW(backendId = 66),
        REFER_A_FRIEND(backendId = 44),
        AMAZON_DASH(backendId = 67);

        @VisibleForApp
        companion object {
            fun all() = values().toList()
        }
    }
}

/**
 * Links [EarnPointsChallenge] with corresponding [FeedbackAction]
 *
 * @param challenge [EarnPointsChallenge]
 * @param feedbackId [FeedbackAction.id]
 */
@Parcelize
@VisibleForApp
data class CompleteEarnPointsChallenge(
    val challenge: EarnPointsChallenge,
    val feedbackId: Long
) : Parcelable {

    val id: EarnPointsChallenge.Id
        get() = challenge.id

    internal companion object {

        fun from(feedback: ChallengeCompletedFeedback): List<CompleteEarnPointsChallenge> {
            return feedback.challengesCompleted.mapNotNull { completedChallenge ->
                (completedChallenge as? BackendChallengeCompleted)
                    ?.toEarnPointsChallenge()
                    ?.let { challenge -> CompleteEarnPointsChallenge(challenge, feedback.id) }
            }
        }

        private fun BackendChallengeCompleted.toEarnPointsChallenge(): EarnPointsChallenge? {
            return EarnPointsChallenge.Id
                .values()
                .find { it.backendId == id }
                ?.let { earnPointsId ->
                    EarnPointsChallenge(
                        id = earnPointsId,
                        points = smilesReward
                    )
                }
        }
    }
}

internal fun List<EarnPointsChallenge>.sorted(): List<EarnPointsChallenge> =
    sortedBy { it.id.ordinal }
