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
import com.google.common.base.Optional
import com.jraska.livedata.test
import com.kolibree.android.angleandspeed.R
import com.kolibree.android.angleandspeed.common.logic.model.AngleFeedback
import com.kolibree.android.angleandspeed.testangles.model.TestAnglesPrescribedZones
import com.kolibree.android.angleandspeed.testangles.mvi.brushing.incisor.TestAnglesIncisorBrushingViewModel
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Test

internal class TestAnglesIncisorBrushingViewModelTest :
    BaseTestAnglesBrushingViewModelTest<TestAnglesIncisorBrushingViewModel>() {

    override fun createViewModel(): TestAnglesIncisorBrushingViewModel {
        return TestAnglesIncisorBrushingViewModel(
            null,
            Optional.of(MAC_ADDRESS),
            gameInteractor,
            facade,
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
        verify(angleAndSpeedUseCase).setPrescribedZones(TestAnglesPrescribedZones.FOR_INCISORS_STAGE)
    }

    @Test
    fun `moveToTheNextStage turns the vibration off and sends OpenConfirmation`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        whenever(facade.onGameFinished()).thenReturn(Completable.complete())

        val actionListener = viewModel.actionsObservable.test()

        viewModel.moveToTheNextStage()

        verify(connection.vibrator()).off()
        actionListener.assertValue(OpenConfirmation)
    }

    @Test
    fun `moveToTheNextStage sends OpenConfirmation even if facade returned error`() {
        viewModelLifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_RESUME)
        whenever(facade.onGameFinished()).thenReturn(Completable.error(RuntimeException()))

        val actionListener = viewModel.actionsObservable.test()

        viewModel.moveToTheNextStage()

        actionListener.assertValue(OpenConfirmation)
    }

    @Test
    fun `brushDegrees reacts to pitch angle changes`() {
        val brushDegreesTestObserver = viewModel.brushDegrees.test()

        updateStateWithAngles(
            AngleFeedback(
                roll = 0.0f,
                pitch = 0.0f,
                yaw = 0.0f
            )
        )
        updateStateWithAngles(
            AngleFeedback(
                roll = -45.0f,
                pitch = 0.0f,
                yaw = 0.0f
            )
        )
        updateStateWithAngles(
            AngleFeedback(
                roll = 45.0f,
                pitch = 0.0f,
                yaw = 0.0f
            )
        )
        updateStateWithAngles(
            AngleFeedback(
                roll = 45.0f,
                pitch = 0.0f,
                yaw = 45.0f
            )
        )
        updateStateWithAngles(
            AngleFeedback(
                roll = 45.0f,
                pitch = 0.0f,
                yaw = -45.0f
            )
        )
        updateStateWithAngles(
            AngleFeedback(
                roll = 45.0f,
                pitch = 45.0f,
                yaw = 45.0f
            )
        )
        updateStateWithAngles(
            AngleFeedback(
                roll = 45.0f,
                pitch = -45.0f,
                yaw = 45.0f
            )
        )
        updateStateWithAngles(
            AngleFeedback(
                roll = 45.0f,
                pitch = 180.0f,
                yaw = 45.0f
            )
        )

        brushDegreesTestObserver.assertValueHistory(
            null, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 135.0f, 135.0f, 0.0f
        )
    }

    @Test
    fun `stateColor reacts to isZoneCorrect changes`() {
        val stateColorTestObserver = viewModel.stateColor.test()

        updateStateWithZoneCorrect(false)
        updateStateWithZoneCorrect(false)
        updateStateWithZoneCorrect(true)
        updateStateWithZoneCorrect(true)

        stateColorTestObserver.assertValueHistory(
            R.color.angle_state_incorrect, R.color.angle_state_correct
        )
    }

    @Test
    fun `stateText reacts to isZoneCorrect changes`() {
        val stateTextTestObserver = viewModel.stateText.test()

        updateStateWithZoneCorrect(false)
        updateStateWithZoneCorrect(false)
        updateStateWithZoneCorrect(true)
        updateStateWithZoneCorrect(true)

        stateTextTestObserver.assertValueHistory(
            R.string.test_angles_state_incorrect, R.string.test_angles_state_correct
        )
    }
}
