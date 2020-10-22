/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.cart

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.shop.buildProduct
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.domain.model.ShippingRate
import com.kolibree.android.shop.domain.model.Taxes
import com.kolibree.android.shop.presentation.list.ShopProductBindingModel
import java.math.BigDecimal
import java.util.Currency
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

@Suppress("LargeClass")
internal class ShopCartViewStateTest : BaseUnitTest() {

    private val currency = Currency.getInstance("EUR")

    /*
    initial
     */

    @Test
    fun `initial sets default values`() {
        val vs = initialViewState()

        assertEquals(emptyList<ShopProductBindingModel>(), vs.cartProducts)
        assertEquals(CartProductsResult.Loading, vs.cartResult)
        assertFalse(vs.useSmiles)
        assertEquals(0, vs.availableSmiles)
    }

    /*
    withAvailableSmiles
     */

    @Test
    fun `withAvailableSmiles sets availableSmiles to smiles and set useSmiles if smiles greater than 0`() {
        val vs = initialViewState()
        val expectedSmiles = 101

        val result = vs.withAvailableSmiles(expectedSmiles)

        assertEquals(expectedSmiles, result.availableSmiles)
        assertTrue(result.useSmiles)
    }

    @Test
    fun `withAvailableSmiles sets availableSmiles to smiles and set useSmiles if smiles less than 1`() {
        val vs = initialViewState()
        val expectedSmiles = 0

        val result = vs.withAvailableSmiles(expectedSmiles)

        assertEquals(expectedSmiles, result.availableSmiles)
        assertFalse(result.useSmiles)
    }

    /*
    subTotalPrice
     */

    @Test
    fun `subTotalPrice returns null if cartProducts empty`() {
        val vs = initialViewState().copy(cartProducts = emptyList())

        assertNull(vs.subTotalPrice())
    }

    @Test
    fun `subTotalPrice returns sum of all products price`() {
        val vs = initialViewState().copy(
            cartProducts = listOf(
                ShopProductBindingModel(
                    buildProduct(
                        price = Price.create(
                            BigDecimal.valueOf(10),
                            currency
                        )
                    ), 10
                ), // 100
                ShopProductBindingModel(
                    buildProduct(
                        price = Price.create(
                            BigDecimal.valueOf(1),
                            currency
                        )
                    ), 1
                ) // 1
            )
        )

        val result = vs.subTotalPrice()

        assertEquals(101.0, result?.doubleAmount)
    }

    /*
    subTotalPrice
     */

    @Test
    fun `totalPrice returns null if cartProducts empty`() {
        val vs = initialViewState().copy(cartProducts = emptyList())

        assertNull(vs.totalPrice())
    }

    @Test
    fun `totalPrice returns sum of all products price if actualDiscount null`() {
        val vs = initialViewState().copy(
            cartProducts = listOf(
                ShopProductBindingModel(
                    buildProduct(
                        price = Price.create(
                            BigDecimal.valueOf(10),
                            currency
                        )
                    ), 10
                ), // 100
                ShopProductBindingModel(
                    buildProduct(
                        price = Price.create(
                            BigDecimal.valueOf(1),
                            currency
                        )
                    ), 1
                ) // 1
            )
        )

        val result = vs.totalPrice()

        assertEquals(101.0, result?.doubleAmount)
    }

    @Test
    fun `totalPrice returns sum of all products price minus actualDiscount when not null`() {
        val vs =
            initialViewState().copy(
                availableSmiles = 100,
                useSmiles = true,
                cartProducts = listOf(
                    ShopProductBindingModel(
                        buildProduct(
                            price = Price.create(
                                BigDecimal.valueOf(10),
                                currency
                            )
                        ), 10
                    ), // 100
                    ShopProductBindingModel(
                        buildProduct(
                            price = Price.create(
                                BigDecimal.valueOf(1),
                                currency
                            )
                        ), 1
                    ) // 1
                ),
                estimatedTaxes = Taxes(
                    taxesAmount = Price.empty(currency),
                    shippingRate = ShippingRate(Price.empty(currency))
                )
            )

        val result = vs.totalPrice()

        assertEquals(100.0, result?.doubleAmount)
    }

    @Test
    fun `totalPrice returns sum of all products price counting actualDiscount and taxes`() {
        val vs =
            initialViewState().copy(
                availableSmiles = 100,
                useSmiles = true,
                cartProducts = listOf(
                    ShopProductBindingModel(
                        buildProduct(
                            price = Price.create(10, currency)
                        ), 10
                    ), // 100
                    ShopProductBindingModel(
                        buildProduct(
                            price = Price.create(1, currency)
                        ), 1
                    ) // 1
                ),
                estimatedTaxes = Taxes(
                    taxesAmount = Price.create(10.50, currency),
                    shippingRate = ShippingRate(Price.create(2, currency))
                )
            )

        val result = vs.totalPrice()

        assertEquals(112.5, result?.doubleAmount)
    }

    /*
    currentCurrency
     */

    @Test
    fun `currentCurrency returns null if cartProducts empty`() {
        val vs = initialViewState().copy(cartProducts = emptyList())

        assertNull(vs.currentCurrency)
    }

    @Test
    fun `currentCurrency returns first currency of product in cartProducts`() {
        val vs = initialViewState().copy(
            cartProducts = listOf(
                ShopProductBindingModel(
                    buildProduct(
                        price = Price.create(
                            BigDecimal.valueOf(10),
                            currency
                        )
                    ), 10
                )
            )
        )

        assertEquals(currency, vs.currentCurrency)
    }

    /*
    isPossibleToUseSmiles
     */

    @Test
    fun `isPossibleToUseSmiles returns true if availableSmiles is greater than 0`() {
        val vs = initialViewState().copy(availableSmiles = 1)

        assertTrue(vs.isPossibleToUseSmiles)
    }

    @Test
    fun `isPossibleToUseSmiles returns false if availableSmiles is less than 0`() {
        val vs = initialViewState().copy(availableSmiles = 0)

        assertFalse(vs.isPossibleToUseSmiles)
    }

    /*
    potentialSmilesUsed
     */

    @Test
    fun `potentialSmilesUsed returns null when subTotalPrice returns null`() {
        val vs = initialViewState().copy(availableSmiles = 0)

        assertNull(vs.potentialSmilesUsed)
    }

    @Test
    fun `potentialSmilesUsed returns availableSmiles if less than subtotalPrice smilesPoints`() {
        val vs = initialViewState().copy(
            availableSmiles = 100, cartProducts = listOf(
                ShopProductBindingModel(
                    buildProduct(
                        price = Price.create(
                            BigDecimal.valueOf(10),
                            currency
                        )
                    ), 10000
                )
            )
        )

        assertEquals(100, vs.potentialSmilesUsed)
    }

    @Test
    fun `potentialSmilesUsed returns subtotalPrice smilesPoints if less than availableSmiles`() {
        val vs = initialViewState().copy(
            availableSmiles = 10000, cartProducts = listOf(
                ShopProductBindingModel(
                    buildProduct(
                        price = Price.create(
                            BigDecimal.valueOf(1),
                            currency
                        )
                    ), 1
                )
            )
        )

        assertEquals(100, vs.potentialSmilesUsed)
    }

    /*
    potentialDiscountPrice
     */

    @Test
    fun `potentialDiscountPrice returns null if currentCurency retunrs null`() {
        val vs = initialViewState()

        assertNull(vs.potentialDiscountPrice)
    }

    @Test
    fun `potentialDiscountPrice returns price from potentialSmilesUsed`() {
        val vs = initialViewState().copy(
            cartProducts = listOf(
                ShopProductBindingModel(
                    buildProduct(price = Price.create(BigDecimal.valueOf(10), currency)),
                    10
                )
            ), availableSmiles = 101, useSmiles = true
        )

        val expectedPrice = Price.createFromSmiles(101, currency)

        assertEquals(expectedPrice, vs.potentialDiscountPrice)
    }

    /*
    actualDiscount
     */

    @Test
    fun `actualDiscount returns null if useSmiles false`() {
        val vs = initialViewState().copy(useSmiles = false)

        assertNull(vs.actualDiscount)
    }

    @Test
    fun `actualDiscount returns potentialDiscountPrice if useSmiles true`() {
        val vs = initialViewState().copy(
            useSmiles = true, availableSmiles = 101, cartProducts = listOf(
                ShopProductBindingModel(
                    buildProduct(price = Price.create(BigDecimal.valueOf(10), currency)),
                    100
                )
            )
        )
        val expectedPrice = Price.createFromSmiles(101, currency)

        assertEquals(expectedPrice, vs.actualDiscount)
    }

    /*
    cartItems
     */

    @Test
    fun `cartItems returns empty list if no products`() {
        val vs = initialViewState().copy(cartProducts = emptyList())
        assertTrue(vs.cartItems.isEmpty())
    }

    @Test
    fun `cartItems returns products with payment details if at least one product`() {
        val cartProducts = listOf(
            ShopProductBindingModel(buildProduct(1), 11),
            ShopProductBindingModel(buildProduct(2), 22)
        )
        val paymentDetails = ShopPaymentDetailsBindingModel(
            actualDiscount = "12",
            subtotal = "123.00$"
        )
        val vs =
            initialViewState().copy(cartProducts = cartProducts, paymentDetails = paymentDetails)
        assertTrue(vs.cartItems.isNotEmpty())
        assertEquals(3, vs.cartItems.size)
        val expectedCartItems = cartProducts + paymentDetails
        assertEquals(expectedCartItems, vs.cartItems)
    }

    /*
    emptyPrice
     */

    @Test
    fun `empty price returns Price object with current currency if not null`() {
        val currency = Currency.getInstance("EUR")
        val cartProduct = ShopProductBindingModel(buildProduct(), 1)
        val vs = initialViewState().copy(
            cartProducts = listOf(cartProduct)
        )
        val expectedPrice = Price.create(BigDecimal.ZERO, currency)
        assertEquals(expectedPrice, vs.emptyPrice())
    }

    @Test
    fun `empty price returns null if no currency`() {
        val vs = initialViewState()
        assertNull(vs.emptyPrice())
    }

    @Test
    fun `withVoucherApplied should propagate the state`() {
        val viewState = initialViewState()

        val voucherApplied =
            viewState.withVoucherApplied(true).voucherApplied

        assertEquals(true, voucherApplied)
    }
}

internal fun initialViewState(isGooglePayButtonVisible: Boolean = true) =
    ShopCartViewState.initial(isGooglePayButtonVisible = isGooglePayButtonVisible)
