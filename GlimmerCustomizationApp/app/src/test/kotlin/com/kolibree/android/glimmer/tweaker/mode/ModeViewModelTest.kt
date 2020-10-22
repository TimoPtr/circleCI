/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.mode

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.glimmer.tweaker.TweakerSharedViewModel
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeSegment
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeSettings
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeTweaker
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test

class ModeViewModelTest : BaseUnitTest() {

    private val sharedViewModel: TweakerSharedViewModel = mock()

    private val tweaker: BrushingModeTweaker = mock()

    private lateinit var viewModel: ModeViewModel

    override fun setup() {
        super.setup()

        whenever(sharedViewModel.modeTweaker).thenReturn(tweaker)

        viewModel = ModeViewModel(ModeViewState.initial(), sharedViewModel)
    }

    /*
    onStart
     */

    @Test
    fun `onStart pulls the settings then updates the view state`() {
        val expectedModeId = 3
        whenever(tweaker.getBrushingModeSettings(any())).thenReturn(
            Single.just(BrushingModeSettings.default().copy(brushingModeId = expectedModeId))
        )

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onStart(mock())

        testObserver.assertLastValueWithPredicate { it.selectedMode.ordinal == expectedModeId }
    }

    /*
    onModeSelected
     */

    @Test
    fun `onModeSelected pulls expected settings then updates the view state`() {
        val expectedMode = BrushingMode.Slow

        whenever(tweaker.getBrushingModeSettings(any())).thenReturn(
            Single.just(BrushingModeSettings.default().copy(brushingModeId = expectedMode.ordinal))
        )

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onStart(mock())

        testObserver.assertLastValueWithPredicate { it.selectedMode == expectedMode }
    }

    /*
    onApplyButtonClick
     */

    @Test
    fun `onApplyButtonClick sets mode settings`() {
        val expectedSettings = BrushingModeSettings.default().copy(
            segments = listOf(
                BrushingModeSegment.default()
            )
        )

        whenever(tweaker.setCustomBrushingModeSettings(any()))
            .thenReturn(Completable.complete())
        whenever(tweaker.getBrushingModeSettings(any())).thenReturn(
            Single.just(expectedSettings)
        )

        viewModel.updateViewState { ModeViewState.withSettings(expectedSettings) }

        viewModel.onApplyButtonClick()

        verify(tweaker).setCustomBrushingModeSettings(expectedSettings)
    }

    /*
    onAddButtonClick
     */

    @Test
    fun `onAddButtonClick increments enabledSegmentCount`() {
        val expectedEnabledSegmentCount = 3
        viewModel.updateViewState { copy(enabledSegmentCount = expectedEnabledSegmentCount) }

        viewModel.onAddButtonClick()

        assertEquals(
            expectedEnabledSegmentCount + 1,
            viewModel.getViewState()!!.enabledSegmentCount
        )
    }

    /*
    onRemoveButtonClick
     */

    @Test
    fun `onRemoveButtonClick decrements enabledSegmentCount`() {
        val expectedEnabledSegmentCount = 3
        viewModel.updateViewState { copy(enabledSegmentCount = expectedEnabledSegmentCount) }

        viewModel.onRemoveButtonClick()

        assertEquals(
            expectedEnabledSegmentCount - 1,
            viewModel.getViewState()!!.enabledSegmentCount
        )
    }

    /*
    onLastSegmentStrengthValue
     */

    @Test
    fun `onLastSegmentStrengthValue updates last segment's strength`() {
        val expectedValue = 1986

        viewModel.onLastSegmentStrengthValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.lastSegment.strength
        )
    }

    /*
    onSegment8StrengthValue
     */

    @Test
    fun `onSegment8StrengthValue updates 8th segment's strength`() {
        val expectedValue = 1986

        viewModel.onSegment8StrengthValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequenceSegment8.strength
        )
    }

    /*
    onSegment7StrengthValue
     */

    @Test
    fun `onSegment7StrengthValue updates 7th segment's strength`() {
        val expectedValue = 1986

        viewModel.onSegment7StrengthValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequenceSegment7.strength
        )
    }

    /*
    onSegment6StrengthValue
     */

    @Test
    fun `onSegment6StrengthValue updates 6th segment's strength`() {
        val expectedValue = 1986

        viewModel.onSegment6StrengthValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequenceSegment6.strength
        )
    }

    /*
    onSegment5StrengthValue
     */

    @Test
    fun `onSegment5StrengthValue updates 5th segment's strength`() {
        val expectedValue = 1986

        viewModel.onSegment5StrengthValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequenceSegment5.strength
        )
    }

    /*
    onSegment4StrengthValue
     */

    @Test
    fun `onSegment4StrengthValue updates 4th segment's strength`() {
        val expectedValue = 1986

        viewModel.onSegment4StrengthValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequenceSegment4.strength
        )
    }

    /*
    onSegment3StrengthValue
     */

    @Test
    fun `onSegment3StrengthValue updates 3th segment's strength`() {
        val expectedValue = 1986

        viewModel.onSegment3StrengthValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequenceSegment3.strength
        )
    }

    /*
    onSegment2StrengthValue
     */

    @Test
    fun `onSegment2StrengthValue updates 2th segment's strength`() {
        val expectedValue = 1986

        viewModel.onSegment2StrengthValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequenceSegment2.strength
        )
    }

    /*
    onSegment1StrengthValue
     */

    @Test
    fun `onSegment1StrengthValue updates 1th segment's strength`() {
        val expectedValue = 1986

        viewModel.onSegment1StrengthValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequenceSegment1.strength
        )
    }
}
