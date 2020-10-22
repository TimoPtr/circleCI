/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.di

import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.testbrushing.TestBrushingActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class TestBrushingModule {

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [
            TestBrushingActivityModule::class,
            TestBrushingFragmentModule::class
        ]
    )
    internal abstract fun bindTestBrushingActivity(): TestBrushingActivity
}
