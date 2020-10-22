/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.pairing

import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import dagger.Module
import dagger.Provides

@Module
object PairingModule {

    @Provides
    internal fun providesHumHomeNavigator(
        activity: PairingActivity,
        factory: PairingNavigatorImpl.Factory
    ): PairingNavigator {
        return activity.createNavigatorAndBindToLifecycle(PairingNavigatorImpl::class) { factory }
    }
}
