/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.di

import com.kolibree.android.testbrushing.TestBrushingActivity
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class BrushingStartFragmentModule {

    @Provides
    @Named(PACKAGE_NAME)
    internal fun providesPackageName(activity: TestBrushingActivity): String =
        activity.packageName
}

const val PACKAGE_NAME = "HumBrushingStartFragmentModule.Argument.PACKAGE_NAME"
