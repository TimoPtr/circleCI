/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.tilt

import com.kolibree.android.jaws.tilt.animated.AnimatedTiltController
import com.kolibree.android.jaws.tilt.animated.AnimatedTiltControllerImpl
import com.kolibree.android.jaws.tilt.gyroscopic.GyroscopicJawsTiltController
import com.kolibree.android.jaws.tilt.gyroscopic.GyroscopicJawsTiltControllerImpl
import com.kolibree.android.jaws.tilt.gyroscopic.JawsSensorsModule
import com.kolibree.android.jaws.tilt.touch.TouchJawsTiltController
import com.kolibree.android.jaws.tilt.touch.TouchJawsTiltControllerImpl
import dagger.Binds
import dagger.Module

@Module(includes = [JawsSensorsModule::class])
internal interface JawsTiltModule {

    @Binds
    fun bindGyroscopicJawsTiltController(
        impl: GyroscopicJawsTiltControllerImpl
    ): GyroscopicJawsTiltController

    @Binds
    fun bindTouchJawsTiltController(
        impl: TouchJawsTiltControllerImpl
    ): TouchJawsTiltController

    @Binds
    fun bindAnimatedTiltController(
        impl: AnimatedTiltControllerImpl
    ): AnimatedTiltController
}
