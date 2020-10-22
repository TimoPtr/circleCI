/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.led.signal

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.glimmer.tweaker.TweakerSharedViewModel
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.toothbrush.led.LedPattern
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Assert.assertEquals
import org.junit.Test

internal class LedSignalViewModelTest : BaseUnitTest() {

    private val sharedViewModel: TweakerSharedViewModel = mock()

    private val connection = KLTBConnectionBuilder.createAndroidLess().build()

    private lateinit var viewModel: LedSignalViewModel

    override fun setup() {
        super.setup()
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val toothbrush = mock<Toothbrush>()
        whenever(connection.toothbrush()).thenReturn(toothbrush)

        whenever(sharedViewModel.connection).thenReturn(connection)

        viewModel = LedSignalViewModel(
            initialViewState = LedSignalViewState.initial(),
            sharedViewModel = sharedViewModel
        )
    }

    override fun tearDown() {
        super.tearDown()

        FailEarly.overrideDelegateWith(TestDelegate)
    }

    /*
    onPlayButtonClick
     */

    @Test
    fun `onPlayButtonClick plays LED signal`() {
        val expectedRed = 1
        val expectedGreen = 10
        val expectedBlue = 99
        val expectedPattern = LedPattern.SHORT_PULSE
        val expectedPeriod = 101
        val expectedDuration = 500

        whenever(
            connection.toothbrush().playLedSignal(
                any(), any(), any(), any(), any(), any()
            )
        ).thenReturn(Completable.complete())

        viewModel.updateViewState {
            copy(
                red = expectedRed,
                green = expectedGreen,
                blue = expectedBlue,
                pattern = expectedPattern,
                periodMillis = expectedPeriod,
                durationMillis = expectedDuration
            )
        }

        viewModel.onPlayButtonClick()

        verify(connection.toothbrush()).playLedSignal(
            expectedRed.toByte(),
            expectedGreen.toByte(),
            expectedBlue.toByte(),
            expectedPattern,
            expectedPeriod,
            expectedDuration
        )
    }

    /*
    onDurationValue
     */

    @Test
    fun `onDurationValue updates duration value`() {
        val expectedValue = 1986

        viewModel.onDurationValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.durationMillis
        )
    }

    /*
    onRedValue
     */

    @Test
    fun `onRedValue updates duration value`() {
        val expectedValue = 10

        viewModel.onRedValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.red
        )
    }

    /*
    onGreenValue
     */

    @Test
    fun `onGreenValue updates duration value`() {
        val expectedValue = 11

        viewModel.onGreenValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.green
        )
    }

    /*
    onBlueValue
     */

    @Test
    fun `onBlueValue updates duration value`() {
        val expectedValue = 12

        viewModel.onBlueValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.blue
        )
    }

    /*
    onPeriodValue
     */

    @Test
    fun `onPeriodValue updates duration value`() {
        val expectedValue = 1986

        viewModel.onPeriodValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.periodMillis
        )
    }
}
