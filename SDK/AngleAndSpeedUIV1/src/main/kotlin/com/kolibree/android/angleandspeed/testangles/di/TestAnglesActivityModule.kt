/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.di

import com.kolibree.android.angleandspeed.testangles.mvi.brushing.incisor.TestAnglesIncisorBrushingFragment
import com.kolibree.android.angleandspeed.testangles.mvi.brushing.molar.TestAnglesMolarBrushingFragment
import com.kolibree.android.angleandspeed.testangles.mvi.brushstart.TestAnglesBrushStartFragment
import com.kolibree.android.angleandspeed.testangles.mvi.brushstart.TestAnglesBrushStartModule
import com.kolibree.android.angleandspeed.testangles.mvi.confirmation.TestAnglesConfirmationFragment
import com.kolibree.android.angleandspeed.testangles.mvi.intro.TestAnglesIntroFragment
import com.kolibree.android.app.dagger.LostConnectionModule
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.app.mvi.brushstart.BrushStartFragmentModule
import com.kolibree.android.game.GameScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(
    includes = [
        TestAnglesFragmentBindingModule::class,
        TestAnglesLogicModule::class,
        LostConnectionModule::class
    ]
)
class TestAnglesActivityModule

@Module
abstract class TestAnglesFragmentBindingModule {

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeTestAnglesIntroFragment(): TestAnglesIntroFragment

    @FragmentScope
    @GameScope
    @ContributesAndroidInjector(
        modules = [
            BrushStartFragmentModule::class,
            TestAnglesBrushStartModule::class
        ]
    )
    internal abstract fun contributeTestAnglesBrushStartFragment(): TestAnglesBrushStartFragment

    @FragmentScope
    @GameScope
    @ContributesAndroidInjector
    internal abstract fun contributeTestAnglesMolarBrushingFragment(): TestAnglesMolarBrushingFragment

    @FragmentScope
    @GameScope
    @ContributesAndroidInjector
    internal abstract fun contributeTestAnglesIncisorBrushingFragment(): TestAnglesIncisorBrushingFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeTestAnglesConfirmationFragment(): TestAnglesConfirmationFragment
}
