/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.di

import android.content.Context
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.GuidedBrushingTipsFeature
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
object GuidedBrushingTipsToggleModule {

    @Provides
    @IntoSet
    fun providesGuidedBrushingTipsFeatureToggle(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(context, GuidedBrushingTipsFeature)
}
