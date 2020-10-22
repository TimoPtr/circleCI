/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.mvi.brushstart

import androidx.annotation.Keep
import com.kolibree.android.commons.ToothbrushModel
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class BrushStartFragmentModule {

    @Keep
    interface ArgumentProvider {

        fun getPackageName(): String

        fun getToothbrushMac(): String

        fun getToothbrushModel(): ToothbrushModel
    }

    @Provides
    @Named(BrushStartConstants.Argument.PACKAGE_NAME)
    fun providesPackageName(provider: ArgumentProvider): String = provider.getPackageName()

    @Provides
    fun provideBrushStartResourceProvider(): BrushStartResourceProvider {
        return object : BrushStartResourceProvider {}
    }
}
