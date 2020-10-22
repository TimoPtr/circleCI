/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.repo.mapper

import com.kolibree.android.app.ui.brushhead.api.model.request.BrushHeadInformationResponse
import com.kolibree.android.app.ui.brushhead.repo.model.BrushHeadInformation

internal object BrushHeadInformationResponseMapper {
    fun map(
        macAddress: String,
        response: BrushHeadInformationResponse
    ): BrushHeadInformation {
        return BrushHeadInformation(
            macAddress = macAddress,
            resetDate = response.firstUsed,
            percentageLeft = response.percentageLeft
        )
    }
}
