/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.inoff.data.api.model

import com.google.gson.annotations.SerializedName

internal data class InOffBrushingsCountResponse(
    @SerializedName("off_total") val offTotal: Int,
    @SerializedName("in_total") val inTotal: Int
)
