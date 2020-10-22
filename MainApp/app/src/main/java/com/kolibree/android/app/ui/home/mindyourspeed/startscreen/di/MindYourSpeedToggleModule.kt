/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.mindyourspeed.startscreen.di

import android.content.Context
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.MindYourSpeedHideDotFeature
import com.kolibree.android.feature.MindYourSpeedSnapDotFeature
import com.kolibree.android.feature.ShowMindYourSpeedFeature
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
object MindYourSpeedToggleModule {
    @Provides
    @IntoSet
    fun provideMindYourSpeedToggleModule(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(context, ShowMindYourSpeedFeature)

    @Provides
    @IntoSet
    fun provideMindYourSpeedHideDotFeature(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(context, MindYourSpeedHideDotFeature)

    @Provides
    @IntoSet
    fun provideMindYourSpeedZoneSnappingFeature(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(context, MindYourSpeedSnapDotFeature)
}
