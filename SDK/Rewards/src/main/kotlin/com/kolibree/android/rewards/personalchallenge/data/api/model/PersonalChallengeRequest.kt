/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.data.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringify
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringifyDuration
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringifyUnit
import com.kolibree.android.rewards.personalchallenge.domain.model.V1PersonalChallenge

@Keep
internal data class PersonalChallengeRequest(
    @SerializedName("objective") val objective: String,
    @SerializedName("level") val level: String,
    @SerializedName("duration_unit") val duration: Long,
    @SerializedName("duration_period") val durationUnit: String
)

internal fun V1PersonalChallenge.toApiRequest() =
    PersonalChallengeRequest(
        objective = objectiveType.stringify(),
        level = difficultyLevel.stringify(),
        duration = period.stringifyDuration(),
        durationUnit = period.stringifyUnit()
    )
