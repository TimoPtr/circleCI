/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.environment

import com.kolibree.android.app.dagger.AppScope
import dagger.Binds
import dagger.Module

@Module
abstract class EnvironmentManagerModule {

    @Binds
    @AppScope
    internal abstract fun bindsEnvironmentManager(impl: EnvironmentManagerImpl): EnvironmentManager
}
