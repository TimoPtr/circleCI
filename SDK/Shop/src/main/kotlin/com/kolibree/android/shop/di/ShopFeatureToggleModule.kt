/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.di

import android.content.Context
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.ShowShopTabsFeature
import com.kolibree.android.feature.UseTestShopFeature
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import com.kolibree.android.shop.data.googlewallet.GooglePayEnvironmentFeatureToggleModule
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module(includes = [GooglePayEnvironmentFeatureToggleModule::class])
class ShopFeatureToggleModule {

    @Provides
    @IntoSet
    fun provideShopBrandDealsFeatureToggle(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(context, ShowShopTabsFeature)

    @Provides
    @IntoSet
    fun provideUseTestShopFeatureToggle(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(context, UseTestShopFeature)
}
