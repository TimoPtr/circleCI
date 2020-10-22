/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request.query

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.shop.ShopifyQueryDSL
import com.kolibree.android.shop.data.request.query.poll.WebViewCheckoutPollingRequest
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.WebViewCheckout
import com.nhaarman.mockitokotlin2.mock
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class WebViewCheckoutPollingRequestTest : BaseUnitTest() {

    @Test
    fun `buildResponse returns true if order object is not null`() {
        val checkout = WebViewCheckout(
            checkoutId = "id_00_002",
            cart = Cart(),
            webUrl = "web2.url"
        )
        val request =
            WebViewCheckoutPollingRequest(
                checkout
            )
        assertTrue(request.buildResponse(mockShopCheckout(mock())))
    }

    @Test
    fun `buildResponse returns true if order object is null`() {
        val checkout = WebViewCheckout(
            checkoutId = "id_00_003",
            cart = Cart(),
            webUrl = "web3.url"
        )
        val request =
            WebViewCheckoutPollingRequest(
                checkout
            )
        assertFalse(request.buildResponse(mockShopCheckout(null)))
    }

    @Test
    fun `retryCondition returns true if order object is not null`() {
        val checkout = WebViewCheckout(
            checkoutId = "id_00_004",
            cart = Cart(),
            webUrl = "web4.url"
        )
        val request =
            WebViewCheckoutPollingRequest(
                checkout
            )
        val retryCondition = request.retryCondition()
        assertTrue(retryCondition(mockShopCheckout(mock()).data!!))
    }

    @Test
    fun `retryCondition returns false if order object is null`() {
        val checkout = WebViewCheckout(
            checkoutId = "id_00_005",
            cart = Cart(),
            webUrl = "web5.url"
        )
        val request =
            WebViewCheckoutPollingRequest(
                checkout
            )
        val retryCondition = request.retryCondition()
        assertFalse(retryCondition(mockShopCheckout(null).data!!))
    }

    private fun mockShopCheckout(order: Storefront.Order?): GraphResponse<Storefront.QueryRoot> =
        ShopifyQueryDSL.checkout {
            setOrder(order)
        }
}
