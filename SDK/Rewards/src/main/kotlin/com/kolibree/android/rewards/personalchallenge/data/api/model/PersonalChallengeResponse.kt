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
import com.kolibree.android.rewards.personalchallenge.data.mapper.levelFromStringedValue
import com.kolibree.android.rewards.personalchallenge.data.mapper.objectiveFromJsonString
import com.kolibree.android.rewards.personalchallenge.data.mapper.periodFromStringedData
import com.kolibree.android.rewards.personalchallenge.domain.model.V1PersonalChallenge
import org.threeten.bp.ZonedDateTime

@Keep
internal data class PersonalChallengeResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("created_at") val createdAt: ZonedDateTime,
    @SerializedName("objective") val objective: String,
    @SerializedName("level") val level: String,
    @SerializedName("duration_unit") val duration: Long,
    @SerializedName("duration_period") val durationUnit: String,
    @SerializedName("progress") val progress: Int,
    @SerializedName("completed_at") val completedAt: ZonedDateTime?
) {

    internal fun toV1Challenge(): V1PersonalChallenge = V1PersonalChallenge(
        objectiveType = objectiveFromJsonString(objective),
        difficultyLevel = levelFromStringedValue(level),
        period = periodFromStringedData(duration, durationUnit),
        progress = progress,
        creationDate = createdAt,
        completionDate = completedAt
    )
}
