/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.repo.model

import androidx.annotation.IntRange
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.defensive.Preconditions
import org.threeten.bp.OffsetDateTime

@VisibleForApp
data class BrushHeadInformation(
    val macAddress: String,
    val resetDate: OffsetDateTime,
    /**
     * 100=new brush head
     */
    @IntRange(from = 0, to = NEW_TOOTHBRUSH_PERCENTAGE.toLong()) val percentageLeft: Int
) {
    init {
        Preconditions.checkArgumentInRange(
            percentageLeft,
            0,
            NEW_TOOTHBRUSH_PERCENTAGE,
            "Percentage has to be in range [0, $NEW_TOOTHBRUSH_PERCENTAGE] (got $percentageLeft)"
        )
    }

    internal companion object {
        fun newBrushHead(mac: String) = BrushHeadInformation(
            macAddress = mac,
            resetDate = TrustedClock.getNowOffsetDateTime(),
            percentageLeft = NEW_TOOTHBRUSH_PERCENTAGE
        )

        internal const val NEW_TOOTHBRUSH_PERCENTAGE = 100
    }
}
