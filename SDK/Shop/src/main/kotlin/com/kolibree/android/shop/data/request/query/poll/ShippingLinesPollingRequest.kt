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
import com.kolibree.android.shop.data.request.IllegalResponseException
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.domain.model.ShippingRate
import com.kolibree.android.shop.domain.model.Taxes
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import java.math.BigDecimal
import java.util.Currency

internal class ShippingLinesPollingRequest(checkoutId: String) :
    GraphClientPollingRequest<Taxes>() {

    override val queryBuilder: (Storefront.QueryRootQuery) -> Storefront.QueryRootQuery = { root ->
        root.node(ID(checkoutId)) { node ->
            node.onCheckout { checkout: Storefront.CheckoutQuery ->
                checkout.shippingRate().currencyCode()
            }
        }
    }

    override val responseBuilder: (Storefront.QueryRoot) -> Taxes =
        { response ->
            val checkout = response.node as Storefront.Checkout
            val shippingRates = checkout.availableShippingRates.shippingRates

            try {
                val currency = Currency.getInstance(checkout.currencyCode.name)
                val shippingRate = shippingRates.firstOrNull()
                Taxes(
                    taxesAmount = Price.empty(currency),
                    shippingRate = ShippingRate(
                        Price.createFromRate(
                            shippingRate?.priceV2?.amount?.let { BigDecimal(it) }, currency
                        ), shippingRate?.handle
                    )
                )
            } catch (e: NumberFormatException) {
                throw IllegalResponseException(e)
            }
        }

    override val isPollingFinished: (Storefront.QueryRoot) -> Boolean = { response ->
        with(response.node as Storefront.Checkout) {
            availableShippingRates != null &&
                availableShippingRates.ready
        }
    }
}

private fun Storefront.CheckoutQuery.shippingRate(): Storefront.CheckoutQuery {
    return this.availableShippingRates { query ->
        query.ready().shippingRates { shippingRateQuery ->
            shippingRateQuery.handle().title().priceV2 { it.amount() }
        }
    }
}
