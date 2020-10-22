/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import com.kolibree.android.app.dagger.scopes.ActivityScope
import dagger.Binds
import dagger.Module

@Module
internal interface SmileCounterChangedModule {

    @ActivityScope
    @Binds
    fun bindsSmileCounterChangedUseCase(impl: SmileCounterChangedUseCaseImpl): SmileCounterChangedUseCase
}
