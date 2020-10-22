/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.persistence

import androidx.room.TypeConverters
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.rewards.models.ChallengeCompletionDetailsConverter
import com.kolibree.android.rewards.models.ChallengeWithProgress
import com.kolibree.android.rewards.synchronization.challengeprogress.ChallengeCompletionDetails
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.ZonedDateTime

/**
 * Class that holds the INNER JOIN between Challenge and ChallengeProgress tables
 *
 * Should be used by direct consumers of ChallengesDao
 */
@TypeConverters(ChallengeProgressZoneDateTimeToStringConverter::class, ChallengeCompletionDetailsConverter::class)
@Parcelize
@VisibleForApp
data class ChallengeWithProgressInternal(
    override val id: Long,
    override val name: String,
    override val description: String,
    override val pictureUrl: String,
    override val category: String,
    override val greetingMessage: String,
    override val smilesReward: Int,
    override val percentage: Int,
    override val completionTime: ZonedDateTime?,
    override val profileId: Long,
    override val action: String?,
    val completionDetails: ChallengeCompletionDetails?
) : ChallengeWithProgress {

    override fun hasRewardSteps(): Boolean {
        val allSteps = completionDetails?.rules ?: 0
        return allSteps > 0
    }

    override fun stepsToReward(): Int {
        val allSteps = completionDetails?.rules ?: 0
        val currentStep = completionDetails?.completion ?: 0
        return allSteps - currentStep
    }

    override fun shouldShowProgress() = completionTime == null && percentage > 0
}
