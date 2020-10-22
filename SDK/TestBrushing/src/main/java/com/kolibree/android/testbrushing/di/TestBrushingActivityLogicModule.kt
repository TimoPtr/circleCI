/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.di

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.common.base.Optional
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.testbrushing.TestBrushingActivity
import com.kolibree.android.testbrushing.TestBrushingNavigator
import com.kolibree.android.testbrushing.TestBrushingSharedViewModel
import com.kolibree.android.testbrushing.TestBrushingViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class TestBrushingActivityLogicModule {

    @Binds
    abstract fun bindAppCompatActivity(activity: TestBrushingActivity): AppCompatActivity

    internal companion object {

        @Provides
        @ToothbrushMac
        internal fun provideToothbrushMac(activity: TestBrushingActivity): Optional<String> =
            activity.extractMac()

        @Provides
        internal fun provideToothbrushModel(activity: TestBrushingActivity): ToothbrushModel =
            activity.extractModel()

        @Provides
        internal fun provideSharedViewModel(
            activity: TestBrushingActivity,
            viewModelFactory: TestBrushingViewModel.Factory
        ): TestBrushingSharedViewModel {
            return ViewModelProvider(
                activity,
                viewModelFactory
            ).get(TestBrushingViewModel::class.java)
        }

        @Provides
        fun providesNavigator(activity: TestBrushingActivity): TestBrushingNavigator {
            return activity.createNavigatorAndBindToLifecycle(TestBrushingNavigator::class)
        }
    }
}
