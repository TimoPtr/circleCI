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
import com.kolibree.android.shop.domain.model.WebViewCheckout
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID

internal class WebViewCheckoutPollingRequest(checkout: WebViewCheckout) :
    GraphClientPollingRequest<Boolean>() {

    override val queryBuilder: (Storefront.QueryRootQuery) -> Storefront.QueryRootQuery =
        { rootQuery ->
            rootQuery.node(ID(checkout.checkoutId)) { nodeQuery ->
                nodeQuery.onCheckout { checkoutQuery ->
                    checkoutQuery.order { orderQuery ->
                        orderQuery
                            .orderNumber()
                            .totalPrice()
                    }
                }
            }
        }

    override val responseBuilder: (Storefront.QueryRoot) -> Boolean = { response ->
        val checkoutObject = response.node as Storefront.Checkout
        checkoutObject.order != null
    }

    override val isPollingFinished: (Storefront.QueryRoot) -> Boolean = { response ->
        val checkoutObject = response.node as Storefront.Checkout
        checkoutObject.order != null
    }
}
