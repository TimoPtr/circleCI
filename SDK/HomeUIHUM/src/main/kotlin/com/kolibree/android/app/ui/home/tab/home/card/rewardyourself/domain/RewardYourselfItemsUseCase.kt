/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.rewardyourself.domain

import com.kolibree.android.app.ui.home.tab.home.card.rewardyourself.RewardYourselfItem
import com.kolibree.android.shop.data.ShopifyFeaturedProductsUseCase
import com.kolibree.android.shop.domain.model.Product
import io.reactivex.Flowable
import javax.inject.Inject

internal interface RewardYourselfItemsUseCase {
    fun getRewardItems(): Flowable<List<RewardYourselfItem>>
}

internal class RewardYourselfItemsUseCaseImpl @Inject constructor(
    private val shopifyFeaturedProductsUseCase: ShopifyFeaturedProductsUseCase
) : RewardYourselfItemsUseCase {

    override fun getRewardItems(): Flowable<List<RewardYourselfItem>> {
        return shopifyFeaturedProductsUseCase
            .getFeaturedProducts()
            .map { it.toRewardItems() }
    }

    private fun List<Product>.toRewardItems(): List<RewardYourselfItem> {
        return map { product ->
            RewardYourselfItem(
                id = product.productId,
                imageUrl = product.mainImage,
                name = product.productTitle,
                price = product.price
            )
        }
    }
}
