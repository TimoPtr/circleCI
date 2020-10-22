/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.game

import com.kolibree.android.app.ui.selecttoothbrush.SelectToothbrushModule
import dagger.Binds
import dagger.Module

@Module(includes = [SelectToothbrushModule::class])
abstract class StartNonUnityGameModule {

    @Binds
    internal abstract fun bindStartNonUnityGameUseCase(
        implementation: StartNonUnityGameUseCaseImpl
    ): StartNonUnityGameUseCase
}
