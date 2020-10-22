/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.brushstart

import com.kolibree.android.angleandspeed.speedcontrol.mvi.SpeedControlActivity
import com.kolibree.android.app.mvi.brushstart.BrushStartFragmentModule
import dagger.Module
import dagger.Provides

@Module
object SpeedControlBrushStartModule {

    @Provides
    fun provideBrushStartModuleArgumentProvider(
        activity: SpeedControlActivity
    ): BrushStartFragmentModule.ArgumentProvider {

        return object : BrushStartFragmentModule.ArgumentProvider {

            override fun getPackageName() = activity.packageName

            override fun getToothbrushMac() = activity.macAddress

            override fun getToothbrushModel() = activity.toothbrushModel
        }
    }
}
