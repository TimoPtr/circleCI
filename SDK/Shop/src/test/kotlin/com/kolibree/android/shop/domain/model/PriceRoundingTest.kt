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
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PriceRoundingTest : BaseUnitTest() {

    // region Prices with fraction digits created from double

    @Test
    fun `price with fraction digits created from double is rounded to subunit`() {
        assertEquals(
            10.23,
            Price.create(10.231, Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.23,
            Price.create(10.234, Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.24,
            Price.create(10.235, Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.24,
            Price.create(10.239, Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.5,
            Price.create(10.5, Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.51,
            Price.create(10.511, Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.59,
            Price.create(10.594, Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.6,
            Price.create(10.595, Currency.getInstance("EUR")).doubleAmount
        )
    }

    @Test
    fun `price with fraction digits created from double with long fraction part is rounded to subunit`() {
        assertEquals(
            10.0,
            Price.create(10.000000000000000000001, Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.5,
            Price.create(10.5000000000001, Currency.getInstance("EUR")).doubleAmount
        )
    }

    // endregion

    // region Prices with fraction digits created from BigDecimal

    @Test
    fun `price with fraction digits created from BigDecimal is rounded to subunit`() {
        assertEquals(
            10.23,
            Price.create(BigDecimal(10.231), Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.23,
            Price.create(BigDecimal(10.234), Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.24,
            Price.create(BigDecimal(10.235), Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.24,
            Price.create(BigDecimal(10.239), Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.5,
            Price.create(BigDecimal(10.5), Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.51,
            Price.create(BigDecimal(10.511), Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.59,
            Price.create(BigDecimal(10.594), Currency.getInstance("EUR")).doubleAmount
        )
        assertEquals(
            10.6,
            Price.create(BigDecimal(10.595), Currency.getInstance("EUR")).doubleAmount
        )
    }

    @Test
    fun `price with fraction digits created from BigDecimal with long fraction part is rounded to subunit`() {
        assertEquals(
            10.0,
            Price.create(
                BigDecimal(10.000000000000000000001),
                Currency.getInstance("EUR")
            ).doubleAmount
        )
        assertEquals(
            10.5,
            Price.create(BigDecimal(10.5000000000001), Currency.getInstance("EUR")).doubleAmount
        )
    }

    // endregion

    // region Prices without fraction digits created from double

    @Test
    fun `price without fraction digits created from double is rounded to unit`() {
        assertEquals(
            10.0,
            Price.create(10.231, Currency.getInstance("JPY")).doubleAmount
        )
        assertEquals(
            10.0,
            Price.create(10.234, Currency.getInstance("JPY")).doubleAmount
        )
        assertEquals(
            10.0,
            Price.create(10.235, Currency.getInstance("JPY")).doubleAmount
        )
        assertEquals(
            10.0,
            Price.create(10.239, Currency.getInstance("JPY")).doubleAmount
        )
        assertEquals(
            10.0,
            Price.create(10.5, Currency.getInstance("JPY")).doubleAmount
        )
        assertEquals(
            11.0,
            Price.create(10.50001, Currency.getInstance("JPY")).doubleAmount
        )
        assertEquals(
            11.0,
            Price.create(10.999999, Currency.getInstance("JPY")).doubleAmount
        )
    }

    @Test
    fun `price without fraction digits created from double with long fraction part is rounded to unit`() {
        assertEquals(
            10.0,
            Price.create(10.000000000000000000001, Currency.getInstance("JPY")).doubleAmount
        )
        assertEquals(
            11.0,
            Price.create(10.5000000000001, Currency.getInstance("JPY")).doubleAmount
        )
    }

    @Test
    fun `price smiles should be rounded according to the amount provided`() {

        with(Price.create(279.96_999999, Currency.getInstance("EUR"))) {
            assertEquals(27997, smilePoints)
            assertEquals(279.97, doubleAmount)
        }

        with(Price.create(279.96, Currency.getInstance("EUR"))) {
            assertEquals(27996, smilePoints)
            assertEquals(279.96, doubleAmount)
        }

        with(Price(6999, Currency.getInstance("EUR")) * 4) {
            assertEquals(27996, smilePoints)
            assertEquals(279.96, doubleAmount)
        }

        with(Price(6999, Currency.getInstance("EUR"))) {
            assertEquals(6999, smilePoints)
            assertEquals(69.99, doubleAmount)
        }

        with(Price(1000, Currency.getInstance("EUR"))) {
            assertEquals(1000, smilePoints)
            assertEquals(10.00, doubleAmount)
        }

        with(Price(1, Currency.getInstance("EUR"))) {
            assertEquals(1, smilePoints)
            assertEquals(0.01, doubleAmount)
        }
    }

    // endregion
}
