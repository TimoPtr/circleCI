/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.di

import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.createAndBindToLifecycle
import com.kolibree.android.app.ui.home.popup.toolbox.ToolboxPriorityDisplayViewModel
import com.kolibree.android.app.ui.home.popup.toolbox.ToolboxPriorityDisplayViewModel.Factory.Companion.ToolboxPulsingDot
import com.kolibree.android.app.ui.home.pulsingdot.di.PulsingDotModule
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCase
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [PulsingDotModule::class])
internal abstract class ToolboxPriorityDisplayModule {

    @Binds
    @ToolboxPulsingDot
    internal abstract fun bindPulsingDotUseCase(implementation: PulsingDotUseCaseImpl): PulsingDotUseCase

    companion object {

        @Provides
        fun provideToolboxExplanationViewModel(
            activity: BaseMVIActivity<*, *, *, *, *>,
            viewModelFactory: ToolboxPriorityDisplayViewModel.Factory
        ): ToolboxPriorityDisplayViewModel =
            viewModelFactory.createAndBindToLifecycle(
                activity,
                ToolboxPriorityDisplayViewModel::class.java
            )
    }
}
