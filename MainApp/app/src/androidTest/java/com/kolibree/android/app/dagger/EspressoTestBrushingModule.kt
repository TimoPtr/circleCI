/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.app.disconnection.LostConnectionModule
import com.kolibree.android.game.BrushingCreatorModule
import com.kolibree.android.game.GameScope
import com.kolibree.android.game.bi.AvroCreatorModule
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.processedbrushings.crypto.ThresholdProvider
import com.kolibree.android.processedbrushings.crypto.TransitionProvider
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProvider
import com.kolibree.android.sdk.util.KpiSpeedProvider
import com.kolibree.android.sdk.util.KpiSpeedProviderModule
import com.kolibree.android.sdk.util.RnnWeightProvider
import com.kolibree.android.sdk.util.RnnWeightProviderModule
import com.kolibree.android.testbrushing.TestBrushingActivity
import com.kolibree.android.testbrushing.di.BrushingStartFragmentModule
import com.kolibree.android.testbrushing.di.TestBrushingActivityLogicModule
import com.kolibree.android.testbrushing.di.TestBrushingGameLogicProviderModule
import com.kolibree.android.testbrushing.di.TestBrushingLogicBindingModule
import com.kolibree.android.testbrushing.di.TestBrushingLostConnectionDialogModule
import com.kolibree.android.testbrushing.ongoing.OngoingBrushingFragment
import com.kolibree.android.testbrushing.start.TestBrushingStartFragment
import com.kolibree.kml.FreeBrushingAppContext
import com.nhaarman.mockitokotlin2.spy
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
abstract class EspressoTestBrushingModule {

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [
            TestBrushingActivityLogicModule::class,
            EspressoTestBrushingFragmentModule::class,
            LostConnectionModule::class
        ]
    )
    internal abstract fun bindHumTestBrushingActivity(): TestBrushingActivity
}

@Module
abstract class EspressoTestBrushingFragmentModule {

    @FragmentScope
    @GameScope
    @ContributesAndroidInjector(modules = [BrushingStartFragmentModule::class])
    internal abstract fun contributeBrushingStartFragment(): TestBrushingStartFragment

    @FragmentScope
    @GameScope
    @ContributesAndroidInjector(modules = [EspressoTestBrushingGameLogicModule::class])
    internal abstract fun contributeOngoingBrushingFragment(): OngoingBrushingFragment
}

@Module(
    includes = [
        TestBrushingLogicBindingModule::class,
        TestBrushingGameLogicProviderModule::class,
        TestBrushingLostConnectionDialogModule::class,
        RnnWeightProviderModule::class,
        KpiSpeedProviderModule::class,
        AvroCreatorModule::class,
        BrushingCreatorModule::class
    ]
)
object EspressoTestBrushingGameLogicModule {

    lateinit var appContext: FreeBrushingAppContext

    @Provides
    @GameScope
    fun provideAppContext(
        rnnWeightProvider: RnnWeightProvider?,
        angleProvider: AngleProvider,
        kpiSpeedProvider: KpiSpeedProvider?,
        transitionProvider: TransitionProvider,
        thresholdProvider: ThresholdProvider,
        zoneValidatorProvider: ZoneValidatorProvider
    ): FreeBrushingAppContext {
        if (!this::appContext.isInitialized) {
            appContext = spy(
                FreeBrushingAppContext(
                    requireNotNull(rnnWeightProvider?.getRnnWeight()),
                    angleProvider.getKPIAngle(),
                    requireNotNull(kpiSpeedProvider?.getKpiSpeed()),
                    transitionProvider.getTransition(),
                    thresholdProvider.getThresholdBalancing(),
                    zoneValidatorProvider.getZoneValidator(),
                    false // FIXME
                )
            )
        }
        return appContext
    }
}
