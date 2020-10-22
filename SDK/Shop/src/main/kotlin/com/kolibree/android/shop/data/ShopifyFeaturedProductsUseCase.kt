/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data

import androidx.annotation.Keep
import com.kolibree.android.shop.domain.model.Product
import io.reactivex.Flowable
import javax.inject.Inject

@Keep
interface ShopifyFeaturedProductsUseCase {
    fun getFeaturedProducts(): Flowable<List<Product>>
}

internal class ShopifyFeaturedProductsUseCaseImpl @Inject constructor(
    private val shopifyClientWrapper: ShopifyClientWrapper
) : ShopifyFeaturedProductsUseCase {

    /**
     * For now this use case is not performing any operations
     * In the future it should sort/filter products based on specification
     * https://kolibree.atlassian.net/wiki/spaces/PROD/pages/208175175/Featured+products
     */
    override fun getFeaturedProducts(): Flowable<List<Product>> {
        return shopifyClientWrapper.getProducts().toFlowable()
    }
}
