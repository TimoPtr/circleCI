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
import com.kolibree.android.shop.data.request.query.error.ShopifyCheckoutUserError
import com.kolibree.android.shop.data.request.query.mutation.mapper.ShopifyAddressMapper
import com.kolibree.android.shop.domain.model.Address
import com.kolibree.android.shop.domain.model.BasicCheckout
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.Checkout
import com.shopify.buy3.Storefront

internal class CreateShopifyCheckoutMutationRequest(address: Address) :
    GraphClientMutationRequest<Checkout>() {

    override val queryBuilder: (Storefront.MutationQuery) -> Storefront.MutationQuery =
        { mutationQuery ->
            val checkoutCreateInput = buildCheckoutCreateInput(address)
            mutationQuery.checkoutCreate(checkoutCreateInput) { createPayloadQuery ->
                createPayloadQuery
                    .checkout {
                        // no-op
                    }
                    .checkoutUserErrors { userErrorQuery: Storefront.CheckoutUserErrorQuery ->
                        userErrorQuery
                            .field()
                            .message()
                    }
            }
        }

    private fun buildCheckoutCreateInput(address: Address): Storefront.CheckoutCreateInput {
        return Storefront.CheckoutCreateInput().apply {
            email = address.email

            takeIf { address.hasAllMandatoryFields() }
                ?.shippingAddress = ShopifyAddressMapper(address)
        }
    }

    override val responseBuilder: (Storefront.Mutation) -> BasicCheckout = { mutation ->
        if (mutation.checkoutCreate.checkoutUserErrors.isNotEmpty()) {
            throw ShopifyCheckoutUserError(mutation.checkoutCreate.checkoutUserErrors)
        } else {
            val checkout = mutation.checkoutCreate.checkout

            val checkoutId: String = checkout.id.toString()

            BasicCheckout(
                checkoutId = checkoutId,
                cart = Cart()
            )
        }
    }

    override val isRequestFinished: (Storefront.Mutation?) -> Boolean = { mutation ->
        mutation?.checkoutCreate?.checkoutUserErrors?.isEmpty() ?: false
    }
}
