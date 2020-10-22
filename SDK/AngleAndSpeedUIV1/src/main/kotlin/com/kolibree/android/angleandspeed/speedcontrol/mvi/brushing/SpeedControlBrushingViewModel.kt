/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.common.base.Optional
import com.kolibree.android.angleandspeed.R
import com.kolibree.android.angleandspeed.common.logic.AngleAndSpeedUseCase
import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.angleandspeed.common.mvi.BrushingViewModel
import com.kolibree.android.angleandspeed.common.widget.progressbar.ProgressState
import com.kolibree.android.angleandspeed.speedcontrol.model.SpeedControlPrescribedZones
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.game.mvi.lostConnectionState
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.distinctUntilChanged
import com.kolibree.kml.MouthZone16
import io.reactivex.Completable
import javax.inject.Inject
import timber.log.Timber

internal class SpeedControlBrushingViewModel(
    initialViewState: SpeedControlBrushingViewState?,
    macAddress: Optional<String>,
    gameInteractor: GameInteractor,
    facade: GameToothbrushInteractorFacade,
    angleAndSpeedUseCase: AngleAndSpeedUseCase,
    prescribedZones: Array<MouthZone16>,
    lostConnectionHandler: LostConnectionHandler,
    keepScreenOnController: KeepScreenOnController
) : BrushingViewModel<SpeedControlBrushingViewState>(
    initialViewState ?: SpeedControlBrushingViewState.initial(),
    macAddress,
    gameInteractor,
    facade,
    angleAndSpeedUseCase,
    prescribedZones,
    lostConnectionHandler,
    keepScreenOnController
) {

    val speedFeedback: LiveData<SpeedFeedback> = map(viewStateLiveData) { state ->
        state?.speedFeedback
    }.distinctUntilChanged()

    val remainingTimeSeconds: LiveData<Long> = map(viewStateLiveData) { state ->
        state?.remainingTime?.seconds ?: SpeedControlBrushingViewState.TOTAL_DURATION.seconds
    }

    val speedStatusText: /* @StringRes */ LiveData<Int> = map(viewStateLiveData) { state ->
        state?.speedFeedback?.let { feedback ->
            when (feedback) {
                SpeedFeedback.OVERSPEED -> R.string.speed_control_feedback_overspeed
                SpeedFeedback.UNDERSPEED -> R.string.speed_control_feedback_underspeed
                else -> R.string.speed_control_feedback_correct
            }
        } ?: R.string.speed_control_feedback_correct
    }

    val speedStatusTextColor: /* @ColorRes */ LiveData<Int> = map(viewStateLiveData) { state ->
        state?.speedFeedback?.let { feedback ->
            when (feedback) {
                SpeedFeedback.OVERSPEED -> R.color.speed_control_feedback_overspeed
                SpeedFeedback.UNDERSPEED -> R.color.speed_control_feedback_underspeed
                else -> R.color.speed_control_feedback_correct
            }
        } ?: R.color.speed_control_feedback_correct
    }

    val progressState: LiveData<ProgressState> =
        map(viewStateLiveData) { state -> state?.progressState }.distinctUntilChanged()

    val speedHintText: /* @StringRes */ LiveData<Int> = map(viewStateLiveData) { state ->
        state?.currentStage?.let { stage ->
            when (stage) {
                Stage.OUTER_MOLARS -> R.string.speed_control_brushing_stage1_hint
                Stage.CHEWING_MOLARS -> R.string.speed_control_brushing_stage2_hint
                Stage.FRONT_INCISORS -> R.string.speed_control_brushing_stage3_hint
                else -> R.string.empty
            }
        } ?: R.string.empty
    }

    val speedHintHighlightText: /* @StringRes */ LiveData<Int> = map(viewStateLiveData) { state ->
        state?.currentStage?.let { stage ->
            when (stage) {
                Stage.OUTER_MOLARS -> R.string.speed_control_brushing_stage1_hint_highlight
                Stage.CHEWING_MOLARS -> R.string.speed_control_brushing_stage2_hint_highlight
                Stage.FRONT_INCISORS -> R.string.speed_control_brushing_stage3_hint_highlight
                else -> R.string.empty
            }
        } ?: R.string.empty
    }

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

    override fun onNewFeedback(response: AngleAndSpeedFeedback) {
        updateViewState { updateWith(response) }
        if (getViewState()?.isCompleted() == true) {
            finishGame()
        }
    }

    @VisibleForTesting
    fun finishGame() {
        disposeOnStop {
            facade.onGameFinished()
                .andThen(connectionWeCareAbout()?.vibrator()?.off() ?: Completable.complete())
                .doOnTerminate { pushAction(OpenConfirmation) }
                .subscribe({ }, Timber::e)
        }
    }

    override fun onConnectionEstablished(connection: KLTBConnection) {
        updateViewState {
            copy(
                lastUpdateTimestamp = nullIfConnectionStateWasSet(),
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
                lastUpdateTimestamp = nullIfConnectionStateWasSet(),
                vibrationOn = true
            )
        }
        super.onVibratorOn(connection)
    }

    override fun onVibratorOff(connection: KLTBConnection) {
        updateViewState {
            copy(
                lastUpdateTimestamp = nullIfConnectionStateWasSet(),
                vibrationOn = false
            )
        }
        super.onVibratorOff(connection)
    }

    class Factory @Inject constructor(
        @ToothbrushMac private val macAddress: Optional<String>,
        private val gameInteractor: GameInteractor,
        private val angleAndSpeedUseCase: AngleAndSpeedUseCase,
        private val facade: GameToothbrushInteractorFacade,
        private val lostConnectionHandler: LostConnectionHandler,
        private val keepScreenOnController: KeepScreenOnController
    ) : BaseViewModel.Factory<SpeedControlBrushingViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SpeedControlBrushingViewModel(
                viewState,
                macAddress,
                gameInteractor,
                facade,
                angleAndSpeedUseCase,
                SpeedControlPrescribedZones.ZONES,
                lostConnectionHandler,
                keepScreenOnController
            ) as T
    }
}

private fun SpeedControlBrushingViewState.nullIfConnectionStateWasSet(): Long? =
    if (lostConnectionState == null) lastUpdateTimestamp else null
