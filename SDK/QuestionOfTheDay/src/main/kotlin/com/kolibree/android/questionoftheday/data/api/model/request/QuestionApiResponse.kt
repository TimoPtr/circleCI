/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.data.api.model.request

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.kolibree.android.commons.gson.OffsetDateTimeTypeAdapter
import org.threeten.bp.OffsetDateTime

internal data class QuestionApiResponse(
    @SerializedName("answers") val answers: List<AnswerApiResponse>,
    @SerializedName("correct") val correct: Long,
    @SerializedName("id") val id: Long,
    @SerializedName("text") val text: String,
    @SerializedName("user_response") val userResponse: UserResponse?
)

internal data class AnswerApiResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("text") val text: String
)

internal data class UserResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("answer_id") val answerId: Long,
    @SerializedName("answered_at")
    @JsonAdapter(OffsetDateTimeTypeAdapter::class) val answeredAt: OffsetDateTime
)
