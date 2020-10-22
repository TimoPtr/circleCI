/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request.query.poll

import com.kolibree.android.shop.data.request.GraphClientPollingRequest
import com.kolibree.android.shop.domain.model.Checkout
import com.kolibree.android.shop.domain.model.WebViewCheckout
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID

internal class UpdateCheckoutWebUrlPollingRequest(checkout: Checkout) :
    GraphClientPollingRequest<WebViewCheckout>() {

    override val queryBuilder: (Storefront.QueryRootQuery) -> Storefront.QueryRootQuery = { root ->
        root.node(ID(checkout.checkoutId)) { node ->
            node.onCheckout { it.webUrl() }
        }
    }

    override val responseBuilder: (Storefront.QueryRoot) -> WebViewCheckout = { response ->
        val webUrl = (response.node as Storefront.Checkout).webUrl

        CheckoutWebViewMapper(checkout, webUrl)
    }

    override val isPollingFinished: (Storefront.QueryRoot) -> Boolean = { response ->
        with(response.node as Storefront.Checkout) {
            availableShippingRates != null &&
                availableShippingRates.ready
        }
    }
}

/**
 * Map a [Checkout] to it's web representation -> [WebViewCheckout]
 */
private object CheckoutWebViewMapper : (Checkout, String) -> WebViewCheckout {
    override fun invoke(checkout: Checkout, webUrl: String) = WebViewCheckout(
        checkoutId = checkout.checkoutId,
        cart = checkout.cart,
        webUrl = webUrl
    )
}
