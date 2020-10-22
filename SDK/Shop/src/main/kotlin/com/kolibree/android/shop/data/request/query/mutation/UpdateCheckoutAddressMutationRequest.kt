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
import com.kolibree.android.shop.data.request.query.error.ShopifyInputError
import com.kolibree.android.shop.data.request.query.mutation.mapper.ShopifyAddressMapper
import com.kolibree.android.shop.domain.model.Address
import com.kolibree.android.shop.domain.model.Checkout
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID

/**
 * Update a Shopify checkout and returns [Checkout]
 * soon as we receive a response without errors
 *
 * There's no check that the returned instance is ready
 */
internal class UpdateCheckoutAddressMutationRequest(
    private val shopifyCheckout: Checkout,
    private val address: Address
) : GraphClientMutationRequest<Checkout>() {

    override val queryBuilder: (Storefront.MutationQuery) -> Storefront.MutationQuery =
        { query ->
            query.checkoutShippingAddressUpdateV2(
                ShopifyAddressMapper(address), ID(shopifyCheckout.checkoutId)
            ) { payload ->
                payload.checkoutUserErrors { error ->
                    error.field().message()
                }
            }
        }

    override val responseBuilder: (Storefront.Mutation) -> Checkout = { mutation ->
        with(mutation.checkoutShippingAddressUpdateV2) {
            if (!checkoutUserErrors.isNullOrEmpty()) {
                throw ShopifyInputError(getErrorMessage(checkoutUserErrors.first()))
            }
        }

        shopifyCheckout
    }

    private fun getErrorMessage(userError: Storefront.CheckoutUserError): String? {
        return userError.responseData
            ?.takeIf { it.containsKey(SHOPIFY_ERROR_MESSAGE_KEY) }
            ?.get(SHOPIFY_ERROR_MESSAGE_KEY) as? String
    }

    // We consider the request to be always finished even if there is an error, because the error
    // can be user-related and in this case it is preferable to show the error message to
    // the user as soon as possible.
    override val isRequestFinished: (Storefront.Mutation?) -> Boolean = { true }
}

private const val SHOPIFY_ERROR_MESSAGE_KEY = "message"
