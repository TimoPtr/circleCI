/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.tilt.gyroscopic

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorManager
import dagger.Binds
import dagger.Module
import dagger.Provides

/** Jaws sensors module */
@Module
internal abstract class JawsSensorsModule {

    @Binds
    abstract fun bindGyroscopeSensorInteractor(
        impl: GyroscopeSensorInteractorImpl
    ): GyroscopeSensorInteractor

    companion object {

        @Provides
        fun provideSensorManagerWrapper(context: Context): SensorManagerWrapper =
            SensorManagerWrapperImpl(
                context
                    .getSystemService(SENSOR_SERVICE)
                    as SensorManager
            )
    }
}
