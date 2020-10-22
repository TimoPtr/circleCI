package com.kolibree.android.sba.testbrushing.progress

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sba.testbrushing.TestBrushingNavigator
import com.kolibree.android.sba.testbrushing.base.FadeInAction
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TestBrushProgressViewModelTest : BaseUnitTest() {

    private val navigator = mock<TestBrushingNavigator>()
    private val testBrushProgressController = mock<TestBrushProgressController>()
    private lateinit var viewModel: TestBrushProgressViewModel
    private val tickDelay = 1

    override fun setup() {
        super.setup()

        whenever(testBrushProgressController.controllerObservable()).doReturn(Observable.just(42L))
        viewModel = spy(TestBrushProgressViewModel(testBrushProgressController, navigator, ToothbrushModel.CONNECT_M1))
    }

    @Test
    fun onTickBrushingDurationAfter1Sec() {
        val sec = 1 * tickDelay

        tick(sec)
        val expected = TestBrushProgressViewState(isProgressStep1Completed = true, action = FadeInAction(sec))
        verifyContentState(expected, sec)
    }

    @Test
    fun onTickMouthCoverageAfter2Secs() {
        val sec = 2 * tickDelay

        tick(sec)
        val expected = TestBrushProgressViewState(isProgressStep1Completed = true,
            isProgressStep2Completed = true,
            action = FadeInAction(sec)
        )
        verifyContentState(expected, sec)
    }

    @Test
    fun onTickAngleAnalysisAfter3sec() {
        val sec = 3 * tickDelay

        tick(sec)
        val expected = TestBrushProgressViewState(isProgressStep1Completed = true,
            isProgressStep2Completed = true,
            isProgressStep3Completed = true,
            action = FadeInAction(sec)
        )
        verifyContentState(expected, sec)
    }

    @Test
    fun onTickBrushingMovements() {
        val sec = 4 * tickDelay

        tick(sec)
        val expected = TestBrushProgressViewState(isProgressStep1Completed = true,
            isProgressStep2Completed = true,
            isProgressStep3Completed = true,
            isProgressStep4Completed = true,
            action = FadeInAction(sec)
        )
        verifyContentState(expected, sec)
    }

    @Test
    fun tickCompleteMoveNextPage() {
        val sec = 5 * tickDelay

        doNothing().whenever(navigator).navigateToResultsScreen()
        tick(sec)
        verify(navigator).navigateToResultsScreen()
        verify(viewModel).disposeTicker()
    }

    @Test
    fun `initViewState hasBuildUpStep returns true for Plaqless toothbrush`() {
        viewModel = TestBrushProgressViewModel(testBrushProgressController, navigator, ToothbrushModel.PLAQLESS)

        val viewState = viewModel.initViewState()

        assertTrue(viewState.hasBuildUpStep)
    }

    @Test
    fun `initViewState hasBuildUpStep returns false for not Plaqless toothbrush`() {
        val viewState = viewModel.initViewState()

        assertFalse(viewState.hasBuildUpStep)
    }

    private fun verifyContentState(expected: TestBrushProgressViewState, sec: Int) {
        assertEquals(expected.isProgressStep1Completed, viewModel.viewState.isProgressStep1Completed)
        assertEquals(expected.isProgressStep2Completed, viewModel.viewState.isProgressStep2Completed)
        assertEquals(expected.isProgressStep3Completed, viewModel.viewState.isProgressStep3Completed)
        assertEquals(expected.isProgressStep4Completed, viewModel.viewState.isProgressStep4Completed)
        assertTrue(viewModel.viewState.action is FadeInAction)
        assertEquals(sec, (viewModel.viewState.action as FadeInAction).secOffset)
    }

    private fun tick(count: Int) {

        for (i in 0L..count) {
            viewModel.onTick(i)
        }
    }
}
