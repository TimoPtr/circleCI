/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.domain.model

import com.kolibree.android.app.test.BaseUnitTest
import java.math.BigDecimal
import java.util.Currency
import org.junit.Assert.assertEquals
import org.junit.Test

class RatesTest : BaseUnitTest() {

    @Test
    fun `rates total should send the correct value`() {

        val rates = Taxes(
            Price.create(BigDecimal(12.12), Currency.getInstance("EUR")),
            ShippingRate(Price.create(BigDecimal(20), Currency.getInstance("EUR")), "handle")
        )

        assertEquals(Price.create(BigDecimal(32.12), Currency.getInstance("EUR")), rates.total)
    }
}
