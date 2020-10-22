/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.di

import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.shop.data.googlewallet.GooglePayAvailabilityModule
import com.kolibree.android.shop.presentation.container.ShopContainerFragment
import com.kolibree.android.shop.presentation.deals.ShopBrandDealsFragment
import com.kolibree.android.shop.presentation.list.ShopListScrollUseCase
import com.kolibree.android.shop.presentation.list.ShopListScrollUseCaseImpl
import com.kolibree.android.shop.presentation.list.ShopProductListFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [ShopFragmentModule::class])
class ShopPresentationModule

@Module
internal abstract class ShopFragmentModule {

    @Binds
    internal abstract fun bindsShopListScrollUseCase(impl: ShopListScrollUseCaseImpl): ShopListScrollUseCase

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeShopContainerFragment(): ShopContainerFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [GooglePayAvailabilityModule::class])
    internal abstract fun contributeShopProductListFragment(): ShopProductListFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeShopBrandDealsFragment(): ShopBrandDealsFragment
}
