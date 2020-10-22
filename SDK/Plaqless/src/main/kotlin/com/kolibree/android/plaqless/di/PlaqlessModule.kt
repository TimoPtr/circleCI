/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.di

import android.content.Context
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.ShowPlaqlessVersionOfViewsFeature
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module(includes = [PlaqlessFeatureToggleModule::class])
class PlaqlessModule

@Module
class PlaqlessFeatureToggleModule {

    @Provides
    @IntoSet
    fun provideShowPlaqlessVersionOfViewsFeatureToggle(context: Context): FeatureToggle<*> {
        return PersistentFeatureToggle(context, ShowPlaqlessVersionOfViewsFeature)
    }
}
