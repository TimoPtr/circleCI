/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui.mindyourspeed

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.google.common.base.Optional
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.angleandspeed.common.logic.AngleAndSpeedUseCase
import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.feedback.FeedbackMessageResource
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.app.ui.widget.ZoneProgressData
import com.kolibree.android.commons.ShortTask
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.MindYourSpeedHideDotFeature
import com.kolibree.android.feature.toggleIsOn
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.game.mvi.BaseGameViewModel
import com.kolibree.android.game.mvi.lostConnectionState
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.game.shorttask.domain.logic.ShortTaskRepository
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.tracker.Analytics
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

private typealias State = MindYourSpeedViewState

internal class MindYourSpeedViewModel(
    initialViewState: State,
    private val angleAndSpeedUseCase: AngleAndSpeedUseCase,
    private val navigator: MindYourSpeedNavigator,
    private val currentProfileProvider: CurrentProfileProvider,
    private val shortTaskRepository: ShortTaskRepository,
    macAddress: Optional<String>,
    gameInteractor: GameInteractor,
    facade: GameToothbrushInteractorFacade,
    lostConnectionHandler: LostConnectionHandler,
    keepScreenOnController: KeepScreenOnController
) : BaseGameViewModel<State>(
    initialViewState,
    macAddress,
    gameInteractor,
    facade,
    lostConnectionHandler,
    keepScreenOnController
) {
    // region Live Data

    val isWaitingForStart = mapNonNull<State, Boolean>(
        viewStateLiveData,
        defaultValue = initialViewState.isWaitingForStart
    ) { viewState -> viewState.isWaitingForStart }

    val isPaused = mapNonNull<State, Boolean>(
        viewStateLiveData,
        defaultValue = initialViewState.isPaused
    ) { viewState -> viewState.isPaused }

    val zoneData = mapNonNull<State, ZoneProgressData>(
        viewStateLiveData,
        defaultValue = initialViewState.zoneProgressData
    ) { viewState -> viewState.zoneProgressData }

    val speedFeedback = mapNonNull<State, SpeedFeedback?>(
        viewStateLiveData,
        defaultValue = initialViewState.speedFeedback
    ) { viewState -> viewState.speedFeedback }

    val feedback = mapNonNull<State, FeedbackMessageResource>(
        viewStateLiveData,
        defaultValue = initialViewState.feedbackMessage.asResource()
    ) { viewState -> viewState.feedbackMessage.asResource() }

    val enableSpeedometer = mapNonNull<State, Boolean>(
        viewStateLiveData,
        defaultValue = initialViewState.enableSpeedometer
    ) { viewState -> viewState.enableSpeedometer }

    // endregion Live Data

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        disposeOnStop { listenForCurrentStageUpdates() }
        disposeOnStop { listenForSpeedFeedback() }
    }

    override fun onStop(owner: LifecycleOwner) {
        pauseMindYourSpeed()
        super.onStop(owner)
    }

    private fun listenForSpeedFeedback(): Disposable {
        return angleAndSpeedUseCase.angleAndSpeedFlowable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response -> onNewFeedback(response) }, Timber::e)
    }

    private fun listenForCurrentStageUpdates(): Disposable {
        return viewStateFlowable
            .map(State::stage)
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ currentStage ->
                angleAndSpeedUseCase.setPrescribedZones(currentStage.prescribedZones)
            }, Timber::e)
    }

    private fun onNewFeedback(response: AngleAndSpeedFeedback) {
        updateViewState {
            val state = updateWith(response)
            Timber.d("onNewFeedback ${state.zoneProgressData.zones.find { it.progress < 1f }?.progress}")
            if (state.isFinished) finishGame()
            state
        }
    }

    override fun onConnectionEstablished(connection: KLTBConnection) {
        updateViewState { copy(lostConnectionState = connection.lostConnectionState()) }
        if (connection.vibrator().isOn) {
            resumeMindYourSpeed()
        }
    }

    override fun onLostConnectionHandleStateChanged(
        connection: KLTBConnection,
        state: LostConnectionHandler.State
    ) {
        updateViewState { copy(lostConnectionState = state) }
        if (state != LostConnectionHandler.State.CONNECTION_ACTIVE) {
            pauseMindYourSpeed()
        }
    }

    override fun onVibratorOn(connection: KLTBConnection) {
        super.onVibratorOn(connection)
        resumeMindYourSpeed()
    }

    override fun onVibratorOff(connection: KLTBConnection) {
        super.onVibratorOff(connection)
        pauseMindYourSpeed()
    }

    fun onResumeButtonClick() {
        resumeMindYourSpeed()
    }

    fun onRestartButtonClick() {
        Analytics.send(MindYourSpeedAnalytics.restart())
        updateViewState { withProgressReset() }
        resumeMindYourSpeed()
    }

    fun onQuitButtonClick() {
        Analytics.send(MindYourSpeedAnalytics.quit())
        navigator.cancel()
    }

    fun onBackPressed() {
        if (getViewState()?.isWaitingForStart == true) {
            navigator.cancel()
        } else {
            pauseMindYourSpeed()
            connectionWeCareAbout()?.let { connection ->
                disposeOnStop { connection.vibrator().off().subscribe({ }, Timber::e) }
            }
        }
    }

    private fun pauseMindYourSpeed() {
        updateViewState {
            if (!isPaused) Analytics.send(MindYourSpeedAnalytics.pause())
            withPausedState()
        }
    }

    private fun resumeMindYourSpeed() {
        updateViewState {
            if (isPaused) Analytics.send(MindYourSpeedAnalytics.resume())
            resumeGame()
            withUnpausedState()
        }
    }

    private fun finishGame() {
        disposeOnStop {
            facade.onGameFinished()
                .subscribeOn(Schedulers.io())
                .andThen(currentProfileProvider.currentProfileSingle()
                    .flatMapCompletable { profile ->
                        shortTaskRepository.createShortTask(
                            profile.id,
                            ShortTask.MIND_YOUR_SPEED
                        )
                    })
                .doOnTerminate { Analytics.send(MindYourSpeedAnalytics.finishedWithSuccess()) }
                .doOnTerminate { navigator.finishWithSuccess() }
                .doOnError { navigator.cancel() }
                .subscribe({ }, Timber::e)
        }
    }

    class Factory @Inject constructor(
        private val angleAndSpeedUseCase: AngleAndSpeedUseCase,
        private val navigator: MindYourSpeedNavigator,
        private val currentProfileProvider: CurrentProfileProvider,
        private val shortTaskRepository: ShortTaskRepository,
        @ToothbrushMac private val macAddress: Optional<String>,
        private val gameInteractor: GameInteractor,
        private val facade: GameToothbrushInteractorFacade,
        private val lostConnectionHandler: LostConnectionHandler,
        private val keepScreenOnController: KeepScreenOnController,
        private val featureToggleSet: FeatureToggleSet
    ) : BaseViewModel.Factory<State>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MindYourSpeedViewModel(
            viewState ?: MindYourSpeedViewState.initial(
                featureToggleSet.toggleIsOn(MindYourSpeedHideDotFeature)
            ),
            angleAndSpeedUseCase,
            navigator,
            currentProfileProvider,
            shortTaskRepository,
            macAddress,
            gameInteractor,
            facade,
            lostConnectionHandler,
            keepScreenOnController
        ) as T
    }
}
