/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.game

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.navigation.HomeNavigator
import com.kolibree.android.app.ui.selecttoothbrush.SelectToothbrushUseCase
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class StartNonUnityGameUseCaseImplTest : BaseUnitTest() {

    private lateinit var useCase: StartNonUnityGameUseCaseImpl

    private val homeNavigator = mock<HomeNavigator>()
    private val selectToothbrushUseCase = mock<SelectToothbrushUseCase>()

    override fun setup() {
        super.setup()
        useCase =
            StartNonUnityGameUseCaseImpl(
                selectToothbrushUseCase,
                homeNavigator
            )
    }

    @Test
    fun `startUserActivity for TestBrushing show TestBrushing`() {
        val mac = "mac:001"
        val model = ToothbrushModel.CONNECT_M1
        useCase.startUserActivity(ActivityGame.TestBrushing, mac, model)

        verify(homeNavigator).showTestBrushing(mac, model)
    }

    @Test
    fun `startUserActivity for TestAngles show TestAngles`() {
        val mac = "mac:002"
        val model = ToothbrushModel.CONNECT_E2
        useCase.startUserActivity(ActivityGame.TestAngles, mac, model)

        verify(homeNavigator).showTestAngles(mac, model)
    }

    @Test
    fun `startUserActivity for SpeedControl show SpeedControl`() {
        val mac = "mac:003"
        val model = ToothbrushModel.CONNECT_B1
        useCase.startUserActivity(ActivityGame.SpeedControl, mac, model)

        verify(homeNavigator).showSpeedControl(mac, model)
    }

    @Test
    fun `startUserActivity for Coach show Coach`() {
        val mac = "mac:004"
        val model = ToothbrushModel.CONNECT_E1
        useCase.startUserActivity(ActivityGame.Coach, mac, model)

        verify(homeNavigator).showCoach(mac)
    }

    @Test
    fun `startUserActivity for CoachPlus show CoachPlus`() {
        val mac = "mac:005"
        val model = ToothbrushModel.CONNECT_E1
        useCase.startUserActivity(ActivityGame.CoachPlus, mac, model)

        verify(homeNavigator).showCoachPlus(mac, model)
    }

    @Test
    fun `startManualModeActivity for Coach show Coach in manual mode`() {
        useCase.startManualModeActivity(ActivityGame.Coach)

        verify(homeNavigator).showCoachInManualMode()
    }

    @Test
    fun `startManualModeActivity for CoachPlus show CoachPlus in manual mode`() {
        useCase.startManualModeActivity(ActivityGame.CoachPlus)

        verify(homeNavigator).showCoachPlusInManualMode()
    }

    @Test
    fun `startManualModeActivity does nothing for no CoachPlus or Coach`() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        useCase.startManualModeActivity(ActivityGame.TestBrushing)
        useCase.startManualModeActivity(ActivityGame.TestAngles)
        useCase.startManualModeActivity(ActivityGame.SpeedControl)
        useCase.startManualModeActivity(ActivityGame.Pirate)
        useCase.startManualModeActivity(ActivityGame.Rabbids)

        verify(homeNavigator, times(0)).showCoachPlusInManualMode()
    }

    @Test
    fun `hasManualMode returns true only or Coach and CoachPlus`() {
        assertTrue(useCase.hasManualMode(ActivityGame.Coach))
        assertTrue(useCase.hasManualMode(ActivityGame.CoachPlus))

        assertFalse(useCase.hasManualMode(ActivityGame.TestAngles))
        assertFalse(useCase.hasManualMode(ActivityGame.TestBrushing))
        assertFalse(useCase.hasManualMode(ActivityGame.SpeedControl))
    }

    @Test
    fun `if connection is not active then start manual mode for Coach+`() {
        whenever(selectToothbrushUseCase.selectToothbrush())
            .thenReturn(Maybe.empty())
        useCase.start(ActivityGame.CoachPlus).test()

        verify(homeNavigator).showCoachPlusInManualMode()
        verifyNoMoreInteractions(homeNavigator)
    }

    @Test
    fun `if connection is not active and manual mode is not allowed then show no toothbrush error`() {
        whenever(selectToothbrushUseCase.selectToothbrush())
            .thenReturn(Maybe.empty())
        useCase.start(ActivityGame.CoachPlus, allowManualMode = false).test()

        verify(homeNavigator, never()).showCoachPlusInManualMode()
        verify(homeNavigator).showNoToothbrushDialog()
        verifyNoMoreInteractions(homeNavigator)
    }

    @Test
    fun `validateAndStart start if there is no mandatory update`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        useCase.validateAndStart(ActivityGame.Coach, connection.toothbrush()).test()

        verify(homeNavigator).showCoach(connection.toothbrush().mac)
    }

    @Test
    fun `validateAndStart show update needed if mandatory update`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().withBootloader(true).build()
        useCase.validateAndStart(ActivityGame.Coach, connection.toothbrush()).test()

        verify(homeNavigator).showMandatoryToothbrushUpdateDialog(
            connection.toothbrush().mac,
            connection.toothbrush().model
        )
    }

    @Test
    fun `start game if toothbrush available`() {
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        whenever(selectToothbrushUseCase.selectToothbrush()).thenReturn(Maybe.just(connection))

        useCase.start(ActivityGame.Coach).test()

        verify(homeNavigator).showCoach(connection.toothbrush().mac)
        verifyNoMoreInteractions(homeNavigator)
    }
}
