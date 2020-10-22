/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID

/**
 * A set of methods to create Shopify GraphQL objects.
 */
internal object ShopifyQueryDSL {

    fun shop(
        params: Storefront.Shop.() -> Unit
    ): GraphResponse<Storefront.QueryRoot> {
        val response: GraphResponse<Storefront.QueryRoot> = mock()
        val responseData: Storefront.QueryRoot = mock()
        val shop = Storefront.Shop()
        params(shop)
        whenever(response.data).thenReturn(responseData)
        whenever(responseData.shop).thenReturn(shop)

        return response
    }

    fun shopPaymentSettings(
        params: Storefront.PaymentSettings.() -> Unit
    ) = Storefront.PaymentSettings().also { params(it) }

    fun products(
        params: () -> List<Storefront.Product>
    ) = Storefront.ProductConnection().also {
        it.edges = params().map { product ->
            Storefront.ProductEdge().also { edge -> edge.node = product }
        }
    }

    fun singleProduct(
        product: () -> Storefront.Product
    ) = Storefront.ProductConnection().also {
        it.edges = listOf(Storefront.ProductEdge().also { edge -> edge.node = product() })
    }

    fun product(
        productId: String,
        params: Storefront.Product.() -> Unit
    ) = Storefront.Product(ID(productId)).also { params(it) }

    fun variants(
        variants: () -> List<Storefront.ProductVariant>
    ) = Storefront.ProductVariantConnection().also {
        it.edges = variants().map { variant ->
            Storefront.ProductVariantEdge().also { edge -> edge.node = variant }
        }
    }

    fun singleVariant(
        variant: () -> Storefront.ProductVariant
    ) = Storefront.ProductVariantConnection().also {
        it.edges = listOf(Storefront.ProductVariantEdge().also { edge -> edge.node = variant() })
    }

    fun variant(
        variantID: String,
        params: Storefront.ProductVariant.() -> Unit
    ) = Storefront.ProductVariant(ID(variantID)).also { params(it) }

    fun priceRange(
        params: Storefront.MoneyV2.() -> Unit
    ) = Storefront.ProductPriceRange().also { priceRange ->
        priceRange.maxVariantPrice = Storefront.MoneyV2().also { price ->
            params(price)
        }
    }

    fun currencyCode(
        currencyCode: () -> String
    ): Storefront.CurrencyCode = Storefront.CurrencyCode.fromGraphQl(currencyCode())

    fun noImages() = Storefront.ImageConnection().also { connection ->
        connection.edges = emptyList()
    }

    fun images(
        urls: () -> List<String>
    ) = Storefront.ImageConnection().also { connection ->
        connection.edges = urls().map { url ->
            Storefront.ImageEdge().also { edge ->
                edge.node = this.image { url }
            }
        }
    }

    fun image(
        url: () -> String
    ) = Storefront.Image().also { it.originalSrc = url() }

    fun checkout(
        params: Storefront.Checkout.() -> Unit
    ): GraphResponse<Storefront.QueryRoot> {
        val response: GraphResponse<Storefront.QueryRoot> = mock()
        val responseData: Storefront.QueryRoot = mock()
        val checkout = Storefront.Checkout()
        params(checkout)
        whenever(response.data).thenReturn(responseData)
        whenever(responseData.node).thenReturn(checkout)
        return response
    }

    fun mutation(
        checkoutResult: Storefront.Checkout,
        fillCheckoutUserErrorsBlock: MutableList<Storefront.CheckoutUserError>.() -> Unit = {},
        fillDataBlock: ((Storefront.Mutation) -> Unit)? = null
    ): GraphResponse<Storefront.Mutation> {
        val response: GraphResponse<Storefront.Mutation> = mock()

        val checkoutUserErrors = mutableListOf<Storefront.CheckoutUserError>()
        fillCheckoutUserErrorsBlock(checkoutUserErrors)

        val checkoutCreatePayload = Storefront.CheckoutCreatePayload().apply {
            this.checkout = checkoutResult
            this.checkoutUserErrors = checkoutUserErrors
        }

        val shippingLineUpdatePayload = Storefront.CheckoutShippingLineUpdatePayload().apply {
            this.checkout = checkoutResult
            this.checkoutUserErrors = checkoutUserErrors
        }

        val mutation: Storefront.Mutation = Storefront.Mutation().apply {
            this.checkoutCreate = checkoutCreatePayload
            this.checkoutShippingLineUpdate = shippingLineUpdatePayload
        }

        fillDataBlock?.let { fillDataBlock(mutation) }

        whenever(response.data).thenReturn(mutation)

        return response
    }
}
