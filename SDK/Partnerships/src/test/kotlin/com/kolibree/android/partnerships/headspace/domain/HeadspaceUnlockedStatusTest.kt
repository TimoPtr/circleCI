/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.headspace.domain

import com.kolibree.android.app.test.BaseUnitTest
import java.lang.IllegalArgumentException
import org.junit.Test

class HeadspaceUnlockedStatusTest : BaseUnitTest() {

    @Test(expected = IllegalArgumentException::class)
    fun `discount code cannot be empty`() {
        HeadspacePartnershipStatus.Unlocked(1, discountCode = "", redeemUrl = "https://")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `discount code cannot be blank`() {
        HeadspacePartnershipStatus.Unlocked(1, discountCode = " ", redeemUrl = "https://")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `redeem URL cannot be empty`() {
        HeadspacePartnershipStatus.Unlocked(1, discountCode = "HUM", redeemUrl = "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `redeem URL cannot be blank`() {
        HeadspacePartnershipStatus.Unlocked(1, discountCode = "HUM", redeemUrl = " ")
    }
}
