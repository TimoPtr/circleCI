/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.di

import com.google.common.base.Optional
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.coachplus.CoachPlusArgumentProvider
import com.kolibree.android.coachplus.controller.BaseCoachPlusControllerImpl.Companion.DEFAULT_MAX_FAIL_TIME_MS
import com.kolibree.android.coachplus.controller.BrushingModeZoneDurationAdjuster
import com.kolibree.android.coachplus.controller.CoachPlusController
import com.kolibree.android.coachplus.controller.CoachPlusManualControllerImpl
import com.kolibree.android.coachplus.controller.NoOpZoneDurationAdjuster
import com.kolibree.android.coachplus.controller.ZoneDurationAdjuster
import com.kolibree.android.coachplus.controller.kml.CoachPlusKmlControllerImpl
import com.kolibree.android.coachplus.di.CoachPlusInjectionConstraints.DEFAULT_BRUSHING_DURATION_SECONDS
import com.kolibree.android.coachplus.di.CoachPlusInjectionConstraints.DI_GOAL_BRUSHING_TIME
import com.kolibree.android.coachplus.di.CoachPlusInjectionConstraints.DI_MAX_FAIL_TIME
import com.kolibree.android.coachplus.di.CoachPlusInjectionConstraints.TICK_PERIOD
import com.kolibree.android.coachplus.feedback.CoachPlusFeedbackMapper
import com.kolibree.android.coachplus.ui.CoachPlusColorSet
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.processedbrushings.crypto.ThresholdProvider
import com.kolibree.android.processedbrushings.crypto.TransitionProvider
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProvider
import com.kolibree.android.sdk.util.KpiSpeedProvider
import com.kolibree.android.sdk.util.RnnWeightProvider
import com.kolibree.kml.SupervisedBrushingAppContext16
import com.kolibree.sdkws.core.IKolibreeConnector
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Provider
import org.threeten.bp.Duration

@Module
internal object CoachPlusControllerInternalModule {

    @Provides
    @ActivityScope
    internal fun provideCoachPlusController(
        argumentProvider: CoachPlusArgumentProvider,
        @Named(DI_GOAL_BRUSHING_TIME) goalBrushingTime: Duration,
        coachPlusKmlControllerProvider: Provider<CoachPlusKmlControllerImpl>
    ): CoachPlusController =
        when {
            argumentProvider.provideManualMode() -> CoachPlusManualControllerImpl(
                goalBrushingTime,
                TICK_PERIOD.toMillis()
            )
            else -> coachPlusKmlControllerProvider.get()
        }

    @Provides
    internal fun providesZoneDurationAdjuster(
        argumentProvider: CoachPlusArgumentProvider,
        noOpZoneDurationAdjuster: dagger.Lazy<NoOpZoneDurationAdjuster>,
        brushingModeZoneDurationAdjuster: dagger.Lazy<BrushingModeZoneDurationAdjuster>
    ): ZoneDurationAdjuster {
        if (argumentProvider.provideToothbrushModel()?.supportsVibrationSpeedUpdate() == true) {
            return brushingModeZoneDurationAdjuster.get()
        }

        return noOpZoneDurationAdjuster.get()
    }

    @Provides
    internal fun providesCoachPlusKMLController(
        @Named(DI_GOAL_BRUSHING_TIME) goalBrushingTime: Duration,
        @Named(DI_MAX_FAIL_TIME) maxFailTime: Long,
        checkupCalculator: CheckupCalculator,
        argumentProvider: CoachPlusArgumentProvider,
        supervisedBrushingAppContext16Provider: Provider<SupervisedBrushingAppContext16>,
        durationAdjuster: ZoneDurationAdjuster
    ): CoachPlusKmlControllerImpl =
        CoachPlusKmlControllerImpl(
            goalBrushingTime,
            TICK_PERIOD.toMillis(),
            maxFailTime,
            checkupCalculator,
            supervisedBrushingAppContext16Provider,
            CoachPlusFeedbackMapper(TICK_PERIOD),
            if (argumentProvider.provideToothbrushModel() == ToothbrushModel.PLAQLESS) null else durationAdjuster
        )

    @Provides
    internal fun providesSupervisedBrushingAppContext16(
        rnnWeightProvider: RnnWeightProvider?,
        kpiSpeedProvider: KpiSpeedProvider?,
        angleProvider: AngleProvider,
        transitionProvider: TransitionProvider,
        thresholdProvider: ThresholdProvider,
        zoneValidatorProvider: ZoneValidatorProvider,
        toothbrushModel: ToothbrushModel?
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
            toothbrushModel?.hasOverPressure() ?: false
        )
    }

    @Provides
    internal fun providesToothbrushModel(argumentProvider: CoachPlusArgumentProvider): ToothbrushModel? =
        argumentProvider.provideToothbrushModel()

    @Provides
    @Named(DI_MAX_FAIL_TIME)
    internal fun providesMaxFailTime(): Long = DEFAULT_MAX_FAIL_TIME_MS

    @Provides
    @Named(DI_GOAL_BRUSHING_TIME)
    internal fun providesGoalBrushingTime(connector: IKolibreeConnector): Duration {
        val currentProfile = connector.currentProfile
        return Duration.ofSeconds(
            currentProfile?.brushingGoalTime?.toLong()
                ?: DEFAULT_BRUSHING_DURATION_SECONDS.toLong()
        )
    }

    @Provides
    @ToothbrushMac
    internal fun providesMacAddress(argumentProvider: CoachPlusArgumentProvider): Optional<String> =
        Optional.fromNullable(argumentProvider.provideToothbrushMac())

    @Provides
    internal fun provideCoachPlusColorSet(argumentProvider: CoachPlusArgumentProvider): CoachPlusColorSet =
        argumentProvider.provideColorSet()
}
