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
import com.kolibree.android.shop.domain.model.BasicCheckout
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.Checkout
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID

internal class UpdateShopifyCheckoutMutationRequest(
    private val checkout: Checkout,
    private val cart: Cart
) : GraphClientMutationRequest<Checkout>() {

    override val queryBuilder: (Storefront.MutationQuery) -> Storefront.MutationQuery =
        { mutationQuery ->

            val itemList = cart.products.map { item ->
                Storefront.CheckoutLineItemInput(
                    item.quantity,
                    ID(item.product.variantId)
                )
            }

            mutationQuery.checkoutLineItemsReplace(
                itemList,
                ID(checkout.checkoutId)
            ) { payloadQuery ->
                payloadQuery.checkout {
                    // no-op
                }.userErrors { userErrorQuery: Storefront.CheckoutUserErrorQuery ->
                    userErrorQuery
                        .field()
                        .message()
                }
            }
        }

    override val responseBuilder: (Storefront.Mutation) -> Checkout = { mutation ->
        if (mutation.checkoutLineItemsReplace.userErrors.isNotEmpty()) {
            throw ShopifyCheckoutUserError(mutation.checkoutCreate.checkoutUserErrors)
        } else {
            BasicCheckout(checkoutId = checkout.checkoutId, cart = cart)
        }
    }

    override val isRequestFinished: (Storefront.Mutation?) -> Boolean = { mutation ->
        mutation?.checkoutLineItemsReplace?.userErrors?.isEmpty() ?: false
    }
}
