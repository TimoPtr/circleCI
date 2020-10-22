/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet

import android.content.Context
import androidx.annotation.Keep
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.GooglePayProductionEnvironmentFeature
import com.kolibree.android.feature.impl.ConstantFeatureToggle
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import com.kolibree.crypto.SecurityKeeper
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Keep
class GooglePayProductionEnvironmentFeatureToggle(
    internal val implementation: FeatureToggle<Boolean>
) : FeatureToggle<Boolean> by implementation {

    companion object {

        fun newInstance(
            context: Context,
            securityKeeper: SecurityKeeper
        ): GooglePayProductionEnvironmentFeatureToggle {
            @Suppress("ConstantConditionIf")
            return GooglePayProductionEnvironmentFeatureToggle(
                if (securityKeeper.testFeaturesAllowed)
                    PersistentFeatureToggle(context, GooglePayProductionEnvironmentFeature)
                else
                    ConstantFeatureToggle(
                        GooglePayProductionEnvironmentFeature,
                        initialValue = true
                    )
            )
        }
    }
}

@Module
object GooglePayEnvironmentFeatureToggleModule {

    @Provides
    @IntoSet
    fun provideGooglePayProductionEnvironmentFeatureToggle(
        context: Context,
        securityKeeper: SecurityKeeper
    ): FeatureToggle<*> {
        return GooglePayProductionEnvironmentFeatureToggle.newInstance(context, securityKeeper)
    }
}
