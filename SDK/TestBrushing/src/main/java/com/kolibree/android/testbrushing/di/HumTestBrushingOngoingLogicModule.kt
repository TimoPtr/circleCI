/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.di

import android.view.ContextThemeWrapper
import com.kolibree.android.app.disconnection.LostConnectionDialogController
import com.kolibree.android.baseui.hum.R
import com.kolibree.android.game.BrushingCreatorModule
import com.kolibree.android.game.GameScope
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.KeepScreenOnControllerImpl
import com.kolibree.android.game.bi.AvroCreatorModule
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.util.KpiSpeedProviderModule
import com.kolibree.android.sdk.util.RnnWeightProviderModule
import com.kolibree.android.testbrushing.TestBrushingActivity
import com.kolibree.android.testbrushing.ongoing.OngoingBrushingFragment
import com.kolibree.android.testbrushing.shared.TestBrushingSensorConfigurationFactory
import com.kolibree.android.testbrushing.shared.TestBrushingUseCase
import com.kolibree.android.testbrushing.shared.TestBrushingUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        TestBrushingLogicBindingModule::class,
        TestBrushingGameLogicProviderModule::class,
        TestBrushingKmlContextModule::class,
        TestBrushingLostConnectionDialogModule::class,
        RnnWeightProviderModule::class,
        KpiSpeedProviderModule::class,
        AvroCreatorModule::class,
        BrushingCreatorModule::class
    ]
)
class TestBrushingGameLogicModule

@Module
class TestBrushingLostConnectionDialogModule {

    @Provides
    fun provideLostConnectionDialogController(
        fragment: OngoingBrushingFragment
    ): LostConnectionDialogController {
        return LostConnectionDialogController(
            ContextThemeWrapper(fragment.requireContext(), R.style.ThemeOverlay_Dialog_Inverse),
            fragment
        )
    }
}

@Module
class TestBrushingGameLogicProviderModule {

    @Provides
    fun providesKeepScreenOnController(
        activity: TestBrushingActivity
    ): KeepScreenOnController = KeepScreenOnControllerImpl(activity)

    @Provides
    internal fun providesGameToothbrushInteractorFacade(
        activity: TestBrushingActivity,
        holder: TestBrushingUseCase
    ): GameToothbrushInteractorFacade = GameToothbrushInteractorFacade(
        activity.applicationContext,
        TestBrushingSensorConfigurationFactory,
        holder,
        activity.lifecycle
    )
}

@Module
abstract class TestBrushingLogicBindingModule {

    @Binds
    @GameScope
    internal abstract fun bindHumTestBrushingUseCase(
        impl: TestBrushingUseCaseImpl
    ): TestBrushingUseCase
}
