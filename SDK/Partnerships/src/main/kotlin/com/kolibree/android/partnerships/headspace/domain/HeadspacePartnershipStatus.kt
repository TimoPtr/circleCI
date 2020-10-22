/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.headspace.domain

import androidx.annotation.IntRange
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.defensive.Preconditions
import com.kolibree.android.partnerships.domain.model.Partner
import com.kolibree.android.partnerships.domain.model.PartnershipStatus

@VisibleForApp
sealed class HeadspacePartnershipStatus(
    override val profileId: Long,
    override val partner: Partner = Partner.HEADSPACE
) : PartnershipStatus {

    @VisibleForApp
    data class InProgress(
        override val profileId: Long,
        val pointsNeeded: Int,
        val pointsThreshold: Int
    ) : HeadspacePartnershipStatus(profileId) {

        init {
            Preconditions.checkArgumentPositive(
                pointsThreshold,
                "Threshold has to be a positive value (got $pointsThreshold)"
            )
            Preconditions.checkArgumentInRange(
                pointsNeeded,
                MIN_PROGRESS,
                pointsThreshold,
                "Points have to be in range [0, $pointsThreshold] (got $pointsNeeded)"
            )
        }

        @IntRange(from = MIN_PROGRESS.toLong(), to = MAX_PROGRESS.toLong())
        val progress: Int = (pointsThreshold - pointsNeeded) * MAX_PROGRESS / pointsThreshold

        val readyToBeUnlocked: Boolean = progress == MAX_PROGRESS

        @VisibleForApp
        companion object {
            const val MIN_PROGRESS = 0
            const val MAX_PROGRESS = 100
        }
    }

    @VisibleForApp
    data class Unlocked(
        override val profileId: Long,
        val discountCode: String,
        val redeemUrl: String
    ) : HeadspacePartnershipStatus(profileId) {

        init {
            Preconditions.checkArgument(
                discountCode.isNotBlank(),
                "Discount code cannot be blank"
            )
            Preconditions.checkArgument(
                redeemUrl.isNotBlank(),
                "Redeem URL cannot be blank"
            )
        }
    }

    @VisibleForApp
    data class Inactive(
        override val profileId: Long
    ) : HeadspacePartnershipStatus(profileId)
}
