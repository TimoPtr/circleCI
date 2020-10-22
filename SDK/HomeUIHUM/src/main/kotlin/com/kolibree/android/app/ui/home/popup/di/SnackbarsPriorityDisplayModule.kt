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
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.ui.home.popup.SnackbarsPriorityDisplayViewModel
import com.kolibree.android.app.ui.home.popup.snackbar.SnackbarBluetoothViewModel
import com.kolibree.android.app.ui.home.popup.snackbar.SnackbarLocationViewModel
import com.kolibree.android.utils.KLQueue
import com.kolibree.android.utils.KLQueueFactory
import com.kolibree.android.utils.KLQueueModule
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler

@Module(includes = [KLQueueModule::class])
internal class SnackbarsPriorityDisplayModule {
    @Provides
    fun provideSnackbarsPriorityDisplayViewModel(
        activity: BaseMVIActivity<*, *, *, *, *>,
        factory: SnackbarsPriorityDisplayViewModel.Factory
    ): SnackbarsPriorityDisplayViewModel =
        factory.createAndBindToLifecycle(activity, SnackbarsPriorityDisplayViewModel::class.java)

    @Provides
    fun provideSnackbarLocationViewModel(
        activity: BaseMVIActivity<*, *, *, *, *>,
        factory: SnackbarLocationViewModel.Factory
    ): SnackbarLocationViewModel =
        factory.createAndBindToLifecycle(activity, SnackbarLocationViewModel::class.java)

    @Provides
    fun provideSnackbarBluetoothViewModel(
        activity: BaseMVIActivity<*, *, *, *, *>,
        factory: SnackbarBluetoothViewModel.Factory
    ): SnackbarBluetoothViewModel =
        factory.createAndBindToLifecycle(activity, SnackbarBluetoothViewModel::class.java)

    @ActivityScope
    @Provides
    fun providesQueue(@SingleThreadScheduler scheduler: Scheduler): KLQueue =
        KLQueueFactory.creates(scheduler)
}
