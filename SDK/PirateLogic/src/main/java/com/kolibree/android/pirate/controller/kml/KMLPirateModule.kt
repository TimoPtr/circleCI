/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate.controller.kml

import com.google.common.base.Optional
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.pirate.BasePirateFragment
import com.kolibree.android.pirate.crypto.PirateCryptoModule
import com.kolibree.android.pirate.crypto.PirateLanesProvider
import com.kolibree.android.processedbrushings.ProcessedBrushingsModule
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.processedbrushings.crypto.ThresholdProvider
import com.kolibree.android.processedbrushings.crypto.TransitionProvider
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProvider
import com.kolibree.android.sdk.util.KpiSpeedProvider
import com.kolibree.android.sdk.util.KpiSpeedProviderModule
import com.kolibree.android.sdk.util.RnnWeightProvider
import com.kolibree.android.sdk.util.RnnWeightProviderModule
import com.kolibree.kml.PirateHelper
import com.kolibree.kml.SupervisedBrushingAppContext12
import com.kolibree.kml.SupervisedBrushingAppContext16
import com.kolibree.kml.SupervisedBrushingAppContext8
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        RnnWeightProviderModule::class,
        KpiSpeedProviderModule::class,
        ProcessedBrushingsModule::class,
        PirateCryptoModule::class]
)
object KMLPirateModule {

    @Suppress("LongParameterList")
    @Provides
    fun providesSupervisedBrushingAppContext8(
        rnnWeightProvider: RnnWeightProvider?,
        kpiSpeedProvider: KpiSpeedProvider?,
        angleProvider: AngleProvider,
        transitionProvider: TransitionProvider,
        thresholdProvider: ThresholdProvider,
        zoneValidatorProvider: ZoneValidatorProvider,
        toothbrushModel: ToothbrushModel
    ): SupervisedBrushingAppContext8 {

        checkNotNull(rnnWeightProvider) { "WeightProvider should not be null did you provide the TB model" }

        checkNotNull(kpiSpeedProvider) { "KpiSpeedProvider should not be null did you provide the TB model" }

        return SupervisedBrushingAppContext8(
            rnnWeightProvider.getRnnWeight(),
            angleProvider.getSupervisedAngle(),
            angleProvider.getKPIAngle(),
            kpiSpeedProvider.getKpiSpeed(),
            transitionProvider.getTransition(),
            thresholdProvider.getThresholdBalancing(),
            zoneValidatorProvider.getZoneValidator(),
            toothbrushModel.hasOverPressure()
        )
    }

    @Suppress("LongParameterList")
    @Provides
    fun providesSupervisedBrushingAppContext12(
        rnnWeightProvider: RnnWeightProvider?,
        kpiSpeedProvider: KpiSpeedProvider?,
        angleProvider: AngleProvider,
        transitionProvider: TransitionProvider,
        thresholdProvider: ThresholdProvider,
        zoneValidatorProvider: ZoneValidatorProvider,
        toothbrushModel: ToothbrushModel
    ): SupervisedBrushingAppContext12 {

        checkNotNull(rnnWeightProvider) { "WeightProvider should not be null did you provide the TB model" }

        checkNotNull(kpiSpeedProvider) { "KpiSpeedProvider should not be null did you provide the TB model" }

        return SupervisedBrushingAppContext12(
            rnnWeightProvider.getRnnWeight(),
            angleProvider.getSupervisedAngle(),
            angleProvider.getKPIAngle(),
            kpiSpeedProvider.getKpiSpeed(),
            transitionProvider.getTransition(),
            thresholdProvider.getThresholdBalancing(),
            zoneValidatorProvider.getZoneValidator(),
            toothbrushModel.hasOverPressure()
        )
    }

    @Suppress("LongParameterList")
    @Provides
    fun providesSupervisedBrushingAppContext16(
        rnnWeightProvider: RnnWeightProvider?,
        kpiSpeedProvider: KpiSpeedProvider?,
        angleProvider: AngleProvider,
        transitionProvider: TransitionProvider,
        thresholdProvider: ThresholdProvider,
        zoneValidatorProvider: ZoneValidatorProvider,
        toothbrushModel: ToothbrushModel
    ): SupervisedBrushingAppContext16 {

        checkNotNull(rnnWeightProvider) { "WeightProvider should not be null did you provide the TB model" }

        checkNotNull(kpiSpeedProvider) { "KpiSpeedProvider should not be null did you provide the TB model" }

        return SupervisedBrushingAppContext16(
            rnnWeightProvider.getRnnWeight(),
            angleProvider.getSupervisedAngle(),
            angleProvider.getKPIAngle(),
            kpiSpeedProvider.getKpiSpeed(),
            transitionProvider.getTransition(),
            thresholdProvider.getThresholdBalancing(),
            zoneValidatorProvider.getZoneValidator(),
            toothbrushModel.hasOverPressure()
        )
    }

    @Provides
    fun providesPirateHelper(pirateLanesProvider: PirateLanesProvider): PirateHelper =
        PirateHelper(pirateLanesProvider.getPirateLanes())

    @Provides
    fun providesToothbrushModel(pirateFragment: BasePirateFragment): ToothbrushModel =
        checkNotNull(pirateFragment.toothbrushModel) {
            "ToothbrushModel should not be null before the creation of the Fragment"
        }

    @Provides
    @ToothbrushMac
    fun providesToothbrushMac(pirateFragment: BasePirateFragment): Optional<String> =
        Optional.of(pirateFragment.macAddress)
}
