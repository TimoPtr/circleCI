package com.kolibree.android.sba.testbrushing.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.kolibree.android.sba.testbrushing.base.legacy.LegacyBaseTestBrushingViewModel
import com.kolibree.android.sba.testbrushing.results.hint.ResultHintsPreferences
import javax.inject.Inject

internal class ResultsViewModel(
    private val navigator: TestBrushingNavigator,
    private val brushingResults: BrushingResults,
    private val hintsPreferences: ResultHintsPreferences,
    private val mouthCoverageDescriptionProvider: MouthCoverageDescriptionProvider,
    private val speedDescriptionProvider: SpeedDescriptionProvider
) : LegacyBaseTestBrushingViewModel<ResultsViewState>(ResultsViewState()) {

    override fun resetActionViewState() = viewState.copy(action = NoneAction)

    override fun initViewState(): ResultsViewState {
        val isChangeViewHintVisible = hintsPreferences.isChangeViewHintVisible()
        val mouthCoverageDescription = mouthCoverageDescriptionProvider.description(brushingResults)
        val speedDescription = speedDescriptionProvider.description(brushingResults)
        return refreshViewState(ANALYSIS_SUCCESS_POSITION).copy(action = ShowAnalysisSuccessfulResult,
            isChangeViewHintVisible = isChangeViewHintVisible,
            mouthCoverageCardDescription = mouthCoverageDescription,
            speedCardDescription = speedDescription)
    }

    private fun refreshViewState(position: Int, slideProgress: Float = 0f): ResultsViewState {
        var analysisAlpha = 0f
        var coverageAlpha = 0f
        var speedAlpha = 0f
        var angleAlpha = 0f
        val alphaIn = slideProgress
        val alphaOut = 1f - slideProgress
        when (position) {
            ANALYSIS_SUCCESS_POSITION -> {
                analysisAlpha = alphaOut
                coverageAlpha = alphaIn
            }
            MOUTH_COVERAGE_POSITION -> {
                coverageAlpha = alphaOut
                speedAlpha = alphaIn
            }
            SPEED_POSITION -> {
                speedAlpha = alphaOut
                angleAlpha = alphaIn
            }
            ANGLE_POSITION -> {
                angleAlpha = 1f
            }
        }

        return viewState.copy(analysisSuccessfulAlpha = analysisAlpha,
            mouthCoverageAlpha = coverageAlpha,
            speedAlpha = speedAlpha,
            angleAlpha = angleAlpha,
            results = brushingResults)
    }

    fun userSlideCards(currentCard: Int, slideProgress: Float) {
        emitState(refreshViewState(currentCard, slideProgress))
    }

    fun userClickMouthCoverageHint() {
        emitState(viewState.copy(action = ShowReadDiagramDialog))
    }

    fun userClickSpeedHint() {
        emitState(viewState.copy(action = ShowSpeedHintDialog))
    }

    fun userClickNeedHelp() {
        emitState(viewState.copy(action = ShowReadDiagramDialog))
    }

    fun userClickDoneOnMenu() {
        navigator.finishScreen()
    }

    fun userChangedCard(position: Int) {
        val action = when (position) {
            MOUTH_COVERAGE_POSITION -> ShowMouthCoverageResult
            SPEED_POSITION -> ShowSpeedResult
            ANGLE_POSITION -> ShowAngleResult
            else -> ShowAnalysisSuccessfulResult
        }
        emitState(viewState.copy(action = action, currentIndicator = position))
    }

    fun userClickTopScreen() {
        hintsPreferences.removeChangeViewHint()
        emitState(viewState.copy(action = ToggleJawsView, isChangeViewHintVisible = false))
    }

    companion object {
        const val ANALYSIS_SUCCESS_POSITION = 0
        const val MOUTH_COVERAGE_POSITION = 1
        const val SPEED_POSITION = 2
        const val ANGLE_POSITION = 3
    }

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(
        private val navigator: TestBrushingNavigator,
        private val brushingResults: BrushingResults,
        private val hitsPreferences: ResultHintsPreferences,
        private val mouthCoverageDescriptionProvider: MouthCoverageDescriptionProvider,
        private val speedDescriptionProvider: SpeedDescriptionProvider
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ResultsViewModel(navigator,
                brushingResults,
                hitsPreferences,
                mouthCoverageDescriptionProvider,
                speedDescriptionProvider) as T
        }
    }
}
