/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.feedback.personal

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.kolibree.android.rewards.models.Challenge
import kotlinx.android.parcel.Parcelize

@Keep
sealed class ChallengeCompleted(
    open val smilesReward: Int
) : Parcelable

@Keep
@Parcelize
data class BackendChallengeCompleted(
    override val id: Long,
    override val name: String,
    override val category: String,
    override val greetingMessage: String,
    override val description: String,
    override val pictureUrl: String,
    override val smilesReward: Int,
    override val action: String?
) : ChallengeCompleted(smilesReward), Challenge {
    constructor(challenge: Challenge) : this(
        challenge.id,
        challenge.name,
        challenge.category,
        challenge.greetingMessage,
        challenge.description,
        challenge.pictureUrl,
        challenge.smilesReward,
        challenge.action
    )
}

@Keep
@Parcelize
data class PersonalChallengeCompleted(
    val id: Long,
    override val smilesReward: Int,
    @StringRes val nameRes: Int,
    @StringRes val greetingMessageRes: Int
) : ChallengeCompleted(smilesReward)
