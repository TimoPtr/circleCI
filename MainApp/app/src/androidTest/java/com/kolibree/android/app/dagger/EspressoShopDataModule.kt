/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.ShopifyFeaturedProductsUseCase
import com.kolibree.android.shop.data.VoucherProvider
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.shop.domain.model.StoreDetails
import com.kolibree.android.shop.domain.model.Voucher
import com.kolibree.android.shop.presentation.di.CartRepositoryModule
import com.kolibree.android.shop.presentation.di.CheckoutModule
import com.kolibree.android.test.utils.EspressoProduct
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import java.math.BigDecimal
import java.util.Currency

@Module(includes = [CheckoutModule::class, CartRepositoryModule::class, EspressoShopDatabaseModule::class])
object EspressoShopDataModule {

    val shopifyClientWrapperMock: ShopifyClientWrapper = mock()
    val shopifyFeaturedProductsUseCase: ShopifyFeaturedProductsUseCase = mock()

    @Provides
    @AppScope
    fun providesShopifyClientWrapper(): ShopifyClientWrapper = shopifyClientWrapperMock

    @Provides
    fun providesVoucherProvider(): VoucherProvider {
        val service = mock<VoucherProvider>()
        whenever(service.getVoucher()).thenReturn(Single.just(Voucher(defaultVoucherCode)))
        return service
    }

    @Provides
    fun providesShopifyFeaturedProductsUseCase() = shopifyFeaturedProductsUseCase

    const val defaultVoucherCode = "helloWorld"

    val defaultShopDetails = StoreDetails(
        name = "Shop mock",
        description = "This is a mock shop",
        countryCode = "FR",
        currency = Currency.getInstance("EUR"),
        supportedDigitalWallets = listOf("GOOGLE_PAY"),
        acceptedCardBrands = listOf("VISA")
    )

    val defaultProduct = Product(
        productId = "abcd",
        variantId = "efgh",
        productTitle = "E1 brush head refill",
        variantTitle = "E1 brush head refill (10 pcs)",
        productType = "Brush",
        description = "Some new heads for you",
        htmlDescription = "<b>Some new heads for you</b>",
        price = Price.create(BigDecimal.TEN, Currency.getInstance("EUR")),
        productImages = listOf("http://example.com/abcd.jpg"),
        variantImage = "http://example.com/efgh.jpg",
        sku = "SKUE110PCS"
    )

    val defaultProductList: List<EspressoProduct> = listOf(defaultProduct)
        .map {
            EspressoProduct(
                product = it,
                quantity = DEFAULT_QUANTITY
            )
        }

    const val DEFAULT_QUANTITY = 0
}
