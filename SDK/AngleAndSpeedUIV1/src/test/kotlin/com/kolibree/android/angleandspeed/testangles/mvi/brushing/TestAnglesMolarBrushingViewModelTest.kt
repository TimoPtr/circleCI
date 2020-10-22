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
import com.kolibree.android.angleandspeed.testangles.mvi.brushing.molar.TestAnglesMolarBrushingViewModel
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class TestAnglesMolarBrushingViewModelTest :
    BaseTestAnglesBrushingViewModelTest<TestAnglesMolarBrushingViewModel>() {

    override fun createViewModel(): TestAnglesMolarBrushingViewModel {
        return TestAnglesMolarBrushingViewModel(
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
        verify(angleAndSpeedUseCase).setPrescribedZones(TestAnglesPrescribedZones.FOR_MOLARS_STAGE)
    }

    @Test
    fun `moveToTheNextStage sends OpenIncisorBrushing`() {
        val actionListener = viewModel.actionsObservable.test()

        viewModel.moveToTheNextStage()

        actionListener.assertValue(OpenIncisorBrushing)
    }

    @Test
    fun `brushDegrees reacts to roll angle changes`() {
        val brushDegreesTestObserver = viewModel.brushDegrees.test()

        updateStateWithAngles(
            AngleFeedback(
                pitch = 0.0f,
                roll = 0.0f,
                yaw = 0.0f
            )
        )
        updateStateWithAngles(
            AngleFeedback(
                pitch = -45.0f,
                roll = 0.0f,
                yaw = 0.0f
            )
        )
        updateStateWithAngles(
            AngleFeedback(
                pitch = 45.0f,
                roll = 0.0f,
                yaw = 0.0f
            )
        )
        updateStateWithAngles(
            AngleFeedback(
                pitch = 45.0f,
                roll = 0.0f,
                yaw = 45.0f
            )
        )
        updateStateWithAngles(
            AngleFeedback(
                pitch = 45.0f,
                roll = 0.0f,
                yaw = -45.0f
            )
        )
        updateStateWithAngles(
            AngleFeedback(
                pitch = 45.0f,
                roll = 45.0f,
                yaw = 45.0f
            )
        )
        updateStateWithAngles(
            AngleFeedback(
                pitch = 45.0f,
                roll = -45.0f,
                yaw = 45.0f
            )
        )
        updateStateWithAngles(
            AngleFeedback(
                pitch = 45.0f,
                roll = 180.0f,
                yaw = 45.0f
            )
        )

        brushDegreesTestObserver.assertValueHistory(
            null, 180.0f, 180.0f, 180.0f, 180.0f, 180.0f, 135.0f, 135.0f, 0.0f
        )
    }

    @Test
    fun `stateColor reacts to isZoneCorrect and roll angle changes`() {
        val stateColorTestObserver = viewModel.stateColor.test()

        updateStateWithAnglesAndZoneCorrect(
            AngleFeedback(
                pitch = 0.0f,
                roll = 180.0f,
                yaw = 0.0f
            ), true
        )
        updateStateWithAnglesAndZoneCorrect(
            AngleFeedback(
                pitch = 0.0f,
                roll = 180.0f,
                yaw = 0.0f
            ), false
        )
        updateStateWithAnglesAndZoneCorrect(
            AngleFeedback(
                pitch = 0.0f,
                roll = 134.0f,
                yaw = 0.0f
            ), false
        )
        updateStateWithAnglesAndZoneCorrect(
            AngleFeedback(
                pitch = 0.0f,
                roll = 135.0f,
                yaw = 0.0f
            ), false
        )
        updateStateWithAnglesAndZoneCorrect(
            AngleFeedback(
                pitch = 0.0f,
                roll = 226.0f,
                yaw = 0.0f
            ), false
        )
        updateStateWithAnglesAndZoneCorrect(
            AngleFeedback(
                pitch = 0.0f,
                roll = 225.0f,
                yaw = 0.0f
            ), false
        )

        stateColorTestObserver.assertValueHistory(
            R.color.angle_state_incorrect,
            R.color.angle_state_correct,
            R.color.angle_state_neutral,
            R.color.angle_state_incorrect,
            R.color.angle_state_neutral,
            R.color.angle_state_incorrect,
            R.color.angle_state_neutral
        )
    }
}
