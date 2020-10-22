/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota.feature

import android.content.Context
import com.kolibree.android.feature.AlwaysOfferOtaUpdateFeature
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
class AlwaysOfferOtaUpdateModule {

    @Provides
    @IntoSet
    fun provideAlwaysOfferOtaUpdateFeatureToggle(context: Context): FeatureToggle<*> {
        return PersistentFeatureToggle(context, AlwaysOfferOtaUpdateFeature)
    }
}
