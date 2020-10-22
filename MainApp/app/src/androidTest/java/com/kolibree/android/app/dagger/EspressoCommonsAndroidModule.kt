/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.android.app.dagger

import com.kolibree.android.location.LocationStatus
import dagger.Module
import dagger.Provides
import org.mockito.Mockito

@Module(includes = [CommonsAndroidCoreModule::class])
internal object EspressoCommonsAndroidModule {
    @AppScope
    @Provides
    fun providesLocationStatus(): LocationStatus {
        return Mockito.mock(LocationStatus::class.java)
    }
}
