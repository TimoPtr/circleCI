/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import dagger.Binds
import dagger.Module

/** Brushing Program feature module */
@Module
internal abstract class BrushingProgramModule {

    @Binds
    internal abstract fun bindBrushingModeRepository(impl: BrushingModePerProfileRepository): BrushingModeRepository
}
