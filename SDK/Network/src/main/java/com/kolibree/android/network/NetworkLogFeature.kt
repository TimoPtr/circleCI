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
import androidx.annotation.Keep
import com.kolibree.android.feature.Feature
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.impl.ConstantFeatureToggle
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import com.kolibree.crypto.SecurityKeeper

@Keep
class NetworkLogFeatureToggle(
    internal val implementation: FeatureToggle<Boolean>
) : FeatureToggle<Boolean> by implementation {

    companion object {

        fun newInstance(context: Context, securityKeeper: SecurityKeeper): NetworkLogFeatureToggle {
            @Suppress("ConstantConditionIf")
            return NetworkLogFeatureToggle(
                if (securityKeeper.isLoggingAllowed)
                    PersistentFeatureToggle(context, NetworkLogFeature)
                else ConstantFeatureToggle(NetworkLogFeature, initialValue = false)
            )
        }
    }
}

@Keep
object NetworkLogFeature : Feature<Boolean> {

    override val initialValue = BuildConfig.DEBUG

    override val displayable = true

    override val displayName = "Enable Network Logs"

    override val requiresAppRestart = true
}
