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
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.coachplus.CoachPlusAnalytics
import com.kolibree.android.coachplus.CoachPlusSensorsListener
import com.kolibree.android.coachplus.controller.CoachPlusController
import com.kolibree.android.coachplus.di.CoachPlusInjectionConstraints.TICK_PERIOD
import com.kolibree.android.coachplus.mvi.CoachPlusViewModel
import com.kolibree.android.coachplus.mvi.SoundInteractor
import com.kolibree.android.coachplus.mvi.SoundInteractorImpl
import com.kolibree.android.coachplus.settings.persistence.repo.CoachSettingsRepository
import com.kolibree.android.coachplus.ui.CoachPlusColorSet
import com.kolibree.android.coachplus.ui.colors.CurrentZoneColorProvider
import com.kolibree.android.coachplus.utils.CoachPlaqlessRingLedColorMapper
import com.kolibree.android.coachplus.utils.CoachPlaqlessRingLedColorMapperImpl
import com.kolibree.android.coachplus.utils.RingLedColorUseCase
import com.kolibree.android.coachplus.utils.RingLedColorUseCaseImpl
import com.kolibree.android.coachplus.utils.ZoneHintProvider
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.game.BrushingCreator
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.game.bi.KmlAvroCreator
import com.kolibree.android.game.sensors.GameSensorListener
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.connection.brushingmode.ConfirmBrushingModeUseCase
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.sdkws.core.IKolibreeConnector
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class CoachPlusActivityLogicModule {

    @Binds
    internal abstract fun bindsGameSensorListener(
        coachPlusSensorsListener: CoachPlusSensorsListener
    ): GameSensorListener

    @Binds
    internal abstract fun bindsCoachPlaqlessRingLedColorMapper(
        colorMapper: CoachPlaqlessRingLedColorMapperImpl
    ): CoachPlaqlessRingLedColorMapper

    @Binds
    internal abstract fun bindsRingLedColorUseCase(
        ringLedColorUseCase: RingLedColorUseCaseImpl
    ): RingLedColorUseCase

    @Binds
    internal abstract fun bindsSoundInteractor(
        soundInteractorImpl: SoundInteractorImpl
    ): SoundInteractor

    internal companion object {

        @Provides
        @Suppress("ExperimentalClassUse", "LongParameterList")
        internal fun providesViewModelFactory(
            @ToothbrushMac macAddress: Optional<String>,
            toothbrushModel: ToothbrushModel?,
            gameInteractor: GameInteractor,
            facade: GameToothbrushInteractorFacade,
            lostConnectionHandler: LostConnectionHandler,
            soundInteractor: SoundInteractor,
            colorSet: CoachPlusColorSet,
            connector: IKolibreeConnector,
            coachPlusController: CoachPlusController,
            coachSettingsRepository: CoachSettingsRepository,
            zoneHintProvider: ZoneHintProvider,
            confirmBrushingModeUseCase: ConfirmBrushingModeUseCase,
            ringLedColorUseCase: RingLedColorUseCase,
            brushingCreator: BrushingCreator,
            kmlAvroCreator: KmlAvroCreator,
            keepScreenOnController: KeepScreenOnController,
            currentZoneColorProvider: CurrentZoneColorProvider,
            featureToggles: FeatureToggleSet,
            analytics: CoachPlusAnalytics
        ): CoachPlusViewModel.Factory =
            CoachPlusViewModel.Factory(
                macAddress,
                toothbrushModel,
                gameInteractor,
                facade,
                lostConnectionHandler,
                soundInteractor,
                colorSet,
                connector,
                coachPlusController,
                coachSettingsRepository,
                zoneHintProvider,
                TICK_PERIOD,
                confirmBrushingModeUseCase,
                ringLedColorUseCase,
                brushingCreator,
                kmlAvroCreator,
                keepScreenOnController,
                currentZoneColorProvider,
                featureToggles,
                analytics
            )
    }
}
