/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.pattern

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.glimmer.tweaker.TweakerSharedViewModel
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeTweaker
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePatternSettings
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test

class PatternViewModelTest : BaseUnitTest() {

    private val sharedViewModel: TweakerSharedViewModel = mock()

    private val tweaker: BrushingModeTweaker = mock()

    private lateinit var viewModel: PatternViewModel

    override fun setup() {
        super.setup()

        whenever(sharedViewModel.modeTweaker).thenReturn(tweaker)

        viewModel = PatternViewModel(PatternViewState.initial(), sharedViewModel)
    }

    /*
    onStart
     */

    @Test
    fun `onStart pulls the settings then updates the view state`() {
        val expectedPatterId = 3
        whenever(tweaker.getPatternSettings(any())).thenReturn(
            Single.just(BrushingModePatternSettings.default().copy(patternId = expectedPatterId))
        )

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onStart(mock())

        testObserver.assertLastValueWithPredicate { it.settings.patternId == expectedPatterId }
    }

    /*
    onPatternSelected
     */

    @Test
    fun `onPatternSelected pulls expected settings then updates the view state`() {
        val expectedPattern = BrushingModePattern.GumCare

        whenever(tweaker.getPatternSettings(any())).thenReturn(
            Single.just(
                BrushingModePatternSettings.default().copy(patternId = expectedPattern.ordinal)
            )
        )

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onStart(mock())

        testObserver.assertLastValueWithPredicate { it.settings.patternId == expectedPattern.ordinal }
    }

    /*
    onApplyButtonClick
     */

    @Test
    fun `onApplyButtonClick sets curve settings`() {
        val expectedSettings = BrushingModePatternSettings.default()

        whenever(tweaker.setPatternSettings(expectedSettings)).thenReturn(Completable.complete())
        viewModel.updateViewState { copy(settings = expectedSettings) }

        viewModel.onApplyButtonClick()

        verify(tweaker).setPatternSettings(expectedSettings)
    }

    /*
    onPatternFrequency
     */

    @Test
    fun `onPatternFrequency updates and emits new value`() {
        val expectedValue = 1986

        viewModel.onPatternFrequency(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.settings.patternFrequency
        )
    }

    /*
    onDutyStrength1Value
     */

    @Test
    fun `onDutyStrength1Value updates and emits new value`() {
        val expectedValue = 1986

        viewModel.onDutyStrength1Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.settings.strength1DutyCycleHalfPercent
        )
    }

    /*
    onDutyStrength10Value
     */

    @Test
    fun `onDutyStrength10Value updates and emits new value`() {
        val expectedValue = 1986

        viewModel.onDutyStrength10Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.settings.strength10DutyCycleHalfPercent
        )
    }

    /*
    onMinimalDutyCycleHalfPercentValue
     */

    @Test
    fun `onMinimalDutyCycleHalfPercentValue updates and emits new value`() {
        val expectedValue = 1986

        viewModel.onMinimalDutyCycleHalfPercentValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.settings.minimalDutyCycleHalfPercent
        )
    }

    /*
    onOscillatingPeriodTenthSecondValue
     */

    @Test
    fun `onOscillatingPeriodTenthSecondValue updates and emits new value`() {
        val expectedValue = 1986

        viewModel.onOscillatingPeriodTenthSecondValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.settings.oscillatingPeriodTenthSecond
        )
    }

    /*
    onOscillationParam1Value
     */

    @Test
    fun `onOscillationParam1Value updates and emits new value`() {
        val expectedValue = 1986

        viewModel.onOscillationParam1Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.settings.oscillationParam1
        )
    }

    /*
    onOscillationParam2Value
     */

    @Test
    fun `onOscillationParam2Value updates and emits new value`() {
        val expectedValue = 1986

        viewModel.onOscillationParam2Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.settings.oscillationParam2
        )
    }

    /*
    onOscillationParam3Value
     */

    @Test
    fun `onOscillationParam3Value updates and emits new value`() {
        val expectedValue = 1986

        viewModel.onOscillationParam3Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.settings.oscillationParam3
        )
    }
}
