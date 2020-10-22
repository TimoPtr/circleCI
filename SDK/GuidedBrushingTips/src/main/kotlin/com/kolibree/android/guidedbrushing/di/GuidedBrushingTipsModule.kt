/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.di

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.guidedbrushing.data.BrushingTipsProvider
import com.kolibree.android.guidedbrushing.data.BrushingTipsProviderImpl
import com.kolibree.android.guidedbrushing.domain.BrushingTipsUseCase
import com.kolibree.android.guidedbrushing.domain.BrushingTipsUseCaseImpl
import com.kolibree.android.guidedbrushing.ui.GuidedBrushingTipsActivity
import com.kolibree.android.guidedbrushing.ui.navigator.GuidedBrushingTipsNavigator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module(includes = [GuidedBrushingTipsDataModule::class, GuidedBrushingTipsDomainModule::class])
abstract class GuidedBrushingTipsModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [GuidedBrushingTipsUiModule::class])
    internal abstract fun bindGuidedBrushingTipsActivity(): GuidedBrushingTipsActivity
}

@Module
internal abstract class GuidedBrushingTipsUiModule {

    @Binds
    internal abstract fun bindAppCompatActivity(activity: GuidedBrushingTipsActivity): AppCompatActivity

    internal companion object {
        @Provides
        internal fun providesNavigator(
            activity: GuidedBrushingTipsActivity
        ): GuidedBrushingTipsNavigator {
            return activity.createNavigatorAndBindToLifecycle(GuidedBrushingTipsNavigator::class)
        }
    }
}

@Module
internal abstract class GuidedBrushingTipsDataModule {
    @Binds
    internal abstract fun bindBrushingTipsProvider(impl: BrushingTipsProviderImpl): BrushingTipsProvider
}

@Module
internal abstract class GuidedBrushingTipsDomainModule {
    @Binds
    internal abstract fun bindBrushingTipsUseCase(impl: BrushingTipsUseCaseImpl): BrushingTipsUseCase
}
