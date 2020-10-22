/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.data.api.model.request

import com.google.gson.annotations.SerializedName
import org.threeten.bp.OffsetDateTime

internal data class AnswerQuestionRequest(
    @SerializedName("id") val id: Long,
    @SerializedName("answer_id") val answerId: Long,
    @SerializedName("answered_at") val answeredAt: OffsetDateTime
)
