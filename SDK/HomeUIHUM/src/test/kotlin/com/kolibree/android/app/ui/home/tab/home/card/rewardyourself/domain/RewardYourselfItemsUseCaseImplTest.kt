/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.rewardyourself.domain

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.shop.data.ShopifyFeaturedProductsUseCase
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.domain.model.Product
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale
import junit.framework.Assert.assertEquals
import org.junit.Test

class RewardYourselfItemsUseCaseImplTest : BaseUnitTest() {

    private val shopifyFeaturedProductsUseCase = mock<ShopifyFeaturedProductsUseCase>()

    private lateinit var useCase: RewardYourselfItemsUseCase

    override fun setup() {
        super.setup()
        useCase = RewardYourselfItemsUseCaseImpl(
            shopifyFeaturedProductsUseCase
        )
    }

    @Test
    fun `returns reward items`() {
        val mockProducts = (0..10).map {
            Product(
                productId = "$it",
                variantId = "1",
                productTitle = "Mock product $it",
                variantTitle = "Mock variant",
                productType = "Mock type",
                description = "Mock type",
                htmlDescription = "Mock description",
                price = Price.create(BigDecimal(0), Currency.getInstance(Locale.getDefault())),
                productImages = emptyList(),
                variantImage = null,
                sku = "Mock sku"
            )
        }

        whenever(shopifyFeaturedProductsUseCase.getFeaturedProducts())
            .thenReturn(Flowable.just(mockProducts))

        val rewardItems = useCase.getRewardItems().test().values().first()

        mockProducts.zip(rewardItems).forEach { (mockProduct, rewardItem) ->
            assertEquals(mockProduct.productId, rewardItem.id)
            assertEquals(mockProduct.mainImage, rewardItem.imageUrl)
            assertEquals(mockProduct.productTitle, rewardItem.name)
            assertEquals(mockProduct.price, rewardItem.price)
        }
    }
}
