/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app

import com.kolibree.android.app.initializers.base.AppInitializer
import com.kolibree.android.app.initializers.di.AppInitializerList
import com.kolibree.android.app.initializers.di.BaseAppInitializerModule
import com.kolibree.android.app.initializers.di.BaseAppInitializers
import dagger.Module
import dagger.Provides

/**
 * Provides initializers for Colgate Connect
 */
@Module(includes = [BaseAppInitializerModule::class])
object AppInitializerModule {

    @Provides
    fun provideColgateInitializers(
        @BaseAppInitializers baseInitializers: AppInitializerList
    ): List<AppInitializer> {
        return baseInitializers
    }
}
