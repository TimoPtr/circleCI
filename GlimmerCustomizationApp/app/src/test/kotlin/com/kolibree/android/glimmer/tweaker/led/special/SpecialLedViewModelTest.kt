/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.led.special

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.glimmer.tweaker.TweakerSharedViewModel
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.connection.toothbrush.led.SpecialLed
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

internal class SpecialLedViewModelTest : BaseUnitTest() {

    private val sharedViewModel: TweakerSharedViewModel = mock()

    private val connection = KLTBConnectionBuilder.createAndroidLess().build()

    private lateinit var viewModel: SpecialLedViewModel

    override fun setup() {
        super.setup()
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val toothbrush = mock<Toothbrush>()
        whenever(connection.toothbrush()).thenReturn(toothbrush)

        whenever(sharedViewModel.connection).thenReturn(connection)

        viewModel = SpecialLedViewModel(
            initialViewState = SpecialLedViewState.initial(),
            sharedViewModel = sharedViewModel
        )
    }

    override fun tearDown() {
        super.tearDown()

        FailEarly.overrideDelegateWith(TestDelegate)
    }

    /*
    onStart
    */

    @Test
    fun `onStart pulls the pwm then updates the view state`() {
        val expectedPwm = 10

        whenever(connection.toothbrush().getSpecialLedPwm(any())).thenReturn(
            Single.just(expectedPwm)
        )

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        testObserver.assertLastValueWithPredicate { it.pwm == expectedPwm }
    }

    /*
    onLedSelected
    */

    @Test
    fun `onLedSelected pulls pwm then updates the view state`() {
        val expectedPwm = 10
        val selectedLed = SpecialLed.WarningLed

        whenever(connection.toothbrush().getSpecialLedPwm(selectedLed)).thenReturn(
            Single.just(expectedPwm)
        )

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onLedSelected(selectedLed.ordinal)

        testObserver.assertLastValueWithPredicate { it.led == selectedLed && it.pwm == expectedPwm }
    }

    /*
    onPwmValue
     */
    @Test
    fun `onPwmValue updates viewState`() {
        val expectedPwm = 10

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onPwmValue(expectedPwm)

        testObserver.assertLastValueWithPredicate { it.pwm == expectedPwm }
    }

    /*
    onApplyButtonClick
     */

    @Test
    fun `onApplyButtonClick sets special led pwm`() {
        val selectedLed = SpecialLed.WarningLed
        val expectedPwm = 10

        whenever(
            connection.toothbrush().setSpecialLedPwm(selectedLed, expectedPwm)
        ).thenReturn(Completable.complete())

        viewModel.updateViewState {
            copy(
                led = selectedLed,
                pwm = expectedPwm
            )
        }

        viewModel.onApplyButtonClick()

        verify(connection.toothbrush()).setSpecialLedPwm(selectedLed, expectedPwm)
    }
}
