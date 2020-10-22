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
import java.math.RoundingMode
import java.util.Currency
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PriceCreationTest : BaseUnitTest() {

    // region Currencies with different sub units created from integer

    @Test
    fun `10 EUR created from integer is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(10, Currency.getInstance("EUR"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("EUR"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 USD created from integer is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(10, Currency.getInstance("USD"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("USD"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 GBP created from integer is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(10, Currency.getInstance("GBP"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("GBP"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 CHF created from integer is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(10, Currency.getInstance("CHF"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("CHF"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 SEK created from integer is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(10, Currency.getInstance("SEK"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("SEK"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 TND created from integer is kept as 10000 with 3 fraction digits`() {
        val price = Price.create(10, Currency.getInstance("TND"))
        assertEquals(10000, price.amount)
        assertEquals(Currency.getInstance("TND"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(3), price.decimalAmount)
    }

    @Test
    fun `10 JPY created from integer is kept as 10 without fraction digits denomination`() {
        val price = Price.create(10, Currency.getInstance("JPY"))
        assertEquals(10, price.amount)
        assertEquals(Currency.getInstance("JPY"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(0), price.decimalAmount)
    }

    // endregion

    // region Currencies with different sub units created from double

    @Test
    fun `10 EUR created from double is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(10.00, Currency.getInstance("EUR"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("EUR"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 USD created from double is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(10.00, Currency.getInstance("USD"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("USD"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 GBP created from double is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(10, Currency.getInstance("GBP"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("GBP"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 CHF created from double is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(10.00, Currency.getInstance("CHF"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("CHF"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 SEK created from double is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(10.00, Currency.getInstance("SEK"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("SEK"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 TND created from double is kept as 10000 with 3 fraction digits`() {
        val price = Price.create(10.00, Currency.getInstance("TND"))
        assertEquals(10000, price.amount)
        assertEquals(Currency.getInstance("TND"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(3), price.decimalAmount)
    }

    @Test
    fun `10 JPY created from double is kept as 10 without fraction digits denomination`() {
        val price = Price.create(10.00, Currency.getInstance("JPY"))
        assertEquals(10, price.amount)
        assertEquals(Currency.getInstance("JPY"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(0), price.decimalAmount)
    }

    // endregion

    // region Currencies with different sub units created from BigDecimal

    @Test
    fun `10 EUR created from BigDecimal is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(BigDecimal(10), Currency.getInstance("EUR"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("EUR"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 USD created from BigDecimal is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(BigDecimal(10), Currency.getInstance("USD"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("USD"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 GBP created from BigDecimal is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(10, Currency.getInstance("GBP"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("GBP"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 CHF created from BigDecimal is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(BigDecimal(10), Currency.getInstance("CHF"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("CHF"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 SEK created from BigDecimal is kept as 1000 with 2 fraction digits`() {
        val price = Price.create(BigDecimal(10), Currency.getInstance("SEK"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("SEK"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 TND created from BigDecimal is kept as 10000 with 3 fraction digits`() {
        val price = Price.create(BigDecimal(10), Currency.getInstance("TND"))
        assertEquals(10000, price.amount)
        assertEquals(Currency.getInstance("TND"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(3), price.decimalAmount)
    }

    @Test
    fun `10 JPY created from BigDecimal is kept as 10 without fraction digits denomination`() {
        val price = Price.create(BigDecimal(10), Currency.getInstance("JPY"))
        assertEquals(10, price.amount)
        assertEquals(Currency.getInstance("JPY"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(0), price.decimalAmount)
    }

    // endregion

    // region Creation from smile points

    @Test
    fun `10 EUR created from smiles is kept as 1000 with 2 fraction digits`() {
        val price = Price.createFromSmiles(1000, Currency.getInstance("EUR"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("EUR"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 TND created from smiles is kept as 10000 with 3 fraction digits`() {
        val price = Price.createFromSmiles(1000, Currency.getInstance("TND"))
        assertEquals(10000, price.amount)
        assertEquals(Currency.getInstance("TND"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(3), price.decimalAmount)
    }

    @Test
    fun `10 JPY created from smiles is kept as 10 without fraction digits denomination`() {
        val price = Price.createFromSmiles(1000, Currency.getInstance("JPY"))
        assertEquals(10, price.amount)
        assertEquals(Currency.getInstance("JPY"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(0), price.decimalAmount)
    }

    // endregion

    // region Creation from rate

    @Test
    fun `10 EUR created from rate is kept as 1000 with 2 fraction digits`() {
        val price = Price.createFromRate(BigDecimal(10), Currency.getInstance("EUR"))
        assertEquals(1000, price.amount)
        assertEquals(Currency.getInstance("EUR"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `10 TND created from createFromRate is kept as 10000 with 3 fraction digits`() {
        val price = Price.createFromRate(BigDecimal(10), Currency.getInstance("TND"))
        assertEquals(10000, price.amount)
        assertEquals(Currency.getInstance("TND"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(3), price.decimalAmount)
    }

    @Test
    fun `10 JPY created from createFromRate is kept as 10 without fraction digits denomination`() {
        val price = Price.createFromRate(BigDecimal(10), Currency.getInstance("JPY"))
        assertEquals(10, price.amount)
        assertEquals(Currency.getInstance("JPY"), price.currency)
        assertEquals(10.00, price.doubleAmount)
        assertEquals(BigDecimal(10).withFractionDigits(0), price.decimalAmount)
    }

    @Test
    fun `0 EUR created from empty rate is kept as 0 with 2 fraction digits`() {
        val price = Price.createFromRate(null, Currency.getInstance("EUR"))
        assertEquals(0, price.amount)
        assertEquals(Currency.getInstance("EUR"), price.currency)
        assertEquals(0.00, price.doubleAmount)
        assertEquals(BigDecimal(0).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `0 TND created from empty rate is kept as 0 with 3 fraction digits`() {
        val price = Price.createFromRate(null, Currency.getInstance("TND"))
        assertEquals(0, price.amount)
        assertEquals(Currency.getInstance("TND"), price.currency)
        assertEquals(0.00, price.doubleAmount)
        assertEquals(BigDecimal(0).withFractionDigits(3), price.decimalAmount)
    }

    @Test
    fun `0 JPY created from empty rate is kept as 0 without fraction digits denomination`() {
        val price = Price.createFromRate(null, Currency.getInstance("JPY"))
        assertEquals(0, price.amount)
        assertEquals(Currency.getInstance("JPY"), price.currency)
        assertEquals(0.00, price.doubleAmount)
        assertEquals(BigDecimal(0).withFractionDigits(0), price.decimalAmount)
    }

    // endregion

    // region Empty creation

    @Test
    fun `empty EUR is kept as 0 with 2 fraction digits`() {
        val price = Price.empty(Currency.getInstance("EUR"))
        assertEquals(0, price.amount)
        assertEquals(Currency.getInstance("EUR"), price.currency)
        assertEquals(0.00, price.doubleAmount)
        assertEquals(BigDecimal(0).withFractionDigits(2), price.decimalAmount)
    }

    @Test
    fun `empty TND is kept as 0 with 3 fraction digits`() {
        val price = Price.empty(Currency.getInstance("TND"))
        assertEquals(0, price.amount)
        assertEquals(Currency.getInstance("TND"), price.currency)
        assertEquals(0.00, price.doubleAmount)
        assertEquals(BigDecimal(0).withFractionDigits(3), price.decimalAmount)
    }

    @Test
    fun `empty JPY is kept as 0 without fraction digits denomination`() {
        val price = Price.empty(Currency.getInstance("JPY"))
        assertEquals(0, price.amount)
        assertEquals(Currency.getInstance("JPY"), price.currency)
        assertEquals(0.00, price.doubleAmount)
        assertEquals(BigDecimal(0).withFractionDigits(0), price.decimalAmount)
    }

    // endregion
}

private fun BigDecimal.withFractionDigits(fractionDigits: Int): BigDecimal =
    setScale(fractionDigits, RoundingMode.HALF_UP)
