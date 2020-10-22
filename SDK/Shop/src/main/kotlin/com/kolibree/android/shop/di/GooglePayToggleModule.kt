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
import com.kolibree.android.feature.GooglePayFeature
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
object GooglePayToggleModule {
    @Provides
    @IntoSet
    fun provideGooglePayFeatureToggle(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(context, GooglePayFeature)
}
