/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.di

import android.content.Context
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.HeadspaceFeature
import com.kolibree.android.feature.impl.ConstantFeatureToggle
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
object HeadspaceFeatureToggleModule {

    @Provides
    @IntoSet
    fun provideHeadspaceFeatureToggle(
        context: Context,
        appConfiguration: AppConfiguration
    ): FeatureToggle<*> =
        if (appConfiguration.showHeadspaceRelatedContent)
            PersistentFeatureToggle(context, HeadspaceFeature)
        else ConstantFeatureToggle(HeadspaceFeature, initialValue = false)
}
