/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.di

import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.app.ui.home.tab.activities.ActivitiesFragment
import com.kolibree.android.app.ui.home.tab.activities.ActivitiesTabModule
import com.kolibree.android.app.ui.home.tab.home.HomeFragment
import com.kolibree.android.app.ui.home.tab.home.card.di.HomeModule
import com.kolibree.android.app.ui.home.tab.profile.ProfileFragment
import com.kolibree.android.app.ui.home.tab.profile.di.ProfileModule
import com.kolibree.android.app.ui.home.tab.shop.ShopFragment
import com.kolibree.android.app.ui.home.tab.shop.di.ShopModule
import com.kolibree.android.shop.presentation.di.ShopPresentationModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [ShopPresentationModule::class])
internal abstract class HomeScreenFragmentModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [HomeModule::class])
    internal abstract fun contributeHomeFragment(): HomeFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [ShopModule::class])
    internal abstract fun contributeShopFragment(): ShopFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [ActivitiesTabModule::class])
    internal abstract fun contributeActivitiesFragment(): ActivitiesFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [ProfileModule::class])
    internal abstract fun contributeProfileFragment(): ProfileFragment
}
