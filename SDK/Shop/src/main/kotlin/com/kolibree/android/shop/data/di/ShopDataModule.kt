/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.di

import android.content.Context
import androidx.room.Room
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.shop.data.ShopRoomDatabase
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.ShopifyClientWrapperImpl
import com.kolibree.android.shop.data.ShopifyFeaturedProductsUseCase
import com.kolibree.android.shop.data.ShopifyFeaturedProductsUseCaseImpl
import com.kolibree.android.shop.data.VoucherProvider
import com.kolibree.android.shop.data.VoucherProviderImpl
import com.kolibree.android.shop.data.api.VoucherApi
import com.kolibree.android.shop.data.configuration.ShopifyCredentials
import com.kolibree.android.shop.data.configuration.ShopifyProductTag
import com.kolibree.android.shop.data.persitence.CartDao
import com.kolibree.android.shop.presentation.di.CartRepositoryModule
import com.kolibree.android.shop.presentation.di.CheckoutModule
import com.shopify.buy3.GraphClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import retrofit2.Retrofit

@Module(includes = [CheckoutModule::class, CartRepositoryModule::class])
abstract class ShopDataModule {

    @Binds
    @IntoSet
    internal abstract fun bindsTruncable(dao: CartDao): Truncable

    @Binds
    internal abstract fun bindsVoucherProvider(impl: VoucherProviderImpl): VoucherProvider

    @Binds
    internal abstract fun bindsFeaturedProductsUseCase(
        impl: ShopifyFeaturedProductsUseCaseImpl
    ): ShopifyFeaturedProductsUseCase

    internal companion object {
        @Provides
        internal fun providesShopifyClientWrapper(
            context: Context,
            credentials: ShopifyCredentials,
            productTag: ShopifyProductTag
        ): ShopifyClientWrapper {
            val graphClientBuilder = GraphClient.build(
                context = context.applicationContext,
                shopDomain = credentials.shopDomain,
                accessToken = credentials.shopApiKey,
                configure = {
                    httpCache(
                        cacheFolder = ShopifyClientWrapperImpl.httpCachePath(context),
                        configure = {
                            cacheMaxSizeBytes = ShopifyClientWrapperImpl.HTTP_CACHE_SIZE
                            defaultCachePolicy = ShopifyClientWrapperImpl.httpCachePolicy
                        }
                    )
                }
            )

            return ShopifyClientWrapperImpl(graphClientBuilder, productTag)
        }

        @Provides
        @AppScope
        internal fun providesShopRoomDatabase(context: Context): ShopRoomDatabase =
            Room.databaseBuilder(
                context,
                ShopRoomDatabase::class.java,
                ShopRoomDatabase.DATABASE_NAME
            )
                .build()

        @Provides
        internal fun providesCartDao(shopDatabase: ShopRoomDatabase): CartDao =
            shopDatabase.cartDao()

        @Provides
        fun provideVoucherApi(retrofit: Retrofit): VoucherApi =
            retrofit.create(VoucherApi::class.java)
    }
}
