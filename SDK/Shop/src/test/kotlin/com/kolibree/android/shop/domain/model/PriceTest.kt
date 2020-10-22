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
import com.kolibree.android.shop.domain.model.Price.Companion.SMILE_POINTS_PER_SINGLE_CURRENCY_UNIT
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Currency
import java.util.Locale
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PriceTest : BaseUnitTest() {

    @Test
    fun `doubleAmount converts decimal to double`() {
        assertEquals(1234.99, euroPrice.doubleAmount)
    }

    @Test
    fun `smilePoints returns amount multiplied by 100`() {
        assertEquals(123499, euroPrice.smilePoints)
    }

    @Test
    fun `formattedPrice applies default locale-based currency formatting`() {
        Locale.setDefault(Locale.US)
        assertEquals("EUR1,234.99", euroPrice.formattedPrice())
        assertEquals("$1,234.00", dollarPrice.formattedPrice())

        Locale.setDefault(Locale.FRANCE)
        assertEquals("1 234,99 €", euroPrice.formattedPrice())
        assertEquals("1 234,00 USD", dollarPrice.formattedPrice())
    }

    // The purpose of this test is to check that all properties of custom format are preserved,
    // even if it shows pounds instead of euros.
    @Test
    fun `formattedPrice applies custom, locale-agnostic format if provided`() {
        val forcedFormat = DecimalFormat(
            "¤######.##",
            DecimalFormatSymbols(Locale.UK).also {
                it.currency = Currency.getInstance("GBP")
                it.currencySymbol = "£"
                it.decimalSeparator = '.'
                it.monetaryDecimalSeparator = '.'
            }
        ).also {
            it.maximumFractionDigits = 2
            it.minimumIntegerDigits = 2
            it.minimumFractionDigits = 2
        }

        Locale.setDefault(Locale.US)
        assertEquals("£1234.99", euroPrice.formattedPrice(forcedFormat))
        assertEquals("£1234.00", dollarPrice.formattedPrice(forcedFormat))

        Locale.setDefault(Locale.FRANCE)
        assertEquals("£1234.99", euroPrice.formattedPrice(forcedFormat))
        assertEquals("£1234.00", dollarPrice.formattedPrice(forcedFormat))
    }

    @Test
    fun `formattedPrice fixes CHF currency symbol for Swiss locale while it keeps it intact for other locales`() {
        assertEquals("CHF1,234.00", swissFrancPrice.formattedPrice(Locale.US))
        assertEquals("1 234,00 CHF", swissFrancPrice.formattedPrice(Locale.FRANCE))
        Locale.setDefault(Locale.forLanguageTag("de-CH"))
        // ' is not there by mistake, Swiss currency format is #'###.##
        // See https://www.thefinancials.com/Default.aspx?SubSectionID=curformat
        assertEquals("Fr. 1'234.00", swissFrancPrice.formattedPrice())
    }

    /*
    Add
     */
    @Test
    fun `two Price objects can be added together`() {
        val first = Price.create(BigDecimal("10923"), euroCurrency)
        val second = Price.create(BigDecimal("1184"), euroCurrency)
        val expectedPrice = Price.create(BigDecimal("12107"), euroCurrency)
        assertEquals(expectedPrice, first + second)
        assertEquals(first + second, second + first)
    }

    @Test
    fun `adding two prices doesn't cause float drifting`() {
        val first = Price.create(BigDecimal("10.12400000000032323"), euroCurrency)
        val second = Price.create(BigDecimal("2.99000000000000033"), euroCurrency)
        val expectedPrice = Price.create(BigDecimal("13.11"), euroCurrency)
        assertEquals(expectedPrice, first + second)
        assertEquals(first + second, second + first)
    }

    @Test(expected = IllegalStateException::class)
    fun `add throws exception if currency is not the same`() {
        val first = Price.create(BigDecimal("1000"), euroCurrency)
        val second = Price.create(BigDecimal("3099"), usdCurrency)
        first + second
    }

    /*
    Minus
     */
    @Test
    fun `two Price objects can be substracted together`() {
        val first = Price.create(BigDecimal("10923"), euroCurrency)
        val second = Price.create(BigDecimal("1184"), euroCurrency)
        val expectedPrice = Price.create(BigDecimal("9739"), euroCurrency)
        assertEquals(expectedPrice, first - second)
    }

    @Test
    fun `substracting two prices doesn't cause float drifting`() {
        val first = Price.create(BigDecimal("10.12400000000032323"), euroCurrency)
        val second = Price.create(BigDecimal("2.99000000000000033"), euroCurrency)
        val expectedPrice = Price.create(BigDecimal("7.13"), euroCurrency)
        assertEquals(expectedPrice, first - second)
        assertEquals(first + second, second + first)
    }

    @Test(expected = IllegalStateException::class)
    fun `minus throws exception if currency is not the same`() {
        val first = Price.create(BigDecimal("1000"), euroCurrency)
        val second = Price.create(BigDecimal("3099"), usdCurrency)
        first - second
    }

    /*
    Times
     */
    @Test
    fun `Price objects can be multiple by scalar`() {
        val price = Price.create(BigDecimal("2333"), euroCurrency)
        val expectedPrice = Price.create(BigDecimal("6999"), euroCurrency)
        assertEquals(expectedPrice, price * 3)
    }

    /*
    createFromSmiles
     */

    @Test
    fun `createFromSmiles creates Price with expected data`() {
        val expectedSmilesCount = 2020
        val expectedCurrency = Currency.getInstance(Locale.US)
        val storeDetails = mock<StoreDetails>()
        whenever(storeDetails.currency).thenReturn(expectedCurrency)

        val expectedAmount = expectedSmilesCount.toDouble() / SMILE_POINTS_PER_SINGLE_CURRENCY_UNIT

        val price = Price.createFromSmiles(expectedSmilesCount, storeDetails)

        assertEquals(expectedCurrency, price.currency)
        assertEquals(expectedAmount, price.doubleAmount)
        assertEquals(
            BigDecimal.valueOf(expectedAmount).setScale(2, RoundingMode.HALF_UP),
            price.decimalAmount
        )
        assertEquals(expectedSmilesCount, price.smilePoints)
    }

    @Test
    fun `create from rate configure the correct price`() {
        val expectedAmount = 20.0

        val price = Price.createFromRate(BigDecimal(expectedAmount), euroCurrency)

        assertEquals(euroCurrency, price.currency)
        assertEquals(expectedAmount, price.doubleAmount)
    }

    @Test
    fun `create empty price configure it correctly`() {

        val price = Price.empty(euroCurrency)

        assertEquals(euroCurrency, price.currency)
        assertEquals(0.0, price.doubleAmount)
    }

    companion object {
        val euroCurrency = Currency.getInstance("EUR")
        val usdCurrency = Currency.getInstance("USD")

        val euroPrice = Price.create(BigDecimal.valueOf(1234.99), Currency.getInstance("EUR"))
        val dollarPrice = Price.create(BigDecimal.valueOf(1234.0), Currency.getInstance("USD"))
        val swissFrancPrice = Price.create(BigDecimal.valueOf(1234.0), Currency.getInstance("CHF"))
    }
}
