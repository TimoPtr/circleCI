/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request

import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront

internal abstract class GraphClientMutationRequest<T : Any> {
    protected abstract val queryBuilder: (Storefront.MutationQuery) -> Storefront.MutationQuery
    protected abstract val responseBuilder: (Storefront.Mutation) -> T

    fun buildQuery(): Storefront.MutationQuery =
        Storefront.mutation { mutationQuery -> queryBuilder(mutationQuery) }

    fun buildResponse(response: GraphResponse<Storefront.Mutation>): T =
        if (response.hasErrors) {
            throw IllegalResponseException(response.errors[0].message())
        } else {
            responseBuilder(requireNotNull(response.data))
        }

    protected abstract val isRequestFinished: (Storefront.Mutation?) -> Boolean

    fun isSuccess(): (Storefront.Mutation?) -> Boolean = isRequestFinished
}
