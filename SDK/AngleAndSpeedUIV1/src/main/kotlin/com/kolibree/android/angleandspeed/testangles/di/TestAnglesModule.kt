/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.di

import com.kolibree.android.angleandspeed.testangles.mvi.TestAnglesActivity
import com.kolibree.android.app.dagger.scopes.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class TestAnglesModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [TestAnglesActivityModule::class])
    internal abstract fun bindTestAnglesActivity(): TestAnglesActivity
}
