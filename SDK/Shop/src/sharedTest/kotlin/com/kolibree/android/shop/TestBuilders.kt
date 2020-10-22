/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop

import com.kolibree.android.shop.data.repo.CartRepository
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.GooglePayCheckout
import com.kolibree.android.shop.domain.model.GoogleWalletAddress
import com.kolibree.android.shop.domain.model.GoogleWalletPayment
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.shop.domain.model.QuantityProduct
import com.kolibree.android.shop.domain.model.StoreDetails
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import io.reactivex.Flowable
import java.math.BigDecimal
import java.util.Currency

internal const val defaultProductOrdinal: Int = 0

internal const val defaultVariantOrdinal: Int = 100

internal fun generateProductId(ordinal: Int): String = "abcd_$ordinal"

internal fun generateVariantId(productOrdinal: Int, ordinal: Int): String =
    "efgh_${productOrdinal}_$ordinal"

internal fun generateProductTitle(ordinal: Int): String = "E1 brush head refill_$ordinal"

internal fun generateVariantTitle(productOrdinal: Int, ordinal: Int): String =
    "E1 brush head refill_${productOrdinal}_$ordinal"

internal fun generateProductType(ordinal: Int): String = "ProductType_$ordinal"

internal fun generateProductDescription(ordinal: Int): String = "Some new heads for you_$ordinal"

internal fun generateProductHtmlDescription(ordinal: Int): String =
    "<b>Some new heads for you_$ordinal</b>"

internal val defaultPrice = Price.create(BigDecimal.TEN, Currency.getInstance("EUR"))

internal fun generateVariantSku(productOrdinal: Int, ordinal: Int): String =
    "SKUE1_${productOrdinal}_$ordinal"

internal fun generateProductImage(ordinal: Int): String = "image_url_$ordinal"

internal fun generateVariantImage(productOrdinal: Int, ordinal: Int): String =
    "image_url_${productOrdinal}_$ordinal"

internal fun buildStoreDetails(
    name: String = "Shop mock",
    description: String = "This is a mock shop",
    countryCode: String = "FR",
    currency: Currency = Currency.getInstance("EUR"),
    supportedDigitalWallets: List<String> = listOf("GOOGLE_PAY"),
    acceptedCardBrands: List<String> = listOf("VISA")
) = StoreDetails(
    name = name,
    description = description,
    countryCode = countryCode,
    currency = currency,
    supportedDigitalWallets = supportedDigitalWallets,
    acceptedCardBrands = acceptedCardBrands
)

internal fun buildProduct(
    productOrdinal: Int = 0,
    variantOrdinal: Int = 100,
    price: Price = defaultPrice,
    productImages: List<String> = listOf(generateProductImage(productOrdinal)),
    variantImage: String? = generateVariantImage(productOrdinal, variantOrdinal)
) = Product(
    productId = generateProductId(productOrdinal),
    variantId = generateVariantId(productOrdinal, variantOrdinal),
    productTitle = generateProductTitle(productOrdinal),
    variantTitle = generateVariantTitle(productOrdinal, variantOrdinal),
    productType = generateProductType(productOrdinal),
    description = generateProductDescription(productOrdinal),
    htmlDescription = generateProductHtmlDescription(productOrdinal),
    price = price,
    productImages = productImages,
    variantImage = variantImage,
    sku = generateVariantSku(productOrdinal, variantOrdinal)
)

internal fun CartRepository.prepareCartWithProducts(
    quantityProducts: List<QuantityProduct> = listOf(
        QuantityProduct(2, buildProduct()),
        QuantityProduct(1, buildProduct(productOrdinal = 1))
    )
): Cart {
    whenever(getCartProducts()).thenReturn(Flowable.just(quantityProducts))

    return cartWithProducts(quantityProducts)
}

internal fun cartWithProducts(
    quantityProducts: List<QuantityProduct> = listOf(
        QuantityProduct(1, buildProduct(productOrdinal = 1))
    )
): Cart = Cart(quantityProducts)

internal fun googleWalletPayment(
    price: Price = price(),
    token: String = TOKEN,
    billingAddress: GoogleWalletAddress = googleWalletAddress(),
    shippingAddress: GoogleWalletAddress = googleWalletAddress()
) = GoogleWalletPayment(
    amount = price,
    token = token,
    billingAddress = billingAddress,
    shippingAddress = shippingAddress,
    isProductionPayment = false
)

internal fun googleWalletAddress(
    address1: String = "address1",
    address2: String = "address2",
    address3: String = "address3",
    sortingCode: String = "sortingCode",
    countryCode: String = "countryCode",
    postalCode: String = "postalCode",
    name: String = "name",
    locality: String = "locality",
    administrativeArea: String = "administrativeArea"
) = GoogleWalletAddress(
    address1 = address1,
    address2 = address2,
    address3 = address3,
    sortingCode = sortingCode,
    countryCode = countryCode,
    postalCode = postalCode,
    name = name,
    locality = locality,
    administrativeArea = administrativeArea
)

internal fun price(
    amount: Double = PRICE,
    currency: String = CURRENCY
) = Price.create(
    decimalAmount = BigDecimal.valueOf(amount),
    currency = Currency.getInstance(currency)
)

internal fun googlePayCheckout(
    order: Storefront.Order = mock(),
    checkout: Storefront.Checkout = mockCheckout(),
    cart: Cart = Cart()
) = GooglePayCheckout(
    orderId = order,
    checkout = checkout,
    cart = cart
)

private fun mockCheckout(): Storefront.Checkout {
    return mock<Storefront.Checkout>().apply {
        whenever(id).thenReturn(ID("id"))
    }
}

internal const val PRICE = 11.99
internal const val CURRENCY = "EUR"

internal const val TOKEN = "MY TOKEN"
