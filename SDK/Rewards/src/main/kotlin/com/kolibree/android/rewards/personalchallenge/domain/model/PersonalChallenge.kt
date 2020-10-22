/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.domain.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.kolibree.android.annotation.VisibleForApp
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.ZonedDateTime

/**
 * Common base for all challenges.
 */
@Keep
sealed class PersonalChallenge : Parcelable

/**
 * V1 challenges, defined as combination of:
 * - type of objective to achieve
 * - level of difficulty
 * - amount of time needed to complete the challenge
 *
 * @see https://kolibree.atlassian.net/wiki/spaces/PROD/pages/30998556/Personal+Challenge
 */
@Parcelize
@VisibleForApp
data class V1PersonalChallenge(
    val objectiveType: PersonalChallengeType,
    val difficultyLevel: PersonalChallengeLevel,
    val period: PersonalChallengePeriod,
    val creationDate: ZonedDateTime,
    val completionDate: ZonedDateTime?,
    val progress: Int
) : PersonalChallenge() {

    @IgnoredOnParcel
    val completed = completionDate != null

    fun hasSameParams(other: V1PersonalChallenge?): Boolean =
        other != null &&
            objectiveType == other.objectiveType &&
            difficultyLevel == other.difficultyLevel &&
            period == other.period
}
