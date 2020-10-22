/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.settings

import androidx.annotation.Keep
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.dagger.scopes.ActivityScope
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Keep
@Module
abstract class GuidedBrushingSettingsBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [
            GuidedBrushingSettingsNavigatorModule::class
        ]
    )
    internal abstract fun bindGuidedBrushingSettingsActivity(): GuidedBrushingSettingsActivity
}

@Module
internal object GuidedBrushingSettingsNavigatorModule {

    @Provides
    fun providesGuidedBrushingSettingsNavigator(
        activity: GuidedBrushingSettingsActivity,
        factory: GuidedBrushingSettingsNavigator.Factory
    ): GuidedBrushingSettingsNavigator {
        return activity.createNavigatorAndBindToLifecycle(GuidedBrushingSettingsNavigator::class) { factory }
    }
}
