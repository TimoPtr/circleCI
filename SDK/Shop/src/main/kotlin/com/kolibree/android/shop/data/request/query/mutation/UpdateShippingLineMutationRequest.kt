/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request.query.mutation

import com.kolibree.android.shop.data.request.GraphClientMutationRequest
import com.kolibree.android.shop.data.request.IllegalResponseException
import com.kolibree.android.shop.data.request.query.error.ShopifyCheckoutUserError
import com.kolibree.android.shop.domain.model.Checkout
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.domain.model.Taxes
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import java.math.BigDecimal
import java.util.Currency

/**
 * Update the current Checkout ShippingLine and retrieve the taxes once it is done
 */
internal class UpdateShippingLineMutationRequest(
    private val shopifyCheckout: Checkout,
    private val taxes: Taxes
) : GraphClientMutationRequest<Taxes>() {

    override val queryBuilder: (Storefront.MutationQuery) -> Storefront.MutationQuery =
        { query ->
            query.checkoutShippingLineUpdate(
                ID(shopifyCheckout.checkoutId), taxes.shippingRate.handle
            ) { payload ->
                payload.checkout { checkout ->
                    checkout.totalTaxV2 { taxV2 ->
                        taxV2.amount().currencyCode()
                    }
                }
                payload.checkoutUserErrors { errors ->
                    errors.field().message()
                }
            }
        }

    override val responseBuilder: (Storefront.Mutation) -> Taxes = { mutation ->
        with(mutation.checkoutShippingLineUpdate) {
            if (!checkoutUserErrors.isNullOrEmpty()) {
                throw ShopifyCheckoutUserError(checkoutUserErrors)
            }

            val amount = checkout.totalTaxV2.amount
            val currency = try {
                Currency.getInstance(checkout.totalTaxV2.currencyCode.name)
            } catch (e: NumberFormatException) {
                throw IllegalResponseException(e)
            }

            taxes.copy(taxesAmount = Price.createFromRate(BigDecimal(amount), currency))
        }
    }

    override val isRequestFinished: (Storefront.Mutation?) -> Boolean = { mutation ->
        mutation?.checkoutShippingLineUpdate?.checkoutUserErrors?.isEmpty() ?: false
    }
}
