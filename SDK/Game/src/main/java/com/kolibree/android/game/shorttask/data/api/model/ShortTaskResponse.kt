/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.shorttask.data.api.model

import com.google.gson.annotations.SerializedName
import org.threeten.bp.OffsetDateTime

internal data class ShortTaskResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("category") val category: String,
    @SerializedName("created_at") val createdAt: OffsetDateTime
)
