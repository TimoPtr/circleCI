/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request.query

import com.kolibree.android.shop.data.request.GraphClientRequest
import com.kolibree.android.shop.domain.model.StoreDetails
import com.shopify.buy3.Storefront.QueryRoot
import com.shopify.buy3.Storefront.QueryRootQuery
import java.util.Currency

internal object GetStoreDetailsRequest : GraphClientRequest<StoreDetails>() {

    override val queryBuilder: (QueryRootQuery) -> QueryRootQuery = { root ->
        root.shop { shop ->
            shop.name()
                .description()
                .paymentSettings { paymentSettings ->
                    paymentSettings.countryCode()
                        .currencyCode()
                        .supportedDigitalWallets()
                        .acceptedCardBrands()
                }
        }
    }

    override val responseBuilder: (QueryRoot) -> StoreDetails = { response ->
        with(response.shop) {
            StoreDetails(
                name = name,
                description = description,
                countryCode = paymentSettings.countryCode.name,
                currency = Currency.getInstance(paymentSettings.currencyCode.name),
                supportedDigitalWallets = paymentSettings.supportedDigitalWallets.map { it.name },
                acceptedCardBrands = paymentSettings.acceptedCardBrands.map { it.name }
            )
        }
    }
}
