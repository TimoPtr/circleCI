/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.lifecycle

import dagger.Binds
import dagger.Module

@Module
abstract class GameLifecycleModule {
    @Binds
    abstract fun providesGameLifecycleProvider(impl: GameLifecycleCoordinatorImpl): GameLifecycleProvider

    @Binds
    abstract fun providesGameLifecycleCoordinator(impl: GameLifecycleCoordinatorImpl): GameLifecycleCoordinator
}
