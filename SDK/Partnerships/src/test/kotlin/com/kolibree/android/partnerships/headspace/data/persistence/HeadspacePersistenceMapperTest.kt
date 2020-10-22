/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.headspace.data.persistence

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.partnerships.data.api.get.success.VALUE_DISCOUNT_CODE
import com.kolibree.android.partnerships.data.api.get.success.VALUE_REDEEM_URL
import com.kolibree.android.partnerships.data.persistence.model.PartnershipEntity
import com.kolibree.android.partnerships.domain.model.Partner
import com.kolibree.android.partnerships.domain.model.PartnershipStatus
import com.kolibree.android.partnerships.headspace.data.persistence.model.HeadspacePartnershipEntity
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import junit.framework.TestCase.assertEquals
import org.junit.Test

class HeadspacePersistenceMapperTest : BaseUnitTest() {

    override fun setup() {
        super.setup()
        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    @Test
    fun `InProgress status is mapped to corresponding entity and vice versa`() {
        val status = HeadspacePartnershipStatus.InProgress(
            1,
            pointsNeeded = 10,
            pointsThreshold = 100
        )
        val entity = HeadspacePartnershipEntity(
            1,
            HeadspacePartnershipEntity.State.IN_PROGRESS,
            pointsNeeded = 10,
            pointsThreshold = 100
        )
        assertEquals(status, HeadspacePersistenceMapper.entityToStatus(entity))
        assertEquals(entity, HeadspacePersistenceMapper.statusToEntity(status))
    }

    @Test
    fun `Unlocked status is mapped to corresponding entity and vice versa`() {
        val status = HeadspacePartnershipStatus.Unlocked(
            1,
            discountCode = VALUE_DISCOUNT_CODE,
            redeemUrl = VALUE_REDEEM_URL
        )
        val entity = HeadspacePartnershipEntity(
            1,
            HeadspacePartnershipEntity.State.UNLOCKED,
            discountCode = VALUE_DISCOUNT_CODE,
            redeemUrl = VALUE_REDEEM_URL
        )

        assertEquals(status, HeadspacePersistenceMapper.entityToStatus(entity))
        assertEquals(entity, HeadspacePersistenceMapper.statusToEntity(status))
    }

    @Test
    fun `Inactive status is mapped to corresponding entity and vice versa`() {
        val status = HeadspacePartnershipStatus.Inactive(1)
        val entity = HeadspacePartnershipEntity(
            1,
            HeadspacePartnershipEntity.State.INACTIVE
        )

        assertEquals(status, HeadspacePersistenceMapper.entityToStatus(entity))
        assertEquals(entity, HeadspacePersistenceMapper.statusToEntity(status))
    }

    @Test
    fun `Unsupported status is mapped to Inactive entity`() {
        assertEquals(
            HeadspacePartnershipEntity(
                1,
                HeadspacePartnershipEntity.State.INACTIVE
            ),
            HeadspacePersistenceMapper.statusToEntity(
                object : PartnershipStatus {

                    override val profileId = 1L
                    override val partner = Partner.HEADSPACE
                }
            )
        )
    }

    @Test
    fun `Unsupported entity is mapped to Inactive entity`() {
        assertEquals(
            HeadspacePartnershipStatus.Inactive(1),
            HeadspacePersistenceMapper.entityToStatus(
                object : PartnershipEntity {

                    override val profileId = 1L
                }
            )
        )
    }

    @Test
    fun `Incorrect entity payload is mapped to Inactive entity`() {
        assertEquals(
            HeadspacePartnershipStatus.Inactive(1),
            HeadspacePersistenceMapper.entityToStatus(
                HeadspacePartnershipEntity(
                    1,
                    HeadspacePartnershipEntity.State.UNLOCKED,
                    discountCode = VALUE_DISCOUNT_CODE
                )
            )
        )
    }
}
