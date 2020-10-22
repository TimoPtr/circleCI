/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui.mindyourspeed.di

import com.kolibree.android.angleandspeed.ui.mindyourspeed.MindYourSpeedActivity
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.game.GameScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MindYourSpeedModule {

    @ActivityScope
    @GameScope
    @ContributesAndroidInjector(modules = [MindYourSpeedActivityModule::class])
    internal abstract fun bindMindYourSpeedActivity(): MindYourSpeedActivity
}
