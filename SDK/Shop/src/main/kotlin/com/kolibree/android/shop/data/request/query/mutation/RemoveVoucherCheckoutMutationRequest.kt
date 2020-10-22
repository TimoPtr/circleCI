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
import com.kolibree.android.shop.domain.model.Checkout
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID

internal class RemoveVoucherCheckoutMutationRequest(checkout: Checkout) :
    GraphClientMutationRequest<Unit>() {

    override val queryBuilder: (Storefront.MutationQuery) -> Storefront.MutationQuery =
        { mutationQuery ->
            mutationQuery.checkoutDiscountCodeRemove(ID(checkout.checkoutId)) { payloadQuery ->
                payloadQuery
                    .checkoutUserErrors { checkoutUserErrorQuery: Storefront.CheckoutUserErrorQuery ->
                        checkoutUserErrorQuery
                            .field()
                            .message()
                    }
            }
        }

    override val responseBuilder: (Storefront.Mutation) -> Unit = { mutation ->
        mutation.checkoutDiscountCodeRemove.checkoutUserErrors.takeIf { it.isNotEmpty() }
            ?.let { userErrors ->
                throw ShopifyCheckoutUserError(userErrors)
            }
    }

    override val isRequestFinished: (Storefront.Mutation?) -> Boolean = { mutation ->
        mutation?.checkoutDiscountCodeRemove?.checkoutUserErrors?.isEmpty() ?: false
    }
}
