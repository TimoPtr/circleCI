/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushstart

import com.kolibree.android.angleandspeed.testangles.mvi.TestAnglesActivity
import com.kolibree.android.app.mvi.brushstart.BrushStartFragmentModule
import dagger.Module
import dagger.Provides

@Module
object TestAnglesBrushStartModule {

    @Provides
    fun provideBrushStartModuleArgumentProvider(
        activity: TestAnglesActivity
    ): BrushStartFragmentModule.ArgumentProvider {

        return object : BrushStartFragmentModule.ArgumentProvider {

            override fun getPackageName() = activity.packageName

            override fun getToothbrushMac() = activity.macAddress

            override fun getToothbrushModel() = activity.toothbrushModel
        }
    }
}
