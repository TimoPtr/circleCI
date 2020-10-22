/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui.mindyourspeed.di

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.common.base.Optional
import com.kolibree.android.angleandspeed.ui.mindyourspeed.MindYourSpeedActivity
import com.kolibree.android.angleandspeed.ui.mindyourspeed.MindYourSpeedNavigator
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.KeepScreenOnControllerImpl
import com.kolibree.android.game.ToothbrushMac
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class MindYourSpeedActivityLogicModule {

    @Binds
    internal abstract fun bindAppCompatActivity(activity: MindYourSpeedActivity): AppCompatActivity

    internal companion object {

        @Provides
        @ActivityScope
        internal fun provideLifecycle(activity: MindYourSpeedActivity): Lifecycle =
            activity.lifecycle

        @Provides
        @ToothbrushMac
        @ActivityScope
        internal fun provideToothbrushMac(activity: MindYourSpeedActivity): Optional<String> =
            Optional.of(requireNotNull(
                activity.readMacFromIntent(),
                { "This activity doesn't support manual mode, you need to provide TB MAC address" }
            ))

        @Provides
        @ActivityScope
        internal fun provideToothbrushModel(activity: MindYourSpeedActivity): ToothbrushModel =
            activity.readModelFromIntent()

        @Provides
        @ActivityScope
        fun providesKeepScreenOnController(activity: MindYourSpeedActivity): KeepScreenOnController =
            KeepScreenOnControllerImpl(activity)

        @Provides
        @ActivityScope
        fun providesNavigator(activity: MindYourSpeedActivity): MindYourSpeedNavigator {
            return activity.createNavigatorAndBindToLifecycle(MindYourSpeedNavigator::class)
        }
    }
}
