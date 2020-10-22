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
import com.kolibree.android.shop.domain.model.Voucher
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID

internal class ApplyVoucherCheckoutMutationRequest(voucher: Voucher, checkout: Checkout) :
    GraphClientMutationRequest<Unit>() {

    override val queryBuilder: (Storefront.MutationQuery) -> Storefront.MutationQuery =
        { mutationQuery ->
            mutationQuery.checkoutDiscountCodeApplyV2(
                voucher.code,
                ID(checkout.checkoutId)
            ) { payloadQuery ->
                payloadQuery
                    .checkout { checkoutQuery: Storefront.CheckoutQuery -> checkoutQuery.webUrl() }
                    .checkoutUserErrors { checkoutUserErrorQuery: Storefront.CheckoutUserErrorQuery ->
                        checkoutUserErrorQuery
                            .field()
                            .message()
                    }
            }
        }

    override val responseBuilder: (Storefront.Mutation) -> Unit = { mutation ->
        mutation.checkoutDiscountCodeApplyV2
            .checkoutUserErrors.takeIf { it.isNotEmpty() }
            ?.let { userErrors ->
                throw ShopifyCheckoutUserError(userErrors)
            }
    }

    override val isRequestFinished: (Storefront.Mutation?) -> Boolean = { mutation ->
        mutation?.checkoutDiscountCodeApplyV2?.checkoutUserErrors?.isEmpty() ?: false
    }
}
