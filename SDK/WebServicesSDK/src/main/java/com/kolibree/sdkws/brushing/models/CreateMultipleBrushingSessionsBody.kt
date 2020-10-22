/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.brushing.models

import androidx.annotation.VisibleForTesting
import com.google.gson.annotations.SerializedName
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.sdkws.data.model.CreateBrushingData

/** Request body for multiple brushing session creation at once */
@VisibleForApp
data class CreateMultipleBrushingSessionsBody(

    @VisibleForTesting
    @SerializedName(BRUSHINGS_FIELD)
    val brushingsToCreate: @JvmSuppressWildcards List<CreateBrushingData>
) {

    internal companion object {

        @VisibleForTesting
        const val BRUSHINGS_FIELD = "brushings"
    }
}
