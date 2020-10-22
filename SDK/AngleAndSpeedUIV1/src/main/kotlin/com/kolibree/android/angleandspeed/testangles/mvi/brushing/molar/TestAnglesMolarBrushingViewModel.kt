/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushing.molar

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.common.base.Optional
import com.kolibree.android.angleandspeed.R
import com.kolibree.android.angleandspeed.common.logic.AngleAndSpeedUseCase
import com.kolibree.android.angleandspeed.testangles.model.TestAnglesPrescribedZones
import com.kolibree.android.angleandspeed.testangles.mvi.brushing.OpenIncisorBrushing
import com.kolibree.android.angleandspeed.testangles.mvi.brushing.TestAnglesBrushingViewModel
import com.kolibree.android.angleandspeed.testangles.mvi.brushing.TestAnglesBrushingViewState
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.LiveDataTransformations.merge
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.abs

internal const val NEUTRAL_DEGREE_BORDER_VALUE: Double = PI / 4

internal class TestAnglesMolarBrushingViewModel(
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
    TestAnglesPrescribedZones.FOR_MOLARS_STAGE,
    lostConnectionHandler,
    keepScreenOnController
) {

    override val brushDegrees: LiveData<Float> = map(viewStateLiveData) { state ->
        state?.angleDegrees?.roll?.let { HALF_CIRCLE_DEGREES - abs(it) }
    }

    val brushRadians: LiveData<Double> = map(viewStateLiveData) { state ->
        state?.angleDegrees?.let {
            val radians = it.roll * PI / HALF_CIRCLE_DEGREES
            return@let if (radians >= 0) -(radians - PI) else -(radians + PI)
        }
    }

    override val stateColor: LiveData<Int> =
        merge<Int>(isZoneCorrect, brushRadians, initialValue = null, mapper = {
            when {
                isZoneCorrect.value == true -> R.color.angle_state_correct
                brushRadians.value?.let { it in -NEUTRAL_DEGREE_BORDER_VALUE..NEUTRAL_DEGREE_BORDER_VALUE }
                    ?: false -> R.color.angle_state_neutral
                else -> R.color.angle_state_incorrect
            }
        })

    override fun moveToTheNextStage() {
        pushAction(OpenIncisorBrushing)
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
            TestAnglesMolarBrushingViewModel(
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
