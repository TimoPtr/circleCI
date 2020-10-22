/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.domain.model

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Format
import java.util.Currency
import java.util.Locale
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 * Representation of the product price, with conversion and formatting capabilities.
 *
 * @param amount integer representation of the amount without denomination,
 *  for ex. EUR 20.20 is represented as 2020 (cause EUR has 2 fraction digits)
 * @param currency [Currency]
 */
@Parcelize
class Price @VisibleForTesting constructor(
    @VisibleForTesting val amount: Int,
    val currency: Currency
) : Parcelable {

    @IgnoredOnParcel
    val doubleAmount: Double = intToDenominatedDouble(amount, currency)

    @IgnoredOnParcel
    val decimalAmount: BigDecimal = intToDenominatedBigDecimal(amount, currency)

    @IgnoredOnParcel
    val smilePoints: Int = round(doubleAmount * SMILE_POINTS_PER_SINGLE_CURRENCY_UNIT).toInt()

    /**
     * TODO extract this to dedicated formatting object
     * Format the price, taking locale & currency under consideration.
     * @return formatted price string
     */
    fun formattedPrice(locale: Locale = Locale.getDefault()): String {
        val formatter = getDefaultFormatterInstance(locale)
        formatter.currency = currency
        maybeApplyOverriddenCurrencySymbol(formatter, locale)
        return formattedPrice(formatter)
    }

    operator fun plus(priceToAdd: Price): Price {
        checkIfTheSameCurrency(priceToAdd)
        return Price(amount + priceToAdd.amount, currency)
    }

    operator fun minus(priceToRemove: Price): Price {
        checkIfTheSameCurrency(priceToRemove)
        return Price(amount - priceToRemove.amount, currency)
    }

    operator fun times(quantity: Int): Price {
        return Price(amount * quantity, currency)
    }

    private fun checkIfTheSameCurrency(price: Price) {
        if (currency != price.currency) {
            throw IllegalStateException("Price must be the same currency")
        }
    }

    // TODO extract this to dedicated formatting object
    @VisibleForTesting
    internal fun formattedPrice(formatter: Format): String = formatter.format(doubleAmount)

    // TODO extract this to dedicated formatting object
    private fun maybeApplyOverriddenCurrencySymbol(formatter: DecimalFormat, locale: Locale) {
        val lookupKey = Pair(locale, currency.currencyCode)
        if (overriddenCurrencySymbols.containsKey(lookupKey)) {
            val symbols = DecimalFormatSymbols.getInstance()
            symbols.currencySymbol =
                overriddenCurrencySymbols[Pair(locale, currency.currencyCode)]
            formatter.decimalFormatSymbols = symbols
        }
    }

    override fun toString(): String =
        "Price(${formattedPrice()} or $smilePoints points)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Price

        if (amount != other.amount) return false
        if (currency != other.currency) return false

        return true
    }

    override fun hashCode(): Int {
        var result = amount
        result = 31 * result + currency.hashCode()
        return result
    }

    companion object {

        /**
         * Create an instance from double value and currency,
         *
         * @param amount amount of money, for ex. 12
         * @param currency [Currency]
         */
        fun create(amount: Int, currency: Currency): Price =
            Price(amount * currencyDenominator(currency), currency)

        /**
         * Create an instance from double value and currency,
         *
         * @param doubleAmount amount of money, for ex. 12.34
         * @param currency [Currency]
         */
        fun create(doubleAmount: Double, currency: Currency): Price =
            Price(round(doubleAmount * currencyDenominator(currency)).toInt(), currency)

        /**
         * Create an instance from decimal value and currency,
         *
         * @param decimalAmount amount of money, for ex. 12.34
         * @param currency [Currency]
         */
        fun create(decimalAmount: BigDecimal, currency: Currency): Price =
            create(decimalAmount.toDouble(), currency)

        /**
         * Create an instance from an amount of smiles and localized store metadata
         *
         * @param smilesCount profile Smiles Points count [Int]
         * @param storeDetails localized [StoreDetails]
         */
        fun createFromSmiles(smilesCount: Int, storeDetails: StoreDetails) =
            create(
                smilesCount.toDouble() / SMILE_POINTS_PER_SINGLE_CURRENCY_UNIT,
                storeDetails.currency
            )

        /**
         * Create an instance from an amount of smiles and a given currency
         *
         * @param smilesCount profile Smiles Points count [Int]
         * @param currency [Currency]
         */
        fun createFromSmiles(smilesCount: Int, currency: Currency) =
            create(
                smilesCount.toDouble() / SMILE_POINTS_PER_SINGLE_CURRENCY_UNIT,
                currency
            )

        /**
         * Create an instance from a rate and a given currency,
         * if the rate does not exists it returns 0.0
         *
         * @param rates the rates to convert in [Price]
         * @param currency [Currency]
         */
        fun createFromRate(rates: BigDecimal?, currency: Currency): Price =
            create(rates ?: BigDecimal.ZERO, currency)

        /**
         * Returns a non null empty Price
         *
         * @param currency [Currency]
         */
        fun empty(currency: Currency): Price = Price(0, currency)

        /*
        * DO NOT CHANGE to `val`.
        * This is a fun because we want format to be re-fetched with currently set locale.
        * TODO extract this to dedicated formatting object
        */
        fun getDefaultFormatterInstance(locale: Locale = Locale.getDefault()): DecimalFormat =
            DecimalFormat.getCurrencyInstance(locale) as DecimalFormat

        /**
         * Mapping we use to override specific currency symbols for specific currencies,
         * but only for specific locales.
         * TODO extract this to dedicated formatting object
         */
        @VisibleForTesting
        val overriddenCurrencySymbols = mapOf(
            // Swiss locale doesn't contain CHF currency symbol
            Pair(Locale.forLanguageTag("de-CH"), "CHF") to "Fr."
        )

        @VisibleForTesting
        internal const val DENOMINATOR_BASE = 10.0

        @VisibleForTesting
        internal const val SMILE_POINTS_PER_SINGLE_CURRENCY_UNIT = 100
    }
}

private fun currencyDenominator(currency: Currency): Int =
    (Price.DENOMINATOR_BASE.pow(currency.defaultFractionDigits)).roundToInt()

private fun intToDenominatedDouble(amount: Int, currency: Currency) =
    amount.toDouble().div(currencyDenominator(currency))

private fun intToDenominatedBigDecimal(amount: Int, currency: Currency) =
    BigDecimal(amount)
        .divide(BigDecimal(currencyDenominator(currency)))
        .setScale(currency.defaultFractionDigits, RoundingMode.HALF_UP)
