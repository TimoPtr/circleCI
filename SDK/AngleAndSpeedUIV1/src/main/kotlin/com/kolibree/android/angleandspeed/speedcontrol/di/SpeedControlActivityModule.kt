/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.di

import com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing.SpeedControlBrushingFragment
import com.kolibree.android.angleandspeed.speedcontrol.mvi.brushstart.SpeedControlBrushStartFragment
import com.kolibree.android.angleandspeed.speedcontrol.mvi.brushstart.SpeedControlBrushStartModule
import com.kolibree.android.angleandspeed.speedcontrol.mvi.confirmation.SpeedControlConfirmationFragment
import com.kolibree.android.angleandspeed.speedcontrol.mvi.intro.SpeedControlIntroFragment
import com.kolibree.android.app.dagger.LostConnectionModule
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.app.mvi.brushstart.BrushStartFragmentModule
import com.kolibree.android.game.GameScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(
    includes = [
        SpeedControlLogicModule::class,
        SpeedControlFragmentBindingModule::class,
        LostConnectionModule::class
    ]
)
class SpeedControlActivityModule

@Module
abstract class SpeedControlFragmentBindingModule {

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeSpeedControlIntroFragment(): SpeedControlIntroFragment

    @FragmentScope
    @GameScope
    @ContributesAndroidInjector(
        modules = [
            BrushStartFragmentModule::class,
            SpeedControlBrushStartModule::class
        ]
    )
    internal abstract fun contributeSpeedControlBrushStartFragment(): SpeedControlBrushStartFragment

    @FragmentScope
    @GameScope
    @ContributesAndroidInjector
    internal abstract fun contributeSpeedControlBrushingFragment(): SpeedControlBrushingFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeSpeedControlConfirmationFragment(): SpeedControlConfirmationFragment
}
