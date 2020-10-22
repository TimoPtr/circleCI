/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.feedback

import androidx.annotation.Keep
import com.kolibree.android.rewards.feedback.personal.ChallengeCompleted
import com.kolibree.android.rewards.models.Tier

@Keep
sealed class FeedbackAction(open val id: Long)

@Keep
object NoFeedback : FeedbackAction(-1)

/**
 * User completed a Brushing but he didn't reach the minimum duration or surface brushing goals, thus he earned 0 smiles
 */
@Keep
data class NoSmilesEarnedFeedback(override val id: Long) : FeedbackAction(id)

/**
 * User completed a Brushing and earned nbOfSmiles
 *
 * This Brushing didn't unlock any Challenge or Tier
 */
@Keep
data class SmilesEarnedFeedback(override val id: Long, val nbOfSmiles: Int) : FeedbackAction(id)

/**
 * User completed a Brushing and unlocked a Challenge, but he didn't reach a new Tier
 *
 * This feedback action contains [1-N] number of challenges
 */
@Keep
data class ChallengeCompletedFeedback(
    override val id: Long,
    val challengesCompleted: List<ChallengeCompleted>
) : FeedbackAction(id)

/**
 * User completed a Brushing and reached a new Tier
 *
 * This feedback action contains [1-N] number of challenges
 */
@Keep
data class TierReachedFeedback(
    override val id: Long,
    val tierReached: Tier,
    val challengesCompleted: List<ChallengeCompleted>
) : FeedbackAction(id)

/**
 * User synchronized at least one offline Brushing
 *
 * This feedback action can contain earned smiles
 */
@Keep
data class OfflineBrushingsSyncedFeedback(
    override val id: Long,
    val offlineBrushings: Int,
    val earnedSmiles: Int
) : FeedbackAction(id)

/**
 * Week streak
 *
 * This feedback action contains earned smiles
 */
@Keep
data class StreakCompletedFeedback(
    override val id: Long,
    val earnedSmiles: Int
) : FeedbackAction(id)
