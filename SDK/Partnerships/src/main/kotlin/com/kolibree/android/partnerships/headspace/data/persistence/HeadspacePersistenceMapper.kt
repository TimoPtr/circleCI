/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.headspace.data.persistence

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.partnerships.data.persistence.PartnershipPersistenceMapper
import com.kolibree.android.partnerships.data.persistence.model.PartnershipEntity
import com.kolibree.android.partnerships.domain.model.PartnershipStatus
import com.kolibree.android.partnerships.headspace.data.persistence.model.HeadspacePartnershipEntity
import com.kolibree.android.partnerships.headspace.data.persistence.model.HeadspacePartnershipEntity.State.INACTIVE
import com.kolibree.android.partnerships.headspace.data.persistence.model.HeadspacePartnershipEntity.State.IN_PROGRESS
import com.kolibree.android.partnerships.headspace.data.persistence.model.HeadspacePartnershipEntity.State.UNLOCKED
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.InProgress
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.Inactive
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.Unlocked

@VisibleForApp
internal object HeadspacePersistenceMapper : PartnershipPersistenceMapper {

    override fun statusToEntity(status: PartnershipStatus): PartnershipEntity = when (status) {
        is InProgress -> inProgressEntity(status)
        is Unlocked -> unlockedEntity(status)
        is Inactive -> inactiveEntity(status)
        else -> {
            FailEarly.fail("$status could not be parsed into valid status")
            inactiveEntity(status)
        }
    }

    private fun inProgressEntity(statusObject: InProgress) =
        HeadspacePartnershipEntity(
            profileId = statusObject.profileId,
            status = IN_PROGRESS,
            pointsNeeded = statusObject.pointsNeeded,
            pointsThreshold = statusObject.pointsThreshold
        )

    private fun unlockedEntity(statusObject: Unlocked) =
        HeadspacePartnershipEntity(
            profileId = statusObject.profileId,
            status = UNLOCKED,
            discountCode = statusObject.discountCode,
            redeemUrl = statusObject.redeemUrl
        )

    private fun inactiveEntity(statusObject: PartnershipStatus) =
        HeadspacePartnershipEntity(
            profileId = statusObject.profileId,
            status = INACTIVE
        )

    override fun entityToStatus(entity: PartnershipEntity): PartnershipStatus = with(entity) {
        when {
            this !is HeadspacePartnershipEntity -> {
                FailEarly.fail("$this is not of type HeadspacePartnershipEntity")
                Inactive(profileId)
            }

            status == INACTIVE -> Inactive(profileId)

            status == UNLOCKED && discountCode != null && redeemUrl != null ->
                Unlocked(profileId, discountCode, redeemUrl)

            status == IN_PROGRESS && pointsNeeded != null && pointsThreshold != null ->
                InProgress(profileId, pointsNeeded, pointsThreshold)

            else -> {
                FailEarly.fail("$this is not valid status")
                Inactive(profileId)
            }
        }
    }
}
