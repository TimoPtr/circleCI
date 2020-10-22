/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.di

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.createAndBindToLifecycle
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.ui.home.HomeScreenActivity
import com.kolibree.android.app.ui.home.popup.di.LowBatteryModule
import com.kolibree.android.app.ui.home.popup.di.OfflineBrushingRetrievalModule
import com.kolibree.android.app.ui.home.popup.di.SnackbarsPriorityDisplayModule
import com.kolibree.android.app.ui.home.popup.di.TestBrushingPriorityDisplayModule
import com.kolibree.android.app.ui.home.popup.di.ToolboxPriorityDisplayModule
import com.kolibree.android.app.ui.home.popup.di.ToothbrushReplacementModule
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmileCounterChangedModule
import com.kolibree.android.app.ui.home.toolbar.HomeToolbarViewModel
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewModel
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority
import com.kolibree.android.app.ui.priority.AsyncDisplayItemUseCase
import com.kolibree.android.app.ui.priority.AsyncDisplayItemUseCaseFactory
import com.kolibree.android.app.ui.priority.DisplayPriorityItemUseCase
import com.kolibree.android.app.ui.priority.DisplayPriorityUseCaseFactory
import com.kolibree.android.app.ui.selectavatar.SelectAvatarDialogModule
import com.kolibree.android.google.auth.GoogleSignInModule
import com.kolibree.android.utils.KLQueue
import com.kolibree.android.utils.KLQueueFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import javax.inject.Qualifier

@Module(
    includes = [
        GoogleSignInModule::class,
        HomeScreenFragmentModule::class,
        HomeScreenActivityModule::class,
        SmileCounterChangedModule::class,
        SelectAvatarDialogModule::class
    ]
)
abstract class HomeScreenActivityBindingModule {

    @Binds
    abstract fun bindMviActivity(
        implementation: HomeScreenActivity
    ): BaseMVIActivity<*, *, *, *, *>

    @Binds
    abstract fun bindAppCompatActivity(
        implementation: HomeScreenActivity
    ): AppCompatActivity
}

@Module(
    includes = [
        SnackbarsPriorityDisplayModule::class,
        TestBrushingPriorityDisplayModule::class,
        ToolboxPriorityDisplayModule::class,
        LowBatteryModule::class,
        ToothbrushReplacementModule::class,
        OfflineBrushingRetrievalModule::class
    ]
)
internal class HomeScreenActivityModule {

    @Provides
    fun provideToolbarViewModel(
        activity: BaseMVIActivity<*, *, *, *, *>,
        viewModelFactory: HomeToolbarViewModel.Factory
    ): HomeToolbarViewModel =
        viewModelFactory.createAndBindToLifecycle(activity, HomeToolbarViewModel::class.java)

    @Provides
    fun provideToolboxViewModel(
        activity: BaseMVIActivity<*, *, *, *, *>,
        viewModelFactory: ToolboxViewModel.Factory
    ): ToolboxViewModel =
        viewModelFactory.createAndBindToLifecycle(activity, ToolboxViewModel::class.java)
}

@Module
class HomeScreenDisplayPriorityModule {

    @Retention(AnnotationRetention.BINARY)
    @Qualifier
    private annotation class HomeQueue

    @Provides
    @HomeQueue
    fun provideQueue(@SingleThreadScheduler scheduler: Scheduler): KLQueue {
        return KLQueueFactory.creates(delayScheduler = scheduler)
    }

    @Provides
    @ActivityScope
    fun provideDisplayPriorityUseCase(
        @HomeQueue queue: KLQueue
    ): DisplayPriorityItemUseCase<HomeDisplayPriority> {
        return DisplayPriorityUseCaseFactory.create(queue)
    }

    @Provides
    @ActivityScope
    fun provideOfflineBrushingAsyncDisplayUseCase(
        @HomeQueue queue: KLQueue
    ): AsyncDisplayItemUseCase<HomeDisplayPriority.OfflineBrushing> {
        return AsyncDisplayItemUseCaseFactory.create(queue)
    }
}
