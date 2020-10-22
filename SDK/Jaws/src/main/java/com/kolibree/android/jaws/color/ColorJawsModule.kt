/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.color

import android.view.animation.DecelerateInterpolator
import com.kolibree.android.jaws.tilt.JawsTiltModule
import dagger.Binds
import dagger.Module
import dagger.Provides

/**
 * Color Jaws module
 *
 * Provides [ColorJawsRenderer]
 */
@Module(includes = [JawsTiltModule::class])
abstract class ColorJawsModule {

    @Binds
    internal abstract fun bindColorJawsRenderer(impl: ColorJawsRendererImpl): ColorJawsRenderer

    internal companion object {

        @Provides
        fun provideInterpolator(): DecelerateInterpolator = DecelerateInterpolator()
    }
}
