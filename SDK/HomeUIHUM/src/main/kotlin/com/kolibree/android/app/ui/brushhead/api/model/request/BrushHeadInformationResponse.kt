/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.api.model.request

import androidx.annotation.IntRange
import com.google.gson.annotations.SerializedName
import com.kolibree.android.annotation.VisibleForApp
import org.threeten.bp.OffsetDateTime

@VisibleForApp
data class BrushHeadInformationResponse(
    @SerializedName("first_used") val firstUsed: OffsetDateTime,
    @SerializedName("number_of_uses") val numberOfUses: Int,
    @SerializedName("days_since_last_change") val daysSinceLastChange: Int,
    @SerializedName("total_brushing_time") val totalBrushingTime: Int,
    /**
     * 100=new brush head
     */
    @SerializedName("percentage") @IntRange(from = 0, to = 100) val percentageLeft: Int
)
