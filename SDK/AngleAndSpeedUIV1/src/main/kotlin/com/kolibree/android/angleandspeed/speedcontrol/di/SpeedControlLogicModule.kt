/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.di

import androidx.lifecycle.Lifecycle
import com.google.common.base.Optional
import com.kolibree.android.angleandspeed.common.logic.AngleAndSpeedModule
import com.kolibree.android.angleandspeed.speedcontrol.mvi.SpeedControlActivity
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.KeepScreenOnControllerImpl
import com.kolibree.android.game.ToothbrushMac
import dagger.Module
import dagger.Provides

@Module(includes = [AngleAndSpeedModule::class])
internal class SpeedControlLogicModule {

    @Provides
    @ActivityScope
    fun providesLifecycle(activity: SpeedControlActivity): Lifecycle = activity.lifecycle

    @Provides
    @ActivityScope
    @ToothbrushMac
    fun providesToothbrushMac(activity: SpeedControlActivity): Optional<String> =
        Optional.of(activity.macAddress)

    @Provides
    @ActivityScope
    fun providesToothbrushModel(activity: SpeedControlActivity): ToothbrushModel = activity.toothbrushModel

    @Provides
    @ActivityScope
    fun providesKeepScreenOnController(activity: SpeedControlActivity): KeepScreenOnController =
        KeepScreenOnControllerImpl(activity)
}
