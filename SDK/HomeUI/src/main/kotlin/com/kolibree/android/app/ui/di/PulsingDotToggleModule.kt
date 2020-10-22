/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.di

import android.content.Context
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.PulsingDotFeature
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
object PulsingDotToggleModule {
    @Provides
    @IntoSet
    fun providePulsingDotFeatureToggle(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(context, PulsingDotFeature)
}
