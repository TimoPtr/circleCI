/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.headspace.data.api

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.partnerships.data.api.HEADSPACE_DISCOUNT_CODE
import com.kolibree.android.partnerships.data.api.HEADSPACE_REDEEM_URL
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import junit.framework.TestCase.assertEquals
import org.junit.Test

class HeadspaceApiMapperTest : BaseUnitTest() {

    override fun setup() {
        super.setup()
        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    @Test
    fun `empty payload is mapped to Inactive state`() {
        assertEquals(
            HeadspacePartnershipStatus.Inactive(1),
            HeadspaceApiMapper.apiResponseToStatus(1, emptyMap())
        )
    }

    @Test
    fun `InProgress state is returned for matching data`() {
        assertEquals(
            HeadspacePartnershipStatus.InProgress(1, 10, 100),
            HeadspaceApiMapper.apiResponseToStatus(
                1, mapOf(
                    KEY_POINTS_NEEDED to 10,
                    KEY_POINTS_THRESHOLD to 100
                )
            )
        )
    }

    @Test
    fun `Inactive state is returned for matching data`() {
        assertEquals(
            HeadspacePartnershipStatus.Inactive(1),
            HeadspaceApiMapper.apiResponseToStatus(
                1, mapOf(KEY_STATUS to VALUE_STATUS_INACTIVE)
            )
        )
    }

    @Test
    fun `Unlocked state is returned for matching data`() {
        assertEquals(
            HeadspacePartnershipStatus.Unlocked(1, HEADSPACE_DISCOUNT_CODE, HEADSPACE_REDEEM_URL),
            HeadspaceApiMapper.apiResponseToStatus(
                1, mapOf(
                    KEY_STATUS to VALUE_STATUS_UNLOCKED,
                    KEY_DISCOUNT_CODE to HEADSPACE_DISCOUNT_CODE,
                    KEY_REDEEM_URL to HEADSPACE_REDEEM_URL
                )
            )
        )
    }

    @Test
    fun `Inactive state is returned for non-matching data`() {
        assertEquals(
            HeadspacePartnershipStatus.Inactive(1),
            HeadspaceApiMapper.apiResponseToStatus(
                1, mapOf("random key" to "random value")
            )
        )
    }
}
