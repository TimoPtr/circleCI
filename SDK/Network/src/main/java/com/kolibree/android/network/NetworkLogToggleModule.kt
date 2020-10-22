/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network

import android.content.Context
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.crypto.SecurityKeeper
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
object NetworkLogToggleModule {

    @Provides
    fun providesNetworkLogFeatureToggle(
        context: Context,
        securityKeeper: SecurityKeeper
    ): NetworkLogFeatureToggle {
        return NetworkLogFeatureToggle.newInstance(context, securityKeeper)
    }

    @Provides
    @IntoSet
    fun providesNetworkLogFeatureToggleIntoSet(
        networkLogFeatureToggle: NetworkLogFeatureToggle
    ): FeatureToggle<*> {
        return networkLogFeatureToggle
    }
}
