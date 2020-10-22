/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.confirmation

import com.kolibree.android.brushingquiz.di.BrushingProgramUseCaseModule
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.ConfirmBrushingModeModule
import dagger.Module
import dagger.Provides

@Module(includes = [BrushingProgramUseCaseModule::class, ConfirmBrushingModeModule::class])
internal object QuizConfirmationModule {
    @Provides
    fun providesSelectedBrushingMode(fragment: QuizConfirmationFragment): BrushingMode {
        return fragment.selectedBrushingMode()
    }
}
