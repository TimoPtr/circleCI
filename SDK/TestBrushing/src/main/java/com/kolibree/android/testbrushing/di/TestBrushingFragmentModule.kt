/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.di

import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.game.GameScope
import com.kolibree.android.testbrushing.ongoing.OngoingBrushingFragment
import com.kolibree.android.testbrushing.start.TestBrushingStartFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class TestBrushingFragmentModule {

    @FragmentScope
    @GameScope
    @ContributesAndroidInjector(modules = [BrushingStartFragmentModule::class])
    internal abstract fun contributeBrushingStartFragment(): TestBrushingStartFragment

    @FragmentScope
    @GameScope
    @ContributesAndroidInjector(modules = [TestBrushingGameLogicModule::class])
    internal abstract fun contributeOngoingBrushingFragment(): OngoingBrushingFragment
}
