/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.led.mode

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.glimmer.tweaker.TweakerSharedViewModel
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.Duration

internal class ModeLedViewModelTest : BaseUnitTest() {

    private val sharedViewModel: TweakerSharedViewModel = mock()

    private val connection = KLTBConnectionBuilder.createAndroidLess().build()

    private lateinit var viewModel: ModeLedViewModel

    override fun setup() {
        super.setup()
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val toothbrush = mock<Toothbrush>()
        whenever(connection.toothbrush()).thenReturn(toothbrush)

        whenever(sharedViewModel.connection).thenReturn(connection)

        viewModel = ModeLedViewModel(
            initialViewState = ModeLedViewState.initial(),
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
    fun `onPlayButtonClick plays mode LEDs pattern`() {
        val expectedPwm1 = 90
        val expectedPwm2 = 0
        val expectedPwm3 = 8
        val expectedPwm4 = 1
        val expectedPwm5 = 1
        val expectedDuration = 500

        whenever(
            connection.toothbrush().playModeLedPattern(
                any(), any(), any(), any(), any(), any()
            )
        ).thenReturn(Completable.complete())

        viewModel.updateViewState {
            copy(
                led1pwm = expectedPwm1,
                led2pwm = expectedPwm2,
                led3pwm = expectedPwm3,
                led4pwm = expectedPwm4,
                led5pwm = expectedPwm5,
                durationMillis = expectedDuration
            )
        }

        viewModel.onPlayButtonClick()

        verify(connection.toothbrush()).playModeLedPattern(
            expectedPwm1,
            expectedPwm2,
            expectedPwm3,
            expectedPwm4,
            expectedPwm5,
            Duration.ofMillis(expectedDuration.toLong())
        )
    }

    @Test
    fun `onPlayButtonClick does nothing when PWM sum is greater than MAX_PWM_SUM`() {
        viewModel.updateViewState {
            copy(
                led1pwm = 60,
                led2pwm = 41
            )
        }

        viewModel.onPlayButtonClick()

        verify(connection.toothbrush(), never())
            .playModeLedPattern(any(), any(), any(), any(), any(), any())
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
    onPwmLed1Value
     */

    @Test
    fun `onPwmLed1Value updates duration value`() {
        val expectedValue = 1986

        viewModel.onPwmLed1Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.led1pwm
        )
    }

    /*
    onPwmLed2Value
     */

    @Test
    fun `onPwmLed2Value updates duration value`() {
        val expectedValue = 1986

        viewModel.onPwmLed2Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.led2pwm
        )
    }

    /*
    onPwmLed3Value
     */

    @Test
    fun `onPwmLed3Value updates duration value`() {
        val expectedValue = 1986

        viewModel.onPwmLed3Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.led3pwm
        )
    }

    /*
    onPwmLed4Value
     */

    @Test
    fun `onPwmLed4Value updates duration value`() {
        val expectedValue = 1986

        viewModel.onPwmLed4Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.led4pwm
        )
    }

    /*
    onPwmLed5Value
     */

    @Test
    fun `onPwmLed5Value updates duration value`() {
        val expectedValue = 1986

        viewModel.onPwmLed5Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.led5pwm
        )
    }
}
