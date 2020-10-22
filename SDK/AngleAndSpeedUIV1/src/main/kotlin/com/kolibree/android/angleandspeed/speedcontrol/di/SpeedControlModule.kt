/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.di

import com.kolibree.android.angleandspeed.speedcontrol.mvi.SpeedControlActivity
import com.kolibree.android.app.dagger.scopes.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SpeedControlModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [SpeedControlActivityModule::class])
    internal abstract fun bindSpeedControlActivity(): SpeedControlActivity
}
