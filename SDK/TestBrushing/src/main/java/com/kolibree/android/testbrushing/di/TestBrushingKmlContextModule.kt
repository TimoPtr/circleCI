/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.di

import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.GameScope
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.processedbrushings.crypto.ThresholdProvider
import com.kolibree.android.processedbrushings.crypto.TransitionProvider
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProvider
import com.kolibree.android.sdk.util.KpiSpeedProvider
import com.kolibree.android.sdk.util.RnnWeightProvider
import com.kolibree.kml.FreeBrushingAppContext
import dagger.Module
import dagger.Provides

@Module
class TestBrushingKmlContextModule {

    @Suppress("LongParameterList", "LongMethod")
    @Provides
    @GameScope
    fun provideAppContext(
        rnnWeightProvider: RnnWeightProvider?,
        angleProvider: AngleProvider,
        kpiSpeedProvider: KpiSpeedProvider?,
        transitionProvider: TransitionProvider,
        thresholdProvider: ThresholdProvider,
        zoneValidatorProvider: ZoneValidatorProvider,
        toothbrushModel: ToothbrushModel
    ): FreeBrushingAppContext = FreeBrushingAppContext(
        requireNotNull(rnnWeightProvider?.getRnnWeight()),
        angleProvider.getKPIAngle(),
        requireNotNull(kpiSpeedProvider?.getKpiSpeed()),
        transitionProvider.getTransition(),
        thresholdProvider.getThresholdBalancing(),
        zoneValidatorProvider.getZoneValidator(),
        toothbrushModel.hasOverPressure()
    )
}
