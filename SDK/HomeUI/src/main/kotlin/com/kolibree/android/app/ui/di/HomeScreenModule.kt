/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.di

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.createAndBindToLifecycle
import com.kolibree.android.app.ui.brushing.BrushingsForCurrentProfileUseCase
import com.kolibree.android.app.ui.brushing.BrushingsForCurrentProfileUseCaseImpl
import com.kolibree.android.app.ui.game.DefaultUserActivityUseCase
import com.kolibree.android.app.ui.game.DefaultUserActivityUseCaseImpl
import com.kolibree.android.app.ui.otachecker.OtaCheckerViewModel
import com.kolibree.android.app.ui.profile.NonActiveProfilesUseCase
import com.kolibree.android.app.ui.profile.NonActiveProfilesUseCaseImpl
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.ToolbarToothbrushViewModel
import com.kolibree.android.app.ui.toolbartoothbrush.legacy.formatter.ToolbarToothbrushFormatter
import com.kolibree.android.offlinebrushings.sync.LastSyncData
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

@Module(
    includes = [
        HomeScreenSharedLogicModule::class,
        SchedulerModule::class,
        DefaultUserActivityUseCaseModule::class,
        NonActiveProfileUseCaseModule::class,
        BrushingUseCaseModule::class
    ]
)
object HomeScreenModule

@Module
class HomeScreenSharedLogicModule {

    // TODO remove once V1 UI is removed completely and ToolbarToothbrushViewModel
    //  will be stripped from UI
    @Provides
    fun provideToolbarToothbrushFormatter(): ToolbarToothbrushFormatter {
        return object : ToolbarToothbrushFormatter {
            override fun format(data: LastSyncData) = ""
        }
    }

    @Provides
    internal fun provideToothbrushConnectionStateViewModel(
        activity: BaseMVIActivity<*, *, *, *, *>,
        viewModelFactory: ToothbrushConnectionStateViewModel.Factory
    ): ToothbrushConnectionStateViewModel = viewModelFactory.createAndBindToLifecycle(
        activity,
        ToothbrushConnectionStateViewModel::class.java
    )

    // TODO refactor once ToolbarToothbrushViewModel will be `BaseViewModel`
    @Provides
    fun provideToolbarToothbrushViewModel(
        activity: BaseMVIActivity<*, *, *, *, *>,
        viewModelFactory: ToolbarToothbrushViewModel.Factory
    ): ToolbarToothbrushViewModel = ViewModelProvider(
        activity,
        viewModelFactory
    ).get(ToolbarToothbrushViewModel::class.java)

    // TODO refactor once ToolbarToothbrushViewModel will be `BaseViewModel`
    @Provides
    fun provideLifecycleOwner(activity: BaseMVIActivity<*, *, *, *, *>): LifecycleOwner =
        activity

    // TODO refactor once OtaCheckerViewModel will be `BaseViewModel`
    @Provides
    fun provideOtaCheckerViewModel(
        activity: BaseMVIActivity<*, *, *, *, *>,
        viewModelFactory: OtaCheckerViewModel.Factory
    ): OtaCheckerViewModel {
        val viewModel =
            ViewModelProvider(activity, viewModelFactory).get(OtaCheckerViewModel::class.java)
        activity.getLifecycle().addObserver(viewModel)
        return viewModel
    }
}

@Module
internal abstract class DefaultUserActivityUseCaseModule {

    @Binds
    internal abstract fun bindDefaultUserActivityUseCase(
        implementation: DefaultUserActivityUseCaseImpl
    ): DefaultUserActivityUseCase
}

@Module
internal abstract class BrushingUseCaseModule {

    @Binds
    internal abstract fun bindBrushingUseCase(
        implementation: BrushingsForCurrentProfileUseCaseImpl
    ): BrushingsForCurrentProfileUseCase
}

@Module
internal abstract class NonActiveProfileUseCaseModule {

    @Binds
    internal abstract fun bindNonActiveProfilesUseCase(
        implementation: NonActiveProfilesUseCaseImpl
    ): NonActiveProfilesUseCase
}

@Module
internal object SchedulerModule {

    @Provides
    internal fun provideScheduler(): Scheduler {
        return Schedulers.io()
    }
}
