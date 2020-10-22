/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.headspace.data.api

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.partnerships.data.api.PartnershipApiMapper
import com.kolibree.android.partnerships.data.api.getDataValue
import com.kolibree.android.partnerships.data.api.hasData
import com.kolibree.android.partnerships.data.api.model.PartnershipData
import com.kolibree.android.partnerships.domain.model.PartnershipStatus
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.InProgress
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.Inactive
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.Unlocked

const val KEY_POINTS_NEEDED = "points_needed"
const val KEY_POINTS_THRESHOLD = "points_threshold"
const val KEY_DISCOUNT_CODE = "discount_code"
const val KEY_REDEEM_URL = "redeem_url"
const val KEY_STATUS = "status"

const val VALUE_STATUS_UNLOCKED = "active"
const val VALUE_STATUS_INACTIVE = "inactive"

@VisibleForApp
internal object HeadspaceApiMapper : PartnershipApiMapper {

    override fun apiResponseToStatus(
        profileId: Long,
        data: PartnershipData
    ): PartnershipStatus = when {
        data.isEmpty() -> inactive(profileId)
        data.isInProgress() -> inProgress(profileId, data)
        data.isInactive() -> inactive(profileId)
        data.isUnlocked() -> unlocked(profileId, data)
        else -> {
            FailEarly.fail("$data could not be parsed into valid status")
            inactive(profileId)
        }
    }

    private fun inactive(profileId: Long) = Inactive(profileId)

    private fun unlocked(
        profileId: Long,
        data: PartnershipData
    ) = Unlocked(
        profileId,
        data.getDataValue(KEY_DISCOUNT_CODE),
        data.getDataValue(KEY_REDEEM_URL)
    )

    private fun inProgress(
        profileId: Long,
        data: PartnershipData
    ) = InProgress(
        profileId,
        pointsNeeded = data.getDataValue<Number>(KEY_POINTS_NEEDED).toInt(),
        pointsThreshold = data.getDataValue<Number>(KEY_POINTS_THRESHOLD).toInt()
    )
}

private fun PartnershipData.isInProgress(): Boolean =
    hasData<Number>(KEY_POINTS_NEEDED) &&
        hasData<Number>(KEY_POINTS_THRESHOLD)

private fun PartnershipData.isUnlocked(): Boolean =
    hasData<String>(KEY_STATUS) &&
        VALUE_STATUS_UNLOCKED == getDataValue(KEY_STATUS) &&
        hasData<String>(KEY_DISCOUNT_CODE) &&
        hasData<String>(KEY_REDEEM_URL)

private fun PartnershipData.isInactive(): Boolean =
    hasData<String>(KEY_STATUS) &&
        VALUE_STATUS_INACTIVE == getDataValue(KEY_STATUS)
