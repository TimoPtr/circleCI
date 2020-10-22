/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui.mindyourspeed

import androidx.lifecycle.Lifecycle
import com.google.common.base.Optional
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jraska.livedata.TestObserver as LiveDataObserver
import com.jraska.livedata.test
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.angleandspeed.common.logic.AngleAndSpeedUseCase
import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.angleandspeed.ui.mindyourspeed.Stage.Companion.NUMBER_OF_ONGOING_STAGES
import com.kolibree.android.app.feedback.FeedbackMessageResource
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.widget.ZoneProgressData
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ShortTask
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.lifecycle.GameLifecycle
import com.kolibree.android.game.mvi.BaseGameAction
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.game.shorttask.domain.logic.ShortTaskRepository
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.test.extensions.advanceTimeBy
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver as RxObserver
import io.reactivex.processors.BehaviorProcessor
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.temporal.ChronoUnit

const val MAC = "01:23:45:67:89:AB"

class MindYourSpeedViewModelTest : BaseUnitTest() {

    private val gameLifecycleStream = BehaviorRelay.create<GameLifecycle>()

    private val feedbackStream = BehaviorProcessor.create<AngleAndSpeedFeedback>()

    private val lostConnectionStream = BehaviorRelay.create<LostConnectionHandler.State>()

    private val useCase: AngleAndSpeedUseCase = mock()

    private val navigator: MindYourSpeedNavigator = mock()

    private val gameInteractor: GameInteractor = mock()

    private val facade: GameToothbrushInteractorFacade = mock()

    private val lostConnectionHandler: LostConnectionHandler = mock()

    private val keepScreenOnController: KeepScreenOnController = mock()

    private val currentProfileProvider: CurrentProfileProvider = mock()

    private val shortTaskRepository: ShortTaskRepository = mock()

    private lateinit var actionTester: RxObserver<BaseGameAction>

    private val connection = KLTBConnectionBuilder.createAndroidLess()
        .withMac(MAC)
        .build()

    override fun setup() {
        super.setup()
        TrustedClock.setFixedDate()
        whenever(useCase.angleAndSpeedFlowable).thenReturn(feedbackStream)
        whenever(facade.gameLifeCycleObservable()).thenReturn(gameLifecycleStream)
        whenever(facade.onGameFinished()).thenReturn(Completable.complete())
        whenever(lostConnectionHandler.connectionObservable(MAC)).thenReturn(lostConnectionStream)
    }

    @Test
    fun `game starts once vibrator starts for the first time`() {
        val viewModel = createViewModel()
        val liveDataTester = viewModel.testLiveData()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        with(liveDataTester) {
            isWaitingForStart.assertValue(true)
            isPaused.assertValue(false)
            zoneData.assertValue(ZoneProgressData.create(NUMBER_OF_ONGOING_STAGES))
            feedback.assertValue(MindYourSpeedFeedback.EMPTY_FEEDBACK.asResource())
        }

        viewModel.onVibratorOn(connection)

        liveDataTester.isWaitingForStart.assertValue(false)
    }

    @Test
    fun `game is paused if vibration is turned off`() {
        val viewModel = createViewModel()
        val liveDataTester = viewModel.testLiveData()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.onVibratorOff(connection)

        liveDataTester.isPaused.assertValue(true)
        verify(eventTracker).sendEvent(MindYourSpeedAnalytics.pause())

        viewModel.onVibratorOn(connection)

        liveDataTester.isPaused.assertValue(false)
        verify(eventTracker).sendEvent(MindYourSpeedAnalytics.resume())
    }

    @Test
    fun `game finishes gracefully once all 3 stages goes to 100%`() {
        val profile = ProfileBuilder.create().build()
        whenever(currentProfileProvider.currentProfileSingle()).thenReturn(Single.just(profile))
        whenever(shortTaskRepository.createShortTask(profile.id, ShortTask.MIND_YOUR_SPEED))
            .thenReturn(Completable.complete())

        val viewModel = createViewModel()
        val liveDataTester = viewModel.testLiveData()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        emitFeedback(feedback(), afterSeconds = 1)
        assertEquals(0, liveDataTester.progressPercentOnStage(Stage.STAGE_1))

        emitFeedback(feedback(), afterSeconds = 1)
        assertEquals(10, liveDataTester.progressPercentOnStage(Stage.STAGE_1))

        emitFeedback(feedback(), afterSeconds = 9)
        assertEquals(100, liveDataTester.progressPercentOnStage(Stage.STAGE_1))
        assertEquals(0, liveDataTester.progressPercentOnStage(Stage.STAGE_2))

        emitFeedback(feedback(), afterSeconds = 1)
        assertEquals(10, liveDataTester.progressPercentOnStage(Stage.STAGE_2))

        emitFeedback(feedback(), afterSeconds = 9)
        assertEquals(100, liveDataTester.progressPercentOnStage(Stage.STAGE_2))
        assertEquals(0, liveDataTester.progressPercentOnStage(Stage.STAGE_3))

        emitFeedback(feedback(), afterSeconds = 1)
        assertEquals(10, liveDataTester.progressPercentOnStage(Stage.STAGE_3))

        emitFeedback(feedback(), afterSeconds = 8)
        assertEquals(90, liveDataTester.progressPercentOnStage(Stage.STAGE_3))

        emitFeedback(feedback(), afterSeconds = 100)
        assertEquals(100, liveDataTester.progressPercentOnStage(Stage.STAGE_3))

        verify(facade).onGameFinished()
        verify(currentProfileProvider).currentProfileSingle()
        verify(shortTaskRepository).createShortTask(profile.id, ShortTask.MIND_YOUR_SPEED)
        verify(eventTracker).sendEvent(MindYourSpeedAnalytics.finishedWithSuccess())
        verify(navigator).finishWithSuccess()
    }

    @Test
    fun `game resumes from where it was before pause`() {
        val viewModel = createViewModel()
        val liveDataTester = viewModel.testLiveData()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        emitFeedback(feedback())
        emitFeedback(feedback(), afterSeconds = 3)
        assertEquals(30, liveDataTester.progressPercentOnStage(Stage.STAGE_1))

        viewModel.onVibratorOff(connection)
        assertEquals(30, liveDataTester.progressPercentOnStage(Stage.STAGE_1))

        emitFeedback(feedback(), afterSeconds = 3)
        assertEquals(30, liveDataTester.progressPercentOnStage(Stage.STAGE_1))

        viewModel.onResumeButtonClick()
        verify(eventTracker).sendEvent(MindYourSpeedAnalytics.resume())
        assertEquals(30, liveDataTester.progressPercentOnStage(Stage.STAGE_1))

        emitFeedback(feedback(), afterSeconds = 3)
        assertEquals(60, liveDataTester.progressPercentOnStage(Stage.STAGE_1))
    }

    @Test
    fun `game restart wipes the progress achieved before pause`() {
        val viewModel = createViewModel(
            initialState = MindYourSpeedViewState.initial()
                .copy(
                    zoneProgressData = ZoneProgressData.create(NUMBER_OF_ONGOING_STAGES)
                        .updateProgressOnZone(0, 1f)
                        .updateProgressOnZone(1, 1f),
                    stage = Stage.STAGE_3
                )
        )
        val liveDataTester = viewModel.testLiveData()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        emitFeedback(feedback(), afterSeconds = 3)
        assertEquals(30, liveDataTester.progressPercentOnStage(Stage.STAGE_3))

        viewModel.onVibratorOff(connection)
        assertEquals(30, liveDataTester.progressPercentOnStage(Stage.STAGE_3))

        viewModel.onRestartButtonClick()

        viewModel.onResumeButtonClick()
        verify(eventTracker).sendEvent(MindYourSpeedAnalytics.restart())
        assertEquals(0, liveDataTester.progressPercentOnStage(Stage.STAGE_1))
        assertEquals(0, liveDataTester.progressPercentOnStage(Stage.STAGE_2))
        assertEquals(0, liveDataTester.progressPercentOnStage(Stage.STAGE_3))

        emitFeedback(feedback(), afterSeconds = 3)
        assertEquals(30, liveDataTester.progressPercentOnStage(Stage.STAGE_1))
    }

    @Test
    fun `feedback message is calculated based on each feedback`() {
        val viewModel = createViewModel()
        val liveDataTester = viewModel.testLiveData()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        with(liveDataTester) {
            isWaitingForStart.assertValue(true)
            isPaused.assertValue(false)
            zoneData.assertValue(ZoneProgressData.create(NUMBER_OF_ONGOING_STAGES))
            feedback.assertValue(MindYourSpeedFeedback.EMPTY_FEEDBACK.asResource())
        }

        emitFeedback(feedback())
        liveDataTester.feedback.assertValue(MindYourSpeedFeedback.EMPTY_FEEDBACK.asResource())

        emitFeedback(feedback(speedFeedback = SpeedFeedback.OVERSPEED), afterSeconds = 1)
        liveDataTester.feedback.assertValue(MindYourSpeedFeedback.TOO_FAST.asResource())

        emitFeedback(feedback(speedFeedback = SpeedFeedback.UNDERSPEED), afterSeconds = 1)
        liveDataTester.feedback.assertValue(MindYourSpeedFeedback.TOO_SLOW.asResource())

        emitFeedback(feedback(isZoneCorrect = false), afterSeconds = 1)
        liveDataTester.feedback.assertValue(MindYourSpeedFeedback.WRONG_ZONE.asResource())
    }

    private fun createViewModel(
        initialState: MindYourSpeedViewState = MindYourSpeedViewState.initial()
    ) = MindYourSpeedViewModel(
        initialState,
        useCase,
        navigator,
        currentProfileProvider,
        shortTaskRepository,
        Optional.of(MAC),
        gameInteractor,
        facade,
        lostConnectionHandler,
        keepScreenOnController
    ).also {
        actionTester = it.actionsObservable.test()
    }

    private fun MindYourSpeedViewModel.testLiveData() = LiveDataTester(
        isWaitingForStart.test(),
        isPaused.test(),
        zoneData.test(),
        feedback.test()
    )

    private fun emitFeedback(feedback: AngleAndSpeedFeedback, afterSeconds: Long = 0) {
        TrustedClock.advanceTimeBy(afterSeconds, ChronoUnit.SECONDS)
        feedbackStream.onNext(feedback)
    }

    private class LiveDataTester(
        val isWaitingForStart: LiveDataObserver<Boolean>,
        val isPaused: LiveDataObserver<Boolean>,
        val zoneData: LiveDataObserver<ZoneProgressData>,
        val feedback: LiveDataObserver<FeedbackMessageResource>
    ) {
        fun progressPercentOnStage(stage: Stage): Int =
            zoneData.value().zones[stage.index].progress.toPercentInt()
    }
}
