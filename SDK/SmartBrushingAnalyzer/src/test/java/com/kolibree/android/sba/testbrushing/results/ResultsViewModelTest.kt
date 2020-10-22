package com.kolibree.android.sba.testbrushing.results

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.mouthmap.logic.BrushingResults
import com.kolibree.android.sba.testbrushing.TestBrushingNavigator
import com.kolibree.android.sba.testbrushing.base.NoneAction
import com.kolibree.android.sba.testbrushing.base.ShowAnalysisSuccessfulResult
import com.kolibree.android.sba.testbrushing.base.ShowAngleResult
import com.kolibree.android.sba.testbrushing.base.ShowMouthCoverageResult
import com.kolibree.android.sba.testbrushing.base.ShowReadDiagramDialog
import com.kolibree.android.sba.testbrushing.base.ShowSpeedHintDialog
import com.kolibree.android.sba.testbrushing.base.ShowSpeedResult
import com.kolibree.android.sba.testbrushing.base.ToggleJawsView
import com.kolibree.android.sba.testbrushing.results.ResultsViewModel.Companion.ANALYSIS_SUCCESS_POSITION
import com.kolibree.android.sba.testbrushing.results.ResultsViewModel.Companion.ANGLE_POSITION
import com.kolibree.android.sba.testbrushing.results.ResultsViewModel.Companion.MOUTH_COVERAGE_POSITION
import com.kolibree.android.sba.testbrushing.results.ResultsViewModel.Companion.SPEED_POSITION
import com.kolibree.android.sba.testbrushing.results.hint.ResultHintsPreferences
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Test

class ResultsViewModelTest : BaseUnitTest() {

    internal val navigator = mock<TestBrushingNavigator>()
    internal val preferences = mock<ResultHintsPreferences>()
    internal val mouthCoverageDescriptionProvider = mock<MouthCoverageDescriptionProvider>()
    internal val speedDescriptionProvider = mock<SpeedDescriptionProvider>()

    internal lateinit var viewModel: ResultsViewModel

    override fun setup() {
        super.setup()

        viewModel = spy(
            ResultsViewModel(
                navigator,
                BrushingResults(),
                preferences,
                mouthCoverageDescriptionProvider,
                speedDescriptionProvider
            )
        )
    }

    @Test
    fun `resetActionViewState method returns viewState with NoneAction action`() {
        val currentViewState =
            ResultsViewState(action = ShowReadDiagramDialog)
        viewModel.viewState = currentViewState

        val viewState = viewModel.resetActionViewState()

        val expectedViewState = currentViewState.copy(action = NoneAction)
        Assert.assertEquals(expectedViewState, viewState)
    }

    @Test
    fun `initViewState method returns initial viewState`() {
        val coverageDescription = "mouth description"
        val speedDescription = "speed description"
        val isChangeHintVisible = true
        whenever(preferences.isChangeViewHintVisible()).thenReturn(isChangeHintVisible)
        doReturn(coverageDescription).whenever(mouthCoverageDescriptionProvider).description(any())
        doReturn(speedDescription).whenever(speedDescriptionProvider).description(any())
        val expectedViewState = ResultsViewState().copy(
            analysisSuccessfulAlpha = 1f,
            action = ShowAnalysisSuccessfulResult,
            mouthCoverageCardDescription = coverageDescription,
            speedCardDescription = speedDescription,
            isChangeViewHintVisible = isChangeHintVisible
        )

        Assert.assertEquals(expectedViewState, viewModel.initViewState())
    }

    @Test
    fun `userSlideCards to 0 position change viewState analysisSuccessfulAlpha`() {
        val expectedViewState = ResultsViewState().copy(
            analysisSuccessfulAlpha = 1f,
            mouthCoverageAlpha = 0f
        )

        viewModel.userSlideCards(ANALYSIS_SUCCESS_POSITION, 0f)

        Assert.assertEquals(expectedViewState, viewModel.viewState)
    }

    @Test
    fun `userSlideCards to 0 position change viewState mouthCoverageAlpha`() {
        val expectedViewState = ResultsViewState().copy(
            mouthCoverageAlpha = 1f,
            analysisSuccessfulAlpha = 0f
        )

        viewModel.userSlideCards(MOUTH_COVERAGE_POSITION, 0f)

        Assert.assertEquals(expectedViewState, viewModel.viewState)
    }

    @Test
    fun `userClickMouthCoverageHint method emits action ShowReadDiagramDialog`() {
        val expectedViewState = viewModel.viewState.copy(action = ShowReadDiagramDialog)

        viewModel.userClickMouthCoverageHint()

        verify(viewModel).emitState(expectedViewState)
    }

    @Test
    fun `userChangedCard to ANALYSIS_SUCCESS_POSITION emits action ShowAnalysisSuccessfulResult`() {
        val expectedViewState = viewModel.viewState.copy(
            currentIndicator = ANALYSIS_SUCCESS_POSITION,
            action = ShowAnalysisSuccessfulResult
        )

        viewModel.userChangedCard(ANALYSIS_SUCCESS_POSITION)

        verify(viewModel).emitState(expectedViewState)
    }

    @Test
    fun `userChangedCard to MOUTH_COVERAGE_POSITION emits action ShowMouthCoverageResult`() {
        val expectedViewState = viewModel.viewState.copy(
            currentIndicator = MOUTH_COVERAGE_POSITION,
            action = ShowMouthCoverageResult
        )

        viewModel.userChangedCard(MOUTH_COVERAGE_POSITION)

        verify(viewModel).emitState(expectedViewState)
    }

    @Test
    fun `userChangedCard to SPEED_POSITION emits action ShowSpeedResult`() {
        val expectedViewState = viewModel.viewState.copy(
            currentIndicator = SPEED_POSITION,
            action = ShowSpeedResult
        )

        viewModel.userChangedCard(SPEED_POSITION)

        verify(viewModel).emitState(expectedViewState)
    }

    @Test
    fun `userChangedCard to ANGLE_POSITION emits action ShowMouthCoverageResult`() {
        val expectedViewState = viewModel.viewState.copy(
            currentIndicator = ANGLE_POSITION,
            action = ShowAngleResult
        )

        viewModel.userChangedCard(ANGLE_POSITION)

        verify(viewModel).emitState(expectedViewState)
    }

    @Test
    fun `userClickTopScreen invokes preferences removeChangeViewHint() method`() {
        doNothing().whenever(preferences).removeChangeViewHint()

        viewModel.userClickTopScreen()

        verify(preferences).removeChangeViewHint()
    }

    @Test
    fun `userClickTopScreen emits ChangeJawsView action and isChangeViewHintVisible`() {
        val currentViewState = viewModel.viewState.copy(
            action = NoneAction,
            isChangeViewHintVisible = true
        )

        viewModel.userClickTopScreen()

        val expectedViewState = currentViewState.copy(
            action = ToggleJawsView,
            isChangeViewHintVisible = false
        )
        verify(viewModel).emitState(expectedViewState)
    }

    @Test
    fun `userClickSpeedHint emits action ShowSpeedHintDialog`() {
        val currentViewState = viewModel.viewState.copy(action = NoneAction)
        viewModel.viewState = currentViewState

        viewModel.userClickSpeedHint()

        val expectedViewState = currentViewState.copy(action = ShowSpeedHintDialog)
        verify(viewModel).emitState(expectedViewState)
    }

    @Test
    fun `userClickNeedHelp emits action ShowReadDiagramDialog`() {
        val currentViewState = viewModel.viewState.copy(action = NoneAction)
        viewModel.viewState = currentViewState

        viewModel.userClickNeedHelp()

        val expectedViewState = currentViewState.copy(action = ShowReadDiagramDialog)
        verify(viewModel).emitState(expectedViewState)
    }

    @Test
    fun `userClickDoneOnMenu invokes finishScreen() on navigator object`() {
        viewModel.userClickDoneOnMenu()

        verify(navigator).finishScreen()
    }
}
