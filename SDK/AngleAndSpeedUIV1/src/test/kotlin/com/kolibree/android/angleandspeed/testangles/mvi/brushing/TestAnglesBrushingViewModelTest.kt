/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushing

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import com.google.common.base.Optional
import com.jraska.livedata.test
import com.kolibree.android.angleandspeed.common.logic.AngleAndSpeedUseCase
import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.android.angleandspeed.common.logic.model.AngleFeedback
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.angleandspeed.common.widget.progressbar.ProgressState
import com.kolibree.android.angleandspeed.testangles.model.ToothSide
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.mvi.VibratorStateChanged
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.test.extensions.advanceTimeBy
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.kml.MouthZone16
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.temporal.ChronoUnit

internal class TestAnglesBrushingViewModelTest :
    BaseTestAnglesBrushingViewModelTest<TestAnglesBrushingViewModelTest.TestAnglesBrushingViewModelUnderTest>() {

    override fun createViewModel(): TestAnglesBrushingViewModelUnderTest {
        return TestAnglesBrushingViewModelUnderTest(
            null,
            MAC_ADDRESS,
            gameInteractor,
            facade,
            PRESCRIBED_ZONES,
            angleAndSpeedUseCase,
            lostConnectionHandler,
            keepScreenOnController
        )
    }

    @Test
    fun `onStart sends prescribed zones to use case`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_CREATE)
        verify(angleAndSpeedUseCase, never()).setPrescribedZones(any())

        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_START)
        verify(angleAndSpeedUseCase).setPrescribedZones(PRESCRIBED_ZONES)
    }

    @Test
    fun `onStop adds clears subscriptions`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_STOP)
        assertEquals(0, viewModel.disposablesSize())
    }

    @Test
    fun `onStop clears lastUpdateTimestamp`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_STOP)
        assertEquals(null, viewModel.getViewState()!!.lastUpdateTimestamp)
    }

    @Test
    fun `onNewFeedback updated live data information`() {
        val durationPercentageTest = viewModel.durationPercentage.test()
        val toothSideTest = viewModel.toothSide.test()
        val isZoneCorrectTest = viewModel.isZoneCorrect.test()

        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        angleAndSpeedTestProcessor.onNext(
            AngleAndSpeedFeedback(
                AngleFeedback(1.0f, 2.0f, 3.0f),
                SpeedFeedback.CORRECT,
                isZoneCorrect = true
            )
        )

        durationPercentageTest.assertValue(0)
        toothSideTest.assertValue(ToothSide.RIGHT)
        isZoneCorrectTest.assertValue(true)

        verify(viewModel, never()).moveToTheNextStage()
    }

    @Test
    fun `onNewFeedback advances duration by 25% if 5s passed between updates`() {
        val durationPercentageTest = viewModel.durationPercentage.test()
        val stateToPush =
            AngleAndSpeedFeedback(
                AngleFeedback(1.0f, 2.0f, 3.0f),
                SpeedFeedback.CORRECT,
                isZoneCorrect = true
            )

        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        angleAndSpeedTestProcessor.onNext(stateToPush)
        durationPercentageTest.assertValue(0)

        TrustedClock.advanceTimeBy(5, ChronoUnit.SECONDS)
        angleAndSpeedTestProcessor.onNext(stateToPush)
        durationPercentageTest.assertValue(25)

        verify(viewModel, never()).moveToTheNextStage()
    }

    @Test
    fun `onNewFeedback invokes moveToTheNextStage if we reached 20s of correct brushing`() {
        val durationPercentageTest = viewModel.durationPercentage.test()
        val stateToPush =
            AngleAndSpeedFeedback(
                AngleFeedback(1.0f, 2.0f, 3.0f),
                SpeedFeedback.CORRECT,
                isZoneCorrect = true
            )

        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        angleAndSpeedTestProcessor.onNext(stateToPush)
        TrustedClock.advanceTimeBy(20, ChronoUnit.SECONDS)
        angleAndSpeedTestProcessor.onNext(stateToPush)

        durationPercentageTest.assertValue(0)
        verify(viewModel).moveToTheNextStage()
    }

    @Test
    fun `onNewFeedback moves to the next stage if state allows it`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
    }

    @Test
    fun `onConnectionEstablished updates view state and facade`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.onConnectionEstablished()

        verify(facade).onConnectionEstablished(gameInteractor.connection!!)

        with(viewModel.getViewState()!!) {
            assertEquals(null, lastUpdateTimestamp)
            assertEquals(true, vibrationOn)
            assertEquals(LostConnectionHandler.State.CONNECTION_ACTIVE, lostConnectionState)
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
    fun `progressState is START when all conditions are met`() {
        val progressStateTest = viewModel.progressState.test()
        updateStateWith(
            isZoneCorrect = true,
            vibrationOn = true,
            lostConnectionState = LostConnectionHandler.State.CONNECTION_ACTIVE
        )
        progressStateTest.assertValue(ProgressState.START)
    }

    @Test
    fun `progressState is PAUSE when connection state is not CONNECTED`() {
        val progressStateTest = viewModel.progressState.test()
        viewModel.updateViewState {
            copy(
                lostConnectionState = LostConnectionHandler.State.CONNECTION_LOST,
                isZoneCorrect = true,
                vibrationOn = true
            )
        }
        progressStateTest.assertValue(ProgressState.PAUSE)

        viewModel.updateViewState {
            copy(
                lostConnectionState = LostConnectionHandler.State.CONNECTING
            )
        }
        progressStateTest.assertValue(ProgressState.PAUSE)
    }

    @Test
    fun `progressState is PAUSE when isZoneCorrect is false or vibrationOn is false`() {
        val progressStateTest = viewModel.progressState.test()
        updateStateWith(
            isZoneCorrect = true, vibrationOn = false,
            lostConnectionState = LostConnectionHandler.State.CONNECTION_ACTIVE
        )
        progressStateTest.assertValue(ProgressState.PAUSE)

        updateStateWith(
            isZoneCorrect = false, vibrationOn = true,
            lostConnectionState = LostConnectionHandler.State.CONNECTION_ACTIVE
        )
        progressStateTest.assertValue(ProgressState.PAUSE)
    }

    internal class TestAnglesBrushingViewModelUnderTest(
        initialViewState: TestAnglesBrushingViewState?,
        macAddress: String,
        gameInteractor: GameInteractor,
        facade: GameToothbrushInteractorFacade,
        prescribedZones: Array<MouthZone16>,
        angleAndSpeedUseCase: AngleAndSpeedUseCase,
        lostConnectionHandler: LostConnectionHandler,
        keepScreenOnController: KeepScreenOnController
    ) : TestAnglesBrushingViewModel(
        initialViewState,
        Optional.of(macAddress),
        gameInteractor,
        facade,
        angleAndSpeedUseCase,
        prescribedZones,
        lostConnectionHandler,
        keepScreenOnController
    ) {
        override val brushDegrees: LiveData<Float> = map(viewStateLiveData) { 0.0f }

        override val stateColor: LiveData<Int> = map(viewStateLiveData) { 0 }

        override fun moveToTheNextStage() {
            // no-op
        }

        fun disposablesSize(): Int = onStopDisposables.compositeDisposable.size()
    }
}
