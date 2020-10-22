/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.sequence

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.glimmer.tweaker.TweakerSharedViewModel
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeTweaker
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequenceSettings
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test

class SequenceViewModelTest : BaseUnitTest() {

    private val sharedViewModel: TweakerSharedViewModel = mock()

    private val tweaker: BrushingModeTweaker = mock()

    private lateinit var viewModel: SequenceViewModel

    override fun setup() {
        super.setup()

        whenever(sharedViewModel.modeTweaker).thenReturn(tweaker)

        viewModel = SequenceViewModel(SequenceViewState.initial(), sharedViewModel)
    }

    /*
    onStart
     */

    @Test
    fun `onStart pulls the settings then updates the view state`() {
        val expectedSequence = BrushingModeSequence.CleanMode

        whenever(tweaker.getSequenceSettings(any())).thenReturn(
            Single.just(
                BrushingModeSequenceSettings.default().copy(sequenceId = expectedSequence.bleIndex)
            )
        )

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onStart(mock())

        testObserver.assertLastValueWithPredicate { it.selectedSequence == expectedSequence }
    }

    /*
    onSequenceSelected
     */

    @Test
    fun `onSequenceSelected pulls expected settings then updates the view state`() {
        val expectedSequence = BrushingModeSequence.GumCare

        whenever(tweaker.getSequenceSettings(any())).thenReturn(
            Single.just(
                BrushingModeSequenceSettings.default().copy(sequenceId = expectedSequence.ordinal)
            )
        )

        val testObserver = viewModel.viewStateFlowable.test()
        viewModel.onStart(mock())

        testObserver.assertLastValueWithPredicate { it.selectedSequence == expectedSequence }
    }

    /*
    onApplyButtonClick
     */

    @Test
    fun `onApplyButtonClick sets sequence settings`() {
        val expectedSettings = BrushingModeSequenceSettings.default()

        whenever(tweaker.setSequenceSettings(any()))
            .thenReturn(Completable.complete())
        whenever(tweaker.getSequenceSettings(any())).thenReturn(
            Single.just(expectedSettings)
        )

        viewModel.updateViewState { SequenceViewState.withSettings(expectedSettings) }

        viewModel.onApplyButtonClick()

        verify(tweaker).setSequenceSettings(expectedSettings.patterns)
    }

    /*
    onAddButtonClick
     */

    @Test
    fun `onAddButtonClick increments enabledSegmentCount`() {
        val expectedEnabledSegmentCount = 3
        viewModel.updateViewState { copy(enabledPatternCount = expectedEnabledSegmentCount) }

        viewModel.onAddButtonClick()

        assertEquals(
            expectedEnabledSegmentCount + 1,
            viewModel.getViewState()!!.enabledPatternCount
        )
    }

    /*
    onRemoveButtonClick
     */

    @Test
    fun `onRemoveButtonClick decrements enabledSegmentCount`() {
        val expectedEnabledSegmentCount = 3
        viewModel.updateViewState { copy(enabledPatternCount = expectedEnabledSegmentCount) }

        viewModel.onRemoveButtonClick()

        assertEquals(
            expectedEnabledSegmentCount - 1,
            viewModel.getViewState()!!.enabledPatternCount
        )
    }

    /*
    onSequencePattern1DurationValue
     */

    @Test
    fun `onSequencePattern1DurationValue updates pattern 1's duration value`() {
        val expectedValue = 1986

        viewModel.onSequencePattern1DurationValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequencePattern1.durationSeconds
        )
    }

    /*
    onSequencePattern2DurationValue
     */

    @Test
    fun `onSequencePattern2DurationValue updates pattern 2's duration value`() {
        val expectedValue = 1986

        viewModel.onSequencePattern2DurationValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequencePattern2.durationSeconds
        )
    }

    /*
    onSequencePattern3DurationValue
     */

    @Test
    fun `onSequencePattern3DurationValue updates pattern 3's duration value`() {
        val expectedValue = 1986

        viewModel.onSequencePattern3DurationValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequencePattern3.durationSeconds
        )
    }

    /*
    onSequencePattern4DurationValue
     */

    @Test
    fun `onSequencePattern4DurationValue updates pattern 4's duration value`() {
        val expectedValue = 1986

        viewModel.onSequencePattern4DurationValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequencePattern4.durationSeconds
        )
    }

    /*
    onSequencePattern5DurationValue
     */

    @Test
    fun `onSequencePattern5DurationValue updates pattern 5's duration value`() {
        val expectedValue = 1986

        viewModel.onSequencePattern5DurationValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequencePattern5.durationSeconds
        )
    }

    /*
    onSequencePattern6DurationValue
     */

    @Test
    fun `onSequencePattern6DurationValue updates pattern 6's duration value`() {
        val expectedValue = 1986

        viewModel.onSequencePattern6DurationValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequencePattern6.durationSeconds
        )
    }

    /*
    onSequencePattern7DurationValue
     */

    @Test
    fun `onSequencePattern7DurationValue updates pattern 7's duration value`() {
        val expectedValue = 1986

        viewModel.onSequencePattern7DurationValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequencePattern7.durationSeconds
        )
    }

    /*
    onSequencePattern8DurationValue
     */

    @Test
    fun `onSequencePattern8DurationValue updates pattern 8's duration value`() {
        val expectedValue = 1986

        viewModel.onSequencePattern8DurationValue(expectedValue)

        assertEquals(
            expectedValue,
            viewModel.getViewState()!!.sequencePattern8.durationSeconds
        )
    }
}
