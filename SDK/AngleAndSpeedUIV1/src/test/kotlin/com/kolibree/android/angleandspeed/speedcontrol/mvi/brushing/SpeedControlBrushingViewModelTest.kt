/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing

import androidx.lifecycle.Lifecycle
import com.google.common.base.Optional
import com.jraska.livedata.test
import com.kolibree.android.angleandspeed.R
import com.kolibree.android.angleandspeed.common.logic.AngleAndSpeedUseCase
import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.logic.model.AngleFeedback
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.angleandspeed.common.widget.progressbar.ProgressState
import com.kolibree.android.angleandspeed.speedcontrol.model.SpeedControlPrescribedZones
import com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing.SpeedControlBrushingViewState.Companion.STAGE_DURATION
import com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing.SpeedControlBrushingViewState.Companion.TOTAL_DURATION
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.mvi.VibratorStateChanged
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTING
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTION_ACTIVE
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTION_LOST
import com.kolibree.android.test.LifecycleObserverTester
import com.kolibree.android.test.extensions.advanceTimeBy
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.mockFacadeWithLifecycleSupport
import com.kolibree.android.tracker.logic.userproperties.UserProperties.MAC_ADDRESS
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

class SpeedControlBrushingViewModelTest : BaseUnitTest() {

    internal lateinit var viewModel: SpeedControlBrushingViewModel

    private lateinit var viewModelLifecycleTester: LifecycleObserverTester

    private lateinit var connection: KLTBConnection

    private val gameInteractor: GameInteractor = mock()

    private val facade: GameToothbrushInteractorFacade = mockFacadeWithLifecycleSupport()

    private val angleAndSpeedUseCase: AngleAndSpeedUseCase = mock()

    private val angleAndSpeedTestProcessor = PublishProcessor.create<AngleAndSpeedFeedback>()

    private val lostConnectionHandler: LostConnectionHandler = mock()

    private val lostConnectionTestProcessor = PublishSubject.create<LostConnectionHandler.State>()

    private val keepScreenOnController: KeepScreenOnController = mock()

    override fun setup() {
        super.setup()

        connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(MAC_ADDRESS)
            .withVibration(true)
            .build()
        whenever(gameInteractor.connection).then { connection }

        whenever(facade.onGameFinished()).thenReturn(Completable.complete())

        whenever(angleAndSpeedUseCase.angleAndSpeedFlowable).thenReturn(angleAndSpeedTestProcessor)
        whenever(lostConnectionHandler.connectionObservable(MAC_ADDRESS))
            .thenReturn(lostConnectionTestProcessor)

        viewModel = spy(
            SpeedControlBrushingViewModel(
                null,
                Optional.of(MAC_ADDRESS),
                gameInteractor,
                facade,
                angleAndSpeedUseCase,
                SpeedControlPrescribedZones.ZONES,
                lostConnectionHandler,
                keepScreenOnController
            )
        )

        viewModelLifecycleTester = viewModel.lifecycleTester()
    }

    @Test
    fun `remainingTimeSeconds starts with TOTAL_DURATION`() {
        viewModel.remainingTimeSeconds.test().assertValue(TOTAL_DURATION.seconds)
    }

    @Test
    fun `remainingTimeSeconds returns state time in seconds`() {
        viewModel.updateViewState {
            copy(
                currentStage = Stage.OUTER_MOLARS, // 30s
                stageDuration = Duration.ofSeconds(5) // - 5s
            )
        }
        viewModel.remainingTimeSeconds.test().assertValue(Duration.ofSeconds(25).seconds) // = 25s
    }

    @Test
    fun `speedStatusText reacts on state changes`() {
        val speedStatusTextTester = viewModel.speedStatusText.test()

        viewModel.updateViewState { copy(speedFeedback = SpeedFeedback.CORRECT) }
        viewModel.updateViewState { copy(speedFeedback = SpeedFeedback.OVERSPEED) }
        viewModel.updateViewState { copy(speedFeedback = SpeedFeedback.CORRECT) }
        viewModel.updateViewState { copy(speedFeedback = SpeedFeedback.UNDERSPEED) }
        viewModel.updateViewState { copy(speedFeedback = SpeedFeedback.CORRECT) }

        speedStatusTextTester.assertValueHistory(
            R.string.speed_control_feedback_correct,
            R.string.speed_control_feedback_correct,
            R.string.speed_control_feedback_overspeed,
            R.string.speed_control_feedback_correct,
            R.string.speed_control_feedback_underspeed,
            R.string.speed_control_feedback_correct
        )
    }

    @Test
    fun `speedStatusTextColor reacts on state changes`() {
        val speedStatusTextColorTester = viewModel.speedStatusTextColor.test()

        viewModel.updateViewState { copy(speedFeedback = SpeedFeedback.CORRECT) }
        viewModel.updateViewState { copy(speedFeedback = SpeedFeedback.OVERSPEED) }
        viewModel.updateViewState { copy(speedFeedback = SpeedFeedback.CORRECT) }
        viewModel.updateViewState { copy(speedFeedback = SpeedFeedback.UNDERSPEED) }
        viewModel.updateViewState { copy(speedFeedback = SpeedFeedback.CORRECT) }

        speedStatusTextColorTester.assertValueHistory(
            R.color.speed_control_feedback_correct,
            R.color.speed_control_feedback_correct,
            R.color.speed_control_feedback_overspeed,
            R.color.speed_control_feedback_correct,
            R.color.speed_control_feedback_underspeed,
            R.color.speed_control_feedback_correct
        )
    }

    @Test
    fun `progressState reacts on state changes`() {
        val progressStateTester = viewModel.progressState.test()

        viewModel.updateViewState {
            copy(
                vibrationOn = true,
                speedFeedback = SpeedFeedback.CORRECT,
                lostConnectionState = CONNECTION_ACTIVE
            )
        }
        viewModel.updateViewState { copy(lostConnectionState = CONNECTION_LOST) }
        viewModel.updateViewState { copy(lostConnectionState = CONNECTING) }
        viewModel.updateViewState { copy(lostConnectionState = CONNECTION_ACTIVE) }
        viewModel.updateViewState { copy(vibrationOn = false) }
        viewModel.updateViewState { copy(vibrationOn = true) }
        viewModel.updateViewState { copy(speedFeedback = SpeedFeedback.OVERSPEED) }
        viewModel.updateViewState { copy(speedFeedback = SpeedFeedback.UNDERSPEED) }
        viewModel.updateViewState { copy(speedFeedback = SpeedFeedback.CORRECT) }

        progressStateTester.assertValueHistory(
            ProgressState.PAUSE, // initial null state
            ProgressState.START,
            ProgressState.PAUSE,
            ProgressState.START,
            ProgressState.PAUSE,
            ProgressState.START,
            ProgressState.PAUSE,
            ProgressState.START
        )
    }

    @Test
    fun `speedHintText reacts on state changes`() {
        val speedHintTextTester = viewModel.speedHintText.test()

        viewModel.updateViewState { copy(currentStage = Stage.OUTER_MOLARS) }
        viewModel.updateViewState { copy(currentStage = Stage.CHEWING_MOLARS) }
        viewModel.updateViewState { copy(currentStage = Stage.FRONT_INCISORS) }
        viewModel.updateViewState { copy(currentStage = Stage.COMPLETED) }

        speedHintTextTester.assertValueHistory(
            R.string.speed_control_brushing_stage1_hint,
            R.string.speed_control_brushing_stage2_hint,
            R.string.speed_control_brushing_stage3_hint,
            R.string.empty
        )
    }

    @Test
    fun `speedHintHighlightText reacts on state changes`() {
        val speedHintHighlightTextTester = viewModel.speedHintHighlightText.test()

        viewModel.updateViewState { copy(currentStage = Stage.OUTER_MOLARS) }
        viewModel.updateViewState { copy(currentStage = Stage.CHEWING_MOLARS) }
        viewModel.updateViewState { copy(currentStage = Stage.FRONT_INCISORS) }
        viewModel.updateViewState { copy(currentStage = Stage.COMPLETED) }

        speedHintHighlightTextTester.assertValueHistory(
            R.string.speed_control_brushing_stage1_hint_highlight,
            R.string.speed_control_brushing_stage2_hint_highlight,
            R.string.speed_control_brushing_stage3_hint_highlight,
            R.string.empty
        )
    }

    @Test
    fun `onStart sends prescribed zones to use case`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        verify(angleAndSpeedUseCase, never()).setPrescribedZones(any())

        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_START)
        verify(angleAndSpeedUseCase).setPrescribedZones(SpeedControlPrescribedZones.ZONES)
    }

    @Test
    fun `onStop adds clears subscriptions`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_STOP)
        assertEquals(0, viewModel.onStopDisposables.compositeDisposable.size())
    }

    @Test
    fun `onStop clears lastUpdateTimestamp`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_STOP)
        assertEquals(null, viewModel.getViewState()!!.lastUpdateTimestamp)
    }

    @Test
    fun `onNewFeedback moves to the next stage if state allows it`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
    }

    @Test
    fun `onNewFeedback updates the view state`() {
        val testObserver = viewModel.viewStateFlowable.test()

        val newFeedback =
            AngleAndSpeedFeedback(
                AngleFeedback(
                    1.0f,
                    2.0f,
                    3.0f
                ),
                SpeedFeedback.UNDERSPEED,
                false
            )
        viewModel.onNewFeedback(newFeedback)

        val currentState = testObserver.values()[testObserver.valueCount() - 1]
        assertEquals(SpeedFeedback.UNDERSPEED, currentState.speedFeedback)
    }

    @Test
    fun `onNewFeedback pushes the stages forward until completion`() {
        doNothing().whenever(viewModel).finishGame()

        val testObserver = viewModel.viewStateFlowable.test()

        onNewFeedbackToCompletion()

        val currentState = testObserver.values()[testObserver.valueCount() - 1]
        assertEquals(Stage.COMPLETED, currentState.currentStage)
        assertEquals(true, currentState.isCompleted())
        verify(viewModel).finishGame()
    }

    @Test
    fun `onConnectionEstablished updates view state and facade`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.onConnectionEstablished()

        verify(facade).onConnectionEstablished(gameInteractor.connection!!)

        with(viewModel.getViewState()!!) {
            assertEquals(null, lastUpdateTimestamp)
            assertEquals(true, vibrationOn)
            assertEquals(
                CONNECTION_ACTIVE,
                lostConnectionState
            )
        }
    }

    @Test
    fun `onConnectionEstablished skips updates if we connected to different toothbrush`() {
        connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac("SO:ME:OT:TH:ER:TB")
            .withVibration(true)
            .build()

        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.onConnectionEstablished()

        verify(facade, never()).onConnectionEstablished(any())

        with(viewModel.getViewState()!!) {
            assertEquals(null, lastUpdateTimestamp)
            assertEquals(null, vibrationOn)
            assertEquals(null, lostConnectionState)
        }
    }

    @Test
    fun `onVibratorOn updates vibration state and pushes action`() {
        connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(MAC_ADDRESS)
            .withVibration(false)
            .build()

        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        val actionListener = viewModel.actionsObservable.test()
        viewModel.onVibratorOn(connection)

        with(viewModel.getViewState()!!) {
            assertEquals(null, lastUpdateTimestamp)
            assertEquals(true, vibrationOn)
        }

        actionListener.assertValue(VibratorStateChanged(true))
    }

    @Test
    fun `onVibratorOff updates vibration state`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.onVibratorOff(connection)

        with(viewModel.getViewState()!!) {
            assertEquals(null, lastUpdateTimestamp)
            assertEquals(false, vibrationOn)
        }
    }

    @Test
    fun `finishGame calls onGameFinished() in facade`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.finishGame()

        verify(facade).onGameFinished()
    }

    @Test
    fun `finishGame turns of the vibration`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.finishGame()

        verify(connection.vibrator()).off()
    }

    @Test
    fun `finishGame emits openConfirmation`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        val testObserver = viewModel.actionsObservable.test()

        viewModel.finishGame()

        testObserver.assertValue(OpenConfirmation)
    }

    private fun onNewFeedbackToCompletion() {
        TrustedClock.setFixedDate()
        val correctFeedback =
            AngleAndSpeedFeedback(
                AngleFeedback(
                    1.0f,
                    2.0f,
                    3.0f
                ),
                SpeedFeedback.CORRECT,
                false
            )
        val denominator = 10L
        repeat(Stage.values().count() - 1) {
            repeat(0.until(STAGE_DURATION.toMillis() / denominator).count()) {
                TrustedClock.advanceTimeBy(denominator, ChronoUnit.MILLIS)
                viewModel.onNewFeedback(correctFeedback)
            }
        }
        TrustedClock.advanceTimeBy(denominator, ChronoUnit.MILLIS)
        viewModel.onNewFeedback(correctFeedback)
    }
}
