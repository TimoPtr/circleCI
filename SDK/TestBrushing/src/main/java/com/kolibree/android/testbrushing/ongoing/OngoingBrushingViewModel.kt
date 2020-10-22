/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.ongoing

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.common.base.Optional
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.game.BrushingCreator
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.game.mvi.BaseGameViewModel
import com.kolibree.android.game.mvi.lostConnectionState
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTION_ACTIVE
import com.kolibree.android.testbrushing.TestBrushingAnalytics
import com.kolibree.android.testbrushing.TestBrushingNavigator
import com.kolibree.android.testbrushing.TestBrushingSharedViewModel
import com.kolibree.android.testbrushing.shared.TestBrushingUseCase
import com.kolibree.android.tracker.Analytics
import com.kolibree.databinding.bindingadapter.LottieDelayedLoop
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

@VisibleForApp
class OngoingBrushingViewModel(
    initialViewState: OngoingBrushingViewState?,
    sharedViewModel: TestBrushingSharedViewModel,
    private val navigator: TestBrushingNavigator,
    private val testBrushingUseCase: TestBrushingUseCase,
    private val brushingCreator: BrushingCreator,
    private val macAddress: Optional<String>,
    gameInteractor: GameInteractor,
    facade: GameToothbrushInteractorFacade,
    lostConnectionHandler: LostConnectionHandler,
    keepScreenOnController: KeepScreenOnController,
    private val timeScheduler: Scheduler
) : BaseGameViewModel<OngoingBrushingViewState>(
    initialViewState ?: OngoingBrushingViewState.initial(),
    macAddress,
    gameInteractor,
    facade,
    lostConnectionHandler,
    keepScreenOnController
), TestBrushingSharedViewModel by sharedViewModel {

    private lateinit var brushingCreatorListener: BrushingCreator.Listener

    init {
        disposeOnCleared { startTurnOffToothbrushMessageTimer().defaultSubscribe() }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        brushingCreator.setLifecycleOwner(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        pauseTestBrushing()
        super.onStop(owner)
    }

    val pauseScreenVisible: LiveData<Boolean> =
        map(viewStateLiveData) { viewState ->
            viewState?.pauseScreenVisible ?: false
        }

    val lottieBrushingAnimation: LiveData<LottieDelayedLoop?> =
        map(viewStateLiveData) { viewState ->
            viewState?.brushingAnimation
        }

    val showTurnOffToothbrushMessage: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.turnOffToothbrushMessageVisible
    }

    override fun onConnectionEstablished(connection: KLTBConnection) {
        updateViewState {
            copy(
                lostConnectionState = connection.lostConnectionState()
            )
        }
        if (connection.vibrator().isOn) {
            resumeTestBrushing()
        }
    }

    override fun onLostConnectionHandleStateChanged(
        connection: KLTBConnection,
        state: LostConnectionHandler.State
    ) {
        updateViewState { copy(lostConnectionState = state) }
        if (state == CONNECTION_ACTIVE) {
            testBrushingUseCase.notifyReconnection()
            updateViewState { withResumedAnimations() }
        } else {
            updateViewState { withPausedAnimations() }
        }
    }

    override fun onVibratorOn(connection: KLTBConnection) {
        super.onVibratorOn(connection)
        resumeTestBrushing()
    }

    override fun onVibratorOff(connection: KLTBConnection) {
        super.onVibratorOff(connection)
        pauseTestBrushing()
    }

    @VisibleForTesting
    fun resumeTestBrushing() {
        getViewState()?.let { state ->
            if (!state.pauseScreenVisible) return
            updateViewState {
                copy(pauseScreenVisible = false).withResumedAnimations()
            }
            Analytics.send(TestBrushingAnalytics.ongoingBrushingScreen())
        }
    }

    @VisibleForTesting
    fun pauseTestBrushing() {
        getViewState()?.let { state ->
            if (state.pauseScreenVisible) return
            updateViewState {
                copy(pauseScreenVisible = true).withPausedAnimations()
            }
            Analytics.send(TestBrushingAnalytics.pauseScreen())
        }
    }

    @VisibleForTesting
    fun continueTestBrushingSession() {
        Analytics.send(TestBrushingAnalytics.notFinished())
        resumeGame()
    }

    private fun startTurnOffToothbrushMessageTimer(): Completable {
        return Observable.interval(1, TimeUnit.SECONDS, timeScheduler)
            .filter {
                getViewState()?.let {
                    !it.pauseScreenVisible && it.lostConnectionState == CONNECTION_ACTIVE
                } ?: false
            }
            .take(TIMER_DURATION_SECONDS)
            .ignoreElements()
            .doOnComplete {
                updateViewState { copy(turnOffToothbrushMessageVisible = true) }
            }
    }

    @VisibleForTesting
    fun tryToCreateTestBrushing() {
        connectionWeCareAbout()?.let { connection ->
            setupBrushingCreatorListener(
                onSuccess = { disposeOnCleared { finishWithSuccess(connection).defaultSubscribe() } },
                onError = { disposeOnCleared { finishAndTerminate(connection).defaultSubscribe() } }
            )
            // success is handled by BrushingCreatorListener so we don;t need dedicated subscription
            disposeOnCleared { createAndUploadBrushing(connection).defaultSubscribe() }
        } ?: run {
            FailEarly.fail("Cannot complete the flow without connection")
            navigator.terminate()
        }
    }

    // This cannot be inline - otherwise brushingCreator looses track of the listener reference
    private fun setupBrushingCreatorListener(
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        brushingCreatorListener = object : BrushingCreator.Listener {

            override fun onSuccessfullySentData() {
                brushingCreator.removeListener(this)
                onSuccess()
            }

            override fun somethingWrong(error: Throwable) {
                Timber.e(error)
                brushingCreator.removeListener(this)
                onError()
            }
        }
        brushingCreator.addListener(brushingCreatorListener)
    }

    @VisibleForTesting
    fun createAndUploadBrushing(connection: KLTBConnection): Completable =
        testBrushingUseCase.createBrushingData(connection)
            .doOnSubscribe { showProgress(true) }
            .doOnSuccess { data -> brushingCreator.onBrushingCompleted(false, connection, data) }
            .ignoreElement()
            .onErrorResumeNext { finishAndTerminate(connection) }

    @VisibleForTesting
    fun finishWithSuccess(connection: KLTBConnection): Completable =
        facade.onGameFinished()
            .doOnSubscribe { showProgress(true) }
            .andThen(connection.vibrator().off())
            .doOnTerminate { showProgress(false) }
            .doOnError { navigator.terminate() }
            .doOnComplete { navigator.finishWithSuccess() }

    @VisibleForTesting
    fun finishAndTerminate(connection: KLTBConnection): Completable =
        facade.onGameFinished()
            .doOnSubscribe { showProgress(true) }
            .doOnTerminate { showProgress(false) }
            .andThen(connection.vibrator().off())
            .doOnTerminate { navigator.terminate() }

    @VisibleForApp
    class Factory @Inject constructor(
        private val sharedViewModel: TestBrushingSharedViewModel,
        private val navigator: TestBrushingNavigator,
        @ToothbrushMac private val macAddress: Optional<String>,
        private val gameInteractor: GameInteractor,
        private val facade: GameToothbrushInteractorFacade,
        private val testBrushingUseCase: TestBrushingUseCase,
        private val brushingCreator: BrushingCreator,
        private val lostConnectionHandler: LostConnectionHandler,
        private val keepScreenOnController: KeepScreenOnController,
        @SingleThreadScheduler private val timeScheduler: Scheduler
    ) : BaseViewModel.Factory<OngoingBrushingViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OngoingBrushingViewModel(
                viewState,
                sharedViewModel,
                navigator,
                testBrushingUseCase,
                brushingCreator,
                macAddress,
                gameInteractor,
                facade,
                lostConnectionHandler,
                keepScreenOnController,
                timeScheduler
            ) as T
    }
}

private fun Completable.defaultSubscribe(): Disposable = this.subscribe({}, Timber::e)

private const val TIMER_DURATION_SECONDS = 20L
