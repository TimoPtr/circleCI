/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.di

import com.kolibree.android.brushingquiz.logic.BrushingProgramUseCase
import com.kolibree.android.brushingquiz.logic.BrushingProgramUtilsImpl
import dagger.Binds
import dagger.Module

/** Brushing Program feature module */
@Module
abstract class BrushingProgramUseCaseModule {
    @Binds
    internal abstract fun bindBrushingProgramUseCaseInternal(impl: BrushingProgramUtilsImpl): BrushingProgramUseCase
}
