/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.dagger

import android.content.Context
import com.kolibree.android.feature.ConvertB1ToHumBatteryFeature
import com.kolibree.android.feature.ConvertCe2ToHumElectricFeature
import com.kolibree.android.feature.ConvertCe2ToPlaqlessFeature
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
object ToothbrushSdkFeatureToggles {

    @Provides
    @ToothbrushSdkFeatureToggle
    @IntoSet
    internal fun provideConvertCB1ToHumBatteryFeatureToggle(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(
            context,
            ConvertB1ToHumBatteryFeature
        )

    @Provides
    @ToothbrushSdkFeatureToggle
    @IntoSet
    internal fun provideConvertCe2ToHumElectricFeatureToggle(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(
            context,
            ConvertCe2ToHumElectricFeature
        )

    @Provides
    @ToothbrushSdkFeatureToggle
    @IntoSet
    internal fun provideConvertCe2ToPlaqlessFeatureToggle(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(
            context,
            ConvertCe2ToPlaqlessFeature
        )

    /*
    Duplicate the bindings for the app

    Qualified bindings qualify as a different Type from Dagger's perspective

    Internally, they'll write the same preferences keys since it's the same feature toggle
     */

    @Provides
    @IntoSet
    fun provideConvertCB1ToHumBatteryFeatureToggleForApp(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(
            context,
            ConvertB1ToHumBatteryFeature
        )

    @Provides
    @IntoSet
    fun provideConvertCe2ToHumElectricFeatureToggleForApp(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(
            context,
            ConvertCe2ToHumElectricFeature
        )

    @Provides
    @IntoSet
    fun provideConvertCe2ToPlaqlessFeatureToggleForApp(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(
            context,
            ConvertCe2ToPlaqlessFeature
        )
}
