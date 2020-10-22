/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.network.environment.DefaultEnvironment
import com.kolibree.android.network.environment.Environment
import dagger.Module
import dagger.Provides

@Module
object VariantModule {
    @Provides
    fun providesDefaultEnvironment(): DefaultEnvironment {
        return DefaultEnvironment(Environment.PRODUCTION)
    }
}
