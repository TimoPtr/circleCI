/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushing.incisor

import androidx.lifecycle.ViewModel
import com.google.common.base.Optional
import com.kolibree.android.angleandspeed.R
import com.kolibree.android.angleandspeed.common.logic.AngleAndSpeedUseCase
import com.kolibree.android.angleandspeed.testangles.model.TestAnglesPrescribedZones
import com.kolibree.android.angleandspeed.testangles.mvi.brushing.OpenConfirmation
import com.kolibree.android.angleandspeed.testangles.mvi.brushing.TestAnglesBrushingViewModel
import com.kolibree.android.angleandspeed.testangles.mvi.brushing.TestAnglesBrushingViewState
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import io.reactivex.Completable
import javax.inject.Inject
import kotlin.math.abs
import timber.log.Timber

internal class TestAnglesIncisorBrushingViewModel(
    initialViewState: TestAnglesBrushingViewState?,
    macAddress: Optional<String>,
    gameInteractor: GameInteractor,
    facade: GameToothbrushInteractorFacade,
    angleAndSpeedUseCase: AngleAndSpeedUseCase,
    lostConnectionHandler: LostConnectionHandler,
    keepScreenOnController: KeepScreenOnController
) : TestAnglesBrushingViewModel(
    initialViewState,
    macAddress,
    gameInteractor,
    facade,
    angleAndSpeedUseCase,
    TestAnglesPrescribedZones.FOR_INCISORS_STAGE,
    lostConnectionHandler,
    keepScreenOnController
) {
    override val brushDegrees = map(viewStateLiveData) { state ->
        state?.angleDegrees?.pitch?.let { HALF_CIRCLE_DEGREES - abs(it) }
    }

    override val stateColor = map(viewStateLiveData) { state ->
        when {
            state?.isZoneCorrect == true -> R.color.angle_state_correct
            else -> R.color.angle_state_incorrect
        }
    }

    val stateText = map(viewStateLiveData) { state ->
        when {
            state?.isZoneCorrect == true -> R.string.test_angles_state_correct
            else -> R.string.test_angles_state_incorrect
        }
    }

    override fun moveToTheNextStage() {
        disposeOnStop {
            facade.onGameFinished()
                .andThen(connectionWeCareAbout()?.vibrator()?.off() ?: Completable.complete())
                .doOnTerminate { finishAndOpenConfirmation() }
                .subscribe({ }, Timber::e)
        }
    }

    private fun finishAndOpenConfirmation() {
        pushAction(OpenConfirmation)
    }

    class Factory @Inject constructor(
        @ToothbrushMac private val macAddress: Optional<String>,
        private val gameInteractor: GameInteractor,
        private val angleAndSpeedUseCase: AngleAndSpeedUseCase,
        private val facade: GameToothbrushInteractorFacade,
        private val lostConnectionHandler: LostConnectionHandler,
        private val keepScreenOnController: KeepScreenOnController
    ) : BaseViewModel.Factory<TestAnglesBrushingViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TestAnglesIncisorBrushingViewModel(
                viewState,
                macAddress,
                gameInteractor,
                facade,
                angleAndSpeedUseCase,
                lostConnectionHandler,
                keepScreenOnController
            ) as T
    }
}
