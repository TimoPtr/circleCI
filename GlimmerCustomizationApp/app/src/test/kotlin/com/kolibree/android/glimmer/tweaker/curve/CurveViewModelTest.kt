/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.curve

import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.glimmer.tweaker.TweakerSharedViewModel
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeTweaker
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurveSettings
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test

internal class CurveViewModelTest : BaseUnitTest() {

    private val sharedViewModel: TweakerSharedViewModel = mock()

    private val tweaker: BrushingModeTweaker = mock()

    private lateinit var viewModel: CurveViewModel

    override fun setup() {
        super.setup()

        whenever(sharedViewModel.modeTweaker).thenReturn(tweaker)

        viewModel = CurveViewModel(CurveViewState.initial(), sharedViewModel)
    }

    /*
    curveSettingsLiveData
     */

    @Test
    fun `curveSettingsLiveData emits view state's settings`() {
        val expectedSettings = BrushingModeCurveSettings.custom(referenceVoltageMv = 3300)

        val testObserver = viewModel.curveSettingsLiveData.test()
        viewModel.updateViewState { copy(curveSettings = expectedSettings) }

        testObserver.assertValue(expectedSettings)
    }

    /*
    onReferenceVoltageValue
     */

    @Test
    fun `onReferenceVoltageValue updates referenceVoltage`() {
        val expectedValue = 1986

        viewModel.onReferenceVoltageValue(expectedValue)

        assertEquals(expectedValue, viewModel.getViewState()!!.curveSettings.referenceVoltageMv)
    }

    /*
    onDividerValue
     */

    @Test
    fun `onDividerValue updates divider`() {
        val expectedValue = 1987

        viewModel.onDividerValue(expectedValue)

        assertEquals(expectedValue, viewModel.getViewState()!!.curveSettings.divider)
    }

    /*
    onSlope10Value
     */

    @Test
    fun `onSlope10Value updates slope10Value`() {
        val expectedValue = 1988

        viewModel.onSlope10Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.curveSettings.slope10PercentsDutyCycle
        )
    }

    /*
    onSlope20Value
     */

    @Test
    fun `onSlope20Value updates slope20Value`() {
        val expectedValue = 1989

        viewModel.onSlope20Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.curveSettings.slope20PercentsDutyCycle
        )
    }

    /*
    onSlope30Value
     */

    @Test
    fun `onSlope30Value updates slope30Value`() {
        val expectedValue = 1990

        viewModel.onSlope30Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.curveSettings.slope30PercentsDutyCycle
        )
    }

    /*
    onSlope40Value
     */

    @Test
    fun `onSlope40Value updates slope40Value`() {
        val expectedValue = 1991

        viewModel.onSlope40Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.curveSettings.slope40PercentsDutyCycle
        )
    }

    /*
    onSlope50Value
     */

    @Test
    fun `onSlope50Value updates slope50Value`() {
        val expectedValue = 1992

        viewModel.onSlope50Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.curveSettings.slope50PercentsDutyCycle
        )
    }

    /*
    onSlope60Value
     */

    @Test
    fun `onSlope60Value updates slope60Value`() {
        val expectedValue = 1993

        viewModel.onSlope60Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.curveSettings.slope60PercentsDutyCycle
        )
    }

    /*
    onSlope70Value
     */

    @Test
    fun `onSlope70Value updates slope70Value`() {
        val expectedValue = 1994

        viewModel.onSlope70Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.curveSettings.slope70PercentsDutyCycle
        )
    }

    /*
    onSlope80Value
     */

    @Test
    fun `onSlope80Value updates slope80Value`() {
        val expectedValue = 1995

        viewModel.onSlope80Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.curveSettings.slope80PercentsDutyCycle
        )
    }

    /*
    onSlope90Value
     */

    @Test
    fun `onSlope90Value updates slope90Value`() {
        val expectedValue = 1996

        viewModel.onSlope90Value(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.curveSettings.slope90PercentsDutyCycle
        )
    }

    /*
    onStart
     */

    @Test
    fun `onStart pulls the settings then updates the view state`() {
        val expectedCurveId = 3
        whenever(tweaker.getCurveSettings(any())).thenReturn(
            Single.just(BrushingModeCurveSettings.default().copy(curveId = expectedCurveId))
        )

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onStart(mock())

        testObserver.assertLastValueWithPredicate { it.curveSettings.curveId == expectedCurveId }
    }

    /*
    onCurveSelected
     */

    @Test
    fun `onCurveSelected pulls expected settings then updates the view state`() {
        val expectedCurve = BrushingModeCurve.Flat

        whenever(tweaker.getCurveSettings(any())).thenReturn(
            Single.just(BrushingModeCurveSettings.default().copy(curveId = expectedCurve.ordinal))
        )

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onStart(mock())

        testObserver.assertLastValueWithPredicate { it.curveSettings.curveId == expectedCurve.ordinal }
    }

    /*
    onApplyButtonClick
     */

    @Test
    fun `onApplyButtonClick sets curve settings`() {
        val expectedSettings = BrushingModeCurveSettings.default()

        whenever(tweaker.setCurveSettings(expectedSettings)).thenReturn(Completable.complete())
        viewModel.updateViewState { copy(curveSettings = expectedSettings) }

        viewModel.onApplyButtonClick()

        verify(tweaker).setCurveSettings(expectedSettings)
    }

    /*
    onReferenceVoltageValue
     */

    @Test
    fun `onReferenceVoltageValue updates view state with expected settings`() {
        val expectedValue = 1986

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onReferenceVoltageValue(expectedValue)

        testObserver.assertLastValueWithPredicate {
            it.curveSettings.referenceVoltageMv == expectedValue
        }
    }

    /*
    onDividerValue
     */

    @Test
    fun `onDividerValue updates view state with expected settings`() {
        val expectedValue = 1986

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onDividerValue(expectedValue)

        testObserver.assertLastValueWithPredicate { it.curveSettings.divider == expectedValue }
    }

    /*
    onSlope10Value
     */

    @Test
    fun `onSlope10Value updates view state with expected settings`() {
        val expectedValue = 1986

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onSlope10Value(expectedValue)

        testObserver.assertLastValueWithPredicate { it.curveSettings.slope10PercentsDutyCycle == expectedValue }
    }

    /*
    onSlope20Value
     */

    @Test
    fun `onSlope20Value updates view state with expected settings`() {
        val expectedValue = 1986

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onSlope20Value(expectedValue)

        testObserver.assertLastValueWithPredicate { it.curveSettings.slope20PercentsDutyCycle == expectedValue }
    }

    /*
    onSlope30Value
     */

    @Test
    fun `onSlope30Value updates view state with expected settings`() {
        val expectedValue = 1986

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onSlope30Value(expectedValue)

        testObserver.assertLastValueWithPredicate { it.curveSettings.slope30PercentsDutyCycle == expectedValue }
    }

    /*
    onSlope40Value
     */

    @Test
    fun `onSlope40Value updates view state with expected settings`() {
        val expectedValue = 1986

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onSlope40Value(expectedValue)

        testObserver.assertLastValueWithPredicate { it.curveSettings.slope40PercentsDutyCycle == expectedValue }
    }

    /*
    onSlope50Value
     */

    @Test
    fun `onSlope50Value updates view state with expected settings`() {
        val expectedValue = 1986

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onSlope50Value(expectedValue)

        testObserver.assertLastValueWithPredicate { it.curveSettings.slope50PercentsDutyCycle == expectedValue }
    }

    /*
    onSlope60Value
     */

    @Test
    fun `onSlope60Value updates view state with expected settings`() {
        val expectedValue = 1986

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onSlope60Value(expectedValue)

        testObserver.assertLastValueWithPredicate { it.curveSettings.slope60PercentsDutyCycle == expectedValue }
    }

    /*
    onSlope70Value
     */

    @Test
    fun `onSlope70Value updates view state with expected settings`() {
        val expectedValue = 1986

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onSlope70Value(expectedValue)

        testObserver.assertLastValueWithPredicate { it.curveSettings.slope70PercentsDutyCycle == expectedValue }
    }

    /*
    onSlope80Value
     */

    @Test
    fun `onSlope80Value updates view state with expected settings`() {
        val expectedValue = 1986

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onSlope80Value(expectedValue)

        testObserver.assertLastValueWithPredicate { it.curveSettings.slope80PercentsDutyCycle == expectedValue }
    }

    /*
    onSlope90Value
     */

    @Test
    fun `onSlope90Value updates view state with expected settings`() {
        val expectedValue = 1986

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onSlope90Value(expectedValue)

        testObserver.assertLastValueWithPredicate { it.curveSettings.slope90PercentsDutyCycle == expectedValue }
    }
}
