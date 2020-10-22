/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushing

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.common.base.Optional
import com.kolibree.android.angleandspeed.common.logic.AngleAndSpeedUseCase
import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.mvi.BrushingViewModel
import com.kolibree.android.angleandspeed.common.widget.progressbar.ProgressState
import com.kolibree.android.angleandspeed.testangles.model.ToothSide
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.mvi.lostConnectionState
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.distinctUntilChanged
import com.kolibree.kml.MouthZone16

internal abstract class TestAnglesBrushingViewModel(
    initialViewState: TestAnglesBrushingViewState?,
    macAddress: Optional<String>,
    gameInteractor: GameInteractor,
    facade: GameToothbrushInteractorFacade,
    angleAndSpeedUseCase: AngleAndSpeedUseCase,
    prescribedZones: Array<MouthZone16>,
    lostConnectionHandler: LostConnectionHandler,
    keepScreenOnController: KeepScreenOnController
) : BrushingViewModel<TestAnglesBrushingViewState>(
    initialViewState ?: TestAnglesBrushingViewState.initial(),
    macAddress,
    gameInteractor,
    facade,
    angleAndSpeedUseCase,
    prescribedZones,
    lostConnectionHandler,
    keepScreenOnController
) {
    val durationPercentage: LiveData<Int> =
        map(viewStateLiveData) { state -> state?.durationPercentage ?: 0 }

    val toothSide: LiveData<ToothSide> = map(viewStateLiveData) { state -> state?.toothSide }

    val isZoneCorrect: LiveData<Boolean> = map(viewStateLiveData) { state -> state?.isZoneCorrect }

    val progressState: LiveData<ProgressState> =
        map(viewStateLiveData) { state -> state?.progressState }.distinctUntilChanged()

    abstract val brushDegrees: LiveData<Float>

    abstract val stateColor: LiveData<Int>

    /**
     * Will be called after Start state has been reach
     */
    abstract fun moveToTheNextStage()

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        updateViewState { copy(isAnimationAllowed = true) }
    }

    override fun onPause(owner: LifecycleOwner) {
        updateViewState { copy(isAnimationAllowed = false) }
        super.onPause(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        updateViewState { copy(lastUpdateTimestamp = null) }
        super.onStop(owner)
    }

    override fun onConnectionEstablished(connection: KLTBConnection) {
        updateViewState {
            copy(
                lastUpdateTimestamp = null,
                vibrationOn = connection.vibrator().isOn,
                lostConnectionState = connection.lostConnectionState()
            )
        }
    }

    override fun onLostConnectionHandleStateChanged(
        connection: KLTBConnection,
        state: LostConnectionHandler.State
    ) {
        updateViewState {
            copy(
                lastUpdateTimestamp = null,
                lostConnectionState = state
            )
        }
    }

    override fun onVibratorOn(connection: KLTBConnection) {
        updateViewState {
            copy(
                lastUpdateTimestamp = null,
                vibrationOn = true
            )
        }
        super.onVibratorOn(connection)
    }

    override fun onVibratorOff(connection: KLTBConnection) {
        updateViewState {
            copy(
                lastUpdateTimestamp = null,
                vibrationOn = false
            )
        }
        super.onVibratorOff(connection)
    }

    override fun onNewFeedback(response: AngleAndSpeedFeedback) {
        updateViewState { updateWith(response) }
        if (getViewState()?.shouldMoveToTheNextStage() == true) {
            updateViewState { copy(duration = 0) }
            moveToTheNextStage()
        }
    }

    internal companion object {

        const val HALF_CIRCLE_DEGREES = 180
    }
}
