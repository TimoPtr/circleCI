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

internal abstract class GraphClientRequest<T : Any> {
    protected abstract val queryBuilder: (Storefront.QueryRootQuery) -> Storefront.QueryRootQuery
    protected abstract val responseBuilder: (Storefront.QueryRoot) -> T

    fun buildQuery(): Storefront.QueryRootQuery =
        Storefront.query { root -> queryBuilder(root) }

    @Throws(IllegalResponseException::class)
    fun buildResponse(response: GraphResponse<Storefront.QueryRoot>): T = try {
        if (response.hasErrors)
            throw IllegalResponseException(response.errors[0].message())
        responseBuilder(requireNotNull(response.data))
    } catch (e: RuntimeException) {
        throw IllegalResponseException(e)
    }
}
